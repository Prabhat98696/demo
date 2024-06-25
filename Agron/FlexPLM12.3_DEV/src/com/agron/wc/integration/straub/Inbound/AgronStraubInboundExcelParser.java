package com.agron.wc.integration.straub.Inbound;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronStraubInboundExcelParser {


	private static final Logger log = LogManager.getLogger("com.agron.wc.integration.straub.inbound");

	WTUser user = null;
	String dataFileName = null;
	String loadType = null;

	/**
	 * setVariables - This Method initialize the user, dataFileName and loadType
	 * 
	 * @param user
	 * @param dataFileName
	 * @param loadType
	 */
	public void setVariables(WTUser userObj, String dataFile, String dataLoadType) {
		log.info("Inside setVariables Method");
		this.user = userObj;
		this.dataFileName = dataFile;
		this.loadType = dataLoadType;
		log.debug("userID: " + this.user.getName());
		log.debug("dataFileName: " + this.dataFileName);
		log.debug("loadType: " + this.loadType);
		}

	/**
	 * parseAndLoad - This Method is use for parsing CSV file 
	 * extracts values from every sheet
	 * 
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	
	public String parseAndLoad(boolean blankValue) throws WTException, WTPropertyVetoException {
		log.debug("parse() fileName -blankValue " + blankValue);
		String message=null;
		BufferedReader br;
		String line = "";  
		String splitBy = ",";

		try {
			log.debug("parse() fileName - with csv  " + dataFileName);
			//BufferedReader br = new BufferedReader(new FileReader("E:\\Agron Upgrade\\StrubCSvFile\\Agron_Export_Report_2021-02-05.csv"));  
			br = new BufferedReader(new FileReader(dataFileName));  
			ArrayList keyList = new ArrayList();
			ArrayList excelSheet = new ArrayList();
			int rowNum = 0;

			while ((line = br.readLine()) != null)   //returns a Boolean value  
			{ 
				ArrayList excelRowData = new ArrayList();

				log.debug("line - " + line);
				
				String[] row = line.split(splitBy);    // use comma as separator  
				StringBuffer StringBufferTokan=new StringBuffer();  
				boolean rowHasContent = false;

				for (String cellValue : row) {
					cellValue=cellValue.replace("\"", "");
					log.debug("cellValue chages - " + cellValue);

					String valueTrim=cellValue;
					if(!"".equals(valueTrim) && valueTrim!=null){
						StringBufferTokan.append(valueTrim.trim());

					}
					StringBufferTokan.append("||");
					if (FormatHelper.hasContent(cellValue)) {
						excelRowData.add(cellValue);
						rowHasContent = true;
					} else {
						log.debug("cellValue - else " +cellValue);
						if("0".equals(cellValue))
						{
							excelRowData.add(cellValue);
						}
						else{
							excelRowData.add(" ");
						}
					}
					//rowNum++;
					//excelRowData.add(cellValue);
				}
				log.debug("excelRowData - " + excelRowData);
				log.debug("StringBufferTokan - " + StringBufferTokan);
				log.debug("rowNum - " + rowNum);
				log.debug("loadType - " + loadType);
				if (FormatHelper.hasContent(StringBufferTokan.toString()))
				StringBufferTokan.setLength(StringBufferTokan.length() - 2);
				if(!(LCSProperties.get(loadType).equalsIgnoreCase(StringBufferTokan.toString()))&&(rowNum == 0)){
					message =LCSProperties.get("com.agron.wc.load.invalidFormatMessage");
					return message;
				}
				if (!rowHasContent)
					continue;
				log.debug("excelRowData size - " + excelRowData.size());
				if (rowNum == 0) { // Create the Key Row
					keyList.addAll(excelRowData);
					excelRowData.clear();
				} else {
					excelSheet.add(excelRowData);
				}
				log.debug("rowNum before - " + rowNum);
				rowNum++;
				log.debug("rowNum After - " + rowNum);

			}
			br.close();
			
			if (!FormatHelper.hasContent(loadType)) {
				log.debug("The value for loadType is null, So Returning ");
				message = "The value for loadType is null, So Returning";
				return message;
			}
			
			AgronStraubInputDataLoader dataLoader = new AgronStraubInputDataLoader(user, dataFileName, loadType);
			try {
				dataLoader.loadData(keyList, excelSheet, blankValue);
				message =LCSProperties.get("com.agron.wc.integration.straub.Inbound.upLoadMessage");
			}catch (WTException | WTPropertyVetoException wt) {
				wt.printStackTrace();
				throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new WTException("Error in parseAndLoad() - " + e.getLocalizedMessage());
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new WTException("Error in parseAndLoad() - " + e.getLocalizedMessage());
		}

		return message; 
	}
	
	
	/**
	 * parseAndLoad - Using XSSF in POI library, the method parses Excel file, and
	 * extracts values from every sheet
	 * 
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	/*public String parseAndLoad1(boolean blankValue) throws WTException, WTPropertyVetoException {
		log.debug("parse() fileName -blankValue " + blankValue);
		String message=null;
		XSSFWorkbook wb = null;
		try {
			log.debug("parse() fileName - " + dataFileName);
			FileInputStream myInput = new FileInputStream(dataFileName);
			wb = new XSSFWorkbook(myInput);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WTException("Error in parseAndLoad() - " + e.getLocalizedMessage());
		}
		ArrayList excelSheet = null;
		ArrayList excelRowData = null;
		// loop for every worksheet in the workbook
		int numOfSheets = wb.getNumberOfSheets();
		ArrayList keyList = null;
		for (int i = 0; i < numOfSheets; i++) {
			XSSFSheet sheet = wb.getSheetAt(i);
			excelSheet = new ArrayList();
			int firstRow = sheet.getFirstRowNum();
			int lastRow = sheet.getLastRowNum();
			keyList = new ArrayList();
			int rowNum = 0;
			// loop for every row in each worksheet
			for (Iterator rows = sheet.rowIterator(); rows.hasNext();) {
				StringBuffer StringBufferTokan=new StringBuffer();  
				XSSFRow row = (XSSFRow) rows.next();
				// First Row in Excel Contains the Class Name to be invoked
				// Second Row contains the Keys
				short c1 = row.getFirstCellNum();
				short c2 = row.getLastCellNum();
				excelRowData = new ArrayList();
				// loop for every cell in each row
				boolean rowHasContent = false;
				for (short c = c1; c < c2; c++) {
					XSSFCell cell = row.getCell(c);
					String cellValue = getCellValue(cell);
					log.debug("cellValue - " + cellValue);
					String valueTrim=cellValue;
					if(!"".equals(valueTrim) && valueTrim!=null){
						StringBufferTokan.append(valueTrim.trim());

					}
					StringBufferTokan.append("||");
					if (FormatHelper.hasContent(cellValue)) {
						log.debug("cellValue - " +cellValue);
						excelRowData.add(cellValue);
						rowHasContent = true;
					} else {
						log.debug("cellValue - else " +cellValue);
						if("0".equals(cellValue))
						{
							excelRowData.add(cellValue);
						}
						else{
							excelRowData.add(" ");
						}
					}
					log.debug("excelRowData - " + excelRowData);

				} // for row cells loop

				StringBufferTokan.setLength(StringBufferTokan.length() - 2);
				if(!(LCSProperties.get(loadType).equalsIgnoreCase(StringBufferTokan.toString()))&&(rowNum == 0)){
					message =LCSProperties.get("com.agron.wc.integration.straub.Inbound.invalidFormatMessage");
					return message;
				}

				if (!rowHasContent)
					continue;
				log.debug("excelRowData size - " + excelRowData.size());
				if (rowNum == 0) { // Create the Key Row
					keyList.addAll(excelRowData);
					excelRowData.clear();
				} else {
					excelSheet.add(excelRowData);
				}
				log.debug("rowNum before - " + rowNum);
				rowNum++;
				log.debug("rowNum After - " + rowNum);
				log.debug("Keylist - " + keyList);
				log.debug("excelSheet - " + excelSheet);
			} // for rows loop
			// Returning if Load Type is null
			if (!FormatHelper.hasContent(loadType)) {
				log.debug("The value for loadType is null, So Returning ");
				message = "The value for loadType is null, So Returning";
				return message;
			}

		} // for sheets loop

		log.debug("loadType - " + loadType);
		log.debug("excelSheet - " + excelSheet);
		log.debug("condition - " + "ClearanceSellPrice".equalsIgnoreCase(loadType));

		AgronStraubInputDataLoader dataLoader = new AgronStraubInputDataLoader(user, dataFileName, loadType);
		try {
			dataLoader.loadData(keyList, excelSheet, blankValue);
			message =LCSProperties.get("com.agron.wc.integration.straub.Inbound.upLoadMessage");
		} catch (WTException | WTPropertyVetoException wt) {
			wt.printStackTrace();
			throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
		}


		return message; 
	}*/

	/**
	 * getCellValue - Method to retrieve the value of a cell regardles of its type,
	 * which will be converted into a String.
	 * 
	 * @param cell
	 * @return
	 */
	private String getCellValue(XSSFCell cell) {
		if (cell == null)
			return null;

		String result = null;
		CellType cellType = cell.getCellType(); // cell type

		switch (cellType) {
		case BLANK:
			result = "";
			break;

		case BOOLEAN:
			result = cell.getBooleanCellValue() ? "true" : "false";
			break;

		case ERROR:
			result = "ERROR: " + cell.getErrorCellValue();
			break;

		case FORMULA:
			result = cell.getCellFormula();
			break;

		case NUMERIC:
			XSSFCellStyle cellStyle = cell.getCellStyle();
			short dataFormat = cellStyle.getDataFormat();
			log.debug("dataFormat - " + dataFormat);
			// valid excel date formats
			if (dataFormat == 14 || dataFormat == 15 || dataFormat == 16 || dataFormat == 17 || dataFormat == 18
					|| dataFormat == 19 || dataFormat == 20 || dataFormat == 21 || dataFormat == 22 || dataFormat == 164
					|| dataFormat == 168 || dataFormat == 165 || dataFormat == 169 || dataFormat == 170
					|| dataFormat == 171 || dataFormat == 172 || dataFormat == 173 || dataFormat == 174
					|| dataFormat == 175 || dataFormat == 176 || dataFormat == 177 || dataFormat == 178
					|| dataFormat == 179 || dataFormat == 180 || dataFormat == 166 || dataFormat == 181
					|| dataFormat == 182 || dataFormat == 166) {
				log.debug("cell.getDateCellValue() - " + cell.getDateCellValue());
				// result = cell.getDateCellValue().toString();
				SimpleDateFormat folderDateFormat;
				folderDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	

				result = folderDateFormat.format(cell.getDateCellValue());
				log.debug("Its a date cell - " + result);
			} else { // not date
				
				int afterDecimal=Integer.parseInt(String.valueOf(cell.getNumericCellValue()).substring(String.valueOf(cell.getNumericCellValue()).indexOf('.')+1, String.valueOf(cell.getNumericCellValue()).length()));  
				if(afterDecimal>0)
				{
					 result = String.valueOf(cell.getNumericCellValue()).substring(0, String.valueOf(cell.getNumericCellValue()).length());
				 }else{
					 result = String.valueOf(cell.getNumericCellValue()).substring(0, String.valueOf(cell.getNumericCellValue()).indexOf('.'));
				 }
				//
				//result = String.valueOf(cell.getNumericCellValue()).substring(0, String.valueOf(cell.getNumericCellValue()).length());
				log.debug("Its a numeric cell Changes- " + result);
			}
			break;

		case STRING:
			result = cell.getStringCellValue(); // string cell
			log.debug("Its a String cell - " + result);
			break;

		default:
			break;
		}

		return result;
	}



}
