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

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;

public class OpsAttribute extends Attribute {

	public OpsAttribute(Config conf, Object parentObject, String uniqueName,
			String name, AttributeType type, String value)
			throws ExceptionDuplicatedUniqueAttribute {
		super(conf, parentObject, uniqueName, name, type, value);
		setInternal(true);
	}

	@Override
	public String getValue() {
		long counter = conf.getCurrentCounter();
		
		Attribute attr1 = conf.getUniqueAttrs().getAttribute(name);
		Attribute attr2 = conf.getUniqueAttrs().getAttribute(value);
		
		if ((attr1 != null) && (attr2 != null)) {
			return "" + ((OpsObject) parentObject).performOperation(conf.getCurrentAttributeValues(counter, attr1.getUniqueName()), conf.getCurrentAttributeValues(counter, attr2.getUniqueName()));
		} else {
			if ((attr1 == null) && (attr2 != null)) 
				return "" + ((OpsObject) parentObject).performOperation(name, conf.getCurrentAttributeValues(counter, attr2.getUniqueName()));
			if ((attr1 != null) && (attr2 == null)) 
				return "" + ((OpsObject) parentObject).performOperation(conf.getCurrentAttributeValues(counter, attr1.getUniqueName()), value);
		}
		return "" + ((OpsObject) parentObject).performOperation(name, value);
	}
}
