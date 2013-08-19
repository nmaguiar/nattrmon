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
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;

public class LogFormat extends OutputFormat {
	protected String prefix = "";

	public LogFormat(Config conf, String param) {
		super(conf, param);
		if (param != null) {
			String pms[] = param.split(";");
			for (String p : pms) {
				String tmp = p.trim();
				String[] prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("prefix")) prefix = prop[1];
				}
			}
		}
		
		showHeader = false;
	}
	
	public static void register() {
		Config.registerFormat(LogFormat.class.getName(), "logOutput");
	}

	@Override
	public void processOutput(long counter) {
		ArrayList<String> attrNames = null;
		
		attrNames = getAttrNames();
		
		//long counter = conf.getCurrentCounter();
		for(String attrName :attrNames) {			
			if (conf.containsCurrentAttributeValues(counter, attrName)) {
				if (!(this.isAttributeValueEqualToLastRun(attrName))) {
					conf.lOG(OutputType.INFO, prefix + "[" + counter + "] " + attrName + "=" + conf.getCurrentAttributeValues(counter, attrName));
				}
			} else {
				conf.lOG(OutputType.ERROR, "Attribute '" + attrName + "' not found.");
			}
		}
	}

}
