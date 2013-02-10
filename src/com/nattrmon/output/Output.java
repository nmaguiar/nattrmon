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
package com.nattrmon.output;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;

public abstract class Output {

	public enum OutputType {
		INFO,
		ERROR,
		DEBUG
	};
	protected OutputType currentType;
	
	public abstract void write(OutputType type, String message);
	
	public abstract void write(OutputType type, String message, Exception e);
	
	public void setLevel(OutputType type) {
		currentType = type;
	}
	
	public Output(Config conf) {
		// Some output extensions may not support setLevel without other initilizations
		currentType = conf.getDefaultType();
	}
	
	public static OutputFormat getNewFormat(Config conf, String type, String param) {
		String aFormatClass = Config.getRegisteredFormats().get(type);
		if (aFormatClass != null) {
			Class<OutputFormat> aFormat;
			try {
				aFormat = (Class<OutputFormat>) Class.forName(aFormatClass);
				Constructor c = aFormat.getDeclaredConstructor(Config.class, String.class);
				try {
					return (OutputFormat) c.newInstance(conf, param);
				} catch (IllegalArgumentException e) {
					conf.lOG(OutputType.ERROR, "Initializing '" + type + "' with params='" + param + "'. No suitable constructor found.");
					conf.lOG(OutputType.DEBUG, "IllegalArgumentException", e);
				} catch (InstantiationException e) {
					conf.lOG(OutputType.ERROR, "Initializing '" + type + "' with params='" + param + "'.");
					conf.lOG(OutputType.DEBUG, "InstantiationException", e);
				} catch (IllegalAccessException e) {
					conf.lOG(OutputType.ERROR, "Initializing '" + type + "' with params='" + param + "'. Illegal access.");
					conf.lOG(OutputType.DEBUG, "InstantiationException", e);
				} catch (InvocationTargetException e) {
					conf.lOG(OutputType.ERROR, "Initializing '" + type + "' with params='" + param + "'.");
					conf.lOG(OutputType.DEBUG, "InvocationTargetException", e);
				}
			} catch (ClassNotFoundException e) {
				conf.lOG(OutputType.ERROR, "Initializing '" + type + "' with params='" + param + "'. Class not found.");
				conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
			} catch (SecurityException e) {
				conf.lOG(OutputType.ERROR, "Initializing '" + type + "' with params='" + param + "'. Security problem.");
				conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
			} catch (NoSuchMethodException e) {
				conf.lOG(OutputType.ERROR, "Initializing '" + type + "' with params='" + param + "'. No suitable constructor found.");
				conf.lOG(OutputType.DEBUG, "ClassNotFoundException", e);
			}
		}
		
		return null;
	}
}
