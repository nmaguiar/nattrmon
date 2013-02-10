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
import com.nattrmon.core.Service;

public class XMLConfiguration {
	protected XMLSettings settings;
	protected XMLServices services;
	protected XMLOutputs outputs;
	
	public XMLConfiguration() {
		services = null;
		outputs = null;
		settings = null;
	}
	
	public void addServices(XMLServices newServices) {
		services = newServices;
	}
	
	public void addOutputs(XMLOutputs newOutputs) {
		outputs = newOutputs;
	}
	
	public void addSettings(XMLSettings newSettings) {
		settings = newSettings;
	}
	
	public XMLServices getServices() {
		return services;
	}
	
	public XMLSettings getSettings() {
		return settings;
	}

	public XMLOutputs getOutputs() { 
		return outputs;
	}
	
	public void config(Config conf) {
		if (services != null) conf.addServices(services.getServices());
		if (outputs != null) conf.addOutputFormats(outputs.getFormats());
		//if (settings != null) conf.addSettings(settings.getSettings());
	}
}
