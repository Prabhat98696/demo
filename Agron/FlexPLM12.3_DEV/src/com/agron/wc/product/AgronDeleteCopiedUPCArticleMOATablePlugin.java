package com.agron.wc.product;

import com.agron.wc.util.AgronArticleNumberUPCHelper;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.product.LCSSKU;

import wt.fc.WTObject;

public class AgronDeleteCopiedUPCArticleMOATablePlugin {

	
	/**
	 * delete the Article UPC Number MOA table if SKU is copied (Except Backpacks to Bags and Team to Socks)
	 * @param object
	 */
	public static void deleteMoaTableOnCopySKU(WTObject object){
		System.out.println("deleteMoaTableOnCopySKU START ::"+object);
		try{

			if (object instanceof LCSSKU) {
				LCSSKU sku = (LCSSKU) object;
				if (sku.getVersionIdentifier() != null&& !"A".equals(sku.getVersionIdentifier().getValue()) || sku.isPlaceholder()) {
					return;
				}
				//sku = (LCSSKU) VersionHelper.getVersion((LCSSKU) VersionHelper.latestIterationOf(sku.getMaster()),"A"); 
				LCSSKU copiedFromSKU=(LCSSKU)sku.getCopiedFrom();
				if(copiedFromSKU !=null) {
					String copiedFromType=copiedFromSKU.getFlexType().getTypeDisplayName();
					String copiedToSkuType=sku.getFlexType().getTypeDisplayName();
					String copiedToSkuFullType=sku.getFlexType().getFullName(true);
					System.out.println("copiedFrom:::"+copiedFromSKU.getName()+"::"+copiedFromType+"::copiedToSkuType:-"+copiedToSkuType);
					if(!((copiedFromType.equalsIgnoreCase("Backpacks") && copiedToSkuType.equalsIgnoreCase("Bags")) 
							|| ( copiedFromType.equalsIgnoreCase("SMU & Clearance") && copiedToSkuFullType.contains("Adidas")))){
						System.out.println("COPY SKU ::DROP MOA TABLE IF DATA EXISTS");
						LCSMOATable articleUPCMoaTable = AgronArticleNumberUPCHelper.getMoatableBySku(sku);
						AgronArticleNumberUPCHelper.dropMoaTabel(articleUPCMoaTable);	
					}
				}}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("deleteMoaTableOnCopySKU END ::");
	}
		
}
