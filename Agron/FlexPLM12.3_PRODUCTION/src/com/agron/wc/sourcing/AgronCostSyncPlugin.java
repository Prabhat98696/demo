package com.agron.wc.sourcing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetLogic;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSSKUCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.method.MethodContext;



public class AgronCostSyncPlugin {

	private static final String COSTSHEET_DIM_LINKS = "COSTSHEET_DIM_LINKS";

	private static final Logger LOGGER = LogManager.getLogger(AgronCostSyncPlugin.class);

	/*private static final String COSTSHEET_TOTAL_COST_AVG_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrTotalCostAvg", "agrTotalCostAvg");
	private static final String COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrNetWSLessAllowanceAvg", "agrNetWSLessAllowanceAvg");
	private static final String COSTSHEET_LANDED_COST_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrLandedCost", "agrLandedCost");*/
	public static final String AGR_AGENT = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.agrAgent");
	public static final String SC_NUMBER = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.number", "number");
	public static final String VENDORGROUP = LCSProperties
			.get("com.agron.wc.sourcing.AgronSourcingConfigurationPlugin.vendorGroup", "vendorGroup");
	private static final String TTI_GROUP = "TTIGroup";

	private static final String COSTSHEET_BASE_FOB_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrBaseFOB", "agrBaseFOB");

	private static final String BASELINE = "Baseline";

	public static void syncCreateCostsheets(WTObject wtObj) throws Exception {
		LOGGER.info("syncCreateCostsheets START>>>>" + wtObj);
		LCSCostSheet costSheetObj = (LCSCostSheet) wtObj;
		if (wtObj == null || wtObj instanceof LCSSKUCostSheet) {
			return;
		}

		LCSSourcingConfigMaster sourcingConfigMaster = costSheetObj.getSourcingConfigMaster();
		if (sourcingConfigMaster == null) {
			LOGGER.debug("sourcingConfigMaster is null hence return");
			return;
		}
		LCSSeasonMaster seasonMaster = costSheetObj.getSeasonMaster();
		if (seasonMaster == null) {
			LOGGER.debug("seasonMaster is null hence return");
			return;
		}
		LCSSourcingConfig sourcingConfigObj = (LCSSourcingConfig) VersionHelper.latestIterationOf(sourcingConfigMaster);
		String vendorGroupStr = "";
		if ((FormatHelper.hasContent(String.valueOf(sourcingConfigObj.getValue(AGR_AGENT))))) {
			LOGGER.debug("::AGENT>>::" + sourcingConfigObj.getValue(AGR_AGENT));
			LCSSupplier agentObj = (LCSSupplier) LCSQuery
					.findObjectById(sourcingConfigObj.getValue(AGR_AGENT).toString());
			vendorGroupStr = (String) agentObj.getValue(VENDORGROUP);
		}

		LCSSeason season = VersionHelper.latestIterationOf(seasonMaster);
		LOGGER.info("sourcingConfigObj::" + sourcingConfigObj.getName() + "::" + sourcingConfigObj + "::Season>> "
				+ season.getName());
		LCSSourceToSeasonLink ssLink = (new LCSSourcingConfigQuery()).getSourceToSeasonLink(sourcingConfigObj, season);
		if (!costSheetObj.isPrimaryCostSheet() && !costSheetObj.isWhatIf() && ssLink.isPrimarySTSL()
				&& vendorGroupStr.equalsIgnoreCase(TTI_GROUP)) {

			LOGGER.info("CS Carried Over from :" + costSheetObj.getCarriedOverFrom());
			LOGGER.info("CS Copied from :" + costSheetObj.getCopiedFrom());
			String scType = sourcingConfigObj.getFlexType().getTypeDisplayName().toUpperCase();

			if ("TEAM".equalsIgnoreCase(scType) || "ATHLETIC".equalsIgnoreCase(scType)) {

				ArrayList<String> applicableColorwayNames = new ArrayList<String>(
						MOAHelper.getMOACollection(costSheetObj.getApplicableColorNames()));
				ArrayList<String> applicableSizes = new ArrayList<String>(
						MOAHelper.getMOACollection(costSheetObj.getApplicableSizes()));
				LOGGER.debug("applicableColorwayNames>>" + applicableColorwayNames + "::applicableSizes>>"
						+ applicableSizes);
				LOGGER.debug("Method Context Params:" + MethodContext.getContext().get(COSTSHEET_DIM_LINKS));

				HashMap<Object, Object> linkMap = new HashMap<>();
				linkMap = (HashMap<Object, Object>) MethodContext.getContext().get(COSTSHEET_DIM_LINKS);

				if (applicableColorwayNames.isEmpty() || applicableSizes.isEmpty()) {
					LOGGER.debug("Costsheet is not associated with Colorway/sizes hence return");
					return;
				}
				/*Double agrTotalCostAvg = ((Double) costSheetObj.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) != null
						? (Double) costSheetObj.getValue(COSTSHEET_TOTAL_COST_AVG_KEY)
								: 0.00);
				Double agrLandedCost = ((Double) costSheetObj.getValue(COSTSHEET_LANDED_COST_KEY) != null
						? (Double) costSheetObj.getValue(COSTSHEET_LANDED_COST_KEY)
								: 0.00);
				Double agrNetWholesaleDollarAvg = ((Double) costSheetObj
						.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) != null
						? (Double) costSheetObj.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY)
								: 0.00);
				LOGGER.debug("agrTotalCostAvg>>>" + agrTotalCostAvg + "::agrNetWholesaleDollarAvg>>>"
						+ agrNetWholesaleDollarAvg);
				Double agrBaseFOB = ((Double) costSheetObj.getValue(COSTSHEET_BASE_FOB_KEY) != null
						? (Double) costSheetObj.getValue(COSTSHEET_BASE_FOB_KEY)
								: 0.00);

				// Trigger the plugin logic only if value of TotalCostAvg,rNetWholesaleDollarAvg
				// and applicableColorwayNames is changed. As of todays formuls landed cost will
				// also change if Base FOB changes. If Landed cost changes , Total Cost AVg will
				// also change.
				LCSCostSheet predCostSheet = (LCSCostSheet) VersionHelper.predecessorOf(costSheetObj);
				if (predCostSheet != null) {
					Double predAgrTotalCostAvg = ((Double) predCostSheet.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) != null
							? (Double) predCostSheet.getValue(COSTSHEET_TOTAL_COST_AVG_KEY)
									: 0.00);
					Double predAgrNetWholesaleDollarAvg = ((Double) predCostSheet
							.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) != null
							? (Double) predCostSheet.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY)
									: 0.00);
					LOGGER.debug("predAgrTotalCostAvg::predAgrNetWholesaleDollarAvg>>>" + predAgrTotalCostAvg + "::::"
							+ predAgrNetWholesaleDollarAvg);
					if (Double.compare(predAgrTotalCostAvg, agrTotalCostAvg) == 0
							&& Double.compare(predAgrNetWholesaleDollarAvg, agrNetWholesaleDollarAvg) == 0
							&& costSheetObj.getApplicableColorNames().equals(predCostSheet.getApplicableColorNames())
							&& costSheetObj.getApplicableSizes().equals(predCostSheet.getApplicableSizes())) {
						LOGGER.debug("No change is relevent costsheet attribute hence retuen");
						// return;
					}
				}*/

				if (ssLink != null) {
					LOGGER.debug("ssLink isPrimary>>>>" + ssLink.isPrimarySTSL());
					Map<String, String> hm = new HashMap<String, String>();
					hm.put("clipboardCopy", "true");
					LCSProduct product = (LCSProduct) VersionHelper.latestIterationOf(
							(LCSProduct) VersionHelper.getVersion(costSheetObj.getProductMaster(), "A"));
					Collection<?> allSources = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(product, season);
					LOGGER.debug("allSources >>>>" + allSources.toString());

					FlexType specType = product.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
					Iterator allStslItr = allSources.iterator();
					while (allStslItr.hasNext()) {
						LCSSourcingConfig scObj = (LCSSourcingConfig) allStslItr.next();
						if (scObj.isPrimarySource())
							continue;
						LOGGER.debug("!!!!!!!!!!!! costSheetObj costsheet name " + costSheetObj.getName());
						LOGGER.debug(
								"!!!!!!!!!!!! costSheetObj.isPrimaryCostSheet " + costSheetObj.isPrimaryCostSheet());
						LOGGER.debug("!!!!!!!!!!!! costSheetObj.getCostSheetType " + costSheetObj.getCostSheetType());
						if (null != scObj && !"SKU".equals(costSheetObj.getCostSheetType())
								&& !BASELINE.equals(costSheetObj.getName().trim())) {
							LOGGER.debug("!!!!!!!!!!!! costSheetObj containing primary costsheet : " + costSheetObj);
							LCSCostSheetLogic costSheetLogic = new LCSCostSheetLogic();
							LOGGER.debug("!!!!!!!!!!!! costSheetObj : " + costSheetObj.getName());
							LOGGER.debug("!!!!!!!!!!!! season : " + season.getName());
							FlexSpecification sourceSpec = new FlexSpecification();
							Collection<?> existingSpecs = LCSQuery.getObjectsFromResults(
									FlexSpecQuery.findSpecsByOwner(product.getMaster(), seasonMaster, scObj.getMaster(),
											specType),
									"VR:com.lcs.wc.specification.FlexSpecification:",
									"FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
							if (existingSpecs != null && existingSpecs.size() > 0) {
								LOGGER.debug(" Spec(s) already exists for Source on the season >>"
										+ existingSpecs.toString());
								sourceSpec = (FlexSpecification) existingSpecs.iterator().next();
								LOGGER.debug(" Source Spec >>" + sourceSpec.getName());

							}

							LCSCostSheet toCostsheet = costSheetLogic.copyCostSheetEnhanced(scObj,
									costSheetObj.getSkuMaster(), costSheetObj, season, false, hm, true);

							LOGGER.debug(
									"!!!!!!! To Costsheet created and now copying attributes of costsheet !!!!");

							toCostsheet.setSpecificationMaster(sourceSpec.getMaster());
							toCostsheet.setValue(COSTSHEET_BASE_FOB_KEY, 0.0f);
							new LCSCostSheetLogic().saveCostSheet(toCostsheet, linkMap);

							LOGGER.debug("!!!!!!!!!!!! Active Costsheet Created !!!!!!!!!!!!");
						}
					}
				}

			} else {
				LOGGER.debug("Its not socks product  ");
			}

		} else {
			LOGGER.debug("Its primary Costsheet or Secondary Source  ");
		}
		LOGGER.info("syncCreateCostsheets END");
	}

	public static void syncUpdateCostsheets(WTObject wtObj) throws Exception {
		LOGGER.info("syncUpdateCostsheets START>>>>" + wtObj);
		LCSCostSheet costSheetObj = (LCSCostSheet) wtObj;
		if (wtObj == null || wtObj instanceof LCSSKUCostSheet) {
			return;
		}

		LCSSourcingConfigMaster sourcingConfigMaster = costSheetObj.getSourcingConfigMaster();
		if (sourcingConfigMaster == null) {
			LOGGER.debug("sourcingConfigMaster is null hence return");
			return;
		}
		LCSSeasonMaster seasonMaster = costSheetObj.getSeasonMaster();
		if (seasonMaster == null) {
			LOGGER.debug("seasonMaster is null hence return");
			return;
		}

		LCSSourcingConfig sourcingConfigObj = (LCSSourcingConfig) VersionHelper.latestIterationOf(sourcingConfigMaster);
		String vendorGroupStr = "";
		if ((FormatHelper.hasContent(String.valueOf(sourcingConfigObj.getValue(AGR_AGENT))))) {
			LOGGER.debug("::AGENT>>::" + sourcingConfigObj.getValue(AGR_AGENT));
			LCSSupplier agentObj = (LCSSupplier) LCSQuery
					.findObjectById(sourcingConfigObj.getValue(AGR_AGENT).toString());
			vendorGroupStr = (String) agentObj.getValue(VENDORGROUP);
		}
		LCSSeason season = VersionHelper.latestIterationOf(seasonMaster);
		LOGGER.info("sourcingConfigObj::" + sourcingConfigObj.getName() + "::" + sourcingConfigObj + "::Season>> "
				+ season.getName());
		LCSSourceToSeasonLink ssLink = (new LCSSourcingConfigQuery()).getSourceToSeasonLink(sourcingConfigObj, season);

		String scType = sourcingConfigObj.getFlexType().getTypeDisplayName().toUpperCase();
		if ("TEAM".equalsIgnoreCase(scType) || "ATHLETIC".equalsIgnoreCase(scType)) {

			if (costSheetObj.isPrimaryCostSheet() && ssLink.isPrimarySTSL()
					&& vendorGroupStr.equalsIgnoreCase(TTI_GROUP)) {

				LOGGER.info("CS Carried Over from :" + costSheetObj.getCarriedOverFrom());
				LOGGER.info("CS Copied from :" + costSheetObj.getCopiedFrom());

				ArrayList<String> applicableColorwayNames = new ArrayList<String>(
						MOAHelper.getMOACollection(costSheetObj.getApplicableColorNames()));
				ArrayList<String> applicableSizes = new ArrayList<String>(
						MOAHelper.getMOACollection(costSheetObj.getApplicableSizes()));

				LOGGER.debug("applicableColorwayNames>>" + applicableColorwayNames + "::applicableSizes>>"
						+ applicableSizes);
				if (applicableColorwayNames.isEmpty() || applicableSizes.isEmpty()) {
					LOGGER.debug("Costsheet is not associated with Colorway/sizes hence return");
					return;
				}

				LOGGER.debug("Method Context Params:" + MethodContext.getContext().get(COSTSHEET_DIM_LINKS));
				
				Double agrBaseFOB = ((Double) costSheetObj.getValue(COSTSHEET_BASE_FOB_KEY) != null
						? (Double) costSheetObj.getValue(COSTSHEET_BASE_FOB_KEY)
								: 0.00);
				LOGGER.debug("Current Base FOB ::>>>" + agrBaseFOB);
				
				HashMap<Object, Object> linkMap = new HashMap<>();
				linkMap = (HashMap<Object, Object>) MethodContext.getContext().get(COSTSHEET_DIM_LINKS);
				boolean wasPrimary = false;
				LCSCostSheet predCostSheet = (LCSCostSheet) VersionHelper.predecessorOf(costSheetObj);
				if (predCostSheet != null) {
					
					LOGGER.debug("predCostSheet was primary ?: " + predCostSheet.isPrimaryCostSheet());
					wasPrimary = predCostSheet.isPrimaryCostSheet();
					
					Double predAgrBaseFOB = ((Double) predCostSheet
							.getValue(COSTSHEET_BASE_FOB_KEY) != null
							? (Double) predCostSheet.getValue(COSTSHEET_BASE_FOB_KEY)
									: 0.00);
					
					LOGGER.debug("Previous Base FOB::>>>" + predAgrBaseFOB);
					
					if(wasPrimary && costSheetObj.getApplicableColorNames().equals(predCostSheet.getApplicableColorNames())
							&& costSheetObj.getApplicableSizes().equals(predCostSheet.getApplicableSizes()) 
							&& Double.compare(predAgrBaseFOB, agrBaseFOB) == 0) {
						LOGGER.debug("No change is relevent costsheet properties hence return");
						return;
					}
				}

				if (ssLink != null) {
					LOGGER.debug("ssLink isPrimary>>>>" + ssLink.isPrimarySTSL());
					Map<String, String> hm = new HashMap<String, String>();
					hm.put("clipboardCopy", "true");

					LCSProduct product = (LCSProduct) VersionHelper.latestIterationOf(
							(LCSProduct) VersionHelper.getVersion(costSheetObj.getProductMaster(), "A"));
					Collection<?> allSources = LCSSourcingConfigQuery.getSourcingConfigForProductSeason(product, season);

					LOGGER.debug("allSources >>>>" + allSources.toString());

					Iterator allStslItr = allSources.iterator();
					while (allStslItr.hasNext()) {
						LCSSourcingConfig scObj = (LCSSourcingConfig) allStslItr.next();
						if (scObj.isPrimarySource())
							continue;
						LOGGER.debug("!!!!!!!!!!!! scObj source name " + scObj.getName());
						if (wasPrimary) {

							Collection costSheetCollection = LCSCostSheetQuery
									.getCostSheetsForSourceToSeason(seasonMaster, scObj.getMaster(), null, true);

							LOGGER.debug("costSheetCollection >>>>" + costSheetCollection.toString());
							for (Object obj : costSheetCollection) {
								LCSCostSheet toCostsheet = (LCSCostSheet) obj;
								LOGGER.debug("toCostsheet >>>>" + toCostsheet.getName());

								toCostsheet.setApplicableColorNames(costSheetObj.getApplicableColorNames());
								toCostsheet
								.setApplicableSizeCategoryNames(costSheetObj.getApplicableSizeCategoryNames());
								toCostsheet.setApplicableSizes(costSheetObj.getApplicableSizes());
								toCostsheet.setApplicableSizes2(costSheetObj.getApplicableSizes2());
								toCostsheet.setRepresentativeSize(costSheetObj.getRepresentativeSize());
								toCostsheet.setRepresentativeSize2(costSheetObj.getRepresentativeSize2());
								toCostsheet.setSkuMaster(costSheetObj.getSkuMaster());

								toCostsheet.setValue(COSTSHEET_BASE_FOB_KEY, agrBaseFOB);
								toCostsheet = LCSCostSheetLogic.updateCostSheetForLinkObjects(toCostsheet, costSheetObj,
										hm);
								toCostsheet = LCSCostSheetLogic.copyCostSheetLinkObjects(toCostsheet, costSheetObj, hm);
								new LCSCostSheetLogic().saveCostSheet(toCostsheet, true, false);
								LOGGER.debug("toCostsheet updated >>>>");
								break;
							}
						} else {

							Collection costSheetCollection = LCSCostSheetQuery
									.getCostSheetsForSourceToSeason(seasonMaster, scObj.getMaster(), null, false, true);
							if (costSheetCollection != null && !costSheetCollection.isEmpty())
								LOGGER.debug("costSheetCollection for active >>>>" + costSheetCollection.toString());
							FlexType specType = product.getFlexType()
									.getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
							LCSCostSheetLogic costSheetLogic = new LCSCostSheetLogic();
							boolean primaryUpdated = false;
							for (Object obj : costSheetCollection) {
								LCSCostSheet toCostsheet = (LCSCostSheet) obj;
								LOGGER.debug("toCostsheet >>>>" + toCostsheet.getName());
								LOGGER.debug("toCostsheet type >>>>" + toCostsheet.getCostSheetType());
								if ("SKU".equals(toCostsheet.getCostSheetType()))
									continue;

								if (toCostsheet.getApplicableSizes()
										.equalsIgnoreCase(costSheetObj.getApplicableSizes())) {
									LOGGER.debug("setting toCostsheet as primary >>>>" + toCostsheet.getName());
									LOGGER.debug(" toCostsheet >>>>" + toCostsheet);
									toCostsheet.setPrimaryCostSheet(true);
									new LCSCostSheetLogic().saveCostSheet(toCostsheet, true, false);
									primaryUpdated = true;
									break;
								}
							}
							if (!primaryUpdated) {

								FlexSpecification sourceSpec = new FlexSpecification();
								Collection<?> existingSpecs = LCSQuery.getObjectsFromResults(
										FlexSpecQuery.findSpecsByOwner(product.getMaster(), seasonMaster,
												scObj.getMaster(), specType),
										"VR:com.lcs.wc.specification.FlexSpecification:",
										"FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
								if (existingSpecs != null && existingSpecs.size() > 0) {
									LOGGER.debug(" Spec(s) already exists for Source on the season >>"
											+ existingSpecs.toString());
									sourceSpec = (FlexSpecification) existingSpecs.iterator().next();
									LOGGER.debug(" Spec >>" + sourceSpec.getName());

								}

								LCSCostSheet newCostsheet = costSheetLogic.copyCostSheetEnhanced(scObj,
										costSheetObj.getSkuMaster(), costSheetObj, season, false, hm, true);

								LOGGER.debug(
										"!!!!!! New Costsheet created and now copying attributes of costsheet !!!!!");
								newCostsheet.setApplicableColorNames(costSheetObj.getApplicableColorNames());
								newCostsheet
								.setApplicableSizeCategoryNames(costSheetObj.getApplicableSizeCategoryNames());
								newCostsheet.setApplicableSizes(costSheetObj.getApplicableSizes());
								newCostsheet.setApplicableSizes2(costSheetObj.getApplicableSizes2());
								newCostsheet.setRepresentativeSize(costSheetObj.getRepresentativeSize());
								newCostsheet.setRepresentativeSize2(costSheetObj.getRepresentativeSize2());
								newCostsheet.setPrimaryCostSheet(true);
								newCostsheet.setSpecificationMaster(sourceSpec.getMaster());
								new LCSCostSheetLogic().saveCostSheet(newCostsheet, true, false);
								LOGGER.debug("!!!!!!!!!!!! Primary Costsheet created !!");

							}

						}

					}
				}

			} else {
				LOGGER.debug("Its not primary Costsheet of Primary Source  ");

			}
		} else {
			LOGGER.debug("not socks product");
		}

	}

}