package com.agron.wc.product;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;
import wt.util.WTException;

public class AgronSKUPlugin {
	
	public static final String CLASSNAME = "AgronSKUPlugin";
	
	public static final Logger LOGGER = LogManager.getLogger(AgronSKUPlugin.class);
	public static final boolean DEBUG = LCSProperties.getBoolean("com.agron.wc.product.AgronSKUPlugin.verbose");
	private static String OVERRIDE_COLOR_NAME_KEY = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.overrideColorName");
	private static String colorname = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.colorname");
	
/**
 * PLugin for Colorway attributes Concatenation
 * @param obj
 * @throws WTException
 */
	public static void colorwayNamesConcatenation(WTObject obj) throws WTException {
		
		LCSSKU sku = null;
		if(DEBUG)System.out.println("********** Inside the AgronSKUPlugin class with mehtod colorwayNamesConcatenation *********Object -->>>"+obj);
		if (!(obj instanceof LCSSKU)) {
			throw new LCSException("Object must be a LCSSKU");
		} else {
			sku = (LCSSKU) obj;
			if(DEBUG)System.out.println("********** SKU Version Identifier Object *********"+sku.getVersionIdentifier());
			if ((sku.getVersionIdentifier() == null || "A".equals(sku.getVersionIdentifier().getValue()))
					&& !sku.isPlaceholder()) {
				String color1Key = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.color1");
				String color2Key = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.color2");
				String color3Key = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.color3");
				String colorModifierKey = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.colorModifier");
				String catalogSKUName = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.catalogColorwayName");
				String pipeDelimiter = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.pipeDelimiter");
				
				
				String colorwayDescription = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.colorwayDescription");
				String nrfDescription = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.nrfDescription");
				String backSlashDelimiter = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.backSlashDelimiter");
				
				String primaryRootColor = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.primaryRootColor");
				String agrColorGroup = LCSProperties.get("com.agron.wc.product.AgronSKUPlugin.colorGroup");
				
				if(DEBUG)System.out.println("color1Key::" + color1Key + "\t color2Key::" + color2Key + "\t color3Key::" + color3Key + "\t colorModifierKey::" + colorModifierKey + 
						"\t catalogSKUName::" + catalogSKUName + "\t colorname::" + colorname + "\t colorwayDescription::"+colorwayDescription + 
						"\t nrfDescription::"+nrfDescription + "\t primaryRootColor::"+primaryRootColor + "\t agrColorGroup::"+agrColorGroup);

				if(DEBUG)System.out.println("color1Key::" + color1Key + "\t color2Key::" + color2Key + "\t color3Key::" + color3Key + "\t colorModifierKey::" + colorModifierKey + 
						"\t catalogSKUName::" + catalogSKUName + "\t colorname::" + colorname + "\t colorwayDescription::"+colorwayDescription + 
						"\t nrfDescription::"+nrfDescription + "\t primaryRootColor::"+primaryRootColor + "\t agrColorGroup::"+agrColorGroup);

				if(color1Key == null || color2Key == null || color3Key == null || colorModifierKey == null || catalogSKUName == null || colorname == null || colorwayDescription == null || nrfDescription == null || primaryRootColor == null || agrColorGroup == null || OVERRIDE_COLOR_NAME_KEY==null) {
					throw new LCSException("SKU or Color keys are not configured in LCS property file");
				}
				
				LCSColor lcsColor1  = (LCSColor) sku.getValue(color1Key);
				LCSColor lcsColor2  = (LCSColor) sku.getValue(color2Key);
				LCSColor lcsColor3  = (LCSColor) sku.getValue(color3Key);
				String colorModifier  = (String) sku.getValue(colorModifierKey);

				if(DEBUG)System.out.println("lcsColor1 object:: "+lcsColor1 + " lcsColor2 object:: "+lcsColor2 + " lcsColor3 object:: "+lcsColor3 + " colorModifier value:: "+colorModifier );

				String color1Name = getColorName(lcsColor1,false,"");
				String color2Name = getColorName(lcsColor2,true,backSlashDelimiter);
				String color3Name = getColorName(lcsColor3,true,backSlashDelimiter);				
				String colorModifierDelimiter = getFormattedStrValue(colorModifier,true," ");

				if(DEBUG)System.out.println("color1Name ::"+color1Name + "---color2Name ::"+color2Name + "---color3Name ::"+color3Name);
				sku.setValue(catalogSKUName, color1Name + color2Name + color3Name + colorModifierDelimiter);
				boolean requireDelimiter = true;
				String color1NrfDesc = getDisplayValue(lcsColor1, getColorAttValue(lcsColor1,nrfDescription,false,""), nrfDescription);
				if(color1NrfDesc == null || "".equals(color1NrfDesc.trim())) {
					requireDelimiter = false;
				}
				String color2NrfDesc = getFormattedStrValue(getDisplayValue(lcsColor2,getColorAttValue(lcsColor2,nrfDescription,false,""), nrfDescription),requireDelimiter,backSlashDelimiter);
				
				requireDelimiter = true;
				if((color1NrfDesc == null || "".equals(color1NrfDesc.trim())) && (color2NrfDesc == null || "".equals(color2NrfDesc.trim()))) {
					requireDelimiter = false;
				}

				String color3NrfDesc = getFormattedStrValue(getDisplayValue(lcsColor3,getColorAttValue(lcsColor3,nrfDescription,false,""), nrfDescription),requireDelimiter,backSlashDelimiter);

				requireDelimiter = true;
				if((color1NrfDesc == null || "".equals(color1NrfDesc.trim())) && (color2NrfDesc == null || "".equals(color2NrfDesc.trim())) && (color3NrfDesc == null || "".equals(color3NrfDesc.trim()))) {
					requireDelimiter = false;
				}

				colorModifierDelimiter = getFormattedStrValue(colorModifier,requireDelimiter," ");
				
				if(DEBUG)System.out.println("color1NrfDesc ::"+color1NrfDesc + "\t color2NrfDesc ::"+color2NrfDesc + "\t color3NrfDesc ::"+color3NrfDesc);				
				sku.setValue(colorwayDescription, color1NrfDesc + color2NrfDesc + color3NrfDesc + colorModifierDelimiter);
				
				String agrColor1Group = getDisplayValue(lcsColor1, getColorAttValue(lcsColor1,agrColorGroup,false,""),agrColorGroup);

				requireDelimiter = true;
				if(agrColor1Group == null || "".equals(agrColor1Group.trim())) {
					requireDelimiter = false;
				}

				String agrColor2Group = getFormattedStrValue(getDisplayValue(lcsColor2, getColorAttValue(lcsColor2,agrColorGroup,false,""),agrColorGroup),requireDelimiter,backSlashDelimiter);
				
				requireDelimiter = true;
				if((agrColor1Group == null || "".equals(agrColor1Group.trim())) && (agrColor2Group == null || "".equals(agrColor2Group.trim()))) {
					requireDelimiter = false;
				}
				String agrColor3Group = getFormattedStrValue(getDisplayValue(lcsColor3, getColorAttValue(lcsColor3,agrColorGroup,false,""),agrColorGroup),requireDelimiter,backSlashDelimiter);

				requireDelimiter = true;
				if((agrColor1Group == null || "".equals(agrColor1Group.trim())) && (agrColor2Group == null || "".equals(agrColor2Group.trim())) && (agrColor3Group == null || "".equals(agrColor3Group.trim()))) {
					requireDelimiter = false;
				}

				colorModifierDelimiter = getFormattedStrValue(colorModifier,requireDelimiter," ");

				if(DEBUG)System.out.println("agrColor1Group ::"+agrColor1Group + "\t agrColor2Group ::"+agrColor2Group + "\t agrColor3Group ::"+agrColor3Group);				
				sku.setValue(primaryRootColor, agrColor1Group + agrColor2Group + agrColor3Group + colorModifierDelimiter);
				
				//sku.setValue("skuName", String.valueOf(sku.getValue("agrColorwayLetter")) + " - " + color1NrfDesc + color2NrfDesc + color3NrfDesc + colorModifierDelimiter); // Should be done via type manager formula
			}
		}
	}
	
	/**
	 * Returns Color attribute value depending on delimiter
	 * @param lcsColor
	 * @param colorAttKey
	 * @param requireDelimiter
	 * @param delimiter
	 * @return
	 * @throws WTException
	 */
	public static String getColorAttValue(LCSColor lcsColor, String colorAttKey, boolean requireDelimiter, String delimiter) throws WTException {
		
		String colorName = "";
		if(lcsColor != null) {
			colorName = (String) lcsColor.getValue(colorAttKey.trim());
			colorName = getFormattedStrValue(colorName, requireDelimiter, delimiter);
		}
		return colorName.trim();
	}
	
	/**
	 * Returns formatted String value depending on delimiter
	 * @param strValue
	 * @param requireDelimiter
	 * @param delimiter
	 * @return
	 */
	public static String getFormattedStrValue(String strValue, boolean requireDelimiter, String delimiter) {
		
		if(strValue == null || "".equals(strValue.trim())) {
			strValue = "";
		}
		else if(requireDelimiter) {
			strValue = delimiter + strValue;
			return strValue; // Don't trim the delimiter if its required.
		}
		return strValue.trim();
	}
	
	/** Returns key values for single list attribute display values
     *   @param flexType
     *   @param display - String
     * , @param attLabel - String
     *   throws WTException
     */
    public static String getDisplayValue(LCSColor lcsColor, String keyValue,String attKey) throws WTException {
        String attValue = "";
        if(FormatHelper.hasContentAllowZero(keyValue) && lcsColor != null)
        {
            FlexTypeAttribute flexAtt = null;
            AttributeValueList attList = null;
            
            flexAtt = lcsColor.getFlexType().getAttribute(attKey);
            attList = flexAtt.getAttValueList();
            attValue = attList.getValue(keyValue,Locale.getDefault());
        }
        return attValue;
    }
    
	/**
	 * Returns Override Color name if there is no Override value than return color Name value depending on delimiter
	 * @param lcsColor
	 * @param requireDelimiter
	 * @param delimiter
	 * @return
	 * @throws WTException
	 */
	public static String getColorName(LCSColor lcsColor, boolean requireDelimiter, String delimiter) throws WTException {
		String colorName = "";
		if(lcsColor != null) {
			colorName = (String) lcsColor.getValue(OVERRIDE_COLOR_NAME_KEY.trim());
			if(DEBUG) System.out.println("Override colorName>>>"+colorName);
			if(colorName==null || "".equals(colorName.trim())) {
				colorName = (String) lcsColor.getValue(colorname);
				if(DEBUG) System.out.println("colorName>>>"+colorName);	
			}
			colorName = getFormattedStrValue(colorName, requireDelimiter, delimiter);
		}
		return colorName.trim();
	}
    
}