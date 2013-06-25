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

import java.util.Date;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.OutputFormat;

public class InternalStatsAttribute extends Attribute {

	public InternalStatsAttribute(Config conf, Object parentObject,
			String uniqueName, String name, AttributeType type, String value)
			throws ExceptionDuplicatedUniqueAttribute {
		super(conf, parentObject, uniqueName, name, type, value);
		setInternal(true);
	}

	@Override
	public String getValue() {
		if (name.equalsIgnoreCase("currentTime")) {
		    return Long.toString(System.currentTimeMillis());
		} else if (name.equalsIgnoreCase("currentTimeFormatted")) {
			return (new Date()).toString();
		} else if (name.equalsIgnoreCase("freeMemory")) {
			return Long.toString(Runtime.getRuntime().freeMemory());
		} else if (name.equalsIgnoreCase("totalMemory")) {
			return Long.toString(Runtime.getRuntime().totalMemory());
		} else if (name.equalsIgnoreCase("executionTime")) {
			InternalStatsObject po = (InternalStatsObject) parentObject;
			long lastTime = po.getLastTime();
			if (lastTime == -1) {
				return OutputFormat.NOT_AVAILABLE;
			} else {
				return String.valueOf(lastTime);
			}
		} else if (name.equalsIgnoreCase("intervalTime")) {
			return String.valueOf(conf.getTimeInterval());
		} else if (name.equalsIgnoreCase("currentCounter")) {
			return String.valueOf(conf.getCurrentCounter());
		} else if (name.equalsIgnoreCase("cacheCounter")) {
			return String.valueOf(conf.getCache().getNumberOfCurrentCachedObjects());
	    } else {
			return OutputFormat.NOT_AVAILABLE;
		}
	}
}
