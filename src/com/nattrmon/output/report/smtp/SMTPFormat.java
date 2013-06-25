package com.nattrmon.output.report.smtp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SimpleSMTPHeader;

import com.nattrmon.config.Config;
import com.nattrmon.core.Attribute;
import com.nattrmon.core.OutputFormat;
import com.nattrmon.output.Output.OutputType;
import com.nattrmon.output.report.ReportUtils;

public class SMTPFormat extends OutputFormat {
	protected String filename = "";
	protected String triggerAttribute = "";
	protected String sender = "";
	protected ArrayList<String> recipients = new ArrayList<String>();
	protected String subject = "";
	protected String emailTemplate = "";
	protected String snmpServer = "";
	
	public SMTPFormat(Config conf, String param) {
		super(conf, param);
		if (param != null) {
			String pms[] = param.split(";");
			for (String p : pms) {
				String tmp = p.trim();
				String[] prop = tmp.split("=");
				if (prop.length == 2) {
					if (prop[0].equalsIgnoreCase("src")) filename = prop[1];
					if (prop[0].equalsIgnoreCase("trigger")) triggerAttribute = prop[1];
					if (prop[0].equalsIgnoreCase("sender")) sender = prop[1];
					if (prop[0].equalsIgnoreCase("to")) recipients.add(prop[1]); 
					if (prop[0].equalsIgnoreCase("subject")) subject = prop[1];
					if (prop[0].equalsIgnoreCase("server")) snmpServer  = prop[1];
				}
			}
		}
		
		if (filename != null) {
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(filename);
			} catch (FileNotFoundException e) {
				conf.lOG(OutputType.ERROR, "File not found: '" + filename + "'");
				conf.lOG(OutputType.DEBUG, "FileNotFoundException", e);
			}
			
			if (fileReader != null) {
				BufferedReader br = new BufferedReader(fileReader);
				StringBuffer sf = new StringBuffer();
				String s = "";
				try {
					while((s = br.readLine()) != null) {
						sf.append(s);
					}
				} catch (IOException e) {
					conf.lOG(OutputType.ERROR, "IO problem reading file: '" + filename + "'");
					conf.lOG(OutputType.DEBUG, "IOException", e);
				}
				emailTemplate = sf.toString();
			}
		}
		
		if (!(conf.getUniqueAttrs().getAttrs().keySet().contains(triggerAttribute))) {
			conf.lOG(OutputType.ERROR, "Trigger attribute '" + triggerAttribute + "' not found. Define with trigger='ATTRIBUTE_NAME'.");
		}
		
		showHeader = false;
	}

	public static void register() {
		Config.registerFormat(SMTPFormat.class.getName(), "smtpOutput");
	}
	
	@Override
	public void processOutput() {
		Attribute triggerAttr = conf.getUniqueAttrs().getAttribute(triggerAttribute);
		
		if (emailTemplate.equals("")) 
			emailTemplate = ReportUtils.buildTemplate(getAttrNames());
		
		if ((triggerAttr != null) && (triggerAttr.getValue()).equals("1") && recipients.size() > 0) {
			String emailToSend = ReportUtils.preProcess(conf, getAttrNames(), emailTemplate);
			System.out.println(ReportUtils.preProcess(conf, getAttrNames(), emailToSend));
			
			SimpleSMTPHeader header = new SimpleSMTPHeader(sender,
					recipients.get(0), subject);
			SMTPClient client = new SMTPClient();
			try {
				client.connect(snmpServer);

				if (!SMTPReply.isPositiveCompletion(client.getReplyCode())) {
					client.disconnect();
					conf.lOG(OutputType.ERROR, "SMTP problem while trying to connect to: '" + snmpServer + "'");
					return;
				}

				client.login();
				client.setSender(sender);
				for (String recp : recipients) {
					client.addRecipient(recp);
				}
				header.addHeaderField("MIME-Version", "1.0");
				header.addHeaderField("Content-Type", "text/html; charset=ISO-8859-1");

				Writer writer = client.sendMessageData();
				
				if (writer != null) {
					writer.write(header.toString());
					writer.write(emailToSend);
					writer.close();
					client.completePendingCommand();
				}

				client.logout();

				client.disconnect();
			} catch (IOException e) {
				conf.lOG(OutputType.ERROR, "IO problem sending email (" + subject + ")");
				conf.lOG(OutputType.DEBUG, "IOException", e);
			}
		}
	}
	
}
