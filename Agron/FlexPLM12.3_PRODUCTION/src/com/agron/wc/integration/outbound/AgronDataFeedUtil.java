package com.agron.wc.integration.outbound;

import com.agron.wc.integration.util.AgronCSVGenerator;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.LCSProperties;
import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.util.WTException;

/**
 * 
 * @author Vinod Dalai
 * @version 1.0
 * 
 * @author Mallikarjuna Savi
 * @version 2.0
 * 
 */
public class AgronDataFeedUtil {
	/**
	 * Defined to store the constant value.
	 */
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");
	private static final String MOA_ADOPTED_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moa.adoptedKey");
	private static final String AGENT_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.LCSSourcingConfig.agentKey");
	private static final String FACTORY_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.LCSSourcingConfig.factoryKey");
	private static final String VENDOR_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.supplier.vendorNumKey");
	private static final String COUNTRY_ORIGIN = LCSProperties
			.get("com.agron.wc.interface.attributes.LCSSourcingConfig.countryOfOriginKey");
	private static final String HTS_CODE = LCSProperties
			.get("com.agron.wc.interface.attributes.LCSSourcingConfig.htsCodeKey");
	private static final String COLLECTION_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.product.collectionKey");
	//private static final String SUBCOLLECTION_KEY = LCSProperties.get("com.agron.wc.interface.attributes.product.subCollectionKey");
	private static final String LINE_KEY = LCSProperties.get("com.agron.wc.interface.season.lineKey");
	private static final String PRODUCT_LINE_KEY = LCSProperties.get("com.agron.wc.interface.product.lineKey");
	private static final String SEGMENT_KEY = LCSProperties.get("com.agron.wc.interface.attributes.product.segmentKey");
	private static final String SMU_CLEARENCE = LCSProperties.get("com.agron.wc.interface.smuClearence",
			"SMU & Clearance");
	private static final String BRAND_INTIATIVE = LCSProperties.get("com.agron.wc.interface.brandIntiative",
			"Brand Initiative");
	private static final String TTI = LCSProperties.get("com.agron.wc.interface.tti", "TTI");
	private static final String SPORT_STYLE = LCSProperties.get("com.agron.wc.interface.sportStyle", "Sport Style");
	private static final String SOCKS = LCSProperties.get("com.agron.wc.interface.socks", "Socks");
	private static final String CARTINA = LCSProperties.get("com.agron.wc.interface.Cortina", "Cortina");
	private static final String SIZES_KEY = LCSProperties
			.get("com.agron.wc.interface.attributes.colorway.moa.sizesKey");

	/**
	 * This method is used to set the boolean status value.
	 * 
	 * @param isErrorOccure is Boolean object
	 */
	AgronDataFeedUtil() {
	}

	/**
	 * This method is used to get the size values.
	 * 
	 * @param moaCol is Collection object
	 * @return String type
	 */
	public static String getSizeValues(Collection<?> moaCol) throws WTException {
		LCSMOAObject moaObject = null;
		String sizes = null;
		StringBuffer buffer = new StringBuffer();
		for (Iterator<?> moaIter = moaCol.iterator(); moaIter.hasNext();) {
			moaObject = (LCSMOAObject) moaIter.next();
			if ("TRUE".equals(AgronCSVGenerator.getData(moaObject, MOA_ADOPTED_KEY, false))) {
				sizes = AgronCSVGenerator.getData(moaObject, SIZES_KEY, false);
				if (moaIter.hasNext())
					buffer.append(sizes).append(",");
				else
					buffer.append(sizes);
			}
		}

		return buffer.toString();
	}

	/**
	 * This method is used to get the collection value.
	 * 
	 * @param product is LCSProduct object
	 * @param season  is LCSSeason object
	 * @return String type
	 */
	public String getCollectionValue(LCSProduct product, LCSSeason season) throws WTException {
		String collection = null;
		
		/* Change Request on 6/18/2020 Collection value should send collection value attribute on for both old and new season. 
		
		String collection = null;
		String lineValue = null;
		lineValue = AgronCSVGenerator.getData(season, LINE_KEY, false);
		if (SPORT_STYLE.equalsIgnoreCase(lineValue))
			collection = AgronCSVGenerator.getData(product, SEGMENT_KEY, false);
		else
		*/
			collection = AgronCSVGenerator.getData(product, COLLECTION_KEY, false);
		return collection;
	}


	/*	
	 * This method is used to get the source data.
	 * 
	 * @param config  is LCSSourcingConfig object
	 * @param product is LCSProduct object
	 * @return FlexObject type
	 */
	public FlexObject getSourceData(LCSSourcingConfig config, LCSProduct product) {
		FlexObject sourceData = null;
		String vendorNumber = null;
		String countryOrigin = null;
		LCSSupplier supplier = null;
		String productType = null;
		String htsCode = "";
		try {
			productType = product.getFlexType().getFullName(true);
			sourceData = new FlexObject();
			supplier = (LCSSupplier) config.getValue(FACTORY_KEY);
			vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
			supplier = (LCSSupplier) config.getValue(AGENT_KEY);
			if (productType.contains(SOCKS)) {
				if (TTI.equalsIgnoreCase(AgronCSVGenerator.getData(supplier, "Name", false)))
					vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
			} else if (TTI.equalsIgnoreCase(AgronCSVGenerator.getData(supplier, "Name", false)))
				vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
			if (CARTINA.equalsIgnoreCase(AgronCSVGenerator.getData(supplier, "Name", false)))
				vendorNumber = AgronCSVGenerator.getData(supplier, VENDOR_KEY, false);
			countryOrigin = AgronCSVGenerator.getData(config, COUNTRY_ORIGIN, false);
			htsCode = AgronCSVGenerator.getData(config, HTS_CODE, false);
			sourceData.put("ERP.htsCodes", htsCode);
			sourceData.put("ERP.primaryVendorNumber", vendorNumber);
			sourceData.put("ERP.countryOfOrigin", countryOrigin);
			

		} catch (WTException e) {
			LOGGER.error((new StringBuilder("ERROR : ")).append(e.fillInStackTrace()).toString());
		}
		return sourceData;
	}

}
