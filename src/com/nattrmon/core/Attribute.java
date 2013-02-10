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
package com.nattrmon.core;

import com.nattrmon.config.Config;
import com.nattrmon.core.Service.ServiceType;

/**
 * This is the Attribute abstract class. All attributes must extend this class.<br>
 * For each Object it’s possible to define one or more Attributes. This will be the exact
 * information, from each object, that needs to be gather.
 * 
 * Attributes are identified by:<br>
 * <ul>
 * <li>an unique identifier - <i>A alphanumeric identifier unique to each collector.</i></li>
 * <li>a name - <i>Identifies the attribute within the object to each it belongs.</i></li>
 * <li>type - <i>Identifies the type of the attribute. </i></li>
 * <li>value - <i>For some attributes, this affects the final value of the attribute.</i></li>
 * <li>a parent object - <i>The object to which an attribute belongs.</i></li> 
 * </ul>
 * <br>
 * Attribute types:<br>
 * <i>Note: In some cases the same class of attribute can have different types and influences
 * how the attribute value is retrieved.</i><br><br>
 * <ul>
 * <li>Simple - <i>The basic and simplest form of an attribute.</i></li>
 * <li>Reflect - <i>Attributes whose value is obtain through Java reflection.</i></li>
 * <li>Name - <i>Attributes where the value of 'name' should be interpreted as alphanumeric.</i></li>
 * <li>Id - <i>Attributes where the value of 'name' should be interpreted as numeric.</i></li>
 * </ul>
 * <br>
 * @see com.nattrmon.core.Object
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public abstract class Attribute {
	public enum AttributeType {
		Simple,
		Reflect,
		Name,
		Id,
	}
	
	protected AttributeType type;
	protected String value;
	protected String name;
	protected Config conf;
	protected String uniqueName;
	protected Object parentObject;
	
	// Is this attribute internal needing to be processed aftet the other ones
	protected boolean isInternal = false;  
	// Is this an attribute which takes a long time to process
	protected boolean isHeavy = false;     

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getUniqueName() {
		return uniqueName;
	}

	public Object getParentObject() {
		return parentObject;
	}

	public void setParentObject(Object parentObject) {
		this.parentObject = parentObject;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Creates an attribute. 
	 * If the attribute's parent object parent service is an InternalService, this attribute
	 * is classified as internal.
	 * 
	 * @param conf The current configuration object
	 * @param parentObject The Object to which this Attribute belongs.
	 * @param uniqueName The unique name that will identify this Attribute.
	 * @param name The name attribute for this Attribute.
	 * @param type The type of this attribute.
	 * @param value The value of this attribute.
	 * @throws ExceptionDuplicatedUniqueAttribute
	 */
	public Attribute(Config conf, Object parentObject, String uniqueName, String name, AttributeType type, String value) throws ExceptionDuplicatedUniqueAttribute {
		this.conf = conf;
		conf.getUniqueAttrs().addAttribute(uniqueName, this);
		this.uniqueName = uniqueName;
		this.type = type;
		this.parentObject = parentObject;
		this.name = name;
		this.value = value;
		if (parentObject != null) 
			if (parentObject.getParentService() != null)
				if (parentObject.getParentService().getType() == ServiceType.InternalService)
					isInternal = true;
	}

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public void setInternal(boolean isInternal) {
		this.isInternal = isInternal;
	}

	public boolean isHeavy() {
		return isHeavy;
	}

	public void setHeavy(boolean isHeavy) {
		this.isHeavy = isHeavy;
	}
}
