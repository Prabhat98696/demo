package com.agron.wc.skusize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.integration.outbound.AgronExportUtil;
import com.agron.wc.util.AgronArticleNumberUPCHelper;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProdSizeCategoryToSeasonMaster;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.ProductSizingLogic;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeMaster;
import com.lcs.wc.skusize.SKUSizeQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;

/**
 * SSP for deleting invalid SKUSize Object.
 * Only one SKUSize object should be created for each size 1 value.
 */
public final class AgronMoveArticleUPCMOATableToSKUSizes {


	private static final String SKUSIZE_ARTICLE_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.articleNumberKey","agrArticleNumber");
	private static final String SKUSIZE_UPC_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.upcNumberKey","upcCode");
	private static final String ARTICLE_NUMBER_SKU_ATTKEY = LCSProperties.get("com.agron.wc.product.SKU.ArticleNumber.attKey","agrArticle");
	private static final String ADOPTED_BOOLEAN_KEY = LCSProperties.get("com.agron.wc.product.SKU.adopted.attKey","agrAdopted");
	private static final Logger log = LogManager.getLogger(AgronMoveArticleUPCMOATableToSKUSizes.class);

	/** 
	 *  Plugin should be on POST_PERSIST /POST_CREATE_PERSIST
	 *  There will be MOA attribute on SKU level. 
	 *   In case a backpack is copied into a Bag , or a Team socks is copied  to Socks then The MOA table data should be moved to SKU Size.
	 *   THe MOA table should show no records.
	 *   Only matching records based on size value  between SKU size and MOA table to be copied
	 *   and No UPC numbers or Article Numbers should be consumed from the Business Objects table.
	 * 
	 * @param obj
	 */
	public static void moveMoaDataToSkuSize(WTObject obj) {
		if(obj instanceof SKUSize) {
			log.debug("PLUGIN moveMoaDataToSku START::"+obj);
			try {
				SKUSize skuSize = (SKUSize) obj;
				SKUSizeMaster skuSizeMaster = (SKUSizeMaster) skuSize.getMaster();
				LCSSKU sku = (LCSSKU) VersionHelper.getVersion((LCSSKU) VersionHelper.latestIterationOf(skuSizeMaster.getSkuMaster()),"A"); 
				boolean adoptedValue=Boolean.valueOf(String.valueOf(sku.getValue(ADOPTED_BOOLEAN_KEY)));
				String articleNumberValue= (String)sku.getValue(ARTICLE_NUMBER_SKU_ATTKEY);
				log.debug("SKUMASTER:"+sku+"::adoptedValue:-"+adoptedValue+"::articleNumberValue:-"+articleNumberValue);
				if(FormatHelper.hasContent(articleNumberValue) && AgronArticleNumberUPCHelper.isCopiedBagORSocksTypeSKU(sku) ) {
					LCSMOATable moaTable=AgronArticleNumberUPCHelper.getMoatableBySku(sku);
					if(moaTable !=null) {
						Collection moaRows = moaTable.getRows();	
						log.debug("moaRows:::"+moaRows);
						if(moaRows !=null && moaRows.size()>0) {
							ProductSizeCategory psc = VersionHelper.latestIterationOf(skuSizeMaster.getPscMaster());
							boolean isValid=AgronArticleNumberUPCHelper.isValidPsc(psc);
							log.debug("Destination PSC:- "+psc+"::"+psc.getName()+"::isValid>>"+isValid);
							if(!isValid) {
								/*On SKU Copy MOA table data needs to be moved to seasonal PSC only.If the  SKU Sizes are already created in
								*the original ProductSizeCategory from which PSC is copied
								*in that case SKU sizes are get created before ProdSizeCategoryToSeason links are created. 
							    *So we need to find in the  original product if the copiedFrom PSC is present in the Season from which Product is copied 
								*IF PSC is  present in old season then move the data because if PSC is seasonal in old product  then Season Link will be 
								*created in  the copied Product  as well
								 */
								log.debug("Validate using source PSC and SKU");
								isValid=isOriginalPSCSeasonal(psc,sku);
							}	
							if(isValid){
								ArrayList<String> sizes1Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSizeValues()));
								ArrayList<String> sizes2Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSize2Values()));
								String   skuSize1Value=skuSizeMaster.getSizeValue();
								String   skuSize2Value=skuSizeMaster.getSize2Value();
								List<String> validCombinations=AgronArticleNumberUPCHelper.getValidSizeCombinationList(sizes1Collection,sizes2Collection);
								log.debug("validCombinations::"+validCombinations);
								if(!FormatHelper.hasContentAllowZero(skuSize2Value) || validCombinations.contains(skuSize1Value+"_"+skuSize2Value)){
									Iterator moaIterator = moaRows.iterator();
									while (moaIterator.hasNext()) {
										FlexObject moaRow = (FlexObject) moaIterator.next();
										String moaRowSizeValue=moaRow.getData("AGRSIZE") ;
										if(FormatHelper.hasContentAllowZero(moaRowSizeValue) && moaRowSizeValue.equals(skuSize1Value) ){
											log.debug("COPY MOA ROW TO SKU SIZE::"+moaRow);
											skuSize.setValue(SKUSIZE_ARTICLE_NBMER_KEY, moaRow.getData("AGRARTICLENUMBER"));
											skuSize.setValue(SKUSIZE_UPC_NBMER_KEY, moaRow.getData("AGRUPCNUMBER"));
											boolean isAssigned=moaRow.getBoolean("AGRADOPTED");
											log.debug("MOA ROW isAssigned::"+isAssigned);
											if(!isAssigned) {
												skuSize.setActive(false);	
											}
											PersistenceServerHelper.manager.update(skuSize);
											AgronArticleNumberUPCHelper.dropMoaRow(moaTable, moaRow);
											break;
										}

									}	
								}	
							}
						}
					}
				}		
			}catch(Exception e) {
				log.error("ERROR while moving moa data:"+e.getMessage());
				e.printStackTrace();
			}
			log.debug("PLUGIN moveMoaDataToSku END ::"+obj);
		}
	}
	
	public static void migrateMoaToSkuSizesByPSCTS(WTObject object) throws WTException {
		if(object instanceof ProdSizeCategoryToSeason){
			ProdSizeCategoryToSeason pscts = (ProdSizeCategoryToSeason) object;
			ProductSizeCategory psc = (ProductSizeCategory) VersionHelper.latestIterationOf(pscts.getSizeCategoryMaster());
			LCSProduct product = (LCSProduct) VersionHelper.getVersion((LCSProduct) VersionHelper.latestIterationOf(psc.getProductMaster()),"A");
			if(product.getCopiedFrom() !=null) {
			    LCSProduct productOrig = (LCSProduct) VersionHelper.getVersion((LCSProduct) VersionHelper.latestIterationOf(product.getCopiedFrom().getMaster()),"A"); 
				if(productOrig !=null) {
					String copiedProductFlexType=product.getFlexType().getFullName(true);
					String originalProductFlexType=productOrig.getFlexType().getFullName(true);
					if( (originalProductFlexType.contains("Backpacks") && copiedProductFlexType.contains("Bags")) 
							|| (originalProductFlexType.contains("SMU & Clearance") && copiedProductFlexType.contains("Adidas")) ) {
						ProdSizeCategoryToSeasonMaster psctsMaster = (ProdSizeCategoryToSeasonMaster) pscts.getMaster();
						LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(psctsMaster.getSeasonMaster());
						//int seasonYear=AgronArticleNumberUPCHelper.getSeaonYear(season);
						log.info("migrateMoaToSkuSizesByPSCTS START::PRODUCT::"+product.getName()+"::PSCTS::"+pscts+"::PSC::"+psc+"::Season>>"+season.getName());
						if(AgronExportUtil.isAgronSeasonType(season)) {
							if(pscts.getCopiedFrom()!=null) {
								log.info("COPIED ProdSizeCategoryToSeason Update: START"+pscts);
								ProductSizingLogic ps=new ProductSizingLogic();
								ps.save(pscts, true);
								log.info("COPIED ProdSizeCategoryToSeason Update: END"+pscts);
							}else if(pscts.getCarriedOverFrom() ==null) {
								log.debug("ADD PSC TO SEASON START::"+season.getName()+"::"+product.getName());
								SearchResults skuSizeSr =SKUSizeQuery.findSKUSizesForPSC(psc, null, (String) null, (String) null);
								log.info("skuSize Search Results for PSC:"+skuSizeSr.getResultsFound());
								if(skuSizeSr.getResultsFound()>0) {
									Collection<LCSSKU> skus = (new ProductHeaderQuery()).findSKUs(product, (LCSSourcingConfig) null, season, false,true);
									log.info("Season::"+season.getName()+":product:"+product.getName()+":SKUs collection:"+skus.size());
									Iterator skuItr = skus.iterator();
									LCSSKU sku=null;
									while (skuItr.hasNext()) {
										sku = (LCSSKU) skuItr.next();
										log.info("------SKU SIZE MIGRATION START--------------"+sku.getName());
										AgronArticleNumberUPCHelper.migrateMoaToSkuSizes(sku,psc);
										log.info("--------SKU SIZE MIGRATION END---------------"+sku.getName());
									}	
								}
								log.debug("ADD PSC TO SEASON END"+season.getName()+"::"+product.getName());
							}	
						}
					}
				}
				log.info("migrateMoaToSkuSizesByPSCTS END::PRODUCT::"+product.getName()+"::PSCTS::"+pscts+"::PSC::"+psc);
				
			}
			log.info("migrateMoaToSkuSizesByPSCTS END>>PSCTS::"+pscts);
		}
	}
	
	
	
	
	/** 
	 * Method to find if the copied ProductSizeCategory is present in the older Season from which 
	 * product is copied.
	 * @param psc  copied ProductSizeCategory
	 * @param sku  copied SKU
	 */
	public static boolean isOriginalPSCSeasonal(ProductSizeCategory psc,LCSSKU sku) {
		log.info("isOriginalPSCSeasonal>>>>Source PSC::"+psc);
		boolean isSeasonal=false;
		try {
			if(psc.getCopiedFrom() !=null) {
				//Get B version of SKU to get the copied to  season
				LCSSKU	skuB = (LCSSKU) VersionHelper.getVersion((LCSSKU) VersionHelper.latestIterationOf(sku.getMaster()),"B"); 
				log.debug("skuB>>>"+skuB);
				if(skuB !=null) {
					LCSSeason seasonUsed=skuB.findSeasonUsed();
					if(seasonUsed !=null) {
						log.debug("currentSeason>>>"+seasonUsed +"::::"+seasonUsed.getName());
						LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, seasonUsed);
						log.debug("spl>>>"+spl +":::Copied FROM SPL>>"+spl.getCopiedFrom());
						if(spl !=null && !spl.isSeasonRemoved() && spl.getCopiedFrom() !=null) {
							LCSSeasonProductLink copiedFromSPL=	(LCSSeasonProductLink) spl.getCopiedFrom();
							LCSSeason copiedFromSeason = SeasonProductLocator.getSeasonRev(copiedFromSPL);
							log.debug("copiedFromSeason>>>"+copiedFromSeason+"::"+copiedFromSeason.getName());
							//Check if source PSC is present in the older season from which product is copied
							ProductSizeCategory sourcePSC =(ProductSizeCategory)VersionHelper.latestIterationOf(psc.getCopiedFrom().getMaster());
							log.debug("sourcePSC>>>"+sourcePSC);
							if(sourcePSC !=null) {
								ProdSizeCategoryToSeason   prodSizeCategoryToSeason=new SizingQuery().getProdSizeCategoryToSeason(sourcePSC, copiedFromSeason);
								log.debug("prodSizeCategoryToSeason>>>"+prodSizeCategoryToSeason);
								if(prodSizeCategoryToSeason !=null) {
									isSeasonal=true;
								}	
							}
						}	
					}
				}
			}	
		}catch(Exception e) {
			e.printStackTrace();
		}
		log.info("isOriginalPSCSeasonal::"+isSeasonal);
		return isSeasonal;
	}
	
}
