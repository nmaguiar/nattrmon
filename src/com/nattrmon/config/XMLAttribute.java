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
package com.nattrmon.config;

import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Attribute.AttributeType;

/**
 * Creates an attribute based on the XML configuration.
 * 
 * @author nuno@aguiar.name
 *
 */
public class XMLAttribute {
	protected Config conf = Config.getConfig(); 
	protected String uid = null;
	protected AttributeType ATtype = AttributeType.Simple; // By default an attribute is Simple
	protected String type = "simple";
	protected String name = null;
	protected String value = null;
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public AttributeType getATType() {
		return ATtype;
	}
	
	public void setATType(String attype) {
		if (attype.equalsIgnoreCase(AttributeType.Simple.toString())) this.ATtype = AttributeType.Simple;
		if (attype.equalsIgnoreCase(AttributeType.Reflect.toString())) this.ATtype = AttributeType.Reflect;
		if (attype.equalsIgnoreCase(AttributeType.Name.toString())) this.ATtype = AttributeType.Name;
		if (attype.equalsIgnoreCase(AttributeType.Id.toString())) this.ATtype = AttributeType.Id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Attribute getAttribute(Object parentObject) {
		return parentObject.getNewAttribute(uid, name, ATtype, value);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		setATType(type);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
