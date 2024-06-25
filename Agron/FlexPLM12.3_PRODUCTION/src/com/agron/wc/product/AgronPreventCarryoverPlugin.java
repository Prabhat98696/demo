package com.agron.wc.product;

import java.util.Locale;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.agron.wc.integration.outbound.AgronExportUtil;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;

public class AgronPreventCarryoverPlugin {
	LCSProduct product = new LCSProduct();
	public static void stopProductCarryover(WTObject object)throws WTException, WTPropertyVetoException{
		if (object instanceof LCSProduct) {
			LCSProduct product = (LCSProduct)object;
			LCSSeason season = product.findSeasonUsed();
			if(null==product.getCarriedOverFrom()){
				return;
			}
			if(season != null){
				if(AgronExportUtil.isAgronSeasonType(season) && product.getFlexType().getTypeName().contains("Backpacks")){
					throw new LCSException("Product of type Backpacks cannot be carried over to latest season.");
				}
			/*	else if(yearL>=2022 && product.getFlexType().getTypeName().contains("Team")){
					throw new LCSException("Product of type Team cannot be carried over to season greater than year 2021 !!!");
				}*/
				else if(AgronExportUtil.isAgronSeasonType(season) && product.getFlexType().getTypeName().contains("SMU & Clearance")){
					throw new LCSException("Product of type SMU & Clearance cannot be carried over to latest season.");
				}
				
			}
		}
	}
	public static void stopColorwayCarryover(WTObject object) throws WTException, WTPropertyVetoException{
		if (object instanceof LCSSKU) {
			LCSSKU sku = (LCSSKU)object;
			LCSSeason season = sku.findSeasonUsed();
			if(null==sku.getCarriedOverFrom()){
				return;
			}
			if(season != null){
				if(AgronExportUtil.isAgronSeasonType(season) && sku.getFlexType().getTypeName().contains("Backpacks")){
					throw new LCSException("Colorway of type Backpacks cannot be carried over to latest season.");
				}
				/*else if(yearL>=2022 && sku.getFlexType().getTypeName().contains("Team")){
					throw new LCSException("Colorway of type Team cannot be carried over to season greater than year 2021 !!!");
				}*/
				else if(AgronExportUtil.isAgronSeasonType(season) && sku.getFlexType().getTypeName().contains("SMU & Clearance")){
					throw new LCSException("Colorway of type SMU & Clearance cannot be carried over to latest season.");
				}
			}
		}
	}
}
