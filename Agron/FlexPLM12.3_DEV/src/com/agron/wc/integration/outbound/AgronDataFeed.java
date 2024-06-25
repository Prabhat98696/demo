package com.agron.wc.integration.outbound;

import com.agron.wc.integration.util.*;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.datatypes.FlexUOM;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObjectQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.*;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.WTObject;
import wt.util.WTException;
import java.text.*;
import com.agron.wc.integration.outbound.AgronExportUtil;


//import com.lcs.wc.season.LCSSeasonProductLink;

/**
 * 

 * 
 * @author Mallikarjuna Savi
 * @version 2.0
 * 
 */
public class AgronDataFeed {
	/**
	 * Defined to store the constant value.
	 */
	public static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");
	private static final String EFFECTIVE_RETAILPRICE_DATE_KEY = LCSProperties
			.get("com.agron.wc.interface.spl.retailEffectiveDateKey");
	//private static final String MAS_MEMO_CODE_KEY = LCSProperties.get("com.agron.wc.interface.spl.masMemoCodeKey");
	//private static final String MAS_MEMO_START_DATE_KEY = LCSProperties.get("com.agron.wc.interface.spl.masMemoStartDateKey");
	//private static final String MAS_MEMO_END_DATE_KEY = LCSProperties.get("com.agron.wc.interface.spl.masMemoEndDateKey");
	private static final String EFFECTIVE_WHOLESALE_DATE_KEY = LCSProperties
			.get("com.agron.wc.interface.spl.wholeSaleEffectiveDateKey");
	private static final String RETAILPRICE_KEY = LCSProperties.get("com.agron.wc.interface.spl.retailKey");
	private static final String WHOLESALE_KEY = LCSProperties.get("com.agron.wc.interface.spl.wholeSaleKey");
	private static final String SEASONTYPE_KEY = LCSProperties.get("com.agron.wc.interface.season.seasonTypeKey");
	private static final String YEAR_KEY = LCSProperties.get("com.agron.wc.interface.season.yearKey");
	private static final String ADOPTED_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.adoptedKey");
	private static final String COLORWAY_MOA_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moaKey");
	private static final String COLLECTION_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.product.collectionKey");
	//private static final String SUBCOLLECTION_KEY = LCSProperties.get("com.agron.wc.interface.attributes.product.subCollectionKey");
	private static final String LINE_KEY = LCSProperties.get("com.agron.wc.interface.season.lineKey");
	private static final String KEY_PAIR = LCSProperties.get("com.agron.wc.interface.keyPair");

	private static final String FORECAST_KEY = LCSProperties.get("com.agron.wc.interface.spl.forecasteKey");
	private static final String SUSTAINABLE_KEY = LCSProperties.get("com.agron.wc.interface.spl.sustainableKey");
	private static final String SPORT_KEY = LCSProperties.get("com.agron.wc.interface.spl.sportKey");
	private static final String TECHNOLOGY_KEY = LCSProperties.get("com.agron.wc.interface.spl.technologyKey");
	private static final String COLORWAY_MOA_SOCKS_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moaSocksTypeKey");

	
	private boolean isErrorOccure = false;
	private Map<String, String> keyPair = new HashMap<String, String>();

	/**
	 * This constructor is used to set the boolean value.
	 * 
	 * @param isErrorOccure is Boolean object
	 */

	public AgronDataFeed(boolean isErrorOccure) {
		this.isErrorOccure = isErrorOccure;
	}

	/**
	 * This method is used to set the data based on the columns.
	 * 
	 * @param objects   is Collection object
	 * @param tableName is String object
	 * @return is Collection type
	 * FlexImport.csv
	 */
	@SuppressWarnings("unchecked")
	public Collection<FlexObject> addRelevantData(Collection<?> objects, String tableName) throws ParseException {
		Collection<FlexObject> dataCol = null;
		Iterator<?> objectsIter = null;
		LCSProduct product = null;
		FlexObject productData = null;
		FlexObject sourcingData = null;
		WTObject object = null;
		Collection<?> skuMasters = null;
		LCSPartMaster skuMaster = null;
		LCSSKU skuObjA = null;
		FlexObject completeRow = null;
		LCSSeason latestSeason = null;
		LCSSeason latestSkuSeason = null;
		LCSSourceToSeasonLink sourceTEst = null;
		String seasonName = "";
		int adoptedCount = 0;
		Collection<FlexObject> productDataCol = null;
		String collectionValue = "";
		String subCollectionValue = "";
		String brandValue = "";
		try {
			init();
			LOGGER.info((new StringBuilder("Objects : ")).append(objects).toString());
			String atts = LCSProperties
					.get((new StringBuilder("com.agron.wc.interface.attributes.")).append(tableName).toString());
			String colorwayAtts = LCSProperties.get("com.agron.wc.interface.attributes.LCSSKU");
			dataCol = new ArrayList<FlexObject>();
			AgronExportUtil exportUtil = new AgronExportUtil(isErrorOccure);
			AgronDataFeedUtil dataFeedUtil = new AgronDataFeedUtil();
			exportUtil.setKeyPair(keyPair);
			AgronDataHelper dataHelper = null;
			objectsIter = objects.iterator();
			FlexTypeAttribute attribute = null;
			AgronDataUtil dataUtil = new AgronDataUtil();
			Collection<?> moaCol = null;
			Collection<?> seasonsCol = new ArrayList<Object>();
			while (objectsIter.hasNext()) {
				dataHelper = new AgronDataHelper();
				Map<LCSSKU, LCSSKU> skuMap = new HashMap<LCSSKU, LCSSKU>();
				product = (LCSProduct) LCSQuery.findObjectById((new StringBuilder("VR:com.lcs.wc.product.LCSProduct:"))
						.append((String) objectsIter.next()).toString());
				LOGGER.info((new StringBuilder("Product Name : ")).append(product.getName()).toString());
				seasonsCol = (new LCSSeasonQuery()).findSeasons(product);
				seasonsCol = dataHelper.removedSeasonRemovedSeasons(product, seasonsCol);
				if (seasonsCol.size() > 0) {
					for (Iterator<?> sItr = seasonsCol.iterator(); sItr.hasNext(); dataCol.addAll(productDataCol)) {
						productDataCol = new ArrayList<FlexObject>();
						latestSeason = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) sItr.next());
						// SKIP this season if Downstream Integration is disabled.
						String isagrDownstreamIntegrated = (String) latestSeason.getValue("agrDownstreamIntegrated");
						if("agrNo".equals(isagrDownstreamIntegrated)) {
							LOGGER.info("addRelavantDataisagrDownstreamIntegrated = false :  Skipping Season " + latestSeason.getName());
							continue;
						}
						
						object = product;
						new LCSSeasonQuery();
						skuMasters = LCSSeasonQuery.getSKUMastersForSeasonAndProduct(latestSeason, product, false,
								false);
						if (skuMasters.size() > 0) {
							adoptedCount = 0;
							for (Iterator<?> skuiter = skuMasters.iterator(); skuiter.hasNext();) {
								skuMaster = (LCSPartMaster) skuiter.next();
								skuObjA = (LCSSKU) VersionHelper.getVersion(skuMaster, "A");
								if (!skuMap.containsKey(skuObjA)) {
									skuMap.put(skuObjA, skuObjA);
									Collection<?> skuSeasons = (new LCSSeasonQuery())
											.findSeasons((LCSPartMaster) skuObjA.getMaster());
									LOGGER.info((new StringBuilder("skuSeasons>>>>")).append(skuSeasons).toString());
									Collection<LCSSeasonMaster> skuSeasonCol = new ArrayList<LCSSeasonMaster>();
									if (skuSeasons.size() > 0) {
										for (Iterator<?> seasonIter = skuSeasons.iterator(); seasonIter.hasNext();) {
											LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
											if("agrNo".equals((String) season.getValue("agrDownstreamIntegrated"))) {
												continue;
												}
											LCSSeasonProductLink skuSPL = LCSSeasonQuery.findSeasonProductLink(skuObjA,
													season);
											if ((!skuSPL.isSeasonRemoved()) && season.isActive())
												skuSeasonCol.add((LCSSeasonMaster) season.getMaster());
										}

									}
									if(skuSeasonCol.size() == 0) {
										continue; // If there is no season to 
									}
									latestSeason = dataHelper.getLatestSeason(product, skuSeasonCol);
									productData = exportUtil.getData(object, atts);
									collectionValue = dataFeedUtil.getCollectionValue(product, latestSeason);
									productData.put((new StringBuilder("ERP."))
											.append((String) keyPair.get(COLLECTION_KEY)).toString(), collectionValue);
							

									seasonName = dataHelper.getFormattedSeasonName();
									exportUtil.setFormattedSeasonName(seasonName);
									// Modified to send skuSeasonCol for getting the price values directly if SKU is
									// in only one season
									productData.put("ERP.retail", dataUtil.getPrice(product, RETAILPRICE_KEY,
											dataHelper.getSeasons(), EFFECTIVE_RETAILPRICE_DATE_KEY, skuSeasonCol));
									productData.put("ERP.wholeSale", dataUtil.getPrice(product, WHOLESALE_KEY,
											dataHelper.getSeasons(), EFFECTIVE_WHOLESALE_DATE_KEY, skuSeasonCol));
																		
									/*Adding Product type value for Socks type*/
									
							
									
									productData.put("ERP.packagingType",AgronCSVGenerator.getData(product, "agrPackageType", false));
									
									if ("Backpacks".equals(product.getFlexType().getTypeDisplayName()) ||"Bags".equals(product.getFlexType().getTypeDisplayName()))
										
									{
									
										if (FormatHelper.hasContent(AgronCSVGenerator.getData(product, "agrCapacity", false)))
										productData.put("ERP.agrCapacity", AgronCSVGenerator.getData(product, "agrCapacity", false));
													
									
									}
									
								
									latestSkuSeason = dataHelper.getLatestSkuSeason(skuObjA, skuSeasonCol);
									 
									productData.put("ERP.agrGlobal",
											dataUtil.getSPLinkValue(product, latestSkuSeason, "agrGlobal"));

								
									productData.put("ERP.agrNsport",
											dataUtil.getSPLinkValue(product,latestSkuSeason, SPORT_KEY));
									productData.put("ERP.agrNTechnology",
											dataUtil.getSPLinkValue(product, latestSkuSeason, TECHNOLOGY_KEY));

									
									
									/* commented the code as per the Trello 386
									productData.put("ERP.agrTeam",
											dataUtil.getSPLinkValue(product,latestSkuSeason, "agrTeam"));*/
									
									LOGGER.info(
											(new StringBuilder("skuObjA>>>>")).append(skuObjA.getName()).toString());
									LCSSeasonProductLink latestSkuSPL = LCSSeasonQuery.findSeasonProductLink(skuObjA,
											latestSkuSeason);
								
									LOGGER.info((new StringBuilder("latestSkuSeason>>>>"))
											.append(latestSkuSeason.getName()).toString());

									productData.put("ERP.latestSkuSeason", latestSkuSeason);
									
									if ("HEADWEAR".equalsIgnoreCase((String)productData.get("ERP.PRODUCTTYPE")))									
									{
										if ( "Knits".equals(product.getFlexType().getTypeDisplayName()))
											
											productData.put("ERP.productType1","KNIT");
																		
										else
											productData.put("ERP.productType1","HEADWEAR");
										
									}	
									
									/* Change Request on 7/7/2020 considering Backpack in sage only if collection is BTS else Bag*/
									
									else if ("BACKPACK".equalsIgnoreCase((String)productData.get("ERP.PRODUCTTYPE"))||"BAG".equalsIgnoreCase((String)productData.get("ERP.PRODUCTTYPE")))
									{
										productData.put("ERP.productType1","BAG");
																							
										if("BTS".equalsIgnoreCase((String)productData.get("ERP.collection")))
										
										{
										productData.put("ERP.PRODUCTTYPE","BACKPACK");
										}
										
										else
										{
										productData.put("ERP.PRODUCTTYPE","BAG");
										}
									}
									 else 
									{
									 productData.put("ERP.productType1",(String)productData.get("ERP.PRODUCTTYPE"));
									}
									
									productData.put("ERP.agrColorwaystatus",AgronCSVGenerator.getData(latestSkuSPL, "agrColorwaystatus", false));
			

								if ((FormatHelper.hasContent((String)productData.get("ERP.agrColorwaystatus"))) && (productData.get("ERP.agrColorwaystatus")!=null))
									{
									
										if ("DISCONTINUED".equalsIgnoreCase((String)productData.get("ERP.agrColorwaystatus")) || "DROPPED".equalsIgnoreCase((String)productData.get("ERP.agrColorwaystatus")))
											{
											productData.put("ERP.resetSkuSizeactive", "YES");
											} 
								
									}
								
									productData.put("ERP.agrNSustainable",AgronCSVGenerator.getData(latestSkuSPL, "agrNSustainable", false));
									productData.put("ERP.agrDeliveryseason",AgronExportUtil.setDeliverySeason());
									productData.put("ERP.agrTotalForecast",AgronCSVGenerator.getData(latestSkuSPL, FORECAST_KEY, false));

									// Start - Added for Inline Colorways Is SMU ? field
									
										if (!"SMU & Clearance".equals(product.getFlexType().getTypeDisplayName())
											&& latestSkuSPL.getValue("agrIsSMU")!=null && "true".equalsIgnoreCase(latestSkuSPL.getValue("agrIsSMU").toString())) {
										LOGGER.info((new StringBuilder("Is SMU ?>>>>"))
												.append(latestSkuSPL.getValue("agrIsSMU")).toString());
										productData.put("ERP.SMU", "Y");
										productData.put("ERP.SMUACCOUNT",
												AgronCSVGenerator.getData(latestSkuSPL, "agrSMUAccountInline", false));
										productData.put("ERP.agrSMUSpecialHandling",
												AgronCSVGenerator.getData(latestSkuSPL, "agrSMUSpecialHandling", false));
										
										
										LOGGER.info((new StringBuilder("SMUACCOUNT>>>>")).append(
												AgronCSVGenerator.getData(latestSkuSPL, "agrSMUAccountInline", false))
												.toString());
									}
									productData.put("ERP.agrCapsule",
												AgronCSVGenerator.getData(latestSkuSPL, "agrCapsule", false));
												{
									if ( productData.get("ERP.agrCapsule")!=null && FormatHelper.hasContent((String)productData.get("ERP.agrCapsule")))
											{
											
											productData.put("ERP.agrIsBI", "Y");
										
											} 
										}
									// Sorting Catalogs of Catalogs
												
									String catalog = AgronCSVGenerator.getData(latestSkuSPL, "agrCatalog", false);
									if(FormatHelper.hasContent(catalog)) {
										
										List<String> catalogList = Arrays.asList(catalog.split(", "));
										Collections.sort(catalogList);
										catalog = String.join(", ", catalogList);								
									}
									productData.put("ERP.agrCatalog",catalog);
									
									if ((productData.get("ERP.agrCatalog")!=null) && (((String)productData.get("ERP.agrCatalog")).contains("TEAM")))
									{
										productData.put("ERP.agrTeam", "TRUE");
									} else
										{
									productData.put("ERP.agrTeam", "FALSE");
									}
									
									productData.put("ERP.agrAvailabilityDate",
											AgronCSVGenerator.getDateData(latestSkuSPL, "agrAvailabilityDate", false));
									productData.put("ERP.agrProductDescription",
											AgronCSVGenerator.getData(product, "agrProductDescription", false));
									//sourceTEst = LCSSourcingConfigQuery.getSourcingConfigForProduct((LCSPartMaster) product.getMaster());
									
									//LOGGER.info("Getting Primary Source Season Link");
									sourceTEst = LCSSourcingConfigQuery.getPrimarySourceToSeasonLink(product.getMaster(), latestSeason.getMaster());
									//LOGGER.info("Found Primary Source Season Link" + sourceTEst) ;
									sourceTEst = (LCSSourceToSeasonLink)VersionHelper.latestIterationOf(sourceTEst.getMaster());
									//LOGGER.info("Found Latest Iteration --" + sourceTEst) ;
									LCSSourcingConfig sc = (LCSSourcingConfig)VersionHelper.latestIterationOf(sourceTEst.getSourcingConfigMaster());

									sourcingData = (new AgronDataFeedUtil()).getSourceData(sc, product);
									productData.putAll(sourcingData);
									
								/*	if (sourceTEst != null && sourceTEst.size() > 0) {
										for (Iterator<?> sourcesT = sourceTEst.iterator(); sourcesT.hasNext();) {
											LCSSourcingConfig sc = (LCSSourcingConfig) sourcesT.next();
											if (sc.isP())
												sourcingData = (new AgronDataFeedUtil()).getSourceData(sc, product);
										}

										productData.putAll(sourcingData);
									} */
									
									LCSSeason latestSkuObjSeason = dataHelper.getLatestSkuSeason(skuObjA, skuSeasonCol);
									boolean IslatestSkuSesaon = false;
									if (latestSkuObjSeason != null)
										IslatestSkuSesaon = AgronExportUtil.isAgronSeasonType(latestSkuObjSeason);
									if (IslatestSkuSesaon &&  "TRUE".equals(AgronCSVGenerator.getData(skuObjA, ADOPTED_KEY, false))) {
										Collection<ProductSizeCategory> psdCollection = AgronExportUtil.findValidPSDs(skuObjA,latestSkuObjSeason);
										if (psdCollection.size() > 0) {
										adoptedCount++;
																			
										completeRow = new FlexObject();
										completeRow.putAll(productData);
										completeRow.putAll(exportUtil.getSKUData(skuObjA, product, colorwayAtts));
										FlexObject flexOBJ = new FlexObject();
										LCSColor colorObj = (LCSColor)skuObjA.getValue("color");
									
										if(colorObj!=null)
										{
									
											String nrfCode1 = AgronCSVGenerator.getData(colorObj, "agrNRFCode", false);
											String nrfDesc1 = AgronCSVGenerator.getData(colorObj, "agrNRFDescription", false);
											flexOBJ.put("ERP.nrfDesc",nrfDesc1);
											flexOBJ.put("ERP.nrfCode",nrfCode1);
											
										}
										completeRow.putAll(flexOBJ);
										productDataCol.add(completeRow);
										
									}
									}
									else
									{
									if ("Socks".equals(product.getFlexType().getTypeDisplayName()) || "Team".equals(product.getFlexType().getTypeDisplayName()))
										attribute = skuObjA.getFlexType().getAttribute(COLORWAY_MOA_SOCKS_KEY);
									else
										attribute = skuObjA.getFlexType().getAttribute(COLORWAY_MOA_KEY);
									
									moaCol = LCSMOAObjectQuery.findMOACollection(skuObjA, attribute, "sortingNumber");
									LOGGER.info((new StringBuilder("SKU Adopted : "))
											.append(AgronCSVGenerator.getData(skuObjA, ADOPTED_KEY, false)).toString());
									if (moaCol.size() > 0
											&& "TRUE".equals(AgronCSVGenerator.getData(skuObjA, ADOPTED_KEY, false))) {
										adoptedCount++;
										completeRow = new FlexObject();
										completeRow.putAll(productData);
										completeRow.putAll(exportUtil.getSKUData(skuObjA, product, colorwayAtts));
										productDataCol.add(completeRow);
									}
									}
								}
							}

						}
						if (adoptedCount != 0) {

							productDataCol = exportUtil.addSizingDataFromSkuSize(product, productDataCol, this.keyPair,latestSeason);
						}

					}

					isErrorOccure = exportUtil.getErrorStatus();
				}
			}
		
		}catch (WTException e) {
			this.isErrorOccure = true;
			LOGGER.error("Error : " + e.fillInStackTrace());
		}
		return dataCol;
	}

	/**
	 * This method is used to set the data for season based prices on the columns.
	 * 
	 * @param objects   is Collection object
	 * @param tableName is String object
	 * @return is Collection type
	 */
	@SuppressWarnings("unchecked")
	public Collection<FlexObject> addRelevantSeasonPriceData(Collection<?> objects, String tableName) {
		Collection<FlexObject> dataCol = null;
		Iterator<?> objectsIter = null;
		LCSProduct product = null;
		FlexObject productData = null;
		FlexObject sourcingData = null;
		WTObject object = null;
		Collection<?> skuMasters = null;
		LCSPartMaster skuMaster = null;
		LCSSKU skuObjA = null;
		FlexObject completeRow = null;
		LCSSeason latestSeason = null;
		Collection<?> sourceTEst = null;
		LCSSeason latestSkuSeason = null;
		String seasonName = "";
		int adoptedCount = 0;
		Collection<FlexObject> productDataCol = null;
		String collectionValue = "";
		String subCollectionValue = "";
		String brandValue = "";
		try {
			init();
			LOGGER.info((new StringBuilder("Objects : ")).append(objects).toString());
			String atts = LCSProperties
					.get((new StringBuilder("com.agron.wc.interface.attributes.")).append(tableName).toString());
			String colorwayAtts = LCSProperties.get("com.agron.wc.interface.attributes.LCSSKU");
			dataCol = new ArrayList<FlexObject>();
			AgronExportUtil exportUtil = new AgronExportUtil(isErrorOccure);
			AgronDataFeedUtil dataFeedUtil = new AgronDataFeedUtil();
			exportUtil.setKeyPair(keyPair);
			AgronDataHelper dataHelper = null;
			objectsIter = objects.iterator();
			FlexTypeAttribute attribute = null;
			Collection<?> moaCol = null;
			Collection<?> seasonsCol = new ArrayList<Object>();
			while (objectsIter.hasNext()) {
				dataHelper = new AgronDataHelper();
				Map<LCSSKU, LCSSKU> skuMap = new HashMap<LCSSKU, LCSSKU>();
				product = (LCSProduct) LCSQuery.findObjectById((new StringBuilder("VR:com.lcs.wc.product.LCSProduct:"))
						.append((String) objectsIter.next()).toString());
				LOGGER.info((new StringBuilder("Product Name : ")).append(product.getName()).toString());
				seasonsCol = (new LCSSeasonQuery()).findSeasons(product);
				seasonsCol = dataHelper.removedSeasonRemovedSeasons(product, seasonsCol);
				if (seasonsCol.size() > 0) {
					for (Iterator<?> sItr = seasonsCol.iterator(); sItr.hasNext(); dataCol.addAll(productDataCol)) {
						productDataCol = new ArrayList<FlexObject>();
						latestSeason = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) sItr.next());
						// SKIP this season if Downstream Integration is disabled.
						String isagrDownstreamIntegrated = (String) latestSeason.getValue("agrDownstreamIntegrated");
						if("agrNo".equals(isagrDownstreamIntegrated)) {
							LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + latestSeason.getName());
							continue;
						}
						object = product;
						new LCSSeasonQuery();
						skuMasters = LCSSeasonQuery.getSKUMastersForSeasonAndProduct(latestSeason, product, false,
								false);
						if (skuMasters.size() > 0) {
							adoptedCount = 0;
							for (Iterator<?> skuiter = skuMasters.iterator(); skuiter.hasNext();) {
								skuMaster = (LCSPartMaster) skuiter.next();
								skuObjA = (LCSSKU) VersionHelper.getVersion(skuMaster, "A");
								if (!skuMap.containsKey(skuObjA)) {
									skuMap.put(skuObjA, skuObjA);
									Collection<?> skuSeasons = (new LCSSeasonQuery())
											.findSeasons((LCSPartMaster) skuObjA.getMaster());
									Collection<LCSSeasonMaster> skuSeasonCol = new ArrayList<LCSSeasonMaster>();
									if (skuSeasons.size() > 0) {
										for (Iterator<?> seasonIter = skuSeasons.iterator(); seasonIter.hasNext();) {
											LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
											if("agrNo".equals((String) season.getValue("agrDownstreamIntegrated"))) {
												continue;
												}
											
											LCSSeasonProductLink skuSPL = LCSSeasonQuery.findSeasonProductLink(skuObjA,
													season);
											if ((!skuSPL.isSeasonRemoved()) && season.isActive()) {
												// continue;
												skuSeasonCol.add((LCSSeasonMaster) season.getMaster());
												latestSeason = dataHelper.getLatestSeason(product, skuSeasonCol);
												LCSSeasonProductLink productSPL = LCSSeasonQuery
														.findSeasonProductLink(product, season);
												productData = exportUtil.getData(object, atts);
											
												seasonName = dataHelper.getFormattedSeasonName();
												exportUtil.setFormattedSeasonName(seasonName);
												// Added to include New attributes at Product Season Level
												productData.put("ERP.retail",
														AgronCSVGenerator.getData(productSPL, RETAILPRICE_KEY, false));
												productData.put("ERP.wholeSale",
														AgronCSVGenerator.getData(productSPL, WHOLESALE_KEY, false));
												

												productData.put("ERP.seasonType",
														AgronCSVGenerator.getData(season, SEASONTYPE_KEY, false));
												productData.put("ERP.year",
														AgronCSVGenerator.getData(season, YEAR_KEY, false));
												productData.put("LCSSEASON.OBJECT", season);
												productData.put("ERP.agrDroppedDate",
														AgronCSVGenerator.getData(skuSPL, "agrDroppedDate", false));
												productData.put("ERP.agrAvailabilityDate", AgronCSVGenerator
														.getData(skuSPL, "agrAvailabilityDate", false));
												productData.put("ERP.colorwaySortOrder",
														AgronCSVGenerator.getData(skuSPL, "colorwaySortOrder", false));
												productData.put("ERP.agrCBD1",
														AgronCSVGenerator.getData(skuSPL, "agrCBD1", false));
												productData.put("ERP.agrCBD2",
														AgronCSVGenerator.getData(skuSPL, "agrCBD2", false));
												productData.put("ERP.agrCBD3",
														AgronCSVGenerator.getData(skuSPL, "agrCBD3", false));
												productData.put("ERP.agrCBD4",
														AgronCSVGenerator.getData(skuSPL, "agrCBD4", false));
												productData.put("ERP.agrMOQ",
														AgronCSVGenerator.getData(skuSPL, "agrMOQ", false));
												
												/* Added New attribute Colourway season - Req Consolidation 7/9/20*/											
												productData.put("ERP.agrReqConsolidation",
														AgronCSVGenerator.getData(skuSPL, "agrReqConsolidation", false));
													
												productData.put("ERP.agrRetailNewEffectiveDate", AgronCSVGenerator
														.getData(productSPL, "agrRetailNewEffectiveDate", false));
												productData.put("ERP.agrWholesaleNewEffectiveDat", AgronCSVGenerator
														.getData(productSPL, "agrWholesaleNewEffectiveDat", false));
												productData.put("ERP.agrOverrideRetailDate", AgronCSVGenerator
														.getData(productSPL, "agrOverrideRetailDate", false));
												productData.put("ERP.agrOverrideWholesaleDate", AgronCSVGenerator
														.getData(productSPL, "agrOverrideWholesaleDate", false));
												productData.put("ERP.agrOverrideRetailPrice", AgronCSVGenerator
														.getData(productSPL, "agrOverrideRetailPrice", false));
												productData.put("ERP.agrOverrideWholesalePrice", AgronCSVGenerator
														.getData(productSPL, "agrOverrideWholesalePrice", false));
												productData.put("ERP.seasonObj", AgronCSVGenerator
														.getData(productSPL, "agrOverrideWholesalePrice", false));
												
												
												sourceTEst = LCSSourcingConfigQuery.getSourcingConfigForProduct(
														(LCSPartMaster) product.getMaster());
												if (sourceTEst != null && sourceTEst.size() > 0) {
													for (Iterator<?> sourcesT = sourceTEst.iterator(); sourcesT
															.hasNext();) {
														LCSSourcingConfig sc = (LCSSourcingConfig) sourcesT.next();
														if (sc.isPrimarySource())
															sourcingData = (new AgronDataFeedUtil()).getSourceData(sc,
																	product);
													}

													productData.putAll(sourcingData);
												}
												latestSkuSeason = dataHelper.getLatestSkuSeason(skuObjA, skuSeasonCol);
												productData.put("ERP.latestSkuSeason", latestSkuSeason);
												
												boolean IslatestSkuSesaon = false;
												if (latestSkuSeason != null)
													IslatestSkuSesaon = AgronExportUtil.isAgronSeasonType(season);
												if (IslatestSkuSesaon) {
													
												
													
													Collection<ProductSizeCategory> psdCollection = AgronExportUtil.findValidPSDs(skuObjA,latestSkuSeason);
													if (psdCollection.size() > 0) {
													adoptedCount++;
													completeRow = new FlexObject();
													completeRow.putAll(productData);
													completeRow.putAll(exportUtil.getSKUData(skuObjA, product, null));
													productDataCol.add(completeRow);
													
												}
												}
												else
												{
												if ("Socks".equals(product.getFlexType().getTypeDisplayName()) || "Team".equals(product.getFlexType().getTypeDisplayName()))
													attribute = skuObjA.getFlexType().getAttribute(COLORWAY_MOA_SOCKS_KEY);
												else
													attribute = skuObjA.getFlexType().getAttribute(COLORWAY_MOA_KEY);
												
												moaCol = LCSMOAObjectQuery.findMOACollection(skuObjA, attribute,
														"sortingNumber");
												LOGGER.info((new StringBuilder("SKU Adopted : "))
														.append(AgronCSVGenerator.getData(skuObjA, ADOPTED_KEY, false))
														.toString());
												if (moaCol.size() > 0 && "TRUE".equals(
														AgronCSVGenerator.getData(skuObjA, ADOPTED_KEY, false))) {
													adoptedCount++;
													completeRow = new FlexObject();
													completeRow.putAll(productData);
													completeRow.putAll(exportUtil.getSKUData(skuObjA, product, colorwayAtts));
													productDataCol.add(completeRow);
												}
												}
											}
										}

									}
								}
							}

						}
						if (adoptedCount != 0) {
							
							productDataCol = exportUtil.addSizingData(product, productDataCol, this.keyPair, latestSeason);

						}
					}

					isErrorOccure = exportUtil.getErrorStatus();
				}
			}
		} catch (WTException e) {
			isErrorOccure = true;
			//LOGGER.error((new StringBuilder("Error : ")).append(e.fillInStackTrace()));
			LOGGER.error(e.fillInStackTrace());
		}
		return dataCol;
	}

	/**
	 * This method is used to form the map with the combination of column key and
	 * attribute key.
	 */
	public void init() {
		String pair = null;
		String pairList[] = null;
		Collection<?> keyPairCol = MOAHelper.getMOACollection(KEY_PAIR);
		for (Iterator<?> iter = keyPairCol.iterator(); iter.hasNext(); keyPair.put(pairList[0], pairList[1])) {
			pair = (String) iter.next();
			pairList = pair.split(":");
		}

	}

	/**
	 * This method is to get the error status.
	 * 
	 * @return Boolean type
	 */
	public boolean getErrorStatus() {
		return isErrorOccure;
	}

}
