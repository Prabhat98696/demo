package com.agron.wc.sftpImageExport;

public class AgronSKUImageExport {

	 /**
	 * This methode use to process image execution
	 * @param mode
	 * @param formDate
	 * @param toDate
	 */
	public static void processImages(String mode, String fromDate, String toDate){

		try {
			System.out.println("***SFTP Images Process START**");
			AgronRMIImageProcess.rmiImageProcess(mode, fromDate, toDate);
			System.out.println("***SFTP Images Process END**");
		} 
		catch(Exception exec) {
			exec.getMessage();
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

		processImages(mode, startdate, endDate);

	}

}
