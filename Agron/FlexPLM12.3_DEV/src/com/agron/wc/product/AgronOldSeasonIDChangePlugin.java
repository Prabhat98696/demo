package com.agron.wc.product;

import java.util.Locale;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.agron.wc.integration.outbound.AgronExportUtil;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

public class AgronOldSeasonIDChangePlugin {
	
	private static final String COLORWAY_LETTER_KEY = LCSProperties.get("com.agron.wc.product.sku.colorwayLetter.key","agrColorwayLetter");
	private static final String COLORWAY_DESCRIPTION_KEY = LCSProperties.get("com.agron.wc.product.sku.colorwayDescription.key","agrColorwayDescription");


	public static void changeWorkNumber(WTObject object)throws WTException, WTPropertyVetoException{
		if (object instanceof LCSProduct) {
			LCSProduct product = (LCSProduct)object;
			LCSSeason season = product.findSeasonUsed();
			if(null==product.getCopiedFrom()){
				return;
			}
			LCSProduct productOrig = VersionHelper.latestIterationOf(product.getCopiedFrom().getMaster());
			productOrig = SeasonProductLocator.getProductARev(productOrig);
			if(season != null){
				if(AgronExportUtil.isAgronSeasonType(season) && productOrig!=null) {
					String copiedProductFlexType=product.getFlexType().getTypeName();
					String copiedProductFullFlexType=product.getFlexType().getFullName(true);
					String originalProductFlexType=productOrig.getFlexType().getTypeName();
					if(changeIdRequired(copiedProductFlexType, copiedProductFullFlexType, originalProductFlexType)) {
						Long workNum = (Long) productOrig.getValue("agrWorkOrderNoProduct");
						if(workNum!=null){
							String workNumStr = String.valueOf(workNum);
							if(!workNumStr.startsWith("-")){
								workNumStr = "-"+workNumStr;								
							}else {
								String productIdentity=productOrig.getName()+" ( Work # : "+workNumStr+")";
								throw new LCSException("This Proudct '"+productIdentity+"' is already been copied");
							}
							productOrig.setValue("agrWorkOrderNoProduct", workNumStr);
							workNumStr = workNumStr.substring(1);
							product = SeasonProductLocator.getProductARev(product);
							product.setValue("agrWorkOrderNoProduct", workNumStr);
							PersistenceServerHelper.manager.update(productOrig);
							PersistenceServerHelper.manager.update(product);
						}
					}
				}
			}
		}
	}
	public static void changeArticleNumber(WTObject object) throws WTException, WTPropertyVetoException{
		if(object instanceof LCSSKU){
			LCSSKU sku = (LCSSKU)object;
			LCSSeason season = sku.findSeasonUsed();
			if(null==sku.getCopiedFrom()){
				return;
			}
			LCSSKU skuOrig = VersionHelper.latestIterationOf(sku.getCopiedFrom().getMaster());
			skuOrig = SeasonProductLocator.getSKUARev(skuOrig);
			if(season != null){
				if(AgronExportUtil.isAgronSeasonType(season) && skuOrig!=null) {
					System.out.println("AgronOldSeasonIDChangePlugin SKU >>"+sku);
					String copiedProductFlexType=sku.getFlexType().getTypeName();
					String copiedProductFullFlexType=sku.getFlexType().getFullName(true);
					String originalProductFlexType=skuOrig.getFlexType().getTypeName();
					if(changeIdRequired(copiedProductFlexType, copiedProductFullFlexType, originalProductFlexType)) {
						String articleNum = (String) skuOrig.getValue("agrArticle");
						if(FormatHelper.hasContent(articleNum)){
							String articleNumStr = String.valueOf(articleNum);
							if(!articleNumStr.startsWith("-")){
								articleNumStr = "-"+articleNumStr;
							}else {
								String skuName=(String)skuOrig.getValue("skuName");
								throw new LCSException("This Colorway '"+skuName+"' is already been copied");
							}
							sku = SeasonProductLocator.getSKUARev(sku);
							sku.setValue("agrArticle", articleNum);
							sku.setValue("agrAdopted", skuOrig.getValue("agrAdopted"));
						/*	String colorDesc = (String) skuOrig.getValue("agrColorwayDescription");
							if (FormatHelper.hasContent(colorDesc))
								sku.setValue("agrColorwayDescription", colorDesc);*/
							skuOrig.setValue("agrArticle", articleNumStr);
							PersistenceServerHelper.manager.update(skuOrig);
							PersistenceServerHelper.manager.update(sku);
						}
					}

				}}}
	}
	
    public static boolean changeIdRequired(String copiedPoductType,String copiedFullFlexType,String originalType) {
    	boolean isChangeIdRequired=false;
    	if( (originalType.contains("Backpacks") && copiedPoductType.contains("Bags")) 
    			|| (originalType.contains("SMU & Clearance") && copiedFullFlexType.contains("Adidas")) ) {
    		isChangeIdRequired=true;
    	}
    	System.out.println("Copied from :"+originalType+" to>>>>"+copiedPoductType+"::"+copiedFullFlexType+":::"+isChangeIdRequired);
    	return isChangeIdRequired;
    }
	
	public static void clearArticleNumber(WTObject object) throws WTException, WTPropertyVetoException{
		if(object instanceof LCSSKU){
			LCSSKU sku = (LCSSKU)object;
			if(sku.getCopiedFrom() !=null){
				sku.setValue("agrArticle", "");
			}
		}
	}
	
	public static void copySKUAttributeValues(WTObject object) throws WTException, WTPropertyVetoException{
		if(object instanceof LCSSKU){
			LCSSKU sku = (LCSSKU)object;
			try {
				if(sku.getCopiedFrom() !=null){
					String copiedProductFlexType=sku.getFlexType().getTypeName();
					String copiedProductFullFlexType=sku.getFlexType().getFullName(true);
					LCSSKU skuOrig = VersionHelper.latestIterationOf(sku.getCopiedFrom().getMaster());
					skuOrig = SeasonProductLocator.getSKUARev(skuOrig);
					String originalProductFlexType=skuOrig.getFlexType().getTypeName();
					if(changeIdRequired(copiedProductFlexType, copiedProductFullFlexType, originalProductFlexType)) {
						String colorwayLetter= (String) skuOrig.getValue(COLORWAY_LETTER_KEY);
						String colorDesc = (String) skuOrig.getValue(COLORWAY_DESCRIPTION_KEY);
						System.out.println("copySKUAttributeValues>>colorwayLetter::colorDesc:"+colorwayLetter+"::"+colorDesc+"::"+skuOrig.getName());
						if (FormatHelper.hasContent(colorDesc)){
							sku.setValue(COLORWAY_DESCRIPTION_KEY, colorDesc);
						}
						if (FormatHelper.hasContent(colorwayLetter)){	
							sku.setValue(COLORWAY_LETTER_KEY, colorwayLetter);
						}
					}
				}

			}catch(Exception e) {
			System.out.println("Exception while configuring copySKUAttributeValues:"+e.getMessage());
			}
		}
	}
	
	
	
	
}
