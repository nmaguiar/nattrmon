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

import java.util.ArrayList;

import com.nattrmon.core.Attribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;

public class XMLObject {
	protected String name = null;
	protected String params = null;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected ArrayList<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

	public ArrayList<XMLAttribute> getXMLAttributes() {
		return attributes;
	}
	
	public ArrayList<Attribute> getAttributes(Object parentObject) {
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		
		for(XMLAttribute xattr :getXMLAttributes()) {
			attrs.add(xattr.getAttribute(parentObject));
		}
		
		return attrs;
	}

	public void setXMLAttributes(ArrayList<XMLAttribute> attributes) {
		this.attributes = attributes;
	}
	
	public void addXMLAttribute(XMLAttribute xmlAttr) {
		attributes.add(xmlAttr);
	}

	public Object getObject(Service parentService) {
		Object obj = parentService.getNewObject(name, params);
		if (obj != null) {
			obj.setName(name);
			obj.setParams(params);
			obj.setAttributes(getAttributes(obj));
		}
		
		return obj;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

}
