package com.agron.wc.integration.straub.outbound;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.agron.wc.integration.straub.util.AgronCSVWriter;
import com.agron.wc.integration.straub.util.AgronDataHelperUtil;
import com.agron.wc.integration.straub.util.AgronEmailConfigration;
import com.agron.wc.integration.straub.util.AgronLogEntryManagement;
import com.agron.wc.integration.straub.util.AgronLogEntryQuery;
import com.agron.wc.integration.straub.util.AgronSFTPServerConfig;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryQuery;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeQuery;
import com.lcs.wc.skusize.SKUSizeToSeason;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;


public class AgronStraubDataProcessor {


	private static final Logger log = LogManager.getLogger("com.agron.wc.integration.straub.outbound");
	public static String COMMA_DELIMITER =  LCSProperties.get("com.agron.wc.integration.straub.outbound.commaDelimiter");
	public static final int SKU_QUERY_LIMIT = LCSProperties.get("com.agron.wc.integration.straub.outbound.sku.queryLimit", 30000);
	public static String SKUMODIFYSTAMPA2FromDate =LCSProperties.get("com.agron.wc.integration.straub.outbound.sku.modifyStamp2FromDate", "LCSSKU_MODIFYSTAMPA2FromDateString");
	public static String SKUMODIFYSTAMPA2ToDate =LCSProperties.get("com.agron.wc.integration.straub.outbound.sku.modifyStamp2ToDate", "LCSSKU_MODIFYSTAMPA2ToDateString");
	public static String LCSPRODUCTMODIFYSTAMPA2FromDate =LCSProperties.get("com.agron.wc.integration.straub.outbound.product.modifyStamp2FromDate", "LCSPRODUCT_MODIFYSTAMPA2FromDateString");
	public static String LCSPRODUCTMODIFYSTAMPA2ToDate =LCSProperties.get("com.agron.wc.integration.straub.outbound.product.modifyStamp2ToDate", "LCSPRODUCT_MODIFYSTAMPA2ToDateString");
	public static String LCSLOGENTRYMODIFYSTAMPA2FromDate =LCSProperties.get("com.agron.wc.integration.straub.outbound.logentry.modifyStamp2FromDate", "LCSLOGENTRY_MODIFYSTAMPA2FromDateString");
	public static String LCSLOGENTRYMODIFYSTAMPA2ToDate =LCSProperties.get("com.agron.wc.integration.straub.outbound.logentry.modifyStamp2ToDate", "LCSLOGENTRY_MODIFYSTAMPA2ToDateString");
	public static String ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.integration.straub.outbound.excelLayoutAttributes");
	private static String LOGENTRY_OWNER_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.ownerId").trim();
	private static String LOGENTRY_SEASON_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.season").trim();
	private static String LOGENTRY_OUTBOUND_SPLINK_PATH =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.lcslogEntrySPLink").trim();
	private static String  LOGENTRY_LINKTYPE_KEY =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.linkType").trim();
	private static String EXPORT_DATAFILE = LCSProperties.get("com.agron.wc.straub.integration.outBound.exportFileName", "ExportDataFile");
	private static String COLORWAY_NAME_DBCOLUMN = LCSProperties.get("com.agron.wc.integration.straub.outbound.sku.colorwayNamedbColumn").trim();
	
	private static String LOGENTRY_OUTBOUND_PRODUCT_PATH =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.product.lcslogEntryProduct").trim();
	private static String LOGENTRY_PRODUCT_BRANCHID =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.product.branchId").trim();
	
	private static String LOGENTRY_OUTBOUND_SKU_PATH =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.sku.lcslogEntrySKU").trim();
	private static String LOGENTRY_SKU_BRANCHID =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.sku.branchId").trim();
	
	private static String HEADER_AGRON_STYLE =LCSProperties.get("com.agron.wc.integration.straub.outbound.excelHeader.AgronProduct").trim();
	private static String HEADER_AGRON_SEASON =LCSProperties.get("com.agron.wc.integration.straub.outbound.excelHeader.AgronSeason").trim();
	private static String HEADER_AGRON_PRODUCTDIVISION =LCSProperties.get("com.agron.wc.integration.straub.outbound.excelHeader.AgronProductDivision").trim();
	private static String FILTER_SEASON =LCSProperties.get("com.agron.wc.integration.straub.outbound.seasonCondition").trim();
	
	public static FlexType logEntryFlextype = null;
	public static BufferedWriter dataLogWriter = null;
	static Map<String, String> keyValuePair = new HashMap<String, String>();
	  static Map<String, String> agronProductMap = new HashMap<String, String>();
	static List <String> skippedProducts = new ArrayList<String>();
	
	static List<String> excelLayoutAttributes = Arrays.asList(ATTRIBUTE_LIST.split(","));
	private static final String FLEX_TIME_FORMAT = "MM-dd-yyyy:hh:mm:ss a";
	private static final String CONVERT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String GENDER_KEY =  LCSProperties.get("com.agron.wc.integration.straub.outBound.genderKey").trim();
	private static final String COLLECTION_KEY =  LCSProperties.get("com.agron.wc.integration.straub.outBound.collectionKey").trim();
	private static final String SILHOUETTE_KEY =  LCSProperties.get("com.agron.wc.integration.straub.outBound.silhouetteKey").trim();
	
	static{
		try{
			logEntryFlextype = FlexTypeCache.getFlexTypeFromPath(LCSProperties.get("com.agron.wc.integration.straub.util.logEntry.lcslogEntry"));
			getAttKeyValue();
			
		}catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to run the batch process
	 * @throws ParseException
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 */
	public static void startExecution(String mode, String sDate, String eDate) {
		log.info("Method startExecution START MODE :::>>>>"+mode);
		agronProductMap= getMOAAgronProductMap();
		skippedProducts.clear();
		boolean isValid=false;
		String status ="FAILED";
		String errorMessage=null;
		Channel channel = null;
		LCSLogEntry logEntry =null;
		File csvFileName=null;
		AgronLogEntryManagement logEntryManagement = new AgronLogEntryManagement();
		log.debug("ExcelLayoutAttributes>>"+excelLayoutAttributes);
		
		
		try{
			if("".equals(sDate) || sDate==null ){
				sDate =  logEntryManagement.getLastSuccessfulRun();
			}
			if("".equals(eDate) || eDate==null ){
				eDate =  logEntryManagement.getCurrentGMTTime(); 
			}

			if(FormatHelper.hasContent(sDate))
			{ 
				sDate = dateFormat(sDate);
			}

			if(FormatHelper.hasContent(eDate))
			{ 
				eDate = dateFormat(eDate);
			}
			
			log.info("Extract data from  Start Date>>"+sDate+",  To End Date >>"+eDate);
			
			csvFileName = AgronCSVWriter.createCSVFileName();
			BufferedWriter getCSVWriter = AgronCSVWriter.getCSVDataWriter(csvFileName);

			logEntry = logEntryManagement.execute("RUNNING", "In Process");

			logEntryManagement.updateLogEntry(logEntry, "RUNNING", "Extracting results");
			List<Map<String, Object>> listOfRowMapData = getData(mode, sDate, eDate);   // MAIN ENTRY POINT
			log.info("Total Rows Data>>>"+listOfRowMapData.size());

			if(listOfRowMapData.size()>0){
				
				AgronCSVWriter.csvWriter(listOfRowMapData, excelLayoutAttributes, keyValuePair, getCSVWriter); 
				if(channel == null){
					Session session = AgronSFTPServerConfig.getSFTPSession();
					if(session!= null){
						channel = session.openChannel("sftp");
						if(channel!= null){
							channel.connect();
						}
					}
				}

				//File file = new File(dataLogFileName); 
				log.info("Exclel export  location::>>"+csvFileName.getPath()+", File Name>>>"+ csvFileName.getName());
				log.info("channel >>>>"+channel);
				logEntryManagement.updateLogEntry(logEntry, "RUNNING", "Uploading file to server");
				String upload = AgronSFTPServerConfig.UploadFileContent(channel, csvFileName.getPath(), csvFileName.getName());
				if("SUCCESS".equals(upload)){
					log.info("***** skippedProducts.size() ****" + skippedProducts.size());
					if(skippedProducts.size() > 0) {
						log.info("*****File Export COMPLETED WITH ERRORS ****");
						errorMessage="Partial file exported to straub successfully.Please fix below errors.";
						for(String skippedArticle  : skippedProducts) {
							errorMessage = errorMessage + "\n"+  skippedArticle;							
						}
						status="COMPLETED WITH ERRORS";
						isValid = true;
					}else {
					log.info("*****File Export SUCCESS****");
					errorMessage="File exported to straub successfully.";
					status="SUCCESS";
					isValid = true;
					//AgronEmailConfigration.notifyPLMUser(isValid, status, csvFileName.getPath(), errorMessage);
					}
				}else{
					log.info("*****File export failed****");
					status="FAILED";
					isValid = false;
					errorMessage="File export failed. Refer to the server logs/contact system Admin for additional details.";
					//AgronEmailConfigration.notifyPLMUser(isValid, status, csvFileName.getPath(), errorMessage);
				}
			}else{
				if(skippedProducts.size() > 0) {
					log.info("*****File Export COMPLETED WITH ERRORS ****");
					errorMessage="Partial file exported to straub successfully.Please fix below errors.";
					for(String skippedArticle  : skippedProducts) {
						errorMessage = errorMessage + "\n"+  skippedArticle;							
					}
					status="COMPLETED WITH ERRORS";
					isValid = true;
				} else {
				log.info("****No record exported***");
				status="SUCCESS";
				isValid = true;
				errorMessage="No data in CSV file";
				}
			}	
			logEntryManagement.updateLogEntry(logEntry, status, errorMessage);
			
		}catch(Exception exec) {
			status="FAILED";
			isValid = false;
			errorMessage=exec.getMessage();
			exec.printStackTrace();
		}finally {
			AgronEmailConfigration.notifyPLMUser(isValid, status, csvFileName.getPath(), errorMessage);
			log.info("############################test#############");
			log.info("**********EXECUTION END ***************");
			log.info("#########################################");
			if(channel!=null){
				channel.disconnect();
				channel=null;
			}
			AgronSFTPServerConfig.closeSession();
		}
		log.info("Method startExecution END >>>>");
	}


	/**
	 * writeToDataLog - This Method writes to log file
	 * @param debug
	 */
	public static void writeRowDataToCSV(String rowData) {
		try {
			if(rowData != null && rowData.endsWith(COMMA_DELIMITER)){
				rowData = rowData.substring(0, rowData.length()-1);
			}
			log.info("!! dataLogWriter!!!!"+dataLogWriter);
			dataLogWriter.write(rowData);
			dataLogWriter.newLine();
			dataLogWriter.flush();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			log.info(ioe.getLocalizedMessage());
			log.debug(ioe.getLocalizedMessage());
		}
	}

	/**
	 * Set date formate
	 */
	public static String dateFormat(String dateFormat){
		String strDateFormat =null;
		strDateFormat=dateFormat;
		Date date =null;
		try {
			SimpleDateFormat sdfSource = new SimpleDateFormat(FLEX_TIME_FORMAT);
			date = sdfSource.parse(strDateFormat);
			SimpleDateFormat sdfDestination = new SimpleDateFormat(CONVERT_TIME_FORMAT);
			strDateFormat = sdfDestination.format(date);

		} catch (ParseException e) {
			log.error("**Error in dateFormat>>", e);
			e.printStackTrace();
		}
		return strDateFormat;
	}



	public static void getAttKeyValue() throws WTException {
		log.info("::getAttKeyValue::START>>");
		
		FlexType flexTypeProduct = FlexTypeCache.getFlexTypeFromPath("Product");
		FlexType flexTypeSeason = FlexTypeCache.getFlexTypeFromPath("Season");
		FlexTypeAttribute ft = null;
		for (int i = 0; i < excelLayoutAttributes.size(); i++)  {

			String attribute = excelLayoutAttributes.get(i);
			String attKey = attribute.split("\\.")[1];
			try{
				if(attribute.startsWith("SEASON.")){
					if(attribute.contains("agronSeason")){
						ft = flexTypeSeason.getAttribute("seasonName");
					}else{
						ft = flexTypeSeason.getAttribute(attKey);
					}
				} else {
					if(!attribute.contains("productRootType")&& !attribute.contains("agrProductDivision") && !attribute.contains("agrUPCNumber")&& !attribute.contains("adidasAgeGroup")){
						ft  = flexTypeProduct.getAttribute(attKey);
					}
				}

				if(ft!=null){

					if(attribute.contains("productRootType")){
						keyValuePair.put(attribute, HEADER_AGRON_STYLE);
					}else if(attribute.contains("agrProductDivision")){
						keyValuePair.put(attribute, HEADER_AGRON_PRODUCTDIVISION);
					}else if(attribute.contains("agronSeason")){
						keyValuePair.put(attribute, HEADER_AGRON_SEASON);
					}else if(attribute.contains("adidasAgeGroup")){
						keyValuePair.put(attribute, "Adidas Age Group");
					}else{
						String displayValue =ft.getAttDisplay();
						keyValuePair.put(attribute, displayValue);
					}
				}
				log.info("getAttValueKey() attKeyValue - " + attKey);

			}catch(WTException ex){
				log.error("Error - in attribute ::>>"+attribute, ex);
				ex.printStackTrace();

			}
		}
		log.debug("::keyValuePair::::getAttKeyValue::::>>>>"+keyValuePair);
		log.info("::getAttKeyValue::END>>");
	}

	/**
	 * 
	 * @param sku
	 * @param product
	 * @param ssLink
	 * @param spLink
	 * @return Map of object
	 */
	public static Map getRowData(LCSSKU sku, LCSProduct product, LCSSeasonProductLink ssLink, LCSSeasonProductLink spLink, LCSSeason season){
		log.info("getRowData START::>>>");
		Map<String, Object> rowMap = new HashMap<String, Object>();
		HashMap<String, String> artilceUPCMap=null;
		String UPVNoForCommSpArticel="";

		for (int i = 0; i < excelLayoutAttributes.size(); i++)  {
			String attKey="";
			try{
				String attribute = excelLayoutAttributes.get(i);
				log.debug("attribute>>>>"+attribute);
				attKey = attribute.split("\\.")[1];
				if(attribute.startsWith("PRODUCT.")){
					if(attribute.contains("productRootType")){
						//rowMap.put(attribute, product.getFlexType().getTypeDisplayName());
						 rowMap.put(attribute, getAgronProduct(product));
						log.debug(">>>product>>>::::"+getAgronProduct(product));
					}else if(attribute.contains("agrProductDivision")){
						 rowMap.put(attribute, getAgronProductDivision(product));
					}else if(attribute.contains("adidasAgeGroup")){
						String agrAdidasAgeGroup ="";
						String prodGender = AgronDataHelperUtil.getDataFromKey(product, "agrGenderProd", true);
						if("Men's".equalsIgnoreCase(prodGender) || "Women's".equalsIgnoreCase(prodGender) || "Unisex".equalsIgnoreCase(prodGender)){
							agrAdidasAgeGroup = "Adult";
						}
						else if("Girl's".equalsIgnoreCase(prodGender) || "Boy's".equalsIgnoreCase(prodGender) || "Youth".equalsIgnoreCase(prodGender)) {
							agrAdidasAgeGroup = "Youth";
						}

						if(FormatHelper.hasContent(agrAdidasAgeGroup)){
							rowMap.put(attribute, agrAdidasAgeGroup);
						}else{
							rowMap.put(attribute, " ");
						}
					}
					
					else{
						String prodValue = AgronDataHelperUtil.getDataFromKey(product, attKey, false);
						if(FormatHelper.hasContent(prodValue)){
							rowMap.put(attribute, prodValue);
						}else{
							rowMap.put(attribute, " ");
						}
					}
				}else if(attribute.startsWith("SKU.")){
					
					String skuValue = AgronDataHelperUtil.getDataFromKey(sku, attKey, false);
					log.info(">>>>>>>>>>>>>>>>>skuValue>>>>>>>before>>>>>>>>>"+skuValue);
					///////////////////
					if(attKey.contains("agrArticle") && skuValue.contains(",")){
						log.info(">>>>>>>>>>>>>>>>>>skuValue>>>>>>>>>>>>>>>"+skuValue);
						String[] arrOfStr = skuValue.split(","); 
						for (String article : arrOfStr) {
							log.info(">>>>>>>>>>>>>>>>>article>>>>>>>>>>>>>>>>"+article);
							String  str="*";
							 artilceUPCMap = getValidSKU(str.concat(article.concat(str)), sku, product, season);
							log.info("getValidSKU>>>>>>>>>>>>"+artilceUPCMap+">>>>>>>colorwayArticle>>>"+article);
							if(artilceUPCMap.containsKey(article)){
								UPVNoForCommSpArticel= artilceUPCMap.get(article);
								skuValue = article;
								break;
							}
							
						}
							
					}
					////////////////////////
					log.info("skuValue------------------finel------------>"+skuValue); 
					if(FormatHelper.hasContent(skuValue)){
						rowMap.put(attribute, skuValue);
					}else{
						rowMap.put(attribute, " ");
					}
				}else if(attribute.startsWith("SKUSEASONLINK.")){
					
					/*if(attribute.contains("agrAdidasAgeGroup")){
						String agrAdidasAgeGroup ="";
						String prodGender = AgronDataHelperUtil.getDataFromKey(product, "agrGenderProd", true);
						if("Men's".equalsIgnoreCase(prodGender) || "Women's".equalsIgnoreCase(prodGender) || "Unisex".equalsIgnoreCase(prodGender)){
							agrAdidasAgeGroup = "Adult";
						}
						else if("Girl's".equalsIgnoreCase(prodGender) || "Boy's".equalsIgnoreCase(prodGender) || "Youth".equalsIgnoreCase(prodGender)) {
							agrAdidasAgeGroup = "Youth";
						}

						if(FormatHelper.hasContent(agrAdidasAgeGroup)){
							rowMap.put(attribute, agrAdidasAgeGroup);
						}else{
							rowMap.put(attribute, " ");
						}
					}*/
					//else{
						String skuValue = AgronDataHelperUtil.getDataFromKey(ssLink, attKey, true);
						if(FormatHelper.hasContent(skuValue)){
							rowMap.put(attribute, skuValue);
						}else{
							rowMap.put(attribute, " ");
						}
					//}
				
				}else if(attribute.startsWith("PRODUCTSEASONLINK.")){
					rowMap.put(attribute,AgronDataHelperUtil.getDataFromKey(spLink, attKey, false));
				}else if(attribute.startsWith("SEASON.")){
					if(attribute.contains("agronSeason")){
						attKey="seasonName";
						String seasonName = AgronDataHelperUtil.getDataFromKey(season, attKey, false);
						//rowMap.put(attribute,AgronDataHelperUtil.getDataFromKey(season, attKey, false));
						rowMap.put(attribute, getQuarterSeasonName(seasonName));
						log.debug(">>>rowMap>Quater>>::::"+getQuarterSeasonName(seasonName));
					}
					else{
						rowMap.put(attribute,AgronDataHelperUtil.getDataFromKey(season, attKey, false));
					}
				}else if(attribute.startsWith("SKUSIZE.")){
					log.debug(">>>rowMap>SKUSIZE>>::::"+rowMap);
					log.debug(">>>UPVNoForCommSpArticel:::"+UPVNoForCommSpArticel);
					if(FormatHelper.hasContent(UPVNoForCommSpArticel)){
						// Adding UPC nu between"\"'" because of in excel file showing in test format like  '125463
						rowMap.put(attribute, "\"'" + UPVNoForCommSpArticel + "\"");
					}else{
						rowMap.put(attribute, getUPCNumber(sku, season, product));
					}
				}
			}
			catch(WTException ex){
				log.error("Exception in getting value in key >>>>>"+attKey+", Message >> "+ex.getMessage());

			}

			log.debug(":Row Map >>>"+rowMap);
		}
		log.info("getRowData END::>>>");
		return rowMap;
	}

	public static HashMap getValidSKU(String colorwayArticle,  LCSSKU sku, LCSProduct product, LCSSeason season) {
		log.info("Method - getValidSKU For Comma separated Article Start >>");
		boolean validArticle=false;
		HashMap<String, String> articleUPCMap = new HashMap<>() ;
		try{
			FlexType flexType_SKUP = FlexTypeCache.getFlexTypeFromPath("Product\\Adidas");
			HashMap<String, String> criteria = new HashMap<String, String>();

			criteria.put(flexType_SKUP.getAttribute("agrArticle").getSearchCriteriaIndex(), colorwayArticle);
			SearchResults skuObjsRes = new LCSSKUQuery().findSKUsByCriteria(criteria, flexType_SKUP, null, null, null);
			//log.info("skuObjsRes>>>>>>>>>>>>"+skuObjsRes);
			Collection<FlexObject> skuObjs = skuObjsRes.getResults();
			LCSSKU skuObject = null;
			if(skuObjs.size() > 0) {
				FlexObject skuObj = skuObjs.iterator().next();
				String branchId = skuObj.getString("LCSSKU.BRANCHIDITERATIONINFO");
				skuObject = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + branchId);
				//log.debug("SKU NAME :: "+skuObject.getName());
				 articleUPCMap = getUPCNumberAndArticle(skuObject, season, product);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		log.info("Method - getValidSKU END >>");
		return articleUPCMap;
	}
	
	
	/**
	 * This Method is used for getting ProductDivision Value base on product type
	 * @param productObj
	 * @return
	 */
	public static String getAgronProductDivision(LCSProduct productObj) {
		String productDivision="";

		if("Adidas\\Underwear".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
			productDivision ="Apparel";
		}else {
			productDivision ="Hardware";
		}
		return productDivision;
	}
	
	
	
	
	
	public static String getAgronProduct(LCSProduct productObj) {
		String agronProduct =" ";
		String agronProductKey="";
		log.info("Porduct full display ::::: >>"+productObj.getFlexType().getFullNameDisplay(false));
		try{
			//COLLECTION_KEY_GENDER_KEY_SILHOUETTE_KEY
			String prodGender=AgronDataHelperUtil.getDataFromKey(productObj, GENDER_KEY, false);
			String prodSilhouette=AgronDataHelperUtil.getDataFromKey(productObj, SILHOUETTE_KEY, false);
			log.info("::::>>agronProductMap>>>>"+agronProductMap);
			
			if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette) && ("Adidas\\Socks Type\\Socks".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)))) 	  
			{
				agronProductKey = "Socks_"+prodGender+"_"+prodSilhouette;
				agronProduct = agronProductMap.get(agronProductKey);
				log.info("::agronProductKey>>>"+agronProductKey+"::agronProduct>>>>"+agronProduct);
				
			} else if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette) && "Adidas\\Socks Type\\Team".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {

				agronProductKey ="Team_"+prodGender+"_"+prodSilhouette;
				if(agronProductMap.containsKey(agronProductKey)){
					agronProduct = agronProductMap.get(agronProductKey);
				}

			} else if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette) && "Adidas\\Headwear\\Hats".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {

				agronProductKey ="Hats_"+prodGender+"_"+prodSilhouette;
				if(agronProductMap.containsKey(agronProductKey)){
					agronProduct = agronProductMap.get(agronProductKey);
				}

			}else if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette) && "Adidas\\Headwear\\Knits".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {

				agronProductKey ="Knits_"+prodGender+"_"+prodSilhouette;
				if(agronProductMap.containsKey(agronProductKey)){
					agronProduct = agronProductMap.get(agronProductKey);
				}

			}else if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette) && "Adidas\\Underwear".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {

				agronProductKey ="Underwear_"+prodGender+"_"+prodSilhouette;
				if(agronProductMap.containsKey(agronProductKey)){
					agronProduct = agronProductMap.get(agronProductKey);
				}

			}else if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette) && "Adidas\\Sport Accessories".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {

				agronProductKey ="SpprtsAcc_"+prodGender+"_"+prodSilhouette;
				if(agronProductMap.containsKey(agronProductKey)){
					agronProduct = agronProductMap.get(agronProductKey);
				}

			}else if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette) && "Adidas\\Bags".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {

				agronProductKey ="Bags_"+prodGender+"_"+prodSilhouette;
				if(agronProductMap.containsKey(agronProductKey)){
					agronProduct = agronProductMap.get(agronProductKey);
				}

			}
			
			log.info("::agronProductKey>>>"+agronProductKey+"::agronProduct>>>>"+agronProduct);
		
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return agronProduct;
	}

	
	public static String getQuarterSeasonName(String seasonName) {
		if(FormatHelper.hasContent(seasonName)) {
			int firstIndexOfSpace= seasonName.indexOf(" ");
			String quarter="";
			
			if(seasonName.contains("Spring") && seasonName.contains("Summer")) {
				quarter="Q1";
			}else if(seasonName.contains("Fall") && seasonName.contains("Winter")) {
				quarter="Q3";
			}
			
			seasonName= seasonName.substring(2,firstIndexOfSpace)+quarter;	            
		}
			
		return seasonName;
	}

/**
 * This Method use for find Logentry by Criteria
 * @param flextypeIdPath
 * @param criteriaMap
 * @return
 */
	public static Collection<LCSLogEntry> findLogEntryByCriteria(String flextypeIdPath ,Map<String, Object> criteriaMap)
	{
		log.info("findLogEntryByCriteria**:START:criteriaMap::*"+criteriaMap);
		Collection<LCSLogEntry> logEntryObject = null;
		SearchResults logEntryResults = new SearchResults();
		try { 
			FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(flextypeIdPath);
			AgronLogEntryQuery agroLogEntry=new AgronLogEntryQuery();
			logEntryResults = agroLogEntry.findLogEntriesByCriteria(criteriaMap, logEntryType, null, null, null); 
			log.debug("logEntryResults>>>>>>"+logEntryResults);
			logEntryObject= LCSLogEntryQuery.getObjectsFromResults(logEntryResults.getResults(), "OR:com.lcs.wc.foundation.LCSLogEntry:", "LCSLogEntry.IDA2A2");
			log.info("logEntryCollection Size::"+logEntryObject.size());
		}catch(Exception exception){
			exception.printStackTrace();
		}
		log.info("***findLogEntryByCriteria Collection END::>>");
		return logEntryObject;
	}



	/**
	 * This method is use for find product sku link
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static Map<String, List<Object>> getModifiedLinkOfSkuProduct(String startDate, String endDate)
	{
		log.info(":::::getModifiedLinkOfSkuProduct:::START>>");
		List<Object> listOfProduct = new ArrayList<>();
		List<Object> listOfSku = new ArrayList<>();
		Map<String, Object> logEntryCriteriaMap = new HashMap<String, Object>();
		Map<String, List<Object>> mapOfListValue = new HashMap<String, List<Object>>();

		if(startDate!=null ) {
			logEntryCriteriaMap.put(LCSLOGENTRYMODIFYSTAMPA2FromDate, startDate);
		}
		if(endDate!= null){
			logEntryCriteriaMap.put(LCSLOGENTRYMODIFYSTAMPA2ToDate, endDate);
		}
		logEntryCriteriaMap.put("skipRanges", "true");
		Collection<LCSLogEntry> collection= findLogEntryByCriteria(LOGENTRY_OUTBOUND_SPLINK_PATH,logEntryCriteriaMap);
		
		if(collection.size()>0) {
			log.info("Logentry collection size:::>>"+collection.size());
			Iterator<LCSLogEntry> logEntryItr = collection.iterator();
			LCSProduct product;
			LCSSKU lcsSKU;
			LCSSeason season;
			while (logEntryItr.hasNext()) {
				try{
					LCSLogEntry entry= (LCSLogEntry) logEntryItr.next(); 
					log.debug("Entry>>>>>>>>>"+entry);
					String OwnerID = String.valueOf(entry.getValue(LOGENTRY_OWNER_KEY));
					String seasonBranchID = String.valueOf(entry.getValue(LOGENTRY_SEASON_KEY));
					String linkType = String.valueOf(entry.getValue(LOGENTRY_LINKTYPE_KEY));
					season = (LCSSeason) VersionHelper.latestIterationOf(((LCSSeason)LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:"+seasonBranchID)).getMaster());
					   
					log.debug("LinkType ::"+linkType+", OwnerID::"+OwnerID);
					if("PRODUCT".equalsIgnoreCase(linkType) && FormatHelper.hasContent(OwnerID)){
						product = (LCSProduct) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + OwnerID);
						LCSSeasonProductLink spLink = LCSSeasonQuery.findSeasonProductLink(product, season);
						//listOfProduct.add(product);
						listOfProduct.add(spLink);
					}else if("SKU".equalsIgnoreCase(linkType) && FormatHelper.hasContent(OwnerID)){
						lcsSKU = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + OwnerID);
					    LCSSeasonProductLink ssLink = LCSSeasonQuery.findSeasonProductLink(lcsSKU, season);
						//listOfSku.add(lcsSKU);
						listOfSku.add(ssLink);
					}

				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}else{
			log.info("Logentry collection size:::>>: 0");
		}
		mapOfListValue.put("PRODUCT", listOfProduct);
		mapOfListValue.put("SKU", listOfSku);

		log.debug("Map Of Product and sku list ::::>>"+mapOfListValue);
		log.info(":::::getModifiedLinkOfSkuProduct:::END>>");
		return mapOfListValue;
	}


	/**
	 * This methos use to find Modified colorway
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws WTException
	 */
	public static Collection getModifiedSKUCollection(String mode, String startDate, String endDate) throws WTException{
		//Collection<Object> collOfskuObjByCriteria = null;
		log.info("Method getModifiedSKUCollection START >>"+startDate+", endDate>>"+endDate);
		Collection<Object> collOfskuObjByCriteria = getSKUByCriteria(startDate, endDate, mode);
		
		/*Map<String, List<Object>> mapOfSKuproduct = getModifiedLinkOfSkuProduct(startDate, endDate);
		log.debug(":::::mapOfSKuproduct.size() >>>> "+mapOfSKuproduct);

		List<Object> productSeasonLink =   mapOfSKuproduct.get("PRODUCT");
		log.debug(":::::productSeasonLink >>>> "+productSeasonLink);
		List<Object> skuSeasonLink = mapOfSKuproduct.get("SKU");
		log.debug(":::::skuSeasonLink >>>> "+skuSeasonLink);*/
		//
		//collOfskuObjByCriteria.addAll(skuSeasonLink);
		if("NC".equals(mode))
		{
		Collection<Object> listOfProductByCriteria = getProductByCriteria(startDate, endDate, mode);
		log.info(":listOfProductByCriteria.size()>>>> "+listOfProductByCriteria.size());
			//listOfProductByCriteria.addAll(productSeasonLink);

			Collection productSKUList ;
			Iterator productObjct = listOfProductByCriteria.iterator();

			while (productObjct.hasNext()) {
				LCSProduct prodObject = (LCSProduct) productObjct.next();
				productSKUList = LCSSKUQuery.findSKUs(prodObject, false);
				log.info(":productSKUList>>>> "+productSKUList);
				log.info(":siize>>>> "+productSKUList.size());
				log.info("before **** >>"+collOfskuObjByCriteria);
				if(productSKUList.size() > 0){
					Iterator iter = productSKUList.iterator();
					while (iter.hasNext()) {
						LCSSKU sku =(LCSSKU) iter.next();
						if(!collOfskuObjByCriteria.contains(sku)){
							collOfskuObjByCriteria.add(sku);
						}
					}

				}
			}
		}
		log.info("Method getModifiedSKUCollection END**** >>"+collOfskuObjByCriteria);
		return collOfskuObjByCriteria;
	}

	
	/**
	 * This methed is use for find update sku
	 * @param startDate
	 * @param endDate
	 * @param mode
	 * @return
	 * @throws WTException
	 */
	public static Collection<Object> getSKUByCriteria(String startDate, String endDate, String mode)  throws WTException{
		LCSSKUQuery query = new LCSSKUQuery();
		Collection<Object> collOfskuObjByCriteria = new ArrayList<Object>();
		Map<String, String> skuCriteriaMap = new HashMap<String, String>();
		if("FR".equals(mode)) {
			if(FormatHelper.hasContent(startDate) && FormatHelper.hasContent(endDate)){
				skuCriteriaMap.put(SKUMODIFYSTAMPA2FromDate, startDate);
				skuCriteriaMap.put(SKUMODIFYSTAMPA2ToDate, endDate);
			}
			else{
				skuCriteriaMap.put(COLORWAY_NAME_DBCOLUMN, "*");
				skuCriteriaMap.put("skipRanges", "true");
			}

			log.info("skuCriteriaMap  >>"+skuCriteriaMap);
			SearchResults skuresults = query.findSKUsByCriteria(skuCriteriaMap, null, null, null, null, SKU_QUERY_LIMIT);
			collOfskuObjByCriteria= LCSSKUQuery.getObjectsFromResults(skuresults.getResults(), "VR:com.lcs.wc.product.LCSSKU:", "LCSSKU.BRANCHIDITERATIONINFO");
		} else if("NC".equals(mode)) {

			Map<String, Object> logEntryCriteriaMap = new HashMap<String, Object>();

			if(startDate!=null ) {
				logEntryCriteriaMap.put(LCSLOGENTRYMODIFYSTAMPA2FromDate, startDate);
			}
			if(endDate!= null){
				logEntryCriteriaMap.put(LCSLOGENTRYMODIFYSTAMPA2ToDate, endDate);
			}
			Collection<LCSLogEntry> collOfskuLogEntryObjByCriteria= findLogEntryByCriteria(LOGENTRY_OUTBOUND_SKU_PATH,logEntryCriteriaMap);

			Iterator<LCSLogEntry> skulogEntryItr = collOfskuLogEntryObjByCriteria.iterator();
			while (skulogEntryItr.hasNext()) {
				LCSLogEntry logEntrySKUObject = (LCSLogEntry) skulogEntryItr.next();
				log.info("logEntrySKUObject>>>>>>>>>"+logEntrySKUObject);
				String skuBranchID = String.valueOf(logEntrySKUObject.getValue(LOGENTRY_SKU_BRANCHID));
				log.info("skuBranchID::>>>"+skuBranchID);
				if(FormatHelper.hasContent(skuBranchID))
				{ 
					LCSSKU skuObject;
					try {
						skuObject = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + skuBranchID);
						collOfskuObjByCriteria.add(skuObject);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
					
				}
			}

		}
		return collOfskuObjByCriteria;
	}
	
	
	
	/**
	 * This Method use for find product by Criteria
	 * @param startDate
	 * @param endDate
	 * @return List<LCSProduct>
	 * @throws WTException 
	 */
	public static Collection<Object> getProductByCriteria(String startDate, String endDate, String mode) throws WTException
	{
		Map<String, String> prodCriteriaMap = new HashMap<String, String>();
		SearchResults prodResults = new SearchResults();
		Collection<Object> collectionOfProd = new ArrayList<Object>();
		//Collection<Object> listOfProductByCriteria =  new ArrayList<Object>();
		LCSProductQuery produQuery = new LCSProductQuery();
		if("FR".equals(mode)){
			prodCriteriaMap.put(LCSPRODUCTMODIFYSTAMPA2FromDate, startDate);
			prodCriteriaMap.put(LCSPRODUCTMODIFYSTAMPA2ToDate, endDate);
			log.info(":getProductByCriteria::*>>"+prodCriteriaMap);
			try{
				FlexType productflexType = FlexTypeCache.getFlexTypeFromPath("Product");
				prodResults = produQuery.findProductsByCriteria(prodCriteriaMap, productflexType, null, null, null);
				if(prodResults!=null && !"".equals(prodResults)){
					collectionOfProd= LCSProductQuery.getObjectsFromResults(prodResults.getResults(), "VR:com.lcs.wc.product.LCSProduct:", "LCSPRODUCT.BRANCHIDITERATIONINFO");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else if("NC".equals(mode)){
			Map<String, Object> logEntryCriteriaMap = new HashMap<String, Object>();

			if(startDate!=null ) {
				logEntryCriteriaMap.put(LCSLOGENTRYMODIFYSTAMPA2FromDate, startDate);
			}
			if(endDate!= null){
				logEntryCriteriaMap.put(LCSLOGENTRYMODIFYSTAMPA2ToDate, endDate);
			}
			Collection<LCSLogEntry> listOfProductByCriteria= findLogEntryByCriteria(LOGENTRY_OUTBOUND_PRODUCT_PATH,logEntryCriteriaMap);

			Iterator<LCSLogEntry> logEntryItr = listOfProductByCriteria.iterator();
			LCSProduct prodObject;
			while (logEntryItr.hasNext()) {
				LCSLogEntry logEntryprodObject = (LCSLogEntry) logEntryItr.next();
				log.debug("logEntryprodObject>>>>>>>>>"+logEntryprodObject);
				String prodBranchID = String.valueOf(logEntryprodObject.getValue(LOGENTRY_PRODUCT_BRANCHID));
				log.debug("prodBranchID::"+prodBranchID);
				if(FormatHelper.hasContent(prodBranchID)) { 
					prodObject = (LCSProduct) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + prodBranchID);
					collectionOfProd.add(prodObject);
				}
			}
		}

		log.info(":collectionOfProd::Size >>>>"+collectionOfProd.size());
		return collectionOfProd;
	}
	
	/**
	 * This method use to Extract data from sDate to endate 
	 * @param startDate
	 * @param endDate
	 * @return listOfRowMap
	 */
	public static List<Map<String, Object>> getData(String mode, String startDate, String endDate){
		log.info("Method getData:START :::>>");
		LCSSKU lcssku = null;
		LCSProduct product = null;
		List<String> processSKUs =new ArrayList<>();
		List<String> processLinks =new ArrayList<>();
		Map<String, Object> rowDataMap =new HashMap<>();
		List<Map<String, Object>> listOfRowMap = new ArrayList<Map<String, Object>>();
		
		try{
			
			if("NC".equalsIgnoreCase(mode)){
				Map<String, List<Object>> mapOfSKuproduct = getModifiedLinkOfSkuProduct(startDate, endDate);
				log.debug(":::::mapOfSKuproduct.size() >>>> "+mapOfSKuproduct);

				List<Object> productSeasonLink =   mapOfSKuproduct.get("PRODUCT");
				log.debug(":::::productSeasonLink >>>> "+productSeasonLink);
				List<Object> skuSeasonLink = mapOfSKuproduct.get("SKU");
				log.debug(":::::skuSeasonLink >>>> "+skuSeasonLink);

				if(skuSeasonLink.size() > 0) {
					Iterator ssLinks = skuSeasonLink.iterator();
					while (ssLinks.hasNext()) {
						LCSSeasonProductLink skuSeasonObj =(LCSSeasonProductLink) ssLinks.next();
						log.debug(":::::LCSSeasonProductLink >>>> "+skuSeasonObj);

						if(skuSeasonObj!=null && !"".equals(skuSeasonObj)) {
							log.debug(":::::skuSeasonObj inside if >>>> "+skuSeasonObj);

							LCSSKU skuObj = SeasonProductLocator.getSKUARev(skuSeasonObj);
							skuObj= (LCSSKU) VersionHelper.getVersion((LCSSKU)VersionHelper.latestIterationOf(skuObj.getMaster()), "A");
							LCSSeason seasonObj = SeasonProductLocator.getSeasonRev(skuSeasonObj);	
							seasonObj = (LCSSeason) VersionHelper.latestIterationOf(seasonObj.getMaster());
							LCSProduct productObj = SeasonProductLocator.getProductARev(skuObj);
							productObj= (LCSProduct) VersionHelper.getVersion((LCSProduct)VersionHelper.latestIterationOf(productObj.getMaster()), "A");
							String skuOid = FormatHelper.getNumericObjectIdFromObject(skuObj);
							String seasonOid = FormatHelper.getNumericObjectIdFromObject(seasonObj);

							if(!processLinks.contains(skuOid+"_"+seasonOid)){
								processLinks.add(skuOid+"_"+seasonOid);
								log.info(":skuSeasonLink Size>>>"+processLinks.size());
								if(seasonObj!=null && !"".equals(seasonObj)) {
									log.info(": skuObj>>"+skuObj);
									log.info(": productObj>>>"+productObj);
									log.info(": seasonObj>>>"+seasonObj);
									rowDataMap = getSeasonlData(skuObj, productObj, seasonObj);
									log.info(": skuSeasonLink rowDataMap.size()>>>"+rowDataMap.size());
									if(rowDataMap.size()>0){
									listOfRowMap.add(rowDataMap);}

								}
							} 
						}
					}
				}

				if(productSeasonLink.size() > 0) {
					Iterator spLinks = productSeasonLink.iterator();

					while (spLinks.hasNext()) {
						LCSSeasonProductLink prodSeasonObj =(LCSSeasonProductLink) spLinks.next();
						log.debug(":::::prodSeasonObj >>>> "+prodSeasonObj);
						if(prodSeasonObj!=null && !"".equals(prodSeasonObj)) {
							log.debug(":::::prodSeasonObj inside if >>>> "+prodSeasonObj);
							LCSProduct prodObj = SeasonProductLocator.getProductARev(prodSeasonObj);
							prodObj= (LCSProduct) VersionHelper.getVersion((LCSProduct)VersionHelper.latestIterationOf(prodObj.getMaster()), "A");

							LCSSeason seasonObj = SeasonProductLocator.getSeasonRev(prodSeasonObj);	
							seasonObj = (LCSSeason) VersionHelper.latestIterationOf(seasonObj.getMaster());

							String seasonOid = FormatHelper.getNumericObjectIdFromObject(seasonObj);

							Collection<LCSSKU> skuColl = new ProductHeaderQuery().findSKUs(prodObj,null,seasonObj,true,false);
							Iterator skuItr = skuColl.iterator();
							while (skuItr.hasNext()) {
								LCSSKU lcsskuObject =(LCSSKU) skuItr.next();
								lcsskuObject = (LCSSKU) VersionHelper.getVersion((LCSSKU)VersionHelper.latestIterationOf(lcsskuObject.getMaster()), "A");
								String skuOid = FormatHelper.getNumericObjectIdFromObject(lcsskuObject);

								if(!processLinks.contains(skuOid+"_"+seasonOid)){
									processLinks.add(skuOid+"_"+seasonOid);
									log.info(":productSeasonLink Size>>>"+processLinks.size());
									if(seasonObj!=null && !"".equals(seasonObj)) {
										rowDataMap = getSeasonlData(lcsskuObject, prodObj, seasonObj);
										log.info(":productSeasonLink rowDataMap.size()>>>"+rowDataMap.size());
										if(rowDataMap.size()>0){
										listOfRowMap.add(rowDataMap);}
										//
									}
								} 
							}
						}
					}
				}
			}
			Collection<Object> collectionOfskuObj = getModifiedSKUCollection(mode, startDate, endDate);
			log.info(":::::collectionOfskuObj.size() >>>> "+collectionOfskuObj.size());
			
			
			if(collectionOfskuObj.size() > 0) {
				Iterator skuObjectItr = collectionOfskuObj.iterator();
				while (skuObjectItr.hasNext()) {
					lcssku =(LCSSKU) skuObjectItr.next();
					product = SeasonProductLocator.getProductARev(lcssku);

					if(lcssku!=null )
					{ 
						lcssku = (LCSSKU) VersionHelper.getVersion((LCSSKU)VersionHelper.latestIterationOf(lcssku.getMaster()), "A");
						String skuOid = FormatHelper.getNumericObjectIdFromObject(lcssku);
						if(!processSKUs.contains(skuOid)) {
							log.info("Processing SKU ids :>>"+skuOid);
							processSKUs.add(skuOid);
							Collection skuSeasons = (new LCSSeasonQuery()).findSeasons((LCSPartMaster) lcssku.getMaster());
							if(skuSeasons!=null && !"".equals(skuSeasons))
								log.info("Collection of skuSeasons-->>>"+skuSeasons.size());
							if (skuSeasons.size() > 0) {
								Iterator skuSeasonmsItr = skuSeasons.iterator();
								while (skuSeasonmsItr.hasNext()) {
									rowDataMap =new HashMap<>();
									LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) skuSeasonmsItr.next());
									String seasonOid = FormatHelper.getNumericObjectIdFromObject(season);
									//
									if(!processLinks.contains(skuOid+"_"+seasonOid)){
										processLinks.add(skuOid+"_"+seasonOid);
										log.info(":processLinks Size>>>"+processLinks.size());
										if(season!=null && !"".equals(season)) {
											rowDataMap = getSeasonlData(lcssku, product, season);
											log.info(":rowDataMap.size()>>>"+rowDataMap.size());
											if(rowDataMap.size()>0){
											listOfRowMap.add(rowDataMap);}
											//
										}
									}
								}

							}
						} else {
							log.info("::::SKIP SKu process Since already process "+skuOid);
						}
					}

				}
			}
			
			log.info(":listOfRowMap::Size>>"+listOfRowMap.size());
		}catch(Exception e){
			log.error(":Exception in getData::>>>", e);
			e.printStackTrace();
		}
		log.info(":getData::END::>>>");
		return listOfRowMap;
	}

	public static Map<String, Object> getSeasonlData(LCSSKU lcssku, LCSProduct product, LCSSeason season) throws WTException{
		Map<String, Object> rowDataMap =new HashMap<>();
	
		if(season!=null && !"".equals(season)) {
			
			// CAN GET A java.lang.ClassCastException: com.lcs.wc.season.LCSProductSeasonLink cannot be cast to com.lcs.wc.season.LCSSKUSeasonLinks
			LCSSeasonProductLink ssLink = (LCSSeasonProductLink) LCSSeasonQuery.findSeasonProductLink(lcssku, season);
			if(!(ssLink instanceof LCSSKUSeasonLink)) {
				return rowDataMap;
			}
			//LCSSeasonProductLink previousSPLLink=LCSSeasonQuery.findSeasonProductLink(ssLink, ssLink.getEffectSequence()-1);
			log.info("::season.isActive():::>>>"+season.isActive()+",  ssLink.isSeasonRemoved()->> "+ssLink.isSeasonRemoved()+", ssLink>>"+ssLink);
			
		if (FormatHelper.hasContent(String.valueOf(ssLink))&& ssLink!=null && season.isActive() && (!ssLink.isSeasonRemoved())){
				LCSSeasonProductLink prodSeasonLink = LCSSeasonQuery.findSeasonProductLink(product, season);
			
				String sampleETDtoStraub = String.valueOf(ssLink.getValue("agrSampleETDtoStraub"));
				//String productReceiptStatus = String.valueOf(ssLink.getValue("agrProductReceiptStatus"));
				String 	straubImageCompletion = String.valueOf(ssLink.getValue("agrStraubImageCompletion"));
				String  adidasArticle = String.valueOf(lcssku.getValue("agrAdidasArticle"));
				String  skuArticle = String.valueOf(lcssku.getValue("agrArticle"));
				String 	agrCatalogImageStatus = String.valueOf(ssLink.getValue("agrAGRCatalogImageStatus"));
				//Trello 397 - Straub Rule - B2BOnly = Yes
				String agrB2BOnly=String.valueOf(ssLink.getValue("agrB2B"));
				String agrProductionLocation = String.valueOf(ssLink.getValue("agrProductionLocation"));
				log.info("B2B only Value:::::::::"+agrB2BOnly);
				log.info("Condition for Straub Image Completion "+!"agrYes".equalsIgnoreCase(straubImageCompletion));
				log.info("Condition for sampleETDtoStraub Straub "+FormatHelper.hasContent(sampleETDtoStraub));
				log.info("Condition for adidasArticle Straub "+FormatHelper.hasContent(adidasArticle));
				log.info("Condition for AGR Catalog Image Status"+FormatHelper.hasContent(agrCatalogImageStatus));
				log.info("Condition for skuArticle neg Straub "+!skuArticle.contains("-"));
				String agronProduct = getAgronProduct(product);
				String seasonNM = AgronDataHelperUtil.getDataFromKey(season, "seasonName", false);
				String sName = seasonNM;
				seasonNM =  seasonNM.substring(0, 4);
				int seasonmuber=Integer.parseInt(seasonNM); 
				log.info("Condition for agronProduct>>>>>> "+agronProduct);
				//if(FormatHelper.hasContent(sampleETDtoStraub) && FormatHelper.hasContent(adidasArticle) && !skuArticle.contains("-") && !"agrReturnedStraub".equalsIgnoreCase(productReceiptStatus) && FormatHelper.hasContent(agronProduct) && (seasonmuber>=2022)){

				/*Trello 397 - Straub Rule - B2BOnly = Yes consider only AGR Catalog Image status
				 * to stop the feed*/
				if("agrYes".equalsIgnoreCase(agrB2BOnly)){
					if(FormatHelper.hasContent(sampleETDtoStraub) && FormatHelper.hasContent(adidasArticle) && !skuArticle.contains("-") && (!"agrYes".equalsIgnoreCase(agrCatalogImageStatus)) && (seasonmuber>=Integer.parseInt(FILTER_SEASON))&&(FormatHelper.hasContent(agrProductionLocation))){
						//System.out.println("Condition for production location11111"+FormatHelper.hasContent(agrProductionLocation));
						if(FormatHelper.hasContent(agronProduct) ) {

							log.info("::sampleETDtoStraub-->>>"+sampleETDtoStraub+",  AdidasArticle-->> "+adidasArticle+", product-->>"+product.getValue("agrWorkOrderNoProduct")+", Straub Image Completion-->>"+straubImageCompletion+", AGR Catalog Image Status-->>"+agrCatalogImageStatus);
							rowDataMap = getRowData(lcssku, product, ssLink, prodSeasonLink, season);
						}else if(agronProduct == null ||  " ".equals(agronProduct)){
							log.info("SKIP value for AdidasArticle>>"+adidasArticle+", SampleETDtoStraub ::>>"+sampleETDtoStraub+", For Product Work >>"+product.getValue("agrWorkOrderNoProduct")+", SKU Article>>"+lcssku.getValue("agrArticle")+", Straub Image Completion-->>"+straubImageCompletion+", AGR Catalog Image Status-->>"+agrCatalogImageStatus+", Agron Style-->>"+agronProduct+", Season-->>"+seasonNM);

							skippedProducts.add("Missing Agron Style Guide for Article " + lcssku.getValue("agrArticle") + " in season " + sName   );
						}

					}else {

						log.info("SKIP value for AdidasArticle>>"+adidasArticle+", SampleETDtoStraub ::>>"+sampleETDtoStraub+", For Product Work >>"+product.getValue("agrWorkOrderNoProduct")+", SKU Article>>"+lcssku.getValue("agrArticle")+", Straub Image Completion-->>"+straubImageCompletion+", AGR Catalog Image Status-->>"+agrCatalogImageStatus+", Agron Style-->>"+agronProduct+", Season-->>"+seasonNM);
						//skippedProducts.add("Missing Agron Style Guide for Article " + lcssku.getValue("agrArticle") + " in season " + sName   );

					}
				}
				/*If B2B only is No consider both AGR Catalog Image status and Straub Image Completion 
				 * values to be yes to stop the feed*/
				else{
					if(FormatHelper.hasContent(sampleETDtoStraub) && FormatHelper.hasContent(adidasArticle) && !skuArticle.contains("-") && ( !"agrYes".equalsIgnoreCase(straubImageCompletion) || !"agrYes".equalsIgnoreCase(agrCatalogImageStatus) ) && (seasonmuber>=Integer.parseInt(FILTER_SEASON))&& (FormatHelper.hasContent(agrProductionLocation))){
					//	System.out.println("Condition for production location2222222222222:::"+FormatHelper.hasContent(agrProductionLocation));
						if(FormatHelper.hasContent(agronProduct) ) {

							log.info("::sampleETDtoStraub-->>>"+sampleETDtoStraub+",  AdidasArticle-->> "+adidasArticle+", product-->>"+product.getValue("agrWorkOrderNoProduct")+", Straub Image Completion-->>"+straubImageCompletion+", AGR Catalog Image Status-->>"+agrCatalogImageStatus);
							rowDataMap = getRowData(lcssku, product, ssLink, prodSeasonLink, season);
						}else if(agronProduct == null ||  " ".equals(agronProduct)){
							log.info("SKIP value for AdidasArticle>>"+adidasArticle+", SampleETDtoStraub ::>>"+sampleETDtoStraub+", For Product Work >>"+product.getValue("agrWorkOrderNoProduct")+", SKU Article>>"+lcssku.getValue("agrArticle")+", Straub Image Completion-->>"+straubImageCompletion+", AGR Catalog Image Status-->>"+agrCatalogImageStatus+", Agron Style-->>"+agronProduct+", Season-->>"+seasonNM);

						skippedProducts.add("Missing Agron Style Guide for Article " + lcssku.getValue("agrArticle") + " in season " + sName   );
					}
			
				}else {
					
					log.info("SKIP value for AdidasArticle>>"+adidasArticle+", SampleETDtoStraub ::>>"+sampleETDtoStraub+", For Product Work >>"+product.getValue("agrWorkOrderNoProduct")+", SKU Article>>"+lcssku.getValue("agrArticle")+", Straub Image Completion-->>"+straubImageCompletion+", AGR Catalog Image Status-->>"+agrCatalogImageStatus+", Agron Style-->>"+agronProduct+", Season-->>"+seasonNM);
					//skippedProducts.add("Missing Agron Style Guide for Article " + lcssku.getValue("agrArticle") + " in season " + sName   );

					}
				}
				log.info(":rowDataMap.size()>>>"+rowDataMap.size());
			
			}
		}
		return rowDataMap;
	
		
	}
	
	
	public static HashMap getUPCNumberAndArticle(LCSSKU lcssku, LCSSeason lcsseason, LCSProduct product){
		log.info("Method - getUPCNumberAndArticle START");
		HashMap<String, String>  mapOfskuUPCNumberOfSizes =new HashMap<String, String>();
		List<String> skuUPCNumberOfSizes =new ArrayList<>();
			String skusizeUPCString ="";
			String skusizeArticle ="";
			try {
				Collection<ProductSizeCategory> psdCollection = findValidPSDs(lcssku,lcsseason,product);
				log.info("skuSizesCollection:: PSD *******************"+psdCollection);
				if (psdCollection.size() > 0) {
					Iterator<ProductSizeCategory> psdIter = psdCollection.iterator();
					while (psdIter.hasNext()) {
						ProductSizeCategory psc = psdIter.next();
						SearchResults searchResultSKUSizeQuery = SKUSizeQuery.findSKUSizesForPSC(psc, lcssku.getMaster(), null,
								null);
						Collection<SKUSize> skuSizesCollection = SKUSizeQuery.getObjectsFromResults(
								searchResultSKUSizeQuery.getResults(), "VR:com.lcs.wc.skusize.SKUSize:",
								"SKUSize.BRANCHIDITERATIONINFO");
						log.info("skuSizesCollection::" + skuSizesCollection.size() + "::" + skuSizesCollection.toString());
						if (skuSizesCollection != null && skuSizesCollection.size() > 0) {
							Iterator<SKUSize> skuSizeIter = skuSizesCollection.iterator();
							while (skuSizeIter.hasNext()) {
								SKUSize skuSize = (SKUSize) VersionHelper.getVersion(skuSizeIter.next(), "A");
								log.info(">>>>>>>>>>Object skuSize>>>>****changes***"+skuSize); 
								
								SKUSizeToSeason skuSizeToSeason=(new SKUSizeQuery()).getSKUSizeToSeasonBySKUSizeSeason(skuSize,lcsseason);
								
								log.info("skuSize.isActive()>>>>>>>>>>>>>>>>>>>>>"+skuSize.isActive()+">>>>>>>>>>skuSizeToSeason.isActive()>>>>*******"+skuSizeToSeason.isActive());
								if(skuSizeToSeason !=null && skuSizeToSeason.isActive() && skuSize.isActive()){
								String upc = (String) skuSize.getValue("agrUPCNumber");
								skusizeArticle = (String) skuSize.getValue("agrArticleNumber");
								log.info(upc+">>>>>>>>>>UPCARTICLE>>>>*******"+skusizeArticle);
								if(FormatHelper.hasContent(upc)){
								//skuUPCNumberOfSizes.add(upc);
									mapOfskuUPCNumberOfSizes.put(skusizeArticle, upc);
								}
								}
								
							}
						}
					}
					
					/*StringBuilder strbul=new StringBuilder();
					for(String str : skuUPCNumberOfSizes)
					{
						strbul.append(str);
						//for adding comma between elements
						strbul.append(",");
					}
					 
					//just for removing last comma
					//strbul.setLength(strbul.length()-1);
					String upcNumber = strbul.toString() ;
					if (upcNumber.endsWith(",")) {
						upcNumber =upcNumber.substring(0, upcNumber.length() - 1);
					}
					 skusizeUPCString="\"'" + upcNumber + "\"";
					 mapOfskuUPCNumberOfSizes.put(skusizeArticle, skusizeUPCString);*/
				}
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Vaild Map Of skuUPCNumberOfSizes:::"+mapOfskuUPCNumberOfSizes);
			log.info("Method - getUPCNumberAndArticle END");
			 return mapOfskuUPCNumberOfSizes;
		}
	
	


	public static String getUPCNumber(LCSSKU lcssku, LCSSeason lcsseason, LCSProduct product){
		List<String> skuUPCNumberOfSizes =new ArrayList<>();
		String skusizeUPCString ="";
		try {
			Collection<ProductSizeCategory> psdCollection = findValidPSDs(lcssku,lcsseason,product);
			log.info("skuSizesCollection:: PSD *******************"+psdCollection);
			if (psdCollection.size() > 0) {
				Iterator<ProductSizeCategory> psdIter = psdCollection.iterator();
				while (psdIter.hasNext()) {
					ProductSizeCategory psc = psdIter.next();
					SearchResults searchResultSKUSizeQuery = SKUSizeQuery.findSKUSizesForPSC(psc, lcssku.getMaster(), null,
							null);
					Collection<SKUSize> skuSizesCollection = SKUSizeQuery.getObjectsFromResults(
							searchResultSKUSizeQuery.getResults(), "VR:com.lcs.wc.skusize.SKUSize:",
							"SKUSize.BRANCHIDITERATIONINFO");
					log.info("skuSizesCollection::" + skuSizesCollection.size() + "::" + skuSizesCollection.toString());
					if (skuSizesCollection != null && skuSizesCollection.size() > 0) {
						Iterator<SKUSize> skuSizeIter = skuSizesCollection.iterator();
						while (skuSizeIter.hasNext()) {
							
							SKUSize skuSize = (SKUSize) VersionHelper.getVersion(skuSizeIter.next(), "A");
							SKUSizeToSeason skuSizeToSeason=(new SKUSizeQuery()).getSKUSizeToSeasonBySKUSizeSeason(skuSize,lcsseason);
							if(skuSizeToSeason !=null && skuSizeToSeason.isActive() && skuSize.isActive()){
								String upc = (String) skuSize.getValue("agrUPCNumber");
								if(FormatHelper.hasContent(upc)){
									skuUPCNumberOfSizes.add(upc);
								}
							}
							
						}
					}
				}
				
				StringBuilder strbul=new StringBuilder();
				for(String str : skuUPCNumberOfSizes)
				{
					strbul.append(str);
					//for adding comma between elements
					strbul.append(",");
				}
				 
				//just for removing last comma
				//strbul.setLength(strbul.length()-1);
				String upcNumber = strbul.toString() ;
				if (upcNumber.endsWith(",")) {
					upcNumber =upcNumber.substring(0, upcNumber.length() - 1);
				}
				if(FormatHelper.hasContent(upcNumber)){
				 skusizeUPCString="\"'" + upcNumber + "\"";
				}
				
			}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return skusizeUPCString;
	}
	
	
	public static Collection<ProductSizeCategory> findValidPSDs(LCSSKU sku,LCSSeason skuseason, LCSProduct product) throws WTException {
		//LCSProduct product = com.lcs.wc.season.SeasonProductLocator.getProductARev(sku);
		
		SearchResults sr = (new SizingQuery()).findPSDByProductAndSeason((Map) null, product, skuseason,
				(FlexType) null, new Vector(), new Vector());
		log.info("sku name>>>>>>> "+sku.getValue("skuName")+" size of product sizes"+sr.getResults().size());
		Iterator psctsIterator = LCSQuery.iterateObjectsFromResults(sr,"OR:com.lcs.wc.sizing.ProdSizeCategoryToSeason:", "PRODSIZECATEGORYTOSEASON.IDA2A2");
		//psctsIterator= sortedIterator(psctsIterator);
		Vector<ProductSizeCategory> validPsdCollection = new Vector();
		while (psctsIterator.hasNext()) {
			ProdSizeCategoryToSeason pscts = (ProdSizeCategoryToSeason) psctsIterator.next();
			ProductSizeCategory psc = (ProductSizeCategory) VersionHelper.latestIterationOf(pscts.getSizeCategoryMaster());
			if (isValidPsc(psc)) {
				log.info("sku name >>>>>> "+sku.getValue("skuName")+"Sizing object name "+psc.getName());
				validPsdCollection.add(psc);
				log.info("PSC:- " + psc + "::-----" + psc.getName() + "::---" + psc.getCreateTimestamp() );
		}
		}
		
		validPsdCollection.sort(Comparator.comparing(ProductSizeCategory::getCreateTimestamp));
		return validPsdCollection;
	}
	
	public static boolean isValidPsc(ProductSizeCategory psc) {
		boolean isValidPSC = false;
		try {
			Collection<ProdSizeCategoryToSeason> pscToSeasonCollection = new SizingQuery()
					.findRelatedProdSizeCategoryToSeason(psc);
			log.debug("PSC:- " + psc + "::" + psc.getName() + "::" + psc.getCreateTimestamp() + "::pscToSeasons::"+ pscToSeasonCollection.size());
			if (pscToSeasonCollection != null && pscToSeasonCollection.size() > 0) {
				isValidPSC = true;
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return isValidPSC;

	}
	
	
	public static  HashMap<String,String> getMOAAgronProductMap() {
		
		log.info("::Method> >>>>>getMOAAgronProductMap>>>>>>>>>>STRAT>>>>>>>>>>");
		HashMap<String,String> mapOfAgrnProduct =  AgronDataHelperUtil.getMoaMapOfAgronProduct();
		log.info("::Method>>>>>>getMOAAgronProductMap>>>>>>>>>>END>>>>>>>>>>"+mapOfAgrnProduct);
		return mapOfAgrnProduct;
	} 

}
