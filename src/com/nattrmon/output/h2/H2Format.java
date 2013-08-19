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
package com.nattrmon.output.h2;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.h2.tools.Server;

import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;

public class H2Format extends OutputFormat {
	protected Connection conn;
	protected String fileName = "nattrmon.db";
	protected String tableName = "nattrmon";
	protected boolean dropexisting = true;
	protected int port = 9092;
	protected String h2options = "";
	protected String baseDir = "";
	protected String url = "";
	protected String login = "sa";
	protected String password = "nattrmon";
	protected Server server;
	protected boolean startServer = false;
	protected static HashMap<String, Connection>conns = new HashMap<String, Connection>();
	protected boolean tableFirstTime = true;
	
	public H2Format(Config conf, String params) {
		super(conf, params);
		String tmp, prop[];
		
		showHeader = false;
		
		if (params != null) {
			String pms[] = params.split(";");
			for (String param : pms) {
				tmp = param.trim();
				prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("port")) {
						try {
							port = Integer.valueOf(prop[1]);
						} catch (Exception e) {
						}
					}
					if (prop[0].equalsIgnoreCase("basedir")) baseDir = prop[1];
					if (prop[0].equalsIgnoreCase("server")) {
						if (prop[1].equals("1")) startServer = true; else startServer = false;
					}
					if (prop[0].equalsIgnoreCase("filename")) fileName = prop[1];
					if (prop[0].equalsIgnoreCase("tablename")) tableName = prop[1];
					if (prop[0].equalsIgnoreCase("url")) url = prop[1];
					if (prop[0].equalsIgnoreCase("login")) login = prop[1];
					if (prop[0].equalsIgnoreCase("password")) password = prop[1];
					if (prop[0].equalsIgnoreCase("options")) h2options = prop[1];
					if (prop[0].equalsIgnoreCase("dropexisting")) {
						if (prop[1].equals("1")) dropexisting = true; else dropexisting = false; 
					}
				}
			}
		}
	
		if (startServer) {
			// Auto-discover baseDir based on filename
			if (baseDir.equals("")) {
				try {
					baseDir = new File(fileName).getParent();
					if (baseDir == null) baseDir = ".";
				} catch(Exception e) {
					conf.lOG(OutputType.DEBUG, "Could auto-discover basedir parameter for H2 server from filename = '" + fileName + "'", e);
					baseDir = ".";
					conf.lOG(OutputType.DEBUG, "Default basedir value set to '" + baseDir + "'");
				} 
			}
			
			// Start the server
			startServer();
		}
	}
	
	public static void register() {
		Config.registerFormat(H2Format.class.getName(), "H2Output");
	}

	public Connection getConnection() {
		try {
			if (conns.containsKey(fileName + url + param)) {
				conn = conns.get(fileName + url + param);
			} 

			if (!((conn != null) && !(conn.isClosed()))) {
				try { 
					Class.forName("org.h2.Driver");
				} catch (ClassNotFoundException e) {
					conf.lOG(OutputType.ERROR, "H2 JDBC driver org.h2.Driver not found in classpath");
					conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
				}
				
				Properties props = new Properties();
				if (url.equals("")) {
					props.setProperty("user", login);
					props.setProperty("password", password);
					
					conn = DriverManager.getConnection("jdbc:h2:" + fileName + ";" + h2options, props);
					conn.setAutoCommit(true);
					Statement s = conn.createStatement();
					try {
						s.execute("CREATE USER " + login + " SET PASSWORD '" + password + "'");
						s.execute("ALTER USER " + login + " ADMIN TRUE");
					} catch (Exception e) {
						conf.lOG(OutputType.DEBUG, "Couldn't create user " + login + ". Changing it's password.");
						try {
							s.execute("ALTER USER " + login + " SET PASSWORD '" + password + "'");
						} catch (Exception ee) {
							conf.lOG(OutputType.DEBUG, "Couldn't change the password for user '" + login + "'");
						}
					}
				} else {
					props.setProperty("user", login);
					props.setProperty("password", password);
					
					conn = DriverManager.getConnection(url, props);
					conn.setAutoCommit(true);
				}
				
				conns.put(fileName + url + param, conn);
			}
		} catch (SQLException e) {
			conf.lOG(OutputType.ERROR, "Exception while connecting to H2 '" + fileName + "'/'" + url + "' database.");
			conf.lOG(OutputType.DEBUG, "SQLException", e);
		}
		
		return conn;
	}
	
	@Override
	public void processOutput(long counter) {
		StringBuffer out = new StringBuffer();
		StringBuffer outAttr = new StringBuffer();
		StringBuffer outAttrCreate = new StringBuffer();
		ArrayList<String> attrNames = null;
		Connection con;
		
		attrNames = getAttrNames();
		
		String suffix = ", ";
		//long counter = conf.getCurrentCounter();
		for(String attrName :attrNames) {
			if (attrNames.indexOf(attrName) == (attrNames.size() - 1)) suffix = "";
			
			if (tableFirstTime) outAttrCreate.append(attrName + " VARCHAR(255)" + suffix);
			
			String value = conf.getCurrentAttributeValues(counter, attrName);
			
			if (value != null) {
				outAttr.append(attrName + suffix);
				out.append("\'" + conf.getCurrentAttributeValues(counter, attrName) + "\'" + suffix);
			} else {
				conf.lOG(OutputType.ERROR, "Attribute '" + attrName + "' not found.");
			}
		}
		
		if (tableFirstTime) {
			Statement stat;
			try {
				if (dropexisting) {
					con = getConnection();
					if (con == null) {
						conf.lOG(OutputType.ERROR, "Couldn't connect to H2");
					} else {
						stat = con.createStatement();
						try {
							stat.executeUpdate("drop table " + tableName);
						} catch (SQLException e) {
							conf.lOG(OutputType.DEBUG, "Couldn't drop table " + tableName);
						}

						if (attrNames != null) {
							stat.executeUpdate("create table " + tableName + " (" + outAttrCreate.toString() + ");");
						}

						stat.close();
						//con.close();
						tableFirstTime = false;
					}
				} else {
					tableFirstTime = false;
				}
			} catch (SQLException e) {
				conf.lOG(OutputType.ERROR, "Problem while creating table '" + tableName + "' in H2. (drop table if exists = " + dropexisting + ")");
				conf.lOG(OutputType.DEBUG, "SQLException", e);
			}
		}
		
		
		try {
			con = getConnection();
			if (con == null) {
				conf.lOG(OutputType.ERROR, "Couldn't connect to H2");
			} else {
				Statement stat = getConnection().createStatement();

				stat.executeUpdate("insert into " + tableName + " (" + outAttr.toString() + ") values (" + out.toString() + ")");
				stat.close();
				//conn.close();
			}
		} catch (SQLException e) {
			conf.lOG(OutputType.ERROR, "Problem while inserting into table '" + tableName + "' in H2.");
			conf.lOG(OutputType.DEBUG, "SQLException", e);
		}
	}
	
	public void startServer() {
		String[] args = { "-tcp", "-tcpPort", String.valueOf(port), "-tcpPassword", "nattrmon", "-tcpAllowOthers", "-ifExists", "-baseDir", baseDir};
		try {
			server = Server.createTcpServer(args).start();
		} catch (SQLException e) {
			conf.lOG(OutputType.ERROR, "Exception while creating H2 server");
			conf.lOG(OutputType.DEBUG, "SQLException", e);
		}
		
	}
}
