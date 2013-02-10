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
package com.nattrmon.services.internal;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.services.db.CacheableResultSet;

public class InternalStatsObject extends Object {
	protected String cacheKey = String.valueOf(this.name);

	public InternalStatsObject(Config conf, Service parentService, String name, String params) {
		super(conf, parentService, ObjectType.SimpleObject, name);
	}

	@Override
	public Attribute getNewAttribute(String uid, String name, AttributeType type, String value) {
		try {
			return new InternalStatsAttribute(conf, this, uid, name, type, value);
		} catch (ExceptionDuplicatedUniqueAttribute e) {
			conf.lOG(OutputType.ERROR, "Trying to add an already existing attribute for uid = '" + uid + "'");
			conf.lOG(OutputType.DEBUG, "ExceptionDuplicatedUniqueAttribute", e);
			return null;
		}
	}

	protected long getLastTime() {
		if (conf.getCache().isObjectStillValid(cacheKey)) {
			long time;
			CacheableStats outCache = (CacheableStats) conf.getCache().getCachedObject(cacheKey);
			time = System.currentTimeMillis() - outCache.getLasttime() - conf.getTimeInterval();
			conf.getCache().addObjectToCacheWithTimeLimit(cacheKey, new CacheableStats(System.currentTimeMillis()), 2);
			return time;
		} else {
			conf.getCache().addObjectToCacheWithTimeLimit(cacheKey, new CacheableStats(System.currentTimeMillis()), 2);
			return -1;
		}
	}
}
