package com.agron.wc.integration.batch;

import com.agron.wc.integration.outbound.AgronDataFeed;
import com.agron.wc.integration.outbound.AgronExportQuery;
import com.agron.wc.integration.util.*;
import com.agron.wc.util.AgronSendMailUtil;
import com.lcs.wc.client.ClientContext;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import java.io.File;
import java.io.IOException;
import java.text.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.util.*;

/**
 * @author Vinod D
 * @version 1.0
 * 
 * @author Mallikarjuna Savi
 * @version 2.0
 */
public class AgronBatchProcessor {

	private static final String COLUMN_LIST = LCSProperties.get("com.agron.wc.inteface.csvColumns");
	private static final String COLUMN_SEASONPRICE_LIST = LCSProperties
			.get("com.agron.wc.inteface.seasonprice.csvColumns");
	private static final String FILECOPY_LOCATION = LCSProperties.get("com.agron.wc.interface.fileLocation");
		//private static final String EMAIL_HEADER = "Agron - ERP Integration Status";
	
	private static final String EMAIL_HEADER = LCSProperties.get("com.agron.wc.integration.EmailHeader");
	private static final String MAIL_HEADER = LCSProperties.get("com.agron.wc.integration.MailHeader");
	private static final String SYSTEM_INFO = LCSProperties.get("com.agron.wc.integration.Mail.SystemInfo");
	private static final String DECLAIMER = LCSProperties.get("com.agron.wc.integration.Mail.Disclaimer");
	private static final boolean PRINTHEADER_CSV = LCSProperties
			.getBoolean("com.agron.wc.integration.printHeaderOnCSVFile");
	private static final boolean ADDEMPTY_ROW = LCSProperties.getBoolean("com.agron.wc.interface.addEmptyRow", true);
	private static final String FILECOPY_ERROR = LCSProperties.get("com.agron.wc.integration.mail.errorMessage",
			"Please remove existing file in the location, and run the interface again.");
	private static final String ERROR_DASH = "--------------------------------------------------------------------------";
	private static final String MAIL_TO_IDS = LCSProperties.get("com.agron.wc.integration.ToMailIds");
	private static final String MAIL_FROM_ID = LCSProperties.get("com.agron.wc.integration.FromMailId");
	public static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");
	private static String log4jPropertyFile;
	private static String wtHome;
	private static String propertyConfigPath = "";
	private static String dash = "--------------------------------";
	private static StringBuffer propertyPathBuff;
	private static final String FLEX_TIME_FORMAT = "MM-dd-yyyy:hh:mm:ss a";
	
	AgronBatchProcessor() {
	}


		
	

	/**
	 * init(): This method is invoked to set the propertyConfigPath during
	 * interface.
	 * 
	 * @return String @param @throws
	 */

	/*public void init() {
		log4jPropertyFile = "";
		wtHome = "";
		propertyPathBuff = new StringBuffer();
		try {
			log4jPropertyFile = LCSProperties.get("com.agron.wc.interface.log4j.File");
			wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
			propertyPathBuff.append(wtHome);
			propertyPathBuff.append(File.separator);
			propertyPathBuff.append("codebase");
			propertyPathBuff.append(File.separator);
			propertyPathBuff.append(log4jPropertyFile);
			propertyConfigPath = propertyPathBuff.toString();
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		
		PropertyConfigurator.configure(propertyConfigPath);
		LOGGER.info("Configuration of log4j complete.");
		
		
	}

	/**
	 * This method is used to run the batch process
	 */

	/**
	 * @throws ParseException
	 */
	public void execute() throws ParseException {

		Set productObjColSet = new HashSet();
		String errorReportExcelURL = null;
		boolean isErrorOccure = false;
		int errorCount = 0;
		int successCount = 0;
		int totalCount = 0;
		StringBuffer buffer = null;

		try {
			ClientContext context = ClientContext.getContext();
			AgronExportQuery exportQuery = new AgronExportQuery();
			AgronDataFeed dataFeed = new AgronDataFeed(isErrorOccure);
			AgronCSVGenerator csvGenerator = new AgronCSVGenerator();
			AgronCSVGenerator agroncsvgenerator1 = new AgronCSVGenerator();
			AgronDataProcessor dataProcessor = new AgronDataProcessor();
			AgronSendMailUtil mailUtil = new AgronSendMailUtil();

			String mailToSubject = LCSProperties.get("com.dl.wc.integration.mailTo.Subject",
					EMAIL_HEADER);
			//init();
			String currentDateTime = getCurrentGMTTime();
			String lastRunTime = (new AgronIntegrationUtil()).getLastSuccessfulRun();
			LOGGER.info(dash);
			LOGGER.info(" Starting Interface Process  ");
			LOGGER.info((new StringBuilder()).append(" Start Time : ").append(currentDateTime).toString());
			LOGGER.info(dash);
			LOGGER.info((new StringBuilder()).append(" Last Run Time : ").append(lastRunTime).toString());
			LOGGER.info(dash);
			LOGGER.info(" **** Starting Outbound Process ****  ");
			Collection<?> productObjectCol = exportQuery.getObjects(currentDateTime, null, "PRODAREV");
			productObjColSet.addAll(productObjectCol);
			Collection<?> seasonProductLinkObjectCol = exportQuery.getObjects(currentDateTime, null,
					"LCSSEASONPRODUCTLINK");
			productObjColSet.addAll(seasonProductLinkObjectCol);
			Collection<?> colorwayObjectCol = exportQuery.getObjects(currentDateTime, null, "SKUAREV");
			productObjColSet.addAll(colorwayObjectCol);
			Collection<?> moaObjCol = exportQuery.getObjects(currentDateTime, null, "LCSMOAOBJECT");
			productObjColSet.addAll(moaObjCol);
			Collection<?> sourcingCol = exportQuery.getObjects(currentDateTime, null, "LCSSOURCINGCONFIG");
			productObjColSet.addAll(sourcingCol);
			productObjectCol = dataFeed.addRelevantData(productObjColSet, "LCSPRODUCT");
			//LOGGER.info(" ****productObjectCol **11111111111**  "+productObjectCol);
			productObjectCol = overrideAdidasArticleValueFromMOA(productObjectCol);
			Collection<?> productObjectSeaReport = dataFeed.addRelevantSeasonPriceData(productObjColSet, "LCSPRODUCT");
			if (productObjectCol != null && productObjectCol.size() > 0) {
				totalCount = productObjectCol.size();
				productObjectCol = dataProcessor.getErrorReportExcel(productObjectCol);
				errorCount = dataProcessor.getErrorRecordsCount();
				successCount = productObjectCol.size();
				errorReportExcelURL = dataProcessor.getExcelGeneratorURL();
				LOGGER.info(
						(new StringBuilder()).append("errorReportExcelURL : ").append(errorReportExcelURL).toString());
			}
			/* Remove Record from Season Import if FOB zero */
			
			if (productObjectSeaReport != null && productObjectSeaReport.size() > 0) {
				
				productObjectSeaReport = dataProcessor.removeZeroFOBRecords(productObjectSeaReport);
				
			}
			isErrorOccure = dataFeed.getErrorStatus();
			LOGGER.info((new StringBuilder()).append("isErrorOccure : ").append(isErrorOccure).toString());
			if (!isErrorOccure) {
				Collection<?> columns = (new AgronDataProcessor()).getColumns(COLUMN_LIST);
				String csvFileURL = csvGenerator.drawTable(productObjectCol, columns, context, "FlexImport",
						PRINTHEADER_CSV, ADDEMPTY_ROW);

				boolean fileCopyStatus = AgronDataProcessor.copyFile(csvFileURL, FILECOPY_LOCATION);
				Collection<?> columnsSR = (new AgronDataProcessor()).getColumns(COLUMN_SEASONPRICE_LIST);
				String csvFileURLSR = agroncsvgenerator1.drawTable(productObjectSeaReport, columnsSR, context,
						"FlexImport_SeasonPrice", PRINTHEADER_CSV, ADDEMPTY_ROW);

				boolean fileCopyStatusSR = AgronDataProcessor.copyFile(csvFileURLSR, FILECOPY_LOCATION);
				if (fileCopyStatus && fileCopyStatusSR) {
					(new AgronIntegrationUtil()).createLogEntry("", currentDateTime, "ExportData", "true");
					buffer = new StringBuffer();
					buffer.append("File Created Successfully...");
					buffer.append("\n");
					buffer.append(ERROR_DASH);
					buffer.append("\n");
					buffer.append("Total records processed : ");
					buffer.append(totalCount);
					buffer.append("\n");
					buffer.append((new StringBuilder()).append("Number of successful records : ").append(successCount)
							.append("   (refer the generated CSV file for results)").toString());
					buffer.append("\n");
					buffer.append((new StringBuilder()).append("Number of error records : ").append(errorCount)
							.append("   (refer attached document for failed records and reasons)").toString());
					buffer.append("\n");
					buffer.append(ERROR_DASH);
					buffer.append("\n");
					mailUtil.sendEmail(errorReportExcelURL, MAIL_FROM_ID, MAIL_TO_IDS, mailToSubject, buffer.toString(),
							false, MAIL_HEADER, SYSTEM_INFO, DECLAIMER, EMAIL_HEADER);
				} else {
					buffer = new StringBuffer();
					buffer.append(ERROR_DASH);
					buffer.append("\n");
					buffer.append((new StringBuilder()).append("File transfer to ").append(FILECOPY_LOCATION)
							.append(" location Failed.").toString());
					buffer.append("\n");
					buffer.append(FILECOPY_ERROR);
					buffer.append("\n");
					buffer.append(ERROR_DASH);
					mailUtil.sendEmail("", MAIL_FROM_ID, MAIL_TO_IDS, mailToSubject, buffer.toString(), false,
							MAIL_HEADER, SYSTEM_INFO, DECLAIMER, EMAIL_HEADER);
				}
			} else {
				mailUtil.sendEmail("", MAIL_FROM_ID, MAIL_TO_IDS, "", "", false, MAIL_HEADER, SYSTEM_INFO, DECLAIMER,
						EMAIL_HEADER);
			}
			LOGGER.info(" **** End Outbound Process ****  ");
		} catch (WTException wtexception) {
			LOGGER.error("Error : " + wtexception.fillInStackTrace());
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			LOGGER.error("Error : " + wtpropertyvetoexception.fillInStackTrace());
		} catch (ParseException parseexception) {
			LOGGER.error("Error : " + parseexception.fillInStackTrace());
		}
	}

	/**
	 * Override some adidas Article value from MOA
	 * @param objects
	 * @return
	 */
	public static Collection<?> overrideAdidasArticleValueFromMOA(Collection<?> objects ){
	LOGGER.info(" ***Method inside overrideAdidasArticleValueFromMOA** STRT ");
		Iterator<?> objectsIter = objects.iterator();
		 HashMap<String,String> adidasArticleMoaRowMap=AgronCSVGenerator.getMapOfAdidasArticleUPC();
		 while (objectsIter.hasNext()) {
			FlexObject objectsItermoa = (FlexObject) objectsIter.next();
			String upcValue = objectsItermoa.getString("ERP.UPC");
			if(FormatHelper.hasContent(upcValue)){
				if(adidasArticleMoaRowMap.containsKey(upcValue)){
					String adidasArticleVal = adidasArticleMoaRowMap.get(upcValue);
					objectsItermoa.put("ERP.ADIDASARTICLE", adidasArticleVal);
				}
			}
		}
		LOGGER.info(" ***Method inside overrideAdidasArticleValueFromMOA** END ");
		return objects;
	}
	
	public static String getCurrentGMTTime() {
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		SimpleDateFormat simpledateformat = new SimpleDateFormat(FLEX_TIME_FORMAT);
		simpledateformat.setTimeZone(timezone);
		Date date = null;
		String s = null;
		date = new Date();
		s = simpledateformat.format(date);
		return s;
	}

}
