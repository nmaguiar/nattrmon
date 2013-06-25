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
package com.nattrmon.output.report.httpd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.output.report.ReportUtils;
import com.nattrmon.output.report.smtp.SMTPFormat;
import com.nwu.httpd.Codes;
import com.nwu.httpd.HTTPd;
import com.nwu.httpd.Request;
import com.nwu.httpd.responses.EchoResponse;
import com.nwu.httpd.responses.Response;
import com.nwu.log.Log;
import com.nwu.log.Log.Type;

public class HTTPDFormat extends OutputFormat {
	protected String filename = null;
	protected int port = 17878;
	protected String htmlTemplate = "";
	protected String currentHTML = "not ready yet...";
	public static HashMap<String, String> currentHTMLs = new HashMap();
	protected String id;

	public static void register() {
		Config.registerFormat(HTTPDFormat.class.getName(), "httpdOutput");
	}
	
	public class HLog extends Log {
		protected Config conf;
		
		public HLog(Config c, String loggerName) {
			super();
			conf = c;
		}
		
		public HLog(Config c, String loggerName, Type type) {
			super();
			conf = c;
		}

		public void log(Type type, String message) {
			switch (type) {
			case DEBUG:
				conf.lOG(OutputType.DEBUG, "[HTTPD]" + message);
				break;
			case ERROR:
				conf.lOG(OutputType.ERROR, "[HTTPD]" + message);
				break;
			case INFO:
				conf.lOG(OutputType.INFO, "[HTTPD]" + message);
				break;
			}
		}
		
		public void log(Type type, long id, String message) {
			switch (type) {
			case DEBUG:
				conf.lOG(OutputType.DEBUG, "[HTTPD]" + id + "|" + message);
				break;
			case ERROR:
				conf.lOG(OutputType.ERROR, "[HTTPD]" + id + "|" + message);
				break;
			case INFO:
				conf.lOG(OutputType.INFO, "[HTTPD]" + id + "|" + message);
				break;
			}			
		}
		
	}
	
	public HTTPDFormat(Config conf, String param) {
		super(conf, param);
		
		if (param != null) {
			String pms[] = param.split(";");
			for (String p : pms) {
				String tmp = p.trim();
				String[] prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("src")) filename = prop[1];
					try {
						if (prop[0].equalsIgnoreCase("port")) port = Integer.valueOf(prop[1]);
					} catch (NumberFormatException e) {
						conf.lOG(OutputType.ERROR, "Port number not recognized: '" + prop[1] + "'");
						conf.lOG(OutputType.DEBUG, "NumberFormatException", e);
					}
				}
			}
		}
		
		id = Integer.toString(port + this.hashCode());
		
		if (filename != null) {
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(filename);
			} catch (FileNotFoundException e) {
				conf.lOG(OutputType.ERROR, "File not found: '" + filename + "'");
				conf.lOG(OutputType.DEBUG, "FileNotFoundException", e);
			}
			
			if (fileReader != null) {
				BufferedReader br = new BufferedReader(fileReader);
				StringBuffer sf = new StringBuffer();
				String s = "";
				try {
					while((s = br.readLine()) != null) {
						sf.append(s);
					}
				} catch (IOException e) {
					conf.lOG(OutputType.ERROR, "IO problem reading file: '" + filename + "'");
					conf.lOG(OutputType.DEBUG, "IOException", e);
				}
				htmlTemplate = sf.toString();
			}
		}
		
		// Start server
		try {
			HTTPd httpd = new HTTPd(new HLog(conf, "httpd_" + port, Type.OFF), port);
			
			Properties pros = new Properties();
			pros.put("id", id);
			HTTPd.registerURIResponse("/", SimpleResponse.class, pros);
			HTTPd.registerURIResponse("/Echo", EchoResponse.class, null);
		} catch (IOException e) {
			conf.lOG(OutputType.ERROR, "Problem setting up httpd server on port: '" + port + "'");
			conf.lOG(OutputType.DEBUG, "IOException", e);
		}
	}

	@Override
	public void processOutput() {
		
		if (htmlTemplate.equals("")) 
			htmlTemplate = ReportUtils.buildTemplate(getAttrNames());
		
		currentHTMLs.put(id, ReportUtils.preProcess(conf, getAttrNames(), htmlTemplate));
		
	}

}
