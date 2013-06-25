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
package com.nattrmon.mx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class MXServer {
	protected MBeanServer mbs;
	protected HashMap<String, ObjectName> objs = new HashMap<String, ObjectName>(); 
	protected JMXConnectorServer cs;
	
	public MXServer(int port) {
		try {
			System.setProperty("java.rmi.server.randomIDs", "true");
			
			LocateRegistry.createRegistry(port);
			mbs = ManagementFactory.getPlatformMBeanServer();
			
			HashMap<String,Object> env = new HashMap<String,Object>();
			//SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
	        //SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
	        //env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
	        //env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
			
			try {
				JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");
				cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
				//cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
				cs.start();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerMBean(Object bean, ObjectName name) {
		try {
			mbs.registerMBean(bean, name);
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerDynaMBean(DynaMXBean bean, ObjectName name) {
		this.registerMBean(bean, name);
	}
	
	public void addBean(String name, Object bean) {
		ObjectName on = null;
		
		if (objs.containsKey(name)) {
			on = objs.get(name);
		} else {
			try {
				on = new ObjectName(name);
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (on != null) {
			registerMBean(bean, on);
		}
	}
	
	public void addDynaBean(String name, DynaMXBean bean) {
		ObjectName on = null;
		
		if (objs.containsKey(name)) {
			on = objs.get(name);
		} else {
			try {
				on = new ObjectName(name);
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (on != null) {
			registerDynaMBean(bean, on);
		}
	}
}
