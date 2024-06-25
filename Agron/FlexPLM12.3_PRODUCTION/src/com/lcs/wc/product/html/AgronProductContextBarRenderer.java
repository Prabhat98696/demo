package com.lcs.wc.product.html;

import com.agron.wc.season.AgronSeasonSortHelper;
import com.lcs.wc.client.ApplicationContext;
import com.lcs.wc.client.web.FormGenerator2;
import com.lcs.wc.client.web.html.header.context.ContextBarRenderer;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.sizing.SizingQuery;
import com.lcs.wc.sourcing.LCSSourceToSeasonLink;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.RB;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import wt.enterprise.RevisionControlled;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTMessage;

public class AgronProductContextBarRenderer implements ContextBarRenderer {
  private ApplicationContext appContext = null;
  
  private String noneSelectedLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.SeasonRB", "noneSelected_LBL", RB.objA);
  
  private String noneAvailableLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.SeasonRB", "noneAvailable_LBL", RB.objA);
  
  public static final boolean USE_PRODUCTDESTINATIONS = LCSProperties.getBoolean("com.lcs.wc.product.useProductDestinations");
  
  public String renderContextBar(FlexPlmWebRequestContext phv) throws WTException {
    StringBuilder productContextBuilder = new StringBuilder();
    if (phv.getApplicationContext() != null) {
      this.appContext = phv.getApplicationContext();
      productContextBuilder
        .append("<div id=\"product-context-selector\" class=\"f-attribute-group product-context-bar\">");
      renderSeasonsDropDown(productContextBuilder, phv);
      renderSourcesDropDown(productContextBuilder, phv);
      renderSpecsDropDown(productContextBuilder, phv);
      renderSKUsDropDown(productContextBuilder, phv);
      if (USE_PRODUCTDESTINATIONS)
        renderDestinationsDropDown(productContextBuilder, phv); 
      renderSizesDropDowns(productContextBuilder, phv);
      productContextBuilder.append("</div>");
    } 
    return productContextBuilder.toString();
  }
  
  private void renderSeasonsDropDown(StringBuilder productContextBuilder, FlexPlmWebRequestContext phv) throws WTException {
    String seasonLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.SeasonRB", "season_LBL", RB.objA);
    if (phv.isTimeSliceView() || phv.isEditBOMMode()) {
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      productContextBuilder.append(FormGenerator2.createDisplay(seasonLabel, this.appContext
            
            .getSeason().getName(), "STRING_FORMAT"));
      productContextBuilder.append("</div>");
    } else {
      HashMap<String, String> seasonTable = this.appContext.getSeasonsForProductMap();
      String splId = null;
      Vector<String> order = new Vector<>();
      order = AgronSeasonSortHelper.sortedSeasonOrder(seasonTable); 
      if (!seasonTable.isEmpty()) {
        seasonTable.put(this.appContext.getProductARevId(), this.noneSelectedLabel);
        order.add(0, this.appContext.getProductARevId());
      } else {
        seasonTable.put("", this.noneAvailableLabel);
      } 
      if (this.appContext.getProductLinkId() != null) {
        splId = this.appContext.getProductLinkId();
      } else {
        splId = this.appContext.getProductARevId();
      } 
      String label = seasonLabel + " (" +  (seasonTable.size() - 1 ) + ")";
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      productContextBuilder
        .append(getDropDownListWidget(label, "splId", null, splId, null, seasonTable, order, null, "switchSPL(this.options[this.selectedIndex].value);"));
      productContextBuilder.append("</div>");
    } 
  }
  
  private void renderSourcesDropDown(StringBuilder productContextBuilder, FlexPlmWebRequestContext phv) throws WTException {
    if (this.appContext.getProductARev().getFlexType()
      .getReferencedFlexType("SOURCING_CONFIG_TYPE_ID") != null) {
      String sourcingConfigId = null;
      Vector<String> order = null;
      if (!phv.isTimeSliceView() || (phv.isTimeSliceView() && phv.getSourcingConfig() != null)) {
        Collection<LCSSourcingConfig> sourcingList = phv.getSourcingConfigList();
        if (sourcingList != null && sourcingList.size() > 0) {
          Iterator<LCSSourcingConfig> configs = phv.getSourcingConfigList().iterator();
          if (configs.hasNext()) {
            phv.getSourcingConfigTable().put(phv.getProdActiveId(), this.noneSelectedLabel);
            order = new Vector<>();
            order.add(0, phv.getProdActiveId());
          } 
        } else {
          phv.getSourcingConfigTable().put("", this.noneAvailableLabel);
        } 
      } 
      if (phv.getStsl() != null) {
        sourcingConfigId = FormatHelper.getVersionId((RevisionControlled)phv.getStsl());
      } else if (phv.getSourcingConfig() != null) {
        sourcingConfigId = FormatHelper.getVersionId((RevisionControlled)phv.getSourcingConfig());
      } else {
        sourcingConfigId = phv.getProdActiveId();
      } 
      if (phv.isEditBOMMode());
      String scName = "";
      if (phv.isTimeSliceView())
        if (phv.getSourcingConfig() != null) {
          scName = (String)phv.getSourcingConfig().getValue("name");
        } else {
          scName = this.noneSelectedLabel;
        }  
     // System.out.println("phv.getSourcingConfigTable()-----"+phv.getSourcingConfigTable());
      HashMap<String,String> SourcingHashMap = (HashMap)phv.getSourcingConfigTable();
      if(SourcingHashMap != null)
      {
    	  for (Map.Entry<String, String> set :
    		  SourcingHashMap.entrySet()) {
    		  // Printing all elements of a Map
             // System.out.println(set.getKey() + " = " + set.getValue());
              if(set.getKey().toString().contains("LCSSourceToSeasonLink"))
              {
            	  LCSSourceToSeasonLink stsl = (LCSSourceToSeasonLink)LCSQuery.findObjectById(set.getKey().toString());
            	  if(stsl.isPrimarySTSL())
            	  {
            		  //phv.getSourcingConfigTable().put(set.getKey(),"(Primary)"+set.getValue() );
            		  set.setValue("(Primary) "+ set.getValue().toString());
            		  break;
            	  }
              }
            
         }
    	  
      }
      String sourceLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.SeasonRB", "source_LBL", RB.objA);
      if (phv.isTimeSliceView()) {
        productContextBuilder.append("<div class=\"f-attribute-box\">");
        productContextBuilder
          .append(FormGenerator2.createDisplay(sourceLabel, scName, "STRING_FORMAT"));
        productContextBuilder.append("<input type=\"hidden\" name=\"sourcingConfigId\" value=\"" + 
            FormatHelper.getObjectId((WTObject)phv.getSourcingConfig()) + "\">");
        productContextBuilder.append("</div>");
      } else {
        String label = sourceLabel + " (" + (phv.getSourcingConfigTable().size() - 1) + ")";
        productContextBuilder.append("<div class=\"f-attribute-box\">");
        productContextBuilder.append(getDropDownListWidget(label, "sourcingConfigId", null, sourcingConfigId, null, phv
              .getSourcingConfigTable(), order, null, "switchSourcingConfig(this.options[this.selectedIndex].value);"));
        productContextBuilder.append("</div>");
      } 
    } 
  }
  
  private void renderSpecsDropDown(StringBuilder productContextBuilder, FlexPlmWebRequestContext phv) throws WTException {
    HashMap<String, String> specTable = this.appContext.getSpecsMap();
    Vector<String> order = null;
    String contextSpecId = phv.getContextSpecId();
    if (!phv.isTimeSliceView()) {
      if (!ACLHelper.hasViewAccess(phv.getSpecType()))
        specTable = new HashMap<>(); 
      if (specTable.isEmpty()) {
        specTable.put("", this.noneAvailableLabel);
      } else if (phv.getStsl() != null) {
        specTable.put(FormatHelper.getVersionId((RevisionControlled)phv.getStsl()), this.noneSelectedLabel);
        order = new Vector<>();
        order.add(0, FormatHelper.getVersionId((RevisionControlled)phv.getStsl()));
        if (!FormatHelper.hasContent(contextSpecId))
          contextSpecId = FormatHelper.getVersionId((RevisionControlled)phv.getStsl()); 
      } else if (phv.getSourcingConfig() != null) {
        specTable.put(FormatHelper.getVersionId((RevisionControlled)phv.getSourcingConfig()), this.noneSelectedLabel);
        order = new Vector<>();
        order.add(0, FormatHelper.getVersionId((RevisionControlled)phv.getSourcingConfig()));
        if (!FormatHelper.hasContent(contextSpecId))
          contextSpecId = FormatHelper.getVersionId((RevisionControlled)phv.getSourcingConfig()); 
      } else {
        specTable.put(phv.getProdActiveId(), this.noneSelectedLabel);
        order = new Vector<>();
        order.add(0, phv.getProdActiveId());
        if (!FormatHelper.hasContent(contextSpecId))
          contextSpecId = phv.getProdActiveId(); 
      } 
    } 
    String specDisplay = "";
    if (this.appContext.getSpecification() != null) {
      specDisplay = (String)this.appContext.getSpecification().getValue("specName");
    } else {
      specDisplay = this.noneSelectedLabel;
    } 
    if (phv.isTimeSliceView()) {
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      String specLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.SpecificationRB", "specification_LBL", RB.objA);
      productContextBuilder
        .append(FormGenerator2.createDisplay(specLabel, specDisplay, "STRING_FORMAT"));
      productContextBuilder.append("</div>");
    } else {
      String specificationLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.ContextBarsRB", "specification_LBL", RB.objA);
      String label = specificationLabel + " (" + (specTable.size() - 1) + ")";
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      productContextBuilder.append(
          getDropDownListWidget(label, "contextSpecId", null, contextSpecId, null, specTable, order, null, "switchSpec(this.options[this.selectedIndex].value);"));
      productContextBuilder.append("</div>");
    } 
  }
  
  private void renderSKUsDropDown(StringBuilder productContextBuilder, FlexPlmWebRequestContext phv) throws WTException {
    HashMap<String, String> skuTable = this.appContext.getSKUsMap();
    Vector<String> order = new Vector<>();
    String contextSKUId = phv.getContextSKUId();
    
    LCSSeason ctxSeason = appContext.getSeason();
   	if(ctxSeason != null){
   		//out.println(" Season is "+ctxSeason.getValue("seasonName"));		
   		
   			Iterator itr = skuTable.keySet().iterator();
   					while (itr.hasNext()) {
   							String skuId1 = (String)itr.next();
   							String skuName = (String) skuTable.get(skuId1);
   							LCSSKU sku1 = (LCSSKU)LCSQuery.findObjectById(skuId1);
   							sku1.getVersionDisplayIdentifier();
   							LCSSKUSeasonLink skLink =(LCSSKUSeasonLink)LCSSeasonQuery.findSeasonProductLink( sku1,  ctxSeason);													
   							if(skLink != null &&  "agrDiscontinued".equalsIgnoreCase((String) skLink.getValue("agrColorwayStatus"))) {														
   								//skuTable.put(skuId1, skuName + " - "+ ((String) skLink.getValue("agrColorwayStatus")).substring(3));
   								skuTable.put(skuId1, "Disc - "+skuName);
   							}else if(skLink != null &&  "agrDropped".equalsIgnoreCase((String) skLink.getValue("agrColorwayStatus"))){
   								//skuTable.put(skuId1, skuName + " - "+ ((String) skLink.getValue("agrColorwayStatus")).substring(3));
   								skuTable.put(skuId1, "Drop - "+skuName);
   							}else{
   								skuTable.put(skuId1, skuName);
   							}
   						}
   	}							
    
    if (!phv.isTimeSliceView()) {
      if (skuTable.isEmpty()) {
        skuTable.put("", this.noneAvailableLabel);
      } else if (this.appContext.getFstsl() != null) {
        skuTable.put(FormatHelper.getObjectId((WTObject)this.appContext.getFstsl()), this.noneSelectedLabel);
        order.add(0, FormatHelper.getObjectId((WTObject)this.appContext.getFstsl()));
        if (!FormatHelper.hasContent(contextSKUId))
          contextSKUId = FormatHelper.getObjectId((WTObject)this.appContext.getFstsl()); 
      } else if (this.appContext.getSpecification() != null) {
        skuTable.put(FormatHelper.getVersionId((RevisionControlled)this.appContext.getSpecification()), this.noneSelectedLabel);
        order.add(0, FormatHelper.getVersionId((RevisionControlled)this.appContext.getSpecification()));
        if (!FormatHelper.hasContent(contextSKUId))
          contextSKUId = FormatHelper.getVersionId((RevisionControlled)this.appContext.getSpecification()); 
      } else if (phv.getStsl() != null) {
        skuTable.put(FormatHelper.getVersionId((RevisionControlled)phv.getStsl()), this.noneSelectedLabel);
        order.add(0, FormatHelper.getVersionId((RevisionControlled)phv.getStsl()));
        if (!FormatHelper.hasContent(contextSKUId))
          contextSKUId = FormatHelper.getVersionId((RevisionControlled)phv.getStsl()); 
      } else if (phv.getSourcingConfig() != null) {
        skuTable.put(FormatHelper.getVersionId((RevisionControlled)phv.getSourcingConfig()), this.noneSelectedLabel);
        order.add(0, FormatHelper.getVersionId((RevisionControlled)phv.getSourcingConfig()));
        if (!FormatHelper.hasContent(contextSKUId))
          contextSKUId = FormatHelper.getVersionId((RevisionControlled)phv.getSourcingConfig()); 
      } else {
        skuTable.put(phv.getProdActiveId(), this.noneSelectedLabel);
        order.add(0, phv.getProdActiveId());
        if (!FormatHelper.hasContent(contextSKUId))
          contextSKUId = phv.getProdActiveId(); 
      } 
      if (null != phv.getRequest().getParameter("returnOid") && phv
        .getRequest().getParameter("returnOid").contains("LCSDocument")) {
        LCSDocument document = (LCSDocument)LCSQuery.findObjectById(phv.getRequest().getParameter("returnOid"), true, false);
        if (null != document && LCSDocumentLogic.isImagePage(document)) {
          LCSSKU sku = LCSDocumentLogic.getColorwayFromColorwayImagePage(document);
          if (null != sku)
            contextSKUId = "VR:com.lcs.wc.product.LCSSKU:" + sku.getBranchIdentifier(); 
        } 
      } 
      if ((skuTable != null && !skuTable.isEmpty() && "BOM".equals(phv.tabPage)) || phv
        .isEditBOMMode()) {
        String viewAllLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.SeasonRB", "viewAll_LBL", RB.objA);
        skuTable.put("ALL_SKUS", viewAllLabel);
      } 
    } 
    String skuDisplay = "";
    if (phv.isSkuLevel()) {
      skuDisplay = (String)this.appContext.getSKUARev().getValue("skuName");
    } else {
      skuDisplay = this.noneSelectedLabel;
    } 
    if (phv.isTimeSliceView()) {
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      productContextBuilder.append(FormGenerator2.createDisplay(this.appContext
            .getProductARev().getFlexType().getAttribute("skuName").getAttDisplay(), skuDisplay, "STRING_FORMAT"));
      productContextBuilder.append("<input type=\"hidden\" name=\"contextSKUId\" value=\"" + 
          FormatHelper.encodeForHTMLAttribute(contextSKUId) + "\">");
      productContextBuilder.append("</div>");
    } else {
      int totalSkus = skuTable.size() - 1;
      if (skuTable.containsKey("ALL_SKUS"))
        totalSkus = skuTable.size() - 2; 
      String label = this.appContext.getProductARev().getFlexType().getAttribute("skuName").getAttDisplay() + " (" + totalSkus + ")";
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      productContextBuilder.append(
          getDropDownListWidget(label, "contextSKUId", null, contextSKUId, null, skuTable, order, null, "switchSKU(this.options[this.selectedIndex].value);"));
      productContextBuilder.append("</div>");
    } 
  }
  
  private void renderDestinationsDropDown(StringBuilder productContextBuilder, FlexPlmWebRequestContext phv) throws WTException {
    if (phv.isViewBOMMode() || phv.isEditBOMMode()) {
      Map<String, String> destinations = this.appContext.getProductDestinationsMap();
      if (destinations.isEmpty()) {
        destinations.put(" ", this.noneAvailableLabel);
      } else {
        destinations.put(" ", this.noneSelectedLabel);
      } 
      String destinationId = " ";
      if (FormatHelper.hasContent(phv.getDestinationId()))
        destinationId = phv.getDestinationId(); 
      Vector<String> order = new Vector<>();
      order.add(0, " ");
      String destination = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.product.ProductDestination").getTypeDisplayName() + " (" + (destinations.size() - 1) + ")";
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      productContextBuilder.append(getDropDownListWidget(destination, "destinationId", null, destinationId, null, destinations, order, null, "switchDestination(this.options[this.selectedIndex].value);"));
      productContextBuilder.append("</div>");
    } 
  }
  
  private void renderSizesDropDowns(StringBuilder productContextBuilder, FlexPlmWebRequestContext phv) throws WTException {
    if ((phv.isViewBOMMode() || phv.isEditBOMMode()) && this.appContext.getProductARev() != null) {
      String sizeCategoryId = phv.getSizeCategoryId();
      Map<String, String> sizeTable = this.appContext.getProductSizeCategoryMap();
      if (sizeTable.isEmpty()) {
        sizeTable.put(" ", this.noneAvailableLabel);
      } else {
        sizeTable.put(" ", this.noneSelectedLabel);
      } 
      Vector<String> sizeOrder = new Vector<>();
      sizeOrder.add(0, " ");
      String sizeCategoryNameLabel = WTMessage.getLocalizedMessage("com.lcs.wc.resource.SizingRB", "sizeDefinition_LBL", RB.objA) + " (" + WTMessage.getLocalizedMessage("com.lcs.wc.resource.SizingRB", "sizeDefinition_LBL", RB.objA) + ")";
      productContextBuilder.append("<div class=\"f-attribute-box\">");
      productContextBuilder.append(getDropDownListWidget(sizeCategoryNameLabel, "sizeCategoryId", null, sizeCategoryId, null, sizeTable, sizeOrder, null, "switchSizeCat(this.options[this.selectedIndex].value);"));
      productContextBuilder.append("</div>");
      if (FormatHelper.hasContent(sizeCategoryId)) {
        SearchResults results = SizingQuery.findProductSizeCategoriesForProduct(this.appContext.getProductARev());
        Iterator<FlexObject> iter = results.getResults().iterator();
        FlexObject obj = null;
        String size1Label = "";
        String size2Label = "";
        String size1Values = "";
        String size2Values = "";
        while (iter.hasNext()) {
          obj = iter.next();
          if (sizeCategoryId.equalsIgnoreCase("VR:com.lcs.wc.sizing.ProductSizeCategory:" + (String)obj
              .get("PRODUCTSIZECATEGORY.BRANCHIDITERATIONINFO"))) {
            size1Label = (String)obj.get("FULLSIZERANGE.SIZE1LABEL");
            size2Label = (String)obj.get("FULLSIZERANGE.SIZE2LABEL");
            size1Values = (String)obj.get("PRODUCTSIZECATEGORY.SIZEVALUES");
            size2Values = (String)obj.get("PRODUCTSIZECATEGORY.SIZE2VALUES");
            break;
          } 
        } 
        Map<String, String> size1Table = FormatHelper.toMap(MOAHelper.getMOACollection(size1Values));
        if (size1Table.isEmpty()) {
          size1Table.put(" ", this.noneAvailableLabel);
        } else {
          size1Table.put(" ", this.noneSelectedLabel);
        } 
        size1Label = size1Label + " (" + (size1Table.size() - 1) + ")";
        Vector<String> order1 = new Vector<>();
        order1.add(0, " ");
        order1.addAll(MOAHelper.getMOACollection(size1Values));
        String size1 = phv.getSize1();
        productContextBuilder.append("<div class=\"f-attribute-box\">");
        productContextBuilder
          .append(getDropDownListWidget(size1Label, "size1", null, size1, null, size1Table, order1, null, "switchSize1(this.options[this.selectedIndex].value);"));
        productContextBuilder.append("</div>");
        if (FormatHelper.hasContent(size2Values) && FormatHelper.hasContent(size1) && size1Values
          .indexOf(size1) > -1) {
          Map<String, String> size2Table = FormatHelper.toMap(MOAHelper.getMOACollection(size2Values));
          if (size2Table.isEmpty()) {
            size2Table.put(" ", this.noneAvailableLabel);
          } else {
            size2Table.put(" ", this.noneSelectedLabel);
          } 
          size2Label = size2Label + " (" + (size2Table.size() - 1) + ")";
          Vector<String> order2 = new Vector<>();
          order2.add(0, " ");
          order2.addAll(MOAHelper.getMOACollection(size2Values));
          String size2 = phv.getSize2();
          productContextBuilder.append("<div class=\"f-attribute-box\">");
          productContextBuilder.append(
              getDropDownListWidget(size2Label, "size2", null, size2, null, size2Table, order2, null, "switchSize2(this.options[this.selectedIndex].value);"));
          productContextBuilder.append("</div>");
        } 
      } 
    } 
  }
  
  private String getDropDownListWidget(String label, String selectName, String parentName, String selectedValue, String placeHolderText, Map<String, String> table, Vector<String> order, String selectStyle, String onChange) {
    StringBuilder builder = new StringBuilder();
    builder.append(FormGenerator2.startFormElement());
    builder.append(FormGenerator2.createFormElementLabel(label, false, null));
    builder.append(FormGenerator2.createDropDownList(selectName, parentName, selectedValue, this.noneSelectedLabel, table, order, selectStyle, onChange, null, null, false, true, false, true));
    builder.append(FormGenerator2.endFormElement());
    return builder.toString();
  }
}
