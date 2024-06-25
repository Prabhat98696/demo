package com.agron.wc.product;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductLogic;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;

public class AgronSetClearance {
	
	private static final String SSLINK_CATALOG_KEY = LCSProperties.get("com.agron.wc.product.sslink.catalog.key","agrCatalog");
	private static final String SSLINK_CATALOG_CLEARANCE_VALUE = LCSProperties.get("com.agron.wc.product.sslink.catalog.clearanceValue","agrClearance");
	
	private static final String SPLINK_WF_KEY = LCSProperties.get("com.agron.wc.product.splink.wholesaleforcast.key","agrWholesaleForecastProduct");
	private static final String SPLINK_CF_KEY = LCSProperties.get("com.agron.wc.product.splink.clearnanceforecast.key","agrClearanceForecastProduct");
	private static final String SSLINK_WF_KEY = LCSProperties.get("com.agron.wc.product.sslink.wholesaleforcast.key","agrForecastedQtySKU");
	private static final String SSLINK_CF_KEY = LCSProperties.get("com.agron.wc.product.sslink.clearnanceforecast.key","agrClearanceForecast");
	private static final String SSLINK_BASECOLOR_KEY = LCSProperties.get("com.agron.wc.product.sslink.basecolor.key","agrBaseColor");
	
	private static final Logger LOGGER = LogManager.getLogger(AgronSetClearance.class);
	
	public static void setClearanceValue(WTObject object) throws Exception {
		if (object instanceof LCSSKUSeasonLink) {
			LCSSKUSeasonLink ssLink = (LCSSKUSeasonLink)object;
			LCSSKU sku = SeasonProductLocator.getSKUARev(ssLink);
			LCSSKU copiedFrom=(LCSSKU)sku.getCopiedFrom();
			if(copiedFrom !=null) {
				String copiedFromType=copiedFrom.getFlexType().getFullName(true);
				String copiedToType=sku.getFlexType().getFullName(true);
				if(ssLink.getSeasonLinkType().equalsIgnoreCase("SKU") && ssLink.isEffectLatest() && ssLink.getEffectSequence() <=1 && !ssLink.isSeasonRemoved()){
					if(copiedFromType.contains("SMU & Clearance") && copiedToType.contains("Adidas")) {
						ssLink.setValue(SSLINK_CATALOG_KEY,SSLINK_CATALOG_CLEARANCE_VALUE);	
					}
				}
			}
		}		
	}
	
	public static void setForecastValue(WTObject object) throws Exception {
		if (object instanceof LCSSKUSeasonLink) {
			LOGGER.debug("## Start method - setForecastValue ###");
			LCSSKUSeasonLink ssLink = (LCSSKUSeasonLink)object;
			
			if(ssLink.isEffectLatest() && ssLink.getEffectOutDate() == null && !ssLink.isSeasonRemoved()){
				LCSSKU skuARev = SeasonProductLocator.getSKUARev(ssLink);
				LCSProduct productARev = SeasonProductLocator.getProductARev(skuARev);
				LCSSeason season = VersionHelper.latestIterationOf(ssLink.getSeasonMaster());
				
				boolean isSKUBaseColor = false;
				if(ssLink.getValue(SSLINK_BASECOLOR_KEY)!=null) {
					isSKUBaseColor = (boolean)ssLink.getValue(SSLINK_BASECOLOR_KEY);
				}
						
				
				LOGGER.debug(">>> SKU Season Link is Effect Latest >>>>");
				boolean updateForecast = false;
				LCSSeasonProductLink spLink = LCSSeasonQuery.findSeasonProductLink(productARev, season);
				if(spLink !=null && isSKUBaseColor) {
					LOGGER.info(">>> SKU is Base Color >>>"+isSKUBaseColor);
					LOGGER.info(">>> Season Product Link >>>"+spLink);
					
					if (spLink.isEffectLatest() && spLink.getEffectOutDate() == null && !spLink.isSeasonRemoved()) {

						if (spLink.getValue(SPLINK_CF_KEY) != null && spLink.getValue(SPLINK_WF_KEY) != null) {
							Long oldCFProd = (Long) spLink.getValue(SPLINK_CF_KEY);
							Long oldWFProd = (Long) spLink.getValue(SPLINK_WF_KEY);

							Long newCFSKU = (Long) ssLink.getValue(SSLINK_CF_KEY);
							Long newWFSKU = (Long) ssLink.getValue(SSLINK_WF_KEY);

							if (oldCFProd.equals(newCFSKU)) {
								LOGGER.info(">>> Clearance Forecast matches!! >>>");
							} else {
								updateForecast = true;
								spLink.setValue(SPLINK_CF_KEY, ssLink.getValue(SSLINK_CF_KEY));
							}

							if (oldWFProd.equals(newWFSKU)) {
								LOGGER.info(">>> Wholesale Forecast matches! >>>!");
							} else {
								updateForecast = true;
								spLink.setValue(SPLINK_WF_KEY, ssLink.getValue(SSLINK_WF_KEY));
							}
						} else {
							updateForecast = true;
							spLink.setValue(SPLINK_CF_KEY, ssLink.getValue(SSLINK_CF_KEY));
							spLink.setValue(SPLINK_WF_KEY, ssLink.getValue(SSLINK_WF_KEY));

						}

						if (updateForecast) {
							LCSProductLogic.persist(spLink, true);
							LOGGER.info(">>> Forecast updated!! >>>");
						}

					}
				}
			}
		}	
		LOGGER.debug("## End method - setForecastValue ###");
	}
}