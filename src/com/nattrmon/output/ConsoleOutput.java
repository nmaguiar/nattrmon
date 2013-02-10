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

import com.nattrmon.config.Config;

/**
 * Basic implementation of output to the console
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class ConsoleOutput extends Output {

	/**
	 * Constructor 
	 * 
	 * @param conf The current configuration to use
	 */
	public ConsoleOutput(Config conf) {
		super(conf);
	}

	/**
	 * Write implementation. Writes anything to the console with 
	 * no filters.
	 * 
	 * @param type Output type
	 * @param message Output message
	 */
	@Override
	public void write(OutputType type, String message) {
		if (type == currentType)
			System.out.println(System.currentTimeMillis() + ":[" + type + "]: " + message);
	}

	@Override
	public void write(OutputType type, String message, Exception e) {
	    StringBuilder result = new StringBuilder("");
	    
	    for (StackTraceElement element : e.getStackTrace() ){
	      result.append(element);
	      result.append(System.getProperty("line.separator"));
	    }
	    
		write(type, message + result);
	}

}
