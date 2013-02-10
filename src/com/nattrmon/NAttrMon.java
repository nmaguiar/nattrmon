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
package com.nattrmon;


import java.io.File;

import com.nattrmon.collector.CollectorOutput;
import com.nattrmon.collector.TimerCollectorOutput;
import com.nattrmon.config.Config;
import com.nattrmon.config.XMLConfig;
import com.nattrmon.config.XMLConfiguration;
import com.nattrmon.output.Output.OutputType;

/**
 * <h2>nAttrMon - Attribute Monitor</h2>
 * 
 * <p>This class can be used as an entry point or embedded in other code.</p>
 * 
 * <p>Using as an entry point it will internally use a TimerCollectorOutput.</p>
 * <p>Using as embedded it will require a CollectorOutput instance to be provided.</p>
 * 
 * @see com.nattrmon.collector.CollectorOutput, com.nattrmon.collector.TimerCollectorOutput
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class NAttrMon {
	protected static String confFile = "nattrmon.xml";
	protected static int timeInterval = -1;
	protected static int count = -2;
	
	protected Config conf;
	
	/**
	 * Creates a attribute monitor with a CollectorOutput
	 * 
	 * @param co A CollectorOutput instance to use.
	 */
	public NAttrMon(CollectorOutput co) {
		conf = co.getConfig();
		
		if (co.timeInterval > -1) {
			conf.setTimeInterval(co.timeInterval);
		}
		
		conf.lOG(OutputType.DEBUG, "Time interval set to = " + co.getConfig().getTimeInterval());
		conf.lOG(OutputType.DEBUG, "Count set to = " + co.count);
	}
	
	/**
	 * Initializes a Config instance to be used by com.nattrmon.* objects
	 * 
	 * @param otype The preferred OutputType
	 * @param cFile The configuration xml file path
	 * @return The instantiated Object
	 */
	public static Config initConfig(OutputType otype, String cFile) {
		Config conf = Config.getConfig();
		conf.setDefaultType(otype);
		
		XMLConfig xconf = new XMLConfig(conf, new File(cFile));
		XMLConfiguration ss = xconf.read();
		if (ss != null)
			ss.config(conf);
		
		conf.lOG(OutputType.DEBUG, "Init");
		conf.lOG(OutputType.DEBUG, "Services Size = " + ss.getServices().getServices().size());
		conf.lOG(OutputType.DEBUG, "Formats Size = " + ss.getOutputs().getFormats().size());
		conf.lOG(OutputType.DEBUG, "Unique attrs size = " + conf.getUniqueAttrs().getAttrs().size());
		
		return conf;
	}
	
	/**
	 * Process the command line arguments.<br>
	 * Currently processes:
	 * <ul>
	 * <li><i>-c [path]</i> - The path for the configuration XML file.</li>
	 * <li><i>[DELAY] [COUNT]</i> - Accepts a delay between outputs and, optionally, also 
	 * the number of times to output (like vmstat)
	 * </ul>
	 * 
	 * @param args The command-line arguments.
	 */
	protected static void processArgs(String[] args) {
		boolean FLAG_CONFIG = false;
		boolean FLAG_DELAY = true;
		boolean FLAG_COUNT = true;
		
		for(String arg : args) {
			if (FLAG_CONFIG) {
				FLAG_CONFIG = false;
				confFile = arg;
				continue;
			}
				
			if (arg.equals("-c")) {
				FLAG_CONFIG = true;
				continue;
			}
			
			if (FLAG_DELAY) {
				try {
					timeInterval = Integer.valueOf(arg).intValue();
					timeInterval = timeInterval * 1000;
				} catch (Exception e) {
					System.out.println("Value '" + arg + "' not valid for setting the delay.");
				}
				FLAG_DELAY = false;
				continue;
			}
			
			if (FLAG_COUNT) {
				try {
					count = Integer.valueOf(arg).intValue();
				} catch (Exception e) {
					System.out.println("Value '" + arg + "' not valid for setting the count.");
				}
				FLAG_COUNT = false;
				continue;				
			}
		}
	}
	
	/**
	 * The Main method.
	 * Instantiates a TimerCollectorOutput with OutputType INFO, infinite count and 
	 * configuration file nattrmon.xml (if not specified otherwise on the configuration)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		processArgs(args);
		Config co = initConfig(OutputType.INFO, confFile);
		
		if (timeInterval > -1) {
			co.setTimeInterval(timeInterval);
		}
		
		new NAttrMon(new TimerCollectorOutput(co, count));
	}

	public Config getConf() {
		return conf;
	}

}
