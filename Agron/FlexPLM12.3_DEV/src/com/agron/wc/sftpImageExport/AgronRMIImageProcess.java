package com.agron.wc.sftpImageExport;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;

 
import com.jcraft.jsch.JSchException;
 
import com.lcs.wc.util.LCSProperties;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.util.WTException;


public class AgronRMIImageProcess implements RemoteAccess {
	
	private static RemoteMethodServer rmsObj = null;
	// Read from unpq file property file 
	private static final String USERNAME = LCSProperties.get("com.agron.wc.sftpImageExport.username", "wcadmin");
	private static final String PASSWORD = LCSProperties.get("com.agron.wc.sftpImageExport.password", "wcadmin");
	 
	
	/**
	 * This method is used to call the execute method.
	 * 
	 * @param args is String object
	 * @throws LCSException              is LCSException object
	 * @throws MalformedURLException     is MalformedURLException object
	 * @throws RemoteException           is RemoteException object
	 * @throws InvocationTargetException is InvocationTargetException object
	 */

	/*public static void main(String args[])
			throws LCSException, MalformedURLException, RemoteException, InvocationTargetException {
		executeImagePro(); // calling execute method to perform the integration process.
	}*/
	
	
	/*RemoteMethodServer server;
	WTProperties localWTProperties;
	try {
		localWTProperties = WTProperties.getLocalProperties();
		String backgroundServer = localWTProperties.getProperty("java.rmi.server.hostname");
		URL url = new URL("http://"+backgroundServer+"/Windchill/rfa");
		server = RemoteMethodServer.getInstance(url,"BackgroundMethodServer");
		
		server.setUserName(USERNAME);
		server.setPassword(PASSWORD);
		Class<?> cs[] = {String.class, String.class, String.class};
		Object args[] = {mode, sDate, eDate};	
 
		server.invoke("extractAndUploadSKUImage", "com.agron.wc.sftpImageExport.AgronRMIImageProcess", null, cs, args);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	
	public static void rmiImageProcess(String mode, String sDate, String eDate) throws MalformedURLException, RemoteException, InvocationTargetException {
		
		rmsObj = RemoteMethodServer.getDefault();
		rmsObj.setUserName(USERNAME);
		rmsObj.setPassword(PASSWORD);
		Class<?> cs[] = {String.class, String.class, String.class};
		Object args[] = {mode, sDate, eDate};	
 
	 rmsObj.invoke("extractAndUploadSKUImage", "com.agron.wc.sftpImageExport.AgronRMIImageProcess", null, cs, args);
		 
	}
	 
	/**
	 * 
	 * @param mode
	 * @param sDate
	 * @param endDate
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws InvocationTargetException
	 * @throws WTException
	 * @throws ParseException
	 * @throws JSchException
	 */
	public static void extractAndUploadSKUImage(String mode, String sDate, String endDate) throws MalformedURLException, RemoteException, InvocationTargetException, WTException, ParseException, JSchException{
		AgronImageExtraction imageExtraction = new AgronImageExtraction();
		imageExtraction.skuImageExtraction(mode, sDate,  endDate);
	}

}
