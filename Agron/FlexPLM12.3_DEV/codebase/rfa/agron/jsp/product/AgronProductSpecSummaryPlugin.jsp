<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page import="wt.access.NotAuthorizedException"%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.document.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.part.*,
                com.lcs.wc.product.*,
                com.lcs.wc.season.*,
                com.lcs.wc.sourcing.*,
                com.lcs.wc.specification.*,
                com.lcs.wc.sample.*,
                wt.ownership.*,
                wt.locks.LockHelper,
                wt.org.*,
                wt.util.*,
                java.text.*,
                java.util.*,
                com.lcs.wc.client.ClientContext,
                com.lcs.wc.client.web.html.header.action.LifecycleManagedActionImpl"
%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="type" scope="request" class="com.lcs.wc.flextype.FlexType" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>

<%
//setting up which RBs to use
Object[] objA = new Object[0];
String Product_MAIN = "com.lcs.wc.resource.ProductRB";
String SPEC_MAIN = "com.lcs.wc.resource.SpecificationRB";
String RB_MAIN = "com.lcs.wc.resource.MainRB";

String imagesPgHead = WTMessage.getLocalizedMessage ( Product_MAIN, "images_PG_HEAD", objA );
String addImagePageButton = WTMessage.getLocalizedMessage ( Product_MAIN, "addImagePage_Btn", objA );
String updateButton = WTMessage.getLocalizedMessage ( RB_MAIN, "update_Btn", objA );
String removeButton = WTMessage.getLocalizedMessage ( Product_MAIN, "remove_Btn", objA );
String importButton = WTMessage.getLocalizedMessage ( Product_MAIN, "import_Btn", objA );
String nameColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "component_LBL", RB.objA );
String componentTypeColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "componentType_LBL", RB.objA );
String isPrimaryComponent_LBL = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "isPrimaryComponent_LBL", RB.objA );
String statusColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "componentStatus_LBL", RB.objA );
String lockedByColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "lockedBy_TBL_HDR", RB.objA );
String lastModifiedColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "lastModified_TBL_HDR", RB.objA );
String lastModifiedByColumnLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "lastModifiedBy_TBL_HDR", RB.objA );

String componentsLabel = WTMessage.getLocalizedMessage ( RB.SPECIFICATION, "components_GRP_LBL", RB.objA );
String noSpecForSeasonPgHead = WTMessage.getLocalizedMessage ( SPEC_MAIN, "noSpecForSeason_PG_HEAD", objA );
String noSpecForProductPgHead = WTMessage.getLocalizedMessage ( SPEC_MAIN, "noSpecForProduct_PG_HEAD", objA );
String noSpecForSourcePgHead = WTMessage.getLocalizedMessage ( SPEC_MAIN, "noSpecForSource_PG_HEAD", objA );
String addExistingSpecsLabel = WTMessage.getLocalizedMessage ( SPEC_MAIN, "specSummaryAddExisting_LBL", objA );
String createNewSpecLabel = WTMessage.getLocalizedMessage ( SPEC_MAIN, "specSummaryAddNew_LBL", objA );
String deleteSpecConfirm = WTMessage.getLocalizedMessage ( SPEC_MAIN, "deleteSpecConfirm_MSG", objA );
String removeSpecConfirm = WTMessage.getLocalizedMessage ( SPEC_MAIN, "removeSpecConfirm_MSG", objA );
String thisSpecIsPrimary = WTMessage.getLocalizedMessage ( SPEC_MAIN, "thisSpecIsPrimary_LBL", objA );
%>
<%!
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String JSPNAME = "ProductSpecSummaryPlugin";
    public static final boolean DEBUG = true;
    public static final String VIEW_SPEC_PLUGIN = PageManager.getPageURL("VIEW_SPEC_PLUGIN", null);
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final boolean USE_PRIMARY_BOM = LCSProperties.getBoolean("com.lcs.wc.specification.usePrimaryBOM");
    public static final String MEASUREMENTS_CREATE_PLUGIN = PageManager.getPageURL("MEASUREMENTS_CREATE_PLUGIN", null);
    private static final int HTTP_TIMEOUT = LCSProperties.get("com.lcs.wc.sample.console.httpTimeoutPeriod", 300000);
    private static final String AUTH_TOKEN = LCSProperties.get("com.ptc.rfa.rest.authorizationToken", "");
    public static final String CREATE_SPEC_PLUGIN = PageManager.getPageURL("CREATE_SPEC_PLUGIN", null);
    public  final boolean isPrimarySpecHighlight = LCSProperties.getBoolean("com.lcs.wc.specification.highlightPrimarySpec", true);
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
    public static final boolean USE_CAD_DATA = LCSProperties.getBoolean("com.lcs.wc.specification.cadData.Enabled");
    public static final boolean USE_PART_DATA = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
    public static final boolean USE_PDF_PRINT_SPEC = LCSProperties.getBoolean("jsp.specification.PrintSpec.PDF");
    public static final boolean FORUMS_ENABLED= LCSProperties.getBoolean("jsp.discussionforum.discussionforum.enabled");
    public static final boolean SUBSCRIPTION_ENABLED= LCSProperties.getBoolean("jsp.subscriptions.enabled");

%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
    String flexplmImageLocation = LCSProperties.get("flexPLM.urlContext.override");
    lcsContext.setCacheSafe(true);
    String webAppName = "/Windchill";
    WTProperties wtproperties = WTProperties.getLocalProperties();
    webAppName = "/" + wtproperties.getProperty("wt.webapp.name");
    String contextualProductId=request.getParameter("oid");
    Locale localeValue = ClientContext.getContext().getResolvedLocale();
    String locale = localeValue.toString();
    boolean isVendor = lcsContext.isVendor();
    String changeStateAccessIds = null;
    LCSSeason season = appContext.getSeason();
    LCSSeasonMaster seasonMaster = null;
    LCSProduct productSeasonRev = appContext.getProductSeasonRev();
    LCSProduct productRevA = appContext.getProductARev();
    LCSPartMaster productMaster = (LCSPartMaster)productRevA.getMaster();
    String showSpecId = appContext.getSpecId();
    LCSSourcingConfig source = appContext.getSourcingConfig();
    String contextualSeasonId=appContext.getSeasonId();
    String contextualSourceConfigId=appContext.getSourcingConfigId();
    LCSSourcingConfigMaster sourceMaster = null;
    if(source != null){
        sourceMaster = (LCSSourcingConfigMaster)source.getMaster();
    }
    FlexType specType = productRevA.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SPEC_TYPE);
    FlexType sourcingConfigType = productRevA.getFlexType().getReferencedFlexType(ReferencedTypeKeys.SOURCING_CONFIG_TYPE);

    //FlexType imagePageType = FlexTypeCache.getFlexTypeFromPath("Document\\Images Page");

    FlexSpecQuery fsq = new FlexSpecQuery();
    SearchResults specs = fsq.findSpecsByOwner(productMaster, seasonMaster, sourceMaster, specType);
    Collection specResults = specs.getResults();
    Map specsMap = appContext.getSpecsMap();
    Iterator results = specResults.iterator();
    SearchResults productSpecs = fsq.findSpecsByOwner(productMaster, null, null, null);
    Collection productSpecsResults = productSpecs.getResults();
    LCSProduct prodActiveVersion = productRevA;
    if(productSeasonRev != null){
        prodActiveVersion = productSeasonRev;
    }
    String prodActiveId = FormatHelper.getVersionId(prodActiveVersion);

    LCSSKU skuActiveVersion = appContext.getSKUARev();
    if(appContext.getSKUSeasonRev() != null){
        skuActiveVersion = appContext.getSKUSeasonRev();
    }
    String skuActiveId = null;
    if(skuActiveVersion != null){
        skuActiveId = FormatHelper.getVersionId(skuActiveVersion);
    }

    String addExisitngSpecsAdditionalData = "&ownerId=" + prodActiveId;
    addExisitngSpecsAdditionalData = addExisitngSpecsAdditionalData + "&action=SEARCH_ONLY";
    if(appContext.getSourcingConfigId() != null){
        addExisitngSpecsAdditionalData = addExisitngSpecsAdditionalData + "&sourceId=" + appContext.getSourcingConfigId();
    }

    String noSpecLabel = noSpecForProductPgHead;
    if(season != null){
        noSpecLabel = noSpecForSeasonPgHead;
    }
    if(source != null){
        noSpecLabel = noSpecForSourcePgHead;
    }

    String returnOid = "";
    if(FormatHelper.hasContent(appContext.getSourceToSeasonLinkId())){
        returnOid = appContext.getSourceToSeasonLinkId();
    }
    else if(FormatHelper.hasContent(appContext.getSourcingConfigId())){
        returnOid = appContext.getSourcingConfigId();
    }
    else{
        returnOid = prodActiveId;
    }

    String cadViewId =  (String)request.getAttribute("cadViewId");
    String partViewId = (String)request.getAttribute("partViewId");

    String viewType = FormatHelper.hasContent(request.getParameter("viewType"))?request.getParameter("viewType"):(String)request.getAttribute("viewType");

    if(request.getAttribute("viewIdOverride") != null&&"CAD".equals(viewType)){
        cadViewId = (String) request.getAttribute("viewIdOverride");
    } if(request.getAttribute("viewIdOverride") != null&&"Part".equals(viewType)){
        partViewId = (String) request.getAttribute("viewIdOverride");
    }

    // consider none view situation
    if(!FormatHelper.hasContent(cadViewId)&&!" ".equals(cadViewId)){
        cadViewId ="";
    }

    if(!FormatHelper.hasContent(partViewId)&&!" ".equals(partViewId)){
        partViewId ="";
    }


%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
    function checkInSpecImagePage(returnId, id){
        document.MAINFORM.activity.value = 'UPDATE_DOCUMENT';
        document.MAINFORM.action.value = 'CHECKIN';
        document.MAINFORM.imagePageId.value = id;
        document.MAINFORM.oid.value = id;
        document.MAINFORM.returnActivity.value = document.REQUEST.activity;
        document.MAINFORM.returnAction.value = document.REQUEST.action;
        document.MAINFORM.returnOid.value = returnId;
        <% if(FormatHelper.hasContent(skuActiveId)) { %>
        document.MAINFORM.returnAddlParams.value = 'contextSKUId=<%= skuActiveId %>';
        <% } %>
        submitForm();
    }

    function checkInSpecConstruction(returnId, id){
        document.MAINFORM.activity.value = 'EDIT_CONSTRUCTIONINFO';
        document.MAINFORM.action.value = 'CHECKIN';
        document.MAINFORM.oid.value = id;
        document.MAINFORM.returnActivity.value = document.REQUEST.activity;
        document.MAINFORM.returnAction.value = document.REQUEST.action;
        document.MAINFORM.returnOid.value = returnId;
        <% if(FormatHelper.hasContent(skuActiveId)) { %>
        document.MAINFORM.returnAddlParams.value = 'contextSKUId=<%= skuActiveId %>';
        <% } %>
        submitForm();
    }

    function checkInSpecMeasurements(returnId, id){
        document.MAINFORM.activity.value = 'EDIT_MEASUREMENTS';
        document.MAINFORM.action.value = 'CHECKIN';
        document.MAINFORM.oid.value = id;
        document.MAINFORM.returnActivity.value = document.REQUEST.activity;
        document.MAINFORM.returnAction.value = document.REQUEST.action;
        document.MAINFORM.returnOid.value = returnId;
        <% if(FormatHelper.hasContent(skuActiveId)) { %>
        document.MAINFORM.returnAddlParams.value = 'contextSKUId=<%= skuActiveId %>';
        <% } %>
        submitForm();
    }

    function checkInSpecBOM(returnId, id){
        document.MAINFORM.activity.value = 'EDIT_BOM';
        document.MAINFORM.action.value = 'CHECK_IN_QUICK';
        document.MAINFORM.oid.value = id;
        document.MAINFORM.returnActivity.value = document.REQUEST.activity;
        document.MAINFORM.returnAction.value = document.REQUEST.action;
        document.MAINFORM.returnOid.value = returnId;
        <% if(FormatHelper.hasContent(skuActiveId)) { %>
        document.MAINFORM.returnAddlParams.value = 'contextSKUId=<%= skuActiveId %>';
        <% } %>
        submitForm();
    }


    function deleteSpec(specId, isPrimary) {

        var message = "";
        if (isPrimary) {
            message = "<%= FormatHelper.encodeForJavascript(thisSpecIsPrimary)%>" + " ";
        }
        message = message + "<%= FormatHelper.encodeForJavascript(deleteSpecConfirm)%>";
        if (confirm(message)) {
            document.MAINFORM.action.value = 'INIT';
            document.MAINFORM.activity.value = 'DELETE_FLEXSPEC';
            document.MAINFORM.oid.value = specId;
            document.MAINFORM.returnOid.value = '<%= returnOid %>';
            document.MAINFORM.returnAction.value = document.REQUEST.action;
            document.MAINFORM.returnActivity.value = document.REQUEST.activity;
            <% if(FormatHelper.hasContent(skuActiveId)) { %>
            document.MAINFORM.returnAddlParams.value = 'contextSKUId=<%= skuActiveId %>';
            <% } %>

            submitForm();
        }
    }

    function removeSpecFromSeason(specId, isPrimary){
        var message = "";
        if (isPrimary) {
            message = "<%= FormatHelper.encodeForJavascript(thisSpecIsPrimary)%>" + " ";
        }
        message = message + "<%= FormatHelper.encodeForJavascript(removeSpecConfirm)%>";
        if (confirm(message)) {
            document.MAINFORM.action.value = 'INIT';
            document.MAINFORM.activity.value = 'REMOVE_FLEXSPEC';
            document.MAINFORM.oid.value = specId;
            document.MAINFORM.returnOid.value = '<%= returnOid %>';
            document.MAINFORM.returnAction.value = document.REQUEST.action;
            document.MAINFORM.returnActivity.value = document.REQUEST.activity;
            <% if(FormatHelper.hasContent(skuActiveId)) { %>
            document.MAINFORM.returnAddlParams.value = 'contextSKUId=<%= skuActiveId %>';
            <% } %>

            submitForm();
        }
    }
    function goToSpec(specId){
        document.MAINFORM.oid.value=specId;
        document.MAINFORM.tabPage.value = 'SPEC_SUMMARY';
        submitForm();
    }

    function initFromProduct(specId,sourceConfigId,seasonId){
            document.MAINFORM.sourceSpecId.value = specId;
            document.MAINFORM.oid.value =sourceConfigId;
            document.MAINFORM.activity.value = 'COPY_SPECIFICATION';
            document.MAINFORM.returnActivity.value = document.REQUEST.activity;
            document.MAINFORM.returnOid.value = '<%= appContext.getActiveProductId() %>';
            if(hasContent(seasonId) != null){
            document.MAINFORM.specSeason.value = seasonId;
            }
            submitForm();
    }
</script>


<input type="hidden" name="productMasterId" value="<%= FormatHelper.getObjectId((wt.fc.WTObject)productRevA.getMaster()) %>">
<input type="hidden" name="measurementsId" value="">
<input type="hidden" name="constructionId" value="">
<input type="hidden" name="imagePageId" value="">
<input type="hidden" name="bomPartId" value="">
<input type="hidden" name="onloadId" value="">
<input type="hidden" name="cadViewId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(cadViewId) %>">
<input type="hidden" name="partViewId" value="<%= FormatHelper.encodeAndFormatForHTMLContent(partViewId) %>">
<input type='hidden' name="relevantActivity">
<input type='hidden' name="typesString">
<link href="<%=URL_CONTEXT%>/css/domtableeditor.css" rel=stylesheet>
 <% if(productSpecsResults.isEmpty()){ %>
    <div id='newSpecDiv'>
            <jsp:include page="<%=subURLFolder+ CREATE_SPEC_PLUGIN %>" flush="true">
                    <jsp:param name="none" value="" />
              </jsp:include>
     </div>
        <script>
           (function($) {
               $(document).ready( function() {
                   var div = document.getElementById('create-spec-panel');
                   div.style.display= 'block';
               })
           })(jQuery);
        </script>
   <% } else { %>
            <jsp:include page="<%=subURLFolder+ CREATE_SPEC_PLUGIN %>" flush="true">
                    <jsp:param name="none" value="" />
            </jsp:include>
            <script>
              (function($) {
                 $(document).ready( function() {
                   var div = document.getElementById('create-spec-panel');
                   div.classList.add("drawer-overlay");
                   div.classList.add("spec-drawer-overlay");
                 })
              })(jQuery);
          </script>

<script language="javascript">
    sessionStorage.setItem('httpTimeout', '<%= HTTP_TIMEOUT %>');
    sessionStorage.setItem('AUTH_TOKEN', '<%= AUTH_TOKEN %>');
    sessionStorage.setItem('contextualSpecId','<%= showSpecId%>');
    sessionStorage.setItem('isPrimarySpecHighlight','<%= isPrimarySpecHighlight%>');
</script>
<%
for (Object obj: productSpecsResults){
    String specId = ((FlexObject) obj).getString("FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
    String flexSpecId="VR:com.lcs.wc.specification.FlexSpecification:" + specId;
    FlexSpecification flexSpec = null;
    try {
        flexSpec = (FlexSpecification)FlexSpecQuery.findObjectById(flexSpecId);
    } catch (WTRuntimeException e){
        if (!e.toString().contains("NotAuthorizedException")){
            throw e;
        } else {
            continue;
        }
    }
    LifecycleManagedActionImpl lifecycleManagedActionImpl = new LifecycleManagedActionImpl();
    if (lifecycleManagedActionImpl.isLifecycleManaged(flexSpec.getFlexType())){
        if (!FormatHelper.hasContent(changeStateAccessIds)){
            changeStateAccessIds = specId;
        } else {
            changeStateAccessIds += "," + specId;
        }
    }
}
while(results.hasNext()){
    FlexObject tfo = (FlexObject) results.next();
    String specId = tfo.getString("FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
    String flexSpecId="VR:com.lcs.wc.specification.FlexSpecification:" + tfo.getString("FLEXSPECIFICATION.BRANCHIDITERATIONINFO");
    FlexSpecification flexSpec = (FlexSpecification)FlexSpecQuery.findObjectById(flexSpecId);
    String nId = FormatHelper.getNumericObjectIdFromObject(flexSpec);
%>
  <script>
  function addComponents<%= specId %>(ids){
      addSpecComponents('<%= FormatHelper.encodeForJavascript(flexSpecId) %>', ids);
  }

  function launchAddOtherComponents<%= specId %>(value, display){
      var addData = "&ownerId=" + value;
      addData = addData + "&specId=<%= FormatHelper.encodeForJavascript(flexSpecId)%>";
      addData = addData + "&action=SEARCH_ONLY&hideClearValueButton=true";
      addData = addData + "&showBOM=true";

      launchModuleChooser('SPEC_COMPONENTS','' ,'' ,'', true, 'addComponents<%= specId %>', false, 'object', '', addData, true);
  }

  function addEPMDocs<%= specId %>(ids){
      addHiddenElement('extendedSpecId', 'OR:com.lcs.wc.specification.FlexSpecification:<%=nId%>');
      addSpecEPMDocs('<%= FormatHelper.encodeForJavascript(flexSpecId) %>',ids);
  }
  function addParts<%= specId %>(ids){
      addHiddenElement('extendedSpecId', 'OR:com.lcs.wc.specification.FlexSpecification:<%=nId%>');
      addSpecParts('<%= FormatHelper.encodeForJavascript(flexSpecId) %>',ids);
  }

  </script>

<%}
%>




<c:if test="${ ! empty appContext.specification }">
<%
//SPR:2157481
Collection coll = new LCSProductQuery().getProductImages(productRevA);
boolean hasDesignCard = false;
for (Iterator it = coll.iterator(); it.hasNext();) {
    LCSDocument doc = (LCSDocument)it.next();
    String pageType = doc.getFlexType().getAttribute("pageType").getValue(doc).toString();
    if("quickSpec".equals(pageType)){
        hasDesignCard = true;
        break;
    }
}

%>
   <jsp:include page="<%=subURLFolder+ VIEW_SPEC_PLUGIN %>" flush="true">
                <jsp:param name="flexSpecId" value="<%= showSpecId %>" />
                <jsp:param name="cadViewId" value="<%= cadViewId %>" />
                <jsp:param name="partViewId" value="<%= partViewId %>" />
    </jsp:include>

</c:if>
<%@ include file="../../../flexplmapps/specifications-summary/specifications-summary.jspf"%>
 <% } %>
