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

import java.io.File;
import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import com.nattrmon.core.Service;
import com.nattrmon.output.DigesterOutput;
import com.nattrmon.output.Log4jOutput;
import com.nattrmon.output.Output.OutputType;

/**
 * Main class for reading the XML configuration (check examples on the config folder)
 * 
 * @author nuno@aguiar.name
 *
 */
public class XMLConfig {
	String xmlConfigURI = "nattrmon.xml";
	File xmlConfigFile = null;
	Config conf;
	
	public XMLConfig(Config conf, File file) {
		xmlConfigFile = file;
		xmlConfigURI = "";
		this.conf = conf;
	}
	
	public XMLConfig(String uri) {
		xmlConfigFile = null;
		xmlConfigURI = uri;
	}
	
	public Digester createDigester() {
		Digester d = new Digester();	
		
		d.setValidating(false);
		d.addObjectCreate("nattrmon", XMLConfiguration.class);
		
		    d.addObjectCreate("nattrmon/settings", XMLSettings.class);
		    d.addSetProperties("nattrmon/settings", "log", "log");
		    d.addSetProperties("nattrmon/settings", "collector", "collector");
		    	d.addObjectCreate("nattrmon/settings/include", XMLInclude.class);
		    	d.addSetProperties("nattrmon/settings/include", "type", "type");
		    	d.addSetProperties("nattrmon/settings/include", "path", "path");
		    	d.addSetNext("nattrmon/settings/include", "addInclude");
			d.addSetNext("nattrmon/settings", "addSettings");
		    
			d.addObjectCreate("nattrmon/services", XMLServices.class);
		
				d.addObjectCreate("nattrmon/services/service", XMLService.class);
				d.addSetProperties("nattrmon/services/service", "url", "url");
				d.addSetProperties("nattrmon/services/service", "params", "params");
				
					d.addObjectCreate("nattrmon/services/service/object", XMLObject.class);
					d.addSetProperties("nattrmon/services/service/object", "name", "name");
					d.addSetProperties("nattrmon/services/service/object", "params", "params");
					
						d.addObjectCreate("nattrmon/services/service/object/attribute", XMLAttribute.class);
						d.addSetProperties("nattrmon/services/service/object/attribute", "uid", "uid");
						d.addSetProperties("nattrmon/services/service/object/attribute", "name", "name");
						d.addSetProperties("nattrmon/services/service/object/attribute", "type", "type");
						d.addSetProperties("nattrmon/services/service/object/attribute", "value", "value");
						d.addSetNext("nattrmon/services/service/object/attribute", "addXMLAttribute");
						
					d.addSetNext("nattrmon/services/service/object", "addObject");
					
				d.addSetNext("nattrmon/services/service", "addService");
		
			d.addSetNext("nattrmon/services", "addServices");
		
		d.addObjectCreate("nattrmon/outputs", XMLOutputs.class);
		
			d.addObjectCreate("nattrmon/outputs/output", XMLOutput.class);
			d.addSetProperties("nattrmon/outputs/output", "type", "type");
			d.addSetProperties("nattrmon/outputs/output", "params", "params");
			d.addBeanPropertySetter("nattrmon/outputs/output", "text");
			d.addSetNext("nattrmon/outputs/output", "addOutput");
			
			d.addSetNext("nattrmon/outputs", "addOutputs");
		
		return d;
	}
	
	public XMLConfiguration read() {
		XMLConfiguration ss = null;
		Digester d = createDigester();
		
		try {
			if (xmlConfigFile != null) { 
				ss = (XMLConfiguration) d.parse(xmlConfigFile);
			} else {
				ss = (XMLConfiguration) d.parse(xmlConfigURI);
			}
		} catch (IOException e) {
			if (xmlConfigFile != null)
				conf.lOG(OutputType.ERROR, "Trying to access the XML configuration file: '" + xmlConfigFile + "'");
			else
				conf.lOG(OutputType.ERROR, "Trying to access the XML configuration URI: '" + xmlConfigURI + "'");
			conf.lOG(OutputType.DEBUG, "IOException", e);
		} catch (SAXException e) {
			if (xmlConfigFile != null)
				conf.lOG(OutputType.ERROR, "Trying to parse the XML configuration file: '" + xmlConfigFile + "'.");
			else
				conf.lOG(OutputType.ERROR, "Trying to parse the XML configuration URI: '" + xmlConfigURI + "'.");
			conf.lOG(OutputType.DEBUG, "SAXException", e);
		}
		
		return ss;
	}
}
