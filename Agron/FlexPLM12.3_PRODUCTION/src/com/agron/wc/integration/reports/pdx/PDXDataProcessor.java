package com.agron.wc.integration.reports.pdx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.sql.Timestamp;
import java.util.Calendar;
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
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryQuery;
import com.lcs.wc.foundation.LCSQuery;
import wt.part.WTPartMaster;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
//import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
//import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.document.LCSDocument;
//import com.lcs.wc.document.LCSDocumentClientModel;
//import com.lcs.wc.document.LCSDocumentLogic;
//import com.lcs.wc.document.LCSDocumentQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeasonProductLink;
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
import wt.util.WTProperties;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import com.agron.wc.integration.reports.pdx.AgronPDXExcelReportGenerator;
import com.agron.wc.integration.straub.outbound.AgronStraubDataProcessor;
import com.agron.wc.integration.util.AgronIntegrationUtil;
import com.agron.wc.integration.reports.pdx.PDXEmailConfigration;
import com.lcs.wc.client.web.TableColumn;


public class PDXDataProcessor {


	private static final Logger log = LogManager.getLogger(PDXDataProcessor.class);
	public static String COMMA_DELIMITER =  LCSProperties.get("com.agron.wc.integration.straub.outbound.commaDelimiter");
	private static final String FLEX_TIME_FORMAT = "MM-dd-yyyy:hh:mm:ss a";
	private static final String CONVERT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String GENDER_KEY =  LCSProperties.get("com.agron.wc.integration.straub.outBound.genderKey").trim();
	private static final String COLLECTION_KEY =  LCSProperties.get("com.agron.wc.integration.straub.outBound.collectionKey").trim();
	private static final String SILHOUETTE_KEY =  LCSProperties.get("com.agron.wc.integration.straub.outBound.silhouetteKey").trim();
	
	//EMAIL Config
	private static final String MAIL_HEADER ="PDXReport"; //LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.MailHeader","MATERIAL AGGREGATION REPORT");
	private static String MAIL_SUBJECT ="PDXReport"; //LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.mailTo.Subject","PTC MATERIAL AGGREGATION REPORT");
	private static final String MAIL_TO_IDS = "murali.sivakumar@acnovate.com";//LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.ToMailIds");
	private static final String MAIL_FROM_ID = LCSProperties.get("com.agron.wc.integration.FromMailId");
	private static String SMTP_HOST = LCSProperties.get("com.agron.wc.integration.SMTPHost");
	static boolean enableEmail = LCSProperties.getBoolean("com.agron.wc.integration.reports.AgronMaterialAggregationReport.Enable.Emails",false);
	private static  String CSV_REPORT_FILE = "";
	private static String NUMERIC_COLUMNS_KEYS=LCSProperties.get("com.agron.wc.integration.reports.PDXReport.Columns","No,Season Name,Work #,Product Name,Colorway Name,Root Article Number,Article #,UPC,Changed Attribute,Changed Date,Changed By,Changed From,Changed To");
	//No,Season Name,Work #,Product Name,Colorway Name,Root Article Number,Article #,UPC,Changed Attribute,Changed Date,Changed By,Changed From,Changed To 
	private static  String REPORT_ID_DATE_FORMAT=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.fileNameDateFormat","yyyyMMddHHmmss");
	private static String REPORT_COLUMNS=LCSProperties.get("com.agron.wc.integration.reports.PDXReport.Columns","No,Season Name,Work #,Product Name,Colorway Name,Root Article Number,Article #,UPC,Changed Attribute,Changed Date,Changed By,Changed From,Changed To");
	private static Collection tableColumns=new ArrayList();
	//PDXReport
	public static final boolean DEBUG = LCSProperties.getBoolean("com.agron.wc.integration.reports.pdx.PDXReport.debug");
	

	
	static {
		try {
			String tempDownloadLocation = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.client.web.CSVGenerator.exportLocation"));
			String wt_home = WTProperties.getLocalProperties().getProperty("wt.home");
			CSV_REPORT_FILE = (new StringBuilder()).append(wt_home).append(tempDownloadLocation).toString();
			log.info("CSV_REPORT_FILE Download Path>>>>"+CSV_REPORT_FILE);
			configureColumnList();
		}catch(Exception e) {
			log.error("Error in configuring PDX Report  ",e);
		}
	}
	
	/**
	 * This method is used to run the batch process
	 * @throws ParseException
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 */
	public static void startExecution(String seasonId, String sDate, String eDate, String user) {
		log.info("Method startExecution START MODE :::>>>>"+seasonId);
		boolean isValid=true;
		String status ="FAILED";
		String errorMessage=null;
		Channel channel = null;
		File file=null;
		File file1=null;
		String  filePath1=CSV_REPORT_FILE;
		String fileName1="PDXReport_Template.xlsx";
		if(CSV_REPORT_FILE.endsWith(File.separator)) {
			filePath1=filePath1+fileName1;
		}else {
			filePath1=filePath1+File.separator+fileName1;
		}
		file1 =new File(filePath1);
		
		
		LCSProduct product;
		
		Collection getUniqueProdMasterIDRS =null;
		Collection getUniqueSKUMasterIDRS =null;
		Collection getUniquePSLinkMasterIDRS =null;
		
		HashMap dataForExcel = new HashMap();
		Vector productData =new Vector();
		Vector skuData =new Vector();
		Vector psLinkData =new Vector();
		PreparedQueryStatement seasonChangeLogQuery1 = null;
		PreparedQueryStatement seasonChangeLogQuery2 = null;
		Collection seasonChangeLogData1 =null;
			
		Iterator uniqueProdMasterItr =null;
		Iterator prodObjectsItr =null;
		Collection seasonChangeLogData2 =null;
		int totalRecords=0;
		Collection records;
		ArrayList prodDataCollection = new ArrayList();
		FlexObject prodDataFO;
		String seasonName ="";
			
				
		try{
			
			if(DEBUG)
				System.out.println("000 ---- In PDXDataProcessor:::startExecution and Input parameter are "+seasonId+"  " +sDate+"  "+eDate+"  "+user);

			LCSSeason season = (LCSSeason)LCSQuery.findObjectById(seasonId);
			FlexType productType = season.getProductType();
			String productDisplayName = productType.getAttribute("productName").getAttDisplay();
			String skuDisplayName = productType.getAttribute("skuName").getAttDisplay();
			//productType = lcsproduct.getFlexType().getFullName(true);
			//System.out.println("002---- In PDXDataProcessor::: getData	ProductName-->"+productType.getAttribute("productName").getAttDisplay());
			//System.out.println("002---- In PDXDataProcessor::: getData	SKUName-->"+productType.getAttribute("skuName").getAttDisplay());
			
			String seasonMasterID = FormatHelper.getNumericObjectIdFromObject((LCSSeasonMaster)season.getMaster());
			seasonName = season.getName();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			Date stDate = sdf.parse(sDate); 
			Date endDate = sdf.parse(eDate);
			
			stDate = adjustHours(stDate, "add");
			endDate = adjustHours(endDate, "add");

			if(DEBUG)
				System.out.println("001 ---- In PDXDataProcessor::: seasonMasterID ; stDate ; endDate "+seasonMasterID+" ; " +stDate+" ; "+endDate);
			//Query to get unique Product  Master
			getUniqueProdMasterIDRS = getUniqueMasterID("com.lcs.wc.product.LCSProduct", stDate,endDate);
			//Query to get unique SKU Master 
			getUniqueSKUMasterIDRS = getUniqueMasterID("com.lcs.wc.product.LCSSKU", stDate,endDate);
			//Query to get unique PSLink Master 
			getUniquePSLinkMasterIDRS = getUniqueMasterID("com.lcs.wc.season.LCSProductSeasonLink", stDate,endDate,seasonMasterID);
			
			if(DEBUG)
				System.out.println("002A ---- In PDXDataProcessor::: getUniqueProdMasterIDRS::::getUniqueSKUMasterIDRS::::getUniquePSLinkMasterIDRS size-->"+getUniqueProdMasterIDRS.size()+"::::"+getUniqueSKUMasterIDRS.size()+"::::"+getUniquePSLinkMasterIDRS.size());
			//Fetch Product Data
			productData = getData("com.lcs.wc.product.LCSProduct", stDate,endDate,getUniqueProdMasterIDRS,season,productDisplayName);
			if(productData.size()>0)
				dataForExcel.put("PRODUCT",productData);
			//Fetch SKU Data
			skuData = getSKUData("com.lcs.wc.product.LCSSKU", stDate,endDate,getUniqueSKUMasterIDRS,season,skuDisplayName);
			if(skuData.size()>0)
				dataForExcel.put("SKU",skuData);
			psLinkData = getPSLinkData("com.lcs.wc.season.LCSProductSeasonLink", stDate,endDate,getUniquePSLinkMasterIDRS,season,seasonMasterID);
			if(psLinkData.size()>0)
				dataForExcel.put("PSLINK",psLinkData);
			if(DEBUG){
				System.out.println("003A ---- In PDXDataProcessor::: productData:::skuData::::psLinkData-->"+productData.size()+"::::"+skuData.size()+"::::"+psLinkData.size());
				System.out.println("003B ---- In PDXDataProcessor::: dataForExcel size"+dataForExcel.size());
			}
			
			//No,Season Name,Work #,Product Name,Colorway Name,Root Article Number,Article #,UPC,Changed Attribute,Changed Date,Changed By,Changed From,Changed To			
			//Write Excel 	
			String jobId= getDocName();
			String fileName= jobId+".xlsx";
			String  filePath=CSV_REPORT_FILE;
			//String message="";
			if(CSV_REPORT_FILE.endsWith(File.separator)) {
				filePath=filePath+fileName;
			}else {
				filePath=filePath+File.separator+fileName;
			}
			
			if (productData.size() == 0 && skuData.size()  == 0 && psLinkData.size() == 0)
			{
				fileName= "PDXReport_Template.xlsx";
				status ="NO DATA";
			}else {
				
				status ="SUCCESFUL";
			}
			//System.out.println("-----------filePath-------------"+filePath);
			//System.out.println("-----------dataForExcel.size()-------------"+dataForExcel.size());
			if (dataForExcel.size()>0)
					 filePath=AgronPDXExcelReportGenerator.generateExcel(filePath.trim(),dataForExcel,tableColumns);
			file =new File(filePath);
			
			log.info("file>>>>"+file);
			
		}catch(Exception exec) {
			status="FAILED";
			isValid = false;
			PDXEmailConfigration.notifyPLMUser(isValid, status, file1.getPath(), "Error Message",user,seasonName,sDate,eDate);
			errorMessage=exec.getMessage();
			exec.printStackTrace();
		}finally {
			if(status.equals("SUCCESFUL"))
				PDXEmailConfigration.notifyPLMUser(isValid, status, file.getPath(), "Error Message",user,seasonName,sDate,eDate);
			else if(status.equals("NO DATA")) 
				PDXEmailConfigration.notifyPLMUser(isValid, status, file1.getPath(), "Error Message",user,seasonName,sDate,eDate);
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
	 * Get Unique Master ID --PRODUCT/SKU
	 */
	public static Collection getUniqueMasterID(String classnamekeyG4Value,Date stDate,Date endDate) throws WTException, IOException{
		
		PreparedQueryStatement getUniqueMasterIDQuery = null;
		Collection uniqueMasterResult =null;
		
		getUniqueMasterIDQuery  = new PreparedQueryStatement();
		if(classnamekeyG4Value.equals("com.lcs.wc.product.LCSProduct"))
			getUniqueMasterIDQuery.appendSelectColumn("LCSSeasonalChangeLog","IdA3B4");
		if(classnamekeyG4Value.equals("com.lcs.wc.product.LCSSKU"))
			getUniqueMasterIDQuery.appendSelectColumn("LCSSeasonalChangeLog","IdA3C4");
		getUniqueMasterIDQuery.appendFromTable("LCSSeasonalChangeLog");
		getUniqueMasterIDQuery.setDistinct(true);
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",stDate, Criteria.GREATER_THAN_EQUAL));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",endDate, Criteria.LESS_THAN_EQUAL));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "idA3A4","0", Criteria.EQUALS));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","?", "IS NOT NULL"));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4",classnamekeyG4Value, Criteria.EQUALS));
			
		uniqueMasterResult = LCSQuery.runDirectQuery(getUniqueMasterIDQuery).getResults();
		
		return uniqueMasterResult;	
	}
	/**
	 * Get Unique Master ID --PSLINK
	 */
	public static Collection getUniqueMasterID(String classnamekeyG4Value,Date stDate,Date endDate, String seasonMasterId) throws WTException, IOException{
		
		PreparedQueryStatement getUniqueMasterIDQuery = null;
		Collection uniqueMasterResult =null;
		
		getUniqueMasterIDQuery  = new PreparedQueryStatement();
		getUniqueMasterIDQuery.appendSelectColumn("LCSSeasonalChangeLog","IdA3B4");
		//getUniqueMasterIDQuery.appendSelectColumn("LCSSeasonalChangeLog","IdA3C4");
		//getUniqueMasterIDQuery.appendSelectColumn("LCSSeasonalChangeLog","PERSISTEDATTRIBUTE");
		getUniqueMasterIDQuery.appendFromTable("LCSSeasonalChangeLog");
		getUniqueMasterIDQuery.setDistinct(true);
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",stDate, Criteria.GREATER_THAN_EQUAL));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",endDate, Criteria.LESS_THAN_EQUAL));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "idA3A4",seasonMasterId, Criteria.EQUALS));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","?", "IS NOT NULL"));
		getUniqueMasterIDQuery.appendAndIfNeeded();
		getUniqueMasterIDQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4",classnamekeyG4Value, Criteria.EQUALS));
		
		//System.out.println("---------------------getUniqueMasterIDQuery-------------------------------------------------"+getUniqueMasterIDQuery);	
		uniqueMasterResult = LCSQuery.runDirectQuery(getUniqueMasterIDQuery).getResults();
		//System.out.println("---------------------uniqueMasterResult-------------------------------------------------"+uniqueMasterResult.size());
		return uniqueMasterResult;	
	}
	/**
	 * Get ProductData
	 */
	public static Vector getData(String classnamekeyG4Value,Date stDate,Date endDate, Collection getUniqueMasterIDRS, LCSSeason season, String productDisplayName) throws WTException, IOException, ParseException{
		
		if(DEBUG) 
			System.out.println("001---- In PDXDataProcessor::: getData size"+getUniqueMasterIDRS.size());
		
		Vector dataObjects =new Vector();
		Collection getDataQueryRS =null;
		Iterator uniqueMasterItr =null;
		Iterator dataRSItr =null;
		
		PreparedQueryStatement getDataQuery = null;
		LCSProduct product;
		LCSSKU sku;
		FlexObject dataFO1;
		String objName=" ";
		
		if(getUniqueMasterIDRS!=null) {
				uniqueMasterItr = getUniqueMasterIDRS.iterator();
				while(uniqueMasterItr.hasNext()) {
					
					FlexObject Obj  = (FlexObject)uniqueMasterItr.next();
					String masterId = Obj.getString("LCSSeasonalChangeLog.IdA3B4");
					System.out.println("02222 ---- masterId"+masterId);
					LCSPartMaster prodORSKUMaster=null;
					prodORSKUMaster = (LCSPartMaster) LCSQuery.findObjectById("OR:com.lcs.wc.part.LCSPartMaster:" + masterId); 
					if(classnamekeyG4Value.contains("LCSProduct")){
						product = (LCSProduct) VersionHelper.latestIterationOf(prodORSKUMaster);
						objName = product.getName();
						if(DEBUG)
							System.out.println("002---- In PDXDataProcessor::: getData product"+objName);
					}else if(classnamekeyG4Value.contains("LCSSKU")) {
						//sku = (LCSSKU) VersionHelper.latestIterationOf(prodORSKUMaster);
						//objName = sku.getName();
						if(DEBUG)
							System.out.println("002---- In PDXDataProcessor::: getData	sku"+objName);
					}
					getDataQuery = new PreparedQueryStatement();
					getDataQuery.appendSelectColumn("WTUser","name");//WTUser.name  as "User Modified",
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","createStampA2");//LCSSeasonalChangeLog.createStampA2
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","newStringValue");//LCSSeasonalChangeLog.newStringValue,
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","oldStringValue");//LCSSeasonalChangeLog.oldStringValue,
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","classnamekeyG4");//LCSSeasonalChangeLog.classnamekeyG4 -- persistable class
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","persistedAttribute");//LCSSeasonalChangeLog.persistedAttribute -- persistable attribute
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","IDA3G4");
					getDataQuery.appendJoin( "WTUser","idA2A2","LCSSeasonalChangeLog","idA3F4" );
					//seasonChangeLogQuery.appendJoin( "lcsproduct","idA2A2","LCSSeasonalChangeLog","idA3G4" );
					getDataQuery.appendFromTable("LCSSeasonalChangeLog");//LCSSeasonalChangeLog, WTUser, LCSProduct 
					getDataQuery.appendFromTable("WTUser");
					//seasonChangeLogQuery.appendFromTable("LCSProduct");
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",stDate, Criteria.GREATER_THAN_EQUAL));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",endDate, Criteria.LESS_THAN_EQUAL));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "idA3A4","0", Criteria.EQUALS));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","?", "IS NOT NULL"));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","com.lcs.wc.product.LCSProduct", Criteria.EQUALS));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "IdA3B4",masterId, Criteria.EQUALS));
					getDataQuery.appendSortBy("LCSSeasonalChangeLog.createStampA2 ASCE");
					getDataQueryRS = LCSQuery.runDirectQuery(getDataQuery).getResults();
					
					if(DEBUG)
						System.out.println("003A---- In PDXDataProcessor::: getData	Query::::::::::RS"+getDataQuery+":::::"+getDataQueryRS.size());
					if(getDataQueryRS!=null) {
						if (getDataQueryRS.size()==1 ){
							String oldValue ="";
							String newValue ="";
							String tempDate = "";
							SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							dataRSItr = getDataQueryRS.iterator();
							while(dataRSItr.hasNext()) {
									
									FlexObject tempObj  = (FlexObject)dataRSItr.next();
									String user = tempObj.getString("WTUser.name");
									newValue = tempObj.getString("LCSSeasonalChangeLog.newStringValue");
									oldValue = tempObj.getString("LCSSeasonalChangeLog.oldStringValue");
									if(DEBUG)
										System.out.println("004---- In PDXDataProcessor::: getData	oldValue :: newValue"+oldValue+" :: "+newValue);
									if(newValue.equals(oldValue))
									{
										if(DEBUG) 
												System.out.println("005---- In PDXDataProcessor::: getData skip ");	
									}else {
										if(DEBUG)
												System.out.println("006---- In PDXDataProcessor::: getData adding i=1 ");
										String prodIDA2A2 =  tempObj.getString("LCSSeasonalChangeLog.IDA3G4");
										LCSProduct prod1 =(LCSProduct) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSProduct:" + prodIDA2A2);
										//System.out.println("006---- In PDXDataProcessor::: getData adding i=1 "+prod1.getName());
										Vector skuSizeUPC =getALLSKUSizeUPC(season,prod1);
										//System.out.println("007A---- In PDXDataProcessor::: skuSizeUPC "+skuSizeUPC.size());
										if (skuSizeUPC.size()>0){
												  for(int j=0; j < skuSizeUPC.size(); j++) { 
													String skuArticleUPC = skuSizeUPC.get(j).toString();
													//System.out.println("011---- In PDXDataProcessor::: skuArticleUPC "+skuArticleUPC);
													String cwName =skuArticleUPC.substring(0, skuArticleUPC.indexOf("~"));
													String rootArticle = skuArticleUPC.substring(skuArticleUPC.indexOf("~")+1, skuArticleUPC.indexOf("^"));
													String article =skuArticleUPC.substring(skuArticleUPC.indexOf("^")+1, skuArticleUPC.indexOf("|"));
													String upc =skuArticleUPC.substring(skuArticleUPC.indexOf("|")+1);
													//tempDate = tempObj.getString("LCSSeasonalChangeLog.createStampA2");
													//System.out.println("----------------changeLogDate--------1---------"+tempDate);//2021-06-29 14:35:19.0
													Date changeLogDate =  sdformat.parse(tempObj.getString("LCSSeasonalChangeLog.createStampA2"));
													changeLogDate =  adjustHours(changeLogDate, "sub");
													//System.out.println("----------------changeLogDate--------1---------"+changeLogDate);
													tempDate = sdformat.format(changeLogDate);
													//System.out.println("----------------changeLogDate--------tempDate---------"+tempDate);
													if(DEBUG)
														System.out.println("011A---- In PDXDataProcessor::: cwName "+cwName+"::::"+article+"::::"+upc);
													//No,Season Name,Work #,Product Name,Colorway Name,Root Article Number,Article #,UPC,Changed Attribute,Changed Date,Changed By,Changed From,Changed To
													String changeAttDisplayName= tempObj.getString("LCSSeasonalChangeLog.persistedAttribute"); //LCSSeasonalChangeLog.persistedAttribute
													dataFO1 = new FlexObject();
													dataFO1.put("No","1");
													dataFO1.put("Season Name",season.getName());
													dataFO1.put("Work #",prod1.getValue("agrWorkOrderNoProduct"));
													dataFO1.put("Product Name",objName);
													dataFO1.put("Colorway Name",cwName);
													dataFO1.put("Root Article Number",rootArticle);
													dataFO1.put("Article #",article);
													dataFO1.put("UPC",upc);
													dataFO1.put("Changed Attribute",productDisplayName);
													dataFO1.put("Changed Date",tempDate);
													dataFO1.put("Changed By",tempObj.getString("WTUser.name"));
													dataFO1.put("Changed From",oldValue);
													dataFO1.put("Changed To",newValue);
													dataObjects.add(dataFO1);
												}
										}else{
											Vector skuVector = getALLSKU(season,prod1);
											if (skuVector.size()>0){
												 for(int j=0; j < skuVector.size(); j++) { 
														LCSSKU sku1 = (LCSSKU)skuVector.get(j);
														dataFO1 = new FlexObject();
														
														Date changeLogDate =  sdformat.parse(tempObj.getString("LCSSeasonalChangeLog.createStampA2"));
														changeLogDate =  adjustHours(changeLogDate, "sub");
														tempDate = sdformat.format(changeLogDate);
														//System.out.println("----------------changeLogDate----2----tempDate---------"+tempDate);
														dataFO1.put("No","1");
														dataFO1.put("Season Name",season.getName());
														dataFO1.put("Work #",prod1.getValue("agrWorkOrderNoProduct"));
														dataFO1.put("Product Name",objName);
														dataFO1.put("Colorway Name",sku1.getName());
														dataFO1.put("Root Article Number",sku1.getValue("agrArticle"));
														dataFO1.put("Article #","");
														dataFO1.put("UPC","");
														dataFO1.put("Changed Attribute",productDisplayName);
														dataFO1.put("Changed Date",tempDate);
														dataFO1.put("Changed By",tempObj.getString("WTUser.name"));
														dataFO1.put("Changed From",oldValue);
														dataFO1.put("Changed To",newValue);
														dataObjects.add(dataFO1);
														System.out.println("012---- In PDXDataProcessor::: getData	dataObjects"+dataObjects.size());
												}
											}else{
														/*LCSSesonProductLink link=LCSSeasonQuery.findSeasonProductLink(prod2,season);
														if(link != null) {
															dataFO1 = new FlexObject();
															dataFO1.put("No","1");
															dataFO1.put("Season Name",season.getName());
															dataFO1.put("Work #",prod1.getValue("agrWorkOrderNoProduct"));
															dataFO1.put("Product Name",objName);
															dataFO1.put("Colorway Name","");
															dataFO1.put("Root Article Number","");
															dataFO1.put("Article #","");
															dataFO1.put("UPC","");
															dataFO1.put("Changed Attribute",productDisplayName);
															dataFO1.put("Changed Date",tempObj.getString("LCSSeasonalChangeLog.createStampA2"));
															dataFO1.put("Changed By",tempObj.getString("WTUser.name"));
															dataFO1.put("Changed From",oldValue);
															dataFO1.put("Changed To",newValue);
															dataObjects.add(dataFO1);
															System.out.println("012---- In PDXDataProcessor::: getData	dataObjects"+dataObjects.size());
														}*/
											}	
										}
									}
													
							}											
						}else if(getDataQueryRS.size()> 1 ){
							if(DEBUG)
								System.out.println("007B---- In PDXDataProcessor::: getDataQueryRS.size()"+getDataQueryRS.size());
							int i=0;
							int k = getDataQueryRS.size();
							dataRSItr = getDataQueryRS.iterator();
							String newValue = "";
							String oldValue = "";
							String tempDate = "";
							SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							//while(dataRSItr.hasNext()) {
							for (int l =1; l<=k; l++){
								String user = "";
								if(l==1){
									FlexObject prodFlexObjold  = (FlexObject)dataRSItr.next();
									oldValue = prodFlexObjold.getString("LCSSeasonalChangeLog.oldStringValue");
									//i++;
								}else if (l<= k)
								{
										if(DEBUG)
											System.out.println("007B---- In PDXDataProcessor::: l in else"+l);
										FlexObject prodFlexObj1  = (FlexObject)dataRSItr.next();
										newValue = prodFlexObj1.getString("LCSSeasonalChangeLog.newStringValue");
										if(DEBUG)
											System.out.println("007B---- In PDXDataProcessor::: getData	oldValue :: newValue"+oldValue+" :: "+newValue);
										if(newValue.equals(oldValue))
										{
											if(DEBUG)
												System.out.println("008B---- In PDXDataProcessor::: getData skip ");
										}
										else{
											if(DEBUG)
												System.out.println("009---- In PDXDataProcessor::: getData adding i+1 ");
											String prodIDA2A2 =  prodFlexObj1.getString("LCSSeasonalChangeLog.IDA3G4");
											LCSProduct prod2 =(LCSProduct) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSProduct:" + prodIDA2A2);
											
											Date changeLogDate =  sdformat.parse(prodFlexObj1.getString("LCSSeasonalChangeLog.createStampA2"));
											changeLogDate =  adjustHours(changeLogDate, "sub");
											tempDate = sdformat.format(changeLogDate);

											Vector skuSizeUPC = getALLSKUSizeUPC(season,prod2);
											if (skuSizeUPC.size()>0 && l == k){
												  for(int j=0; j < skuSizeUPC.size(); j++) { 
													String skuArticleUPC = skuSizeUPC.get(j).toString();
													//System.out.println("011---- In PDXDataProcessor::: skuArticleUPC "+skuArticleUPC);
													String cwName =skuArticleUPC.substring(0, skuArticleUPC.indexOf("~"));
													String rootArticle = skuArticleUPC.substring(skuArticleUPC.indexOf("~")+1, skuArticleUPC.indexOf("^"));
													String article =skuArticleUPC.substring(skuArticleUPC.indexOf("^")+1, skuArticleUPC.indexOf("|"));
													String upc =skuArticleUPC.substring(skuArticleUPC.indexOf("|")+1);
													System.out.println("011B---- In PDXDataProcessor::: cwName "+cwName+"::::"+article+"::::"+upc);
													user = prodFlexObj1.getString("WTUser.name");
													dataFO1 = new FlexObject();
													dataFO1.put("No","1");
													dataFO1.put("Season Name",season.getName());
													dataFO1.put("Work #",prod2.getValue("agrWorkOrderNoProduct"));
													dataFO1.put("Product Name",objName);
													dataFO1.put("Colorway Name",cwName);
													dataFO1.put("Root Article Number",rootArticle);
													dataFO1.put("Article #",article);
													dataFO1.put("UPC",upc);
													dataFO1.put("Changed Attribute",productDisplayName);
													dataFO1.put("Changed Date",tempDate);
													dataFO1.put("Changed By",prodFlexObj1.getString("WTUser.name"));
													dataFO1.put("Changed From",oldValue);
													dataFO1.put("Changed To",newValue);
													dataObjects.add(dataFO1);
													System.out.println("012---- In PDXDataProcessor::: getData	dataObjects"+dataObjects.size());
												 }
											}else if (l==k){
												Vector skuVector = getALLSKU(season,prod2);
												if (skuVector.size()>0){
													 for(int j=0; j < skuVector.size(); j++) { 
														LCSSKU sku2 = (LCSSKU)skuVector.get(j);
														dataFO1 = new FlexObject();
														dataFO1.put("No","1");
														dataFO1.put("Season Name",season.getName());
														dataFO1.put("Work #",prod2.getValue("agrWorkOrderNoProduct"));
														dataFO1.put("Product Name",objName);
														dataFO1.put("Colorway Name",sku2.getName());
														dataFO1.put("Root Article Number",sku2.getValue("agrArticle"));//agrArticle
														dataFO1.put("Article #","");
														dataFO1.put("UPC","");
														dataFO1.put("Changed Attribute",productDisplayName);
														dataFO1.put("Changed Date",prodFlexObj1.getString("LCSSeasonalChangeLog.createStampA2"));
														dataFO1.put("Changed By",prodFlexObj1.getString("WTUser.name"));
														dataFO1.put("Changed From",oldValue);
														dataFO1.put("Changed To",newValue);
														dataObjects.add(dataFO1);
														System.out.println("012---- In PDXDataProcessor::: getData	dataObjects"+dataObjects.size());
													 }
												}else{
													    /*LCSSesonProductLink link =LCSSeasonQuery.findSeasonProductLink(prod2,season);
														if(link != null) {
															dataFO1 = new FlexObject();
															dataFO1.put("No","1");
															dataFO1.put("Season Name",season.getName());
															dataFO1.put("Work #",prod2.getValue("agrWorkOrderNoProduct"));
															dataFO1.put("Product Name",objName);
															dataFO1.put("Colorway Name","");
															dataFO1.put("Root Article Number","");
															dataFO1.put("Article #","");
															dataFO1.put("UPC","");
															dataFO1.put("Changed Attribute",productDisplayName);
															dataFO1.put("Changed Date",prodFlexObj1.getString("LCSSeasonalChangeLog.createStampA2"));
															dataFO1.put("Changed By",prodFlexObj1.getString("WTUser.name"));
															dataFO1.put("Changed From",oldValue);
															dataFO1.put("Changed To",newValue);
															dataObjects.add(dataFO1);
															System.out.println("012---- In PDXDataProcessor::: getData	dataObjects"+dataObjects.size());
														}*/
												}
											}
										}
								}
							}
						}
					}						
				}
		}
		if(DEBUG)
			System.out.println("013---- In PDXDataProcessor::: getData	dataObjects"+dataObjects.size());
		
		return dataObjects;		
	}
	
	/**
	 * Get SKUData
	 */
	public static Vector getSKUData(String classnamekeyG4Value,Date stDate,Date endDate, Collection getUniqueMasterIDRS, LCSSeason season, String skuDisplayName) throws WTException, IOException, ParseException{
		
		if(DEBUG) 
			System.out.println("001---- In PDXDataProcessor::: getSKUData size"+getUniqueMasterIDRS.size());
		
		Vector dataObjects =new Vector();
		Collection getSKUDataQueryRS =null;
		Iterator uniqueMasterItr =null;
		Iterator dataRSItr =null;
		PreparedQueryStatement getDataQuery = null;
		LCSSKU sku;
		LCSSeasonProductLink skuseasonLink = null;
		FlexObject dataFO1;
		String objName=" ";
		
		if(getUniqueMasterIDRS!=null) {
			uniqueMasterItr = getUniqueMasterIDRS.iterator();
				while(uniqueMasterItr.hasNext()) {
					
					FlexObject Obj  = (FlexObject)uniqueMasterItr.next();
					System.out.println("002---- In PDXDataProcessor::: getSKUData	Obj"+Obj);
					String masterId = Obj.getString("LCSSeasonalChangeLog.IdA3C4");
					System.out.println("002---- In PDXDataProcessor::: getSKUData	masterId"+masterId);
					getDataQuery = new PreparedQueryStatement();
					getDataQuery.appendSelectColumn("WTUser","name");//WTUser.name  as "User Modified",
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","createStampA2");
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","newStringValue");
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","oldStringValue");
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","classnamekeyG4");
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","persistedAttribute");
					getDataQuery.appendSelectColumn("LCSSeasonalChangeLog","IDA3G4");
					getDataQuery.appendJoin( "WTUser","idA2A2","LCSSeasonalChangeLog","idA3F4" );
					getDataQuery.appendFromTable("LCSSeasonalChangeLog");//LCSSeasonalChangeLog, WTUser, LCSProduct 
					getDataQuery.appendFromTable("WTUser");
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",stDate, Criteria.GREATER_THAN_EQUAL));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",endDate, Criteria.LESS_THAN_EQUAL));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "idA3A4","0", Criteria.EQUALS));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","?", "IS NOT NULL"));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","com.lcs.wc.product.LCSSKU", Criteria.EQUALS));
					getDataQuery.appendAndIfNeeded();
					getDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "IdA3C4",masterId, Criteria.EQUALS));				
					getSKUDataQueryRS = LCSQuery.runDirectQuery(getDataQuery).getResults();
					
					if(DEBUG)
							System.out.println("002---- In PDXDataProcessor::: getSKUData	Query::::QueryRS  size"+getDataQuery+"::::"+getSKUDataQueryRS.size());
					
					if(getSKUDataQueryRS!=null) {
						if (getSKUDataQueryRS.size()==1 ){
							String tempDate = "";
							SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							dataRSItr = getSKUDataQueryRS.iterator();
							while(dataRSItr.hasNext()) {								
									FlexObject tempObj  = (FlexObject)dataRSItr.next();
									String user = tempObj.getString("WTUser.name");
									String newValue = tempObj.getString("LCSSeasonalChangeLog.newStringValue");
									String oldValue = tempObj.getString("LCSSeasonalChangeLog.oldStringValue");
									if(DEBUG)
										System.out.println("003---- In PDXDataProcessor::: getSKUData	oldValue :: newValue-->"+oldValue+" :: "+newValue);
									if(newValue.equals(oldValue))
									{
										if(DEBUG) 
											System.out.println("003A---- In PDXDataProcessor::: getSKUData skip ");
										
									}else {
										String skuIDA2A2 =  tempObj.getString("LCSSeasonalChangeLog.IDA3G4");
										LCSSKU sku1 =(LCSSKU) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSSKU:" + skuIDA2A2);
										LCSProduct product1 = sku1.getProduct();
										if(DEBUG)
											System.out.println("004---- In PDXDataProcessor::: getSKUData skuname :::: Product Name-->"+sku1.getName()+"::::"+product1.getName());
										skuseasonLink = LCSSeasonQuery.findSeasonProductLink(sku1,season);
										if(skuseasonLink != null){
											System.out.println("005---- In PDXDataProcessor::: getSKUData");
											Date changeLogDate =  sdformat.parse(tempObj.getString("LCSSeasonalChangeLog.createStampA2"));
											changeLogDate =  adjustHours(changeLogDate, "sub");
											tempDate = sdformat.format(changeLogDate);
											System.out.println("----------------changeLogDate-----4---------tempDate---"+tempDate);
											HashMap<String, String> skuSizeArticleUPCMap = new HashMap<String, String> ();			
											skuSizeArticleUPCMap = AgronStraubDataProcessor.getUPCNumberAndArticle(sku1,season,product1);
											if(skuSizeArticleUPCMap.size()>0){
												for ( Map.Entry<String, String> entry : skuSizeArticleUPCMap.entrySet()) {
														String keyArticle = entry.getKey();
														String valueUPC = entry.getValue();
														//System.out.println("008---- In PDXDataProcessor::: getSKUData keyArticle ::: valueUPC "+keyArticle+":::"+valueUPC);
														String changeAttDisplayName= tempObj.getString("LCSSeasonalChangeLog.persistedAttribute"); //LCSSeasonalChangeLog.persistedAttribute
														dataFO1 = new FlexObject();
														dataFO1.put("No","1");
														dataFO1.put("Season Name",season.getName());
														dataFO1.put("Work #",product1.getValue("agrWorkOrderNoProduct"));
														dataFO1.put("Product Name",product1.getName());
														dataFO1.put("Colorway Name",sku1.getName());
														dataFO1.put("Root Article Number",sku1.getValue("agrArticle"));
														dataFO1.put("Article #",keyArticle);
														dataFO1.put("UPC",valueUPC);
														dataFO1.put("Changed Attribute",skuDisplayName);
														dataFO1.put("Changed Date",tempDate);
														dataFO1.put("Changed By",tempObj.getString("WTUser.name"));
														dataFO1.put("Changed From",oldValue);
														dataFO1.put("Changed To",newValue);
														dataObjects.add(dataFO1);
													}
											 }else{
												String changeAttDisplayName= tempObj.getString("LCSSeasonalChangeLog.persistedAttribute"); //LCSSeasonalChangeLog.persistedAttribute
												dataFO1 = new FlexObject();
												dataFO1.put("No","1");
												dataFO1.put("Season Name",season.getName());
												dataFO1.put("Work #",product1.getValue("agrWorkOrderNoProduct"));
												dataFO1.put("Product Name",product1.getName());
												dataFO1.put("Colorway Name",sku1.getName());
												dataFO1.put("Root Article Number",sku1.getValue("agrArticle"));
												dataFO1.put("Article #","");
												dataFO1.put("UPC","");
												dataFO1.put("Changed Attribute",skuDisplayName);
												dataFO1.put("Changed Date",tempDate);
												dataFO1.put("Changed By",tempObj.getString("WTUser.name"));
												dataFO1.put("Changed From",oldValue);
												dataFO1.put("Changed To",newValue);
												dataObjects.add(dataFO1);
											
												 
											 }												 
										}
										else{
											//System.out.println("006---- In PDXDataProcessor::: getSKUData Skip no SeasonSKULink for "+sku1.getName());
										}	
									}				
							}											
						}else if(getSKUDataQueryRS.size()> 1 ){
							int i=0;
							String newValue = "";
							String oldValue = "";
							String user = "";
							String tempDate = "";
							SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							dataRSItr = getSKUDataQueryRS.iterator();
							while(dataRSItr.hasNext()) {
								
								if(i==0){
									FlexObject skuFlexObj1  = (FlexObject)dataRSItr.next();
									oldValue = skuFlexObj1.getString("LCSSeasonalChangeLog.oldStringValue");
								}else if (i== getSKUDataQueryRS.size()-1)
								{
									FlexObject skuFlexObj1  = (FlexObject)dataRSItr.next();
									newValue = skuFlexObj1.getString("LCSSeasonalChangeLog.newStringValue");
									if(DEBUG)
										System.out.println("007B---- In PDXDataProcessor::: getData	oldValue :: newValue"+oldValue+" :: "+newValue);
									if(newValue.equals(oldValue))
									{
										//if(DEBUG)
											//System.out.println("008B---- In PDXDataProcessor::: getData skip ");
									}
									else{
										if(DEBUG)
											System.out.println("009---- In PDXDataProcessor::: getData adding i+1 ");
										String skuIDA2A2 =  skuFlexObj1.getString("LCSSeasonalChangeLog.IDA3G4");
										LCSSKU sku2 =(LCSSKU) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSSKU:" + skuIDA2A2);
										LCSProduct product2 = sku2.getProduct();
										Date changeLogDate =  sdformat.parse(skuFlexObj1.getString("LCSSeasonalChangeLog.createStampA2"));
										changeLogDate =  adjustHours(changeLogDate, "sub");
										tempDate = sdformat.format(changeLogDate);
										System.out.println("----------------changeLogDate-----5---------tempDate---"+tempDate);
										if(DEBUG)
											System.out.println("004---- In PDXDataProcessor::: getSKUData skuname :::: Product Name-->"+sku2.getName()+"::::"+product2.getName());
										skuseasonLink = LCSSeasonQuery.findSeasonProductLink(sku2,season);
										if(skuseasonLink != null){
											//System.out.println("005---- In PDXDataProcessor::: getSKUData");
											HashMap<String, String> skuSizeArticleUPCMap = new HashMap<String, String> ();			
											skuSizeArticleUPCMap = AgronStraubDataProcessor.getUPCNumberAndArticle(sku2,season,product2);
											if(skuSizeArticleUPCMap.size()>0){
												for ( Map.Entry<String, String> entry : skuSizeArticleUPCMap.entrySet()) {
													String keyArticle = entry.getKey();
													String valueUPC = entry.getValue();
													//System.out.println("008---- In PDXDataProcessor::: getSKUData keyArticle ::: valueUPC "+keyArticle+":::"+valueUPC);	
													String changeAttDisplayName= skuFlexObj1.getString("LCSSeasonalChangeLog.persistedAttribute"); //LCSSeasonalChangeLog.persistedAttribute
													dataFO1 = new FlexObject();
													dataFO1.put("No","1");
													dataFO1.put("Season Name",season.getName());
													dataFO1.put("Work #",product2.getValue("agrWorkOrderNoProduct"));
													dataFO1.put("Product Name",product2.getName());
													dataFO1.put("Colorway Name",sku2.getName());
													dataFO1.put("Root Article Number",sku2.getValue("agrArticle"));
													dataFO1.put("Article #",keyArticle);
													dataFO1.put("UPC",valueUPC);
													dataFO1.put("Changed Attribute",skuDisplayName);
													dataFO1.put("Changed Date",tempDate);
													dataFO1.put("Changed By",skuFlexObj1.getString("WTUser.name"));
													dataFO1.put("Changed From",oldValue);
													dataFO1.put("Changed To",newValue);
													dataObjects.add(dataFO1);
												}
											}else{
												String changeAttDisplayName= skuFlexObj1.getString("LCSSeasonalChangeLog.persistedAttribute"); //LCSSeasonalChangeLog.persistedAttribute
												dataFO1 = new FlexObject();
												dataFO1.put("No","1");
												dataFO1.put("Season Name",season.getName());
												dataFO1.put("Work #",product2.getValue("agrWorkOrderNoProduct"));
												dataFO1.put("Product Name",product2.getName());
												dataFO1.put("Colorway Name",sku2.getName());
												dataFO1.put("Root Article Number",sku2.getValue("agrArticle"));
												dataFO1.put("Article #","");
												dataFO1.put("UPC","");
												dataFO1.put("Changed Attribute",skuDisplayName);
												dataFO1.put("Changed Date",tempDate);
												dataFO1.put("Changed By",skuFlexObj1.getString("WTUser.name"));
												dataFO1.put("Changed From",oldValue);
												dataFO1.put("Changed To",newValue);
												dataObjects.add(dataFO1);
												
												
											}
										}
									}
								}i++;	
							}
						}
					}						
				}
		}
		if(DEBUG)
			System.out.println("013---- In PDXDataProcessor::: getSKUData	dataObjects"+dataObjects.size());
		
		return dataObjects;		
	}
	
	
	/**
	 * Get PSLinkData
	 */
	public static Vector getPSLinkData(String classnamekeyG4Value,Date stDate,Date endDate, Collection getUniqueMasterIDRS, LCSSeason season, String seasonMasterId) throws WTException, IOException, ParseException{
		
		if(DEBUG) 
			System.out.println("001---- In PDXDataProcessor::: getPSLinkData size"+getUniqueMasterIDRS.size());
		
		Vector dataObjects =new Vector();
		FlexObject dataFO2;
		Collection getPSLinkDataQueryRS =null;
		Iterator uniqueMasterItr =null;
		Iterator uniqueSPLinkItr =null;
		Iterator dataRSItr =null;
		
		PreparedQueryStatement getPSLinkDataQuery = null;
		if(getUniqueMasterIDRS!=null) {
			uniqueMasterItr = getUniqueMasterIDRS.iterator();
			while(uniqueMasterItr.hasNext()) {
				FlexObject Obj  = (FlexObject)uniqueMasterItr.next();
				Vector psLinkAttribute =new Vector();
				psLinkAttribute.add("%~MBA|50118534");
				psLinkAttribute.add("%~MBA|50118573");
				psLinkAttribute.add("%~MBA|50118230");
				psLinkAttribute.add("%~MBA|50118514");
				for (int k=0; k < psLinkAttribute.size() ;k++){
					String tempAttr = (String)psLinkAttribute.get(k);
					String masterId = Obj.getString("LCSSeasonalChangeLog.IdA3B4");
					getPSLinkDataQuery = new PreparedQueryStatement();
					getPSLinkDataQuery.appendSelectColumn("WTUser","name");
					getPSLinkDataQuery.appendSelectColumn("LCSSeasonalChangeLog","createStampA2");
					getPSLinkDataQuery.appendSelectColumn("LCSSeasonalChangeLog","NEWDOUBLEVALUE");
					getPSLinkDataQuery.appendSelectColumn("LCSSeasonalChangeLog","OLDDOUBLEVALUE");
					getPSLinkDataQuery.appendSelectColumn("LCSSeasonalChangeLog","classnamekeyG4");
					getPSLinkDataQuery.appendSelectColumn("LCSSeasonalChangeLog","persistedAttribute");
					getPSLinkDataQuery.appendSelectColumn("LCSSeasonalChangeLog","IDA3G4");
					getPSLinkDataQuery.appendJoin( "WTUser","idA2A2","LCSSeasonalChangeLog","idA3F4" );
					getPSLinkDataQuery.appendFromTable("LCSSeasonalChangeLog");//LCSSeasonalChangeLog, WTUser
					getPSLinkDataQuery.appendFromTable("WTUser");
					getPSLinkDataQuery.appendAndIfNeeded();
					getPSLinkDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",stDate, Criteria.GREATER_THAN_EQUAL));
					getPSLinkDataQuery.appendAndIfNeeded();
					getPSLinkDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "createStampA2",endDate, Criteria.LESS_THAN_EQUAL));
					getPSLinkDataQuery.appendAndIfNeeded();			
					getPSLinkDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "idA3A4",seasonMasterId, Criteria.EQUALS));
					getPSLinkDataQuery.appendAndIfNeeded();
					getPSLinkDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","?", "IS NOT NULL"));
					getPSLinkDataQuery.appendAndIfNeeded();
					getPSLinkDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "classnamekeyG4","com.lcs.wc.season.LCSProductSeasonLink", Criteria.EQUALS));
					getPSLinkDataQuery.appendAndIfNeeded();
					getPSLinkDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "IdA3B4",masterId, Criteria.EQUALS));
					getPSLinkDataQuery.appendAndIfNeeded();
					getPSLinkDataQuery.appendCriteria(new Criteria("LCSSeasonalChangeLog", "persistedAttribute",tempAttr, Criteria.LIKE));
					getPSLinkDataQueryRS = LCSQuery.runDirectQuery(getPSLinkDataQuery).getResults();	
					if(DEBUG)
						System.out.println("002---- In PDXDataProcessor::: getPSLinkData	Query::::QueryRS  size"+getPSLinkDataQuery+"::::"+getPSLinkDataQueryRS.size());
					
					if(getPSLinkDataQueryRS !=null){
						if(getPSLinkDataQueryRS.size() == 1){
							String tempDate = "";
							SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							uniqueSPLinkItr = getPSLinkDataQueryRS.iterator();
							while (uniqueSPLinkItr.hasNext() ){
								FlexObject tempPSLinkObj  = (FlexObject)uniqueSPLinkItr.next();
								String splID =  (String)tempPSLinkObj.getString("LCSSeasonalChangeLog.IDA3G4");
								String persistedAttribute =  (String)tempPSLinkObj.getString("LCSSeasonalChangeLog.persistedAttribute");
								System.out.println("------------splID-------1-------IDA2A2----------------"+splID);
								String persistedAttributeID = persistedAttribute.substring(persistedAttribute.lastIndexOf('|'));
								//System.out.println("------------persistedAttributeID------------------------------"+persistedAttributeID);
								LCSSeasonProductLink spl =(LCSSeasonProductLink)LCSQuery.findObjectById("OR:com.lcs.wc.season.LCSProductSeasonLink:" + splID);
								//System.out.println("------------splID-------1-------getCarriedOverFrom----------------"+spl.getCarriedOverFrom());
								//System.out.println("------------splID-------1-------getEffectSequence----------------"+spl.getEffectSequence());
								LCSProduct prodObj = SeasonProductLocator.getProductARev(spl);
								Date changeLogDate =  sdformat.parse(tempPSLinkObj.getString("LCSSeasonalChangeLog.createStampA2"));
								changeLogDate =  adjustHours(changeLogDate, "sub");
								tempDate = sdformat.format(changeLogDate);
								//System.out.println("------------spl------1------------------------"+prodObj.getName());
								Vector skuSizeUPC2 =getALLSKUSizeUPC(season,prodObj);
								//System.out.println("------------skuSizeUPC2------------------size-----1-------"+skuSizeUPC2.size());								
								if ( spl.getCarriedOverFrom() == null ){	
									if (skuSizeUPC2.size()>0 ){	
										for(int j=0; j < skuSizeUPC2.size(); j++) { 	
											String skuArticleUPC = skuSizeUPC2.get(j).toString();
											String cwName =skuArticleUPC.substring(0, skuArticleUPC.indexOf("~"));
											String rootArticle = skuArticleUPC.substring(skuArticleUPC.indexOf("~")+1, skuArticleUPC.indexOf("^"));
											String article =skuArticleUPC.substring(skuArticleUPC.indexOf("^")+1, skuArticleUPC.indexOf("|"));
											String upc =skuArticleUPC.substring(skuArticleUPC.indexOf("|")+1);
											System.out.println("011B---- In PDXDataProcessor:::pslink::: cwName "+cwName+"::::"+article+"::::"+upc);
											dataFO2 = new FlexObject();
											dataFO2.put("No","1");
											dataFO2.put("Season Name",season.getName());
											dataFO2.put("Work #",prodObj.getValue("agrWorkOrderNoProduct"));
											dataFO2.put("Product Name",prodObj.getName());
											dataFO2.put("Colorway Name",cwName);
											dataFO2.put("Root Article Number",rootArticle);
											dataFO2.put("Article #",article);
											dataFO2.put("UPC",upc);
											if (persistedAttribute.contains("50118534"))//50118534
												dataFO2.put("Changed Attribute","Retail");
											else if (persistedAttribute.contains("50118573"))
												dataFO2.put("Changed Attribute","Wholesale");
											else if (persistedAttribute.contains("50118230"))
												dataFO2.put("Changed Attribute","Override Retail Price");
											else if (persistedAttribute.contains("50118514"))
												dataFO2.put("Changed Attribute","Override Wholesale Price");
											else
												dataFO2.put("Changed Attribute","");
											dataFO2.put("Changed Date",tempDate);
											dataFO2.put("Changed By",tempPSLinkObj.getString("WTUser.name"));
											dataFO2.put("Changed From",tempPSLinkObj.getString("LCSSeasonalChangeLog.OLDDOUBLEVALUE"));
											dataFO2.put("Changed To",tempPSLinkObj.getString("LCSSeasonalChangeLog.NEWDOUBLEVALUE"));									
											dataObjects.add(dataFO2);
										}
									}
								}								
							}
						}else if (getPSLinkDataQueryRS.size() > 1){
							int i=0;
							String newValue ="";
							String oldValue = "";
							String tempDate = "";
							SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							uniqueSPLinkItr = getPSLinkDataQueryRS.iterator();
							while (uniqueSPLinkItr.hasNext() ){	
									FlexObject tempPSLinkObj  = (FlexObject)uniqueSPLinkItr.next();
									String splID =  (String)tempPSLinkObj.getString("LCSSeasonalChangeLog.IDA3G4");
									//System.out.println("------------splID-------2-------IDA2A2----------------"+splID);
									LCSSeasonProductLink spl =(LCSSeasonProductLink)LCSQuery.findObjectById("OR:com.lcs.wc.season.LCSProductSeasonLink:" + splID);
									//System.out.println("------------splID------->1-------getCarriedOverFrom----------------"+spl.getCarriedOverFrom());
									//System.out.println("------------splID------->1-------getEffectSequence----------------"+spl.getEffectSequence());
									
									if (i==0 && spl.getCarriedOverFrom() == null ){
										//FlexObject tempPSLinkObj  = (FlexObject)uniqueSPLinkItr.next();
										oldValue = tempPSLinkObj.getString("LCSSeasonalChangeLog.OLDDOUBLEVALUE");//tempPSLinkObj.getString("LCSSeasonalChangeLog.OLDDOUBLEVALUE")
									}else if (i==1 && spl.getCarriedOverFrom() != null ){
										oldValue = tempPSLinkObj.getString("LCSSeasonalChangeLog.OLDDOUBLEVALUE");
										if (i== getPSLinkDataQueryRS.size()-1 ){
											String persistedAttribute =  (String)tempPSLinkObj.getString("LCSSeasonalChangeLog.persistedAttribute");
										newValue = tempPSLinkObj.getString("LCSSeasonalChangeLog.NEWDOUBLEVALUE");
										String persistedAttributeID = persistedAttribute.substring(persistedAttribute.lastIndexOf('|'));
										System.out.println("------------persistedAttributeID------------------------------"+persistedAttributeID);
										LCSProduct prodObj = SeasonProductLocator.getProductARev(spl);
										Date changeLogDate =  sdformat.parse(tempPSLinkObj.getString("LCSSeasonalChangeLog.createStampA2"));
										changeLogDate =  adjustHours(changeLogDate, "sub");
										tempDate = sdformat.format(changeLogDate);
										//System.out.println("------------spl------2------------------------"+prodObj.getName());
										Vector skuSizeUPC2 =getALLSKUSizeUPC(season,prodObj);
										//System.out.println("------------skuSizeUPC2------------------size-----2-------"+skuSizeUPC2.size());
										if (skuSizeUPC2.size()>0){
											for(int j=0; j < skuSizeUPC2.size(); j++) { 
												String skuArticleUPC = skuSizeUPC2.get(j).toString();
												//System.out.println("011---- In PDXDataProcessor::: skuArticleUPC:::pslink "+skuArticleUPC);
												String cwName =skuArticleUPC.substring(0, skuArticleUPC.indexOf("~"));
												String rootArticle = skuArticleUPC.substring(skuArticleUPC.indexOf("~")+1, skuArticleUPC.indexOf("^"));
												String article =skuArticleUPC.substring(skuArticleUPC.indexOf("^")+1, skuArticleUPC.indexOf("|"));
												String upc =skuArticleUPC.substring(skuArticleUPC.indexOf("|")+1);
												System.out.println("011B---- In PDXDataProcessor:::pslink::: cwName "+cwName+"::::"+article+"::::"+upc);
												dataFO2 = new FlexObject();
												dataFO2.put("No","1");
												dataFO2.put("Season Name",season.getName());
												dataFO2.put("Work #",prodObj.getValue("agrWorkOrderNoProduct"));
												dataFO2.put("Product Name",prodObj.getName());
												dataFO2.put("Colorway Name",cwName);
												dataFO2.put("Root Article Number",rootArticle);
												dataFO2.put("Article #",article);
												dataFO2.put("UPC",upc);
												if (persistedAttribute.contains("50118534"))//50118534
													dataFO2.put("Changed Attribute","Retail");
												else if (persistedAttribute.contains("50118573"))
													dataFO2.put("Changed Attribute","Wholesale");
												else if (persistedAttribute.contains("50118230"))
													dataFO2.put("Changed Attribute","Override Retail Price");
												else if (persistedAttribute.contains("50118514"))
													dataFO2.put("Changed Attribute","Override Wholesale Price");
												else
													dataFO2.put("Changed Attribute","");
												dataFO2.put("Changed Date",tempDate);
												dataFO2.put("Changed By",tempPSLinkObj.getString("WTUser.name"));
												dataFO2.put("Changed From",oldValue);
												dataFO2.put("Changed To",newValue);
												//System.out.println("------------OLDDOUBLEVALUE--j--oldValue::::newValue::::createStampA2----->"+j+":::"+oldValue+":::"+newValue+":::"+tempPSLinkObj.getString("LCSSeasonalChangeLog.createStampA2"));								
												dataObjects.add(dataFO2);
											}
										}
										}
									}else if (i== getPSLinkDataQueryRS.size()-1 ){
										//FlexObject tempPSLinkObj  = (FlexObject)uniqueSPLinkItr.next();
										String persistedAttribute =  (String)tempPSLinkObj.getString("LCSSeasonalChangeLog.persistedAttribute");
										newValue = tempPSLinkObj.getString("LCSSeasonalChangeLog.NEWDOUBLEVALUE");
										String persistedAttributeID = persistedAttribute.substring(persistedAttribute.lastIndexOf('|'));
										System.out.println("------------persistedAttributeID------------------------------"+persistedAttributeID);
										LCSProduct prodObj = SeasonProductLocator.getProductARev(spl);
										Date changeLogDate =  sdformat.parse(tempPSLinkObj.getString("LCSSeasonalChangeLog.createStampA2"));
										changeLogDate =  adjustHours(changeLogDate, "sub");
										tempDate = sdformat.format(changeLogDate);
										//System.out.println("------------spl------2------------------------"+prodObj.getName());
										Vector skuSizeUPC2 =getALLSKUSizeUPC(season,prodObj);
										//System.out.println("------------skuSizeUPC2------------------size-----2-------"+skuSizeUPC2.size());
										if (skuSizeUPC2.size()>0){
											for(int j=0; j < skuSizeUPC2.size(); j++) { 
												String skuArticleUPC = skuSizeUPC2.get(j).toString();
												//System.out.println("011---- In PDXDataProcessor::: skuArticleUPC:::pslink "+skuArticleUPC);
												String cwName =skuArticleUPC.substring(0, skuArticleUPC.indexOf("~"));
												String rootArticle = skuArticleUPC.substring(skuArticleUPC.indexOf("~")+1, skuArticleUPC.indexOf("^"));
												String article =skuArticleUPC.substring(skuArticleUPC.indexOf("^")+1, skuArticleUPC.indexOf("|"));
												String upc =skuArticleUPC.substring(skuArticleUPC.indexOf("|")+1);
												System.out.println("011B---- In PDXDataProcessor:::pslink::: cwName "+cwName+"::::"+article+"::::"+upc);
												dataFO2 = new FlexObject();
												dataFO2.put("No","1");
												dataFO2.put("Season Name",season.getName());
												dataFO2.put("Work #",prodObj.getValue("agrWorkOrderNoProduct"));
												dataFO2.put("Product Name",prodObj.getName());
												dataFO2.put("Colorway Name",cwName);
												dataFO2.put("Root Article Number",rootArticle);
												dataFO2.put("Article #",article);
												dataFO2.put("UPC",upc);
												if (persistedAttribute.contains("50118534"))//50118534
													dataFO2.put("Changed Attribute","Retail");
												else if (persistedAttribute.contains("50118573"))
													dataFO2.put("Changed Attribute","Wholesale");
												else if (persistedAttribute.contains("50118230"))
													dataFO2.put("Changed Attribute","Override Retail Price");
												else if (persistedAttribute.contains("50118514"))
													dataFO2.put("Changed Attribute","Override Wholesale Price");
												else
													dataFO2.put("Changed Attribute","");
												dataFO2.put("Changed Date",tempDate);
												dataFO2.put("Changed By",tempPSLinkObj.getString("WTUser.name"));
												dataFO2.put("Changed From",oldValue);
												dataFO2.put("Changed To",newValue);
												//System.out.println("------------OLDDOUBLEVALUE--j--oldValue::::newValue::::createStampA2----->"+j+":::"+oldValue+":::"+newValue+":::"+tempPSLinkObj.getString("LCSSeasonalChangeLog.createStampA2"));								
												dataObjects.add(dataFO2);
											}
										}
									}else{
										//tempPSLinkObj  = (FlexObject)uniqueSPLinkItr.next();	
									}i++;
							}	
						}
					}
				}
			}
		}
		if(DEBUG)
			System.out.println("------------dataObjects------------------size------------"+dataObjects.size());	
		return dataObjects;		
	}
	
	public static String getDocName() {
		SimpleDateFormat sdfDestination = new SimpleDateFormat(REPORT_ID_DATE_FORMAT);
		String documentName= "PDXReport"+"_"+sdfDestination.format(new Date());
		return documentName;
	}
	
	public static void configureColumnList() {
		log.info("configureColumnList>>>>>");
		List<String> integerColumns=new ArrayList(Arrays.asList(NUMERIC_COLUMNS_KEYS.toUpperCase().trim().split(",")));
		List<String> columns=new ArrayList(Arrays.asList(REPORT_COLUMNS.toUpperCase().trim().split(",")));
		log.info("columns Array>>>>>"+columns);
		columns.forEach(column->{
			String tableColumArray[]=column.split(":");
			String columnKey=tableColumArray[0].trim().toUpperCase();
			String columnDispalyValue=columnKey;
			if(tableColumArray.length>1 && FormatHelper.hasContent(tableColumArray[1]) ){
				columnDispalyValue=tableColumArray[1];
			}
			TableColumn tColumn=new TableColumn(columnKey,columnDispalyValue);
			if(columnKey.toUpperCase().indexOf("THUMBNAIL")>-1) {
				tColumn.setImage(true);
			}
           if(integerColumns.contains(columnKey)) {
        	   tColumn.setAttributeType("NUMERIC");
			}else {
				 tColumn.setAttributeType("STRING");
			}
			tableColumns.add(tColumn);
		});	
	}
	
	public static Vector getALLSKUSizeUPC(LCSSeason season,LCSProduct product) throws WTException{
		
		Vector skuSizeUPC =new Vector();
		Collection getSKUDataQueryRS = null;
		Iterator dataSKURSItr =null;
		String addToVector ="";
		
		PreparedQueryStatement getSKUDataQuery = getSKUDataForSeasonAndProductQuery ((LCSSeasonMaster)season.getMaster(),(WTPartMaster)product.getMaster());
		getSKUDataQueryRS = LCSQuery.runDirectQuery(getSKUDataQuery).getResults();
		 if(DEBUG)
				System.out.println("008---- In PDXDataProcessor::: getALLSKUSizeUPC getSKUData "+getSKUDataQueryRS.size());
		 if (getSKUDataQueryRS.size()>0) {
			
			 dataSKURSItr = getSKUDataQueryRS.iterator();
			 while(dataSKURSItr.hasNext()) {
				 FlexObject skuObj  = (FlexObject)dataSKURSItr.next();
				 String skuARevID = "VR:com.lcs.wc.product.LCSSKU:"+ (String)skuObj.getString("LCSSeasonProductLink.SKUAREVID");
				 LCSSKU sku = (LCSSKU)LCSQuery.findObjectById(skuARevID);
				 if(DEBUG)
					System.out.println("008---- In PDXDataProcessor::: getALLSKUSizeUPC sku skuName -->"+sku.getValue("skuName"));
				 HashMap<String, String> skuSizeUPCMap = new HashMap<String, String> ();			
			     skuSizeUPCMap = AgronStraubDataProcessor.getUPCNumberAndArticle(sku,season,product);
				 System.out.println("008---- In PDXDataProcessor::: getALLSKUSizeUPC skuSizeUPCMap "+skuSizeUPCMap.size());
				 //System.out.println(Collections.singletonList(skuSizeUPCMap));
				 String keyArticle ="";
				 String valueUPC = "";
				 if(skuSizeUPCMap.size()>0){
					for ( Map.Entry<String, String> entry : skuSizeUPCMap.entrySet()) {
							keyArticle = entry.getKey();
							valueUPC = entry.getValue();
							 // Assumption these ~^| symbol is not used in Sku Name , Article# & UPC No
							 addToVector = sku.getValue("skuName")+"~"+sku.getValue("agrArticle")+"^"+keyArticle+"|"+valueUPC;
							 if(DEBUG)
								System.out.println("008---- In PDXDataProcessor::: getALLSKUSizeUPC keyArticle:::::valueUPC "+keyArticle+":::::"+valueUPC);
							 skuSizeUPC.add(addToVector);
						}
				 }
			}
		}
		return skuSizeUPC;
	}
	
	public static Vector getALLSKU(LCSSeason season,LCSProduct product) throws WTException{
		
		Vector skuVector =new Vector();
		Collection getSKUDataQueryRS = null;
		Iterator dataSKURSItr =null;
		String addToVector ="";
		PreparedQueryStatement getSKUDataQuery = getSKUDataForSeasonAndProductQuery ((LCSSeasonMaster)season.getMaster(),(WTPartMaster)product.getMaster());
		getSKUDataQueryRS = LCSQuery.runDirectQuery(getSKUDataQuery).getResults();
		if(DEBUG)
				System.out.println("008---- In PDXDataProcessor::: getALLSKUSizeUPC getSKUData "+getSKUDataQueryRS.size());
		if (getSKUDataQueryRS.size()>0) {		
			dataSKURSItr = getSKUDataQueryRS.iterator();
			while(dataSKURSItr.hasNext()) {
				 FlexObject skuObj  = (FlexObject)dataSKURSItr.next();
				 String skuARevID = "VR:com.lcs.wc.product.LCSSKU:"+ (String)skuObj.getString("LCSSeasonProductLink.SKUAREVID");
				 //skuARevId = "VR:com.lcs.wc.product.LCSSKU:"+ (String) flexObj.getString("LCSSeasonProductLink.SKUAREVID");
				 LCSSKU sku = (LCSSKU)LCSQuery.findObjectById(skuARevID);
				 System.out.println("008---- In PDXDataProcessor::: getALLSKUSizeUPC sku skuName ::: -->"+sku.getValue("skuName"));
				 skuVector.add(sku);
				 
			}
		}
		return skuVector;
	}
	
	public static PreparedQueryStatement getSKUDataForSeasonAndProductQuery(LCSSeasonMaster seasonMaster, WTPartMaster productMaster) throws WTException
     {

         PreparedQueryStatement getSKUDataStatement = new PreparedQueryStatement();
         getSKUDataStatement.appendFromTable("skuseasonlink","LCSSeasonProductLink");
         getSKUDataStatement.appendSelectColumn(new QueryColumn("LCSSeasonProductLink", "IDA2A2"));
         getSKUDataStatement.appendSelectColumn(new QueryColumn("LCSSeasonProductLink", "SKUAREVID"));
         getSKUDataStatement.appendSelectColumn(new QueryColumn("LCSSeasonProductLink", "PRODUCTSEASONREVID"));
         getSKUDataStatement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", "IDA3B5"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(seasonMaster)));
         getSKUDataStatement.appendAndIfNeeded();
         getSKUDataStatement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", "productMasterId"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(productMaster)));
         getSKUDataStatement.appendAndIfNeeded();
         getSKUDataStatement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", "seasonRemoved"), "0", "="));
         getSKUDataStatement.appendAndIfNeeded();
         getSKUDataStatement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", "seasonLinkType"), "SKU", "="));
         getSKUDataStatement.appendAnd();
         getSKUDataStatement.appendCriteria(new Criteria("LCSSeasonProductLink", "EFFECTLATEST", "1", "="));
         return getSKUDataStatement;
     }
	 
	 public static Date adjustHours(Date date, String change) throws WTException
     {
		Timestamp timestamp = new Timestamp(date.getTime());
		System.out.println("-------timestamp---0--------"+timestamp);
 
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp.getTime());
			
		cal.setTimeInMillis(timestamp.getTime());
		if(change.equals("sub"))
			cal.add(Calendar.HOUR, -7);
		else
			cal.add(Calendar.HOUR, 7);
		timestamp = new Timestamp(cal.getTime().getTime());
		System.out.println("-------timestamp---1--------"+timestamp);
			
		date = new java.sql.Date(timestamp.getTime()); 
		System.out.println("-------stDate---1--------"+date);
        
         return date;
     }
}
