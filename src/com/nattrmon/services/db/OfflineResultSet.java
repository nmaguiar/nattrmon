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
import java.util.HashMap;

public class OfflineResultSet {
	protected String[] results;
	protected HashMap<String, Integer> labels2pos = new HashMap<String, Integer>();
	
	public OfflineResultSet(ResultSet rs) throws SQLException {
		if (rs != null) {
			results = new String[rs.getMetaData().getColumnCount()];
			
			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
				labels2pos.put(rs.getMetaData().getColumnLabel(i), new Integer(i));
				results[i - 1] = rs.getString(i);
			}
		}
	}
	
	public String getString(int i) {
		if ((i - 1) < results.length) {
			return results[(i - 1)];
		} else {
			return null;
		}
	}
	
	public String getString(String name) {
		String n = name.toUpperCase();
		if (labels2pos.containsKey(n)) {
			return results[labels2pos.get(n).intValue() - 1];
		} else {
			return null;
		}
	}
}
