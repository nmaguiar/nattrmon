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

import java.util.HashMap;

/**
 * This class represents and holds the unique attributes for which services will collect values
 * and outputs will output the values
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class UniqueAttributes {
	protected HashMap<String, Attribute> attrs = new HashMap<String, Attribute>();
	
	public HashMap<String, Attribute> getAttrs() {
		return attrs;
	}
	
	public UniqueAttributes() {
		
	}
	
	/**
	 * Adds a new attribute
	 * 
	 * @param uniqueName The unique attribute id
	 * @param attr The attribute
	 * @throws ExceptionDuplicatedUniqueAttribute If the attribute already exists an exception will be thrown
	 */
	public void addAttribute(String uniqueName, Attribute attr) throws ExceptionDuplicatedUniqueAttribute {
		if (!(attrs.containsKey(uniqueName))) {
			attrs.put(uniqueName, attr);
		} else {
			throw new ExceptionDuplicatedUniqueAttribute(uniqueName);
		}
	}
	
	/**
	 * Obtains the stored attribute given it's unique attribute id
	 * 
	 * @param uniqueName Unique attribute id
	 * @return The corresponding attribute or null if not found
	 */
	public Attribute getAttribute(String uniqueName) {
		return attrs.get(uniqueName);
	}
}
