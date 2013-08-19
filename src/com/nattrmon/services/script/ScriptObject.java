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
package com.nattrmon.services.script;

import java.util.ArrayList;
import java.util.HashMap;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import sun.org.mozilla.javascript.internal.Context;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.core.Object.ObjectType;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.services.ops.OpsAttribute;
import com.nattrmon.services.ops.OpsService;

public class ScriptObject extends Object {
	//Interpreter bs = new Interpreter();
	protected org.mozilla.javascript.Context cx;
	protected static Scriptable globalscope;
	protected Script compiledScript;
	ArrayList<String> attrs = new ArrayList<String>();
	long lastExec = -1;
	
	public ScriptObject(Config conf, ScriptService parentService, String name,
			String params) {
		super(conf, parentService, ObjectType.SimpleObject, name);
		cx = org.mozilla.javascript.Context.enter();
		getGlobalScope();
		cx.setOptimizationLevel(9);
		cx.setLanguageVersion(170);
		cx.setGeneratingSource(false);
		compiledScript = cx.compileString(name, "<cmd>" + name.hashCode(), 1, null);
		org.mozilla.javascript.Context.exit();
	}

	@Override
	public Attribute getNewAttribute(String uid, String name,
			AttributeType type, String value) {
		try {
			attrs.add(uid);
			cx = org.mozilla.javascript.Context.enter();
			ScriptableObject.defineProperty(getGlobalScope(), name, OutputFormat.NOT_AVAILABLE, 0);
			org.mozilla.javascript.Context.exit();
			return new ScriptAttribute(conf, this, uid, name, type, value);
		} catch (ExceptionDuplicatedUniqueAttribute e) {
			conf.lOG(OutputType.ERROR, "Attribute " + e.getUniqueName() + " already exists.");
			return null;
		}
	}
	
	public boolean needsExecution() {
		return (lastExec != conf.getCurrentCounter());
	}
	
	protected Scriptable getGlobalScope() {
		if (globalscope == null) {
			globalscope = cx.initStandardObjects();
		}
		
		return globalscope;
	}
	
	public synchronized void execute() {
		if (needsExecution()) {
			synchronized(this) {
				cx = org.mozilla.javascript.Context.enter();
	
				try {
					for(String attrName : conf.getUniqueAttrs().getAttrs().keySet()) {
						if (!(attrs.contains(attrName)))
							ScriptableObject.putProperty(getGlobalScope(), attrName, conf.getCurrentAttributeValues(conf.getCurrentCounter(), attrName));
					}
					ScriptableObject.putProperty(getGlobalScope(), "__currentCounter", conf.getCurrentCounter());
					ScriptableObject.putProperty(getGlobalScope(), "__cacheCounter", conf.getCache().getNumberOfCurrentCachedObjects());
					ScriptableObject.putProperty(getGlobalScope(), "__intervalTime", conf.getTimeInterval());
				
					//cx.evaluateString(getGlobalScope(), name, "<cmd>" + name.hashCode(), 1, null);
					compiledScript.exec(cx, getGlobalScope());
				} catch (org.mozilla.javascript.EvaluatorException e) {
					conf.lOG(OutputType.ERROR, "Script evaluation error");
					conf.lOG(OutputType.DEBUG, "EvaluatorException", e); 
				} catch (java.lang.IllegalStateException e) {
					conf.lOG(OutputType.ERROR, "Illegal state");
					conf.lOG(OutputType.DEBUG, "IllegalStateException", e); 				
				} finally {
					lastExec = conf.getCurrentCounter(); 
					org.mozilla.javascript.Context.exit();			
				}
			}
		}
	}
	
	public String getShellVariable(String var) {
		String res = OutputFormat.NOT_AVAILABLE;
		
		if (needsExecution()) execute();
		cx = org.mozilla.javascript.Context.enter();
		try {
			java.lang.Object o = getGlobalScope().get(var, globalscope);
			if (o == Scriptable.NOT_FOUND) 
				res = OutputFormat.NOT_AVAILABLE; 
			else 
				res = o.toString();
		} catch (org.mozilla.javascript.EvaluatorException e) {
			conf.lOG(OutputType.ERROR, "Script evaluation error");
			conf.lOG(OutputType.DEBUG, "EvaluatorException", e);
		}
		org.mozilla.javascript.Context.exit();
		
		return res;
	}

}
 