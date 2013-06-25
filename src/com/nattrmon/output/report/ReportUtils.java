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
package com.nattrmon.output.report;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import oracle.sql.DATE;

import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;

public class ReportUtils {

	public ReportUtils() {
		// TODO Auto-generated constructor stub
	}

	public static String preProcess(Config cf, ArrayList<String> attrNames, String text) {
		String result = text;
		
		for(String attrName :attrNames) {			
			if (cf.containsCurrentAttributeValues(cf.getCurrentCounter(), attrName)) {
				result = result.replaceAll("\\$"+attrName+"\\$", cf.getCurrentAttributeValues(cf.getCurrentCounter(), attrName));
			}
		}
		
		result = result.replaceAll("\\$__currentCounter\\$", String.valueOf(cf.getCurrentCounter()));
		result = result.replaceAll("\\$__cacheCounter\\$", String.valueOf(cf.getCache().getNumberOfCurrentCachedObjects()));
		result = result.replaceAll("\\$__intervalTime\\$", String.valueOf(cf.getTimeInterval()));
		result = result.replaceAll("\\$__intervalTimeInSecs\\$", String.valueOf(cf.getTimeInterval() / 1000));
		result = result.replaceAll("\\$__currentTime\\$", (new Timestamp((new Date()).getTime())).toString());
		
		return result;
	}
	
	public static String buildTemplate(ArrayList<String> attrNames) {
		StringBuffer template = new StringBuffer();
		
		if (attrNames == null) return null;
		
		template.append("<html><head>");
		template.append("<meta charset=\"ISO-8859-1\"/>");
		template.append("<meta http-equiv=\"refresh\" content=\"$__intervalTimeInSecs$\">");
		template.append("<style type=\"text/css\">");
		template.append("body {background-color: #ffffff; color: #000000;}");
		template.append("body, td, th, h1, h2 {font-family: sans-serif;}");
		template.append("pre {margin: 0px; font-family: monospace;}");
		template.append("a:link {color: #000099; text-decoration: none; background-color: #ffffff;}");
		template.append("a:hover {text-decoration: underline;}");
		template.append("table {border-collapse: collapse;}");
		template.append(".center {text-align: center;}");
		template.append(".center table { margin-left: auto; margin-right: auto; text-align: left;}");
		template.append(".center th { text-align: center !important; }");
		template.append("td, th { border: 1px solid #000000; font-size: 75%; vertical-align: baseline;}");
		template.append("h1 {font-size: 150%;}");
		template.append("h2 {font-size: 125%;}");
		template.append(".p {text-align: left;}");
		template.append(".e {background-color: #ccccff; font-weight: bold; color: #000000; text-align: right }");
		template.append(".h {background-color: #9999cc; font-weight: bold; color: #000000;}");
		template.append(".v {background-color: #cccccc; color: #000000;}");
		template.append(".vr {background-color: #cccccc; text-align: right; color: #000000;}");
		template.append("img {float: right; border: 0px;}");
		template.append("hr {background-color: #cccccc; border: 0px; height: 1px; color: #000000;}");
		template.append("</style></head>");
		template.append("<body>");
		template.append("<h3>nAttrMon report for count $__currentCounter$</h3>");
		template.append("<hr>");
		template.append("<table>");
		
		for(String attrName :attrNames) {
			template.append("<tr><th class=\"e\">" + attrName + "</th><td>$" + attrName + "$</td></tr>");
		}
		
		template.append("</table><hr>Produced on $__currentTime$</body></html>");
		
		return template.toString();
	}
}
