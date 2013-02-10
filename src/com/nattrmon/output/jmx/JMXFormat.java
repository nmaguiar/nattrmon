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
package com.nattrmon.output.jmx;

import java.util.ArrayList;
import java.util.HashMap;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.mx.DynaMXBean;
import com.nattrmon.mx.MXServer;

public class JMXFormat extends OutputFormat {
	protected MXServer mxs;
	protected int port = 9999;
	protected String oname = "com.nattrmon:type=output";
	protected DynaMXBean dbean;
	protected boolean convertToLong = true;
	protected boolean internal = false;
	
	public JMXFormat(Config conf, String params) {
		super(conf, params);
		String tmp, prop[];
		
		if (params != null) {
			String pms[] = params.split(";");
			for (String param : pms) {
				tmp = param.trim();
				prop = tmp.split("=", 2);
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("port")) {
						try {
							port = Integer.valueOf(prop[1]);
						} catch (Exception e) {
						}  
					}
					if (prop[0].equalsIgnoreCase("name")) {
						oname = prop[1];
					}
					if (prop[0].equalsIgnoreCase("convert")) {
						if (prop[1].equals("n")) convertToLong = false; else convertToLong = true; 
					}
					if (prop[0].equalsIgnoreCase("internal")) {
						if (prop[1].equals("y")) internal = true; else internal = false;
					}
				}
			}
		}
		
		mxs = new MXServer(port);
	}

	public static void register() {
		Config.registerFormat(JMXFormat.class.getName(), "jmxOutput");
	}
	
	protected DynaMXBean getAttributesDMXBean(String[] attrs) {
		DynaMXBean db;
		final long counter = conf.getCurrentCounter();
		
		db = new DynaMXBean(attrs) {			
			public Object getAttribute(String attrName) throws AttributeNotFoundException, MBeanException, ReflectionException {
				if (conf.containsCurrentAttributeValues(counter, attrName)) {
					String value = conf.getCurrentAttributeValues(counter, attrName);

					if (convertToLong) {
						try {
							return Long.valueOf(value);
						} catch (Exception e) {
							return value;
						}
					} else {
						return value;
					}
				}
				
				return null;
			}
		};
		
		return db;
	}
	
	protected void setInternalDMXBean() {
		DynaMXBean db;
		
		String attrs[];

		attrs = new String[] { "Count", "OutputType", "TimeInterval", "ObjectsInCache" };
		
		db = new DynaMXBean(attrs) {
			public Object getAttribute(String attrName) throws AttributeNotFoundException, MBeanException, ReflectionException {
				if (attrName.equalsIgnoreCase("Count")) return conf.getCurrentCounter();
				if (attrName.equalsIgnoreCase("OutputType")) return conf.getDefaultType().name();
				if (attrName.equalsIgnoreCase("TimeInterval")) return conf.getTimeInterval();
				if (attrName.equalsIgnoreCase("ObjectsInCache")) return conf.getCache().getNumberOfCurrentCachedObjects();
				
				return null;
			}
		};
		mxs.addDynaBean("com.nattrmon:type=Config", db);
		
	}
	
	@Override
	public void processOutput() {
		ArrayList<String> attrNames = null;
		
		if (dbean == null) {
			//String attrs[] = new String[conf.getCurrentAttributeValues4Counter(conf.getCurrentCounter()).keySet().size()];
			String attrs[] = new String[getAttrNames().size()];
			attrNames = getAttrNames();
			
			int i = 0;
			for (String attr : attrNames) {
				attrs[i] = attr;
				i++;
			}
			
			/*i = 0;
			for(String attr : conf.getCurrentAttributeValues4Counter(conf.getCurrentCounter()).keySet()) {
				attrs[i] = attr;
				i++;
			}*/
			
			dbean = getAttributesDMXBean(attrs);
			mxs.addDynaBean(oname, dbean);
			
			if (internal) setInternalDMXBean();
		}
	}

}
