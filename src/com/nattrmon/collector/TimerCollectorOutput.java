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

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.nattrmon.collector.TimerCollectorOutput.OutputThread;
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
	protected boolean waitForOutput = true;
	protected long timeout = -1;
	protected ReentrantLock startSignal = null;
	protected CyclicBarrier doneSignal = null;
	protected Integer myTotalThreads = 0;
	protected static ScheduledExecutorService fillOutputs;
	protected static ExecutorService execOutputs;
	protected static ArrayList<OutputThread> outputs;

	//protected class TimerOutputTask implements Runnable {
		//ConcurrentHashMap<OutputFormat, OutputThread> threads = new ConcurrentHashMap<OutputFormat, OutputThread>();
//		int currentCount = 0;
//		
//		synchronized void incCount() {
//			currentCount++;
//		}
//		
//		synchronized void decCount() {
//			currentCount--;
//		}
//		
//		synchronized int getCount() {
//			return currentCount;
//		}
//		
//		synchronized void zeroCount() {
//			currentCount = 0;
//		}
		
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
//							try {
//								startSignal.lockInterruptibly();
//							} catch (InterruptedException e1) {
//							}
							
							long counter = conf.getCurrentCounter();
							
							// call a second time if it's the first time and show header is active
							if (output.isFirstTime() && output.isShowHeader()) {
								output.output(-1); 
							}
							// normal call
							output.output(counter);
							
//							try {
//								doneSignal.await();
//							} catch (InterruptedException e) {
//							} catch (BrokenBarrierException e) {
//							}
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
//				setPriority(Thread.MAX_PRIORITY - 1);
//				synchronized (threads) {
//					threads.put(o, this);
//				}
			}
		}
		
//		@Override
//		public void run() {
//			
//			// Set threads if needed
//			for(OutputFormat out :conf.getOutputformats()) {
//				boolean contains;
//				synchronized (threads) {
//					contains = threads.containsKey(out);
//				}
//				if (contains) {
//					// Nothing.. for now
//				} else {
//					OutputThread ot = new OutputThread(out);
//					ot.setPriority(Thread.MAX_PRIORITY - 1);
//					ot.setDaemon(true);
//					ot.start();
//					
//					while(!(ot.getState().equals(State.WAITING)) && !(ot.getState().equals(State.TERMINATED))) {
//						try {
//							Thread.sleep(50);
//						} catch (InterruptedException e) {
//						}
//					}
//
//				}
//			}
//			
//			if (doneSignal == null) {
//				doneSignal = new CyclicBarrier(myTotalThreads);
//			} else {
//				doneSignal.reset();
//			}
//			
//			// Start threads
//			synchronized(threads) {
//				for(OutputFormat t : threads.keySet()) {
//					if (threads.get(t).getState().equals(State.WAITING)) {
//						threads.get(t).setPriority(Thread.MAX_PRIORITY - 1);
//						threads.get(t).interrupt();
//					}
//				}
//			}
//			
//			long timeInWaitingLimit;
//			if (timeout != -1) {
//				timeInWaitingLimit = timeout;
//			} else {
//				timeInWaitingLimit = conf.getTimeInterval() * 5 * 1000;
//			}
//			
//			boolean wasItTimeOut = false;
//			
//			if (waitForOutput) {
//				try {
//					doneSignal.await();
//				} catch (InterruptedException e) {
//				}
//				catch (BrokenBarrierException e) {
//				}
//			} else {
//				try {
//					doneSignal.await(timeInWaitingLimit, TimeUnit.MILLISECONDS);
//				} catch (InterruptedException e) {
//				}
//				  catch (BrokenBarrierException e) {
//				} catch (TimeoutException e) {
//					wasItTimeOut = true;
//				}
//			}
//
//			
//			// Put to sleep threads
//			synchronized (threads) {
//				for(OutputFormat t : threads.keySet()) {
//					while (!(threads.get(t).getState().equals(State.WAITING))) {
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//						}
//					}
//					threads.get(t).setPriority(Thread.MIN_PRIORITY);
//				}
//			}
//			
//			//OutputFormat.cleanAttributeValues();
//			increaseGeneralCounter();
//			if (internalCount > 0) {
//				if (conf.getCurrentCounter() >= internalCount) {
//					cancel();
//					System.exit(0);
//					conf.lOG(OutputType.DEBUG, "Done");
//				}
//			}  
//			
//			if (wasItTimeOut)
//				conf.lOG(OutputType.DEBUG, "Time out waiting for output threads (" + timeInWaitingLimit + "ms)");
			
			// Before starting clean previous values
//			OutputFormat.cleanAttributeValues();
//			increaseGeneralCounter();
//			if (internalCount > 0) {
//				if (conf.getCurrentCounter() >= internalCount) {
//					cancel();
//					System.exit(0);
//					conf.lOG(OutputType.DEBUG, "Done");
//				}
//			}
//		}
//		
//		public TimerOutputTask() {
//			super();
//			
//			Thread.currentThread().setPriority(Thread.MAX_PRIORITY -1);
//		}
//		
//	}

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
//					if (prop[0].equalsIgnoreCase("wait"))
//						if (prop[1].equalsIgnoreCase("y"))
//							waitForOutput = true;
//					if (prop[0].equalsIgnoreCase("timeout"))
//						try { timeout = Long.valueOf(prop[1]); } catch (Exception e) {} 
				}
			}
		}
		
		fillOutputs = Executors.newScheduledThreadPool(1);
		
//		if (startSignal == null) {
//			startSignal = new ReentrantLock();
//			startSignal.lock();
//		}
		
		//try {
		//t = new TimerOutputTask();
		//tt = new Timer();
	
	}
	
	public void increaseGeneralCounter() {
		conf.increaseCounter();
	}
	
	public void run() {
		ScheduledFuture<?> f;
		if (waitForOutput) {
			f = fillOutputs.scheduleWithFixedDelay(new OutputsThread(), 0, timeInterval, TimeUnit.MILLISECONDS);
		} else {
			f = fillOutputs.scheduleAtFixedRate(new OutputsThread(), timeInterval, 0, TimeUnit.MILLISECONDS);
		}
	}


	public Config getConfig() {
		return conf;
	}
	
	
}
