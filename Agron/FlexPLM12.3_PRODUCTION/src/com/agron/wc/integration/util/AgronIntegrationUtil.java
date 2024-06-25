package com.agron.wc.integration.util;

import com.lcs.wc.db.*;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.LCSProperties;
import java.sql.Timestamp;
import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import wt.fc.*;
import wt.query.*;
import wt.queue.*;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author Mallikarjuna Savi
 *
 */
public class AgronIntegrationUtil {
	/**
	 * Defined to store the constant value.
	 */
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");
	private static boolean isInfoEnabled = true;
	private static final String LCS_LOG_ENTRY = "LCSLogEntry";
	private static final String LASTRUNTIME_KEY = LCSProperties.get("com.agron.wc.logEntry.lastRunKey",
			"agronLastRunTime");
	private static final String NAME_KEY = LCSProperties.get("com.agron.wc.logEntry.nameKey", "name");
	private static final String SUCCESS_KEY = LCSProperties.get("com.agron.wc.logEntry.successKey", "agronSuccess");
	private static final String MESSAGE_KEY = LCSProperties.get("com.agron.wc.logEntry.messageKey", "agronMessage");

	public AgronIntegrationUtil() {
	}

	/**
	 * This method is used to add the batch process to the queue.
	 * 
	 * @param queueName is String object
	 * @param method    is String object
	 * @param className is String object
	 */
	public static void addQueueEntry(String queueName, String method, String className) {
		try {
			ProcessingQueue processQueue = getProcessQueue(queueName);
			wt.org.WTPrincipal user = SessionHelper.manager.getPrincipal();
			Class<?> argTypes[] = new Class[0];
			Object args[] = new Object[0];
			processQueue.addEntry(user, method, className, argTypes, args);
		} catch (WTException exp) {
			LOGGER.error("ERROR# Could not put message in windchill queue");
			exp.printStackTrace();
		}
	}

	/**
	 * This method is used to get the process queue.
	 * 
	 * @param strQueueName is String object
	 * @return ProcessingQueue type
	 */
	public static ProcessingQueue getProcessQueue(String strQueueName) {
		ProcessingQueue processingQueue = null;
		try {
			QuerySpec querySpec = new QuerySpec(wt.queue.ProcessingQueue.class);
			SearchCondition searchCondition = new SearchCondition(wt.queue.ProcessingQueue.class, "name", "LIKE",
					strQueueName);
			querySpec.appendWhere(searchCondition, new int[] { 0 });
			QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
			if (!queryResult.hasMoreElements()) {
				processingQueue = QueueHelper.manager.createQueue(strQueueName);
				processingQueue.setEnabled(true);
			} else {
				processingQueue = (ProcessingQueue) queryResult.nextElement();
			}
		} catch (QueryException e) {
			LOGGER.error("ERROR# Unable to get process queue");
		} catch (WTException e) {
			LOGGER.error("ERROR# Unable to get process queue");
		}
		return processingQueue;
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
			LOGGER.info("-------Create Log Entry in integration Util------>");
			FlexType flextype = FlexTypeCache
					.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.interface.LcsLogEntry.outbound"));
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
			FlexType logEntryType = FlexTypeCache
					.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.interface.LcsLogEntry.outbound"));
			String flexTypeIdPath = logEntryType.getIdPath();
			String dbColumnForLastRun = logEntryType.getAttribute(LASTRUNTIME_KEY).getColumnName();
			String runStatus = logEntryType.getAttribute(SUCCESS_KEY).getColumnName();
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable(LCS_LOG_ENTRY);
			pqs.appendSelectColumn(LCS_LOG_ENTRY, dbColumnForLastRun);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, runStatus, "1", "="));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCS_LOG_ENTRY, "FLEXTYPEIDPATH", flexTypeIdPath, "="));
			pqs.appendSortBy("LCSLogEntry.createstampa2 DESC");
			if (isInfoEnabled)
				LOGGER.info((new StringBuilder()).append("getLastSuccessfulRun pqs : ").append(pqs).toString());
			SearchResults queryResult = LCSQuery.runDirectQuery(pqs);
			if (queryResult != null && queryResult.getResults().size() > 0) {
				Collection<?> results = queryResult.getResults();
				Iterator<?> resultsIter = results.iterator();
				if (resultsIter.hasNext()) {
					FlexObject fob = (FlexObject) resultsIter.next();
					if (isInfoEnabled)
						LOGGER.info((new StringBuilder()).append("getLastSuccessfulRun fob : ").append(fob).toString());
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
