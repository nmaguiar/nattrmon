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
package com.nattrmon.services.jmx;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.nattrmon.config.Config;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.services.db.DBService;

public class JMXService extends Service {
	protected static HashMap<String, JMXConnector> jmxCons = new HashMap<String, JMXConnector>();
	protected JMXConnector jmxCon = null;
	protected MBeanServerConnection mbeanCon = null;
	protected long timewait = -1;
	protected String provider = "";
	
	public static final java.lang.String CREDENTIALS_LOGIN_KEY = "login";
	public static final java.lang.String CREDENTIALS_PASSWORD_KEY = "password";
	
	public static void register() {
		Config.registerService(JMXService.class.getName(), "service");
	}
	
	public JMXService(Config conf, String serviceUrl, String params) {
		super(conf, ServiceType.NormalService, serviceUrl, params);
	}

	public Object getNewObject(String name, String params) {
		return new JMXObject(conf, this, name, params);
	}

	public synchronized void connect() {
		if (jmxCons.containsKey(this.serviceUrl + params)) {
			jmxCon = jmxCons.get(this.serviceUrl + params);
		}
		
		synchronized (this) {
			if (jmxCon == null) {
				JMXServiceURL serviceUrl = null;
				try {
					serviceUrl = new JMXServiceURL(getServiceUrl());
				} catch (MalformedURLException e) {
					conf.lOG(OutputType.ERROR, "Malformed service url: '" + getServiceUrl() + "'");
					conf.lOG(OutputType.DEBUG, "MalformedURLException", e);
				}
				
				Hashtable<String, Serializable> env = new Hashtable<String, Serializable>();
				
				String tmp = "";
				String prop[];
				String login = "";
				String password = "";
				boolean oldlogin = false;
				
				if (params != null) {
					String pms[] = params.split(";");
					for (String param : pms) {
						tmp = param.trim();
						prop = tmp.split("=");
						if (prop.length == 2) {
							if (prop[0].equalsIgnoreCase("login")) login = prop[1];
							if (prop[0].equalsIgnoreCase("password")) password = prop[1];
							if (prop[0].equalsIgnoreCase("provider")) provider = prop[1];
							if (prop[0].equalsIgnoreCase("oldlogin")) if (prop[1].equalsIgnoreCase("y")) oldlogin = true;
							if (prop[0].equalsIgnoreCase("timewait")) setTimewait(prop[1]);
						}
					}
				}
				
				Hashtable<String, String> credentials= new Hashtable<String, String>();
				
				credentials.put(CREDENTIALS_LOGIN_KEY, login);
				credentials.put(CREDENTIALS_PASSWORD_KEY, password);
				
				if (!(login.equals("")) && !(password.equals(""))) 
				    if (oldlogin) 
				    	env.put(JMXConnector.CREDENTIALS, credentials); 
				    else 
				    	env.put(JMXConnector.CREDENTIALS, new String[] {login, password});
				
				if (!(provider.equals(""))) 
					env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, provider);
	
				try {
					jmxCon = JMXConnectorFactory.connect(serviceUrl, env);
				} catch (IOException e) {
					conf.lOG(OutputType.ERROR, "IO problem while trying to connect to '" + serviceUrl + "'");
					conf.lOG(OutputType.DEBUG, "IOException", e);
				}
				
				if (jmxCon != null) {
					try {
						mbeanCon = jmxCon.getMBeanServerConnection();
					} catch (IOException e) {
						conf.lOG(OutputType.ERROR, "IO problem while trying to connect to '" + serviceUrl + "'");
						conf.lOG(OutputType.DEBUG, "IOException", e);
					}
					
					jmxCons.put(this.serviceUrl + params, jmxCon);
				}
				
			}
		}
	}

	public JMXConnector getJmxCon() {
		return jmxCon;
	}

	public synchronized void setJmxCon(JMXConnector jmxCon) {
		this.jmxCon = jmxCon;
	}

	public MBeanServerConnection getMbeanCon() {
		return mbeanCon;
	}

	public synchronized void setMbeanCon(MBeanServerConnection mbeanCon) {
		this.mbeanCon = mbeanCon;
	}

	public long getTimewait() {
		return timewait;
	}

	public void setTimewait(long timewait) {
		this.timewait = timewait;
	}
	
	public void setTimewait(String tw) {
		try {
			timewait = Long.valueOf(tw);
		} catch (NumberFormatException e) {
			timewait = -1;
		}
	}

	public String getProvider() {
		return provider;
	}
}
