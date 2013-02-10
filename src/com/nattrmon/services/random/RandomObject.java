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
package com.nattrmon.services.random;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Service;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.output.Output.OutputType;

/**
 * Random object. Generates random numbers for testing proposes.<br>
 * <br>
 * <b>URL prefix</b>: 'random:'<br>
 * <b>Object types:</b>: simple<br>
 * <b>Attribute types:</b> <i>none</i><br>
 * <br>
 * Example:
 * <br>
 * <pre>
 * &lt;service url="random://1"&gt;
 *  &lt;object name="random1"&gt;
 *   &lt;attribute uid="ATTR_1" name="random"/&gt;
 *   &lt;attribute uid="ATTR_2" name="random"/&gt;
 *  &lt;/object&gt;
 *
 *  &lt;object name="random2"&gt;
 *   &lt;attribute uid="ATTR_3" name="random"/&gt;
 *  &lt;/object&gt;
 * &lt;/service&gt;
 * </pre>
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 * @see com.nattrmon.services.random.RandomService
 * @see com.nattrmon.services.random.RandomAttribute
 */
public class RandomObject extends com.nattrmon.core.Object {

	/**
	 * Creates a RandomObject as a ObjectType.SimpleObject
	 * 
	 * @param conf The current configuration. 
	 * @param parentService a RandomService that originated this object.
	 */
	public RandomObject(Config conf, Service parentService) {
		super(conf, parentService, ObjectType.SimpleObject, "SimpleObject");
	}

	@Override
	public Attribute getNewAttribute(String uid, String name, AttributeType type, String value) {
		try {
			return new RandomAttribute(conf, this, uid, value);
		} catch (ExceptionDuplicatedUniqueAttribute e) {
			conf.lOG(OutputType.ERROR, "Attribute " + e.getUniqueName() + " already exists.");
			return null;
		}
	}

}
