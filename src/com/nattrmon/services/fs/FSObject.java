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
package com.nattrmon.services.fs;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;

public class FSObject extends Object {

	protected String operation;

	public String getOperation() {
		return operation;
	}

	public FSObject(Config conf, Service parentService, ObjectType type,
			String name) {
		super(conf, parentService, type, name);
		operation = name;
	}

	@Override
	public Attribute getNewAttribute(String uid, String name,
			AttributeType type, String value) {
		// TODO Auto-generated method stub
		try {
			return new FSAttribute(conf, this, uid, name, type, value);
		} catch (ExceptionDuplicatedUniqueAttribute e) {
			conf.lOG(OutputType.ERROR, "Trying to add an already existing attribute for uid = '" + uid + "'");
			conf.lOG(OutputType.DEBUG, "ExceptionDuplicatedUniqueAttribute", e);
			return null;
		}
	}

}
