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
	protected TimerTask t;
	protected Timer tt;

	protected class TimerOutputTask extends TimerTask {
		HashMap<OutputFormat, OutputThread> threads = new HashMap<OutputFormat, OutputThread>();
		int currentCount = 0;
		
		synchronized void incCount() {
			currentCount++;
		}
		
		synchronized void decCount() {
			currentCount--;
		}
		
		synchronized int getCount() {
			return currentCount;
		}
		
		synchronized void zeroCount() {
			currentCount = 0;
		}
		
		class OutputThread extends Thread {
			Object lock = new Object();
			OutputFormat output;
			
			public void run() {
				while (true) {
					synchronized (lock) {
						try {
							try {
								lock.wait();
							} catch (InterruptedException e) {
							}
							
							// call a second time if it's the first time and show header is active
							if (output.isFirstTime() && output.isShowHeader()) output.output(); 
							// normal call
							output.output();
						} catch (Exception e) {
							conf.lOG(OutputType.DEBUG, "Exception", e);
						} finally {
							decCount();
						}
					}
				}
			}
			
			public OutputThread(OutputFormat o) {
				super();
				
				output = o;
				setPriority(Thread.MAX_PRIORITY - 1);
			}
		}
		
		@Override
		public void run() {
			
			// Set threads if needed
			zeroCount();
			for(OutputFormat out :conf.getOutputformats()) {
				incCount();
				if (threads.containsKey(out)) {
					// Nothing.. for now
				} else {
					OutputThread ot = new OutputThread(out);
					ot.setPriority(Thread.MAX_PRIORITY - 1);
					ot.setDaemon(true);
					ot.start();
					threads.put(out, ot);
				}
			}
			
			// Start threads
			for(OutputFormat t : threads.keySet()) {
				threads.get(t).setPriority(Thread.MAX_PRIORITY - 1);
				threads.get(t).interrupt();
			}
			
			// Wait for threads
			while(getCount() > 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
			
			// Put to sleep threads
			for(OutputFormat t : threads.keySet()) {
				threads.get(t).setPriority(Thread.MIN_PRIORITY);
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
		
		public TimerOutputTask() {
			super();
			
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		}
		
	}

	/**
	 * Instantiates a TimerCollectorOutput
	 * 
	 * @param conf The global configuration parameters in use (this is where the time interval will be retrieved)
	 * @param count How many times the collector process will execute (<=0 infinite)
	 */
	public TimerCollectorOutput(Config conf, long count) {
		this.conf = conf;
		internalCount = count;

		if (conf.getTimeInterval() > -1) {
			timeInterval = conf.getTimeInterval();
		} else {
			// Default to 1 second if nothing specified
			timeInterval = 1000;
		}

		//try {
		t = new TimerOutputTask();
		tt = new Timer();
	
	}
	
	public void increaseGeneralCounter() {
		conf.increaseCounter();
	}
	
	public void run() {
		tt.scheduleAtFixedRate(t, 0, timeInterval);
	}

	public Config getConfig() {
		return conf;
	}
}
