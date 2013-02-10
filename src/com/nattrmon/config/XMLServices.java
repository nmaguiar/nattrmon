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
import com.nattrmon.output.Output.OutputType;

public class XMLServices {
	protected Config conf = Config.getConfig();
	
	protected ArrayList<Service> services = new ArrayList<Service>();
	
	
	public ArrayList<Service> getServices() {
		return services;
	}

	public void addService(XMLService xsrv) {
		if (xsrv != null) {
			Service srv = xsrv.getService();
			if (srv != null) {
				services.add(srv);
			}
		}
	}
	
//	public void addNewService(String url) {
//		conf.lOG(OutputType.INFO, "addNewService url='" + url + "'");
//		if (url.indexOf(":") > 0) {
//			String protocol = url.substring(0, url.indexOf(":"));
//			Service srv = Config.getNewService(conf, protocol);
//			if (srv != null)
//				services.add(srv);
//		}
//	}
	
	/*
	public void addFormat(Format frt) {
		conf.lOG(OutputType.INFO, "addFormat format='" + frt.toString() + "'");
		formats.add(frt);
	}*/
	
	/*
	public Format addNewFormat(String type) {
		conf.lOG(OutputType.INFO, "addNewFormat type='" + type + "'");
		return Config.getNewFormat(conf, type);
	}*/
}
