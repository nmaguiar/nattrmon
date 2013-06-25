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

import java.util.Random;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;

/**
 * Random attribute. Generates random numbers for testing proposes.<br>
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
 * @see com.nattrmon.services.random.RandomObject
 */
public class RandomAttribute extends Attribute {
	protected Random randNum = new Random(); // the internal random generated number
	
	/**
	 * Creates a RandomAttribute based on a RandomObject
	 * 
	 * @param conf The current configuration. 
	 * @param parentObj a RandomObject that originated this object.
	 * @param uniqueName the attribute unique name
	 * @throws ExceptionDuplicatedUniqueAttribute Thrown when creating an object with an existing attribute unique name (uid)
	 */
	public RandomAttribute(Config conf, com.nattrmon.core.Object parentObj, String uniqueName, String value) throws ExceptionDuplicatedUniqueAttribute {
		super(conf, parentObj, uniqueName, "random", AttributeType.Simple, value);
		//setHeavy(true); // for testing
	}
	
	/**
	 * Generates a int random number
	 * 
	 * @return a random integer number
	 */
	@Override
	public String getValue() {
		return String.valueOf(randNum.nextInt());
	}

}
