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
package com.nattrmon.output.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.core.UniqueAttributes;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.output.order.OrderFormat;

public class SQLiteFormat extends OutputFormat {
	protected Connection conn;
	protected String fileName = "nattrmon.db";
	protected String tableName = "nattrmon";
	protected boolean dropexisting = true;
	
	public SQLiteFormat(Config conf, String params) {
		super(conf, params);
		String tmp, prop[];
		
		showHeader = false;
		
		if (params != null) {
			String pms[] = params.split(";");
			for (String param : pms) {
				tmp = param.trim();
				prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("filename")) fileName = prop[1];
					if (prop[0].equalsIgnoreCase("tablename")) tableName = prop[1];
					if (prop[0].equalsIgnoreCase("dropexisting")) {
						if (prop[1].equals("1")) dropexisting = true; else dropexisting = false; 
					}
				}
			}
		}
		
	}
	
	public static void register() {
		Config.registerFormat(SQLiteFormat.class.getName(), "sqliteOutput");
	}

	public Connection getConnection() {
		try {
			if (!((conn != null) && !(conn.isClosed()))) {
				try {
					Class.forName("org.sqlite.JDBC");
				} catch (ClassNotFoundException e) {
					conf.lOG(OutputType.ERROR, "SQLite JDBC driver org.sqlite.JDBC not found in classpath");
					conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
				}
				
				Properties props = new Properties();
				//props.setProperty("shared_cache", "true");
				conn = DriverManager.getConnection("jdbc:sqlite:" + fileName, props);
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			conf.lOG(OutputType.ERROR, "Exception while connecting to SQLite '" + fileName + "' database.");
			conf.lOG(OutputType.DEBUG, "SQLException", e);
		}
		
		return conn;
	}
	
	@Override
	public void processOutput(long counter) {
		StringBuffer out = new StringBuffer();
		StringBuffer outAttr = new StringBuffer();
		ArrayList<String> attrNames = null;
		Connection con;
		
		attrNames = getAttrNames();
		
		String suffix = ", ";
		//long counter = conf.getCurrentCounter();
		for(String attrName :attrNames) {
			if (attrNames.indexOf(attrName) == (attrNames.size() - 1)) suffix = "";
			
			if (conf.containsCurrentAttributeValues(counter, attrName)) {
				outAttr.append(attrName + suffix);
				out.append("\"" + conf.getCurrentAttributeValues(counter, attrName) + "\"" + suffix);
			} else {
				conf.lOG(OutputType.ERROR, "Attribute '" + attrName + "' not found.");
			}
		}
		
		if (isFirstTime()) {
			Statement stat;
			try {
				if (dropexisting) {
					con = getConnection();
					if (con == null) {
						conf.lOG(OutputType.ERROR, "Couldn't connect to SQLite");
					} else {
						stat = con.createStatement();
						stat.executeUpdate("drop table if exists " + tableName);

						if (attrNames != null) {
							stat.executeUpdate("create table " + tableName + " (" + outAttr.toString() + ");");
						}

						stat.close();
						con.close();
					}
				}
			} catch (SQLException e) {
				conf.lOG(OutputType.ERROR, "Problem while creating table '" + tableName + "' in SQLite. (drop table if exists = " + dropexisting + ")");
				conf.lOG(OutputType.DEBUG, "SQLException", e);
			}
		}
		
		
		try {
			con = getConnection();
			if (con == null) {
				conf.lOG(OutputType.ERROR, "Couldn't connect to SQLite");
			} else {
				Statement stat = getConnection().createStatement();

				stat.executeUpdate("insert into " + tableName + " (" + outAttr.toString() + ") values (" + out.toString() + ")");
				stat.close();
				conn.close();
			}
		} catch (SQLException e) {
			conf.lOG(OutputType.ERROR, "Problem while inserting into table '" + tableName + "' in SQLite.");
			conf.lOG(OutputType.DEBUG, "SQLException", e);
		}
	}
}
