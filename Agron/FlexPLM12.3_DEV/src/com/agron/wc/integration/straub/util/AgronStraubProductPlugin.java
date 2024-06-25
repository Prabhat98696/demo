package com.agron.wc.integration.straub.util;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.foundation.LCSLogEntryQuery;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronStraubProductPlugin {
	
	private static String LOGENTRY_BRANCHID_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.product.branchId").trim();
	private static  String LOGENTRY_IDENTIFIER_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.product.identifier").trim();
	private static String LOGENTRY_OUTBOUND_PRODUCT_PATH =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.product.lcslogEntryProduct").trim();

	//Keys for criteria map
	private static String LOGENTRY_BRANCHID_DBCOLUMN_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.product.branchIDDbColumnKey","ptc_str_1").trim();
	private final static String LOGENTRY_CRITERIA_PREFIX="LCSLOGENTRY_";
	public static String ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.integration.straub.outbound.excelLayoutAttributes");
	
	private static final Logger LOGGER = LogManager.getLogger(AgronStraubProductPlugin.class);
	static List<String> excelLayoutAttributes = Arrays.asList(ATTRIBUTE_LIST.split(","));
	
	
	public static void logProductChanges(WTObject object) throws Exception {
		try{
			if (object instanceof LCSProduct) {
				LCSProduct product = (LCSProduct)object;
				if(product.getBranchIdentifier() == 0)  {
					return; // This is when the colorway is created , we do not need to use this plugin.
				}
				LOGGER.info(":::logProductChanges START-->>>>"+product);
				
				if(checkStraubValueModifiedForProduct(product)){
				
				product = (LCSProduct) VersionHelper.latestIterationOf((LCSProduct) VersionHelper.getVersion( ((product).getMaster()),"A"));
					
				HashMap<String,Object> logEntryMap=new 	HashMap<String,Object>();
				Map<String,Object> logEntryCriteriaMap=new 	HashMap<String,Object>();
				String  productid =  String.valueOf(product.getBranchIdentifier());
				
				String  productBranchId = String.valueOf(SeasonProductLocator.getProductARev(product).getBranchIdentifier());
				LOGGER.info(":::productBranchId->>"+productBranchId);
				String identifier=product.getIdentity();
				
				logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_BRANCHID_DBCOLUMN_KEY, productBranchId);
				
				LOGGER.info("logEntryCriteriaMap::Product:>>"+logEntryCriteriaMap);
				
				logEntryMap.put(LOGENTRY_BRANCHID_KEY, productBranchId);
				logEntryMap.put(LOGENTRY_IDENTIFIER_KEY, identifier);
				 
				LOGGER.info("logEntryMap:::>>"+logEntryMap);
				
				Collection<LCSLogEntry> collection=findLogEntryByCriteria(LOGENTRY_OUTBOUND_PRODUCT_PATH,logEntryCriteriaMap);

				LOGGER.info("Product Logentry collection size:::>>"+collection.size());
				if(collection.size()>0) {
					Iterator<LCSLogEntry> logEntryItr = collection.iterator();
					while (logEntryItr.hasNext()) {
						LCSLogEntry entry= (LCSLogEntry) logEntryItr.next(); 
						entry=updateLogEntryWithMapValue(entry,logEntryMap);
					}
				}else {
					LCSLogEntry lcslogentry= createLogEntry(LOGENTRY_OUTBOUND_PRODUCT_PATH,logEntryMap);
					System.out.println("Product Logentry created:::>>"+lcslogentry);
				}
				
			}else{
				LOGGER.info("No straub Attribute update on product-level-");
			}
				
				LOGGER.info("logProductChanges END-->>>"+object);
		 }
		}catch(Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception in logProductChanges Plugin", e);
		}
	}

	
	public static boolean checkStraubValueModifiedForProduct(LCSProduct product) throws WTException{
		boolean isModified = false;
		LCSProduct previousProduct = (LCSProduct)VersionHelper.predecessorOf(product);
		//String versionId =FormatHelper.getVersionId(product);
		//LCSProduct previousProduct = (LCSProduct) LCSQuery.findObjectById(versionId, false);
		for (int i = 0; i < excelLayoutAttributes.size(); i++)  {
			String attribute = excelLayoutAttributes.get(i);
			String attKey = attribute.split("\\.")[1];
			LOGGER.debug("attKey ::>>"+attKey);
			if(attribute.startsWith("PRODUCT.") && !attribute.contains("productRootType") && !attribute.contains("agrProductDivision") && !attribute.contains("adidasAgeGroup")){
				
				String newProduct=String.valueOf(String.valueOf(product.getValue(attKey)).trim());
				String oldProduct= null;
				if(previousProduct != null) {
					 oldProduct=String.valueOf(String.valueOf(previousProduct.getValue(attKey)).trim());

				}
				
				
				if(newProduct==null){
					newProduct= "";
				}
				if(oldProduct==null){
					oldProduct="";
				}
				LOGGER.debug("Product new Value ::>>"+newProduct+",  Product old Value ::>>"+oldProduct);
				if(!newProduct.equals(oldProduct)) {
					isModified=true;
					break;
				}
			}
		}
		if(isModified) {
			LOGGER.info("This product is for straub attrbutes ");
		}
		return isModified;
	}
	
	
	/**
	 * This method is used to find LCSLogEntry by criteria Map.
	 * @param flextypeIdPath
	 * @param criteriaMap
	 * @throws WTException
	 * @return Collection<LCSLogEntry> collection of LCSLogEntry objects
	 */
	public static Collection<LCSLogEntry> findLogEntryByCriteria(String flextypeIdPath,Map<String, Object> criteriaMap) throws WTException
	{
		System.out.println("***PRoduct***findLogEntryByCriteria*****::criteriaMap::*"+criteriaMap);
		FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(flextypeIdPath);
		AgronLogEntryQuery agroLogEntry=new AgronLogEntryQuery();
		SearchResults logEntryResults = agroLogEntry.findLogEntriesByCriteria(criteriaMap, logEntryType, null, null, null); 
		Collection<LCSLogEntry> logEntryObject= LCSLogEntryQuery.getObjectsFromResults(logEntryResults.getResults(), "OR:com.lcs.wc.foundation.LCSLogEntry:", "LCSLogEntry.IDA2A2");
		LOGGER.info("logEntryCollection::"+logEntryObject.size()+":::"+logEntryObject);
		return logEntryObject;
	}
	
	/**
	 * This method is used to update the log entry.
	 * 
	 * @param lcslogentry 
	 * @param valueMap      is MAP of the Attribute Key and Value
	 * @throws WTException             
	 * @throws WTPropertyVetoException 
	 * @throws ParseException          
	 */
	public static LCSLogEntry updateLogEntryWithMapValue(LCSLogEntry lcslogentry, HashMap<String,Object> valueMap) throws WTException, WTPropertyVetoException, ParseException {
		for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if(FormatHelper.hasContent(key))
			{
				lcslogentry.setValue(key, value);
			}
			(new LCSLogEntryLogic()).saveLog(lcslogentry);
			LOGGER.info((new StringBuilder()).append("Exiting update Logentry : ").append(lcslogentry).toString());
		}
		return lcslogentry;
	}
	
	/**
	 * This method is used to create the log entry.
	 * 
	 * @param flextypeIdPath 
	 * @param valueMap      is MAP of the Attribute Key and Value
	 * @throws WTException             
	 * @throws WTPropertyVetoException 
	 * @throws ParseException          
	 */
	public static LCSLogEntry createLogEntry(String flextypeIdPath,Map<String, Object> valueMap)
			throws WTException, WTPropertyVetoException, ParseException {
		System.out.println("createLogEntry START>>>>"+valueMap);
		LCSLogEntry lcslogentry = LCSLogEntry.newLCSLogEntry();
		String currentTimeStamp = null;
		FlexType flextype = FlexTypeCache.getFlexTypeFromPath(flextypeIdPath);
		System.out.println("flextype"+flextype);
		lcslogentry.setFlexType(flextype);
		System.out.println("lcslogentry>>>>"+lcslogentry);
		for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if(FormatHelper.hasContent(key))
			{
				lcslogentry.setValue(key, value);
			}
		}
		(new LCSLogEntryLogic()).saveLog(lcslogentry);
		LOGGER.info((new StringBuilder()).append("Exiting createLogEntry : ").append(lcslogentry).toString());
		System.out.println("createLogEntry END>>>>"+lcslogentry);
		return lcslogentry;
	}
}
