package com.agron.wc.sourcing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSSKUCostSheet;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.LCSSourcingConfigQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;



public class AgronExtendedCostPlugin {
	
	
private static final Logger LOGGER = LogManager.getLogger(AgronExtendedCostPlugin.class);

public static final String SSLINK_FORECAST_QTY_SKU_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.forecastedQtySkuKey","agrForecastedQtySKU");
public static final String SSLINK_EXT_AVG_NETWS_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.extNetWSLessAllowanceAvgKey","agrExtNetWSLessAllowanceAvg");
public static final String SSLINK_EXT_TOTAL_COST_AVG_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.extTotalCostAvg","agrExtTotalCostAvg");
public static final String SSLINK_ADIDAS_NET_PRICE_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agradidasNetPrice","agradidasNetPrice");

public static final String COSTSHEET_TOTAL_COST_AVG_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrTotalCostAvg","agrTotalCostAvg");
public static final String COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrNetWSLessAllowanceAvg","agrNetWSLessAllowanceAvg");
public static final String COSTSHEET_LANDED_COST_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrLandedCost","agrLandedCost");
//Trello 434 
public static final String SSLINK_EXT_FOB = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrExtFOB","agrExtFOB");
public static final String SSLINK_TOTAL_EXT_FORECAST = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrTotalExtForecast","agrTotalExtForecast");
public static final String COSTSHEET_BASE_FOB_KEY = LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrBaseFOB","agrBaseFOB");
public static final String SSLINK_CLEARANCE_FORECAST_KEY=LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrClearanceForecast","agrClearanceForecast");
public static final String SSLINK_CLEARANCE_SELLPRICE_KEY=LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrClearanceSellPrice","agrClearanceSellPrice");
public static final String SSLINK_CLEARANCE_EXT_WHOLESALE=LCSProperties.get("com.agron.wc.sourcing.AgronExtendedCostPlugin.agrClearanceExtWholesale","agrClearanceExtWholesale");

public static void calculateExtednedCostByCostSheet(WTObject obj) throws Exception {
	LOGGER.info("calculateExtednedCostByCostSheet START>>>>"+obj);
	LCSCostSheet costSheetObj = (LCSCostSheet) obj;	
	if(obj == null || obj instanceof LCSSKUCostSheet) {
		return;
	}
	LCSSourcingConfigMaster sourcingConfigMaster = costSheetObj.getSourcingConfigMaster();
	if (sourcingConfigMaster == null) {
		LOGGER.debug("sourcingConfigMaster is null hence return");
		return;
	}
	LCSSeasonMaster seasonMaster = costSheetObj.getSeasonMaster();
	if(seasonMaster== null) {
		LOGGER.debug("seasonMaster is null hence return");
		return;
	}
	ArrayList<String> applicableColorwayNames = new ArrayList<String>(MOAHelper.getMOACollection(costSheetObj.getApplicableColorNames()));
	  ArrayList<String> applicableSizes = new ArrayList<String>(MOAHelper.getMOACollection(costSheetObj.getApplicableSizes()));
	   
	LOGGER.debug("applicableColorwayNames>>"+applicableColorwayNames+"::applicableSizes>>"+applicableSizes);
	if(applicableColorwayNames.isEmpty() || applicableSizes.isEmpty()) {
		LOGGER.debug("Costsheet is not associated with Colorway/sizes hence return");
		return;
	}	
	Double  agrTotalCostAvg  = ( (Double)costSheetObj.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) != null ? (Double)costSheetObj.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) :0.00);
	Double  agrLandedCost  = ( (Double)costSheetObj.getValue(COSTSHEET_LANDED_COST_KEY) != null ? (Double)costSheetObj.getValue(COSTSHEET_LANDED_COST_KEY) :0.00);
	Double  agrNetWholesaleDollarAvg  = ((Double)costSheetObj.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) != null ? (Double)costSheetObj.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) :0.00);
	LOGGER.debug("agrTotalCostAvg>>>"+agrTotalCostAvg+"::agrNetWholesaleDollarAvg>>>"+agrNetWholesaleDollarAvg);
	Double  agrBaseFOB=(  (Double)costSheetObj.getValue(COSTSHEET_BASE_FOB_KEY) != null ? (Double)costSheetObj.getValue(COSTSHEET_BASE_FOB_KEY) :0.00);
	
	
	//Trigger the plugin logic only if value of TotalCostAvg,rNetWholesaleDollarAvg  and applicableColorwayNames is changed. As of todays formuls landed cost will also change if Base FOB changes. If Landed cost changes , Total Cost AVg will also change.
	LCSCostSheet predCostSheet= (LCSCostSheet) VersionHelper.predecessorOf(costSheetObj);
	if(predCostSheet !=null) {
		Double  predAgrTotalCostAvg  = ((Double)predCostSheet.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) != null ? (Double)predCostSheet.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) :0.00);
		Double  predAgrNetWholesaleDollarAvg  = ((Double)predCostSheet.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) != null ? (Double)predCostSheet.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) :0.00);
		LOGGER.debug("predAgrTotalCostAvg::predAgrNetWholesaleDollarAvg>>>"+predAgrTotalCostAvg+"::::"+predAgrNetWholesaleDollarAvg);
		if(Double.compare(predAgrTotalCostAvg, agrTotalCostAvg)==0 && Double.compare(predAgrNetWholesaleDollarAvg, agrNetWholesaleDollarAvg)==0 && costSheetObj.getApplicableColorNames().equals(predCostSheet.getApplicableColorNames()) && costSheetObj.getApplicableSizes().equals(predCostSheet.getApplicableSizes())) {
			LOGGER.debug("No change is relevent costsheet attribute hence retuen");
			return;
		}
	}
	LCSSourcingConfig sourcingConfigObj = (LCSSourcingConfig) VersionHelper.latestIterationOf(sourcingConfigMaster);
	LCSSeason season = VersionHelper.latestIterationOf(seasonMaster);
	LOGGER.info("sourcingConfigObj::"+sourcingConfigObj.getName() +"::"+sourcingConfigObj +"::Season>> "+season.getName());
	LCSSourceToSeasonLink ssLink=(new LCSSourcingConfigQuery()).getSourceToSeasonLink(sourcingConfigObj, season);
	LOGGER.debug("ssLink>>>>"+ssLink);
	if(ssLink !=null) {
		LOGGER.debug("ssLink isPrimary>>>>"+ssLink.isPrimarySTSL());
		if(ssLink.isPrimarySTSL()) {
			LCSProduct product =  (LCSProduct) VersionHelper.latestIterationOf((LCSProduct) VersionHelper.getVersion(costSheetObj.getProductMaster(), "A"));
			LOGGER.debug("product>>>"+product+"::"+product.getName());
			String baseSize=getBaseSize(product,season);
		    if(!applicableSizes.contains(baseSize)) {
			 return;	 
			 }
			HashMap<String,LCSSKU> skuMap=getProductSKUMap(product);
			LOGGER.debug("skuMap>>>"+skuMap);
			if(skuMap !=null &&  skuMap.size()>0) {
				Iterator colorIt=applicableColorwayNames.iterator();
				while(colorIt.hasNext() ) { 
					String skuName=(String)colorIt.next();
					if(skuMap.containsKey(skuName)) {
						LCSSKU sku=skuMap.get(skuName);
						LOGGER.debug("SKU Name>>>"+sku.getName());
						LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
						LOGGER.debug("spl : "+spl);
						if(spl !=null && !spl.isSeasonRemoved()) {
							//LOGGER.info("SSLINK_FORECAST_QTY_SKU_KEY-----"+spl.getValue(SSLINK_FORECAST_QTY_SKU_KEY));
							if(spl.getValue(SSLINK_FORECAST_QTY_SKU_KEY)!=null)
							{
							long forecastedQtySKU=(long)spl.getValue(SSLINK_FORECAST_QTY_SKU_KEY);
							BigDecimal agrNetWholesaleDollarAvgBD = new BigDecimal(agrNetWholesaleDollarAvg.toString()).setScale(2, RoundingMode.HALF_UP);
							Double extAverageNetWS  = agrNetWholesaleDollarAvgBD.doubleValue()*forecastedQtySKU;
							BigDecimal agrTotalCostAvgBD = new BigDecimal(agrTotalCostAvg.toString()).setScale(2, RoundingMode.HALF_UP);
							
							//Trello 434 - Pull Ext. FOB on Colorway Season.
							long agrClearanceForecast= (  (long)spl.getValue(SSLINK_CLEARANCE_FORECAST_KEY));
							Double totalExtForecast  = (Double)spl.getValue(SSLINK_TOTAL_EXT_FORECAST);Double extTotalCostAvg= agrTotalCostAvgBD.doubleValue()*forecastedQtySKU;
							LOGGER.info("forecastedQtySKU>>"+forecastedQtySKU+"::extTotalCostAvg>>>"+extTotalCostAvg+"::extAverageNetWS>>>"+extAverageNetWS+"::totalExtForecast>>>"+totalExtForecast);
							//Trello 434
							Double extFOB=agrBaseFOB.doubleValue()*((forecastedQtySKU)+(agrClearanceForecast));
							LOGGER.debug("extFOB on costsheet update : "+spl);
							spl.setValue(SSLINK_EXT_AVG_NETWS_KEY, extAverageNetWS);
							spl.setValue(SSLINK_EXT_TOTAL_COST_AVG_KEY, extTotalCostAvg);
							spl.setValue(SSLINK_ADIDAS_NET_PRICE_KEY, (agrLandedCost/0.8)); 
							spl.setValue(SSLINK_EXT_FOB, extFOB);
							PersistenceServerHelper.manager.update(spl);
						}else {
							spl.setValue(SSLINK_ADIDAS_NET_PRICE_KEY, (agrLandedCost/0.8)); 
							PersistenceServerHelper.manager.update(spl);
						}
						}
					}
				}	
			}	
		}	
	}	
	LOGGER.info("calculateExtednedCostByCostSheet END" );
}

public static void calculateExtednedCostBySSLink(WTObject object) throws Exception {
	if (object instanceof LCSSKUSeasonLink) {
		LCSSKUSeasonLink ssLink = (LCSSKUSeasonLink)object;
		if(ssLink.getSeasonLinkType().equalsIgnoreCase("SKU") && ssLink.isEffectLatest() && !ssLink.isSeasonRemoved()){
			LOGGER.info("calculateExtednedCostBySSLink START>>>"+ssLink +"::EffectiveSequence>>"+ssLink.getEffectSequence());
			if(ssLink.getValue(SSLINK_FORECAST_QTY_SKU_KEY)==null){	
				LOGGER.info("forecastedQtySKU is null hence return");
				return;
			}
			long forecastedQtySKU  = (long)ssLink.getValue(SSLINK_FORECAST_QTY_SKU_KEY);
			
			/*Block comment added by Sandeep - This check is not required now. Qty should be re-computed irrespective.
			 *  if(ssLink.getEffectSequence()>1 ) {
				LCSSeasonProductLink priorSPLLink= LCSSeasonQuery.getPriorSeasonProductLink(ssLink);
				LOGGER.debug("forecastedQtySKU>>>>>"+forecastedQtySKU +"::priorSPLLink>>"+priorSPLLink);
				if(priorSPLLink !=null) {
					if(priorSPLLink.getValue(SSLINK_FORECAST_QTY_SKU_KEY)!=null)
					{	
					long priorForecastedQtySKU=(long) priorSPLLink.getValue(SSLINK_FORECAST_QTY_SKU_KEY);
					if(forecastedQtySKU ==priorForecastedQtySKU) {
						System.out.println("No Change in Forecasted Qty hence return::Values>>"+forecastedQtySKU+"::"+priorForecastedQtySKU);
						return;	
					}
				}
				}
			} */

			LCSSeason season = SeasonProductLocator.getSeasonRev(ssLink);
			LCSProduct product = SeasonProductLocator.getProductARev(ssLink);
			LCSSKU sku = SeasonProductLocator.getSKUARev(ssLink);
			String skuName=(String)sku.getValue("skuName");
			LCSSourceToSeasonLink stsl=  LCSSourcingConfigQuery.getPrimarySourceToSeasonLink(product.getMaster(), season.getMaster());
			System.out.println("stsl:::>>"+stsl);
			if(stsl !=null) {
				 String baseSize=getBaseSize(product,season);
				 if(!FormatHelper.hasContent(baseSize)) {
					LOGGER.info("base Size is  null hence return");
					return;
				 }
				Collection costSheetCollection=  LCSCostSheetQuery.getCostSheetsForSourceToSeason(stsl, null,false);
				LOGGER.debug("costSheets:::"+costSheetCollection.size()+":::"+costSheetCollection);
				Iterator costSheetsIter = costSheetCollection.iterator();
				while(costSheetsIter.hasNext() ) {  
					LCSCostSheet costSheet = (LCSCostSheet) costSheetsIter.next();
					costSheet = (LCSCostSheet) VersionHelper.latestIterationOf(costSheet);
					ArrayList<String> applicableSizes = new ArrayList<String>(MOAHelper.getMOACollection(costSheet.getApplicableSizes()));
					LOGGER.info("costSheet>>>"+costSheet+"::Name>>"+costSheet.getName() +"::TYPE>>"+costSheet.getCostSheetType());
					if("PRODUCT".equalsIgnoreCase(costSheet.getCostSheetType())) {
						ArrayList<String> applicableColorNames = new ArrayList<String>(MOAHelper.getMOACollection(costSheet.getApplicableColorNames()));
						LOGGER.debug("applicableCOlorNames>>>"+applicableColorNames);	
						if(!applicableColorNames.isEmpty() && applicableColorNames.contains(skuName)  && applicableSizes.contains(baseSize) ) {
							Double  agrTotalCostAvg  = ( (Double)costSheet.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) != null ? (Double)costSheet.getValue(COSTSHEET_TOTAL_COST_AVG_KEY) :0.00);
							Double  agrNetWholesaleDollarAvg  = (  (Double)costSheet.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) != null ? (Double)costSheet.getValue(COSTSHEET_NET_WHOLESALE_DOLLA_AVG_KEY) :0.00);
								//Trello 434
								long agrClearanceForecast= (  (long)ssLink.getValue(SSLINK_CLEARANCE_FORECAST_KEY));
								Double agrClearanceSellPrice= (  (Double)ssLink.getValue(SSLINK_CLEARANCE_SELLPRICE_KEY) != null ? (Double)ssLink.getValue(SSLINK_CLEARANCE_SELLPRICE_KEY) :0.00);
								Double  agrBaseFOB=(  (Double)costSheet.getValue(COSTSHEET_BASE_FOB_KEY) != null ? (Double)costSheet.getValue(COSTSHEET_BASE_FOB_KEY) :0.00);
								LOGGER.debug("COSTSHEET ::agrTotalCostAvg>>>"+agrTotalCostAvg+"::agrNetWholesaleDollarAvg>>"+agrNetWholesaleDollarAvg);
								LOGGER.debug("agrClearanceForecast>>>>"+agrClearanceForecast+"::agrClearanceSellPrice>>>>>"+agrClearanceSellPrice);

								BigDecimal agrNetWholesaleDollarAvgBD = new BigDecimal(agrNetWholesaleDollarAvg.toString()).setScale(2, RoundingMode.HALF_UP);
								Double extAverageNetWS  = agrNetWholesaleDollarAvgBD.doubleValue()*forecastedQtySKU;
								/*Trello 434 - Pull Ext. FOB on Colorway Season. 
								 * Moved Total Ext Forecast and ClearanceExtWholesale type manger formula calculation to code
								 * to get total ext forecast value on pre derive
								 */
								Double clearanceExtWholesale  = agrClearanceForecast*agrClearanceSellPrice;
								LOGGER.debug(":::clearanceExtWholesale at Colorway Season update::::"+clearanceExtWholesale);
								Double totalExtForecast  = extAverageNetWS+clearanceExtWholesale;
								LOGGER.debug(":::totalExtForecast at Colorway Season update::::"+totalExtForecast);
								Double extFOB=agrBaseFOB.doubleValue()*((forecastedQtySKU)+(agrClearanceForecast));
										//agrBaseFOB.doubleValue()*totalExtForecast;
								LOGGER.debug("EXT.FOB>>>>"+extFOB);

								BigDecimal agrTotalCostAvgBD = new BigDecimal(agrTotalCostAvg.toString()).setScale(2, RoundingMode.HALF_UP);
								Double extTotalCostAvg= agrTotalCostAvgBD.doubleValue()*forecastedQtySKU;
								LOGGER.debug("extTotalCostAvg>>>"+extTotalCostAvg+"::extAverageNetWS>>>"+extAverageNetWS);
								ssLink.setValue(SSLINK_EXT_AVG_NETWS_KEY, extAverageNetWS);
								ssLink.setValue(SSLINK_EXT_TOTAL_COST_AVG_KEY, extTotalCostAvg);
								//Trello 434
								ssLink.setValue(SSLINK_CLEARANCE_EXT_WHOLESALE, clearanceExtWholesale);
								ssLink.setValue(SSLINK_TOTAL_EXT_FORECAST, totalExtForecast);
								ssLink.setValue(SSLINK_EXT_FOB, extFOB);

								break;
							}
						}	
					}	
				}
				LOGGER.debug("setExtAverageValueSPL END>>>"+ssLink);
			}
		}

}
	


public static HashMap<String,LCSSKU> getProductSKUMap(LCSProduct product) {
	HashMap<String,LCSSKU> skuMap=new HashMap();
	try {
		Collection skuList = LCSSKUQuery.findSKUs(product, false);
		LOGGER.info("skuList>>>"+skuList.size()+"::"+skuList);
	    Iterator skuIter = skuList.iterator();
	    while(skuIter.hasNext()) {
	    	 LCSSKU sku = (LCSSKU) VersionHelper.latestIterationOf((LCSSKU) VersionHelper.getVersion( ((LCSSKU) skuIter.next()).getMaster(),"A"));
			 skuMap.put((String)sku.getValue("skuName"), sku);
	    }
	} catch (WTException e) {
		e.printStackTrace();
	}
	return skuMap;
}


	public static String getBaseSize(LCSProduct product,LCSSeason season) {
		String baseSize="";
		try {
			SearchResults sr = (new SizingQuery()).findPSDByProductAndSeason((Map) null, product, season,
					(FlexType) null, new Vector(), new Vector());
			LOGGER.debug("findPSDByProductAndSeason:::"+sr.getResultsFound()+"::"+sr);
			Iterator psctsIterator = LCSQuery.iterateObjectsFromResults(sr,"OR:com.lcs.wc.sizing.ProdSizeCategoryToSeason:", "PRODSIZECATEGORYTOSEASON.IDA2A2");
			if(psctsIterator.hasNext()) {
				ProdSizeCategoryToSeason pscts = (ProdSizeCategoryToSeason) psctsIterator.next();
				ProductSizeCategory psc = (ProductSizeCategory) VersionHelper.latestIterationOf(pscts.getSizeCategoryMaster());
				LOGGER.debug("psc>>>"+psc);
				baseSize=psc.getBaseSize();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		LOGGER.debug("baseSize>>"+baseSize);
		return baseSize;
	}
}