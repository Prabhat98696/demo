package com.agron.wc.product;

import com.lcs.wc.construction.LCSConstructionInfo;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.util.FormatHelper;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import wt.fc.PersistenceHelper;
import wt.util.WTException;

public class AgronCoverPageUtil {
	private AgronCoverPage page = new AgronCoverPage();

	public PdfPCell createTableOfContent(Collection paramCollection, Map paramMap, Document paramDocument)
			throws WTException {
		PdfPTable pdfPTable1 = new PdfPTable(1);
		PdfPTable pdfPTable2 = new PdfPTable(1);
		pdfPTable2.setWidthPercentage(95.0F);
		pdfPTable2.addCell(this.page.createBlankDataCell());
		pdfPTable2.addCell(this.page.createBlankDataCell());
		float[] arrayOfFloat = { 65.0F, 50.0F, 35.0F };
		PdfPCell pdfPCell1 = this.page.returnTableOfContentCell("Table Of Contents: ", 8, true);

		pdfPCell1.setColspan(2);
		pdfPTable2.addCell(pdfPCell1);
		pdfPTable2.addCell(this.page.createBlankDataCell());
		pdfPTable2.addCell(this.page.createBlankDataCell());
		pdfPTable2.addCell(this.page.createBlankDataCell());
		pdfPTable2.addCell(this.page.createBlankDataCell());
		PdfPTable pdfPTable3 = new PdfPTable(arrayOfFloat);
		pdfPTable3.setWidthPercentage(80.0F);
		pdfPTable3.addCell(this.page.returnTableOfContentCell("Component", 8, true));
		pdfPTable3.addCell(this.page.returnTableOfContentCell("Component Type", 8, true));
		pdfPCell1 = this.page.returnTableOfContentCell("Last Updated", 8, true);
		pdfPCell1.setHorizontalAlignment(0);
		pdfPTable3.addCell(pdfPCell1);
		PdfPCell pdfPCell2 = new PdfPCell(pdfPTable3);
		pdfPCell2.setBorder(0);
		pdfPTable1.addCell(pdfPCell2);
		pdfPTable1.addCell(this.page.createBlankDataCell());
		pdfPTable1.addCell(this.page.createBlankDataCell());
		Collection collection1 = (new AgronPDFSpecCommentGenerator()).getPDFContentCollection(paramMap, paramDocument);
		if (collection1 != null && collection1.size() > 0) {
			pdfPTable1.addCell(this.page.returnTableOfContentCell("Comments Page", 8, false));
		}
		ArrayList arrayList = new ArrayList(paramCollection);
		PdfPTable pdfPTable4 = getContentTable(arrayList);
		PdfPCell pdfPCell3 = new PdfPCell(pdfPTable4);
		pdfPCell3.setBorder(0);
		pdfPTable1.addCell(pdfPCell3);
		Collection collection2 = (new AgronPDFFitSpecReportsGenerator()).getPDFContentCollection(paramMap,
				paramDocument);
		if (collection2 != null && collection2.size() > 0) {
			pdfPTable1.addCell(this.page.returnTableOfContentCell("Sample Comments Page", 8, false));
		}

		PdfPCell pdfPCell4 = new PdfPCell(pdfPTable1);

		pdfPCell4.setBorderWidth(1.0F);
		pdfPCell4.setPadding(3.0F);
		pdfPTable2.addCell(pdfPCell4);
		PdfPCell pdfPCell5 = new PdfPCell(pdfPTable2);
		pdfPCell5.setBorder(0);
		pdfPCell5.setPadding(2.0F);
		return pdfPCell5;
	}

	private PdfPTable getContentTable(List paramList) throws WTException {
		float[] arrayOfFloat = { 65.0F, 50.0F, 35.0F };
		PdfPTable pdfPTable = new PdfPTable(arrayOfFloat);
		pdfPTable.setWidthPercentage(95.0F);

		String str1 = "";
		String str2 = "";
		Timestamp timestamp = null;
		HashMap hashMap = new HashMap();

		for (byte b = 0; b <= paramList.size() - 1; b++) {
			String str4, str3 = paramList.get(b).toString();

			str1 = "";
			if (str3.indexOf("VR:") != -1) {
				str4 = str3.substring(str3.indexOf("VR:"), str3.length());
			} else {
				str4 = str3.substring(str3.indexOf("OR:"), str3.length());
			}
			Object object = LCSQuery.findObjectById(str4);

			if (str3.contains("BOM")) {

				FlexBOMPart flexBOMPart = (FlexBOMPart) object;
				timestamp = PersistenceHelper.getModifyStamp(flexBOMPart);
				hashMap.put(timestamp.toString(), flexBOMPart);
			} else if (str3.contains("Images Page")) {

				LCSDocument lCSDocument = (LCSDocument) object;
				timestamp = PersistenceHelper.getModifyStamp(lCSDocument);
				hashMap.put(timestamp.toString(), lCSDocument);
			} else if (str3.contains("Measurements")) {

				LCSMeasurements lCSMeasurements = (LCSMeasurements) object;
				if (AgronDataUtil.checkPOMSForMeasurement(lCSMeasurements)) {
					timestamp = PersistenceHelper.getModifyStamp(lCSMeasurements);
					hashMap.put(timestamp.toString(), lCSMeasurements);
				}

			} else if (str3.contains("Construction")) {
				LCSConstructionInfo lCSConstructionInfo = (LCSConstructionInfo) object;
				str1 = lCSConstructionInfo.getName();
				str1 = str1.substring(str1.indexOf(':') + 2);
				str2 = "Construction";
				timestamp = PersistenceHelper.getModifyStamp(lCSConstructionInfo);
				hashMap.put(timestamp.toString(), lCSConstructionInfo);

			} else if (str3.contains("Sample")) {
				LCSSample lCSSample = (LCSSample) object;
				timestamp = PersistenceHelper.getModifyStamp(lCSSample);
				hashMap.put(timestamp.toString(), lCSSample);
			}
		}
		TreeMap treeMap = new TreeMap(hashMap);
		String[] arrayOfString = new String[treeMap.size()];
		treeMap.keySet().toArray(arrayOfString);

		Arrays.sort(arrayOfString, Collections.reverseOrder());

		for (String str3 : arrayOfString) {
			System.out.println(str3 + " " + treeMap.get(str3));

			String str4 = treeMap.get(str3).toString();
			Object object = LCSQuery.findObjectById(str4);
			if (str4.contains("BOM")) {

				FlexBOMPart flexBOMPart = (FlexBOMPart) object;
				str1 = flexBOMPart.getName();
				str1 = str1.substring(str1.indexOf(':') + 2);
				str2 = "BOM";
				timestamp = PersistenceHelper.getModifyStamp(flexBOMPart);
			} else if (str4.contains("Document")) {

				LCSDocument lCSDocument = (LCSDocument) object;
				str1 = lCSDocument.getName();
				str1 = str1.substring(str1.indexOf(':') + 2);
				str2 = "Images Page";
				timestamp = PersistenceHelper.getModifyStamp(lCSDocument);
			} else if (str4.contains("Measurements")) {
				LCSMeasurements lCSMeasurements = (LCSMeasurements) object;
				if (AgronDataUtil.checkPOMSForMeasurement(lCSMeasurements)) {
					str1 = lCSMeasurements.getName();
					str1 = str1.substring(str1.indexOf(':') + 2);
					str2 = "Measurements";
					timestamp = PersistenceHelper.getModifyStamp(lCSMeasurements);
				}
			} else if (str4.contains("Construction")) {
				LCSConstructionInfo lCSConstructionInfo = (LCSConstructionInfo) object;
				str1 = lCSConstructionInfo.getName();
				str1 = str1.substring(str1.indexOf(':') + 2);
				str2 = "Construction";
				timestamp = PersistenceHelper.getModifyStamp(lCSConstructionInfo);
			} else if (str4.contains("Sample")) {
				LCSSample lCSSample = (LCSSample) object;
				str1 = lCSSample.getName();
				str2 = "Sample";
				timestamp = PersistenceHelper.getModifyStamp(lCSSample);
			}
			if (FormatHelper.hasContent(str1)) {
				String str = "";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
				str = simpleDateFormat.format(timestamp);
				pdfPTable.addCell(this.page.returnTableOfContentCell(str1, 8, false));
				pdfPTable.addCell(this.page.returnTableOfContentCell(str2, 8, false));
				pdfPTable.addCell(this.page.returnTableOfContentCell(str.toString() + " PST", 8, false));
			}
		}
		return pdfPTable;
	}
}
