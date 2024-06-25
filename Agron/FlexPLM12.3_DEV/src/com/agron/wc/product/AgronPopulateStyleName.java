package com.agron.wc.product;
import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import java.util.Iterator;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQueryStatement;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.util.FormatHelper;

/**
 * @author Acnovate Corp
 * @version 1.0
 */
public final class AgronPopulateStyleName {
	/**
	 * Defined to store the contant value.
	 */
	private static final String STYLENAME_ABBREAVION_BO_NAME = LCSProperties.get("com.agron.wc.busObj.styleNameAbbreavationName","MAS Style Name Abbreviations");
	/**
	 * Defined to store the contant value.
	 */
	private static final String STYLENAME_ABBREAVION_BO_TYPE = LCSProperties.get("com.agron.wc.busObj.styleNameAbbreavationType","Business Object\\MAS Style Name Abbreviations");
	/**
	 * Defined to store the contant value.
	 */
	private static final String BUSOBJ_MOA_KEY = LCSProperties.get("com.agron.wc.busObj.moaKey","agrStyleNameAbbreviation");
	/**
	 * Defined to store the contant value.
	 */
	private static final String MOA_STYLENAME_PARTKEY = LCSProperties.get("com.agron.wc.moa.styleNamePartKey","agrStyleName");
	/**
	 * Defined to store the contant value.
	 */
	private static final String MOA_STYLENAME_ABRKEY = LCSProperties.get("com.agron.wc.moa.styleNameAbrKey","agrStyleNameShort");
	/**
	 * Defined to store the contant value.
	 */
	private static final String PRODUCTNAME_KEY = LCSProperties.get("com.agron.wc.product.productNameKey","productName");
	/**
	 * Defined to store the contant value.
	 */
	private static final String MASSTYLENAME_KEY = LCSProperties.get("com.agron.wc.product.masStyleNameKey","agrMASStyleName");
	/**
	 * Defined to store the contant value.
	 */
	private static final String LIFECYCLEMANAGED = "LCSLifeCycleManaged";
	
	private static final String BULLETE_DESCRIPTION_KEY = LCSProperties.get("com.agron.wc.product.bulletDescription","agrBulletDescription");
	
	/**
	 * Default Constructor.
	 */
	private AgronPopulateStyleName(){
	}
	/**
	 * This method is used to set the style name.
	 * @param object is WTObject object
	 */
	public static void setStyleName(WTObject object)throws WTException, WTPropertyVetoException{
		LCSProduct product = null;
		LCSProduct productARev = null;
		LCSLifecycleManaged busObj = null;
		String productName = "";
		Iterator moaIter = null;
		LCSMOAObject moaObject = null;
		FlexTypeAttribute attribute = null;
		FlexType busType = null;
		SearchResults moaResults = null;
		FlexObject flexObj = null;
		String styleNamePart = null;
		String styleNameAbr = null;
		//String masStyleName = "";
		boolean isInMOA = false;
		//String formattedProductName = "";
		try{
			//if(object instanceof LCSProduct){
				product  = (LCSProduct)object;
				productARev = getVersion(product);
				busType = FlexTypeCache.getFlexTypeFromPath(STYLENAME_ABBREAVION_BO_TYPE);
				attribute = busType.getAttribute(BUSOBJ_MOA_KEY);
				
				setBulletDescription(productARev);
				
				productName = (String)productARev.getValue(PRODUCTNAME_KEY);
				//masStyleName= (String)productARev.getValue(MASSTYLENAME_KEY);
				//formattedProductName = productName;
				busObj = getBusObj();
				if(busObj != null && productName.length()>30){
					moaResults = LCSMOAObjectQuery.findMOACollectionData(busObj, attribute);
					if(moaResults.getResults().size()>0){
						for (String retval: productName.split(" ")){
							moaIter = moaResults.getResults().iterator();
							while(moaIter.hasNext()){							
								flexObj = (FlexObject)moaIter.next();
								moaObject = (LCSMOAObject)LCSQuery.findObjectById("OR:com.lcs.wc.moa.LCSMOAObject:" + flexObj.getData("LCSMOAObject.IDA2A2"));
								styleNamePart = (String)moaObject.getValue(MOA_STYLENAME_PARTKEY);
								if(retval.equalsIgnoreCase(styleNamePart) && productName.length()>30){
									styleNameAbr = (String)moaObject.getValue(MOA_STYLENAME_ABRKEY);
									productName = productName.toUpperCase().replace(styleNamePart.toUpperCase(), styleNameAbr.toUpperCase());
									isInMOA = true;
									break;
								}
							}
						}
						if(isInMOA){
							productARev.setValue(MASSTYLENAME_KEY, productName);
							PersistenceServerHelper.manager.update(productARev);
						}
					}
				}
				if(!isInMOA){
					productARev.setValue(MASSTYLENAME_KEY, productName);
					PersistenceServerHelper.manager.update(productARev);
				}
			//}
		}catch(WTException e){
			LCSLog.stackTrace(e);
		}
	}
	/**
	 * This method is used to get the version of the Product.
	 * @param prodObj is LCSProduct object
	 * @return LCSProduct type
	 */
	public static LCSProduct getVersion(LCSProduct prodObj){
		LCSProduct product = null;
		try{
			if("A".equals(prodObj.getVersionIdentifier()
					.getValue())){
				product = prodObj;
			}else{
				product = (LCSProduct)VersionHelper.getVersion(prodObj, "A");
			}
		}catch(WTException e){
			LCSLog.stackTrace(e);
		}
		return product;
	}
	/**
	 * This method is used to get the business object.
	 * @return LCSLifecycleManaged type
	 */
	public static LCSLifecycleManaged getBusObj(){
		LCSLifecycleManaged busObj = null;
		try{
			FlexTypeQueryStatement ftqs = new FlexTypeQueryStatement();
			FlexType flexType = FlexTypeCache
			.getFlexTypeFromPath(STYLENAME_ABBREAVION_BO_TYPE);
			ftqs.setType(flexType); // setting flex type.
			ftqs.appendSelectColumn(LIFECYCLEMANAGED, "ida2a2");
			ftqs.appendAndIfNeeded();
			ftqs.appendFlexCriteria("name", STYLENAME_ABBREAVION_BO_NAME, Criteria.EQUALS);
			SearchResults sr = LCSQuery.runDirectQuery(ftqs, true);
			if(sr != null && sr.getResults().size()>0){
				Iterator<Object> it = sr.getResults().iterator();	// iterating business objects collection.
				FlexObject flexObj = (FlexObject)it.next();	// getting flex object from the collection.
				busObj = (LCSLifecycleManaged)LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + (String)flexObj.get("LCSLIFECYCLEMANAGED.IDA2A2"));	// getting business object id from the flex object.
				
			}
		}catch(WTException e){
			LCSLog.stackTrace(e);
		}
		return busObj;
	}
	
	/*
	 * This method is used to set the BulletDescription attribute by removing trailing dots.
	 * @param object is LCSproduct product
	 */
		public static void setBulletDescription(LCSProduct productARev) throws WTException, WTPropertyVetoException
		{
			String bulletDescriptionValue = (String)productARev.getValue(BULLETE_DESCRIPTION_KEY);
			System.out.println("bulletDescriptionValue>>>>>>"+bulletDescriptionValue);
			if(FormatHelper.hasContent(bulletDescriptionValue)) {
				bulletDescriptionValue=bulletDescriptionValue.trim();
				while (bulletDescriptionValue.length() > 0 && bulletDescriptionValue.charAt(bulletDescriptionValue.length() - 1) == '.') {
					bulletDescriptionValue = bulletDescriptionValue.substring(0, bulletDescriptionValue.length() - 1).trim();
				}
				productARev.setValue(BULLETE_DESCRIPTION_KEY, bulletDescriptionValue);
			}

		}
}

