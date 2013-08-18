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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.nattrmon.collector.TimerCollectorOutput.OutputThread;
import com.nattrmon.collector.TimerCollectorOutput.OutputsThread;
import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;

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
	protected HashMap<OutputFormat, OutputThread> threads = new HashMap<OutputFormat, OutputThread>();
	protected int currentCount = 0;
	protected boolean hasChanged = false;
	protected static ScheduledExecutorService fillOutputs;
	protected static ExecutorService execOutputs;
	protected static ArrayList<OutputThread> outputs;
	protected boolean waitForOutput = true;
	
//	synchronized void incCount() {
//		currentCount++;
//	}
//	
//	synchronized void decCount() {
//		currentCount--;
//	}
//	
//	synchronized int getCount() {
//		return currentCount;
//	}
//	
//	synchronized void zeroCount() {
//		currentCount = 0;
//	}
//	
	synchronized boolean isChanged() {
		return hasChanged;
	}
	
	synchronized void setChanged(boolean c) {
		hasChanged = c;
	}
	
//	class OutputThread extends Thread {
//		Object lock = new Object();
//		OutputFormat output;
//		
//		public void run() {
//			while (true) {
//				synchronized (lock) {
//					try {
//						try {
//							lock.wait();
//						} catch (InterruptedException e) {
//						}
//
//						long counter = conf.getCurrentCounter();
//						// call a second time if it's the first time and show
//						// header is active
//						if (output.isFirstTime() && output.isShowHeader())
//							output.output(counter);
//
//						// normal call
//						output.preOutput(counter);
//						if (!(output.isAttributeValuesEqualToLastRun())) {
//							output.processOutput(counter);
//							output.posOutput(counter);
//							setChanged(true);
//						}
//					} catch (Exception e) {
//						conf.lOG(OutputType.ERROR, "Exception", e);
//					} finally {
//						decCount();
//					}
//				}
//			}
//		}
//		
//		public OutputThread(OutputFormat o) {
//			super();
//			
//			output = o;
//			setPriority(Thread.MAX_PRIORITY - 1);
//		}
//	}
//	
//	@Override
//	public void run() {
//
//		while(true) {	
//			
//			// Set threads if needed
//			zeroCount();
//			for(OutputFormat out :conf.getOutputformats()) {
//				incCount();
//				if (threads.containsKey(out)) {
//					// Nothing.. for now
//				} else {
//					OutputThread ot = new OutputThread(out);
//					ot.setPriority(Thread.MAX_PRIORITY - 1);
//					ot.setDaemon(true);
//					ot.start();
//					threads.put(out, ot);
//				}
//			}
//			
//			// Start threads
//			for(OutputFormat t : threads.keySet()) {
//				threads.get(t).setPriority(Thread.MAX_PRIORITY - 1);
//				threads.get(t).interrupt();
//			}
//			
//			// Wait for threads
//			while(getCount() > 0) {
//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//				}
//			}
//			
//			// Put to sleep threads
//			for(OutputFormat t : threads.keySet()) {
//				threads.get(t).setPriority(Thread.MIN_PRIORITY);
//			}
//			
//			if (isChanged()) {
//				setChanged(false);
//			}
//			
//			OutputFormat.cleanAttributeValues();
//			increaseGeneralCounter();
//			
//			if (internalCount > 0) {
//				if (conf.getCurrentCounter() >= internalCount) {
//					System.exit(0);
//					conf.lOG(OutputType.DEBUG, "Done");
//				}
//			}
//			
//			try {
//				Thread.sleep(timeInterval);
//			} catch (InterruptedException e) {
//			}
//		}
//		
//	}
	
	/**
	 * Increase the general counter
	 */
	public void increaseGeneralCounter() {
		conf.increaseCounter();
	}
	
	/**
	 * Instantiates EventCollectorOutput
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
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		String settingsParams = conf.getParams();
		if (settingsParams != null) {
			String pms[] = settingsParams.split(";");
			String tmp = "";
			String prop[];
	
			for (String p : pms) {
				tmp = p.trim();
				prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("wait"))
						if (prop[1].equalsIgnoreCase("y"))
							waitForOutput = true;
//					if (prop[0].equalsIgnoreCase("timeout"))
//						try { timeout = Long.valueOf(prop[1]); } catch (Exception e) {} 
				}
			}
		}
		
		fillOutputs = Executors.newScheduledThreadPool(1);
		
		run();
	}
	
	public void run() {
		ScheduledFuture<?> f;
		if (waitForOutput) {
			f = fillOutputs.scheduleWithFixedDelay(new OutputsThread(), 0, timeInterval, TimeUnit.MILLISECONDS);
		} else {
			f = fillOutputs.scheduleAtFixedRate(new OutputsThread(), timeInterval, 0, TimeUnit.MILLISECONDS);
		}
	}
	
	@Override
	public Config getConfig() {
		return conf;
	}
	
	class OutputsThread implements Runnable {

		@Override
		public void run() {
			if (execOutputs == null)
				execOutputs = Executors.newFixedThreadPool(conf.getOutputformats().size());
			
			if (outputs == null) {
				outputs = new ArrayList<OutputThread>();
				for(OutputFormat out : conf.getOutputformats()) {
					outputs.add(new OutputThread(out));
				}
			}
			
			try {
				execOutputs.invokeAll(outputs);
			} catch (InterruptedException e) {
			}
			
			if (isChanged()) {
				setChanged(false);
			}
			increaseGeneralCounter();
		}
		
	}
	
	class OutputThread implements Callable<Boolean> {
		//Object lock = new Object();
		OutputFormat output;
		
		public Boolean call() {
			//while (true) {
				//synchronized (lock) {
					try {
//						try {
//							startSignal.lockInterruptibly();
//						} catch (InterruptedException e1) {
//						}
						
						long counter = conf.getCurrentCounter();
						
						// call a second time if it's the first time and show header is active
						if (output.isFirstTime() && output.isShowHeader()) {
							output.output(-1); 
						}
						
						// normal call
						output.preOutput(counter);
						if (!(output.isAttributeValuesEqualToLastRun())) {
							output.processOutput(counter);
							output.posOutput(counter);
							setChanged(true);
						}
					} catch (Exception e) {
						conf.lOG(OutputType.DEBUG, "Exception", e);
					} finally {
						//decCount();
					}
					
					return true;
				//}
			//}
		}
		
		public OutputThread(OutputFormat o) {
			output = o;
//			setPriority(Thread.MAX_PRIORITY - 1);
//			synchronized (threads) {
//				threads.put(o, this);
//			}
		}
	}

}
