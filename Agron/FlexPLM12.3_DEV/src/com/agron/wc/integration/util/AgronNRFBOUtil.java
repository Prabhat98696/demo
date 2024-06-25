package com.agron.wc.integration.util;

import com.lcs.wc.db.*;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import wt.util.WTException;

/**
 * @author Mallikarjuna Savi
 *
 */
public final class AgronNRFBOUtil {

	/**
	 * Defined to store the constant value.
	 */
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");
	private static final String NRF_MOA_PATH = LCSProperties.get("com.agron.wc.interface.nrfMOAPath");
	private static final String LCSMOAOBJECT = "LCSMOAObject";
	private static final String PRODUCT_GENDER_KEY = LCSProperties.get("com.agron.wc.interface.product.genderKey");
	private static final String MOA_GENDER_KEY = LCSProperties.get("com.agron.wc.interface.moa.genderKey");
	private static final String COLORWAY_MOA_SIZE_KEY = LCSProperties.get("com.agron.wc.interface.attributes.colorway.moa.sizesKey");
	private static final String MOA_SIZE_KEY = LCSProperties.get("com.agron.wc.interface.moa.sizeKey");
	private static final String MOA_NRFSIZE_KEY = LCSProperties.get("com.agron.wc.interface.moa.nrfSizeKey");
	private static final String MOA_NRFDESC_KEY = LCSProperties.get("com.agron.wc.interface.moa.nrfDescKey");
	private static final String MOA_PRODUCTTYPE_KEY = LCSProperties.get("com.agron.wc.interface.moa.productTypeKey");

	/**
	 * Default Constructor.
	 */
	public AgronNRFBOUtil() {
	}

	/**
	 * This method is used to get the NRF Size and Description.
	 * 
	 * @param product     is LCSProduct object
	 * @param productType is String object
	 * @param moaObject   is LCSMOAObject
	 * @return FlexObject type
	 */
	public FlexObject getNRFValuesFromBO(LCSProduct product, String productType, LCSMOAObject moaObject) {
		FlexObject nrfData = null;
		String moaSizeValue = null;
		String skuSize2Value = null;
		String moaSizeAttColumn = null;
		String moaSize2AttColumn = null; //agrShoeSize
		String productGenderValue = null;
		String moaGenderAttColumn = null;
		String moaNRFSizeAttColumn = null;
		String moaNRFDescAttColumn = null;
		String moaProductTypeAttColumn = null;
		FlexType moaObjType = null;
		String localProductType = null;
		try {
			LOGGER.info((new StringBuilder()).append("Product Type : ").append(productType).toString());

			nrfData = new FlexObject();
			moaObjType = FlexTypeCache.getFlexTypeFromPath(NRF_MOA_PATH);
			moaSizeAttColumn = moaObjType.getAttribute(MOA_SIZE_KEY).getColumnName();
			moaSize2AttColumn = moaObjType.getAttribute("agrskuSize2Value").getColumnName();
			moaGenderAttColumn = moaObjType.getAttribute(MOA_GENDER_KEY).getColumnName();
			moaNRFSizeAttColumn = moaObjType.getAttribute(MOA_NRFSIZE_KEY).getColumnName();
			moaNRFDescAttColumn = moaObjType.getAttribute(MOA_NRFDESC_KEY).getColumnName();
			moaProductTypeAttColumn = moaObjType.getAttribute(MOA_PRODUCTTYPE_KEY).getColumnName();
			moaSizeValue = (String) moaObject.getValue(COLORWAY_MOA_SIZE_KEY);
			skuSize2Value = (String) moaObject.getValue("agrShoeSize");
			productGenderValue = (String) product.getValue(PRODUCT_GENDER_KEY);
			localProductType = productType.replaceAll(" ", "");
			localProductType = localProductType.replaceAll("-", "");
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.setDistinct(true);
			pqs.appendFromTable(LCSMOAOBJECT);
			pqs.appendSelectColumn(LCSMOAOBJECT, moaNRFSizeAttColumn);
			pqs.appendSelectColumn(LCSMOAOBJECT, moaNRFDescAttColumn);
			pqs.appendSelectColumn(LCSMOAOBJECT, moaSize2AttColumn);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSMOAOBJECT, moaProductTypeAttColumn, localProductType, "="));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSMOAOBJECT, moaSizeAttColumn, moaSizeValue, "="));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSMOAOBJECT, moaGenderAttColumn, productGenderValue, "="));
			SearchResults boCol = LCSQuery.runDirectQuery(pqs);
			if (boCol != null && boCol.getResults().size() > 0) {
				Collection<?> flexObjCol = boCol.getResults();
				Iterator<?> iter = flexObjCol.iterator();
				while(iter.hasNext()) {
				FlexObject flexObj = (FlexObject) iter.next();
				String size2value = (String)flexObj.getData("LCSMOAObject."+moaSize2AttColumn.toUpperCase());
				nrfData.put("ERP.nrfSize",
						flexObj.getData(
								(new StringBuilder()).append("LCSMOAObject.").append(moaNRFSizeAttColumn).toString())
								.toUpperCase());
				nrfData.put("ERP.nrfSizeDesc",
						flexObj.getData(
								(new StringBuilder()).append("LCSMOAObject.").append(moaNRFDescAttColumn).toString())
								.toUpperCase());
				// Get the best match with Size 2 value
				if(FormatHelper.hasContent(size2value) && size2value.equals(skuSize2Value)) {
					break;
				}
			}
			}
		} catch (WTException e) {
			LOGGER.error("ERROR : " + e.fillInStackTrace());
		}
		return nrfData;
	}

	/**
	 * This method is used to get the NRF Size and Description.
	 * 
	 * @param product     is LCSProduct object
	 * @param productType is String object
	 * @param moaObject   is LCSMOAObject
	 * @return FlexObject type
	 */
	public FlexObject getNRFValuesFromBO1(LCSProduct product, String productType, String moaObject,String skuSize2Value) {
		FlexObject nrfData = null;
		String moaSizeValue = null;
		String moaSizeAttColumn = null;
		String moaSize2AttColumn = null;
		String productGenderValue = null;
		String moaGenderAttColumn = null;
		String moaNRFSizeAttColumn = null;
		String moaNRFDescAttColumn = null;
		String moaProductTypeAttColumn = null;
		FlexType moaObjType = null;
		String localProductType = null;
		try {
			LOGGER.info((new StringBuilder()).append("Product Type : ").append(productType).toString());
			nrfData = new FlexObject();
			moaObjType = FlexTypeCache.getFlexTypeFromPath(NRF_MOA_PATH);
			moaSize2AttColumn = moaObjType.getAttribute("agrskuSize2Value").getColumnName();
			moaSizeAttColumn = moaObjType.getAttribute(MOA_SIZE_KEY).getColumnName();
			moaGenderAttColumn = moaObjType.getAttribute(MOA_GENDER_KEY).getColumnName();
			moaNRFSizeAttColumn = moaObjType.getAttribute(MOA_NRFSIZE_KEY).getColumnName();
			moaNRFDescAttColumn = moaObjType.getAttribute(MOA_NRFDESC_KEY).getColumnName();
			moaProductTypeAttColumn = moaObjType.getAttribute(MOA_PRODUCTTYPE_KEY).getColumnName();
			moaSizeValue = (String) moaObject;
			productGenderValue = (String) product.getValue(PRODUCT_GENDER_KEY);
			localProductType = productType.replaceAll(" ", "");
			localProductType = localProductType.replaceAll("-", "");
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.setDistinct(true);
			pqs.appendFromTable(LCSMOAOBJECT);
			pqs.appendSelectColumn(LCSMOAOBJECT, moaNRFSizeAttColumn);
			pqs.appendSelectColumn(LCSMOAOBJECT, moaNRFDescAttColumn);
			pqs.appendSelectColumn(LCSMOAOBJECT, moaSize2AttColumn);
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSMOAOBJECT, moaProductTypeAttColumn, localProductType, "="));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSMOAOBJECT, moaSizeAttColumn, moaSizeValue, "="));			
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(LCSMOAOBJECT, moaGenderAttColumn, productGenderValue, "="));
			SearchResults boCol = LCSQuery.runDirectQuery(pqs);
			if (boCol != null && boCol.getResults().size() > 0) {
				
				Collection<?> flexObjCol = boCol.getResults();
				Iterator<?> iter = flexObjCol.iterator();
				while(iter.hasNext()) {
				FlexObject flexObj = (FlexObject) iter.next();
				
				String size2value = (String)flexObj.getData("LCSMOAObject."+moaSize2AttColumn.toUpperCase());
				LOGGER.info(" flexObj --- > " + flexObj);
				nrfData.put("ERP.nrfSize",
						flexObj.getData(
								(new StringBuilder()).append("LCSMOAObject.").append(moaNRFSizeAttColumn).toString())
								.toUpperCase());
				nrfData.put("ERP.nrfSizeDesc",
						flexObj.getData(
								(new StringBuilder()).append("LCSMOAObject.").append(moaNRFDescAttColumn).toString())
								.toUpperCase());
				// Get the best match with Size 2 value

				if(FormatHelper.hasContent(size2value) && size2value.equals(skuSize2Value)) {
					LOGGER.info("Found a match on NRF BO - size2value = " + size2value +" - skuSize2Value = " + skuSize2Value );
					break;
				}
				
				
				}
				
				
			}
		} catch (WTException e) {
			LOGGER.error("ERROR : " + e.fillInStackTrace());
		}
		return nrfData;
	}

}
