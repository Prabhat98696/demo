package com.agron.wc.load;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.product.AgronProductQuery;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MetricFormatter;
import com.lcs.wc.util.VersionHelper;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class AgronProductDimensionDataLoader {


	private String loadType = null;
	private WTUser user = null;
	public static String dataLogFileName; 
	public static String logFileLocation = null;
	public static String COMMA_DELIMITER = null;
	public static String LOGGER_DELIMITER = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	public static BufferedWriter dataLogWriter = null;
	
	public static String defaultUnitOfMeasure = null;
	public static int PRECISION = 0;
	public static String convertUnitOfMeasure=null;
	
	public static final Logger logger = LogManager.getLogger(AgronProductDimensionDataLoader.class);
	
	static{
		try{
			defaultUnitOfMeasure = LCSProperties.get("com.lcs.productDimension.capacity.defaultUnitOfMeasure", "si.Volume.in");
			convertUnitOfMeasure = LCSProperties.get("com.lcs.productDimension.capacity.convertUnitOfMeasure", "si.Volume.m"); //"si.Volume.m"; //+ uomFlexType.getValue(defaultUnitOfMeasure);
			
			PRECISION = FormatHelper.parseInt(LCSProperties.get("com.lcs.wc.measurements.PRECISION", "14"));

			LOGGER_DELIMITER = LCSProperties.get("com.agron.wc.utilities.load.Data.loggerDelimiter");
			COMMA_DELIMITER =  LCSProperties.get("com.agron.wc.utilities.load.Data.commaDelimiter");
			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			logFileLocation = wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.load.GenerateExcelLogfilePath"));
			new File(logFileLocation).mkdirs();
		}
		catch(IOException io){
			io.printStackTrace();
		}

	}
	
	public AgronProductDimensionDataLoader(WTUser userObj, String dataFile, String dataLoadType) {
		this.loadType = dataLoadType;
		this.user = userObj;
	}

	public void loadData(ArrayList keyList, ArrayList dataList, boolean ignoreBlankValue) throws WTException, WTPropertyVetoException {
		String dataLogFile = null;
		try {
			dataLogFile = setLogFile(loadType);
		} catch (WTException wt) {
			wt.printStackTrace();
			throw new WTException(wt.getLocalizedMessage());
		}
		logger.info("####################################################");
		logger.info("**Agron Product Dimension Data upload started...** ");
		DateFormat dateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
				
		ArrayList tempRow;
		StringBuffer loggerRowData = null;
		
		if(dataList.isEmpty() && dataList.size() == 0) {
			 	appendRowLoadStatus(new StringBuffer(), "", "There is no data in the excel file So please add the data in the excel file");
				notifyPLMUser(false, dataList, 0, 0);
				return;
		}
		keyList.add("Status");
	 	keyList.add("Error Code");
	 	loggerRowData= getFormattedLogEntryRow(keyList);
		keyList.remove("Status");
		keyList.remove("Error Code");
		appendRowLoadStatus(loggerRowData, "", "");
		
		Iterator dataListItr = dataList.iterator();
		
		int successCount=0;
		int failedCount=0;
		
		while(dataListItr.hasNext()){
			boolean isError = false;
			tempRow = (ArrayList) dataListItr.next();
			loggerRowData = getFormattedLogEntryRow(tempRow);
			String prodWork=tempRow.get(0).toString();
			if(FormatHelper.hasContent(prodWork)){
			prodWork=prodWork.trim();
			}
			logger.info("Process Work::>>"+prodWork);
				if(tempRow.size() < keyList.size())
				{
						tempRow.ensureCapacity( keyList.size());
						for(int i=tempRow.size();i< keyList.size();i++)
							{
								tempRow.add("");
							}
				}

			
			FlexType flexType_Product = FlexTypeCache.getFlexTypeFromPath("Product");
			AgronProductQuery query = new AgronProductQuery();
			Collection attList = new ArrayList();
			attList.add("agrWorkOrderNoProduct");
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("displayAttribute", "agrWorkOrderNoProduct");
			map.put("quickSearchCriteria", prodWork);
			
			SearchResults results = new SearchResults();
		   
			// Find results for Product
			results = query.findProductsByCriteria((map), flexType_Product, attList, null, null, "agrWorkOrderNoProduct");
			//System.out.println("SearchResults :: "+results);

			Collection<FlexObject> prodObj = results.getResults();
			LCSProduct product = null;
			if(prodObj.size() > 0) {
				FlexObject prObj = prodObj.iterator().next();
				String branchId = prObj.getString("LCSPRODUCT.BRANCHIDITERATIONINFO");
				product = (LCSProduct) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + branchId);

				logger.debug("PRODUCT NAME :: "+product.getName());
				
				if(product.isRevA() != true) {
					product = (LCSProduct) VersionHelper.getVersion(product.getMaster(), "A");
				}
				

				try{
					FlexType flexType_product = product.getFlexType();
					 HashMap attMap = getAttributeMap(flexType_product, keyList);
					 
					 //Checking data type //		
					for (int j = 1; j < attMap.size(); j++) {
						FlexTypeAttribute ft = flexType_product.getAttribute((String) attMap.get(j));
						if(ft!=null){
						if (ft.getAttVariableType().equals("boolean") && (!"".equals(tempRow.get(j).toString().trim()) || ft.isAttRequired() )) {
							
							String bolAttribute  = (String) tempRow.get(j);
							if(!bolAttribute.trim().equalsIgnoreCase("TRUE") && !bolAttribute.trim().equalsIgnoreCase("FALSE")){

								if(isError) {
									loggerRowData.append(", "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]")	;
								}else if("".equals(tempRow.get(j).toString().trim()) && ft.isAttRequired()) {
									loggerRowData.append("Failed");
									loggerRowData.append(AgronProductDimensionDataLoader.LOGGER_DELIMITER);
									loggerRowData.append("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									logger.info("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									
									isError = true;
								}
								else {
									loggerRowData.append("Failed");
									loggerRowData.append(AgronProductDimensionDataLoader.LOGGER_DELIMITER);
									loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									logger.info("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									isError = true;
								}

							}
						}

						else if (ft.getAttVariableType().equals("date")) {
							
							String date = (String) tempRow.get(j);
							SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
							format.setLenient(false);
							Date parsedDate = null;
							String finalDate = "";
							if(!"".equals(date.trim()) || ft.isAttRequired()){
								try {
									if(!"".equals(date.trim())){
										parsedDate = format.parse(date);
										finalDate = format.format(parsedDate);
									}
									else{
										loggerRowData.append("Failed");
										loggerRowData.append(AgronProductDimensionDataLoader.LOGGER_DELIMITER);
										loggerRowData.append("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										logger.info("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										isError = true;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									if(isError) {
										loggerRowData.append(", "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]")	;

									}else {
										loggerRowData.append("Failed");
										loggerRowData.append(AgronProductDimensionDataLoader.LOGGER_DELIMITER);
										loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										logger.info("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										isError = true;
									}

								}
								tempRow.set(j, finalDate);
							}

						} else if (ft.getAttVariableType().equalsIgnoreCase("integer") || ft.getAttVariableType().equalsIgnoreCase("float")) {
							
							String intData = (String) tempRow.get(j);
							boolean numeric = true;

							try {
								Double num = Double.parseDouble(intData);
							} catch (NumberFormatException e) {
								numeric = false;
							}
							if(!numeric && !"".equals(intData.trim()))
							{
								loggerRowData.append("Failed");
								loggerRowData.append(AgronProductDimensionDataLoader.LOGGER_DELIMITER);
								loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
								logger.info("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
								isError = true;
							}

						}else if (ft.getAttVariableType().equals("uom")) {

							String uomTypeData = (String) tempRow.get(j);
							logger.info("uomTypeData >>>>>>>>> "+uomTypeData);
							try{
								String valueUMOMAtt=MetricFormatter.parseNumberFromInputUomToOutputUom(uomTypeData, PRECISION, defaultUnitOfMeasure, convertUnitOfMeasure);
								logger.info("Convert Data>>>> "+valueUMOMAtt);
								if(FormatHelper.hasContent(valueUMOMAtt)){
									tempRow.set(j, valueUMOMAtt);
								}
								}catch(Exception e){
									loggerRowData.append("Failed");
									loggerRowData.append(AgronProductDimensionDataLoader.LOGGER_DELIMITER);
									loggerRowData.append("Incorrect Value uploaded For Attribut : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									logger.info("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									isError = true;
								}

						 }
						}

					}
					
					if(!isError) {
						for(int j = 1; j<attMap.size(); j++)
						{
							logger.debug("Key attMap ::"+(String)attMap.get(j)+"Value tempRow ::"+(String)tempRow.get(j));
							String value = String.valueOf(tempRow.get(j)).trim();
							 if(ignoreBlankValue){
								if(!"".equals(value) && value!= null){
									if("0".equals(value)){
										 product.setValue((String)attMap.get(j), (String)tempRow.get(j));
									}else{
										 product.setValue((String)attMap.get(j), (String)tempRow.get(j));
				
										
									}

								}
							}else{
								product.setValue((String)attMap.get(j), (String)tempRow.get(j));
							}
						}

						new LCSProductLogic().save(product);
						successCount++;
						appendRowLoadStatus(loggerRowData, "Success", "Updated Successfully");
					}else{
						appendRowLoadStatus(loggerRowData, "", "");
						failedCount++;
					}
				
				}catch(Exception e){
					e.printStackTrace();
					failedCount++;
					appendRowLoadStatus(loggerRowData, "Failed", e.getMessage());
				}

				/////////////////
			} else{
				logger.info("ERROR : PRODUCT Object not found For Work "+prodWork);
				appendRowLoadStatus(loggerRowData, "Failed", "PRODUCT Object not found");
				failedCount++;
				continue;
			}
			
			
		}
		
		notifyPLMUser(true, dataList, successCount, failedCount);
		
		logger.info("******Product Dimension Data Load Summary**********");
		logger.info("****SUCCESS Row***::::"+successCount);
		logger.info("****FAILED Row****::::"+failedCount);
		logger.info("###################################");
		logger.info("Excle data loader logs location::"+dataLogFileName);
	}
	
	private void notifyPLMUser(boolean isValidFile,  ArrayList dataList, int successCount, int failedCount ) throws WTException {
		
		StringBuilder emailContent = new StringBuilder();
	
		emailContent.append("Dear PLM User,");
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append(System.getProperty("line.separator"));
		
		if(isValidFile) {
			emailContent.append("Product Dimension Data Load Summary.");
			}else {
				emailContent.append("Product Dimension Data Load Summary.");
				emailContent.append("There is no data in the excel file So please add the data in the excel file");
			}
		String emailSubject = LCSProperties.get("com.agron.wc.load.productDimension.EmailSub");//"Product Dimension Upload Summary";
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append("Total Rows = "+dataList.size());
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append("Success = "+successCount);
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append("Failed = "+failedCount);
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append("Thanks,");
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append("PLM Admin");
		String userEmail = user.getEMail();
		if(FormatHelper.hasContent(userEmail))
		{
			String userToId = userEmail;

			try
			{
				//Calling an internal function to send email to the distribution list by providing the list of email ID's, email-subject and email body content
				AgronMailUtil.notifyUserOnDataLoad(emailSubject, emailContent.toString(), dataLogFileName, userToId);
			}
			catch(Exception e)
			{
				e.printStackTrace();	
			}


		}
		else {
			System.out.println("Email not configured for the user: "+ user.getName());

		}
	}
	
	/**
	 * getAttributeMap - Method to create Map of the Attributes keys
	 * @param flexType
	 * @param columnHeaderSet
	 * @return
	 * @throws WTException
	 */
	public static HashMap getAttributeMap(FlexType flexType, ArrayList columnHeaderSet) throws WTException {
		logger.debug("getAttributeMap() keySet - "+columnHeaderSet);
		ArrayList attKeyList = getAttKeysFromDisplay(flexType, columnHeaderSet);
		
		logger.debug("getAttributeMap() attKeyList - "+attKeyList);
		HashMap attMap = new HashMap();
		for(int i = 0; i < attKeyList.size(); i++){
			attMap.put(i, attKeyList.get(i));
			logger.debug(("Elements of Map - "+attMap));
		}
		logger.debug("Complete Map is - "+attMap);
		return attMap;
	}
	
	/**
	 * getAttKeysFromDisplay - Method returns keyList for the corresponding Value List
	 * @param flexType
	 * @param keySet
	 * @return
	 */
	public static ArrayList getAttKeysFromDisplay(FlexType flexType, ArrayList keySet) throws WTException{
		ArrayList keyList = new ArrayList();

		Iterator<String> keySetItr = keySet.iterator();
		while(keySetItr.hasNext()){
			String attDisplayValue = keySetItr.next().trim();

			if(attDisplayValue.trim().equals("Season Name")){
				keyList.add("seasonName");
			}else if(attDisplayValue.trim().equals("Work #")){
				keyList.add("agrWorkOrderNoProduct");
			} 			
			else if(attDisplayValue.trim().equals("Master Carton Length")){
				keyList.add("agrMasterCartonLength");
			}
			else if(attDisplayValue.trim().equals("Master Carton Width")){
				keyList.add("agtMasterCartonWidth");
			}
			else if(attDisplayValue.trim().equals("Master Carton Depth")){
				keyList.add("agrMasterCartonDepth");
			}
			else if(attDisplayValue.trim().equals("Master Carton Weight")){
				keyList.add("agrMasterCartonWt");
			}
			else if(attDisplayValue.trim().equals("Master Carton Case Pack")){
				keyList.add("agrMasterCartonCasePack");
			}
			else if(attDisplayValue.trim().equals("Master Carton Inner Pack")){
				keyList.add("agrMasterCartonInnerPack");
			}
			else if(attDisplayValue.trim().equals("Cubiscan/Packing Product Length")){
				keyList.add("agrPackingProdLength");
			}
			else if(attDisplayValue.trim().equals("Cubiscan/Packing Product Width")){
				keyList.add("agrPackingProdWidth");
			}
			else if(attDisplayValue.trim().equals("Cubiscan/Packing Product Depth")){
				keyList.add("agrPackingProdDepth");
			}
			else if(attDisplayValue.trim().equals("Cubiscan/Packing Product Weight")){
				keyList.add("agrPackingProdWt");
			}
			
			else if(attDisplayValue.trim().equals("Length (in)")){
				keyList.add("agrHeight");
			}
			else if(attDisplayValue.trim().equals("Width (in)")){
				keyList.add("agrWidth");
			}
			else if(attDisplayValue.trim().equals("Height (in)")){
				keyList.add("agrDepth");
			}
			
			else if(attDisplayValue.trim().equals("Packaged Product Length (in)")){
				keyList.add("agrPackagedProductLength");
			}
			else if(attDisplayValue.trim().equals("Packaged Product Width (in)")){
				keyList.add("agrPackagedProductWidth");
			}
			else if(attDisplayValue.trim().equals("Packaged Product Height (in)")){
				keyList.add("agrPackagedProductHeight");
			}
			else if(attDisplayValue.trim().equals("Packaged Product Weight (lbs)")){
				keyList.add("agrPackagedProductWeight");
			}
			else if(attDisplayValue.trim().equals("Strapped Length, Adjustable. (In)")){
				keyList.add("agrStrappedLength");
			}
			else if(attDisplayValue.trim().equals("Handle Drop length. (In)")){
				keyList.add("agrHandleDropLength");
			}
			else if(attDisplayValue.trim().equals("Capacity")){
				keyList.add("agrCapacity");
			}
						
			else{
				throw new WTException("Cannot find any Key with Display - "+attDisplayValue);
			}

		}
		return keyList;	
	}
	
	/**
	 * setLogFile - This Method sets Log File Name 
	 * @throws WTException
	 * @throws IOException
	 */
	public static String setLogFile(String loadType) throws WTException {
		try {
			String time = String.valueOf(new Date().getTime());
			dataLogFileName = logFileLocation + loadType +"_"+time+".csv";
			dataLogWriter = new BufferedWriter(new FileWriter(dataLogFileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new WTException("Error in Setting Log file - "+e.getLocalizedMessage());
		}
	return dataLogFileName;
	}
	
	/**
	 * getFormattedLogEntryRow - Helper to write line into Log file
	 * @param logLine
	 * @return
	 * @throws WTException
	 */
	public  static StringBuffer getFormattedLogEntryRow (ArrayList row) throws WTException  {
		
		logger.debug("getFormattedLogEntryRow() log row - "+row);
		StringBuffer logEntryLine = new StringBuffer();
		Iterator rowItr = row.iterator();
		String cellValue;
		while(rowItr.hasNext()){
			cellValue = (String) rowItr.next();
			logger.debug("getFormattedLogEntryRow() cellValue - "+cellValue);
			logEntryLine.append(cellValue);
			logEntryLine.append(LOGGER_DELIMITER);
		}
		logger.debug("getFormattedLogEntryRow() logEntryLine - "+logEntryLine);
		return logEntryLine;
	}
	
	/**Method used to write log rows based on status of rows whether success or failed
	 * appendRowLoadStatus
	 * @param logEntryLine
	 * @param status
	 * @param printMsg
	 */
	public static void appendRowLoadStatus(StringBuffer logEntryLine, String status, String printMsg) {
		logEntryLine.append(status);
		logEntryLine.append(AgronProductDimensionDataLoader.LOGGER_DELIMITER);
		logEntryLine.append(printMsg);
		writeToDataLog(logEntryLine.toString());
	}
	/**
	 * writeToDataLog - This Method writes to log file
	 * @param debug
	 */
	public static void writeToDataLog(String debug) {
        try {
        	dataLogWriter.write(debug);
        	dataLogWriter.newLine();
        	dataLogWriter.flush();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe.getLocalizedMessage());
        }
    }

}
