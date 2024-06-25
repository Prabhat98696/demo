package com.agron.wc.load;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.util.WTException;
import wt.util.WTProperties;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

public class AgronMailUtil {
	
	public static final Logger log = LogManager.getLogger(AgronMailUtil.class);
	public static String fromEmailId = LCSProperties.get("com.agron.wc.integration.FromMailId","FlexPLMSupport@agron.com");
	public static String sSmtpServer = null;
	static {
		try {
			sSmtpServer = WTProperties.getLocalProperties().getProperty("wt.mail.mailhost");
			log.info(" mailhost..." + sSmtpServer);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void send(String[] recipients, String sSubject, String sText, String fileURL)
			throws WTException, IOException {
		send(fromEmailId, recipients, sSubject, sText, fileURL);
	}

	
	/**
	 * send
	 * 
	 * @param sSender
	 * @param recipients
	 * @param sSubject
	 * @param sText
	 * @param fileURL
	 * @throws WTException
	 */
	public static void send(String sSender, String[] recipients, String sSubject, String sText, String fileURL)
			throws WTException {
		log.info(" Send Message with an attachment " + fileURL);
		log.info(" recipents..." + recipients);
		Properties props = new Properties();
		log.info(" props..." + props);
		props.put("mail.smtp.host", sSmtpServer);
		Session session = Session.getDefaultInstance(props, null);
		log.info(" recipents..." + recipients);

		 if ((recipients != null) && (recipients.length > 0)) {
			InternetAddress[] toAddresses = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; ++i) {
				InternetAddress tempAddress = new InternetAddress();
				tempAddress.setAddress(recipients[i]);
				log.info(" temp addresss..." + recipients[i]);
				toAddresses[i] = tempAddress;
			}
			try {

				log.info(" to addresss..." + toAddresses);
				Message msg = new MimeMessage(session);
				InternetAddress fromAddress = new InternetAddress();
				fromAddress.setAddress(sSender);
				msg.setFrom(fromAddress);
				msg.setRecipients(Message.RecipientType.TO, toAddresses);
				msg.setSubject(sSubject);

				// Include Attachment
				MimeBodyPart messageBodyPart = new MimeBodyPart();

				// fill message
				messageBodyPart.setText(sText);

				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);

				// Part two is attachment
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(fileURL);
				messageBodyPart.setDataHandler(new DataHandler(source));
				File logFile = new File(fileURL);
				messageBodyPart.setFileName(logFile.getName());
				// messageBodyPart.setFileName(fileURL);
				multipart.addBodyPart(messageBodyPart);
				// Put parts in message
				msg.setContent(multipart);
				Transport.send(msg);

			} catch (MessagingException mex) {
				mex.printStackTrace();
				throw new WTException(mex);
			}
		} else {
			throw new WTException("To addresses not defined");
		}

	}

	/**
	 * send
	 * 
	 * @param sSender
	 * @param recipients
	 * @param sSubject
	 * @param sText
	 * @throws WTException
	 * @throws IOException
	 */
		
	public static void notifyUserOnDataLoad(String subject, String errorMsg, String fileURL, String emailList) throws WTException  {
		try {
			String userEmail = "";
			String[] userToId = getSendToEmailList(userEmail, emailList);
			log.debug("userToId ::: "+userToId.toString());
			if(userToId!=null&&userToId.length>0)
				AgronMailUtil.send(userToId, subject, errorMsg, fileURL);				
		} catch (IOException e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}

	/**
	 * getSendToEmailList - Method prepares to email-ids to send notification
	 * @param userEmail
	 * @param emailIds
	 * @return
	 * @throws WTException
	 */
	public static String[] getSendToEmailList(String userEmail, String emailIds) throws WTException{
		log.debug("getSendToEmailList() emailIds "+emailIds);
		ArrayList<String> emailList = new ArrayList<String>();
		if(FormatHelper.hasContent(userEmail)){
			emailList.add(userEmail);
		}
		if(FormatHelper.hasContent(emailIds)) {
			StringTokenizer st = new StringTokenizer(emailIds,",");
			while(st.hasMoreTokens()){
				emailList.add(st.nextToken());
			}
		}
		log.debug("getSendToEmailList() emailList "+emailList);
		// If no email id to send to then throw error
		if(emailList.size() < 1){
			log.debug(" No email ids to send notifictaion ");
			//throw new WTException (" No email ids to send notifictaion ");
		}
		String[] notiticationEmailIds = null;
		if(emailList.size() > 0)
			notiticationEmailIds = (String[]) emailList.toArray(new String[emailList.size()]);
		return notiticationEmailIds;
	}
}
