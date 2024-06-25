package com.agron.wc.sourcing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSSKUCostSheet;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;


/**
 * @author Sandeep Kureel
 * Class moved for FLexPLM upgrade to 11.1. Fox release 
 * April 2020
 */

public class AgronCostsheetPlugin {



	// PROPERTIES FOR AgencyFee CALCULATIONS
	public static final Logger LOGGER = LogManager.getLogger(AgronCostsheetPlugin.class);
	public static final String AGR_AGENCYFEE_FLEXTYPE_PATH = LCSProperties.get("com.agron.wc.sourcing.AgronAgencyFeePlugin.FlexTypePath");
	public static final String AGR_AGENT = LCSProperties.get("com.agron.wc.sourcing.AgronAgencyFeePlugin.Agent");
	public static final String AGR_AGENCY_FEE = LCSProperties.get("com.agron.wc.sourcing.AgronAgencyFeePlugin.AgencyFee");
	public static final String AGR_AGENCY_FEE_PERCENT = LCSProperties.get("com.agron.wc.sourcing.AgronAgencyFeePlugin.AgencyFeePercent");
	public static final String AGR_AGENCY_FEE_DOLLAR = LCSProperties.get("com.agron.wc.sourcing.AgronAgencyFeePlugin.AgencyFeeDollar", "agrAgencyFeeDollar");

	// PROPERTIES FOR DUTY CALCULATIONS
	public static final String AGR_FLEXTYPE_DUTY_PATH = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.FlexTypePath");
	public static final String AGR_PRODUCT_LINE = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.productLine");
	public static final String AGR_PRODUCT_LINE_DUTY = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.productLineDuty");
	public static final String AGR_OVERRIDE = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.overrideWithHTSCodeDuty");
	public static final String AGR_PRODUCT_TYPE = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.productType");
	public static final String AGR_COUNTRY_OF_ORIGIN = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.countryOfOrigin");
	public static final String AGR_NAME = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.name");
	public static final String AGR_HTS_CODE_DUTY = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.HTSCodeDuty");
	public static final String AGR_DUTY_PERCENT = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.dutyPercent");
	public static final String AGR_DEFAULT_COUNTRY_NAME = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.defaultCountryName");
	public static final String AGR_OVERRIDE_DUTY_PERCENT = LCSProperties.get("com.agron.wc.sourcing.AgronCostSheetDutyPlugin.OverrideDutyPercent");
	public static final String AGR_HIDDEN_OVERRIDE_FLOAT = LCSProperties.get("com.agron.wc.sourcing.CostSheet.agrHiddenHTSOverrideFloat.attKey");

	//changes added for subroyalty and error message.
	public static void performCostingCalculations(WTObject obj) throws Exception {
		try {

			LOGGER.info("<<<<<<<<<<<< performCostingCalculations >>>>>>>>>>>" );
			if(obj == null) {
				return;
			}
			if (obj instanceof LCSSKUCostSheet) {
				LOGGER.info("<<<<<<<<<<<< This is a SKU Costsheet hence returning >>>>>>>>>>>" );
				return; 
			}
			LCSCostSheet costSheetObj = (LCSCostSheet) obj;			
			LCSSourcingConfigMaster sourcingConfigMaster = costSheetObj.getSourcingConfigMaster();
			if (sourcingConfigMaster == null) {
				return;
			}	
			LCSSourcingConfig sourcingConfigObj = (LCSSourcingConfig) VersionHelper.latestIterationOf(sourcingConfigMaster);
			LCSLifecycleManaged costSheetDutyBObj = getCostsheetDutyBO();		
			LCSLifecycleManaged costSheetAgencyFeeBObj = getAgencyFeeBO();

			setCostSheetAgencyFee(costSheetObj, costSheetAgencyFeeBObj, sourcingConfigObj);
			setCostSheetValues(costSheetObj, costSheetDutyBObj, sourcingConfigObj);

		} catch (WTException e) {
			e.printStackTrace();
			throw e;
		}
	}





	public static void setCostSheetAgencyFee(LCSCostSheet costSheetObj, LCSLifecycleManaged costSheetDutyBObj,LCSSourcingConfig sourcingConfigObj) throws WTException, WTPropertyVetoException {
		try {


			Map inputCriteria = new HashMap();

			LCSSupplier supplierObj = (LCSSupplier) sourcingConfigObj.getValue(AGR_AGENT);
			if (supplierObj == null) {
				return;
			}
			String agentObjId = FormatHelper.getNumericVersionIdFromObject(supplierObj);

			LOGGER.debug("<<<<<<<<<<<<agentObjId>>>>>>>>>>>" + agentObjId);
			Long agentOIdDouble = Long.valueOf(Long.parseLong(agentObjId));	
			inputCriteria.put(AGR_AGENT, agentOIdDouble);
			LCSMOATable moaTable = (LCSMOATable) costSheetDutyBObj.getValue(AGR_AGENCY_FEE);
			Collection moaTableColl = moaTable.getRows(inputCriteria, true);
			LOGGER.debug("<<<<<<<<moaTableColl result size>>>>>>" + moaTableColl.size());
			Double agentFeePercent = Double.valueOf(0);
			Double agentFeeDollar = Double.valueOf(0);
			if (moaTableColl.size() == 0 ) {
				//costSheetObj.setValue(AGR_AGENCY_FEE_PERCENT, agentFeePercent);  User should not be able to override agency fee

			} else {
				Iterator moaCollIter = moaTableColl.iterator();
				if (moaCollIter.hasNext() ) {  // What if there are multiple agents with the same name in the look up table ?
					FlexObject flexObj = (FlexObject) moaCollIter.next();
					agentFeePercent = Double.valueOf(flexObj.getFloat(AGR_AGENCY_FEE_PERCENT));
					costSheetObj.setValue(AGR_AGENCY_FEE_PERCENT, agentFeePercent);
					LOGGER.info("Setting Agency Fee %" + agentFeePercent);

				}
			}

			Double agrBaseFOB = (Double) costSheetObj.getValue("agrBaseFOB");
			agentFeeDollar = agrBaseFOB * agentFeePercent / 100;
			LOGGER.info("Base FOB -- " + agrBaseFOB);
			LOGGER.info("Setting Agency Fee  -- " + agentFeeDollar);
			costSheetObj.setValue(AGR_AGENCY_FEE_DOLLAR, agentFeeDollar);


		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw e;
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
			throw e;
		} catch (WTException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/** This method is called from the plugin to set the duty for the costsheet
	 * @param obj
	 * @throws Exception 
	 */
	public static void setCostSheetValues(LCSCostSheet costSheetObj, LCSLifecycleManaged costSheetDutyBObj,LCSSourcingConfig sourcingConfigObj) throws Exception
	{
		try
		{        
			LOGGER.debug("<<<<<<<<<<<<<<< setCostSheetValues>>>>>>>>>>>>>");
			LCSProduct productObj = null;
			LCSSeason season=null;
			String productLine = "";
			boolean isSock = false;

			if (costSheetObj != null && costSheetObj.getProductMaster() != null)
			{
				WTPartMaster prodMaster = costSheetObj.getProductMaster();            
				productObj = (LCSProduct) VersionHelper.getVersion(prodMaster, "A"); 
				productObj =  (LCSProduct) VersionHelper.latestIterationOf(productObj);
				LCSSeasonMaster seasonMaster = costSheetObj.getSeasonMaster();
				LOGGER.info("seasonMaster>>>>"+seasonMaster);
				if(seasonMaster !=null) {
					season = VersionHelper.latestIterationOf(seasonMaster);

				} 
				if(productObj != null && "Adidas\\Underwear".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
					productLine = (String)productObj.getValue("agrProductLine");  // Product Line is changed for underwear
				}else if(productObj != null && !"Adidas\\Underwear".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
					productLine = (String)productObj.getValue(AGR_PRODUCT_LINE);
				}
				costSheetObj.setValue(AGR_PRODUCT_LINE, productLine);

			}

			if(productObj == null ) {
				return;
			}
			LOGGER.debug("<<<<<<<<<<<<<<<Entered Code for Product Line Duty>>>>>>>>>>>>>");
			LOGGER.debug("<<<<<<<<<<<<<<<productObj === >>>>>>>>>>>>>" + productObj.getName());
			LOGGER.debug("<<<<<<<<<<<<<<<FlexType for Product -- " + productObj.getFlexType().getFullNameDisplay(false));

			Map inputCriteriaMap = new HashMap();

			if("Adidas\\Socks Type\\Socks".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) || "Adidas\\Socks Type\\Team".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {
				LOGGER.debug("<<<<<<<<<<<<<<<Pack Count >>>>>>>>>>>>>" + (String)productObj.getValue("agrPackCountProd"));
				LOGGER.debug("<<<<<<<<<<<<<<< Packaging Type >>>>>>>>>>>>>" + (String)productObj.getValue("agrPackageType"));
				isSock = true;
				inputCriteriaMap.put("agrPackCount", (String)productObj.getValue("agrPackCountProd")); 
				inputCriteriaMap.put("agrPackagingType", (String)productObj.getValue("agrPackageType")); 	
			}
			LOGGER.debug("<<<<<<<<<<<<<<< Product Line >>>>>>>>>>>>> " + productLine);
			if(FormatHelper.hasContent(productLine)) {
				inputCriteriaMap.put(AGR_PRODUCT_LINE, productLine);
				setValueAccordingToCriteria(costSheetDutyBObj, costSheetObj, inputCriteriaMap, AGR_PRODUCT_LINE_DUTY,isSock);      
			}
			// Set costsheet values
			// Set customer discount and allowance
			setCustomerDiscountandAllowance(costSheetDutyBObj, costSheetObj,productObj);

			LOGGER.debug("<<<<<<<<<<<<<<<<Entered Code for HTS Code Duty>>>>>>>>>>>>");
			if (costSheetObj.getSourcingConfigMaster() != null)
			{


				String productType = (String)sourcingConfigObj.getValue(AGR_PRODUCT_TYPE);  
				LOGGER.debug(">>>>>>>>>>>PRODUCT TYPE ON SOURCE   >>>>>>>>>>>>>>>>>" + productType);

				LCSCountry countryOfOrigin = (LCSCountry)sourcingConfigObj.getValue(AGR_COUNTRY_OF_ORIGIN);

				String countryName = "";
				if (countryOfOrigin != null) {
					countryName = (String)countryOfOrigin.getValue(AGR_NAME);
				}
				LOGGER.debug("<<<<<<<<<<<<Country Name from Sourcing:" + countryName);

				Map map = new HashMap();
				map.put(AGR_PRODUCT_TYPE, productType);

				LOGGER.debug(">>>>>>>>>>>PRODUCT TYPE ON SOURCE   >>>>>>>>>>>>>>>>>" + productType);

				setDutyTariffFromHTSCodeAndCountry(costSheetDutyBObj, costSheetObj, map, AGR_HTS_CODE_DUTY, countryName);
			}



			Double  agrAgencyFeeDollar  = ( (Double)costSheetObj.getValue("agrAgencyFeeDollar") != null ? (Double)costSheetObj.getValue("agrAgencyFeeDollar") :0.00);
			Double agrBaseFOB = ( (Double)costSheetObj.getValue("agrBaseFOB") != null ? (Double)costSheetObj.getValue("agrBaseFOB") :0.00);
			try {
				// Set costsheet effective FOB
				costSheetObj.setValue("fobPrice", agrAgencyFeeDollar+agrBaseFOB);
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}

			// Set Sub Royalty 
			if(season !=null) {
				setSubRoyaltyValues(costSheetObj, costSheetDutyBObj,season);	
			}
			//agrCapsuleSubRoyalty
			//agrCapsuleSubRoyalty
			//agrCapsule

		}
		catch (WTException e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	/** Set Customer allowance and discount 
	 * @param costSheetDutyBObj
	 * @param costSheetObj
	 */
	private static void setCustomerDiscountandAllowance(LCSLifecycleManaged costSheetDutyBObj, LCSCostSheet costSheetObj,LCSProduct productObj) throws Exception {


		try {
			LOGGER.info("Costsheet Plugin -  setCustomerDiscountandAllowance ");

			Float agrAllowancePercent = Float.valueOf(0);
			Float agrDiscountPercent = Float.valueOf(0);
			String productGender = (String)productObj.getValue("agrGenderProd");
			Map<String, String> map = new HashMap();

			// Gender is only Applicable for Socks and Headwear
			LOGGER.debug("Product Gender - " + productGender);

			if("Adidas\\Socks Type\\Socks".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {	  
				map.put("agrGender", (String)productObj.getValue("agrGenderProd")); 
				map.put("agrCategory", "SOCK");     		
				LOGGER.debug("This category is SOCK");
			}else if("Adidas\\Socks Type\\Team".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {
				//map.put("agrGender", (String)productObj.getValue("agrGenderProd")); 
				map.put("agrCategory", "TEAMSOCK"); 
				LOGGER.debug("This category is TEAMSOCK");
			} else if("Adidas\\Backpacks".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
				map.put("agrCategory", "BACKPACK");  
				LOGGER.debug("This category is BACKPACK");
			} else if("Adidas\\Bags".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){

				String agrSilhouetteProd = (String) productObj.getValue("agrSilhouetteProd");
				if(agrSilhouetteProd !=null && "agrBackPack".contentEquals(agrSilhouetteProd)) {
					map.put("agrCategory", "BACKPACK");
					LOGGER.debug("This category is BAG's - > BACKPACK");
				}else {
					map.put("agrCategory", "BAG"); 
					LOGGER.debug("This category is BAG");
				}
			}else if("Adidas\\Headwear\\Hats".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
				map.put("agrGender", (String)productObj.getValue("agrGenderProd")); 
				map.put("agrCategory", "HEADWEAR");  
				LOGGER.debug("This category is HEADWEAR");
			} else if("Adidas\\Headwear\\Knits".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
				map.put("agrGender", (String)productObj.getValue("agrGenderProd")); 
				map.put("agrCategory", "HEADWEAR");  
				LOGGER.debug("This category is HEADWEAR");
			} else if("Adidas\\Underwear".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
				map.put("agrGender", (String)productObj.getValue("agrGenderProd"));  	
				map.put("agrCategory", "UNDERWEAR"); 
				LOGGER.debug("This category is UNDERWEAR");
			} else if("Adidas\\Sport Accessories".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
				map.put("agrCategory", "SPORTSACC"); 
				LOGGER.debug("This category is SPORTSACC");
			}
			LOGGER.info("<<<<<<<<<<<<<<< map - " + map);
			if(map.size() == 0) {

				return; // By Mistake a user tries to update an SMU and Clearance season.    		
			}

			LCSMOATable customerDiscountMOATable = (LCSMOATable)costSheetDutyBObj.getValue("agrCustomerDiscount");
			LOGGER.debug("<<<<<<<<<<<<< customerDiscountMOATable.getRows():" + customerDiscountMOATable.getRows());   
			Collection agrCustomerDiscountTableColl = customerDiscountMOATable.getRows(map, "agrCustomer",true);
			//map.put("agrCollection",(String) productObj.getValue("agrCollectionProduct"));
			LOGGER.debug("<<<<<<<<<<<<< Filtered Customer Discount table " + agrCustomerDiscountTableColl.toString());

			Collection filteredValuesCollection = customerDiscountMOATable.lookupValuesByAttribute(map,"agrCollection");
			LOGGER.debug("<<<<<<<<<<<<< filteredValuesCollection" + filteredValuesCollection.toString());
			String prodCollection =  (String) productObj.getValue("agrCollectionProduct");
			LOGGER.debug(" >>>>>>>> prodCollection --- " + prodCollection );
			Iterator moaCollIter = agrCustomerDiscountTableColl.iterator();
			boolean duplicateCustomerRecordDSG = false;
			boolean duplicateCustomerRecordADIDAS = false;
			boolean duplicateCustomerRecordFOOTLOCKER = false;
			boolean duplicateCustomerRecordKOHLS = false;
			boolean duplicateCustomerRecordMARMAXX = false;
			boolean duplicateCustomerRecordAVERAGE = false;


			while (moaCollIter.hasNext())
			{
				FlexObject flexObj = (FlexObject)moaCollIter.next();
				String customer  = 	flexObj.getString("agrCustomer");

				LOGGER.info("Setting Discount and Allowance for customer -  " + customer);

				if("DSG".equalsIgnoreCase(customer)) {

					if(FormatHelper.hasContent(prodCollection) && filteredValuesCollection.contains(prodCollection)) {
						if(prodCollection.equals(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent+ "For Collection " + prodCollection); 
							costSheetObj.setValue("agrCustomerAllowanceDSG", agrAllowancePercent);
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent+ "For Collection " + prodCollection); 
							costSheetObj.setValue("agrDiscountPerWC", agrDiscountPercent);
							if(!duplicateCustomerRecordDSG) {
								duplicateCustomerRecordDSG = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}
						}
					}else {
						if(!FormatHelper.hasContent(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent);
							costSheetObj.setValue("agrCustomerAllowanceDSG", agrAllowancePercent);
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent);
							costSheetObj.setValue("agrDiscountPerWC", agrDiscountPercent);
							if(!duplicateCustomerRecordDSG) {
								duplicateCustomerRecordDSG = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}
						}

					}
				}

				if("ADIDAS".equalsIgnoreCase(customer)) {

					if(FormatHelper.hasContent(prodCollection) && filteredValuesCollection.contains(prodCollection)) {

						if(prodCollection.equals(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent+ "For Collection " + prodCollection); 
							costSheetObj.setValue("agrCustomerAllowanceAdidas", agrAllowancePercent); 
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent + "For Collection " + prodCollection); 
							costSheetObj.setValue("agrDiscountPerAdidas", agrDiscountPercent);
							if(!duplicateCustomerRecordADIDAS) {
								duplicateCustomerRecordADIDAS = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}
						}
					}else {
						if(!FormatHelper.hasContent(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent);
							costSheetObj.setValue("agrCustomerAllowanceAdidas", agrAllowancePercent); 
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent);
							costSheetObj.setValue("agrDiscountPerAdidas", agrDiscountPercent);
							if(!duplicateCustomerRecordADIDAS) {
								duplicateCustomerRecordADIDAS = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}

						}
					}

				}

				if("KOHLS".equalsIgnoreCase(customer)) {

					if(FormatHelper.hasContent(prodCollection) && filteredValuesCollection.contains(prodCollection)) {

						if(prodCollection.equals(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent + "For Collection " + prodCollection); 
							costSheetObj.setValue("agrCustomerAllowanceKOHLS", agrAllowancePercent);
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent+ "For Collection " + prodCollection); 
							costSheetObj.setValue("agrDiscountPerKOHLS", agrDiscountPercent);
							if(!duplicateCustomerRecordKOHLS) {
								duplicateCustomerRecordKOHLS = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}
						}
					}else {
						if(!FormatHelper.hasContent(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent);
							costSheetObj.setValue("agrCustomerAllowanceKOHLS", agrAllowancePercent);
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent);
							costSheetObj.setValue("agrDiscountPerKOHLS", agrDiscountPercent);
							if(!duplicateCustomerRecordKOHLS) {
								duplicateCustomerRecordKOHLS = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}
						}
					}


				}
				if("AVERAGE".equalsIgnoreCase(customer)) {


					if(FormatHelper.hasContent(prodCollection) && filteredValuesCollection.contains(prodCollection)) {

						if(prodCollection.equals(flexObj.getString("agrCollection"))){
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent + "For Collection " + prodCollection);        	 
							costSheetObj.setValue("agrCustomerAllowanceAVG", agrAllowancePercent);
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent+ "For Collection " + prodCollection);       
							costSheetObj.setValue("agrDiscountPerAvg", agrDiscountPercent);
							if(!duplicateCustomerRecordAVERAGE) {
								duplicateCustomerRecordAVERAGE = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}	  	        	  
						}      		  
					}else {

						if(!FormatHelper.hasContent(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent);        	 
							costSheetObj.setValue("agrCustomerAllowanceAVG", agrAllowancePercent);
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent);
							costSheetObj.setValue("agrDiscountPerAvg", agrDiscountPercent);
							if(!duplicateCustomerRecordAVERAGE) {
								duplicateCustomerRecordAVERAGE = true;
							} else {
								throw new LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}        	  
						}
					}
				}
				if("MARMAXX".equalsIgnoreCase(customer)) {

					if(prodCollection.equals(flexObj.getString("agrCollection"))){
						agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
						agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
						LOGGER.debug("Setting  Discount % - " + agrDiscountPercent+ "For Collection " + prodCollection); 
						costSheetObj.setValue("agrCustomerAllowanceMARMAXX", agrAllowancePercent); //
						// THIS IS ALWAYS 0 
						LOGGER.debug("Setting  Allowance % - " +agrAllowancePercent+ "For Collection " + prodCollection); 
						costSheetObj.setValue("agrDiscountPerMARMAXX",agrDiscountPercent); 
						if(!duplicateCustomerRecordMARMAXX) {
							duplicateCustomerRecordMARMAXX = true; 
						}else { throw new
							LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table."); 
						}


					}else {
						if(!FormatHelper.hasContent(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent);
							costSheetObj.setValue("agrCustomerAllowanceMARMAXX", agrAllowancePercent); //
							// THIS IS ALWAYS 0 
							LOGGER.debug("Setting  Allowance % - " +agrAllowancePercent); 
							costSheetObj.setValue("agrDiscountPerMARMAXX",agrDiscountPercent); 
							if(!duplicateCustomerRecordMARMAXX) {
								duplicateCustomerRecordMARMAXX = true; 
							}else { throw new
								LCSException("Fix duplicate "+customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table."); 
							}
						}
					}		  

				}
				if("FOOTLOCKER".equalsIgnoreCase(customer)) {
					if(prodCollection.equals(flexObj.getString("agrCollection"))){
						agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
						agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
						LOGGER.debug("Setting  Discount % - " + agrDiscountPercent+ "For Collection " + prodCollection); 
						costSheetObj.setValue("agrCustomerAllowanceFOOTLOCKER", agrAllowancePercent);
						LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent+ "For Collection " + prodCollection); 
						costSheetObj.setValue("agrDiscountPerFOOTLOCKER", agrDiscountPercent);
						if(!duplicateCustomerRecordFOOTLOCKER) { 
							duplicateCustomerRecordFOOTLOCKER =true; }
						else {
							throw new LCSException("Fix duplicate " +customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
						}

					}else {
						if(!FormatHelper.hasContent(flexObj.getString("agrCollection"))) {
							agrAllowancePercent = Float.valueOf(flexObj.getFloat("agrAllowancePercent"));
							agrDiscountPercent = Float.valueOf(flexObj.getFloat("agrDiscountPercent"));
							LOGGER.debug("Setting  Discount % - " + agrDiscountPercent);
							costSheetObj.setValue("agrCustomerAllowanceFOOTLOCKER", agrAllowancePercent);
							LOGGER.debug("Setting  Allowance % - " + agrAllowancePercent);
							costSheetObj.setValue("agrDiscountPerFOOTLOCKER", agrDiscountPercent);
							if(!duplicateCustomerRecordFOOTLOCKER) { 
								duplicateCustomerRecordFOOTLOCKER =true; }
							else {
								throw new LCSException("Fix duplicate " +customer+" discounts in Buisiness Object > Cost Sheet Duty > Customer Discount table.");
							}
						}
					}
				} 
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void setSubRoyaltyValues(LCSCostSheet costSheetObj,LCSLifecycleManaged costSheetDutyBObj,LCSSeason season)  throws WTPropertyVetoException, WTException 
	{
		LOGGER.info("Costsheet Plugin -   setSubRoyaltyValues");

		LCSProduct product = null;
		LCSSKU sku =null;
		LCSSeasonProductLink spl=null;
		Map<Object, Object> inputCriteria = new HashMap();
		try {

			LCSPartMaster skuMaster = costSheetObj.getSkuMaster();
			if(skuMaster!=null){
				LOGGER.info("Season : "+season.getName());
				sku = (LCSSKU) VersionHelper.getVersion(skuMaster,"A");
				LOGGER.info("getversion : "+sku.getName());
				if(sku!=null)
				{
					sku = (LCSSKU) VersionHelper.latestIterationOf(sku); 
					LOGGER.info("SKU : "+sku.isPlaceholder());
				}

				spl = LCSSeasonQuery.findSeasonProductLink(sku, season);


				LOGGER.info("spl :"+spl);
				if(spl.getValue("agrCapsule")!=null)
					inputCriteria.put("agrCapsule", spl.getValue("agrCapsule"));
				LOGGER.info("Capsule : "+spl.getValue("agrCapsule"));
			}

			if(spl.getValue("agrCapsule")!=null){
				LCSMOATable capsuleSubRoyaltyMOATable = (LCSMOATable)costSheetDutyBObj.getValue("agrCapsuleSubRoyalty");
				Collection moaTableColl = capsuleSubRoyaltyMOATable.getRows(inputCriteria, true);
				LOGGER.debug("<<<<<<<<moaTableColl result size>>>>>>" + moaTableColl.size());
				System.out.println("moatableColl size : "+moaTableColl.size());
				Double SubRoyaltyPercent = Double.valueOf(0);
				if (moaTableColl.size() == 0 ) {
					// throw new LCSException("Capsule value not Correct");
					// Capsule is not a required field on product/colorway. this might be zero.

				} else {
					Iterator moaCollIter = moaTableColl.iterator();
					if (moaCollIter.hasNext() ) {  
						FlexObject flexObj = (FlexObject) moaCollIter.next();
						LOGGER.info("flexObj : "+flexObj);
						SubRoyaltyPercent = Double.valueOf(flexObj.getFloat("agrCapsuleSubRoyalty"));
						LOGGER.info("setting SubRoyaltyPercent : "+SubRoyaltyPercent);
						costSheetObj.setValue("agrSubRoyaltyPercent", SubRoyaltyPercent);

					}
				}
			}
			else
			{
				costSheetObj.setValue("agrSubRoyaltyPercent",  Double.valueOf(0));
			}

		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}





	/** Set Freight , Royalty , Packaging and warehouse cost
	 * @param obj
	 * @param costSheetObj
	 * @param map
	 * @param attToGet
	 * @param socks
	 */
	public static void setValueAccordingToCriteria(LCSLifecycleManaged obj, LCSCostSheet costSheetObj, Map map, String attToGet ,boolean socks)
	{
		try
		{
			LOGGER.info("Costsheet Calulations - Inside method setValueAccordingToCriteria () - Setting Values for Freight , Warehouse, Royalty and Packaging");

			if ((obj != null) && (costSheetObj != null))
			{
				String agrFreightPercentAttKey = LCSProperties.get("com.agron.wc.sourcing.CostSheet.agrFreightPercentAttKey");
				String attRoyaltyPercentAttKey = LCSProperties.get("com.agron.wc.sourcing.CostSheet.attColumnRoyaltyPercentAttKey");
				String attPackagingCostDollarAttKey = LCSProperties.get("com.agron.wc.sourcing.CostSheet.attColumnPackagingCostDollarAttKey");
				String attWarehouseCostPercentAttKey = LCSProperties.get("com.agron.wc.sourcing.CostSheet.attColumnWarehouseCostPercentAttKey");


				LCSMOATable moaTable = (LCSMOATable)obj.getValue(attToGet);
				LOGGER.debug("<<<<<<<<<<<<<moaTable.getRows():" + moaTable.getRows());   
				Collection moaTableColl = moaTable.getRows(map, true);
				Float duty = 0.0f;
				Float freightPercentValue = 0.0f;
				Double agrFreightdollar = Double.valueOf(0); // Required For Socks
				Float royaltyValue = 0.0f;
				Float packagingValue = 0.0f;
				Float wareHoustCostValue = 0.0f;
				Double agrBaseFOB = (Double) costSheetObj.getValue("agrBaseFOB");

				Iterator moaCollIter = moaTableColl.iterator();
				if (moaCollIter.hasNext())
				{
					FlexObject flexObj = (FlexObject)moaCollIter.next();

					freightPercentValue = Float.valueOf(flexObj.getFloat(agrFreightPercentAttKey));
					royaltyValue = Float.valueOf(flexObj.getFloat(attRoyaltyPercentAttKey));
					packagingValue = Float.valueOf(flexObj.getFloat(attPackagingCostDollarAttKey));
					wareHoustCostValue = Float.valueOf(flexObj.getFloat(attWarehouseCostPercentAttKey));
					//agrFreightdollar =  Double.valueOf(flexObj.getDouble("agrFreightdollar"));

				}


				agrFreightdollar = (agrBaseFOB * freightPercentValue) / 100;   

				costSheetObj.setValue(agrFreightPercentAttKey, freightPercentValue);
				costSheetObj.setValue("agrFreightDollar", agrFreightdollar);   // For socks set this from the Product Line Duty Table	
				LOGGER.info("Setting Freight (%) : " + freightPercentValue);
				LOGGER.info("Setting Freight ($) : " + agrFreightdollar);

				costSheetObj.setValue(attRoyaltyPercentAttKey, royaltyValue);
				LOGGER.info("Setting Royalty (%) : " + royaltyValue);

				costSheetObj.setValue(attPackagingCostDollarAttKey, packagingValue);
				LOGGER.info("Setting Packaging ($) : " + packagingValue);

				costSheetObj.setValue(attWarehouseCostPercentAttKey, wareHoustCostValue);
				LOGGER.info("Setting WareHouse (%) : " + wareHoustCostValue);


			}
		}
		catch (WTPropertyVetoException e)
		{
			e.printStackTrace();
		}
		catch (WTException e)
		{
			e.printStackTrace();
		}
	}

	/** Set DUty and Tariff for costsheet 
	 * @param obj
	 * @param costSheetObj
	 * @param map
	 * @param attToGet
	 * @param countryName
	 * @throws WTException 
	 */
	public static void setDutyTariffFromHTSCodeAndCountry(LCSLifecycleManaged obj, LCSCostSheet costSheetObj, Map map, String attToGet, String countryName)
	{
		try
		{
			LCSMOATable moaTable = (LCSMOATable)obj.getValue(attToGet);
			Collection moaTableColl = moaTable.getRows(map, true); 
			Iterator moaCollIter = moaTableColl.iterator();
			LOGGER.debug(">>>>>>>>>> # Cost Sheet Duty records for Product Type  >>>>>>>>>>>>>>>>>" + moaTableColl.size());
			FlexObject flexObjectAllOthers = new FlexObject();
			Boolean flag = Boolean.valueOf(false);
			Float dutyPercent = new Float(0.0F);
			Float tariffPercent = new Float(0.0F);
			while (moaCollIter.hasNext())
			{
				FlexObject flexObj = (FlexObject)moaCollIter.next();     
				String countryOId = flexObj.getData(AGR_COUNTRY_OF_ORIGIN);
				LOGGER.debug(">>>>>>>>>>>countryOId String >>>>>>>>>>>>>>>>>" + countryOId);
				LCSCountry countryObjFromTable = (LCSCountry)LCSQuery.findObjectById("VR:com.lcs.wc.country.LCSCountry:" + FormatHelper.format(countryOId));
				String namefromTable = (String)countryObjFromTable.getValue(AGR_NAME);       
				LOGGER.debug("<<<<<<<<<<<<Country Name: " + namefromTable);
				if (AGR_DEFAULT_COUNTRY_NAME.equals(namefromTable)) {
					flexObjectAllOthers = flexObj;
				}
				if (namefromTable.equals(countryName))
				{
					LOGGER.info("<<<<<<<<<<<<<<<Found Exact Country Name in HTS Code Duty Table >>>>>>>>>>>");
					dutyPercent = Float.valueOf(flexObj.getFloat(AGR_DUTY_PERCENT));
					tariffPercent = Float.valueOf(flexObj.getFloat("agrTariffPercent"));
					LOGGER.info("Setting Duty (%)" + dutyPercent);
					costSheetObj.setValue(AGR_DUTY_PERCENT, dutyPercent);
					LOGGER.info("Setting Tariff (%)" + tariffPercent);
					costSheetObj.setValue("agrTariffPercent", tariffPercent);
					flag = Boolean.valueOf(true);
					break;
				}
			}
			if (!flag.booleanValue())
			{
				dutyPercent = Float.valueOf(flexObjectAllOthers.getFloat(AGR_DUTY_PERCENT));
				LOGGER.debug("<<<<<<<<<<<<<<<<<<Duty for all others %:" + dutyPercent); // This might never be required
				if (dutyPercent != null) {
					costSheetObj.setValue(AGR_DUTY_PERCENT, dutyPercent);
					costSheetObj.setValue("agrTariffPercent", tariffPercent);
				}
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (WTPropertyVetoException e)
		{
			e.printStackTrace();
		}
		catch (WTException e)
		{
			e.printStackTrace();
		}
	}

	public static LCSLifecycleManaged getCostsheetDutyBO()
	{
		LCSLifecycleManaged costSheetDutyBObj = null;
		try
		{
			FlexTypeQuery ftq = new FlexTypeQuery();
			FlexType businessObjectType = FlexTypeCache.getFlexTypeFromPath(AGR_FLEXTYPE_DUTY_PATH);      
			String boIdA2A2 = "";
			Collection<?> businessObjectColl = ftq.findAllObjectsTypedBy(businessObjectType).getResults();
			if ((businessObjectColl != null) && (businessObjectColl.size() > 0))
			{
				Iterator<?> businessCollIterator = businessObjectColl.iterator();
				while (businessCollIterator.hasNext())
				{
					FlexObject flexObj = (FlexObject)businessCollIterator.next();         
					boIdA2A2 = flexObj.getString("LCSLIFECYCLEMANAGED.IDA2A2");
					LOGGER.debug("<<<<<<<<Business Object ID:" + boIdA2A2);
					if (FormatHelper.hasContent(boIdA2A2)) {     
						costSheetDutyBObj = (LCSLifecycleManaged)LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + boIdA2A2);
						if(costSheetDutyBObj != null && "Cost Sheet Duty Table".equals(costSheetDutyBObj.getName())); // CHECKING WITH NAME 
						LOGGER.debug("<<<<<<<<Cost Sheet Duty Table");
						break;
						// LCSLifecycleManagedQuery q = new LCSLifecycleManagedQuery();            
					}

				}
			}
		}
		catch (WTException e)
		{
			e.printStackTrace();
		}
		return costSheetDutyBObj;
	}


	private static LCSLifecycleManaged getAgencyFeeBO() {

		LCSLifecycleManaged agencyFeeBO = null;
		try
		{
			FlexTypeQuery ftq = new FlexTypeQuery();
			FlexType businessObjectType = FlexTypeCache.getFlexTypeFromPath(AGR_AGENCYFEE_FLEXTYPE_PATH);      
			String boIdA2A2 = "";
			Collection<?> businessObjectColl = ftq.findAllObjectsTypedBy(businessObjectType).getResults();
			if ((businessObjectColl != null) && (businessObjectColl.size() > 0))
			{
				Iterator<?> businessCollIterator = businessObjectColl.iterator();
				while (businessCollIterator.hasNext())
				{
					FlexObject flexObj = (FlexObject)businessCollIterator.next();         
					boIdA2A2 = flexObj.getString("LCSLIFECYCLEMANAGED.IDA2A2");
					LOGGER.debug("<<<<<<<AGENCY FEE Business Object ID:" + boIdA2A2);
					if (FormatHelper.hasContent(boIdA2A2)) {     
						agencyFeeBO = (LCSLifecycleManaged)LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + boIdA2A2);
						if(agencyFeeBO != null && "Agency Look Up Table".equals(agencyFeeBO.getName())); // CHECKING WITH NAME 
						LOGGER.debug("<<<<<<<<Found Agency Look Up Table");
						break;
						// LCSLifecycleManagedQuery q = new LCSLifecycleManagedQuery();

					}

				}
			}
		}
		catch (WTException e)
		{
			e.printStackTrace();
		}
		return agencyFeeBO;

	}


}
