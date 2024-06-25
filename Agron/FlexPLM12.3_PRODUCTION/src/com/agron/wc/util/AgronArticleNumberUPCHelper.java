package com.agron.wc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.integration.outbound.AgronExportUtil;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectLogic;
import com.lcs.wc.moa.LCSMOAObjectQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.AsynchronizedProcessingStatus;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeMaster;
import com.lcs.wc.skusize.SKUSizeProcessLogEntry;
import com.lcs.wc.skusize.SKUSizeProcessingType;
import com.lcs.wc.skusize.SKUSizeQuery;
import com.lcs.wc.skusize.SKUSizeToSeason;
import com.lcs.wc.skusize.SKUSizeUtility;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.util.WTException;

/**
 * Helper Class for Article/UPC 
 */
public final class AgronArticleNumberUPCHelper {

	private static final String YEAR_KEY = LCSProperties.get("com.agron.wc.interface.season.yearKey","year");
	private static final String SKU_MOA_KEY = LCSProperties.get("com.agron.wc.product.SKU.MOA.attKey");  
	private static final String SKU_SOCKS_MOA_KEY = LCSProperties.get("com.agron.wc.product.SKU.SOCKS.MOA.attKey"); //agrUPCArticleSocks

	private static final String SKUSIZE_ARTICLE_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.articleNumberKey","agrArticleNumber");
	private static final String SKUSIZE_UPC_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.upcNumberKey","upcCode");
	private static final String ADOPTED_BOOLEAN_KEY = LCSProperties.get("com.agron.wc.product.SKU.adopted.attKey","agrAdopted");
	private static final String ARTICLE_NUMBER_SKU_ATTKEY = LCSProperties.get("com.agron.wc.product.SKU.ArticleNumber.attKey","agrArticle");


	public static final Logger log = LogManager.getLogger(AgronArticleNumberUPCHelper.class);

	private static final int QUEUE_SIZE;
	static {
		int queueSize = LCSProperties.get("com.lcs.wc.skusize.SKUSizeUtility.processingQueueSize", 1);
		QUEUE_SIZE = queueSize > 0 ? queueSize : 1;
	}


	/**
	 * Check if ProductSizeCategory is valid,PSC is only valid if PSC is associated to any season
	 * @param psc
	 *            - ProductSizeCategory which needs to be validated.
	 * @return isValidPSC
	 *           
	 */
	public static boolean isValidPsc(ProductSizeCategory psc){
		boolean isValidPSC=false;
		if(psc !=null) {
			try{
				Collection<ProdSizeCategoryToSeason>   pscToSeasonCollection=new SizingQuery().findRelatedProdSizeCategoryToSeason(psc);
				if(pscToSeasonCollection !=null && pscToSeasonCollection.size()>0) {
					isValidPSC= true;
				}
				log.debug("isValidPsc>>PSC:"+psc+"::"+psc.getName()+"::pscToSeasons::"+pscToSeasonCollection.size()+"::isValidPSC>>"+isValidPSC);
			}catch(Exception e){
				log.error("Error while getting findRelatedProdSizeCategoryToSeason: "+psc+"::"+e.getMessage());
				e.printStackTrace();
			}
		}
		return isValidPSC;

	}

	public static HashMap<String,FlexObject> getSizeMoaRowMap(Collection moaRows){
		log.debug("method getSizeMoaRowMap START");
		HashMap<String,FlexObject> sizeMoaRowMap=new HashMap<String,FlexObject>();
		try{
			if(moaRows !=null && moaRows.size()>0  ){
				Iterator moaIterator = moaRows.iterator();
				String size="";
				while (moaIterator.hasNext()) {
					FlexObject skuSizeMoaData = (FlexObject) moaIterator.next();
					size=skuSizeMoaData.getData("AGRSIZE") ;
					if(size !=null && size.trim() !=""){
						sizeMoaRowMap.put(size,skuSizeMoaData);
					}
				}	
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		log.debug("method getSizeMoaRowMap END::sizeMoaRowMap::"+sizeMoaRowMap);
		return sizeMoaRowMap;
	}


	/**
	 * method to delete the existing moaTable
	 * @param moaTable
	 *            - moaTable which needs to be deleted
	 * @throws WTException.
	 */
	public static void dropMoaTabel(LCSMOATable moaTable) throws WTException{
		// Delete the Existing MOA Table 
		if(moaTable !=null) {
			LCSMOAObjectLogic moaLogic = new LCSMOAObjectLogic();
			Collection rowIds = moaTable.getRowIds(null);
			if(rowIds !=null && rowIds.size()>0) {
				log.debug("rowIds>>>>>>>>>>> "+rowIds); 
				Iterator<String> itr = rowIds.iterator();
				while(itr.hasNext()){			
					String key = itr.next(); 
					LCSMOAObject  moa  = (LCSMOAObject) LCSMOAObjectQuery.findObjectById("OR:com.lcs.wc.moa.LCSMOAObject:"+key);				
					//moaTable.dropRow(key);   
					try {
						moaLogic.delete(moa);  
					} catch (Exception e) {			
						System.out.println("Error Deleting Row "); 
					}
				}	
			}
		}
	}


	/**
	 * method to drop the row from moaTable
	 * @param moaTable
	 *            - moaTable from which row needs to be dropped.
	 * @param moaRow
	 *            - moa row which needs to be deleted            
	 * @throws WTException.
	 */
	public static void dropMoaRow(LCSMOATable moaTable,FlexObject moaRow) {
		// Delete the Existing MOA Table 
		if(moaRow !=null && moaTable !=null) {
			String moaRowKey=(String) moaRow.get("OID");
			LCSMOAObjectLogic moaLogic = new LCSMOAObjectLogic();
			try {
				if(FormatHelper.hasContentAllowZero(moaRowKey)){
					LCSMOAObject  moa  = (LCSMOAObject) LCSMOAObjectQuery.findObjectById("OR:com.lcs.wc.moa.LCSMOAObject:"+moaRowKey);				
					log.info("Delete MOA Row::"+moaRowKey+"::"+moa);
					//moaTable.dropRow(moaRowKey);
					if(moa !=null){
						moaLogic.delete(moa);
					}	
				}
			} catch (Exception e) {			
				log.error("Error while Deleting moa Row::"+moaRowKey+"::"+moaRow); 
			}
		}
	}

	public static List<String> getValidSizeCombinationList(ArrayList<String> sizes1Collection,ArrayList<String> sizes2Collection) {
		List<String> validCombinations = new ArrayList<String>();
		if(sizes2Collection.size() > 0 && sizes1Collection.size() ==  sizes2Collection.size()) {
			for(int i=0; i<sizes1Collection.size(); i++) {
				try {
					validCombinations.add(sizes1Collection.get(i)+"_"+sizes2Collection.get(i));
				} catch(IndexOutOfBoundsException execp) {
					execp.printStackTrace();
				}
			}
		} 
		return validCombinations;
	}

	public static boolean isCopiedBagORSocksTypeSKU(LCSSKU sku) {
		boolean isCopied=false;
		try {
			LCSSKU copiedFromSKU=(LCSSKU)sku.getCopiedFrom();
			if(copiedFromSKU !=null) {
				copiedFromSKU = (LCSSKU) VersionHelper.getVersion( copiedFromSKU,"A");
				String copiedFromType=copiedFromSKU.getFlexType().getTypeDisplayName();
				String copiedToSkuType=sku.getFlexType().getTypeDisplayName();
				String copiedToSkuFullType=sku.getFlexType().getFullName(true);
				log.info("copiedFrom:::"+copiedFromSKU.getName()+"::"+copiedFromType+"::copiedToSkuType:-"+copiedToSkuType);
				if((copiedFromType.equalsIgnoreCase("Backpacks") && copiedToSkuType.equalsIgnoreCase("Bags")) 
						|| ( copiedFromType.equalsIgnoreCase("SMU & Clearance") && copiedToSkuFullType.contains("Adidas"))){
					isCopied= true;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		log.info("isCopied::"+sku+"::"+isCopied);
		return isCopied;

	}

	/**
	 * There will be MOA attribute on SKU level ,get Article UPC Number MOA Table by SKU
	 * @param sku
	 *            - sku whose moa attribute needs to find.
	 * @return - LCSMOATable
	 * @throws WTException.
	 */
	public static LCSMOATable getMoatableBySku(LCSSKU sku){
		LCSMOATable articleUPCMOATable=null;
		try {
			String moaKey=	SKU_MOA_KEY;
			if(sku.getFlexType().getFullNameDisplay(true).contains("Socks")){
				moaKey=	SKU_SOCKS_MOA_KEY; 
			}
			articleUPCMOATable= (LCSMOATable)sku.getValue(moaKey);
			if(moaKey.equals(SKU_SOCKS_MOA_KEY) &&  (articleUPCMOATable==null || articleUPCMOATable.getRows().isEmpty())) {
				log.info("getMoaTable By key:: "+moaKey);
				articleUPCMOATable= (LCSMOATable)sku.getValue(SKU_MOA_KEY);	
			}
			log.debug("MOATable:: "+articleUPCMOATable);
		}catch(Exception e) {
			log.error("Exception while getting articleUPCMOATable:"+e.getMessage());
			e.printStackTrace();
		}
		return articleUPCMOATable;
	}

	public static int getLatestSeasonYear(LCSSKU sku){
		int latestYear=0;
		try{
			Collection seasonsCol = new LCSSeasonQuery().findSeasons(sku.getMaster());
			log.info("getLatestSeasonYear>>"+sku+"::"+sku.getName()+"::seasonsCol::"+seasonsCol);
			LCSSeason season = null;
			LCSSeasonProductLink spl = null;
			int year = 0;
			if (seasonsCol.size() > 0) {
				Iterator seasonIter = seasonsCol.iterator();
				while(seasonIter.hasNext()){
					season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
					spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
					if(!spl.isSeasonRemoved()){
						year =getSeaonYear(season);
						if (latestYear < year) {
							latestYear = year;
						}
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		log.info("latestYear::"+latestYear+"::"+sku.getName());
		return latestYear;
	}
   
	public static boolean isSKUUsedInAgronSeasonType(LCSSKU sku) {
		boolean isAgronType = false;
		try{
			Collection seasonsCol = new LCSSeasonQuery().findSeasons(sku.getMaster());
			log.info("isSKUUsedInAgronSeasonType>>"+sku+"::"+sku.getName()+"::seasonsCol::"+seasonsCol.size());
			LCSSeason season = null;
			LCSSeasonProductLink spl = null;
			if (seasonsCol.size() > 0) {
				Iterator seasonIter = seasonsCol.iterator();
				while(seasonIter.hasNext()){
					season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
					spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
					if(!spl.isSeasonRemoved()){
						if(AgronExportUtil.isAgronSeasonType(season)) {
							log.debug("Agron Type Season Found:"+season.getName());
							isAgronType=true;
							return true;
						}
					}
				}
			}
		}catch (Exception e){
			log.error("Exception in isSKUUsedInAgronSeasonType:"+sku+"::"+sku.getName()+""+e.getLocalizedMessage());
			e.printStackTrace();
		}
		return isAgronType;
	}
	public static int getSeaonYear(LCSSeason season) {
		int numericYear=0;
		try {
			String year = (String) season.getValue(YEAR_KEY);
			FlexTypeAttribute fta = season.getFlexType().getAttribute(YEAR_KEY);
			AttributeValueList avl = fta.getAttValueList();
			year = avl.getValue(year, Locale.getDefault());
			if(FormatHelper.hasContent(year)){
				numericYear=Integer.parseInt(year);	

			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return numericYear;
	}
	public static void regenerateSKUSizes(Persistable persistable,LCSPartMaster prodMaster){
		try {
			log.info("regenerateSKUSizes START>>>>>persistable::"+persistable+"::prodMaster::"+prodMaster);
			String prodMasterId = FormatHelper.getObjectId(prodMaster);
			LCSProduct product=(LCSProduct) VersionHelper.latestIterationOf(prodMaster);
			String queueName = getQueueName(prodMasterId);
			log.info("queueName>>>"+queueName+"::Product>>"+product.getName());
			SKUSizeProcessLogEntry logEntry = new SKUSizeProcessLogEntry();
			logEntry.setCreationDateTime(new Date());
			logEntry.setStatus(AsynchronizedProcessingStatus.WAIT_IN_QUEUE);
			logEntry.setType(SKUSizeProcessingType.REGENERATE_MISSING_SKUSIZE_OBJECTS);
			logEntry.setProductNumericId(FormatHelper.getNumericVersionIdFromObject(product));
			logEntry.setQueueName(queueName);
			logEntry = logEntry.save();
			log.info("logEntry::"+logEntry.getFlexObjectId());
			LinkedHashMap<String, Boolean> objects=new LinkedHashMap();
			objects.put(FormatHelper.getObjectId(persistable),false);
			log.info("objects::"+objects);

			SKUSizeUtility.syncCreateUpdateSKUSizeObjects(objects,logEntry.getFlexObjectId(),true, true, false);
		} catch (WTException e) {
			System.out.println("exception while regenerateSKUSizes:::"+persistable+"::error>>"+e.getMessage());
			e.printStackTrace();
		}
		log.info("regenerateSKUSizes COMPLETED>>>>>>>>>"+persistable+"::prodMaster>>"+prodMaster);
	}

	private static String getQueueName(String prodMasterOid) throws WTException {
		String queueName = SKUSizeProcessLogEntry.findOcupiedQueueName(prodMasterOid);
		if (!FormatHelper.hasContent(queueName)) {
			int minQueueSize = Integer.MAX_VALUE;
			for (int i = 0; i < QUEUE_SIZE; ++i) {
				String tName = "SKUSizeProcessingQueue" + i;
				ProcessingQueue queue = QueueHelper.manager.getQueue(tName);
				if (queue == null) {
					queue = QueueHelper.manager.createQueue(tName, true);
				}
				int entriesCount = queue.getTotalEntryCount();
				log.debug("queue::"+queue+"::entriesCount::"+entriesCount);
				if (entriesCount < minQueueSize) {
					queueName = tName;
					minQueueSize = entriesCount;
					if (entriesCount == 0) {
						break;
					}
				}
			}
		}
		return queueName;
	}


	public static void migrateMoaToSkuSizes(LCSSKU sku,ProductSizeCategory psc) {
		log.info("migrateMoaToSkuSizes START SKU>>>"+sku+"::"+sku.getName()+"::"+psc);
		try {
			if(sku !=null && psc !=null) {
				sku = (LCSSKU) VersionHelper.getVersion((LCSSKU) VersionHelper.latestIterationOf(sku.getMaster()),"A"); 
				boolean adoptedValue=Boolean.valueOf(String.valueOf(sku.getValue(ADOPTED_BOOLEAN_KEY)));
				String articleNumberValue= (String)sku.getValue(ARTICLE_NUMBER_SKU_ATTKEY);
				log.info("articleNumberValue:"+articleNumberValue+"::adopted:>"+adoptedValue);
				if(FormatHelper.hasContent(articleNumberValue) && AgronArticleNumberUPCHelper.isCopiedBagORSocksTypeSKU(sku) ){
					LCSMOATable moaTable=AgronArticleNumberUPCHelper.getMoatableBySku(sku);
					if(moaTable !=null) {
						Collection moaRows= moaTable.getRows();
						if (moaRows !=null && moaRows.size()>0) {
							SearchResults skuSizeSr =SKUSizeQuery.findSKUSizesForPSC(psc, sku.getMaster(), (String) null, (String) null);
							log.info("skuSizeSr Results::"+skuSizeSr.getResultsFound());
							Iterator skuSizeIterator = LCSQuery.iterateObjectsFromResults(skuSizeSr,"VR:com.lcs.wc.skusize.SKUSize:", "SKUSIZE.BRANCHIDITERATIONINFO");
							HashMap<String,FlexObject> upcArticleMoaTableMap=AgronArticleNumberUPCHelper.getSizeMoaRowMap(moaRows);
							while (skuSizeIterator.hasNext()) {
								SKUSize skuSize = (SKUSize) skuSizeIterator.next();
								if(skuSize.isActive()) {
									SKUSizeMaster skuSizeMaster=(SKUSizeMaster) skuSize.getMaster();
									String skuSize1Value=skuSizeMaster.getSizeValue();
									String skuSize2Value= skuSizeMaster.getSize2Value();
									ArrayList<String> sizes1Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSizeValues()));
									ArrayList<String> sizes2Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSize2Values()));
									List<String> validCombinations=AgronArticleNumberUPCHelper.getValidSizeCombinationList(sizes1Collection,sizes2Collection);

									if(FormatHelper.hasContentAllowZero(skuSize1Value) && (sizes2Collection.size()<=1 || validCombinations.contains(skuSize1Value+"_"+skuSize2Value))) {
										if(upcArticleMoaTableMap.containsKey(skuSizeMaster.getSizeValue()) && upcArticleMoaTableMap.get(skuSizeMaster.getSizeValue()) !=null){
											try{
												FlexObject moaRow=upcArticleMoaTableMap.get(skuSizeMaster.getSizeValue());
												log.info("Move row::"+skuSize+"::"+moaRow);
												if(!FormatHelper.hasContent((String)skuSize.getValue(SKUSIZE_UPC_NBMER_KEY)) && FormatHelper.hasContent(moaRow.getData("AGRUPCNUMBER"))) {
													skuSize.setValue(SKUSIZE_UPC_NBMER_KEY, moaRow.getData("AGRUPCNUMBER"));	
												}
												if(!FormatHelper.hasContent((String)skuSize.getValue(SKUSIZE_ARTICLE_NBMER_KEY))  && FormatHelper.hasContent(moaRow.getData("AGRARTICLENUMBER"))) {
													skuSize.setValue(SKUSIZE_ARTICLE_NBMER_KEY, moaRow.getData("AGRARTICLENUMBER"));	
												}
												boolean isAssigned=moaRow.getBoolean("AGRADOPTED");
												log.debug("MOA ROW isAssigned:"+isAssigned);
												if(!isAssigned) {
													skuSize.setActive(false);	
												}
												PersistenceServerHelper.manager.update(skuSize);
												AgronArticleNumberUPCHelper.dropMoaRow(moaTable, moaRow);
											}catch(Exception e){
												log.error("Exception while moving moa row ::"+skuSize+"::"+skuSizeMaster.getSizeValue());
												e.printStackTrace();
											}
										}
									}
								}
							}

						}
					}
				}	
			}
		}catch(Exception e) {
			log.error("Exception while moa data migration::"+sku+"::"+psc);
			e.printStackTrace();
		}
		log.info("migrateMoaToSkuSizes END>>>"+sku+"::"+sku.getName());
	}
	
	
	public static void regenerateMissingSKUSizeToSeasonLinks(LCSProduct product,LCSSKU sku,LCSSeason season,LCSSKUSeasonLink ssLink) {
		if(ssLink.getEffectSequence()==0) {
			try {
				log.info("regenerateMissingSKUSizeToSeasonLinks START>>>>SEASON::"+season+">>"+season.getName()+"::product::"+product+">>"+product.getName()+"::sku::"+sku+">>"+sku.getName());
				boolean isRegenerateRequired=true;
				log.info("regenerateSKUSizes for SKU START>>>>"+sku);
				AgronArticleNumberUPCHelper.regenerateSKUSizes(sku,sku.getProductMaster());
				log.info("regenerateSKUSizes for SKU END>>>>>");
				SizingQuery squery = new SizingQuery();
				SKUSizeQuery skuSizeQuery = new SKUSizeQuery();
				 //Check if SKUSizeToSeasonLinks are available in source PSD
				Collection<ProdSizeCategoryToSeason> sourcePSDToSeasons = LCSQuery.getObjectsFromResults(
						squery.findPSDByProductAndSeason((Map) null, product, season, (FlexType) null,
								(Collection) null, (Collection) null),
						"OR:com.lcs.wc.sizing.ProdSizeCategoryToSeason:", "PRODSIZECATEGORYTOSEASON.IDA2A2");
				log.info("sourcePSDToSeasons SIZES:"+sourcePSDToSeasons.size()+ "::"+sourcePSDToSeasons);
				Iterator sourcePSDToSeasonIt = sourcePSDToSeasons.iterator();
				while (sourcePSDToSeasonIt.hasNext()) {
					ProdSizeCategoryToSeason sourcePSDToSeason = (ProdSizeCategoryToSeason) sourcePSDToSeasonIt.next();
					Collection<SKUSizeToSeason> sourceSKUSizeToSeasons = LCSQuery.getObjectsFromResults(
							skuSizeQuery.findSKUSizeToSeasonsByPSDandSKU(sourcePSDToSeason, sku),
							"VR:com.lcs.wc.skusize.SKUSizeToSeason:", "SKUSIZETOSEASON.BRANCHIDITERATIONINFO");
					log.info("sourcePSDToSeason::"+sourcePSDToSeason+"::sourceSKUSizeToSeasons>>"+sourceSKUSizeToSeasons.size());
					 //SKUSizeToSeason needs to be created if SKUSizeToSeasonLinks are not created in source PSD
					if(sourceSKUSizeToSeasons.size()>0) {
						isRegenerateRequired=false;
						break;
					}
				}
				if(isRegenerateRequired) {
					log.info("regenerateSKUSizes for SSLINK>>>");
					AgronArticleNumberUPCHelper.regenerateSKUSizes(ssLink,sku.getProductMaster());
				}

			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		log.info("regenerateMissingSKUSizeToSeasonLinks END>>>>"+sku+"::"+ssLink);
	}
	
	public static void deActivateSkuSizeToSeasonLinks(SKUSize skuSize,LCSSeason season) {
		log.debug("deActivateSkuSizeToSeasonLinks START::skuSize:"+skuSize+">>Season::"+season);
		try {
			//If season is not null then Seasonal SKU Size will be deactivated
			if(season !=null) {
				SKUSizeToSeason skuSizeToSeason=(new SKUSizeQuery()).getSKUSizeToSeasonBySKUSizeSeason(skuSize,season);
				if(skuSizeToSeason !=null) {
					log.debug("Deactivate skuSizeToSeason::"+skuSizeToSeason);
					skuSizeToSeason.setActive(false); 
					PersistenceServerHelper.manager.update(skuSizeToSeason);					
				}
			}else {
				//If season value is null then all Seasonal SKU Size will be deactivated
				Collection<SKUSizeToSeason> skuSizeToSeasonCol=SKUSizeQuery.getSKUSizeToSeasonsFromSKUSize(skuSize,true);	
				log.debug("skuSizeToSeasonColection Size::"+skuSizeToSeasonCol.size());
				Iterator<SKUSizeToSeason> skuSizeIter = skuSizeToSeasonCol.iterator();
				while (skuSizeIter.hasNext()) {
					SKUSizeToSeason skuSizeToSeason= skuSizeIter.next(); 
					if(skuSizeToSeason !=null) {
						log.debug("Deactivate skuSizeToSeason::"+skuSizeToSeason);
						skuSizeToSeason.setActive(false); 
						PersistenceServerHelper.manager.update(skuSizeToSeason);
					}
				}	
			}

		}catch(Exception e) {
			e.printStackTrace();
		}
		log.debug("deActivateSkuSizeToSeasonLinks END");
		
	}
	
}

