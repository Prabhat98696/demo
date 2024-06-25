package com.agron.wc.sftpImageExport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.agron.wc.sftpImageExport.AgronSFTPServerConfig;
import com.agron.wc.sftpImageExport.AgronSKUPrepareQuery;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.util.WTException;
import wt.util.WTProperties;

public class AgronImageExtraction {

	public static String dataLogFileName;
	public static String logFileLocation = null;
	public static String COMMA_DELIMITER = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	public static BufferedWriter dataLogWriter = null;
	static String IMAGE_PATH = null;
	public static int timeInterval = LCSProperties.get("com.agron.wc.sftpImageExport.ncTimeInterval", 0);
	public static final Logger logger = LogManager.getLogger("com.agron.wc.sftpImageExport");
	static int successCount = 0;
	static int failedCount = 0;

	static {
		try {
			COMMA_DELIMITER = LCSProperties.get("com.agron.wc.sftpImageExport.commaDelimiter");
			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			logFileLocation = wtHomePath + FormatHelper
					.formatOSFolderLocation(LCSProperties.get("com.agron.wc.sftpImageExport.logFileLocation"));
			IMAGE_PATH = wtHomePath
					+ FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.sftpImageExport.ImagePath"));
			new File(logFileLocation).mkdirs();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	/**
	 * 
	 * @param mode
	 * @param startDate
	 * @param eDate
	 * @throws WTException
	 * @throws ParseException
	 * @throws JSchException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws InvocationTargetException
	 */
	public static void skuImageExtraction(String mode, String startDate, String eDate) throws WTException,
			ParseException, JSchException, MalformedURLException, RemoteException, InvocationTargetException {

		String sDate = startDate;
		String endDate = eDate;
		SearchResults sr = null;
		String status = "FAILED";
		String error = "";
		StringBuffer productWork = null;
		boolean isValid = false;
		boolean fileDirCreate = false;

		Channel channel = null;
		boolean fileRemoved = false;
		boolean removeDirectory = false;
		List<Map<String, Object>> listOfMapValue = new ArrayList<Map<String, Object>>();
		try {
			if ("".equals(sDate) || sDate == null) {
				AgronImageExportLogEntry ImageExportProcess = new AgronImageExportLogEntry();
				sDate = ImageExportProcess.getLastSuccessfulRun();

			}
			if ("".equals(endDate) || endDate == null) {
				AgronImageExportLogEntry ImageExportProcess = new AgronImageExportLogEntry();
				endDate = ImageExportProcess.getCurrentGMTTime();
			}

			if (FormatHelper.hasContent(sDate)) {
				sDate = subtractTimeFromDate(sDate, timeInterval);
			}

			Map<String, Object> mapOfValue;
			AgronSKUPrepareQuery runQuery = new AgronSKUPrepareQuery();
			if ("FR".equals(mode.trim())) {
				sr = runQuery.findSKUPreparedQueryFR(sDate, endDate);
				removeDirectory = true;
			} else if ("NC".equals(mode.trim())) {
				removeDirectory = false;
				if (!"".equals(sDate) && sDate != null) {
					sr = runQuery.findSKUPreparedQueryNC(sDate, endDate);
				} else {
					System.out.println("No START DATE found please run FR first................");
					logger.debug("No START DATE found please run FR first................");
					System.exit(0);
				}
			}

			if (channel == null) {
				Session session = AgronSFTPServerConfig.getSFTPSession();
				if (session != null) {
					channel = session.openChannel("sftp");
					if (channel != null) {
						channel.connect();
						logger.debug("channel >>>>" + channel);
					}
				}

			}
			if ("FR".equals(mode.trim())) {
				fileDirCreate = AgronSFTPServerConfig.renameORCreateDirectory(channel);
			}

			String dataLogFile = null;
			try {
				dataLogFile = setLogFile(sDate, endDate);
			} catch (WTException wt) {
				wt.printStackTrace();
			}

			ArrayList dataList = new ArrayList();
			dataList.add("Work#");
			dataList.add("Status");
			dataList.add("UploadFile");
			dataList.add("Message");
			productWork = getFormattedLogEntryRow(dataList);

			writeToDataLog(productWork.toString());

			Collection<FlexObject> prodObj = sr.getResults();
			logger.debug("Search Result AgronSKUPrepareQuery Row Size !! " + prodObj.size());
			Iterator<FlexObject> flexObjItr = prodObj.iterator();
			if (prodObj.size() > 0) {
				while (flexObjItr.hasNext()) {
					mapOfValue = new HashMap<String, Object>();
					FlexObject fObject = flexObjItr.next();
					String clrlatter = fObject.getString("LCSSKU.ptc_str_6typeInfoLCSSKU");
					String skuAti = fObject.getString("LCSSKU.ptc_str_4typeInfoLCSSKU");
					String finname = fObject.getString("LCSSKU.PARTPRIMARYIMAGEURL");
					String work = fObject.getString("LCSProduct.ptc_lng_4typeInfoLCSProduct");
					String skubranchId = fObject.getString("LCSSKU.BRANCHIDITERATIONINFO");

					mapOfValue.put("colorwayLetter", fObject.getString("LCSSKU.ptc_str_6typeInfoLCSSKU"));
					mapOfValue.put("colorwayArticle", fObject.getString("LCSSKU.ptc_str_4typeInfoLCSSKU"));
					mapOfValue.put("fileName", fObject.getString("LCSSKU.PARTPRIMARYIMAGEURL"));
					mapOfValue.put("prodWork", fObject.getString("LCSProduct.ptc_lng_4typeInfoLCSProduct"));
					mapOfValue.put("skuBranchId", skubranchId);

					listOfMapValue.add(mapOfValue);
					logger.debug("***Colorway letter : " + clrlatter + "!!, SKU Article " + skuAti + "!!, File Name "
							+ finname + "!!, Work no " + work);

				}
			}
			if (listOfMapValue.size() > 0) {
				if (channel != null && channel.isConnected() == true) {
					logger.debug("!!! List Of value Map !!!" + listOfMapValue);
					for (Map<String, Object> mapValue : listOfMapValue) {
						logger.debug("!! Map Value !!!" + mapValue);
						String coloorwayLetter = "";
						String skuArticle = "";
						String filename = "";
						String prodWork = "";
						String skuId = "";
						for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
							String key = entry.getKey();
							Object value = entry.getValue();

							if ("colorwayLetter".equals(key)) {
								coloorwayLetter = (String) entry.getValue();
							}
							if ("colorwayArticle".equals(key)) {
								skuArticle = (String) entry.getValue();
							}
							if ("fileName".equals(key)) {
								filename = (String) entry.getValue();
							}
							if ("prodWork".equals(key)) {
								prodWork = (String) entry.getValue();
							}
							if ("skuBranchId".equals(key)) {
								skuId = (String) entry.getValue();
							}

						}

						if ((filename == null || "".equals(filename.trim())) && "NC".equals(mode)) {
							fileRemoved = true;
						}

						String fileNameArticleno = "";
						String fileNameWithWorkno = "";
						String fileExists = null;
						productWork = new StringBuffer();
						productWork.append(prodWork);

						if (filename != null && !"".equals(filename.trim())) {
							if (filename.indexOf("RFAImages") > -1) {
								// to remove the /RFAImages from path
								filename = filename.substring(10);
							} else if (filename.indexOf("Windchill/000001") > -1) {
								// to remove the /Windchill/rfa/images from path
								filename = filename.substring(21);
							} else if (filename.indexOf("Windchill") > -1) {
								// to remove the /Windchill/rfa/images from path
								filename = filename.substring(10);
							}
						}

						// logger.debug("filename "+filename);
						String filenameExtension = null;
						if (filename != null && !"".equals(filename.trim())) {
							filenameExtension = filename.substring(filename.lastIndexOf("."), filename.length());
						} else {
							filenameExtension = ".png";
						}

						if (FormatHelper.hasContent(skuArticle)) {
							if (!skuArticle.contains(",")) {
								fileNameArticleno = skuArticle + "_30_CAD" + filenameExtension;
								fileNameWithWorkno = prodWork + "_" + coloorwayLetter + "_CAD" + filenameExtension;
								/*
								 * if("NC".equals(mode)){ fileNameWithWorkno =
								 * prodWork+"_"+coloorwayLetter+"_CAD"+filenameExtension;}
								 */
							}
						} else {
							fileNameWithWorkno = prodWork + "_" + coloorwayLetter + "_CAD" + filenameExtension;
						}

						File file = new File(IMAGE_PATH + filename.trim());
						boolean fileModified = true;

						if (FormatHelper.hasContent(filename) && file.exists()) {
							String fileLocation = IMAGE_PATH + filename;
							logger.debug("!!!Filename Path !!!" + fileLocation);

							if ("NC".equals(mode)) {
								fileModified = checkFileModifiedDate(file, sDate, endDate);
							}

							if (!skuArticle.contains(",") && !skuArticle.contains("-")) {
								Map<String, String> removeArticleUpdatedFile = new HashMap<String, String>();
								String removedOldFile = "";
								String oldFileName = "";
								if (fileModified) {
									logger.debug("*****UPLOAD FILE****");
									// If article number is changed,delete previous file from sftp
									removeArticleUpdatedFile = checkForArticleNumberChange(channel, skuId,
											filenameExtension);
									removedOldFile = (String) (removeArticleUpdatedFile.get("removedOldFile"));
									oldFileName = ((String) removeArticleUpdatedFile.get("oldArticleFile"));

									uploadImageToSftp(productWork, fileNameWithWorkno, fileNameArticleno, skuArticle,
											filename, file.toString(), channel, removedOldFile, oldFileName);

								} else {
									if ((FormatHelper.hasContent(skuArticle))) {
										if (!AgronSFTPServerConfig.fileStatsCheck(channel, fileNameArticleno)) {
											removeArticleUpdatedFile = checkForArticleNumberChange(channel, skuId,
													filenameExtension);
											removedOldFile = (String) (removeArticleUpdatedFile.get("removedOldFile"));
											oldFileName = ((String) removeArticleUpdatedFile.get("oldArticleFile"));
											uploadImageToSftp(productWork, fileNameWithWorkno, fileNameArticleno,
													skuArticle, filename, file.toString(), channel, removedOldFile,
													oldFileName);
										}
									}
									if (!AgronSFTPServerConfig.fileStatsCheck(channel, fileNameWithWorkno)) {
										uploadImageToSftp(productWork, fileNameWithWorkno, fileNameArticleno,
												skuArticle, filename, file.toString(), channel, removedOldFile,
												oldFileName);
									}
								}
							} else {
								logger.debug("*****UPLOAD SKIP****");
								if (skuArticle.contains(",")) {
									skuArticle = skuArticle.replaceAll(",", "_");
								}
								fileNameArticleno = skuArticle;
								failedCount++;
								if (skuArticle.contains(",")) {
									appendRowLoadStatus(productWork, fileNameArticleno, "FAILED",
											"Skip comma separated article");
								} else {
									appendRowLoadStatus(productWork, fileNameArticleno, "FAILED",
											"Skip negative article no");
								}
							}
						} else if (FormatHelper.hasContent(filename) && !file.exists()) {
							failedCount++;
							logger.debug("Image file does not exsit at loaction" + IMAGE_PATH + filename);
							if ((FormatHelper.hasContent(fileNameArticleno))) {
								appendRowLoadStatus(productWork, fileNameWithWorkno + "||" + fileNameArticleno,
										"FAILED", "Image not found at " + IMAGE_PATH + filename);
							} else {
								appendRowLoadStatus(productWork, fileNameWithWorkno, "FAILED",
										"Image not found at " + IMAGE_PATH + filename);
							}
						} else {
							boolean fileRemove = false;

							if (fileRemoved && "NC".equals(mode)) {
								if (FormatHelper.hasContent(skuArticle)) {
									fileRemove = AgronSFTPServerConfig.removeFileContent(channel, fileNameArticleno);
								}
								fileRemove = AgronSFTPServerConfig.removeFileContent(channel, fileNameWithWorkno);
							}

							if (fileRemove) {
								successCount++;
								logger.debug("Image Removed successfully");

								if (FormatHelper.hasContent(fileNameArticleno)) {
									appendRowLoadStatus(productWork, fileNameWithWorkno + "||" + fileNameArticleno,
											"SUCCESS", "Image removed successfully");
								} else {
									appendRowLoadStatus(productWork, fileNameWithWorkno, "SUCCESS",
											"Image removed successfully");
								}
							}
							/*
							 * else{ failedCount++;
							 * System.out.println("Image file does not exsit at loaction"+IMAGE_PATH+
							 * filename); appendRowLoadStatus(productWork, destinationFileName, "FAILED",
							 * "Image not found at "+IMAGE_PATH+filename); }
							 */
						}

					}

					status = "SUCCESS";
					error = "Updated Successfully";
					isValid = true;

					logger.debug("******Image Upload Summary**********");
					logger.debug("****SUCCESS Row***::::" + successCount);
					logger.debug("****FAILED Row****::::" + failedCount);
					logger.debug("###################################");
					logger.debug("Excle uploade logs location::" + dataLogFileName);

				} else {
					error = "Error in gettting in Channel is : " + channel;
					status = "FAILED";
					isValid = false;

				}
			} else {
				error = "Record not found";
				status = "SUCCESS";
				isValid = true;

			}
			AgronImageEmail.notifyPLMUser(isValid, listOfMapValue.size(), successCount, failedCount, dataLogFileName,
					error);
			AgronImageExportLogEntry agronProc = new AgronImageExportLogEntry();
			agronProc.execute(status, error);
		} catch (Exception exec) {
			error = exec.getMessage();
			status = "FAILED";
			isValid = false;
			AgronImageEmail.notifyPLMUser(isValid, listOfMapValue.size(), successCount, failedCount, dataLogFileName,
					error);
			AgronImageExportLogEntry agronPro = new AgronImageExportLogEntry();
			agronPro.execute(status, error);
			exec.printStackTrace();
		} finally {
			successCount = 0;
			failedCount = 0;
			logger.debug("**********EXECUTION END ***************");
			if (channel != null) {
				channel.disconnect();
				channel = null;
			}
			AgronSFTPServerConfig.closeSession();
		}

	}

	private static Map<String, String> checkForArticleNumberChange(Channel channel, String skuId,
			String filenameExtension) throws WTException {
		Map<String, String> removeArticleUpdatedMap = new HashMap<String, String>();
		LCSSKU skuObj = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuId);
		if (((LCSSKU) VersionHelper.predecessorOf(skuObj)) != null) {

			if (FormatHelper
					.hasContent((String) ((LCSSKU) VersionHelper.predecessorOf(skuObj)).getValue("agrArticle"))) {
				String oldArticleNumber = (String) (((LCSSKU) VersionHelper.predecessorOf(skuObj))
						.getValue("agrArticle"));
				if(FormatHelper
					.hasContent((String) (skuObj).getValue("agrArticle"))&&
					(!(((String) (skuObj).getValue("agrArticle")).equals((String) ((LCSSKU) VersionHelper.predecessorOf(skuObj)).getValue("agrArticle"))))) {
				String oldArticleFile = oldArticleNumber + "_30_CAD" + filenameExtension;
				boolean removeArticleUpdatedFile = AgronSFTPServerConfig.removeFileContent(channel, oldArticleFile);
				if (removeArticleUpdatedFile) {
					removeArticleUpdatedMap.put("removedOldFile", "true");
				} else {
					removeArticleUpdatedMap.put("removedOldFile", "false");
				}
				removeArticleUpdatedMap.put("oldArticleFile", oldArticleFile);
			}
			}
		}
		return removeArticleUpdatedMap;
	}

	/**
	 * This method is use for uploading image to server
	 * 
	 * @param prodWorkNo
	 * @param fileNameByWorkNo
	 * @param fileNameByArticle
	 * @param skuArticle
	 * @param skuThumbnail
	 * @param fileName
	 * @param channel
	 */

	public static void uploadImageToSftp(StringBuffer prodWorkNo, String fileNameByWorkNo, String fileNameByArticle,
			String skuArticle, String skuThumbnail, String fileName, Channel channel, String articleUpdated,
			String oldFileName) {
		if (FormatHelper.hasContent(skuThumbnail)) {
			String uploadFileWithWork = AgronSFTPServerConfig.uploadFileContent(channel, fileName, fileNameByWorkNo);
			if (FormatHelper.hasContent(skuArticle)) {
				String uploadFileWithArticle = AgronSFTPServerConfig.uploadFileContent(channel, fileName,
						fileNameByArticle);
				if ("SUCCESS".equals(uploadFileWithArticle)) {
					successCount++;
					
					if (FormatHelper.hasContent(articleUpdated) && articleUpdated.equalsIgnoreCase("true")) {
						appendRowLoadStatus(prodWorkNo, fileNameByWorkNo + "||" + fileNameByArticle, "SUCCESS",
								"Article number changed - old image " + oldFileName
										+ " removed and new image updated successfully.");
					} else {
						appendRowLoadStatus(prodWorkNo, fileNameByWorkNo + "||" + fileNameByArticle, "SUCCESS",
								"Image updated successfully.");
						logger.debug("Product Work :" + prodWorkNo + ", !! fileNameArticleno :: " + fileNameByWorkNo
								+ "||" + fileNameByArticle);
					}

				} else {
					failedCount++;
					appendRowLoadStatus(prodWorkNo, fileNameByWorkNo + "||" + fileNameByArticle, "FAILED",
							"Image update failed.");
					logger.debug("Product Work :" + prodWorkNo + ", !! fileNameArticleno :: " + fileNameByWorkNo + "||"
							+ fileNameByArticle);

				}

			} else {
				if ("SUCCESS".equals(uploadFileWithWork)) {
					successCount++;
					appendRowLoadStatus(prodWorkNo, fileNameByWorkNo, "SUCCESS", "Image updated successfully.");
					logger.debug("Product Work :" + prodWorkNo + ", !! fileNameProduct Work :: " + fileNameByWorkNo);

				} else {
					failedCount++;
					appendRowLoadStatus(prodWorkNo, fileNameByWorkNo, "FAILED", "Image update failed.");
					logger.debug("Product Work :" + prodWorkNo + ", !! fileNameProduct Work :: " + fileNameByWorkNo);

				}
			}

		} else {
			boolean fileRemoved = false;
			if (FormatHelper.hasContent(skuArticle)) {
				fileRemoved = AgronSFTPServerConfig.removeFileContent(channel, fileNameByArticle);
			}
			fileRemoved = AgronSFTPServerConfig.removeFileContent(channel, fileNameByWorkNo);
			logger.info("File removed fronj sfTP server");
		}
	}

	/**
	 * setLogFile - This Method sets Log File Name
	 * 
	 * @throws WTException
	 * @throws IOException
	 */
	public static String setLogFile(String fromDate, String toDate) throws WTException {
		Date fdate = null;
		Date tdate = null;
		try {
			if (!"".equals(fromDate) && !"".equals(toDate) && fromDate != null && toDate != null) {
				SimpleDateFormat sdfSource = new SimpleDateFormat("MM-dd-yyyy:hh:mm:ss a");
				fdate = sdfSource.parse(fromDate);
				tdate = sdfSource.parse(toDate);
				SimpleDateFormat sdfDestination = new SimpleDateFormat("MMddyyyyhhmmss");

				dataLogFileName = logFileLocation + sdfDestination.format(fdate) + "_TO_" + sdfDestination.format(tdate)
						+ ".csv";
			} else {
				dataLogFileName = logFileLocation + "FR_initialload.csv";
			}
			logger.debug("!!Data Log File!! " + dataLogFileName);
			dataLogWriter = new BufferedWriter(new FileWriter(dataLogFileName));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new WTException("Error in Setting Log file - " + e.getLocalizedMessage());
		}
		return dataLogFileName;
	}

	/*
	 * * getFormattedLogEntryRow - Helper to write line into Log file
	 * 
	 * @param logLine
	 * 
	 * @return
	 * 
	 * @throws WTException
	 */
	public static StringBuffer getFormattedLogEntryRow(ArrayList row) throws WTException {

		logger.debug("getFormattedLogEntryRow() log row - " + row);
		StringBuffer logEntryLine = new StringBuffer();
		Iterator rowItr = row.iterator();
		String cellValue;
		while (rowItr.hasNext()) {
			cellValue = (String) rowItr.next();
			logger.debug("getFormattedLogEntryRow() cellValue - " + cellValue);
			logEntryLine.append(cellValue);
			logEntryLine.append(COMMA_DELIMITER);
		}
		logger.debug("getFormattedLogEntryRow() logEntryLine - " + logEntryLine);
		return logEntryLine;
	}

	/**
	 * Method used to write log rows based on status of rows whether success or
	 * failed appendRowLoadStatus
	 * 
	 * @param logEntryLine
	 * @param status
	 * @param printMsg
	 */
	public static void appendRowLoadStatus(StringBuffer logEntryLine, String uplodedFile, String status,
			String printMsg) {
		logEntryLine.append(COMMA_DELIMITER);
		logEntryLine.append(status);
		logEntryLine.append(COMMA_DELIMITER);
		logEntryLine.append("(" + uplodedFile + ")");
		logEntryLine.append(COMMA_DELIMITER);
		logEntryLine.append(printMsg);
		writeToDataLog(logEntryLine.toString());
	}

	/**
	 * writeToDataLog - This Method writes to log file
	 * 
	 * @param debug
	 */
	public static void writeToDataLog(String debug) {
		try {
			logger.debug("!! dataLogWriter!!!!" + dataLogWriter);
			dataLogWriter.write(debug);
			dataLogWriter.newLine();
			dataLogWriter.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.error(ioe.getLocalizedMessage());
			System.out.println(ioe.getLocalizedMessage());
		}
	}

	/**
	 * This Method will check image is modified in between last run time and current
	 * time
	 * 
	 * @param file
	 * @param dateStart
	 * @param dateEnd
	 * @return
	 */
	public static boolean checkFileModifiedDate(File file, String dateStart, String dateEnd) {
		boolean isModified = true;
		try {
			logger.debug("!!! checkFileModifiedDate !!!!");
			if (!"".equals(dateStart) && !"".equals(dateEnd) && dateStart != null && dateEnd != null) {
				Date fileDate = null;
				fileDate = new Date(file.lastModified());

				SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy:hh:mm:ss a", Locale.ENGLISH);
				formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

				Date stDate = formatter.parse(dateStart);
				Date edDate = formatter.parse(dateEnd);

				if (fileDate != null && stDate != null && edDate != null) {
					if (fileDate.after(stDate) && fileDate.before(edDate)) {

						isModified = true;
					} else {
						isModified = false;
					}
				}
			} else {
				logger.debug(
						"!!! Error In  checkFileModifiedDate !!!!startDate" + dateStart + ", EndDate !!" + dateEnd);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			isModified = false;
		}
		logger.debug("***File Modified***" + isModified);
		return isModified;
	}

	/**
	 * This method is use for NC interval which run before time
	 * 
	 * @param Date
	 * @param minutes
	 * @return
	 */
	public static String subtractTimeFromDate(String Date, int minutes) {
		String strDateFormat = null;
		strDateFormat = Date;
		Date date = null;
		try {
			logger.debug("***subtractTimeFromDate***");
			SimpleDateFormat sdfSource = new SimpleDateFormat("MM-dd-yyyy:hh:mm:ss a");
			date = sdfSource.parse(strDateFormat);
			Date currentDate = new Date(date.getTime() - minutes * 60 * 1000);
			System.out.println(currentDate);
			strDateFormat = sdfSource.format(currentDate);

		} catch (ParseException e) {
			logger.debug("!!*subtractTimeFromDate*Exception*");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return strDateFormat;
	}
}
