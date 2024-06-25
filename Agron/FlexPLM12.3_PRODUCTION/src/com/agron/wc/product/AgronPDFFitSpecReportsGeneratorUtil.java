package com.agron.wc.product;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.measurements.LCSFitTest;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.util.Collection;
import java.util.Iterator;
import wt.util.WTException;

public final class AgronPDFFitSpecReportsGeneratorUtil {
	private static final String PRINT_ON_TECHPACK = LCSProperties.get("com.agron.wc.techpack.printOnTechpack");

	private static final String MEAS_NAME_ATT_KEY = LCSProperties
			.get("com.agron.wc.product.AgronPDFFitSpecReportsGenerator.measurementNameAttKey");

	private static final String MEAS_NUMBER_ATT_KEY = LCSProperties
			.get("com.agron.wc.product.AgronPDFFitSpecReportsGenerator.measurementNumAttKey");

	public static PdfPTable getSampleCommentTable(Collection samplesIda2a) throws WTException {
		Iterator itr = samplesIda2a.iterator();
		LCSFitTest fitTestobj = null;
		Collection results = null;
		SearchResults fitSearch = null;
		FlexObject pomObj = null;
		Iterator pomIter = null;
		LCSSample sampleObj = null;
		String sampleSize = "";
		String ida2a2 = "";
		FlexType measurementType = FlexTypeCache.getFlexTypeFromPath("Measurements");
		String numberAttColumn = measurementType.getAttribute(MEAS_NUMBER_ATT_KEY).getVariableName();
		String nameAttColumn = measurementType.getAttribute(MEAS_NAME_ATT_KEY).getVariableName();
		PdfPTable table = new PdfPTable(1);
		boolean validSampleFound = false;
		boolean validSample = false;
		PdfPCell cell = null;
		while (itr.hasNext()) {

			ida2a2 = (String) itr.next();
			validSample = false;
			sampleObj = (LCSSample) LCSQuery.findObjectById("OR:com.lcs.wc.sample.LCSSample:" + ida2a2);
			if ("true".equals(String.valueOf(sampleObj.getValue(PRINT_ON_TECHPACK)))) {
				fitTestobj = LCSMeasurementsQuery.findFitTest(sampleObj);
				if (fitTestobj == null) {
					validSample = true;
				} else {
					fitSearch = LCSMeasurementsQuery.findFitTestData(fitTestobj);
					results = fitSearch.getResults();
					if (results.size() <= 1) {
						pomIter = results.iterator();

						pomObj = (FlexObject) pomIter.next();
						if (pomIter.hasNext()
								&& !FormatHelper.hasContent(pomObj.getData("LCSPOINTSOFMEASURE." + numberAttColumn))
								&& !FormatHelper.hasContent(pomObj.getData("LCSPOINTSOFMEASURE." + nameAttColumn))) {
							validSample = true;
							sampleSize = pomObj.getData("LCSFITTEST.SAMPLESIZE");
						}
					}
				}

				if (validSample) {
					validSampleFound = true;
					cell = new PdfPCell(AgronPDFFitSpecReportsGenerator.drawSampleInfo(sampleObj, sampleSize));

					table.addCell(cell);
				}
			}
		}

		if (!validSampleFound) {
			return null;
		}

		return table;
	}
}
