package com.agron.wc.integration.batch;

import com.agron.wc.integration.util.AgronIntegrationUtil;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import java.text.*;

/**
 * @author Vinod D
 * @version 1.0
 * 
 * @author Mallikarjuna Savi
 * @version 2.0
 */

public final class AgronBatchScheduler implements RemoteAccess {
	private static RemoteMethodServer rmsObj = null;
	private static final String USERNAME = LCSProperties.get("com.agron.wc.integration.username", "wcadmin");
	private static final String PASSWORD = LCSProperties.get("com.agron.wc.integration.password", "wcadmin");
	
	/**
	 * Default Constructor
	 */
	private AgronBatchScheduler() {
	}

	/**
	 * This method is used to call the execute method.
	 * 
	 * @param args is String object
	 * @throws LCSException              is LCSException object
	 * @throws MalformedURLException     is MalformedURLException object
	 * @throws RemoteException           is RemoteException object
	 * @throws InvocationTargetException is InvocationTargetException object
	 */

	public static void main(String args[])
			throws LCSException, MalformedURLException, RemoteException, InvocationTargetException {
		execute(); // calling execute method to perform the integration process.
	}

	/**
	 * This method is used to call the executeBatchProcess(), using RMI.
	 * 
	 * @throws MalformedURLException     is MalformedURLException object
	 * @throws RemoteException           is RemoteException object
	 * @throws InvocationTargetException is InvocationTargetException object
	 */
	public static void execute() throws MalformedURLException, RemoteException, InvocationTargetException {
		rmsObj = RemoteMethodServer.getDefault();
		rmsObj.setUserName(USERNAME);
		rmsObj.setPassword(PASSWORD);
		Class<?> cs[] = new Class[0];
		Object args[] = new Object[0];
		rmsObj.invoke("executeBatchProcess", "com.agron.wc.integration.batch.AgronBatchScheduler", null, cs, args);
	}

	/**
	 * This method is used to add the queue
	 */
	public static void executeBatchProcess() {
		String queueName = null;
		queueName = LCSProperties.get("com.agron.wc.interface.scheduler.queue.name", "AgronSchedulerQueue");
		// adding process to the queue.
		AgronIntegrationUtil.addQueueEntry(queueName, "processor",
				"com.agron.wc.integration.batch.AgronBatchScheduler");
	}

	/**
	 * This method is used to call the executeBatchProcess method.
	 * 
	 * @throws WTException is WTException object
	 */
	public static void processor() throws WTException, ParseException {
		batchProcessor();
	}

	/**
	 * This method is ised to call the execute method.
	 * 
	 * @throws WTException is WTException object
	 */
	public static void batchProcessor() throws WTException, ParseException {
		AgronBatchProcessor agronBatchPro = new AgronBatchProcessor();
		agronBatchPro.execute();
	}

}
