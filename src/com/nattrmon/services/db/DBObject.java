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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.output.Output.OutputType;

public class DBObject extends Object {
	//protected ResultSet rS = null;
	protected String cacheKey = String.valueOf(this.getName());
	protected boolean parseVariables = false;

	public DBObject(Config conf, Service parentService) {
		super(conf, parentService, ObjectType.SQLQuery, "DBObject");
	}
	
	@Override
	public void setParams(String prs) {
		super.setParams(prs);
		
		if (prs != null) {
			String tmp, prop[];
			String pars[] = prs.split(";");
			for (String param : pars) {
				tmp = param.trim();
				prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("parse") && prop[1].equalsIgnoreCase("1")) parseVariables = true;
				}
			}
		}
	}

	@Override
	public Attribute getNewAttribute(String uid, String name, AttributeType type, String value) {
		try {
			return new DBAttribute(conf, this, uid, name, type, value);
		} catch (ExceptionDuplicatedUniqueAttribute e) {
			conf.lOG(OutputType.ERROR, "Attribute " + e.getUniqueName() + " already exists.");
			return null;
		}
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		cacheKey = name;
	}
	
	protected String parseAttributesVariables(String sql) {
		String result = sql;
		
		for (String attr :conf.getUniqueAttrs().getAttrs().keySet()) {
			if (result.contains("{$" + attr + "}")) {
				result = result.replaceAll("\\{\\$" + attr + "\\}", conf.getUniqueAttrs().getAttribute(attr).getValue());
			}
		}
		
		return result;
	}
	
	protected OfflineResultSet getRS() {
		PreparedStatement pS = null;
		ResultSet rS = null;
		OfflineResultSet orS = null;
		
		if (parentService != null) {
			DBService dbS = ((DBService) parentService);
			Connection dbCon = dbS.getDbCon();
			
			if (dbCon != null) {
				String sql;
				
				if (parseVariables) 
					sql = parseAttributesVariables(name);
				else
					sql = name;
					
				try {
					pS = dbCon.prepareStatement(sql);
					//pS.setQueryTimeout((int) conf.getTimeInterval());
				} catch (SQLException e) {
					conf.lOG(OutputType.ERROR, "Exception during prepare statement to database '" + dbS.getUrl() + "': ", e);
				}
				
				if (pS != null) {
					try {
						rS = pS.executeQuery();
						if (rS != null) {
							rS.next(); // Only the first line
							orS = new OfflineResultSet(rS);
							rS.close(); // as fast as possible to avoid locks in some types of databases (e.g. sqlite)
							pS.close();
							conf.getCache().addObjectToCacheWithTimeLimit(cacheKey, new CacheableResultSet(orS), 1);
						}
					} catch (SQLException e) {
						conf.lOG(OutputType.ERROR, "Exception during execute query to database '" + dbS.getUrl() + "': ", e);
					}
				}
			}
		}
		
		return orS;
	}

	public OfflineResultSet getResultSet() {
		if (conf.getCache().isObjectStillValid(cacheKey)) {
			CacheableResultSet outCache = (CacheableResultSet) conf.getCache().getCachedObject(cacheKey);
			if (outCache != null) {
				return outCache.getRs();
			} else {
				return getRS();
			}
		} else {
			return getRS();
		}
	}
}
