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
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.mx.DynaMXBean;
import com.nattrmon.mx.MXServer;

public class JMXFormat extends OutputFormat {
	protected MXServer mxs;
	protected int port = 9999;
	protected String oname = "com.nattrmon:type=Output";
	protected DynaMXBean dbean;
	protected boolean convert = true;
	protected boolean internal = false;
	protected HashMap<String, String> retrievedValues = new HashMap<String, String>();
	protected HashMap<String, String> retrievedTypes = new HashMap<String, String>();
	
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
						if (prop[1].equals("n")) convert = false; else convert = true; 
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
	
	protected DynaMXBean getAttributesDMXBean(String[] attrs, String[] types) {
		DynaMXBean db;

		db = new DynaMXBean(attrs, types) {			
			public Object getAttribute(String attrName) throws AttributeNotFoundException {
//				long counter = conf.getCurrentCounter();
//				if (conf.containsCurrentAttributeValues(counter, attrName)) {
//					String value = conf.getCurrentAttributeValues(counter, attrName);
				String value = retrievedValues.get(attrName);
				
				if (value == null) throw new AttributeNotFoundException();
				
				if (convert) {
					if (retrievedTypes.get(attrName).equals("long")) {
						try {
							return new Long(value);
						} catch (Exception e) {
							return new String(value);
						}
					}
					if (retrievedTypes.get(attrName).equals("double")) {
						try {
							return new Double(value);
						} catch (Exception e) {
							return new String(value);
						}
					}
					return new String(value);
				} else {
					return new String(value);
				}
			}
		};
		
		return db;
	}
	
	protected void setInternalDMXBean() {
		DynaMXBean db;
		
		String attrs[], types[];

		attrs = new String[] { "Count", "OutputType", "TimeInterval", "ObjectsInCache" };
		types = new String[] { "long", "java.lang.String", "long", "int" };
		
		db = new DynaMXBean(attrs, types) {
			public Object getAttribute(String attrName) throws AttributeNotFoundException {
				if (attrName.equalsIgnoreCase("Count")) return new Long(conf.getCurrentCounter());
				if (attrName.equalsIgnoreCase("OutputType")) return conf.getDefaultType().name();
				if (attrName.equalsIgnoreCase("TimeInterval")) return new Long(conf.getTimeInterval());
				if (attrName.equalsIgnoreCase("ObjectsInCache")) return new Integer(conf.getCache().getNumberOfCurrentCachedObjects());
				
				throw new AttributeNotFoundException();
			}
		};
		mxs.addDynaBean("com.nattrmon:type=Config", db);
	}
	
	@Override
	public void processOutput() {
		ArrayList<String> attrNames = null;
		long counter = conf.getCurrentCounter();
		String value = null;
		attrNames = getAttrNames();
		
		if (dbean == null) {
			//String attrs[] = new String[conf.getCurrentAttributeValues4Counter(conf.getCurrentCounter()).keySet().size()];
			String attrs[] = new String[getAttrNames().size()];
			String types[] = new String[getAttrNames().size()];
			
			int i = 0;
			for (String attr : attrNames) {
				if (conf.containsCurrentAttributeValues(counter, attr)) {
					value = conf.getCurrentAttributeValues(counter, attr);
					retrievedValues.put(attr, value);
				} else {
					retrievedValues.put(attr, null);
				}
				
				if ((value != null) && (convert = true)) {
					try {
						Long.valueOf(value);
						types[i] = "long";
						retrievedTypes.put(attr, "long");
					} catch (Exception e) {
						try {
							Double.valueOf(value);
							types[i] = "double";
							retrievedTypes.put(attr, "double");
						} catch (Exception ee) {
							types[i] = "java.lang.String";
							retrievedTypes.put(attr, "java.lang.String");
						}
					}
				} else {
					types[i] = "java.lang.String";
				}
				
				attrs[i] = attr;
				i++;
			}
			
			/*i = 0;
			for(String attr : conf.getCurrentAttributeValues4Counter(conf.getCurrentCounter()).keySet()) {
				attrs[i] = attr;
				i++;
			}*/
			
			dbean = getAttributesDMXBean(attrs, types);
			mxs.addDynaBean(oname, dbean);
			
			if (internal) setInternalDMXBean();
		} else {
			
			for (String attr : attrNames) {
				if (conf.containsCurrentAttributeValues(counter, attr)) {
					value = conf.getCurrentAttributeValues(counter, attr);
					retrievedValues.put(attr, value);
				} else {
					retrievedValues.put(attr, null);
				}
			}
		}
	}

}
