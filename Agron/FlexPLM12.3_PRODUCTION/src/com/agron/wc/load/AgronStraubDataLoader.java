package com.agron.wc.load;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.FlexObjectHelper;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonHelper;
import com.lcs.wc.season.LCSSeasonLogic;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.LCSSeasonSKULinkClientModel;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.AttributeValueSetter;
import com.lcs.wc.util.FlexObjectUtil;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class AgronStraubDataLoader {
	
	private String loadType = null;
	private WTUser user = null;
	public static String dataLogFileName; 
	public static String logFileLocation = null;
	public static String COMMA_DELIMITER = null;
	public static String LOGGER_DELIMITER = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	public static BufferedWriter dataLogWriter = null;
	
	public static final Logger logger = LogManager.getLogger(AgronStraubDataLoader.class);
	public static String UPLOAD_ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.load.straub.excelUploadAttributes");
	static List<String> excelLayoutAttributes = Arrays.asList(UPLOAD_ATTRIBUTE_LIST.split(","));
	
	static{
		try{
			LOGGER_DELIMITER = LCSProperties.get("com.agron.wc.utilities.load.Data.loggerDelimiter");
			COMMA_DELIMITER =  LCSProperties.get("com.agron.wc.utilities.load.Data.commaDelimiter");
			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			logFileLocation = wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.load.GenerateExcelLogfilePath"));
			new File(logFileLocation).mkdirs();
		}catch(IOException io){
			io.printStackTrace();
		}

	}
	
	public AgronStraubDataLoader(WTUser userObj, String dataFile, String dataLoadType) {
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

		logger.info("Straub Data Loader");

		//FlexType flexType_SKU = FlexTypeCache.getFlexTypeFromPath("Colorway-Season Link\\Adidas_Product");

		ArrayList tempRow;
		StringBuffer loggerRowData =null;

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
			
			if(tempRow.size() < keyList.size())
			{
				tempRow.ensureCapacity( keyList.size());
				for(int i=tempRow.size();i< keyList.size();i++)
				{
					tempRow.add("");
				}
			}
			
			loggerRowData = getFormattedLogEntryRow(tempRow);
			String seasonName=tempRow.get(0).toString();
			seasonName=seasonName.trim();
			String colorwayArticle=tempRow.get(1).toString();
			colorwayArticle=colorwayArticle.trim();
			////////////////////////////START
			
			FlexType flexType_SKUP = FlexTypeCache.getFlexTypeFromPath("Product\\Adidas");
			
			HashMap<String, String> skuCriteriaMap = new HashMap<String, String>();
			skuCriteriaMap.put(flexType_SKUP.getAttribute("agrArticle").getSearchCriteriaIndex(), colorwayArticle);

			logger.debug("::::skuCriteriaMap::>>"+skuCriteriaMap);

			SearchResults skuObjSearchRes = new LCSSKUQuery().findSKUsByCriteria(skuCriteriaMap, flexType_SKUP, null, null, null);

			Collection<FlexObject> collSKUObj = skuObjSearchRes.getResults();

			logger.debug("::::collSKUObj.size::>>"+collSKUObj.size());
			logger.debug("::::collSKUObj::>>"+collSKUObj);
			
			LCSSKU skuObject = null;
			if(collSKUObj.size() > 0) {
				FlexObject skObj = collSKUObj.iterator().next();
				String branchId = skObj.getString("LCSSKU.BRANCHIDITERATIONINFO");
				skuObject = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + branchId);

				logger.debug("SKU NAME :: "+skuObject.getName());
			} else{
				logger.debug("ERROR : SKU Object not found For Article "+colorwayArticle);
				appendRowLoadStatus(loggerRowData, "Failed", "SKU Object not found");
				failedCount++;
				continue;
			}

			skuObject= (LCSSKU) VersionHelper.getVersion((LCSSKU)VersionHelper.latestIterationOf(skuObject.getMaster()), "A");

			LCSProduct product = SeasonProductLocator.getProductARev(skuObject);
			product= (LCSProduct) VersionHelper.getVersion((LCSProduct)VersionHelper.latestIterationOf(product.getMaster()), "A");

			//Find results for Season	
			LCSSeason seasonObj =  new LCSSeasonQuery().findSeasonByNameType(seasonName.trim(), null);
			if(seasonObj==null ||  seasonObj.getMaster()==null){
				logger.debug("ERROR : SEASON Object not found for seasonName  "+seasonName);
				appendRowLoadStatus(loggerRowData, "Failed", "Season Object not found");
				failedCount++;
				continue;
			}
			logger.debug("SEASON NAME :: "+seasonObj.getName());

			seasonObj = (LCSSeason) VersionHelper.latestIterationOf(seasonObj.getMaster());

			LCSSKUSeasonLink skuSeasonLink = (LCSSKUSeasonLink) new LCSSeasonQuery().findSeasonProductLink(skuObject, seasonObj);

			if(skuSeasonLink== null){
				logger.debug("SEASON  ::"+seasonObj.getName()+", PRODUCT ::"+product.getName()+", SKU ::"+skuObject.getName()+", "+" LCSSKUSeasonLink Object not found");
				appendRowLoadStatus(loggerRowData, "Failed", "LCSSKUSeasonLink Object not found");
				failedCount++;
				continue;
			}else {
				logger.debug("SEASON  ::"+seasonObj.getName()+", PRODUCT ::"+product.getName()+", SKU ::"+skuObject.getName());
				try{
					

					FlexType flexType_SKU = skuSeasonLink.getFlexType();
					HashMap attMap = getAttributeMap(flexType_SKU, keyList);
				
					for (int j = 2; j < attMap.size(); j++) {
						FlexTypeAttribute ft = flexType_SKU.getAttribute((String) attMap.get(j));
						if (ft.getAttVariableType().equals("boolean") && (!"".equals(tempRow.get(j).toString().trim()) || ft.isAttRequired() )) {
							String bolAttribute  = (String) tempRow.get(j);
							if(!bolAttribute.trim().equalsIgnoreCase("TRUE") && !bolAttribute.trim().equalsIgnoreCase("FALSE")){
								
								//appendRowLoadStatus(loggerRowData, "Failed", "An invalid value for boolean attributes"+ft.getAttDisplay());
								if(isError) {
									//appendRowLoadStatus(new StringBuffer(), ",", ""+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									loggerRowData.append(", "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]")	;
								}else if("".equals(tempRow.get(j).toString().trim()) && ft.isAttRequired()) {
										//appendRowLoadStatus(loggerRowData, "Failed", "Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									loggerRowData.append("Failed");
									loggerRowData.append(AgronStraubDataLoader.LOGGER_DELIMITER);
									loggerRowData.append("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									isError = true;
									}
								else {
									//appendRowLoadStatus(loggerRowData, "Failed", "Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
								loggerRowData.append("Failed");
								loggerRowData.append(AgronStraubDataLoader.LOGGER_DELIMITER);
								loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
								isError = true;
								}
							
							}
						}
						
						else if (ft.getAttVariableType().equals("date")) {
							String date = (String) tempRow.get(j);
							SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
							TimeZone gmtTime = TimeZone.getTimeZone("GMT");
							format.setTimeZone(gmtTime);
							format.setLenient(false);
							//SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
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
										loggerRowData.append(AgronStraubDataLoader.LOGGER_DELIMITER);
										loggerRowData.append("Blank Value uploaded For Mandatory Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										isError = true;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									//appendRowLoadStatus(loggerRowData, "", "The date string, "+date+", has an invalid format for"+ft.getAttDisplay());
									if(isError) {
										//appendRowLoadStatus(new StringBuffer(), ",", ""+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										loggerRowData.append(", "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]")	;

									}else {
										loggerRowData.append("Failed");
										loggerRowData.append(AgronStraubDataLoader.LOGGER_DELIMITER);
										loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										//appendRowLoadStatus(loggerRowData, "Failed", "Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										isError = true;
									}

								}
								tempRow.set(j, finalDate);
							}

						} else if (ft.getAttVariableType().equalsIgnoreCase("integer")) {
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
									loggerRowData.append(AgronStraubDataLoader.LOGGER_DELIMITER);
									loggerRowData.append("Incorrect Value uploaded For Attribut : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
									isError = true;
						        }
						        
						} else if ((ft.getAttVariableType().equals("choice")&& (!"".equals(tempRow.get(j).toString().trim()))) || ft.getAttVariableType().equals("driven")) {
							String displayValue  = (String) tempRow.get(j);
							String attKey = getAttValueKey(ft, displayValue);
							if(attKey!=null)
							{
								tempRow.set(j, attKey);
							}else if(attKey==null){
								loggerRowData.append("Failed");
								loggerRowData.append(AgronStraubDataLoader.LOGGER_DELIMITER);
								loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
								isError = true;
							}

						}
						
					}
					if(!isError) {
						for(int j = 2; j<attMap.size(); j++)
						{
							logger.debug("Value tempRow ::"+(String)tempRow.get(j));
							String value = String.valueOf(tempRow.get(j)).trim();
							logger.debug("IgnoreBlankValue ::"+ignoreBlankValue);
							if(ignoreBlankValue){
								if(!"".equals(value) && value!= null){
									if("0".equals(value)){
										logger.debug("Value if >>> ::"+value);
										skuSeasonLink.setValue((String)attMap.get(j), (String)tempRow.get(j));
									}else{
										logger.debug("Value else if >>> ::"+value);
										skuSeasonLink.setValue((String)attMap.get(j), (String)tempRow.get(j));
									}
								}
							}else{  
								skuSeasonLink.setValue((String)attMap.get(j), (String)tempRow.get(j));
							}
						}
						
							System.out.println("Save Season Product Link");
							new LCSSeasonLogic().saveSeasonProductLink(skuSeasonLink,false);
							
						

							successCount++;
						appendRowLoadStatus(loggerRowData, "Success", "Updated Successfully");
					}else{
						appendRowLoadStatus(loggerRowData, "", "");
						failedCount++;
					}
					
				}catch(Exception e){
					failedCount++;
					appendRowLoadStatus(loggerRowData, "Failed", e.getMessage());
				}

			}


		}

		notifyPLMUser(true, dataList, successCount, failedCount);
	
		logger.info("******Straub Photography Attributes Data Load Summary**********");
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
			emailContent.append("Straub Photography Attributes Data Load is successful.");
			}else {
				emailContent.append("Straub Photography Attributes Data Load is failed.");
				emailContent.append("There is no data in the excel file So please add the data in the excel file");
			}
		String emailSubject = LCSProperties.get("com.agron.wc.load.straubPhotography.EmailSub");//"Straub Photography Attributes Upload Summary";
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
		logEntryLine.append(AgronStraubDataLoader.LOGGER_DELIMITER);
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
	
}
