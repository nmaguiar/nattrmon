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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import com.nattrmon.config.Config;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;

public class DBService extends Service {
	protected static HashMap<String, Connection> dbCons = new HashMap<String, Connection>();
	protected static final String SERVICE_NAME = "jdbc";
	protected Connection dbCon = null;
	protected boolean inError = false;

	public static void register() {
		Config.registerService(DBService.class.getName(), SERVICE_NAME);
	}
	
	public DBService(Config conf, String aUrl, String aParams) {
		super(conf, ServiceType.NormalService, SERVICE_NAME, aParams);
		setUrl(aUrl);
		String dbClass = "";
		String dbUrl = aUrl;
		
		String url[] = getUrl().split(":");
		if (url.length >= 1) {
			dbClass = url[1];
		}
		
		//dbUrl = aUrl.replace(SERVICE_NAME + ":", "");
		registerJDBCClass(dbClass, dbUrl, aParams);
	}

	/**
	 * Register and connect to the database specified
	 * 
	 * @param dbClass The JDBC database vendor (currently supported: oracle, mysql, sqlite)
	 * @param url The JDBC URL
	 * @param aParams The database connection parameters (on the form "login=myLogin;password=myPassword")
	 */
	public void registerJDBCClass(String dbClass, String url, String aParams) {
		String login = "";
		String password = "";
		String dbclass = "";
		String tmp = "";
		String prop[];
		Properties props = new Properties();
		
		String params[] = aParams.split(";");
		for (String param : params) {
			tmp = param.trim();
			prop = tmp.split("=");
			if (prop.length == 2) {
				if (prop[0].equalsIgnoreCase("login")) login = prop[1];
				if (prop[0].equalsIgnoreCase("password")) password = prop[1];
				if (prop[0].equalsIgnoreCase("dbclass")) dbclass = prop[1];
			}
		}
		
		props.setProperty("user", login);
		props.setProperty("password", password);
		
		if (dbClass.equals("oracle")) {
			dbclass = "oracle.jdbc.OracleDriver";
		}
		
		if (dbClass.equals("mysql")) {
			dbclass = "com.mysql.jdbc.Driver";			
		}
		
		if (dbClass.equals("sqlite")) {
			dbclass = "org.sqlite.JDBC";
			props.setProperty("shared_cache", "true");
		}
		
		connect(dbclass, url, props);		
	}
	
	/**
	 * Connect to a database filling the internal property dbCon.  
	 *  
	 * @param JDBCDriverClass The JDBC driver full package and class name
	 * @param url The jdbc url to use
	 * @param login The database login
	 * @param password The database password
	 */
	public void connect(String JDBCDriverClass, String url, Properties prop) {
		if (dbCons.containsKey(url + params)) {
			dbCon = dbCons.get(url + params);
			try {
				if ( (dbCon != null) && (dbCon.isClosed()) ) {
					dbCon = null;
				}
			} catch (SQLException e) {
				// Do nothing
			}
		}
		
		if (dbCon == null) {
			conf.lOG(OutputType.DEBUG, "so.parentServices.url: " + url);
			try {
				Class.forName(JDBCDriverClass);
				inError = false;
			} catch (ClassNotFoundException e) {
				inError = true;
				conf.lOG(OutputType.ERROR, "JDBC Driver " + JDBCDriverClass + " not found in classpath.");
				conf.lOG(OutputType.DEBUG, "Exception:", e);
			}
			
			try {
				dbCon = DriverManager.getConnection(url, prop);
				dbCon.setAutoCommit(false);
				dbCon.setReadOnly(true);
				inError = false;
			} catch (SQLException e) {
				inError = true;
				conf.lOG(OutputType.ERROR, "SQLException while connecting to " + url);
				conf.lOG(OutputType.DEBUG, "Exception:", e);
			}
			
			dbCons.put(url + params, dbCon);
		}
	}
	
	@Override
	public Object getNewObject(String name, String params) {		
		return new DBObject(conf, this);
	}

	public Connection getDbCon() {
		return dbCon;
	}
	
	/**
	 * Close the current database connection
	 * 
	 */
	public void close() {
		if (dbCon != null) {
			try {
				dbCon.close();
				conf.lOG(OutputType.DEBUG, "Database connection closed.");
			} catch (SQLException e) {
				conf.lOG(OutputType.ERROR, "Exception during db connection close:", e);
			}
		}
	}

	protected void finalize() {
		close();
		try {
			super.finalize();
		} catch (Throwable e) {
			conf.lOG(OutputType.ERROR, "Exception during finalize: " + e.getMessage());
		}
	}
}
