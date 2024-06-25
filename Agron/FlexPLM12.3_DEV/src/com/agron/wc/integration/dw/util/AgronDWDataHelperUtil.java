package com.agron.wc.integration.dw.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeException;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.placeholder.Placeholder;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.LookupTableHelper;
import com.lcs.wc.util.MOAHelper;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WrappedTimestamp;

public class AgronDWDataHelperUtil {
	
	public static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.dw");
	
	public static final String AGRON_PRODUCT_FLEXTYPE_PATH = LCSProperties.get("com.agron.wc.integration.straub.util.FlexTypePath").trim();
	public static final String AGRON_PRODUCT_OBJECT_NAME = LCSProperties.get("com.agron.wc.integration.straub.util.busObj.objectName").trim();
	public static final String MOA_AGRON_PRODUCT_OBJ_KEY = LCSProperties.get("com.agron.wc.integration.straub.util.busObj.agronProductObj").trim();
	
	public static final String MOA_AGRON_PRODUCT_VALUE = LCSProperties.get("com.agron.wc.integration.straub.util.moa.agronProductValue").trim();
	public static final	String MOA_PRODUCT_FLEXTYPE_KEY =  LCSProperties.get("com.agron.wc.integration.straub.util.moa.prodFlexTypeKey").trim();
	public static final	String MOA_PRODUCT_GEN_VALUE =  LCSProperties.get("com.agron.wc.integration.straub.util.moa.prodGenKey").trim();
	public static final	String MOA_PRODUCT_SILHOUTTE_VALUE =  LCSProperties.get("com.agron.wc.integration.straub.util.moa.prodSilhouetteKey").trim();
	/**
	 * 
	 * @param obj
	 * @param key
	 * @param needValue2
	 * @return
	 * @throws WTException
	 */
	public static String getDataFromKey(WTObject obj, String key, boolean needValue2) throws WTException {
		LOGGER.debug("!! Method getDataFromKey START key>>>>>"+key);
		String value = "";
		FlexType type;
		FlexTypeAttribute att;
		FlexTyped typed;
		type = null;
		att = null;
		String attType = "";
		Object attValue = null;
		String format = null;
		try{
			if (obj instanceof FlexTyped) {
				typed = (FlexTyped) obj;
				type = typed.getFlexType();
				if (type != null) {
					try {
						att = type.getAttribute(key);
					} catch (FlexTypeException e) {
						LOGGER.debug((new StringBuilder("Attribute not Found with the key : ")).append(key).toString());
						return value;
					}
					try {
						attType = att.getAttVariableType();

						if (ACLHelper.hasViewAccess(att)) {
							attValue = getAttValue(obj, key);
							LOGGER.debug("!! Method getDataFromKey FROM attValue>>>>>"+attValue+">>>attType>>"+attType);


							if (attType.equals("choice") || attType.equals("driven")) {
								if (attValue != null)
									if (!needValue2) {
										value = type.getAttribute(key).getAttValueList().getValue((String) attValue,
												Locale.getDefault());

									} else
										value = getValue2(typed, key, attValue.toString());
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("derivedString") || attType.equals("text")
									|| attType.equals("textArea")) {
								if (attValue != null)
									value = (String) attValue;
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
								
							} else if (attType.equals("float") || attType.equals("currency") || attType.equals("integer"))

							{
								if (attType.equals("integer")) {

									if (attValue != null) {

										String str3 = String.valueOf(attValue);
										value = str3;

									} else {
										value = "0";
									}
								}


								else if (attValue != null) {

									Double d = (Double) attValue;

									int precision = att.getAttDecimalFigures();

									if (precision != 0) {

										value = FormatHelper.formatWithPrecision(d, precision);
										
									} else {

										value = (new StringBuilder()).append(d.intValue()).toString();


									}
								} else {
									value = "0";
								}
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("sequence")) {
								if (attValue != null) {
									Long l = (Long) attValue;
									Double d = l.doubleValue();
									int i = (int) Math.round(d.doubleValue());
									value = String.valueOf(i);
								}
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("date")) {
								if (attValue != null) {
									WrappedTimestamp time = (WrappedTimestamp) attValue;
									format = "MM/dd/yyyy";
									DateFormat formatter = new SimpleDateFormat(format);
									TimeZone timeZone = null;
									timeZone = TimeZone.getTimeZone("GMT");
									formatter.setTimeZone(timeZone);
									value = formatter.format(time);
								}
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("moaList") || attType.equals("moaEntry")) {
								if (attValue != null) {

									String stringValue = (String) attValue;
									value = MOAHelper.parseOutDelimsLocalized(stringValue, ",", att.getAttValueList(),
											null);
								}
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("composite")) {
								String token = null;
								String compositeKey = null;
								String percentValue = null;
								String keyValue = "";
								if (attValue != null) {
									String stringValue = (String) attValue;
									for (StringTokenizer compositeST = new StringTokenizer(stringValue,
											"|~*~|"); compositeST.hasMoreElements();) {
										token = compositeST.nextToken();
										if (token != null && token.indexOf(' ') != -1) {
											percentValue = token.substring(0, token.indexOf(' '));
											compositeKey = token.substring(token.indexOf(' ') + 1, token.length());
											keyValue = type.getAttribute(key).getAttValueList().getValue(compositeKey,
													ClientContext.getContext().getLocale());
											if (compositeST.hasMoreElements())
												value = (new StringBuilder(String.valueOf(value))).append(percentValue)
												.append(" ").append(keyValue).append(",").toString();
											else
												value = (new StringBuilder(String.valueOf(value))).append(percentValue)
												.append(" ").append(keyValue).toString();
										}
									}

								}
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("object_ref") || attType.equals("object_ref_list")) {
								if (attValue != null)
									if (attValue instanceof LCSSupplier) {
										LCSSupplier val = (LCSSupplier) attValue;
										value = val.getName();
									} else if (attValue instanceof LCSColor) {
										LCSColor val = (LCSColor) attValue;
										value = val.getName();
									} else if (attValue instanceof LCSSeason) {
										LCSSeason val = (LCSSeason) attValue;
										value = val.getName();
									} else if (attValue instanceof LCSProduct) {
										LCSProduct val = (LCSProduct) attValue;
										value = val.getName();
									} else if (attValue instanceof LCSSourcingConfig) {
										LCSSourcingConfig val = (LCSSourcingConfig) attValue;
										value = val.getName();
									} else if (attValue instanceof FlexSpecification) {
										FlexSpecification val = (FlexSpecification) attValue;
										value = val.getName();
									} else if (attValue instanceof LCSLifecycleManaged) {
										LCSLifecycleManaged val = (LCSLifecycleManaged) attValue;
										value = val.getName();
									} else if (attValue instanceof LCSCountry) {
										LCSCountry val = (LCSCountry) attValue;
										value = val.getName();
									}
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("userList") && attValue != null) {
								FlexObject flexobj = (FlexObject) attValue;
								if ("developer".equals(key) || "productManager".equals(key) || "patternMaker".equals(key))
									value = flexobj.getString("EMAIL");
								else
									value = flexobj.getString("FULLNAME");
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							} else if (attType.equals("boolean")) {
								attValue=att.getDisplayValue(typed);
								if (attValue != null)
									value = attValue.toString();
								LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
							}else if (attType.equals("uom")) {
								attValue=att.getDisplayValue(typed);
								if (attValue != null) {
									value = attValue.toString();
									}																
								}
						} else {
							value = "** Restricted Access";
							LOGGER.debug("!! Method getDataFromKey "+attValue+",,  value>>>>>"+value);
						}
					} catch (WTException wtException) {
						wtException.printStackTrace();
						LOGGER.error((new StringBuilder("Error : ")).append(wtException.getLocalizedMessage()));
					}
				}
			}

		}catch(Exception ec){
			ec.printStackTrace();
		}

		if(FormatHelper.hasContent(value)){
			value = value.replaceAll("(\r\n|\n)", "");
	
		}

		LOGGER.debug("Object !!>>>"+obj+", ***key>>>>"+key+", ****Value >>>>>"+value);
		LOGGER.debug("!! Method getDataFromKey END>>>>>");
		return value;

	}

	
	/**
	 * 
	 * @param object
	 * @param key
	 * @return
	 */
	public static Object getAttValue(WTObject object, String key) {
		Object attValue = null;
		try {
			if (object instanceof LCSSupplier) {
				LCSSupplier val = (LCSSupplier) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSColor) {
				LCSColor val = (LCSColor) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSeason) {
				LCSSeason val = (LCSSeason) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSeasonProductLink) {
				LCSSeasonProductLink val = (LCSSeasonProductLink) object;
				if ("SKU".equals(val.getSeasonLinkType()))
					attValue = val.getLogicalValue(key);
				else
					attValue = val.getValue(key);
			} else if (object instanceof LCSProduct) {
				LCSProduct val = (LCSProduct) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSourcingConfig) {
				LCSSourcingConfig val = (LCSSourcingConfig) object;
				attValue = val.getValue(key);
			} else if (object instanceof FlexSpecification) {
				FlexSpecification val = (FlexSpecification) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSLifecycleManaged) {
				LCSLifecycleManaged val = (LCSLifecycleManaged) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSCountry) {
				LCSCountry val = (LCSCountry) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSMOAObject) {
				LCSMOAObject val = (LCSMOAObject) object;
				attValue = val.getValue(key);
			} else if (object instanceof Placeholder) {
				Placeholder val = (Placeholder) object;
				attValue = val.getValue(key);
			} else if (object instanceof LCSSKU) {
				LCSSKU val = (LCSSKU) object;
				attValue = val.getValue(key);
			}
		} catch (Exception e) {
			LOGGER.error((new StringBuilder("Error : ")).append(e.getLocalizedMessage()));
		}
		return attValue;
	}

	
/**
 * 
 * @param typed
 * @param key
 * @param value
 * @return
 */
	public static String getValue2(FlexTyped typed, String key, String value) {
		String attValue = null;
		Map tempMap = null;
		Map value2Map = null;
		String keyValue =null;
		try {
			FlexTypeAttribute att = typed.getFlexType().getAttribute(key);
			Map listMap = (HashMap) att.getAttValueList().toLocalizedMapAll(ClientContext.getContext().getLocale());
			LOGGER.info("!!listMap!!>>>>"+listMap);
			
			Set listCollection = listMap.keySet();
			Iterator listIter = listCollection.iterator();
			value2Map = new HashMap();
			while (listIter.hasNext()) {
				key = (String) listIter.next();
				
				/*tempMap = (Map) listMap.get(key);
				LOGGER.debug("!!!!!!!tempMap!!!!!!"+tempMap);
				if (tempMap.size() > 0)
					value2Map.put(key, tempMap.get("Value2"));*/
				keyValue = String.valueOf(listMap.get(key));
				if(FormatHelper.hasContent(keyValue))
					value2Map.put(key, keyValue);
			}
			attValue = (String) value2Map.get(value);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error((new StringBuilder("Error : ")).append(e.getLocalizedMessage()));
		}
		return attValue;
	}

	
	/**
	 * 
	 * @return
	 */
	
	public static HashMap<String,String> getMoaMapOfAgronProduct() {
		 LOGGER.info("MEthod getMoaMapOfAgronProduct>>> STRT");
		 HashMap<String,String> adidasArticleMoaRowMap=new HashMap<String,String>();
		try {
			
			LOGGER.info("ADIDAS_ARTICLE_FLEXTYPE_PATH>>> "+AGRON_PRODUCT_FLEXTYPE_PATH);
			FlexType agronProductBSObj = FlexTypeCache.getFlexTypeFromPath(AGRON_PRODUCT_FLEXTYPE_PATH);  
			LOGGER.info("Agron Product BSObj>>> "+agronProductBSObj);
			LookupTableHelper tableHelpr = new LookupTableHelper();

			LCSMOATable moaAgronProduct = tableHelpr.getLookupTable(agronProductBSObj,  AGRON_PRODUCT_OBJECT_NAME, MOA_AGRON_PRODUCT_OBJ_KEY);
			Collection moaRows= moaAgronProduct.getRows();
			
			if (moaRows !=null && moaRows.size()>0) {
				Iterator moaIterator = moaRows.iterator();
				while (moaIterator.hasNext()) {
					
					FlexObject adidasArticelMoaData = (FlexObject) moaIterator.next();
					String prodflexType=adidasArticelMoaData.getData(MOA_PRODUCT_FLEXTYPE_KEY) ;
					//String prodCollection=adidasArticelMoaData.getData(MOA_PRODUCT_COLL_VALUE) ;
					String prodGender=adidasArticelMoaData.getData(MOA_PRODUCT_GEN_VALUE) ;
					String prodSilhouette=adidasArticelMoaData.getData(MOA_PRODUCT_SILHOUTTE_VALUE) ;
					String agronProductKey = "";
					String prefix ="";
					String agronProductValue=adidasArticelMoaData.getData(MOA_AGRON_PRODUCT_VALUE) ;
					LOGGER.info("Agron Product agronProductKey>>> "+prodflexType);
					LOGGER.info("Agron Product prodGender>>> "+prodGender);
					LOGGER.info("Agron Product prodSilhouette>>> "+prodSilhouette);
					
					if(FormatHelper.hasContent(prodGender)&&FormatHelper.hasContent(prodSilhouette)){
						prefix = prodflexType.substring(prodflexType.lastIndexOf("\\")+1, prodflexType.length());
						if(prefix.contains("Sport Accessories")){
							prefix = "SpprtsAcc";
						}
						agronProductKey = prefix+"_"+prodGender+"_"+prodSilhouette;
					}

					LOGGER.info("Agron Product agronProductKey>>> "+agronProductKey+", Agron Product agronProductValue>>>>>>>>"+agronProductValue);
					if(agronProductKey !=null && agronProductValue.trim() !=""){
						adidasArticleMoaRowMap.put(agronProductKey, agronProductValue);
					}
				}	
			}

		} catch (WTException e) {
			e.printStackTrace();
		}
		LOGGER.info(": getMoaMapOfAgronProduct>>> "+adidasArticleMoaRowMap);
		LOGGER.info("MEthod getMoaMapOfAgronProduct>>> END");
		return adidasArticleMoaRowMap;
	}
}
