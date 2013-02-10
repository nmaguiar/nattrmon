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
package com.nattrmon.collector;

import com.nattrmon.config.Config;

/**
 * General interface for CollectorOutput.
 * CollectorOutput implementations should provide the retrieval of attribute
 * values from services and the output to the specified output services.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public interface CollectorOutput {
	Config conf = null;
	long timeInterval = 0;
	long count = 0;

	/**
	 * The main execution method that should retrieve values from Services and output to OutputFormats
	 *  
	 * @see com.nattrmon.core.Service
	 * @see com.nattrmon.core.OutputFormat 
	 */
	public abstract void run();
	
	/**
	 * Should obtain the current configuration been used. And should be used everywhere in this class
	 * to refer to the current configuration.
	 * 
	 * @return The current Config object
	 * @see com.nattrmon.config.Config
	 */
	public abstract Config getConfig();
	
}
