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
import com.nattrmon.core.Object;

/**
 * Random service. Generates random numbers for testing proposes.<br>
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
 * @see com.nattrmon.services.random.RandomObject
 * @see com.nattrmon.services.random.RandomAttribute
 *
 */
public class RandomService extends com.nattrmon.core.Service {
	
	/**
	 * Registers the service class with URL prefix 'random'
	 * 
	 */
	public static void register() {
		Config.registerService(RandomService.class.getName(), "random");
	}
	
	/**
	 * Creates the RandomService as a ServiceType.SimpleService
	 * 
	 * @param conf The current configuration.
	 * @param aUrl The service url ('random:')
	 * @param aParams The service params (none is used)
	 */
	public RandomService(Config conf, String aUrl, String aParams) {
		super(conf, ServiceType.NormalService, "random", "");
	}

	@Override
	public Object getNewObject(String name, String params) {
		return new RandomObject(conf, this);
	}

}
