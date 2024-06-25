package com.agron.wc.skusize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeLogic;
import com.lcs.wc.skusize.SKUSizeMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;

/**
 * SSP for deleting invalid SKUSize Object.
 * Only one SKUSize object should be created for each size 1 value.
 */
public final class AgronDeleteInvalidSKUSizes {

	public static final Logger log = LogManager.getLogger(AgronDeleteInvalidSKUSizes.class);
	
	/**
	 * By Default multiple SKU Sizes objects are created based on size 1 and size 2 value's combinations
	 * Only One SKU Size object should be created for each size 1 value 
	 * Other combinations which are created due to size 2 value should be deleted
	 * @param obj
	 */
	public static void syncSKUSizes(WTObject obj) {
		if(obj instanceof SKUSize) {
			try {
				SKUSize skuSize = (SKUSize) obj;
				SKUSizeMaster skuSizeMaster = (SKUSizeMaster) skuSize.getMaster();
				ProductSizeCategory productSizeCategory = VersionHelper.latestIterationOf(skuSizeMaster.getPscMaster());
				String sizeValues = productSizeCategory.getSizeValues();
				String size2Values  = productSizeCategory.getSize2Values();
				Collection<String> sizeValueCol = MOAHelper.getMOACollection(sizeValues);
				Collection<String> size2ValueCol = null;
				if(FormatHelper.hasContentAllowZero(size2Values)) {
					size2ValueCol = MOAHelper.getMOACollection(size2Values);
					if(size2ValueCol.size() > 1 && sizeValueCol.size() ==  size2ValueCol.size()) {
						List<String> validCombinations = new ArrayList<String>();
						List<String> sizeValueList = new ArrayList<>(sizeValueCol);
						List<String> size2ValueList = new ArrayList<>(size2ValueCol);
						for(int i=0; i<sizeValueList.size(); i++) {
							try {
								validCombinations.add(sizeValueList.get(i)+"_"+size2ValueList.get(i));
							} catch(IndexOutOfBoundsException execp) {
								execp.printStackTrace();
							}
						}
						if(!validCombinations.contains(skuSizeMaster.getSizeValue()+"_"+skuSizeMaster.getSize2Value())) {
							log.info("SKUSize Object to be deleted::"+skuSize);
							new SKUSizeLogic().delete(skuSize);
							log.info("SKUSize Object is deleted::"+skuSize);
						}
					}
				}

			} catch (WTException e) {
				e.printStackTrace();
			}

		}
	}
	
	
}
