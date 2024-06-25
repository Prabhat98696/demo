package com.agron.wc.product;

import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.client.web.pdf.PDFContentCollection;
import com.lcs.wc.client.web.pdf.PDFTableGenerator;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectQuery;
import com.lcs.wc.product.SpecPageSet;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.SortHelper;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import wt.util.WTException;

public class AgronPDFSpecCommentGenerator implements PDFContentCollection, SpecPageSet {
	private static final String LCSMOAOBJECT = "LCSMOAObject";
	private Collection<String> pageTitles = null;

	private FlexSpecification spec;

	private static final String PAGE_TITLE = LCSProperties.get("com.agron.wc.techpack.specificationCommentsPageTitle",
			"Specification Comments Page");

	private static final String COMMENTS_TYPE = LCSProperties
			.get("com.agron.wc.techpack.specificationCommentsMultiObjectType", "Multi-Object\\Discussion");

	public Collection<PdfPTable> getPDFContentCollection(Map paramMap, Document paramDocument) throws WTException {
		LCSMOAObject lCSMOAObject = null;
		FlexObject flexObject1 = null;
		SearchResults searchResults = null;
		Vector vector = null;
		ArrayList arrayList = new ArrayList();
		Iterator iterator1 = null;
		FlexType flexType = null;
		Collection collection1 = null;
		Iterator iterator2 = null;
		FlexTypeAttribute flexTypeAttribute = null;
		FlexObject flexObject2 = null;
		Collection collection2 = new ArrayList();
		this.pageTitles = new ArrayList();

		try {
			PDFTableGenerator pDFTableGenerator = new PDFTableGenerator(paramDocument);
			this.spec = (FlexSpecification) LCSQuery.findObjectById((String) paramMap.get("SPEC_ID"));

			flexType = FlexTypeCache.getFlexTypeFromPath(COMMENTS_TYPE);

			collection1 = SortHelper.sortByComparableAttribute(new ArrayList(flexType.getAllAttributes()),
					"sortingValue");
			Collection collection = getColumns(collection1);
			searchResults = LCSMOAObjectQuery.findIteratedMOACollectionData(this.spec,
					this.spec.getFlexType().getAttribute("agrDiscussion"));
			if (searchResults != null && searchResults.getResults().size() > 0) {
				LinkedList linkedList = new LinkedList();

				vector = searchResults.getResults();

				Collection collection3 = SortHelper.sortFlexObjectsByNumber(vector, "LCSMOAOBJECT.SORTINGNUMBER");
				iterator1 = collection3.iterator();
				while (iterator1.hasNext()) {
					flexObject1 = (FlexObject) iterator1.next();
					flexObject2 = new FlexObject();
					lCSMOAObject = (LCSMOAObject) LCSQuery.findObjectById(
							"OR:com.lcs.wc.moa.LCSMOAObject:" + flexObject1.getString("LCSMOAOBJECT.IDA2A2"));
					iterator2 = collection1.iterator();

					while (iterator2.hasNext()) {
						flexTypeAttribute = (FlexTypeAttribute) iterator2.next();
						if (flexTypeAttribute.getAttKey().equals("enteredOnDate")) {

							try {

								SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
								SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MM/dd/yyyy");
								String str1 = AgronPDFProductSpecificationGenerator2.getData(lCSMOAObject,
										flexTypeAttribute.getAttKey());
								Date date = simpleDateFormat1.parse(str1);
								String str2 = simpleDateFormat2.format(date);
								flexObject2.put("LCSMOAObject." + flexTypeAttribute.getVariableName(), str2);
								continue;
							} catch (ParseException parseException) {

								System.out.println("Parse Exception : " + parseException);
								continue;
							}
						}

						flexObject2.put("LCSMOAObject." + flexTypeAttribute.getVariableName(),
								AgronPDFProductSpecificationGenerator2.getData(lCSMOAObject,
										flexTypeAttribute.getAttKey()));
					}

					if (flexObject2.getData("LCSMOAOBJECT.PTC_STR_1TYPEINFOLCSMOAOBJECT") != null
							&& !flexObject2.getData("LCSMOAOBJECT.PTC_STR_1TYPEINFOLCSMOAOBJECT").equals("")) {
						linkedList.add(flexObject2);
					}
				}

				collection2 = pDFTableGenerator.drawTables(linkedList, collection);
			}
		} catch (WTException wTException) {
			LCSLog.stackTrace(wTException);
		}

		for (byte b = 0; b < collection2.size(); b++) {
			this.pageTitles.add(PAGE_TITLE);
		}

		return collection2;
	}

	public Collection getPageHeaderCollection() {
		return this.pageTitles;
	}

	public Collection getColumns(Collection paramCollection) {
		TableColumn tableColumn = null;
		FlexTypeAttribute flexTypeAttribute = null;
		ArrayList arrayList = new ArrayList();
		Iterator iterator = paramCollection.iterator();

		while (iterator.hasNext()) {
			flexTypeAttribute = (FlexTypeAttribute) iterator.next();
			tableColumn = new TableColumn();
			System.out.println("flexTypeAttribute.getVariableName() 3333== "+flexTypeAttribute.getVariableName());
			System.out.println("flexTypeAttribute.getcolumnName() 3333== "+flexTypeAttribute.getColumnName());

			tableColumn.setTableIndex("LCSMOAObject." + flexTypeAttribute.getVariableName());
			tableColumn.setDisplayed(true);
			tableColumn.setHeaderLabel(flexTypeAttribute.getAttDisplay());
			if (flexTypeAttribute.getAttKey().equals("season")) {
				tableColumn.setPdfColumnWidthRatio(3.0F);

			} else if (flexTypeAttribute.getAttKey().equals("comments")) {
				tableColumn.setPdfColumnWidthRatio(10.0F);
			} else if (flexTypeAttribute.getAttKey().equals("user")) {
				tableColumn.setPdfColumnWidthRatio(1.5F);
			} else if (flexTypeAttribute.getAttKey().equals("enteredOnDate")) {
				tableColumn.setPdfColumnWidthRatio(1.5F);
			}
			arrayList.add(tableColumn);
		}
		return arrayList;
	}
}
