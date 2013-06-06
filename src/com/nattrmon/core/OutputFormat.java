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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.nattrmon.config.Config;
import com.nattrmon.output.Output.OutputType;

/**
 * Abstract class for implementing OutputFormat classes.
 * Most of the base functionality for OutputFormat is provided here.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public abstract class OutputFormat {
	protected Config conf;
	protected String text;
	protected String param;
	protected boolean firstTime = true;
	protected boolean showHeader = true;
	protected static boolean refreshValues = true;
	protected boolean determineParallelFill = true;
	protected boolean useParallelFill = false;
	protected boolean processOutput = true;
    
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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
	}

	/**
	 * Main output method. Should be called whenever necessary to 
	 * output data.
	 */
	public void output() {
		preOutput();
		//if (processOutput) {
			processOutput();
			posOutput();
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
	public synchronized void preOutput() {
		//if (inFill == false) {
			if (useParallelFill()) {
				parallelFillAttributeValues();
			} else {
				fillAttributesValues();
			}
			//inFill = true;
		//} 
	}
		
	/**
	 * Should be called only from output method. Classes extending this one
	 * should implement this method.
	 * 
	 */
	public abstract void processOutput();
	
	public synchronized void posOutput() {
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
		String orderText = getText();
		
		if (orderText != null) {		
			orderText = orderText.replaceAll(" ", "");
			orderText = orderText.trim();
			return new ArrayList<String>(Arrays.asList(orderText.split(",")));
		}
		
		return null;
	}
	
	/**
	 * Determine if a parallel fill should be used.
	 * 
	 * @return Returns true if parallel fill should be used.
	 */
	public boolean useParallelFill() {
		if (determineParallelFill) {
			UniqueAttributes ua = conf.getUniqueAttrs();
			boolean use = false;
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
	public void fillAttributesValues() {
		UniqueAttributes ua = conf.getUniqueAttrs();
		Attribute atr = null;
		ArrayList<String> lastToRun = new ArrayList<String>();
		long counter = conf.getCurrentCounter();
		
		for(String attr :getAttrNames()) {
			if (!(conf.containsCurrentAttributeValues(counter, attr)) || (refreshValues)) {
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
			if (!(conf.containsCurrentAttributeValues(counter, attr2)) || (refreshValues)) {
				if (attr2 != null) {
					atr = ua.getAttribute(attr2);
					if (atr != null) {
						conf.setCurrentAttributeValues(counter, attr2, atr.getValue());
					}				
				}			
			}
		}
		
		refreshValues = false;
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
	 * Fill all unique attributes with values in parallel processing.
	 * 
	 */
	public void parallelFillAttributeValues() {
		final UniqueAttributes ua = conf.getUniqueAttrs();
		Attribute atr = null;
		ArrayList<Thread> threads = new ArrayList<Thread>();
		ArrayList<String> lastToRun = new ArrayList<String>();
		final long counter = conf.getCurrentCounter();
		
		class ValueThread extends Thread {
			protected Attribute a;
			
			public ValueThread(String atr) {
				a = ua.getAttribute(atr);
			}
			
			public void run() {
				if (a != null) setResult(a.getValue());
			}
			
			public synchronized void setResult(String r) {
				conf.lOG(OutputType.DEBUG, "Thread processing attribute: " + a.getUniqueName());
				conf.setCurrentAttributeValues(counter, a.getUniqueName(), r);
			}
		}
		
		// First group of attributes (non internal)
		for(String attr :getAttrNames()) {
			if (!(conf.containsCurrentAttributeValues(counter, attr)) || (refreshValues)) {
				if (attr != null) {
					if (ua.getAttribute(attr) != null)
							if (ua.getAttribute(attr).isInternal) {
								lastToRun.add(attr);
							} else {
								{Thread t = new ValueThread(attr);
								threads.add(t);
								t.start();}						
							}
				}
			}
		}
		
		// Wait for everyone to finish
		for(Thread t : threads) {
			if (t.isAlive()) 
				try {
					t.join();
				} catch (InterruptedException e) {
				}
		}
		
		// Run internal, if necessary, in sequential fashion
		for(String lastAtr : lastToRun) {
			if (!(conf.containsCurrentAttributeValues(counter, lastAtr)) || (refreshValues)) {
				atr = ua.getAttribute(lastAtr);
				if (atr != null) conf.setCurrentAttributeValues(counter, lastAtr, atr.getValue()); 
				conf.lOG(OutputType.DEBUG, "Processing attribute: " + lastAtr);
			}			
		}
		
		refreshValues = false;
	}
	
	/**
	 * Indicates that a next interaction should clean previously retrieved values.
	 * 
	 */
	public static void cleanAttributeValues() {
		refreshValues = true;
	}
}
