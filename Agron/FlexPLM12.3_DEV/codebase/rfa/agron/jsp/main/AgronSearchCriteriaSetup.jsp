<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.db.FlexObject,
                com.lcs.wc.classification.*,
                com.lcs.wc.client.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.product.*,
                com.lcs.wc.util.*,
                com.lcs.wc.flextype.*,
                wt.util.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.report.*,
                wt.lifecycle.*,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    public static boolean INDEX_ENABLED = false; 
    static {
        try {
            WTProperties props = WTProperties.getLocalProperties();
            INDEX_ENABLED = props.getProperty("wt.index.enabled", false);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
%>
<%
String searchLbl  = WTMessage.getLocalizedMessage ( RB.MAIN, "search_Btn", RB.objA ) ;
String typeGrpTle = WTMessage.getLocalizedMessage ( RB.MAIN, "type_LBL", RB.objA ) ;
String criteriaGrpTle = WTMessage.getLocalizedMessage ( RB.MAIN, "criteria_GRP_TLE", RB.objA ) ;
String searchButton = WTMessage.getLocalizedMessage ( RB.MAIN, "search_Btn", RB.objA ) ;
String youMustSelectAViewToUpdateAlrt = WTMessage.getLocalizedMessage ( RB.MAIN, "youMustSelectAViewToUpdate_ALRT",RB.objA ) ;
String youMustSelectAFilterToUpdateAlrt = WTMessage.getLocalizedMessage ( RB.MAIN, "youMustSelectAFilterToUpdate_ALRT",RB.objA ) ;
String nameLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "name_LBL",RB.objA ) ;
String asteriskIllegalMessage = WTMessage.getLocalizedMessage ( RB.MAIN, "asteriskIllegal_MSG",RB.objA ) ;
	
    String module = request.getParameter("module");
    String searchScope = request.getParameter("searchScope");
    String searchLevel = request.getParameter("searchLevel");
    String searchLabel = request.getParameter("searchLabel");
    String activity = request.getParameter("activity");
    String typeclass = request.getParameter("typeclass");
    String searchActivity = request.getParameter("searchActivity");
    String type = FormatHelper.format(request.getParameter("type"));
    String lastType = FormatHelper.format(request.getParameter("lastType"));
    String rootTypeId = FormatHelper.format(request.getParameter("rootTypeId"));
    String nameAttribute = FormatHelper.format(request.getParameter("nameAttribute"));
    String displayAttribute = FormatHelper.format(request.getParameter("displayAttribute"));
    String plugIn = FormatHelper.format(request.getParameter("additionalPlugIn"));
    boolean chooser = FormatHelper.parseBoolean(request.getParameter("chooser"));
    boolean multiple = FormatHelper.parseBoolean(request.getParameter("multiple"));
    boolean skipChildFlexTypes = FormatHelper.parseBoolean(request.getParameter("skipChildFlexTypes"));
    String parentPaletteId = FormatHelper.format(request.getParameter("parentPaletteId"));
    FlexType childFlexType = (FlexType)request.getAttribute("childFlexType");
   	String searchType = "";

	String ajaxWindow = "false";

	if(FormatHelper.hasContent(request.getParameter("ajaxWindow"))) {
		 ajaxWindow =request.getParameter("ajaxWindow");
	}
	String formName = "MAINFORM";

	String defaultCriteriaDivId = "defaultCriteria_div";
	String criteriaDisplayId    = "criteriaDisplay";
	if(ajaxWindow.equals("true")){
		formName = "AJAXFORM";
		defaultCriteriaDivId = "defaultAjaxCriteria_div";
		criteriaDisplayId    = "criteriaAjaxDisplay";
	}

   /*This Search Purpose indicates that we want to search t view or edit.
   If we want to search to edit, need to check for Edit, Delete
   */

   //We need this block, because the pages that call this do not all use the parameter to pass in the type info.
   if ("MaterialSupplier".equalsIgnoreCase(searchLabel) ) {
	  searchType = WTMessage.getLocalizedMessage ( RB.MATERIALSUPPLIER, "materialSupplier_LBL", RB.objA ) ;
   } else if("Placeholder".equalsIgnoreCase(searchLabel)){
      searchType = WTMessage.getLocalizedMessage ( RB.PLACEHOLDER, "placeholder_LBL", RB.objA ) ;
   } else {
	  if(FormatHelper.hasContent(type)) {
		 searchType =FlexTypeCache.getFlexType(type).getTypeDisplayName();
	  } else {
		 if (FormatHelper.hasContent(rootTypeId) ) {
			searchType = FlexTypeCache.getFlexType(rootTypeId).getTypeDisplayName();
		 } else {
			searchType =FlexTypeCache.getFlexTypeRootByClass(typeclass).getTypeDisplayName();
		 }
	  }
   }
Object[] objsearchType = {searchType };
String findPgHead  = WTMessage.getLocalizedMessage ( RB.MAIN, "findObject_LBL", objsearchType ) ;

    String typesPrefix = request.getParameter("typesPrefix");

    if(FormatHelper.hasContent(searchScope)){
      flexg.setScope(searchScope);
    }
    if(FormatHelper.hasContent(searchLevel)){
      flexg.setLevel(searchLevel);
    }


    FlexType flexType = null;
    FlexTypeClassificationTreeLoader loader  = new FlexTypeClassificationTreeLoader(typeclass);
    loader.constructTree();

    String rootId = ((TreeNode)loader.getRootNode().getChildren().iterator().next()).getId();
    if(FormatHelper.hasContent(rootTypeId)){
        rootId = FormatHelper.getNumericFromOid(rootTypeId);
    }
    if(!FormatHelper.hasContent(type)){
        type = rootId;
    }
    flexType = FlexTypeCache.getFlexType(type);
	type = FormatHelper.getObjectId(flexType);


    String stKey = ((wt.org.WTUser)wt.session.SessionHelper.manager.getPrincipal()).getAuthenticationName()
                                                + "~ChooserPurpose";
    String stPurpose = (String)session.getAttribute(stKey);

    //For now, this code is a hack for a temp patch for CATO, and I want it to apply to LCSMeasurements only
    if (! "com.lcs.wc.measurements.LCSMeasurements".equals(typeclass))
        stPurpose = "";

    /*
    A Search Screen can be used to search fo objects to view, edit or create.
    We must search for the appropriate permission
    */
    if(  (!ACLHelper.hasCreateAccess (flexType) && "CREATE".equals(stPurpose) )
       ||(!ACLHelper.hasEditAccess (flexType)   && "EDIT".equals(stPurpose))
       ||!ACLHelper.hasViewAccess(flexType)){
        String stMessageKey = (stPurpose == null || "".equals(stPurpose))?"noAccessToViewThisType_MSG":"youDoNotHaveCreatePermForThisType_MSG";
        throw new LCSAccessException(RB.EXCEPTION, stMessageKey,RB.objA);
    }
	String typeStringDef = request.getParameter("typeStringDef");
	String types = "";
	if(FormatHelper.hasContent(typeStringDef)) {
	  types = typeStringDef;
	} else {
	  types = typesPrefix + (FormatHelper.getObjectId(flexType));
	}
    String nodeName = "node_" + FormatHelper.getNumericFromOid(FormatHelper.getObjectId(flexType));
    String firstFieldName = "nothing";

    String searchterm = "";
    if (FormatHelper.hasContent(request.getParameter("indexSearchKeyword"))) {
        searchterm = request.getParameter("indexSearchKeyword");
    }    
%>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/ajax.js"></script>

<%@ include file="/rfa/jsp/reports/ViewLookup.jspf" %>
<%!

	public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static String toTree(TreeNode node, String parent, String activeNode, FlexType childFlexType, String module) {
        StringBuffer buffer = new StringBuffer();
        boolean active = false;
        String nodeName = "node_" + node.getId();
        if(nodeName.equals(activeNode)){ active = true; }
		try{
			FlexType type = FlexTypeCache.getFlexType(node.getId());
			buffer.append("var " + nodeName + " = " + parent + ".addChild('', '" + FormatHelper.encodeAndFormatForHTMLContent(node.getName()) + "', 'javascript:chooseType(\"" + FormatHelper.getObjectId(type) +"\")', '', " + parent + ", " + active + ");\n");
			Iterator childNodes = SortHelper.sort(node.getChildren(), "name").iterator();
			TreeNode child = null;
			while(childNodes.hasNext()){
				child = (TreeNode) childNodes.next();
				FlexType childType = FlexTypeCache.getFlexType(child.getId() );
				if("TEMPLATESIZEDEFINITION".equals(module)){
					if(!ACLHelper.hasCreateAccess(childType)){
						continue;
					}
				}
				if(	(childFlexType == null) 
					||(childFlexType.isAncestorType(childType ) || childFlexType.equals(childType))
							
					){
					buffer.append(toTree(child, "node_" + node.getId(), activeNode, childFlexType, module));
				}
			}
        } catch(WTException we){
        	we.printStackTrace();
        }
        return buffer.toString();
    }

%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
   var divHash = new Array();
</script>

<script>
	var urlContext_masterControllerURL = '<%= PageManager.getContextOverrideUrlAndMasterContorller(true) %>';

   function search(){
        // If Index Search is enabled and the criteria is valid
        if (document.<%=formName%>.indexSearchKeyword) {
             if (validate_keyword()) {
                if (document.<%=formName%>.currentDiv.value != '<%=defaultCriteriaDivId%>') {
                    defaultCriteria_div.innerHTML = '';
                }
                if(validate()){
                    document.<%=formName%>.activity.value = '<%= FormatHelper.encodeForJavascript(searchActivity) %>';
                    document.<%=formName%>.action.value = 'SEARCH';
                    document.<%=formName%>.type.value = '<%= type %>';
                    setSearchCriteria();
                    if(window.sessionStorage){
       			      sessionStorage.setItem("RETURN_TO_SEARCH_oid", null);
       			    }else{
       			      CookieManager.setCookie({name:'RETURN_TO_SEARCH_oid',value:null});
       			     }
                      submitForm();
                }
             }
        } else {
                if (document.<%=formName%>.currentDiv.value != '<%=defaultCriteriaDivId%>') {
                    defaultCriteria_div.innerHTML = '';
                }
                if(validate()){
                    document.<%=formName%>.activity.value = '<%= FormatHelper.encodeForJavascript(searchActivity) %>';
                    document.<%=formName%>.action.value = 'SEARCH';
                    document.<%=formName%>.type.value = '<%= type %>';
                    setSearchCriteria();
                  if(window.sessionStorage){
       			     sessionStorage.setItem("RETURN_TO_SEARCH_oid", null);
       			  }else{
       			     CookieManager.setCookie({name:'RETURN_TO_SEARCH_oid',value:null});
       			  }
                    submitForm();
                }
        }
    }

   function setSearchCriteria(){
       var windowNameString = window.name;
       if( windowNameString != 'sidebarframe' && windowNameString != 'headerframe' && windowNameString != 'contentframe'){
           var c = getAllParametersForReturnToSearch();    
           c = c+"&type="+'<%= type %>';
           if(window.sessionStorage){
               sessionStorage.setItem("RETURN_TO_SEARCH_activity", '<%= FormatHelper.encodeForJavascript(searchActivity) %>');
               sessionStorage.setItem("RETURN_TO_SEARCH_action", 'SEARCH');
               sessionStorage.setItem("RETURN_TO_SEARCH_params", c); 
               sessionStorage.setItem("RETURN_TO_SEARCH_view", "true");
               
           }else{
               CookieManager.setCookie({name:'RETURN_TO_SEARCH_activity', value:document.<%=formName%>.activity.value});
               CookieManager.setCookie({name:'RETURN_TO_SEARCH_action', value:document.<%=formName%>.action.value});
               CookieManager.setCookie({name:'RETURN_TO_SEARCH_params',value:c});
               CookieManager.setCookie({name:'RETURN_TO_SEARCH_view',value:"true"})
               
           }       
       }
   }
    function validate_keyword() {
        if (trim(document.<%=formName%>.indexSearchKeyword.value) == "*") {
            alert('<%=FormatHelper.encodeForJavascript(asteriskIllegalMessage)%>');
            return false;
        } 
        return true;
    }

    // CODE WHICH SUBMITS THE FORM ON AN ENTER KEY PRESS
    document.body.onkeypress = enterKey;
    function enterKey(evt) {
      var is = new Is();
      var evt = (evt) ? evt : event

	  // IF ELEMENT TRIGGERING EVENT IS THE SEARCH BUTTON THEN EXECUTE SEARCH
      var el = (evt.srcElement)?evt.srcElement : evt.target;
      if (el.id == 'SearchButton1' || el.id == 'SearchButton2') {
         search();
         return;
      }

      // OTHERWISE SIMPLY SET FOCUS TO THE SEARCH BUTTON
      // THIS ALLOWS THE ONBLUR OF AN INPUT FIELD WITH FOCUS TO FIRE
      // ON THE ENTER KEY PRESS
      var charCode = (evt.which) ? evt.which : evt.keyCode
      if (charCode == 13) { 
		if(activeSelectorRow) selectSelectorValue(activeSelectorRow);
		  // enter key on PC, return key on Mac
        var sb = document.getElementById('SearchButton2').focus();
      } else if (is.mac){
        if (charCode == 3) { // enter key on Mac
            document.getElementById('SearchButton2').focus();
        }
      }
    }

    function validate(){
        if(document.<%=formName%>.currentDiv.value == '<%=defaultCriteriaDivId%>'){
        <%= flexg.generateFormValidation(flexType, true) %>
        }
        return true;
    }

    function chooseType(id){
        document.<%=formName%>.activity.value = '<%= FormatHelper.encodeForJavascript(searchActivity) %>';
        document.<%=formName%>.action.value = 'CHANGE_TYPE';

        document.<%=formName%>.returnAction.value = document.<%=formName%>.action.value;

        document.<%=formName%>.fromIndex.value = '';
        document.<%=formName%>.toIndex.value = '';
        document.<%=formName%>.mode.value = '';
        document.<%=formName%>.showAll.value = '';
        document.<%=formName%>.type.value = id;
        if(document.<%=formName%>.viewId){
			if(document.<%=formName%>.viewId.tagName == 'SELECT'){
				removeAllOptions(document.<%=formName%>.viewId);
			}else{
				document.<%=formName%>.viewId.value = '';
			}
        }
        if(document.<%=formName%>.filterId){
			if(document.<%=formName%>.filterId.tagName == 'SELECT'){
				removeAllOptions(document.<%=formName%>.filterId);
			}else{
				document.<%=formName%>.filterId.value = '';
			}
        }

        submitForm();
    }

    function pickNewType(){
        document.<%=formName%>.activity.value = '<%= FormatHelper.encodeForJavascript(searchActivity) %>';
        document.<%=formName%>.action.value = 'INIT';
        document.<%=formName%>.type.value = '';
        document.<%=formName%>.viewId.value = '';
        submitForm();
    }

   function doLoad() {
        objTree.buildDOM($("treeDiv"));
    }

    function changeView(selectList){
        //don't do anything
    }

    function newView(){

        document.<%=formName%>.returnActivity.value =   document.REQUEST.activity;
        document.<%=formName%>.activity.value = 'CREATE_REPORT_VIEW';
        document.<%=formName%>.action.value = 'INIT';
        document.<%=formName%>.type.value = '<%= FormatHelper.getObjectId(flexType) %>';
        document.<%=formName%>.returnAction.value = document.REQUEST.action;
        submitForm();
    }

    function updateView(){
        if(document.<%=formName%>.viewId.value == null || document.<%=formName%>.viewId.value == '' || document.<%=formName%>.viewId.value == ' '){
            alert('<%= FormatHelper.encodeForJavascript(youMustSelectAViewToUpdateAlrt)%>');
            return;
        }
        document.<%=formName%>.returnActivity.value =   document.REQUEST.activity;
        document.<%=formName%>.returnAction.value = document.REQUEST.action;

        document.<%=formName%>.activity.value = 'UPDATE_REPORT_VIEW';
        document.<%=formName%>.action.value = 'INIT';
        document.<%=formName%>.oid.value=document.<%=formName%>.viewId.value;
        submitForm();
    }

    function changeFilter(selectList){
        
        var divName = '<%=defaultCriteriaDivId%>';
        if(selectList.value.indexOf('FiltersList') > -1){
            divName = selectList.value + '_div';
        }
        document.<%=formName%>.currentDiv.value = divName;

        if(hasContent(divHash[divName])){
            switchDisplayDiv(divName);
        }
        else{
            if(selectList.value.indexOf('FiltersList') > -1){
                runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterControllerURL, 'filterAction=CLEAR&activity=FIND_REPORT_FILTER&action=AJAXSEARCH&filterId' + "=" + selectList.value, 'loadFilterToPage');
            }
            else{
                switchDisplayDiv('<%=defaultCriteriaDivId%>');
            }
        }
    }

    function loadFilterToPage(xml, text){
        //alert("xml: " + xml);
        //alert("text: " + text);
        if(hasContent(text)){
            var divName = document.<%=formName%>.currentDiv.value;
            divHash[divName] = text;
            switchDisplayDiv(divName);
            parseScripts(text);
        }
    }

    function switchDisplayDiv(divName){
        criteriaDisplay.innerHTML = divHash[divName];
        parseCalendarTags(criteriaDisplay.innerHTML);
    }

	<%if(ajaxWindow.equals("false")){%>
    function parseCalendarTags(ihtml){
        var sBegin = "Calendar.setup";
        var sEnd = ")";

        var beginTag = ihtml.indexOf(sBegin);
        if(beginTag < 0) return;

        ihtml = ihtml.substring(beginTag);
        ihtml =replaceAll(ihtml,'&quot;','"')
        var endTag = ihtml.indexOf(sEnd);

        var evalStr = ihtml.substring(0, endTag)+sEnd;
        eval(evalStr);

        var newBegin = endTag +1;
        ihtml = ihtml.substring(newBegin);
        parseCalendarTags(ihtml);
    }
	<%}%>


    function newFilter(){
        document.<%=formName%>.returnActivity.value =   document.REQUEST.activity;
        document.<%=formName%>.returnAction.value = document.REQUEST.action;
        document.<%=formName%>.activity.value = 'CREATE_REPORT_FILTER';
        document.<%=formName%>.action.value = 'INIT';
        document.<%=formName%>.type.value = '<%= FormatHelper.getObjectId(flexType) %>';
        submitForm();
    }

    function updateFilter(){
        if(document.<%=formName%>.filterId.value == null || document.<%=formName%>.filterId.value == '' || document.<%=formName%>.filterId.value == ' '){
            alert('<%= FormatHelper.encodeForJavascript(youMustSelectAFilterToUpdateAlrt)%>');

            return;
        }
        document.<%=formName%>.returnActivity.value = document.REQUEST.activity;
        document.<%=formName%>.returnAction.value = document.REQUEST.action;

        document.<%=formName%>.activity.value = 'UPDATE_REPORT_FILTER';
        document.<%=formName%>.action.value = 'INIT';
        document.<%=formName%>.oid.value=document.<%=formName%>.filterId.value;
        submitForm();
    }
	</script>
	<script>

    ////////////////////////////////////////////////////////////////////////////
	<%if(ajaxWindow.equals("false")){%>
    var objTree = new jsTree();
	<%}else{%>
		if(Object.isUndefined(objTree)){
			var objTree = new jsTree();
		}
	<%}%>
    //create the root
    objTree.createRoot("", "<%= loader.getRootNode().getName() %>", "", "");

   <% TreeNode rootNode = (TreeNode) loader.getRootNode().getChildren().iterator().next();
      if(FormatHelper.hasContent(rootTypeId)){
         rootNode = (TreeNode)loader.getAllNodesTable().get(FormatHelper.getNumericFromOid(rootTypeId));
      }
   %>
    <%= toTree(rootNode, "objTree.root", nodeName, childFlexType, module) %>
    ////////////////////////////////////////////////////////////////////////////
</script>
<input type="hidden" name="mode" value="">
<input type="hidden" name="fromIndex" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("fromIndex")) %>">
<input type="hidden" name="toIndex" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("toIndex")) %>">
<input type="hidden" name="showAll" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("showAll")) %>">
<input type="hidden" name="sortBy1" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("sortBy1")) %>">

<input type="hidden" name="lastType" value="<%= FormatHelper.encodeForHTMLAttribute(type) %>">
<input type="hidden" name="userId"  value="">

<input type="hidden" name="nameAttribute" value="<%= FormatHelper.encodeForHTMLAttribute(nameAttribute) %>">
<input type="hidden" name="displayAttribute" value="<%= FormatHelper.encodeForHTMLAttribute(displayAttribute) %>">
<!--<input type="hidden" name="viewId" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("viewId")) %>">-->
<input type='hidden' name="typesString" value='<%= FormatHelper.encodeForHTMLAttribute(types) %>'>
<input type='hidden' name="relevantActivity" value='<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("activity")) %>'>
<input type="hidden" name="selectParameter" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("selectParameter")) %>">
<input type="hidden" name="parentPaletteId" value="<%= FormatHelper.encodeForHTMLAttribute(parentPaletteId) %>">
<input type="hidden" name="currentDiv"  value="">
<%if(FormatHelper.hasContent(request.getParameter("reportGroupByFilterAttribute"))){ %>
<input type="hidden" name="reportGroupByFilterAttribute" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("reportGroupByFilterAttribute"))%>">
<%
}
if(FormatHelper.hasContent(request.getParameter("reportFilterAttribute"))){
%>
<input type="hidden" name="reportFilterAttribute" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("reportFilterAttribute"))%>">
<%}%>

<%if("TEMPLATESIZEDEFINITION".equals(module)){
%>
<input type="hidden" name="stPurpose" value="CREATE">
<%}%>

<table width="100%" align="center">
    <tr>
        <td class="PAGEHEADING" colspan="2">
           <span class="PAGEHEADINGTITLE"><%= FormatHelper.encodeAndFormatForHTMLContent(findPgHead) %>
        </td>
    </tr>
   <tr>
      <td class="button" colspan="1" align="left">
         <a id="SearchButton1" class="button" href="javascript:search()"><%= searchLbl %></a>
      </td>
<!-- Include the buttons/selector for views -->
<% boolean useUpdate = true; %>
<%@ include file="/rfa/jsp/reports/ViewButtons.jspf" %>
   </tr>
     <tr>
      <td  valign="top" width="20%">
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle() %><%= typeGrpTle %><%= tg.endTitle() %>
         <%= tg.startGroupContentTable() %>
         <tr>
            <td bgcolor="white" align="left" colspan="2">
               <div id="treeDiv"></div>
            </td>
         </tr>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
      </td>
      <td valign="top">
        <div id='keywordDisplay'>
            <% // Added for KEYWORD Index Search %>
            <%@ include file="/rfa/jsp/indexsearch/IndexSearchCriteria.jspf" %>
        </div>     
		<%
          if(!"com.lcs.wc.foundation.EffectivityContext".equalsIgnoreCase(flexType.getRootType().getTypeClass()) &&
              !"com.lcs.wc.document.LCSImage".equalsIgnoreCase(flexType.getRootType().getTypeClass()) &&
              !"com.lcs.wc.foundation.LCSLogEntry".equalsIgnoreCase(flexType.getRootType().getTypeClass()) &&
              !"com.lcs.wc.planning.FlexPlan".equalsIgnoreCase(flexType.getRootType().getTypeClass()) &&
              !"com.lcs.wc.collection.FlexCollection".equalsIgnoreCase(flexType.getRootType().getTypeClass()) &&
              !"com.lcs.wc.moa.LCSMOAObject".equalsIgnoreCase(flexType.getRootType().getTypeClass()) &&
              !"com.lcs.wc.material.LCSMaterialColor".equalsIgnoreCase(flexType.getRootType().getTypeClass())){ %>      		
			<%@ include file="../main/AgronConfigurableQuickSearch.jspf" %>
         <%}  %>
        <div id='<%=criteriaDisplayId%>'>
        
        </div>
        <div id='<%=defaultCriteriaDivId%>'>
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle() %><%= criteriaGrpTle %><%= tg.endTitle() %>
         <%= tg.startGroupContentTable() %>
         <col width="15%"></col><col width="35%"></col>
         <col width="15%"></col><col width="35%"></col>
         <% if(FormatHelper.hasContent(plugIn)){ %>
         <jsp:include page="<%=subURLFolder+ plugIn %>" flush="true" >
		                         		<jsp:param name="none" value="" />
						</jsp:include>

         <% } %>

         <tr>
            <% if(FormatHelper.hasContent(nameAttribute)){ %>
               <%
                    firstFieldName = nameAttribute;
               %>
               <%= FormGenerator.createTextInput(nameAttribute, nameLabel, request.getParameter(nameAttribute), 30, 45, false) %>
            <% } %>
            <% if(FormatHelper.hasContent(displayAttribute)){ %>
                <%
                    firstFieldName = flexType.getAttribute(displayAttribute).getSearchCriteriaIndex();
                %>
                <%= flexg.createSearchCriteriaWidget(flexType.getAttribute(displayAttribute), flexType, request) %>
            <% } %>
            <%= FormGenerator.createPlaceholder() %>
         </tr>
         <tr>
            <%  if(flexType != null) {
                    // THIS CODE HANDLES THE CASE WHERE TYPE IS CHANGED AND THE
                    // FORM FIELDS ARE BEING LOADED WITH VALUES FROM A SIBLING ATTRIBUTE
                    // ALLOCATION. TO BATTLE THIS, IF THE TYPE CHANGED THEN LOAD IN
                    // EMPTY CRITERIA INSTEAD OF THE LOADED REQUEST.
                    if(FormatHelper.hasContent(lastType) && !lastType.equals(type)){
                        out.print(flexg.generateSearchCriteriaInput(flexType, new Hashtable(), true));
                    } else {
                       out.print(flexg.generateSearchCriteriaInput(flexType, request));
                    }
                }
            %>
         </tr>
            <tr>
              <td class="button" colspan="1" align="left">
                 <a id="SearchButton2" class="button" href="javascript:search()"><%= searchButton %></a>
              </td>
            </tr>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
         </div>

      </td>
   </tr>

<script>
    divHash['<%=defaultCriteriaDivId%>'] = document.getElementById('<%=defaultCriteriaDivId%>').innerHTML;
    document.getElementById('<%=defaultCriteriaDivId%>').innerHTML = '';
</script>
</table>
<%
if(chooser) {
   if(!FormatHelper.hasContent(ractivity)){
	  ractivity = searchActivity;
   }
   //Filters currently not being displayed on page for choosers
   //(That needs to be resolved)

   //Commented out...Need to insure that an activity is used or all filters for
   //all activities will be returned
   //if(!FormatHelper.hasContent(activity)){
   //  throw new WTException("No activity set for the search...can not determine filters");
   //}
}
%>
<%
    lcsContext.filterCache.additionalRoots = null;
    lcsContext.filterCache.attributeMap = null;

	if(!TemplateFilterHelper.firstTimeTemplateFilterSearch(request) && filter != null){
		lcsContext.filterCache.request = TemplateFilterHelper.getTemplateFilterParameters(filter,  RequestHelper.hashRequest(request));
	}else{
		lcsContext.filterCache.request = RequestHelper.hashRequest(request);
	}

%>
<%//= lcsContext.filterCache.getFilterDisplays(FormatHelper.getObjectId(flexType), activity, true, null  , RequestHelper.hashRequest(request)) %>

<script>

        var criteriaDisplay = document.getElementById("<%=criteriaDisplayId%>");
        <% String divId = defaultCriteriaDivId; %>

        <%
        if(filter != null){
            divId = FormatHelper.getObjectId(filter) + "_div";
        %>
            runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterControllerURL, 'returnAction=<%=FormatHelper.encodeForJavascript(request.getParameter("returnAction"))%>&activity=FIND_REPORT_FILTER&action=AJAXSEARCH&filterId=<%= FormatHelper.getObjectId(filter) %>', 'loadFilterToPage');

        <%} else { %>
            criteriaDisplay.innerHTML = divHash['<%=defaultCriteriaDivId%>'];
            parseCalendarTags(criteriaDisplay.innerHTML);

        <% } %>


        showDiv('<%=criteriaDisplayId%>');
        document.<%=formName%>.currentDiv.value='<%=divId%>';
</script>

<script>
	doLoad();
    <%= nodeName %>.expand()
    objTree.root.expandAll()

	<%if(ajaxWindow.equals("false")){%>
    <% if(defaultCriteriaDivId.equals(divId) && !"nothing".equals(firstFieldName)){ %>
        window.setTimeout(function(){
            document.<%=formName%>.<%=FormatHelper.encodeForJavascript(firstFieldName)%>.focus();
        },0);
    <% } %>
		<%}%>
</script>
