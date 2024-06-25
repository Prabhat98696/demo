package com.agron.wc.product;

import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lowagie.text.pdf.PdfPCell;
import java.util.Collection;
import java.util.Iterator;
import wt.util.WTException;

public final class AgronPSDUtil {
	private static final String SEPERATOR = LCSProperties.get("com.agron.wc.seperator", "|~*~|");

	private static final String DISPLAYTEXT = "DISPLAYTEXT";

	private static PDFGeneratorHelper pgh = new PDFGeneratorHelper();

	public static PdfPCell drawSizeRangeCell(LCSProduct product, LCSSeason season) {
		PdfPCell sizeRangeCell = null;
		PreparedQueryStatement query = null;
		Collection sizes = null;
		SearchResults results = null;
		FlexObject fob = null;
		ProductSizeCategory prodSizeCategory = null;
		try {
			query = SizingQuery.getPSDDataQueryForProductSeason(product, season);

			results = LCSQuery.runDirectQuery(query);
			if (results != null && results.getResults().size() > 0) {
				sizes = results.getResults();
				Iterator iter = sizes.iterator();
				if (iter.hasNext()) {
					fob = (FlexObject) iter.next();
					prodSizeCategory = (ProductSizeCategory) LCSQuery.findObjectById(
							"OR:com.lcs.wc.sizing.ProductSizeCategory:" + fob.getString("PRODUCTSIZECATEGORY.IDA2A2"));

					String sizeValue = prodSizeCategory.getSizeValues();
					sizeValue = sizeValue.replace(SEPERATOR, ",");
					char comma = sizeValue.charAt(sizeValue.length() - 1);
					if (',' == comma) {
						sizeValue = sizeValue.substring(0, sizeValue.length() - 1);
					}
					sizeRangeCell = new PdfPCell(
							pgh.multiFontPara(sizeValue, pgh.getCellFont("DISPLAYTEXT", null, null)));
				}

			} else {

				sizeRangeCell = new PdfPCell(pgh.multiFontPara("", pgh.getCellFont("DISPLAYTEXT", null, null)));
				sizeRangeCell.setBorder(0);
			}
		} catch (WTException e) {
			LCSLog.stackTrace(e);
		}
		return sizeRangeCell;
	}
}
