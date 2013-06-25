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
package com.nattrmon.output.syslog;

import java.util.ArrayList;
import java.util.Arrays;

import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.output.order.OrderFormat;

/**
 * Output nattrmon attribute values to a syslog.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 * 
 */
public class SyslogFormat extends OutputFormat {
	protected String host;
	protected String port;
	protected String protocol = "tcp";
	protected SyslogIF syslog = null;
	protected String prefix = "nattrmon";
	protected String debug = "no";
	
	public SyslogFormat(Config conf, String params) {
		super(conf, params);
		String tmp, prop[];
		
		showHeader = false;
		
		if (params != null) {
			String pms[] = params.split(";");
			for (String param : pms) {
				tmp = param.trim();
				prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("host")) host = prop[1];
					if (prop[0].equalsIgnoreCase("port")) port = prop[1];
					if (prop[0].equalsIgnoreCase("protocol")) protocol = prop[1];
					if (prop[0].equalsIgnoreCase("prefix")) prefix = prop[1];
					if (prop[0].equalsIgnoreCase("debug")) debug  = prop[1];
				}
			}
		}
		
		connect();
	}
	
	/**
	 * Connect a syslog connection
	 */
	public void connect() {
		if ((port != null) && (host != null) && (debug.equals("no"))) {
			syslog = Syslog.getInstance(protocol);
			syslog.getConfig().setHost(host);
			try {
				syslog.getConfig().setPort(Integer.parseInt(port));
			} catch(NumberFormatException e) {
			}
			//writeLog("Connected");
		}
	}
	
	/**
	 * Disconnect from a syslog connection
	 */
	public void disconnect() {
		if (syslog != null) {
			//writeLog("Disconnected");
			syslog.shutdown();
		}
	}

	/**
	 * Register this output
	 */
	public static void register() {
		Config.registerFormat(SyslogFormat.class.getName(), "syslogOutput");
	}
	
	/**
	 * Build a prefix for messages
	 * 
	 * @return prefix
	 */
	public String getPrefix() {
		return prefix + ": ";
	}
	
	/**
	 * Write a message to current syslog connection
	 * 
	 * @param s The message
	 */
	public void writeLog(String s) {
		if (debug.equals("no"))
			syslog.info(getPrefix() + s);
	}
	
	@Override
	public void processOutput() {
		ArrayList<String> attrNames = null;
		
		if (syslog == null) {
			connect();
		}
		
		attrNames = getAttrNames();
		
		String suffix = "";
		long counter = conf.getCurrentCounter();
		for(String attrName :attrNames) {
			//if (attrNames.indexOf(attrName) == (attrNames.size() - 1)) suffix = ";";
			
			if (conf.containsCurrentAttributeValues(counter, attrName)) {
				if (!(this.isAttributeValueEqualToLastRun(attrName))) {
					if (debug.equals("no")) 
						writeLog("[" + counter + "] " + attrName + "=" + conf.getCurrentAttributeValues(counter, attrName));
					else
						conf.lOG(OutputType.INFO, "[" + counter + "] " + attrName + "=" + conf.getCurrentAttributeValues(counter, attrName) + suffix);
					
				}
			} else {
				conf.lOG(OutputType.ERROR, "Attribute '" + attrName + "' not found.");
			}
		}
	}

}
