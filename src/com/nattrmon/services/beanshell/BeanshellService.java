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
package com.nattrmon.services.beanshell;

import com.nattrmon.config.Config;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;

public class BeanshellService extends Service {

	public BeanshellService(Config conf, String serviceUrl, String params) {
		super(conf, ServiceType.InternalService, serviceUrl, params);
	}

	public static void register() {
		Config.registerService(BeanshellService.class.getName(), "beanshell");
	}
	
	@Override
	public Object getNewObject(String name, String params) {
		return new BeanshellObject(conf, this, name, params);
	}
	

}
