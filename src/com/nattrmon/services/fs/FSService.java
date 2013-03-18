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
package com.nattrmon.services.fs;

import com.nattrmon.config.Config;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.core.Object.ObjectType;
import com.nattrmon.services.jmx.JMXService;

public class FSService extends Service {
	public FSService(Config conf, String aUrl, String aParams) {
		super(conf, ServiceType.NormalService, "fs", aParams);
	}

	@Override
	public Object getNewObject(String name, String params) {
		return new FSObject(conf, this, ObjectType.SimpleObject, name);
	}
	
	public static void register() {
		Config.registerService(FSService.class.getName(), "fs");
	}

}
