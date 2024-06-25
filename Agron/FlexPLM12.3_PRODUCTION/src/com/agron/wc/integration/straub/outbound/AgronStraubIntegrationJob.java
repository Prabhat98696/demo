package com.agron.wc.integration.straub.outbound;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class AgronStraubIntegrationJob {
	
	private static final Logger log = LogManager.getLogger("com.agron.wc.integration.straub.outbound");
	 /**
	 * This methode use to process image execution
	 * @param formDate
	 * @param toDate
	 */
	public static void executeStraubIntegartion(String mode, String fromDate, String toDate){ 

		try {
			log.info("Integartion Process START***");
			AgronRMIProcess.rmiDataProcess(mode, fromDate, toDate);
			log.info("Integartion Process END**"); 
		} 
		catch(Exception ex) {
			ex.printStackTrace();
			log.error("Exception AgronIntegrationJobProcess ", ex);
		}
	}

/**
* Main methode Start
* @param args
*/
	public static void main(String[] args) {
		String startdate = "";
		String endDate = "";

		System.out.println("date::" + args.length);
		
		String mode = 	args[0];
		if(args.length>=2){
			startdate =  args[1]; 
		}
		if(args.length==3){
			startdate =  args[1]; 
			endDate =  args[2]; 
		}
		
		System.out.println("InCall processDataIntegartion***");
		executeStraubIntegartion(mode, startdate, endDate);

	}

}
