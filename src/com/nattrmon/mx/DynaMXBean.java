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

import java.util.ArrayList;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

public class DynaMXBean implements DynamicMBean {
	protected MBeanAttributeInfo[] attrInfo;
	protected MBeanConstructorInfo[] attrCons;
	protected MBeanOperationInfo[] attrOps;
	protected MBeanNotificationInfo[] attrNoti;
	
	protected HashMap<String, String> attrValues = new HashMap<String, String>();
	
	public DynaMXBean() {
		String[] attrs = new String[0];
		init(attrs);
	}
	
	public DynaMXBean(String[] attrs) {
		init(attrs);
	}
	
	public void init(String[] attrs) {
		int size = attrs.length;
		
		attrInfo = new MBeanAttributeInfo[size];
		attrCons = new MBeanConstructorInfo[0];
		attrOps  = new MBeanOperationInfo[0];
		attrNoti = new MBeanNotificationInfo[0];
		
		int i = 0;

		for (String attr : attrs) {		
			attrInfo[i] = new MBeanAttributeInfo(attr, attr, "Attribute " + attr, true, false, false);
			//attrCons[i] = null;
			//attrOps[i]  = null;
			//attrNoti[i] = null;
			    
			attrValues.put(attr, "");
			i++;
		}		
	}
	
	public Object getAttribute(String attrName) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		if (attrValues.containsKey(attrName)) {
			return new Attribute(attrName, attrValues.get(attrName));
		} else {
			return new Attribute(attrName, null);
		}
		
		
	}

	public AttributeList getAttributes(String[] attrNames) {
		AttributeList al = new AttributeList();
		
		for(String key : attrNames) {
			try {
				al.add(getAttribute(key));
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return al;
	}

	public MBeanInfo getMBeanInfo() {
		MBeanInfo info = new MBeanInfo(getClass().getName(), "", attrInfo, attrCons, attrOps, attrNoti);
		
		return info;
	}

	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAttribute(Attribute attr) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
		attrValues.put(attr.getName(), (String) attr.getValue());
	}

	public AttributeList setAttributes(AttributeList attrList) {
		ArrayList<String> attrs = new ArrayList<String>();
		
		for(Object attr : attrList) {
			try {
				setAttribute((Attribute) attr);
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAttributeValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			attrs.add(((Attribute) attr).getName());
		}
		
		return getAttributes((String[]) attrs.toArray());
	}

}
