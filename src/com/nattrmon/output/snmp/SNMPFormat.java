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
package com.nattrmon.output.snmp;

import java.io.IOException;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.nattrmon.config.Config;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.order.OrderFormat;

/**
 * Special format for output to SNMP
 */
public class SNMPFormat extends OutputFormat {
	
	// TODO Check https://code.google.com/p/springside-sub/source/browse/trunk/tiny-examples/simple-snmp-agent/src/main/java/com/simple/snmp/SimpleSnmpAgent.java

	protected Address targetAddress;
	protected TransportMapping transport;
	protected Snmp snmp;
	protected USM usm;
	
	public SNMPFormat(Config conf, String param) {
		super(conf, param);
		
		targetAddress = GenericAddress.parse("udp:127.0.0.1/161");
		try {
			transport = new DefaultUdpTransportMapping();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		snmp = new Snmp(transport);
		usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		try {
			transport.listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void register() {
		Config.registerFormat(SNMPFormat.class.getName(), "snmpOutput");
	}
	
	@Override
	public void processOutput(long counter) {
		CommunityTarget target = new CommunityTarget();
		
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version1);
		
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,1})));
		pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,2})));
		pdu.setType(PDU.GETNEXT);
		ResponseListener listener = new ResponseListener() {
			@Override
			public void onResponse(ResponseEvent event) {
				((Snmp)event.getSource()).cancel(event.getRequest(), this);
			    System.out.println("Received response PDU is: "+event.getResponse());
			}
		};
		
		CommandResponder trapPrinter = new CommandResponder() {
			public synchronized void processPdu(CommandResponderEvent e) {
				PDU command = e.getPDU();
				if (command != null) {
					System.out.println(command.toString());
				}
			}
		};
		snmp.addCommandResponder(trapPrinter);
		
		try {
			snmp.send(pdu, target, null, listener);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
