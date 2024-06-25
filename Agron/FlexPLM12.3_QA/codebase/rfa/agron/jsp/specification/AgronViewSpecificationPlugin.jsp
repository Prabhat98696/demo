<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,
                com.lcs.wc.client.web.*,
                com.lcs.wc.client.web.html.*,
                com.lcs.wc.client.web.html.JavascriptFunctionCall.*,
                com.lcs.wc.client.web.html.header.action.*,
                com.lcs.wc.epmstruct.FlexEPMDocToSpecLink,
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.season.*,
                com.lcs.wc.part.*,
                com.lcs.wc.product.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.partstruct.FlexPartToSpecLink,
                com.lcs.wc.foundation.LCSQuery,
                wt.doc.WTDocumentMaster,
                wt.enterprise.RevisionControlled,
                wt.ownership.*,
                wt.locks.LockHelper,
                wt.org.*,
                wt.fc.WTObject,
                wt.part.*,
                wt.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                java.text.*,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="type" scope="request" class="com.lcs.wc.flextype.FlexType" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
private static final Logger logger = LogManager.getLogger("rfa.jsp.specification.specification.ViewSpecificationPlugin");
private static final String IMG_LOCATION = LCSProperties.get("flexPLM.urlContext.override") + "/images";
%>
<%
//setting up which RBs to use

//String flexSpecDetailsPageHead = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "flexSpecDetails_PG_HEAD", RB.objA ) ;
String actionsPageHead = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "actions_PG_HEAD", RB.objA ) ;
String actionsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "actions_LBL",RB.objA ) ;
String oldIteration = WTMessage.getLocalizedMessage ( RB.MAIN, "oldIteration_LBL", RB.objA ) ;
String iterationHistoryoption = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "iterationHistory_OPT", RB.objA ) ;
//String nameLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "name_LBL", RB.objA ) ;
//String typeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "type_LBL", RB.objA ) ;
//String flexSpecIdentificationGrpTitle = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "flexSpecIdentification_GRP_TLE", RB.objA ) ;

String updateSpecLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "editSpec_LBL", RB.objA ) ;//"Update Spec";
String deleteSpecLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "deleteSpec_LBL", RB.objA ) ;//"Delete Spec";
String removeSpecLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "removeSpec_LBL", RB.objA ) ;//"Remove Spec";
String addExistingComponentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "addExisistingComponent_LBL", RB.objA ) ;//"Add Existing Components";
String removeComponentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "removeComponents_LBL", RB.objA ) ;//"Remove Components";
String addExistingEPMDocumentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "addExistingEPMDocuments_LBL", RB.objA ) ;//"Add Existing CAD Documents";
String addExistingPartsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "addExistingParts_LBL", RB.objA ) ;//"Add Existing Parts";
//String componentsTableHeader = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "components_TBL_HDR", RB.objA ) ;//"Components";
String nameColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "component_LBL", RB.objA ) ;//"Component" ;
String statusColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "componentStatus_LBL", RB.objA ) ;//"Status";
String componentTypeColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "componentType_LBL", RB.objA ) ;//"Component Type";
String generatePDFOption = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "generatePDF_LBL", RB.objA ) ;//"Generate PDF";
String copySpecToolTip = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "copySpecToolTip_LBL", RB.objA ) ;//"Copy Components to Clipboard";
String detailsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "details_GRP_LBL", RB.objA ) ;//"Details";
String componentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "components_GRP_LBL", RB.objA ) ;//"Components";
String docsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "associatedRefDocuments_LBL", RB.objA ) ;//"Documents";
String descByDocsLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "describedByDocuments_LBL", RB.objA ) ;//"Documents";
String epmDocsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "EPMDocumentsLabel_LBL", RB.objA ) ;//"CAD Documents";
String partsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "PartsLabel_LBL", RB.objA ) ;//"Parts";
String metaLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "metaData__GRP_LBL", RB.objA ) ;//"System";
String seasonReportLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "associatedSeasons_TBL_HDR", RB.objA ) ;//"Associated Seasons";
String allLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL", RB.objA ) ;

String isPrimaryComponent_LBL = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "isPrimaryComponent_LBL", RB.objA ) ;//"is Primary Component";
//String setBOMAsPrimaryActionLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "setBOMAsPrimary_LBL", RB.objA ) ;//"setBomAsPrimary";
//String removeComponentActionLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "removeComponent_LBL", RB.objA ) ;//"Remove";
//String unlinkComponentActionLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "unlinkComponent_LBL", RB.objA ) ;//"Unlink";
//String updateComponentActionLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "updateComponent_LBL", RB.objA ) ;//"Update";
//String deleteComponentActionLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "deleteComponent_LBL", RB.objA ) ;//"Delete";
//String viewComponentActionLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "viewComponent_LBL", RB.objA ) ;//"View";
//String checkinLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION , "checkIn_OPT", RB.objA ) ;

String lockedByColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "lockedBy_TBL_HDR", RB.objA ) ;//"Locked By";
String lastModifiedColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "lastModified_TBL_HDR", RB.objA ) ;//"Modified";
String lastModifiedByColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "lastModifiedBy_TBL_HDR", RB.objA ) ;//"Modified By";

String addExistingComponentsLabelAnotherProd = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "addExisistingComponentAnotherProd_LBL", RB.objA ) ;//"Add Components From Another Product";
String primarySpecLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "primarySpecIndicator_LBL", RB.objA ) ;//"Primary";
String setSpecAsPrimaryLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "setSpecAsPrimary_LBL", RB.objA ) ;//"Set As Primary";
String childSpecificationsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "childSpecs_LBL", RB.objA ) ;//"Child Specifications";
String removeSpecAsPrimaryLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "removeSpecAsPrimary_LBL", RB.objA ) ;//"Remove As Primary";
String areYouSureYouWantRemoveComponent = WTMessage.getLocalizedMessage ( RB.SEASON, "areYouSureYouWantRemoveComponent_CNFRM" , RB.objA );
String areYouSureYouWantRemoveComponents = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "areYouSureYouWantRemoveComponents_CNFRM" , RB.objA );
String generatedTechPacksLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "generatedTechPacks_LBL" , RB.objA );
String specDetailsLabel= WTMessage.getLocalizedMessage ( RB.FLEXSPEC, "flexSpecDetails_PG_HEAD" , RB.objA );
String copyLabel=WTMessage.getLocalizedMessage ( RB.MAIN, "copy_LBL" , RB.objA );
String primarySpecificationLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "primarySpec_LBL", RB.objA ) ;//"Primary";
%>
<%!
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String JSPNAME = "ViewFlexSpec";    
    public static final boolean FORUMS_ENABLED= LCSProperties.getBoolean("jsp.discussionforum.discussionforum.enabled");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final String DOCUMENT_REFERENCES = PageManager.getPageURL("DOCUMENT_REFERENCES_V2", null);
    public static final String EPMDOCUMENT_REFERENCES_PLUGIN = PageManager.getPageURL("EPMDOCUMENT_REFERENCES_PLUGIN_V2", null);
    public static final String PART_REFERENCES_PLUGIN = PageManager.getPageURL("PART_REFERENCES_PLUGIN", null);
    public static final String DISCUSSION_FORM = PageManager.getPageURL("DISCUSSION_FORM", null);
    public static final String DISCUSSION_FORM_POSTINGS = PageManager.getPageURL("DISCUSSION_FORM_POSTINGS", null);
    public static final String ACTION_OPTIONS = PageManager.getPageURL("ACTION_OPTIONS", null);
    public static final String WC_META_DATA= PageManager.getPageURL("WC_META_DATA_V2", null);
    public static final String SPEC_SEASON_REPORT = PageManager.getPageURL("WC_META_DATA", null);
    public static final boolean USE_DOCUMENTS = LCSProperties.getBoolean("jsp.specification.ViewSpecificationPlugin.useDocuments");
    public static final boolean USE_CAD_DATA = LCSProperties.getBoolean("com.lcs.wc.specification.cadData.Enabled");
    public static final boolean USE_PART_DATA = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
    public static final boolean USE_META = LCSProperties.getBoolean("jsp.specification.ViewSpecificationPlugin.useMeta");
    public static final boolean USE_SEASON_REPORT = LCSProperties.getBoolean("jsp.specification.ViewSpecificationPlugin.useSeasonReport");
    public static final String LIFE_CYCLE_MANAGED = PageManager.getPageURL("LIFE_CYCLE_MANAGED", null);
    public static final boolean USE_PDF_PRINT_SPEC = LCSProperties.getBoolean("jsp.specification.PrintSpec.PDF");
    public static final boolean USE_PRIMARY_BOM = LCSProperties.getBoolean("com.lcs.wc.specification.usePrimaryBOM");
    public static final boolean USE_PRIMARY_SPEC = LCSProperties.getBoolean("com.lcs.wc.specification.usePrimarySpec");
    public static final boolean REVISE_VAL = LCSProperties.getBoolean("com.lcs.wc.document.LCSDocument.revise");

 public static final boolean SUBSCRIPTION_ENABLED= LCSProperties.getBoolean("jsp.subscriptions.enabled");
 public static final String SUBSCRIPTION_FORM = PageManager.getPageURL("SUBSCRIPTION_FORM", null);

    public static final String OID = "OID";
    public static final String NAME = "NAME";
    public static final String STATUS = "STATUS";
    public static final String VIEWSCRIPT = "VIEWSCRIPT";
    public static final String UPDATESCRIPT = "UPDATESCRIPT";
    public static final String COMPONENT_TYPE = "COMPONENT_TYPE";
    public static final String PRIMARY_COMPONENT = "PRIMARY_COMPONENT";
    public static final String COMPONENT_PARENT = "COMPONENT_PARENT";
    public static final String COMPONENT_LINK_ID = "COMPONENT_LINK_ID";
    public static final String LOCKED_BY_ID = "LOCKED_BY_ID";
    public static final String LAST_MODIFIED_ON = "LAST_MODIFIED_ON";
    public static final String MODIFIED_BY = "MODIFIED_BY";
    public static final String CHECKED_OUT = "CHECKED_OUT";
    public static final String CHECK_IN_SCRIPT = "CHECK_IN_SCRIPT";

    public String getParentSpecDisplay(FlexSpecification spec) throws WTException{
        String DELIM = LCSProperties.get("jsp.specification.CreateSpecification.parentSpecNameDelim", "-");
        Object[] objA = new Object[0];
        String SPEC_MAIN = "com.lcs.wc.resource.SpecificationRB";

        String parentSpecLabel = WTMessage.getLocalizedMessage ( SPEC_MAIN, "parentSpec_LBL", objA ) ;//"Parent Spec";
        parentSpecLabel = FormatHelper.formatJavascriptString(parentSpecLabel);

        Collection parents = FlexSpecQuery.findSpecToSpecLinks(spec, null);
        StringBuffer buffer = new StringBuffer();

        if(parents != null && parents.size() > 0){
            FlexObject link = (FlexObject)parents.iterator().next();
            String specId = "VR:com.lcs.wc.specification.FlexSpecification:" + link.getString("PARENTSPECIFICATION.BRANCHIDITERATIONINFO");
            FlexSpecification pspec = (FlexSpecification)FlexSpecQuery.findObjectById(specId);

            String specName = (String)pspec.getValue("specName");
            String prodName = (String)SeasonProductLocator.getProductARev((LCSPartMaster)pspec.getSpecOwner()).getValue("productName");
            String sourceName = (String)((LCSSourcingConfig)VersionHelper.latestIterationOf((LCSSourcingConfigMaster)pspec.getSpecSource())).getValue("name");

            String displayName = FormatHelper.encodeAndFormatForHTMLContent(prodName) + " " + DELIM + " " + FormatHelper.encodeAndFormatForHTMLContent(sourceName) + " " + DELIM + " " + FormatHelper.encodeAndFormatForHTMLContent(specName);
            int width = displayName.length() * 9;

            buffer.append("<a ");
            buffer.append("onmouseover=\"return overlib('" + parentSpecLabel + ": " + displayName + "', WIDTH, " + width + ");\" ");
            buffer.append("onmouseout=\"return nd();\" ");
            buffer.append("href=\"javascript:goToSpec('" + specId + "');\" ");
            buffer.append(">");
            buffer.append("<img border=\"0\" src=\"" + WT_IMAGE_LOCATION + "/hotlist.gif\">");
            buffer.append("</a>");
        }
        
        return buffer.toString();
    }
    //add for SPR# 2010667 03/17/2011
    //set the flag of working state for Specification Components
    public Collection processSpecComponents(Collection components) throws Exception {
        for (Iterator it = components.iterator(); it.hasNext(); ) {
            final Map<String, String> h = (Map<String,String>) it.next();
            final WTObject wtObj = (WTObject)LCSQuery.findObjectById(h.get("OID"), true, false);
            
            if (wtObj == null) {
                // user does not have READ access to wtObj
                continue;
            }
            
            RevisionControlled revision_controlled = (RevisionControlled) wtObj;
             
            boolean isCheckedOut = VersionHelper.isCheckedOut(revision_controlled);
            boolean isCheckedOutByUser = VersionHelper.isCheckedOutByUser(revision_controlled);
            boolean isWorkingCopy = VersionHelper.isWorkingCopy(revision_controlled);
            
            // as curernt user does not have access to teh checked out version, it cannot see
            // who it is locked by because of permission rules
            boolean replaceCopy=(isCheckedOut && !isCheckedOutByUser && isWorkingCopy);
            if (replaceCopy) {
                revision_controlled = (RevisionControlled) VersionHelper.getOriginalCopy(revision_controlled);
            }
            
            boolean isLocked = LockHelper.isLocked(revision_controlled);
            
            if (isCheckedOut) {
                WTUser user = (WTUser) LockHelper.getLocker(revision_controlled);
                if (replaceCopy) {
                    //fix the current values
                    h.put(CHECKED_OUT,Boolean.toString(isCheckedOut));
                    if (isLocked) {
                        h.put(LOCKED_BY_ID,FormatHelper.getNumericObjectIdFromObject(user));
                    }
                    
                    if (isCheckedOutByUser) {
                        h.put("COMSWORKINGSTATE", "c/o");
                    } else {
                        h.put("COMSWORKINGSTATE", "n/a");
                    }
                } else if (isLocked) {
                    h.put("COMSWORKINGSTATE", "n/a");
                } else {
                    h.put("COMSWORKINGSTATE", "c/i");
                }
            }
        }
        
        return components;
    }

%>
<%
    String oid = request.getParameter("oid");
    String activity = request.getParameter("activity");
    String cadViewId =   request.getParameter("cadViewId");
    String partViewId =   request.getParameter("partViewId");

    
    lcsContext.setCacheSafe(true);

    flexg2.setModuleName("FLEXSPEC");
    String flexSpecId = FormatHelper.hasContent(appContext.getFstslId())? appContext.getFstslId() :request.getParameter("flexSpecId");

    FlexSpecification flexSpec = null;
    FlexSpecToSeasonLink fstsl = null;
    boolean isPrimarySpec = false;
    if(flexSpecId.indexOf("FlexSpecToSeasonLink") > -1){
        fstsl = (FlexSpecToSeasonLink)FlexSpecQuery.findObjectById(flexSpecId);
        flexSpec = (FlexSpecification)VersionHelper.latestIterationOf(fstsl.getSpecificationMaster());
        isPrimarySpec = fstsl != null && fstsl.isPrimarySpec();
    }
    else{
        flexSpec = (FlexSpecification)FlexSpecQuery.findObjectById(flexSpecId);
    } 
    String primarySpecIndicator = USE_PRIMARY_SPEC && isPrimarySpec ? "<span class='PRIMARYINDICATOR'>" + primarySpecLabel + "</span>" : "";
    boolean canModifyFlexSpec = ACLHelper.hasModifyAccess(flexSpec); 
    boolean isPrimaryInAnySeason = false;
    if (USE_PRIMARY_SPEC) {
        SearchResults results = FlexSpecQuery.findPrimarySpecToSeasonLinks(null, null, flexSpec.getMaster());
        isPrimaryInAnySeason = results.getResultsFound() > 0;
    }
    String parentSpecDisplay = getParentSpecDisplay(flexSpec);

    //logger.debug("parentSpecDisplay: " + parentSpecDisplay);

    String sourceName = flexSpec.getSpecSource().getIdentity();

    String nId = FormatHelper.getNumericObjectIdFromObject(flexSpec);
    String SpecOid = FormatHelper.getObjectId(flexSpec);
    String specNId=FormatHelper.getNumericFromOid(flexSpecId);
    boolean isLatestIteration = VersionHelper.isLatestIteration(flexSpec);
    boolean isCheckedOut = VersionHelper.isCheckedOut(flexSpec);
    boolean isWorkingCopy = VersionHelper.isWorkingCopy(flexSpec);
    boolean isCheckedOutByUser = VersionHelper.isCheckedOutByUser(flexSpec);
    boolean editable = (!isCheckedOut || isCheckedOutByUser);
    if(isCheckedOut && isCheckedOutByUser && !isWorkingCopy){
        flexSpec = (FlexSpecification) VersionHelper.getWorkingCopy(flexSpec);
    }
    String checkedOutBy = "";
    String checkedOutByEmail = "";
    if(!editable){
        WTUser user = (WTUser) LockHelper.getLocker(flexSpec);
        checkedOutBy = user.getFullName();
        checkedOutByEmail = user.getEMail();
    }

    type = flexSpec.getFlexType();
    String specTypeId = FormatHelper.getObjectId(type);

    String errorMessage = request.getParameter("errorMessage");

    //this is de-generalized due to part severence.  Will need to work with SpecOwner if ever use spec outside of LCSProduct (i.e. Materials)
    LCSPartMaster ownerMaster = (LCSPartMaster)flexSpec.getSpecOwner();
    LCSProduct owner = (LCSProduct)VersionHelper.latestIterationOf(ownerMaster);
    String ownerId = FormatHelper.getObjectId(owner);

    String additionalData = "";
    additionalData = additionalData + "&ownerId=" + ownerId;
    additionalData = additionalData + "&specId=" + flexSpecId;
    additionalData = additionalData + "&action=SEARCH_ONLY";

    Collection specComponents = FlexSpecQuery.getSpecToComponentObjectsData(flexSpec);
    Vector sortOrder = new Vector();
    sortOrder.add(COMPONENT_TYPE);
    sortOrder.add(NAME);
    specComponents = SortHelper.sortFlexObjects(specComponents, sortOrder);
    specComponents = processSpecComponents(specComponents);

    boolean showDetails = "true".equals(request.getParameter("showDetails"));

    String seasonReport = new FlexSpecUtil().getSpecSeasonReportTable(flexSpec, 0);

    String indent = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    String cAction = request.getParameter("action");
    String cActivity = request.getParameter("activity");
    List<ActionLink> dropdownitems = new ArrayList<>();
    DropdownMenuIcon actionmenu = new DropdownMenuIcon();
    actionmenu.setPopupOnClick(true);
    actionmenu.setImageUrl(IMG_LOCATION+"/icon_group.svg");
    JavascriptFunctionCall kebabToggle = JavascriptFunctionCall.create("openDropdownMenu")
            .addArgument(Argument.asJsVariable("event"));
    actionmenu.setOnClick(kebabToggle);
    actionmenu.setMenuContentId("SpecificationDetailsActionsMenu");

    CommonActionsProvider actionProvider=CommonActionsProvider.getCommonActionsProvider();
%>


<%
    //Compontent table columns
    //Build potentially required "Hard attribute" columns
    TableColumn column = null;
    Collection columnList = new ArrayList();
    

    // start to build ajax menu column
    SpecComponentTableActionColumn actionsTableColumn = new SpecComponentTableActionColumn();
    actionsTableColumn.setHeaderLabel("");
    actionsTableColumn.setDisplayed(true);
    actionsTableColumn.setLinkTableIndex("COMPONENT_LINK_ID");
    actionsTableColumn.setLinkMethodPrefix("");
    actionsTableColumn.setColumnWidth("5%");
    columnList.add(actionsTableColumn);



    Iterator it = specComponents.iterator();
    FlexObject fo = null;
    // prepare data in specComponents to display action menu
    while (it.hasNext()){
        fo = ((FlexObject)it.next());
        fo.setData("AdditionalParameter", "&additionalParameter="+flexSpecId + MOAHelper.DELIM + fo.get("OID") + "&hasDesignCard="+request.getParameter("hasDesignCard")); //Set hasDesignCard for SPR:2157481 
    }
    

    // added for SPR 13205184
    column = new LinkedComponentTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel("");
    column.setHeaderAlign("left");
    column.setTableIndex(COMPONENT_PARENT);
    column.setColumnWidth("1%");
    columnList.add(column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(nameColumnLabel);
    column.setHeaderAlign("left");
    column.setTableIndex(NAME);
    column.setLinkMethodIndex(VIEWSCRIPT);
    column.setLinkTableIndex(OID);
    column.setUseQuickInfo(true);
    columnList.add(column);

    //LAST_MODIFIED_ON
    //MODIFIED_BY

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(componentTypeColumnLabel);
    column.setHeaderAlign("left");
    column.setTableIndex(COMPONENT_TYPE);
    //column.setHeaderLink("javascript:resort('WTUser.name')");
    columnList.add(column);

    if(USE_PRIMARY_BOM){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(isPrimaryComponent_LBL);
        column.setHeaderAlign("left");
        column.setTableIndex(PRIMARY_COMPONENT);
        columnList.add(column);
    }

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(statusColumnLabel);
    column.setHeaderAlign("left");
    column.setTableIndex(STATUS);
    //column.setHeaderLink("javascript:resort('WTUser.name')");
    columnList.add(column);

    column = new UserTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(lockedByColumnLabel);
    column.setHeaderAlign("left");
    column.setTableIndex(LOCKED_BY_ID);
    columnList.add(column);

    column = new UserTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(lastModifiedByColumnLabel);
    column.setHeaderAlign("left");
    column.setTableIndex(MODIFIED_BY);
    columnList.add(column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(lastModifiedColumnLabel);
    column.setHeaderAlign("left");
    column.setTableIndex(LAST_MODIFIED_ON);
    column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
    columnList.add(column);


    //LOCKED_BY_ID
    //LAST_MODIFIED_ON
    //MODIFIED_BY


%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JSP METHODS /////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
   function addComponents(ids){
       addSpecComponents('<%= FormatHelper.encodeForJavascript(flexSpecId) %>', ids);
   }

   function launchAddOtherComponents(value, display){
       var addData = "&ownerId=" + value;
       addData = addData + "&specId=<%= FormatHelper.encodeForJavascript(flexSpecId)%>";
       addData = addData + "&action=SEARCH_ONLY&hideClearValueButton=true";
       addData = addData + "&showBOM=true";

       launchModuleChooser('SPEC_COMPONENTS','' ,'' ,'', true, 'addComponents', false, 'object', '', addData, true);
   }

   function addEPMDocs(ids){
       addHiddenElement('extendedSpecId', 'OR:com.lcs.wc.specification.FlexSpecification:<%=nId%>');
       addSpecEPMDocs('<%= FormatHelper.encodeForJavascript(flexSpecId) %>',ids);
   }

   function addParts(ids){
        addHiddenElement('extendedSpecId', 'OR:com.lcs.wc.specification.FlexSpecification:<%=nId%>');
        addSpecParts('<%= FormatHelper.encodeForJavascript(flexSpecId) %>',ids);
   }
</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<input type="hidden" name="specTypeId" value="<%= FormatHelper.getObjectId(type) %>">
<input type="hidden" name="flexSpecMasterId" value="<%= FormatHelper.getObjectId(flexSpec.getMaster()) %>">
<div class="contentPage card card__overflow">
   <input type="checkbox" id="SpecificationDetailsCard" class="cardCheckbox" checked disabled>
  <div class="card-header">
    <label class="card-label" for="SpecificationDetailsCard"><%=specDetailsLabel%></label>
    <div class="card-header-menu" style="float:right">
      <% if(fstsl != null && USE_PRIMARY_SPEC){
             String fSTSL = FormatHelper.format(fstsl.isPrimarySpec());
              %>
      <div class ="card-header-menu-item sourcing-header-label">
          <%= primarySpecificationLabel %> <%= fSTSL %>
      </div>
       <% } %>
      <div class="card-header-menu-item">
  <% if (isLatestIteration){
       if (USE_PRIMARY_SPEC && fstsl != null && !isPrimarySpec && canModifyFlexSpec) { 
           dropdownitems.add(new ActionLink("setSpecAsPrimary",setSpecAsPrimaryLabel,
                 JavascriptFunctionCall.create("setSpecAsPrimary").addArgument(Argument.asSafeStringLiteral(FormatHelper.getObjectId(fstsl)))));
       }
       if (USE_PRIMARY_SPEC && fstsl != null && isPrimarySpec && canModifyFlexSpec) { 
           dropdownitems.add(new ActionLink("removeSpecAsPrimary",removeSpecAsPrimaryLabel,
                   JavascriptFunctionCall.create("removeSpecAsPrimary").addArgument(Argument.asSafeStringLiteral(FormatHelper.getObjectId(fstsl)))));
       }
       if (canModifyFlexSpec) {
           dropdownitems.add(new ActionLink("updateSpec",updateSpecLabel,
                   JavascriptFunctionCall.create("updateSpecification").addArgument(Argument.asSafeStringLiteral(FormatHelper.getVersionId(flexSpec)))));

           additionalData = "&chooserSelectFunction=opener.launchAddOtherComponents";

           dropdownitems.add(new ActionLink("addExistingComponentsFromAnotherProd",addExistingComponentsLabelAnotherProd,
                   JavascriptFunctionCall.create("launchModuleChooser")
                   .addArgument(Argument.asSafeStringLiteral("SPEC_COMPONENTS_OTHER_PROD"))
                   .addArgument(Argument.asSafeStringLiteral(""))
                   .addArgument(Argument.asSafeStringLiteral(""))
                   .addArgument(Argument.asSafeStringLiteral(""))
                   .addArgument(Argument.asBoolean(false))
                   .addArgument(Argument.asSafeStringLiteral("addComponents"))
                   .addArgument(Argument.asBoolean(false))
                   .addArgument(Argument.asSafeStringLiteral("object"))
                   .addArgument(Argument.asSafeStringLiteral(""))
                   .addArgument(Argument.asSafeStringLiteral(FormatHelper.encodeForJavascript(additionalData)))));

           if (USE_CAD_DATA){
               additionalData = "&ownerId=" + ownerId + "&specId=" + flexSpecId + "&action=SEARCH_ONLY" + "&hideClearValueButton=true";
               dropdownitems.add(new ActionLink("addExistingEPMDocuments",addExistingEPMDocumentsLabel,
                       JavascriptFunctionCall.create("launchModuleChooser")
                       .addArgument(Argument.asSafeStringLiteral("SPEC_EPM_DOCUMENTS"))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asBoolean(false))
                       .addArgument(Argument.asSafeStringLiteral("addEPMDocs"))
                       .addArgument(Argument.asBoolean(false))
                       .addArgument(Argument.asSafeStringLiteral("object"))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asSafeStringLiteral(FormatHelper.encodeForJavascript(additionalData)))));
           }
           if (USE_PART_DATA){
               additionalData = "&ownerId=" + ownerId + "&specId=" + flexSpecId + "&action=SEARCH_ONLY" + "&hideClearValueButton=true";
               dropdownitems.add(new ActionLink("addExistingParts",addExistingPartsLabel,
                       JavascriptFunctionCall.create("launchModuleChooser")
                       .addArgument(Argument.asSafeStringLiteral("SPEC_PARTS"))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asBoolean(false))
                       .addArgument(Argument.asSafeStringLiteral("addParts"))
                       .addArgument(Argument.asBoolean(false))
                       .addArgument(Argument.asSafeStringLiteral("object"))
                       .addArgument(Argument.asSafeStringLiteral(""))
                       .addArgument(Argument.asSafeStringLiteral(FormatHelper.encodeForJavascript(additionalData)))));
           }

           dropdownitems.add(actionProvider.getLifecycleManagedLink(FormatHelper.getObjectId(flexSpec.getFlexType()), null, FormatHelper.getVersionId(flexSpec), activity, oid));

           dropdownitems.add(new ActionLink("copy",copyLabel,
                   JavascriptFunctionCall.create("initFromProduct")
                   .addArgument(Argument.asSafeStringLiteral(FormatHelper.getVersionId(flexSpec)))
                   .addArgument(Argument.asSafeStringLiteral(appContext.getSourcingConfigId()))
                   .addArgument(Argument.asSafeStringLiteral(appContext.getSeasonId()))));
       }
       if(USE_PDF_PRINT_SPEC){
           dropdownitems.add(new ActionLink("generatePDF",generatePDFOption,
                   JavascriptFunctionCall.create("generateSpecPDFAjax")
                   .addArgument(Argument.asSafeStringLiteral(FormatHelper.encodeForJavascript(flexSpecId)))
                   .addArgument(Argument.asBoolean(true))));
       }
      if(ACLHelper.hasDeleteAccess(flexSpec)){
           dropdownitems.add(new ActionLink("deleteSpec",deleteSpecLabel,
               JavascriptFunctionCall.create("deleteSpec")
               .addArgument(Argument.asSafeStringLiteral(FormatHelper.encodeForJavascript(flexSpecId)))
               .addArgument(Argument.asBoolean(isPrimaryInAnySeason))));
      }
      dropdownitems.add(new ActionLink("copySpecToClipBoard",copySpecToolTip,
              JavascriptFunctionCall.create("copyToClipboard")
              .addArgument(Argument.asSafeStringLiteral(FormatHelper.getVersionId(flexSpec)))
              .addArgument(Argument.asSafeStringLiteral("COPY_SPEC_TO_CLIPBOARD"))));
      if(flexSpec instanceof wt.workflow.forum.SubjectOfForum & flexSpec instanceof wt.inf.container.WTContained) { 
            if(FORUMS_ENABLED){
                dropdownitems.add(actionProvider.getDiscussionForumLink(FormatHelper.getVersionId(flexSpec)));
            }
            if(SUBSCRIPTION_ENABLED){
                dropdownitems.add(actionProvider.getSubscriptionLink(FormatHelper.getVersionId(flexSpec)));
            }
        }
   }
     actionmenu.setSubActions (dropdownitems);
   %>
    <%=FormGenerator2.getFormFieldRenderer().renderDropdownMenuIconButton(actionmenu) %>
     </div>
   </div>
 </div>
</div>
<div class="contentPage card">
 <input type="checkbox" id="ComponentCard" class="cardCheckbox" checked>
 <div class="card-header">
  <label class="card-label card-collapsible" for="ComponentCard"><%= componentsLabel %></label>
 </div>
<div class="contents-table card-content">
        <%= tg.drawTable(specComponents, columnList, "", false, false) %>
</div>
</div>
<jsp:include page="<%=subURLFolder+ DOCUMENT_REFERENCES %>" flush="true">
    <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(flexSpec) %>" />
    <jsp:param name="returnActivity" value="VIEW_SEASON_PRODUCT_LINK" />
    <jsp:param name="returnOid" value="<%= flexSpecId %>" />
    <jsp:param name="cardLBL" value="<%=generatedTechPacksLabel %>" />
    <jsp:param name="showGeneratedTechPackOnly" value="true" />
    <jsp:param name="hideGeneratedTechPackOnly" value="false" />
</jsp:include>


     <%= flexg2.generateDetails(flexSpec) %>



                <% if(USE_DOCUMENTS) { %>
                            <jsp:include page="<%=subURLFolder+ DOCUMENT_REFERENCES %>" flush="true">
                                <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(flexSpec) %>" />
                                <jsp:param name="returnActivity" value="VIEW_SEASON_PRODUCT_LINK" />
                                <jsp:param name="returnOid" value="<%= flexSpecId %>" />
                                <jsp:param name="cardLBL" value="<%= docsLabel %>" />
                                <jsp:param name="showGeneratedTechPackOnly" value="false" />
                                <jsp:param name="hideGeneratedTechPackOnly" value="true" />
                            </jsp:include>
                <%} %>
                <% if(REVISE_VAL){ %>
                            <jsp:include page="<%=subURLFolder+ DOCUMENT_REFERENCES %>" flush="true">
                                <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(flexSpec) %>" />
                                <jsp:param name="returnActivity" value="VIEW_SEASON_PRODUCT_LINK" />
                                <jsp:param name="returnOid" value="<%= flexSpecId %>" />
                                <jsp:param name="cardLBL" value="<%= descByDocsLabel %>" />
                                <jsp:param name="revise" value="<%=REVISE_VAL%>" />
                                <jsp:param name="showGeneratedTechPackOnly" value="false" />
                                <jsp:param name="hideGeneratedTechPackOnly" value="true" />
                            </jsp:include>
                <% } %>
                <% if(USE_PART_DATA) { %>
                            <jsp:include page="<%=subURLFolder+ PART_REFERENCES_PLUGIN %>" flush="true">
                                <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(flexSpec) %>" />
                                <jsp:param name="returnActivity" value="VIEW_SEASON_PRODUCT_LINK" />
                                <jsp:param name="returnOid" value="<%= flexSpecId %>" />
                            </jsp:include>
                <% } %>
                <% if(USE_CAD_DATA) { %>
                          <jsp:include page="<%=subURLFolder+ EPMDOCUMENT_REFERENCES_PLUGIN %>" flush="true">
                              <jsp:param name="targetOid" value="<%= FormatHelper.getVersionId(flexSpec) %>" />
                              <jsp:param name="returnActivity" value="VIEW_SEASON_PRODUCT_LINK" />
                              <jsp:param name="returnOid" value="<%= flexSpecId %>" />
                           </jsp:include>
                <% } %>

<div class="contentPage card">
 <input type="checkbox" id="childSpecificationsCard" class="cardCheckbox" checked>
 <div class="card-header">
  <label class="card-label card-collapsible" for="childSpecificationsCard"><%= childSpecificationsLabel %></label>
 </div>
<div class="contents-table card-content">
          <%
               String childReport = (new FlexSpecUtil()).getChildSpecTable(flexSpec);
           %>
          <%= childReport %>
</div>
</div>
                <% if(USE_META){ %>
                            <jsp:include page="<%=subURLFolder+ WC_META_DATA %>" flush="true">
                                <jsp:param name="targetOid" value="<%= FormatHelper.getObjectId(flexSpec) %>" />
                            </jsp:include>
                <% } %>
<script>
addScrollListnerOnTable(".table-wrapper");
</script>