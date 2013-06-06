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
package com.nattrmon.services.beanshell;

import java.util.HashMap;

import bsh.EvalError;
import bsh.Interpreter;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.core.Object.ObjectType;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.services.ops.OpsAttribute;
import com.nattrmon.services.ops.OpsService;

public class BeanshellObject extends Object {
	Interpreter bs = new Interpreter();
	long lastExec = -1;
	
	public BeanshellObject(Config conf, BeanshellService parentService, String name,
			String params) {
		super(conf, parentService, ObjectType.SimpleObject, name);
	}

	@Override
	public Attribute getNewAttribute(String uid, String name,
			AttributeType type, String value) {
		try {
			return new BeanshellAttribute(conf, this, uid, name, type, value);
		} catch (ExceptionDuplicatedUniqueAttribute e) {
			conf.lOG(OutputType.ERROR, "Attribute " + e.getUniqueName() + " already exists.");
			return null;
		}
	}
	
	public boolean needsExecution() {
		return (lastExec != conf.getCurrentCounter());
	}
	
	public void execute() {
		if (needsExecution()) {
			try {
				for(String attrName : conf.getUniqueAttrs().getAttrs().keySet()) {
					if (!(conf.getUniqueAttrs().getAttribute(attrName).isInternal()))
						bs.set(attrName, conf.getUniqueAttrs().getAttribute(attrName).getValue());
				}
				bs.set("__currentCounter", conf.getCurrentCounter());
				bs.set("__cacheCounter", conf.getCache().getNumberOfCurrentCachedObjects());
				bs.set("__intervalTime", conf.getTimeInterval());
				
				bs.eval(name);
			} catch (EvalError e) {
				conf.lOG(OutputType.ERROR, "Beanshell evaluation error");
				conf.lOG(OutputType.DEBUG, "EvalError", e); 
			}
		}
	}
	
	public String getShellVariable(String var) {
		if (needsExecution()) execute();
		try {
			java.lang.Object o = bs.get(var);
			if (o != null) return o.toString();
		} catch (EvalError e) {
			conf.lOG(OutputType.ERROR, "Beanshell evaluation error");
			conf.lOG(OutputType.DEBUG, "EvalError", e);
		}
		
		return "n/a";
	}

}
