package com.agron.wc.integration.dw;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.agron.wc.integration.dw.model.PTC_ColorwaySeason_Staging;
import com.agron.wc.integration.dw.model.PTC_Colorway_Staging;
import com.agron.wc.integration.dw.model.PTC_LoadHistory;
import com.agron.wc.integration.dw.model.PTC_ModelSeasonVendor_Staging;
import com.agron.wc.integration.dw.model.PTC_ModelSeason_Staging;
import com.agron.wc.integration.dw.model.PTC_Model_Staging;
import com.agron.wc.integration.dw.model.PTC_SizeSeasonVendor_Staging;
import com.agron.wc.integration.dw.model.PTC_Size_Staging;
import com.agron.wc.integration.dw.model.PTC_Vendor_Staging;
import com.agron.wc.integration.dw.util.AgronDWDataHelperUtil;
import com.agron.wc.integration.dw.util.HibernateUtil;
import com.extjs.gxt.ui.client.util.Format;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.LineSheetQuery;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeMaster;
import com.lcs.wc.skusize.SKUSizeQuery;
import com.lcs.wc.skusize.SKUSizeToSeason;
import com.lcs.wc.skusize.SKUSizeToSeasonMaster;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.util.WTException;

public class ExtractAndLoadModelData {

	public static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.dw");
	public static  AtomicInteger seq = new AtomicInteger(); // For sequences in SKU SIze table.
	private static Map<String,String> costsheetParams = new HashMap<String,String>();
	private static ArrayList<String> attList = new ArrayList<String>();
	private static	FlexType csFlexType =null;
	private static	  String csNameCol = "";
	private static	  String baseFobCol = "";
	private static	  String effFobCol = ""; 
	private static	  String agrAgencyFeePercentCol = ""; 
	private static	  String agrDutyPercentCol = ""; 
	private static	  String agrFreightPercentCol = ""; 
	private static	  String agrPackagingCostDollarCol = ""; 
	private static	  String agrWarehouseCostPercentCol = ""; 
	private static	  String agrRoyaltyPercentCol = ""; 
	private static	  String agrSubRoyaltyPercentCol = ""; 
	private static	  String agrTariffPercentCol = ""; 
	
	private static	  HashMap<String, Integer> sizeMap = new HashMap<String, Integer>(20); 
	static {
		attList.add("name");
		attList.add("agrBaseFOB");
		attList.add("fobPrice");
		attList.add("productName");
		attList.add("applicableSizes2");
		attList.add("agrAgencyFeePercent");
		attList.add("agrDutyPercent");
		attList.add("agrFreightPercent");
		attList.add("agrPackagingCostDollar");
		attList.add("agrWarehouseCostPercent");
		attList.add("agrRoyaltyPercent");
		attList.add("agrSubRoyaltyPercent");
		attList.add("agrTariffPercent");
		costsheetParams.put("sortBy1:", "name");
		
		
		// Build the size map to generate unique PTC SIze ID.
		
		sizeMap.put("XS", 0);
		sizeMap.put("S", 1);
		sizeMap.put("M", 2);
		sizeMap.put("L", 3);
		sizeMap.put("XL", 4);
		sizeMap.put("XXL", 5);
		sizeMap.put("S/M", 6);
		sizeMap.put("L/XL", 7);
		sizeMap.put("OSFA", 8);
		sizeMap.put("2XL", 9);
		sizeMap.put("3XL", 10);
		sizeMap.put("4XL", 11);
		


	}
	public void  processSeason(ArrayList<LCSSeason> seasonList, PTC_LoadHistory loadHistory) throws Exception {

		csFlexType = FlexTypeCache.getFlexTypeRoot("Cost Sheet");
		csNameCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("name").getColumnName();
		baseFobCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrBaseFOB").getColumnName();
		effFobCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("fobPrice").getColumnName();
		agrAgencyFeePercentCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrAgencyFeePercent").getColumnName();
		agrDutyPercentCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrDutyPercent").getColumnName();
		agrFreightPercentCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrFreightPercent").getColumnName();
		agrPackagingCostDollarCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrPackagingCostDollar").getColumnName();
		agrWarehouseCostPercentCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrWarehouseCostPercent").getColumnName();
		agrRoyaltyPercentCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrRoyaltyPercent").getColumnName();
		agrSubRoyaltyPercentCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrSubRoyaltyPercent").getColumnName();
		agrTariffPercentCol = FlexTypeCache.getFlexTypeRoot("Cost Sheet").getAttribute("agrTariffPercent").getColumnName();


		boolean txState = false;
		Session session = null;

		try {
			if(seasonList == null) {
				return;
			}

			FlexType flexTypeProduct = FlexTypeCache.getFlexTypeFromPath("Product");

			// Process season by season , the latest season gets processed first
			for (int i = 0; i < seasonList.size(); i++) {

				LCSSeason season = seasonList.get(i);
				// SKIP this season if Downstream Integration is disabled.
				String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
				if("agrNo".equals(isagrDownstreamIntegrated)) {
					LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
					continue;
				}
				LineSheetQuery lsQuery = new LineSheetQuery();
				Map<String, String>  criteria = new HashMap<String, String>();
				criteria.put(flexTypeProduct.getAttribute("agrAdopted").getSearchCriteriaIndex(),"true");
				LOGGER.debug("skuCriteriaMap  >>"+criteria);
				lsQuery.setPlaceholderMode("PRODUCTONLY");
				Collection<FlexObject> coll = lsQuery.runSeasonProductReport(null, season, true,criteria, true,null,false, false, null, false,false, null); 

				session = HibernateUtil.getSessionFactory().openSession();
				session.beginTransaction();

				Iterator<FlexObject> itr = coll.iterator();
				LOGGER.debug(season.getName() + " --  Size of the Linesheet to Process " + coll.size());   

				while(itr.hasNext()) {
					FlexObject fObj = itr.next();

					LCSProduct product = (LCSProduct) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSProduct:"+fObj.getData("LCSPRODUCT.IDA2A2"));
					if(product == null) {
						continue; // For data issues
					}
					if(!product.isLatestIteration()) {
						product = (LCSProduct) VersionHelper.latestIterationOf((LCSProduct) VersionHelper.getVersion( ((product).getMaster()),"A"));
					}

					Long workNumber = (Long)product.getValue("agrWorkOrderNoProduct");
					LOGGER.info(" @@@@@@@@@@ Season " +season.getName()+" Processing Work #  " + workNumber +  "@@@@@@@@@@");


					PTC_Model_Staging existingModel = session.get(PTC_Model_Staging.class, workNumber.intValue());
					if (existingModel == null) {

						PTC_Model_Staging _model = createProduct(product);				  
						session.save(_model);
						LOGGER.info("PTC_Model_Staging - Adding  Work# " + workNumber);
					}else {
						LOGGER.info("PTC_Model_Staging - Model Already Loaded , Skipping Work# " + workNumber);  
					}

					// Process Model Season 
					PTC_ModelSeason_Staging _modelSeason = createProductSeason(product,season, session);		
					PTC_ModelSeason_Staging existingModelSeason = session.get(PTC_ModelSeason_Staging.class, _modelSeason.getPTC_ModelSeasonKey());

					if(existingModelSeason != null) {
						LOGGER.info(" PTC_ModelSeason_Staging - Already Loaded hence Skipping Work# " + workNumber + "PTC_ModelSeason_Key" + _modelSeason.getPTC_ModelSeasonKey());  
					}else {				 
						session.save(_modelSeason);
						LOGGER.info(" PTC_ModelSeason_Staging - Added Work# " + workNumber + "PTC_ModelSeason_Key" + _modelSeason.getPTC_ModelSeasonKey());  

					}

					// Process SKU
					PTC_Colorway_Staging _existingcolorway = null;
					LCSSKU sku = (LCSSKU) LCSQuery.findObjectById("OR:com.lcs.wc.product.LCSSKU:"+fObj.getData("LCSSKU.IDA2A2"));
					if(sku != null && !sku.isLatestIteration()) {
						sku = (LCSSKU)VersionHelper.latestIterationOf((LCSSKU) VersionHelper.getVersion( ((sku).getMaster()),"A"));
					}

					_existingcolorway =  session.get(PTC_Colorway_Staging.class,(sku != null ? ((Long)sku.getValue("agrColorwayUniqueID")).intValue() :null));

					if(_existingcolorway == null) {

						PTC_Colorway_Staging _sku = createSKU(sku, workNumber);
						session.save(_sku);
						LOGGER.info(" PTC_Colorway_Staging - SKU Added " + (String)sku.getValue("agrArticle") );  
						
					}else {
						LOGGER.info(" PTC_Colorway_Staging - SKU Already Loaded hence Skipping Article " + (String)sku.getValue("agrArticle") );  

					}

					// Process SKU Season
					PTC_ColorwaySeason_Staging _colorwaySeason = createSKUSeason(sku, season, workNumber);
					if(_colorwaySeason != null) {
						PTC_ColorwaySeason_Staging existingskuSeason = session.get(PTC_ColorwaySeason_Staging.class, _colorwaySeason.getPTC_ColorwaySeasonKey());
						if(existingskuSeason == null) {

							session.save(_colorwaySeason);
							LOGGER.info(" PTC_ColorwaySeason_Staging -  Adding  Article " + (String)sku.getValue("agrArticle") );  

						}else {
							LOGGER.info(" PTC_ColorwaySeason_Staging -  Already loaded Skipping Article " + (String)sku.getValue("agrArticle") );  

						}
					}

					LCSSourcingConfigQuery sConfigQuery = new LCSSourcingConfigQuery();
					Collection<LCSSourcingConfig> sourceColl = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(product.getMaster(), season.getMaster());				 
					Iterator<LCSSourcingConfig> sourceItr = sourceColl.iterator();

					// Processing sourcing Config and SourtoseasonLink
					while(sourceItr.hasNext()) {
						LCSSourcingConfig sConfig = sourceItr.next();
						LCSSourceToSeasonLink sourceToSeasonLink = sConfigQuery.getSourceToSeasonLink( sConfig,  season) ;		

						PTC_Vendor_Staging _sConfigFromDW = createSourcingData(sConfig,sourceToSeasonLink,workNumber,_modelSeason,season,session); 
						
						// PROCESS SKU SIZE
						createSKUSize(product,workNumber, season,sku,sConfig,_sConfigFromDW,_colorwaySeason,session);

					}
				}
				session.getTransaction().commit();

			}// Season Loop Ends


			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();

			loadHistory.setStagingTableLoadEndTime(new Date());
			loadHistory.setStagingTableLoadComplete(true);
			session.save(loadHistory);
			LOGGER.error("PTC_Loadhistory Success Entry created");

			session.getTransaction().commit();
			session.close();
			txState = true;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception coughtin DW Export " + this.getClass().getSimpleName());
			throw e;
		}finally {

			if(txState == false && session != null) {
				session.getTransaction().rollback();
				LOGGER.error("Issue with DW EXport in " + this.getClass().getSimpleName());
				if(session.isOpen()){
						session.close();
					}
			}
		}
	}
	
	public PTC_Model_Staging createProduct (LCSProduct product) throws Exception {

		PTC_Model_Staging _model = new PTC_Model_Staging();
		Long workNumber = (Long)product.getValue("agrWorkOrderNoProduct");
		_model.setWorkNumber(workNumber.intValue());
		_model.setAdidasModelNumber((String)product.getValue("agrAdidasModel"));
		_model.setBagStrapDropLengthInch(String.valueOf(product.getValue("agrBagstrapdroplength"))); 
		Double catalogSortOrder = (Double)product.getValue("agrCatalogSortOrder");
		_model.setCatalogSortOrder(catalogSortOrder != null ? catalogSortOrder.intValue():0);
		_model.setBrand(AgronDWDataHelperUtil.getDataFromKey(product, "agrDrbrand", false));
		_model.setBullet01((String)product.getValue("agrBullet01"));
		_model.setBullet02((String)product.getValue("agrBullet02"));
		_model.setBullet03((String)product.getValue("agrBullet03"));
		_model.setBullet04((String)product.getValue("agrBullet04"));
		_model.setBullet05((String)product.getValue("agrBullet05"));
		_model.setBullet06((String)product.getValue("agrBullet06"));
		_model.setBullet07((String)product.getValue("agrBullet07"));
		_model.setBullet08((String)product.getValue("agrBullet08"));
		_model.setBullet09((String)product.getValue("agrBullet09"));
		_model.setBullet10((String)product.getValue("agrBullet10"));
		_model.setCareInstructions(AgronDWDataHelperUtil.getDataFromKey(product, "argCareInstructions", false));
		_model.setCatalogFabric(AgronDWDataHelperUtil.getDataFromKey(product, "agrCatalogFabric", false));
		_model.setCollection(AgronDWDataHelperUtil.getDataFromKey(product, "agrCollectionProduct", false));
		_model.setAdidasProductType(AgronDWDataHelperUtil.getDataFromKey(product, "agrAdidasProductType", false));
		_model.setOrderMultiples(AgronDWDataHelperUtil.getDataFromKey(product, "agrOrderMultiple", false));
		Long agrMasterCartonInnerPack = (Long)product.getValue("agrMasterCartonInnerPack");
		_model.setMasterInnerCartonQty((agrMasterCartonInnerPack !=  null ) ? agrMasterCartonInnerPack.intValue() : 0);
		_model.setFamily(AgronDWDataHelperUtil.getDataFromKey(product, "agrNdrfamily", false));
		_model.setGender(AgronDWDataHelperUtil.getDataFromKey(product, "agrGenderProd", false));
		_model.setHandleDropLengthInch(String.valueOf(product.getValue("agrHandleDropLength"))); // - Float

		String agrEcomName = (String)product.getValue("agrEcomName");
		_model.setEcom_Name(agrEcomName);

		String masStyleName = (String)product.getValue("agrMASStyleName");
		_model.setMASStyleName(masStyleName);
		
		_model.setStyleNameRoot((String)product.getValue("agrStyleName"));
		
		Long agrMasterCartonCasePack = (Long)product.getValue("agrMasterCartonCasePack");
		_model.setMasterCartonCasePack((agrMasterCartonCasePack !=  null ) ? agrMasterCartonCasePack.doubleValue() : 0); //  Integer 

		_model.setMasterCartonDepthInch(((Double)product.getValue("agrMasterCartonDepth"))); 
		_model.setMasterCartonWeightLB(((Double)product.getValue("agrMasterCartonWt")));
		_model.setMasterCartonWidthInch(((Double)product.getValue("agtMasterCartonWidth")));
		_model.setMasterCartonLengthInch(((Double)product.getValue("agrMasterCartonLength"))); 

		_model.setOverrideMASStyleName((String)product.getValue("agrOverrideMASStyleName"));
		_model.setPackagingType(AgronDWDataHelperUtil.getDataFromKey(product, "agrPackageType", false));
		_model.setParentWorkNumber((String)product.getValue("agrParentWorkNo")); 
		_model.setProductActualHeightInch(String.valueOf(product.getValue("agrDepth"))); 
		_model.setProductActualLengthInch(String.valueOf(product.getValue("agrHeight")));
		_model.setProductActualWidthInch(String.valueOf(product.getValue("agrWidth")));
		_model.setProductDescription((String)product.getValue("agrProductDescription"));
		_model.setProductLine(AgronDWDataHelperUtil.getDataFromKey(product, "agrProductLine", false));
		_model.setProductName((String)product.getValue("productName"));
		_model.setPackSize(AgronDWDataHelperUtil.getDataFromKey(product, "agrPackCountProd", false));
		_model.setSilhouette(AgronDWDataHelperUtil.getDataFromKey(product, "agrSilhouetteProd", false));
		String productType = product.getFlexType().getTypeDisplayName().toUpperCase();
		if("HEADWEAR".equals(productType)) {

			if ( "KNITS".equals(productType)) {

				productType = "KNIT";
			}							

		}else if("TEAM".equalsIgnoreCase(productType)){
			productType = "SOCK-TEAM";
		}else if("SOCKS".equalsIgnoreCase(productType)){
			productType = "SOCK";

		}else if("BAGS".equalsIgnoreCase(productType)){
			productType = "BAG";

		}else if("Sport Accessories".equalsIgnoreCase(productType)){
			productType = "SP ACC";

		}

		_model.setType(productType);
		String capacity = AgronDWDataHelperUtil.getDataFromKey(product, "agrCapacity", false);
		_model.setCapacity(capacity);

		_model.setPackagedProductHeightInch(((Double)product.getValue("agrPackagedProductHeight")));
		_model.setPackagedProductLengthInch(((Double)product.getValue("agrPackagedProductLength")));
		_model.setPackagedProductWeightLB(((Double)product.getValue("agrPackagedProductWeight")));
		_model.setPackagedProductWidthInch(((Double)product.getValue("agrPackagedProductWidth")));
		_model.setReferenceWorkNumber((String)product.getValue("agrReferenceWorkNo"));
		return _model;

	}


	private PTC_ModelSeason_Staging createProductSeason(LCSProduct product, LCSSeason season, Session session) throws Exception {

		PTC_ModelSeason_Staging _modelSeason = new PTC_ModelSeason_Staging();
		LCSSeasonProductLink spLink = LCSSeasonQuery.findSeasonProductLink(product, season);
		if(spLink ==null) {
			return _modelSeason;
		}

		_modelSeason.setPTC_ModelSeasonKey((int)spLink.getProductSeasonRevId());
		Long workNumber = (Long)product.getValue("agrWorkOrderNoProduct");
		_modelSeason.setWorkNumber(workNumber.intValue());
		_modelSeason.setPTC_SeasonKey(Long.valueOf(season.getBranchIdentifier()).intValue());

		Long catalogSKUCount = (Long)spLink.getValue("agrCatalogSKUCount");
		_modelSeason.setCatalogSKUCount((catalogSKUCount != null) ? Integer.valueOf(catalogSKUCount.intValue()) : 0); 

		String agrDeliveryseason = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrDeliveryseason", false);
		_modelSeason.setDeliverySeasons(agrDeliveryseason);


		_modelSeason.setGlobal(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrGlobal", false));
		_modelSeason.setSustainable(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrNSustainable", false));

		_modelSeason.setTechnologies(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrNTechnology", false));

		_modelSeason.setRetailPrice(Double.valueOf(((Double)spLink.getValue("agrRetail")).doubleValue()));
		
		double wholesalePrice = ((Double)spLink.getValue("agrWholesale")).doubleValue();
		String wholesale  = FormatHelper.formatWithPrecision(wholesalePrice, 2);	
		if(FormatHelper.hasContent(wholesale)) {
		 _modelSeason.setWholesalePrice(Double.valueOf(wholesale));
		}
		
		_modelSeason.setOverrideRetailPrice(Double.valueOf(((Double)spLink.getValue("agrOverrideRetailPrice")).doubleValue()));
		_modelSeason.setOverrideWholeSalePrice(Double.valueOf(((Double)spLink.getValue("agrOverrideWholesalePrice")).doubleValue()));

		_modelSeason.setOverrideWholesaleDate((Boolean)spLink.getValue("agrOverrideWholesaleDate")); 
		_modelSeason.setOverrideRetailDate((Boolean)spLink.getValue("agrOverrideRetailDate")); 
		_modelSeason.setRetailEffectiveDate((Date)spLink.getValue("agrRetailNewEffectiveDate"));
		_modelSeason.setWholesaleEffectiveDate((Date)spLink.getValue("agrWholesaleNewEffectiveDat"));
		_modelSeason.setSport(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrNsport", false));
		String recycledPer = (String)spLink.getValue("agrRecycledPercentnew");
		_modelSeason.setRecycledPercent(recycledPer);
		_modelSeason.setFranchiseStyle((Boolean)spLink.getValue("agrFranchiseStyle"));
		
		

		FlexObject strManager = (FlexObject) spLink.getValue("agrStraubManager");

		if(strManager != null) {
			_modelSeason.setStraubManager(strManager.getString("FULLNAME"));
		}
	
		return _modelSeason;
	}

	private PTC_Colorway_Staging createSKU(LCSSKU	sku,Long workNumber) throws WTException {

		PTC_Colorway_Staging _sku = new PTC_Colorway_Staging();
		_sku.setWorkNumber(workNumber.intValue());
		_sku.setPTC_ColorwayUniqueID(((Long)sku.getValue("agrColorwayUniqueID")).intValue());
		_sku.setRootArticleNumber((String)sku.getValue("agrArticle"));
		_sku.setAdidasArticleNumber((String)sku.getValue("agrAdidasArticle"));
		_sku.setCatalogColowayName((String)sku.getValue("agrColorwayName"));

		LCSColor color = (LCSColor)sku.getValue("color");
		if(color != null) {
			_sku.setColor1(color.getName());
			_sku.setNRFColorCode(AgronDWDataHelperUtil.getDataFromKey(color, "agrNRFCode", false));
			_sku.setNRFColorDescription(AgronDWDataHelperUtil.getDataFromKey(color, "agrNRFDescription", false));
		}		
		color = (LCSColor)sku.getValue("agrColor2");
		if(color != null) {
			_sku.setColor2(color.getName());
		}
		color = (LCSColor)sku.getValue("agrColor3");
		if(color != null) {
			_sku.setColor3(color.getName());
		}
		_sku.setColorwayLetter((String)sku.getValue("agrColorwayLetter"));

		_sku.setColorModifier((String)sku.getValue("agrColormodifier"));
		_sku.setFabricContent((String)sku.getValue("agrFabric"));

		String catalogFabric = AgronDWDataHelperUtil.getDataFromKey(sku, "agrCatalogFabric", false);
		_sku.setCatalogFabric(catalogFabric);
		_sku.setNRFCombinedColorDescription((String)sku.getValue("agrColorwayDescription"));
		_sku.setAssigned(AgronDWDataHelperUtil.getDataFromKey(sku, "agrAdopted", false));
		
		return _sku;
	}
	

	private PTC_ColorwaySeason_Staging createSKUSeason(LCSSKU	sku,LCSSeason season,Long workNumber) throws WTException, ParseException {

		PTC_ColorwaySeason_Staging _skuSeason = new PTC_ColorwaySeason_Staging();

		_skuSeason.setPTC_SeasonKey(Long.valueOf(season.getBranchIdentifier()).intValue());
		_skuSeason.setPTC_ColorwayUniqueID(((Long)sku.getValue("agrColorwayUniqueID")).intValue());

		LCSSeasonProductLink spLink = LCSSeasonQuery.findSeasonProductLink(sku, season);
		
		if(spLink == null) {return null;}
		_skuSeason.setPTC_ColorwaySeasonKey((int)spLink.getSkuSeasonRevId());	
		Date agrAvailabilityDate = (Date)spLink.getValue("agrAvailabilityDate");
		_skuSeason.setAvailabilityDate(agrAvailabilityDate);
		_skuSeason.setBaseColor(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrBaseColor", false));
		_skuSeason.setCarryoverColorway(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrCarryoverColorway", false));
		_skuSeason.setCapsule(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrCapsule", false));
		
		String catalog = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrCatalog", false);
		if(FormatHelper.hasContent(catalog)) {
			
			List<String> catalogList = Arrays.asList(catalog.split(", "));
			Collections.sort(catalogList);
			catalog = String.join(", ", catalogList);								
		}
		_skuSeason.setCatalogs(catalog);
		_skuSeason.setColorwayStatus(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrColorwaystatus", false));
		_skuSeason.setConsolidatedBuyDate1((Date)spLink.getValue("agrCBD1"));
		_skuSeason.setConsolidatedBuyDate2((Date)spLink.getValue("agrCBD2"));
		_skuSeason.setConsolidatedBuyDate3((Date)spLink.getValue("agrCBD3"));
		_skuSeason.setConsolidatedBuyDate4((Date)spLink.getValue("agrCBD4"));
		_skuSeason.setDateDropped((Date)spLink.getValue("agrDroppedDate"));
		_skuSeason.setIsSMU(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrIsSMU", false));
		_skuSeason.setSMUAccount(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrSMUAccountInline", false));
		Long agrMOQ = (Long)spLink.getValue("agrMOQ");
		_skuSeason.setMinimumOrderQuantity(agrMOQ != null ? agrMOQ.intValue():0);
		_skuSeason.setReqConsolidation(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrReqConsolidation", false));
		_skuSeason.setSustainable(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrNSustainable", false));
		_skuSeason.setPrimaryRootColor((String)sku.getValue("primaryRootColorName"));
		_skuSeason.setSMUSpecialHandling(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrSMUSpecialHandling", false));
		Long agrTotalForecast = (Long)spLink.getValue("agrTotalForecast");
		_skuSeason.setForecast(agrTotalForecast != null ? agrTotalForecast.intValue():0);
		
		
		_skuSeason.setProductReceiptStatus(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrProductReceiptStatus", false));
		_skuSeason.setProductReceiptStatusDate((Date)spLink.getValue("agrProductReceiptStatusDate"));
		
		String agrDHLAgron= (String)spLink.getValue("agrDHLAgron");
		if(FormatHelper.hasContent(agrDHLAgron)) {
				_skuSeason.setDhlAgron(agrDHLAgron);			
		}
		
		String agrDHLStraub= (String)spLink.getValue("agrDHLStraub");
		if(FormatHelper.hasContent(agrDHLStraub)) {		
				_skuSeason.setDhlStraub(agrDHLStraub);		
		}

		_skuSeason.setLaydownImageStatus(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrLaydownImageStatus", false));
		_skuSeason.setMannequinImageStatus(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrMannequinImageStatus", false));
		_skuSeason.setOnModelImageStatus(AgronDWDataHelperUtil.getDataFromKey(spLink, "agrOnModelImageStatus", false));
		_skuSeason.setPriorityComments((String)spLink.getValue("agrPriorityComments"));
		
		String agrPriorityComments= (String)spLink.getValue("agrPriorityComments");
	
		 if(FormatHelper.hasContent(agrPriorityComments)) {
			if(agrPriorityComments.length() <=200) {
				_skuSeason.setPriorityComments(agrPriorityComments);
			}else {
				_skuSeason.setPriorityComments("COLSIZEISSUE-" + agrPriorityComments.length());
			}
		}
	

		String agrSpecialPhotoDirections= (String)spLink.getValue("agrSpecialPhotoDirections");
		if(FormatHelper.hasContent(agrSpecialPhotoDirections)) {
				_skuSeason.setSpecialPhotographyDirections(agrSpecialPhotoDirections);		
		}
		
		
		String agrB2B = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrB2B", false);
		
		if("Yes".equals(agrB2B)) {
			_skuSeason.setB2Bonly(true);
		}
		
		String agrPriority = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrPriority", false);
		
		if("Yes".equals(agrPriority)) {
			_skuSeason.setPriority(true);
		}
		String agrProductionCancelled = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrProductionCancelled", false);
		
		if("Yes".equals(agrProductionCancelled)) {
			_skuSeason.setProductionCancelled(true);
		}
		
	   String agrReturnSample = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrReturnSample", false);
		
		if("Yes".equals(agrReturnSample)) {
			_skuSeason.setReturnSampleToAgron(true);
		}
		
		
		String agrAGRCatalogImageStatus = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrAGRCatalogImageStatus", false);
		
		 if(FormatHelper.hasContent(agrAGRCatalogImageStatus)) {
			 _skuSeason.setCatalogImageStatus(agrAGRCatalogImageStatus);
		 }
		
		String BagsOnMannequin = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrBagsonMannequin", false);
		
		 if(FormatHelper.hasContent(BagsOnMannequin)) {
			 _skuSeason.setBagsOnMannequin(BagsOnMannequin);
		 }
		 
		String agrSampleETDtoStraub = AgronDWDataHelperUtil.getDataFromKey(spLink, "agrSampleETDtoStraub", false);
	
	
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		 if(FormatHelper.hasContent(agrSampleETDtoStraub)) {
			 _skuSeason.setSampleEtaToStraub(formatter.parse(agrSampleETDtoStraub));
		 }
		return _skuSeason;
	}


	private void createSKUSize(LCSProduct product,Long workNumber,LCSSeason season, LCSSKU sku, LCSSourcingConfig sConfig, PTC_Vendor_Staging _sConfigFromDW, PTC_ColorwaySeason_Staging _colorwaySeason, Session session) throws Exception {


		SearchResults sr = (new SizingQuery()).findPSDByProductAndSeason((Map) null, product, season,(FlexType) null, new Vector(), new Vector());

		Iterator psctsIterator = LCSQuery.iterateObjectsFromResults(sr,"OR:com.lcs.wc.sizing.ProdSizeCategoryToSeason:", "PRODSIZECATEGORYTOSEASON.IDA2A2");
		String sizeRange = "";
		while (psctsIterator.hasNext()) {
			ProdSizeCategoryToSeason psctoseason = (ProdSizeCategoryToSeason) psctsIterator.next();
			ProductSizeCategory psc = (ProductSizeCategory) VersionHelper.latestIterationOf(psctoseason.getSizeCategoryMaster());
			SearchResults skuSizeResults =  SKUSizeQuery.findSKUSizesForPSC(psc, sku.getMaster(), null, null);

			if(skuSizeResults.getResults().size() > 0 && FormatHelper.hasContent( psc.getSizeValues())) {						
				sizeRange = sizeRange + psc.getSizeValues();  						
			}

			FlexType skuSizeType = FlexTypeCache.getFlexTypeFromPath("Colorway Size");
			SKUSizeQuery sizeQuery = new SKUSizeQuery();
			SearchResults	skuSizeSeasonResults = new SKUSizeQuery().findViewableSKUSizesForPSC(new HashMap(), new Vector(), skuSizeType, psc, season, null, sku.getMaster(), null, null, true, true, false, false, null);
			Iterator<FlexObject> skuSizeSeasonItr = skuSizeSeasonResults.getResults().iterator();
			while(skuSizeSeasonItr.hasNext()) {

				FlexObject skuSizeToSeasonFlexObj = skuSizeSeasonItr.next();
				SKUSizeToSeason skuSizeToSeasonObj = (SKUSizeToSeason) LCSQuery.findObjectById("VR:com.lcs.wc.skusize.SKUSizeToSeason:"+skuSizeToSeasonFlexObj.getData("SKUSIZETOSEASON.BRANCHIDITERATIONINFO"));

				if(!skuSizeToSeasonObj.isLatestIteration()) {
					skuSizeToSeasonObj = (SKUSizeToSeason) VersionHelper.latestIterationOf(skuSizeToSeasonObj);
				}
				SKUSizeToSeasonMaster skuSizeSeasonMaster = (SKUSizeToSeasonMaster) skuSizeToSeasonObj.getMaster();
 
				SKUSize skuSizeObj =  (SKUSize) VersionHelper.latestIterationOf((SKUSize)VersionHelper.getVersion(skuSizeSeasonMaster.getSkuSizeMaster(),"A")); //(LCSProduct) VersionHelper.getVersion( ((product).getMaster()),"A")
				SKUSizeMaster skuSizeMaster = (SKUSizeMaster)(skuSizeObj.getMaster());
				
				if(!FormatHelper.hasContent((String)skuSizeObj.getValue("agrArticleNumber"))){
					continue; // This is to skip the SKU Size rows with no Article #
				}

				long seqPTC_SizeUniqueId = getUniquePTCSizeID(((Long)sku.getValue("agrColorwayUniqueID")),skuSizeMaster.getSizeValue());		
				
				Query query = session.createQuery("from PTC_Size_Staging where PTC_SizeUniqueID = :PTC_SizeUniqueID");
				query.setParameter("PTC_SizeUniqueID", seqPTC_SizeUniqueId); // Find this by Article # 
				
				PTC_Size_Staging _skuSize = null;
				if(query.getResultList().size() > 0) { 
					_skuSize = (PTC_Size_Staging)query.list().get(0);
				}
				if(_skuSize == null) {
					_skuSize = new PTC_Size_Staging();
					_skuSize.setPTC_SizeUniqueID( seqPTC_SizeUniqueId);
					_skuSize.setPTC_ColorwayUniqueID(((Long)sku.getValue("agrColorwayUniqueID")).intValue());
					_skuSize.setItemCode((String)skuSizeObj.getValue("agrArticleNumber"));
					_skuSize.setUPC((String)skuSizeObj.getValue("agrUPCNumber"));
					_skuSize.setProductSize1(skuSizeMaster.getSizeValue());
					_skuSize.setProductSize2(skuSizeMaster.getSize2Value());
					
					 session.save(_skuSize);
					
					LOGGER.info("PTC_Size_Staging - Add Article " + (String)skuSizeObj.getValue("agrArticleNumber") + "Season " + season.getName());
				}else {
					LOGGER.info("PTC_Size_Staging - Skip Article " + (String)skuSizeObj.getValue("agrArticleNumber") + "Season " + season.getName());

				}
				// SKU SIZE VENDOR @@@@@@@@@@@@@@@@@@@@ 
				PTC_SizeSeasonVendor_Staging _sizeSeasonVendor = null;

				int seasonKey = Long.valueOf(season.getBranchIdentifier()).intValue();
				int sConfigId = Long.valueOf(sConfig.getBranchIdentifier()).intValue();
				Query querySSV = session.createQuery("from PTC_SizeSeasonVendor_Staging where PTC_SeasonKey = :PTC_SeasonKey AND PTC_SizeUniqueID = :PTC_SizeUniqueID AND PTC_VendorUniqueID = : PTC_VendorUniqueID");
				querySSV.setParameter("PTC_SeasonKey",seasonKey); 
				querySSV.setParameter("PTC_SizeUniqueID",_skuSize.getPTC_SizeUniqueID());
				querySSV.setParameter("PTC_VendorUniqueID",sConfigId); 
				if(querySSV.getResultList().size() > 0) { 
					_sizeSeasonVendor = (PTC_SizeSeasonVendor_Staging)querySSV.list().get(0);
					LOGGER.debug("Item Code "+(String)skuSizeObj.getValue("agrArticleNumber")+" -- > Found _sizeSeasonVendor  --- "+ _sizeSeasonVendor);
				}
				if(_sizeSeasonVendor == null) {
					_sizeSeasonVendor = new PTC_SizeSeasonVendor_Staging();
					_sizeSeasonVendor.setPTC_SizeVendorSeasonKey(seq.incrementAndGet()); 
					_sizeSeasonVendor.setPTC_SeasonKey(seasonKey);
					_sizeSeasonVendor.setPTC_SizeUniqueID(_skuSize.getPTC_SizeUniqueID());							
					_sizeSeasonVendor.setPTC_VendorUniqueID(Long.valueOf(sConfig.getBranchIdentifier()).intValue());

					if(!"Active".equals(_colorwaySeason.getColorwayStatus()) && FormatHelper.hasContent(_colorwaySeason.getColorwayStatus())) {
						_sizeSeasonVendor.setSKUSizeStatus("Inactive"); // If the colorway is deactivated , override the SKU Size status.
					}else {			

						_sizeSeasonVendor.setSKUSizeStatus((skuSizeToSeasonFlexObj.getBoolean("SKUSIZETOSEASON.ACTIVE") && skuSizeToSeasonFlexObj.getBoolean("SKUSIZE.ACTIVE")) == true ? "Active": "Inactive");
					}


					HashMap<String,String> costsheet = getCostsheetData(costsheetParams,product,sku,  sConfig,  season, attList,skuSizeMaster) ;

					_sizeSeasonVendor.setSourcingConfig(_sConfigFromDW.getFactoryAgent());
					String csName = costsheet.get("name");
					_sizeSeasonVendor.setCostSheet(csName);
					if(costsheet.get("fobPrice") != null) {
						String fobPrice = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("fobPrice")), 2);						
						_sizeSeasonVendor.setFOB(Double.valueOf(fobPrice).doubleValue());
					}else {
						_sizeSeasonVendor.setFOB(0);
					}
					
					String IsPrimaryCostsheet = costsheet.get("IsPrimaryCostsheet");
					if(!FormatHelper.hasContent(IsPrimaryCostsheet) || "0".equals(IsPrimaryCostsheet) ) {
						_sizeSeasonVendor.setIsPrimaryCostSheet(false);
					}else if("1".equals(IsPrimaryCostsheet) ) {
						_sizeSeasonVendor.setIsPrimaryCostSheet(true);
					}
					
			
					if(costsheet.get("agrAgencyFeePercent") != null) {
						String agencyFee = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrAgencyFeePercent")), 2);						
						_sizeSeasonVendor.setAgencyFee(Double.valueOf(agencyFee).doubleValue());
					}else {
						_sizeSeasonVendor.setAgencyFee(0);
					}
					
					if(costsheet.get("agrDutyPercent") != null) {
						String Duty = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrDutyPercent")), 2);		
						_sizeSeasonVendor.setDuty(Double.valueOf(Duty).doubleValue());
					}else {
						_sizeSeasonVendor.setDuty(0);
					}
					
					if(costsheet.get("agrFreightPercent") != null) {
						String Freight = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrFreightPercent")), 2);						
						_sizeSeasonVendor.setFreight(Double.valueOf(Freight).doubleValue());
					}else {
						_sizeSeasonVendor.setFreight(0);
					}
					
					if(costsheet.get("agrPackagingCostDollar") != null) {
						String packaging = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrPackagingCostDollar")), 2);						
						_sizeSeasonVendor.setPackagingCost(Double.valueOf(packaging).doubleValue());
					}else {
						_sizeSeasonVendor.setPackagingCost(0);
					}
					
					if(costsheet.get("agrWarehouseCostPercent") != null) {
						String warehouseCost = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrWarehouseCostPercent")), 2);						
						_sizeSeasonVendor.setWarehouse(Double.valueOf(warehouseCost).doubleValue());
					}else {
						_sizeSeasonVendor.setWarehouse(0);
					}
					
					if(costsheet.get("agrRoyaltyPercent") != null) {
						String royalty = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrRoyaltyPercent")), 2);						
						_sizeSeasonVendor.setRoyaltyRate(Double.valueOf(royalty).doubleValue());
					}else {
						_sizeSeasonVendor.setRoyaltyRate(0);
					}
					
					if(costsheet.get("agrSubRoyaltyPercent") != null) {
						String collabRoyalty = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrSubRoyaltyPercent")), 2);						
						_sizeSeasonVendor.setSubRoyaltyRate(Double.valueOf(collabRoyalty).doubleValue());
					}else {
						_sizeSeasonVendor.setSubRoyaltyRate(0);
					}
					
					if(costsheet.get("agrTariffPercent") != null) {
						String tariff = FormatHelper.formatWithPrecision(Double.valueOf(costsheet.get("agrTariffPercent")), 2);						
						_sizeSeasonVendor.setTariff(Double.valueOf(tariff).doubleValue());
					}else {
						_sizeSeasonVendor.setTariff(0);
					}
					
					if(_sizeSeasonVendor.getFOB() > 0 ) {
						session.save(_sizeSeasonVendor);  // No need to save Costsheet with 0 FOB
					}
					LOGGER.info("PTC_SizeSeasonVendor_Staging + Add (Root)Article "+(String)skuSizeObj.getValue("agrArticleNumber") +" _sizeSeasonVendor " + _sizeSeasonVendor.toString() +" Season "+ season.getName());
				}else {
					LOGGER.info("PTC_SizeSeasonVendor_Staging + Skip  (Root)Article "+(String)skuSizeObj.getValue("agrArticleNumber") +" _sizeSeasonVendor " + _sizeSeasonVendor.toString() +" Season "+ season.getName());

				}
			}			
		}

		PTC_Model_Staging _existingmodel = session.get(PTC_Model_Staging.class, workNumber.intValue());
		if(_existingmodel != null) {
			_existingmodel.setSizeRange(MOAHelper.parseOutDelims(sizeRange, true));
			session.saveOrUpdate(_existingmodel);
			LOGGER.info(" Setting Size for Work # "+workNumber +" sizeRange "+ MOAHelper.parseOutDelims(sizeRange, true)); 

		}
	}

	private long getUniquePTCSizeID(long colorwayid, String sizeValue) {
		
		if(sizeValue != null && FormatHelper.hasContent(sizeValue) && colorwayid > 0) {
			
			int sizecode =  sizeMap.get(sizeValue).intValue();
			return Long.parseLong((String.valueOf(colorwayid) + String.valueOf(sizecode)));
		}
		
		return seq.incrementAndGet(); // Get from sequence
	 
	}

	private HashMap<String,String> getCostsheetData(Map<String, String> costsheetParams,LCSProduct  product, LCSSKU sku, LCSSourcingConfig sConfig, LCSSeason season, ArrayList<String> attList, SKUSizeMaster skuSizeMaster) throws WTException {

		HashMap<String,String> fObj = new HashMap<String,String> ();

		Collection<FlexObject> csList = LCSCostSheetQuery.getCostSheetsForProduct(costsheetParams, product,  sConfig,  season, attList, false, false) ;
		if (csList.isEmpty()){
			return fObj;

		}

		Iterator<FlexObject> csItr = csList.iterator();
		String SizeList = "";
		String ColorwayList = "";
		String skuSize = skuSizeMaster.getSizeValue();
		List SizeListArr = new ArrayList<String>();
		List ColorwayListArr = new ArrayList<String>();
		while(csItr.hasNext()) {

			FlexObject costSheetObj = csItr.next();
			LOGGER.debug("CostSheetName "+ costSheetObj.getData("LCSCOSTSHEET."+csNameCol));
			String costsheetName = costSheetObj.getData("LCSCOSTSHEET."+csNameCol);
			
		
			if(costsheetName.lastIndexOf("(") > 0 && costsheetName.lastIndexOf("(") > costsheetName.indexOf("(") &&
				costsheetName.lastIndexOf(")") > 0 && costsheetName.lastIndexOf(")")> costsheetName.indexOf(")") ) {
				SizeList = costsheetName.substring(costsheetName.lastIndexOf("(") + 1, costsheetName.lastIndexOf(")"));
				LOGGER.info("Size = " + SizeList);
				 SizeListArr =  Arrays.asList(SizeList.split(" "));
				ColorwayList = costsheetName.substring(costsheetName.indexOf("(") + 1, costsheetName.indexOf(")"));
				LOGGER.info("Colorways " + ColorwayList);
				 ColorwayListArr =  Arrays.asList(ColorwayList.split(" "));
			}

			if( FormatHelper.hasContent(SizeList) && !SizeListArr.contains(skuSize) ) {
				continue;
			}else if(SizeListArr != null && SizeListArr.contains(skuSize) && ColorwayListArr.contains((String)sku.getValue("agrColorwayLetter"))) { // TODO :Solve the problem when Name has A and AB

				fObj.put("fobPrice", costSheetObj.getData("LCSCOSTSHEET."+effFobCol));
				fObj.put("name", costSheetObj.getData("LCSCOSTSHEET."+csNameCol));
				fObj.put("IsPrimaryCostsheet", costSheetObj.getData("LCSCOSTSHEET.PRIMARYCOSTSHEET"));				
				fObj.put("agrAgencyFeePercent", costSheetObj.getData("LCSCOSTSHEET."+agrAgencyFeePercentCol));
				fObj.put("agrDutyPercent", costSheetObj.getData("LCSCOSTSHEET."+agrDutyPercentCol));
				fObj.put("agrFreightPercent", costSheetObj.getData("LCSCOSTSHEET."+agrFreightPercentCol));
				fObj.put("agrPackagingCostDollar", costSheetObj.getData("LCSCOSTSHEET."+agrPackagingCostDollarCol));
				fObj.put("agrWarehouseCostPercent", costSheetObj.getData("LCSCOSTSHEET."+agrWarehouseCostPercentCol));
				fObj.put("agrRoyaltyPercent", costSheetObj.getData("LCSCOSTSHEET."+agrRoyaltyPercentCol));
				fObj.put("agrSubRoyaltyPercent", costSheetObj.getData("LCSCOSTSHEET."+agrSubRoyaltyPercentCol));
				fObj.put("agrTariffPercent", costSheetObj.getData("LCSCOSTSHEET."+agrTariffPercentCol));
				LOGGER.info("Returning Costsheet 1111  " + fObj);
				break;
			}else if(!FormatHelper.hasContent(SizeList) && ColorwayList.contains((String)sku.getValue("agrColorwayLetter")) ) {
				fObj.put("fobPrice", costSheetObj.getData("LCSCOSTSHEET."+effFobCol));
				fObj.put("name", costSheetObj.getData("LCSCOSTSHEET."+csNameCol));
				fObj.put("IsPrimaryCostsheet", costSheetObj.getData("LCSCOSTSHEET.PRIMARYCOSTSHEET"));
				fObj.put("agrAgencyFeePercent", costSheetObj.getData("LCSCOSTSHEET."+agrAgencyFeePercentCol));
				fObj.put("agrDutyPercent", costSheetObj.getData("LCSCOSTSHEET."+agrDutyPercentCol));
				fObj.put("agrFreightPercent", costSheetObj.getData("LCSCOSTSHEET."+agrFreightPercentCol));
				fObj.put("agrPackagingCostDollar", costSheetObj.getData("LCSCOSTSHEET."+agrPackagingCostDollarCol));
				fObj.put("agrWarehouseCostPercent", costSheetObj.getData("LCSCOSTSHEET."+agrWarehouseCostPercentCol));
				fObj.put("agrRoyaltyPercent", costSheetObj.getData("LCSCOSTSHEET."+agrRoyaltyPercentCol));
				fObj.put("agrSubRoyaltyPercent", costSheetObj.getData("LCSCOSTSHEET."+agrSubRoyaltyPercentCol));
				fObj.put("agrTariffPercent", costSheetObj.getData("LCSCOSTSHEET."+agrTariffPercentCol));
				LOGGER.info("Returning Costsheet 2222  " + fObj);
			}
		}
		LOGGER.info("Returning Costsheet  " + fObj);
		return fObj;
		
	}

	private PTC_Vendor_Staging createSourcingData(LCSSourcingConfig sConfig, LCSSourceToSeasonLink sourceToSeasonLink,Long workNumber,PTC_ModelSeason_Staging _modelSeason, LCSSeason season, Session session) throws Exception {
		PTC_Vendor_Staging _sourcingConfig = new PTC_Vendor_Staging();
		_sourcingConfig.setPTC_VendorUniqueID(Long.valueOf(sConfig.getBranchIdentifier()).intValue()); // Duplicate 


		PTC_Vendor_Staging _existingsourcingConfig =  session.get(PTC_Vendor_Staging.class, _sourcingConfig.getPTC_VendorUniqueID());
		if(_existingsourcingConfig == null) {

			LCSSupplier factoryAgentObj = (LCSSupplier)sConfig.getValue("agrFactory");
			String  factoryAgent = "";
			String vendorNumber = "";
			if(factoryAgentObj == null) {
				factoryAgentObj = (LCSSupplier)sConfig.getValue("agrAgent");

			}

			if(factoryAgentObj != null) {
				factoryAgent = (String) factoryAgentObj.getName();
				vendorNumber = (String) factoryAgentObj.getValue("agrVendorNo");

			}
			_sourcingConfig.setFactoryAgent(factoryAgent); 
			_sourcingConfig.setVendorNumber(vendorNumber);
			LCSCountry country = (LCSCountry)sConfig.getValue("agrCountryOfOrigin");
			_sourcingConfig.setCountryOfOrigin(country != null ? country.getName(): "");  

			
			
			session.save(_sourcingConfig);
			LOGGER.info("PTC_Vendor_Staging - Add source " + sConfig.getName() + " Season " + season.getName());
			
		}else {		
			LOGGER.info("PTC_Vendor_Staging - Skip source " + sConfig.getName() + " Season " + season.getName());
		}

		PTC_ModelSeasonVendor_Staging _sourceSeasonLink = new PTC_ModelSeasonVendor_Staging();
		_sourceSeasonLink.setPTC_ModelSeasonVendorKey(Long.valueOf(sourceToSeasonLink.getBranchIdentifier()).intValue()); 


		PTC_ModelSeasonVendor_Staging _existingSourceSeasonLink = session.get(PTC_ModelSeasonVendor_Staging.class, _sourceSeasonLink.getPTC_ModelSeasonVendorKey());
		if(_existingSourceSeasonLink == null) {
			_sourceSeasonLink.setPTC_VendorUniqueID(Long.valueOf(sConfig.getBranchIdentifier()).intValue());
			_sourceSeasonLink.setWorkNumber(workNumber.intValue());
			_sourceSeasonLink.setPTC_SeasonKey(Long.valueOf(season.getBranchIdentifier()).intValue());
			_sourceSeasonLink.setIsPrimaryVendor(sourceToSeasonLink.isPrimarySTSL());
			
			// HTS CODE UPDATE
			String agrHTSCode = (String) sConfig.getValue("agrHTSCode");
			
			if(FormatHelper.hasContent(agrHTSCode) && sourceToSeasonLink.isPrimarySTSL()) {
				PTC_ModelSeason_Staging _existingmodelSeason = session.get(PTC_ModelSeason_Staging.class, _modelSeason.getPTC_ModelSeasonKey());
				if(_existingmodelSeason != null && !FormatHelper.hasContent(_existingmodelSeason.getHTSCode()) ) {
					_existingmodelSeason.setHTSCode(agrHTSCode); // agrHTSCode
					session.saveOrUpdate(_existingmodelSeason);
				}

			}
			
			session.save(_sourceSeasonLink);
			LOGGER.info("PTC_ModelSeasonVendor_Staging - Add source " + sConfig.getName() + " Season " + season.getName());
		}else {
			LOGGER.info("PTC_ModelSeasonVendor_Staging - Skip source " + sConfig.getName() + " Season " + season.getName());	
			}

		return _sourcingConfig;
	}



}
