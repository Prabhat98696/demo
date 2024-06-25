<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@ page language="java"
    errorPage="../exception/ErrorReport.jsp"
        import="com.lcs.wc.util.*,
                com.lcs.wc.client.Activities,
                com.lcs.wc.changeAudit.ChangeTrackingPageHelper,
                com.lcs.wc.client.web.PageManager,
                wt.util.*,
                java.util.*,
                com.lcs.wc.util.json.*,
                com.lcs.wc.flextype.*,
                com.ptc.netmarkets.util.misc.NetmarketURL,
                com.ptc.netmarkets.util.beans.NmURLFactoryBean,
                com.ptc.netmarkets.util.misc.NmAction,
                com.ptc.mvc.util.MVCUrlHelper,
                wt.httpgw.URLFactory,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,

                com.lcs.wc.client.web.WebControllers,
                com.ptc.netmarkets.util.misc.NmAction"
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean  id="url_factory" class="wt.httpgw.URLFactory" scope="request" />
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />

<%=NmAction.getXUACompatibleTag(request)%>

<%
response.setDateHeader("Expires", -1);
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

lcsContext.initContext();
lcsContext.setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));

wt.util.WTContext.getContext().setLocale(wt.httpgw.LanguagePreference.getLocale(request.getHeader("Accept-Language")));%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.main.Header");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String defaultContentType = LCSProperties.get("com.lcs.wc.util.CharsetFilter.ContentType","text/html");
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
	public static final String QUICKSEARCH_PRODUCT_ATTS = LCSProperties.get("jsp.main.Header.quickSearchProductAtts","agrArticle,agrWorkOrderNoProduct");
    public static final String PRODUCT_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Product.Root");
    public static final String MATERIAL_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Material.Root");
    public static final boolean USE_SAMPLE = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useSample");
    public static final boolean USE_MATERIAL = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useMaterial");
    public static final boolean USE_RFQ = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useRFQ");
    public static final boolean ENABLE_RFQ = LCSProperties.getBoolean("com.lcs.wc.sourcing.useRFQ");
    public static final boolean USE_ORDERCONF = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.orderConfirmation");
    public static final boolean USE_ORDERCONFIRMATION = LCSProperties.getBoolean("com.lcs.wc.product.useOrderConfirmation");
    public static final String PRODUCT_ORDER_CONFIRMATION_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sourcing.OrderConfirmation.Product.Root", "Order Confirmation\\Product");
    public static final boolean USE_PRODUCT = LCSProperties.getBoolean("jsp.vendorportal.VendorPortalSideMenu.useProduct");
    public static final boolean USE_CAD = LCSProperties.getBoolean("com.lcs.wc.specification.cadData.Enabled");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final String refreshDiscussionInboxFrequency = LCSProperties.get("flexPLM.header.refreshDiscussionInboxFrequency","900");
    public static final boolean FORUMS_ENABLED= LCSProperties.getBoolean("jsp.discussionforum.discussionforum.enabled");
    public static final String APPS_URL = LCSProperties.get("flexPLM.apps.mainURL","None");
    public static final String SIDE_MENU_NAVIGATION_CLICK = LCSProperties.get("sidenavigator.Click","true");

    public static String OHC_URL_CONTEXT = "";
    public static String COMPLETE_URL = "";
    static {
        try {
            OHC_URL_CONTEXT = wt.util.WTProperties.getLocalProperties().getProperty ("wt.fhc.url");
            COMPLETE_URL = wt.util.WTProperties.getLocalProperties().getProperty ("wt.server.codebase");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

%>
<%
    response.setContentType( defaultContentType+"; charset=" +defaultCharsetEncoding);

    String myHomeButton  = WTMessage.getLocalizedMessage ( RB.MAIN, "myHome_Btn", RB.objA ) ;
    String clipboardLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "clipboard_LBL", RB.objA ) ;
    String toggleSideBarButton = WTMessage.getLocalizedMessage (RB.MAIN, "toggleSideBar_LBL", RB.objA ) ;
    String searchLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "search_Btn", RB.objA ) ;
    String newLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "new_LBL", RB.objA ) ;
    String inboxLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "inbox_LBL",RB.objA ) ;


    String pLMNavigatorLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "pLMNavigator_LBL", RB.objA ) ;
    String myWorkButton = WTMessage.getLocalizedMessage ( RB.MAIN, "myWork_Btn", RB.objA ) ;
    String homeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "home_LBL", RB.objA ) ;
    String userPreferencesLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "userPreferences_LBL", RB.objA ) ;
    String libraryLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "library_LBL", RB.objA ) ;
    String logoutLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "logOut_LBL", RB.objA ) ;
    String helpLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "flexPLMHelpCenter_LBL", RB.objA ) ;
    String aboutLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "aboutFlexPLM_LBL", RB.objA ) ;
    String goToWindchillLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "goToWindchill_LBL", RB.objA ) ;

    String searchFieldLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "search_Btn", RB.objA ) ;
    searchFieldLabel=FormatHelper.encodeForHTMLContent(searchFieldLabel);
    String quickLinksLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "quickLinks_LBL", RB.objA ) ;

    String skuLabel = WTMessage.getLocalizedMessage ( RB.CLIPBOARD, "skus_LBL", RB.objA ) ;

    String welcomeLbl = WTMessage.getLocalizedMessage ( RB.MAIN, "welcome_LBL", RB.objA ) ;
    String gotoHomePagePgTle = WTMessage.getLocalizedMessage ( RB.MAIN, "gotoHomePage_PG_TLE",RB.objA ) ;
    String asteriskIllegalMessage = WTMessage.getLocalizedMessage ( RB.MAIN, "asteriskIllegal_MSG",RB.objA ) ;
    String blankIllegalMessage = WTMessage.getLocalizedMessage ( RB.MAIN, "blankIllegal_MSG",RB.objA ) ;

    String allOptionLabel = WTMessage.getLocalizedMessage ( RB.INDEXSEARCH, "allOption_LBL",RB.objA ) ;
    String showChangeSinceLabel = WTMessage.getLocalizedMessage(RB.EVENTS, "showChangeSince_lbl",RB.objA);

    String showChangeSinceDefault = "";
    String jsCalendarFormat = WTMessage.getLocalizedMessage( RB.DATETIMEFORMAT, "jsCalendarFormat", RB.objA);
    String jsCalendarInputFormat = WTMessage.getLocalizedMessage( RB.DATETIMEFORMAT, "jsCalendarInputFormat", RB.objA);
    String appSwitcherLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "app_switcher_LBL", RB.objA ) ;

        String activity = request.getParameter("activity");
        String flexTypeName = request.getParameter("flexTypeName");

        boolean allowExplorer = LCSProperties.getBoolean("webroot.jsp.main.Header.allowExplorer");
        boolean enableHelp = LCSProperties.getBoolean("webroot.jsp.main.Header.enableHelp");
        String wexplorer = url_factory.getHREF("wt/clients/folderexplorer/explorer.jsp");
        String firstTimestamp = request.getParameter("timestamp");
        boolean skipTemplateEnds = FormatHelper.parseBoolean(request.getParameter("bypassTemplateEnds"));

        boolean INDEX_ENABLED = false;
        WTProperties props = WTProperties.getLocalProperties();
        INDEX_ENABLED = props.getProperty ("wt.index.enabled", false);
		
	// Additions for searching on additional attributes other than name
	FlexType productType = FlexTypeCache.getFlexTypeRoot("Product");
	Collection quickSearchAtts = new Vector();
	StringTokenizer tokenizer = new StringTokenizer(QUICKSEARCH_PRODUCT_ATTS, ",");
	while(tokenizer.hasMoreTokens()){
		quickSearchAtts.add(productType.getAttribute(tokenizer.nextToken()));
	}
	NmURLFactoryBean urlFactoryBean = new NmURLFactoryBean();
	String libOid =	new wt.fc.ReferenceFactory().getReferenceString(com.lcs.wc.util.FlexContainerHelper.getFlexContainer());
        String parameters = "components$loadWizardStep$" + libOid + "$|forum$discuss$" + libOid + "&oid=" + libOid;
    HashMap urlParam = new HashMap();
    urlParam.put("context", parameters);
    String discussionLink = FormatHelper.convertToShellURL(NetmarketURL.buildURL(urlFactoryBean, "FlexPLM/forumTopic", "participationView", null, urlParam, true, new NmAction()));

%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>

	function refreshProperties(){
        document.MAINFORM.activity.value = 'RESET_PROPERTIES';
        submitForm();
    }
    function viewReportsList(){
        document.MAINFORM.activity.value = 'REPORTS_LIST';
        submitForm();
    }

    function PreferencesPopupPage() {
     var windowopts = "location=no,scrollbars=no,menubars=no,toolbars=no,resizable=yes,left=8,top=8,width=350,height=500";
        popup = open('<%=URL_CONTEXT%>/jsp/main/Preferences.jsp','preferencesPopup',windowopts);
     popup.focus();

    }

    function libraryPopupPage() {
     var windowopts = "location=no,scrollbars=no,menubars=no,toolbars=no,resizable=yes,left=8,top=8,width=450,height=500";
        popup = open('<%=URL_CONTEXT%>/jsp/library/libraryMain.jsp','libraryPopup',windowopts);
     popup.focus();

    }

     function OHCPopupPage() {
        wcHomePage('<%=OHC_URL_CONTEXT%>');

    }

    function viewExecutiveDashboard(oid){
        document.MAINFORM.activity.value = 'EXECUTIVE_DASHBOARD_CRITERIA';
        submitForm();
    }

    function viewAbout(){
        var w  = 780;
        var h = 400;
        var left = (screen.width/2)-(w/2);
        var top = (screen.height/3)-(h/2)+14;
        var windowopts = "location=no,scrollbars=no,menubars=no,toolbars=no,resizable=yes,left="+left+",top="+top+",width=780,height=500";
        popup = open('<%=COMPLETE_URL%>/netmarkets/jsp/util/about.jsp','copyright',windowopts);
        popup.focus();
    }


<% if(allowExplorer) { %>
   function docOpen() {
      docwin=open("<%=wexplorer%>",
            "windchill_explorer",
            "resizable=yes,scrollbars=yes,menubar=no,toolbar=no,location=no,status=yes,height=370,width=640");
   }
<% } %>

function checkSearchSubmit(navEvent){
    var enterKey = false;
    if(window.event && window.event.keyCode == 13){
        enterKey = true;
    }
    if(navEvent && navEvent.which == 13){
        enterKey = true;
    }

    if(enterKey){
        doSearch();
        return false;
    }

}



function doSearch(){
	var optionvalue;
	var optionString;
	optionvalue = document.MAINFORM.searchdropdown.options[document.MAINFORM.searchdropdown.selectedIndex].value;
	document.MAINFORM.returnActivity.value=document.REQUEST.returnActivity;
	document.MAINFORM.returnAction.value='INIT';
	document.MAINFORM.returnOid.value='';
	document.MAINFORM.quickSearchCriteria.value = trim(document.MAINFORM.quickSearchCriteria.value);
	addHiddenElement("filterId", " ");
    // REMOVE THE OLD DISPLAY ATTRIBUTE INPUT TO AVOID
    // ISSUES WHEN SEARCHES ARE DONE FOR DIFFERETN OBJECTS
    var oldDisplayAtt = $("displayAttribute");
    if(oldDisplayAtt){
        oldDisplayAtt.parentNode.removeChild(oldDisplayAtt);
    }


    var index = document.MAINFORM.elements.length;
    displayAtt = document.createElement('input');
    displayAtt.setAttribute("type", "HIDDEN");
    displayAtt.setAttribute("name", "displayAttribute");
    displayAtt.setAttribute("value", "name");
    displayAtt.setAttribute("id", "displayAttribute");
	
	var checkAtt = document.createElement('input');
	checkAtt.setAttribute("type", "HIDDEN");
	checkAtt.setAttribute("name", "checkAttribute");
	checkAtt.setAttribute("value", "true");
    checkAtt.setAttribute("id", "checkAttribute");
	
	if(optionvalue=="Keyword"){
        indexSearch();
    } else if(optionvalue=="Product"){
        displayAtt.setAttribute("value", "productName");
        document.MAINFORM.appendChild(displayAtt);
        doProductSearch();
    } else if(optionvalue=="Material"){
        document.MAINFORM.appendChild(displayAtt);
        doMaterialSearch();
    } else if(optionvalue=="Document"){
        document.MAINFORM.appendChild(displayAtt);
        doDocumentSearch();
    } else if(optionvalue=="Sku"){
        displayAtt.setAttribute("value", "skuName");
        document.MAINFORM.appendChild(displayAtt);
        doSkuSearch();
    }  else if(optionvalue=="Color"){
        document.MAINFORM.appendChild(displayAtt);
        doColorSearch();
    }  else if(optionvalue=="BusinessObject"){
        document.MAINFORM.appendChild(displayAtt);
        doBusinessObjectSearch();
    }  else if(optionvalue=="ChangeActivity"){
        document.MAINFORM.appendChild(displayAtt);
        doChangeActivitySearch();
    }  else if(optionvalue=="Country"){
        document.MAINFORM.appendChild(displayAtt);
        doCountrySearch();
    }  else if(optionvalue=="DocumentCollection"){
        document.MAINFORM.appendChild(displayAtt);
        doDocumentCollectionSearch();
    }  else if(optionvalue=="Last"){
        document.MAINFORM.appendChild(displayAtt);
        doLastSearch();
    }  else if(optionvalue=="Media"){
        document.MAINFORM.appendChild(displayAtt);
        doMediaSearch();
    }  else if(optionvalue=="OrderConfirmation"){
        document.MAINFORM.appendChild(displayAtt);
        doOrderConfirmationSearch();
    }  else if(optionvalue=="OrderConfirmation/Product"){
        document.MAINFORM.appendChild(displayAtt);
        doOrderConfirmationSearch('OrderConfirmation/Product');
    }  else if(optionvalue=="Palette"){
        document.MAINFORM.appendChild(displayAtt);
        doPaletteSearch();
    }  else if(optionvalue=="RFQ"){
        document.MAINFORM.appendChild(displayAtt);
        doRFQSearch();
    }  else if(optionvalue=="Sample"){
        document.MAINFORM.appendChild(displayAtt);
        doSampleSearch();
    }  else if(optionvalue=="Sample/Material"){
        document.MAINFORM.appendChild(displayAtt);
        doSampleSearch('Sample/Material');
    }  else if(optionvalue=="Sample/Product"){
        document.MAINFORM.appendChild(displayAtt);
        doSampleSearch('Sample/Product');
    }  else if(optionvalue=="Season"){
        document.MAINFORM.appendChild(displayAtt);
        doSeasonSearch();
    }  else if(optionvalue=="Supplier"){
        document.MAINFORM.appendChild(displayAtt);
        doSupplierSearch();
    } else if(optionvalue=="Placeholder"){
        displayAtt.setAttribute("value", "placeholderName");
        document.MAINFORM.appendChild(displayAtt);
        doPlaceholderSearch();
    }
		// Agron Custom code starts
	else if(optionvalue.startsWith("PRODUCT")){

		checkAtt.setAttribute("value", "true");		
		document.MAINFORM.appendChild(checkAtt);

		optionString = optionvalue.split("_");
		displayAtt.setAttribute("value", optionString[1]);		
        document.MAINFORM.appendChild(displayAtt);
		doProductSearch();
	} else if(optionvalue.startsWith("SKU")){

		checkAtt.setAttribute("value", "true");		
		document.MAINFORM.appendChild(checkAtt);

		optionString = optionvalue.split("_");
		displayAtt.setAttribute("value", optionString[1]);		
        document.MAINFORM.appendChild(displayAtt);
		doSkuSearch();
	}

	// Agron Custom code ends
}

function doAttSearch(activity){
	document.MAINFORM.quickSearchCriteria.value = '';
	document.MAINFORM.activity.value = activity;
	document.MAINFORM.action.value = 'SEARCH';
	document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Product")) %>';
	submitForm();
}

function doColorSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_COLOR';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Color")) %>';
        submitForm();
    }
}function doBusinessObjectSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_CONTROLLED_BUSINESS_OBJECT';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Business Object")) %>';
        submitForm();
    }
}function doChangeActivitySearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_CHANGEACTIVITY';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Change Activity")) %>';
        submitForm();
    }
}function doCountrySearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_COUNTRY';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Country")) %>';
        submitForm();
    }
}function doDocumentCollectionSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_DOCUMENTCOLLECTION';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Document Collection")) %>';
        submitForm();
    }
}function doLastSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_LAST';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Last")) %>';
        submitForm();
    }
}function doMediaSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_MEDIA';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Media")) %>';
        submitForm();
    }
}function doPaletteSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_PALETTE';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Palette")) %>';
        submitForm();
    }
}function doRFQSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_RFQ';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("RFQ")) %>';
        submitForm();
    }
}function doSampleSearch(typePath){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_SAMPLE';
        document.MAINFORM.action.value = 'SEARCH';
                if(hasContent(typePath)){
                    if(typePath == 'Sample/Material'){
                        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(MATERIAL_ROOT_TYPE)) %>';
                    }else if(typePath == 'Sample/Product'){
                        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(PRODUCT_ROOT_TYPE)) %>';
                    }
                }else{
                    document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Sample")) %>';
                }
        submitForm();
    }
}function doSeasonSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_SEASON';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Season")) %>';
        submitForm();
    }
}function doSupplierSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_SUPPLIER';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Supplier")) %>';
        submitForm();
    }
}



function doSkuSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_SKU';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Product")) %>';
        submitForm();
    }
}

function doPlaceholderSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_PLACEHOLDER';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Product")) %>';
        submitForm();
    }
}
function doProductSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;
    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_PRODUCT';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Product")) %>';
        submitForm();
    }
}


function doOrderConfirmationSearch(typePath){
    var criteria = document.MAINFORM.quickSearchCriteria.value;
    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_ORDER_CONFIRMATION';
        document.MAINFORM.action.value = 'SEARCH';

               if(hasContent(typePath)){
                    if(typePath == 'OrderConfirmation/Product'){
                        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath(PRODUCT_ORDER_CONFIRMATION_ROOT_TYPE)) %>';
                    }
                }else{
                    document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Order Confirmation")) %>';
                }

        submitForm();
    }
}



function doMaterialSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){

        document.MAINFORM.activity.value = 'FIND_ADVANCED_MATERIAL';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Material")) %>';
        submitForm();
    }
}

function doDocumentSearch(){
    var criteria = document.MAINFORM.quickSearchCriteria.value;

    if(hasContent(criteria)){
        document.MAINFORM.activity.value = 'FIND_DOCUMENT';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.type.value = '<%= FormatHelper.getObjectId(FlexTypeCache.getFlexTypeFromPath("Document")) %>';
        submitForm();
    }
}

function indexSearch(){
    if (validate_keyword()) {
        document.MAINFORM.activity.value = 'INDEX_SEARCH';
        document.MAINFORM.action.value = 'SEARCH';
        document.MAINFORM.globalSearch.value = 'true';
        document.MAINFORM.headerIndexSearchKeyword.value = document.MAINFORM.quickSearchCriteria.value;
        document.MAINFORM.quickSearchCriteria.value = '';
        submitForm();
    }
}

function validate_keyword() {
    if (trim(document.MAINFORM.quickSearchCriteria.value) == "*") {
        alert('<%=FormatHelper.formatJavascriptString(asteriskIllegalMessage, false)%>');
        return false;
    } else if (trim(document.MAINFORM.quickSearchCriteria.value) == "") {
        alert('<%=FormatHelper.formatJavascriptString(blankIllegalMessage, false)%>');
        return false;
    }
    return true;
}

function handleChangeSearchDropDown(){
    var dropDown = $('searchDropDownSelect');

    var searchButton = $('searchButton');
    var newIcon = $('newObjectIcon');

    var value = dropDown.options[dropDown.selectedIndex].value;
    if(value == 'Sku' || value == 'Sample' || value == 'Sample/Material' || value == 'Sample/Product'|| value == 'Placeholder' || value.startsWith("PRODUCT") ){
        newIcon.style.display = 'none';
        // Add space to the right of search   margin-right:25px;
        searchButton.style.marginRight = "33px";

    } else {
        newIcon.style.display = 'block';
        // Reset space on right of search to  margin-right:5px;
        searchButton.style.marginRight = "5px";

    }
    document.MAINFORM.quickSearchCriteria.focus();
    document.MAINFORM.quickSearchCriteria.select();
}

</script>

<%!
    public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");

    public static final String STANDARD_TEMPLATE_HEADER = PageManager.getPageURL("STANDARD_TEMPLATE_HEADER", null);
    public static final String STANDARD_TEMPLATE_FOOTER = PageManager.getPageURL("STANDARD_TEMPLATE_FOOTER", null);

    public static final String adminGroup = LCSProperties.get("jsp.main.administratorsGroup", "Administrators");
    public static final String typeAdminGroup = LCSProperties.get("jsp.flextype.adminGroup", "Type Administrators");
    public static final String userAdminGroup = LCSProperties.get("jsp.users.adminGroup","User Administrators");
    public static final String calendarAdminGroup = LCSProperties.get("jsp.calendar.adminGroup","Calendar Administrators");
    public static final String processAdminGroup = LCSProperties.get("jsp.process.adminGroup","Process Administrators");

    public static final String COMPANY_IMAGE = PageManager.getPageURL("COMPANY_IMAGE", null);
    public static final String NEW_SELECT = PageManager.getPageURL("NEW_SELECT", null);
    public static final String FIND_SELECT = PageManager.getPageURL("FIND_SELECT", null);
    public static final boolean USE_CHANGE_ACTIVITY = LCSProperties.getBoolean("com.lcs.wc.change.useChangeActivity");

    public static String WindchillContext = "/Windchill";

    static{
        try{
            WTProperties wtproperties = WTProperties.getLocalProperties();
            WindchillContext = "/" + wtproperties.getProperty("wt.webapp.name");
        }
        catch(Throwable throwable){
            throw new ExceptionInInitializerError(throwable);
        }
    }
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////////// BEGIN HTML ///////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<html>
<Head>
</Head>
<body>

<input name="<%= FlexTypeCache.getFlexTypeFromPath("Product").getAttribute("productName").getSearchCriteriaIndex() %>" value="" type="hidden">
<input name="<%= FlexTypeCache.getFlexTypeFromPath("Material").getAttribute("name").getSearchCriteriaIndex() %>" value="" type="hidden">

<% if(!skipTemplateEnds){ %>
<jsp:include page="<%=subURLFolder+ STANDARD_TEMPLATE_HEADER %>" flush="true" />
<% } %>
<input type=hidden name=skipRanges value="">
<input name="headerIndexSearchKeyword" value="" type="hidden">
<input type="hidden" name="globalSearch" value="">

<!-- side navigation functionality for mouse over or click -->
<% if(Boolean.parseBoolean(SIDE_MENU_NAVIGATION_CLICK)){ %>
<div class="pageheader" id="container">

<% } else { %>
<div class="pageheader" id="container" onmouseenter="collapseSideBarOnMouseOut(event)">
<% } %>

          <div id="leftWelcomeSection"  >
            <%if(APPS_URL!="None"){%>
            <div id="image" >
                <a href="javascript:toggleAppSwitcher()" tabindex="0"><img id="apps" title='<%= FormatHelper.formatHTMLString(appSwitcherLabel)%>' border="0" src="<%=URL_CONTEXT%>/images/blue-apps-icon.svg" ></img></a>
            </div>
            <%}%>
            <div id="image">
                <a href="javascript:home()" tabindex="0"><img id="logo" title='<%= FormatHelper.formatHTMLString(gotoHomePagePgTle)%>' border="0" src="<%=URL_CONTEXT%><%=COMPANY_IMAGE%>" height="32"></a>
            </div>

            <%if(FORUMS_ENABLED){%>
                <div id="discussionForumImg">
                    <img id="discussionForumButton" tabindex="3" onClick="javascript:createDialogWindow('<%=discussionLink%>','Inbox', '700', '700')" title='<%= FormatHelper.formatHTMLString(inboxLabel) %>' src="<%=WT_IMAGE_LOCATION%>/topic.gif">
                </div>
                <div id="discussionCountDiv">
                    <font id="discussionCountLabel"></font>
                </div>
            <%}%>
            <div id="userLabelDiv">
                    <font id="userNameLabel" ><%= FormatHelper.encodeAndFormatForHTMLContent(lcsContext.getUser().getFullName()) %></font>
            </div>
          </div>
                  <%
                    /** Creating change Widget */
                    FlexType changeTrackSinceType =  FlexTypeCache.getFlexTypeFromPath("Business Object\\Common Attribute Lists");
                    AttributeValueList CTSValList = null;
                    Vector order = new Vector();
                    try {
                        FlexTypeAttribute changeTrackingSinceFlexAttr = changeTrackSinceType.getAttribute("changeTrackingSince");
                        if (changeTrackingSinceFlexAttr !=null) {
                            showChangeSinceDefault = ChangeTrackingPageHelper.getChangeEventDaysDefault();
                            CTSValList = changeTrackingSinceFlexAttr.getAttValueList();
                            order.addAll(CTSValList.getSelectableKeys(lcsContext.getLocale(), true));
                           // order.remove(showChangeSinceDefault);
                           // order.add(showChangeSinceDefault);
                        }
                    } catch(FlexTypeException fte) {
                        logger.debug(fte.getLocalizedMessage());
                    }

                    HashMap<String, String> daysMap = new HashMap<String, String>();
                    Map daysDisplayMap = new HashMap();
                    String defaultDaysValue = "";
                    if (CTSValList != null) {
                        defaultDaysValue = CTSValList.get(showChangeSinceDefault, AttributeValueList.DAYS);
                        daysDisplayMap = CTSValList.toLocalizedMapSelectable(lcsContext.getLocale());

                        Collection<String> keys = daysDisplayMap.keySet();
                        Iterator<String> keysIt = keys.iterator();
                        while (keysIt.hasNext()) {
                            String key = keysIt.next();
                            String daysValue = CTSValList.get(key, AttributeValueList.DAYS);
                            daysMap.put(key, daysValue);
                        }
                    }

                  %>



                      <% if(enableHelp) {%>

                       <jsp:include page='<%=subURLFolder  + "/jsp/help/HelpLinkPlugin.jsp"%>' flush="true">
                            <jsp:param name="activity" value='<%= request.getParameter("activity") %>'/>
                            <jsp:param name="objType" value='<%= request.getParameter("flexTypeName") %>'/>
                            <jsp:param name="windowTitle" value='Create Product'/>
                       </jsp:include>
                       <%}%>

               <div id="showSearchSection">
                        <div id="quickLinkDiv" >
                        <%
                                /* Create the Menu for the Quick Links. JS function gotoQuickLink will fire on Change.*/
                                Map<String, String> quickLinkMenuMap = new LinkedHashMap<String, String>();

                                quickLinkMenuMap.put("quicklinks", quickLinksLabel);
                                quickLinkMenuMap.put("showClipboard", clipboardLabel);
                                quickLinkMenuMap.put("showPreferences", userPreferencesLabel);
                                quickLinkMenuMap.put("logout", logoutLabel);

                                if(lcsContext.inGroup(adminGroup.toUpperCase()) || lcsContext.inGroup("DEVELOPERS"))
                                {
                                    quickLinkMenuMap.put("showLibrary", libraryLabel);
                                }

                                quickLinkMenuMap.put("showHelp", helpLabel);

                                /* If the CAD view is enabled, add */
                                if(USE_CAD)
                                {
                                    quickLinkMenuMap.put("showGoToWindchill", goToWindchillLabel);
                                }

                                quickLinkMenuMap.put("showAbout", aboutLabel);

                                %>

                        <%=fg.createMenuDropDownList(quickLinkMenuMap, "quickLinkSelectionId", quickLinksLabel, "gotoQuickLink()", 1, "") %>

                        </div>
                        <div id="searchDropDown" >
                        <img id="newObjectIcon" class="page-header__new-object-button" title="<%= FormatHelper.formatHTMLString(newLabel) %>"
                            onClick="createObject(document.MAINFORM.searchdropdown.options[document.MAINFORM.searchdropdown.selectedIndex].value)"
                            src="<%=URL_CONTEXT%>/images/icon_add.svg">

                        <div id='searchButton' class='page-header__search-button' onclick="doSearch()" title='<%= FormatHelper.formatHTMLString(searchLabel) %>'></div>

                        <input id='searchField' type="text" name="quickSearchCriteria" onKeyPress="return checkSearchSubmit(event)"  placeholder="<%= searchFieldLabel %>...">

                        <select id="searchDropDownSelect" name="searchdropdown" onChange="handleChangeSearchDropDown()">

                                        <%
                                        FlexType businessObject = FlexTypeCache.getFlexTypeRoot("Business Object");
                                        FlexType changeActivity = FlexTypeCache.getFlexTypeRoot("Change Activity");
                                        FlexType country = FlexTypeCache.getFlexTypeRoot("Country");
                                        FlexType color = FlexTypeCache.getFlexTypeRoot("Color");
                                        FlexType document = FlexTypeCache.getFlexTypeRoot("Document");
                                        FlexType documentCollection = FlexTypeCache.getFlexTypeRoot("Document Collection");
                                        FlexType last = FlexTypeCache.getFlexTypeRoot("Last");
                                        FlexType material = FlexTypeCache.getFlexTypeRoot("Material");
                                        FlexType media = FlexTypeCache.getFlexTypeRoot("Media");
                                        FlexType orderConfirmation = FlexTypeCache.getFlexTypeRoot("Order Confirmation");
                                        FlexType orderConfirmationProdType = FlexTypeCache.getFlexTypeFromPath(PRODUCT_ORDER_CONFIRMATION_ROOT_TYPE);
                                        FlexType palette = FlexTypeCache.getFlexTypeRoot("Palette");
                                        FlexType product = FlexTypeCache.getFlexTypeRoot("Product");
                                        FlexType rfq = FlexTypeCache.getFlexTypeRoot("RFQ");
                                        FlexType sample = FlexTypeCache.getFlexTypeRoot("Sample");
                                        FlexType season = FlexTypeCache.getFlexTypeRoot("Season");
                                        FlexType supplier = FlexTypeCache.getFlexTypeRoot("Supplier");
                                        FlexType prodSampleType = FlexTypeCache.getFlexTypeFromPath(PRODUCT_ROOT_TYPE);
                                        FlexType matSampleType = FlexTypeCache.getFlexTypeFromPath(MATERIAL_ROOT_TYPE);

                                        boolean hasSupplierLinks = false;

                                        if(lcsContext.isVendor){

                                            for(Iterator vendorObjsItr = lcsContext.getVendorObjects().iterator();vendorObjsItr.hasNext(); ){
                                                    if(hasSupplierLinks){
                                                            break;
                                                    }else if(vendorObjsItr.next().toString().indexOf("LCSSupplier:") > -1){
                                                            hasSupplierLinks = true;
                                                    }
                                            }
                                        }



                                        if(!lcsContext.isVendor){%>
                                            <%if(INDEX_ENABLED){%><option value="Keyword"><%=allOptionLabel%></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(businessObject)){%><option value="BusinessObject"><%= FormatHelper.formatHTMLString(businessObject.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(changeActivity)){%><option value="ChangeActivity"><%= FormatHelper.formatHTMLString(changeActivity.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(product)){%><option value="Sku"><%= FormatHelper.formatHTMLString(skuLabel) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(country)){%><option value="Country"><%= FormatHelper.formatHTMLString(country.getTypeDisplayName()) %></option><%}%>
                                        <%}%>

                                        <%if(ACLHelper.hasViewAccess(color)){%><option value="Color"><%= FormatHelper.formatHTMLString(color.getTypeDisplayName()) %></option><%}%>
                                        <%if(ACLHelper.hasViewAccess(document)){%><option value="Document"><%= FormatHelper.formatHTMLString(document.getTypeDisplayName()) %></option><%}%>


                                        <%if(!lcsContext.isVendor){%>
                                            <%if(ACLHelper.hasViewAccess(documentCollection)){%><option value="DocumentCollection"><%= FormatHelper.formatHTMLString(documentCollection.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(last)){%><option value="Last"><%= FormatHelper.formatHTMLString(last.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(material)){%><option value="Material"><%= FormatHelper.formatHTMLString(material.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(media)){%><option value="Media"><%= FormatHelper.formatHTMLString(media.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(orderConfirmation)){%><option value="OrderConfirmation"><%= FormatHelper.formatHTMLString(orderConfirmation.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(palette)){%><option value="Palette"><%= FormatHelper.formatHTMLString(palette.getTypeDisplayName()) %></option><%}%>
											<%if(ACLHelper.hasViewAccess(product)){%> 
																					<option value="Placeholder"><%= LCSMessage.getHTMLMessage(RB.PLACEHOLDER, "placeholder_LBL", RB.objA) %></option>
																					<option selected value="Product"><%= FormatHelper.formatJavascriptString(product.getTypeDisplayName()) %></option>
																				<%
																					// Agron Custom code starts
																					Iterator quickSearchAttsIter = quickSearchAtts.iterator();
																						while(quickSearchAttsIter.hasNext())
																						{
																							FlexTypeAttribute att = (FlexTypeAttribute) quickSearchAttsIter.next();
																						%>
																						<option selected value="<%= att.getAttObjectLevel() + "_" + att.getAttKey()%>">
																							<%= FormatHelper.formatScopeLevelLabel(att.getAttObjectLevel()) %> (<%= att.getAttDisplay(true)%>)
																						</option>
																						<%
																						}
																				// Agron Custom code ends
																				%>
																					<%}%>
										   <%if(ACLHelper.hasViewAccess(rfq)){%> <option value="RFQ"><%= LCSMessage.getHTMLMessage(RB.RFQ, "rfq_LBL", RB.objA) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(sample)){%><option value="Sample"><%= FormatHelper.formatHTMLString(sample.getTypeDisplayName()) %></option><%}%>
                                           <%if(ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Sample")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Material"))){%>
                                                <%if(ACLHelper.hasViewAccess(matSampleType)){%><option value="Sample/Material"><%= FormatHelper.formatHTMLString(matSampleType.getFullNameDisplay(true)) %></option><%}%>
                                            <%}%>
                                           <%if(ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Sample")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product"))){%>
                                                <%if(ACLHelper.hasViewAccess(prodSampleType)){%><option value="Sample/Product"><%= FormatHelper.formatHTMLString(prodSampleType.getFullNameDisplay(true)) %></option><%}%>
                                            <%}%>
                                        <%}else{%>

                                           <%if(USE_MATERIAL &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Material")) && hasSupplierLinks) {%>
                                                <%if(ACLHelper.hasViewAccess(material)){%><option value="Material"><%= FormatHelper.formatHTMLString(material.getTypeDisplayName())%></option><%}%>
                                           <%}%>

                                           <%if(USE_ORDERCONF &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Order Confirmation")) && hasSupplierLinks) {%>
                                                <%if(ACLHelper.hasViewAccess(orderConfirmation)){%><option value="OrderConfirmation/Product"><%= FormatHelper.formatHTMLString(orderConfirmationProdType.getFullNameDisplay(true)) %></option><%}%>
                                           <%}%>

                                           <%if(USE_PRODUCT &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product"))  && hasSupplierLinks) {%>
                                                <%if(ACLHelper.hasViewAccess(product)){%> <option selected value="Product"><%= FormatHelper.formatHTMLString(product.getTypeDisplayName()) %></option>
												<%
															// Agron Custom code starts
																					Iterator quickSearchAttsIter = quickSearchAtts.iterator();
																					while(quickSearchAttsIter.hasNext())	
																						{
																							FlexTypeAttribute att = (FlexTypeAttribute) quickSearchAttsIter.next();
																						 %>
																						<option selected value="<%= att.getAttObjectLevel() + "_" + att.getAttKey()%>">
																							<%= FormatHelper.formatScopeLevelLabel(att.getAttObjectLevel()) %> (<%= att.getAttDisplay(true)%>)
																						</option>
																						<%}
																						 
												%>
																					<%}%>
                                           <%}%>
										   
										   <%if(USE_PRODUCT &&  ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product"))  && hasSupplierLinks) {%>
                                                <%if(ACLHelper.hasViewAccess(product)){%><option value="Sku"><%= FormatHelper.formatHTMLString(skuLabel) %></option><%}%>
                                           <%}%>

                                          <%if(ENABLE_RFQ && USE_RFQ && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("RFQ")) && hasSupplierLinks) {%>
                                                <%if(ACLHelper.hasViewAccess(rfq)){%> <option value="RFQ"><%= LCSMessage.getHTMLMessage(RB.RFQ, "rfq_LBL", RB.objA) %></option><%}%>
                                           <%}%>

                                           <%if(USE_SAMPLE && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Sample")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Material"))){%>
                                                <%if(ACLHelper.hasViewAccess(matSampleType)){%><option value="Sample/Material"><%= FormatHelper.formatHTMLString(matSampleType.getFullNameDisplay(true)) %></option><%}%>
                                            <%}%>

                                           <%if(USE_SAMPLE && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Sample")) && ACLHelper.hasViewAccess(FlexTypeCache.getFlexTypeRoot("Product"))){%>
                                                <%if(ACLHelper.hasViewAccess(prodSampleType)){%><option value="Sample/Product"><%= FormatHelper.formatHTMLString(prodSampleType.getFullNameDisplay(true)) %></option><%}%>
                                            <%}%>
                                         <%}%>

                                       <%if(!lcsContext.isVendor){%>
                                            <%if(ACLHelper.hasViewAccess(season)){%><option value="Season"><%= FormatHelper.formatHTMLString(season.getTypeDisplayName()) %></option><%}%>
                                            <%if(ACLHelper.hasViewAccess(supplier)){%><option value="Supplier"><%= FormatHelper.formatHTMLString(supplier.getTypeDisplayName()) %></option><%}%>
                                        <%}%>
                         </select>

                          <div id="sinceChangesWidget">

                          <%
                          if(!daysDisplayMap.isEmpty()){ %>

                                    <div id="sinceChangesDateOptionDiv" style="float:right;width=120px;margin-right:0px">

                                    <%= fg.createDropDownList(daysDisplayMap, "daysValueId", showChangeSinceDefault, "setShowChangeSince()", 1, false, "",order,null,false,"setOnMouseDown()", "setOnMouseOut()")  %>

                                    </div>
                                    <div id="sinceChangesInfoDiv" align="right" >
                                        <div id="showChangeLabel"><%= showChangeSinceLabel %></div>
                                        <div id="showChangeSinceDiv"></div>
                                    </div>
                          </div>

                  <%} %>

                     </div>
  </div>

</div>

<% if(!skipTemplateEnds){ %>
<jsp:include page="<%=subURLFolder+ STANDARD_TEMPLATE_FOOTER %>" flush="true" />
<% } %>

<script language="Javascript">

var daysValueMap = <%= JSONHelper.toJSONMap(daysMap) %>;

var daysDD = $("daysValueId");
var preIndex = daysDD.selectedIndex;
if(daysDD){
    var defaultVal = '<%=defaultDaysValue%>';
    if(hasContent(defaultVal)){
        setShowChangeSince(defaultVal);
        for(var i=0;i<daysDD.options.length;i++){
            if(daysDD.options[i].value==defaultVal){
                daysDD.options[i].selected='selected';
                break;
            }
        }
    }
}

function  setOnMouseDown() {
    if (document.MAINFORM.daysValueId.selectedIndex != -1) {
       preIndex = document.MAINFORM.daysValueId.selectedIndex;
    }
    daysDD.selectedIndex = -1;
}

var isChrome = navigator.userAgent.indexOf("Chrome") > -1;
var isSafari = navigator.userAgent.indexOf("Safari") > -1;
var isFirefox = navigator.userAgent.indexOf("Firefox") > -1;

function setOnMouseOut() {
    if (!isChrome && !isSafari || preIndex == -1 || daysDD.options[preIndex].value != 'selectDate') {
        daysDD.selectedIndex = preIndex;
    }
}

if (daysDD.addEventListener) { // all browsers except IE before version 9
    daysDD.addEventListener('blur', resetShowChangesSinceSelection, false);
} else if (daysDD.attachEvent) { // IE before version 9
    daysDD.attachEvent('onblur', resetShowChangesSinceSelection);
}

function resetShowChangesSinceSelection() {
    if (daysDD.selectedIndex == -1) {
        daysDD.selectedIndex = preIndex;
    }
}

function setShowChangeSince(defaultVal){
    var daysVal;
    if(hasContent(defaultVal)){
        daysVal = defaultVal;
    }else{
        daysVal = daysValueMap[document.MAINFORM.daysValueId.value];
    }
    if (document.MAINFORM.daysValueId.selectedIndex == -1) {
        document.MAINFORM.daysValueId.selectedIndex = preIndex;
        if(daysVal == null){
            daysVal = daysValueMap[document.MAINFORM.daysValueId.value];
        }
    }
    if(daysVal=='SelectDate'){
        CalendarPopup();
    }else if(isNumber(daysVal)){
        var sinceDate = new Date();
        sinceDate.setDate(sinceDate.getDate()-parseInt(daysVal));
        sinceDate.setHours(0);
        sinceDate.setMinutes(1);

        $("showChangeSinceDiv").innerHTML = formatDateString(sinceDate, "<%= WTMessage.getLocalizedMessage ( RB.DATETIMEFORMAT, "jsCalendarFormat", RB.objA ) %>") ;
        changeSelectedDateHiddenValue(sinceDate);
    }

    preIndex = document.MAINFORM.daysValueId.selectedIndex;

}

function changeSelectedDateHiddenValue(dateVal){
        curDateStr = dateVal.print('<%=jsCalendarFormat%>');
        var globalHidden = window.parent.frames['contentframe'].document.getElementById('globalChangeTrackingSinceDate');
         if(globalHidden) {
                globalHidden.value=curDateStr;
         }
         var sideBarHidden = window.parent.frames['sidebarframe'].document.getElementById('globalChangeTrackingSinceDate');
         if(sideBarHidden) {
                sideBarHidden.value=curDateStr;
         }

}

function setSelectedDateCallback(dateString){
        var sinceDate = chkDateString(dateString,
                   '<%= FormatHelper.formatJavascriptString(jsCalendarInputFormat, false) %>',
                   '<%= jsCalendarFormat %>',
                   true);
        sinceDate.setHours(0);
        sinceDate.setMinutes(1);

        $("showChangeSinceDiv").innerHTML = formatDateString(sinceDate, "<%= WTMessage.getLocalizedMessage ( RB.DATETIMEFORMAT, "jsCalendarFormat", RB.objA ) %>") ;
        changeSelectedDateHiddenValue(sinceDate);
}

function CalendarPopup(){
    var additionalParams='?activity=POPUP_CALENDAR&action=INIT';
    flexWindowOpenAsPost('<%= PageManager.getContextOverrideUrlAndMasterContorller(true)%>' + additionalParams ,'chooser','dependent=yes,width=290,height=200,top=100,left=100,titlebar=yes,resizable=no');
}


var curDateStr;
function getGlobalChangeTrackingSinceDate() {
    return curDateStr;
}

var ajaxUpdater;
var refreshFreq = <%=refreshDiscussionInboxFrequency%>;

function showDiscussionSubscription() {

    ajaxUpdater = new Ajax.PeriodicalUpdater({success: "discussionCountLabel", failure: "discussionCountLabel"},  "<%= PageManager.getContextOverrideUrlAndMasterContorller(true)%>", {
        parameters : "activity=QUERY_DISCUSSION_INBOX&templateType=AJAX&isSystemFunctionCall=true",
        method: 'get', frequency:refreshFreq, decay: 0});
}

/**
 *
 *Function to process events from the QuickLink menu
 *
 */
function gotoQuickLink()
{
        var selectedIndex  = $('quickLinkSelectionId').selectedIndex;
        var selectValue = $('quickLinkSelectionId').options[selectedIndex].value;
        switch (selectValue)
        {
            case "showGoToWindchill":

                window.top.location.href = "<%=WindchillContext%>/app/#ptc1/homepage";
                $('quickLinkSelectionId').selectedIndex = 0;
                break;


            case "showHelp":

                OHCPopupPage();
                $('quickLinkSelectionId').selectedIndex = 0;
                break;

            case "showAbout":

                viewAbout();
                $('quickLinkSelectionId').selectedIndex = 0;
                break;

            case "showClipboard":

                viewClipboard();
                $('quickLinkSelectionId').selectedIndex = 0;
                break;

            case "showPreferences":

                PreferencesPopupPage();
                $('quickLinkSelectionId').selectedIndex = 0;
                break;

            case "logout":

                logOut();
                $('quickLinkSelectionId').selectedIndex = 0;
                break;

            <% if(lcsContext.inGroup(adminGroup.toUpperCase()) || lcsContext.inGroup("DEVELOPERS")){
                %>
                case "showLibrary":
                    libraryPopupPage();
                    $('quickLinkSelectionId').selectedIndex = 0;
                    break;
                    <%
                }%>

            default:
                $('quickLinkSelectionId').selectedIndex = 0;
                // do nothing
        }


        return true;
}




// Catch the onresize event to show/hide content
window.onresize = function() {

    showHideWidgetsWithWidth();
 };


 // Hide parts of the header whenever the user collapses the windows width to avoid widgets disorderly collapsing.
function showHideWidgetsWithWidth()
{
    var allControlsWidth = 1170;
    var onlyQuickLinksWidth = 620;
    var leftWelcomeSectionWidth = "400px";

    if( $('discussionForumImg') )
    {
        leftWelcomeSectionWidth = "430px";
        allControlsWidth = 1205;
    }
    var windowWidth = getWidth();

    //window is wide enough to show all controls
    if (windowWidth >= allControlsWidth )
    {
        //$('headerMask').style.display="block";
        $('showSearchSection').style.display="block";
        $('sinceChangesWidget').style.display="block";
        $('sinceChangesInfoDiv').style.display="block";
        $('searchDropDown').style.display="block";

        if (isChrome) {
            $('showSearchSection').style.minWidth = "765px";
        } else if (isFirefox){
            //firefox on mac os x - even English is wider
            $('showSearchSection').style.minWidth = "820px";
        } else {
            $('showSearchSection').style.minWidth = "760px";
        }
    }
    //window is not wide enough for all controls, only quick links
    else if (windowWidth >= onlyQuickLinksWidth )
    {
        //$('headerMask').style.display="block";
        $('showSearchSection').style.display="block";
        $('sinceChangesWidget').style.display="none";
        $('searchDropDown').style.display="none";
        $('showSearchSection').style.minWidth = "220px";
    }
    else
    {
        //window is not wide enough to show any controls - hide all
        //$('headerMask').style.display="none";
        $('showSearchSection').style.display="none";
    }
    $('leftWelcomeSection').style.minWidth = leftWelcomeSectionWidth;


    <%
    /*
    //OLD code include here for reference
    (Enclosing in scriptlet so that the comment will not be dumped into the
    JS rendered by the browser)
    //With the new minwidth changes (SVN: 45645) the below code is not required
    //including it here just in case. This code is in SVN revision:45644
    var zoomLevel = 1;
    if(isSafari || isChrome) {

        zoomLevel = (Math.round(1000 *(window.outerWidth) / window.innerWidth)/1000);
        if ((zoomLevel < 1) && (zoomLevel > 0.75)) {
            $('showSearchSection').style.width = "720px";
        } else if ((zoomLevel <= 0.75) && (zoomLevel > 0.6)) {
            $('showSearchSection').style.width = "740px";
        } else if ((zoomLevel <= 0.6) && (zoomLevel > 0.5)) {
            $('showSearchSection').style.width = "800px";
        } else if ((zoomLevel <= 0.5) && (zoomLevel > 0.4)) {
            $('showSearchSection').style.width = "950px";
        } else if ((zoomLevel <= 0.4) && (zoomLevel > 0.3)) {
            $('showSearchSection').style.width = "1050px";
        } else if (zoomLevel <= 0.3) {
            $('showSearchSection').style.width = "1350px";
        }
    }


    if (isFirefox) {
        zoomLevel = (Math.round(1000 * mediaQueryBinarySearch('min--moz-device-pixel-ratio','', 0, 10, 20, .0001)) / 1000);
        if ((zoomLevel < 1) && (zoomLevel > 0.75)) {
            $('showSearchSection').style.width = "720px";
        } else if ((zoomLevel <= 0.75) && (zoomLevel > 0.6)) {
            $('showSearchSection').style.width = "740px";
        } else if ((zoomLevel <= 0.6) && (zoomLevel > 0.3)) {
            $('showSearchSection').style.width = "780px";
        } else if (zoomLevel <= 0.3) {
            $('showSearchSection').style.width = "850px";
        }
    }
    */
    %>
}

function mediaQueryMatches(property, r) {
    var styles = document.createElement('style');
    document.getElementsByTagName("head")[0].appendChild(styles);

    var dummyElement = document.createElement('div');
    dummyElement.innerHTML="test";
    dummyElement.id="mq_dummyElement";
    document.body.appendChild(dummyElement);

    styles.sheet.insertRule('@media('+property+':'+r+'){#mq_dummyElement{text-decoration:underline}}', 0);
    var matched = getComputedStyle(dummyElement, null).textDecoration == 'underline';
    styles.sheet.deleteRule(0);
    document.body.removeChild(dummyElement);
    document.getElementsByTagName("head")[0].removeChild(styles);
    return matched;
}

function mediaQueryBinarySearch(property, unit, a, b, maxIter, epsilon) {
    var mid = (a + b)/2;
    if (maxIter == 0 || b - a < epsilon) return mid;
    if (mediaQueryMatches(property, mid + unit)) {
        return mediaQueryBinarySearch(property, unit, mid, b, maxIter-1, epsilon);
    } else {
        return mediaQueryBinarySearch(property, unit, a, mid, maxIter-1, epsilon);
    }
}


  // Get the screen width and height. Any browser.
  function getWidth()
  {
    xWidth = null;
    if(window.screen != null)
      xWidth = window.screen.availWidth;

    if(window.innerWidth != null)
      xWidth = window.innerWidth;

    if(document.body != null)
      xWidth = document.body.clientWidth;

    return xWidth;
  }

  function getHeight() {
      xHeight = null;
      if(window.screen != null)
        xHeight = window.screen.availHeight;

      if(window.innerHeight != null)
        xHeight =   window.innerHeight;

      if(document.body != null)
        xHeight = document.body.clientHeight;

      return xHeight;
    }


   // Show discussion if enabled
   <%if(FORUMS_ENABLED){%>
    showDiscussionSubscription();
    <%}%>

  function toggleAppSwitcher(){
    var frm = window.parent.document.getElementById('sidebarframe');
    if(frm.scrollWidth==30){
        toggleSideBar();
    }
    parent.frames['sidebarframe'].showAppSwitcher();

  }


  // Do once the document is ready. (initialization)
  document.observe("dom:loaded", function() {
      // initially hide all containers for tab content
      showHideWidgetsWithWidth();

      $('daysValueId').tabIndex="5";

      $('searchDropDownSelect').tabIndex="6";

      $('searchField').tabIndex="7";
      $('searchButton').tabIndex="8";
      $('newObjectIcon').tabIndex="9";

      $('quickLinkSelectionId').tabIndex="10";
    });

</script>

</body>

</html>
