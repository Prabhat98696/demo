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
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQueryStatement;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.util.FormatHelper;


/**
 * @author Acnovate Corp
 * @version 1.0
 */
public final class AgronSetProdDimensons {

	/**
	 * Defined to store the contant value.
	 */
	private static final String PRODUCTNAME_KEY = LCSProperties.get("com.agron.wc.product.productNameKey","productName");
	public static final boolean DEBUG = LCSProperties.getBoolean("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.debug");
	
	/**
	 * Default Constructor.
	 */
	private AgronSetProdDimensons(){
	}
	/**
	 * This method is used to set the style name.
	 * @param object is WTObject object
	 */
	public static void setProductDimensions(WTObject object)throws WTException, WTPropertyVetoException{
		
		LCSProduct product = null;
		PreparedQueryStatement buProdDimensionsQuery = null;
		Collection prodDimensionBU =null;
		Iterator businessObjectsItr =null;
		if(DEBUG)
			System.out.println("000 ---- In AgronSetProdDimensons:::setProductDimensions");
		
		String partTypeKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.partTypeKey");
		String genderKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.GenderKey");
		String silhouetteKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.SilhouetteKey");
		String familyKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.FamilyKey");
		String packCountKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.PackCountKey");
		String packTypeKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.PackTypeKey");
		String prodDimFlexTypePath = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.ProdDimensions.FlexTypePath");
		String partTypeBUKey = LCSProperties.get("com.agron.wc.foundation.LCSLifecycleManaged.AgronSetProdDimensons.BUProductTypeKey");
		
		
		String packagedProductLengthKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.PackagedProductLength");
		String packagedProductWidthKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.PackagedProductWidth");
		String packagedProductHeightKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.PackagedProductHeight");
		String packagedProductWeightKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.PackagedProductWeight");
		String heightKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.Height");
		String widthtKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.Width");
		String depthKey = LCSProperties.get("com.agron.wc.product.LCSProduct.AgronSetProdDimensons.Depth");
		try{
			//if(object instanceof LCSProduct){
				product  = (LCSProduct)object;
				FlexType flexTypeProd = product.getFlexType();
				String flexTypepath = (String)flexTypeProd.getFullNameDisplay(true);
				flexTypepath = flexTypepath.substring(flexTypepath.indexOf("A"));
				if(DEBUG) {
					System.out.println("001 ---- In AgronSetProdDimensons:::setProductDimensions::: partTypeKey || genderKey || silhouetteKey||familyKey|| packCountKey||packTypeKey"+partTypeKey+"||"+genderKey+"||"+silhouetteKey+"||"+familyKey+"||"+packCountKey+"||"+packTypeKey);
					System.out.println("002 ---- In AgronSetProdDimensons:::setProductDimensionsWTObject -->  "+flexTypepath);
				}
								
				String genderValue = (String)product.getValue(genderKey);
				String silhouetteValue = (String)product.getValue(silhouetteKey);
				String familyValue = null;
				String packCountValue = null;
				String packTypeValue = null;
				if (FormatHelper.hasContent (product.getValue(familyKey).toString())) 
					familyValue = (String)product.getValue(familyKey);
				if (FormatHelper.hasContent (product.getValue(packCountKey).toString()))
					packCountValue = (String)product.getValue(packCountKey);
				if (FormatHelper.hasContent (product.getValue(packTypeKey).toString()))				
					packTypeValue = (String)product.getValue(packTypeKey);

				System.out.println("021 ---- In AgronSetProdDimensons:::setProductDimensions familyValue-->"+familyValue+"::::");
				System.out.println("021 ---- In AgronSetProdDimensons:::setProductDimensions packCountValue-->"+packCountValue+"::::");
				System.out.println("021 ---- In AgronSetProdDimensons:::setProductDimensions packTypeValue-->"+packTypeValue+"::::");
			
				

				FlexType prodDimensionBUFlexType = FlexTypeCache.getFlexTypeFromPath(prodDimFlexTypePath);
				String flextypeIDBUProdDim = prodDimensionBUFlexType.getTypeIdPath();
				if(DEBUG)
					System.out.println("003 ---- In AgronSetProdDimensons:::setProductDimensions flextypeIDBUProdDim-->"+flextypeIDBUProdDim);
				
				String buPartTypecolumn  = prodDimensionBUFlexType.getAttribute(partTypeBUKey).getColumnName();
				String genderColumn = prodDimensionBUFlexType.getAttribute(genderKey).getColumnName();
				String silhouetteColumn = prodDimensionBUFlexType.getAttribute(silhouetteKey).getColumnName();
				String familyColumn = prodDimensionBUFlexType.getAttribute(familyKey).getColumnName();
				String packCountColumn = prodDimensionBUFlexType.getAttribute(packCountKey).getColumnName();
				String packTypeColumn = prodDimensionBUFlexType.getAttribute(packTypeKey).getColumnName();
				
				buProdDimensionsQuery = new PreparedQueryStatement();
				buProdDimensionsQuery.appendSelectColumn("LCSLIFECYCLEMANAGED","IDA2A2");
				buProdDimensionsQuery.appendFromTable("LCSLIFECYCLEMANAGED"); 
				buProdDimensionsQuery.appendAndIfNeeded();
				buProdDimensionsQuery.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", buPartTypecolumn,flexTypepath, Criteria.EQUALS));
				buProdDimensionsQuery.appendAndIfNeeded();
				buProdDimensionsQuery.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", genderColumn,genderValue, Criteria.EQUALS));				
				buProdDimensionsQuery.appendAndIfNeeded();
				buProdDimensionsQuery.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", silhouetteColumn,silhouetteValue, Criteria.EQUALS));

				if (FormatHelper.hasContent(product.getValue(familyKey).toString()))
				{
					//System.out.println("006A ---- In AgronSetProdDimensons:::setProductDimensions");
					if (product.getValue(familyKey).toString().isEmpty())
						familyValue =" ";
					buProdDimensionsQuery.appendAndIfNeeded();
					buProdDimensionsQuery.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", familyColumn,familyValue, Criteria.EQUALS));
				}
				if ( FormatHelper.hasContent(product.getValue(packCountKey).toString()) ){
					//System.out.println("006B ---- In AgronSetProdDimensons:::setProductDimensions");
					if (product.getValue(packCountKey).toString().isEmpty())
						packCountValue =" ";
					buProdDimensionsQuery.appendAndIfNeeded();
					buProdDimensionsQuery.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", packCountColumn,packCountValue, Criteria.EQUALS));
				}
				if ( FormatHelper.hasContent(product.getValue(packTypeKey).toString()) ){
					//System.out.println("006C---- In AgronSetProdDimensons:::setProductDimensions");
					if (product.getValue(packTypeKey).toString().isEmpty())
						packTypeValue =" ";
					buProdDimensionsQuery.appendAndIfNeeded();
					buProdDimensionsQuery.appendCriteria(new Criteria("LCSLIFECYCLEMANAGED", packTypeColumn,packTypeValue, Criteria.EQUALS));
				}
				
				if(DEBUG) {
					System.out.println("004 ---- In AgronSetProdDimensons:::setProductDimensions::: genderValue ||  silhouetteValue||familyValue|| packCountValue||packTypeValue -->  "+genderValue+"||"+silhouetteValue+"||"+familyValue+"||"+packCountValue+"||"+packTypeValue);
					System.out.println("005 ---- In AgronSetProdDimensons:::setProductDimensions Query -->  "+buProdDimensionsQuery);
				}
				
				prodDimensionBU = LCSQuery.runDirectQuery(buProdDimensionsQuery).getResults();
								
				if(DEBUG)
					System.out.println("006 ---- In AgronSetProdDimensons:::setProductDimensions::Query Result Size = "+prodDimensionBU.size());
				if(prodDimensionBU!=null) {
					if (prodDimensionBU.size() == 1){
						businessObjectsItr = prodDimensionBU.iterator();
						while(businessObjectsItr.hasNext()) {
							FlexObject buFlexObj  = (FlexObject)businessObjectsItr.next();
							String oid = buFlexObj.getString("LCSLIFECYCLEMANAGED.IDA2A2");
							LCSLifecycleManaged businessObj = (LCSLifecycleManaged)LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:" + oid);
							if(DEBUG){
								System.out.println("007 ---- In AgronSetProdDimensons:::setProductDimensions ::: Business Object Name is -->"+businessObj.getName());
								System.out.println("008 ---- In AgronSetProdDimensons:::setProductDimensions::: Business Oject Values packagedProductLengthKey ||  packagedProductWidthKey||packagedProductHeightKey|| packagedProductWeightKey||heightKey ||widthtKey || depthKey"+businessObj.getValue(packagedProductLengthKey).toString()+"||"+businessObj.getValue(packagedProductWidthKey).toString()+"||"+businessObj.getValue(packagedProductHeightKey).toString()+"||"+businessObj.getValue(packagedProductWeightKey).toString()+"||"+businessObj.getValue(heightKey).toString()+"||"+businessObj.getValue(widthtKey).toString()+"||"+businessObj.getValue(depthKey).toString());
								
							}
							product.setValue(packagedProductLengthKey, businessObj.getValue(packagedProductLengthKey).toString());
							product.setValue(packagedProductWidthKey, businessObj.getValue(packagedProductWidthKey).toString());
							product.setValue(packagedProductHeightKey, businessObj.getValue(packagedProductHeightKey).toString());
							product.setValue(packagedProductWeightKey, businessObj.getValue(packagedProductWeightKey).toString());
							product.setValue(heightKey, businessObj.getValue(heightKey).toString());
							product.setValue(widthtKey, businessObj.getValue(widthtKey).toString());
							product.setValue(depthKey, businessObj.getValue(depthKey).toString());
						}
					}else{
						System.out.println("009 ---- In AgronSetProdDimensons:::setProductDimensions query result has more than 1 match");
					}
				}else {
					System.out.println("010 ---- In AgronSetProdDimensons:::setProductDimensions..No results");
				}					
				System.out.println("100 ---- In AgronSetProdDimensons:::setProductDimensions");
					
			//}
		}catch(WTException e){
			LCSLog.stackTrace(e);
		}
	}		
}

