package com.agron.wc.integration.straub.Inbound;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.util.LCSProperties;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.util.WTException;

public class AgronStraubInboundRMI implements RemoteAccess  {
	
	private static RemoteMethodServer rmsObj = null;
	private static final String USERNAME = LCSProperties.get("com.agron.wc.integration.straub.outbound.username", "wcadmin");
	private static final String PASSWORD = LCSProperties.get("com.agron.wc.integration.straub.outbound.password", "wcadmin");
	private static final Logger logger = LogManager.getLogger("com.agron.wc.integration.straub.inbound");
	
	static AgronStraubInboundRMI object = new AgronStraubInboundRMI();
	/**
	 * Default Constructor
	 */
	private AgronStraubInboundRMI() {
		 
	}

	/**
	 * This method is used to call the executeBatchProcess(), using RMI.
	 * 
	 * @throws MalformedURLException     is MalformedURLException object
	 * @throws RemoteException           is RemoteException object
	 * @throws InvocationTargetException is InvocationTargetException object
	 */
	public static void rmiInboundDataProcess() throws MalformedURLException, RemoteException, InvocationTargetException {
		System.out.println("!!!AgronStraubInboundRMI STR !!!");
		rmsObj = RemoteMethodServer.getDefault();
		rmsObj.setUserName(USERNAME);
		rmsObj.setPassword(PASSWORD);
		Class<?> cs[] = {};
		Object args[] = {};	
		rmsObj.invoke("executeRMIUploaderProcess", "com.agron.wc.integration.straub.Inbound.AgronStraubInboundRMI", null, cs, args);
	}

	
	/**
	 * This method is used to add the queue
	 */ 
	public static void executeRMIUploaderProcess() {
		System.out.println("!!!executeRMIUploaderProcess STR>>");
		Class<?> cs[] = {};
		Object args[] = {};
		String queueName = LCSProperties.get("com.agron.wc.integration.straub.inbound.scheduler.queue.name", "AgronStraubInboundSchedulerQueue");
		AgronStraubInboundQueueProcessUtil.addQueueEntry(queueName, "inboundProcessor", "com.agron.wc.integration.straub.Inbound.AgronStraubInboundRMI",  cs, args);
		logger.info("!!!executeRMIUploaderProcess END !!!");
	}

	/**
	 * This method is used to call the executeBatchProcess method.
	 * 
	 * @throws WTException is WTException object
	 */
	 public static void inboundProcessor() throws WTException, ParseException {
		 logger.info("!!inboundProcessor START >>");
		 jobInboundProcessor();
		 logger.info("!!inboundProcessor END >>");
	} 

	
	 /* This method is ised to call the execute method.
	 * 
	 * @throws WTException is WTException object
	 */
	 public static void jobInboundProcessor() throws WTException, ParseException {
		 logger.info("!!!jobInboundProcessor START !!!");
		 AgronStraubInboundUploader dataUploader = new AgronStraubInboundUploader();
		 dataUploader.executeStart();
		 logger.info("!!!jobInboundProcessor END !!!");
	}

}
