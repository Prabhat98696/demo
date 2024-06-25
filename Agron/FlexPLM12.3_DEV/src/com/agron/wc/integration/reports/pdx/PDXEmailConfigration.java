package com.agron.wc.integration.reports.pdx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;

public class PDXEmailConfigration {

	private static WTUser user = null;
	public static String fromEmailId = LCSProperties.get("com.agron.wc.integration.straub.util.FromMailId","FlexPLMSupport@agron.com");
	public static String userEmail = LCSProperties.get("com.agron.wc.integration.reports.pdx.userEmail","murali.sivakumar@acnovate.com");
	public static String env = LCSProperties.get("com.agron.wc.integration.reports.pdx.ENV","ENV");
	public static String sSmtpServer = null;
	private static final Logger logger = LogManager.getLogger(PDXEmailConfigration.class);
	
	static{
		try{
			 sSmtpServer = WTProperties.getLocalProperties().getProperty("wt.mail.mailhost");
			 
		}catch(IOException io){
			io.printStackTrace();
		}

	}
  
	/**
	 * 
	 * @param isValidFile
	 * @param dataList
	 * @param successCount
	 * @param failedCount
	 * @throws WTException
	 */
	public static void notifyPLMUser(boolean isValidFile, String Status, String fileName, String Message,String sesuser, String seasonName, String sDate, String eDate){

		try{
			StringBuilder emailContent = new StringBuilder();

			emailContent.append("Dear "+sesuser+",");
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append(System.getProperty("line.separator"));

			if(isValidFile) {
				if(Status.equals("SUCCESFUL")){
					emailContent.append("PDX Report Genrated Succefully and attached");
				}else if(Status.equals("NO DATA")){
					emailContent.append("PDX Report Genrated Succefully and no changes found in the date range");
				}else if (Status.equals("FAILED")){
					emailContent.append("PDX Report executed with exceptions and PLM Admin will look into this and get back");
				}
			}else {
				emailContent.append("Data failed to upload to sFTP Server.");

			}
			String emailSubject=  env+" :::PDX Report Executed Successfully";
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append("------------------------------------------------------");
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append("Season     :"+seasonName);
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append("Start Date :"+sDate);
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append("End Date  	:"+eDate);
			emailContent.append(System.getProperty("line.separator"));
			//emailContent.append("Message : "+Message);
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append("------------------------------------------------------");
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append("Thanks,");
			emailContent.append(System.getProperty("line.separator"));
			emailContent.append("PLM Admin");

			if(true)
			{
				String userToId = userEmail;

				try
				{
					//Calling an internal function to send email to the distribution list by providing the list of email ID's, email-subject and email body content

					System.out.println("Before  notifyUserOnDataLoad ::::::::::fileName:::::"+fileName);
					System.out.println("Before  notifyUserOnDataLoad ::::::::::userToId:::::"+userToId);
					notifyUserOnDataLoad(emailSubject, emailContent.toString(), fileName, userToId);
				}
				catch(Exception e)
				{
					e.printStackTrace();	
				}


			}
			else {
				logger.info("Email not configured for the user: "+ user.getName());
				System.out.println("Email not configured for the user: "+user.getName());

			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("In catch Exception ");
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
			//log.debug("userToId ::: "+userToId.toString());
			if(userToId!=null&&userToId.length>0)
				send(userToId, subject, errorMsg, fileURL);				
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
		//log.debug("getSendToEmailList() emailIds "+emailIds);
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
		//log.debug("getSendToEmailList() emailList "+emailList);
		// If no email id to send to then throw error
		if(emailList.size() < 1){
			//log.debug(" No email ids to send notifictaion ");
			//throw new WTException (" No email ids to send notifictaion ");
		}
		String[] notiticationEmailIds = null;
		if(emailList.size() > 0)
			notiticationEmailIds = (String[]) emailList.toArray(new String[emailList.size()]);
		return notiticationEmailIds;
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
		logger.info(" Send Message with an attachment " + fileURL);
		logger.info(" recipents..." + recipients);
		Properties props = new Properties();
		logger.info(" props..." + props);
		props.put("mail.smtp.host", sSmtpServer);
		Session session = Session.getDefaultInstance(props, null);
		logger.info(" recipents..." + recipients);

		if ((recipients != null) && (recipients.length > 0)) {
			InternetAddress[] toAddresses = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; ++i) {
				InternetAddress tempAddress = new InternetAddress();
				tempAddress.setAddress(recipients[i]);
				logger.info(" temp addresss..." + recipients[i]);
				toAddresses[i] = tempAddress;
			}
			try {

				logger.info(" to addresss..." + toAddresses);
				
				

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
