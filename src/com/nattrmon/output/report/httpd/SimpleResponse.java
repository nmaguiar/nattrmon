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
package com.nattrmon.output.report.httpd;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import com.nwu.httpd.Codes;
import com.nwu.httpd.HTTPd;
import com.nwu.httpd.Request;
import com.nwu.httpd.responses.Response;

	public class SimpleResponse extends Response {
		protected String id = "";
		
		public SimpleResponse(HTTPd httpd, String rUri, Properties props) {
			super(httpd, rUri);
			if (props != null)
				id = props.getProperty("id");
		}

		@Override
		public void execute(Request request) {	
			String out = HTTPDFormat.currentHTMLs.get(id);
			this.status = Codes.HTTP_OK;
			this.mimeType = Codes.MIME_HTML;
			if (out != null)
				this.data = new ByteArrayInputStream( out.toString().getBytes());

		}

	}