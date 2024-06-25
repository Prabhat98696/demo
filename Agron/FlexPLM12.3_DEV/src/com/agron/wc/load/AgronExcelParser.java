package com.agron.wc.load;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class AgronExcelParser {

	public static final Logger log = LogManager.getLogger(AgronExcelParser.class);

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
	 * parseAndLoad - Using XSSF in POI library, the method parses Excel file, and
	 * extracts values from every sheet
	 * 
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public String parseAndLoad(boolean blankValue) throws WTException, WTPropertyVetoException {
		log.debug("parse() fileName -blankValue " + blankValue);
		String message=null;
		String season ="";
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
		//int numOfSheets = wb.getNumberOfSheets();
		ArrayList keyList = null;
		//for (int i = 0; i < numOfSheets; i++) {
			XSSFSheet sheet = wb.getSheetAt(0);
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
				log.debug("LCSProperties.get(loadType)  ::" + LCSProperties.get(loadType) +"::");
				log.debug("StringBufferTokan.toString() ::" + StringBufferTokan.toString() + "::");
				if("AgronFOBLoader".equalsIgnoreCase(loadType)){
					if(rowNum == 0){
						season = StringBufferTokan.toString();
					}
					else if(!(LCSProperties.get(loadType).equalsIgnoreCase(StringBufferTokan.toString()))&&(rowNum == 1)){
						message =LCSProperties.get("com.agron.wc.load.invalidFormatMessage");
						return message;
					}
				}
				else{
				if(!(LCSProperties.get(loadType).equalsIgnoreCase(StringBufferTokan.toString()))&&(rowNum == 0)){
					message =LCSProperties.get("com.agron.wc.load.invalidFormatMessage");
					return message;
				}
				}
				
				if (!rowHasContent)
					continue;
				log.debug("excelRowData size - " + excelRowData.size());
				if (rowNum == 0) { // Create the Key Row
					keyList.addAll(excelRowData);
					excelRowData.clear();
				} else if("AgronFOBLoader".equalsIgnoreCase(loadType) &&(rowNum == 1)){
					keyList.addAll(excelRowData);
					excelRowData.clear();
				}
				else{
					excelSheet.add(excelRowData);
				}
					
				log.debug("rowNum before - " + rowNum);
				rowNum++;
				log.debug("rowNum After - " + rowNum);
			} // for rows loop
				// Returning if Load Type is null
			if (!FormatHelper.hasContent(loadType)) {
				log.debug("The value for loadType is null, So Returning ");
				message = "The value for loadType is null, So Returning";
				return message;
			}
			
		//} // for sheets loop
		
			if ("SKUSeasonConsolidatedBuyDate".equalsIgnoreCase(loadType)) {
				AgronSKUSeasonConsolidatedBuyDateLoader dataLoader = new AgronSKUSeasonConsolidatedBuyDateLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			if ("PRODAdidasModel".equalsIgnoreCase(loadType)) {
				AgronAdidasModelDataLoader dataLoader = new AgronAdidasModelDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			if ("SKUAdidasArticle".equalsIgnoreCase(loadType)) {
				AgronAdidasArticleDataLoader dataLoader = new AgronAdidasArticleDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			if ("ProductDimension".equalsIgnoreCase(loadType)) {
				AgronProductDimensionDataLoader dataLoader = new AgronProductDimensionDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}
  
			} 
			
			if ("ForecastData".equalsIgnoreCase(loadType)) {
				AgronForecastUploadTemplate dataLoader = new AgronForecastUploadTemplate(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			
			if ("ClearanceSellPrice".equalsIgnoreCase(loadType)) {
				AgronClearanceSellPriceUploadTemplate dataLoader = new AgronClearanceSellPriceUploadTemplate(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			
			if ("StraubAttribute".equalsIgnoreCase(loadType)) {
				AgronStraubDataLoader dataLoader = new AgronStraubDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in StraubAttribute parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			if ("MinimumOrderQty".equalsIgnoreCase(loadType)) {
				AgronMinimumOrderQtyDataLoader dataLoader = new AgronMinimumOrderQtyDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in MinimumOrderQty parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			
			if ("DHLTracking".equalsIgnoreCase(loadType)) {
				AgronDHLTrackingUploader dataLoader = new AgronDHLTrackingUploader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in AgronDHLTrackingUploader parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			
			if ("SustainabilityAttribute".equalsIgnoreCase(loadType)) {
				AgronSustainabilityDataLoader dataLoader = new AgronSustainabilityDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in SustainabilityAttribute parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			
			if ("ColorwayStatusAttribute".equalsIgnoreCase(loadType)) {
				AgronColorwayStatusDataLoader dataLoader = new AgronColorwayStatusDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in Colorway Status parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			if ("ProductStatusAttribute".equalsIgnoreCase(loadType)) {
				AgronProductStatusDataLoader dataLoader = new AgronProductStatusDataLoader(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in Product Status parseAndLoad() - " + wt.getLocalizedMessage());
					}

			}
			if ("ReassignArticleNumbers".equalsIgnoreCase(loadType)) {
				AgronReassignArticleNumbers dataLoader = new AgronReassignArticleNumbers(user, dataFileName, loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}

			} 
			if("AgronFOBLoader".equalsIgnoreCase(loadType)) {
				AgronFOBLoader dataLoader = new AgronFOBLoader(user, dataFileName,  loadType);
					try {
						dataLoader.loadData(keyList, excelSheet, season, blankValue);
						message =LCSProperties.get("com.agron.wc.load.upLoadMessage");
					} catch (WTException | WTPropertyVetoException wt) {
						wt.printStackTrace();
						throw new WTException("Error in parseAndLoad() - " + wt.getLocalizedMessage());
					}

			} 
		return message; 
	}

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
				log.debug("Cell Numeric Value :" + cell.getNumericCellValue());
				double numericVal = cell.getNumericCellValue();
				if(numericVal>0) {
					DecimalFormat df = new DecimalFormat("#.##");      
					numericVal = Double.valueOf(df.format(numericVal));
					log.debug("Cell Numeric Value Rounded :" + cell.getNumericCellValue());
				}
				int afterDecimal=Integer.parseInt(String.valueOf(numericVal).substring(String.valueOf(numericVal).indexOf('.')+1, String.valueOf(numericVal).length()));  
				log.debug("AfterDecimal - " + afterDecimal);
				if(afterDecimal>0)
				{
					 result = String.valueOf(numericVal).substring(0, String.valueOf(numericVal).length());
					 log.debug("Result for afterDecimal > 0 :" + result);
				 }else{
					 result = String.valueOf(numericVal).substring(0, String.valueOf(numericVal).indexOf('.'));
					 log.debug("Result for afterDecimal :" + result);
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
