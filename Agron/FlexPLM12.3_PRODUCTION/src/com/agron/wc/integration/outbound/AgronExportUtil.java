package com.agron.wc.integration.outbound;

import com.agron.wc.integration.util.*;
import com.lcs.wc.sourcing.LCSSKUSourcingLink;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.datatypes.FlexUOM;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.skusize.SKUSizeToSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.skusize.SKUSizeMaster;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.util.WTException;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;

/**
 * @author Mallikarjuna Savi
 *
 */
public class AgronExportUtil {

	private static final String MOA_ATTS = LCSProperties.get("com.agron.wc.interface.attributes.colorway.moa.atts");
	private static final String MOA_ADOPTED_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moa.adoptedKey");
	private static final String COLORWAY_MOA_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moaKey");
	private static final String MOA_WORK_NUMBER_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moa.workOrderKey");
	private static final String MOA_ARTICLE_NUMBER_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moa.articleNumberKey");
	private static final Logger LOGGER = LogManager.getLogger(AgronExportUtil.class);
	private static final String SMU_ACCOUNT_KEY = LCSProperties.get("com.agron.wc.interface.product.smuAccKey");
	private static final String WORK_KEY = LCSProperties.get("com.agron.wc.interface.attributes.product.workKey");
	private static final String COLORWAY_LETTER_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.letterKey");
	private static final String CATALOG_COLORWAY_NAME = LCSProperties
			.get("com.agron.wc.reports.lineplanreport.colorwaydescription");
	private static final String SEASON_ASSOCIATED_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.seasonsAssociatedKey");
	private Map keyPair = null;
	private static String formattedSeasonName = null;
	private boolean isErrorOccure = false;
	private static final String PACK_SIZE_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.agrPackCountkey");
	private static final String COSTSHEET_FOB_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.costsheet.fob.attKey");
		private static final String COSTSHEET_RETAILPRICE_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.costsheet.retailPrice.attKey","agrRetailPriceDollar");
	private static final String seasonTypes = LCSProperties.get("com.agron.wc.integration.seaonTypes");
	private static final String COLORWAY_MOA_SOCKS_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moaSocksTypeKey");
	private static final String AGENT_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.LCSSourcingConfig.agentKey");
	private static final String FACTORY_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.LCSSourcingConfig.factoryKey");
	private static final String VENDOR_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.supplier.vendorNumKey");
	private static final String TTI = LCSProperties.get("com.agron.wc.interface.tti", "TTI");
	private static final String SPORT_STYLE = LCSProperties.get("com.agron.wc.interface.sportStyle", "Sport Style");
	private static final String SOCKS = LCSProperties.get("com.agron.wc.interface.socks", "Socks");
	private static final String CARTINA = LCSProperties.get("com.agron.wc.interface.Cortina", "Cortina");
	private static final String ARTICLE_NUMBER_SKU_ATTKEY = LCSProperties.get("com.agron.wc.product.SKU.ArticleNumber.attKey","agrArticle");

	/**
	 * This constructor is used to initailize the error status.
	 * 
	 * @param isErrorOccure Boolean object
	 */
	AgronExportUtil(boolean isErrorOccure) {
		this.isErrorOccure = isErrorOccure;
	}

	/**
	 * This method is used to get the relevant data.
	 * 
	 * @param object is WTObject object
	 * @param atts   is String object
	 * @return FlexObject type
	 */
	public FlexObject getData(WTObject wtobject, String atts) throws WTException {
		FlexObject flexobject = null;
		flexobject = new FlexObject();
		String attributeKey;
		for (StringTokenizer stringtokenizer = new StringTokenizer(atts, ","); stringtokenizer
				.hasMoreTokens(); flexobject.put(
						(new StringBuilder()).append("ERP.").append((String) keyPair.get(attributeKey)).toString(),
						AgronCSVGenerator.getData(wtobject, attributeKey, false)))
			attributeKey = stringtokenizer.nextToken();

		if (wtobject instanceof LCSProduct) {
			LCSProduct lcsproduct = (LCSProduct) wtobject;
			LOGGER.info((new StringBuilder()).append("keyPair : ").append(this.keyPair).toString());
			
					flexobject.put("ERP.PRODUCTTYPE", (new AgronDataHelper()).getFormatedProductType(lcsproduct, this.keyPair));
					
					if ("SMU & Clearance".equals(lcsproduct.getFlexType().getTypeDisplayName())) {

					if("agrTeam".equalsIgnoreCase((String)lcsproduct.getValue("agrCategoryProd")))
						{
						flexobject.put("ERP.PRODUCTTYPE", "SOCK-TEAM");	
						}
					flexobject.put("ERP.SMU", "Y");
					flexobject.put("ERP.SMUACCOUNT", AgronCSVGenerator.getData(wtobject, SMU_ACCOUNT_KEY, false));
					} else {
					flexobject.put("ERP.SMU", "N");
					flexobject.put("ERP.SMUACCOUNT", "");
							}
				}
		
		return flexobject;
	}

	/**
	 * This method is used to get the sizing data.
	 * 
	 * @param data is Collection object
	 * @return Collection type
	 * FlexImportSeasonPrice.csv
	 */
	public Collection addSizingData(LCSProduct lcsproduct, Collection collection, Map map, LCSSeason lcsseason) {
		ArrayList arraylist = null;

		boolean flag = false;
		try {
			arraylist = new ArrayList();
			Iterator iterator = collection.iterator();
			label0: do {
				if (!iterator.hasNext())
					break;
				FlexObject flexobject = (FlexObject) iterator.next();
				LCSSKU lcssku = (LCSSKU) LCSQuery.findObjectById(flexobject.getData("LCSSKU.OBJECT"));
			
				LCSSeason skuSeason = (LCSSeason) LCSQuery.findObjectById(flexobject.getData("LCSSEASON.OBJECT"));
				boolean latestSkuSesaon = false;
				if (skuSeason != null)
					latestSkuSesaon = isAgronSeasonType(skuSeason);
				if (latestSkuSesaon) {
					
					ArrayList skuSizecollec = getDataFromCostSheet(lcssku, lcsproduct, skuSeason, flexobject);
					if (skuSizecollec!=null && skuSizecollec.size() > 0) {
						arraylist.addAll(skuSizecollec);
						// completeRowCol.add(flexobject);
					}
				}				
			
				else {
					String articleNum=(String)lcssku.getValue(ARTICLE_NUMBER_SKU_ATTKEY);
					
					/* if(FormatHelper.hasContent(articleNum) && !(articleNum.contains("-")))
					{
						
						articleNum = articleNum.substring(1);
					} */
					if(FormatHelper.hasContent(articleNum) && !(articleNum.startsWith("-")))
					{	
				FlexTypeAttribute flextypeattribute = null;

				if ("Socks".equals(lcsproduct.getFlexType().getTypeDisplayName())
						|| "Team".equals(lcsproduct.getFlexType().getTypeDisplayName()))
					flextypeattribute = lcssku.getFlexType().getAttribute(COLORWAY_MOA_SOCKS_KEY);
				else
					flextypeattribute = lcssku.getFlexType().getAttribute(COLORWAY_MOA_KEY);
				Collection collection1 = LCSMOAObjectQuery.findMOACollection(lcssku, flextypeattribute,
						"sortingNumber");
				int i = 0;
				if (collection1 != null && collection1.size() > 0) {
					Iterator iterator1 = collection1.iterator();
					do {
						LCSMOAObject lcsmoaobject;
						do {
							if (!iterator1.hasNext())
								continue label0;
							lcsmoaobject = (LCSMOAObject) iterator1.next();
						} while (!"TRUE".equals(AgronCSVGenerator.getData(lcsmoaobject, MOA_ADOPTED_KEY, false)));
						i++;
						FlexObject flexobject1 = new FlexObject();
						flexobject1.putAll(flexobject);
						flexobject1.putAll(getData(lcsmoaobject, MOA_ATTS));
						if (!FormatHelper
								.hasContent(AgronCSVGenerator.getData(lcsmoaobject, MOA_WORK_NUMBER_KEY, false)))
							;
						if (FormatHelper
								.hasContent(AgronCSVGenerator.getData(lcsmoaobject, MOA_ARTICLE_NUMBER_KEY, false))) {
							
							String s1 = "";
							s1 = AgronCSVGenerator.getData(lcsmoaobject, MOA_ARTICLE_NUMBER_KEY, false);
							if (s1 != null)
								if (s1.startsWith("5", 0)) {
									StringBuffer stringbuffer = new StringBuffer(s1);
									stringbuffer.delete(7, 100).trimToSize();
									String s2 = stringbuffer.toString();
									flexobject1.put("ERP.articalNumber", s2);
								} else if (s1.length() >= 6) {
									StringBuffer stringbuffer1 = new StringBuffer(s1);
									stringbuffer1 = stringbuffer1.delete(6, 100);
									flexobject1.put("ERP.articalNumber", stringbuffer1.toString());
								} else {
									flexobject1.put("ERP.articalNumber", s1);
								}
						}
						if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, PACK_SIZE_KEY, false))) {
							String s = AgronCSVGenerator.getData(lcsproduct, PACK_SIZE_KEY, false);
							String as[] = s.split("-PACK");
							for (int j = 0; j < as.length; j++)
								flexobject1.put("ERP.packSize", as[j].toString().replaceAll("SINGLE", "1").trim());

						}
						flexobject1.putAll((new AgronNRFBOUtil()).getNRFValuesFromBO(lcsproduct,
								flexobject.getData("ERP.PRODUCTTYPE"), lcsmoaobject));
						arraylist.add(flexobject1);
					} while (true);
				}
				
				arraylist.add(flexobject);
					}
				}
			} while (true);
		} catch (WTException wtexception) {
			isErrorOccure = true;
			LOGGER.error((new StringBuilder()).append("Error : ").append(wtexception.fillInStackTrace()));
		}
		return arraylist;
	}

	/**
	 * This method is used to get the sizing data.
	 * 
	 * @param data is Collection object
	 * @return Collection type
	 * FlexImport.csv
	 */
	public Collection addSizingDataFromSkuSize(LCSProduct lcsproduct, Collection data, Map map,
			LCSSeason latestSeason) {
		ArrayList completeRowCol = null;
		boolean flag = false;
		try {
			completeRowCol = new ArrayList();

			Iterator skuIter = data.iterator();
			label0: do {
				if (!skuIter.hasNext())
					break;
				FlexObject flexobject = (FlexObject) skuIter.next();
				
				LCSSKU lcssku = (LCSSKU) LCSQuery.findObjectById(flexobject.getData("LCSSKU.OBJECT"));
				LCSSeason latestskuSeason = (LCSSeason) LCSQuery
						.findObjectById(flexobject.getData("ERP.LATESTSKUSEASON"));
				boolean latestSkuSesaon = false;
				if (latestskuSeason != null)
					latestSkuSesaon = isAgronSeasonType(latestskuSeason);
				if (latestSkuSesaon) {
				
					ArrayList skuSizecollec = getDataFromCostSheet(lcssku, lcsproduct, latestskuSeason, flexobject);
					if (skuSizecollec!=null && skuSizecollec.size() > 0) {
						completeRowCol.addAll(skuSizecollec);
						// completeRowCol.add(flexobject);
					}
				} else {
					/**
					 * // code to add sourcename and Costsheet name in FlexImport report ----- Start
					 * ----------------- LCSSourcingConfig primarySConfig =
					 * LCSSourcingConfigQuery.getPrimarySourceForProduct(lcsproduct);
					 * flexobject.put("ERP.primarySource", primarySConfig.getValue("name"));
					 * LCSSourcingConfigQuery lscq = new LCSSourcingConfigQuery();
					 * LCSSourceToSeasonLink stsl = (LCSSourceToSeasonLink)
					 * lscq.getSourceToSeasonLink(primarySConfig, latestskuSeason); LCSCostSheet
					 * csObject = null; if (stsl != null) { Iterator costSheetIter =
					 * LCSCostSheetQuery.getCostSheetsForSourceToSeason(stsl, null).iterator();
					 * while (costSheetIter.hasNext()) { csObject = (LCSCostSheet)
					 * costSheetIter.next(); if (FormatHelper.hasContent((String)
					 * csObject.getName())) { if (csObject.isPrimaryCostSheet()) {
					 * flexobject.put("ERP.csName", csObject.getName()); break; } else csObject =
					 * null; }
					 * 
					 * } } // code to add soourcename and Costsheet name in FlexImport report -----
					 * END -----------------
					 **/
					String articleNum=(String)lcssku.getValue(ARTICLE_NUMBER_SKU_ATTKEY);

					
					if(FormatHelper.hasContent(articleNum) && !(articleNum.contains("-")))
					{	
					FlexTypeAttribute flextypeattribute = null;
					if ("Socks".equals(lcsproduct.getFlexType().getTypeDisplayName())
							|| "Team".equals(lcsproduct.getFlexType().getTypeDisplayName())) {
						flextypeattribute = lcssku.getFlexType().getAttribute(COLORWAY_MOA_SOCKS_KEY);
					} else {
						flextypeattribute = lcssku.getFlexType().getAttribute(COLORWAY_MOA_KEY);
					}
					Collection moaCol = LCSMOAObjectQuery.findMOACollection(lcssku, flextypeattribute, "sortingNumber");
					int i = 0;
					
					if (moaCol != null && moaCol.size() > 0) {
						Iterator moaIter = moaCol.iterator();
						do {
							LCSMOAObject moaObject;
							do {
								if (!moaIter.hasNext())
									continue label0;
								moaObject = (LCSMOAObject) moaIter.next();
							} while (!"TRUE".equals(AgronCSVGenerator.getData(moaObject, MOA_ADOPTED_KEY, false)));
							i++;
							FlexObject sizingObj = new FlexObject();
							sizingObj.putAll(flexobject);
							sizingObj.putAll(getData(moaObject, MOA_ATTS));
							if (!FormatHelper
									.hasContent(AgronCSVGenerator.getData(moaObject, MOA_WORK_NUMBER_KEY, false)))
								;
							if (FormatHelper
									.hasContent(AgronCSVGenerator.getData(moaObject, MOA_ARTICLE_NUMBER_KEY, false))) {

								String articleLeftmost = "";
								articleLeftmost = AgronCSVGenerator.getData(moaObject, MOA_ARTICLE_NUMBER_KEY, false);
								if (articleLeftmost != null)
									if (articleLeftmost.startsWith("5", 0)) {
										StringBuffer stringbuffer = new StringBuffer(articleLeftmost);
										stringbuffer.delete(7, 100).trimToSize();
										String s2 = stringbuffer.toString();
										sizingObj.put("ERP.articalNumber", s2);
									} else if (articleLeftmost.length() >= 6) {
										StringBuffer stringbuffer1 = new StringBuffer(articleLeftmost);
										stringbuffer1 = stringbuffer1.delete(6, 100);
										sizingObj.put("ERP.articalNumber", stringbuffer1.toString());
									} else {
										sizingObj.put("ERP.articalNumber", articleLeftmost);
									}
							}
							if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, PACK_SIZE_KEY, false))) {
								String packName = AgronCSVGenerator.getData(lcsproduct, PACK_SIZE_KEY, false);
								String as[] = packName.split("-PACK");
								for (int j = 0; j < as.length; j++)
									sizingObj.put("ERP.packSize", as[j].toString().replaceAll("SINGLE", "1").trim());

							}
							sizingObj.putAll((new AgronNRFBOUtil()).getNRFValuesFromBO(lcsproduct,
									flexobject.getData("ERP.PRODUCTTYPE"), moaObject));
							completeRowCol.add(sizingObj);
						} while (true);
					}
					completeRowCol.add(flexobject);
					}
					}

			} while (true);
		} catch (WTException e) {
			this.isErrorOccure = true;
			LOGGER.error("Error : " + e.fillInStackTrace());
		}
		return completeRowCol;
	}

	/**
	 * This method is used to get the sizing data from skuSize and FOB from
	 * CostSheet
	 * 
	 * @param data is Collection object
	 * @return Collection type
	 */
	public ArrayList getDataFromCostSheet(LCSSKU lcssku, LCSProduct lcsproduct, LCSSeason lcsseason,
			FlexObject flexobject) throws WTException {

		Collection<ProductSizeCategory> psdCollection = findValidPSDs(lcssku,lcsseason);
		
		ArrayList arraylist = new ArrayList();
		if (psdCollection.size() > 0) {
			Iterator<ProductSizeCategory> psdIter = psdCollection.iterator();

			Hashtable sizeNames = new Hashtable();
			while (psdIter.hasNext()) {
				ProductSizeCategory psc = psdIter.next();
				SearchResults searchResultSKUSizeQuery = SKUSizeQuery.findSKUSizesForPSC(psc, lcssku.getMaster(), null,
						null);
				Collection<SKUSize> skuSizesCollection = SKUSizeQuery.getObjectsFromResults(
						searchResultSKUSizeQuery.getResults(), "VR:com.lcs.wc.skusize.SKUSize:",
						"SKUSize.BRANCHIDITERATIONINFO");
			
				LOGGER.info("skuSizesCollection::" + skuSizesCollection.size() + "::" + skuSizesCollection.toString());
				if (skuSizesCollection != null && skuSizesCollection.size() > 0) {
					String isActiveSkuSizeToSeason="";
					Iterator<SKUSize> skuSizeIter = skuSizesCollection.iterator();
					while (skuSizeIter.hasNext()) {

						FlexObject flexobject1 = new FlexObject();
						flexobject1.putAll(flexobject);
						SKUSize skuSize = (SKUSize) VersionHelper.getVersion(skuSizeIter.next(), "A");
						SKUSizeMaster skuSizeMaster = (SKUSizeMaster) skuSize.getMaster();
						String skuSize1Value = skuSizeMaster.getSizeValue();
						String skuSize2Value = skuSizeMaster.getSize2Value();
						String articleNumber = (String) skuSize.getValue("agrArticleNumber");
						String rootArticleNumber = (String)lcssku.getValue("agrArticle");
						if (!FormatHelper.hasContent(articleNumber) || !FormatHelper.hasContent(skuSize1Value))
							continue;

						String isActiveSkuSize = Boolean.toString(skuSize.isActive());
						String upc = (String) skuSize.getValue("agrUPCNumber");


						SKUSizeToSeason skuSizeToSeason=(new SKUSizeQuery()).getSKUSizeToSeasonBySKUSizeSeason(skuSize,lcsseason);
						if(skuSizeToSeason !=null) {
							isActiveSkuSizeToSeason = Boolean.toString(skuSizeToSeason.isActive());		
						}
						
						LOGGER.info("Season::>>>>"+((LCSSeason) VersionHelper.latestIterationOf(lcsseason.getMaster())).getName()+",   SKU::>>>>"+((LCSSKU) VersionHelper.latestIterationOf(lcssku.getMaster())).getName()+", SKUSIZETOSEASONLINKACTIVATIONCHECK::>>>>:"+ isActiveSkuSizeToSeason);
						
						
						// Added inactive sizes also in report based on the changes 5/28
						//if (articleNumber != null && FormatHelper.hasContent(isActiveSkuSize) && "TRUE".equalsIgnoreCase(isActiveSkuSize))
						
						if (articleNumber != null && FormatHelper.hasContent(isActiveSkuSize))
						{
						
							{
								if(FormatHelper.hasContent(rootArticleNumber)&& rootArticleNumber.contains(",")) { // This is an exception scenario since some legacy articles have , in them
									
									flexobject1.put("ERP.articalNumber", articleNumber); // Pick the article # from SKU ssize
								}else {
								flexobject1.put("ERP.articalNumber", rootArticleNumber);  // Else pick the root article number
								}
							}
						if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, PACK_SIZE_KEY, false))) {
							String s = AgronCSVGenerator.getData(lcsproduct, PACK_SIZE_KEY, false);
							String as[] = s.split("-PACK");
							for (int j = 0; j < as.length; j++)
								flexobject1.put("ERP.packSize", as[j].toString().replaceAll("SINGLE", "1").trim());

						}

						flexobject1.put("ERP.sizes", skuSize1Value);
						if ("YES".equalsIgnoreCase((String)flexobject1.get("ERP.resetSkuSizeactive")))
						{
						flexobject1.put("ERP.active", "FALSE");
						} else {
						flexobject1.put("ERP.active", isActiveSkuSize);
						}
						flexobject1.put("ERP.skuSTSactive", isActiveSkuSizeToSeason); 
						flexobject1.put("ERP.itemCode", articleNumber);
						flexobject1.put("ERP.upc", upc);
						boolean foundCSheet = false;
						

							
						Collection skuSourcesids = new LCSSourcingConfigQuery().getSkuSourcingLinkIds(null,
								lcssku.getMaster(), lcsseason.getMaster(), true);
						
						if (skuSourcesids != null && skuSourcesids.size() > 0) {

							Iterator skuSourcesidsitr = skuSourcesids.iterator();
							while (skuSourcesidsitr.hasNext()) {
								String skuSourceID = (String) skuSourcesidsitr.next();
								LCSSKUSourcingLink skuSourceObj = (LCSSKUSourcingLink) LCSQuery
										.findObjectById(skuSourceID);
								LCSSourcingConfig skuSource = (LCSSourcingConfig) VersionHelper
										.latestIterationOf(skuSourceObj.getConfigMaster());
								LCSSourcingConfigQuery lscq = new LCSSourcingConfigQuery();
								LCSSourceToSeasonLink stsl = (LCSSourceToSeasonLink) lscq
										.getSourceToSeasonLink(skuSource, lcsseason);

								if (stsl != null && stsl.isPrimarySTSL())
								{
																						
									Collection costsheets = LCSCostSheetQuery.getCostSheetsForSourceToSeason(stsl,
											null);
									
									ArrayList<String> listOfCostsheet= new ArrayList<>();
									Iterator costSheetItertemp = costsheets.iterator();
									while (costSheetItertemp.hasNext()) {
										LCSCostSheet csObject1 = (LCSCostSheet) costSheetItertemp.next();
										 csObject1 = (LCSCostSheet) VersionHelper.latestIterationOf(csObject1);
										if (csObject1 != null && !(csObject1.isWhatIf()) && !(csObject1.getName().equalsIgnoreCase("0")) && csObject1.getApplicableSizes() != null && csObject1.getApplicableColorNames() != null){
											 listOfCostsheet.add(csObject1.getName());
										 }
									}
								 LOGGER.info(":::::ArrayList of Costsshet:::::::::"+listOfCostsheet);
									// sort listOfCostsheet indecending order 
//									Collections.sort(listOfCostsheet, Collections.reverseOrder());
									
	//								String listOfCostsheetObj[]=listOfCostsheet.toArray(new String[listOfCostsheet.size()]);	
									  //LOGGER.info(":::::ArrayList in descending order:::::::::"+listOfCostsheet);
									if (costsheets != null && costsheets.size() > 0) {
										Iterator costSheetIter = costsheets.iterator();
										while (costSheetIter.hasNext()) {
											LCSCostSheet csObject = (LCSCostSheet) costSheetIter.next();
											
										if (csObject != null && !(csObject.isWhatIf()) && !(csObject.getName().equalsIgnoreCase("0"))) {
												String isValidCoshsheet="TRUE";
												
												String representativeSize = csObject.getApplicableSizes();
												String representativeColor = csObject.getApplicableColorNames();
												
												if(representativeColor == null && representativeSize == null) {
													isValidCoshsheet="TRUE";
												}
												else if(representativeColor != null && !"".equals(representativeColor.trim()) && representativeSize != null && !"".equals(representativeSize.trim())){
													isValidCoshsheet="FALSE";
												}
												else if(listOfCostsheet.stream().filter(st -> st.trim().contains(csObject.getName())).count() > 0) {
													isValidCoshsheet="FALSE";
												}
//												else {
//													duplicateCoshsheet="TRUE";
//												}
												/*int count=0;
												for(String costsheetObjitr: listOfCostsheetObj)
													{
														if(costsheetObjitr.contains(String.valueOf(csObject.getName())))
														{
															//count++;
															
															if(!costsheetObjitr.equalsIgnoreCase(String.valueOf(csObject.getName()))){
																LOGGER.info(":::::Is Valid Costsshet:::::::::"+csObject.getName());	
																duplicateCoshsheet="FALSE";
																break;
															}else{
																if(FormatHelper.hasContent(representativeColor)){
																count++;
																if(count>1){
																	LOGGER.info(":::::Don't have size ::Error true:::::"+csObject.getName());	
																	duplicateCoshsheet="TRUE";
																}
																}
															}
														}
													}*/
												 LOGGER.info("COSTSSHET>>>>"+csObject.getName()+":::::::::::::::::::duplicateCoshsheet>>>>>>>"+isValidCoshsheet);
												if (FormatHelper.hasContent(representativeSize)
														&& FormatHelper.hasContent(representativeColor))

												{
													
													boolean foundSKUSize = false;
													StringTokenizer skuSizeST = new StringTokenizer(representativeSize,"|~*~|");
													LOGGER.info("skuSizeST================"+skuSizeST);
													while (skuSizeST.hasMoreTokens()) {
														String token = skuSizeST.nextToken();
														LOGGER.info("token================"+token);
														LOGGER.info("token================"+skuSize1Value);
														if(token.equalsIgnoreCase(skuSize1Value))
														{
															foundSKUSize = true;
															break;
														}
													}
													FlexObject flexobject2 ;
													if (foundSKUSize && representativeColor
																	.contains((String) lcssku.getValue("skuName"))) {
														
													
														 flexobject2 = new FlexObject();
														flexobject2.putAll(flexobject1);
														//String fobstr = AgronCSVGenerator.getData(csObject, COSTSHEET_FOB_KEY, false);
														Double d = (Double) csObject.getValue(COSTSHEET_FOB_KEY);
														String fobstr = FormatHelper.formatWithPrecision(d, 2);
														Double retailPrice = (Double) csObject.getValue(COSTSHEET_RETAILPRICE_KEY);
														
														
														
														
														String retailPricestr = FormatHelper.formatWithPrecision(retailPrice, 2);
														
														LOGGER.info("COSTSHEET_RETAILPRICE_KEY String=========="+retailPricestr);
														
														//String fobstr = String.valueOf(d);
												
														String vendorNumber = null;
														LCSSupplier supplier = null;
														String productType = null;
														productType = lcsproduct.getFlexType().getFullName(true);
														supplier = (LCSSupplier) skuSource.getValue(FACTORY_KEY);
														vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
														supplier = (LCSSupplier) skuSource.getValue(AGENT_KEY);
														if (productType.contains(SOCKS)) {
																if (TTI.equalsIgnoreCase(AgronCSVGenerator.getData(supplier, "Name", false)))
																	vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
															} else if (TTI.equalsIgnoreCase(AgronCSVGenerator.getData(supplier, "Name", false)))
																vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
															if (CARTINA.equalsIgnoreCase(AgronCSVGenerator.getData(supplier, "Name", false)))
																vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
													
														
														flexobject2.put("ERP.FOB", fobstr);
														flexobject2.put("ERP.CSRETAILPRICE", retailPricestr);
														LOGGER.info("Final Cost Sheet retail Price"+flexobject2.getData("ERP.CSRETAILPRICE"));
														
														flexobject2.put("ERP.csName", csObject.getName());
														flexobject2.put("ERP.isprimarySource",skuSource.isPrimarySource());
														LOGGER.info("isPrinamySource=========="+skuSource.isPrimarySource());
														LOGGER.info("isPrinamySource=========="+flexobject2.getData("ERP.isprimarySource"));
														flexobject2.put("ERP.primarySource",vendorNumber);
																
														flexobject2.putAll((new AgronNRFBOUtil()).getNRFValuesFromBO1(
																lcsproduct, flexobject.getData("ERP.PRODUCTTYPE"),
																skuSize1Value,skuSize2Value));
														arraylist.add(flexobject2);
												
														foundCSheet = true;
													}
												}else{
													if ( "TRUE".equalsIgnoreCase(isValidCoshsheet) && FormatHelper.hasContent(representativeColor) && !"Baseline".equals(String.valueOf(csObject.getName().trim()))){
														FlexObject flexobject2 = new FlexObject();
														flexobject2.put("ERP.WORKORDER",flexobject1.getData("ERP.WORKORDER"));
														flexobject2.put("ERP.ARTICALNUMBER",flexobject1.getData("ERP.ARTICALNUMBER"));
														flexobject2.put("ERP.isCSExist", isValidCoshsheet);
														flexobject2.put("ERP.csName", csObject.getName());
														flexobject2.put("ERP.sizes", "");
														flexobject2.remove("ERP.FOB");
														arraylist.add(flexobject2);
														foundCSheet = true;
													}
												}

											}
										}
									}

								}
							}  
							if (FormatHelper.hasContent(articleNumber) && !foundCSheet) {
								flexobject1.put("ERP.FOB", "");
								flexobject1.put("ERP.CSRETAILPRICE", "0.0");
								flexobject1.put("ERP.csName", "");
								flexobject1.put("ERP.primarySource", "");
								flexobject1.putAll((new AgronNRFBOUtil()).getNRFValuesFromBO1(lcsproduct,
										flexobject.getData("ERP.PRODUCTTYPE"), skuSize1Value,skuSize2Value));
								arraylist.add(flexobject1);

							}
						} else {
							flexobject1.put("ERP.FOB", "");
							flexobject1.put("ERP.CSRETAILPRICE", "0.0");
							flexobject1.put("ERP.csName", "");
							flexobject1.put("ERP.primarySource", "");
							flexobject1.putAll((new AgronNRFBOUtil()).getNRFValuesFromBO1(lcsproduct,
									flexobject.getData("ERP.PRODUCTTYPE"), skuSize1Value,skuSize2Value));
							arraylist.add(flexobject1);
						}

					}
					}
				}
			}
		}
		return arraylist;
	}

	/**
	 * This method is used to find the latest sku season for to check 2022 season.
	 * 
	 * @param data is LCSSeason object
	 * @return latestskuSeason type
	 */
/*
	public static boolean findLatestSkuSeason(LCSSeason latestskuSeason) throws WTException {
		boolean latestskuSeas = false;
		String year = (String) latestskuSeason.getValue("year");
		String seasonType = (String) latestskuSeason.getValue("seasonType");
		FlexTypeAttribute fta = latestskuSeason.getFlexType().getAttribute("year");
		FlexTypeAttribute seasonTypefta = latestskuSeason.getFlexType().getAttribute("seasonType");
		AttributeValueList avl = fta.getAttValueList();
		AttributeValueList seasonTypeavl = seasonTypefta.getAttValueList();
		Long yearL = (long) 0;
		year = avl.getValue(year, Locale.getDefault());
		seasonType = seasonTypeavl.getValue(seasonType, Locale.getDefault());
		if (FormatHelper.hasContent(year)) {
			yearL = Long.valueOf(year);
		}
		if ((yearL == 2022 && seasonTypes.contains(seasonType)) || yearL > 2022) {
			latestskuSeas = true;

		}

		return latestskuSeas;

	}
*/
	/**
	 * This method is used to get the colorway data.
	 * 
	 * @param sku     is LCSSKU object
	 * @param product is LCSProduct object
	 * @param atts    is String object
	 * @return FlexObject type
	 * @throws WTException
	 */
	public FlexObject getSKUData(LCSSKU lcssku, LCSProduct lcsproduct, String atts) throws WTException {
		FlexObject skuData = new FlexObject();
		if (atts != null)
			skuData.putAll(getData(lcssku, atts));
		skuData.put("LCSSKU.OBJECT", lcssku);
		skuData.put("ERP.workOrderColorwayLetter",
				(new StringBuilder()).append(AgronCSVGenerator.getData(lcsproduct, WORK_KEY, false)).append("-")
						.append(AgronCSVGenerator.getData(lcssku, COLORWAY_LETTER_KEY, false)).toString());
		skuData.put("ERP.STYLENAME", AgronCSVGenerator.getData(lcsproduct, "agrMASStyleName", false));
		LOGGER.info((new StringBuilder()).append("Overriden Value*******##########")
				.append(AgronCSVGenerator.getData(lcsproduct, "agrOverrideMASStyleName", false)).toString());
		
		if(FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrOverrideMASStyleName", false)))
			skuData.put("ERP.STYLENAME", AgronCSVGenerator.getData(lcsproduct, "agrOverrideMASStyleName", false));
		
		/* Not required in Integration as per client request on 5/19/2020
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrOverrideMASStyleName", false)))
			skuData.put("ERP.agrOverrideMASStyleName",
					AgronCSVGenerator.getData(lcsproduct, "agrOverrideMASStyleName", false));
			*/

		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrStrappedLength", false))) {

			//String value = "";
			//value = AgronCSVGenerator.getData(lcsproduct, "agrBagstrapdroplength", false);
			skuData.put("ERP.agrStrappedLength",
					AgronCSVGenerator.getData(lcsproduct, "agrStrappedLength", false));

		}

		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet01", false)))
			skuData.put("ERP.agrBullet01", AgronCSVGenerator.getData(lcsproduct, "agrBullet01", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet02", false)))
			skuData.put("ERP.agrBullet02", AgronCSVGenerator.getData(lcsproduct, "agrBullet02", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet03", false)))
			skuData.put("ERP.agrBullet03", AgronCSVGenerator.getData(lcsproduct, "agrBullet03", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet04", false)))
			skuData.put("ERP.agrBullet04", AgronCSVGenerator.getData(lcsproduct, "agrBullet04", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet05", false)))
			skuData.put("ERP.agrBullet05", AgronCSVGenerator.getData(lcsproduct, "agrBullet05", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet06", false)))
			skuData.put("ERP.agrBullet06", AgronCSVGenerator.getData(lcsproduct, "agrBullet06", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet07", false)))
			skuData.put("ERP.agrBullet07", AgronCSVGenerator.getData(lcsproduct, "agrBullet07", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet08", false)))
			skuData.put("ERP.agrBullet08", AgronCSVGenerator.getData(lcsproduct, "agrBullet08", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet09", false)))
			skuData.put("ERP.agrBullet09", AgronCSVGenerator.getData(lcsproduct, "agrBullet09", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBullet10", false)))
			skuData.put("ERP.agrBullet10", AgronCSVGenerator.getData(lcsproduct, "agrBullet10", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrBulletDescription", false)))
			skuData.put("ERP.agrBulletDescription",
					AgronCSVGenerator.getData(lcsproduct, "agrBulletDescription", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrFabric%", false)))
			skuData.put("ERP.agrFabric%", AgronCSVGenerator.getData(lcsproduct, "agrFabric%", false));
		/*if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrCapacity", false)))
			skuData.put("ERP.agrCapacity", AgronCSVGenerator.getData(lcsproduct, "agrCapacity", false));
		FlexUOM uom = (FlexUOM)lcsproduct.getValue("agrCapacity");
		if(uom!=null)
		{
		LOGGER.info("FlexUOM--------11111111"+uom.getUOMId());
		LOGGER.info("FlexUOM--------11111111"+uom.getCalculatableValue());
		
		//LOGGER.info("FlexUOM--------11111111"+uom.valueOf(uom.getUOMId()));
		}*/
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrEcomName", false)))
			skuData.put("ERP.agrEcomName", AgronCSVGenerator.getData(lcsproduct, "agrEcomName", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrDrBrand", false)))
			skuData.put("ERP.agrDrBrand", AgronCSVGenerator.getData(lcsproduct, "agrDrBrand", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrNdrfamily", false)))
			skuData.put("ERP.agrNdrfamily", AgronCSVGenerator.getData(lcsproduct, "agrNdrfamily", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "argCareInstructions", false)))
			skuData.put("ERP.argCareInstructions", AgronCSVGenerator.getData(lcsproduct, "argCareInstructions", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "agrProductDescription", false)))
			skuData.put("ERP.agrProductDescription",
					AgronCSVGenerator.getData(lcsproduct, "agrProductDescription", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcsproduct, "productName", false)))
			skuData.put("ERP.productName", AgronCSVGenerator.getData(lcsproduct, "productName", false));
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcssku, CATALOG_COLORWAY_NAME, false))) {
			String removePipeAndSpaces = "";
			removePipeAndSpaces = AgronCSVGenerator.getData(lcssku, CATALOG_COLORWAY_NAME, false);
			StringTokenizer stringtokenizer = new StringTokenizer(removePipeAndSpaces, " | ");
			String temp = "";
			String token = "";
			while (stringtokenizer.hasMoreTokens()) {
				token = stringtokenizer.nextToken();
				temp = (new StringBuilder()).append(temp).append(" ").append(token).toString();
			}
			skuData.put("ERP.catalogColorwayName", temp.replaceAll(" / ", "").trim());
		}
		if (FormatHelper.hasContent(AgronCSVGenerator.getData(lcssku, SEASON_ASSOCIATED_KEY, false))) {
			String input = (new StringBuilder()).append(AgronCSVGenerator.getData(lcssku, SEASON_ASSOCIATED_KEY, false))
					.append(",").append(formattedSeasonName).toString();
			deDup(input);
			skuData.put("ERP.seasonAssociated", deDup(input));
		} else {
			String input = formattedSeasonName;
			skuData.put("ERP.seasonAssociated", deDup(input));
		}
		return skuData;
	}

	/**
	 * This method is used to get the key pair map.
	 * 
	 * @return String type
	 */
	public static String deDup(String s) {
		return (new LinkedHashSet(Arrays.asList(s.split(",")))).toString().replaceAll("(^\\[|\\]$)", "").replace(", ",
				",");
	}

	/**
	 * This method is used to get the key pair map.
	 * 
	 * @return Map type
	 */
	public Map getKeyPair() {
		return this.keyPair;
	}

	/**
	 * This method is used to set the keyPair map.
	 * 
	 * @param keyPair is Map object
	 */
	public void setKeyPair(Map map) {
		this.keyPair = map;
	}

	/**
	 * This method is used to set the formatted season name.
	 * 
	 * @param seasonName is String object
	 */
	public void setFormattedSeasonName(String seasonName) {
		this.formattedSeasonName = seasonName;
	}

	/**
	 * This method is used to set the DeliverySeason in ERP.
	 */

	public static String setDeliverySeason() {

		String input = formattedSeasonName;
		return (deDup(input));
	}

	/**
	 * This method is used to get the error status.
	 * 
	 * @return Boolean type
	 */
	public boolean getErrorStatus() {
		return this.isErrorOccure;
	}

	public static Collection<ProductSizeCategory> findValidPSDs(LCSSKU sku,LCSSeason skuseason) throws WTException {
		LCSProduct product = com.lcs.wc.season.SeasonProductLocator.getProductARev(sku);
		
		SearchResults sr = (new SizingQuery()).findPSDByProductAndSeason((Map) null, product, skuseason,
				(FlexType) null, new Vector(), new Vector());
		LOGGER.info("sku name ============= "+sku.getValue("skuName")+" size of product sizes"+sr.getResults().size());
		Iterator psctsIterator = LCSQuery.iterateObjectsFromResults(sr,"OR:com.lcs.wc.sizing.ProdSizeCategoryToSeason:", "PRODSIZECATEGORYTOSEASON.IDA2A2");
		//psctsIterator= sortedIterator(psctsIterator);
		Vector<ProductSizeCategory> validPsdCollection = new Vector();
		while (psctsIterator.hasNext()) {
			ProdSizeCategoryToSeason pscts = (ProdSizeCategoryToSeason) psctsIterator.next();
			ProductSizeCategory psc = (ProductSizeCategory) VersionHelper.latestIterationOf(pscts.getSizeCategoryMaster());
			if (isValidPsc(psc)) {
				LOGGER.info("sku name ============= "+sku.getValue("skuName")+"Sizing object name "+psc.getName());
				validPsdCollection.add(psc);
				LOGGER.info("PSC:- " + psc + "::-----" + psc.getName() + "::---" + psc.getCreateTimestamp() );
		}
		}
		
		validPsdCollection.sort(Comparator.comparing(ProductSizeCategory::getCreateTimestamp));
		return validPsdCollection;
	}

	public static boolean isAgronSeasonType(LCSSeason season) {
		boolean isAgronType = false;
					
		if ("Agron".equalsIgnoreCase(season.getFlexType().getTypeDisplayName()))
			isAgronType=true;	
		
		return isAgronType;
		
		
	}
	public static boolean isValidPsc(ProductSizeCategory psc) {
		boolean isValidPSC = false;
		try {
			Collection<ProdSizeCategoryToSeason> pscToSeasonCollection = new SizingQuery()
					.findRelatedProdSizeCategoryToSeason(psc);
			LOGGER.debug("PSC:- " + psc + "::" + psc.getName() + "::" + psc.getCreateTimestamp() + "::pscToSeasons::"
					+ pscToSeasonCollection.size());
			if (pscToSeasonCollection != null && pscToSeasonCollection.size() > 0) {
				isValidPSC = true;
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return isValidPSC;

	}

}
