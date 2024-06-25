package com.agron.wc.color;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.color.LCSColor;

import com.lcs.wc.util.LCSProperties;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public final class AgronColorPlugin {

	public static final Logger logger = LogManager.getLogger(AgronColorPlugin.class);

	public static final String AGR_DISTINCTIVE_COLOR_KEY = LCSProperties.get("com.agron.wc.color.LCSColor.AgronColorPlugin.DistinctiveColor");
	public static final String AGR_COLOR_NAME = LCSProperties.get("com.agron.wc.color.LCSColor.AgronColorPlugin.Name");
	public static final String AGR_SOLID_COLOR_NAME = LCSProperties.get("com.agron.wc.color.LCSColor.AgronColorPlugin.SolidColorName");

	public static void setSolidNameToPatterns(WTObject obj) throws WTPropertyVetoException {
		try {
			LCSColor patternColorObj = (LCSColor) obj;
			LCSColor distinctiveColorObj = null;
			if (patternColorObj.getValue(AGR_DISTINCTIVE_COLOR_KEY) == null) {
				return;
			}

			distinctiveColorObj = (LCSColor) patternColorObj.getValue(AGR_DISTINCTIVE_COLOR_KEY);
			String colorName = (String) distinctiveColorObj.getValue(AGR_COLOR_NAME);

			patternColorObj.setValue(AGR_SOLID_COLOR_NAME, colorName);
		} catch (WTException exception) {
			logger.debug(exception.getMessage());
		}

	}
}