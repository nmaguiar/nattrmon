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
package com.nattrmon.services.fs;

import java.io.File;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.ExceptionDuplicatedUniqueAttribute;
import com.nattrmon.core.Object;
import com.nattrmon.output.Output.OutputType;

public class FSAttribute extends Attribute {

	public FSAttribute(Config conf, Object parentObject, String uniqueName,
			String name, AttributeType type, String value)
			throws ExceptionDuplicatedUniqueAttribute {
		super(conf, parentObject, uniqueName, name, type, value);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getValue() {
		if (((FSObject) parentObject).getOperation().equalsIgnoreCase("size")) {
			try {
				File f = new File(name);
				
				if (f.isFile()) return Long.toString(f.length()); 
				if (f.isDirectory()) {
					return Long.toString(getFileSize(f));
				}
			} catch (Exception e) {
				conf.lOG(OutputType.DEBUG, "Exception while trying to obtain size of '" + name + "'", e);
			}
		}
		
		if (((FSObject) parentObject).getOperation().equalsIgnoreCase("number")) {
			try {
				File f = new File(name);
				
				if (f.isFile()) return "1";
				if (f.isDirectory()) {
					return Long.toString(getFileNumber(f));
				}
			} catch (Exception e) {
				conf.lOG(OutputType.DEBUG, "Exception while trying to obtain number of files in '" + name + "'", e);	
			}
		}
		
		return "n/a";
	}
	
	public long getFileNumber(File folder) {
		long totalFolder = 0;
		
		File[] filelist = folder.listFiles();
		for (int i = 0; i < filelist.length; i++) {
			if (filelist[i].isDirectory()) {
				totalFolder += getFileNumber(filelist[i]);
			} else {
				totalFolder++;
			}
		}
		return totalFolder;
	}
	
	public long getFileSize(File folder) {
		long totalFile = 0;
		long foldersize = 0;

		File[] filelist = folder.listFiles();
		for (int i = 0; i < filelist.length; i++) {
			if (filelist[i].isDirectory()) {
				foldersize += getFileSize(filelist[i]);
			} else {
				foldersize += filelist[i].length();
			}
		}
		return foldersize;
	}

}
