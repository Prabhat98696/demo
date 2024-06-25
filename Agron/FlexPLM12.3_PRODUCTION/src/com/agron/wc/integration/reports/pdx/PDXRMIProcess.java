package com.agron.wc.integration.reports.pdx;


import com.agron.wc.integration.reports.pdx.PDXReportQueueProcessUtil;
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

public class PDXRMIProcess implements RemoteAccess {
	
	/**
	* @author Acnovate
	 * @version 1.0
	 */

		private static RemoteMethodServer rmsObj = null;
		private static final String USERNAME = LCSProperties.get("com.agron.wc.integration.straub.outbound.username", "wcadmin");
		private static final String PASSWORD = LCSProperties.get("com.agron.wc.integration.straub.outbound.password", "wcadmin");
		private static final Logger logger = LogManager.getLogger(PDXRMIProcess.class);
		
		/**
		 * Default Constructor
		 */
		private PDXRMIProcess() {
		}

		/**
		 * This method is used to call the executeBatchProcess(), using RMI.
		 * 
		 * @throws MalformedURLException     is MalformedURLException object
		 * @throws RemoteException           is RemoteException object
		 * @throws InvocationTargetException is InvocationTargetException object
		 */
		public static void rmiDataProcess(String mode, String sDate, String eDate,String user) throws MalformedURLException, RemoteException, InvocationTargetException {
			rmsObj = RemoteMethodServer.getDefault();
			rmsObj.setUserName(USERNAME);
			rmsObj.setPassword(PASSWORD);
			Class<?> cs[] = {String.class, String.class, String.class};
			Object args[] = {mode, sDate, eDate};
			executeRMIProcess(mode, sDate, eDate,user);			
			//rmsObj.invoke("executeRMIProcess", "com.agron.wc.integration.reports.pdx.PDXRMIProcess", null, cs, args);
		}

		
		/**
		 * This method is used to add the queue
		 */ 
		public static void executeRMIProcess(String mode, String sDate, String eDate, String user) {
			Class<?> cs[] = {String.class, String.class, String.class, String.class};
			Object args[] = {mode, sDate, eDate,user};
			System.out.println("0001 ---executeRMIProcess--- start");
			String queueName = LCSProperties.get("com.agron.wc.integration.reports.pdx.scheduler.queue.name", "AgronPDXReportSchedulerQueue");
			PDXReportQueueProcessUtil.addQueueEntry(queueName, "processor", "com.agron.wc.integration.reports.pdx.PDXRMIProcess",  cs, args);
			System.out.println("0010 ---executeRMIProcess--- end");
			logger.info("!!!rmiDataProcess END !!!");
		}

		/**
		 * This method is used to call the executeBatchProcess method.
		 * 
		 * @throws WTException is WTException object
		 */
		 public static void processor(String mode, String sDate, String eDate, String user) throws WTException, ParseException {
			
			 jobProcessor(mode, sDate, eDate,user);
		} 

		
		 /* This method is ised to call the execute method.
		 * 
		 * @throws WTException is WTException object
		 */
		 public static void jobProcessor(String mode, String sDate, String eDate, String user) throws WTException, ParseException {
			 try{
				 logger.info("!!!jobProcessor START MODE!!!"+mode);
				 logger.info("!!!MODE!!!"+mode+"!!!MODE!!!"+sDate+"!!!MODE!!!"+eDate+"!!!USER!!!"+user);
				 PDXDataProcessor processData = new PDXDataProcessor();
				 System.out.println("0001 ---processData--- start");
				 processData.startExecution(mode, sDate, eDate,user);
				 System.out.println("0010 ---processData--- end");
				 logger.info("!!!jobProcessor END !!!");
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
 
	}

