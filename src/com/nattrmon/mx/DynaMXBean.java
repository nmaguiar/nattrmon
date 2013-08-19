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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.ImmutableDescriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class DynaMXBean implements DynamicMBean {
	protected OpenMBeanAttributeInfo[] attrInfo;
	protected OpenMBeanConstructorInfo[] attrCons;
	protected OpenMBeanOperationInfo[] attrOps;
	protected MBeanNotificationInfo[] attrNoti;
	
	protected HashMap<String, String> attrValues = new HashMap<String, String>();
	
	public DynaMXBean() {
		String[] attrs = new String[0];
		String[] types = new String[0];
		init(attrs, types);
	}
	
	public DynaMXBean(String[] attrs, String[] types) {
		init(attrs, types);
	}
	
	public void init(String[] attrs, String[] types) {
		int size = attrs.length;
		
		attrInfo = new OpenMBeanAttributeInfo[size];
		attrCons = new OpenMBeanConstructorInfo[0];
		attrOps  = new OpenMBeanOperationInfo[0];
		attrNoti = new MBeanNotificationInfo[0];
		
		int i = 0;
		Descriptor desc;

		for (String attr : attrs) {	
//			desc = null;
			OpenType type = null;
			if (types[i].equals("double"))
				type = SimpleType.DOUBLE;
//				desc = new ImmutableDescriptor( new String[] { "openType", "originalType" }, 
//	                      new String[] { "javax.management.openmbean.SimpleType(name=java.lang.Double)",
//	                                     "double"} );
			if (types[i].equals("long"))
				type = SimpleType.LONG;
//				desc = new ImmutableDescriptor( new String[] { "openType", "originalType" }, 
//	                      new String[] { "javax.management.openmbean.SimpleType(name=java.lang.Long)",
//	                                     "long"} );
//			if (desc == null) 
			if (type == null) 
				type = SimpleType.STRING;
//				desc = new ImmutableDescriptor( new String[] { "openType", "originalType" }, 
//						                      new String[] { "javax.management.openmbean.SimpleType(name=java.lang.String)",
//						                                     "java.lang.String"} );
//			Constructor[] constructors = this.getClass().getConstructors();
//			attrCons[0] = new MBeanConstructorInfo(
//			        "DynaMXBean(): No-parameter constructor",  //description
//			        constructors[0]);                  // the contructor object
			attrInfo[i] = new OpenMBeanAttributeInfoSupport(attr, attr, type, true, false, false);
			//attrCons[i] = null;
			//attrOps[i]  = null;
			//attrNoti[i] = null;
			    
			attrValues.put(attr, "");
			i++;
		}		
	}
	
/*	public Object getAttribute(String attrName) throws AttributeNotFoundException,
			MBeanException, ReflectionException {*/
	public Object getAttribute(String attrName) throws AttributeNotFoundException {
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
//			} catch (MBeanException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ReflectionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
		
		return al;
	}

	public MBeanInfo getMBeanInfo() {
		Descriptor desc = new ImmutableDescriptor( new String[] { "immutableInfo", "mxbean" }, 
                new String[] { "true", "true"} );
		MBeanInfo info = new OpenMBeanInfoSupport(getClass().getName(), "nAttrMon dynamic MX Bean", attrInfo, attrCons, attrOps, attrNoti, desc);
		
		return info;
	}

	public Object invoke(String arg0, Object[] arg1, String[] arg2)
			throws MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return new java.lang.Object();
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
