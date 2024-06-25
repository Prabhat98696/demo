package com.agron.wc.product;

import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.client.web.pdf.PDFContentCollection;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.product.SpecPageSet;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleQuery;
import com.lcs.wc.sample.LCSSampleRequest;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import wt.fc.WTObject;
import wt.part.WTPartMaster;
import wt.util.WTException;


public class AgronPDFFitSpecReportsGenerator
  implements PDFContentCollection, SpecPageSet
{
  private Collection<String> pageTitles = null;
  private static final String PAGE_TITLE = LCSProperties.get("com.agron.wc.techpack.fitSpecReportsPageTitle");

  
  private static final String FORMLABLE = "FORMLABEL";
  
  private static final String DISPLAYTEXT = "DISPLAYTEXT";
  
  private static PDFGeneratorHelper pgh = new PDFGeneratorHelper();


  
  public Collection getPageHeaderCollection() { return this.pageTitles; }



  
  public Collection getPDFContentCollection(Map params, Document document) throws WTException {
    FlexSpecification spec = null;
    LCSProduct product = null;
    Collection collection = null;
    String productId = "PRODUCT_ID";
    String spceId = "SPEC_ID";
    
    this.pageTitles = new ArrayList();
    WTObject obj = (WTObject)LCSProductQuery.findObjectById((String)params.get(productId));
    WTObject obj2 = (WTObject)LCSProductQuery.findObjectById((String)params.get(spceId));
    product = (LCSProduct)obj;
    spec = (FlexSpecification)obj2;
    
    PdfPTable sampleTableColl = getSample(product, spec);
    if (sampleTableColl != null) {
      collection = new ArrayList();
      collection.add(sampleTableColl);
    } 
    this.pageTitles.add(PAGE_TITLE);
    return collection;
  }
  
  public PdfPTable getSample(LCSProduct product, FlexSpecification spec) throws WTException {
    PdfPTable table = null;
    FlexType sampleFlexType = FlexTypeCache.getFlexTypeFromPath("Sample\\Product\\Product Sample");
    String smpIda2a2 = "";
    Collection sampleColComb = new ArrayList();
    Collection samplesIda2a = null;
    FlexObject flexObj = null;
    PdfPTable sampleTable = null;
    Hashtable criteria = new Hashtable();
    
    LCSSourcingConfig sourcingConfig = (LCSSourcingConfig)VersionHelper.latestIterationOf((LCSSourcingConfigMaster)spec.getSpecSource());
    SearchResults sampleRequestsNew = LCSSampleQuery.findSamplesForProduct(product, criteria, null, sourcingConfig, sampleFlexType, spec);
    Collection sampleColNew = sampleRequestsNew.getResults();
    if (sampleColNew != null) {
      sampleColComb.addAll(sampleColNew);
    }




    
    Collection sampleRequestCol = AgronDataUtil.getSampleRequestsCol(sampleColComb);
    if (sampleRequestCol != null && sampleRequestCol.size() > 0) {
      FlexObject srObj = null;
      FlexObject sampleObj = null;
      LCSSampleRequest sampleRequest = null;
      Collection sampleCol = null;
      Iterator sampleIter = null;
      String sampleReqName = null;
      PdfPCell cell = null;
      PdfPCell contentCell = null;
      PdfPTable contentTable = null;
      Iterator srIter = sampleRequestCol.iterator();
      while (srIter.hasNext()) {
        srObj = (FlexObject)srIter.next();
        samplesIda2a = new ArrayList();
        sampleRequest = (LCSSampleRequest)LCSQuery.findObjectById(srObj.getData("LCSSAMPLEREQUEST.OBJECT"));
        
        sampleReqName = sampleRequest.getName();
        cell = new PdfPCell(pgh.multiFontPara("Sample Request Name : " + sampleReqName, pgh.getCellFont("FORMLABEL", null, null)));
        cell.setBorder(0);

        
        sampleCol = (new LCSSampleQuery()).findSamplesIdForSampleRequest(sampleRequest, true);
        sampleIter = sampleCol.iterator();
        while (sampleIter.hasNext()) {
          sampleObj = (FlexObject)sampleIter.next();
          samplesIda2a.add(sampleObj.getString("LCSSAMPLE.IDA2A2"));
        } 
        contentTable = AgronPDFFitSpecReportsGeneratorUtil.getSampleCommentTable(samplesIda2a);
        if (contentTable != null) {
          sampleTable = new PdfPTable(1);
          contentCell = new PdfPCell(contentTable);
          contentCell.setBorder(0);
          sampleTable.addCell(cell);
          cell = new PdfPCell(pgh.multiFontPara("", pgh.getCellFont("FORMLABEL", null, null)));
          cell.setBorder(0);
          sampleTable.addCell(cell);


          
          sampleTable.addCell(contentCell);
          cell = new PdfPCell(pgh.multiFontPara("", pgh.getCellFont("FORMLABEL", null, null)));
          cell.setBorder(0);
          sampleTable.addCell(cell);
          cell = new PdfPCell(pgh.multiFontPara("", pgh.getCellFont("FORMLABEL", null, null)));
          cell.setBorder(0);
          sampleTable.addCell(cell);
          cell = new PdfPCell(pgh.multiFontPara("", pgh.getCellFont("FORMLABEL", null, null)));
          cell.setBorder(0);
          sampleTable.addCell(cell);
        } 
      } 
    } 











    
    return sampleTable;
  }

  
  public static PdfPTable drawSampleInfo(LCSSample sample, String sampleSize) {
    PdfPTable sampleTable = new PdfPTable(4);
    LCSSampleRequest sampleReqObj = sample.getSampleRequest();
    String sampleReqName = "";
    String colorwayName = "";
    String sampleName = "";
    String sampleComments = "";
    PdfPCell cell = null;
    
    try {
      sampleReqName = sampleReqObj.getName();






      
      if (sample.getColor() != null) {
        LCSSKU skuObj = (LCSSKU)VersionHelper.latestIterationOf((WTPartMaster)sample.getColor());
        LCSSKU skuAObj = (LCSSKU)VersionHelper.getVersion(skuObj, "A");
        colorwayName = (String)skuAObj.getValue("skuName");
      } 
      cell = new PdfPCell(pgh.multiFontPara("Colorway:", pgh.getCellFont("FORMLABEL", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);
      cell = new PdfPCell(pgh.multiFontPara(colorwayName, pgh.getCellFont("", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);
      
      sampleName = sample.getSampleName();
      cell = new PdfPCell(pgh.multiFontPara("Sample Name : ", pgh.getCellFont("FORMLABEL", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);
      cell = new PdfPCell(pgh.multiFontPara(sampleName, pgh.getCellFont("", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);
      
      cell = new PdfPCell(pgh.multiFontPara("Size:", pgh.getCellFont("FORMLABEL", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);
      cell = new PdfPCell(pgh.multiFontPara(sampleSize, pgh.getCellFont("", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);
      
      if (sample.getValue("comments") != null) {
        sampleComments = sample.getValue("comments").toString();
      }
      cell = new PdfPCell(pgh.multiFontPara("Comments:", pgh.getCellFont("FORMLABEL", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);
      cell = new PdfPCell(pgh.multiFontPara(sampleComments, pgh.getCellFont("", null, null)));
      cell.setBorder(0);
      sampleTable.addCell(cell);







    
    }
    catch (WTException e) {
      LCSLog.stackTrace(e);
    } 
    return sampleTable;
  }
}
