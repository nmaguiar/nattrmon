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
package com.nattrmon.output;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.varia.DenyAllFilter;
import org.apache.log4j.varia.LevelMatchFilter;
import org.apache.log4j.varia.LevelRangeFilter;

import com.nattrmon.config.Config;
import com.nattrmon.output.Output.OutputType;

public class Log4jOutput extends Output {
	protected final String LOGGER_NAME = "nAttrMon";
	// Log4J default pattern
	protected static String LOG4J_DEFAULT_PATTERN = "%d{dd MMM yyyy HH:mm:ss,SSS};[%p]; %m%n";
	protected Logger lOG;
	
	public Log4jOutput(Config conf) {
		super(conf);
		lOG = Logger.getLogger(LOGGER_NAME);
		
		ConsoleAppender ca1 = new ConsoleAppender(new PatternLayout(LOG4J_DEFAULT_PATTERN));
		ConsoleAppender ca2 = new ConsoleAppender(new PatternLayout(LOG4J_DEFAULT_PATTERN));
		ConsoleAppender ca3 = new ConsoleAppender(new PatternLayout(LOG4J_DEFAULT_PATTERN));
		
		LevelRangeFilter lrf = new LevelRangeFilter();
		
		ca1.setTarget("System.out");
		lrf.setLevelMax(Level.DEBUG);
		lrf.setLevelMin(Level.DEBUG);
		lrf.setAcceptOnMatch(true);
		ca1.addFilter(lrf);
		
		ca2.setTarget("System.out");
		lrf = new LevelRangeFilter();
		lrf.setLevelMax(Level.INFO);
		lrf.setLevelMin(Level.INFO);
		lrf.setAcceptOnMatch(true);
		ca2.addFilter(lrf);
		
		ca3.setTarget("System.err");
		lrf = new LevelRangeFilter();
		lrf.setLevelMax(Level.ERROR);
		lrf.setLevelMin(Level.ERROR);
		lrf.setAcceptOnMatch(true);
		ca3.addFilter(lrf);

		BasicConfigurator.configure(ca1);
		BasicConfigurator.configure(ca2);
		BasicConfigurator.configure(ca3);

		setLevel(currentType);
		
	}

	@Override
	public void write(OutputType type, String message) {
		switch (type) {
		case DEBUG:
			lOG.debug(message); break;
		case ERROR:
			lOG.error(message); break;
		case INFO:
			lOG.info(message); break;
		}
	}

	@Override
	public void setLevel(OutputType type) {
		super.setLevel(type);
	
		lOG.setLevel(getOutputType2Level(type));
	}
	
	public static Level getOutputType2Level(OutputType type) {
		switch (type) {
		case DEBUG:
			return Level.DEBUG; 
		case ERROR:
			return Level.ERROR; 
		case INFO:
			return Level.INFO;
		}
		
		return null;
	}

	@Override
	public void write(OutputType type, String message, Exception e) {
	    StringBuilder result = new StringBuilder("");
	    StringWriter sw = new StringWriter();
	    
	    e.printStackTrace(new PrintWriter(sw));
	    
	    result.append(sw.toString());
	    /*
	    for (StackTraceElement element : e.getStackTrace() ){
	      result.append(element);
	      result.append(System.getProperty("line.separator"));
	    }*/
	    
	    write(type, message + System.getProperty("line.separator") + result);
	}

}
