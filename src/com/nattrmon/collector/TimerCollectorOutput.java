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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.core.UniqueAttributes;
import com.nattrmon.output.Output.OutputType;


/**
 * Timer collector based output. 
 * Uses the configured time interval to implement a collector output (defaults to
 * one second if nothing is specified)
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class TimerCollectorOutput implements CollectorOutput  {
	protected Config conf;
	protected long internalCount = -1;
	protected long timeInterval = -1;
	
	protected class TimerOutputTask extends TimerTask {
		@Override
		public void run() {
			for(OutputFormat out :conf.getOutputformats()) {
				// call a second time if it's the first time and show header is active
				if (out.isFirstTime() && out.isShowHeader()) out.output(); 
				// normal call
				out.output();
			}
			
			// Before starting clean previous values
			OutputFormat.cleanAttributeValues();
			increaseGeneralCounter();
			if (internalCount > 0) {
				if (conf.getCurrentCounter() >= internalCount) {
					cancel();
					System.exit(0);
					conf.lOG(OutputType.DEBUG, "Done");
				}
			}
		}
	}

	/**
	 * Instantiates a TimerCollectorOutput
	 * 
	 * @param conf The global configuration parameters in use (this is where the time interval will be retrived)
	 * @param count How many times the collector process will execute (<=0 infinite)
	 */
	public TimerCollectorOutput(Config conf, long count) {
		this.conf = conf;
		internalCount = count;
		
		TimerTask t;
		Timer tt;

		//try {
		t = new TimerOutputTask();
		tt = new Timer();
		
		if (conf.getTimeInterval() > -1) {
			timeInterval = conf.getTimeInterval();
		} else {
			// Default to 1 second if nothing specified
			timeInterval = 1000;
		}
		
		tt.scheduleAtFixedRate(t, 0, timeInterval);
	}
	
	public void increaseGeneralCounter() {
		conf.increaseCounter();
	}
	
	public void run() {

	}

	public Config getConfig() {
		return conf;
	}
}
