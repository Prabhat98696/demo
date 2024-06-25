package com.agron.wc.util;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import java.util.ArrayList;
import java.util.Collection;


public class AgronCostingPropertiesPluginHelper {

	public static final Collection getAllPropertySettings(String property) {
		String localProperty = property;

		ArrayList attLists;
		int indx;
		for (attLists = new ArrayList(); FormatHelper
				.hasContent(localProperty); localProperty = localProperty.substring(0, indx)) {
			int count = 1;

			for (boolean hasMore = true; hasMore; ++count) {
				String attList = LCSProperties.get(localProperty + "." + count);
				if (FormatHelper.hasContent(attList)) {
					attLists.add(attList);
				} else {
					hasMore = false;
				}
			}

			char a = 92;
			indx = localProperty.lastIndexOf(a) < 0 ? 0 : localProperty.lastIndexOf(a);
		}

		return attLists;
	}

}
