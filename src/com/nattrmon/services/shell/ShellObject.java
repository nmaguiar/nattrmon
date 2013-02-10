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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.Service;
import com.nattrmon.core.Attribute.AttributeType;
import com.nattrmon.output.Output.OutputType;

public class ShellObject extends Object {
	protected String output = null;
	protected String cacheKey = String.valueOf(this.getName());
	protected String regex = null;
	protected boolean nocr = false;
	protected boolean isSSH = false;
	
	public ShellObject(Config conf, Service parentService, String name, String params) {
		super(conf, parentService, ObjectType.SystemCommand, name);
		if (params != null) {
			String pArray[] = params.split(";");
			if (pArray != null) {
				List<String> pList = Arrays.asList(pArray);
				for (String param : pList) {
					if (param.startsWith("regex=")) {
						this.regex = param.split("=")[1];
					}
					if (param.startsWith("nocr=y")) {
						nocr = true;
					}
				}
			}
		}
		isSSH = ((ShellService) parentService).isUsingSSH();
	}

	@Override
	public Attribute getNewAttribute(String uid, String name, AttributeType type, String value) {
			try {
				return new ShellAttribute(conf, this, uid, type, name, value);
			} catch (ExceptionDuplicatedUniqueAttribute e) {
				conf.lOG(OutputType.ERROR, "Trying to add an already existing attribute for uid = '" + uid + "'");
				conf.lOG(OutputType.DEBUG, "ExceptionDuplicatedUniqueAttribute", e);
				return null;
			}
	}

	protected void runCommand() {
		try {
			Process p = Runtime.getRuntime().exec(getName());
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer sb = new StringBuffer();
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				sb.append(line);
				sb.append("\n");
			}
			output = sb.toString();
			/*String outline = br.readLine();
			output = "";
			while(outline != null) {
				output += outline + "\n";
				outline = br.readLine();
			}*/
			conf.getCache().addObjectToCacheWithTimeLimit(cacheKey, new CacheableCommandOutput(output), 1);
		} catch (IOException e) {
			conf.lOG(OutputType.ERROR, "Error executing command: '" + getName() + "'");
			conf.lOG(OutputType.DEBUG, e.getMessage());
		}
	}
	
	protected synchronized void runSSHCommand() {
		
		String key = ((ShellService) parentService).getSshConnectionKey();
		Session session = null;
		synchronized (((ShellService) parentService)) {
			session = ((ShellService) parentService).getSshSession();
		}
		
		Channel channel = null;
		int i = -1;

		try {
			channel = session.openChannel("exec");
		} catch (JSchException e) {
			conf.lOG(OutputType.ERROR, "SSH obtaining channel to " + key + " : " + e.getMessage());
		}
		if (channel != null) {
			((ChannelExec) channel).setCommand(getName());
			
			try {
				channel.connect();		
				BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
				
				if (channel.isConnected()) {
					StringBuffer sb = new StringBuffer();
					
					for(String line = br.readLine(); line != null; line = br.readLine()) {
	                    sb.append(line);
	                    sb.append("\n");
					}
				
					output = sb.toString();
				}
				
				/*output = ""; int iii = 1;
				String tmp = br.readLine();
				while((tmp != null) && (!(channel.isClosed()))) {
					output += tmp + "\n";
					tmp = br.readLine();
					iii++;
					//try{Thread.sleep(1000);} catch(Exception ee){}
				}
				System.out.println(iii + " | " + channel.isEOF());*/
				/*
				byte[] tmp = new byte[1024];
				output = ""; int ii = -1;
				while (true) {
					while (in.available() > 0 ) {
						i = in.read(tmp, 0, 1024);
						if (i < 0) {
							break;
						} else {
							output += (new String(tmp, 0, i));
						}
						ii = i;
					}
					if (channel.isClosed()) {
						System.out.println("VAI1: " + channel.);
						System.out.println("VAI2: " + ii);
						break;
					}
					//try{Thread.sleep(1000);}catch(Exception ee){}
				}*/
				br.close();
				channel.disconnect();
			} catch (IOException e) {
				conf.lOG(OutputType.ERROR, "SSH connection to " + key + " IO error: " + e.getMessage());
			} catch (JSchException e) {
				conf.lOG(OutputType.ERROR, "SSH connection to " + key + " : " + e.getMessage());
			}
		}
	}

	public String getOutput(boolean force) {
		output = null;
		
		if (conf.getCache().isObjectStillValid(cacheKey)) {
			CacheableCommandOutput outCache = (CacheableCommandOutput) conf.getCache().getCachedObject(cacheKey);
			if (outCache != null) {
				output = outCache.getOutput(); 
			}
		}
		
		if (force || output == null) {
			if (isSSH) {
				runSSHCommand();
			} else {
				runCommand();
			}
		} 
		
		return output;
	}

	public String getRegex() {
		return regex;
	}

	public boolean isNocr() {
		return nocr;
	}

	public void setNocr(boolean nocr) {
		this.nocr = nocr;
	}

}
