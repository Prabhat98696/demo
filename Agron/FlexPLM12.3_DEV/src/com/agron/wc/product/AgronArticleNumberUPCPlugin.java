package com.agron.wc.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agron.wc.util.AgronArticleNumberUPCHelper;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.skusize.SKUSize;
import com.lcs.wc.skusize.SKUSizeMaster;
import com.lcs.wc.skusize.SKUSizeQuery;
import com.lcs.wc.skusize.SKUSizeToSeason;
import com.lcs.wc.skusize.SKUSizeUtility;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * SSP for assigning Article/UPC #(s).
 * 
 * @author Sunil Murthy (updated version from Doug Ford)
 * @version 1.1
 */
public final class AgronArticleNumberUPCPlugin {

	// //////////////////////////////////////////////////////////////////////////
	/** alphaValues field */
	private static final String[] alphaValues = { "A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
			"T", "U", "V", "W", "X", "Y", "Z" };
	/** DEBUG field */
	private static final boolean DEBUG = LCSProperties.getBoolean("com.agron.wc.product.agronArticleNumberUPCPlugin.verbose");
	/** nameColumnSearchResultIndex field */
	private static String nameColumnSearchResultIndex = null;
	/** nameColumnName field */
	private static String nameColumnName = null;
	/** articleAvailableColumnname field */
	private static String articleAvailableColumnname = null;
	/** articlePathId field */
	private static String articlePathId = null;
	/** upcAvailableColumnname field */
	private static String upcAvailableColumnname = null;
	/** upcPathId field */
	private static String upcPathId = null;
	/** ADOPTED_BOOLEAN_KEY field */
	private static final String ADOPTED_BOOLEAN_KEY = LCSProperties.get("com.agron.wc.product.SKU.adopted.attKey","agrAdopted");
	/** ARTICLE_NUMBER_BO_TYPE field */
	private static final String ARTICLE_NUMBER_BO_TYPE = LCSProperties.get("com.agron.wc.product.BO.ArticleNumber.flextype");
	/** UPC_CODE_BO_TYPE field */
	private static final String UPC_CODE_BO_TYPE = LCSProperties.get("com.agron.wc.product.BO.UPCNumber.flextype");
	/** AVAILABLE_ATT_KEY_BO field */
	private static final String AVAILABLE_ATT_KEY_BO = LCSProperties.get("com.agron.wc.product.SKU.Article.UPC.available.attKey");
	/** ARTICLE_NUMBER_SKU_ATTKEY field */
	private static final String ARTICLE_NUMBER_SKU_ATTKEY = LCSProperties.get("com.agron.wc.product.SKU.ArticleNumber.attKey","agrArticle");
	/** MOA_ARTICLE_NUMBER_KEY field */
	private static final String MOA_ARTICLE_NUMBER_KEY = LCSProperties.get("com.agron.wc.product.SKU.MOA.articleNumber.attKey");
	/** MOA_SIZE_KEY field */
	private static final String MOA_SIZE_KEY = LCSProperties.get("com.agron.wc.product.SKU.MOA.agrSize.attKey");
	/** MOA_SHOE_SIZE_KEY field */
	private static final String MOA_SHOE_SIZE_KEY = LCSProperties.get("com.agron.wc.product.SKU.MOA.agrShoeSize.attKey");
	/** MOA_UPCNUMBER_KEY field */
	private static final String MOA_UPCNUMBER_KEY = LCSProperties.get("com.agron.wc.product.SKU.MOA.agrUPCNumber.attKey");
	/** MOA_ADOPTED_KEY field */
	private static final String MOA_ADOPTED_KEY = LCSProperties.get("com.agron.wc.product.SKU.MOA.agrAdopted.attKey");
	/** SKU_MOA_KEY field */
	private static final String SKU_MOA_KEY = LCSProperties.get("com.agron.wc.product.SKU.MOA.attKey");  

	private static final String SKU_SOCKS_MOA_KEY = LCSProperties.get("com.agron.wc.product.SKU.SOCKS.MOA.attKey"); //agrUPCArticleSocks

	/** LCSLIFECYCLEMANAGED field */
	private static final String LCSLIFECYCLEMANAGED = "LCSLIFECYCLEMANAGED";

	/** LCSLIFECYCLEMANAGED field */
	private static final String MOA_FOB_KEY = LCSProperties.get("com.agron.wc.product.MOAArticleNumberPlugin.moa.FOB.attkey");
	/** SKU_APPLICABLE_SIZES_KEY field */
	//private static final String SKU_APPLICABLE_SIZES_KEY = LCSProperties.get("com.agron.wc.product.applicableSizesKey");

	/** LOADED_SKU_KEY field */
	private static final String LOADED_SKU_KEY = LCSProperties.get("com.agron.wc.product.lcssku.loadedSkuKey");

	/** TRUE_KEY field */
	private static final String TRUE_KEY = LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.trueKey","1");;
	/** ARTICLE_SUFFIX_KEY field */
	private static final String ARTICLE_SUFFIX_KEY = LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.articleSuffix");

	private static final String SKUSIZE_ARTICLE_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.articleNumberKey","agrArticleNumber");
	private static final String SKUSIZE_UPC_NBMER_KEY= LCSProperties.get("com.agron.wc.product.AgronArticleNumberUPCPlugin.skusize.upcNumberKey","upcCode");
	private static final String YEAR_KEY = LCSProperties.get("com.agron.wc.interface.season.yearKey","year");

	public static final Logger log = LogManager.getLogger(AgronArticleNumberUPCPlugin.class);
	/** Creates a new instance of AgronArticleNumberUPCPlugin */
	private AgronArticleNumberUPCPlugin() {
	}

	
	
	public static void regenerateSKUSizes(WTObject object) throws Exception {
		
		if (object instanceof LCSSKU) {
			LCSSKU sku = (LCSSKU) object;
		LCSProduct product = com.lcs.wc.season.SeasonProductLocator.getProductARev(sku);
		String masterId = FormatHelper.getObjectId(product.getMaster());
		
		log.info("AgronArticleNumberUPCPlugin regenerateSKUSizes START>>>> masterId "+masterId);
		SKUSizeUtility.regenerateMissingSKUSizeObjects(masterId);
		log.info("AgronArticleNumberUPCPlugin regenerateSKUSizes END >>>>");
		}
			
	}

	/**
	 * Assign Article #(s) and UPC #(s). Business rules are as follows: 
	 * CASE 1 : If adidas, find Article #(s) and UPC #(s) from lookup table
	 * For single size, Article # and UPC # need to be set on SKU Sizes. 
	 * For multiple sizes, Article # will be number and letter appended.
	 * The letter should start from A, B, C, etc. UPC will need to be unique for each sizes.
	 * @param object
	 * @throws WTException
	 */
	public static void autoAssign(WTObject object) throws Exception {
		log.info("AgronArticleNumberUPCPlugin autoAssign START>>>>"+object);
		if (object instanceof LCSSKU) {
			LCSSKU sku = (LCSSKU) object;
			if (sku.getVersionIdentifier() != null&& !"A".equals(sku.getVersionIdentifier().getValue()) || sku.isPlaceholder()) {
				return;
			}
			sku = (LCSSKU) VersionHelper.getVersion((LCSSKU) VersionHelper.latestIterationOf(sku.getMaster()),"A"); 
			//int latestSeasonYear=AgronArticleNumberUPCHelper.getLatestSeasonYear(sku);
			boolean isSKUUsedInAgronSeasonType=AgronArticleNumberUPCHelper.isSKUUsedInAgronSeasonType(sku);
			boolean adoptedValue=Boolean.valueOf(String.valueOf(sku.getValue(ADOPTED_BOOLEAN_KEY)));
			String articleNumberValue= (String)sku.getValue(ARTICLE_NUMBER_SKU_ATTKEY);
			log.info(sku+"::"+sku.getName()+" ::adoptedValue::"+adoptedValue+"::articleNumberValue::+"+articleNumberValue+"::isSKUUsedInAgronSeasonType:"+isSKUUsedInAgronSeasonType);
			if (isSKUUsedInAgronSeasonType && adoptedValue) {
				boolean reAssign=false;
				if(FormatHelper.hasContent(articleNumberValue)) {
					LCSSKU predSKU = (LCSSKU) VersionHelper.predecessorOf(sku);
					log.info("predSKU>>>"+predSKU);
					if(predSKU !=null){
						boolean previousAdoptedValue=Boolean.valueOf(String.valueOf(predSKU.getValue(ADOPTED_BOOLEAN_KEY)));
						log.debug("Previouse version adoptedValue::"+previousAdoptedValue);
						if(!previousAdoptedValue) {
							reAssign=true;
						}
					}
				}
				log.debug("reAssign::"+reAssign);
				if(!FormatHelper.hasContent(articleNumberValue) || reAssign) {
					log.info("Assign Article and UPC Number :::");
					setColumnNames();
					findAndUpdateSKUSizes(sku);	
				}
			} 
		}
		log.info("AgronArticleNumberUPCPlugin autoAssign END>>>>");
	}


	
	/** 
	 * Method to update the SKUSize with article and UPC Number
	 * @param sku
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */	
	public static void findAndUpdateSKUSizes(LCSSKU sku) throws WTException, WTPropertyVetoException {
		log.debug("method findAndUpdateSKUSizes START>>>>>>>>>>");
		Collection<ProductSizeCategory> psdCollection= findValidPSDs(sku);
		String articleNum=(String)sku.getValue(ARTICLE_NUMBER_SKU_ATTKEY);
		boolean assignArticle=false;
		if(!FormatHelper.hasContent(articleNum)) {
			assignArticle=true;
		}
		FlexObject articleNumData=null;
		ArrayList upcNums=new ArrayList();
		try {
			if(psdCollection.size()>0){
				if(assignArticle) {
					articleNumData = findArticleNum();
					articleNum = (String) articleNumData.get(nameColumnSearchResultIndex);
					log.info("articleNum::"+articleNum);	
				}		
				Iterator<ProductSizeCategory> psdIter = psdCollection.iterator();
				while (psdIter.hasNext()) {
					ProductSizeCategory psc =  psdIter.next();
					log.debug("PSC: "+psc+"::"+psc.getName());
					ArrayList<String> sizes1Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSizeValues()));
					ArrayList<String> sizes2Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSize2Values()));
					ArrayList<String> articleSuffixCollection = new ArrayList<String>((MOAHelper.getMOACollection((String)psc.getValue(ARTICLE_SUFFIX_KEY))));
					HashMap<String,String> sizeSuffixMap=new HashMap();
					if(sizes1Collection.size()==articleSuffixCollection.size()) {
						for(int i=0; i<sizes1Collection.size(); i++) {
							sizeSuffixMap.put(sizes1Collection.get(i), articleSuffixCollection.get(i));
						}
					}
					log.debug("sizeSuffixMap::"+sizeSuffixMap);
					SearchResults searchResultSKUSizeQuery=SKUSizeQuery.findSKUSizesForPSC(psc, sku.getMaster(), null, null);
					Collection<SKUSize> skuSizesCollection= SKUSizeQuery.getObjectsFromResults(searchResultSKUSizeQuery.getResults(), "VR:com.lcs.wc.skusize.SKUSize:", "SKUSize.BRANCHIDITERATIONINFO");
					log.debug("skuSizesCollection Size::"+skuSizesCollection.size());
					if(skuSizesCollection !=null && skuSizesCollection.size()>0){
						if(assignArticle) {
							upcNums = (ArrayList) findUPCNum(sizes1Collection.size());
							log.info("upcNums::"+upcNums.size()+"::"+upcNums);
						}
						List<String> validCombinations=AgronArticleNumberUPCHelper.getValidSizeCombinationList(sizes1Collection,sizes2Collection);
						int counter=0;
						Iterator<SKUSize> skuSizeIter = skuSizesCollection.iterator();
						while (skuSizeIter.hasNext()) {
							SKUSize skuSize= (SKUSize) VersionHelper.getVersion(skuSizeIter.next(),"A"); 
							FlexObject upcNumData=null;
							if(assignArticle) {
								upcNumData=(FlexObject)upcNums.get(counter);
							}
							Collection<SKUSizeToSeason> skuSizeToSeasonCol=SKUSizeQuery.getSKUSizeToSeasonsFromSKUSize(skuSize,true);
							log.debug("skuSizeToSeasonCol Size-:"+skuSizeToSeasonCol.size());
							if(skuSize.isActive() && skuSizeToSeasonCol.size()>0) {
								SKUSizeMaster skuSizeMaster=(SKUSizeMaster) skuSize.getMaster();
								String skuSize1Value=skuSizeMaster.getSizeValue();
								String skuSize2Value= skuSizeMaster.getSize2Value();
								boolean updateSKUSize=false;
								try {
									if(FormatHelper.hasContentAllowZero(skuSize1Value) && (sizes2Collection.size()<=1 || validCombinations.contains(skuSize1Value+"_"+skuSize2Value))) {
										String skuSizeArticleNumber=(String)skuSize.getValue(SKUSIZE_ARTICLE_NBMER_KEY);
										if(!FormatHelper.hasContent(skuSizeArticleNumber) || !skuSizeArticleNumber.startsWith(articleNum)) {
											if(sizeSuffixMap.containsKey(skuSize1Value)) {
												skuSize.setValue(SKUSIZE_ARTICLE_NBMER_KEY, articleNum+sizeSuffixMap.get(skuSize1Value));	
											}else {
												skuSize.setValue(SKUSIZE_ARTICLE_NBMER_KEY, articleNum);		
											}
											updateSKUSize=true;
										}
										if(!FormatHelper.hasContent((String)skuSize.getValue(SKUSIZE_UPC_NBMER_KEY))) {
											if(upcNumData ==null) {
												upcNumData  = (FlexObject) findUPCNum(1).get(0); 
											}
											skuSize.setValue(SKUSIZE_UPC_NBMER_KEY, (String) upcNumData.get(nameColumnSearchResultIndex));	
											updateSKUSize=true;
										}
										if(updateSKUSize) {
											PersistenceServerHelper.manager.update(skuSize);
										}
									} 
								}
								catch(WTException exp) {
									updateUPCNum(upcNumData, true);
									exp.printStackTrace();
								}
							}else {
								if(upcNumData !=null) {
									updateUPCNum(upcNumData, true);	
								}
							}
							counter++;
						}
					}
				}
				if(assignArticle) {
					sku.setValue(ARTICLE_NUMBER_SKU_ATTKEY,	articleNum);
					PersistenceServerHelper.manager.update(sku);
				}
			}	
		}catch(Exception e) {
			if(assignArticle) {
				updateAvailableFlag(articleNumData,true);	
			}
			e.printStackTrace();
			log.error("Exception::"+e.getMessage());
			throw new LCSException(e.getMessage());
		}
		log.debug("method findAndUpdateSKUSizes END>>>>>>>>");
	}
	
	
	/**
	 * Set column names and search result index.
	 * 
	 * @throws WTException
	 * @throws LCSException
	 */
	public static void setColumnNames() throws WTException, LCSException {
		nameColumnSearchResultIndex = FlexTypeCache
				.getFlexTypeFromPath("Business Object").getAttribute("name")
				.getSearchResultIndex();
		nameColumnName = FlexTypeCache.getFlexTypeFromPath("Business Object")
				.getAttribute("name").getColumnName();
		FlexType ft =FlexTypeCache.getFlexTypeFromPath(ARTICLE_NUMBER_BO_TYPE);
		articleAvailableColumnname = FlexTypeCache
				.getFlexTypeFromPath(ARTICLE_NUMBER_BO_TYPE)
				.getAttribute(AVAILABLE_ATT_KEY_BO).getColumnName();
		articlePathId = FlexTypeCache.getFlexTypeFromPath(
				ARTICLE_NUMBER_BO_TYPE).getIdPath();
		upcAvailableColumnname = FlexTypeCache
				.getFlexTypeFromPath(UPC_CODE_BO_TYPE)
				.getAttribute(AVAILABLE_ATT_KEY_BO).getColumnName();
		upcPathId = FlexTypeCache.getFlexTypeFromPath(UPC_CODE_BO_TYPE)
				.getIdPath();
	}

	/**
	 * Finds all available UPC # from lookup table.
	 * 
	 * @param resultsNeeded
	 *            - Number of results needed.
	 * @return - All available UPC # from lookup table
	 * @throws WTException.
	 */
	public synchronized static List findUPCNum(int resultsNeeded) throws WTException {

		List flexObjs = findArticleOrUPCNumsFromLookupTable(upcAvailableColumnname, upcPathId, "UPC", resultsNeeded);
		if(flexObjs != null && !flexObjs.isEmpty()) {
			Iterator itr = flexObjs.iterator();
			while(itr.hasNext()) {
				updateUPCNum((FlexObject)itr.next(), false);
			}
		}
		return flexObjs;
	}

	/**
	 * Finds  available Article # from lookup table. Since Article # suffix
	 * (number part without letter) should be the same for a given colorway,
	 * should only need one article number.
	 * 
	 * @return -FlexObject-  available Article # FlexObject from lookup table.
	 * @throws WTException
	 *             - WTException.
	 * @throws WTPropertyVetoException 
	 */
	public synchronized static FlexObject findArticleNum() throws WTException, WTPropertyVetoException {
		ArrayList articleNums = (ArrayList)findArticleOrUPCNumsFromLookupTable(articleAvailableColumnname,
				articlePathId, "Article", 1);
		FlexObject articleNumData = (FlexObject) articleNums.get(0);
		
		updateAvailableFlag(articleNumData, false);
		
		return articleNumData;
	}

	/**
	 * Finds all available Article #/UPC # from lookup table (ordered by value.
	 * which is stored in name attribute).
	 * 
	 * @param availableColumnName
	 *            - name of the column for Article #/UPC # available
	 * @param pathId
	 *            - FlexType path ID to Article #/UPC # Lookup table (subtype of
	 *            Business Object)
	 * @param typeNameString
	 *            - "Article" or "UPC"
	 * @param resultsNeeded
	 *            Number of results needed
	 * @return - All available Article # or UPC # from lookup table
	 * @throws WTException
	 *             - Throws LCSException if the lookup table does not contain
	 *             enough Article #s/UPC #s needed
	 */
	public static List findArticleOrUPCNumsFromLookupTable(
			String availableColumnName, String pathId, String typeNameString,
			int resultsNeeded) throws WTException {
		Collection results = new ArrayList();
		PreparedQueryStatement pqs = new PreparedQueryStatement();

		pqs.appendSelectColumn(LCSLIFECYCLEMANAGED, nameColumnName);
		pqs.appendSelectColumn(LCSLIFECYCLEMANAGED, "IDA2A2");
		pqs.appendFromTable(LCSLIFECYCLEMANAGED);
		pqs.appendCriteria(new Criteria(LCSLIFECYCLEMANAGED,	availableColumnName, TRUE_KEY, Criteria.EQUALS));
		pqs.appendAnd();
		pqs.appendCriteria(new Criteria(LCSLIFECYCLEMANAGED, "FLEXTYPEIDPATH",
				pathId, Criteria.EQUALS));
		pqs.appendSortBy(LCSLIFECYCLEMANAGED + ".PTC_STR_1TYPEINFOLCSLIFECYCL");
		pqs.setToIndex(resultsNeeded);
		System.out.println("pqs::"+pqs);
		results = LCSQuery.runDirectQuery(pqs).getResults();
		List<?> tempResults = new ArrayList();
		tempResults.addAll(results);

		if (results.size() < resultsNeeded) {
			throw new LCSException(
					"The "
							+ typeNameString
							+ " Number lookup table does not have enough available numbers. Please contact system administrator.");
		}
		return tempResults;

	}

	/**
	 * Set Article # object on SKU and update the lookup table's flag.
	 * 
	 * @param sku
	 *            - SKU object whose status is changed to Adopted.
	 * @param articleNum
	 *            - Article Number Business Object from the lookup table.
	 * @param updateArticleNum
	 *            - updateArticleNum.
	 * @throws LCSException
	 *             - LCSException.
	 */
	public static LCSSKU updateArticleNum(LCSSKU sku, FlexObject articleNum,
			boolean updateArticleNum) throws LCSException {
		if (updateArticleNum) {
			//updateAvailableFlag(articleNum,false);
			sku.setValue(ARTICLE_NUMBER_SKU_ATTKEY,	(String) articleNum.get(nameColumnSearchResultIndex));
		}
		return sku;
	}

	/**
	 * Set available attribute to false on the given Business Object.
	 * 
	 * @param flexObj
	 *            - Business Object to set available attribute to false on.
	 * @throws WTException
	 *             - WTException.
	 * @throws WTPropertyVetoException
	 *             - WTPropertyVetoException.
	 */
	public static void updateAvailableFlag(FlexObject flexObj,boolean isAvailable)
			throws WTException, WTPropertyVetoException {
		if(flexObj !=null) {
			// updating the available flag on the article or upc to false, must
			// instantiate a LCSLifecycleManaged to do this
			String lcsManagedId = "OR:com.lcs.wc.foundation.LCSLifecycleManaged:"
					+ (String) flexObj.get(LCSLIFECYCLEMANAGED + ".IDA2A2");
			LCSLifecycleManaged businessObj = (LCSLifecycleManaged) LCSQuery.findObjectById(lcsManagedId, false);
			if(isAvailable){
				businessObj.setValue(AVAILABLE_ATT_KEY_BO, "true");
			}else{
				businessObj.setValue(AVAILABLE_ATT_KEY_BO, "false");	
			}
			LCSLogic.persist(businessObj);
		}
	}

	
	/**
	 * Update the lookup table's flag.
	 * @param upcNum
	 *            - UPC Number Business Object from the lookup table.
	 * @param updateUPCNum
	 *            - updateUPCNum.
	 * @throws LCSException
	 *             - LCSException.
	 */
	public static void updateUPCNum(FlexObject upcNum,
			boolean updateUPCNum)
					throws LCSException {
		boolean gotException = false;
		try {
			updateAvailableFlag(upcNum,updateUPCNum);
		} catch (WTPropertyVetoException e) {
			// TODO Auto-generated catch block
			gotException = true;
			e.printStackTrace();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			gotException = true;
			e.printStackTrace();
		}
		if (gotException) {
			log.error("The system was unable to assign UPC Number. Please contact system administrator.");
			throw new LCSException("The system was unable to assign UPC Number. Please contact system administrator.");
		}
	}
	
	/** 
	 * Method to get the Collection of the valid ProductSizeCategories ,PSD is only valid if PSD is associated with season 
	 * @param sku
	 * @throws WTException
	 */		
	public static Collection<ProductSizeCategory> findValidPSDs(LCSSKU sku) throws WTException {
		LCSProduct product = com.lcs.wc.season.SeasonProductLocator.getProductARev(sku);	
		Collection psdCollection = SizingQuery.findProductSizeCategoriesForProduct(product).getResults();
		Vector<ProductSizeCategory> validPsdCollection =new Vector();
		log.debug("psdCollections ::"+psdCollection.size());
		ProductSizeCategory psc = null;	
		if(psdCollection.size()>0){
			Iterator psdIter = psdCollection.iterator();
			while (psdIter.hasNext()) {
				FlexObject fob = (FlexObject) psdIter.next();
				psc = (ProductSizeCategory)VersionHelper.latestIterationOf((ProductSizeCategory)LCSQuery.findObjectById("VR:com.lcs.wc.sizing.ProductSizeCategory:"+ (String) fob.getString("PRODUCTSIZECATEGORY.BRANCHIDITERATIONINFO")));
				if(AgronArticleNumberUPCHelper.isValidPsc(psc)) {
					ArrayList<String> sizes1Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSizeValues()));
					if(sizes1Collection.size()>0) {
						ArrayList<String> sizes2Collection = new ArrayList<String>(MOAHelper.getMOACollection(psc.getSize2Values()));
						ArrayList<String> articleSuffixCollection = new ArrayList<String>((MOAHelper.getMOACollection((String)psc.getValue(ARTICLE_SUFFIX_KEY))));
						log.debug("sizes1Collection:-"+sizes1Collection+"\n  ::sizes2Collection :-"+sizes2Collection+" \n :: articleSuffixCollection:-"+articleSuffixCollection);
						if((sizes1Collection.size()>1 && sizes1Collection.size()!=articleSuffixCollection.size() ) || (sizes1Collection.size()==1 && articleSuffixCollection.size()>1)) {
							log.error("Issue with Product Size Definition: Mismatch in article suffix and size options:articleSuffixCollection:"+articleSuffixCollection+"::sizes1Collection- "+sizes1Collection);
							if(sizes1Collection.size()>1 && articleSuffixCollection.size()==0) {
								throw new LCSException("Please enter Article Suffix values for Product Size Definition of this Product and Colorways latest season.");
							}else {
								throw new LCSException("Issue with Product Size Definition: Mismatch in article suffix and size options.");
							}
						}

					}
					validPsdCollection.add(psc);
				}
			}
		}
		if(validPsdCollection.isEmpty()) {
			log.error("Please create a Product Size Definition for this Product and Season before assigning Article# to SKU.");
			throw new LCSException("Please create a Product Size Definition for this Product and Season before assigning Article# to SKU.");
		}
		//validPsdCollection.sort(Comparator.comparing(ProductSizeCategory::getCreateTimestamp));
		log.debug("validPsdCollection ::"+validPsdCollection.size() +"::"+validPsdCollection);
		return validPsdCollection;
	}
	  
	   
}
