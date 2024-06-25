package com.agron.wc.product;

import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import wt.util.WTException;

public class AgronPDFHeaderTableGenerator {
	private static final String FORMLABEL = "FORMLABEL";
	private static final String DISPLAYTEXT = "DISPLAYTEXT";
	private static final String PRODUCTNAME_KEY = LCSProperties.get("com.agron.wc.techpack.productNameKey");

	private static final String SEASONNAME_KEY = LCSProperties.get("com.agron.wc.techpack.seasonNameKey");

	private static final String SOURCENAME_KEY = LCSProperties.get("com.agron.wc.techpack.sourceNameKey");

	private static final String COUNTRYOFORIGIN_KEY = LCSProperties.get("com.agron.wc.techpack.contryOfOriginKey");

	private static final String LINE_KEY = LCSProperties.get("com.agron.wc.techpack.lineKey");

	private static final String INLINEWORK_KEY = LCSProperties.get("com.agron.wc.techpack.inLineWorkKey");

	private static final String WORK_KEY = LCSProperties.get("com.agron.wc.techpack.workKey");

	private static final String DATEFORMATSTYLE = LCSProperties.get("com.agron.wc.techpack.dateFormateStyle");

	private static final String PRODUCT_NAME = LCSProperties.get("com.agron.wc.techpack.productDisplayName");

	private static final String WORK = LCSProperties.get("com.agron.wc.techpack.workDisplayName");

	private static final String SOURCE = LCSProperties.get("com.agron.wc.techpack.sourceDisplayName");

	private static final String SEASON = LCSProperties.get("com.agron.wc.techpack.SeasonDisplayName");

	private static final String SIZE_RUN = LCSProperties.get("com.agron.wc.techpack.sizeRunDisplayName");

	private static final String DATE = LCSProperties.get("com.agron.wc.techpack.date", "Print Date : ");

	private static final String SMU_CLEARENCE = LCSProperties
			.get("com.agron.wc.product.PDFProductSpecificationHeader.season.smuClearenceLbl");

	private DateFormat df = new SimpleDateFormat(DATEFORMATSTYLE);

	private Date date = new Date();

	private String dateVal = this.df.format(this.date);

	private static PDFGeneratorHelper pgh = new PDFGeneratorHelper();

	public PdfPTable getLeftTable(Map params, LCSProduct product, FlexSpecification spec, LCSSeason season) {
		PdfPCell cell = null;
		LCSSourcingConfig config = null;

		float[] widths = { 15.0F, 25.0F };
		PdfPTable table = new PdfPTable(2);

		try {
			if (product != null) {
				cell = new PdfPCell(pgh.multiFontPara(PRODUCT_NAME, pgh.getCellFont("FORMLABEL", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
				cell = new PdfPCell(
						pgh.multiFontPara(AgronPDFProductSpecificationGenerator2.getData(product, PRODUCTNAME_KEY),
								pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);

				cell = new PdfPCell(pgh.multiFontPara(WORK, pgh.getCellFont("FORMLABEL", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
				cell = new PdfPCell(pgh.multiFontPara(AgronPDFProductSpecificationGenerator2.getData(product, WORK_KEY),
						pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
			}

			if (spec != null) {
				config = (LCSSourcingConfig) VersionHelper
						.latestIterationOf((LCSSourcingConfigMaster) spec.getSpecSource());
				cell = new PdfPCell(pgh.multiFontPara(SOURCE, pgh.getCellFont("FORMLABEL", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
				cell = new PdfPCell(
						pgh.multiFontPara(AgronPDFProductSpecificationGenerator2.getData(config, SOURCENAME_KEY),
								pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);

				cell = new PdfPCell(
						pgh.multiFontPara(config.getFlexType().getAttribute(COUNTRYOFORIGIN_KEY).getAttDisplay() + ":",
								pgh.getCellFont("FORMLABEL", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
				cell = new PdfPCell(
						pgh.multiFontPara(AgronPDFProductSpecificationGenerator2.getData(config, COUNTRYOFORIGIN_KEY),
								pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
			}

			table.setWidths(widths);
		} catch (WTException e) {
			LCSLog.stackTrace(e);
		} catch (DocumentException e) {
			LCSLog.stackTrace(e);
		}
		return table;
	}

	public PdfPTable getRightTable(Map params, LCSProduct product, FlexSpecification spec, LCSSeason season) {
		PdfPCell cell = null;
		float[] widths = { 15.0F, 45.0F };
		PdfPTable table = new PdfPTable(2);

		try {
			cell = new PdfPCell(pgh.multiFontPara(SEASON, pgh.getCellFont("FORMLABEL", null, null)));
			cell.setBorder(0);
			table.addCell(cell);
			if (season != null) {
				cell = new PdfPCell(
						pgh.multiFontPara(AgronPDFProductSpecificationGenerator2.getData(season, SEASONNAME_KEY),
								pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
			} else {
				cell = new PdfPCell(pgh.multiFontPara("-", pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
			}

			if (product.getFlexType().getFullNameDisplay(true).contains(SMU_CLEARENCE)) {
				cell = new PdfPCell(
						pgh.multiFontPara(product.getFlexType().getAttribute(INLINEWORK_KEY).getAttDisplay() + ":",
								pgh.getCellFont("FORMLABEL", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
				cell = new PdfPCell(
						pgh.multiFontPara(AgronPDFProductSpecificationGenerator2.getData(product, INLINEWORK_KEY),
								pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
			} else if (season != null) {
				cell = new PdfPCell(pgh.multiFontPara(season.getFlexType().getAttribute(LINE_KEY).getAttDisplay() + ":",
						pgh.getCellFont("FORMLABEL", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
				cell = new PdfPCell(pgh.multiFontPara(AgronPDFProductSpecificationGenerator2.getData(season, LINE_KEY),
						pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
			} else {
				cell = new PdfPCell(pgh.multiFontPara(
						FlexTypeCache.getFlexTypeFromPath("Season\\Adidas").getAttribute(LINE_KEY).getAttDisplay()
								+ ":",
						pgh.getCellFont("FORMLABEL", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
				cell = new PdfPCell(pgh.multiFontPara("", pgh.getCellFont("DISPLAYTEXT", null, null)));
				cell.setBorder(0);
				table.addCell(cell);
			}

			cell = new PdfPCell(pgh.multiFontPara(SIZE_RUN, pgh.getCellFont("FORMLABEL", null, null)));
			cell.setBorder(0);
			table.addCell(cell);

			cell = AgronPSDUtil.drawSizeRangeCell(product, season);

			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(pgh.multiFontPara(DATE, pgh.getCellFont("FORMLABEL", null, null)));
			cell.setBorder(0);
			table.addCell(cell);
			cell = new PdfPCell(pgh.multiFontPara(this.dateVal, pgh.getCellFont("DISPLAYTEXT", null, null)));
			cell.setBorder(0);
			table.addCell(cell);

			table.setWidths(widths);
		} catch (WTException e) {
			LCSLog.stackTrace(e);
		} catch (DocumentException e) {
			LCSLog.stackTrace(e);
		}
		return table;
	}
}
