package com.agron.wc.season;
import java.util.Date;

import com.agron.wc.integration.util.AgronCSVGenerator;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Mastered;
import wt.vc.Versioned;

@SuppressWarnings("deprecation")
public final class AgronSeasonPlugin {
	public static final String AGR_AVAILABILITY_DATE_KEY = LCSProperties
			.get("com.agron.wc.season.LCSProductSeasonLink.AgronSeasonPlugin.AvailabilityDate");
				public static final String AGR_DELIVERY_SEASON_KEY = LCSProperties
			.get("com.agron.wc.season.LCSProductSeasonLink.AgronSeasonPlugin.Deliveryseason");

	public static final String AGR_RETAIL_EFFECTIVE_DATE = LCSProperties
			.get("com.agron.wc.season.LCSProductSeasonLink.AgronSeasonPlugin.RetailEffectiveDate");

	public static final String AGR_WHOLESALE_EFFECTIVE_DATE = LCSProperties
			.get("com.agron.wc.season.LCSProductSeasonLink.AgronSeasonPlugin.WholesaleEffectiveDate");

	public static final String AGR_CARRYOVER_COLORWAY = LCSProperties
			.get("com.agron.wc.season.LCSProductSeasonLink.AgronSeasonPlugin.carryOverColorwayAttribute");

	public static final String AGR_CARRYOVER_COLORWAY_YES = LCSProperties
			.get("com.agron.wc.season.LCSProductSeasonLink.AgronSeasonPlugin.carryOverColorwayYesValue");
			
private static final String YEAR_KEY = LCSProperties.get("com.agron.wc.interface.season.yearKey");
private static final String SEASONTYPE_KEY = LCSProperties.get("com.agron.wc.interface.season.seasonTypeKey");			

	public static final String AGR_VERSION_A = "A";

	public static final String AGR_VERSION_B = "B";
//Agron FlexPLM 11.1 Upgrade - Autopopulate Availability Date on SP Link - START
	public static void populateAvailabilityDate(WTObject obj) {
		
		try {
			if (obj instanceof LCSSKUSeasonLink) {

				LCSSKUSeasonLink lcsskuSeasonLink = (LCSSKUSeasonLink) obj;
				if (lcsskuSeasonLink.isEffectLatest() && lcsskuSeasonLink.getEffectOutDate() == null) {
					if (!lcsskuSeasonLink.isSeasonRemoved()) {
						setDateforColorway(lcsskuSeasonLink);
						setDeliveryseason(lcsskuSeasonLink);
						
					}
					LCSLogic.persist((Persistable) lcsskuSeasonLink, true);
				}

			} else if (obj instanceof LCSProductSeasonLink) {
				LCSProductSeasonLink lcsProductSeasonLink = (LCSProductSeasonLink) obj;
				LCSSKU skuSeasonRev = SeasonProductLocator.getSKUSeasonRev(lcsProductSeasonLink);
				String versionIdentifierValue = VersionHelper.getFullVersionIdentifierValue((Versioned) skuSeasonRev);
				if (lcsProductSeasonLink.isEffectLatest() && lcsProductSeasonLink.getEffectOutDate() == null) {
					if (!lcsProductSeasonLink.isSeasonRemoved()) {
						setDateforCarriedOverProduct(lcsProductSeasonLink, versionIdentifierValue);
						setDeliveryseason(lcsProductSeasonLink);
					}
					LCSLogic.persist((Persistable) lcsProductSeasonLink, true);
				}
			}

		} catch (WTException e) {
			LCSLog.debug(e.getMessage());
		}
	}

	public static void setDateforColorway(LCSSKUSeasonLink lcsskuSeasonLink) {
		try {
			WTPartMaster skuMaster = lcsskuSeasonLink.getSkuMaster();
			LCSSKU skuObj = (LCSSKU) VersionHelper.latestIterationOf((Mastered) skuMaster);
			LCSLog.debug("<<<<<<<<Colorway Creation>>>>>>>>>>");
			if (skuObj != null)
				if (lcsskuSeasonLink.getCarriedOverFrom() == null) {
					LCSLog.debug("<<<<<Colorway Not Carried Over>>>>>");
					LCSSeasonMaster seasonMaster = lcsskuSeasonLink.getSeasonMaster();
					LCSSeason seasonObj = (LCSSeason) VersionHelper.latestIterationOf((Mastered) seasonMaster);
					if (seasonObj.getValue(AGR_AVAILABILITY_DATE_KEY) == null) {
						LCSLog.debug("Season has No availability date");
						return;
					}
					Date availabilityDateAtColorway = (Date) lcsskuSeasonLink.getValue(AGR_AVAILABILITY_DATE_KEY);
					if (availabilityDateAtColorway == null)
						lcsskuSeasonLink.setValue(AGR_AVAILABILITY_DATE_KEY,
								seasonObj.getValue(AGR_AVAILABILITY_DATE_KEY));
				} /*else {
					lcsskuSeasonLink.setValue(AGR_CARRYOVER_COLORWAY, AGR_CARRYOVER_COLORWAY_YES);
				}*/
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	public static void setDateforCarriedOverProduct(LCSProductSeasonLink lcsProductSeasonLink,
			String versionIdentifierValue) {
		try {
			
			if (versionIdentifierValue.contains("A") || versionIdentifierValue.contains("B"))
				return;
			Date date = new Date();
			LCSLog.debug("Setting Product-Season Retail and WholeSale Effective Date");
			Date agrRetailEffectiveDate = (Date) lcsProductSeasonLink.getValue(AGR_RETAIL_EFFECTIVE_DATE);
			Date agrWholesaleEffectiveDate = (Date) lcsProductSeasonLink.getValue(AGR_WHOLESALE_EFFECTIVE_DATE);
			if (agrRetailEffectiveDate == null)
				lcsProductSeasonLink.setValue(AGR_RETAIL_EFFECTIVE_DATE, date);
			if (agrWholesaleEffectiveDate == null)
				lcsProductSeasonLink.setValue(AGR_WHOLESALE_EFFECTIVE_DATE, date);
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void setDeliveryseason(LCSSeasonProductLink spl) {
		try {
			if (spl.getEffectSequence() == 0)	{
				LCSSeason season = SeasonProductLocator.getSeasonRev(spl);
				String seasonType = (String) AgronCSVGenerator.getData(season, SEASONTYPE_KEY, false);	
				if("Spring / Summer".equalsIgnoreCase(seasonType) || "Spring".equalsIgnoreCase(seasonType)){
					spl.setValue(AGR_DELIVERY_SEASON_KEY, "agrQ1|~*~|agrQ2");
				}else if("Fall / Winter".equalsIgnoreCase(seasonType) || "Fall".equalsIgnoreCase(seasonType)){
					spl.setValue(AGR_DELIVERY_SEASON_KEY, "agrQ3|~*~|agrQ4");
				}
			}
		} catch (WTException e) {
			System.out.println("Error while setting Delivery Season:"+e.getMessage());
			e.printStackTrace();
		}
	}
	
}	
	

//Agron FlexPLM 11.1 Upgrade - Autopopulate Availability Date on SP Link - END