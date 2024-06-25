package com.agron.wc.sizing;

import java.util.ArrayList;

import com.lcs.wc.sizing.ProdSizeCategoryToSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.MOAHelper;

import wt.fc.WTObject;

public class AgronPSCSizeValueValidation {

	public static void validatePSC(WTObject object) throws LCSException{
		if (object instanceof ProductSizeCategory) {
			ProductSizeCategory psc = (ProductSizeCategory) object;
			if(FormatHelper.hasContent(psc.getSize2Values())){
				ArrayList<String> sizes2Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSize2Values()));
				ArrayList<String> size1Collection = new ArrayList<String>((MOAHelper.getMOACollection(psc.getSizeValues())));
				if(size1Collection.size() != sizes2Collection.size()){
					throw new LCSException("One or more Sizes have not been provided with Size2 Value in Product Size Definition.");
				}
			}
					
		}else if(object instanceof ProdSizeCategoryToSeason){
			ProdSizeCategoryToSeason pscts = (ProdSizeCategoryToSeason) object;
			if(FormatHelper.hasContent(pscts.getSize2Values())){
				ArrayList<String> sizes2Collection = new ArrayList<String>(MOAHelper.getMOACollection(pscts.getSize2Values()));
				ArrayList<String> size1Collection = new ArrayList<String>((MOAHelper.getMOACollection(pscts.getSizeValues())));
				if(size1Collection.size() != sizes2Collection.size()){
					throw new LCSException("One or more Sizes have not been provided with Size2 Value in Product Size Definition/Season.");
				}
			}
		}
	}

}
