package com.agron.wc.integration.util;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.*;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.util.StringUtils;
import wt.util.WTException;

import com.agron.wc.integration.outbound.AgronExportUtil;
import com.agron.wc.integration.util.AgronCSVGenerator;

/**
 * @author Mallikarjuna Savi
 *
 */
public class AgronDataHelper {

	/**
	 * Defined to store the constant value.
	 */
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");
	private static final String LATEST_SEASON = LCSProperties.get("com.agron.wc.interface.season.latestSeason");
	private static final String SEASONTYPE_KEY = LCSProperties.get("com.agron.wc.interface.season.seasonTypeKey");
	private static final String YEAR_KEY = LCSProperties.get("com.agron.wc.interface.season.yearKey");
	private static final String CATEGORY_KEY = LCSProperties.get("com.agron.wc.interface.product.categoryKey");
	private static final String ERROR_LBL = "Error : ";
	private static final String SMU_CLEARENCE = LCSProperties.get("com.agron.wc.interface.smuClearence",
			"SMU & Clearance");
	private Collection<LCSSeason> seasons = new ArrayList<LCSSeason>();

	/**
	 * This method is used to get the latest season of the product.
	 * 
	 * @param product is LCSProduct object
	 * @return LCSSeason type
	 */

	public LCSSeason getLatestSeason(LCSProduct product, Collection<?> seasonsCol) {
		LCSSeason season = null;
		Iterator<?> seasonIter = null;
		LCSSeason latestSeason = null;
		int latestYear = 0;
		int year = 0;
		LCSSeasonProductLink spl = null;
		seasons = new ArrayList<LCSSeason>();
		try {
			seasonIter = seasonsCol.iterator();
			if (seasonsCol.size() > 1) {
				latestYear = getLatestYear(seasonsCol);
				while (seasonIter.hasNext()) {
					season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
					// SKIP this season if Downstream Integration is disabled.
					String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
					if("agrNo".equals(isagrDownstreamIntegrated)) {
						LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
						continue;
					}
					LOGGER.info((new StringBuilder("season : ")).append(season).toString());
					spl = LCSSeasonQuery.findSeasonProductLink(product, season);
					if (!spl.isSeasonRemoved()) {
						year = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
						seasons.add(season);
						if (latestYear == year)
							if (LATEST_SEASON.equals((String) season.getValue(SEASONTYPE_KEY)))
								latestSeason = season;
							else
								latestSeason = season;
					}
				}
			} else {
				latestSeason = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
				if("agrYes".equals((String) latestSeason.getValue("agrDownstreamIntegrated"))) {
					seasons.add(latestSeason);
				}else {
					latestSeason = null;
				}
				
				
				
			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.getLocalizedMessage());
		}
		return latestSeason;
	}

	/**
	 * This method is used to get the latest season of the sku.
	 * 
	 * @param sku is LCSSKU object
	 * @return LCSSeason type
	 */
	public LCSSeason getLatestSkuSeason(LCSSKU sku, Collection<?> seasonsCol) {
		LCSSeason season = null;
		Iterator<?> seasonIter = null;
		LCSSeason latestSeason = null;
		int latestYear = 0;
		int year = 0;
		LCSSeasonProductLink spl = null;
		try {
			seasonIter = seasonsCol.iterator();
			if (seasonsCol.size() > 1) {
				latestYear = getLatestYear(seasonsCol);
				if(latestYear == 2021)
				{
					latestSeason = findLatestGenericSeason(sku,seasonsCol,"agrFallWinter");
					if(latestSeason==null)
					latestSeason = findLatestGenericSeason(sku,seasonsCol,"agrSpringSummer");
					LOGGER.info("getLatestSkuSeason=============latest year"+latestYear);
					//LOGGER.info("getLatestSkuSeason=============latestseason anme"+latestSeason.getName());
				}
				
				
				if(latestSeason == null)
				{
				while (seasonIter.hasNext()) {
					season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
					// SKIP this season if Downstream Integration is disabled.
					String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
					if("agrNo".equals(isagrDownstreamIntegrated)) {
						LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
						continue;
					}
					LOGGER.info((new StringBuilder("season : ")).append(season).toString());
					spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
					if (spl.isSeasonRemoved())
						continue;
					year = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
					if (latestYear != year)
						continue;
					
					if (LATEST_SEASON.equals((String) season.getValue(SEASONTYPE_KEY))
							|| "agrWinter".equals((String) season.getValue(SEASONTYPE_KEY))) {
						latestSeason = season;
						break;
					}
					if ("agrFall".equals((String) season.getValue(SEASONTYPE_KEY)))
						latestSeason = season;
					else if ("agrSummer".equals((String) season.getValue(SEASONTYPE_KEY)))
						latestSeason = season;
					else
						latestSeason = season;
				}
			}
			} else {
				latestSeason = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
				if("agrYes".equals((String) latestSeason.getValue("agrDownstreamIntegrated"))) {
					seasons.add(latestSeason);
					}else {
						latestSeason = null;
					}
			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.getLocalizedMessage());
		}
		return latestSeason;
	}
	
	public LCSSeason findLatestGenericSeason(LCSSKU sku,Collection<?> seasons,String seasonType) {
		LOGGER.info("findLatestGenericSeason=============Start");
		LCSSeason season = null;
		LCSSeason latestSeason = null;
		Iterator<?> seasonIter = null;
		seasonIter = seasons.iterator();
		int year = 0;
		LCSSeasonProductLink spl = null;
		try {
		while (seasonIter.hasNext()) {
			season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
			// SKIP this season if Downstream Integration is disabled.
			String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
			if("agrNo".equals(isagrDownstreamIntegrated)) {
				LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
				continue;
			}
			spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
			if (spl.isSeasonRemoved())
				continue;
			year = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
			if (2021 != year)
				continue;
			if (seasonType.equals((String) season.getValue(SEASONTYPE_KEY)) && AgronExportUtil.isAgronSeasonType(season)
					) {
				LOGGER.info("find2021FallWinterSeasonExist=============insidecondition"+season.getName());
				latestSeason = season;
				break;
			}
			
			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.getLocalizedMessage());
		}
		return latestSeason;
		
	}

	/**
	 * @param product
	 * @param seasonsCol
	 * @return
	 * Need to Remove once Testing is over for flex import
	 */
	public Collection<LCSSeasonMaster> removedSeasonRemovedSeasons(LCSProduct product, Collection<?> seasonsCol) {
		LCSSeason season = null;
		LCSSeasonProductLink spl = null;
		LCSSeasonMaster master = null;
		Collection<LCSSeasonMaster> seasonCollectionLatest = new ArrayList<LCSSeasonMaster>();
		try {
			if (seasonsCol.size() > 0) {
				for (Iterator<?> seasonIter = seasonsCol.iterator(); seasonIter.hasNext();) {
					master = (LCSSeasonMaster) seasonIter.next();
					season = (LCSSeason) VersionHelper.latestIterationOf(master);
					// SKIP this season if Downstream Integration is disabled.
					String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
					if("agrNo".equals(isagrDownstreamIntegrated)) {
						LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
						continue;
					}
					spl = LCSSeasonQuery.findSeasonProductLink(product, season);
					if ((!spl.isSeasonRemoved()) && season.isActive())
						seasonCollectionLatest.add(master);
				}

			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.getLocalizedMessage());
		}
		return seasonCollectionLatest;
	}
	
	/**
	 * This method is used to get the latest year of the seasons of the product.
	 * 
	 * @param seasons Collection object
	 * @return Integer type
	 */
	public int getLatestYear(Collection<?> seasons) {
		int currentYear = 0;
		int yr = 0;
		LCSSeason season = null;
		try {
			Iterator<?> iter = seasons.iterator();
			season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) iter.next());
			// SKIP this season if Downstream Integration is disabled.
			String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
			if("agrYes".equals(isagrDownstreamIntegrated)) {
				yr = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
			}
			
			currentYear = yr;
			while (iter.hasNext()) {
				season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) iter.next());
				// SKIP this season if Downstream Integration is disabled.
				
				if("agrNo".equals((String) season.getValue("agrDownstreamIntegrated"))) {
					LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
					continue;
				}
				yr = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
				if (currentYear < yr)
					currentYear = yr;
			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.fillInStackTrace());
		}
		return currentYear;
	}

	/**
	 * This method is used to get the formatted season name.
	 * 
	 * @return String type
	 */
	public String getFormattedSeasonName() {
		String seasonName = "";
		LCSSeason season = null;
		List<String> seasonNameCol = new ArrayList<String>();
		int year = 0;
		String seasonValue = "";
		try {
			LOGGER.info((new StringBuilder("*********** : ")).append(seasons).append(" ***********").toString());
			for (Iterator<LCSSeason> seasonIter = seasons.iterator(); seasonIter.hasNext(); seasonNameCol
					.add(seasonName)) {
				season = (LCSSeason) seasonIter.next();
				year = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
				year %= 2000;
				seasonName = String.valueOf(year);
				seasonValue = LCSProperties.get(
						(new StringBuilder("com.agron.wc.season.")).append(season.getValue(SEASONTYPE_KEY)).toString());
				seasonName = (new StringBuilder(String.valueOf(seasonName))).append(seasonValue).toString();
				LOGGER.info((new StringBuilder("seasonName : ")).append(seasonName).toString());
			}

			Collections.sort(seasonNameCol);
			LOGGER.info((new StringBuilder("seasonNameCol : ")).append(seasonNameCol).toString());
			seasonName = StringUtils.arrayToCommaDelimitedString(seasonNameCol.toArray());
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.getLocalizedMessage());
		}
		return seasonName;
	}

	/**
	 * This method is used to get the current time stamp.
	 * 
	 * @return String type
	 */
	public static String getCurrentTimeStamp() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
		Date now = new Date();
		return sdfDate.format(now);
	}

	/**
	 * This method is used to get the seasons.
	 * 
	 * @return Collection type
	 */
	public Collection<LCSSeason> getSeasons() {
		return this.seasons;
	}

	/**
	 * This method is used to get the formatted product type.
	 * 
	 * @param product is LCSProduct object
	 * @param keyPair is Map object
	 * @return String type
	 */
	public String getFormatedProductType(LCSProduct product, Map<?, ?> keyPair) {
		String productType = null;
		String productFlexType = null;
		String category = null;
		try {
			productFlexType = product.getFlexType().getTypeDisplayName();
			if (SMU_CLEARENCE.equals(productFlexType)) {
				category = (String) product.getValue(CATEGORY_KEY);
				productType = (String) keyPair.get(category);
				LOGGER.info((new StringBuilder("In  SMU Product Type : ")).append(productType).toString());
			} else {
				productType = (String) keyPair.get(productFlexType);
			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.getLocalizedMessage());
		}
		return productType;
	}
	
	
	public LCSSeason getLatestFromSeasonColl(LCSProduct product, Collection<?> seasonsCol) {
		LCSSeason season = null;
		Iterator<?> seasonIter = null;
		LCSSeason latestSeason = null;
		int latestYear = 0;
		int year = 0;
		LCSSeasonProductLink spl = null;
		
		try {
			seasonIter = seasonsCol.iterator();
			if (seasonsCol.size() > 1) {
				latestYear = getLatestYearFromSeasonColl(seasonsCol);
				while (seasonIter.hasNext()) {
					season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeason) seasonIter.next());
					// SKIP this season if Downstream Integration is disabled.
					String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
					if("agrNo".equals(isagrDownstreamIntegrated)) {
						LOGGER.info("getLatestFromSeasonColl -- isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
						continue;
					}
					spl = LCSSeasonQuery.findSeasonProductLink(product, season);
					if (!spl.isSeasonRemoved()) {
						year = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
						if (latestYear == year) {
							if (LATEST_SEASON.equals((String) season.getValue(SEASONTYPE_KEY))){
								latestSeason = season;
								break;
							}else{
								latestSeason = season;

							}
						}
					}
				}
			} else {
				latestSeason = (LCSSeason) VersionHelper.latestIterationOf((LCSSeasonMaster) seasonIter.next());
				if("agrNo".equals((String) latestSeason.getValue("agrDownstreamIntegrated"))) {
					latestSeason = null;
					}
			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.getLocalizedMessage());
		}
		if(latestSeason != null) {
			LOGGER.info(latestSeason+">getLatestFromSeasonColl::::"+latestSeason.getName());
		}
		return latestSeason;
	}
	
	public int getLatestYearFromSeasonColl(Collection<?> seasons) {
		int currentYear = 0;
		int yr = 0;
		LCSSeason season = null;
		try {
			Iterator<?> iter = seasons.iterator();
			season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeason) iter.next());
			yr = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
			currentYear = yr;
			while (iter.hasNext()) {
				season = (LCSSeason) VersionHelper.latestIterationOf((LCSSeason) iter.next());
				yr = Integer.parseInt(AgronCSVGenerator.getData(season, YEAR_KEY, false));
				if (currentYear < yr)
					currentYear = yr;
			}
		} catch (WTException e) {
			LOGGER.error(ERROR_LBL + e.fillInStackTrace());
		}
		LOGGER.info("LatestYear::::"+currentYear);
		return currentYear;
	}
	
}
