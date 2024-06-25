package com.agron.wc.product;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsQuery;
import com.lcs.wc.measurements.LCSPointsOfMeasure;
import com.lcs.wc.sample.LCSSampleRequest;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.SortHelper;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import wt.fc.PersistenceHelper;
import wt.util.WTException;

public final class AgronDataUtil {
	public static boolean checkPOMSForMeasurement(LCSMeasurements measurements) {
		boolean isPOMAvailable = false;
		Collection pomCol = null;
		try {
			pomCol = LCSMeasurementsQuery.findPointsOfMeasure(measurements);

			if (pomCol.size() <= 1) {
				LCSPointsOfMeasure pom = null;
				Iterator poms = pomCol.iterator();
				while (poms.hasNext()) {
					pom = (LCSPointsOfMeasure) poms.next();
					if (FormatHelper.hasContent((String) pom.getValue("number"))
							&& FormatHelper.hasContent((String) pom.getValue("measurementName"))) {
						isPOMAvailable = true;
					}
				}
			} else {
				isPOMAvailable = true;
			}
		} catch (WTException e) {
			LCSLog.stackTrace(e);
		}
		return isPOMAvailable;
	}

	public static Collection getSampleRequestsCol(Collection sampleIds) {
		LCSSampleRequest sampleRequest = null;
		Timestamp componentCreatedDate = null;
		FlexObject sampleRequestFlexObj = null;
		LinkedList sampleRequestCol = new LinkedList();
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
		Set sampleRequestSet = new HashSet();
		try {
			if (sampleIds != null && sampleIds.size() > 0) {
				FlexObject flexObj = null;
				Iterator sampleIter = sampleIds.iterator();
				while (sampleIter.hasNext()) {
					flexObj = (FlexObject) sampleIter.next();
					sampleRequestSet.add(flexObj.getString("LCSSAMPLEREQUEST.IDA2A2"));
				}
				if (sampleRequestSet.size() > 0) {
					String srId = null;
					sampleIter = sampleRequestSet.iterator();
					while (sampleIter.hasNext()) {
						sampleRequestFlexObj = new FlexObject();
						srId = (String) sampleIter.next();
						sampleRequest = (LCSSampleRequest) LCSQuery
								.findObjectById("OR:com.lcs.wc.sample.LCSSampleRequest:" + srId);
						sampleRequestFlexObj.put("LCSSAMPLEREQUEST.OBJECT",
								"OR:com.lcs.wc.sample.LCSSampleRequest:" + srId);
						componentCreatedDate = PersistenceHelper.getCreateStamp(sampleRequest);
						sampleRequestFlexObj.put("LCSSAMPLEREQUEST.CREATEDTIMESTAMP",
								dateFormat.format(componentCreatedDate));
						sampleRequestCol.add(sampleRequestFlexObj);
					}
					SortHelper.sortFlexObjects(sampleRequestCol, "LCSSAMPLEREQUEST.CREATEDTIMESTAMP:ASC");
				}
			}
		} catch (WTException e) {
			LCSLog.stackTrace(e);
		}
		return sampleRequestCol;
	}
}
