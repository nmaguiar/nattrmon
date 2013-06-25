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
package com.nattrmon.services.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;

public class DBAttribute extends Attribute {
	
	public DBAttribute(Config conf, Object parentObject, String uniqueName,
			String name, AttributeType type, String value)
			throws ExceptionDuplicatedUniqueAttribute {
		super(conf, parentObject, uniqueName, name, type, value);
		setHeavy(true);
	}

	@Override
	public String getValue() {
		String result = OutputFormat.NOT_AVAILABLE;
		if (parentObject != null) {
			DBObject dbO = (DBObject) parentObject;
			DBService dbS = (DBService) parentObject.getParentService();
			OfflineResultSet rS = dbO.getResultSet();
			
			if ((rS != null) && (dbS != null)) {
				if (type == AttributeType.Id) {
					try {
						result = rS.getString(Integer.parseInt(name));
						if (result == null) result = OutputFormat.NOT_AVAILABLE;
					} catch (NumberFormatException e) {
						conf.lOG(OutputType.ERROR, "Exception when parsing value from name '" + name + "':", e);
					}
				} else if (type == AttributeType.Name) {
						result = rS.getString(name);
						if (result == null) result = OutputFormat.NOT_AVAILABLE;
				} else {
					conf.lOG(OutputType.DEBUG, "Attribute of type '" + type + "' not expected.");
				}
			}
		}
		
		return result;
	}
}
 