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
package com.nattrmon.output.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;

public class OracleFormat extends OutputFormat {
	protected Connection conn;
	protected String url = "";
	protected String login = "";
	protected String password = "";
	protected String tableName = "nattrmon";
	protected boolean dropexisting = true;
	protected static HashMap<String, Connection>conns = new HashMap<String, Connection>();

	public OracleFormat(Config conf, String params) {
		super(conf, params);
		String tmp, prop[];
		
		showHeader = false;
		
		if (params != null) {
			String pms[] = params.split(";");
			for (String param : pms) {
				tmp = param.trim();
				prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("url")) url = prop[1];
					if (prop[0].equalsIgnoreCase("login")) login = prop[1];
					if (prop[0].equalsIgnoreCase("password")) password = prop[1];
					if (prop[0].equalsIgnoreCase("tablename")) tableName = prop[1];
					if (prop[0].equalsIgnoreCase("dropexisting")) {
						if (prop[1].equals("y")) dropexisting = true; else dropexisting = false; 
					}
				}
			}
		}
	}

	public static void register() {
		Config.registerFormat(OracleFormat.class.getName(), "oracleOutput");
	}
	
	public Connection getConnection() {
		try {
			if (conns.containsKey(url + param)) {
				conn = conns.get(url + param);
			}
			
			if (!((conn != null) && !(conn.isClosed()))) {
				try {
					Class.forName("oracle.jdbc.OracleDriver");
				} catch (ClassNotFoundException e) {
					conf.lOG(OutputType.ERROR, "Oracle JDBC driver oracle.jdbc.OracleDriver not found in classpath");
					conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
				}
				
				Properties props = new Properties();
				props.setProperty("user", login);
				props.setProperty("password", password);
				
				conn = DriverManager.getConnection(url, props);
				conn.setAutoCommit(true);
				
				conns.put(url + param, conn);
			}
		} catch (SQLException e) {
			conf.lOG(OutputType.ERROR, "Exception while connecting to '" + url + "' database.");
			conf.lOG(OutputType.DEBUG, "SQLException", e);
		}
		
		return conn;
	}
	
	@Override
	public void processOutput() {
		StringBuffer out = new StringBuffer();
		StringBuffer outAttr = new StringBuffer();
		StringBuffer outAttrCreate = new StringBuffer();
		ArrayList<String> attrNames = null;
		Connection con;
		long counter = conf.getCurrentCounter();
		
		if ((url == null) || (url.equals(""))) {
			conf.lOG(OutputType.ERROR, "No url specified for Oracle output");
			return;
		}
		
		attrNames = getAttrNames();
		
		String suffix = ", ";
		for(String attrName :attrNames) {
			if (attrNames.indexOf(attrName) == (attrNames.size() - 1)) suffix = "";
			
			if ((conf.containsCurrentAttributeValues(counter, attrName))) {
				outAttrCreate.append(attrName + " varchar2(255)"+ suffix);
				outAttr.append(attrName + suffix);
				out.append("\'" + conf.getCurrentAttributeValues(counter, attrName) + "\'" + suffix);
			} else {
				conf.lOG(OutputType.ERROR, "Attribute '" + attrName + "' not found.");
			}
		}
		
		if (isFirstTime()) {
			Statement stat;
			try {
				con = getConnection();
				if (con == null) {
					conf.lOG(OutputType.ERROR, "Couldn't connect to Oracle url:'" + url + "'");
				} else {
					if (dropexisting) {
						stat = con.createStatement();
						try {
							stat.executeUpdate("drop table " + tableName);
						} catch(Exception e) {
							conf.lOG(OutputType.DEBUG, "Table " + tableName + " wasn't drop.");
							//conf.lOG(OutputType.DEBUG, "Exception", e);
						}

						if (attrNames != null) {
							stat.executeUpdate("create table " + tableName + " (" + outAttrCreate.toString() + ")");
						}

						stat.close();
						//con.close();
					}
				}
			} catch (SQLException e) {
				conf.lOG(OutputType.ERROR, "Problem while creating table '" + tableName + "' in Oracle. (drop table if exists = " + dropexisting + ")");
				conf.lOG(OutputType.DEBUG, "SQLException", e);
			}
		}
		
		
		try {
			con = getConnection();
			if (con == null) {
				conf.lOG(OutputType.ERROR, "Couldn't connect to Oracle url:'" + url + "'");
			} else {
				Statement stat = getConnection().createStatement();
				stat.executeUpdate("insert into " + tableName + " (" + outAttr.toString() + ") values (" + out.toString() + ")");
				stat.close();
				//con.close();
			}
		} catch (SQLException e) {
			conf.lOG(OutputType.ERROR, "Problem while inserting into table '" + tableName + "' in Oracle.");
			conf.lOG(OutputType.DEBUG, "SQLException", e);
		}
	}

}
