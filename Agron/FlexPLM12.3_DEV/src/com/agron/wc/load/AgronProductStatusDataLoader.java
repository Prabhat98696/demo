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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.product.AgronProductQuery;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.season.LCSSeasonProductLinkClientModel;
import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import com.agron.wc.load.AgronColorwayStatusDataLoader;

public class AgronProductStatusDataLoader {

	
	private String loadType = null;
	private WTUser user = null;
	public static String dataLogFileName; 
	public static String logFileLocation = null;
	public static String COMMA_DELIMITER = null;
	public static String LOGGER_DELIMITER = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	public static BufferedWriter dataLogWriter = null;
	
	public static final Logger logger = LogManager.getLogger(AgronProductStatusDataLoader.class);
	//public static String UPLOAD_ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.load.colorwayStatusAttribute.excelUploadAttributes");
	public static String UPLOAD_PRODUCT_ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.load.productStatusAttribute.excelUploadAttributes");
	static List<String> excelProductLayoutAttributes = Arrays.asList(UPLOAD_PRODUCT_ATTRIBUTE_LIST.split(","));
	
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
	
	public AgronProductStatusDataLoader(WTUser userObj, String dataFile, String dataLoadType) {
		this.loadType = dataLoadType;
		this.user = userObj;
	}

	public void loadData(ArrayList keyList, ArrayList dataList, boolean ignoreBlankValue) throws WTException, WTPropertyVetoException {
		String dataLogFile = null;
		try {
			dataLogFile = AgronColorwayStatusDataLoader.setLogFile(loadType);
		} catch (WTException wt) {
			wt.printStackTrace();
			throw new WTException(wt.getLocalizedMessage());
		}

		ArrayList tempRow;
		StringBuffer loggerRowData =null;
		logger.info("####################################################");
		logger.info("**Agron Status Data upload started...** ");
		
	 if(dataList.isEmpty() && dataList.size() == 0) {
		 	AgronColorwayStatusDataLoader.appendRowLoadStatus(new StringBuffer(), "", "There is no data in the excel file So please add the data in the excel file");
			notifyPLMUser(false, dataList, 0, 0);
			return;
		}
	
	 	keyList.add("Status");
	 	keyList.add("Error Code");
	 	loggerRowData= AgronColorwayStatusDataLoader.getFormattedLogEntryRow(keyList);
		keyList.remove(4);
		keyList.remove("Error Code");
		AgronColorwayStatusDataLoader.appendRowLoadStatus(loggerRowData, "", "");
	 
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
			
			loggerRowData = AgronColorwayStatusDataLoader.getFormattedLogEntryRow(tempRow);
			String seasonName=tempRow.get(0).toString();
			if(FormatHelper.hasContent(seasonName)){
				seasonName=seasonName.trim();
				}
			String productWork=tempRow.get(1).toString();
			if(FormatHelper.hasContent(productWork)){
				productWork=productWork.trim();
				}
			
			
			logger.info("Procss seasonName :: >"+seasonName+", productWork ::>"+productWork);
			
			FlexType flexType_Product = FlexTypeCache.getFlexTypeFromPath("Product");
			AgronProductQuery query = new AgronProductQuery();
			Collection attList = new ArrayList();
			attList.add("agrWorkOrderNoProduct");

			Map<String, String> map = new HashMap<String, String>();
			map.put("displayAttribute", "agrWorkOrderNoProduct");
			map.put("quickSearchCriteria", productWork);

			SearchResults results = new SearchResults();

			// Find results for Product
			results = query.findProductsByCriteria((map), flexType_Product, attList, null, null, "agrWorkOrderNoProduct");
			Collection<FlexObject> prodObj = results.getResults();
			LCSProduct product = null;
			if(prodObj.size() > 0) {
				FlexObject prObj = prodObj.iterator().next();
				String branchId = prObj.getString("LCSPRODUCT.BRANCHIDITERATIONINFO");
				product = (LCSProduct) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + branchId);

				logger.debug("PRODUCT NAME :: "+product.getName());
			} else{
				logger.info("ERROR : PRODUCT Object not found For Work "+productWork);
				AgronColorwayStatusDataLoader.appendRowLoadStatus(loggerRowData, "Failed", "PRODUCT Object not found");
				failedCount++;
				continue;
			}
			
			product= (LCSProduct) VersionHelper.getVersion((LCSProduct)VersionHelper.latestIterationOf(product.getMaster()), "A");
				LCSSeasonProductLinkClientModel seasonProdLinkModel=new LCSSeasonProductLinkClientModel();
			//Find results for Season	
			LCSSeason seasonObj =  new LCSSeasonQuery().findSeasonByNameType(seasonName.trim(), null);
			if(seasonObj==null ||  seasonObj.getMaster()==null){
				logger.info("ERROR : SEASON Object not found for seasonName  "+seasonName);
				AgronColorwayStatusDataLoader.appendRowLoadStatus(loggerRowData, "Failed", "Season Object not found");
				failedCount++;
				continue;
			}
			logger.debug("SEASON NAME :: "+seasonObj.getName());
			seasonObj = (LCSSeason) VersionHelper.latestIterationOf(seasonObj.getMaster());
			LCSProductSeasonLink prodSeasonLink = (LCSProductSeasonLink) new LCSSeasonQuery().findSeasonProductLink(product, seasonObj);
				
			if(prodSeasonLink== null){
				logger.debug("SEASON  ::"+seasonObj.getName()+", PRODUCT ::"+product.getName()+" LCSProductSeasonLink Object not found");
				AgronColorwayStatusDataLoader.appendRowLoadStatus(loggerRowData, "Failed", "LCSProductSeasonLink Object not found");
				logger.info("FAILED :: LCSProductSeasonLink Object not found");
				failedCount++;
				continue;
			}else {
				logger.debug("SEASON  ::"+seasonObj.getName()+", PRODUCT ::"+product.getName());
				try{
					FlexType flexType_prod = prodSeasonLink.getFlexType();
					HashMap attMap = getAttributeMap(flexType_prod, keyList);
				
					for (int j = 2; j < attMap.size(); j++) {
						String attribute =  excelProductLayoutAttributes.get(j);

						if(attribute.contains("LCSPRODUCTSEASONLINK.")){
							FlexTypeAttribute ft = flexType_Product.getAttribute((String) attMap.get(j));
							if(ft!=null){
								if ((ft.getAttVariableType().equals("choice")&& (!"".equals(tempRow.get(j).toString().trim()))) || ft.getAttVariableType().equals("driven")) {
									String displayValue  = (String) tempRow.get(j);
									String attKey = AgronColorwayStatusDataLoader.getAttValueKey(ft, displayValue);
									if(attKey!=null)
									{
										tempRow.set(j, attKey);
									}else if(attKey==null){
										loggerRowData.append("Failed");
										loggerRowData.append(AgronProductStatusDataLoader.LOGGER_DELIMITER);
										loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										logger.info("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										isError = true;
									}
								}
							}
						}
						
						if(attribute.contains("LCSPRODUCT.")){
							FlexTypeAttribute ft = flexType_Product.getAttribute((String) attMap.get(j));
							if(ft!=null){
								if ((ft.getAttVariableType().equals("choice")&& (!"".equals(tempRow.get(j).toString().trim()))) || ft.getAttVariableType().equals("driven")) {
									String displayValue  = (String) tempRow.get(j);
									String attKey = AgronColorwayStatusDataLoader.getAttValueKey(ft, displayValue);
									if(attKey!=null)
									{
										tempRow.set(j, attKey);
									}else if(attKey==null){
										loggerRowData.append("Failed");
										loggerRowData.append(AgronProductStatusDataLoader.LOGGER_DELIMITER);
										loggerRowData.append("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										logger.info("Incorrect Value uploaded For Attribute : "+ft.getAttDisplay()+" [Expected: "+ft.getAttVariableType()+" & Uploaded: "+String.valueOf(tempRow.get(j))+"]");
										isError = true;
									}
								}
							}
						}
					}
					if(!isError) {
						for(int j = 2; j<attMap.size(); j++)
						{
							logger.debug("Value tempRow ::"+(String)tempRow.get(j));
							String value = String.valueOf(tempRow.get(j)).trim();
							logger.debug("IgnoreBlankValue ::"+ignoreBlankValue);
							String attribute =  excelProductLayoutAttributes.get(j);
							
							if(attribute.contains("LCSPRODUCTSEASONLINK.")){
								
								/*if (VersionHelper.isCheckedOut(product)) {
									product = VersionHelper.getWorkingCopy(product);
								} else {
									product = VersionHelper.checkout(product);
								}*/
								seasonProdLinkModel.load(FormatHelper.getObjectId(prodSeasonLink));
								if(ignoreBlankValue){
									if(!"".equals(value) && value!= null){
										if("0".equals(value)){
											logger.debug("Value if PRoduct>>> ::"+value);
											seasonProdLinkModel.setValue((String)attMap.get(j), (String)tempRow.get(j));
										}else{
											logger.debug("Value else if PRoduct >>> ::"+value);
											seasonProdLinkModel.setValue((String)attMap.get(j), (String)tempRow.get(j));
										}
									}
								}else{
									seasonProdLinkModel.setValue((String)attMap.get(j), (String)tempRow.get(j));
								}
							}
							
							if(attribute.contains("LCSPRODUCT.")){
								
								if (VersionHelper.isCheckedOut(product)) {
									product = VersionHelper.getWorkingCopy(product);
								} else {
									product = VersionHelper.checkout(product);
								}
								
								if(ignoreBlankValue){
									if(!"".equals(value) && value!= null){
										if("0".equals(value)){
											logger.debug("Value if PRoduct>>> ::"+value);
											product.setValue((String)attMap.get(j), (String)tempRow.get(j));
										}else{
											logger.debug("Value else if PRoduct >>> ::"+value);
											product.setValue((String)attMap.get(j), (String)tempRow.get(j));
										}
									}
								}else{
									product.setValue((String)attMap.get(j), (String)tempRow.get(j));
								}
							}
							
						}
				
						

						seasonProdLinkModel.save();
						
						LCSLogic.persist(product);
						
						successCount++;
						if (VersionHelper.isCheckedOut(product)) {
							VersionHelper.checkin(product);
						}
						AgronColorwayStatusDataLoader.appendRowLoadStatus(loggerRowData, "Success", "Updated Successfully");
					}else{
						AgronColorwayStatusDataLoader.appendRowLoadStatus(loggerRowData, "", "");
						failedCount++;
					}
					
				}catch(Exception e){
					e.printStackTrace();
					failedCount++;
					AgronColorwayStatusDataLoader.appendRowLoadStatus(loggerRowData, "Failed", e.getMessage());
				}

			}


		}

		notifyPLMUser(true, dataList, successCount, failedCount);
	
		logger.info("******Product Status Data Load Summary**********");
		logger.info("****SUCCESS Row***::::"+successCount);
		logger.info("****FAILED Row****::::"+failedCount);
		logger.info("###################################");
		logger.info("Excle data loader logs location::"+dataLogFileName);
	}
	
	private  void notifyPLMUser(boolean isValidFile,  ArrayList dataList, int successCount, int failedCount ) throws WTException {
		
		StringBuilder emailContent = new StringBuilder();
	
		emailContent.append("Dear PLM User,");
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append(System.getProperty("line.separator"));
		
		if(isValidFile) {
			emailContent.append("Product Status Attribute Data Load is successful.");
			}else {
				emailContent.append("Product Status Attribute Data Load is failed.");
				emailContent.append("There is no data in the excel file So please add the data in the excel file");
			}
		String emailSubject = LCSProperties.get("com.agron.wc.load.productStatusAttribute.EmailSub"); //"QA-Colorway Status Attribute Upload Summary";
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
			
			String attribute =  excelProductLayoutAttributes.get(i);
			String attKey = attribute.split("~")[1];
			String attValue = attribute.split("~")[0];

			logger.debug("Complete attValue is - "+attValue);
			logger.debug("Complete attDisplayValue is - "+attDisplayValue);
			logger.debug("Complete attKey is - "+attKey);
			logger.debug("Complete attKey is - "+attKey.split("\\.")[1]);
			if(attDisplayValue.equalsIgnoreCase(attValue)){
				keyList.add(attKey.split("\\.")[1]);
			 }
			else{
				throw new WTException("Cannot find any Key with Display - "+attDisplayValue);
			}
			i++;
		
		}
		return keyList;	
	}
	
}
