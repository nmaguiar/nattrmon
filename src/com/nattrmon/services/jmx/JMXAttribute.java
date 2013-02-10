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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.output.Output.OutputType;

public class JMXAttribute extends Attribute {
	protected static HashMap<String, Long> attributeCycleRef = new HashMap<String, Long>();
	protected static HashMap<String, String> attributeValue = new HashMap<String, String>();

	public JMXAttribute(Config conf, Object parentObject, String uniqueName,
			String name, AttributeType type, String value)
			throws ExceptionDuplicatedUniqueAttribute {
		super(conf, parentObject, uniqueName, name, type, value);
		setHeavy(true);
	}

	protected String getAttributeValue(java.lang.Object attr) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		if (type == AttributeType.Simple) {
			if (attr != null) {
				return attr.toString();
			}
		} else if (type == AttributeType.Reflect) {
			if (value != null) {
				String[] methods = value.split("::");
				for (String method : methods) {
					if (attr.getClass().equals(javax.management.openmbean.CompositeDataSupport.class)) {
						attr = ((javax.management.openmbean.CompositeDataSupport) attr).get(method);
					} else {
						attr = attr.getClass().getMethod(method, null).invoke(attr, null);
					}
				}
				return attr.toString();
			}
		}
		
		return "n/a";
	}
	
	@Override
	public String getValue() {
		String objectName = "";
		
		/*if (attributeCycleRef.containsKey(uniqueName + value)) {
			if (attributeCycleRef.get(uniqueName + value).compareTo(Long.valueOf(conf.getCurrentCounter())) == 0) {
				return attributeValue.get(uniqueName + value);
			}
		} */
		
		JMXService js = (JMXService) parentObject.getParentService();
		js.connect();
		
		if (js.getMbeanCon() != null) {
			try {
				objectName = ((JMXObject) parentObject).getObjectName().toString();
				
				java.lang.Object attr;
				// WebLogic Bug 13339111
				if (((JMXService) ((JMXObject) parentObject).getParentService()).getProvider().startsWith("weblogic.management")) {
					try {
						attr = ((JMXObject) parentObject).getMbeanCon().getAttribute(((JMXObject) parentObject).getObjectName(), name);
					} catch (java.lang.IllegalStateException e) {
						attr = ((JMXObject) parentObject).getMbeanCon().getAttribute(((JMXObject) parentObject).getObjectName(), name);
						conf.lOG(OutputType.DEBUG, "WebLogic Bug 13339111 Workaround", e);
					}
				} else {
					attr = ((JMXObject) parentObject).getMbeanCon().getAttribute(((JMXObject) parentObject).getObjectName(), name);
				}
				
				return getAttributeValue(attr);
				
			} catch (InstanceNotFoundException e) {
				conf.lOG(OutputType.ERROR, "Can't find attribute instance: '" + objectName + "' with name: '" + name + "'");
				conf.lOG(OutputType.DEBUG, "InstanceNotFoundException", e);
			} catch (MalformedObjectNameException e) {
				conf.lOG(OutputType.ERROR, "Problem with object name: '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "MalformedObjectNameException", e);
			} catch (ReflectionException e) {
				conf.lOG(OutputType.ERROR, "Problem with reflection of attribute '" + name + "' of object '" + objectName + "' with ");
				conf.lOG(OutputType.DEBUG, "ReflectionException", e);
			} catch (NullPointerException e) {
				conf.lOG(OutputType.ERROR, "Problem with attribute '" + name + " of object '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "NullPointerException", e);
			} catch (IOException e) {
				conf.lOG(OutputType.ERROR, "IO problem with attribute '" + name + " of object '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "IOException", e);
			} catch (IllegalArgumentException e) {
				conf.lOG(OutputType.ERROR, "Problem with attribute '" + name + " of object '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "IllegalArgumentException", e);
			} catch (SecurityException e) {
				conf.lOG(OutputType.ERROR, "Security problem with attribute '" + name + " of object '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "SecurityException", e);
			} catch (IllegalAccessException e) {
				conf.lOG(OutputType.ERROR, "Security problem with attribute '" + name + " of object '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "IllegalAccessException", e);
			} catch (InvocationTargetException e) {
				conf.lOG(OutputType.ERROR, "Problem with attribute '" + name + " of object '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "InvocationTargetException", e);
			} catch (NoSuchMethodException e) {
				conf.lOG(OutputType.ERROR, "Problem with attribute '" + name + " of object '" + objectName + "'");
				conf.lOG(OutputType.DEBUG, "NoSuchMethodException", e);
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			conf.lOG(OutputType.ERROR, "Could obtain a jmx connection for attribute '" + name + "' for unique attribute '" + getUniqueName() + "'");
		}
		
		return "n/a";
	}
}
