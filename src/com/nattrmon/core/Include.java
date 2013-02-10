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
package com.nattrmon.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.nattrmon.config.Config;
import com.nattrmon.output.Output;
import com.nattrmon.output.Output.OutputType;

/**
 * Helper class for including Services (type service) and OutputFormat (type output) classes through configuration
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 * @see com.nattrmon.core.Service
 * @see com.nattrmon.core.OutputFormat
 */
public class Include {
	protected Config conf = Config.getConfig(); 
    protected String type;
    protected String path;
    
    /**
     * Constructor for an instance of an Include configuration
     * 
     * @param aType Type of this instance (either "service" or "output")
     * @param aPath The entire classpath for the class implementing the type
     */
    public Include(String aType, String aPath) {
    	type = aType;
    	path = aPath;
    	
    	process();
    }
    
    /**
     * Process type "service" and type "output"
     * 
     */
    protected void process() {
    	if (type.equalsIgnoreCase("service")) {
    		processService();
    	}
    	
    	if (type.equalsIgnoreCase("output")) {
    		processOutput();
    	}
    }
    
    /**
     * Process this Include instance as a service
     * 
     */
    protected void processService() {
    	Class<Service> aService;
    	try {
			aService = (Class<Service>) Class.forName(path);
			try {
				Method m = aService.getMethod("register", null);
				m.invoke(null);
			} catch (SecurityException e) {
				conf.lOG(OutputType.ERROR, "Security error while trying to include '" + type + "', '" + path + "'");
				conf.lOG(OutputType.DEBUG, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				conf.lOG(OutputType.ERROR, "Include '" + type + "', '" + path + "' not recognized.");
				conf.lOG(OutputType.DEBUG, "NoSuchMethodException", e);
			} catch (IllegalArgumentException e) {
				conf.lOG(OutputType.ERROR, "Include '" + type + "', '" + path + "' not recognized.");
				conf.lOG(OutputType.DEBUG, "IllegalArgumentException", e);
			} catch (IllegalAccessException e) {
				conf.lOG(OutputType.ERROR, "Security error while trying to include '" + type + "', '" + path + "'");
				conf.lOG(OutputType.DEBUG, "IllegalAccessException", e);
			} catch (InvocationTargetException e) {
				conf.lOG(OutputType.ERROR, "Error while trying to include '" + type + "', '" + path + "'");
				conf.lOG(OutputType.DEBUG, "InvocationTargetException", e);
			}
		} catch (ClassNotFoundException e) {
			conf.lOG(OutputType.ERROR, "Include '" + type + "', '" + path + "' not found. Check the path and type provided.");
			conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
		}
    }
    
    /**
     * Process this Include instance as an output
     * 
     */
    protected void processOutput() {
    	Class<Output> aOutput;
    	try {
			aOutput = (Class<Output>) Class.forName(path);
			try {
				Method m = aOutput.getMethod("register", null);
				m.invoke(null);
			} catch (SecurityException e) {
				conf.lOG(OutputType.ERROR, "Security error while trying to include '" + type + "', '" + path + "'");
				conf.lOG(OutputType.DEBUG, "SecurityException", e);
			} catch (NoSuchMethodException e) {
				conf.lOG(OutputType.ERROR, "Include '" + type + "', '" + path + "' not recognized.");
				conf.lOG(OutputType.DEBUG, "NoSuchMethodException", e);
			} catch (IllegalArgumentException e) {
				conf.lOG(OutputType.ERROR, "Include '" + type + "', '" + path + "' not recognized.");
				conf.lOG(OutputType.DEBUG, "IllegalArgumentException", e);
			} catch (IllegalAccessException e) {
				conf.lOG(OutputType.ERROR, "Security error while trying to include '" + type + "', '" + path + "'");
				conf.lOG(OutputType.DEBUG, "IllegalAccessException", e);
			} catch (InvocationTargetException e) {
				conf.lOG(OutputType.ERROR, "Error while trying to include '" + type + "', '" + path + "'");
				conf.lOG(OutputType.DEBUG, "InvocationTargetException", e);
			}
		} catch (ClassNotFoundException e) {
			conf.lOG(OutputType.ERROR, "Include '" + type + "', '" + path + "' not found. Check the path and type provided.");
			conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
		}    	
    }
    
    /** 
     * Obtain the type
     * 
     * @return The type
     */
	public String getType() {
		return type;
	}
	
	/**
	 * Set the type
	 * 
	 * @param type The new type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Obtain the path
	 * 
	 * @return The path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Set the path
	 * 
	 * @param path The new path
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
