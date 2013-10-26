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
package com.nattrmon.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.nattrmon.collector.CollectorOutput;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.core.Service;
import com.nattrmon.core.SimpleCache;
import com.nattrmon.core.UniqueAttributes;
import com.nattrmon.output.Log4jOutput;
import com.nattrmon.output.Output;
import com.nattrmon.output.Output.OutputType;

/**
 * Configuration class to hold Attribute Monitor parameters.
 * Other components of Attribute Monitor use this class to acquire the configuration parameters.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class Config {
	protected long generalCount = 0; // Main output line counter
	protected OutputType defaultType = OutputType.INFO;
	protected Output output;
	protected UniqueAttributes uniqueAttrs = new UniqueAttributes();
	protected ConcurrentHashMap<Long, ConcurrentHashMap<String, String>> attributeValues = new ConcurrentHashMap<Long, ConcurrentHashMap<String,String>>();
	protected ConcurrentHashMap<Long, ConcurrentHashMap<String, Date>> attributeValuesDate = new ConcurrentHashMap<Long, ConcurrentHashMap<String,Date>>(); 
	protected static HashMap<String, String> registeredServices = new HashMap<String, String>();
	protected static HashMap<String, String> registeredFormats = new HashMap<String, String>();
	protected SimpleCache cache = new SimpleCache(this);
	protected long timeInterval = 1000;
	protected String collector = "timer";  // Default to timer if no collector specified
	protected ArrayList<Service> services = new ArrayList<Service>(); // The services configured
	protected ArrayList<OutputFormat> outputformats = new ArrayList<OutputFormat>(); // The outputs configured
	private String params;
	
	protected static final long limitObjectInCacheAge = -1;
	protected static Config conf;

	public static Config getConfig() {
		if (conf != null) {
			return conf;
		} else {
			conf = new Config();
			return conf;
		}
	}


	/**
	 * Get the current collector
	 */
	public String getCollector() {
		return collector;
	}

	/**
	 * Set the current collector
	 */
	public void setCollector(String collector) {
		this.collector = collector;
	}

	
	/**
	 * Obtain the current registered services
	 * 
	 * @return Returns an HashMap whose key is the service prefix (sometimes called the service's protocol)
	 * and the value is the java class that implements the service. 
	 */
	public static HashMap<String, String> getRegisteredServices() {
		return registeredServices;
	}

	/**
	 * Overwrites the current registered service classes
	 * 
	 * @param registeredServices A HashMap whose key is the service prefix (sometimes called the service's protocol)
	 * and the value is the java class name that implements the service.
	 */
	public static void setRegisteredServices(
			HashMap<String, String> registeredServices) {
		Config.registeredServices = registeredServices;
	}

	/**
	 * Obtains the current list of registered output format classes
	 * 
	 * @return A HashMap whose key is the output type and the value is the java class name that implements the service.
	 */
	public static HashMap<String, String> getRegisteredFormats() {
		return registeredFormats;
	}

	/**
	 * Overwrites the current registered output format classes
	 * 
	 * @param registeredFormats
	 */
	public static void setRegisteredFormats(
			HashMap<String, String> registeredFormats) {
		Config.registeredFormats = registeredFormats;
	}
	
	/**
	 * Obtains the current list of unique attributes
	 * 
	 * @return The current instance of unique attributes
	 */
	public UniqueAttributes getUniqueAttrs() {
		return uniqueAttrs;
	}

	/**
	 * Overwrites the current list of unique attributes
	 * 
	 * @param uniqueAttrs The UniqueAttributes list to overwrite
	 */
	public void setUniqueAttrs(UniqueAttributes uniqueAttrs) {
		this.uniqueAttrs = uniqueAttrs;
	}

	/**
	 * Returns the current default output format 
	 * 
	 * @return The default output instance 
	 */
	public Output getOutput() {
		return output;
	}

	/**
	 * Sets the current default output format
	 * 
	 * @param output The default output to set
	 */
	public void setOutput(Output output) {
		this.output = output;
	}

	/**
	 * Returns the current default log type (for logging proposes)
	 * 
	 * @return The current default type
	 */
	public OutputType getDefaultType() {
		return defaultType;
	}

	/**
	 * Sets the current default log type (for logging proposes)
	 * 
	 * @param defaultType The new default type 
	 */
	public void setDefaultType(OutputType defaultType) {
		this.defaultType = defaultType;
		output.setLevel(defaultType);
	}
	
	/**
	 * Initializes default values. Prepares logging capabilities.
	 * 
	 */
	private Config() {
		output = (Output) new Log4jOutput(this);
		
		// Initialization log4j for digester
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.defaultlog", Log4jOutput.getOutputType2Level(defaultType).toString());
	}
	
	/**
	 * Logs a message given a correspondent type to the current output.
	 * 
	 * @param type The OutputType of the logging message.
	 * @param message The message.
	 */
	public void lOG(OutputType type, String message) {
		getOutput().write(type, message);
	}
	
	/**
	 * Logs a message given a correspondent type and java exception to the current output.
	 * 
	 * @param type The OutputType of the logging message.
	 * @param message The message.
	 * @param e The Java exception
	 */
	public void lOG(OutputType type, String message, Exception e) {
		getOutput().write(type, message, e);
	}
	
	/**
	 * Registers a service.
	 * 
	 * @param aClass The service class (note: aClass must exist and should be a sub-class of Service)
	 * @param protocol The protocol/prefix
	 */
	public static void registerService(String aClass, String protocol) {
		try {
			Class c = Class.forName(aClass);
			if (c.asSubclass(Service.class) != null)
				registeredServices.put(protocol, aClass);
		} catch (ClassNotFoundException e) {
			getConfig().lOG(OutputType.ERROR, "Class for '" + protocol + "' not found.");
			getConfig().lOG(OutputType.DEBUG, "ClassNotFoundException", e);
		}
		
	}

	/**
	 * Registers a output format
	 * 
	 * @param aClass The output class (note: aClass must exist and should be a sub-class of OutputFormat)
	 * @param protocol
	 */
	public static void registerFormat(String aClass, String protocol) {
		try {
			Class c = Class.forName(aClass);
			if (c.asSubclass(OutputFormat.class) != null)
				registeredFormats.put(protocol, aClass);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds an ArrayList of Services
	 * 
	 * @param srvs Must be an ArrayList of instances of Services.
	 */
	public void addServices(ArrayList<Service> srvs) {
		services.addAll(srvs);
	}
	
	/**
	 * Adds an ArrayList of OutputFormat
	 * 
	 * @param outs Must be an ArrayList of instances of OutputFormat.
	 */
	public void addOutputFormats(ArrayList<OutputFormat> outs) {
		outputformats.addAll(outs);
	}
	
	/**
	 * Obtains the current list of services.
	 * 
	 * @return An ArrayList of Service.
	 */
	public ArrayList<Service> getServices() {
		return services;
	}

	/**
	 * Obtains the current list of output formats.
	 * 
	 * @return An ArrayList of OutputFormat.
	 */
	public ArrayList<OutputFormat> getOutputformats() {
		return outputformats;
	}
	
	/**
	 * Only Output classes should call this method
	 * 
	 */
	public void increaseCounter() {
		if ((generalCount + 1) == Long.MAX_VALUE) 
			generalCount = 1;	// Turn around if limit is achieved
		else 
			generalCount++;
	}
	
	/**
	 * Obtains the current counter for Attribute Monitor (number of times the collector has been called)
	 * 
	 * @return The internal current counter
	 */
	public long getCurrentCounter() {
		return generalCount;
	}

	/**
	 * Obtains the limit of times the counter can advance while object in cache are valid.
	 *  
	 * @return The current limit.
	 */
	public static long getLimitObjectInCacheAge() {
		return limitObjectInCacheAge;
	}

	/**
	 * Obtains the current cache be used
	 * 
	 * @return A SimpleCache object
	 */
	public SimpleCache getCache() {
		return cache;
	}

	/**
	 * Overwrites the current cache in use.
	 * 
	 * @param cache The new SimpleCache cache.
	 */
	public void setCache(SimpleCache cache) {
		this.cache = cache;
	}

	/**
	 * Obtains the current time interval between times the collector is called)
	 * 
	 * @return The current time interval.
	 */
	public synchronized long getTimeInterval() {
		return timeInterval;
	}

	/**
	 * Overwrites the current time interval
	 * 
	 * @param timeInterval The new time interval
	 */
	public synchronized void setTimeInterval(long timeInterval) {
		this.timeInterval = timeInterval;
	}

	/**
	 * Clears the stored attribute values for a specific counter value 
	 * 
	 * @param counter The counter value for which to clear stored attribute values
	 */
	public void clearCurrentAttributeValues(long counter) {
		attributeValues.remove(new Long(counter));
		attributeValuesDate.remove(new Long(counter));
	}

	/**
	 * Obtain all the stored attribute values for a specific counter value
	 * 
	 * @param counter The specific counter value for which to obtain attribute values
	 * @return An HashMap whose key is the unique attribute name and the value the corresponding attribute value
	 */
	public synchronized ConcurrentHashMap<String, String> getCurrentAttributeValues4Counter(long counter) {
		synchronized (attributeValues) {
			if (!(attributeValues.containsKey(counter))) attributeValues.put(counter, new ConcurrentHashMap<String, String>());
			return attributeValues.get(counter);
		}
	}
	
	/**
	 * Obtain all the stored attribute values modified date for a specific counter value
	 * 
	 * @param counter The specific counter value for which to obtain attribute modified dates
	 * @return An HashMap whose key is the unique attribute name and the value the corresponding attribute modified date
	 */
	public synchronized ConcurrentHashMap<String, Date> getCurrentAttributeDates4Counter(long counter) {
		synchronized (attributeValues) {
			if (!(attributeValuesDate.containsKey(counter))) attributeValuesDate.put(counter, new ConcurrentHashMap<String, Date>());
			return attributeValuesDate.get(counter);
		}
	}
	
	/**
	 * Set a specific attribute value for a specific counter value
	 * 
	 * @param counter The counter value for which to set attribute values
	 * @param attr The unique attribute identifier
	 * @param value The attribute value to set
	 */
	public void setCurrentAttributeValues(long counter, String attr, String value) {
		ConcurrentHashMap<String, Date> chmDate = getCurrentAttributeDates4Counter(counter);
		ConcurrentHashMap<String, String> chm = getCurrentAttributeValues4Counter(counter);
		ConcurrentHashMap<String, String> chmLast = null;
		ConcurrentHashMap<String, Date> chmDateLast = null;
		
		if (counter > 1) {
			chmLast = getCurrentAttributeValues4Counter(counter-1);
			chmDateLast = getCurrentAttributeDates4Counter(counter-1);
		}
		
		synchronized (chm) {
			chm.put(attr, value);
			if (chmLast != null) {
				if (!chmLast.get(attr).equals(value))  
					chmDate.put(attr, new Date());
				else
					chmDate.put(attr, chmDateLast.get(attr));
			} else {
				chmDate.put(attr, new Date());
			}
		}
	}
	
	/**
	 * Checks if an attribute value stored exists for a specific counter value. 
	 * 
	 * @param counter The counter value for which to check if there is a stored attribute value.
	 * @param attr The attribute to check.
	 * @return True if the attribute exists, false if not.
	 */
	public boolean containsCurrentAttributeValues(long counter, String attr) {
		ConcurrentHashMap<String, String> chm = getCurrentAttributeValues4Counter(counter);
		synchronized (chm) {
			return chm.containsKey(attr);
		}
	}
	
	/**
	 * Verifies that an attribute value is equal to a value provided.
	 * 
	 * @param counter The counter value for which to check.
	 * @param attr The attribute to check.
	 * @param value The value to check against.
	 * @return True if the attribute exists and is equal to value, false if not.
	 */
	public boolean isCurrentAttributeValue(long counter, String attr, String value) {
		if (value.equals(getCurrentAttributeValues(counter, attr)))
			return true;
		else 
			return false;
	}
	
	/**
	 * Obtains the current attribute values for a specific counter value
	 * 
	 * @param counter The counter value for which to obtain the attribute stored value
	 * @param attr The unique attribute for which the stored attribute value for the corresponding counter refers. 
	 * @return The corresponding attribute value for the specified unique attribute and counter value. 
	 */
	public String getCurrentAttributeValues(long counter, String attr) {
		String r = null;
		if (!(containsCurrentAttributeValues(counter, attr))) {
			r = uniqueAttrs.getAttribute(attr).getValue();
			setCurrentAttributeValues(counter, attr, r);
		}
		
		r = getCurrentAttributeValues4Counter(counter).get(attr);
		
		return r;
	}
	
	/**
	 * Obtains the current attribute values modified date  for a specific counter value
	 * 
	 * @param counter The counter value for which to obtain the attribute stored value
	 * @param attr The unique attribute for which the stored attribute value for the corresponding counter refers. 
	 * @return The corresponding attribute modified date for the specified unique attribute and counter value. 
	 */
	public Date getCurrentAttributeDates(long counter, String attr) {
		Date r = null;
		if (!(containsCurrentAttributeValues(counter, attr))) {
			return null;
		}
		
		r = getCurrentAttributeDates4Counter(counter).get(attr);
		
		return r;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getParams() {
		return params;
	}
}
