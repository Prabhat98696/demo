package com.agron.wc.sftpImageExport;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.LCSProperties;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronImageExportLogEntry {

	public static final Logger LOGGER = LogManager.getLogger("com.agron.wc.sftpImageExport");
	private static final String FLEX_TIME_FORMAT = "MM-dd-yyyy:hh:mm:ss a";
	private static String dash = "--------------------------------";
	
	private static boolean isInfoEnabled = true;
	private static final String LCS_LOG_ENTRY = "LCSLogEntry";
	private static final String LASTRUNTIME_KEY =  LCSProperties.get("com.agron.wc.logEntry.sftpImageExport.lastRunKey", "agrLastRunTime");
	private static final String NAME_KEY = LCSProperties.get("com.agron.wc.logEntry.sftpImageExport.nameKey", "agrName");
	private static final String SUCCESS_KEY =  LCSProperties.get("com.agron.wc.logEntry.sftpImageExport.successKey", "agrSuccess");
	private static final String MESSAGE_KEY =  LCSProperties.get("com.agron.wc.logEntry.sftpImageExport.messageKey", "agrMessage");
	
 /**
	 * This method is used to run the batch process
	 */

	/**
	 * @throws ParseException
	 */
	public void execute(String successOrFailure, String message) throws ParseException {
	LOGGER.debug("execute--log Entry-----");
		
		try {
			ClientContext context = ClientContext.getContext();
			
			String currentDateTime = getCurrentGMTTime();
			String lastRunTime = getLastSuccessfulRun();
			
			LOGGER.info(dash);
			LOGGER.info(" Starting Image Export log entry Process  ");
			LOGGER.info((new StringBuilder()).append(" Start Time : ").append(currentDateTime).toString());
			LOGGER.info(dash);
			LOGGER.info((new StringBuilder()).append(" Last Run Time : ").append(lastRunTime).toString());
			LOGGER.info(dash);
			LOGGER.info(" **** Starting ImageExport Process ****  "); 
			
			createLogEntry(message, currentDateTime, "ExportImageFile", successOrFailure);
					
			 LOGGER.info(" **** End ImageExport Process ****  ");
		} catch (WTException wtexception) {
			LOGGER.error("Error : " + wtexception.fillInStackTrace());
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			LOGGER.error("Error : " + wtpropertyvetoexception.fillInStackTrace());
		} catch (ParseException parseexception) {
			LOGGER.error("Error : " + parseexception.fillInStackTrace());
		}
	}

	public static String getCurrentGMTTime() {
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		SimpleDateFormat simpledateformat = new SimpleDateFormat(FLEX_TIME_FORMAT);
		simpledateformat.setTimeZone(timezone);
		Date date = null;
		String s = null;
		date = new Date();
		s = simpledateformat.format(date);
		return s;
	}
	
	/**
	 * This method is used to create the successful log entry.
	 * 
	 * @param message          is String object
	 * @param currentTime      is String object
	 * @param logEntryName     is String object
	 * @param successOrFailure is String object
	 * @throws WTException             type
	 * @throws WTPropertyVetoException type
	 * @throws ParseException          type
	 */
	public void createLogEntry(String message, String currentTime, String logEntryName, String successOrFailure)
			throws WTException, WTPropertyVetoException, ParseException {
		LCSLogEntry lcslogentry = LCSLogEntry.newLCSLogEntry();
		String currentTimeStamp = null;
		try {
			FlexType flextype = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.logEntry.sftpImageExport.lcslogEntry"));
			lcslogentry.setFlexType(flextype);
			lcslogentry.setValue(LASTRUNTIME_KEY, currentTime);
			currentTimeStamp = getlogTimeStamp(currentTime).toString();
			currentTimeStamp = currentTimeStamp.replaceAll("[-+.^:,]", "");
			LOGGER.info("-------currentTimeStamp------>"+currentTimeStamp);
			lcslogentry.setValue(NAME_KEY,
					(new StringBuilder()).append(logEntryName).append("_").append(currentTimeStamp).toString());
			lcslogentry.setValue(SUCCESS_KEY, successOrFailure);
			lcslogentry.setValue(MESSAGE_KEY, message);
			LOGGER.info("-------message------>"+message);
			//lcslogentry = (LCSLogEntry) PersistenceHelper.manager.save(lcslogentry);
			(new LCSLogEntryLogic()).saveLog(lcslogentry);
			LOGGER.info((new StringBuilder()).append("Exiting createLogEntry : ").append(lcslogentry).toString());
		} catch (WTException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
			e.printStackTrace();
		} catch (ParseException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is used to get the log time.
	 * 
	 * @param currentTime is String object
	 * @return TimeStamp type
	 * @throws WTException    type
	 * @throws ParseException type
	 */
	public Timestamp getlogTimeStamp(String currentTime) throws WTException, ParseException {
		Timestamp logDate = null;
		Date date = null;
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy:hh:mm:ss a");
		date = dateFormat.parse(currentTime);
		logDate = new Timestamp(date.getTime());
		return logDate;
	}

	/**
	 * This method is used to get the last run time.
	 * 
	 * @return String type
	 */
	public String getLastSuccessfulRun() {
		String lastrunTime = null;
		try {
			 FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.logEntry.sftpImageExport.lcslogEntry"));
			String flexTypeIdPath = logEntryType.getIdPath();
			String dbColumnForLastRun = logEntryType.getAttribute(LASTRUNTIME_KEY).getColumnName();
			String runStatus = logEntryType.getAttribute(SUCCESS_KEY).getColumnName();
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable(LCS_LOG_ENTRY);
			pqs.appendSelectColumn(LCS_LOG_ENTRY, dbColumnForLastRun);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, runStatus, "SUCCESS", "="));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, "FLEXTYPEIDPATH", flexTypeIdPath, "="));
			pqs.appendSortBy("LCSLogEntry.createstampa2 DESC");
			LOGGER.info("------PreparedQueryStatement---Log Entry---"+pqs);
			
			if (isInfoEnabled)
			LOGGER.info((new StringBuilder()).append("getLastSuccessfulRun pqs : ").append(pqs).toString());
			SearchResults queryResult = LCSQuery.runDirectQuery(pqs);
			if (queryResult != null && queryResult.getResults().size() > 0) {
				Collection<?> results = queryResult.getResults();
				Iterator<?> resultsIter = results.iterator();
				if (resultsIter.hasNext()) {
					FlexObject fob = (FlexObject) resultsIter.next();
					if (isInfoEnabled)
						LOGGER.debug((new StringBuilder()).append("getLastSuccessfulRun fob : ").append(fob).toString());
					lastrunTime = fob.getString(
							(new StringBuilder()).append("LCSLogEntry.").append(dbColumnForLastRun).toString());
				}
			}
		} catch (WTException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
			e.printStackTrace();
		}
		return lastrunTime;
	}
	}
