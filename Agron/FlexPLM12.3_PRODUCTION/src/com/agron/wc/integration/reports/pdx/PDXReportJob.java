package com.agron.wc.integration.reports.pdx;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class PDXReportJob {
	
	private static final Logger log = LogManager.getLogger(PDXReportJob.class);
	 /**
	 * This methode use to process image execution
	 * @param formDate
	 * @param toDate
	 */
	public static void executePDXReport(String mode, String fromDate, String toDate,String user){ 

		try {
			log.info("Integartion Process START***");
			System.out.println("0001 ---PDXReportJob");
			PDXRMIProcess.rmiDataProcess(mode, fromDate, toDate,user);
			System.out.println("0100 ---PDXReportJob");
			log.info("Integartion Process END**"); 
		} 
		catch(Exception ex) {
			ex.printStackTrace();
			log.error("Exception PDXReportJob ", ex);
		}
	}

/**
* Main methode Start
* @param args
*/
	public static void main(String[] args) {
		String startdate = "";
		String endDate = "";
		String user = "";

		System.out.println("date::" + args.length);
		
		String mode = 	args[0];
		if(args.length>=2){
			startdate =  args[1]; 
		}
		if(args.length==3){
			startdate =  args[1]; 
			endDate =  args[2]; 
		}
		if(args.length==4){
			startdate =  args[1]; 
			endDate =  args[2];
			user =  args[3];			
		}
		
		System.out.println("InCall processDataIntegartion***");
		executePDXReport(mode, startdate, endDate, user);

	}

}
