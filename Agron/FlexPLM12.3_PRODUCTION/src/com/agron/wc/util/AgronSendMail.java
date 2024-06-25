package com.agron.wc.util;

import com.lcs.wc.util.FormatHelper;
import java.io.File;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.util.WTException;

// Referenced classes of package com.agron.wc.util:
//            AgronSendMailUtil

/**
 * @author Mallikarjuna savi
 * @version 1.0
 */

public class AgronSendMail
{

    public AgronSendMail()
    {
    }
	/**
	 * This method is used to send the email notification to the given mail id's.
	 * @param strFromMailId is String object
	 * @param strTo is String object
	 * @param strSMTPHost is String object
	 * @param strSubjectParam is String object
	 * @param strMailBodyParam is String object
	 * @param strAttachmentLocation String object
	 * @return Boolean type
	 * @throws WTException type
	 */
    public boolean send(String strFromMailId, String strTo, String strSMTPHost, String strSubjectParam, String strMailBodyParam, String strAttachmentLocation, String mailHeader, 
            String sysInfo, String declaimer)
        throws WTException
    {
        boolean bMailStatus = false; // defined to store the Boolean value.
        String strSubject = "";// defined to store the String value.
        String strMailBody = "";// defined to store the String value.
        List toIdList = new ArrayList();// defined to store the List value.
        Properties props = new Properties();// defined to store the Properties value.
        Session session = Session.getInstance(props); // defined to store the Session value.
        toIdList = AgronSendMailUtil.parseToken(strTo, ",");// defined to store the List value.
        props.put("mail.smtp.host", strSMTPHost);// setting host value to the properties.
        props.put("mail.debug", "true");// setting debug value to the properties.
        int toIdCount = toIdList.size(); // defined to store the Integer value.
        File zipFile = null; // defined to store the File value.
        String strZipFileName = null;// defined to store the String value.
        if(FormatHelper.hasContent(strAttachmentLocation))
        {
            zipFile = new File(strAttachmentLocation);
            strZipFileName = zipFile.getName();
        }
        if(!FormatHelper.hasContent(strSubjectParam))
            strSubject = "Flex Error: Unknown Error";
        else
            strSubject = strSubjectParam;
        if(!FormatHelper.hasContent(strMailBodyParam))
        {
            strMailBody = " \n ";
            strMailBody = (new StringBuilder()).append(strMailBody).append("Unknown error has occured. \n \n ").toString();
            strMailBody = (new StringBuilder()).append(strMailBody).append("Please contact flex admin. Refer Interface logs for more details. \n ").toString();
        } else
        {
            StringBuffer sb = new StringBuffer();
            sb.append(mailHeader);
            sb.append(strMailBodyParam);
            sb.append(sysInfo);
            sb.append(declaimer);
            strMailBody = sb.toString();
        }
        while(toIdCount > 0) 
            try
            {
                toIdCount--;
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(strFromMailId));
                InternetAddress address[] = {
                    new InternetAddress((String)toIdList.get(toIdCount))
                };
                msg.setRecipients(javax.mail.Message.RecipientType.TO, address);
                msg.setSubject(strSubject);
                msg.setSentDate(new Date());
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(strMailBody);
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                if(strAttachmentLocation != null && FormatHelper.hasContent(strAttachmentLocation))
                {
                    messageBodyPart = new MimeBodyPart();
                    javax.activation.DataSource source = new FileDataSource(strAttachmentLocation);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(strZipFileName);
                    multipart.addBodyPart(messageBodyPart);
                }
                msg.setContent(multipart);
                Transport.send(msg);
                bMailStatus = true;
            }
            catch(AddressException e)
            {
                LOGGER.info((new StringBuilder()).append("Error#: ").append(e.getMessage()).toString());
            }
            catch(MessagingException e)
            {
                LOGGER.info((new StringBuilder()).append("Error#: ").append(e.getMessage()).toString());
            }
        return bMailStatus;
    }

    public static final Logger LOGGER = LogManager.getLogger(AgronSendMail.class);

}
