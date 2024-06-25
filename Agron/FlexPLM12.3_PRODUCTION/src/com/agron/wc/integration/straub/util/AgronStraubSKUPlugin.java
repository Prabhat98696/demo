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
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronStraubSKUPlugin {

	
	private static String LOGENTRY_BRANCHID_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.sku.branchId").trim();
	private static  String LOGENTRY_IDENTIFIER_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.sku.identifier").trim();
	private static String LOGENTRY_OUTBOUND_SKU_PATH =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.sku.lcslogEntrySKU").trim();

	//Keys for criteria mapptc_str_10typeInfoLCSLogEntr
	private static String LOGENTRY_BRANCHID_DBCOLUMN_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.sku.branchIDDbColumnKey","ptc_str_10").trim();
	private final static String LOGENTRY_CRITERIA_PREFIX="LCSLOGENTRY_";
	public static String ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.integration.straub.outbound.excelLayoutAttributes");
	
	private static final Logger LOGGER = LogManager.getLogger(AgronStraubSKUPlugin.class);
	static List<String> excelLayoutAttributes = Arrays.asList(ATTRIBUTE_LIST.split(","));
	
	
	public static void logSkuChanges(WTObject object) throws Exception {
		try{
			if (object instanceof LCSSKU) {
				LCSSKU sku = (LCSSKU)object;
				
				//LOGGER.info("logSkuChanges START SKU BRANCH --------------"+sku.getBranchIdentifier());
				//LOGGER.info("logSkuChanges START SKU BRANCH --------------"+Long.valueOf(sku.getBranchIdentifier()));
				//LOGGER.info("logSkuChanges START (sku).getMaster() --------------"+(sku).getMaster());
				
				if(sku.getBranchIdentifier() == 0)  {
					return; // This is when the colorway is created , we do not need to use this plugin.
				}
				
				
				if(checkStraubValueModifiedForProduct(sku)){
					
					sku = (LCSSKU) VersionHelper.latestIterationOf((LCSSKU) VersionHelper.getVersion( ((sku).getMaster()),"A"));
					
					String  skuBranchId = String.valueOf(SeasonProductLocator.getSKUARev(sku).getBranchIdentifier());
					LOGGER.info("skuBranchId-----------"+skuBranchId);

					HashMap<String,Object> logEntryMap=new 	HashMap<String,Object>();
					Map<String,Object> logEntryCriteriaMap=new 	HashMap<String,Object>();


					String identifier=sku.getIdentity();

					logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_BRANCHID_DBCOLUMN_KEY, skuBranchId);

					LOGGER.info("logEntryCriteriaMap::Product:>>"+logEntryCriteriaMap);


					logEntryMap.put(LOGENTRY_BRANCHID_KEY, skuBranchId);
					logEntryMap.put(LOGENTRY_IDENTIFIER_KEY, identifier);

					LOGGER.info("logEntryMap:::>>"+logEntryMap);

					Collection<LCSLogEntry> collection=findLogEntryByCriteria(LOGENTRY_OUTBOUND_SKU_PATH,logEntryCriteriaMap);

					LOGGER.info("SKU Logentry collection size:::>>"+collection.size());
					if(collection.size()>0) {
						Iterator<LCSLogEntry> logEntryItr = collection.iterator();
						while (logEntryItr.hasNext()) {
							LCSLogEntry entry= (LCSLogEntry) logEntryItr.next(); 
							entry=updateLogEntryWithMapValue(entry,logEntryMap);
						}
					}else {
						LCSLogEntry lcslogentry= createLogEntry(LOGENTRY_OUTBOUND_SKU_PATH,logEntryMap);
						System.out.println("SKU Logentry created:::>>"+lcslogentry);
					}

					
				}else {
					System.out.println("No straub Attribute update on sku-level-");
				}
				System.out.println("logSkuChanges END--------------"+object);
			}
		}catch(Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception in logSkuChanges Plugin", e);
		}
	}

	
	public static boolean checkStraubValueModifiedForProduct(LCSSKU sku) throws WTException{
		boolean isModified = false;
		LCSSKU previousSKU = (LCSSKU)VersionHelper.predecessorOf(sku);
		//String versionId =FormatHelper.getVersionId(sku);
		//LCSSKU previousSKU = (LCSSKU) LCSQuery.findObjectById(versionId, false);
		for (int i = 0; i < excelLayoutAttributes.size(); i++)  {
			String attribute = excelLayoutAttributes.get(i);
			String attKey = attribute.split("\\.")[1];
			LOGGER.info("attKey ::>>"+attKey);
			if(attribute.startsWith("SKU.")){
				String newSKU=String.valueOf(sku.getValue(attKey)).trim();
				String oldSKU = null;
				if(previousSKU != null) {
					oldSKU=String.valueOf(previousSKU.getValue(attKey)).trim();
				
				}
				if(newSKU==null){
					newSKU= "";
				}
				if(oldSKU==null){
					oldSKU="";
				}
				LOGGER.info("SKU new Value ::>>"+newSKU+",  SKU Old Value >>>"+oldSKU);
				if(!newSKU.equals(oldSKU)) {
					isModified=true;
					break;
				}
			}
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
		System.out.println("***SKU***findLogEntryByCriteria*****::criteriaMap::*"+criteriaMap);
		FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(flextypeIdPath);
		AgronLogEntryQuery agroLogEntry=new AgronLogEntryQuery();
		SearchResults logEntryResults = agroLogEntry.findLogEntriesByCriteria(criteriaMap, logEntryType, null, null, null); 
		Collection<LCSLogEntry> logEntryObject= LCSLogEntryQuery.getObjectsFromResults(logEntryResults.getResults(), "OR:com.lcs.wc.foundation.LCSLogEntry:", "LCSLogEntry.IDA2A2");
		//LOGGER.info("logEntryCollection::"+logEntryObject.size()+":::"+logEntryObject);
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
		LOGGER.info("createLogEntry START>>>>"+valueMap);
		LCSLogEntry lcslogentry = LCSLogEntry.newLCSLogEntry();
		String currentTimeStamp = null;
		FlexType flextype = FlexTypeCache.getFlexTypeFromPath(flextypeIdPath);
		LOGGER.info("flextype"+flextype);
		lcslogentry.setFlexType(flextype);
		//System.out.println("lcslogentry>>>>"+lcslogentry);
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
		System.out.println("createLogEntry END>>>>");
		return lcslogentry;
	}


}
