package com.agron.wc.sftpImageExport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.util.FormatHelper;

public class AgronThumbnailExportFileHelper {

	public static final Logger logger = LogManager.getLogger("com.agron.wc.sftpImageExport");
	
 	public static boolean createDir(String Path){
 		logger.info("Method createDir START::");
		File file = new File(Path);
	      //Creating the directory
	      boolean bool = file.mkdir();
	      if(bool){
	      }else{
	      }
	      logger.info("Method createDir END::");
		return true;
	}
 	
 	public static Boolean fileStatsCheck(String path, String fileName){
 		logger.info("Method fileStatsCheck START::");
 		boolean isFileExist=false;
 		try {
 			File file = new File(path+fileName);
 			if(file.exists()){
 				isFileExist=true;
 			}
 		} catch (Exception e) {
 			isFileExist=false;
 			//logger.debug("File not found to the server "+fileName);
 		}
 		logger.info("Method fileStatsCheck END::");
 		return isFileExist;
 	}
 	
 	public static String uploadFileContent(String currentLocalFile, String copyRemoteFileLoc, String fileName){
 		logger.info("Method uploadFileContent START::");
		logger.debug("currentLocalFile::"+currentLocalFile+", copyRemoteFileLoc::"+copyRemoteFileLoc+", New fileName::"+fileName);
		String message="FAILED";
	
		try {
			if(FormatHelper.hasContent(fileName) && uploadFileToShareDrive(currentLocalFile, copyRemoteFileLoc, fileName)){
				message="SUCCESS";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			message="FAILED";
		}
		logger.debug("Upload file *uploadFileContent***::"+message);
		logger.info("Method uploadFileContent END::");
		return message;
	} 
 	
 	
 	/**
	 * This method is used for copy file to share drive
	 * @param srFile
	 * @param dtFile
	 * @throws IOException
	 */
	
	public static boolean uploadFileToShareDrive(String srFile, String fileDirLocation, String fileName) {
		logger.info("Method uploadFileToShareDrive START::");
		boolean copyFile=false;
		try{

			File sourceFile = new File(srFile);
			//File destinationFile = new File(dtFile + sourceFile.getName());
			File destinationFile = new File(fileDirLocation + fileName);

			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			FileOutputStream fileOutputStream = new FileOutputStream(
					destinationFile);

			int bufferSize;
			byte[] bufffer = new byte[512];
			while ((bufferSize = fileInputStream.read(bufffer)) > 0) {
				fileOutputStream.write(bufffer, 0, bufferSize);
			}
			fileInputStream.close();
			fileOutputStream.close();
			copyFile=true;
		}
		catch (IOException e) {
			copyFile=false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Method uploadFileToShareDrive END::");
		return copyFile ;
	}
	
	/**
	 * This method is user for File remove
	 * @param channel
	 * @param fileName
	 * @return
	 */
	public static boolean removeFileContent(String remoteDirFileLoc, String fileName){
		logger.info("Method removeFileContent START::");
		boolean  isFileExist=false;
		
		try {
			boolean filecheck = fileStatsCheck(remoteDirFileLoc, fileName);
			if(filecheck){
				removeFileFromShareDrive(remoteDirFileLoc, fileName);
			}
			isFileExist=filecheck;

		} catch (Exception e) {
			e.printStackTrace();
			isFileExist = false;
		}
		logger.info("Method removeFileContent END::");
		return isFileExist;
	}
	
	/**
	 * Remove File form ShareDrive
	 * @param sorc
	 * @return
	 */
	
	public static boolean removeFileFromShareDrive(String fileDirPath, String fileName){
		logger.info("Method removeFileFromShareDrive START::");
		boolean fileRemove=false;
		File fdelete = new File(fileDirPath+fileName);
		if (fdelete.exists()) {
			if (fdelete.delete()) {
				fileRemove=false;
				System.out.println("file Deleted :" +fileName);
			} else {
				fileRemove=false;
				System.out.println("file not Deleted :"+fileName);
			}
		}
		logger.info("Method removeFileFromShareDrive END::");
		return fileRemove;
	}
	
}
