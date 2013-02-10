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
package com.nattrmon.services.shell;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.nattrmon.config.Config;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;

/**
 * Connects to a shell to run commands. The url provides the host on which to run the shell.<br>
 * <b>Note: Only localhost shell for now</b><br>
 * <br>
 * <b>URL prefix</b>: 'shell:'<br>
 * <b>Object types:</b>: <i>id, simple</i><br>
 * <b>Attribute types:</b> <i>none</i><br>
 * <br>
 * Example:
 * <pre>
 * &lt;service url="shell://localhost"&gt;
 *  &lt;object name="c:\a\x5.bat" params="regex='\n';"&gt;
 *   &lt;attribute uid="ATTR_1" name="1" type="id"/&gt;
 *   &lt;attribute uid="ATTR_2" name="2" type="id"/&gt;
 *  &lt;/object&gt;
 *  
 *  &lt;object name="c:\b\y2.bat"&gt;
 *   &lt;attribute uid="ATTR_3" name="shell"/&gt;
 *  &lt;/object&gt;
 * &lt;/service&gt;
 * </pre>
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 * @see com.nattrmon.services.shell.ShellObject
 * @see com.nattrmon.services.shell.ShellAttribute
 */
public class ShellService extends Service {
	protected static String SSH_PROTO     = "ssh://";
	protected static String SERVICE_PROTO = "shell";
	protected static HashMap<String, Session> sshCons = new HashMap<String, Session>();
	
	protected String SSH_URL = null;
	protected boolean usingSSH = false;
	protected Session session = null;

	protected String login = "";
	protected String password = "";
	protected String host = "";
	protected int port = 22;
	
	public static class SUserInfo implements UserInfo, UIKeyboardInteractive {
		protected String passwd;
		
		public SUserInfo(String p) {
			passwd = p;
		}
		
		public String[] promptKeyboardInteractive(String destination, String name,
				String instruction, String[] prompt, boolean[] echo) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getPassphrase() {
			return null;
		}

		public String getPassword() {
			return passwd;
		}

		public boolean promptPassphrase(String arg0) {
			return true;
		}

		public boolean promptPassword(String arg0) {
			return true;
		}

		public boolean promptYesNo(String arg0) {
			return true;
		}

		public void showMessage(String arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected void connectSSH() {
		String key = getSshConnectionKey();
		
		if (sshCons.containsKey(key)) {
			session = sshCons.get(key);
			if ((session != null) && !(session.isConnected())) {
				try {
					session.connect();
				} catch (JSchException e) {
					conf.lOG(OutputType.ERROR, "SSH connection to " + login + "@" + host + ":" + port + " error: " + e.getMessage());
					session = null;
				}
			}
		} 

		if (session == null) {
			JSch jsch = new JSch();
			try {
				session = jsch.getSession(login, host, port);
			} catch (JSchException e) {
				conf.lOG(OutputType.ERROR, "SSH obtaining session to " + login + "@" + host + ":" + port + " error: " + e.getMessage());
			}
			session.setUserInfo(new SUserInfo(password));
			try {
				session.connect();
			} catch (JSchException e) {
				conf.lOG(OutputType.ERROR, "SSH connecting to " + login + "@" + host + ":" + port + " error: " + e.getMessage());
			}
		}

	}
	
	public ShellService(Config conf, String aUrl, String aParams) {
		super(conf, ServiceType.NormalService, SERVICE_PROTO, "");

		String tp = "";
		String prop[];
		
		if (aUrl.indexOf(SSH_PROTO) > 0) {
			setUsingSSH(true);
			String systemhost = aUrl.substring(aUrl.indexOf(SSH_PROTO) + SSH_PROTO.length());
			
			if (systemhost.indexOf(':') > 0) {
				prop = systemhost.split(":");
				if (prop.length == 2) {
					host = prop[0];
					try {
						port = Integer.parseInt(prop[1]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					
				}
			}
			
			String params[] = aParams.split(";");
			for (String param : params) {
				tp = param.trim();
				prop = tp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("login")) login = prop[1];
					if (prop[0].equalsIgnoreCase("password")) password = prop[1];
				}
			}
		}
	}

	public static void register() {
		Config.registerService(ShellService.class.getName(), "shell");
	}

	@Override
	public Object getNewObject(String name, String params) {
		return new ShellObject(conf, this, name, params);
	}

	public boolean isUsingSSH() {
		return usingSSH;
	}

	public void setUsingSSH(boolean usingSSH) {
		this.usingSSH = usingSSH;
	}
	
	public Session getSshSession() {
		if (session == null) connectSSH();
		return session;
	}
	
	/*
	 * Returns the ssh connection key to use to uniquely identify a ssh connection
	 */
	protected String getSshConnectionKey() {
		return login + "@" + host + ":" + port;
	}
}
