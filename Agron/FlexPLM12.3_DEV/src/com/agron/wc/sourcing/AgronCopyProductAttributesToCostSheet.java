package com.agron.wc.sourcing;

import com.agron.wc.util.AgronCostingPropertiesPluginHelper;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.sourcing.LCSSKUCostSheet;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * SSP for Copy Product Attriute to Costsheet  .
 * @author Pawan Prajapat
 * move code FLexPLM upgrade to 11.1. Fox release 
 * April 2020 
 */
public class AgronCopyProductAttributesToCostSheet {
	/** WHOLESALE_PRICE_DOLLAR field */
	public static final String WHOLESALE_PRICE_DOLLAR = LCSProperties.get("com.agron.wc.sourcing.CostSheet.WholesalePrice.attKey");
	/** RETAIL_PRICE_DOLLAR field */
	public static final String RETAIL_PRICE_DOLLAR = LCSProperties.get("com.agron.wc.sourcing.CostSheet.RetailPrice.attKey");
	/** RETAILPRICE_OVERRIDE field */
	private static final String RETAILPRICE_OVERRIDE = LCSProperties.get("com.agron.wc.sourcing.CostSheet.RetailPriceOverride.attKey");
	/** DISCOUNT_DOLLAR field */
	public static final String DISCOUNT_DOLLAR = LCSProperties.get("com.agron.wc.sourcing.CostSheet.DiscountDollar.attKey");
	/** IMU_PERCENT field */
	public static final String IMU_PERCENT = LCSProperties.get("com.agron.wc.sourcing.CostSheet.imuPercent.attKey");
	/** NET_WHOLESALE_BOOLEAN_CHECK field */
	public static final String NET_WHOLESALE_BOOLEAN_CHECK = LCSProperties.get("com.agron.wc.sourcing.CostSheet.NetWholeSaleBasedOnIMU.attKey");
	/** NET_WHOLESALE_DOLLAR_KEY field */
	public static final String NET_WHOLESALE_DOLLAR_KEY = LCSProperties.get("com.agron.wc.sourcing.CostSheet.NetWholeSaleDollar.attKey");
	/** LOGGER field */
	public static final Logger logger = LogManager.getLogger(AgronCopyProductAttributesToCostSheet.class);
	
	public static final WTObject copyProdAttrToCostSheet(WTObject wtObject) {
		try {
			
			if (wtObject instanceof LCSSKUCostSheet) {
				return wtObject; 
			}
			
			logger.debug(">>>>>> CopyProductAttributesToCostSheet.copyProdAttrToCostSheet: Start " + wtObject);
			LCSCostSheet localLCSCostSheet = null;
			if (!(wtObject instanceof LCSCostSheet)) {
				throw new WTException(">>> copyProdAttrToCostSheet: Object is not instance of LCSCostSheet");
			}

			localLCSCostSheet = (LCSCostSheet) wtObject;
			LCSProduct localLCSProduct = null;
			if(localLCSCostSheet.getProductMaster()!= null && localLCSCostSheet.getSeasonMaster()!= null)
			{
				localLCSProduct = (LCSProduct) VersionHelper.getVersion(localLCSCostSheet.getProductMaster(), "A");
				LCSSeason localLCSSeason = null;
				LCSProductSeasonLink localLCSSeasonProductLink = null;
				localLCSSeason = VersionHelper.latestIterationOf(localLCSCostSheet.getSeasonMaster());

				localLCSSeasonProductLink = (LCSProductSeasonLink) LCSSeasonQuery.findSeasonProductLink(localLCSProduct, localLCSSeason);
				if (localLCSSeasonProductLink != null) {
					Collection attLists = AgronCostingPropertiesPluginHelper
							.getAllPropertySettings("com.vrd.costsheet.CopyProductAttributesToCostSheet");
					if (attLists.size() > 0) {
						Object[] list = attLists.toArray();
						String attList = "";
						String costAttKey = "";
						String prodAttKey = "";

						for (int i = list.length; i > 0; --i) {
							attList = (String) list[i - 1];
							costAttKey = attList.substring(attList.lastIndexOf(44) + 1, attList.length());
							prodAttKey = attList.substring(0, attList.indexOf(44));
							logger.debug("costAttKey:::"+costAttKey+"prodAttKey:::"+prodAttKey);
							if (FormatHelper.hasContent(prodAttKey)) {
								if(localLCSSeasonProductLink.getValue(prodAttKey) != null) {
									localLCSCostSheet.setValue(costAttKey, localLCSSeasonProductLink.getValue(prodAttKey));
								}
							}
						}
					}
				}

				logger.debug(">>>>>> CopyProductAttributesToCostSheet.copyProdAttrToCostSheet: Finish ");
			}	
		} catch (WTPropertyVetoException PropertyVetoException) {
			PropertyVetoException.printStackTrace();
		} catch (WTException Exception) {
			Exception.printStackTrace();
		}

		return wtObject;
	}
	

}
