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

import com.nattrmon.core.Include;
import com.nattrmon.output.Output.OutputType;

public class XMLSettings {
	protected Config conf = Config.getConfig();
	protected ArrayList<Include> includes = new ArrayList<Include>();

	public ArrayList<Include> getSettings() {
		return includes;
	}
	
	public void addInclude(XMLInclude xinc) {
		if (xinc != null) {
			Include srv = xinc.getInclude();
			if (srv != null) {
				includes.add(srv);
			}
		}
	}
	
	public void setLog(String type) {
		if (type.equalsIgnoreCase("DEBUG")) conf.setDefaultType(OutputType.DEBUG);
		if (type.equalsIgnoreCase("ERROR")) conf.setDefaultType(OutputType.ERROR);
		if (type.equalsIgnoreCase("INFO")) conf.setDefaultType(OutputType.INFO);
	}
	
	public String getLog() {
		return conf.getDefaultType().toString();
	}
	
	public void setCollector(String collect) {
		if (collect.equalsIgnoreCase("EVENT")) conf.setCollector("event");
		if (collect.equalsIgnoreCase("TIMER")) conf.setCollector("timer");
	}
	
	public String getCollector() {
		return conf.getCollector();
	}

}
 