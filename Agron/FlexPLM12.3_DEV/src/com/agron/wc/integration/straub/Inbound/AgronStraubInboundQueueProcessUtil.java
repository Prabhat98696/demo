package com.agron.wc.integration.straub.Inbound;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.session.SessionHelper;
import wt.util.WTException;

public class AgronStraubInboundQueueProcessUtil {
	/**
	 * This method is used to add the batch process to the queue.
	 * 
	 * @param queueName is String object
	 * @param method    is String object
	 * @param className is String object
	 */
	private static final Logger logger = LogManager.getLogger("com.agron.wc.integration.straub.inbound");
	
	public static void addQueueEntry(String queueName, String method, String className, Class<?> argTypes[], Object args[] ) {
		try {
			
			logger.info("::addQueueEntry:::::");
			ProcessingQueue processQueue = getProcessQueue(queueName);
			wt.org.WTPrincipal user = SessionHelper.manager.getPrincipal();
			processQueue.addEntry(user, method, className, argTypes, args);
		} catch (WTException exp) {
			logger.error("ERROR# Could not put message in windchill queue");
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
			logger.info(":::::getProcessQueue:::::");
			QuerySpec querySpec = new QuerySpec(wt.queue.ProcessingQueue.class);
			SearchCondition searchCondition = new SearchCondition(wt.queue.ProcessingQueue.class, "name", "LIKE",
					strQueueName);
			querySpec.appendWhere(searchCondition, new int[] { 0 });
			logger.debug(":::::searchCondition:::::"+searchCondition);
			QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
			if (!queryResult.hasMoreElements()) {
				logger.debug(":::::::queryResult:::::"+queryResult);
				processingQueue = QueueHelper.manager.createQueue(strQueueName);
				processingQueue.setEnabled(true);
			} else {
				processingQueue = (ProcessingQueue) queryResult.nextElement();
			}
			logger.debug(":::::::::searchCondition:::::"+processingQueue);
		} catch (QueryException e) {
			logger.error("ERROR# Unable to get process queue for straub");
		} catch (WTException e) {
			logger.error("ERROR# Unable to get process queue for straub");
		}
		return processingQueue;
	}


}
