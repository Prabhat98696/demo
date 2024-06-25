package com.agron.wc.product;

import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import wt.util.WTProperties;

public class AgronProductThumbnailUtil {
	private static final String webHomeLocation = LCSProperties.get("flexPLM.webHome.location");

	private static float fixedHeight = 45.0F;

	public PdfPCell createThumbnailLogoCell(LCSProduct product) {
		String wthome = "";
		String imageFile = "";
		String imageNotFoundFile = "";
		Image img = null;
		PdfPCell cell = null;

		try {
			imageNotFoundFile = LCSProperties.get("com.lcs.wc.product.PDFProductSpecificationHeader.imageNotFound",FormatHelper.formatOSFolderLocation(webHomeLocation) + "/images/ImageNotAvailable_2.PNG");
			wthome = WTProperties.getServerProperties().getProperty("wt.home");

			imageNotFoundFile = wthome + File.separator + imageNotFoundFile;
			String thumbnailImageURL = product.getPartPrimaryImageURL();

			if (FormatHelper.hasContent(thumbnailImageURL)) {

				thumbnailImageURL = thumbnailImageURL.replace("/Windchill/images/", "");
				wthome = WTProperties.getServerProperties().getProperty("wt.home");

				imageFile = wthome + File.separator + "codebase" + File.separator + "images" + File.separator
						+ thumbnailImageURL;
				//System.out.println("imageFile -- "+ imageFile);
				try {
					img = Image.getInstance(imageFile);
					//System.out.println("Found Image  -- "+ img);
				} catch (FileNotFoundException e) {
					img = Image.getInstance(imageNotFoundFile);
					//System.out.println("Image Not Found -- "+ img);
				}
			} else {
				img = Image.getInstance(imageNotFoundFile);
				//System.out.println("Image Not Found 2 -- "+ img);
			}
			//img.scaleToFit(img.getWidth(), fixedHeight + 3.0F);
			cell = new PdfPCell(img, true);
			cell.setUseBorderPadding(true);
			cell.setPadding(4.0F);
			cell.setBorder(0);

			cell.setHorizontalAlignment(1);
			cell.setVerticalAlignment(1);

			cell.setFixedHeight(fixedHeight);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadElementException e) {
			e.printStackTrace();
		}
		return cell;
	}
}
