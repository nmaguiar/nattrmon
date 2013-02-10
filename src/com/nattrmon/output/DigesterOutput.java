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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.nattrmon.config.Config;

/**
 * Special output, to use with Commons Digester, to wrap around an existing output
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class DigesterOutput extends Output implements ErrorHandler {
	protected Output defaultOutput;
	
	public DigesterOutput(Config conf) {
		super(conf);
		defaultOutput = conf.getOutput();
	}

	public void error(SAXParseException arg0) throws SAXException {
		defaultOutput.write(OutputType.ERROR, arg0.getMessage());
	}

	public void fatalError(SAXParseException arg0) throws SAXException {
		defaultOutput.write(OutputType.ERROR, arg0.getMessage());

	}

	public void warning(SAXParseException arg0) throws SAXException {
		defaultOutput.write(OutputType.DEBUG, arg0.getMessage());

	}

	@Override
	public void write(OutputType type, String message) {
		defaultOutput.write(type, message);
	}

	@Override
	public void write(OutputType type, String message, Exception e) {
	    StringBuilder result = new StringBuilder("");
	    
	    for (StackTraceElement element : e.getStackTrace() ){
	      result.append(element);
	      result.append(System.getProperty("line.separator"));
	    }
	    
	    write(type, message + result);
	}

}
