package com.agron.wc.load;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.supplier.LCSSupplierQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class AgronSustainabilityDataLoader {

	private String loadType = null;
	private WTUser user = null;
	public static String dataLogFileName; 
	public static String logFileLocation = null;
	public static String COMMA_DELIMITER = null;
	public static String LOGGER_DELIMITER = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	public static BufferedWriter dataLogWriter = null;

	public static final Logger logger = LogManager.getLogger(AgronSustainabilityDataLoader.class);
	public static String UPLOAD_ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.load.sustainabilityAttribute.excelUploadAttributes");
	static List<String> excelLayoutAttributes = Arrays.asList(UPLOAD_ATTRIBUTE_LIST.split(","));

	private static FlexType Material_FT = null;
	private static FlexType Material_SUPPLIER_FT = null;
	static{
		try{
			LOGGER_DELIMITER = LCSProperties.get("com.agron.wc.utilities.load.Data.loggerDelimiter");
			COMMA_DELIMITER =  LCSProperties.get("com.agron.wc.utilities.load.Data.commaDelimiter");
			wtProperties = WTProperties.getLocalProperties();
			Material_FT = FlexTypeCache.getFlexTypeRoot("Material");
			Material_SUPPLIER_FT = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterialSupplier");
			wtHomePath = wtProperties.getProperty("wt.home");
			logFileLocation = wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.load.GenerateExcelLogfilePath"));
			new File(logFileLocation).mkdirs();
		}catch(IOException | WTException io){
			io.printStackTrace();
		}

	}

	public AgronSustainabilityDataLoader(WTUser userObj, String dataFile, String dataLoadType) {
		this.loadType = dataLoadType;
		this.user = userObj;
	}

	public void loadData(ArrayList keyList, ArrayList dataList, boolean ignoreBlankValue) throws WTException, WTPropertyVetoException {

		logger.info("***********Sustainability Data upload started...****************************");
		DateFormat dateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
		ArrayList tempRow;
		StringBuffer loggerRowData = null;
		String dataLogFile = null;
		try {
			dataLogFile = setLogFile(loadType);
		} catch (WTException wt) {
			wt.printStackTrace();
			throw new WTException(wt.getLocalizedMessage());
		}

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
		LCSMaterialQuery query = new LCSMaterialQuery();
		while(dataListItr.hasNext()){
			boolean isError = false;
			tempRow = (ArrayList) dataListItr.next();
			
			if(tempRow.size() < keyList.size())
			{
				tempRow.ensureCapacity( keyList.size());
				for(int i=tempRow.size();i< keyList.size();i++)
				{
					tempRow.add("");
				}
			}
			loggerRowData = getFormattedLogEntryRow(tempRow);

			String materialAGRRef=tempRow.get(0).toString();
			String supplierName=tempRow.get(1).toString();
			if(!FormatHelper.hasContent(String.valueOf(materialAGRRef))) {
				logger.debug("ERROR : Material Object not found");
				appendRowLoadStatus(loggerRowData, "Failed", "Material not Object not found");
				failedCount++;
				continue;
			}else {
			
			HashMap<String, String> criteria = new HashMap<String, String>();

			criteria.put(Material_FT.getAttribute("agrMaterialRefNumber").getSearchCriteriaIndex(), materialAGRRef);
			SearchResults matSearchRes = query.findMaterialsByCriteria(criteria, Material_FT, null, null, null);
			Collection<FlexObject> matObj = matSearchRes.getResults();

			LCSMaterial materialObject = null;
			if(matObj.size() > 0) {
				FlexObject mtObj = matObj.iterator().next();
				String branchId = mtObj.getString("LCSMATERIAL.BRANCHIDITERATIONINFO");
				materialObject = (LCSMaterial) LCSQuery.findObjectById("VR:com.lcs.wc.material.LCSMaterial:" + branchId);

				if(materialObject == null) {
					logger.debug("ERROR : Material Object not found for Material agr REF#  "+materialObject);
					appendRowLoadStatus(loggerRowData, "Failed", "Material not Object not found");
					failedCount++;
					continue;
				}
				else{

					logger.debug("Material NAME :: "+materialObject.getName());
					materialObject = (LCSMaterial) VersionHelper.getVersion(materialObject.getMaster(), "A");


					if (VersionHelper.isCheckedOut(materialObject)) {
						materialObject = VersionHelper.getWorkingCopy(materialObject);
					} else {
						materialObject = VersionHelper.checkout(materialObject);
					}

					LCSSupplier supplier = null;
					LCSMaterialSupplier matSupp = null;
					System.out.println("Material_SUPPLIER_====");
					if(FormatHelper.hasContent(String.valueOf(supplierName))) {
						System.out.println("SUPPLIER_===="+supplierName);
						supplier  = new LCSSupplierQuery().findSupplierByNameType(supplierName, null);
						System.out.println("supplier_===="+supplier);
						if(supplier == null){
							//loggerRowData.append("Failed");
							//loggerRowData.append(AgronSustainability.LOGGER_DELIMITER);
							//loggerRowData.append("LCSSupplier Object not found");
							appendRowLoadStatus(loggerRowData, "Failed", "Supplier not Object not found");
							failedCount++;
							continue;
							
						}
						else{
							System.out.println("supplier_===="+supplier);
							matSupp = (LCSMaterialSupplier) new LCSMaterialSupplierQuery().findMaterialSupplier((LCSMaterialMaster)materialObject.getMaster(), (LCSSupplierMaster)supplier.getMaster());

							if(matSupp == null){
							
								/*loggerRowData.append("Failed");
								loggerRowData.append(AgronSustainability.LOGGER_DELIMITER);
								loggerRowData.append("LCSMaterialSupplier Object not found");
								failedCount++;
								isError = true;*/
								
								appendRowLoadStatus(loggerRowData, "Failed", "LCSMaterialSupplier Object not found");
								failedCount++;
								continue;
							}else {
								try{
									logger.debug("Material  ::"+materialObject.getName()+", Supplier ::"+supplier.getName()+" LCSMaterialSupplier ::"+matSupp.getName());
									HashMap attMap = getAttributeMap(keyList);
									logger.debug("attMap.size>>>"+attMap.size());
									logger.debug("attMap>>>"+attMap);
									for (int j = 2; j < attMap.size(); j++) {
										FlexTypeAttribute ft = Material_SUPPLIER_FT.getAttribute((String) attMap.get(j));
										if (ft.getAttVariableType().equals("boolean") && (!"".equals(tempRow.get(j).toString().trim()) || ft.isAttRequired() )) {
											String bolAttribute  = (String) tempRow.get(j);
											if(!bolAttribute.trim().equalsIgnoreCase("TRUE") && !bolAttribute.trim().equalsIgnoreCase("FALSE")){

												if(isError) {
													loggerRowData.append(", "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]")	;
												}else if("".equals(tempRow.get(j).toString().trim()) && ft.isAttRequired()) {
													loggerRowData.append("Failed");
													loggerRowData.append(AgronSustainabilityDataLoader.LOGGER_DELIMITER);
													loggerRowData.append("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
													isError = true;
												}
												else {
													loggerRowData.append("Failed");
													loggerRowData.append(AgronSustainabilityDataLoader.LOGGER_DELIMITER);
													loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
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
														loggerRowData.append(AgronSustainabilityDataLoader.LOGGER_DELIMITER);
														loggerRowData.append("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
														isError = true;
													}
												} catch (ParseException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
													if(isError) {
														loggerRowData.append(", "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]")	;
													}else {
														loggerRowData.append("Failed");
														loggerRowData.append(AgronSustainabilityDataLoader.LOGGER_DELIMITER);
														loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
														isError = true;
													}

												}
												tempRow.set(j, finalDate);
											}

										} else if (ft.getAttVariableType().equalsIgnoreCase("integer") || ft.getAttVariableType().equalsIgnoreCase("currency") || ft.getAttVariableType().equalsIgnoreCase("float")){
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
												loggerRowData.append(AgronSustainabilityDataLoader.LOGGER_DELIMITER);
												loggerRowData.append("Incorrect Value uploaded For Attribut : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
												isError = true;
											}

										}
										else if (ft.getAttVariableType().equals("choice")&& (!"".equals(tempRow.get(j).toString().trim()))) {
											String displayValue  = (String) tempRow.get(j);
											String attKey = getAttValueKey(ft, displayValue);
											if(attKey!=null)
											{
												tempRow.set(j, attKey);
											}else if(attKey==null){
												loggerRowData.append("Failed");
												loggerRowData.append(AgronSustainabilityDataLoader.LOGGER_DELIMITER);
												loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
												isError = true;
											}

										}

									}

									if(!isError) {
										if (VersionHelper.isCheckedOut(matSupp) || VersionHelper.isCheckedOutByUser(matSupp)) {
											matSupp = (LCSMaterialSupplier) VersionHelper.getWorkingCopy(matSupp);
										}else {
											matSupp = (LCSMaterialSupplier) VersionHelper.checkout(matSupp);
										}

										for(int j = 2; j<attMap.size(); j++)
										{
											logger.debug("Value tempRow ::"+(String)tempRow.get(j));
											String value = String.valueOf(tempRow.get(j)).trim();
											logger.debug("IgnoreBlankValue ::"+ignoreBlankValue);
											if(ignoreBlankValue){
												if(!"".equals(value) && value!= null){
													if("0".equals(value)){
														logger.debug("Value if >>> ::"+value);
														matSupp.setValue((String)attMap.get(j), (String)tempRow.get(j));
													}else{
														logger.debug("Value else if >>> ::"+value);
														matSupp.setValue((String)attMap.get(j), (String)tempRow.get(j));
													}
												}
											}else{
												matSupp.setValue((String)attMap.get(j), (String)tempRow.get(j));
											}
										}
										matSupp = (LCSMaterialSupplier) LCSLogic.persist(matSupp,true);
										if (matSupp != null && (VersionHelper.isCheckedOut(matSupp) || VersionHelper.isCheckedOutByUser(matSupp))) {
											matSupp = (LCSMaterialSupplier) VersionHelper.checkin(matSupp);
										}

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
									if (VersionHelper.isCheckedOut(matSupp)) {
										VersionHelper.checkin(matSupp);
									}
								}
							}
						}
					} else {
						isError = true;
						appendRowLoadStatus(loggerRowData, "Failed", "Supplier not Object not found");
						failedCount++;
						continue;
					}

				}
			}else {
				isError = true;
				appendRowLoadStatus(loggerRowData, "Failed", "Material not Object not found");
				failedCount++;
				continue;
			}
		}
	}
		notifyPLMUser(true, dataList, successCount, failedCount);

		logger.info("*****Sustainability Data Load Summary**********");
		logger.info("****SUCCESS Row***::::"+successCount);
		logger.info("****FAILED Row****::::"+failedCount);
		logger.info("###################################");
		logger.info("Excle data loader logs location::"+dataLogFileName);
	}

	
	
	public static String getAttValueKey(FlexTypeAttribute ft, String dispValue) throws WTException {
		logger.debug("getAttValueKey() ft - " + ft);
		logger.debug("getAttValueKey() dispValue - " + dispValue);
		String attKey=null;
		Collection attributevaluekeyColl = ft.getAttValueList().getKeys();
		Iterator it = attributevaluekeyColl.iterator();
		try {
			if(!"".equals(dispValue.trim())){
				while (it.hasNext()) {
					String key = (String) it.next();
					String dispVal = ft.getAttValueList().getValue(key, null);
					logger.debug("dispValue " + dispValue);
					logger.debug("dispVal " + dispVal);
					
					if (dispValue.equalsIgnoreCase(dispVal)) {
						return key;
					}
				}
			}
			else {
				return "";
			}
		} catch (Exception e) {
			logger.error("Exception is" + e.getLocalizedMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
			// throw new WTException(e.getLocalizedMessage());
		}
		return attKey;
	}
	
	
	
	private void notifyPLMUser(boolean isValidFile,  ArrayList dataList, int successCount, int failedCount ) throws WTException {
		
		StringBuilder emailContent = new StringBuilder();
	
		emailContent.append("Dear PLM User,");
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append(System.getProperty("line.separator"));
		
		if(isValidFile) {
			emailContent.append("Sustainability Data Load is successful.");
			}else {
				emailContent.append("Agron Sustainability Data Load is failed.");
				emailContent.append("There is no data in the excel file So please add the data in the excel file");
			}
		String emailSubject = "Sustainability Upload Summary";
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
	public static HashMap getAttributeMap(ArrayList columnHeaderSet) throws WTException {
		logger.debug("getAttributeMap() keySet - "+columnHeaderSet);
		ArrayList attKeyList = getAttKeysFromDisplay(columnHeaderSet);

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
	public static ArrayList getAttKeysFromDisplay(ArrayList keySet) throws WTException{
		ArrayList keyList = new ArrayList();
		int i =0;
		Iterator<String> keySetItr = keySet.iterator();
		while(keySetItr.hasNext()){
			String attDisplayValue = keySetItr.next().trim();
			
			String attribute = excelLayoutAttributes.get(i);
			String attKey = attribute.split("~")[1];
			String attValue = attribute.split("~")[0];

			logger.debug("Complete attValue is - "+attValue);
			logger.debug("Complete attDisplayValue is - "+attDisplayValue);
			logger.debug("Complete attKey is - "+attKey);
			if(attDisplayValue.equalsIgnoreCase(attValue)){
				keyList.add(attKey);
			 }
			else{
				throw new WTException("Cannot find any Key with Display - "+attDisplayValue);
			}
			i++;
		
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
		logEntryLine.append(AgronAdidasArticleDataLoader.LOGGER_DELIMITER);
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
