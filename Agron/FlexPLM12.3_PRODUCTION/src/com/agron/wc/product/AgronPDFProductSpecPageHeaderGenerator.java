package com.agron.wc.product;

import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import wt.util.WTMessage;

public class AgronPDFProductSpecPageHeaderGenerator extends PdfPageEventHelper {
	private PdfTemplate tpl;
	private PdfTemplate tp2;
	private BaseFont font;
	public String headerTextLeft = "Test1";

	public String headerTextCenter = "TEST";

	public Map params = null;

	public LCSProduct product = null;

	public FlexSpecification spec = null;

	public String fontClass = "TABLESECTIONHEADER";

	private String pageNumFontClass = "PAGE_NUMBERS";

	private PDFGeneratorHelper pgh = new PDFGeneratorHelper();

	private float cellHeight = 15.0F;
	private PdfPTable headerTable = null;
	public LCSSeason season = null;
	private int i = 0;

	private String footerText = LCSProperties
			.get("com.agron.wc.product.AgronPDFProductSpecPageHeaderGenerator.footerText");

	public static void main(String[] args) {
		try {
			Document doc = new Document(PageSize.A4, 50, 50, 100, 72);

			PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("test.pdf"));

			writer.setPageEvent(new AgronPDFProductSpecPageHeaderGenerator());
			doc.open();

			String text = "some padding text ";

			Paragraph p = new Paragraph(text);
			p.setAlignment(3);
			doc.add(p);

			doc.close();
		} catch (NullPointerException e) {
			LCSLog.stackTrace(e);
		} catch (DocumentException e) {
			LCSLog.stackTrace(e);
		} catch (FileNotFoundException e) {
			LCSLog.stackTrace(e);
		}
	}

	public void onOpenDocument(PdfWriter writer, Document document) {
		try {
			this.i = 0;

			this.tpl = writer.getDirectContent().createTemplate(100, 100);

			this.tpl.setBoundingBox(new Rectangle(-20, -20, 100, 100));

			this.tp2 = writer.getDirectContent().createTemplate(100.0F, 100.0F);

			this.tp2.setBoundingBox(new Rectangle(-20.0F, -20.0F, 100.0F, 100.0F));

			this.font = BaseFont.createFont("Helvetica", "Cp1252", false);

		} catch (DocumentException e) {
			LCSLog.stackTrace(e);
		} catch (IOException e) {

			LCSLog.stackTrace(e);
		}
	}

	public void onStartPage(PdfWriter writer, Document document) {
		try {
			if (this.i != 0) {
				this.headerTable = (new AgronPDFHeaderGenerator()).drawHeader(this.params, this.product, this.spec,
						this.season);
				document.add(this.headerTable);
			}
			this.i++;
		} catch (DocumentException e) {
			LCSLog.stackTrace(e);
		}
	}

	public void onEndPage(PdfWriter writer, Document document) {
		PdfContentByte cb = writer.getDirectContent();
		cb.saveState();
		cb.restoreState();
		Font cellfont = this.pgh.getCellFont(this.fontClass, null, "8");

		PdfPTable table = new PdfPTable(3);

		table.setTotalWidth(document.right() - document.left());

		PdfPCell left = new PdfPCell(this.pgh.multiFontPara(this.headerTextLeft, cellfont));
		left.setHorizontalAlignment(0);
		left.setFixedHeight(this.cellHeight);
		left.setBorder(0);
		table.addCell(left);

		PdfPCell center = new PdfPCell(this.pgh.multiFontPara(this.spec.getName(), cellfont));
		center.setHorizontalAlignment(0);
		center.setFixedHeight(this.cellHeight);
		center.setBorder(0);
		table.addCell(center);

		Object[] objB = { Integer.toString(writer.getPageNumber()) };
		String text = WTMessage.getLocalizedMessage("com.lcs.wc.resource.MainRB", "pageOf_LBL", objB, Locale.ENGLISH);
		Font pageOfFont = this.pgh.getCellFont(this.pageNumFontClass, null, "8");
		this.font = pageOfFont.getCalculatedBaseFont(false);
		PdfPCell pageOf = new PdfPCell(this.pgh.multiFontPara(text, pageOfFont));
		pageOf.setPaddingRight(this.font.getWidthPoint("0000", 8.0F));
		pageOf.setHorizontalAlignment(2);
		pageOf.setBorder(0);
		table.addCell(pageOf);

		table.writeSelectedRows(0, -1, document.left(), document.getPageSize().getHeight(), cb);

		float textBase = document.getPageSize().getHeight() - 9.0F;

		float adjust = this.font.getWidthPoint("000", 8.0F);
		cb.addTemplate(this.tpl, document.right() - adjust, textBase);
		cb.saveState();
		cb.restoreState();

		PdfContentByte cb1 = writer.getDirectContent();
		cb1.saveState();
		cb1.restoreState();
		String pageInfo = this.footerText;
		cb1.beginText();
		cb1.setFontAndSize(this.font, 8.0F);
		cb1.setTextMatrix((document.right() - document.left()) / 2.0F, document.bottom() - 15.0F);
		cb1.showText(this.footerText);
		cb1.endText();
		cb1.addTemplate(this.tp2, (document.right() - document.left()) / 2.0F + this.font.getWidthPoint(pageInfo, 8.0F),
				document.bottom() - 15.0F);
		cb1.saveState();
		cb1.restoreState();
	}

	public void onCloseDocument(PdfWriter writer, Document document) {
		this.tpl.setColorFill(this.pgh.getColor(this.pageNumFontClass));
		this.tpl.setColorStroke(this.pgh.getColor(this.pageNumFontClass));
		this.tpl.beginText();
		this.tpl.setFontAndSize(this.font, 8.0F);
		this.tpl.setTextMatrix(0.0F, -1.0F);
		this.tpl.showText("" + (writer.getPageNumber() - 1));
		this.tpl.endText();
	}
}