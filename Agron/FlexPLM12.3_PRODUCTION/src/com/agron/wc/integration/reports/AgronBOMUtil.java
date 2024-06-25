package com.agron.wc.integration.reports;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.document.ImageUtil;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.LCSFindFlexBOMHelper;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flexbom.gen.BomDataGenerator;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.ProductHeaderQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;

public class AgronBOMUtil {

   private static final String COLORWAY_ARTICLE_KEY= LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.skuArticleKey", "agrArticle");
	private static final String PRODUCT_WORK_KEY= LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.productWorkKey", "agrWorkOrderNoProduct"); 
	private static final String BOMLINK_SECTION_KEY= LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.bomLinkSectionKey", "section"); 
	private static final String MATERIAL_UOM_KEY= LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.materialUnitOfMeasureKey", "msUnitOfMeasure"); 
	
	private static final boolean ACTIVE_SEASONS_ONLY= LCSProperties.getBoolean("com.agron.wc.reports.AgronMaterialAggregationReport.activeSeasonOnly",true); 
   
	private static String BOMLINK_TABLE_USED_ATTRIBUTES=LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.bomLinkTableUsedAttributes","section,partName,agrMaterialRefNumber,materialDescription,supplierDescription,agrSupplierRefNumber,colorDescription,msUnitOfMeasure,materialPrice,quantity");

	public static final String SSLINK_FORECAST_QTY_SKU_KEY = LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.skuSeasonforecastedQtyKey","agrForecastedQtySKU");

	public static final String BOMLINK_QUANTITY_KEY = LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.bomLinkQuantityKey","quantity");

	public static final String SSLINK_COLORWAY_STATUS = LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.skuSeasonColorwayStatusKey","agrColorwaystaus");

	
	
    private static Map<String, Collection<LCSSKU> > skuCollectionsCache=new HashMap();
    private static Map<String, String> bomLinkSectionKeyValueMap=new HashMap();
    private static Map<String, String > uomKeyValueCache=new HashMap();
    private static Map<String, String > materialTypeKeyValueCache=new HashMap();
    private static final String MATERIAL_FLEX_TYPE_PATH="Material";
    private static final String BOM_FLEX_TYPE_PATH="BOM\\Materials";
    private static List<String> usedAttributes=new ArrayList();
    
    private static  Map<String,Map<String, String>> keyValueCacheMap=new HashMap();
    
    private static final Logger LOGGER = LogManager.getLogger(AgronMaterialAggregationReport.class);
	

	 private static Map<String,AttributeValueList> aatributeValueListMap=new HashMap<String, AttributeValueList>();
	 private static Map<String,FlexType> flexTypeMap=new HashMap<String, FlexType>();
	
	 
	 static {
		 if(FormatHelper.hasContent(BOMLINK_TABLE_USED_ATTRIBUTES)) {
			 usedAttributes=new ArrayList(Arrays.asList(BOMLINK_TABLE_USED_ATTRIBUTES.split(","))); 
		 }
		 setupSingleListKeyValueCahce();
	 }
	
	 @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	 public static List getBomLinksByBom( FlexBOMPart part) throws WTException,Exception{
		 ArrayList applicableBomLinks = new ArrayList();
		 try{
			 part =  (FlexBOMPart) VersionHelper.getOriginalCopy((FlexBOMPart) VersionHelper.latestIterationOf((part).getMaster()));
			 String partFlexTypeId=part.getFlexType().getIdNumber();
			 // Find the Information about where the BOM is used
			 Collection<FlexObject> bomWhereUsedcoll = FlexSpecQuery.componentWhereUsed(part.getMaster());
			 LinkedHashSet<String> specRepeatCheckCollection = new LinkedHashSet<String>();
			 for(FlexObject bomWhereUsedFO : bomWhereUsedcoll){
				 String seasonBranchID = bomWhereUsedFO.getString("SPECTOLATESTITERSEASON.SEASONBRANCHID");
				 if(!FormatHelper.hasContent(seasonBranchID)){
				   //SKIP - No Season for BOM
					 continue;
				 }
                LCSSeason season = (LCSSeason) VersionHelper.latestIterationOf(((LCSSeason)LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:"+seasonBranchID)).getMaster());
				 if(ACTIVE_SEASONS_ONLY && !season.isActive()) {
					//SKIP Inactive season
					 continue;
				 }
				 String prodBranchId = bomWhereUsedFO.getString("LCSPRODUCT.BRANCHIDITERATIONINFO");
				 String sourceMaster = bomWhereUsedFO.getString("LCSSOURCINGCONFIG.IDA3MASTERREFERENCE");
				 String sourceBranchId = bomWhereUsedFO.getString("LCSSOURCINGCONFIG.BRANCHIDITERATIONINFO");
				 String seasonProductSourceHash = seasonBranchID + "-" + prodBranchId + "-" + sourceBranchId;
				 LCSSourcingConfig source = (LCSSourcingConfig) VersionHelper.latestIterationOf(((LCSSourcingConfig)LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSSourcingConfig:"+sourceBranchId)).getMaster());

				 if (specRepeatCheckCollection.contains(seasonProductSourceHash)) {
                 //The combination of Season - Product - Source is already processed for this BOM since Same BOM present in multiple Specification. Ignoring the records
				 continue;
				 }
				 specRepeatCheckCollection.add(seasonProductSourceHash);
				 LCSProduct product = (LCSProduct) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:"+prodBranchId);
				 if(!FormatHelper.hasContent(prodBranchId) || !FormatHelper.hasContent(sourceMaster) || product == null){
					 LOGGER.warn("SKIP Corrupt BOM - prodBranchId " + prodBranchId + "sourceMaster - " + sourceMaster);
					 continue; // Data is corrupt , move to next records
				 }
				 Collection<LCSSKU> skuColl=findSKUsByProductSeason(product,season);
				 Collection<FlexObject> bomData = new ArrayList();
				 for(LCSSKU sku : skuColl){
					 bomData.clear();
					 bomData = null;
					 sku = (LCSSKU) VersionHelper.latestIterationOf((LCSSKU) VersionHelper.getVersion( (sku.getMaster()),"A"));
					 String skuMasterid = FormatHelper.getObjectId((WTObject) sku.getMaster());
					 if(!FormatHelper.hasContent(skuMasterid)){
						 //LOGGER.info("Cannot Find SKU master for sku -" +sku);
						 continue; // Data is corrupt , move to next records
					 }
					 String skuMasterNumeric = FormatHelper.getNumericObjectIdFromObject((WTObject) sku.getMaster());
					 try {
						 bomData = LCSFindFlexBOMHelper.findBOM(part,skuMasterid,sourceMaster,null,null,null,"SINGLE",null,new Boolean(true),null,usedAttributes);
					 } catch(Exception ex) {
						 //ex.printStackTrace();
						 LOGGER.info("Error occured-LCSFindFlexBOMHelper.findBOM ::"+part.getName()+"::"+part.getBranchIdentifier()+"::SKU ID::"+sku.getName()+"skuMasterid>>"+skuMasterid+"::Source Master::"+sourceMaster+"::"+ex.getMessage());
						 continue;
					 }
					 bomData=BomDataGenerator.groupDataToBranchId((Collection)bomData, "BRANCHID", "MASTERBRANCHID", "SORTINGNUMBER");
					 for(FlexObject bomLinkFO : bomData){
						//FlexBOMLink bomlink = (FlexBOMLink)LCSQuery.findObjectById("OR:com.lcs.wc.flexbom.FlexBOMLink:" + bomLinkFO.getString("FLEXBOMLINKID"));
						 Float ConsolidatedQty   = 0.0f;
						 long forecastedQtySKU=0;
						 String colorwayStatus="";
						 Float quantity =0.0f;
						 try {
							 LCSSeasonProductLink spl = LCSSeasonQuery.findSeasonProductLink(sku, season);
							 FlexType flexType_splink = spl.getFlexType();
							 
							 if(spl.getValue(SSLINK_FORECAST_QTY_SKU_KEY) !=null) {
								 forecastedQtySKU=(long)spl.getValue(SSLINK_FORECAST_QTY_SKU_KEY);
								 quantity =bomLinkFO.getFloat(BOMLINK_QUANTITY_KEY);
								 if(forecastedQtySKU !=0 && quantity !=null && quantity !=0.0f) {
									 ConsolidatedQty=forecastedQtySKU*bomLinkFO.getFloat(BOMLINK_QUANTITY_KEY);
								 }  
							 }
							 if(spl.getValue(SSLINK_COLORWAY_STATUS) !=null) {
								String colorwayStatusKey=(String) spl.getValue(SSLINK_COLORWAY_STATUS);
								 colorwayStatus=getDisplayValue(spl, colorwayStatusKey,SSLINK_COLORWAY_STATUS);
							 }

						 }catch(Exception e) {
                        	LOGGER.error("Error while calculating forecasted value>>>forecastedQtySKU:"+forecastedQtySKU+"::quantity>>"+quantity+":"+e.getMessage());
                         }
						 String matrialFlexTypeId="0";
						 try {
							 String materialMaster = "";
							 String materialSupplierBranchid = "";
							 String materialSupplierMasterId = "";
							 String supplierMaster = "";
							 if(bomLinkFO.containsKey("CHILDID$SKU$"+skuMasterNumeric)){
								 materialMaster = bomLinkFO.getString("CHILDID$SKU$"+skuMasterNumeric);
							 }else{
								 materialMaster = bomLinkFO.getString("CHILDID");
							 }
							 
							 if(bomLinkFO.containsKey("MATERIALSUPPLIERBRANCHID")){
								 materialSupplierBranchid = bomLinkFO.getString("MATERIALSUPPLIERBRANCHID");
							 }
							 if(bomLinkFO.containsKey("MATERIALSUPPLIERMASTERID")){
								 materialSupplierMasterId = bomLinkFO.getString("MATERIALSUPPLIERMASTERID");
							 }
							 if(bomLinkFO.containsKey("SUPPLIERMASTERID")){
								 supplierMaster = bomLinkFO.getString("SUPPLIERMASTERID");
							 }
							 
							 String materialName="";
						
							 if(FormatHelper.hasContent(materialMaster)){
								 LCSMaterialMaster materialPartMaster = (LCSMaterialMaster) LCSQuery.findObjectById("OR:com.lcs.wc.material.LCSMaterialMaster:"+materialMaster);
								 if(materialPartMaster == null) continue; // Data corrupt skip
								 LCSMaterial material = (LCSMaterial) VersionHelper.latestIterationOf(materialPartMaster);
								 materialName=material.getName();
								 bomLinkFO.put("MATERIALNAME",materialName);
								 						 						 
								 
								 matrialFlexTypeId=material.getFlexType().getIdNumber();
							 } 
							 if(!FormatHelper.hasContent(materialName) || "PLACEHOLDER".equalsIgnoreCase(materialName) || !FormatHelper.hasContent((String)bomLinkFO.get(BOMLINK_SECTION_KEY))) {
								// LOGGER.debug("SKIP the record with empty material/section>>>"+bomLinkFO); 
								 continue;
							 }	
							
							 if(FormatHelper.hasContent(materialSupplierBranchid)){
								 LCSMaterialSupplier materialSupplier = (LCSMaterialSupplier) LCSQuery.findObjectById("VR:com.lcs.wc.material.LCSMaterialSupplier:"+materialSupplierBranchid);
								 								 
								 if(materialSupplier.isPlaceholder()) {
									 LCSMaterialSupplierMaster msMaster = (LCSMaterialSupplierMaster)LCSQuery.findObjectById("OR:com.lcs.wc.material.LCSMaterialSupplierMaster:"+materialSupplierMasterId);
									 materialSupplier = (LCSMaterialSupplier) VersionHelper.latestIterationOf(msMaster);
								 }
								 
								 
								 if(materialSupplier != null) {
									 									 
									 materialSupplier = (LCSMaterialSupplier) VersionHelper.latestIterationOf(materialSupplier);
									 							 
									 								 
									 String agrSustainabilityCategory=(String) materialSupplier.getValue("agrSustainabilityCategory");
									 agrSustainabilityCategory = getDisplayValue(materialSupplier, agrSustainabilityCategory, "agrSustainabilityCategory");
									 bomLinkFO.put("AGRSUSTAINABILITYCATEGORY",agrSustainabilityCategory);
								
									 
									 String agrStatus=(String) materialSupplier.getValue("agrStatus");
									 agrStatus = getDisplayValue(materialSupplier, agrStatus, "agrStatus");		
									 bomLinkFO.put("AGRSTATUS",agrStatus);
									 
									 if((FormatHelper.hasContent(String.valueOf(materialSupplier.getValue("agrTestingExpirationDate"))))){
									 Date agrTestingExpirationDate =(Date) materialSupplier.getValue("agrTestingExpirationDate");
									 DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");  
									 String stragrTestingExpirationDate = dateFormat.format(agrTestingExpirationDate);
									 bomLinkFO.put("AGRTESTINGEXPIRATIONDATE",stragrTestingExpirationDate);
									 }else {
										 bomLinkFO.put("AGRTESTINGEXPIRATIONDATE",""); 
									 }
									 
									 if((FormatHelper.hasContent(String.valueOf(materialSupplier.getValue("agrExpirationDate"))))){
										 Date agrExpirationDate =(Date) materialSupplier.getValue("agrExpirationDate");
										 DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");  
										 String stragrExpirationDate = dateFormat.format(agrExpirationDate);
										 bomLinkFO.put("AGREXPIRATIONDATE",stragrExpirationDate);
										 }else {
											 bomLinkFO.put("AGREXPIRATIONDATE",""); 
										 }
								 }
								
							 } 
							 
						 }catch(Exception e) {
							 LOGGER.error("Error while getting material Name>>>"+bomLinkFO+"::"+e.getMessage()); 
							 continue;
						 }
                         bomLinkFO.put(SSLINK_FORECAST_QTY_SKU_KEY.toUpperCase(), forecastedQtySKU);
                         bomLinkFO.put(SSLINK_COLORWAY_STATUS.toUpperCase(), colorwayStatus);
                         bomLinkFO.put("CONSOLIDATEDQTY", ConsolidatedQty);
						 bomLinkFO.put("SEASON", season.getValue("seasonName"));
						 bomLinkFO.put("PRODUCT", product.getValue("productName")); 
						 
						 String agrCollectionProduct=(String) product.getValue("agrCollectionProduct");
						 agrCollectionProduct = getDisplayValue(product, agrCollectionProduct, "agrCollectionProduct");						 
						 bomLinkFO.put("COLLECTION", agrCollectionProduct); 						 
						 String agrSilhouetteProd = (String) product.getValue("agrSilhouetteProd");
						 agrSilhouetteProd = getDisplayValue(product, agrSilhouetteProd, "agrSilhouetteProd");						 
						 bomLinkFO.put("SILHOUETTE", agrSilhouetteProd); 
						 bomLinkFO.put("PRODUCTTYPE", product.getFlexType().getTypeDisplayName()); 
						 bomLinkFO.put("WORK#", product.getValue(PRODUCT_WORK_KEY));
						 bomLinkFO.put("SOURCE", source.getSourcingConfigName());
						 LCSSupplier supplier = (LCSSupplier) source.getValue("agrFactory");
						 if(supplier != null) {
							 bomLinkFO.put("FACTORY", supplier.getName());
						 }else {
							 bomLinkFO.put("FACTORY","");
						 }
						 
						 bomLinkFO.put("SKU", sku.getValue("skuName"));
						 bomLinkFO.put("ARTICLE#", sku.getValue(COLORWAY_ARTICLE_KEY));
						 bomLinkFO.put("BOM", part.getName()); 
						 bomLinkFO.put(MATERIAL_UOM_KEY,getDisplayValueFromCache((String)bomLinkFO.get(MATERIAL_UOM_KEY),matrialFlexTypeId+"_"+MATERIAL_UOM_KEY)); 
						 bomLinkFO.put(BOMLINK_SECTION_KEY,getDisplayValueFromCache((String)bomLinkFO.get(BOMLINK_SECTION_KEY),partFlexTypeId+"_"+BOMLINK_SECTION_KEY));
						  applicableBomLinks.add(bomLinkFO);
					 }
				
				 }
			 }
			 bomWhereUsedcoll.clear();
			 bomWhereUsedcoll = null;
		 }catch(Exception ex){
			 ex.printStackTrace();
			 throw ex;
		 }
		 LOGGER.info("getBomLinksByBom>>>"+part.getName()+":: applicableBomLinks>>"+applicableBomLinks.size());
		 return applicableBomLinks;
	 }
	 
	 
	 public static ArrayList getAllBomLinkRows(Collection<FlexBOMPart>  productBOMs) {
		 LOGGER.info("getAllBomLinkRows START>>>>>>>>>Total BOMS::"+productBOMs.size());
		 ArrayList allRows = new ArrayList();
		 int i=0;	
		 for(FlexBOMPart bom : productBOMs) {
			 i++;
			 try {
				 LOGGER.info(i+" ["+productBOMs.size()+"]::PROCESSING BOM>>"+bom.getName()+"::BOMID>>"+bom.getBranchIdentifier());
				 allRows.addAll(getBomLinksByBom(bom));
			 }catch(Exception e) {
				 LOGGER.info("Exception while adding BOMLINKS for  BOM:"+bom.getName()+"::"+bom.getBranchIdentifier()+":"+e.getMessage(),e);
				 e.printStackTrace();
			 }

		 }
		 LOGGER.info("getAllBomLinkRows END::BOMLINKS>>>"+allRows.size()+"::BOMS>>"+productBOMs.size());
		 return allRows;
	 }
	 
	 public static Collection findAllBoms(Collection<LCSProduct> allProducts) throws Exception {
			LOGGER.info("findAllBoms START>>>>>>>>>>>>>>>>>PRODUCTS::"+allProducts.size());
	         Collection<FlexBOMPart>  allBoms=new ArrayList<FlexBOMPart>();
	         int i=0;
	         for(LCSProduct product : allProducts) {
	        	 i++;
	        	 String workNo=String.valueOf(product.getValue(PRODUCT_WORK_KEY));
	        	 try {
	        		 if(!FormatHelper.hasContent(workNo) || workNo.startsWith("-")){
	        			 LOGGER.info("SKIP -ve WORK#>>>"+workNo);
	        			 continue;
	        		 }
	        		 
	        		 Collection<FlexBOMPart>  productBOMs = new LCSFlexBOMQuery().findBOMPartsForOwner(product, "A", "MAIN");
	        		 LOGGER.info(i+" ["+allProducts.size()+"]::PROCESSING PRODUCT:"+product.getName()+"::BOMS FOUND>>"+productBOMs.size());
	        		 allBoms.addAll(productBOMs);
	        	 }catch(Exception e) {
	        		 LOGGER.error("Exception while adding product BOMS>>>PRODUCT:"+product.getName()+"::work# >>"+workNo+":"+e.getMessage(),e);	
	        	    e.printStackTrace();
	        	 }
	         }
	         LOGGER.info("findAllBoms END>>>>>>>>>>>>>>>>>BOMS>>::"+allBoms.size()+"::PRODUCTS>>"+allProducts.size());
			return allBoms;
		}
	    
	    public static Collection<LCSSKU> findSKUsByProductSeason(LCSProduct product,LCSSeason season) {
	    	Collection<LCSSKU> skuColl=new Vector();
			String seasonProductHash = product.getBranchIdentifier()+"-"+season.getBranchIdentifier();
			try {
				if(skuCollectionsCache.containsKey(seasonProductHash)) {
					skuColl=skuCollectionsCache.get(seasonProductHash);
				}else {
					skuColl = new ProductHeaderQuery().findSKUs(product,null,season,true,false);
					skuCollectionsCache.put(seasonProductHash, skuColl);	
				}
			}catch(Exception e) {
	    		LOGGER.info("error in findSKUsByProductSeason::"+seasonProductHash+"::"+e.getMessage());
	    	}
			 return skuColl;
	    }
	    
	    
	    
	    public static void setupSingleListKeyValueCahce() {
	    	LOGGER.info("setupSingleListKeyValueCahce START>>>>>>>>>>>>>");
	    	try {
	    		
	    		FlexType materialFlexType = FlexTypeCache.getFlexTypeFromPath(MATERIAL_FLEX_TYPE_PATH); 
                keyValueCacheMap.put(materialFlexType.getIdNumber()+"_"+MATERIAL_UOM_KEY,materialFlexType.getAttribute(MATERIAL_UOM_KEY).getAttValueList().toLocalizedMapAll(Locale.getDefault()));
	    		materialFlexType.getAllChildren().forEach(type->{
	    			try {
	    				keyValueCacheMap.put(type.getIdNumber()+"_"+MATERIAL_UOM_KEY, type.getAttribute(MATERIAL_UOM_KEY).getAttValueList().toLocalizedMapAll(Locale.getDefault()));
	    			} catch (WTException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			} 
	    		}); 
	    		LOGGER.info("uomKeyValueCache>>>"+uomKeyValueCache);
	    		FlexType bomFlexType = FlexTypeCache.getFlexTypeFromPath(BOM_FLEX_TYPE_PATH);
	    		keyValueCacheMap.put(bomFlexType.getIdNumber()+"_"+BOMLINK_SECTION_KEY,bomFlexType.getAttribute(BOMLINK_SECTION_KEY).getAttValueList().toLocalizedMapAll(Locale.getDefault()));
				
	    		bomFlexType.getAllChildren().forEach(type->{
	    			try {
	    				keyValueCacheMap.put(type.getIdNumber()+"_"+BOMLINK_SECTION_KEY, type.getAttribute(BOMLINK_SECTION_KEY).getAttValueList().toLocalizedMapAll(Locale.getDefault()));
	    			} catch (WTException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			} 
	    		}); 
	    		
	    		LOGGER.info("keyValueCacheMap>>>"+keyValueCacheMap);
	    	}catch(Exception e) {
	    		e.printStackTrace();
	    	}
	    	LOGGER.info("setupSingleListKeyValueCahce END>>>>>>>>>>>>>");    
	    }  
	    
	   public static String getDisplayValueFromCache(String key,String cacheKey){
		   if(cacheKey !=null && keyValueCacheMap !=null && keyValueCacheMap.containsKey(cacheKey) ) {
			   Map<String, String>   keyValueMap=keyValueCacheMap.get(cacheKey);
			   if(FormatHelper.hasContent(key) && keyValueMap !=null && keyValueMap.containsKey(key)) {
				   return  keyValueMap.get(key);   
			   }
		   }
	       return key;
	    }
	   
	   
	   
	   /** Returns key values for single list attribute display values
	     *   @param flexType
	     *   @param display - String
	     * , @param attLabel - String
	     *   throws WTException
	     */
	    public static String getDisplayValue(LCSSeasonProductLink sslink, String keyValue,String attKey) throws WTException {
	        String attValue = "";
	        if(FormatHelper.hasContentAllowZero(keyValue) && sslink != null)
	        {
	            FlexTypeAttribute flexAtt = null;
	            AttributeValueList attList = null;
	            
	            flexAtt = sslink.getFlexType().getAttribute(attKey);
	            attList = flexAtt.getAttValueList();
	            attValue = attList.getValue(keyValue,Locale.getDefault());
	        }
	        return attValue;
	    }
	    
	    /** Returns key values for single list attribute display values
	     *   @param flexType
	     *   @param display - String
	     * , @param attLabel - String
	     *   throws WTException
	     */
	    public static String getDisplayValue(FlexTyped type, String keyValue,String attKey) throws WTException {
	        String attValue = "";
	        if(FormatHelper.hasContentAllowZero(keyValue) && type != null)
	        {
	            FlexTypeAttribute flexAtt = null;
	            AttributeValueList attList = null;
	            
	            flexAtt = type.getFlexType().getAttribute(attKey);
	            attList = flexAtt.getAttValueList();
	            attValue = attList.getValue(keyValue,Locale.getDefault());
	        }
	        return attValue;
	    }
	    
}
