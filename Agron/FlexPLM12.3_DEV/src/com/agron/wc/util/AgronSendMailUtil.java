
package com.agron.wc.util;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.util.WTException;

// Referenced classes of package com.agron.wc.util:
//            AgronSendMail
/**
 * @author Mallikarjuna Savi
 * @version 1.0
 */
public class AgronSendMailUtil
{

    public AgronSendMailUtil()
    {
    }
  /**
     * This method is used to send the mail notification to the given mail id's.
     * @param filePath is String object
     * @param mailFromID is String object
     * @param maiToIDs is String object
     * @param mailToSubject is String object
     * @param mailBody is String object
     * @param useKeysAlso is Boolean object
     * @throws WTException type
     */
    public void sendEmail(String filePath, String mailFromID, String maiToIDs, String mailToSubject, String mailBody, boolean useKeysAlso, String mailHeader, 
            String sysInfo, String declaimer, String heading)
        throws WTException
    {
        if(isInfoEnabled)
            LOGGER.info("Entering AgronSendMailUtil.sendEmail()");
        boolean enableEmail = LCSProperties.getBoolean("com.agron.wc.integration.Enable.Emails");
        if(enableEmail)
            try
            {
                String message = "";
                String smtpHost = LCSProperties.get("com.agron.wc.integration.SMTPHost");
                StringBuffer sb = new StringBuffer();
                if(FormatHelper.hasContent(mailBody))
                {
                    sb.append(heading);
                    sb.append(" \n ");
                    sb.append(mailBody);
                }
                message = sb.toString();
                (new AgronSendMail()).send(mailFromID, maiToIDs, smtpHost, mailToSubject, message, filePath, mailHeader, sysInfo, declaimer);
            }
            catch(WTException e)
            {
                LOGGER.info((new StringBuilder()).append("Error#: ").append(e.getMessage()).toString());
            }
        if(isInfoEnabled)
            LOGGER.info("Exiting AgronSendMailUtil.sendEmail()");
    }

 /**
     * This method is used to convert the tokenized values to the list.
     * @param inputString is String object
     * @param token is String object
     * @return List type
     */
    public static List parseToken(String inputString, String token)
    {
        ArrayList tokenValues = new ArrayList();
        if(FormatHelper.hasContent(inputString))
        {
            for(StringTokenizer tokenizer = new StringTokenizer(inputString, token); tokenizer.hasMoreTokens(); tokenValues.add(tokenizer.nextToken().trim()));
        }
        return tokenValues;
    }

    private static final Logger LOGGER = LogManager.getLogger(AgronSendMailUtil.class);
    private static boolean isInfoEnabled = true;

}
