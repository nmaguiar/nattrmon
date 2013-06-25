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
package com.nattrmon.services.ops;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;

/**
 * Ops object. Provides the ability to perform operations over existing attributes values
 * producing new attributes.<br>
 * <br>
 * <b>URL prefix</b>: 'ops:'<br>
 * <b>Object types:</b>: simple<br>
 * <b>Attribute types:</b> <i>none</i><br>
 * <br>
 * Example:
 * <br>
 * <pre>
 * &lt;service url="ops:"&gt;
 *  &lt;object name="split"&gt;
 *   &lt;attribute uid="ATTR_1" name="VMSTAT_FREEMEM" value="free memory"/&gt;
 *  &lt;/object&gt;
 *
 *  &lt;object name="sub"&gt;
 *   &lt;attribute uid="ATTR_2" name="TOTALMEM" value="FREEMEM"/&gt;
 *  &lt;/object&gt;
 * &lt;/service&gt;
 * </pre>
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 * @see com.attrmon.services.random.OpsService
 * @see com.attrmon.services.random.OpsAttribute
 */
public class OpsObject extends Object {

	public OpsObject(Config conf, OpsService parentService, String name,
			String params) {
		super(conf, parentService, ObjectType.SimpleObject, name);
	}

	@Override
	public Attribute getNewAttribute(String uid, String name,
			AttributeType type, String value) {
		try {
			return new OpsAttribute(conf, this, uid, name, type, value);
		} catch (ExceptionDuplicatedUniqueAttribute e) {
			conf.lOG(OutputType.ERROR, "Attribute " + e.getUniqueName() + " already exists.");
			return null;
		}
	}

	/**
	 * Performs the corresponding operation to two attributes.
	 * 
	 * @param op1 The first parameter
	 * @param op2 The second parameter
	 * @return The toString() result of the operation
	 */
	public String performOperation(String op1, String op2) {
		if ((op1 == null) || (op2 == null)) return OutputFormat.NOT_AVAILABLE;
		
		if (name.equalsIgnoreCase("split")) return (op1.split(op2))[0]; 
		if (name.equalsIgnoreCase("regextrim")) return (op1.replaceAll(op2, ""));
		if (name.equalsIgnoreCase("match")) return (Pattern.compile(op2).matcher(op1).find()) ? "1" : "0";
		if (name.equalsIgnoreCase("regex")) {
			Matcher m = Pattern.compile(op2).matcher(op1);
			if (m.find()) {
				return m.group();
			} else {
				return OutputFormat.NOT_AVAILABLE;
			} 
		}
		
		long lOp1 = -1;
		long lOp2 = -1;
		try {
			lOp1 = Long.valueOf(op1);
			lOp2 = Long.valueOf(op2);
		} catch (NumberFormatException e) {
		}
		long result = -1;
		
		if (name.equalsIgnoreCase("sub")) result = (lOp1 - lOp2);
		if (name.equalsIgnoreCase("avg")) result = ((lOp1 + lOp2) / 2);
		if (name.equalsIgnoreCase("add")) result = lOp1 + lOp2;
		if (name.equalsIgnoreCase("min")) result = (Math.min(lOp1, lOp2));
		if (name.equalsIgnoreCase("max")) result = (Math.max(lOp1, lOp2));
		if (name.equalsIgnoreCase("mul")) result = (lOp1 * lOp2);
		if (name.equalsIgnoreCase("div")) result = (lOp1 / lOp2);
		
		return Long.toString(result);
	}
}
