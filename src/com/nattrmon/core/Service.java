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
package com.nattrmon.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import com.nattrmon.config.Config;
import com.nattrmon.output.Output.OutputType;

/**
 * This is the base class for services implementing basic functionality.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 */
public abstract class Service {
	public enum ServiceType {
		NormalService,
		InternalService
	}
	protected ServiceType type;
	protected String serviceUrl;
	protected String url;
	protected String params; // for example: login=xx;password=yy;locale=en
	protected ArrayList<Object> objects = new ArrayList<Object>();
	protected Config conf;
	
	public String getParamsString() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
	public HashMap<String, String> getParams() {
		HashMap<String, String> pms = new HashMap<String, String>();
		String paramString = getParamsString();
		String[] params;
		
		if (paramString != null) {
			for (String param : paramString.split(";")) {
				params = param.split("=");
				if ((params != null) && (params.length >= 2)) {
					pms.put(params[0], params[1]);
					conf.lOG(OutputType.DEBUG, "Service param name='" + params[0] + "';value='" + params[1] + "'");
				}
			}
			
		}
		
		return pms;
	}
	
	public ArrayList<Object> getObjects() {
		return objects;
	}

	public void setObjects(ArrayList<Object> objects) {
		this.objects = objects;
	}
	
	public void addObject(Object newObject) {
		if (newObject != null) {
			newObject.setParentService(this);
			objects.add(newObject);
		}
	}
	
	public void addObjects(ArrayList<Object> newObjects) {
		if (newObjects != null) 
			for (Object obj :newObjects) {
				addObject(obj);
			}
	}
	
	public void removeObject(Object delObject) {
		if ( (delObject != null) && (objects.contains(delObject)) ) {
			delObject.setParentService(null);
			objects.remove(delObject);
		}
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public Service(Config conf, ServiceType type, String serviceUrl, String params) {
		this.type = type;
		this.conf = conf;
		setServiceUrl(serviceUrl);
		setParams(params);
		
		conf.lOG(OutputType.DEBUG, "Service url='" + serviceUrl + "'");
	}
	
	public static void register() {
		
	}
	
	public static Service getNewService(Config conf, String protocol, String aUrl, String aParams) {
		String aServiceClass = Config.getRegisteredServices().get(protocol);
		
		if (aServiceClass != null) {
			
				Class<Service> aService;
				try {
					aService = (Class<Service>) Class.forName(aServiceClass);
					Constructor c = aService.getDeclaredConstructor(Config.class, String.class, String.class);
					try {
						return (Service) c.newInstance(conf, aUrl, aParams);
					} catch (IllegalArgumentException e) {
						conf.lOG(OutputType.ERROR, "Initializing service '" + aUrl + "'. Service not recognized.");
						conf.lOG(OutputType.DEBUG, "IllegalArgumentException", e);
					} catch (InstantiationException e) {
						conf.lOG(OutputType.ERROR, "Initializing service '" + aUrl + "'. Service couldn't be instantiated.");
						conf.lOG(OutputType.DEBUG, "InstantiationException", e);
					} catch (IllegalAccessException e) {
						conf.lOG(OutputType.ERROR, "Initializing service '" + aUrl + "'. Security problem.");
						conf.lOG(OutputType.DEBUG, "IllegalAccessException", e);
					} catch (InvocationTargetException e) {
						conf.lOG(OutputType.ERROR, "Initializing service '" + aUrl + "'. Service couldn't be instantiated.");
						conf.lOG(OutputType.DEBUG, "InvocationTargetException", e);
					}
				} catch (ClassNotFoundException e) {
					conf.lOG(OutputType.ERROR, "Initializing service '" + aUrl + "'. Service not found.");
					conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
				} catch (SecurityException e) {
					conf.lOG(OutputType.ERROR, "Initializing service '" + aUrl + "'. Security problem.");
					conf.lOG(OutputType.DEBUG, "SecurityException", e);
				} catch (NoSuchMethodException e) {
					conf.lOG(OutputType.ERROR, "Initializing service '" + aUrl + "'. Service not found.");
					conf.lOG(OutputType.DEBUG, "NoSuchMethodException", e);
				}
		}	
			
		return null;
	}

	public abstract Object getNewObject(String name, String params);

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ServiceType getType() {
		return type;
	}
	
}
