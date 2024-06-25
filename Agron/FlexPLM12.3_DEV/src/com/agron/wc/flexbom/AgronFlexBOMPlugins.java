package com.agron.wc.flexbom;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToComponentLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.WTObject;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronFlexBOMPlugins {

	/** AGR_LPNUMBER_KEY read from property entry. */
	public static final String AGR_LPNUMBER_KEY = LCSProperties
			.get("com.agron.wc.flexbom.FlexBOMPart.AgronPopulateLPNumberPlugin.LPNumber");
	
	/** AGR_MATERIAL_REF_NUMBER read from property entry. */
	public static final String AGR_MATERIAL_REF_NUMBER = LCSProperties
			.get("com.agron.wc.flexbom.FlexBOMPart.AgronPopulateLPNumberPlugin.MaterialRefNumber");
	public static final String AGR_SECTION_KEY = LCSProperties
			.get("com.agron.wc.flexbom.FlexBOMPart.AgronPopulateLPNumberPlugin.Section");
	public static final String AGR_LABELLING_PACKAGING_KEY = LCSProperties
			.get("com.agron.wc.flexbom.FlexBOMPart.AgronPopulateLPNumberPlugin.LabelPackaging");

	/** DEBUG read from property entry. */
	public static final boolean DEBUG = LCSProperties
			.getBoolean("com.agron.wc.flexbom.FlexBOMPart.AgronPopulateLPNumberPlugin.debug");

	public static final Logger logger = LogManager.getLogger(AgronFlexBOMPlugins.class);

	
	public static void handleBOMRollUpPlugins(WTObject object) throws WTException {
		FlexBOMPart flexBomoPart = (FlexBOMPart)object ;
		if (flexBomoPart != null) {
			populateLPNumber(flexBomoPart);
		}

	}

	public static void populateLPNumber(FlexBOMPart flexBomoPart) throws WTException {
		FlexBOMPart bomPart = flexBomoPart;
		if (DEBUG) {
			logger.info("BOM PART NAME!!!!! : " + flexBomoPart.getName());
		}

		String matRefNumbers = getMaterialRefNumbers(flexBomoPart.toString());
		LCSPartMaster partMaster = (LCSPartMaster) flexBomoPart.getOwnerMaster();
		WTPart wtpart = (WTPart) VersionHelper.getVersion(partMaster, "A");
		if (wtpart instanceof LCSProduct) {
			LCSProduct product = (LCSProduct) wtpart;
			if (DEBUG) {
				logger.info("Product Name!!!!! : "  + product.getName());
			}

			LCSSourcingConfig source = null;
			LCSSeason season = null;
			FlexSpecification specification = null;
			FlexSpecToComponentLink specComponentLink = null;
			String specToCompLinkOid = "";
			FlexObject flexObject1 = null;
			FlexObject flexObject2 = null;
			LCSSeasonProductLink seasonProduct = null;
			LCSSourcingConfigQuery sourcingQuery = null;
			LCSSourceToSeasonLink sourceSeason = null;
			FlexSpecMaster specMaster = null;
			
			try {
				SearchResults specCompSearchResults = FlexSpecQuery.getSpecToComponentLinksForComponent(bomPart);
				Vector specComplinks = specCompSearchResults.getResults();
				Iterator iter = specComplinks.iterator();

				while (iter.hasNext()) {
					flexObject1 = (FlexObject) iter.next();
					specToCompLinkOid = "OR:com.lcs.wc.specification.FlexSpecToComponentLink:"
							+ flexObject1.getString("FLEXSPECTOCOMPONENTLINK.IDA2A2");
					if (DEBUG) {
						logger.info("specToCompLinkOid!!!!! : "+ specToCompLinkOid);
					}

					if (specToCompLinkOid == null) {
						logger.info(" !!!No Specification is Associated to BOM");
						return;
					}

					specComponentLink = (FlexSpecToComponentLink) LCSQuery.findObjectById(specToCompLinkOid);
					specMaster = (FlexSpecMaster)specComponentLink.getSpecificationMaster();
					specification = (FlexSpecification) VersionHelper.latestIterationOf(specMaster);
					if (DEBUG) {
						logger.info("!!!!!!!!FlexSpecification!!!! " + specification.getName());
					}

					source = (LCSSourcingConfig) VersionHelper
							.latestIterationOf((LCSSourcingConfigMaster) specification.getSpecSource());
					if (DEBUG) {
						logger.info("Sourcing Config  ::" + source.getName());
					}

					Iterator specSeasonUsed = FlexSpecQuery.specSeasonUsedReport(specMaster).iterator();
					
					while (specSeasonUsed.hasNext()) {
						flexObject2 = (FlexObject) specSeasonUsed.next();
						season = (LCSSeason) LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:"
								+ flexObject2.getString("LATESTITERLCSSEASON.BRANCHIDITERATIONINFO"));
						
						if (DEBUG) {
							logger.info("Season Name  :: " + season.getName());
						}

						sourcingQuery = new LCSSourcingConfigQuery();
						sourceSeason = sourcingQuery.getSourceToSeasonLink(source, season);
						if (DEBUG) {
							logger.info("sourceSeasonLink is Primary : " + sourceSeason.isPrimarySTSL());
						}

						if (sourceSeason != null && sourceSeason.isPrimarySTSL()) {
							seasonProduct = LCSSeasonQuery.findSeasonProductLink(product, season);
							if (seasonProduct != null) {

								seasonProduct.setValue(AGR_LPNUMBER_KEY, matRefNumbers);
								LCSLogic.persist(seasonProduct, true);
								if (DEBUG) {
									logger.info("Season Product Link is updated with LP Numbers");
								}
							}
						}
					}
				}
			} catch (WTException exception) {
				exception.printStackTrace();
			}
		}

	}

	public static String getMaterialRefNumbers(String flexBom) {
		StringBuilder strOfMatRefNo = new StringBuilder();
		String matRefNumbers = "";
		FlexBOMLink bomLink = null;
		FlexBOMPart bom = null;
		LCSMaterial material = null;
		Collection bomLinkColl = null;
		String matRefNo = "";

		try {
			bom = (FlexBOMPart) LCSQuery.findObjectById(flexBom);
			bomLinkColl = LCSFlexBOMQuery.findFlexBOMLinks(bom, (String) null, (String) null, (String) null, (String) null,
					(String) null, (String) null, (Date) null, false, (String) null, (String) null, (String) null,
					(String) null);
			Iterator itr = bomLinkColl.iterator();

			while (itr.hasNext()) {
				bomLink = (FlexBOMLink) itr.next();
				if (AGR_LABELLING_PACKAGING_KEY.equals(bomLink.getValue(AGR_SECTION_KEY)) && bomLink.getChild() != null) {
					material = (LCSMaterial) VersionHelper.latestIterationOf(bomLink.getChild());
					if(null != material) {
						if (DEBUG) {
							logger.info("material Name : " + material.getName());
						}

						matRefNo = (String) material.getValue(AGR_MATERIAL_REF_NUMBER);
						if (matRefNo != null) {
							strOfMatRefNo.append(matRefNo);
							strOfMatRefNo.append(",");
						}
					}
				}
			}

			if (strOfMatRefNo.length() > 0) {
				matRefNumbers = strOfMatRefNo.substring(0, strOfMatRefNo.length() - 1).toString();
			}

			if (DEBUG) {
				logger.info("LP Numbers : " + matRefNumbers);
			}
		} catch (WTException exception) {
			exception.printStackTrace();
		}

		return matRefNumbers;
	}

}
