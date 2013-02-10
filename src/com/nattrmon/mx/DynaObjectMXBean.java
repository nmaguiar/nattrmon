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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

public class DynaObjectMXBean extends DynaMXBean {
	protected Object objRef;
	protected ArrayList<String> validMethods;

	public DynaObjectMXBean(Object obj, String[] methodNames) {
		super();
		objRef = obj;

		init(methodNames);
	}

	public String[] listOfMethods() {
		ArrayList<String> validMethods = new ArrayList<String>();

		Method[] methods = objRef.getClass().getMethods();
		for (Method method : methods) {
			if (method.getParameterTypes().length == 0) {
				if (!(method.getReturnType().toString().equals("void"))) {
					if (method.getReturnType().isPrimitive()) {
						validMethods.add(method.getName());
					} else {
						try {
							method.getReturnType().getMethod("toString");
							validMethods.add(method.getName());
						} catch (SecurityException e) {

						} catch (NoSuchMethodException e) {

						}
					}
				}
			}
		}
		
		String[] mts = new String[validMethods.size()];
		int i = 0;
		for (String m : validMethods) {
			mts[i] = m;
			i++;
		}
		
		return mts;
	}
	
	public Object getAttribute(String attrName) throws AttributeNotFoundException, MBeanException, ReflectionException  {
		if (validMethods.contains(attrName)) {
			
			try {
				return objRef.getClass().getMethod(attrName).invoke(objRef);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
