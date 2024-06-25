package com.agron.wc.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;

public class AgronUniqueSKUPlugin {
	
	public static final String CLASSNAME = "AgronUniqueSKUPlugin";
	
	public static final Logger logger = LogManager.getLogger(AgronUniqueSKUPlugin.class);
	public static final String CATALOG_COLORWAY_NAME_KEY = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.catalogColorwayName");
	public static final  String COLORWAY_LETTER_KEY = LCSProperties.get("com.agron.wc.product.colorwayLetter");
	
	public static void isSKUNameUnique(WTObject obj) throws WTException {
		LCSProduct product = null;
	
		LCSSKU sku = null;
		Collection productSKUList = null;
		Collection skuCollection = new ArrayList();
		Collection oldProductSKUList = null;
		if (!(obj instanceof LCSSKU)) {
			throw new LCSException("Object must be a LCSSKU");
		} else {
			sku = (LCSSKU) obj;
			if ((sku.getVersionIdentifier() == null || "A".equals(sku.getVersionIdentifier().getValue()))
					&& !sku.isPlaceholder()) {
				product = SeasonProductLocator.getProductARev(sku);
				productSKUList = LCSSKUQuery.findSKUs(product, true);
				skuCollection.addAll(productSKUList);
				logger.info("productSKUList::"+productSKUList.size());
				//To Check if that colorway letter exists in the copied from product.
				if(product.getCopiedFrom() !=null && sku.getCopiedFrom()==null){
					LCSProduct productOrig = (LCSProduct) VersionHelper.getVersion((LCSProduct) VersionHelper.latestIterationOf(product.getCopiedFrom().getMaster()),"A"); 
					if(productOrig !=null) {
						String copiedProductFlexType=product.getFlexType().getFullName(true);
						String originalProductFlexType=productOrig.getFlexType().getFullName(true);
						if( (originalProductFlexType.contains("Backpacks") && copiedProductFlexType.contains("Bags")) 
								|| (originalProductFlexType.contains("SMU & Clearance") && copiedProductFlexType.contains("Adidas")) ) {
							oldProductSKUList = LCSSKUQuery.findSKUs(productOrig, true);
							logger.debug("oldProductSKUList::"+oldProductSKUList.size()+"::"+oldProductSKUList);
							if(oldProductSKUList !=null && oldProductSKUList.size()>0) {
								productSKUList.addAll(oldProductSKUList);	
								logger.info("Added original product's SKUs to validate colorway letter");
							}
						}	
					}
				}
				validateColorwayLetterUniquness(productSKUList,sku,(String)product.getValue("productName"));
				validateCatalogNameUniquness(skuCollection,sku,(String)product.getValue("productName"));
				
			}
		}
	}
	
    static void validateColorwayLetterUniquness(Collection productSKUList,LCSSKU sku,String productName) throws WTException{
    	Iterator iter = productSKUList.iterator();
		int inc = 0;
		String colorwayLetter = (String) sku.getValue(COLORWAY_LETTER_KEY);
		logger.debug("SKU Unique Name Validation:"+sku.getName()+"::"+colorwayLetter+"::SKU Size::"+productSKUList.size());
		LCSSKU tempSKU;
		do {
			if (!iter.hasNext()) {
				return;
			}

			++inc;
			tempSKU = (LCSSKU) iter.next();
			logger.info(inc+"::SKU>>"+tempSKU+"::"+tempSKU.getName()+"::"+tempSKU.getValue("agrColorwayLetter"));
			
		} while (!colorwayLetter.equalsIgnoreCase((String) tempSKU.getValue("agrColorwayLetter")));
		logger.info("ColorwayLetter:: "+colorwayLetter+", SKUlater"+tempSKU.getValue("agrColorwayLetter"));
		Object[] objB = new Object[]{colorwayLetter, productName};
		throw new LCSException("com.lcs.wc.resource.ExceptionRB", "skuNameNotUnique_MSG", objB);
    }
	
    static void validateCatalogNameUniquness(Collection productSKUList,LCSSKU sku,String productName) throws WTException{
    	String catalogColorName =(String)sku.getValue(CATALOG_COLORWAY_NAME_KEY);
		logger.info("validateCatalogNameUniquness::SKU>>"+sku.getName()+"::catalogColorName>>"+catalogColorName+"::SKU Size>>"+productSKUList.size());
    	if(FormatHelper.hasContent(catalogColorName)) {
    		Iterator iter = productSKUList.iterator();
    		int inc = 0;
    		LCSSKU tempSKU;
    		do {
    			if (!iter.hasNext()) {
    				return;
    			}
    			++inc;
    			tempSKU = (LCSSKU) iter.next();
    			logger.debug(inc+"::SKU>>"+tempSKU+"::SKUName>>"+tempSKU.getValue("skuName")+"::catalogColorName>>"+tempSKU.getValue(CATALOG_COLORWAY_NAME_KEY));

    		} while (!catalogColorName.equalsIgnoreCase((String) tempSKU.getValue(CATALOG_COLORWAY_NAME_KEY)));

    	}
		throw new LCSException("The Colorway '"+sku.getValue("skuName")+"'  was not created/updated, because the Catalog Colorway Name '"+catalogColorName+"' is not unique for this Product.");
    }
}
