package com.agron.wc.sourcing;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigLogic;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.sourcing.SourcingConfigHelper;
import com.lcs.wc.specification.FlexSpecHelper;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToSeasonLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FlexObjectUtil;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.method.MethodContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

//Agron FlexPLM 11.1 Upgrade - HTS Code From Business Object set to Source - START
public final class AgronSourcingConfigurationPlugin {

	private static final String BASELINE = "Baseline";

	private static final String NO = "No";

	private static final String AGR_SOURCES_CREATED_ON = "agrSourcesCreatedOn";

	private static final String AGR_CREATE_SOURCE_FLAG = "agrCreateSourceFlag";

	private static final String AGR_PROTO = "agrProto";

	private static final String AGR_DEVELOPMENT_STAGE = "agrDevelopmentStage";

	private static final String AGR_YEAR_REFERENCE = "agrYearReference";

	private static final String AGR_SEASON_REFERENCE = "agrSeasonReference";

	private static final String AGR_COUNTRY_OF_ORIGIN = "agrCountryOfOrigin";

	private static final String PRIMARY_FACTORY_VENDORNUM = LCSProperties
	.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.primaryFactoryVendorNum");
	
	private static final String TTI_GROUP = LCSProperties
	.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.primaryFactory");

	public static final Logger logger = LogManager.getLogger(AgronSourcingConfigurationPlugin.class);

	public static final String AGR_FLEXTYPE_PATH = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.FlexTypePath");

	public static final String AGR_PRODUCT_TYPE = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.ProductType");

	public static final String AGR_HTS_CODE = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.HTSCode");

	public static final String AGR_HTS_CODE_DUTY = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.HTSCodeDuty");
	
	public static final String AGR_FACTORY = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrFactory");
	
	public static final String AGR_FACTORY_PRODUCTION_COUNTRY = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrProductionOfficeCountry");
	
	public static final String AGR_COUNTRY_ORIGIN = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrCountryOfOrigin");
	
	public static final String AGR_AGENT = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrAgent");
	
	public static final String AGR_AGENT_PRODUCTION_COUNTRY = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrProductionOfficeCountry");
	
	public static final String AGR_MMF_SOCKS = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrMMFSOCKS", "agrMMFSOCKS");
	
	public static final String SC_NUMBER = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.number", "number");
	
	public static final String AGR_FACTORY_FLEXTYPE_PATH = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.Factory.FlexTypePath", "Supplier\\Factory");
	
	public static final String AGR_AGENT_FLEXTYPE_PATH = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.Agent.FlexTypePath", "Supplier\\Agent");

	public static final String VENDORGROUP = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.vendorGroup", "vendorGroup");
	
	public static final String VENDORNUMBER = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrVendorNo", "agrVendorNo");

	private static final String COSTSHEET_BASE_FOB_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrBaseFOB", "agrBaseFOB");
	
	private static String SEASONTYPE = "seasonType";

	private static String YEAR = "year";

	public static void getHTSCodeFromLookUpTable(WTObject obj) {
		try {
			LCSSourcingConfig sourcingConfigObj = (LCSSourcingConfig) obj;
			String productType = (String) sourcingConfigObj.getValue(AGR_PRODUCT_TYPE);
			logger.debug("<<<<<<<<<<<<<productType %:" + productType);
			logger.debug("<<<<<<<<<<<<<productType %:" + AGR_PRODUCT_TYPE);
			FlexTypeQuery ftq = new FlexTypeQuery();
			FlexType businessObjectType = FlexTypeCache.getFlexTypeFromPath(AGR_FLEXTYPE_PATH);
			Collection businessObjectColl = ftq.findAllObjectsTypedBy(businessObjectType).getResults();
			String boIdA2A2 = "";
			LCSLifecycleManaged costSheetDutyBObj = null;
			String htsCode = "";

			if (businessObjectColl != null && businessObjectColl.size() > 0) {
				Iterator<FlexObject> businessCollIterator = businessObjectColl.iterator();
				if (businessCollIterator.hasNext()) {
					FlexObject flexObj = businessCollIterator.next();
					boIdA2A2 = flexObj.getString("LCSLIFECYCLEMANAGED.IDA2A2");
					logger.debug("<<<<<<<<Business Object ID:" + boIdA2A2);
					logger.debug("<<<<<<<<Business Object ID:" + boIdA2A2);
				}
				if (FormatHelper.hasContent(boIdA2A2)) {
					costSheetDutyBObj = (LCSLifecycleManaged) LCSQuery
							.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + boIdA2A2);
					Map<Object, Object> inputCriteria = new HashMap<Object, Object>();
					inputCriteria.put(AGR_PRODUCT_TYPE, productType);
					LCSMOATable moaTable = (LCSMOATable) costSheetDutyBObj.getValue(AGR_HTS_CODE_DUTY);
					Collection moaTableColl = moaTable.getRows(inputCriteria, true);
					Iterator<FlexObject> moaCollIter = moaTableColl.iterator();
					logger.debug("<<<<<<<<<<<<<moaTableColl %:" + moaTableColl);
					if (moaCollIter.hasNext()) {
						FlexObject flexObj = moaCollIter.next();
						htsCode = (String) flexObj.get(AGR_HTS_CODE);
						logger.debug("<<<<<<<<<<<<<HTSCode %:" + htsCode);
						logger.debug("<<<<<<<<<<<<<HTSCode %:" + htsCode);
					}
				}
			}
			logger.debug("<<<<<<<<<<<<<SETTING HTS CODE %:" + htsCode);
			sourcingConfigObj.setValue(AGR_HTS_CODE, htsCode);

			/*Default country of origin to Vendor production office country
			 *Link Country of Origin Attribute to Vendor Master*/
			
			if ((FormatHelper.hasContent(String.valueOf(sourcingConfigObj.getValue(AGR_FACTORY))))) {
				logger.debug("::FACTORY>>::" + sourcingConfigObj.getValue(AGR_FACTORY));
				LCSSupplier factoryObj = (LCSSupplier) LCSQuery
						.findObjectById(sourcingConfigObj.getValue(AGR_FACTORY).toString());
				LCSCountry countryObj = (LCSCountry) factoryObj.getValue(AGR_FACTORY_PRODUCTION_COUNTRY);
				countryObj = (LCSCountry) VersionHelper.latestIterationOf(countryObj);
				String countryObjId = (String) FormatHelper.getNumericVersionIdFromObject(countryObj);
				logger.debug("::Factory countryOfOrigin>>::" + countryObjId);
				if ((FormatHelper.hasContent(countryObjId))) {
					sourcingConfigObj.setValue(AGR_COUNTRY_ORIGIN, countryObjId);
				} else {
					sourcingConfigObj.setValue(AGR_COUNTRY_ORIGIN, "");
				}
			} else if ((FormatHelper.hasContent(String.valueOf(sourcingConfigObj.getValue(AGR_AGENT))))) {
				logger.debug("::AGENT>>::" + sourcingConfigObj.getValue(AGR_AGENT));
				LCSSupplier agentObj = (LCSSupplier) LCSQuery
						.findObjectById(sourcingConfigObj.getValue(AGR_AGENT).toString());
				LCSCountry countryObj = (LCSCountry) agentObj.getValue(AGR_AGENT_PRODUCTION_COUNTRY);
				countryObj = (LCSCountry) VersionHelper.latestIterationOf(countryObj);
				String countryObjId = (String) FormatHelper.getNumericVersionIdFromObject(countryObj);
				logger.debug("::countryOfOrigin FACTORY>>::" + countryObjId);
				if ((FormatHelper.hasContent(countryObjId))) {
					sourcingConfigObj.setValue(AGR_COUNTRY_ORIGIN, countryObjId);
				} else {
					sourcingConfigObj.setValue(AGR_COUNTRY_ORIGIN, "");
				}
			} else {
				sourcingConfigObj.setValue(AGR_COUNTRY_ORIGIN, "");
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
	}
	
	public static void createBulkSourcing(WTObject obj) throws WTException, WTPropertyVetoException {
		LCSSourceToSeasonLink stslObj = (LCSSourceToSeasonLink) obj;
		logger.debug("Source to Season Link Obj is >>"+stslObj);
		
		LCSSeasonMaster seasonMaster = stslObj.getSeasonMaster();
		LCSSeason seasonObj = VersionHelper.latestIterationOf(seasonMaster);
		LCSSourcingConfigMaster sourcingconfigmaster = stslObj.getSourcingConfigMaster();
		LCSSourcingConfig sourcingConfigObj = VersionHelper.latestIterationOf(sourcingconfigmaster);
		logger.debug("Sourcing Config Name>>"+sourcingConfigObj.getName());
		logger.debug("Sourcing Config >>"+sourcingConfigObj);
		
		logger.debug("Product A Rev>>"+sourcingConfigObj.getProductARevId());
		logger.debug("Product Master Reference>>"+sourcingConfigObj.getProductMasterReference());
		
		//Add check for sourcing number 
		String sourceNumber = (String) sourcingConfigObj.getValue(SC_NUMBER);
		logger.debug("Sourcing Config Number>>"+sourceNumber);
		String productID = FormatHelper.format(sourcingConfigObj.getProductARevId());
		logger.debug("productID  >>"+productID);
		LCSProduct productObj = SeasonProductLocator.getProductARev(stslObj.getMaster());
		LCSSeasonProductLink spl = SeasonProductLocator.getSeasonProductLink(stslObj.getMaster());
		logger.debug("Product >>"+productObj);
		logger.debug("Product Copied From>>"+productObj.getCopiedFrom());
		
		logger.debug("SeasonProductLink >>"+spl);
		logger.debug("SeasonProductLink Carried Over From>>"+spl.getCarriedOverFrom());
		
		LCSSourcingConfigLogic scLogic = new LCSSourcingConfigLogic();
		
		if(sourceNumber.equalsIgnoreCase("001") &&  spl.getCarriedOverFrom()==null && productObj.getCopiedFrom()==null) {
			
			FlexType specType = productObj.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
			SearchResults agentObjects = getSuupliersForVendorGroup(AGR_AGENT_FLEXTYPE_PATH, TTI_GROUP);
			Collection<FlexObject> agentColl = agentObjects.getResults();
			logger.trace("Agents >>"+agentColl.toString());
			LCSSupplier agentObject = null;
			if(agentColl.size() > 0) {
				FlexObject flexObj = agentColl.iterator().next();
				agentObject = getSupplierObj(flexObj);
			}
			SearchResults factoryObjects = getSuupliersForVendorGroup(AGR_FACTORY_FLEXTYPE_PATH, TTI_GROUP);
			Collection<FlexObject> factoryColl = factoryObjects.getResults();
			logger.trace("Factories >>"+factoryColl.toString());
			FlexObject flexObj = getPrimaryFactory(factoryColl, PRIMARY_FACTORY_VENDORNUM);
			if (!flexObj.isEmpty()) {
				LCSSupplier factoryObject = getSupplierObj(flexObj);
				factoryObject = getSupplierObj(flexObj);
				sourcingConfigObj.setValue(AGR_AGENT, agentObject);
				sourcingConfigObj.setValue(AGR_FACTORY, factoryObject);
				sourcingConfigObj.setValue(AGR_PRODUCT_TYPE, AGR_MMF_SOCKS);
				sourcingConfigObj = (LCSSourcingConfig) scLogic.saveSourcingConfig(sourcingConfigObj, true);
				logger.debug("Sourcing Config >>>"+sourcingConfigObj);
				factoryColl.remove(flexObj);
				
				FlexSpecification flexSpecification = createSpec(seasonMaster, sourcingconfigmaster, productObj.getMaster(), specType, spl);
				Collection<?> costColl = LCSCostSheetQuery.getCostSheetsForSourceToSeason(stslObj, null);
				if(costColl!=null && costColl.size()>0) {
					logger.debug("Costsheets for stsl >>>>"+costColl.toString());
					LCSCostSheet csObject = (LCSCostSheet)costColl.iterator().next();
					csObject.setSpecificationMaster(flexSpecification.getMaster());
					new LCSCostSheetLogic().saveCostSheet(csObject, false, true);
				}
				
			}
			logger.trace("Factories updated >>"+factoryColl);
			LCSSupplier supplierObject = null;
			Iterator<FlexObject> sIter = factoryColl.iterator();
			while(sIter.hasNext()) {
				FlexObject flexObject = sIter.next();
				supplierObject = getSupplierObj(flexObject);
				createSourcingConfig(seasonObj, sourcingConfigObj, supplierObject, agentObject, spl, null, null, specType, null);
			}
			
		}
		
		}

	/**
	 * @param factoryColl
	 * @param vendorNum
	 * @return
	 * @throws WTException
	 */
	private static FlexObject getPrimaryFactory(Collection<FlexObject> factoryColl, String vendorNum) throws WTException {
		FlexType supplierFlexType = FlexTypeCache.getFlexTypeFromPath(AGR_FACTORY_FLEXTYPE_PATH);
		FlexTypeAttribute vendorNumFTA = supplierFlexType.getAttribute(VENDORNUMBER);
		logger.debug("Vendor Number Column is ::"+vendorNumFTA.getSearchResultIndex().toUpperCase());
		FlexObject flexObj  = null;
		Collection<FlexObject> primaryFactory = FlexObjectUtil.filter(factoryColl, vendorNumFTA.getSearchResultIndex().toUpperCase(), 
				vendorNum);
		logger.debug("Primary Factory Collection ::"+primaryFactory);
		if(!primaryFactory.isEmpty()) 
			flexObj = (FlexObject) primaryFactory.iterator().next();
		logger.debug("Primary Factory is ::"+flexObj);
		return flexObj;
	}

	/**
	 * @param flexbj
	 * @return
	 * @throws WTException
	 */
	private static LCSSupplier getSupplierObj(FlexObject flexbj) throws WTException {
		LCSSupplier supplierObject;
		logger.debug("FlexObj ::"+flexbj.toString());
		String branchID = flexbj.getString("LCSSUPPLIER.branchiditerationinfo");
		logger.debug("branchID  :: "+branchID);
		supplierObject = (LCSSupplier) LCSQuery.findObjectById("VR:com.lcs.wc.supplier.LCSSupplier:" + branchID);
		logger.debug("Supplier  :: "+supplierObject);
		logger.debug("Supplier Name :: "+supplierObject.getName());
		return supplierObject;
	}

	/**
	 * @param seasonObj
	 * @param sourcingConfigObj
	 * @param supplierObject 
	 * @param agentObject 
	 * @param specType 
	 * @param spl 
	 * @param destSpec 
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	private static void createSourcingConfig(LCSSeason seasonObj, LCSSourcingConfig sourcingConfigObj, LCSSupplier supplierObject, 
			LCSSupplier agentObject, LCSSeasonProductLink spl, LCSProduct productObj, FlexSpecification sourceSpec, FlexType specType, Collection<?> previousCostSheetColl)
			throws WTPropertyVetoException, WTException {
		logger.debug("Creating Sourcing Config");
		LCSSourcingConfig newSourcingConfigObj = new LCSSourcingConfig();
		FlexSpecLogic specLogic = new FlexSpecLogic();
		LCSSourcingConfigMaster newSourcingconfigmaster = new LCSSourcingConfigMaster();
		
		newSourcingconfigmaster.setProductMasterReference(sourcingConfigObj.getProductMasterReference()); 
		newSourcingconfigmaster.setProductARevId(sourcingConfigObj.getProductARevId());
		newSourcingConfigObj.setMaster(newSourcingconfigmaster);
						
		newSourcingConfigObj.setPrimarySource(false);
		newSourcingConfigObj.setFlexType(sourcingConfigObj.getFlexType());
		newSourcingConfigObj.setProductARevId(sourcingConfigObj.getProductARevId());
		newSourcingConfigObj.setProductMasterReference(sourcingConfigObj.getProductMasterReference());
		newSourcingConfigObj.setValue("name", "SC");
		newSourcingConfigObj.setValue(AGR_AGENT, agentObject);
		newSourcingConfigObj.setValue(AGR_FACTORY, supplierObject);
		newSourcingConfigObj.setValue(AGR_PRODUCT_TYPE, AGR_MMF_SOCKS);
		newSourcingConfigObj.setValue(AGR_COUNTRY_OF_ORIGIN, sourcingConfigObj.getValue(AGR_COUNTRY_OF_ORIGIN));
		newSourcingConfigObj  = SourcingConfigHelper.service.saveSourcingConfig(newSourcingConfigObj);
		logger.debug("Sourcing Config Number>>>"+newSourcingConfigObj.getValue(SC_NUMBER));
		LCSSourcingConfigLogic scLogic = new LCSSourcingConfigLogic();
		LCSSourceToSeasonLink newStslObj = scLogic.createSourceToSeasonLink(newSourcingConfigObj, seasonObj);
		logger.debug("Source to Season Link Obj is >>>"+newStslObj);
		
		FlexSpecification flexSpecification = FlexSpecification.newFlexSpecification();
		if(productObj!=null) {
			logger.debug("Copying Spec >>>"+sourceSpec);
			flexSpecification = specLogic.copySpec(sourceSpec, newSourcingConfigObj, productObj, seasonObj, true);
		}else {
			logger.debug("Creating Spec >>>");
			flexSpecification = createSpec(newStslObj.getSeasonMaster(), newSourcingConfigObj.getMaster(), 
					newSourcingConfigObj.getProductMaster(), specType, spl);
			Collection<?> costColl = LCSCostSheetQuery.getCostSheetsForSourceToSeason(newStslObj, null);
			if(costColl!=null && costColl.size()>0) {
				logger.debug("Costsheets for stsl >>>"+costColl.toString());
				LCSCostSheet csObject = (LCSCostSheet)costColl.iterator().next();
				csObject.setSpecificationMaster(flexSpecification.getMaster());
				new LCSCostSheetLogic().saveCostSheet(csObject, false, true);
				
			}
		}
		
		if(previousCostSheetColl!=null && !previousCostSheetColl.isEmpty()) {
			Iterator previousCostSheetIter = previousCostSheetColl.iterator();
			while (previousCostSheetIter.hasNext()) {
				LCSCostSheet previousCsObject = (LCSCostSheet)previousCostSheetIter.next();
				logger.debug("!!!!!!!!!!!! previousCsObject costsheet name " + previousCsObject.getName());
				logger.debug("!!!!!!!!!!!! previousCsObject.isPrimaryCostSheet " + previousCsObject.isPrimaryCostSheet());
				logger.debug("!!!!!!!!!!!! previousCsObject.getCostSheetType " + previousCsObject.getCostSheetType());
				if(null != newSourcingConfigObj && !"SKU".equals(previousCsObject.getCostSheetType()) && 
						!BASELINE.equals(previousCsObject.getName().trim())) {
					logger.debug("!!!!!!!!!!!! previousCsObject containing primary costsheet : " + previousCsObject);
					LCSCostSheetLogic costSheetLogic = new LCSCostSheetLogic();
					logger.debug("!!!!!!!!!!!! previousCsObject : " + previousCsObject.getName());
					logger.debug("!!!!!!!!!!!! season : " + seasonObj.getName());
					logger.debug("!!!!!!!!!!!! product : " + spl.getProductARevId());
					logger.debug("!!!!!!!!!!!! Specification : " + flexSpecification.getName());
					
					Map<String,String> hm = new HashMap<String,String>();
					hm.put("clipboardCopy", "true");
					LCSCostSheet newCostSheet = costSheetLogic.copyCostSheetEnhanced(newSourcingConfigObj, previousCsObject.getSkuMaster(),
							previousCsObject, seasonObj, false, hm,true);

					logger.debug("!!!!!!!!!!!! Costsheet created and now copying attributes of costsheet !!!!!!!!!!!!");
					newCostSheet.setApplicableSizeCategoryNames(previousCsObject.getApplicableSizeCategoryNames());
					newCostSheet.setApplicableSizes(previousCsObject.getApplicableSizes());
					newCostSheet.setApplicableSizes2(previousCsObject.getApplicableSizes2());
					newCostSheet.setRepresentativeSize(previousCsObject.getRepresentativeSize());
					newCostSheet.setRepresentativeSize2(previousCsObject.getRepresentativeSize2());
					newCostSheet.setSkuMaster(previousCsObject.getSkuMaster());
					newCostSheet.setSpecificationMaster(flexSpecification.getMaster());
					logger.debug("!!!!!!!!!!!!Is Cost sheet Primary !!!"+newCostSheet.isPrimaryCostSheet());
					if(!newCostSheet.isPrimaryCostSheet() || !previousCsObject.isPrimaryCostSheet()) {
						logger.debug("!!!!!!!!!!!! Active costsheet , reset base FOB !!!");
						newCostSheet.setValue(COSTSHEET_BASE_FOB_KEY, 0.0f);
					}
					new LCSCostSheetLogic().saveCostSheet(newCostSheet, true, false);
					logger.debug("!!!!!!!!!!!! Costsheet linked to new specification !!!!!!!!!!!!");
				}
			}
			
			Collection<?> newCostSheetColl = null;
			if(newStslObj != null) {
				newCostSheetColl = LCSCostSheetQuery.getCostSheetsForSourceToSeason(newStslObj, null, true);
				logger.debug("Costsheets >>"+newCostSheetColl.toString());
				Iterator newCostSheetIter = newCostSheetColl.iterator();
				while (newCostSheetIter.hasNext()) {
					LCSCostSheet newCsObject = (LCSCostSheet)newCostSheetIter.next();
					logger.debug("!!!!!!!!!!!! newCsObject costsheet name " + newCsObject.getName());
					logger.debug("!!!!!!!!!!!! newCsObject.isPrimaryCostSheet " + newCsObject.isPrimaryCostSheet());
					if(BASELINE.equals(newCsObject.getName().trim())){
						logger.debug("!!!!!!!!!!!! newCsObject is Baseline !!");
						new LCSCostSheetLogic().deleteCostSheet(newCsObject, true);
						logger.debug("!!!!!!!!!!!! newCsObject deleted !!");
						return;
					}
				}
				
			}
		}
		
	}

	/**
	 * @param flexTypePath 
	 * @throws WTException 
	 * 
	 */
	private static SearchResults getSuupliersForVendorGroup(String flexTypePath, String vendorGroup) throws WTException {
		SearchResults searchresults = null;
		FlexType supplierFlexType = FlexTypeCache.getFlexTypeFromPath(flexTypePath);
		FlexTypeAttribute vendorGroupFTA = supplierFlexType.getAttribute(VENDORGROUP);
		FlexTypeAttribute vendorNumFTA = supplierFlexType.getAttribute(VENDORNUMBER);
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable("LCSSupplier");
		statement.appendFromTable("LCSSupplierMaster");
		statement.appendFromTable("wttypedefinition");
		statement.appendSelectColumn(new QueryColumn("LCSSupplier", "ida2a2"));
		statement.appendSelectColumn(new QueryColumn("LCSSupplier", "branchIditerationInfo"));
		statement.appendSelectColumn(new QueryColumn("LCSSupplierMaster", "suppliername"));
		statement.appendSelectColumn(new QueryColumn("LCSSupplier", vendorGroupFTA.getColumnName()));
		statement.appendSelectColumn(new QueryColumn("LCSSupplier", vendorNumFTA.getColumnName()));
		statement.setDistinct(true);
		statement.appendCriteria(new Criteria(new QueryColumn("LCSSupplier", "idA3masterReference"), 
				new QueryColumn("LCSSupplierMaster", "ida2a2"), Criteria.EQUALS));
		statement.appendAndIfNeeded(); 
		statement.appendCriteria(new Criteria(new QueryColumn("LCSSupplier", "branchida2typedefinitionrefe"), 
				new QueryColumn("wttypedefinition", "branchiditerationinfo"), Criteria.EQUALS));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("LCSSupplier", "latestiterationinfo"), 
				"1", Criteria.EQUALS));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("wttypedefinition", "latestiterationinfo"), 
				"1", Criteria.EQUALS));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("LCSSupplier", "statecheckoutinfo"), 
				"wrk", Criteria.NOT_EQUAL_TO));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("lcssuppliermaster", "suppliername"), 
				"placeholder", Criteria.NOT_EQUAL_TO));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("LCSSupplier", "branchida2typedefinitionrefe"), 
				supplierFlexType.getIdNumber(), Criteria.EQUALS));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("LCSSupplier", vendorGroupFTA.getColumnName()), 
				vendorGroup, Criteria.EQUALS));
		
		logger.debug("Query ::" +statement.toString());
        searchresults = LCSQuery.runDirectQuery(statement);
        logger.debug("Query count ::" +searchresults.getResultsFound());
        return searchresults;
	}	
	
	/**
	 * 
	 * Creates a specification object for Product-Season and SourcingConfiguration
	 * @return 
	 */
	private static FlexSpecification createSpec(LCSSeasonMaster seasonMaster, LCSSourcingConfigMaster configMaster,
			LCSPartMaster prodMaster,FlexType specType, LCSSeasonProductLink spl) {
		FlexSpecification flexSpecification = null ;
		try
		{
			flexSpecification = FlexSpecification.newFlexSpecification();
			logger.debug("!!!!!!!!!!!! createSpec ::");
			LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);
			String seasonType ="";
			String year="";
			logger.debug("!!!!!!!!!!!! season ::"+season.getName());
	
			//logic to get the season type and year to set the name for New specification
			if (season != null) {
				seasonType = (String)season.getValue(SEASONTYPE);
				if (FormatHelper.hasContent(seasonType)) {
					logger.debug("!!!!!!!!!!!! seasonType ::"+seasonType);
					if (seasonType.equalsIgnoreCase("agrFallWinter")) {
						seasonType = "agrF";
					} else if (seasonType.equalsIgnoreCase("agrSpringSummer")) {
						seasonType = "agrS";
					} else if (seasonType.equalsIgnoreCase("agrSpring")) {
						seasonType = "agrSP";
					} else if (seasonType.equalsIgnoreCase("agrSummer")) {
						seasonType = "agrSU";
					} else if (seasonType.equalsIgnoreCase("agrFall")) {
						seasonType = "agrFL";
					} else if (seasonType.equalsIgnoreCase("agrWinter")) {
						seasonType = "agrWI";
					}
				}
				year = (String)season.getValue(YEAR);
				if (FormatHelper.hasContent(year)) {
					year = year.replace("agr20", "agr");
				}
			}
			logger.debug("!!!!!!!!!!!! seasonType " + seasonType + " year " + year);
			
			flexSpecification.setFlexType(specType);
			flexSpecification.setMaster(new FlexSpecMaster());
			flexSpecification.setSpecOwner(prodMaster);
			flexSpecification.setSpecSource(configMaster);
			flexSpecification.setValue(AGR_SEASON_REFERENCE,seasonType);
			flexSpecification.setValue(AGR_YEAR_REFERENCE,year);
			flexSpecification.setValue(AGR_DEVELOPMENT_STAGE,AGR_PROTO);

			flexSpecification = FlexSpecHelper.service.saveSpec(flexSpecification);
			logger.debug("!!!!!!!!!!!! Spec created successfully");
			FlexSpecLogic specLogic = new FlexSpecLogic();

			FlexSpecToSeasonLink specLink = specLogic.addSpecToSeason((FlexSpecMaster)flexSpecification.getMaster(), seasonMaster);
			logger.debug("!!!!!!!!!!!! flexSpecification "+flexSpecification.getName());
			
		}
		catch (WTException wte) {
			logger.error("WTException :"+wte.getLocalizedMessage());
			wte.printStackTrace();
		}
		catch (WTPropertyVetoException wtpv) {
			logger.error("WTPropertyVetoException :"+wtpv.getLocalizedMessage());
			wtpv.printStackTrace();
		}
		return flexSpecification; 
	}
	
	public static void copySourceInfo(WTObject obj) throws WTException, WTPropertyVetoException  {
		LCSSourceToSeasonLink stslObj = (LCSSourceToSeasonLink) obj;
		logger.debug("copySourceInfo - Source to Season Link Obj is >>>"+stslObj);
		
		if(stslObj.getValue(AGR_CREATE_SOURCE_FLAG)!=null ) {
			String flagVal = (String) stslObj.getValue(AGR_CREATE_SOURCE_FLAG);

			if(flagVal.equalsIgnoreCase("Yes") && stslObj.getValue(AGR_SOURCES_CREATED_ON)==null ) {
				logger.debug("Source to Season Links to be created is >>>");
				boolean noSourcesExist = false;
				LCSSeasonMaster seasonMaster = stslObj.getSeasonMaster();
				LCSSeason seasonObj = VersionHelper.latestIterationOf(seasonMaster);
				LCSSourcingConfigMaster sourcingconfigmaster = stslObj.getSourcingConfigMaster();
				LCSSourcingConfig sourcingConfigObj = VersionHelper.latestIterationOf(sourcingconfigmaster);
				logger.debug("Sourcing Config Name>>"+sourcingConfigObj.getName());
				logger.debug("Sourcing Config >>"+sourcingConfigObj);
				
				logger.debug("Product A Rev>>"+sourcingConfigObj.getProductARevId());
				logger.debug("Product Master Reference>>"+sourcingConfigObj.getProductMasterReference());
				
				//Add check for sourcing number 
				String sourceNumber = (String) sourcingConfigObj.getValue(SC_NUMBER);
				logger.debug("Sourcing Config Number>>"+sourceNumber);
				String productID = FormatHelper.format(sourcingConfigObj.getProductARevId());
				logger.debug("productID  >>"+productID);
				LCSProduct productObj = SeasonProductLocator.getProductARev(stslObj.getMaster());
				LCSSeasonProductLink spl = SeasonProductLocator.getSeasonProductLink(stslObj.getMaster());
				logger.debug("Product >>"+productObj);
				logger.debug("Product Copied From>>"+productObj.getCopiedFrom());
				
				logger.debug("SeasonProductLink >>"+spl);
				logger.debug("SeasonProductLink Carried Over From>>"+spl.getCarriedOverFrom());
				
				LCSSourceToSeasonLink carryStslObj = null;
				if(stslObj.getCarriedOverFrom()!=null) {
					carryStslObj = (LCSSourceToSeasonLink) VersionHelper.latestIterationOf(stslObj.getCarriedOverFrom());
					if(carryStslObj.getValue(AGR_SOURCES_CREATED_ON) == null)
						noSourcesExist = true;
				}
				
				
				LCSSeason sourceSeason= null;
				if(spl.getCarriedOverFrom()!=null)
				sourceSeason= (LCSSeason) VersionHelper.latestIterationOf(spl.getCarriedOverFrom().getSeasonMaster());
				
				LCSSourcingConfigLogic scLogic = new LCSSourcingConfigLogic();
				String vendorGroupStr = "" ;
				FlexType specType = productObj.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
				if ((FormatHelper.hasContent(String.valueOf(sourcingConfigObj.getValue(AGR_AGENT))))) {
					logger.debug("::AGENT>>::" + sourcingConfigObj.getValue(AGR_AGENT));
					LCSSupplier agentObj = (LCSSupplier) LCSQuery
							.findObjectById(sourcingConfigObj.getValue(AGR_AGENT).toString());
					vendorGroupStr = (String) agentObj.getValue(VENDORGROUP);
				}
				
				try {
				if(sourceNumber.equalsIgnoreCase("001") && vendorGroupStr.equalsIgnoreCase(TTI_GROUP) 
						&& (noSourcesExist || spl.getCarriedOverFrom() == null )) {
				
							
					FlexSpecification sourceSpec = new FlexSpecification();
					Collection<?> existingSpecs = LCSQuery.getObjectsFromResults(FlexSpecQuery.findSpecsByOwner(productObj.getMaster(), 
							seasonMaster, sourcingConfigObj.getMaster(), specType), 
							"VR:com.lcs.wc.specification.FlexSpecification:", "FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
					if(existingSpecs != null && existingSpecs.size() > 0){
						logger.debug(" Spec(s) already exists for Source on the season >>"+existingSpecs.toString() ); 
						sourceSpec = (FlexSpecification)existingSpecs.iterator().next();
						logger.debug(" Spec >>"+sourceSpec.getName() );
						
					}
					
					
					Collection<?> previousCostSheetColl = null;
					if(stslObj != null && stslObj.isPrimarySTSL()) {
						previousCostSheetColl = LCSCostSheetQuery.getCostSheetsForSourceToSeason(stslObj, null);
						logger.debug("Costsheets >>"+previousCostSheetColl.toString());
					}
					
					SearchResults agentObjects = getSuupliersForVendorGroup(AGR_AGENT_FLEXTYPE_PATH, TTI_GROUP);
					Collection<FlexObject> agentColl = agentObjects.getResults();
					logger.trace("Agents >>"+agentColl.toString());
					LCSSupplier agentObject = null;
					if(agentColl.size() > 0) {
						FlexObject flexObj = agentColl.iterator().next();
						agentObject = getSupplierObj(flexObj);
					}
					SearchResults factoryObjects = getSuupliersForVendorGroup(AGR_FACTORY_FLEXTYPE_PATH, TTI_GROUP);
					Collection<FlexObject> factoryColl = factoryObjects.getResults();
					logger.trace("Factories >>"+factoryColl.toString());
					FlexObject flexObj = getPrimaryFactory(factoryColl, PRIMARY_FACTORY_VENDORNUM);
					if (!flexObj.isEmpty()) {
						LCSSupplier factoryObject = getSupplierObj(flexObj);
						factoryObject = getSupplierObj(flexObj);
						sourcingConfigObj.setValue(AGR_FACTORY, factoryObject);
						sourcingConfigObj = (LCSSourcingConfig) scLogic.saveSourcingConfig(sourcingConfigObj, true);
						logger.debug("Sourcing Config >>>"+sourcingConfigObj);
						factoryColl.remove(flexObj);
						
						
					}
					logger.trace("Factories updated >>"+factoryColl);
					LCSSupplier supplierObject = null;
					Iterator<FlexObject> sIter = factoryColl.iterator();
					while(sIter.hasNext()) {
						FlexObject flexObject = sIter.next();
						supplierObject = getSupplierObj(flexObject);
						createSourcingConfig(seasonObj, sourcingConfigObj, supplierObject, agentObject, spl, 
								productObj, sourceSpec, specType, previousCostSheetColl);
					}
					logger.debug("Updating Source to season");
					
					
					  stslObj.setValue(AGR_CREATE_SOURCE_FLAG, NO);
					  stslObj.setValue(AGR_SOURCES_CREATED_ON, new Date());
					  
					  PersistenceServerHelper.manager.update(stslObj);
					  
					 
				}else if (sourceSeason!=null) {
					logger.debug("Source Season Carried Over From  >>"+sourceSeason.getName());
					logger.debug("Source Carried Over From  >>"+carryStslObj);
					logger.debug("Source Carried over from flag >>"+carryStslObj.getValue(AGR_CREATE_SOURCE_FLAG));
					logger.debug("Source Carried over from date>>"+carryStslObj.getValue(AGR_SOURCES_CREATED_ON));
					
					SearchResults results = LCSSourcingConfigQuery.getSourcingConfigDataForProductSeason(productObj.getMaster(),
							sourceSeason.getMaster(), false);
					
					Collection<?> previousCostSheetColl = null;
					if(stslObj != null && stslObj.isPrimarySTSL()) {
						previousCostSheetColl = LCSCostSheetQuery.getCostSheetsForSourceToSeason(stslObj, null);
						logger.debug("Costsheets >>"+previousCostSheetColl.toString());
					}
					logger.debug("results >>"+results.getResults());
					Iterator scIter = results.getResults().iterator();
					while(scIter.hasNext()) {
						FlexObject fObj = (FlexObject) scIter.next();
						if(fObj.getString("LCSSOURCINGCONFIGMASTER.PRIMARYSOURCE").equalsIgnoreCase("1"))
							continue;
						String scBranchID = fObj.getString("LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO");
						LCSSourcingConfig scObj = (LCSSourcingConfig) LCSQuery.findObjectById
								("VR:com.lcs.wc.sourcing.LCSSourcingConfig:"+scBranchID);
						logger.debug("scObj >>"+scObj.getName());
						copySourcingConfig(scObj, productObj, sourceSeason, seasonObj, spl, specType, previousCostSheetColl );
					}
					logger.debug("Updating Source to season");
			
					  stslObj.setValue(AGR_CREATE_SOURCE_FLAG, NO);
					  stslObj.setValue(AGR_SOURCES_CREATED_ON, new Date());
					  
					  PersistenceServerHelper.manager.update(stslObj);
					 
				}
			}catch (WTException we) {
				logger.error("Copy Source WTException we >>"+we.getLocalizedMessage());
				we.printStackTrace();
			} catch (WTPropertyVetoException wpe) {
				logger.error("Copy Source WTPropertyVetoException we >>"+wpe.getLocalizedMessage());
				wpe.printStackTrace();
			}
			}
		}
	}

	
	private static void copySourcingConfig(LCSSourcingConfig sourcingConfigObj, LCSProduct productObj,
			LCSSeason sourceSeason, LCSSeason seasonObj, LCSSeasonProductLink spl, FlexType specType, 
			Collection<?> previousCostSheetColl) {
		logger.debug("copySourcingConfig >>");
		LCSSourcingConfigLogic scLogic = new LCSSourcingConfigLogic();
		FlexSpecLogic specLogic = new FlexSpecLogic();
		Map<String,String> hm = new HashMap<String,String>();
		hm.put("clipboardCopy", "true");
		try {

			FlexSpecification sourceSpec = new FlexSpecification();
			Collection<?> existingSpecs = LCSQuery.getObjectsFromResults(FlexSpecQuery.findSpecsByOwner(productObj.getMaster(), 
					sourceSeason.getMaster(), sourcingConfigObj.getMaster(), specType), 
					"VR:com.lcs.wc.specification.FlexSpecification:", "FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
			if(existingSpecs != null && existingSpecs.size() > 0){
				logger.debug(" Spec(s) already exists for Source on the season >>"+existingSpecs.toString() ); 
				sourceSpec = (FlexSpecification)existingSpecs.iterator().next();
				logger.debug(" Spec >>"+sourceSpec.getName() );
				
			}
			
			LCSSourceToSeasonLink newStslObj = scLogic.createSourceToSeasonLink(sourcingConfigObj, seasonObj);
			logger.debug("Source to Season Link Obj is >>>"+newStslObj);
			
			
			logger.debug("adding Spec to season >>>"+sourceSpec.getName());
			FlexSpecToSeasonLink fstsl = specLogic.addSpecToSeason(sourceSpec.getMaster(), seasonObj.getMaster());
			logger.debug("added Spec to season >>>"+fstsl);
			
			
			if(!previousCostSheetColl.isEmpty()) {
				Iterator previousCostSheetIter = previousCostSheetColl.iterator();
				while (previousCostSheetIter.hasNext()) {
					LCSCostSheet previousCsObject = (LCSCostSheet)previousCostSheetIter.next();
					logger.debug("!!!!!!!!!!!! previousCsObject costsheet name " + previousCsObject.getName());
					logger.debug("!!!!!!!!!!!! previousCsObject.isPrimaryCostSheet " + previousCsObject.isPrimaryCostSheet());
					logger.debug("!!!!!!!!!!!! previousCsObject.getCostSheetType " + previousCsObject.getCostSheetType());
					if(null != sourcingConfigObj && !"SKU".equals(previousCsObject.getCostSheetType()) && 
							!BASELINE.equals(previousCsObject.getName().trim())) {
						logger.debug("!!!!!!!!!!!! previousCsObject containing primary costsheet : " + previousCsObject);
						LCSCostSheetLogic costSheetLogic = new LCSCostSheetLogic();
						logger.debug("!!!!!!!!!!!! previousCsObject : " + previousCsObject.getName());
						logger.debug("!!!!!!!!!!!! season : " + seasonObj.getName());
						logger.debug("!!!!!!!!!!!! product : " + spl.getProductARevId());
						logger.debug("!!!!!!!!!!!! Specification : " + sourceSpec.getName());
						
						LCSCostSheet newCostSheet = costSheetLogic.copyCostSheetEnhanced(sourcingConfigObj, previousCsObject.getSkuMaster(),
								previousCsObject, seasonObj, false, hm,true);

						logger.debug("!!!!!!!!!!!! Costsheet created and now copying attributes of costsheet !!!!!!!!!!!!");
						newCostSheet.setApplicableSizeCategoryNames(previousCsObject.getApplicableSizeCategoryNames());
						newCostSheet.setApplicableSizes(previousCsObject.getApplicableSizes());
						newCostSheet.setApplicableSizes2(previousCsObject.getApplicableSizes2());
						newCostSheet.setRepresentativeSize(previousCsObject.getRepresentativeSize());
						newCostSheet.setRepresentativeSize2(previousCsObject.getRepresentativeSize2());
						newCostSheet.setSkuMaster(previousCsObject.getSkuMaster());
						newCostSheet.setSpecificationMaster(sourceSpec.getMaster());
						logger.debug("!!!!!!!!!!!!Is Cost sheet Primary !!!"+newCostSheet.isPrimaryCostSheet());
						if(!newCostSheet.isPrimaryCostSheet() || !previousCsObject.isPrimaryCostSheet()) {
							logger.debug("!!!!!!!!!!!! Active costsheet , reset base FOB !!!");
							newCostSheet.setValue(COSTSHEET_BASE_FOB_KEY, 0.0f);
						}
						new LCSCostSheetLogic().saveCostSheet(newCostSheet, true, false);
						logger.debug("!!!!!!!!!!!! Costsheet linked to new specification !!!!!!!!!!!!");
					}
				}
			}
			
			Collection<?> newCostSheetColl = null;
			if(newStslObj != null) {
				newCostSheetColl = LCSCostSheetQuery.getCostSheetsForSourceToSeason(newStslObj, null, true);
				logger.debug("Costsheets >>"+newCostSheetColl.toString());
				Iterator newCostSheetIter = newCostSheetColl.iterator();
				while (newCostSheetIter.hasNext()) {
					LCSCostSheet newCsObject = (LCSCostSheet)newCostSheetIter.next();
					logger.debug("!!!!!!!!!!!! newCsObject costsheet name " + newCsObject.getName());
					logger.debug("!!!!!!!!!!!! newCsObject.isPrimaryCostSheet " + newCsObject.isPrimaryCostSheet());
					if(BASELINE.equals(newCsObject.getName().trim())){
						logger.debug("!!!!!!!!!!!! newCsObject is Baseline !!");
						new LCSCostSheetLogic().deleteCostSheet(newCsObject, true);
						logger.debug("!!!!!!!!!!!! newCsObject deleted !!");
						return;
					}
				}
				
			}
		} catch (WTException we) {
			logger.error("WTException we >>"+we.getLocalizedMessage());
			we.printStackTrace();
		} catch (WTPropertyVetoException wpe) {
			logger.error("WTPropertyVetoException we >>"+wpe.getLocalizedMessage());
			wpe.printStackTrace();
		}
	}
	
	
	
	
}
//Agron FlexPLM 11.1 Upgrade - HTS Code From Business Object set to Source - END