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

import com.nattrmon.core.Object;
import com.nattrmon.core.Service;

public class XMLService {
	protected Config conf = Config.getConfig();
	public String url = null;
	public String params = null;
	protected ArrayList<XMLObject> objects = new ArrayList<XMLObject>();


	public ArrayList<XMLObject> getXMLObjects() {
		return objects;
	}
	
	public ArrayList<Object> getObjects(Service parentSrv) {
		ArrayList<Object> objs = new ArrayList<Object>();
		for (XMLObject xobj :objects) {
			objs.add(xobj.getObject(parentSrv));
		}
		return objs;
	}

	public void setObjects(ArrayList<XMLObject> objects) {
		this.objects = objects;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void set(String url) {
		setUrl(url);
	}
	
	public void addObject(XMLObject aObj) {
		objects.add(aObj);
	}
	
	public Service getService() {
		if (url != null && url.indexOf(":") > 0) {
			String protocol = url.substring(0, url.indexOf(":"));
			Service srv = Service.getNewService(conf, protocol, url, params);
			if (srv != null) {
				//srv.setUrl(url);
				//srv.setServiceUrl(url);
				//srv.setParams(params);
				srv.addObjects(getObjects(srv));
				return srv;
			}
		} 
		
		return null;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
}
