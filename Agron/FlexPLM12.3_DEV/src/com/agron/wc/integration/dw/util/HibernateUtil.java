package com.agron.wc.integration.dw.util;

import java.io.File;
import java.util.Properties;

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

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.agron.wc.util.AgronSendMail;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.util.WTException;

public class HibernateUtil {

	private static String SMTP_HOST = LCSProperties.get("com.agron.wc.integration.SMTPHost");
	static boolean enableEmail = LCSProperties.getBoolean("com.agron.wc.integration.dw.ETLProcessor.Enable.Emails",true);
	private static final String MAIL_HEADER = LCSProperties.get("com.agron.wc.integration.dw.ETLProcessor.MailHeader","PTC STAGING EXPORT STATUS");
    private static String MAIL_SUBJECT = LCSProperties.get("com.agron.wc.integration.dw.ETLProcessor.mailTo.Subject","PTC STAGING EXPORT STATUS");
	private static final String MAIL_TO_IDS = LCSProperties.get("com.agron.wc.integration.dw.ETLProcessor.ToMailIds","sandeep@acnovate.com");
	private static final String MAIL_FROM_ID = LCSProperties.get("com.agron.wc.integration.dw.ETLProcessor.FromMailId","flexplmsupport@agron.com");

	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {
		try {
			return new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("build SeesionFactory failed :" + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	} 

	public static void close() {
		// Close all cached and active connection pools
		getSessionFactory().close();
	}


	/**
	 * send
	 * @param sSender
	 * @param recipients
	 * @param sSubject
	 * @param sText
	 * @param fileURL
	 * @throws WTException
	 */
	public static void sendEmail(String sSender, String[] recipients, String sSubject, String sText, String fileURL)
			throws WTException {
		sSender = MAIL_FROM_ID;
		//recipients = MAIL_TO_IDS;
		Properties props = new Properties();
		System.out.println(" props..." + props);
		props.put("mail.smtp.host", "agron-com.mail.protection.outlook.com");
	
		Session session = Session.getDefaultInstance(props, null);
		
		
		System.out.println(" recipents..." + recipients);

		if ((recipients != null) && (recipients.length > 0)) {
			InternetAddress[] toAddresses = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; ++i) {
				InternetAddress tempAddress = new InternetAddress();
				tempAddress.setAddress(recipients[i]);
				System.out.println(" temp addresss..." + recipients[i]);
				toAddresses[i] = tempAddress;
			}
			try {

				System.out.println(" to addresss..." + toAddresses);
				
				

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
				System.out.println("  send ::::::::::sText:::::"+sText);
				System.out.println("  send ::::::::::fileURL:::::"+fileURL);
				

					// Part two is attachment
					messageBodyPart = new MimeBodyPart();
					if(fileURL != null) {
						DataSource source = new FileDataSource(fileURL);
						messageBodyPart.setDataHandler(new DataHandler(source));
						if(!fileURL.equals("Empty")){
							System.out.println("-------21----filePath------------- Not Empty");
							File xlsFile = new File(fileURL);
							messageBodyPart.setFileName(xlsFile.getName());
							System.out.println("-------21----filePath-------------"+xlsFile.getName());
						}
						else if (fileURL.equals("Empty")) {
							System.out.println("-------21----filePath------------- Empty");
							File xlsFile = new File("D:\\PTC\\Windchill_11.1\\Windchill\\codebase\\rfa\\temp\\csvexport\\PDXReport_Template.xlsx");
							messageBodyPart.setFileName(xlsFile.getName());	
					}
					
					//messageBodyPart.setFileName(fileURL);
					multipart.addBodyPart(messageBodyPart);
					}
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
}
