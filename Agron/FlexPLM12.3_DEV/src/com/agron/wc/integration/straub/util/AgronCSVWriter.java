package com.agron.wc.integration.straub.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.util.WTException;
import wt.util.WTProperties;

public class AgronCSVWriter {
	
	
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	public static String csvFileLocation = null;
	private static final Logger logger = LogManager.getLogger("com.agron.wc.integration.straub.outbound");
	public static String COMMA_DELIMITER = LCSProperties.get("com.agron.wc.integration.straub.outbound.commaDelimiter",",");
	private static String EXPORT_DATAFILE = LCSProperties.get("com.agron.wc.integration.straub.util.exportFileName", "ExportDataFile");
	private static final String DATE_FORMATE = "MMddyyyyhhmmss";
	public static String CSV_ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.integration.straub.outbound.agronCSVHeaderRenameAttribute");
	static Map<String, String> keyValuePairForCSVHeader = new HashMap<String, String>();
	static List<String> excelCSVHeaderRenameAttributes = Arrays.asList(CSV_ATTRIBUTE_LIST.split(","));
	
	static{
		try{
			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			csvFileLocation = wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.integration.straub.util.logFileLocation"));
			new File(csvFileLocation).mkdirs();
			getCSVHeaderAttKeyValue();
		}catch(IOException io){
			io.printStackTrace();
		}
	}
	
	/**
	 * createCSVFileName
	 * @return
	 */
	
	public static File createCSVFileName(){
		logger.info("Method createCSVFileName START->>");
		File file = null ;
		Date date= new Date();
		try{
			SimpleDateFormat sdfDestination = new SimpleDateFormat(DATE_FORMATE);
			file =new File(csvFileLocation + EXPORT_DATAFILE+"_"+sdfDestination.format(date)+"_GMT.csv");
			if(!file.exists()) {
				file.createNewFile();	
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		logger.info("Method createCSVFileName END->>");
		return file;
	}

	
	/**
	 * 
	 * @param fileName
	 * @return CSVDataLogWriter
	 * @throws WTException
	 */
	public static BufferedWriter getCSVDataWriter(File fileName) throws WTException {
		logger.info("Method getCSVDataWriter START->>");
		BufferedWriter dataLogWriter = null;
		try {
			dataLogWriter = new BufferedWriter(new FileWriter(fileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new WTException("Error in Setting Log file - "+e.getLocalizedMessage());
		}
		logger.info("Method getCSVDataWriter END->>");
		return dataLogWriter;
	}
	
	/**
	 * @param listOfRowMap
	 * @param excelLayoutAttributes
	 * @param keyValuePair
	 */
	public static void csvWriter(List<Map<String, Object>> listOfRowMap,  List<String> excelLayoutAttributes, Map<String, String> keyValuePair, BufferedWriter dataLogWriter){
		logger.info("Method csvWriter START->>>>"+listOfRowMap.size());
		logger.debug("Method csvWriter listOfRowMap->>>>"+listOfRowMap);
		logger.debug("Method csvWriter keyValuePair->>>>"+keyValuePair);
		if(listOfRowMap.size()>0){
			StringBuffer rowEntry= null;
			rowEntry= new StringBuffer();
			for (String column : excelLayoutAttributes) {
				String columnVal = "";
				if(keyValuePair.containsKey(column)){

					columnVal=(String) keyValuePairForCSVHeader.get(column);
					if(!FormatHelper.hasContent(columnVal)){
						columnVal= (String) keyValuePair.get(column);
					}
				}
				rowEntry.append(columnVal);
				rowEntry.append(COMMA_DELIMITER);
			}
			writeRowDataToCSV(rowEntry.toString().trim(), dataLogWriter);

			for (Map<String, Object> row : listOfRowMap) {
				try{
						rowEntry= new StringBuffer();
						for (String column : excelLayoutAttributes) {
						String columnVal = "";
						if(row.containsKey(column)){
							columnVal= String.valueOf(row.get(column));
						}
						rowEntry.append(columnVal);
						rowEntry.append(COMMA_DELIMITER);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				writeRowDataToCSV(rowEntry.toString().trim(), dataLogWriter);
			}

		}
		logger.info("Method csvWriter END->>>>");
	}
	
	/**
	 * writeToDataLog - This Method writes to log file
	 * @param debug
	 */
	public static void writeRowDataToCSV(String rowData, BufferedWriter dataLogWriter) {
		try {
			if(rowData != null && rowData.endsWith(COMMA_DELIMITER)){
				rowData = rowData.substring(0, rowData.length()-1);
			}
			dataLogWriter.write(rowData);
			dataLogWriter.newLine();
			dataLogWriter.flush();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			logger.info(ioe.getLocalizedMessage());
			logger.debug(ioe.getLocalizedMessage());
		}
	}
	
	public static void getCSVHeaderAttKeyValue (){

		for (int i = 0; i < excelCSVHeaderRenameAttributes.size(); i++)  {

			String attribute = excelCSVHeaderRenameAttributes.get(i);
			String attKey = attribute.split("~")[0];
			String attValue = attribute.split("~")[1];
			keyValuePairForCSVHeader.put(attKey, attValue);
		}
	}

}
