package com.agron.wc.sourcing;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSCostSheet;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.WTObject;
import wt.method.MethodContext;
import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author Pawan Prajapat
 * Class moved for FLexPLM upgrade to 11.1. Fox release 
 * April 2020
 */
public class AgronCostSheetNamePlugins {

	public static final String AGR_COSTSHEET_TYPE_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronCostSheetPlugins.Type");
	public static final String AGR_COSTSHEET_MARKETINGSCENARIO_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronCostSheetPlugins.MarketingScenario");
	public static final String AGR_COSTSHEET_NAME_KEY = LCSProperties
			.get("com.agron.wc.sourcing.AgronCostSheetPlugins.Name");

	public static final Logger logger = LogManager.getLogger(AgronCostSheetNamePlugins.class);

	public static void setCostSheetName(WTObject object) throws WTPropertyVetoException {
		try {
			LCSCostSheet costSheetObject = (LCSCostSheet) object;
			String colorwayLatterFromColorways = "";
			String getColorwayName = "";
			String productSizeName = "";
			String skuName = "";
			//String var7 = "";
			String nameOfColorway = "";
			String costsheetAttMarginScenario = "";
			String valueOfMarginScenario = "";
			int colorwayCount = 0;
			String costSheetType = (String) costSheetObject.getValue(AGR_COSTSHEET_TYPE_KEY);
			String marginScenario = (String) costSheetObject.getValue(AGR_COSTSHEET_MARKETINGSCENARIO_KEY);
			String allChosenSizes = costSheetObject.getApplicableSizes();
			logger.debug("All chosen sizes>>>>>>>>>>>>" + allChosenSizes);
			String allChosenColorways = costSheetObject.getApplicableColorNames();
			logger.debug("All Chosen colorways>>>>>>>>>>>>" + allChosenColorways);
			StringTokenizer colorwayStringToken = null;
			if (FormatHelper.hasContent(allChosenColorways)) {
				getColorwayName = allChosenColorways.replace("|~*~|", ",");

				colorwayStringToken = new StringTokenizer(getColorwayName, ",");
				colorwayCount = colorwayStringToken.countTokens();
				if (colorwayStringToken != null) {
					while (colorwayStringToken.hasMoreElements()) {
						nameOfColorway = colorwayStringToken.nextToken();
						String[] stringOfColorway = nameOfColorway.split("-");
						colorwayLatterFromColorways = colorwayLatterFromColorways + stringOfColorway[0];
					}
				}
			}

			String representativeSize2;
			if (colorwayStringToken != null && colorwayCount == 1) {
				skuName = nameOfColorway;
			} else {
				HashMap hashMap = (HashMap) MethodContext.getContext().get("COSTSHEET_DIM_LINKS");
				if (hashMap != null) {
					representativeSize2 = (String) hashMap.get("REPCOLOR");
					if (representativeSize2 != null && representativeSize2 != "" && FormatHelper.hasContent(representativeSize2)) {
						LCSSKU var18 = (LCSSKU) LCSQuery.findObjectById(representativeSize2);
						skuName = var18.getName();
					}
				}
			}

			logger.debug("Representative SKU Name >>>>>>>>>>>>>>>>" + skuName);
			String representativeSize1 = costSheetObject.getRepresentativeSize();
			logger.debug("Representative Size>>>>>>>>>>>>>>>>" + costSheetObject.getRepresentativeSize());
			representativeSize2 = costSheetObject.getRepresentativeSize2();
			logger.debug("Representative Size 2>>>>>>>>>>>>>>>>" + representativeSize2);
			if (FormatHelper.hasContent(allChosenSizes)) {
				productSizeName = allChosenSizes.replace("|~*~|", " ");
			}

			Locale var30 = WTContext.getContext().getLocale();
			FlexType getFlexType = costSheetObject.getFlexType();
			FlexTypeAttribute getflexTypeAtt = getFlexType.getAttribute(AGR_COSTSHEET_TYPE_KEY);

			if (FormatHelper.hasContent(costSheetType)) {
				costsheetAttMarginScenario = getflexTypeAtt.getAttValueList().getValue(costSheetType, var30);

			}

			FlexType flexType = costSheetObject.getFlexType();
			FlexTypeAttribute flexTypeAtt = flexType.getAttribute(AGR_COSTSHEET_MARKETINGSCENARIO_KEY);
			if (FormatHelper.hasContent(marginScenario)) {
				valueOfMarginScenario = flexTypeAtt.getAttValueList().getValue(marginScenario, var30);
			}

			StringBuffer deriveCostsheetName = new StringBuffer();
			deriveCostsheetName.append(costsheetAttMarginScenario);
			deriveCostsheetName.append(" ");
			deriveCostsheetName.append(valueOfMarginScenario);
			deriveCostsheetName.append(" ");

			if (FormatHelper.hasContent(colorwayLatterFromColorways)) {
				deriveCostsheetName.append(" (" + colorwayLatterFromColorways.trim() + ")");
			}

			logger.debug("sizeName >>>>>>>>>>>>>>>" + productSizeName);
			if (FormatHelper.hasContent(productSizeName.trim())) {
				deriveCostsheetName.append(" (" + productSizeName + ")");
			}

			String costSheetName = deriveCostsheetName.toString();
			logger.debug("costSheetName >>>>>>>>>>" + costSheetName);
			if(!AGR_COSTSHEET_NAME_KEY.equals("") && !costSheetName.equals("")){
				costSheetObject.setValue(AGR_COSTSHEET_NAME_KEY, costSheetName);
			}

		} catch (WTException exception) {
			logger.debug(exception.getMessage());
		}

	}
	


}
