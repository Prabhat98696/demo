package com.agron.wc.document;

import java.io.File;
import java.io.IOException;

import wt.util.WTProperties;

import com.lcs.wc.util.FormatHelper;

import com.lcs.wc.util.LCSProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Vinod Dalai
 * @version 1.0
 */
public final class AgronDocumentPluginsUtil {
	/**
	 * Defined to store the constant value.
	 */
	private static final String IAMGE_PATH = LCSProperties.get("com.lcs.wc.content.imagefilePathOverride");
	/**
	 * Defined to store the constant value.
	 */
	private static final String LINK_FOLDER = LCSProperties.get("com.lcs.wc.content.imageURL");
	/**
	 * Default Constructor. 
	 */
	public static final Logger logger = LogManager.getLogger("com.agron.wc.document");
	 
	private AgronDocumentPluginsUtil(){
	}
	/**
	 * This method is used to copy the image to target folder.
	 * @param sourceFilePath is String object
	 * @param targetPath is String object
	 * @param modifiedFileName is String object
	 * @return Boolean type
	 */
	public static boolean copyImage(String sourceFilePath, String targetPath, String modifiedFileName){
		logger.info("sourceFilePath::"+sourceFilePath+"targetPath ::"+targetPath+"modifiedFileName::"+modifiedFileName);
		boolean copied = false;
		String fileName = "";
		//try{
			File afile = new File(sourceFilePath);
			fileName = afile.getName();
			fileName = fileName.replaceAll(" ", "_");
			File tFile = new File(targetPath + modifiedFileName);
			if(tFile.exists()){
				tFile.delete();
			}
			if (afile.renameTo(new File(targetPath + modifiedFileName))) {
				logger.info("File Moved Successfully");
				copied = true;
			} else {
				//LCSLog.debug("File is failed to move!");
				copied = false;
			}
		//}catch(Exception e){
			//LCSLog.stackTrace(e);
		//}
		return copied;
	}
	/**
	 * This method is used to append the time stamp to the file.
	 * @param fileName is String object
	 * @return String type
	 */
	public static String appendTimeStamp(String fileName){
		String modifiedFile = "";
		String temp = null;
		temp = fileName.replace(".png", "");
		//try{
			java.util.TimeZone gmtTime = java.util.TimeZone.getTimeZone("GMT");
			java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(
					"MM_dd_yyyy_hh_mm_ss");
			dateFormat.setTimeZone(gmtTime);
			java.util.Date date = null;
			String strDate = null;
			date = new java.util.Date();
			strDate = dateFormat.format(date);
			modifiedFile = temp + strDate + ".png";
		//}catch(Exception e){
			//e.printStackTrace();
		//}
		return modifiedFile;
	}
	/**
	 * This method is used to delete the existing file from the system.
	 * @param fileLocation is String object
	 */
	public static void deleteExistingThumbnail(String fileLocation){
		String existingFileName = null;
		File existingFile = null;
		String wthome = null;
		String targetFilePath = null;	
		String imageThumbnailPath =fileLocation;
		
		try{
			if(FormatHelper.hasContent(fileLocation)){
				imageThumbnailPath = imageThumbnailPath.substring(
						imageThumbnailPath.lastIndexOf("images/") + 7,
						imageThumbnailPath.lastIndexOf("/") + 1);
				String[] fileArray = fileLocation.split("/");
				existingFileName = fileArray[(fileArray.length) - 1];
				if(FormatHelper.hasContent(IAMGE_PATH)){
					existingFile = new File(IAMGE_PATH + "/" + imageThumbnailPath + existingFileName);
				}else{
					wthome = WTProperties.getServerProperties().getProperty("wt.home");
					targetFilePath = (new StringBuilder()).append(wthome).append(File.separator).append("codebase/images/").toString();
					existingFile = new File(targetFilePath + existingFileName);
					logger.info("targetFilePath:::::::::::::::::::::"+targetFilePath+"existingFile ::::::::::::::::"+existingFile);
				}
				if(existingFile.exists()){
					logger.info("delete file!!! "+existingFile);
					existingFile.delete();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
