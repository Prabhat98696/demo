package com.agron.wc.integration.straub.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.foundation.LCSLogEntryQuery;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPart;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author Acnovate
 * Plugin to Capture the Data changes in the PLM
 *
 */
/**
 * @author PTC-Service
 *
 */
public class AgronStraubPlugin {
	
	private static String LOGENTRY_LINKTYPE_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.linkType").trim();
	
	private static String LOGENTRY_OWNER_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.ownerId").trim();
	private static String LOGENTRY_SEASON_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.season").trim();
	private static  String LOGENTRY_IDENTIFIER_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.identifier").trim();
	private static String LOGENTRY_OUTBOUND_SPLINK_PATH =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.lcslogEntrySPLink").trim();
    private static String  LOGENTRY_SPLINKID_KEY =LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.spLinkId").trim();
   
	//Keys for criteria map
	private static String LOGENTRY_LINKTYPE_DBCOLUMN_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.linkTypeDbColumnKey","ptc_str_13").trim();
	private static String LOGENTRY_OWNER_DBCOLUMN_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.ownerIdDbColumnKey","ptc_str_10").trim();
	private static String LOGENTRY_SEASON_DBCOLUMN_KEY=LCSProperties.get("com.agron.wc.logEntry.straub.integration.outBound.seasonDbColumnKey","ptc_str_11").trim();
	private final static String LOGENTRY_CRITERIA_PREFIX="LCSLOGENTRY_";
	public static String ATTRIBUTE_LIST=LCSProperties.get("com.agron.wc.integration.straub.outbound.excelLayoutAttributes");
	
	private static final Logger LOGGER = LogManager.getLogger(AgronStraubPlugin.class);
	static List<String> excelLayoutAttributes = Arrays.asList(ATTRIBUTE_LIST.split(","));
	
	public static void logSeasonProductLinkChanges(WTObject object) throws Exception {
		try {
			if (object instanceof LCSSeasonProductLink) {
				LCSSeasonProductLink spLink = (LCSSeasonProductLink)object;
				
				if(spLink.isEffectLatest() && checkStraubValueModified(spLink)) {
					LOGGER.info("captureChanges START--------------"+object);
					HashMap<String,Object> logEntryMap=new 	HashMap<String,Object>();
					Map<String,Object> logEntryCriteriaMap=new 	HashMap<String,Object>();
					LCSPart owner=spLink.getOwner();
					String  seasonBranchId = String.valueOf(SeasonProductLocator.getSeasonRev(spLink).getBranchIdentifier());
					String ownerId=String.valueOf(owner.getBranchIdentifier());
					String linkType=spLink.getSeasonLinkType();
					String spLinkId=spLink.toString();
					String identifier=spLink.getIdentity();

					logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_OWNER_DBCOLUMN_KEY, ownerId);
					logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_SEASON_DBCOLUMN_KEY, seasonBranchId);
					logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_LINKTYPE_DBCOLUMN_KEY, linkType);

					logEntryMap.put(LOGENTRY_OWNER_KEY, ownerId);
					logEntryMap.put(LOGENTRY_SEASON_KEY, seasonBranchId);
					logEntryMap.put(LOGENTRY_LINKTYPE_KEY, linkType);
					logEntryMap.put(LOGENTRY_IDENTIFIER_KEY, identifier);
					logEntryMap.put(LOGENTRY_SPLINKID_KEY, spLinkId);

					LOGGER.debug("logEntryMap:::>>"+logEntryMap);

					Collection<LCSLogEntry> collection=findLogEntryByCriteria(LOGENTRY_OUTBOUND_SPLINK_PATH,logEntryCriteriaMap);

					if(collection.size()>0) {
						Iterator<LCSLogEntry> logEntryItr = collection.iterator();
						while (logEntryItr.hasNext()) {
							LCSLogEntry entry= (LCSLogEntry) logEntryItr.next(); 
							entry=updateLogEntryWithMapValue(entry,logEntryMap);
						}
					}else {
						LCSLogEntry lcslogentry= createLogEntry(LOGENTRY_OUTBOUND_SPLINK_PATH,logEntryMap);
						LOGGER.debug("Logentry created:::>>"+lcslogentry);
					}

					LOGGER.info("captureChanges END--------------"+object);	
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception in captureChanges Plugin", e);
		}


	}
	
	
	
	public static void logskuSeasonLinkChanges(WTObject object) throws Exception {
		try {
			if (object instanceof LCSSKUSeasonLink) {
				LCSSKUSeasonLink skuLink = (LCSSKUSeasonLink)object;
				if(skuLink.isEffectLatest() && checkSKUSeasonStraubValueModified(skuLink)) {
					LOGGER.info("captureChanges START--------------"+object);
					HashMap<String,Object> logEntryMap=new 	HashMap<String,Object>();
					Map<String,Object> logEntryCriteriaMap=new 	HashMap<String,Object>();
					LCSPart owner=skuLink.getOwner();
					String  seasonBranchId = String.valueOf(SeasonProductLocator.getSeasonRev(skuLink).getBranchIdentifier());
					String ownerId=String.valueOf(owner.getBranchIdentifier());
					String linkType=skuLink.getSeasonLinkType();
					String spLinkId=skuLink.toString();
					String identifier=skuLink.getIdentity();

					logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_OWNER_DBCOLUMN_KEY, ownerId);
					logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_SEASON_DBCOLUMN_KEY, seasonBranchId);
					logEntryCriteriaMap.put(LOGENTRY_CRITERIA_PREFIX+LOGENTRY_LINKTYPE_DBCOLUMN_KEY, linkType);


					logEntryMap.put(LOGENTRY_OWNER_KEY, ownerId);
					logEntryMap.put(LOGENTRY_SEASON_KEY, seasonBranchId);
					logEntryMap.put(LOGENTRY_LINKTYPE_KEY, linkType);
					logEntryMap.put(LOGENTRY_IDENTIFIER_KEY, identifier);
					logEntryMap.put(LOGENTRY_SPLINKID_KEY, spLinkId);


					Collection<LCSLogEntry> collection=findLogEntryByCriteria(LOGENTRY_OUTBOUND_SPLINK_PATH,logEntryCriteriaMap);

					if(collection.size()>0) {
						Iterator<LCSLogEntry> logEntryItr = collection.iterator();
						while (logEntryItr.hasNext()) {
							LCSLogEntry entry= (LCSLogEntry) logEntryItr.next(); 
							entry=updateLogEntryWithMapValue(entry,logEntryMap);
						}
					}else {
						LCSLogEntry lcslogentry= createLogEntry(LOGENTRY_OUTBOUND_SPLINK_PATH,logEntryMap);
						LOGGER.info("Logentry created:::>>"+lcslogentry);
					}

					LOGGER.info("captureChanges END--------------"+object);	
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception in captureChanges Plugin", e);
		}


	}
	
	/*
	 * Trello 387
	 * 1. Autopopulate Production cancelled - Once an Article is Discontinued, set the production cancelled to Yes.

		2.  Priority is Yes then Ecom Photos Needed Date (Catalog Photo Date Needed ) is the Sample ETA Date + 30 days?
	 */
	public static void autoPopulateAttributes(WTObject object) throws Exception {
		try {
			if (object instanceof LCSSKUSeasonLink) {
				LCSSKUSeasonLink skuLink = (LCSSKUSeasonLink)object;

				
				if(skuLink.isEffectLatest()) {
					LOGGER.info("autoPopulateAttributes for SKU Link"+object);
					String colorwayStatus = (String)skuLink.getValue("agrColorwaystatus");

					if("agrDiscontinued".equalsIgnoreCase(colorwayStatus)) {
						skuLink.setValue("agrProductionCancelled", "agrYes");
						Date currentdate = new Date();
						Date agrDroppedDate = (Date)skuLink.getValue("agrDroppedDate");
						LOGGER.info(" *** Setting the dropped date while dropping the SKU *** ");
						if(agrDroppedDate == null) {
							skuLink.setValue("agrDroppedDate", currentdate);
							LOGGER.info(" *** Blanking the dropped date while activating the SKU *** ");
						}
					}else {
						skuLink.setValue("agrDroppedDate", null);
					}
					
					//agrPPSampleExFactorys
					
					Date agrSampleETDtoStraub =(Date)skuLink.getValue("agrSampleETDtoStraub");
					  Date agrPPSampleExFactory  = (Date)skuLink.getValue("agrPPSampleExFactory");
					  
					  if(agrSampleETDtoStraub != null) {
						  agrPPSampleExFactory = new Date();
						  Calendar c = Calendar.getInstance();
						  c.setTime(agrSampleETDtoStraub); 
						  c.add(Calendar.DATE, -14);
						  agrPPSampleExFactory.setTime( c.getTime().getTime() );
						  skuLink.setValue("agrPPSampleExFactory", agrPPSampleExFactory);
						  LOGGER.info("Setting agrPPSampleExFactory "+object);
						  
						  
						  Date agrAvailabilityDate = (Date)skuLink.getValue("agrAvailabilityDate");
						  Date agreComPhotoDueDate =(Date)skuLink.getValue("agreComPhotoDueDate");
						  if(agreComPhotoDueDate == null && agrAvailabilityDate != null) {
							  
							  	  agreComPhotoDueDate = new Date(); 
							  	  Calendar eComCal = Calendar.getInstance();
							  	  eComCal.setTime(agrAvailabilityDate); 
							  	  eComCal.add(Calendar.DATE, -90);
								  agreComPhotoDueDate.setTime( eComCal.getTime().getTime() );
								  skuLink.setValue("agreComPhotoDueDate", agreComPhotoDueDate); 
								  
						  }
						  
						  
					  }
					  

						/*
						 * String agrPriority = (String)skuLink.getValue("agrPriority");
						 * if("agrYes".equalsIgnoreCase(agrPriority)) {
						 * 
						 * Date agrCatalogPhotoPlanDate =
						 * (Date)skuLink.getValue("agrCatalogPhotoPlanDate"); if(agrCatalogPhotoPlanDate
						 * == null && agrSampleETDtoStraub != null) { agrCatalogPhotoPlanDate = new
						 * Date(); Calendar c = Calendar.getInstance(); c.setTime(agrSampleETDtoStraub);
						 * c.add(Calendar.DATE, 30); agrCatalogPhotoPlanDate.setTime(
						 * c.getTime().getTime() ); skuLink.setValue("agrCatalogPhotoPlanDate",
						 * agrCatalogPhotoPlanDate); } }else { Date agrCatalogPhotoPlanDate =
						 * (Date)skuLink.getValue("agrCatalogPhotoPlanDate"); Date agrAvailabilityDate =
						 * (Date)skuLink.getValue("agrAvailabilityDate");
						 * 
						 * if(agrCatalogPhotoPlanDate == null && agrAvailabilityDate != null) {
						 * agrCatalogPhotoPlanDate = new Date(); Calendar c = Calendar.getInstance();
						 * c.setTime(agrAvailabilityDate); c.add(Calendar.DATE, -90);
						 * agrCatalogPhotoPlanDate.setTime( c.getTime().getTime() );
						 * skuLink.setValue("agrCatalogPhotoPlanDate", agrCatalogPhotoPlanDate); } }
						 */
					 

				}
			}
		
		}catch(Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception in AgronStraubPlugin.autoPopulateAttributes Plugin", e);
		}
	}
	
	public static boolean checkStraubValueModified(LCSSeasonProductLink skuSeasonLink) throws WTException{
		boolean isModified = false;
		LCSSeasonProductLink previousSPLLink=LCSSeasonQuery.findSeasonProductLink(skuSeasonLink, skuSeasonLink.getEffectSequence()-1);
		for (int i = 0; i < excelLayoutAttributes.size(); i++)  {
			String attribute = excelLayoutAttributes.get(i);
			String attKey = attribute.split("\\.")[1];
			String preFix = "PRODUCTSEASONLINK."; 
			if("SKU".equalsIgnoreCase(skuSeasonLink.getSeasonLinkType()))
			{
				preFix = "SKUSEASONLINK."; 
			}
			if(attribute.startsWith(preFix)){
				String newValue = String.valueOf(skuSeasonLink.getValue(attKey)).trim();
				String oldValue  = null;
				if( previousSPLLink != null) {
					 oldValue = String.valueOf(previousSPLLink.getValue(attKey)).trim();
				}
				
				if(newValue==null){
					newValue="";
				}
				if(oldValue==null){
					oldValue="";
				}
				//LOGGER.info("New Value ::>>"+newValue+",   OLD Value ::>>"+oldValue);
				
				if(!newValue.equals(oldValue)){
					isModified=true;
					break;
				}
			}
		}

		return isModified;
	}
	
	
	public static boolean checkSKUSeasonStraubValueModified(LCSSKUSeasonLink skuSeasonLink) throws WTException{
		boolean isModified = false;
		
		LCSSKUSeasonLink previousSPLLink=(LCSSKUSeasonLink) LCSSeasonQuery.findSeasonProductLink(skuSeasonLink, skuSeasonLink.getEffectSequence()-1);

		if ( previousSPLLink == null ) return false;
		
		for (int i = 0; i < excelLayoutAttributes.size(); i++)  {
			String attribute = excelLayoutAttributes.get(i);
			String attKey = attribute.split("\\.")[1];

			String preFix = "PRODUCTSEASONLINK."; 
			if("SKU".equalsIgnoreCase(skuSeasonLink.getSeasonLinkType()))
			{
				preFix = "SKUSEASONLINK."; 
			}
			if(attribute.startsWith(preFix)){
				String newValue = String.valueOf(skuSeasonLink.getValue(attKey)).trim();
				String oldValue = String.valueOf(previousSPLLink.getValue(attKey)).trim();
				if(newValue==null){
					newValue="";
				}
				if(oldValue==null){
					oldValue="";
				}

				
				if(!newValue.equals(oldValue)){
					isModified=true;
					break;
				}
			}
		}

		return isModified;
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
		lcslogentry.setFlexType(flextype);

		for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if(FormatHelper.hasContent(key))
			{
				lcslogentry.setValue(key, value);
			}
		}
		(new LCSLogEntryLogic()).saveLog(lcslogentry);

		LOGGER.info("createLogEntry END>>>>"+lcslogentry);
		return lcslogentry;
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

		FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(flextypeIdPath);
		AgronLogEntryQuery agroLogEntry=new AgronLogEntryQuery();
		SearchResults logEntryResults = agroLogEntry.findLogEntriesByCriteria(criteriaMap, logEntryType, null, null, null); 
		Collection<LCSLogEntry> logEntryObject= LCSLogEntryQuery.getObjectsFromResults(logEntryResults.getResults(), "OR:com.lcs.wc.foundation.LCSLogEntry:", "LCSLogEntry.IDA2A2");

		return logEntryObject;
	}


	  public static Collection findSKULinks(LCSProductSeasonLink prodLink)
	    throws WTException
	  {
	    PreparedQueryStatement statement = LCSSeasonQuery.findLCSSKUSeasonLinksForProductQuery(prodLink);
	    Collection skus = LCSQuery.getObjectsFromResults(statement, "OR:com.lcs.wc.season.LCSSKUSeasonLink:", "LCSSKUSeasonLink.idA2A2");
	    return skus;
	  }
	
}


