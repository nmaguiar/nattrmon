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

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.nattrmon.config.Config;
import com.nattrmon.output.Output.OutputType;
import com.sun.jmx.snmp.tasks.ThreadService;

/**
 * Abstract class for implementing OutputFormat classes.
 * Most of the base functionality for OutputFormat is provided here.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class OutputFormat {
	final static public String NOT_AVAILABLE = "n/a";
	final static public String TIME_OUT = "t/o";
	final static public String NOT_RETRIEVED = "n/r";
	final static public String BEING_RETRIEVED = "b/r";
	protected static ExecutorService fillThreads = Executors.newCachedThreadPool();
	protected static ConcurrentLinkedQueue<String> attributesToFill;
	protected static java.lang.Object lock = new java.lang.Object();
	protected Config conf;
	protected String text;
	protected String param;
	protected boolean firstTime = true;
	protected boolean showHeader = true;
	protected static boolean refreshValues = true;
	protected static Boolean determineParallelFill = true;
	protected static boolean useParallelFill = false;
	protected boolean processOutput = true;
	protected boolean waitForServices = true;
	protected long timeout = -1;
	protected static CyclicBarrier outputsSignal = null;
	protected static Semaphore startSignal = null;
	protected static CyclicBarrier doneSignal = null;
	//protected static Counter countThreads = new Counter();
	protected static ConcurrentHashMap<String, ValueThread> threads = new ConcurrentHashMap<String, ValueThread>();
	protected ConcurrentHashMap<String, Thread> localThreads = new ConcurrentHashMap<String, Thread>();
	protected static ArrayList<String> internalAttrs = new ArrayList<String>();
	protected static int numberOfDisplayableAttrs = 0;
	protected ArrayList<String> attributes = null;
	public int myTotalThreads = 0;
	protected long localCounter;
	protected Boolean jobDone = false;
	//protected static Semaphore jobDone = new Semaphore(0);
	protected boolean cancelThread = false;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		attributes = null;
		this.text = text;
		
		if (numberOfDisplayableAttrs < getAttrNames().size())
			numberOfDisplayableAttrs = getAttrNames().size();
	}

	/**
	 * Registers the OutputFormat on the environment for use.
	 * 
	 */
	public static void register() {
    	
    }
	
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	/**
	 * Initializes an OutputFormat. 
	 * Parameters should be handled by extensions to this class.
	 * 
	 * @param conf The general configuration to use.
	 * @param param The parameters passed on the configuration
	 */
	public OutputFormat(Config conf, String param) {
		this.conf = conf;
		this.param = param;
		
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
							waitForServices = true;
						else
							waitForServices = false;
					if (prop[0].equalsIgnoreCase("timeout"))
						try { timeout = Long.valueOf(prop[1]); } catch (Exception e) {} 
					if (prop[0].equalsIgnoreCase("serial")) 
						if (prop[1].equalsIgnoreCase("y")) {
							determineParallelFill = false;
							useParallelFill = false;
						} else {
							determineParallelFill = false;
							useParallelFill = true;							
						}
					if (prop[0].equalsIgnoreCase("parallel")) 
						if (prop[1].equalsIgnoreCase("y")) {
							determineParallelFill = false;
							useParallelFill = true;
						} else {
							determineParallelFill = false;
							useParallelFill = false;
						}
					if (prop[0].equalsIgnoreCase("cancelThread")) 
						if (prop[1].equalsIgnoreCase("y")) 
							cancelThread = true;
				}
			}
		}
	}
	
	public long getLocalCounter() {
		return localCounter;
	}

	/**
	 * Main output method. Should be called whenever necessary to 
	 * output data.
	 */
	public void output(long counter) {	
		preOutput(counter);
		//if (processOutput) {
			processOutput(counter);
			posOutput(counter);
		//}
		//processOutput();
	}
	
	/**
	 * Is the processing of the output enabled or just
	 * pre-output?
	 */
	public boolean isProcessOutput() {
		return processOutput;
	}

	/**
	 * Set if the processing of the output shoud be performed or not
	 */
	public void setProcessOutput(boolean inFill) {
		this.processOutput = inFill;
	}

	/**
	 * To be called before actually performing the output
	 * 
	 */
	public void preOutput(long counter) {
		//if (inFill == false) {
		localCounter = counter; 
		refreshValues = true;
		
			if (useParallelFill()) {
				parallelFillAttributeValues(counter);
			} else {
				fillAttributesValues(counter);
			}
			//inFill = true;
		//} 
	}
		
	/**
	 * Should be called only from output method. Classes extending this one
	 * should implement this method.
	 * @param counter 
	 * 
	 */
	public void processOutput(long counter) {
		
	}
	
	public void posOutput(long counter) {
		if (firstTime) firstTime = false;
		//inFill = false;
	}
	
	public long getGeneralCounter() {
		return conf.getCurrentCounter();
	}

	/**
	 * Is this the first invocation? (for including headers)
	 * 
	 * @return true if this is the first invocation
	 */
	public boolean isFirstTime() {
		return firstTime;
	}

	/**
	 * Should the header be displayed? 
	 * 
	 * @return true if it should
	 */
	public boolean isShowHeader() {
		return showHeader;
	}
	
	/**
	 * Obtain a list of the attributes names.
	 * 
	 * @return a list of attribute names
	 */
	protected ArrayList<String> getAttrNames() {
		if (attributes == null) {
			String orderText = getText();

			attributes = new ArrayList<String>();
			
			if (orderText != null) {		
				orderText = orderText.replaceAll("[ \n\r\t]", "");
				orderText = orderText.trim();
				
				ArrayList<String> res = new ArrayList<String>(Arrays.asList(orderText.split(",")));
				if (res.contains(null) || res.contains("")) {
					conf.lOG(OutputType.ERROR, "Bad attribute on list for output '" + this.text + "'");
				} else {
					attributes = new ArrayList<String>(Arrays.asList(orderText.split(",")));
				}
			}
		} 
		
		return attributes;
	}
	
	/**
	 * Determine if a parallel fill should be used.
	 * 
	 * @return Returns true if parallel fill should be used.
	 */
	public boolean useParallelFill() {
		if (determineParallelFill) {
			if (useParallelFill) return true;
			boolean use = false;
			UniqueAttributes ua = conf.getUniqueAttrs();
			Attribute atr = null;

			int countHeavy = 0;
			int countInternal = 0;
			int count = 0;

			for(String attr : getAttrNames()) {
				atr = ua.getAttribute(attr);
				if ( (atr != null) && (atr.isHeavy) ) countHeavy++; 
				if ( (atr != null) && (atr.isInternal) ) countInternal++;
				count++;
			}

			conf.lOG(OutputType.DEBUG, "Use parallel fill? Heavy=" + countHeavy + "; Internal=" + countInternal + "; Total = " + count);

			// Temporary until better found
			if (countHeavy >= 2) 
				use = true;
			
			useParallelFill = use;
			determineParallelFill = false;
			return use;
		} else {
			return useParallelFill;
		}
	}
	
	/**
	 * Fill all unique attributes with values in a sequential way.
	 * 
	 */
	public void fillAttributesValues(long counter) {
		UniqueAttributes ua = conf.getUniqueAttrs();
		Attribute atr = null;
		ArrayList<String> lastToRun = new ArrayList<String>();
		//long counter = conf.getCurrentCounter();
		
		for(String attr :getAttrNames()) {
			if (!(conf.containsCurrentAttributeValues(counter, attr))) {
				if (attr != null) {
					atr = ua.getAttribute(attr);
					if (atr != null) {
						if (ua.getAttribute(attr).isInternal) {
							lastToRun.add(attr);
						} else {
							conf.setCurrentAttributeValues(counter, attr, atr.getValue());
						}
					}				
				}
			}
		}
			
		// Internal attributes must be the last to run
		for(String attr2 :lastToRun) {
			if (!(conf.containsCurrentAttributeValues(counter, attr2))) {
				if (attr2 != null) {
					atr = ua.getAttribute(attr2);
					if (atr != null) {
						conf.setCurrentAttributeValues(counter, attr2, atr.getValue());
					}				
				}			
			}
		}
		
		//refreshValues = false;
	}
	
	/**
	 * Determine if attribute values are equal from the last counter
	 */
	public boolean isAttributeValuesEqualToLastRun() {
		UniqueAttributes ua = conf.getUniqueAttrs();
		Attribute atr = null;
		long counter = conf.getCurrentCounter();
		
		if (isFirstTime() || counter < 1) return false;

		if (!(conf.getCurrentAttributeValues4Counter(counter).equals(conf.getCurrentAttributeValues4Counter(counter -1)))) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Given an attribute name determine if an attribute value is equal to the value on the last counter 
	 */
	public boolean isAttributeValueEqualToLastRun(String attrName) {
		UniqueAttributes ua = conf.getUniqueAttrs();
		Attribute atr = null;
		long counter = conf.getCurrentCounter();
		
		if (isFirstTime() || counter < 1) return false;

		if(!(conf.getCurrentAttributeValues(counter, attrName).equals(conf.getCurrentAttributeValues(counter - 1, attrName)))) {
			return false;
		}
		
		return true;		
	}
	
	/**
	 * Sub-class to retrieve attribute values
	 * 
	 *
	 */
	class ValueThread extends Thread {
		protected Attribute a;
		boolean shouldRun = false;
		
		public ValueThread(String atr) {
			a = conf.getUniqueAttrs().getAttribute(atr);
			setName(atr);
			shouldRun = true;
			
//			synchronized (threads) {
//				if (threads.containsKey(a.getUniqueName())) {
//					shouldRun = false;
//				} else {
//					threads.put(a.getUniqueName(), this);
//					synchronized (localThreads) {
//						localThreads.put(a.getUniqueName(), this);
//					}
//					shouldRun = true;
//				}
//			}
		}
		
		public void run() {
			if (shouldRun) myTotalThreads++;
			while (shouldRun) {
				startSignal.acquireUninterruptibly();
				
				long start = System.currentTimeMillis();

				if (a != null) {
					conf.lOG(OutputType.DEBUG, "Thread processing attribute: [" + getLocalCounter() + "] " + a.getUniqueName());
					String ss = a.getValue();
					setResult(ss);
				}
				conf.lOG(
						OutputType.DEBUG,
						"Thread processing attribute [" + getLocalCounter() + "] "
								+ a.getUniqueName() + " took "
								+ (System.currentTimeMillis() - start)
								+ " to run.");
				
				try {
					doneSignal.await();
				} catch (InterruptedException e) {
				} catch (BrokenBarrierException e) {
				}
			}
		}
		
		public void setResult(String r) {
			if (conf.isCurrentAttributeValue(getLocalCounter(), a.getUniqueName(), OutputFormat.BEING_RETRIEVED))
				conf.setCurrentAttributeValues(getLocalCounter(), a.getUniqueName(), r);
			else 
				conf.setCurrentAttributeValues(conf.getCurrentCounter(), a.getUniqueName(), r);
		}
	}

	protected void prepareThreads(long counter) {
		ArrayList<String> runNow = new ArrayList<String>();
		// If no start signal lock exists, make one
		synchronized(threads) {
			if (startSignal == null) {
				startSignal = new Semaphore(-numberOfDisplayableAttrs); // don't forget to reduce internal
			}
			//if (startSignal.availablePermits() < numberOfDisplayableAttrs);
		}
		
		// First group of attributes (non internal)
		synchronized(threads) {
			for(String attr :getAttrNames()) {
				// Is it already being handled?
				if (!(conf.containsCurrentAttributeValues(counter, attr))) {
					if (attr != null) {
						if (conf.getUniqueAttrs().getAttribute(attr) != null) {
								conf.setCurrentAttributeValues(counter, attr, OutputFormat.BEING_RETRIEVED);
								if (conf.getUniqueAttrs().getAttribute(attr).isInternal) {
									if (!(internalAttrs.contains(attr))) internalAttrs.add(attr);
								} else {
									if (!(threads.containsKey(attr))) runNow.add(attr);
								}
						}
					}
				}
			}
						
			for(String attr : runNow) {
				ValueThread t = new ValueThread(attr);
				t.setPriority(Thread.MAX_PRIORITY-3);
				t.start();
				
				// Ensure thread starts and is ready and waiting to perform work or died
				while(!(t.getState().equals(State.WAITING)) && !(t.getState().equals(State.TERMINATED))) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
				threads.put(attr, t);
			}
		}
	}
	
	protected synchronized void prepareDoneSignal() {
		final String IIIIII = this.getClass().getName();
		if (doneSignal == null) {
			Runnable runInternal = new Runnable() {
				@Override
				public void run() {
					Attribute atr;
					// Run internal, if necessary, in sequential fashion
					for(String lastAtr : internalAttrs) {
						if (conf.isCurrentAttributeValue(getLocalCounter(), lastAtr, OutputFormat.BEING_RETRIEVED)) {
							atr = conf.getUniqueAttrs().getAttribute(lastAtr);
							startSignal.acquireUninterruptibly();
							conf.lOG(OutputType.DEBUG, "Starting processing internal attribute: [" + getLocalCounter() + "] " + lastAtr);
							if (atr != null) conf.setCurrentAttributeValues(getLocalCounter(), lastAtr, atr.getValue()); 
							conf.lOG(OutputType.DEBUG, "Processed internal attribute: [" + getLocalCounter() + "] " + lastAtr);
						}
					}
					jobDone = true;
				}			
			};
			
			//if (threads.size() > 0) 
			doneSignal = new CyclicBarrier(numberOfDisplayableAttrs - internalAttrs.size(), runInternal);
			//else 
			//	doneSignal = new CyclicBarrier(1, runInternal);
		} else {
		}
	}
	
	class FillValues implements Callable<String> {
		String atr = null;
		
		public FillValues(String attr) {
			atr = attr;
		}
		@Override
		
		public String call() throws Exception {
			try {
				conf.setCurrentAttributeValues(getLocalCounter(), atr, OutputFormat.NOT_RETRIEVED);
				long start = System.currentTimeMillis();
				conf.lOG(OutputType.DEBUG, "Thread processing attribute: [" + getLocalCounter() + "] " + atr);
				conf.setCurrentAttributeValues(getLocalCounter(), atr, conf.getUniqueAttrs().getAttribute(atr).getValue());
				conf.lOG(
						OutputType.DEBUG,
						"Thread processing attribute [" + getLocalCounter() + "] "
								+ atr + " took "
								+ (System.currentTimeMillis() - start)
								+ " to run.");
			} catch(Exception e) {
				conf.lOG(OutputType.DEBUG, "Exception for [" + getLocalCounter() + "] - " + atr, e);
			}
			return atr;
		}
		
		public String getAttributeName() {
			return atr;
		}
		
	}
	
	public void parallelFillAttributeValues(long counter) {
		synchronized(lock) {
			if (refreshValues) {
				if (attributesToFill == null) attributesToFill = new ConcurrentLinkedQueue<String>();

				ArrayList<String> internal = new ArrayList<String>();
				ArrayList<String> normal = new ArrayList<String>();
				for(Attribute attr :conf.getUniqueAttrs().getAttrs().values()) {
					if (attr.isInternal) {
						if (!(internal.contains(attr.uniqueName))) {
							internal.add(attr.uniqueName);
						}
					} else {
						if (!(normal.contains(attr.uniqueName))) {
							normal.add(attr.uniqueName);
						}
					}
				}
				
//				for(String attr : internal) {
//					if (!(attributesToFill.contains(attr))) {
//						attributesToFill.add(attr);
//					}
//				}
				
				for(String attr : normal) {
					if (!(attributesToFill.contains(attr))) {
						attributesToFill.add(attr);
					}
				}
				
	//			}
			
				//ArrayList<Callable> tasks = new ArrayList<Callable>();
				ArrayList<FillValues> tasksToRun = new ArrayList<FillValues>();
				for(String attr : attributesToFill) {
					FillValues task;
					try {
						task = new FillValues(attr);
						if (task != null) tasksToRun.add(task);
					} catch (Exception e) {
						conf.lOG(OutputType.DEBUG, "Exception for [" + getLocalCounter() + "] - " + e.getMessage());
					}
					
				}
				
				//for(Future task : tasks) {

				List<Future<String>> retList = null;
				if (waitForServices)
					try {
						retList = fillThreads.invokeAll(tasksToRun);
					} catch (InterruptedException e) {
						conf.lOG(OutputType.DEBUG, "InterruptedException", e);
					} catch (RejectedExecutionException e) {
						conf.lOG(OutputType.DEBUG, "RejectedExecutionException", e);
					}
				else {
					try {
						retList = fillThreads.invokeAll(tasksToRun, timeout, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						conf.lOG(OutputType.DEBUG, "InterruptedException", e);
					} catch (RejectedExecutionException e) {
						conf.lOG(OutputType.DEBUG, "RejectedExecutionException", e);
					}
				}
				
				if (retList != null)
					for(Future<String> ret : retList) {
						if (ret.isCancelled()) {
							try {
								conf.setCurrentAttributeValues(getLocalCounter(), ret.get(), OutputFormat.NOT_RETRIEVED);
							} catch (InterruptedException e) {
								conf.lOG(OutputType.DEBUG, "InterruptedException", e);
								if (cancelThread) ret.cancel(true);
							} catch (ExecutionException e) {
								conf.lOG(OutputType.DEBUG, "ExecutionException", e);
								if (cancelThread) ret.cancel(true);
							} catch (CancellationException e) {
								conf.lOG(OutputType.DEBUG, "Canceled thread", e);
								if (cancelThread) ret.cancel(true);
							} 
						}
					}
				
				Attribute atr;
				long start;
				for(String attr2 :internal) {
					if (!(conf.containsCurrentAttributeValues(counter, attr2))) {
						if (attr2 != null) {
							atr = conf.getUniqueAttrs().getAttribute(attr2);
							if (atr != null) {
								conf.lOG(OutputType.DEBUG, "Processing attribute: [" + getLocalCounter() + "] " + atr.getUniqueName());
								start = System.currentTimeMillis();
								conf.setCurrentAttributeValues(counter, attr2, atr.getValue());
								conf.lOG(
										OutputType.DEBUG,
										"Thread processing attribute [" + getLocalCounter() + "] "
												+ atr.getUniqueName() + " took "
												+ (System.currentTimeMillis() - start)
												+ " to run.");
							}				
						}			
					}
				}
				
				
//				//}	
			}
			refreshValues = false;
		}
	}
	/**
	 * Fill all unique attributes with values in parallel processing.
	 * 
	 */
	public void parallelFillAttributeValues2(long counter) {
		UniqueAttributes ua = conf.getUniqueAttrs();
		//Semaphore jobDone = new Semaphore(1);
	
		jobDone = false;
		
		// 1 - PREPARE THREADS
		// -------------------	
		prepareThreads(counter);
		
		// 2 - WAIT FOR EVERY OUTPUT PREPARES THREADS
		// ------------------------------------------
		int totalNumberAttributes = numberOfDisplayableAttrs;
		synchronized(threads) {
			threads.notifyAll();
		}
		
		while (internalAttrs.size() + startSignal.getQueueLength() < totalNumberAttributes) {
			try {
				synchronized (threads) {
					threads.wait();
				}
			} catch (InterruptedException e) {
			}
		}

		// 3 - START EVERYONE
		// ------------------
		
		// 3.1 - PREPARE DONE SIGNAL
		// -------------------------
		prepareDoneSignal();
		
		// 3.2 - SEND SIGNAL
		// -----------------
		synchronized(threads) {
			startSignal.release(startSignal.getQueueLength());
		}
		
		long timeInWaitingLimit;
		if (timeout != -1) {
			timeInWaitingLimit = timeout;
		} else {
			timeInWaitingLimit = conf.getTimeInterval() * 5 * 1000;
		}
		boolean wasItTimeOut = false;

		
		if (waitForServices) {		
			try {
				doneSignal.await();
			} catch (InterruptedException e) {
			}
			catch (BrokenBarrierException e) {
			}
			
			synchronized(jobDone) {
				jobDone.notifyAll();
			}
			
			while(jobDone != true) {
				try {
					synchronized(jobDone) {
						jobDone.wait();
					}
				} catch (InterruptedException e) {
				}
			}
		} else {
			try {
				doneSignal.await(timeInWaitingLimit, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			} catch (BrokenBarrierException e) {
			} catch (TimeoutException e) {
				wasItTimeOut = true;
			}
		}
		
		// Ensure everyone executed
//		//for (String attr : getAttrNames()) {
//			if (waitForServices) {
//				synchronized(threads) {
//					threads.notifyAll();
//				} 
//				
//				while( (startSignal.getQueueLength() < (numberOfDisplayableAttrs - this.internalAttrs.size())
//						&& jobDone == false)) {
//					try {
//						threads.wait();
//					} catch (InterruptedException e) {
//					}
//				}
//			} else {
//				for (String attr : getAttrNames()) {
//					if (conf.isCurrentAttributeValue(getLocalCounter(), attr, OutputFormat.BEING_RETRIEVED)) {
//						if (wasItTimeOut) {
//							conf.setCurrentAttributeValues(getLocalCounter(), attr, OutputFormat.TIME_OUT);
//						} else {
//							conf.setCurrentAttributeValues(getLocalCounter(), attr, OutputFormat.NOT_RETRIEVED);
//						}
//					}
//				}
//			}
		//}		
		//refreshValues = false;
		
		if (wasItTimeOut)
			conf.lOG(OutputType.DEBUG, "Time out waiting for attribute threads (" + timeInWaitingLimit + "ms)");
		
	}
	
	/**
	 * Indicates that a next interaction should clean previously retrieved values.
	 * 
	 * @deprecated
	 */
	public static void cleanAttributeValues() {
		//refreshValues = true;
	}
}
