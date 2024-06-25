package com.agron.wc.load;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.product.AgronProductQuery;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lcs.wc.client.web.CSVGenerator;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.query.LSQModule;
import com.lcs.wc.season.query.LineSheetQuery;
import com.lcs.wc.season.query.LineSheetQueryOptions;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetClientModel;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FlexObjectUtil;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;

public class AgronFOBLoader {

	private static final String PRODUCT_ADIDAS = "Product\\Adidas";
	private static final String SUCCESS = "Success";
	private static final String MOA_DELIM = "|~*~|";
	private static final String LCSPRODUCTCOSTSHEET_APPLICABLESIZES = "LCSPRODUCTCOSTSHEET.APPLICABLESIZES";
	private static final String FAILED = "Failed";
	private static final String AGR_ARTICLE_NUM = "agrArticle";
	private static final String AGR_BASE_FOB = "agrBaseFOB";
	private String loadType = null;
	private WTUser user = null;
	public static String dataLogFileName; 
	public static String logFileLocation = null;
	public static String COMMA_DELIMITER = null;
	public static String LOGGER_DELIMITER = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;
	public static BufferedWriter dataLogWriter = null;
	
	public static final Logger logger = LogManager.getLogger(AgronFOBLoader.class);
	
	static{
		try{
			LOGGER_DELIMITER = LCSProperties.get("com.agron.wc.utilities.load.Data.loggerDelimiter");
			COMMA_DELIMITER =  LCSProperties.get("com.agron.wc.utilities.load.Data.commaDelimiter");
			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			logFileLocation = wtHomePath + 
					FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.load.GenerateExcelLogfilePath"));
			new File(logFileLocation).mkdirs();
		}catch(IOException io){
			io.printStackTrace();
		}

	}
	
	public AgronFOBLoader(WTUser userObj, String dataFile, String dataLoadType) {
		this.loadType = dataLoadType;
		this.user = userObj;
	}

	public void loadData(ArrayList keyList, ArrayList dataList, String Season, boolean ignoreBlankValue) throws WTException,
	WTPropertyVetoException {
		String dataLogFile = null;
		try {
			dataLogFile = setLogFile(loadType);
		} catch (WTException wt) {
			wt.printStackTrace();
			throw new WTException(wt.getLocalizedMessage());
		}
		String  Seasonname = findSeasonName(Season);
		LCSSeason season = findSeason(Seasonname);
		logger.debug("Season Name "+season.getName());
		logger.info("***********Agron FOB Updater Started...****************************");
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
	 	keyList.remove(" ");
	 	
	 	loggerRowData= getFormattedLogEntryRow(keyList);
	 	logger.info("***********  loggerRowData data ::"+loggerRowData.toString());
		keyList.remove("Status");
		keyList.remove("Error Code");
		logger.info("***********  loggerRowData after ::"+loggerRowData.toString());
		appendRowLoadStatus(loggerRowData, "", "");
		logger.info("***********  dataList data ::"+dataList.toString());
		Iterator dataListItr = dataList.iterator();

		int successCount=0;
		int failedCount=0;
		//boolean run = true;
		
		Collection<String> attList = new Vector();
		attList.add("Product.agrWorkOrderNoProduct");
		
		//*******NEW LSQ*******
        LineSheetQuery lsqn = null;
        LineSheetQueryOptions options = new LineSheetQueryOptions();
		options.setSeason(season);
		
		 //options.skus = true;
         options.costing = true;
         options.skuCosting = false;
         options.setIncludeSourcing(true);
         options.secondarySourcing = true;
         options.primaryCostOnly = false;
         options.whatifCosts = false;
         //options.usedAttKeys = attList;
         options.includeRemoved = false;
         options.includePlaceHolders = false;
         //options.includeCostSpec = options.costing;
         options.includeCostSpec = false;
         //options.setCostSpec(null);
         options.statement = null;
         //options.excludeInActiveSKUSource = true;
         //options.includedProductsWithFilteredColorways = false;
         
         FlexType flexType_SKUP = FlexTypeCache.getFlexTypeFromPath(PRODUCT_ADIDAS);
         
		while(dataListItr.hasNext()){
			tempRow = (ArrayList) dataListItr.next();
			loggerRowData = getFormattedLogEntryRow(tempRow);
			logger.info("***********  loggerRowData while ::"+loggerRowData.toString());
			
			String WorkNo=tempRow.get(1).toString();
			String ArticleNo=tempRow.get(2).toString();
			String ProductName=tempRow.get(3).toString();
			//String Colorwayname=tempRow.get(4).toString();
			String colorwayletter=tempRow.get(4).toString();
			String Costsheetname=tempRow.get(5).toString();
			String sourceName=tempRow.get(6).toString();
			String specification =tempRow.get(7).toString();
			String costsheetSize=tempRow.get(8).toString();
			String CostsheetBaseFOB=tempRow.get(9).toString();

			
			
			String seasonMasterId = FormatHelper.getNumericObjectIdFromObject(season.getMaster());
			logger.debug("Season Master ID "+seasonMasterId);
			LCSProduct product = findProductByWorkNo(WorkNo);
			LCSSourcingConfig sourceObj = findSourceByName(sourceName, product, season);
			if(sourceObj==null){
				logger.debug("ERROR : Source Object not found For Source Name "+sourceName);
				appendRowLoadStatus(loggerRowData, FAILED, "Source Object not found");
				failedCount++;
				continue;
			}

			
			Map<String, Object> criteria = new HashMap<String, Object>();
			criteria.put(flexType_SKUP.getAttribute(AGR_ARTICLE_NUM).getSearchCriteriaIndex(), ArticleNo);

			//SearchResults csObjects =  findCostsheet( WorkNo,	 ArticleNo,  ProductName,  Colorwayname, 	 
			//colorwayletter, 	 Seasonname, 	 Costsheetname); OLD CODE
			//SearchResults csObjects =  findCostsheetBySize( WorkNo,	 ArticleNo,  ProductName,  null, 	 colorwayletter, 	
					//Seasonname, 	 Costsheetname, costsheetSize, seasonMasterId, sourceMasterId);
			//PreparedQueryStatement pqs = LCSCostSheetQuery.getCostSheetForConfigQuery(null, sourceObj.getMaster(), 
			//season.getMaster(), false);
			//logger.debug("pqs  :"+pqs.toString());
			
			
			
            //*********************
            options.setProduct(product);
            options.setConfig(sourceObj);
            options.criteria = criteria;
           
            //*********************
            
            
            Injector injector = Guice.createInjector(new LSQModule());
            lsqn = injector.getInstance(com.lcs.wc.season.query.LineSheetQuery.class);
            
            
            
    		//LOGGER.info("Linesheet Query Options to string >>>:"+options.toString());
            logger.info("Linesheet Query >>>:"+lsqn.getLineSheetQuery(options));
    		Collection<FlexObject> results = lsqn.getLineSheetResults(options);
    		logger.info("TOTAL Season ROWS Count>>>"+results.size());
    		logger.info("TOTAL Season ROWS >>>"+results.toString());
    		Collection<FlexObject> finalResults  = new Vector<FlexObject>();
    		if(FormatHelper.hasContent(costsheetSize)) {
    			finalResults = FlexObjectUtil.filter(results, LCSPRODUCTCOSTSHEET_APPLICABLESIZES, costsheetSize+MOA_DELIM);
			
    			if(finalResults!=null && finalResults.size() ==0)
    				finalResults = FlexObjectUtil.filter(results, LCSPRODUCTCOSTSHEET_APPLICABLESIZES, costsheetSize);
    		
    		}
    		logger.info("TOTAL Final Season ROWS >>>>"+finalResults.toString());

			LCSProductCostSheet csObject = null;
			if(finalResults.size() > 0) {
				FlexObject flexObj = finalResults.iterator().next();
				logger.debug("Flex Obj :"+flexObj.toString());
				String branchId = flexObj.getString("LCSPRODUCTCOSTSHEET.BRANCHIDITERATIONINFO");
				if(!FormatHelper.hasContent(branchId) ){
					continue;
				}
				csObject = (LCSProductCostSheet) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSProductCostSheet:" + branchId);
				logger.debug("LCSCostSheet NAME :: "+csObject.getName());
				if(FormatHelper.hasContentAllowZero(CostsheetBaseFOB) && csObject.getValue(AGR_BASE_FOB)!=null ) {
					Double agrBaseFOB = (Double) csObject.getValue(AGR_BASE_FOB);
					if(agrBaseFOB.toString().equalsIgnoreCase(CostsheetBaseFOB))
					{
						logger.debug("LCSCostSheet Base FOB is same :: "+agrBaseFOB);
						successCount++;
						appendRowLoadStatus(loggerRowData, SUCCESS, "Base FOB is same");
						continue;
					}
				}
				
				
				try{

					csObject.setValue(AGR_BASE_FOB, Float.valueOf(CostsheetBaseFOB));
				
					//csObject = new LCSCostSheetLogic().saveCostSheet(csObject);
					csObject = (LCSProductCostSheet) new LCSCostSheetLogic().saveCostSheet(csObject);
				
					successCount++;
					appendRowLoadStatus(loggerRowData, SUCCESS, "Updated Successfully");
					if (VersionHelper.isCheckedOut(csObject)) {
						//VersionHelper.checkin(csObject);
					}
				}catch(Exception e){
					failedCount++;
					appendRowLoadStatus(loggerRowData, FAILED, e.getMessage());
					if (VersionHelper.isCheckedOut(csObject)) {
						//VersionHelper.checkin(csObject);
					}
				}
			}
			else{
				logger.debug("ERROR : Costsheet Object not found for Costsheet "+Costsheetname);
				//appendRowLoadStatus(loggerRowData, "Failed", "Costsheet Object not found");
				//failedCount++;
				 
				
				if(product==null){
					logger.debug("ERROR : PRODUCT Object not found For Work "+WorkNo);
					appendRowLoadStatus(loggerRowData, FAILED, "PRODUCT Object not found");
					failedCount++;
					continue;
				}

				LCSSKU skuObject = findSKUByLetter(WorkNo, colorwayletter, product);
				if(skuObject==null){
					logger.error("ERROR : SKU Object not found For Colorway Letter "+colorwayletter);
					appendRowLoadStatus(loggerRowData, FAILED, "SKU Object not found");
					failedCount++;
					continue;
				}

				LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(product, season);
				if(spl==null){
					logger.debug("ERROR : Season Product Link Object not found For Product Work No "+WorkNo);
					appendRowLoadStatus(loggerRowData, FAILED, "Season Product Link Object not found");
					failedCount++;
					continue;
				}
				logger.debug("Season Product Link "+spl);
				ProductSizeCategory psc = getBaseSizeCategory(product, season);
				if(psc==null){
					logger.debug("ERROR : Product Size Category Object not found For Cost Sheet Size "+costsheetSize);
					appendRowLoadStatus(loggerRowData, FAILED, "Product Size Category Object not found");
					failedCount++;
					continue;
				}
				FlexSpecification specObj = findSpec(specification, product, season, sourceObj);
				if(specObj==null){
					logger.debug("ERROR : Specification Object not found For Specification Name "+specification);
					appendRowLoadStatus(loggerRowData, FAILED, "Specification Object not found");
					failedCount++;
					continue;
				}
				LCSCostSheet csObj = createCostsheetForSizeAndSKU(costsheetSize, CostsheetBaseFOB, product, skuObject, season, sourceObj,
						spl, psc, specObj);
				logger.debug("Cost Sheet Name >>>>"+csObj.getName());
				successCount++;
				appendRowLoadStatus(loggerRowData, SUCCESS, "Created Successfully");
				//break;
			}

		}
		notifyPLMUser(true, dataList, successCount, failedCount);
		
		logger.info("******Data Load Summary**********");
		logger.info("****SUCCESS Row***::::"+successCount);
		logger.info("****FAILED Row****::::"+failedCount);
		logger.info("###################################");
		logger.info("Excle data loader logs location::"+dataLogFileName);
	}

	/**
	 * @param costsheetSize
	 * @param CostsheetBaseFOB
	 * @param product
	 * @param skuObject
	 * @param season
	 * @param sourceObj
	 * @param spl
	 * @param psc
	 * @param specObj
	 * @return csObj
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	private LCSCostSheet createCostsheetForSizeAndSKU(String costsheetSize, String CostsheetBaseFOB, LCSProduct product,
			LCSSKU skuObject, LCSSeason season, LCSSourcingConfig sourceObj, LCSSeasonProductLink spl,
			ProductSizeCategory psc, FlexSpecification specObj) throws WTException, WTPropertyVetoException {
		LCSCostSheetLogic csl = new LCSCostSheetLogic();
		
		LCSCostSheet csObj = csl.createNewCostSheet(sourceObj, product.getPlaceholderMaster(), spl, season, false, "CS");
		 
		LCSCostSheetClientModel csm = new LCSCostSheetClientModel();
		csm.load(FormatHelper.getObjectId(csObj));
		if(specObj!=null)
		csm.setSpecificationMaster(specObj.getMaster());
		csm.setColorwayGroup(FormatHelper.getVersionId(skuObject)+MOAHelper.DELIM);
		csm.setRepresentativeColorway(FormatHelper.getVersionId(skuObject));
		csm.setSizeCategoryGroup(FormatHelper.getObjectId(psc)+MOAHelper.DELIM);
		csm.setSizeGroup(costsheetSize+MOAHelper.DELIM);
		//csm.setRepresentativeSize(FormatHelper.getObjectId(psc));
		csm.setValue(AGR_BASE_FOB, Float.valueOf(CostsheetBaseFOB));
		csm.save();
		
		logger.debug("Cost Sheet >>>>"+csObj);
		
		return csObj;
	}

	/**
	 * @param specification
	 * @param product
	 * @param season
	 * @param sourceObj
	 * @return specObj
	 * @throws WTException
	 */
	private FlexSpecification findSpec(String specification, LCSProduct product, LCSSeason season,
			LCSSourcingConfig sourceObj) throws WTException {
		FlexSpecification specObj = null ;
		SearchResults specResults = FlexSpecQuery.findExistingSpecs(product, season, sourceObj, true);
		logger.debug("specResults :: "+specResults.toString());
		
		Iterator specIter = specResults.getResults().iterator();
		if(specIter.hasNext()) { 
			FlexObject fObj = (FlexObject) specIter.next();

			logger.debug("SPEC fObj :: "+fObj);
			String branchId = fObj.getString("FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
			specObj = (FlexSpecification) LCSQuery.findObjectById("VR:com.lcs.wc.specification.FlexSpecification:" + branchId);
			logger.debug("SPEC Obj :: "+specObj);

		} else{
			logger.debug("ERROR : SPEC Object not found For Work "+specification);
			//appendRowLoadStatus(loggerRowData, "Failed", "SPEC Object not found");
			//failedCount++; 
			//continue; 
		}
		return specObj;
	}

	/**
	 * @param sourceName
	 * @param product
	 * @param season
	 * @return sourceObj
	 * @throws WTException
	 */
	private LCSSourcingConfig findSourceByName(String sourceName, LCSProduct product, LCSSeason season)
			throws WTException {
		LCSSourcingConfig sourceObj = null ;

		FlexType sourcingConfigType = FlexTypeCache.getFlexTypeFromPath("Sourcing Configuration");
		FlexTypeAttribute nameAtt = sourcingConfigType.getAttribute("name");
		Map<String, String> cMap = new HashMap<String, String>();
		cMap.put("displayAttribute", "name");
		cMap.put("quickSearchCriteria", sourceName);
		cMap.put(nameAtt.getSearchCriteriaIndex(), sourceName);

		Collection<?> sourcingList = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(product.getMaster(), season.getMaster(), cMap);

		logger.debug("Source List >>"+sourcingList);
		Iterator<?> sIter = sourcingList.iterator();
		if(sIter.hasNext()) {
			sourceObj = (LCSSourcingConfig) sIter.next();
			
			logger.debug("SOURCE NAME :: "+sourceObj.getName());
			
			
		} else{
			logger.debug("ERROR : SOURCE Object not found For Work "+sourceName);
			//appendRowLoadStatus(loggerRowData, "Failed", "SOURCE Object not found");
			//failedCount++;
			//continue;
		}
		return sourceObj;
	}

	/**
	 * @param WorkNo
	 * @param colorwayletter
	 * @param product
	 * @return skuObject
	 * @throws WTException
	 */
	private LCSSKU findSKUByLetter(String WorkNo, String colorwayletter, LCSProduct product) throws WTException {
		LCSSKU skuObject = null;
		Collection productSKUList = null;
		productSKUList = LCSSKUQuery.findSKUs(product, false);
		logger.debug("ProductSKUList size  ::"+productSKUList.size());
		if(productSKUList.size() > 0){
		Iterator iter = productSKUList.iterator();
		int inc = 0;
		LCSSKU tempSKU;
		boolean colorwayLetterNotFound = false;
		while (iter.hasNext()) {
				tempSKU = (LCSSKU) iter.next();
				if(colorwayletter.trim().equals((String) tempSKU.getValue("agrColorwayLetter"))){
					logger.debug("SKU NAME :: "+tempSKU.getName()+"Colorway Letter:"+tempSKU.getValue("agrColorwayLetter"));
					 colorwayLetterNotFound = true;
					 skuObject =  (LCSSKU) VersionHelper.latestIterationOf(tempSKU.getMaster());
					 break;
				}
			}
			if(!colorwayLetterNotFound){
				logger.debug("ERROR : SKU Object not found for Colorway Letter "+colorwayletter);
				}
			
		}else{
			logger.debug("ERROR : SKU Object not found for Product "+WorkNo);
			//appendRowLoadStatus(loggerRowData, "Failed", "SKU Object not found for Product");
			//failedCount++;
			//continue;
		}
		return skuObject;
	}

	/**
	 * @param WorkNo
	 * @return productObj
	 * @throws WTException
	 */
	private LCSProduct findProductByWorkNo(String WorkNo) throws WTException {
		FlexType flexType_Product = FlexTypeCache.getFlexTypeFromPath("Product");
		AgronProductQuery query = new AgronProductQuery();
		Collection attList = new ArrayList();
		attList.add("agrWorkOrderNoProduct");

		Map<String, String> map = new HashMap<String, String>();
		map.put("displayAttribute", "agrWorkOrderNoProduct");
		map.put("quickSearchCriteria", WorkNo);

		SearchResults results = new SearchResults();

		// Find results for Product
		results = query.findProductsByCriteria((map), flexType_Product, attList, null, null, "agrWorkOrderNoProduct");
		
		Collection<FlexObject> prodObj = results.getResults();
		LCSProduct productObj = null;
		if(prodObj.size() > 0) {
			FlexObject prObj = prodObj.iterator().next();
			String branchId = prObj.getString("LCSPRODUCT.BRANCHIDITERATIONINFO");
			productObj = (LCSProduct) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + branchId);

			logger.debug("PRODUCT NAME :: "+productObj.getName());
			
		} else{
			logger.debug("ERROR : PRODUCT Object not found For Work "+WorkNo);
		}
		return productObj;
	}
	
	/**
	 * @param product
	 * @param season
	 * @return pscObj
	 */
	private ProductSizeCategory getBaseSizeCategory(LCSProduct product,LCSSeason season) {
		String baseSize="";
		ProductSizeCategory pscObj = null ;
		try {
			SearchResults sr = (new SizingQuery()).findPSDByProductAndSeason((Map) null, product, season,
					(FlexType) null, new Vector(), new Vector());
			logger.debug("findPSDByProductAndSeason:::"+sr.getResultsFound()+"::"+sr);
			Iterator psctsIterator = LCSQuery.iterateObjectsFromResults(sr,"OR:com.lcs.wc.sizing.ProdSizeCategoryToSeason:", 
					"PRODSIZECATEGORYTOSEASON.IDA2A2");
			if(psctsIterator.hasNext()) {
				ProdSizeCategoryToSeason pscts = (ProdSizeCategoryToSeason) psctsIterator.next();
				pscObj = (ProductSizeCategory) VersionHelper.latestIterationOf(pscts.getSizeCategoryMaster());
				logger.debug("psc>>>"+pscObj);
				baseSize=pscObj.getBaseSize();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		logger.debug("baseSize>>"+baseSize);
		return pscObj;
	}
	private void notifyPLMUser(boolean isValidFile,  ArrayList dataList, int successCount, int failedCount ) throws WTException {
		
		StringBuilder emailContent = new StringBuilder();
	
		emailContent.append("Dear PLM User,");
		emailContent.append(System.getProperty("line.separator"));
		emailContent.append(System.getProperty("line.separator"));
		
		if(isValidFile) {
			emailContent.append("Adidas Article Data Load is successful.");
			}else {
				emailContent.append("Adidas Article Data Load is failed.");
				emailContent.append("There is no data in the excel file So please add the data in the excel file");
			}
		String emailSubject = "Adidas Article Upload Summary";
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
				//Calling an internal function to send email to the distribution list by providing the list of email ID's, email-subject 
				//and email body content
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

			if(attDisplayValue.equals("Season Name")){
				keyList.add("seasonName");
			}else if(attDisplayValue.equals("Work #")){
				keyList.add("agrWorkOrderNoProduct");
			}else if(attDisplayValue.equals("Colorway Letter")){
				keyList.add("agrColorwayLetter");
			}else if(attDisplayValue.equals("Article#")){
				keyList.add(AGR_ARTICLE_NUM);
			}

			else if(attDisplayValue.equals("Req. Consolidation")){
				keyList.add("agrReqConsolidation");
			}else if(attDisplayValue.equals("Minimum Order Quantity")){
				keyList.add("agrMOQ");
			}else if(attDisplayValue.equals("Consolidated Buy Date 1")){
				keyList.add("agrCBD1");
			}else if(attDisplayValue.equals("Consolidated Buy Date 2")){
				keyList.add("agrCBD2");
			}else if(attDisplayValue.equals("Consolidated Buy Date 3")){
				keyList.add("agrCBD3");
			}else if(attDisplayValue.equals("Consolidated Buy Date 4")){
				keyList.add("agrCBD4");
			}else if(attDisplayValue.trim().equals("Adidas Model #")){
				keyList.add("agrAdidasModel");
			}else if(attDisplayValue.trim().equals("Adidas Article #")){
				keyList.add("agrAdidasArticle");
			}else{
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
	public static StringBuffer getFormattedLogEntryRow (ArrayList row) throws WTException  {
		
		logger.debug("getFormattedLogEntryRow() log row - "+row);
		StringBuffer logEntryLine = new StringBuffer();
		Iterator rowItr = row.iterator();
		String cellValue;
		while(rowItr.hasNext()){
			cellValue = (String) rowItr.next();
			if(cellValue.contains(COMMA_DELIMITER))
				cellValue = CSVGenerator.formatForCSV(cellValue);
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
		logEntryLine.append(AgronFOBLoader.LOGGER_DELIMITER);
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
	
	   private static SearchResults findCostsheet(String WorkNo,	String ArticleNo, String ProductName, String Colorwayname, 	
			   String colorwayletter, 	String Seasonname, 	String Costsheetname)
	            throws WTException, WTPropertyVetoException
	        {
	            FlexType seasontype = FlexTypeCache.getFlexTypeFromPath("Season");
	            FlexType productType = FlexTypeCache.getFlexTypeFromPath("Product");
				FlexType skuType = FlexTypeCache.getFlexTypeFromPath("Product");
				skuType.setTypeScopeDefinition("SKU");
				FlexType costsheetType = FlexTypeCache.getFlexTypeFromPath("Cost Sheet");
				SearchResults searchresults = null;
				//Work#,Article#,Product Name,Colorway name,colorway letter,Season name,Costsheet name,Cost sheet FOB,Cost sheet size
				
				
	    		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	            preparedquerystatement.appendFromTable("LCSPRODUCTCOSTSHEET");
	    		preparedquerystatement.appendFromTable("LCSCOSTSHEETMASTER");
	    		preparedquerystatement.appendFromTable("LCSPRODUCT");
	    		preparedquerystatement.appendFromTable("LCSSEASON"); 
	    		preparedquerystatement.appendFromTable("LCSSKU");
				
				//preparedquerystatement.appendFromTable("LCSPARTMASTER");
				
	    		preparedquerystatement.appendSelectColumn("LCSSEASON","IDA2A2");
	    		preparedquerystatement.appendSelectColumn("LCSPRODUCT","IDA2A2");
	    		preparedquerystatement.appendSelectColumn("LCSCOSTSHEETMASTER","IDA2A2");
				preparedquerystatement.appendSelectColumn("LCSPRODUCTCOSTSHEET","BRANCHIDITERATIONINFO");
				preparedquerystatement.appendSelectColumn("LCSSKU","IDA2A2");
	    		preparedquerystatement.setDistinct(true);
	    		preparedquerystatement.appendJoin("LCSPRODUCT","IDA3B12","LCSSEASON","IDA3MASTERREFERENCE");
				preparedquerystatement.appendJoin("LCSPRODUCTCOSTSHEET","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA2A2");
				
				preparedquerystatement.appendJoin("LCSSEASON","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA3C6");
				//preparedquerystatement.appendJoin("LCSSKU","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA3B6");
				preparedquerystatement.appendJoin("LCSPRODUCT","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA3D6");

				preparedquerystatement.appendJoin("LCSSKU","IDA3A12","LCSPRODUCT","IDA3MASTERREFERENCE");
	    		
				preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProduct.class, 
						productType.getAttribute("agrWorkOrderNoProduct").getColumnDescriptorName()), WorkNo, "="));
	    		preparedquerystatement.appendAnd();
				preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSKU.class, 
						skuType.getAttribute(AGR_ARTICLE_NUM).getColumnDescriptorName()), ArticleNo, "="));
	    		preparedquerystatement.appendAnd();
				/*
				if(FormatHelper.hasContent(ProductName)){
					ProductName = ProductName.trim();
					preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProduct.class, 
					productType.getAttribute("agrStyleName").getColumnDescriptorName()), ProductName, "="));
					preparedquerystatement.appendAnd();
				}
				*/
				/*if(FormatHelper.hasContent(Colorwayname)){
					if(Colorwayname.indexOf("-") != -1){
						Colorwayname = Colorwayname.substring(Colorwayname.indexOf("-")+1);
						Colorwayname = Colorwayname.trim();
					}
					preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	com.lcs.wc.part.LCSPartMaster.class, 
					"name"), Colorwayname, "="));
					preparedquerystatement.appendAnd();
				}*/
				//preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, "master>name"), Colorwayname, "="));
				//preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, 
	    		//skuType.getAttribute("name").getColumnDescriptorName()), Colorwayname, "="));
	    		//preparedquerystatement.appendAnd();
				
				/*
				preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSKU.class, 
				skuType.getAttribute("agrColorwayLetter").getColumnDescriptorName()), colorwayletter, "="));
	    		preparedquerystatement.appendAnd();
				*/
				if(FormatHelper.hasContent(Costsheetname)) {
				//preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSCostSheetMaster.class, "name"),
					//Costsheetname, "="));
				preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProductCostSheet.class,
						costsheetType.getAttribute("name").getColumnDescriptorName()), Costsheetname, "="));
	    		preparedquerystatement.appendAnd();
	    		}
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, 
	    				seasontype.getAttribute("seasonName").getColumnDescriptorName()), Seasonname, "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProduct.class, 
	    				LCSProduct.LATEST_ITERATION), "1", "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSKU.class, LCSSKU.LATEST_ITERATION), "1", "="));
				preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProductCostSheet.class, 
	    				LCSProductCostSheet.LATEST_ITERATION), "1", "="));

	    		logger.debug("Query ::" +preparedquerystatement.toString());
	            searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
	            logger.debug("Query count ::" +searchresults.getResultsFound());


	            return searchresults;
	        } 
	   
	   private SearchResults findCostsheetBySize(String WorkNo,	String ArticleNo, String ProductName, String Colorwayname, 	
			   String colorwayletter, 	String Seasonname, 	String Costsheetname, String size, String seasonMasterId, String sourceMasterId)
	            throws WTException, WTPropertyVetoException
	        {
	            FlexType seasontype = FlexTypeCache.getFlexTypeFromPath("Season");
	            FlexType productType = FlexTypeCache.getFlexTypeFromPath("Product");
				FlexType skuType = FlexTypeCache.getFlexTypeFromPath("Product");
				skuType.setTypeScopeDefinition("SKU");
				FlexType costsheetType = FlexTypeCache.getFlexTypeFromPath("Cost Sheet");
				SearchResults searchresults = null;
				//Work#,Article#,Product Name,Colorway name,colorway letter,Season name,Costsheet name,Cost sheet FOB,Cost sheet size
				
				
	    		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
	            preparedquerystatement.appendFromTable("LCSPRODUCTCOSTSHEET");
	    		preparedquerystatement.appendFromTable("LCSCOSTSHEETMASTER");
	    		preparedquerystatement.appendFromTable("LCSPRODUCT");
	    		preparedquerystatement.appendFromTable("LCSSEASON"); 
	    		preparedquerystatement.appendFromTable("LCSSKU");
				
				//preparedquerystatement.appendFromTable("LCSPARTMASTER");
				
	    		preparedquerystatement.appendSelectColumn("LCSSEASON","IDA2A2");
	    		preparedquerystatement.appendSelectColumn("LCSPRODUCT","IDA2A2");
	    		preparedquerystatement.appendSelectColumn("LCSCOSTSHEETMASTER","IDA2A2");
				preparedquerystatement.appendSelectColumn("LCSPRODUCTCOSTSHEET","BRANCHIDITERATIONINFO");
				preparedquerystatement.appendSelectColumn("LCSSKU","IDA2A2");
	    		preparedquerystatement.setDistinct(true);
	    		preparedquerystatement.appendJoin("LCSPRODUCT","IDA3B12","LCSSEASON","IDA3MASTERREFERENCE");
				preparedquerystatement.appendJoin("LCSPRODUCTCOSTSHEET","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA2A2");
				
				preparedquerystatement.appendJoin("LCSSEASON","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA3C6");
				//preparedquerystatement.appendJoin("LCSSKU","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA3B6");
				preparedquerystatement.appendJoin("LCSPRODUCT","IDA3MASTERREFERENCE","LCSCOSTSHEETMASTER","IDA3D6");

				preparedquerystatement.appendJoin("LCSSKU","IDA3A12","LCSPRODUCT","IDA3MASTERREFERENCE");
	    		
				preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProduct.class, 
						productType.getAttribute("agrWorkOrderNoProduct").getColumnDescriptorName()), WorkNo, "="));
	    		preparedquerystatement.appendAnd();
				preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSKU.class, 
						skuType.getAttribute(AGR_ARTICLE_NUM).getColumnDescriptorName()), ArticleNo, "="));
	    		preparedquerystatement.appendAnd();  //REPRESENTATIVESIZE
	    		
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProductCostSheet.class, 
	    				LCSProductCostSheet.SEASON_MASTER_ID), seasonMasterId, "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProductCostSheet.class, 
	    				LCSProductCostSheet.SOURCE_MASTER_ID), sourceMasterId, "="));
	    		preparedquerystatement.appendAnd();

				if(FormatHelper.hasContent(Costsheetname)) {
				//preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSCostSheetMaster.class, "name"), 
					//Costsheetname, "="));
				preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProductCostSheet.class, 
						costsheetType.getAttribute("name").getColumnDescriptorName()), Costsheetname, "="));
	    		preparedquerystatement.appendAnd();
	    		}
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, 
	    				seasontype.getAttribute("seasonName").getColumnDescriptorName()), Seasonname, "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, LCSSeason.LATEST_ITERATION), "1", "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProduct.class, 
	    				LCSProduct.LATEST_ITERATION), "1", "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSKU.class, LCSSKU.LATEST_ITERATION), "1", "="));
				preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProductCostSheet.class,
	    				LCSProductCostSheet.LATEST_ITERATION), "1", "="));
	    		preparedquerystatement.appendAnd();
	    		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSProductCostSheet.class,
	    				LCSProductCostSheet.APPLICABLE_SIZES), size.concat("%"), Criteria.LIKE));

	    		logger.debug("Query ::" +preparedquerystatement.toString());
	            searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
	            logger.debug("Query count ::" +searchresults.getResultsFound());


	            return searchresults;
	        } 
	
	private String findSeasonName(String seasonName) throws WTException{
		
		FlexType seasontype = FlexTypeCache.getFlexTypeFromPath("Season");
		String name ="";
		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
		preparedquerystatement.appendFromTable("LCSSEASON"); 
		preparedquerystatement.appendSelectColumn("LCSSEASON","IDA2A2");
		if(seasonName.startsWith("Line Sheet -")){
			seasonName = seasonName.replace("Line Sheet -","");
			seasonName = seasonName.trim();
		}
		String[] words = seasonName.split("\\s+");
		if(words.length >2){
			int e=0;
			while(seasonName.indexOf(" ")  != -1 && e<2 ){
				name = seasonName.substring(0, seasonName.indexOf(" ")+2);
				e++;
			}
		}

		
		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, 
				seasontype.getAttribute("seasonName").getColumnDescriptorName()), name + "%", "LIKE"));
		preparedquerystatement.appendAnd();
		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, LCSSeason.LATEST_ITERATION), "1", "="));
		
		SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
		Iterator iter = searchresults.getResults().iterator();
		String tName ="";
		while(iter.hasNext()){
			FlexObject seasonObj = (FlexObject)iter.next();
			String branchId = seasonObj.getString("LCSSEASON.IDA2A2");
				if(!FormatHelper.hasContent(branchId) ){
					continue;
				}
				LCSSeason seaObject = (LCSSeason) LCSQuery.findObjectById("OR:com.lcs.wc.season.LCSSeason:" + branchId);
				tName = (String)seaObject.getValue("seasonName");
				if(tName.indexOf(words[2]) != -1 ){
					name = tName;
				}
			
		}

		return name;
	}
	
private LCSSeason findSeason(String seasonName) throws WTException{
		
		FlexType seasontype = FlexTypeCache.getFlexTypeFromPath("Season");
		String name ="";
		LCSSeason season = new LCSSeason();
		PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
		preparedquerystatement.appendFromTable("LCSSEASON"); 
		preparedquerystatement.appendSelectColumn("LCSSEASON","IDA2A2");
		if(seasonName.startsWith("Line Sheet -")){
			seasonName = seasonName.replace("Line Sheet -","");
			seasonName = seasonName.trim();
		}
		String[] words = seasonName.split("\\s+");
		if(words.length >2){
			int e=0;
			while(seasonName.indexOf(" ")  != -1 && e<2 ){
				name = seasonName.substring(0, seasonName.indexOf(" ")+2);
				e++;
			}
		}

		
		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, 
				seasontype.getAttribute("seasonName").getColumnDescriptorName()), name + "%", "LIKE"));
		preparedquerystatement.appendAnd();
		preparedquerystatement.appendCriteria(new Criteria(new QueryColumn(	LCSSeason.class, LCSSeason.LATEST_ITERATION), "1", "="));
		
		logger.trace("Season Query ::"+preparedquerystatement.toString());
		SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
		Iterator iter = searchresults.getResults().iterator();
		String tName ="";
		while(iter.hasNext()){
			FlexObject seasonObj = (FlexObject)iter.next();
			String branchId = seasonObj.getString("LCSSEASON.IDA2A2");
				if(!FormatHelper.hasContent(branchId) ){
					continue;
				}
				LCSSeason seaObject = (LCSSeason) LCSQuery.findObjectById("OR:com.lcs.wc.season.LCSSeason:" + branchId);
				tName = (String)seaObject.getValue("seasonName");
				if(tName.indexOf(words[2]) != -1 ){
					name = tName;
					season = seaObject;
				}
			
		}

		return season;
	}
}
