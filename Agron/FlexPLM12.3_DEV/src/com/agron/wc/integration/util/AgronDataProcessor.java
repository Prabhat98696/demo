package com.agron.wc.integration.util;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.ExcelGenerator;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import java.io.File;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import wt.util.WTException;

/**
 * @author Mallikarjuna Savi
 *
 */
public class AgronDataProcessor {
	/**
	 * Defined to store the constant value.
	 */
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.util.erp");
	private static final String ERROR_REPORT_TABLECOLUMN_ORDER = LCSProperties
			.get("com.agron.wc.interface.error.excelTableColumns");
	private static final String STYLENAME_LIMIT = LCSProperties.get("com.agron.wc.interface.product.styleNameLimit",
			"30");
	private String fileURL;
	private int errorCount;

	public AgronDataProcessor() {
		fileURL = "";
		errorCount = 0;
	}

	/**
	 * This method is used to get the error data collection.
	 * 
	 * @param dataCol is Collection object
	 * @return Collection type
	 */
	public Collection<FlexObject> getErrorReportExcel(Collection<?> dataCol) {

		ArrayList<FlexObject> finalDataCol = new ArrayList<FlexObject>();
		String attErrorMessage = null;

		try {
			ArrayList<FlexObject> errorCol = new ArrayList<FlexObject>();
			ArrayList messageErrorReport = new ArrayList();
			ArrayList messageErrorReport2 = new ArrayList();
			int i = Integer.parseInt(STYLENAME_LIMIT);
			Collection<TableColumn> columns = getColumns(ERROR_REPORT_TABLECOLUMN_ORDER);
			Map<String, String> itemCodeUPCMap = new HashMap<String, String>();
			for (Iterator<?> dataIter = dataCol.iterator(); dataIter.hasNext();) {
				FlexObject flexObj = (FlexObject) dataIter.next();
				StringBuffer stringbuffer = new StringBuffer();
			
				boolean errorFound = false;
				boolean skipRecord = false;
				String upcFromMap = null;
				String upcValue = null;
			
				if(flexObj.containsKey("ERP.ISCSEXIST") && ("TRUE".equalsIgnoreCase(flexObj.getData("ERP.isCSExist")))) {

					//if(!FormatHelper.hasContent(flexObj.getData("ERP.sizes"))){
					//	attErrorMessage = flexObj.getData("ERP.csName")+"- Please select a size";//LCSProperties.get("com.agron.wc.interface.errorMessage.styleName");
					//}else {
					//	attErrorMessage = flexObj.getData("ERP.csName")+"- Please fix the costsheet";//LCSProperties.get("com.agron.wc.interface.errorMessage.styleName");
						
					//}
					attErrorMessage = flexObj.getData("ERP.csName")+"- Please fix the costsheet";
					//LOGGER.info("*******attErrorMessage******"+attErrorMessage);
					stringbuffer.append(attErrorMessage).append("\n");
					if(!messageErrorReport.contains(attErrorMessage)){
						messageErrorReport.add(attErrorMessage);
						errorFound = true;
					}else{
						skipRecord = true;
					}
				}else {			
				if( ("TRUE".equalsIgnoreCase(flexObj.getData("ERP.active")) && "TRUE".equalsIgnoreCase(flexObj.getData("ERP.skuSTSactive"))) || (flexObj.getData("ERP.active")== null))
				{
				
				if (flexObj.getData("ERP.STYLENAME").length() > i) {
					attErrorMessage = LCSProperties.get("com.agron.wc.interface.errorMessage.styleName");
					stringbuffer.append(attErrorMessage).append("\n");
					errorFound = true;
				}
				if (!FormatHelper.hasContent(flexObj.getData("ERP.PRIMARYVENDORNUMBER"))) {
					attErrorMessage = "Please add the Primary Vendor to the product";
					stringbuffer.append(attErrorMessage).append("\n");
					errorFound = true;
				}
				
				
					else if (flexObj.containsKey("ERP.FOB") && !FormatHelper.hasContent(flexObj.getData("ERP.FOB"))
						|| "0".equals(flexObj.getData("ERP.FOB")) || "0.0".equals(flexObj.getData("ERP.FOB"))) {
					attErrorMessage = LCSProperties.get("com.agron.wc.interface.errorMessage.fob");
					stringbuffer.append(attErrorMessage).append("\n");
					errorFound = true;
				//}
				}
				}
				upcFromMap = itemCodeUPCMap.get(flexObj.getData("ERP.itemCode"));
				upcValue = flexObj.getData("ERP.upc");
				if(FormatHelper.hasContent(upcFromMap))
				{
					attErrorMessage = "Duplicate record or Cost Sheet exist with same article number";
					stringbuffer.append(attErrorMessage).append("\n");
					errorFound = true;
				}
				
				else if(FormatHelper.hasContent(upcValue) && itemCodeUPCMap.containsValue(upcValue))
				{
					attErrorMessage = "Duplicate record exist with same upc code";
					stringbuffer.append(attErrorMessage).append("\n");
					errorFound = true;
				}		
				
		}
				if (errorFound) {
					flexObj.put("ERP.errorDesc", stringbuffer.toString());
					errorCol.add(flexObj);
				} else if(skipRecord){
					//skip error report for all size 
					//LOGGER.info("****skipRecord*****"+flexObj);
				}else {
					if(flexObj.containsKey("ERP.ISCSEXIST") && ("TRUE".equalsIgnoreCase(flexObj.getData("ERP.isCSExist")))) {
						if(!FormatHelper.hasContent(flexObj.getData("ERP.sizes"))){
							attErrorMessage = flexObj.getData("ERP.csName")+"- Please select a size";//LCSProperties.get("com.agron.wc.interface.errorMessage.styleName");
						}else {
							attErrorMessage = flexObj.getData("ERP.csName")+"- Please fix the costsheet";//LCSProperties.get("com.agron.wc.interface.errorMessage.styleName");

						}	
						//LOGGER.info("*********attErrorMessage***2**"+attErrorMessage);
						stringbuffer.append(attErrorMessage).append("\n");
						if(!messageErrorReport2.contains(attErrorMessage)){
							messageErrorReport2.add(attErrorMessage);
							errorFound = true;
						}
						else{
							skipRecord = true;
						}
					}
					
					else if ( "TRUE".equalsIgnoreCase(flexObj.getData("ERP.active")) || (flexObj.getData("ERP.active")== null))
					{
					//Retail Price never be NUll or blank
					if(FormatHelper.hasContent(flexObj.getData("ERP.retail")) && FormatHelper.hasContent(flexObj.getData("ERP.CSRETAILPRICE")))
					{
						if(!(flexObj.getData("ERP.retail").equalsIgnoreCase(flexObj.getData("ERP.CSRETAILPRICE"))))
						{
							attErrorMessage = "Retail Price Mismatch between product and costsheet";
							stringbuffer.append(attErrorMessage).append("\n");
							errorFound = true;
							}
					}
					
						
					// Check the NRF Size is null, then pushed to error report.
					
					if((flexObj.getData("ERP.nrfSize")== null))
						{
							attErrorMessage = "NRF Size is not available in the NRF BO";
							stringbuffer.append(attErrorMessage).append("\n");
							errorFound = true;
						}
					
					if (errorFound) {
						flexObj.put("ERP.errorDesc", stringbuffer.toString());
						errorCol.add(flexObj);

					}else if(skipRecord){
						//skip error report for all size 
						//LOGGER.info("****skipRecord**2***"+flexObj);
					}
					}
					itemCodeUPCMap.put(flexObj.getData("ERP.itemCode"), flexObj.getData("ERP.upc"));
					finalDataCol.add(flexObj);
					
				}
			}

			if (errorCol.size() > 0) {
				errorCount = errorCol.size();
				fileURL = (new ExcelGenerator()).drawTable(errorCol, columns, ClientContext.getContext(),
						"Error Report");
			}
		} catch (WTException e) {
			LOGGER.error("ERROR : " + e.fillInStackTrace());
		}
		return finalDataCol;
	} 

	/**
	 * This method is used to get the error data collection.
	 * 
	 * @param dataCol is Collection object
	 * @return Collection type
	 */
	public Collection<FlexObject> removeZeroFOBRecords(Collection<?> dataCol) {

		ArrayList<FlexObject> finalDataCol = new ArrayList<FlexObject>();

			for (Iterator<?> dataIter = dataCol.iterator(); dataIter.hasNext();) {
				FlexObject flexObj = (FlexObject) dataIter.next();
				boolean errorFound = false;
				if ((flexObj.containsKey("ERP.FOB") && !FormatHelper.hasContent(flexObj.getData("ERP.FOB"))
						|| "0".equals(flexObj.getData("ERP.FOB")) || "0.0".equals(flexObj.getData("ERP.FOB"))) 
						|| (flexObj.containsKey("ERP.ISCSEXIST") && ("TRUE".equalsIgnoreCase(flexObj.getData("ERP.isCSExist"))))) 
					{
					
					errorFound = true;
				
				}
				else {
					finalDataCol.add(flexObj);
					
				}
			}

		return finalDataCol;
	}
	/**
	 * This method is used to get the error records count.
	 * 
	 * @return Integer type
	 */
	public int getErrorRecordsCount() {
		return this.errorCount;
	}

	/**
	 * This method is used to get the excel generator URL.
	 * 
	 * @return String type
	 */
	public String getExcelGeneratorURL() {
		return this.fileURL;
	}

	/**
	 * Description API - moveFile(String srFile, String dtFile). This method is used
	 * to move the file from source to destination.
	 * 
	 * @param srFile
	 * @param dtFile
	 */
	public static boolean copyFile(String srFile, String dtFile) {
		boolean status = false;
		File file = new File(srFile);
		if (file.renameTo(new File((new StringBuilder()).append(dtFile).append(file.getName()).toString()))) {
			LOGGER.info("File Moved Successfully..");
			status = true;
		} else {
			LOGGER.info("File is failed to move!");
			status = false;
		}
		return status;
	}

	/**
	 * This method is used to get the column list.
	 * 
	 * @param columnsList is String object
	 * @return Collection type
	 */
	public Collection<TableColumn> getColumns(String columnsList) {
		Collection<TableColumn> columns = null;
		String columnCombination = null;
		StringTokenizer columnToken = null;
		StringTokenizer headerToken = null;
		columns = new ArrayList<TableColumn>();
		columnToken = new StringTokenizer(columnsList, "|~*~|");
		do {
			if (!columnToken.hasMoreTokens())
				break;
			columnCombination = columnToken.nextToken();
			headerToken = new StringTokenizer(columnCombination, ":");
			if (headerToken.hasMoreTokens()) {
				TableColumn tablecolumn = new TableColumn();
				tablecolumn.setDisplayed(true);
				tablecolumn.setHeaderLabel(headerToken.nextToken());
				tablecolumn
						.setTableIndex((new StringBuilder()).append("ERP.").append(headerToken.nextToken()).toString());
				columns.add(tablecolumn);
			}
		} while (true);
		return columns;
	}

}
