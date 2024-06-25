package com.agron.wc.integration.straub.outbound;


import com.agron.wc.integration.straub.util.AgronStraubQueueProcessUtil;
import com.lcs.wc.util.LCSProperties;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import java.text.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class AgronRMIProcess implements RemoteAccess {
	
	/**
	* @author Pawan Prajapat
	 * @version 2.0
	 */

		private static RemoteMethodServer rmsObj = null;
		private static final String USERNAME = LCSProperties.get("com.agron.wc.integration.straub.outbound.username", "wcadmin");
		private static final String PASSWORD = LCSProperties.get("com.agron.wc.integration.straub.outbound.password", "wcadmin");
		private static final Logger logger = LogManager.getLogger("com.agron.wc.integration.straub.outbound");
		
		/**
		 * Default Constructor
		 */
		private AgronRMIProcess() {
		}

		/**
		 * This method is used to call the executeBatchProcess(), using RMI.
		 * 
		 * @throws MalformedURLException     is MalformedURLException object
		 * @throws RemoteException           is RemoteException object
		 * @throws InvocationTargetException is InvocationTargetException object
		 */
		public static void rmiDataProcess(String mode, String sDate, String eDate) throws MalformedURLException, RemoteException, InvocationTargetException {
			rmsObj = RemoteMethodServer.getDefault();
			rmsObj.setUserName(USERNAME);
			rmsObj.setPassword(PASSWORD);
			Class<?> cs[] = {String.class, String.class, String.class};
			Object args[] = {mode, sDate, eDate};	
			rmsObj.invoke("executeRMIProcess", "com.agron.wc.integration.straub.outbound.AgronRMIProcess", null, cs, args);
		}

		
		/**
		 * This method is used to add the queue
		 */ 
		public static void executeRMIProcess(String mode, String sDate, String eDate) {
			Class<?> cs[] = {String.class, String.class, String.class};
			Object args[] = {mode, sDate, eDate};
			String queueName = LCSProperties.get("com.agron.wc.integration.straub.outbound.scheduler.queue.name", "AgronStraubSchedulerQueue");
			AgronStraubQueueProcessUtil.addQueueEntry(queueName, "processor", "com.agron.wc.integration.straub.outbound.AgronRMIProcess",  cs, args);
			logger.info("!!!rmiDataProcess END !!!");
		}

		/**
		 * This method is used to call the executeBatchProcess method.
		 * 
		 * @throws WTException is WTException object
		 */
		 public static void processor(String mode, String sDate, String eDate) throws WTException, ParseException {
			
			 jobProcessor(mode, sDate, eDate);
		} 

		
		 /* This method is ised to call the execute method.
		 * 
		 * @throws WTException is WTException object
		 */
		 public static void jobProcessor(String mode, String sDate, String eDate) throws WTException, ParseException {
			 try{
				 logger.info("!!!jobProcessor START MODE!!!"+mode);
				 logger.info("!!!MODE!!!"+mode+"!!!MODE!!!"+sDate+"!!!MODE!!!"+eDate);
				 AgronStraubDataProcessor processData = new AgronStraubDataProcessor();
				 processData.startExecution(mode, sDate, eDate);
				 logger.info("!!!jobProcessor END !!!");
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
 
	}

