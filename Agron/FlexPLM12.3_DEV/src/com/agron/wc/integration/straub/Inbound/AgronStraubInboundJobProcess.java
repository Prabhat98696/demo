package com.agron.wc.integration.straub.Inbound;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.util.WTProperties;

public class AgronStraubInboundJobProcess {

	public static String logFileLocation = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;

	private static final Logger log = LogManager.getLogger("com.agron.wc.integration.straub.inbound");
	public static String INBOUND_DIR = LCSProperties.get("com.agron.wc.integration.straub.inbound.upload.fileDirPath");
	
	static{
		try{
			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			logFileLocation = wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.integration.straub.Inbound.GenerateExcelLogfilePath"));
			new File(logFileLocation).mkdirs();
		}catch(IOException io){
			io.printStackTrace();
		}
	}

	/**
	 * STart Execution from main
	 * @param agrs
	 */
	public static void main(String agrs[])
	{
		executePro();
	}

	/**
	 * 
	 */
	public static void executePro(){
		try {
			AgronStraubInboundRMI.rmiInboundDataProcess();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
