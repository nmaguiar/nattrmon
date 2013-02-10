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

import java.util.ArrayList;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute.AttributeType;

/**
 * Object abstract class. All Objects must extend this class.
 * 
 * Objects are identified by:
 * <ul>
 * <li>a name - <i>Identifies the object within a service to each it belongs.</i></li>
 * <li>a type - <i>Identified the type of the object.</i></li>
 * </ul>
 * 
 * Objects types:
 * 
 * <i>Note: In some cases the same class of object can have different types and influences
 * how it's handled internally.</i>
 * 
 * <ul>
 * <li>SimpleObject - <i>The basic and simplest form of an Object.</i></li>
 * <li>SystemCommand- <i>An object that will execute system commands.</i></li>
 * <li>SQLQuery - <i>An object that will execute SQL queries on a database.</i></li>
 * <li>JMXObject - <i>An object that will be used to query remote JMX objects.</i></li>
 * </ul>
 * 
 * @see com.nattrmon.core.Service
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public abstract class Object {
	public enum ObjectType {
		JMXObject,
		SQLQuery,
		SystemCommand,
		SimpleObject
	}
	protected ObjectType type;
	protected String name;
	protected ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	protected String params = null; // for example: login=xx;password=yy;locale=en
	protected Config conf;
	protected Service parentService;

	public Service getParentService() {
		return parentService;
	}

	public void setParentService(Service parentService) {
		this.parentService = parentService;
	}

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(Attribute newAttribute) {
		if (newAttribute != null) {
			newAttribute.setParentObject(this);
			attributes.add(newAttribute);			
		}
	}
	
	public void removeAttribute(Attribute delAttribute) {
		if ( (delAttribute != null) && (attributes.contains(delAttribute)) ) {
			delAttribute.setParentObject(null);
			attributes.remove(delAttribute);			
		}

	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * 
	 * @param conf The current configuration object.
	 * @param parentService The Service to which this Object belongs.
	 * @param type The type of this Object.
	 * @param name The name attribute for this Object.
	 */
	public Object(Config conf, Service parentService, ObjectType type, String name) {
		this.conf = conf;
		this.type = type;
		setName(name);
	
		this.parentService = parentService;
	}
	
	/**
	 * Each Object class must implement this method. It's basically a mini factory for 
	 * Attributes for this Object.
	 * 
	 * @param uid The unique identifier of the Attribute.
	 * @param name The name attribute of the Attribute.
	 * @param type The attribute type for this Attribute.
	 * @param value The value attribute for this Attribute.
	 * @return The result is an Attribute.
	 */
	public abstract Attribute getNewAttribute(String uid, String name, AttributeType type, String value);

	public String getParamsString() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

}
