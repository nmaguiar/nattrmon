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
package com.nattrmon.services.shell;

import java.util.ArrayList;
import java.util.Arrays;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.core.Service;
import com.nattrmon.output.Output.OutputType;

public class ShellAttribute extends Attribute {
	protected String regex = "[ \t\r\n\f]";
	
	public ShellAttribute(Config conf, ShellObject parentObject, String uniqueName, AttributeType type, String name, String value)
			throws ExceptionDuplicatedUniqueAttribute {
		super(conf, parentObject, uniqueName, name, type, value);
		setHeavy(true);
		
		if (parentObject.getRegex() != null) {
			regex = parentObject.getRegex();
		}
	}
	
	@Override
	public String getValue() {
		String output;
		
		if (getType() == AttributeType.Simple) {
			output = getParentObject().getOutput(true);
			if (getParentObject().isNocr()) {
				output = output.replaceAll("\n", "");
			}
			return output;
		} 
		
		if (getType() == AttributeType.Id) {
			output = getParentObject().getOutput(false);
			ArrayList list = new ArrayList(Arrays.asList(output.split(regex)));
			int value = -1;
			
			try {
				value = Integer.valueOf(getName()).intValue();
			} catch (Exception e) {
				conf.lOG(OutputType.ERROR, "No value found in '" + getName() + "' for attribute " + this.getUniqueName());
			}
			
			if ((value > 0) && (value <= list.size())) {
				String s = (String) list.get(value - 1);
				return s;
			}
		}

		return OutputFormat.NOT_AVAILABLE;

	}

	public ShellObject getParentObject() {
		return (ShellObject) parentObject;
	}

	public void setParentObject(ShellObject parentObject) {
		this.parentObject = parentObject;
	}

}
