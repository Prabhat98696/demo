<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.db.FlexObject,
                com.lcs.wc.classification.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.flexbom.*,
                com.lcs.wc.client.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.material.*,
                 com.lcs.wc.product.*,
                com.lcs.wc.supplier.*,
                com.lcs.wc.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                com.lcs.wc.flextype.*,
                wt.lifecycle.*,
                wt.part.*,
                wt.util.*,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="materialModel" scope="request" class="com.lcs.wc.material.LCSMaterialClientModel" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<%
//setting up which RBs to use

String allLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL", RB.objA ) ;
String lcStateLable = WTMessage.getLocalizedMessage ( RB.MAIN, "lifeCycleState_LBL",RB.objA ) ;
String checkedOutLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "checkedOut_LBL",RB.objA );
String searchResults_LBL = WTMessage.getLocalizedMessage ( RB.MAIN, "searchResults_LBL",RB.objA );
String bomSectionsAffectedLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomSectionsAffected_LBL",RB.objA );
String linksAffectedLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "linksAffected_LBL",RB.objA );
String bomsMatchingCriteriaLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomsMatchingCriteria_LBL",RB.objA );
String bomComponentsMatchingCriteriaLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomComponentsMatchingCriteria_LBL",RB.objA );
String usageByBomLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "usageByBom_LBL",RB.objA );
String usageByBomComponentsLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "usageByBomComponents_LBL",RB.objA );
String showBomsOnlyLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "showBomsOnly_LBL",RB.objA );
String runReportButton2 = WTMessage.getLocalizedMessage ( RB.MATERIAL, "runReport_Btn", RB.objA ) ;

String materialSupplierLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "materialSupplier_LBL",RB.objA ) ;
String whereUsedLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "whereUsed_LBL",RB.objA ) ;
String newMaterialLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "newMaterial_LBL",RB.objA ) ;
String newColorLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "newColor_LBL", RB.objA ) ;
String massAddToSectionLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massAddToSection_LBL", RB.objA ) ;
String materialNotCurrentlyBeingUsed = WTMessage.getLocalizedMessage ( RB.MATERIAL, "materialNotCurrentlyBeingUsed_LBL",RB.objA ) ;
String noResultsFound = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "noResultsFound_LBL",RB.objA ) ;
String productsUsingMaterialLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL, "productsUsingMaterial_LBL",RB.objA ) ;

String noApplicableDataToReplaceAlert = WTMessage.getLocalizedMessage ( RB.MAIN, "noApplicableDataToReplace_ALRT",RB.objA ) ;
String mustSelectAtleastOneRowToChangeAlert = WTMessage.getLocalizedMessage ( RB.MAIN, "mustSelectAtleastOneRowToChange_ALRT",RB.objA ) ;
String performMassChangeToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "performMassChangeToolTip",RB.objA ) ;


String massChangeAddLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massChangeAddLabel",RB.objA );
String massChangeLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massChangeLabel",RB.objA );
String massAddLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massAddLabel",RB.objA );

String massChangeMaterialMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massChangeMaterial_MSG",RB.objA );
String massChangeColorMsg = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massChangeColor_MSG",RB.objA );


String performMassChangeAddToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "performMassChangeAddToolTip",RB.objA ) ;
String performMassDeleteToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "performMassDeleteToolTip_LBL",RB.objA ) ;
String massDeleteLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massDeleteLabel",RB.objA );

String massChangeCnfrm = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massChangeCnfrm",RB.objA );
String massAddCnfrm = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massAddCnfrm",RB.objA );
String massDropCnfrm = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massDropCnfrm",RB.objA );

String runMassChangeLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "runMassChangeLabel",RB.objA );
String runMassAddLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "runMassAddLabel",RB.objA );
String bomColumnLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bomColumnLabel",RB.objA );
String ownerColumnLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "ownerColumnLabel",RB.objA );
String detailSearchBtn = WTMessage.getLocalizedMessage ( RB.WHEREUSED, "detailSearch_Btn",RB.objA ) ;
String detailSearchToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "detailSearchToolTip",RB.objA );

String bomTypeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "type_LBL", RB.objA ) ;
String productTypeLabel = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "productTypeLabel", RB.objA );
String massChangeAlertMessage = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "massChangeAlert", RB.objA );


String complexMaterialLockToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "complexMaterialLockToolTip", RB.objA );
String bOMCheckedOutToolTip = WTMessage.getLocalizedMessage ( RB.FLEXBOM, "bOMIsCheckedOut_TOOLTIP", RB.objA );

String outOfQueryLimitExceptionMsg = WTMessage.getLocalizedMessage ( RB.EXCEPTION, "outOfQueryLimitException_MSG" , RB.objA );

%>
<%!
   private static final Logger logger = LogManager.getLogger("rfa.jsp.flexbom.BOMSearchResults");
   public static final String JSPNAME = "WhereUsedPlugin";
    public static final boolean DEBUG = false;
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final boolean BOM_SEARCH_DEBUG = LCSProperties.getBoolean("flexbom.bomSearchDebug");
    public static int BOM_QUERY_LIMIT = LCSProperties.get("com.lcs.wc.flexbom.LCSFlexBOMQuery.queryLimit", 1000);
    public static final String PRIMARY_MATERIAL_GROUP = LCSProperties.get("com.lcs.wc.flexbom.PrimaryMaterialGroup","Primary Material");
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");

    boolean hasBOMEditAccess = ACLHelper.hasEditAccess(FlexTypeCache.getFlexTypeRoot("BOM"));
    String materialId = request.getParameter("materialId");
    if(FormatHelper.hasContent(materialId) && materialId.indexOf("LCSMaterial:") < 0){
            materialId = "VR:com.lcs.wc.material.LCSMaterial:" + materialId;
    }
    String supplierId = request.getParameter("supplierId");
    String colorId = request.getParameter("colorId");

    LCSMaterial material = (LCSMaterial) request.getAttribute("contextMaterial");

    if(material == null && FormatHelper.hasContent(materialId) && materialId.indexOf("LCSMaterial:") > -1){
        material = (LCSMaterial) LCSQuery.findObjectById(materialId);
    }

    if(material != null){
        materialId = FormatHelper.getObjectId((LCSMaterialMaster)material.getMaster());
    }
    LCSSupplier supplier = (LCSSupplier) request.getAttribute("contextSupplier");
    if(supplier != null && "placeholder".equals(supplier.getSupplierName())){
        supplier = null;
        supplierId = "";
    }
    if(supplier == null && FormatHelper.hasContent(supplierId) && supplierId.indexOf("LCSSupplier:") > -1){
        supplier = (LCSSupplier) LCSQuery.findObjectById(supplierId);
    } else if(supplier == null && FormatHelper.hasContent(supplierId)){
        supplier = (LCSSupplier) LCSQuery.findObjectById("VR:com.lcs.wc.supplier.LCSSupplier:" + supplierId);
    }

    if(supplier != null){
        supplierId = FormatHelper.getObjectId((LCSSupplierMaster)supplier.getMaster());
    } else {
        supplierId = "";
    }
    String ownerOption = request.getParameter("ownerOption");

    boolean bomPartsOnly = true;
    if(FormatHelper.hasContent(request.getParameter("reportType"))) {
        bomPartsOnly = FormatHelper.parseBoolean(request.getParameter("reportType"));
    }
  //SPR 2009303
    boolean outOfQueryLimit = false;
    Collection whereUsedData = new Vector();
    try{
		 whereUsedData= com.agron.wc.flexbom.AgronFlexBOMQuery.findWhereUsedData(RequestHelper.hashRequest(request, true), materialId, supplierId, colorId, BOM_QUERY_LIMIT);

		
       // whereUsedData= LCSFlexBOMQuery.findWhereUsedData(RequestHelper.hashRequest(request, true), materialId, supplierId, colorId, BOM_QUERY_LIMIT);
    }catch(OutOfQueryLimitException e){
        outOfQueryLimit=true;
        e.printStackTrace();

    }
    //logger.debug("results: " + whereUsedData);

    boolean hasPrimaryMaterialGroup = false;
    if (FormatHelper.hasContent(PRIMARY_MATERIAL_GROUP) && FlexTypeCache.getFlexTypeFromPath("BOM\\Materials").getAttributeGroup(PRIMARY_MATERIAL_GROUP, FlexBOMFlexTypeScopeDefinition.BOM_SCOPE, null).size() > 0) {
        hasPrimaryMaterialGroup = true;
    }

    final String productNameDef = "LCSPRODUCT." + FlexTypeCache.getFlexTypeRoot("Product").getAttribute("productName").getAttributeName();
	final String agronWorkNumberDef = "LCSPRODUCT." + FlexTypeCache.getFlexTypeRoot("Product").getAttribute("agrWorkOrderNoProduct").getAttributeName();
    final String materialNameDef = "LCSMATERIAL." + FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getAttributeName();
    final String bomNameDef = "FLEXBOMPART." + FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("name").getColumnName();

    Vector boms = new Vector();
    if (bomPartsOnly) {
        // Aggregate linkIds for each bomPart
        String prefix = "OR:com.lcs.wc.flexbom.FlexBOMLink:";
        Map linkMap = FlexObjectUtil.concatIndex(whereUsedData, "FlexBOMPart.idA2A2", "FlexBOMLink.idA2A2", MOAHelper.DELIM, prefix);

        // Aggregate link sections for each bomPart
        FlexTypeAttribute sectionAtt = bomType.getAttribute("section");
        prefix = "";
        Map sectionMap = FlexObjectUtil.concatIndex(whereUsedData, "FlexBOMPart.idA2A2", sectionAtt.getSearchResultIndex(), ", ", prefix, sectionAtt);

        // Filter out link level data
        ArrayList tracker = new ArrayList();
        FlexObject fo = null;
        Iterator dataIt = whereUsedData.iterator();
        while (dataIt.hasNext()) {
            fo = (FlexObject)dataIt.next();
            FlexObject fo2 = new FlexObject();
            // Capture each unique BOMPart as part of roll-up
            if (!tracker.contains(fo.get("FlexBOMPart.idA2A2"))) {
                tracker.add((String)fo.get("FlexBOMPart.idA2A2"));
                fo2.put("FlexBOMPart.idA2A2", fo.get("FlexBOMPart.idA2A2"));
                fo2.put("FlexBOMPart.statestate", fo.get("FlexBOMPart.statestate"));
                fo2.put("FlexBOMPart.idA3A10", fo.get("FlexBOMPart.idA3A10"));
                fo2.put("FlexBOMPart.statecheckoutInfo", fo.get("FlexBOMPart.statecheckoutInfo"));
                fo2.put("FlexBOMPart.BRANCHIDA2TYPEDEFINITIONREFE", fo.get("FlexBOMPart.BRANCHIDA2TYPEDEFINITIONREFE"));
                // If the ownermaster is a product or material use the name from LCSProduct or LCSMaterial.
                String productName = (String) fo.get(productNameDef);
				String agronWorkNumber = (String) fo.get("WORKNUMBER");
                String materialName = (String) fo.get(materialNameDef);
                if (productName != null && productName.length() > 0) {
                    fo2.put("OWNERMASTER.name", productName);
					if(agronWorkNumber != null ){
					 fo2.put("WORKNUMBER", agronWorkNumber);
					}
                } else if (materialName != null && materialName.length() > 0) {
                    fo2.put("OWNERMASTER.name", materialName);
                } else {
					if(agronWorkNumber != null ){
					 fo2.put("WORKNUMBER", agronWorkNumber);
					}
                    fo2.put("OWNERMASTER.name", fo.get("OWNERMASTER.name"));
                }
				
				
				
                fo2.put(bomNameDef, fo.get(bomNameDef));
                fo2.put("BOMPARTMASTER.idA2A2", fo.get("BOMPARTMASTER.idA2A2"));

                fo2.put("AllLinkIds", linkMap.get(fo.get("FlexBOMPart.idA2A2")));
                fo2.put("LinkCount", MOAHelper.getMOACollection((String)linkMap.get((String)fo.get("FlexBOMPart.idA2A2"))).size());
                fo2.put("AllSections", sectionMap.get(fo.get("FlexBOMPart.idA2A2")));

                // initialize the hide checkbox based on the first link read for this bom part
                if (FormatHelper.hasContent((String)fo.get("FLEXBOMLINK.MASTERBRANCH")) || FormatHelper.hasContent((String)fo.get("FLEXBOMLINK.MASTERBRANCHID"))) {
                    fo2.put("HideCheckboxComplexMaterial", "true");
                } else {
                    fo2.put("HideCheckboxComplexMaterial", "false");
                }

                boms.add(fo2);

            } else { //  if any bomlink for a single BOMPart is part of a copied complex material then set the hide checkbox true
                Iterator bit = boms.iterator();
                while (bit.hasNext()) {
                    FlexObject fo3 = (FlexObject)bit.next();
                    if (((String)fo3.get("FlexBOMPart.idA2A2")).equals(fo.get("FlexBOMPart.idA2A2"))) {
                        if (FormatHelper.hasContent((String)fo.get("FLEXBOMLINK.MASTERBRANCH")) || FormatHelper.hasContent((String)fo.get("FLEXBOMLINK.MASTERBRANCHID"))) {
                            fo3.put("HideCheckboxComplexMaterial", "true");
                            continue;
                        }
                    }
                }
            }
        }
    } else {
        // show all links mode, if any bomlink is part of a copied complex material then set the hide checkbox true
        FlexObject fo = null;
        Iterator dataIt = whereUsedData.iterator();
        while (dataIt.hasNext()) {
            fo = (FlexObject)dataIt.next();
            if (FormatHelper.hasContent((String)fo.get("FLEXBOMLINK.MASTERBRANCH")) || FormatHelper.hasContent((String)fo.get("FLEXBOMLINK.MASTERBRANCHID"))) {
                fo.put("HideCheckboxComplexMaterial", "true");
            } else {
                fo.put("HideCheckboxComplexMaterial", "false");
            }
        }
    }

    TableColumn column;
    TableColumn orColumn;
    Collection columns = new ArrayList();
    Map columnMap = new HashMap();


    columnMap.put("BRANCHID", new TableColumn("FLEXBOMLINK.BRANCHID", "Branch"));
    columnMap.put("FLEXBOMLINK.IDA2A2", new TableColumn("FLEXBOMLINK.IDA2A2", "LINK.IDA2A2"));
    columnMap.put("FLEXBOMLINK.DIMENSIONID", new TableColumn("FLEXBOMLINK.DIMENSIONID", "LINK.DIMENSIONID"));

    CheckBoxTableFormElement fe = new CheckBoxTableFormElement();
    if (bomPartsOnly) {
        fe.setValueIndex("ALLLINKIDS");
        fe.setValuePrefix("");
        fe.setIdIndex("FLEXBOMPART.IDA2A2");
        fe.setIdPrefix("OR:com.lcs.wc.flexbom.FlexBOMPart:");
    } else {
        fe.setValueIndex("FLEXBOMLINK.IDA2A2");
        fe.setValuePrefix("OR:com.lcs.wc.flexbom.FlexBOMLink:");
    }
    fe.setName("selectedIds");

    orColumn = new BOMMassChangeSelectTableColumn();
    orColumn.setDisplayed(true);
    orColumn.setShowCriteria("c/i");
    orColumn.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel));
    orColumn.setFormat("UNFORMATTED_HTML");
    orColumn.setHeaderAlign("left");
    orColumn.setTableIndex("");
    orColumn.setColumnWidth("1%");
    orColumn.setFormElement(fe); // "fe" FROM ABOVE
    orColumn.setShowCriteria("true");
    orColumn.setShowCriteriaNot(true);
    orColumn.setShowCriteriaTarget("HIDECHECKBOXCOMPLEXMATERIAL");

    column = new BOMMassChangeSelectTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel));
    column.setFormat("UNFORMATTED_HTML");
    column.setHeaderAlign("left");
    column.setTableIndex("");
    column.setColumnWidth("1%");
    column.setFormElement(fe); // "fe" FROM ABOVE
    column.setShowCriteria("c/i");
    column.setShowCriteriaTarget("FLEXBOMPART.STATECHECKOUTINFO");
    column.addOverrideOption("HIDECHECKBOXCOMPLEXMATERIAL", "true", orColumn);
    column.setShowCriteriaOverride(true);
    columnMap.put("SELECTOR", column);


    if(BOM_SEARCH_DEBUG){
        column = new TableColumn();
        column.setDisplayed(true);
        if (bomPartsOnly) {
            column.setTableIndex("ALLLINKIDS");
        } else {
            column.setTableIndex("FLEXBOMLINK.IDA2A2");
        }
        column.setHeaderLabel("LINKSTUFF");
        columnMap.put("LINKSTUFF", column);
    }

    column = new TableColumn();
    column.setDisplayed(true);
    column.setTableIndex("OWNERMASTER.NAME");
    column.setLinkMethod("viewBOM");
    column.setLinkTableIndex("FLEXBOMPART.IDA2A2");
    column.setLinkMethodPrefix("OR:com.lcs.wc.flexbom.FlexBOMPart:");
    column.setUseQuickInfo(true);
    column.setHeaderLabel(ownerColumnLabel);
    columnMap.put("OWNERMASTER.NAME", column);


 column = new TableColumn();
    column.setDisplayed(true);
    column.setTableIndex("WORKNUMBER");
   // column.setLinkMethod();
   // column.setLinkTableIndex();
   // column.setLinkMethodPrefix();
    column.setUseQuickInfo(false);
    column.setHeaderLabel("Work #");
    columnMap.put("WORKNUMBER", column);
	
	
    column = new TableColumn();
    column.setDisplayed(true);
    column.setTableIndex(bomNameDef);
    //column.setLinkMethod("viewPart");
    column.setLinkTableIndex("BOMPARTMASTER.IDA2A2");
    column.setLinkMethodPrefix("OR:com.lcs.wc.flexbom.FlexBOMPartMaster:");
    column.setUseQuickInfo(true);
    column.setHeaderLabel(bomColumnLabel);
    columnMap.put("BOMPARTMASTER.NAME", column);


    column = new LifecycleStateTableColumn();
    column.setDisplayed(true);
    column.setTableIndex("FLEXBOMPART.STATESTATE");
    column.setHeaderLabel(lcStateLable);
    columnMap.put("FLEXBOMPART.STATESTATE", column);


    column = new TableColumn();
    column.setDisplayed(true);
    column.setTableIndex("MATERIALMASTER.NAME");
//    column.setLinkMethod("viewSupplier");

    if(FormatHelper.hasContent(materialId)){
        column.setLinkTableIndex("LCSSUPPLIERMASTER.IDA2A2");
    }
//    column.setLinkMethodPrefix("OR:com.lcs.wc.supplier.LCSSupplierMaster:");
    column.setHeaderLabel("Material");
    columnMap.put("MATERIALMASTER.NAME", column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setTableIndex("LCSSUPPLIERMASTER.SUPPLIERNAME");
    column.setLinkMethod("viewSupplier");
    column.setLinkTableIndex("LCSSUPPLIERMASTER.IDA2A2");
    column.setLinkMethodPrefix("OR:com.lcs.wc.supplier.LCSSupplierMaster:");
    column.setHeaderLabel(materialSupplierLabel);
    columnMap.put("MATERIALSUPPLIER.NAME", column);

    TableColumn orColumn1 = new TableColumn();
    orColumn1.setHeaderLabel("");
    orColumn1.setDisplayed(true);
    orColumn1.setShowCriteria("true");
    orColumn1.setShowCriteriaTarget("HIDECHECKBOXCOMPLEXMATERIAL");
    orColumn1.setConstantDisplay(true);
    orColumn1.setConstantValue("<img src='" + URL_CONTEXT + "/images/lockedSubAssy.gif' border='0'>");

    orColumn1.setFormatHTML(false);

    column = new TableColumn();
    column.setHeaderLabel("");
    column.setDisplayed(true);
    column.setShowCriteria("c/o");
    column.setShowCriteriaNot(false);
    column.setShowCriteriaTarget("FLEXBOMPART.STATECHECKOUTINFO");
    column.setConstantDisplay(true);
    column.setConstantValue("<img src='" + URL_CONTEXT + "/images/lock.gif' title='" + FormatHelper.formatHTMLString(bOMCheckedOutToolTip) + "' border='0'>");
    column.setFormatHTML(false);
    column.addOverrideOption("HIDECHECKBOXCOMPLEXMATERIAL", "true", orColumn1);
    column.setShowCriteriaOverride(true);
    columnMap.put("FLEXBOMPART.STATECHECKOUTINFO", column);


    column = new FlexTypeDescriptorTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(productTypeLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("LCSPRODUCT.BRANCHIDA2TYPEDEFINITIONREFE");
    column.setHeaderLink("javascript:resort('LCSProduct.branchIdA2typeDefinitionRefe')");
    columnMap.put("product.typename", column);

    column = new FlexTypeDescriptorTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(bomTypeLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("FlexBOMPart.BRANCHIDA2TYPEDEFINITIONREFE");
    column.setHeaderLink("javascript:resort('FlexBOMPart.branchIdA2typeDefinitionRefe')");
    columnMap.put("bom.typename", column);

    if (bomPartsOnly) {
        column = new TableColumn();
        column.setDisplayed(true);
        column.setTableIndex("ALLSECTIONS");
        column.setHeaderLabel(bomSectionsAffectedLabel);
        columnMap.put("bom.sections", column);

        column = new TableColumn();
        column.setDisplayed(true);
        column.setTableIndex("LINKCOUNT");
        column.setHeaderLabel(linksAffectedLabel);
        columnMap.put("bom.linkcount", column);
    }

    flexg.setScope(com.lcs.wc.flexbom.FlexBOMFlexTypeScopeDefinition.LINK_SCOPE);
    columnMap = flexg.createTableColumns(bomType, columnMap, bomType.getAllAttributes(FlexBOMFlexTypeScopeDefinition.LINK_SCOPE, null), false, "FLEXBOMLINK.");


    Collection columnList = new ArrayList();
    columnList.add("SELECTOR");
    columnList.add("FLEXBOMPART.STATECHECKOUTINFO");
    columnList.add("OWNERMASTER.NAME");
	columnList.add("WORKNUMBER");
    columnList.add("bom.typename");

    if("PRODUCT".equals(ownerOption)){
        columnList.add("product.typename");
    }
    columnList.add("BOMPARTMASTER.NAME");

    if (!bomPartsOnly) {
        columnList.add("FLEXBOMLINK.section");
        columnList.add("FLEXBOMLINK.partName");
        //    columnList.add("MATERIALMASTER.NAME");
        //    columnList.add("MATERIALSUPPLIER.NAME");
        if(BOM_SEARCH_DEBUG){
            columnList.add("LINKSTUFF");
            columnList.add("FLEXBOMLINK.DIMENSIONID");
            columnList.add("BRANCHID");
            columnList.add("FLEXBOMLINK.IDA2A2");
        }
        columnList.add("FLEXBOMLINK.quantity");
    } else {
        columnList.add("bom.sections");
        columnList.add("bom.linkcount");
        if(BOM_SEARCH_DEBUG){
            columnList.add("LINKSTUFF");
        }
    }

    columnList.add("FLEXBOMPART.STATESTATE");



    TableColumn potentialColumn;
    Iterator it = columnList.iterator();
    String key = "";
    while(it.hasNext()){
        key = (String)it.next();
        potentialColumn = (com.lcs.wc.client.web.TableColumn) columnMap.get(key);
        if(potentialColumn != null){
            columns.add(potentialColumn);
            columnMap.remove(key);
            if(key.indexOf(".") > 0){
                key = key.substring(key.indexOf(".") + 1, key.length());
            }
            //attList.add(key);
        }
    }

%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
    var hasPrimaryMaterialGroup = <%=hasPrimaryMaterialGroup%>;
   function runMassEdit(mode){

      <% if(whereUsedData.size() < 1) { %>
            alert('<%= FormatHelper.encodeForJavascript(noApplicableDataToReplaceAlert)%>');
            return;
      <% } %>


      if("DROP" == mode || "CHANGE" == mode || "ADD" == mode) {

         var ids = '';


         var checked = false;
         if(document.MAINFORM.selectedIds){ // is there at least one checkbox on the page?
            if(document.MAINFORM.selectedIds.length){ // is there more than one
                // GROUP THE SELECED IDS INTO A MOA STRING
                var group = document.MAINFORM.selectedIds;
                for (var k =0; k < group.length; k++)  {
                    if (group[k].checked)   {
                        ids = buildMOA(ids, group[k].value);
                        checked = true;
                    }
                }
            } else { // there must be only one checkbox on the page

                // SPECIAL HANDLING FOR WHEN THERE IS ONLY ONE ROW.
                if(document.MAINFORM.selectedIds.checked){
                    ids = document.MAINFORM.selectedIds.value;
                    checked = true;
                }

            }
         }

         if(!checked){
            alert('<%= FormatHelper.encodeForJavascript(mustSelectAtleastOneRowToChangeAlert)%>');
            return;
         }

         // Message Handle
         if (!hasPrimaryMaterialGroup) {
            massChangeConfirmMSG = '<%= FormatHelper.encodeForJavascript(massChangeCnfrm)%>';
            massDropConfirmMSG = '<%= FormatHelper.encodeForJavascript(massDropCnfrm)%>';
            massAddConfirmMSG    = '<%= FormatHelper.encodeForJavascript(massAddCnfrm)%>';
        }

         if(mode == "CHANGE" && massChangeValidate() && confirm(massChangeConfirmMSG)){
            document.MAINFORM.activity.value = 'BOM_MASS_CHANGE';
            document.MAINFORM.action.value = 'RUN';
            document.MAINFORM.oids.value = ids;
            document.MAINFORM.returnActivity.value = document.REQUEST.activity;
            document.MAINFORM.returnAction.value = document.REQUEST.action;
            document.MAINFORM.returnOid.value = document.REQUEST.oid;
            submitForm();
         }

         if(mode == "DROP" && confirm(massDropConfirmMSG)){
            document.MAINFORM.activity.value = 'BOM_MASS_DROP';
            document.MAINFORM.action.value = 'RUN';
            document.MAINFORM.oids.value = ids;
            document.MAINFORM.returnActivity.value = document.REQUEST.activity;
            document.MAINFORM.returnAction.value = document.REQUEST.action;
            document.MAINFORM.returnOid.value = document.REQUEST.oid;
            submitForm();
         }

         if(mode == "ADD" && confirm(massAddConfirmMSG)){

            <%
                String sectionAttName = bomType.getAttribute("section").getAttributeName();
                String sectionLabel = bomType.getAttribute("section").getAttDisplay();
            %>
            if(!document.MAINFORM.<%= sectionAttName%>){
                alert('<%= LCSMessage.getJavascriptMessage(RB.FLEXBOM, "sectionAttributeIsHidden_MSG", RB.objA, false)%>');
                return;
            }
            if(valRequiredSingleSelect(document.MAINFORM.<%= sectionAttName %>, '<%= FormatHelper.encodeForJavascript(sectionLabel) %>', true)){

                document.MAINFORM.activity.value = 'BOM_MASS_ADD';
                document.MAINFORM.action.value = 'RUN';
                document.MAINFORM.oids.value = ids;
                document.MAINFORM.returnActivity.value = document.REQUEST.activity;
                document.MAINFORM.returnAction.value = document.REQUEST.action;
                document.MAINFORM.returnOid.value = document.REQUEST.oid;
                submitForm();
            }
         }


      }
   }

    function toggleAllItems() {
        var selectAllCheckbox = document.getElementById('selectAllCheckBox');

        var checkboxes = document.getElementsByName('selectedIds');
        for (i=0; i<checkboxes.length; i++) {

            if (selectAllCheckbox.checked) {
                checkboxes[i].checked = true;
            } else {
                checkboxes[i].checked = false;
            }
        }
    }



   function massChangeValidate(){

      <% if (!FormatHelper.hasContent(materialId) && !FormatHelper.hasContent(colorId)) { %>
            if( !valMassChangeCriteria() ) { return false; }
      <% } else if (FormatHelper.hasContent(materialId) && !FormatHelper.hasContent(colorId)) { %>
      if( !valRequiredChooser(document.MAINFORM.newMCMaterialSupplierId, "<%= FormatHelper.encodeForJavascript(newMaterialLabel) %>")) { return false; }
      <% } else if (!FormatHelper.hasContent(materialId) && FormatHelper.hasContent(colorId)) { %>
      if( !valRequiredChooser(document.MAINFORM.newMCColorId, "<%= FormatHelper.encodeForJavascript(newColorLabel) %>") ) { return false; }
      <% } %>

      return true;
   }

    // prove that either a new material or a new color has been selected or both
    function valMassChangeCriteria(){
        var formElements = document.getElementById("massChangeDiv").getElementsByTagName("input");

        for(var i=0;i<formElements.length;i++) {
            if(formElements[i].type=="hidden"||formElements[i].type=="text") {
                if(formElements[i].id == "newMCMaterialSupplierId" || formElements[i].id == "newMCColorId" ) {
                    if(formElements[i].value!="0" && trim(formElements[i].value)!="") {
                        return true;
                    }
                }
            }
        }
        alert('<%=FormatHelper.encodeForJavascript(massChangeAlertMessage)%>');
        return false;
    }

   // Override the default "choose" function so that we can strip the
   // placeholder tag when a named material/supplier placeholder is returned
   function choose(value, display, detail){
      // Trim off the - placeholder tag for a Material/Supplier placeholder link
      display = display.replace(/ - placeholder/, "");

      inputDisplay.innerHTML = display;
      inputValue.value = value;
      inputDisplayValue = display;
      chooserWindow.close();

      // SET DETAIL OBJECT
      chosenDetail = detail;

      if(chooserListener){
          handleChooseEvent();
      }
   }

   function runDetailSearch(){
        document.MAINFORM.activity.value = "BOM_SEARCH";
        submitForm();
   }

   function viewBOM(bpId){
        document.MAINFORM.action.value = '';
        document.MAINFORM.activity.value = 'VIEW_BOM';
        document.MAINFORM.oid.value = bpId;
        submitForm();
   }

    function manageDivs(divid) {
        var mcd = document.getElementById('massChangeDiv');
        var mad = document.getElementById('massAddDiv');
        if (divid == 'massChangeDiv') {
            if(mcd.style.display == 'none'){
                mcd.style.display = 'block';
                mad.style.display = 'none';
            } else {
                mcd.style.display = 'none';
            }
        } else {
            if(mad.style.display == 'none'){
                mad.style.display = 'block';
                mcd.style.display = 'none';
            } else {
                mad.style.display = 'none';
            }
        }
    }

    function changeTab(tabId){
         if(tabId == 'BOM_REF_TAB') {
            if('<%=FormatHelper.encodeForJavascript(request.getParameter("reportType"))%>' == 'null'){
                 setMainFormValue('reportType','true');
             }
         }
         submitForm();
     }
</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<!-- BEGIN form search criteria ....
     These hidden inputs hold the search criteria that returned these results
     if the user clicks on Modify Search button, these inputs will be sent back to the
     search criteria form to populate it with the users last search.
-->

<!-- BOM search criteria -->
    <input type="hidden" name="materialId" id="materialId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("materialId")) %>" >
    <input type="hidden" name="supplierId" id="supplierId"  value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("supplierId")) %>">
    <input type="hidden" name="colorId" id="colorId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("colorId")) %>">
    <%String bomNameAttValue = FormatHelper.format(request.getParameter(bomType.getAttribute("name").getSearchCriteriaIndex())); %>
     <input type="hidden" name="<%= bomType.getAttribute("name").getSearchCriteriaIndex()%>" value="<%= FormatHelper.encodeAndFormatForHTMLContent(bomNameAttValue) %>">
    <input type="hidden" name="bomFlexTypeId" id="bomFlexTypeId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("bomFlexTypeId")) %>">
<%
    flexg.setScope(com.lcs.wc.flexbom.FlexBOMFlexTypeScopeDefinition.BOM_SCOPE);
    flexg.setLevel(null);
    out.print( flexg.generateSearchCriteriaPlaceholders(bomType, request)) ;

    // ADD COMPONENT OR ANY OTHER TOP LEVEL BOM LINK ATTRIBUTE WE WANT TO SEARCH BY
    String partNameAttValue = FormatHelper.format(request.getParameter(bomType.getAttribute("partName").getSearchCriteriaIndex()));
%>
    <input type="hidden" name="<%= bomType.getAttribute("partName").getSearchCriteriaIndex() %>" value="<%= FormatHelper.encodeAndFormatForHTMLContent( partNameAttValue) %>">

<!-- OWNER search criteria -->
    <input type="hidden" name="ownerOption" id="ownerOption" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("ownerOption")) %>" >
    <input type="hidden" name="ownerFlexTypeId" id="ownerFlexTypeId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("ownerFlexTypeId")) %>">
    <input type="hidden" name="bomSeasonId" id="bomSeasonId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("bomSeasonId")) %>">
<%
    FlexType selectedType = FlexTypeCache.getFlexType(request.getParameter("ownerFlexTypeId"));
    if(selectedType == null){
        selectedType = FlexTypeCache.getFlexTypeFromPath("Product");
    }
    flexg.setScope(com.lcs.wc.flextype.FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
    flexg.setLevel(com.lcs.wc.flextype.FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);

    String productNameAttValue = FormatHelper.format(request.getParameter(selectedType.getAttribute("productName").getSearchCriteriaIndex()));

    out.print(flexg.generateSearchCriteriaPlaceholders(selectedType, request));

    %>
<!-- SPEC search criteria -->
    <%
    if("PRODUCT".equals(ownerOption)){

        flexg.setScope(null);
        flexg.setLevel(null);


        FlexType productType = null;
        if(FormatHelper.hasContent(request.getParameter("ownerFlexTypeId"))){
            productType = FlexTypeCache.getFlexType(request.getParameter("ownerFlexTypeId"));
        } else {
            productType = FlexTypeCache.getFlexTypeFromPath("Product");
        }
        FlexType specType = productType.getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
        flexg.setScope(null);
        flexg.setLevel(null);

        out.print(flexg.generateSearchCriteriaPlaceholders(specType, request));
    }
    %>
<!-- END form search criteria .... --->



<% if(FormatHelper.parseBoolean(request.getParameter("whereUsedPlugin"))){ %>
    <%-- shouldn't be needed now that criteria placeholders are being used above
        <input name="colorId" type="hidden" value="<%= colorId %>">
        <input name="materialId" type="hidden" value="<%= request.getParameter("materialId") %>">
        <%  if(supplier != null){ %>
    <input name="supplierId" id="supplierId" type="hidden" value="<%= FormatHelper.getNumericVersionIdFromObject(supplier) %>">
        <% } %>
    --%>

    <input name="reportType" id="reportType" type="hidden" value="<%= bomPartsOnly %>">

    <script>
        function setReportType() {
            var cbox = document.getElementById('reportTypeChkBox');

            if (cbox && cbox.checked ) {
                document.MAINFORM.reportType.value = 'true';
            } else {
                document.MAINFORM.reportType.value = 'false';
            }
        }
    </script>


<% } %>
<table width="95%" cellspacing="0" cellpadding="0" align="center">
    <% if(whereUsedData.size() > 0) { %>
    <tr>
        <td>
            <table width="100%">

                <tr>
                    <td class="button" align="left">
                        <%if(hasBOMEditAccess){ %>
                            <a onmouseover="return overlib('<%= FormatHelper.encodeForJavascript(performMassChangeToolTip) %>');" onmouseout="return nd();" class="button" href="javascript:manageDivs('massChangeDiv')"><%= massChangeLabel %></a>&nbsp;&nbsp;&nbsp;
                            <a onmouseover="return overlib('<%= FormatHelper.encodeForJavascript(performMassChangeAddToolTip) %>');" onmouseout="return nd();" class="button" href="javascript:manageDivs('massAddDiv')"><%= massAddLabel %></a>&nbsp;&nbsp;&nbsp;
                            <a onmouseover="return overlib('<%= FormatHelper.encodeForJavascript(performMassDeleteToolTip) %>');" onmouseout="return nd();" class="button" href="javascript:runMassEdit('DROP')"><%= massDeleteLabel %></a>
                        <% }%>
                    </td>
                    <% if(!"BOM_SEARCH".equals(request.getParameter("activity"))){ %>
                        <td class="button" align="left">
                            <a onmouseover="return overlib('<%= FormatHelper.encodeForJavascript(detailSearchToolTip) %>');" onmouseout="return nd();") class="button" href="javascript:runDetailSearch()"><%= detailSearchBtn %></a>
                        </td>
                        <td class="button" align="right">
                            <!-- // Add the Run Report button to the Where Used BOM Tab for use with Show BOMS Only -->
                            <% if (bomPartsOnly) { %>
                                <input type="checkbox" id="reportTypeChkBox" onClick="setReportType();" checked><%= showBomsOnlyLabel %>
                            <% } else { %>
                                <input type="checkbox" id="reportTypeChkBox" onclick="setReportType();"><%= showBomsOnlyLabel %>
                            <% } %>
                            <a class="button" href="javascript:changeTab('BOM_REF_TAB')"><%= runReportButton2 %></a>
                        </td>
                    <% } %>
                </tr>

            </table>
        </td>
    </tr>
    <tr>
        <td>
        <%if(hasBOMEditAccess) {%>
            <div id="massChangeDiv">
                <table width="100%" cellspacing="0" cellpadding="0">
                    <tr>
                        <td>
                            <%-- --%>
                            <%-- --%>
                            <%-- Start MASS Change Content --%>
                            <%-- --%>
                            <%-- --%>
                            <%= tg.startGroupBorder() %>
                            <%= tg.startTable() %>
                            <%= tg.startGroupTitle() %><%= massChangeLabel %><%= tg.endTitle() %>
                            <%= tg.startGroupContentTable() %>
                            <col width="15%">
                            <col width="35%">
                            <col width="15%">
                            <col width="35%">
                            <%-- Currently Mass Change is only used to update Material and Color attributes, no others --%>
                            <%-- If both material and color criteria are specified as filters, then disable the Mass Change function --%>
                            <% if (FormatHelper.hasContent(materialId) && FormatHelper.hasContent(colorId)) { %>
                                <tr>
                                        <td colspan="2"><img src="<%=WT_IMAGE_LOCATION%>/warning_16x16.gif"/>&nbsp;&nbsp;&nbsp;<%= LCSMessage.getJavascriptMessage(RB.FLEXBOM, "massChangeNotWhenBothMaterialandColor_MSG", RB.objA) %> </td>
                                </tr>
                                <tr>
                                    <td class="button" colspan="4">
                                        <a class="button" onmouseover="return overlib('<%= LCSMessage.getJavascriptMessage(RB.FLEXBOM, "massChangeNotWhenBothMaterialandColor_MSG", RB.objA)%>');" onmouseout="return nd();") disabled="true"><%= runMassChangeLabel %></a>
                                    </td>
                                </tr>

                            <%-- If neither material nor color are specified as filters, then the Mass Change will only affect top level BOM rows, not overrides --%>
                            <% } else if (!FormatHelper.hasContent(materialId) && !FormatHelper.hasContent(colorId)) { %>
                                <tr>
                                    <%= fg.createChooserWidget("newMCMaterialSupplierId", newMaterialLabel, "", "", "MATERIALSUPPLIER", "master", false) %>
                                </tr>
                                <tr>
                                    <%= fg.createChooserWidget("newMCColorId", newColorLabel, "", "", "COLOR", "object", false) %>
                                </tr>
                                <tr>
                                    <td class="button" colspan="4">
                                        <a class="button" href="javascript:runMassEdit('CHANGE')"><%= runMassChangeLabel %></a>
                                    </td>
                                </tr>

                            <%-- One and Only one criteria may be specified between Material and Color because of the way Override records are processed --%>
                            <% } else { %>
                                <%-- If material criteria is specified, then color will be ignored --%>
                                <tr>
                                    <% if(FormatHelper.hasContent(materialId) ) { %>
                                        <%= fg.createChooserWidget("newMCMaterialSupplierId", newMaterialLabel, "", "", "MATERIALSUPPLIER", "master", false) %>
                                    <% } else { %>
                                        <td colspan="2"> <%= massChangeMaterialMsg %> </td>
                                    <% } %>
                                </tr>
                                <tr>
                                    <%-- If material criteria is not specified, and color criteria is specified then color will be processed --%>
                                    <% if(!FormatHelper.hasContent(materialId) ) { %>
                                        <% if (FormatHelper.hasContent(colorId)) { %>
                                            <%= fg.createChooserWidget("newMCColorId", newColorLabel, "", "", "COLOR", "object", false) %>
                                        <% } else { %>
                                            <td colspan="2"> <%= massChangeColorMsg %>  </td>
                                        <% } %>
                                    <% } else { %>
                                        <td colspan="2"> <%= massChangeColorMsg %> </td>
                                    <% } %>
                                </tr>
                                <tr>
                                    <td class="button" colspan="4">
                                        <a class="button" href="javascript:runMassEdit('CHANGE')"><%= runMassChangeLabel %></a>
                                    </td>
                                </tr>
                            <% } %>
                            <%= tg.endContentTable() %>
                            <%= tg.endTable() %>
                            <%= tg.endBorder() %>
                            <%-- --%>
                            <%-- --%>
                            <%-- Start MASS ADD Content --%>
                            <%-- --%>
                            <%-- --%>
                    </td>
                </tr>
            </table>
        </div>
        <div id="massAddDiv">
            <table width="100%" cellspacing="0" cellpadding="0">
                <tr>
                    <td>
                        <%= tg.startGroupBorder() %>
                        <%= tg.startTable() %>
                        <%= tg.startGroupTitle() %><%= massAddLabel %><%= tg.endTitle() %>
                        <%= tg.startGroupContentTable() %>
                        <col width="15%">
                        <col width="35%">
                        <col width="15%">
                        <col width="35%">
                        <tr>
                            <%= fg.createChooserWidget("newMAMaterialSupplierId", newMaterialLabel, "", "", "MATERIALSUPPLIER", "master", false) %>
                        </tr>
                        <tr>
                            <%= fg.createChooserWidget("newMAColorId", newColorLabel, "", "", "COLOR", "object", false) %>
                        </tr>
                        <tr>
                            <td class="button" colspan="4">
                                <a class="button" href="javascript:runMassEdit('ADD')"><%= runMassAddLabel %></a>
                            </td>
                        </tr>
                        <%= tg.endContentTable() %>
                        <%= tg.endTable() %>
                        <%= tg.endBorder() %>
                    </td>
                </tr>
                <tr>
                    <td>
                        <%
                            FlexBOMLink link = new FlexBOMLink();
                            String bomFlexTypeId = request.getParameter("bomFlexTypeId");
                            if(FormatHelper.hasContent(bomFlexTypeId)){
                                link.setFlexType(FlexTypeCache.getFlexType(bomFlexTypeId));
                            } else {
                                link.setFlexType(FlexTypeCache.getFlexTypeFromPath("BOM\\Materials"));
                            }
                            flexg.setScope(com.lcs.wc.flexbom.FlexBOMFlexTypeScopeDefinition.LINK_SCOPE);
                            flexg.setLevel(null);
                            flexg.setCreate(true);
                        %>
                        <%= flexg.generateForm(link) %>
                    </td>
                </tr>
            </table>
        </div>
        <%}%>
        </td>
    </tr>
    <tr>
        <td colspan="3" class="HEADING3" nowrap>
            <% if("BOM_SEARCH".equals(request.getParameter("activity"))){ %>
                <% if(bomPartsOnly){ %>
                    <%= bomsMatchingCriteriaLabel %>
                <% } else { %>
                    <%= bomComponentsMatchingCriteriaLabel %>
                <% } %>
            <% } else { // WHEREUSED is activity %>
                <% if(bomPartsOnly){ %>
                    <%= usageByBomLabel %>
                <% } else { %>
                    <%= usageByBomComponentsLabel %>
                <% } %>
            <% } %>
        </td>
    </tr>
    <tr>
       <td class="HEADING1" align="left">
               <%
               Iterator ci = columns.iterator();
               while (ci.hasNext()) {
                   TableColumn tc = (TableColumn)ci.next();
                   tc.setColumnClassIndex(bomType.getAttribute("highLight").getSearchResultIndex());
               }
               %>
        <% tg.setClientSort(true); %>
        <% if (bomPartsOnly) { %>
            <%= tg.drawTable(boms, columns, null, true) %>
        <% } else { %>
            <%= tg.drawTable(whereUsedData, columns, null, true) %>
        <% } %>
       </td>
    </tr>
    <script>
        <%if(hasBOMEditAccess){ %>

            toggleDiv('massChangeDiv');
            toggleDiv('massAddDiv');
        <% } %>

    </script>
    <% } else {
        //SPR 2009303
      if(outOfQueryLimit){%>
          <% if(!"BOM_SEARCH".equals(request.getParameter("activity"))){ %>
          <tr>
               <td class="button" align="left">
                  <a onmouseover="return overlib('<%= FormatHelper.encodeForJavascript(detailSearchToolTip) %>');" onmouseout="return nd();" class="button" href="javascript:runDetailSearch()"><%= detailSearchBtn %></a>
               </td>
          </tr>
          <% } %>
      <tr>
        <td class="HEADING1" align="center"><%=outOfQueryLimitExceptionMsg%></td>
    </tr>
    <%}else{%>
    <tr>
        <td class="HEADING1" align="center"><%= noResultsFound %></td>
    </tr>
    <% }} %>

</table>
