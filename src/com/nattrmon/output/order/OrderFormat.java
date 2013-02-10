/**       
 *	   Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 */
package com.nattrmon.output.order;

import java.util.ArrayList;
import java.util.Arrays;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.core.UniqueAttributes;
import com.nattrmon.output.Output.OutputType;

public class OrderFormat extends OutputFormat {

	
	public OrderFormat(Config conf, String param) {
		super(conf, param);
		if (param != null) {
			for (String p : Arrays.asList(getParam().split(","))) {
				if (p.equalsIgnoreCase("noheader")) showHeader = false;
			}
		}
	}

	public static void register() {
		Config.registerFormat(OrderFormat.class.getName(), "orderOutput");
	}

	@Override
	public void processOutput() {
		StringBuffer out = new StringBuffer();
		ArrayList<String> attrNames = null;
		
		attrNames = getAttrNames();
		
		String suffix = "; ";
		long counter = conf.getCurrentCounter();
		for(String attrName :attrNames) {
			if (attrNames.indexOf(attrName) == (attrNames.size() - 1)) suffix = ";";
			
			if (conf.containsCurrentAttributeValues(counter, attrName)) {
				if (firstTime && showHeader) {
					out.append(attrName + suffix);
				} else {
					out.append(conf.getCurrentAttributeValues(counter, attrName) + suffix);
				}
			} else {
				conf.lOG(OutputType.ERROR, "Attribute '" + attrName + "' not found.");
			}
		}
		
		conf.lOG(OutputType.INFO, out.toString());
	}
	
}
