package com.agron.wc.document;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentHelper;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.LCSSKUQuery;
import com.lcs.wc.util.FormatHelper;

import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.fc.WTObject;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTProperties;

public final class AgronDocumentPlugins {
	/**
	 * Default Constructor.
	 */
	private AgronDocumentPlugins() {
	}

	/**
	 * Defined to store the constant value.
	 */
	private static final String COLORBOARD_STR = LCSProperties
			.get("com.agron.wc.document.ImagesPage.PageType.colorBoardKey", "agrColorboard");
	/**
	 * Defined to store the constant value.
	 */
	private static final String IMAGES_PAGE_TYPE_KEY = LCSProperties
			.get("com.agron.wc.document.ImagesPage.PageType.key", "pageType");
	/**
	 * Defined to store the constant value.
	 */
	private static final String OWNERREF_KEY = LCSProperties.get("com.agron.wc.document.ImagePage.ownerRefKey",
			"ownerReference");
	/**
	 * Defined to store the constant value.
	 */
	private static final String COLORWAYLETTER = LCSProperties.get("com.agron.wc.colorway.colorwayLetterKey",
			"agrColorwayLetter");
	/**
	 * Defined to store the constant value.
	 */
	private static final String WINDCHILL_IMAGES = "/Windchill/images/";
	/**
	 * Defined to store the constant value.
	 */
	private static final String IAMGE_PATH = LCSProperties.get("com.lcs.wc.content.imagefilePathOverride");
	/**
	 * Defined to store the constant value.
	 */
	private static final String LINK_FOLDER = LCSProperties.get("com.lcs.wc.content.imageURL");
	
	public static final Logger logger = LogManager.getLogger("com.agron.wc.document");
	/**
	 * This method is used to set the thumb nail to the colorway.
	 * 
	 * @param obj
	 *            is WTObject object
	 * @throws WTException
	 */
	public static void setColorwayThumbnails(WTObject obj) throws WTException {
		int imageCommentsCount = 0;
		WTPartMaster master = null;
		LCSProduct product = null;
		Map skuMap = null;
		String imageComments = "";
		LCSSKU skuObj = null;
		String fileName = null;
		String wthome = "";
		String targetFilePath = null;
		StringTokenizer st = null;
		String imageLbl = null;
		ApplicationData ad = null;
		String modifiedFileName = null;
		String copyLocation = null;
		String absolutePath = null;
		String existingThumbnail = null;
		Iterator adIter = null;

		try {
			if(obj == null ) { 
				return ;
				}
			LCSDocument document = (LCSDocument) obj;
			logger.debug(document.getFlexType().getFullNameDisplay());
			if (COLORBOARD_STR.equals((String) document.getValue(IMAGES_PAGE_TYPE_KEY))) {

				document = (LCSDocument) ContentHelper.service.getContents(document);
				master = (WTPartMaster) LCSQuery.findObjectById((String) document.getValue(OWNERREF_KEY));
				product = (LCSProduct) VersionHelper.latestIterationOf(master);
				skuMap = getSKUMap(product);
				if (skuMap != null && skuMap.size() > 0) {
					Collection<?> ads = ContentHelper.getApplicationData(document);
					String desc = "";
					adIter = ads.iterator();
					while (adIter.hasNext()) {
						ad = (ApplicationData) adIter.next();
						desc = ad.getDescription();
						if (desc.startsWith("IMAGE")) {
							st = new StringTokenizer(desc, "|~*~|");
							imageLbl = st.nextToken();
							logger.debug("##>>>>imageCommentsCount>>>>>>"+imageCommentsCount);
							imageCommentsCount = Integer.parseInt(imageLbl.substring((imageLbl.length()) - 1));
							imageComments = (String) document.getValue("imageComments" + imageCommentsCount);
							logger.debug("##>>>>imageComments>>>>>>>>"+imageComments);
							if(imageComments!=null && !"".equals(imageComments))
							{
							imageComments = imageComments.replaceAll("[\\n\\t ]", "");
							
							if (imageComments.contains("<p>")) {
								imageComments = imageComments.substring(imageComments.indexOf("<p>") + 3);
								imageComments = imageComments.substring(0, imageComments.indexOf("</p>"));
							}
							
							skuObj = (LCSSKU) skuMap.get(imageComments.toString().trim());

								if (skuObj != null) {
									if(skuObj.isRevA() != true) {
										skuObj = (LCSSKU) VersionHelper.getVersion(skuObj.getMaster(), "A");
									}
									
									if (VersionHelper.isCheckedOut(skuObj)) {
										skuObj = VersionHelper.getWorkingCopy(skuObj);
									} else {
										skuObj = VersionHelper.checkout(skuObj);
									}
									existingThumbnail = skuObj.getPartPrimaryImageURL();

									absolutePath = LCSDocumentHelper.service.downloadContent(ad);
									fileName = absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1);
									fileName = fileName.replaceAll(" ", "_");
									modifiedFileName = AgronDocumentPluginsUtil.appendTimeStamp(fileName);
									String imageThumbnailPath = document.getThumbnailLocation();

									if (imageThumbnailPath != null) {
										imageThumbnailPath = imageThumbnailPath.substring(
												imageThumbnailPath.lastIndexOf("images/") + 7,
												imageThumbnailPath.lastIndexOf("/") + 1);

										if (FormatHelper.hasContent(existingThumbnail)) {
											AgronDocumentPluginsUtil.deleteExistingThumbnail(existingThumbnail);
										}
										if (FormatHelper.hasContent(IAMGE_PATH)) {
											
											imageThumbnailPath = imageThumbnailPath.trim();
											copyLocation = LINK_FOLDER + "/" + imageThumbnailPath;

											targetFilePath = IAMGE_PATH + "/" + imageThumbnailPath;
										} else {
											logger.info("existingThumbnail --Windchill- " + imageThumbnailPath);

											copyLocation = WINDCHILL_IMAGES + imageThumbnailPath;
											wthome = WTProperties.getServerProperties().getProperty("wt.home");
											targetFilePath = (new StringBuilder()).append(wthome).append(File.separator).append("codebase/images/"+imageThumbnailPath).toString();
										}

										/*
										 * It will save the image with timestamp in to the DB
										 *  like : 9M_ULTIMATE_PLUS_II_COLORBOARD_FLEX04_06_2020_06_42_27.png
										 *  * */
										if (AgronDocumentPluginsUtil.copyImage(absolutePath, targetFilePath, modifiedFileName)) {
											skuObj.setPartPrimaryImageURL(copyLocation + modifiedFileName);

											skuObj = (LCSSKU) LCSLogic.persist(skuObj, true);
										}
										if (VersionHelper.isCheckedOut(skuObj)) {
											VersionHelper.checkin(skuObj);
										}
									}
								}
							} else{
								logger.info("Image comment is blank hence not need to set with colorways thumbnail:" + imageComments);
							}
						} else {
							logger.info("!Page Description Should be IMAGE only ");
						}
					}
				}else{
					logger.info("PLEASE ADD Colorway First");
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
			if (VersionHelper.isCheckedOut(skuObj)) {
				VersionHelper.checkin(skuObj);
			}
		}
	}

	/**
	 * This method is used to get Map with the combination of colorway letter
	 * and sku object.
	 * 
	 * @param product
	 *            is LCSProduct object
	 * @return Map type
	 */
	public static Map getSKUMap(LCSProduct product) {
		Collection skuCol = null;
		Map skuMap = null;
		LCSSKU skuObj = null;
		LCSSKU skuObjA = null;
		Iterator skuIter = null;
		String colorwayLetter = null;
		try {
			skuCol = LCSSKUQuery.findAllSKUs(product, false);
			if (skuCol != null && skuCol.size() > 0) {
				skuMap = new HashMap();
				skuIter = skuCol.iterator();
				while (skuIter.hasNext()) {
					skuObj = (LCSSKU) skuIter.next();
					skuObjA = (LCSSKU) VersionHelper.getVersion(skuObj, "A");
					colorwayLetter = (String) skuObjA.getValue(COLORWAYLETTER);
					if (FormatHelper.hasContent(colorwayLetter)) {
						skuMap.put(colorwayLetter, skuObjA);
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return skuMap;
	}

}