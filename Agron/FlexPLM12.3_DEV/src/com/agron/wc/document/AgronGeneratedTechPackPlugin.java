package com.agron.wc.document;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.IteratedDocumentReferenceLink;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.VersionHelper;
import wt.doc.WTDocumentMaster;
import wt.fc.WTObject;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.foundation.LCSQuery;

//Agron FlexPLM UUpgrade 11.1 - Tech Pack Document Name - START
public class AgronGeneratedTechPackPlugin {
	WTPartMaster productMaster = null;
	FlexType flexTypeProd = null;
	LCSDocumentLogic documentLogic = null;
	LCSDocument document = null;
	
	public static void setTechPackObjName(WTObject obj) throws WTException, WTPropertyVetoException {
			
			IteratedDocumentReferenceLink iteratedDocumentReferenceLink = (IteratedDocumentReferenceLink) obj;

			// Getting the product,document and seasonproductLink object
			if (iteratedDocumentReferenceLink.getReferencedBy() instanceof FlexSpecification) {
				FlexSpecification spec = (FlexSpecification) iteratedDocumentReferenceLink.getReferencedBy();
				LCSProduct product = (LCSProduct) VersionHelper.latestIterationOf(spec.getSpecOwner());

				WTDocumentMaster docMaster = (WTDocumentMaster) iteratedDocumentReferenceLink.getReferences();
				LCSDocument document = (LCSDocument) VersionHelper.latestIterationOf(docMaster);
				if (VersionHelper.isCheckedOut(document)) {
					document = VersionHelper.getWorkingCopy(document);
				} else {
					document = VersionHelper.checkout(document);
				}
				
				String specName = spec.getName();
				
				if (specName != null && !"".equals(specName.trim())) {
					specName = "_" + specName;
				}
				else {
					specName = "";
				}
				
				int count = 1;
				boolean flag = true;
				while (flag) {
					String docName = product.getName() + specName + " - "+ count;
					PreparedQueryStatement pqs = new PreparedQueryStatement();
					pqs.appendSelectColumn(new QueryColumn("LCSDocument", "IDA2A2"));
					pqs.appendFromTable("LCSDocument");

					String documentColumnName = document.getFlexType().getAttribute("ptcdocumentName").getColumnName();
					pqs.appendCriteria(new Criteria(new QueryColumn("LCSDocument", documentColumnName), "?", "=", true),
							docName);
					System.out.println("pqs***********^^^^^^^^^^^^" + pqs);
					SearchResults results = LCSQuery.runDirectQuery(pqs);
					if (results.getResultsFound() > 0) {
						count++;
					} else {
						flag = false;
						document.setValue("name", docName);
					}
				}

				try {
					document = (LCSDocument) LCSDocumentLogic.persist(document, false);
				} catch (WTException wtException) {
					wtException.printStackTrace();
				} finally {
					if (VersionHelper.isCheckedOut(document)) {
						document = VersionHelper.checkin(document);
					}
				}

			}
	}
}
// Agron FlexPLM Upgrade 11.1 - Tech Pack Document Name - END