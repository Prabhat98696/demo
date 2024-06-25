package com.agron.wc.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.integration.outbound.AgronExportUtil;
import com.agron.wc.util.AgronArticleNumberUPCHelper;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeMaster;
import com.lcs.wc.skusize.SKUSizeQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;

public class AgronCarryoverMoveArticleUPCMoaTable {

	public static final Logger log = LogManager.getLogger(AgronCarryoverMoveArticleUPCMoaTable.class);
	//private static final int CARRY_OVER_LOGIC_FROM_YEAR= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.enable.CarryoverFromSeasonYear",2022);
	private static final String SKUSIZE_ARTICLE_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.articleNumberKey","agrArticleNumber");
	private static final String SKUSIZE_UPC_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.upcNumberKey","upcCode");
	private static final String ADOPTED_BOOLEAN_KEY = LCSProperties.get("com.agron.wc.product.SKU.adopted.attKey","agrAdopted");
	private static final String ARTICLE_NUMBER_SKU_ATTKEY = LCSProperties.get("com.agron.wc.product.SKU.ArticleNumber.attKey","agrArticle");
	
	
	/**
	 *   On SKU carryover There will be MOA attribute on SKU level. 
	 *   Move the Article UPC MOA table data from Pre SS22 seasons to SS22 and beyond seasons in case of the following scenarios
	 *   Users Carries over old styles to new Seasons. The matching  MOA table data should be moved to SKU Size.THe MOA table should show no records.
	 * @param object
	 * @throws WTException
	 */
	public static void moveMoaDataToSkuSizes(WTObject object) throws Exception {
		if (object instanceof LCSSKUSeasonLink) {
			LCSSKUSeasonLink ssLink = (LCSSKUSeasonLink)object;
			LCSSeason season = SeasonProductLocator.getSeasonRev(ssLink);
		     //int currentSeasonYear = AgronArticleNumberUPCHelper.getSeaonYear((LCSSeason) VersionHelper.latestIterationOf(ssLink.getSeasonMaster()));
			if(ssLink.getSeasonLinkType().equalsIgnoreCase("SKU") && ssLink.isEffectLatest() && !ssLink.isSeasonRemoved()  && ssLink.getCarriedOverFrom() !=null && AgronExportUtil.isAgronSeasonType(season) ){
				LCSSeason sourceSeason=(LCSSeason) VersionHelper.latestIterationOf(ssLink.getCarriedOverFrom().getSeasonMaster());
				//int carriedOverFromSeasonYear =AgronArticleNumberUPCHelper.getSeaonYear((LCSSeason) VersionHelper.latestIterationOf(ssLink.getCarriedOverFrom().getSeasonMaster()));
				if(!AgronExportUtil.isAgronSeasonType(sourceSeason)) {
					log.info("CarryoverMoveArticleUPCMoaTable START::>>>"+object+"::"+ssLink.getEffectSequence());
					LCSProduct product = SeasonProductLocator.getProductARev(ssLink);
					LCSSKU sku = SeasonProductLocator.getSKUARev(ssLink);
				    AgronArticleNumberUPCHelper.regenerateMissingSKUSizeToSeasonLinks(product,sku,sourceSeason,ssLink);
					boolean adoptedValue=Boolean.valueOf(String.valueOf(sku.getValue(ADOPTED_BOOLEAN_KEY)));
					String articleNumberValue= (String)sku.getValue(ARTICLE_NUMBER_SKU_ATTKEY);
					if(FormatHelper.hasContent(articleNumberValue)){
						LCSMOATable moaTable=AgronArticleNumberUPCHelper.getMoatableBySku(sku);
						if(moaTable !=null) {
							Collection moaRows= moaTable.getRows();
							if (moaRows !=null && moaRows.size()>0) {
								SearchResults sr = (new SizingQuery()).findPSDByProductAndSeason((Map) null, product, season,
										(FlexType) null, new Vector(), new Vector());
								log.info("findPSDByProductAndSeason:::"+sr.getResultsFound());
								Iterator psctsIterator = LCSQuery.iterateObjectsFromResults(sr,"OR:com.lcs.wc.sizing.ProdSizeCategoryToSeason:", "PRODSIZECATEGORYTOSEASON.IDA2A2");
								psctsIterator= sortedIterator(psctsIterator);
								while (psctsIterator.hasNext()) {
									ProdSizeCategoryToSeason pscts = (ProdSizeCategoryToSeason) psctsIterator.next();
									ProductSizeCategory psc = (ProductSizeCategory) VersionHelper.latestIterationOf(pscts.getSizeCategoryMaster());
									SearchResults skuSizeSr =SKUSizeQuery.findSKUSizesForPSC(psc, sku.getMaster(), (String) null, (String) null);
									log.debug("skuSizeSr::"+skuSizeSr.getResultsFound()+"::"+skuSizeSr);
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
														if(!FormatHelper.hasContent((String)skuSize.getValue(SKUSIZE_UPC_NBMER_KEY)) && FormatHelper.hasContent(moaRow.getData("AGRUPCNUMBER"))) {
															skuSize.setValue(SKUSIZE_UPC_NBMER_KEY, moaRow.getData("AGRUPCNUMBER"));	
														}
														if(!FormatHelper.hasContent((String)skuSize.getValue(SKUSIZE_ARTICLE_NBMER_KEY))  && FormatHelper.hasContent(moaRow.getData("AGRARTICLENUMBER"))) {
															skuSize.setValue(SKUSIZE_ARTICLE_NBMER_KEY, moaRow.getData("AGRARTICLENUMBER"));	
														}
														boolean isAssigned=moaRow.getBoolean("AGRADOPTED");
														log.debug("MOA ROW isAssigned::"+isAssigned);
														if(!isAssigned) {
															skuSize.setActive(false);	
														}
														PersistenceServerHelper.manager.update(skuSize);
														AgronArticleNumberUPCHelper.dropMoaRow(moaTable, moaRow);
													}catch(Exception e){
														log.debug("Exception while moving moa row ::"+skuSize+"::"+skuSizeMaster.getSizeValue());
														e.printStackTrace();
													}
												}
											}
										}
									}
									break;
								}	
							}
						}
					}
					log.info("CarryoverMoveArticleUPCMoaTable END>>>>>>");
				}
				
			}
		}
	}
	
	public static Iterator sortedIterator(Iterator it) {
		List<ProdSizeCategoryToSeason> list = new ArrayList();
		if(it !=null) {
			while (it.hasNext()) {
				list.add((ProdSizeCategoryToSeason)it.next());
			}
			if(!list.isEmpty()) {
				list.sort(Comparator.comparing(ProdSizeCategoryToSeason::getCreateTimestamp));
			}
		}
		return list.iterator();
	}
}
