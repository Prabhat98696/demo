package com.agron.wc.product;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductClientModel;
import com.lcs.wc.product.LCSProductHelper;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.specification.FlexSpecLogic;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToSeasonLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.specification.*;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMLogic;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flextype.*;

import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.ptc.rfa.rest.search.SearchResult;
import com.lcs.wc.part.LCSPartMaster;

import wt.enterprise.RevisionControlled;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Mastered;
import wt.vc.wip.Workable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class handles Agron logic for CARRYOVER_PRODUCTS/CARRYOVER_SKUS
 * @author Vishu Singhvi
 * @version 2.0
 */
//Agron FlexPLM 11.1 Upgrade - Carry over functionality in AgronSeasonController.jsp - START
public class AgronCarryoverProductSKUUtil {
	public static final String CLASSNAME = AgronCarryoverProductSKUUtil.class.getName();
	public static final boolean DEBUG = LCSProperties.getBoolean("com.agron.wc.product.AgronCarryoverProductSKUUtil.verbose");
	public static final String agrSeasonType = LCSProperties.get("com.agron.wc.season.seasonType");
	public static final String agrSeasonYear = LCSProperties.get("com.agron.wc.season.year");
	public static String carryoverProduct = LCSProperties.get("com.agron.wc.product.AgronCarryoverProductSKUUtil.carryoverProductKey");
	public static String configlink = null;
	/**
	 * manageProductSKUCarryOver - activity CARRYOVER_PRODUCTS/CARRYOVER_SKUS the complete Agron carryover logic is handled
	 * On carry over a Colorway or product the system would do the following 
	 * a. Carry over only the primary source from the carryover from season. 
	 * b. Does not carry over any specifications associated with the primary source, 
	 * c. Create a default spec for the primary source. 
	 * d. This spec will not contain any components such as BOM , measurements and image pages. It would be empty. 
	 * e. For the primary source , default primary cost sheets would be created.

	 * @param object
	 * @throws WTException
	 */
	public static void manageProductCarryOver(Collection versionIds, String destSeason, String previousSeasonId) throws WTException, WTPropertyVetoException {
		if (DEBUG) System.out.println("**********AgronCarryoverProductSKUUtil.manageProductCarryOver - Start**********");
		LCSSeason season = (LCSSeason)LCSQuery.findObjectById(destSeason);

		Iterator productIds = LCSQuery.getObjectsFromCollection(versionIds, 2).iterator();
		while (productIds.hasNext()) {

			LCSProduct product = (LCSProduct)productIds.next();
			LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(product, season);
			if (DEBUG) System.out.println("!!!!!!!!!!!! spl " + spl);
			if (DEBUG) System.out.println("!!!!!!!!!!!! spl carryoverProduct :: " + spl.getValue(carryoverProduct)); 

			if (!"true".equalsIgnoreCase(String.valueOf(spl.getValue(carryoverProduct)))) {
				System.out.println("!!!!!!!!!!!! deleteSourceSpecSeasonLinks for spl ");
				deleteSourceSpecSeasonLinks(spl,previousSeasonId);
				spl.setValue(carryoverProduct, Boolean.valueOf(true));
				LCSLogic.persist(spl, true);
				continue;
			}
			System.out.println("!!!!!!!!!!!! Product was already carried over. Carryover customization will not kick off !!!!!!!!!!!!!");
		}
		if (DEBUG) System.out.println("**********AgronCarryoverProductSKUUtil.manageProductCarryOver - End********** \n");
	}

	public static void manageSKUCarryOver(Collection versionIds, String destSeason, String previousSeasonId) throws WTException, WTPropertyVetoException {
		if (DEBUG) System.out.println("**********AgronCarryoverProductSKUUtil.manageSKUCarryOver - Start**********"); 
		LCSSeason season = (LCSSeason)LCSQuery.findObjectById(destSeason);
		Iterator skuIds = LCSQuery.getObjectsFromCollection(versionIds, 2).iterator();
		while (skuIds.hasNext()) {
			LCSSKU sku = (LCSSKU)skuIds.next();
			LCSProduct product = sku.getProduct();
			LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(product, season);
			if (DEBUG) System.out.println("!!!!!!!!!!!! spl 1" + spl); 
			if (DEBUG) System.out.println("!!!!!!!!!!!! spl carryoverProduct 1 " + spl.getValue(carryoverProduct)); 
			if (!"true".equalsIgnoreCase(String.valueOf(spl.getValue(carryoverProduct)))) {
				System.out.println("!!!!!!!!!!!! deleteSourceSpecSeasonLinks for spl " + spl);
				deleteSourceSpecSeasonLinks(spl,previousSeasonId);
				spl.setValue(carryoverProduct, Boolean.valueOf(true));
				LCSLogic.persist(spl, true);
				continue;
			}
			System.out.println("!!!!!!!!!!!! Product was already carried over. Colorway will be carried over as per OOTB!!!!!!!!!!!!!");
		}
		if (DEBUG) System.out.println("**********AgronCarryoverProductSKUUtil.manageSKUCarryOver - End********** \n");
	}

	/**
	 * @author Vishu Singhvi
	 * @param LCSSeasonProductLink
	 * Method to deleted the SpecToSeasonLink, Non Primary SourceToSeasonLink, non primary CS
	 * and Create New blank Spec
	 */ 
	public static void deleteSourceSpecSeasonLinks(LCSSeasonProductLink spLink, String previousSeasonId) throws WTException {
		LCSProduct prodObj = null;
		LCSSeason seasonObj = null;
		LCSPartMaster prodMaster=null;
		try
		{
			LCSSeasonProductLink spl = (LCSSeasonProductLink) spLink;
			LCSSeasonMaster seasonMaster = (LCSSeasonMaster) spl.getSeasonMaster();
			//Retrieving the LCSSeason object from Season Master Object
			seasonObj = (LCSSeason)VersionHelper.latestIterationOf(seasonMaster);
			if (DEBUG) System.out.println("!!!!!!!!!!!! seasonObj Name " + seasonObj.getName());
			String prodMasterID = FormatHelper.format(spl.getProductMasterId()); 
			prodMaster = (LCSPartMaster) LCSQuery.findObjectById("OR:com.lcs.wc.part.LCSPartMaster:" + prodMasterID);  //cast changed from WTPartMaster to LCSPartMaster
			//Retrieving the LCSPRODUCT object from PRODUCT Master Object
			prodObj = (LCSProduct)VersionHelper.latestIterationOf(prodMaster);
			if (DEBUG) System.out.println("!!!!!!!!!!!! prodObj.getName " + prodObj.getName());

			if (prodObj != null) {
				// Find the Sourcing configs of Season-Product
				FlexType specType  = FlexTypeCache.getFlexTypeFromPath("Specification\\Adidas");
				Collection destSourceConfigs = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(prodMaster, seasonMaster);
				if (DEBUG) System.out.println("!!!!!!!!!!!! destSourceConfigs " + destSourceConfigs); 
				Iterator destSources = destSourceConfigs.iterator();
				LCSSourcingConfig config = null;
				LCSCostSheet csObject = null;
				LCSSourceToSeasonLink sourceLink = null;
				FlexSpecQuery specQuery = new FlexSpecQuery();
				FlexSpecLogic specLogic = new FlexSpecLogic();
				LCSCostSheetLogic csLogic = new LCSCostSheetLogic();
				LCSSourcingConfigLogic sourcingLogic = new LCSSourcingConfigLogic();
				LCSSourcingConfigQuery configQuery = new LCSSourcingConfigQuery();
				String specCompLinkOid = "";
				List<Iterator> productBOMItr = new ArrayList<Iterator>();				
				while(destSources.hasNext()) {
					LCSSourceToSeasonLink stsl = null;
					config = (LCSSourcingConfig) destSources.next();
					if (DEBUG) System.out.println("!!!!!!!!!!!! config " + config.getSourcingConfigName());
					config = (LCSSourcingConfig) VersionHelper.latestIterationOf(config);
					LCSSourcingConfigQuery lscq = new LCSSourcingConfigQuery();
					stsl = lscq.getSourceToSeasonLink(config, seasonObj);
					Collection  destSpecsColl = LCSQuery.getObjectsFromResults(FlexSpecQuery.findSpecsByOwner(prodMaster, seasonMaster, (LCSSourcingConfigMaster)config.getMaster(), specType), "VR:com.lcs.wc.specification.FlexSpecification:", "FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
					if (DEBUG) System.out.println("!!!!!!!!!!!! destSpecsColl " + destSpecsColl);
					Iterator destSpecs = destSpecsColl.iterator();
					Collection destSpecComponents = null;
					Iterator productBOMs = null;
					while(destSpecs.hasNext()){
						FlexSpecification destSpec = (FlexSpecification)destSpecs.next();
						if (DEBUG) System.out.println("!!!!!!!!!!!! Destination Spec  " + destSpec);
						// Find the Specs which are linked to current season and delete SpecToSeasonlinks from the Current Season-Product-Source
						if (destSpec != null)
						{
							FlexSpecToSeasonLink specLink = FlexSpecQuery.findSpecToSeasonLink((FlexSpecMaster)destSpec.getMaster(),seasonMaster);//cast changed from WTPartMaster to FlexSpecMaster
							if (DEBUG) System.out.println("!!!!!!!!!!!! specification Link " + specLink);
							Iterator specLinks = FlexSpecQuery.getObjectsFromResults(FlexSpecQuery.getSpecToComponentLinks(destSpec),
									"OR:com.lcs.wc.specification.FlexSpecToComponentLink:", "FLEXSPECTOCOMPONENTLINK.IDA2A2").iterator();

							FlexSpecToComponentLink link = null;
							String oid = "";
							while (specLinks.hasNext()) {
								link = (FlexSpecToComponentLink) specLinks.next();
								WTObject component = link.getComponent();
								if ((component instanceof Mastered)) {
									String cVersion = link.getComponentVersion();
									component = (WTObject) VersionHelper.getVersion((Mastered) component, cVersion);
									if (component == null) {
										continue;
									}
									oid = FormatHelper.getVersionId((RevisionControlled) component);
								} else if (((!(component instanceof FlexTyped)) || (ACLHelper.hasViewAccess(component))) && (component instanceof Workable)) {
									component = (WTObject) VersionHelper.getOriginalCopy((Workable) component);
									oid = FormatHelper.getVersionId((RevisionControlled) component);
								} else {
									oid = FormatHelper.getObjectId(component);
								}

								if (DEBUG) System.out.println("!!!!!!!!!!!! oid: " + oid);
								if (DEBUG) System.out.println("is BOMPart ::" +(oid.indexOf("FlexBOMPart") > -1));

								if(oid != null && !(oid.indexOf("FlexBOMPart") > -1)) {
									specCompLinkOid = specCompLinkOid.concat("|~*~|");
									specCompLinkOid = specCompLinkOid.concat(oid);
									if (DEBUG) System.out.println("!!!!!!!!!!!! specCompLinkOid " + specCompLinkOid);
								}
							}
							if(null != stsl && stsl.isPrimarySTSL()) {
								productBOMs = new LCSFlexBOMQuery().findBOMPartsForOwner(prodObj, null, null, destSpec).iterator();
								productBOMItr.add(productBOMs);
							}
							specLogic.deleteSpecToSeason(specLink);
							if (DEBUG) System.out.println("!!!!!!!!!!!! deleting spectoseasonlink " + specLink);
						}
					}
					
					if (DEBUG) System.out.println("!!!!!!!!!!!! config.isPrimarySource() " + config.isPrimarySource());
					if (DEBUG) System.out.println("!!!!!!!!!!!! stsl.isPrimarySTSL() " + stsl.isPrimarySTSL());
					Iterator ghostCostSheetIter = LCSCostSheetQuery.getCostSheetsForSourceToSeason(stsl, null).iterator();
					while (ghostCostSheetIter.hasNext()) {
						csObject = (LCSCostSheet)ghostCostSheetIter.next();
						if (DEBUG) System.out.println("!!!!!!!!!!!! csObject.isPrimaryCostSheet " + csObject.isPrimaryCostSheet()); 
						if (DEBUG) System.out.println("!!!!!!!!!!!! csObject.getCostSheetType " + csObject.getCostSheetType());
						if (DEBUG) System.out.println("!!!!!!!!!!!! costsheet name " + csObject.getName());
						//If not primary CS then delete the CS's and Colorway cost sheet
						if (DEBUG) System.out.println("!!!!!!!!!!!! csObject to be deleted " + csObject.getName());
						csLogic.deleteCostSheet(csObject,true);
					}
					//If not primary source to season link of Product-Season then delete the source to season link
					if(!stsl.isPrimarySTSL()) {
						if (DEBUG) System.out.println("!!!!!!!!!!!! Deleting non Primary STSL " + stsl);
//						stsl = (LCSSourceToSeasonLink) PersistenceHelper.manager.refresh(stsl);
						stsl = (LCSSourceToSeasonLink) VersionHelper.latestIterationOf(stsl);
						sourcingLogic.deleteSourceToSeasonLink(stsl,true);
						continue;
					}
					LCSSeason previousSeasonObj = (LCSSeason) LCSSeasonQuery.findObjectById(previousSeasonId);
					previousSeasonObj = (LCSSeason) VersionHelper.latestIterationOf(previousSeasonObj);
					if (DEBUG) System.out.println("!!!!!!!!!!!! Previous Season Name :: "+previousSeasonObj.getName());
					LCSSourceToSeasonLink previousStsl = new LCSSourcingConfigQuery().getSourceToSeasonLink(config, previousSeasonObj);
					Iterator previousCostSheetIter = null;
					if(previousStsl != null && previousStsl.isPrimarySTSL()) {
						previousCostSheetIter = LCSCostSheetQuery.getCostSheetsForSourceToSeason(previousStsl, null).iterator();
					}
					if (DEBUG) System.out.println("!!!!!!!!!!!! Creating New Spec"); 
					createSpec(seasonMaster, (LCSSourcingConfigMaster)config.getMaster(), prodMaster, specType, specCompLinkOid, previousCostSheetIter,spl,productBOMItr,previousStsl);
				}
			} 
		} catch (WTException wtException) {
			wtException.printStackTrace();
			throw new WTException(" Error in managing carry over of product " + wtException.getLocalizedMessage());
		} 
	}
	/**
	 * @author Vishu Singhvi
	 * Creates a specification object for Product-Season and SourcingConfiguration
	 */
	public static void createSpec(LCSSeasonMaster seasonMaster, LCSSourcingConfigMaster configMaster,LCSPartMaster prodMaster,FlexType specType, String specCompLinkOid, Iterator previousCostSheetIter, LCSSeasonProductLink spl, List<Iterator> productBOMItr, LCSSourceToSeasonLink previousStsl) {
		try
		{
			LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(seasonMaster);
			String seasonType ="";
			String year="";

			FlexSpecQuery specQuery = new FlexSpecQuery();
			Collection existingSpecs = LCSQuery.getObjectsFromResults(FlexSpecQuery.findSpecsByOwner(prodMaster, seasonMaster, configMaster, specType), "VR:com.lcs.wc.specification.FlexSpecification:", "FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
			if(existingSpecs != null && existingSpecs.size() > 0){
				if (DEBUG) System.out.println(String.valueOf(CLASSNAME) + " Spec(s) already exists for Source on the season...not creating a new one"); 
				return;
			}
			LCSProduct product = (LCSProduct)VersionHelper.latestIterationOf(prodMaster);
			//logic to get the season type and year to set the name for New specification
			if (season != null) {
				seasonType = (String)season.getValue(agrSeasonType);
				if (FormatHelper.hasContent(seasonType)) {
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
				year = (String)season.getValue(agrSeasonYear);
				if (FormatHelper.hasContent(year)) {
					year = year.replace("agr20", "agr");
				}
			}
			if (DEBUG) System.out.println("!!!!!!!!!!!! seasonType " + seasonType + " year " + year);
			FlexSpecification flexSpecification = FlexSpecification.newFlexSpecification();
			flexSpecification.setFlexType(specType);
			flexSpecification.setMaster(new FlexSpecMaster());//changed to WTPartMaster to FlexSpecMaster.
			flexSpecification.setSpecOwner(prodMaster);
			flexSpecification.setSpecSource(configMaster);
			flexSpecification.setValue("agrSeasonReference",seasonType);
			flexSpecification.setValue("agrYearReference",year);
			flexSpecification.setValue("agrDevelopmentStage","agrProto");

			flexSpecification = FlexSpecHelper.service.saveSpec(flexSpecification);
			if(DEBUG)System.out.println("!!!!!!!!!!!! Spec created successfully");
			FlexSpecLogic specLogic = new FlexSpecLogic();

			FlexSpecToSeasonLink specLink = specLogic.addSpecToSeason((FlexSpecMaster)flexSpecification.getMaster(), seasonMaster);//cast changed from WTPartMaster to FlexSpecMaster
			if(DEBUG)System.out.println("!!!!!!!!!!!! flexSpecification "+flexSpecification.getName());
			if(DEBUG)System.out.println("!!!!!!!!!!!! flexSpecification specCompLinkOid : "+specCompLinkOid);

			Collection compLinksCollection = LCSQuery.getObjectsFromCollection(MOAHelper.getMOACollection(specCompLinkOid));
			FlexSpecHelper.service.addComponentsToSpec(flexSpecification, null, compLinksCollection);
			if(DEBUG)System.out.println("!!!!!!!!!!!! Components added to newly created flexSpecification "+flexSpecification.getName());
			if(DEBUG)System.out.println("!!!!!!!!!!!! Creating Costsheets and BOMs !!!!!!!!!!!");

			LCSSourcingConfig lcsSourcingConfig = (LCSSourcingConfig) VersionHelper.latestIterationOf(configMaster);
			String sourceId = FormatHelper.getVersionId(lcsSourcingConfig);
			String specId = FormatHelper.getVersionId(flexSpecification);
			LCSProductClientModel pcm = new LCSProductClientModel();
			LCSProductLogic logic = new LCSProductLogic();

			if (DEBUG) System.out.println("!!!!!!!!!!!! Source isPrimarySource " + lcsSourcingConfig.isPrimarySource());
			if (DEBUG) System.out.println("!!!!!!!!!!!! Source Name : " + lcsSourcingConfig.getName());
			
			if(null != previousCostSheetIter) {
				while (previousCostSheetIter.hasNext()) {
					LCSCostSheet previousCsObject = (LCSCostSheet)previousCostSheetIter.next();
					if (DEBUG) System.out.println("!!!!!!!!!!!! previousCsObject costsheet name " + previousCsObject.getName());
					if (DEBUG) System.out.println("!!!!!!!!!!!! previousCsObject.isPrimaryCostSheet " + previousCsObject.isPrimaryCostSheet());
					if (DEBUG) System.out.println("!!!!!!!!!!!! previousCsObject.getCostSheetType " + previousCsObject.getCostSheetType());
					if (DEBUG) System.out.println("!!!!!!!!!!!! is source to season link - Primary : " + previousStsl.isPrimarySTSL());
					if(null != lcsSourcingConfig && previousStsl.isPrimarySTSL() && !"SKU".equals(previousCsObject.getCostSheetType()) && !"Baseline".equals(previousCsObject.getName().trim())) {
						if (DEBUG) System.out.println("!!!!!!!!!!!! previousCsObject containing primary costsheet : " + previousCsObject);
						LCSCostSheetLogic costSheetLogic = new LCSCostSheetLogic();
						if (DEBUG) System.out.println("!!!!!!!!!!!! previousCsObject : " + previousCsObject.getName());
						if (DEBUG) System.out.println("!!!!!!!!!!!! season : " + season.getName());
						if (DEBUG) System.out.println("!!!!!!!!!!!! product : " + spl.getProductARevId());
						if (DEBUG) System.out.println("!!!!!!!!!!!! Specification : " + flexSpecification.getName());
						
						Map<String,String> hm = new HashMap<String,String>();
						hm.put("clipboardCopy", "true");
						LCSCostSheet newCostSheet = costSheetLogic.copyCostSheetEnhanced(lcsSourcingConfig, previousCsObject.getSkuMaster(), previousCsObject, season, false, hm,true);

						if (DEBUG) System.out.println("!!!!!!!!!!!! Costsheet created and now copying attributes of costsheet !!!!!!!!!!!!");
						newCostSheet.setApplicableSizeCategoryNames(previousCsObject.getApplicableSizeCategoryNames());
						newCostSheet.setApplicableSizes(previousCsObject.getApplicableSizes());
						newCostSheet.setApplicableSizes2(previousCsObject.getApplicableSizes2());
						newCostSheet.setRepresentativeSize(previousCsObject.getRepresentativeSize());
						newCostSheet.setRepresentativeSize2(previousCsObject.getRepresentativeSize2());
						newCostSheet.setSkuMaster(previousCsObject.getSkuMaster());
						newCostSheet.setSpecificationMaster(flexSpecification.getMaster());
						new LCSCostSheetLogic().save(newCostSheet);
						if (DEBUG) System.out.println("!!!!!!!!!!!! Costsheet linked to new specification !!!!!!!!!!!!");
					}
				}
			}
			if(DEBUG)System.out.println("!!!!!!!!!!!! Season Name !!!!!!!!!!!" + season.getName());
			if(DEBUG)System.out.println("!!!!!!!!!!!! Season Version Id !!!!!!!!!!!" + FormatHelper.getVersionId(season));
			if(DEBUG)System.out.println("!!!!!!!!!!!! Product Version Id !!!!!!!!!!!" + FormatHelper.getVersionId(product));
			if(DEBUG)System.out.println("!!!!!!!!!!!! productBOMItr !!!!!!!!!!!" + productBOMItr);
			
			FlexBOMPart newflexBOMPart = null;
			if(previousStsl != null && previousStsl.isPrimarySTSL() && productBOMItr != null) {
				Iterator iterator = productBOMItr.iterator();
				while (iterator.hasNext()) {
					Iterator productBOMs = (Iterator) iterator.next();
					while(productBOMs.hasNext()) {
						FlexBOMPart bomPart = (FlexBOMPart) productBOMs.next();
						LCSFlexBOMLogic bomLogic = new LCSFlexBOMLogic();
						bomPart.setValue("agrSeasonReference",seasonType);
						bomPart.setValue("agrYearReference",year);

						newflexBOMPart = bomLogic.associateToProduct(bomPart, product, true, null, false, "COPY");

						String bomVersionId = FormatHelper.getVersionId(newflexBOMPart);
						if(DEBUG)System.out.println("!!!!!!!!!!!! bomVersionId !!!!!!!!!!!" + FormatHelper.getVersionId(newflexBOMPart));

						compLinksCollection = LCSQuery.getObjectsFromCollection(MOAHelper.getMOACollection(bomVersionId));
						if(DEBUG)System.out.println("!!!!!!!!!!!! compLinksCollection !!!!!!!!!!!" + compLinksCollection);

						FlexSpecHelper.service.addComponentsToSpec(flexSpecification, null, compLinksCollection);
						if(DEBUG)System.out.println("!!!!!!!!!!!! Added BOM Component to Spec !!!!!!!!!!!");
					}
				}
			}			
		}
		catch (WTException wte) {
			wte.printStackTrace();
		}
		catch (WTPropertyVetoException wtpv) {
			wtpv.printStackTrace();
		} 
	}
}
//Agron FlexPLM 11.1 Upgrade - Carry over functionality in AgronSeasonController.jsp - END