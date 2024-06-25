package com.agron.wc.integration.straub.Inbound;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.foundation.LCSQuery;


import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class AgronInboundLogEntry {

	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.straub.inbound");
	private static final String FLEX_TIME_FORMAT = "MM-dd-yyyy:hh:mm:ss a";
	private static String dash = "--------------------------------";
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	private static boolean isInfoEnabled = true;
	private static final String LCS_LOG_ENTRY = "LCSLogEntry";
	private static final String LASTRUNTIME_KEY =  LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.lastRunKey").trim();
	private static final String NAME_KEY = LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.nameKey").trim();
	private static final String STATUS_KEY =  LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.successKey").trim();
	private static final String MESSAGE_KEY =  LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.messageKey").trim();
	//private static final String URL_LOG_FILE =  LCSProperties.get("com.agron.wc.integration.straub.util.logEntry.logFileURL").trim();
	private static String URL_LOG_path = null;

	
	static {
		try{
			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			URL_LOG_path=  wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.integration.straub.util.logEntry.path"));
		} catch(IOException io){
			io.printStackTrace();
		}
	}
	public LCSLogEntry execute(String successOrFailure, String message) throws ParseException {
		LOGGER.info("Method Inbound LOG Entry execute START-->>>");
		LCSLogEntry logEntry= new LCSLogEntry();
		try {
			ClientContext context = ClientContext.getContext();

			String currentDateTime = getCurrentGMTTime();
			String lastRunTime = getLastSuccessfulRun();

			LOGGER.info(dash);
			LOGGER.info("Starting Data Inbound log entry Process  ");
			LOGGER.info((new StringBuilder()).append(" Start Time : ").append(currentDateTime).toString());
			LOGGER.info(dash);
			LOGGER.info((new StringBuilder()).append(" Last Run Time : ").append(lastRunTime).toString());
			LOGGER.info(dash);
			LOGGER.info(" **** Starting  Export Process ****  "); 

			logEntry=createLogEntry(message, currentDateTime, "StraubInboundFile", successOrFailure);

			LOGGER.info(" **** End  Export Process ****  ");
		} catch (WTException wtexception) {
			LOGGER.error("Error : " + wtexception.fillInStackTrace());
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			LOGGER.error("Error : " + wtpropertyvetoexception.fillInStackTrace());
		} catch (ParseException parseexception) {
			LOGGER.error("Error : " + parseexception.fillInStackTrace());
		}
		LOGGER.info("Method execute END-->>>");
		return logEntry;
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
	public LCSLogEntry createLogEntry(String message, String currentTime, String logEntryName, String successOrFailure)
			throws WTException, WTPropertyVetoException, ParseException {
		LOGGER.info("Method createLogEntry START-->>>");
		LCSLogEntry lcslogentry = LCSLogEntry.newLCSLogEntry();
		String currentTimeStamp = null;
		try {
			FlexType flextype = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.lcslogEntry"));
			lcslogentry.setFlexType(flextype);
			lcslogentry.setValue(LASTRUNTIME_KEY, currentTime);
			currentTimeStamp = getlogTimeStamp(currentTime).toString();
			currentTimeStamp = currentTimeStamp.replaceAll("[-+.^:,]", "");
			LOGGER.info("-----Inbound LogEntry --currentTimeStamp------>"+currentTimeStamp+", message-->"+message);
			lcslogentry.setValue(NAME_KEY,
					(new StringBuilder()).append(logEntryName).append("_").append(currentTimeStamp).toString());
			lcslogentry.setValue(STATUS_KEY, successOrFailure);
			lcslogentry.setValue(MESSAGE_KEY, message);
			//lcslogentry.setValue(URL_LOG_FILE, URL_LOG_path);
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
		LOGGER.info("Method createLogEntry END-->>>");
		return lcslogentry;
	}


	/**
	 * This method is used to update the successful log entry.
	 * 
	 * @param message          is String object
	 * @param currentTime      is String object
	 * @param logEntryName     is String object
	 * @param successOrFailure is String object
	 * @throws WTException             type
	 * @throws WTPropertyVetoException type
	 * @throws ParseException          type
	 */
	public void updateLogEntry(LCSLogEntry lcslogentry, String successOrFailure, String message)
			throws WTException, WTPropertyVetoException, ParseException {
		LOGGER.info("Method --updateLogEntry START--->>");
		try {
			lcslogentry.setValue(STATUS_KEY, successOrFailure);
			lcslogentry.setValue(MESSAGE_KEY, message);
			LOGGER.info("::successOrFailure------>"+successOrFailure+", message---->"+message);
			(new LCSLogEntryLogic()).saveLog(lcslogentry);
			LOGGER.info((new StringBuilder()).append("Exiting update Logentry : ").append(lcslogentry).toString());
		} catch (WTException e) {
			LOGGER.error("****Error : " + e.fillInStackTrace());
			e.printStackTrace();
		}
		LOGGER.info("Method --updateLogEntry END--->>");
	}


	/*public boolean updateLogEntryWithMapValue(LCSLogEntry lcslogentry, HashMap<String,Object> map) throws WTException, WTPropertyVetoException, ParseException {
		LOGGER.info("Method execute START-->>>");
		boolean update=false;
		try {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if(FormatHelper.hasContent(key))
				{
					lcslogentry.setValue(key, value);
				}
			}
			(new LCSLogEntryLogic()).saveLog(lcslogentry);
			LOGGER.info((new StringBuilder()).append("Exiting update Logentry : ").append(lcslogentry).toString());
			update=true;
		} catch (WTException e) {
			update=true;
			LOGGER.error("Error : " + e.fillInStackTrace());
			e.printStackTrace();
		}
		return update;
	}*/

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
		LOGGER.info("Method getLastSuccessfulRun START-->>>");
		String lastrunTime = null;
		try {
			FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.lcslogEntry"));
			String flexTypeIdPath = logEntryType.getIdPath();
			String dbColumnForLastRun = logEntryType.getAttribute(LASTRUNTIME_KEY).getColumnName();
			String runStatus = logEntryType.getAttribute(STATUS_KEY).getColumnName();
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable(LCS_LOG_ENTRY);
			pqs.appendSelectColumn(LCS_LOG_ENTRY, dbColumnForLastRun);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, runStatus, "SUCCESS", "="));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, "FLEXTYPEIDPATH", flexTypeIdPath, "="));
			pqs.appendSortBy("LCSLogEntry.createstampa2 DESC");

			if (isInfoEnabled)
				LOGGER.info((new StringBuilder()).append("getLastSuccessfulRun pqs : ").append(pqs).toString());
			SearchResults queryResult = LCSQuery.runDirectQuery(pqs);
			//LOGGER.debug("-------queryResult------"+queryResult);
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
		LOGGER.info("Method getLastSuccessfulRun END-->>>"+lastrunTime);
		return lastrunTime;
	}


	/**
	 * This method is used to get the last run time.
	 * 
	 * @return String type
	 */
	public String getLastRunTime() {
		LOGGER.info("Method getLastRunTime START-->>>");
		String lastrunTime = null;
		try {
			FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.lcslogEntry"));
			String flexTypeIdPath = logEntryType.getIdPath();
			String dbColumnForLastRun = logEntryType.getAttribute(LASTRUNTIME_KEY).getColumnName();
			//String runStatus = logEntryType.getAttribute(STATUS_KEY).getColumnName();
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable(LCS_LOG_ENTRY);
			pqs.appendSelectColumn(LCS_LOG_ENTRY, dbColumnForLastRun);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, "FLEXTYPEIDPATH", flexTypeIdPath, "="));
			pqs.appendSortBy("LCSLogEntry.createstampa2 DESC");

			if (isInfoEnabled)
				LOGGER.info((new StringBuilder()).append("getLastRun pqs : ").append(pqs).toString());
			SearchResults queryResult = LCSQuery.runDirectQuery(pqs);
			//LOGGER.debug("-------queryResult------"+queryResult);
			if (queryResult != null && queryResult.getResults().size() > 0) {

				Collection<?> results = queryResult.getResults();
				Iterator<?> resultsIter = results.iterator();
				if (resultsIter.hasNext()) {
					FlexObject fob = (FlexObject) resultsIter.next();
					if (isInfoEnabled)
						LOGGER.debug((new StringBuilder()).append("getLastRun fob : ").append(fob).toString());
					lastrunTime = fob.getString(
							(new StringBuilder()).append("LCSLogEntry.").append(dbColumnForLastRun).toString());
				}
			}
		} catch (WTException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
			e.printStackTrace();
		}
		LOGGER.info("Method getLastRunTime END-->>>");
		return lastrunTime;
	}

	/**
	 * This method is used to get the last run time.
	 * 
	 * @return String type
	 */
	public String getStatusOfLogEntry() {
		LOGGER.info("Method getStatusOfLogEntry START-->>>");
		String getStatus = null;
		try {
			FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.integration.straub.Inbound.logEntry.lcslogEntry"));
			String flexTypeIdPath = logEntryType.getIdPath();
			String dbColumnForStatus = logEntryType.getAttribute(STATUS_KEY).getColumnName();
			//String runStatus = logEntryType.getAttribute(STATUS_KEY).getColumnName();
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable(LCS_LOG_ENTRY);
			pqs.appendSelectColumn(LCS_LOG_ENTRY, dbColumnForStatus);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, "FLEXTYPEIDPATH", flexTypeIdPath, "="));
			pqs.appendSortBy("LCSLogEntry.createstampa2 DESC");

			if (isInfoEnabled)
				LOGGER.info((new StringBuilder()).append("getStatus pqs : ").append(pqs).toString());
			SearchResults queryResult = LCSQuery.runDirectQuery(pqs);
			//LOGGER.debug("-------queryResult------"+queryResult);
			if (queryResult != null && queryResult.getResults().size() > 0) {

				Collection<?> results = queryResult.getResults();
				Iterator<?> resultsIter = results.iterator();
				if (resultsIter.hasNext()) {
					FlexObject fob = (FlexObject) resultsIter.next();
					if (isInfoEnabled)
						LOGGER.debug((new StringBuilder()).append("getStatus flexObj : ").append(fob).toString());
					getStatus = fob.getString((new StringBuilder()).append("LCSLogEntry.").append(dbColumnForStatus).toString());
				}
			}
		} catch (WTException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
			e.printStackTrace();
		}
		LOGGER.info("Method getStatusOfLogEntry END-->>>");
		return getStatus;
	}
}
