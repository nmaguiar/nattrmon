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
package com.nattrmon.config;

import java.util.ArrayList;

import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;

public class XMLOutputs {
	protected Config conf = Config.getConfig();
	
	protected ArrayList<OutputFormat> formats = new ArrayList<OutputFormat>();
	
	public ArrayList<OutputFormat> getFormats() {
		return formats;
	}

	public void setFormats(ArrayList<OutputFormat> formats) {
		this.formats = formats;
	}
	
	public void addOutput(XMLOutput xoutput) {
		if (xoutput != null) {
			OutputFormat of = xoutput.getOutputFormat();
			
			if (of != null) {
				formats.add(of);
			}
		}
	}

//	public void addFormat(OutputFormat frt) {
//		conf.lOG(OutputType.INFO, "addFormat format='" + frt.toString() + "'");
//		formats.add(frt);
//	}
//	
//	public OutputFormat addNewFormat(String type) {
//		conf.lOG(OutputType.INFO, "addNewFormat format='" + type + "'");
//		return Config.getNewFormat(conf, type);
//	}

}
