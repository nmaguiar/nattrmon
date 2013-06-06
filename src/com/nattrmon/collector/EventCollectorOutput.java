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
import com.nattrmon.core.OutputFormat;

/**
 * Event controller based output.
 * Uses the configured time interval to implement a collector output (defaults to
 * one second if nothing is specified). Will only output in case of an attribute 
 * change.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class EventCollectorOutput implements CollectorOutput {
	protected Config conf;
	protected long internalCount = -1;
	protected long timeInterval = -1;
	
	@Override
	public void run() {
		boolean hasChanged = false;
		while(true) {	
			for(OutputFormat out :conf.getOutputformats()) {
				// call a second time if it's the first time and show header is active
				if (out.isFirstTime() && out.isShowHeader()) out.output(); 
				// normal call
				out.preOutput();
				if (!(out.isAttributeValuesEqualToLastRun())) {
					out.processOutput();
					out.posOutput();
					hasChanged = true;
				}
			}
			
			if (hasChanged) {
				hasChanged = false;
			}
			
			OutputFormat.cleanAttributeValues();
			increaseGeneralCounter();
			
			try {
				Thread.sleep(timeInterval);
			} catch (InterruptedException e) {
			}
		}
		
	}
	
	/**
	 * Increase the general counter
	 */
	public void increaseGeneralCounter() {
		conf.increaseCounter();
	}
	
	/**
	 * Instantiates EventCollectorOutpu
	 * 
	 * @param conf The global configuration parameters in use (this is where the time interval will be retrived)
	 * @param count How many times the collector process will execute (<=0 infinite)
	 */
	public EventCollectorOutput(Config conf, long count) {
		this.conf = conf;
		internalCount = count;
		
		if (conf.getTimeInterval() > -1) {
			timeInterval = conf.getTimeInterval();
		} else {
			// Default to 1 second if nothing specified
			timeInterval = 1000;
		}
		
		run();
	}
	
	@Override
	public Config getConfig() {
		return conf;
	}

}
