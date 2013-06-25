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

import java.util.HashMap;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import com.nattrmon.core.OutputFormat;

import bsh.EvalError;
import bsh.Interpreter;

public class DynaBeanShellMXBean extends DynaMXBean {
	protected HashMap<String, String> bsScripts;
	protected Interpreter interpreter = new Interpreter();
	
	public DynaBeanShellMXBean(HashMap<String, String> scripts) {
		super();
		String[] keys = { "" };
		String[] types = { "" };
		bsScripts = scripts;
		
		bsScripts.keySet().toArray(keys);
		init(keys, types);
	}
	
	public Object getAttribute(String attrName) throws AttributeNotFoundException  {
		if (bsScripts.containsKey(attrName)) {
			try {
				return interpreter.eval(bsScripts.get(attrName));
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		return OutputFormat.NOT_AVAILABLE;
	}
}
