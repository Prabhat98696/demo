package com.agron.wc.product;

import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import wt.util.WTException;
import wt.util.WTProperties;

public class AgronPDFHeaderGenerator {
	private static PDFGeneratorHelper pgh = new PDFGeneratorHelper();

	private static final String SPORTS_TYPE = LCSProperties
			.get("com.agron.wc.product.PDFProductSpecificationHeader.season.sportsStyleLbl");

	private static final String seasonLine = LCSProperties
			.get("com.agron.wc.product.PDFProductSpecificationHeader.season.line");
	
	private static final String PRODUCT_BRAND_KEY=LCSProperties.get("com.agron.wc.product.PDFProductSpecificationHeader.productBrandKey","agrDrbrand");
	private static final  String PRODUCT_BRAND_ORIGINALS_KEY=LCSProperties.get("com.agron.wc.product.PDFProductSpecificationHeader.productBrandOriginalsKey","agrOriginals");
	private static final  String COMPANY_SS_LOGO =LCSProperties.get("com.agron.wc.product.PDFProductSpecificationHeader.companySSLogo");		
	private static final  String COMPANY_SP_LOGO = LCSProperties.get("com.agron.wc.product.PDFProductSpecificationHeader.companySPLogo");		

	private float fixedHeight = 45.0F;

	public PdfPTable drawHeader(Map params, LCSProduct product, FlexSpecification spec, LCSSeason season) {
		PdfPCell dataCell = null;
		PdfPCell cell = null;
		float[] widths = { 10.0F, 40.0F, 40.0F, 10.0F };
		PdfPTable finalTable = new PdfPTable(1);
		PdfPTable headerTable = new PdfPTable(4);
		try {	
			headerTable.addCell(createLogoCell(season,product));
			dataCell = new PdfPCell((new AgronPDFHeaderTableGenerator()).getLeftTable(params, product, spec, season));
			dataCell.setBorder(0);

			headerTable.addCell(dataCell);
			dataCell = new PdfPCell((new AgronPDFHeaderTableGenerator()).getRightTable(params, product, spec, season));

			dataCell.setBorder(0);
			headerTable.addCell(dataCell);
			headerTable.addCell((new AgronProductThumbnailUtil()).createThumbnailLogoCell(product));
			headerTable.setWidths(widths);
			cell = new PdfPCell(headerTable);
			cell.setBorderColor(pgh.getColor("HEX669999"));
			finalTable.addCell(cell);
			finalTable.setWidthPercentage(95.0F);
			finalTable.setSpacingAfter(20.0F);
		} catch (DocumentException e) {

			e.printStackTrace();
		}
		return finalTable;
	}

	private PdfPCell createLogoCell(LCSSeason season,LCSProduct product) {
		String wthome = "";
		String imageFile = "";
		PdfPCell cell = null;
		String productBrand="";
		try {
			String line = AgronPDFProductSpecificationGenerator2.getData(season, seasonLine);
			 try {
				productBrand=(String)product.getValue(PRODUCT_BRAND_KEY);
			} catch (WTException e) {
				e.printStackTrace();
			}
			if (SPORTS_TYPE.equals(AgronPDFProductSpecificationGenerator2.getData(season, seasonLine)) || PRODUCT_BRAND_ORIGINALS_KEY.equals(productBrand)) {
				imageFile = COMPANY_SS_LOGO;
			} else {
				imageFile = COMPANY_SP_LOGO;
			}
			wthome = WTProperties.getServerProperties().getProperty("wt.home");
			imageFile = wthome + File.separator + imageFile;
			Image img = Image.getInstance(imageFile);
			cell = new PdfPCell(img, true);
			cell.setUseBorderPadding(true);
			cell.setPadding(4.0F);
			cell.setHorizontalAlignment(1);
			cell.setVerticalAlignment(1);
			cell.setBorder(0);
			cell.setFixedHeight(this.fixedHeight);
			
			
		} catch (IOException e) {

			e.printStackTrace();
		} catch (BadElementException e) {

			e.printStackTrace();
		}
		return cell;
	}
	
}
