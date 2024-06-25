package com.agron.wc.skusize;

import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;

public class AgronClearUPCArticleNumberFromSKUSize {
	
	private static final String SKUSIZE_ARTICLE_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.articleNumberKey","agrArticleNumber");
	private static final String SKUSIZE_UPC_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.upcNumberKey","upcCode");

	/**
	 * clear  the Article and UPC Number from SKU Sizes
	 * @param object
	 */
	public static void clearSKUSizeArticleUPCValues(WTObject object){
		System.out.println("clearSKUSizeArticleUPCValues START ::"+object);
		try{
			if (object instanceof SKUSize) {
				SKUSize skuSize = (SKUSize) object;
				String sizeArticleNumber= (String) skuSize.getValue(SKUSIZE_ARTICLE_NBMER_KEY);
				String sizeUPCNumber= (String) skuSize.getValue(SKUSIZE_UPC_NBMER_KEY);
				System.out.println("sizeArticleNumber::sizeUPCNumber"+sizeArticleNumber+"::"+sizeUPCNumber);
				if(FormatHelper.hasContent(sizeArticleNumber)) {
					skuSize.setValue(SKUSIZE_ARTICLE_NBMER_KEY, "");
				}
				if(FormatHelper.hasContent(sizeUPCNumber)) {
					skuSize.setValue(SKUSIZE_UPC_NBMER_KEY, "");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("clearSKUSizeArticleUPCValues END ::");
	}	
}
