package com.agron.wc.product;

import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.client.web.pdf.PDFContentCollection;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.SpecPageSet;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import wt.util.WTException;

public class AgronCoverPage implements PDFContentCollection, SpecPageSet {
	private Collection pageTitles;
	private Collection pages = new ArrayList();

	private PDFGeneratorHelper pgh = new PDFGeneratorHelper();

	private static final String PAGE_TITLE = LCSProperties.get("com.agron.wc.techpack.summaryPageTitle", "Cover Page");

	private static final String DESIGN = LCSProperties.get("com.agron.wc.techpack.coverPage.teamInformation.design");

	private static final String MARKETING = LCSProperties
			.get("com.agron.wc.techpack.coverPage.teamInformation.marketing");

	private static final String DEVELOPMENT = LCSProperties
			.get("com.agron.wc.techpack.coverPage.teamInformation.development");

	public Collection getPDFContentCollection(Map paramMap, Document paramDocument) throws WTException {
		this.pages = (Collection) paramMap.get("AGRONCOVERPAGE_KEYS");

		this.pageTitles = new ArrayList();

		LCSProduct lCSProduct = null;
		LCSSeason lCSSeason = null;
		LCSSeasonProductLink lCSSeasonProductLink = null;

		try {
			lCSProduct = (LCSProduct) LCSQuery.findObjectById((String) paramMap.get("PRODUCT_ID"));
			if (paramMap.get("SEASONMASTER_ID") != null) {
				LCSSeasonMaster lcsSeasonMaster = (LCSSeasonMaster) LCSQuery.findObjectById((String) paramMap.get("SEASONMASTER_ID"));
				lCSSeason = (LCSSeason) VersionHelper.latestIterationOf(lcsSeasonMaster);
				lCSSeasonProductLink = LCSSeasonQuery.findSeasonProductLink(lCSProduct, lCSSeason);
			}
		} catch (WTException wTException) {

			LCSLog.stackTrace(wTException);
		}
		PdfPTable pdfPTable1 = new PdfPTable(1);
		PdfPCell pdfPCell1 = null;
		float[] arrayOfFloat = { 40.0F, 20.0F, 40.0F };
		PdfPTable pdfPTable2 = new PdfPTable(arrayOfFloat);
		pdfPTable2.setWidthPercentage(95.0F);
		pdfPCell1 = (new AgronCoverPageUtil()).createTableOfContent(this.pages, paramMap, paramDocument);

		pdfPTable2.addCell(pdfPCell1);
		pdfPTable2.addCell(createBlankDataCell());
		PdfPCell pdfPCell2 = teamInformationDataCell(lCSSeasonProductLink, lCSProduct);
		pdfPCell2.setBorder(0);

		pdfPTable2.addCell(pdfPCell2);
		pdfPCell1 = new PdfPCell(pdfPTable2);
		pdfPCell1.setBorder(0);
		pdfPTable1.addCell(pdfPCell1);
		pdfPTable1.addCell(createBlankDataCell());
		pdfPTable1.addCell(createBlankDataCell());
		pdfPTable1.addCell(createBlankDataCell());
		pdfPTable1.addCell(createBlankDataCell());

		ArrayList arrayList = new ArrayList();

		pdfPTable1.setWidthPercentage(95.0F);
		arrayList.add(pdfPTable1);
		this.pageTitles.add(PAGE_TITLE);
		return arrayList;
	}

	private PdfPCell teamInformationDataCell(LCSSeasonProductLink paramLCSSeasonProductLink, LCSProduct paramLCSProduct)
			throws WTException {
		String str = "FORMLABEL";
		PdfPTable pdfPTable1 = new PdfPTable(2);

		PdfPTable pdfPTable2 = new PdfPTable(1);
		PdfPTable pdfPTable3 = new PdfPTable(1);
		pdfPTable3.setWidthPercentage(95.0F);
		pdfPTable2.addCell(createBlankDataCell());
		pdfPTable2.addCell(createBlankDataCell());
		PdfPCell pdfPCell1 = new PdfPCell(
				this.pgh.multiFontPara("Team Information:", this.pgh.getCellFont(str, null, null)));
		pdfPCell1.setBorder(0);
		pdfPTable2.addCell(pdfPCell1);

		pdfPTable2.addCell(createBlankDataCell());
		pdfPTable2.addCell(createBlankDataCell());
		pdfPTable2.addCell(createBlankDataCell());
		pdfPTable2.addCell(createBlankDataCell());
		PdfPCell pdfPCell2 = null;

		try {
			if (paramLCSSeasonProductLink != null) {

				PdfPCell pdfPCell = new PdfPCell(this.pgh.multiFontPara(
						paramLCSSeasonProductLink.getFlexType().getAttribute(MARKETING).getAttDisplay() + ":",
						this.pgh.getCellFont(str, null, null)));
				pdfPCell.setBorder(0);
				pdfPTable1.addCell(pdfPCell);
				pdfPCell = new PdfPCell(this.pgh.multiFontPara(
						AgronPDFProductSpecificationGenerator2.getData(paramLCSSeasonProductLink, MARKETING),
						this.pgh.getCellFont("", null, null)));
				pdfPCell.setBorder(0);
				pdfPTable1.addCell(pdfPCell);

				pdfPCell = new PdfPCell(this.pgh.multiFontPara(
						paramLCSSeasonProductLink.getFlexType().getAttribute(DESIGN).getAttDisplay() + ":",
						this.pgh.getCellFont(str, null, null)));
				pdfPCell.setBorder(0);
				pdfPTable1.addCell(pdfPCell);
				pdfPCell = new PdfPCell(this.pgh.multiFontPara(
						AgronPDFProductSpecificationGenerator2.getData(paramLCSSeasonProductLink, DESIGN),
						this.pgh.getCellFont("", null, null)));
				pdfPCell.setBorder(0);
				pdfPTable1.addCell(pdfPCell);

				pdfPCell = new PdfPCell(this.pgh.multiFontPara(
						paramLCSSeasonProductLink.getFlexType().getAttribute(DEVELOPMENT).getAttDisplay() + ":",
						this.pgh.getCellFont(str, null, null)));
				pdfPCell.setBorder(0);
				pdfPTable1.addCell(pdfPCell);
				pdfPCell = new PdfPCell(this.pgh.multiFontPara(
						AgronPDFProductSpecificationGenerator2.getData(paramLCSSeasonProductLink, DEVELOPMENT),
						this.pgh.getCellFont("", null, null)));
				pdfPCell.setBorder(0);
				pdfPTable1.addCell(pdfPCell);
			} else {

				PdfPCell pdfPCell5 = new PdfPCell(this.pgh.multiFontPara(
						paramLCSProduct.getFlexType().getAttribute(MARKETING).getAttDisplay() + ":",
						this.pgh.getCellFont(str, null, null)));
				pdfPCell5.setBorder(0);
				pdfPTable1.addCell(pdfPCell5);
				pdfPCell5 = new PdfPCell(this.pgh.multiFontPara("", this.pgh.getCellFont("", null, null)));
				pdfPCell5.setBorder(0);
				pdfPTable1.addCell(pdfPCell5);

				PdfPCell pdfPCell6 = new PdfPCell(
						this.pgh.multiFontPara(paramLCSProduct.getFlexType().getAttribute(DESIGN).getAttDisplay() + ":",
								this.pgh.getCellFont(str, null, null)));
				pdfPCell6.setBorder(0);
				pdfPTable1.addCell(pdfPCell6);
				pdfPCell6 = new PdfPCell(this.pgh.multiFontPara("", this.pgh.getCellFont("", null, null)));
				pdfPCell6.setBorder(0);
				pdfPTable1.addCell(pdfPCell6);

				PdfPCell pdfPCell7 = new PdfPCell(this.pgh.multiFontPara(
						paramLCSProduct.getFlexType().getAttribute(DEVELOPMENT).getAttDisplay() + ":",
						this.pgh.getCellFont(str, null, null)));
				pdfPCell7.setBorder(0);
				pdfPTable1.addCell(pdfPCell7);
				pdfPCell7 = new PdfPCell(this.pgh.multiFontPara("", this.pgh.getCellFont("", null, null)));
				pdfPCell7.setBorder(0);
				pdfPTable1.addCell(pdfPCell7);
			}

			PdfPCell pdfPCell3 = new PdfPCell(pdfPTable1);

			pdfPCell3.setBorderWidth(1.0F);
			pdfPCell3.setPadding(5.0F);

			pdfPTable2.addCell(pdfPCell3);
			PdfPCell pdfPCell4 = new PdfPCell(pdfPTable2);
			pdfPCell4.setBorder(0);
			pdfPCell4.setPadding(3.0F);
			pdfPTable3.addCell(pdfPCell4);
			pdfPTable3.addCell(createBlankDataCell());
			pdfPCell2 = new PdfPCell(pdfPTable3);
			pdfPCell2.setPadding(2.0F);
			pdfPCell2.setBorder(0);
		} catch (WTException wTException) {
			LCSLog.stackTrace(wTException);
		}

		return pdfPCell2;
	}

	public Collection getPageHeaderCollection() {
		return this.pageTitles;
	}

	public PdfPCell returnTableOfContentCell(String paramString, int paramInt, boolean paramBoolean) {
		PdfPCell pdfPCell = null;
		if (paramBoolean) {
			pdfPCell = new PdfPCell(
					this.pgh.multiFontPara(paramString, this.pgh.getCellFont("FORMLABEL", null, "" + paramInt)));
		} else {

			pdfPCell = new PdfPCell(
					this.pgh.multiFontPara(paramString, this.pgh.getCellFont("DISPLAYTEXT", null, "" + paramInt)));
		}
		pdfPCell.setBorderWidth(0.0F);
		pdfPCell.setPaddingTop(1.0F);
		pdfPCell.setHorizontalAlignment(0);

		return pdfPCell;
	}

	public PdfPCell createBlankDataCell() {
    PdfPTable pdfPTable = new PdfPTable(1);
    PdfPCell pdfPCell = new PdfPCell(pdfPTable);
    pdfPCell.setPadding(1.0F);
    pdfPCell.setBorder(0);
    //this.pgh; 
    pdfPCell.setBackgroundColor(this.pgh.getColor(PDFGeneratorHelper.WHITE));
    
    pdfPCell.setHorizontalAlignment(0);
    pdfPCell.setVerticalAlignment(5);
    
    return pdfPCell;
  }
}
