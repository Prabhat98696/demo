package com.agron.wc.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.product.AgronProductQuery;
import com.lcs.wc.color.LCSColorLogic;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.LookupTableHelper;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.color.LCSColorQuery;

import wt.util.WTException;



public class QueryBuilderAttributeTransformUtil {
	
	private static final String NRF_MOA_PATH = LCSProperties.get("com.agron.wc.integration.moaNRFSize.FlexTypePath");
	private static final String PRODUCT_GENDER_KEY = LCSProperties.get("com.agron.wc.interface.product.genderKey");
	private static final String MOA_GENDER_KEY = LCSProperties.get("com.agron.wc.interface.moa.genderKey");
	private static final String COLORWAY_MOA_SIZE_KEY = LCSProperties.get("com.agron.wc.interface.attributes.colorway.moa.sizesKey");
	private static final String MOA_SIZE_KEY = LCSProperties.get("com.agron.wc.interface.moa.sizeKey");
	private static final String MOA_NRFSIZE_KEY = LCSProperties.get("com.agron.wc.interface.moa.nrfSizeKey");
	private static final String MOA_NRFDESC_KEY = LCSProperties.get("com.agron.wc.interface.moa.nrfDescKey");
	private static final String MOA_PRODUCTTYPE_KEY = LCSProperties.get("com.agron.wc.interface.moa.productTypeKey");
	
	 private static Map<String,AttributeValueList> aatributeValueListMap=new HashMap<String, AttributeValueList>();
	 private static Map<String,FlexType> flexTypeMap=new HashMap<String, FlexType>();
	 public static final Logger log = LogManager.getLogger(QueryBuilderAttributeTransformUtil.class);
	 private static LCSColorLogic colorLogic = new LCSColorLogic();
	 public static String parseOutDelims(String key) {
		 return MOAHelper.parseOutDelims(key, ",");
	 }
	 static Map<String, String> moaNRFSizeMap = new HashMap<String, String>();
	 static{
			try{
				moaNRFSizeMap= getMoaMapNRFSize();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	 
	 public static AttributeValueList getAttValueList(String key,String typeId) {
		 AttributeValueList avl=null;
		 try {
			 avl=aatributeValueListMap.get(typeId+"_"+key);
			 if(avl ==null) {
				 FlexType flexType =  flexTypeMap.get(typeId);
				 if(flexType==null) {
					 flexType = FlexTypeCache.getFlexType(typeId);
					 flexTypeMap.put(typeId, flexType);
				 }
				 avl=flexType.getAttribute(key).getAttValueList(); 
				 aatributeValueListMap.put(typeId+"_"+key, avl);
			 }	 
		 }catch(Exception e) {
	      log.error("Exception in getAttValueList:Key :Type>>> "+key+":"+typeId+"::"+e.getMessage());
		 }
		 return avl;
	 }
	 
	 
	  /** Transform Multi List Attribute Value 
	  * @param columnValue
	  * @param key
	  * @param typeId
	  * @return String 
	  */
	 public static String transformMultiListValue(String columnValue,String key,String typeId) {
		 if(FormatHelper.hasContent(columnValue) && FormatHelper.hasContent(key) && FormatHelper.hasContent(typeId)) {
			 AttributeValueList avl=getAttValueList(key,typeId);
			 if(avl !=null) {
				 ArrayList<String> valueCollection = new ArrayList<String>((MOAHelper.getMOACollection(columnValue)));
				 String transformedValue="";
				 String tempValue="";
				 for(int i=0; i<valueCollection.size(); i++) {
					 String eKey=valueCollection.get(i);
					 if(FormatHelper.hasContent(eKey)) {
						 try {
							 tempValue =avl.getValue(eKey,Locale.getDefault());   
						 }catch(Exception e) {
							 e.printStackTrace(); 
						 }
						 if(i !=0) {
							 transformedValue = transformedValue+", ";
						 }
						 transformedValue = transformedValue + tempValue; 
					 }

				 } 
				 return transformedValue;
			 }
		 }
		 return parseOutDelims(columnValue);

	 }
	 
	  /**Covert hexValue to RGB 
	  * @param columnValue
	  * @return String 
	  */
	 public static String hex2RGB(String hexValue) {
		 String redString = null;
		 String greenString = null;
		 String blueString = null;
		 if(FormatHelper.hasContent(hexValue) && hexValue.length()==6 ) {
			 redString=Integer.toString(colorLogic.toDex(hexValue.substring(0,2)));
			 greenString=Integer.toString(colorLogic.toDex(hexValue.substring(2,4)));
			 blueString=Integer.toString(colorLogic.toDex(hexValue.substring(4,6)));
		 }
		 return redString+"-"+greenString+"-"+blueString;
	 }
	 
	 
	 /**Covert Send value If catalog contain Team  
	  * @param columnValue
	  * @return String 
	  */
	 public static String getCatalogContentTeam(String cataLog) {
		 String catalogTeam = "No";
		 if(FormatHelper.hasContent(cataLog)) {
			 if(cataLog.contains("Team")){
				 catalogTeam ="Yes";
			 }
		 }
		 return catalogTeam;
	 }
	 
	  /**Covert Value of Brand  
	  * @param columnValue
	  * @return String 
	  */
	 public static String getBrandValueConvert(String brand) {
		 String brandValue = "";
		 if(FormatHelper.hasContent(brand)) {
			 if(brand.equalsIgnoreCase("agrBOS") || brand.equalsIgnoreCase("agrCore")){
				 brandValue ="Sport Performance ";
			 }else if(brand.equalsIgnoreCase("agrOriginals")){
				 brandValue = "Originals";
			 }
		 }
		 return brandValue;
	 }
	
	 /**
	  * getNRFSizeValueFromBO
	  * @param workNumber
	  * @param size
	  * @return
	  */
	 
	 
	 public static String getNRFSizeValueFromBO(String workNumber, String size, String nrfValue) {
		 String moaSizeValueKey = "";
		 String sizeValue="";
	 	 String productGen= "";
	 	 try{
			 FlexType flexType_Product = FlexTypeCache.getFlexTypeFromPath("Product");
			 AgronProductQuery query = new AgronProductQuery();
			 Collection attList = new ArrayList();
			 attList.add("agrWorkOrderNoProduct");

			 Map<String, String> map = new HashMap<String, String>();
			 map.put("displayAttribute", "agrWorkOrderNoProduct");
			 map.put("quickSearchCriteria", workNumber);

			 SearchResults results = new SearchResults();
			 results = query.findProductsByCriteria((map), flexType_Product, attList, null, null, "agrWorkOrderNoProduct");

			 Collection<FlexObject> prodObj = results.getResults();
			 LCSProduct product = null;
			 if(prodObj.size() > 0) {
				 FlexObject prObj = prodObj.iterator().next();
				 String branchId = prObj.getString("LCSPRODUCT.BRANCHIDITERATIONINFO");
				  product = (LCSProduct) VersionHelper.latestIterationOf(((LCSProduct)LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:"+branchId)).getMaster());
				 System.out.println("PRODUCT NAME :: "+product.getName());
			 }

			 productGen =  (String) product.getValue(PRODUCT_GENDER_KEY);
			 
			 String productType = getProductType(product);
			 moaSizeValueKey = productType+"_"+productGen+"_"+size;
			// System.out.println("moaSizeValueKey >>>>>>>>>>>>: "+moaSizeValueKey);
			// System.out.println("nrfSize >>>>>>>>>>>>: "+nrfValue);
			 if(moaNRFSizeMap.containsKey(moaSizeValueKey) && "NRFSIZE".equalsIgnoreCase(nrfValue)){
				 String obValuesSizeandDiscreption = moaNRFSizeMap.get(moaSizeValueKey);
				 sizeValue=obValuesSizeandDiscreption.split("_")[0];
			 }else if(moaNRFSizeMap.containsKey(moaSizeValueKey) && "NRFCODE".equalsIgnoreCase(nrfValue)){
				 String obValuesSizeandDiscreption = moaNRFSizeMap.get(moaSizeValueKey);
				 sizeValue=obValuesSizeandDiscreption.split("_")[1];
			 }

		 }catch(WTException e){
			 e.printStackTrace();
		 }

		 return sizeValue;
	 }
	 
	 
	 
	 
	 /**
	  * MOA Create Moa Map of NRf Value from MOA 
	  * Ex. SOCK_Men's_L=234565;
	  * @return
	  */
	 public static HashMap<String,String> getMoaMapNRFSize() {
		HashMap<String,String> NRFMoaRowMap=new HashMap<String,String>();
		try {
			
			FlexType agronProductBSObj = FlexTypeCache.getFlexTypeFromPath(NRF_MOA_PATH);  
			LookupTableHelper tableHelpr = new LookupTableHelper();

			LCSMOATable moaNRFSize = tableHelpr.getLookupTable(agronProductBSObj,  "NRF Size and Description", "agrNRFSizes");
			Collection moaRows= moaNRFSize.getRows();
			
			if (moaRows !=null && moaRows.size()>0) {
				Iterator moaIterator = moaRows.iterator();
				while (moaIterator.hasNext()) {

					FlexObject moaData = (FlexObject) moaIterator.next();
					String moaflexType=moaData.getData(NRF_MOA_PATH) ;
					String moaProdTypeKey=moaData.getData(MOA_PRODUCTTYPE_KEY) ;
					String moaGender=moaData.getData(MOA_GENDER_KEY) ;
					String moaSize=moaData.getData(MOA_SIZE_KEY) ;
					String agronSizeKey = "";
					String nrfSizeValue=moaData.getData(MOA_NRFSIZE_KEY) ;
					String nrfSizeDiscription=moaData.getData(MOA_NRFDESC_KEY) ;
					
					if(FormatHelper.hasContent(moaGender)&&FormatHelper.hasContent(moaSize)){
						agronSizeKey = moaProdTypeKey+"_"+moaGender+"_"+moaSize;
					}

					if(agronSizeKey !=null && agronSizeKey.trim() !=""){
						NRFMoaRowMap.put(agronSizeKey, nrfSizeValue+"_"+nrfSizeDiscription);
					}
				}	
			}

		} catch (WTException e) {
			e.printStackTrace();
		}
		return NRFMoaRowMap;
	}
	 
	 
	 /**
	  * Get Ptoduct Type for MOA NRF Size
	  * @param productObj
	  * @return
	  */
	 public static String getProductType(LCSProduct productObj) {
			String productType ="";
			try{
				if("Adidas\\Socks Type\\Socks".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {	  
					productType = "SOCK";
				}else if("Adidas\\Socks Type\\Team".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false)) ) {
					productType = "SOCKTEAM"; 
				} else if("Adidas\\Backpacks".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
					productType =  "BACKPACK";  
				} else if("Adidas\\Bags".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){

					String agrSilhouetteProd = (String) productObj.getValue("agrSilhouetteProd");
					if(agrSilhouetteProd !=null && "agrBackPack".contentEquals(agrSilhouetteProd)) {
						productType="BACKPACK";
					}else {
						productType="BAG"; 
					}
				}else if("Adidas\\Headwear\\Hats".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
					productType="HEADWEAR";  
				 } else if("Adidas\\Headwear\\Knits".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
					productType="HEADWEAR";  
				 } else if("Adidas\\Underwear".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
					productType="UNDERWEAR"; 
				} else if("Adidas\\Sport Accessories".equalsIgnoreCase(productObj.getFlexType().getFullNameDisplay(false))){
					productType="SPACC"; 
				}
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return productType;
		} 
	 	 /**
	  * Get Color Object Name from color object.
	  * @param color Object ID
	  * @return
	  */
	 public static String showColorNameFromColorObject(String colourobjectrefernece) throws WTException
	 {
		 String ColorName = "";
		 if (colourobjectrefernece!=null && FormatHelper.hasContent("colourobjectrefernece"))
		 { 
			 LCSColor colorobject = (LCSColor) LCSQuery.findObjectById("OR:"+colourobjectrefernece);
			 ColorName = (String) colorobject.getValue("name");
		 }else
		 {
			  ColorName = "";
		 }
		 return ColorName;
	 }
	 
	 	 	 /**
	  * Get Color Object - Adidas reference no from color object.
	  * @param color Object ID
	  * @return
	  */
	public static String showAdidasColorRefFromColorObject(String colourobjectrefernece) throws WTException
	 {
		 String AdidasColorRef = "";
		 if (colourobjectrefernece!=null && FormatHelper.hasContent("colourobjectrefernece"))
		 { 
			 LCSColor colorobject = (LCSColor) LCSQuery.findObjectById("OR:"+colourobjectrefernece);
			 AdidasColorRef = (String) colorobject.getValue("agrAdidasRefNo");
		 }else
		 {
			  AdidasColorRef = "";
		 }
		 
		 return AdidasColorRef;
	 } 
}
