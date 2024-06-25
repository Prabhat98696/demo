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
				com.lcs.wc.material.*,
				com.lcs.wc.sample.*,
                wt.lifecycle.*,
                java.util.*"%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request"
	class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request"
	class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request"
	class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext"
	scope="session" />
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!public static boolean INDEX_ENABLED = false;
	static {
		try {
			WTProperties props = WTProperties.getLocalProperties();
			INDEX_ENABLED = props.getProperty("wt.index.enabled", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
	public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
	public static final String COLOR_DEV_ROOT_TYPE = LCSProperties
			.get("com.lcs.wc.sample.LCSSample.Material.ColorDevelopement.Root");
	public static final String MATERIAL_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Material.Root");
	public static final String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties
			.get("com.lcs.wc.supplier.MaterialSupplierRootType", "Supplier");%>
<%
	String searchLbl = WTMessage.getLocalizedMessage(RB.MAIN, "search_Btn", RB.objA);
	String typeGrpTle = WTMessage.getLocalizedMessage(RB.MAIN, "type_LBL", RB.objA);
	String criteriaGrpTle = WTMessage.getLocalizedMessage(RB.MAIN, "criteria_GRP_TLE", RB.objA);
	String searchButton = WTMessage.getLocalizedMessage(RB.MAIN, "search_Btn", RB.objA);
	String youMustSelectAViewToUpdateAlrt = WTMessage.getLocalizedMessage(RB.MAIN,
			"youMustSelectAViewToUpdate_ALRT", RB.objA);
	String youMustSelectAFilterToUpdateAlrt = WTMessage.getLocalizedMessage(RB.MAIN,
			"youMustSelectAFilterToUpdate_ALRT", RB.objA);
	String nameLabel = WTMessage.getLocalizedMessage(RB.MAIN, "name_LBL", RB.objA);
	String materialLabel = WTMessage.getLocalizedMessage(RB.MATERIAL, "material_LBL", RB.objA);
	String colorLabel = WTMessage.getLocalizedMessage(RB.COLOR, "color_LBL", RB.objA);
	String sampleRequestLabel = WTMessage.getLocalizedMessage(RB.SAMPLES, "sampleRequest_PG_HEAD", RB.objA);
	String sampleLabel = WTMessage.getLocalizedMessage(RB.SAMPLES, "sample_LBL", RB.objA);
	String supplierLabel = WTMessage.getLocalizedMessage(RB.SUPPLIER, "supplier_LBL", RB.objA);
	String materialSupplierLabel = WTMessage.getLocalizedMessage(RB.MATERIALSUPPLIER, "materialSupplier_LBL",
			RB.objA);
	String materialColorLabel = WTMessage.getLocalizedMessage(RB.MATERIALCOLOR, "materialColor_LBL", RB.objA);
	String metaLabel = WTMessage.getLocalizedMessage(RB.MAIN, "system_LBL", RB.objA);

	String searchBySupplierLabel = WTMessage.getLocalizedMessage(RB.MATERIAL, "searchBySupplier_LBL", RB.objA);
	String searchByColorLabel = WTMessage.getLocalizedMessage(RB.MATERIAL, "searchByColor_LBL", RB.objA);
	String searchBySampleLabel = WTMessage.getLocalizedMessage(RB.MATERIAL, "searchBySample_LBL", RB.objA);
	String includeSupplierLabel = WTMessage.getLocalizedMessage(RB.MATERIAL, "includeSupplier_LBL", RB.objA);
	String includeColorLabel = WTMessage.getLocalizedMessage(RB.MATERIAL, "includeColor_LBL", RB.objA);
	String includeSampleLabel = WTMessage.getLocalizedMessage(RB.MATERIAL, "includeSample_LBL", RB.objA);

	////View Stuffs//////////////
	String updateFilterMouseOver = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS,
			"updateFilterMouseOver_TOOLTIP", RB.objA);
	String createNewFilterMouseOver = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS,
			"createNewFilterMouseOver_TOOLTIP", RB.objA);
	String updateTableLayoutMoueseOver = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS,
			"updateTableLayoutMoueseOver_TOOLTIP", RB.objA);
	String createNewTableLayoutMouseOver = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS,
			"createNewTableLayoutMouseOver_TOOLTIP", RB.objA);

	String viewsLabel = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS, "views_LBL", RB.objA);
	String filtersLabel = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS, "filters_LBL", RB.objA);
	String viewNoAvailLabel = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS, "viewsNotAvailable_LBL",
			RB.objA);
	String filterNoAvailLabel = wt.util.WTMessage.getLocalizedMessage(RB.REPORTS, "filtersNotAvailable_LBL",
			RB.objA);
	String clearAllButton = WTMessage.getLocalizedMessage(RB.REPORTS, "clearAll_Filter_Btn", RB.objA);
	String clearAllMouseOver = WTMessage.getLocalizedMessage(RB.REPORTS, "clearAllMouseOver_TOOLTIP", RB.objA);
	String resetDefaultsButton = WTMessage.getLocalizedMessage(RB.REPORTS, "resetDefaults_Filter_Btn", RB.objA);
	String resetDefaultsMouseOver = WTMessage.getLocalizedMessage(RB.REPORTS, "resetDefaultsMouseOver_TOOLTIP",
			RB.objA);
	String asteriskIllegalMessage = WTMessage.getLocalizedMessage(RB.MAIN, "asteriskIllegal_MSG", RB.objA);

	///////////////////////////

	String searchScope = MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE;
	String searchLevel = "";
	String searchLabel = "Material";
	String activity = request.getParameter("activity");
	String typeclass = "com.lcs.wc.material.LCSMaterial";
	String searchActivity = "FIND_ADVANCED_MATERIAL";
	String type = FormatHelper.format(request.getParameter("type"));
	String lastType = FormatHelper.format(request.getParameter("lastType"));
	String rootTypeId = FormatHelper.format(request.getParameter("rootTypeId"));
	String nameAttribute = FormatHelper.format(request.getParameter("nameAttribute"));
	String displayAttribute = "name";
	String plugIn = FormatHelper.format(request.getParameter("additionalPlugIn"));
	boolean chooser = FormatHelper.parseBoolean(request.getParameter("chooser"));
	boolean multiple = FormatHelper.parseBoolean(request.getParameter("multiple"));
	String searchType = "";

	boolean filterSelected = FormatHelper.hasContent(request.getParameter("filterId"));
	boolean modifySearchClicked = FormatHelper.hasContent(request.getParameter("modifySearchClicked"));
	/*This Search Purpose indicates that we want to search t view or edit.
	If we want to search to edit, need to check for Edit, Delete
	*/

	//We need this block, because the pages that call this do not all use the parameter to pass in the type info.
	if ("MaterialSupplier".equalsIgnoreCase(searchLabel)) {
		searchType = WTMessage.getLocalizedMessage(RB.MATERIALSUPPLIER, "materialSupplier_LBL", RB.objA);
	} else {
		if (FormatHelper.hasContent(type)) {
			searchType = FlexTypeCache.getFlexType(type).getTypeDisplayName();
		} else {
			if (FormatHelper.hasContent(rootTypeId)) {
				searchType = FlexTypeCache.getFlexType(rootTypeId).getTypeDisplayName();
			} else {
				searchType = FlexTypeCache.getFlexTypeRootByClass(typeclass).getTypeDisplayName();
			}
		}
	}

	Object[] objsearchType = {searchType};
	String findPgHead = WTMessage.getLocalizedMessage(RB.MAIN, "findObject_LBL", objsearchType);

	String typesPrefix = "Material|";

	if (FormatHelper.hasContent(searchScope)) {
		flexg.setScope(searchScope);
	}
	if (FormatHelper.hasContent(searchLevel)) {
		flexg.setLevel(searchLevel);
	}

	String includeSupplier = "false";
	String includeColor = "false";
	String includeSample = "false";

	String searchBySupplier = "false";
	String searchByColor = "false";
	String searchBySample = "false";

	if (FormatHelper.hasContent(request.getParameter("includeSupplier")) && (!filterSelected || modifySearchClicked)) {
		includeSupplier = request.getParameter("includeSupplier");
	}

	///Vendor search////
	if (lcsContext.isVendor) {
		includeSupplier = "true";
	}

	if (FormatHelper.hasContent(request.getParameter("includeColor")) && (!filterSelected || modifySearchClicked)) {
		includeColor = request.getParameter("includeColor");
	}

	if (FormatHelper.hasContent(request.getParameter("includeSample")) && (!filterSelected || modifySearchClicked)) {
		includeSample = request.getParameter("includeSample");
	}

	if (FormatHelper.hasContent(request.getParameter("searchBySupplier")) && (!filterSelected || modifySearchClicked)) {
		searchBySupplier = request.getParameter("searchBySupplier");
	}

	if (FormatHelper.hasContent(request.getParameter("searchByColor")) && (!filterSelected || modifySearchClicked)) {
		searchByColor = request.getParameter("searchByColor");
	}

	if (FormatHelper.hasContent(request.getParameter("searchBySample")) && (!filterSelected || modifySearchClicked)) {
		searchBySample = request.getParameter("searchBySample");
	}

	FlexType flexType = null;
	FlexTypeClassificationTreeLoader loader = new FlexTypeClassificationTreeLoader(typeclass);
	loader.constructTree();

	String rootId = ((TreeNode) loader.getRootNode().getChildren().iterator().next()).getId();
	if (FormatHelper.hasContent(rootTypeId)) {
		rootId = FormatHelper.getNumericFromOid(rootTypeId);
	}
	if (!FormatHelper.hasContent(type)) {
		type = rootId;
	}
	flexType = FlexTypeCache.getFlexType(type);
	type = FormatHelper.getObjectId(flexType);

	FlexType materialColorRoot = FlexTypeCache.getFlexTypeRoot("Material Color");
	//TODO: is this okay to get material supplier root type?
	FlexType materialSupplierRoot = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterialSupplier");
	FlexType supplierRoot = FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE);
	String supplierRootId = FormatHelper.getObjectId(supplierRoot);

	FlexType supplierType = supplierRoot;
	String supplierTypeId = FormatHelper.getObjectId(supplierRoot);

	FlexType colorRoot = FlexTypeCache.getFlexTypeRoot("Color");
	String colorRootId = FormatHelper.getObjectId(colorRoot);

	FlexType colorType = colorRoot;
	String colorTypeId = FormatHelper.getObjectId(colorRoot);

	FlexType colorDevelopmentSampleType = FlexTypeCache.getFlexTypeFromPath(COLOR_DEV_ROOT_TYPE);
	String colorDevelopmentSampleTypeId = FormatHelper.getObjectId(colorDevelopmentSampleType);

	FlexType materialSampleRoot = FlexTypeCache.getFlexTypeFromPath(MATERIAL_ROOT_TYPE);
	String materialSampleRootId = FormatHelper.getObjectId(materialSampleRoot);

	FlexType sampleType = materialSampleRoot;
	String sampleTypeId = FormatHelper.getObjectId(materialSampleRoot);

	if (FormatHelper.hasContent(request.getParameter("colorType"))) {
		colorTypeId = request.getParameter("colorType");
		colorType = FlexTypeCache.getFlexType(colorTypeId);
	}

	if (FormatHelper.hasContent(request.getParameter("supplierType"))) {
		supplierTypeId = request.getParameter("supplierType");
		supplierType = FlexTypeCache.getFlexType(supplierTypeId);
	}

	if (FormatHelper.hasContent(request.getParameter("sampleType"))) {
		sampleTypeId = request.getParameter("sampleType");
		sampleType = FlexTypeCache.getFlexType(sampleTypeId);
	}
	boolean hasAccessToSearchMatSuppliers = false;
	boolean hasAccessToSearchMatSamples = false;
	boolean hasAccessToSearchMatColors = false;
	
	hasAccessToSearchMatSuppliers = ACLHelper.hasViewAccess(supplierType) && ACLHelper.hasViewAccess(materialSupplierRoot);
	hasAccessToSearchMatColors = hasAccessToSearchMatSuppliers && ACLHelper.hasViewAccess(materialColorRoot) && ACLHelper.hasViewAccess(colorType);
	hasAccessToSearchMatSamples = hasAccessToSearchMatSuppliers && ACLHelper.hasViewAccess(sampleType);
	////////////////////////////////////////////////////////////////////////

	String stKey = ((wt.org.WTUser) wt.session.SessionHelper.manager.getPrincipal()).getAuthenticationName()
			+ "~ChooserPurpose";
	String stPurpose = (String) session.getAttribute(stKey);

	//For now, this code is a hack for a temp patch for CATO, and I want it to apply to LCSMeasurements only
	if (!"com.lcs.wc.measurements.LCSMeasurements".equals(typeclass))
		stPurpose = "";

	/*
	A Search Screen can be used to search fo objects to view, edit or create.
	We must search for the appropriate permission
	*/
	if ((!ACLHelper.hasCreateAccess(flexType) && "CREATE".equals(stPurpose))
			|| (!ACLHelper.hasEditAccess(flexType) && "EDIT".equals(stPurpose))
			|| !ACLHelper.hasViewAccess(flexType)) {
		String stMessageKey = (stPurpose == null || "".equals(stPurpose))
				? "noAccessToViewThisType_MSG"
				: "youDoNotHaveCreatePermForThisType_MSG";
		throw new LCSAccessException(RB.EXCEPTION, stMessageKey, RB.objA);
	}
	
	//String typeStringDef = request.getParameter("typeStringDef");
	String types = "";
	/*	if(FormatHelper.hasContent(typeStringDef)) {
		  types = typeStringDef;
		} else {
		  types = typesPrefix + (FormatHelper.getObjectId(flexType));
		}
	*/
	types = MOAHelper.addValue(types, "Material|" + FormatHelper.getObjectId(flexType));
	types = MOAHelper.addValue(types, "Material Color|" + FormatHelper.getObjectId(materialColorRoot));
	types = MOAHelper.addValue(types, "Color|" + FormatHelper.encodeAndFormatForHTMLContent(colorTypeId));
	types = MOAHelper.addValue(types, "Supplier|" + FormatHelper.encodeAndFormatForHTMLContent(supplierTypeId));
	types = MOAHelper.addValue(types, "Sample|" + FormatHelper.encodeAndFormatForHTMLContent(sampleTypeId));

	if (FormatHelper.hasContent(request.getParameter("typesString"))) {
		types = request.getParameter("typesString");
	}

	String nodeName = "node_" + FormatHelper.getNumericFromOid(FormatHelper.getObjectId(flexType));
	String firstFieldName = "nothing";

	String nodeName2 = "node_" + FormatHelper.getNumericFromOid(supplierTypeId);

	String nodeName3 = "node_" + FormatHelper.getNumericFromOid(colorTypeId);

	String nodeName4 = "node_" + FormatHelper.getNumericFromOid(sampleTypeId);

	// Used by Index Search
	String searchterm = "";
	if (FormatHelper.hasContent(request.getParameter("indexSearchKeyword"))) {
		searchterm = request.getParameter("indexSearchKeyword");
	}
%>
<script type="text/javascript" src="<%=URL_CONTEXT%>/javascript/ajax.js"></script>


<%@ include file="../../../jsp/reports/ViewLookup.jspf"%>
<%!public static String toTree(TreeNode node, String parent, String activeNode, String type, String objTreeName) {
    	
		StringBuffer buffer = new StringBuffer();
		boolean active = false;
		String nodeName = "node_" + node.getId();
		if (nodeName.equals(activeNode)) {
			active = true;
		}
		try {
			FlexType flexType = FlexTypeCache.getFlexType(node.getId());
			//@Likhit: XSS related change
			buffer.append("var " + nodeName + " = " + parent + ".addChild('', '"
					+ FormatHelper.encodeForHTMLThenJavascript(node.getName()) + "', 'javascript:chooseType(\""
					+ FormatHelper.getObjectId(flexType) + "\",\"" + type + "\", true)', '', " + parent + ", " + active
					+ ", " + objTreeName + ");\n");
			Iterator childNodes = SortHelper.sort(node.getChildren(), "name").iterator();
			TreeNode child = null;
			while (childNodes.hasNext()) {
				child = (TreeNode) childNodes.next();
				buffer.append(toTree(child, "node_" + node.getId(), activeNode, type, objTreeName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script>
   var typeArray = new Array();
   typeArray['Material'] = 'sc1';
   typeArray['Supplier'] = 'sc2';
   typeArray['Color'] = 'sc3';
   typeArray['Sample'] = 'sc4';

	var selectedMaterialNode = '<%=FormatHelper.getObjectId(flexType)%>';
	var selectedColorNode = '<%=FormatHelper.encodeForJavascript(colorTypeId)%>';
	var selectedSupplierNode = '<%=FormatHelper.encodeForJavascript(supplierTypeId)%>';
	var selectedSampleNode = '<%=FormatHelper.encodeForJavascript(sampleTypeId)%>';
	var urlContext_masterController_url = '<%=PageManager.getContextOverrideUrlAndMasterContorller(true)%>';


    var divHash = new Array();

    function search(){

       if (document.MAINFORM.currentDiv.value != 'defaultCriteria_div') {
           defaultCriteria_div.innerHTML = '';
       }
	   
	   // If Index Search is enabled and the criteria is valid
	   var validateFlag = false;
       if (document.MAINFORM.indexSearchKeyword) {
    	   validateFlag = validate_keyword();
       } else {
    	   validateFlag = validate();
	   }
       
       if (validateFlag) {
	       document.MAINFORM.activity.value = '<%=searchActivity%>';
	       document.MAINFORM.action.value = 'SEARCH';
	       document.MAINFORM.type.value = selectedMaterialNode;
	       submitForm();
       }

   }

   function validate_keyword() {
       if (trim(document.MAINFORM.indexSearchKeyword.value) == "*") {
    	   alert('<%=FormatHelper.encodeForJavascript(asteriskIllegalMessage)%>');
           return false;
       } 
       return validate();
   }   
   

    // CODE WHICH SUBMITS THE FORM ON AN ENTER KEY PRESS
    document.body.onkeypress = enterKey;
    function enterKey(evt) {
      var is = new Is();
      var evt = (evt) ? evt : event

      // IF ELEMENT TRIGGERING EVENT IS THE SEARCH BUTTON THEN EXECUTE SEARCH
      var el = evt.srcElement;
      if (el.id == 'SearchButton1' || el.id == 'SearchButton2') {
         search();
         return;
      }

      // OTHERWISE SIMPLY SET FOCUS TO THE SEARCH BUTTON
      // THIS ALLOWS THE ONBLUR OF AN INPUT FIELD WITH FOCUS TO FIRE
      // ON THE ENTER KEY PRESS
      var charCode = (evt.which) ? evt.which : evt.keyCode
      if (charCode == 13) { // enter key on PC, return key on Mac
        var sb = document.getElementById('SearchButton2').focus();
      } else if (is.mac){
        if (charCode == 3) { // enter key on Mac
            document.getElementById('SearchButton2').focus();
        }
      }
    }

    function validate(){
        if(document.MAINFORM.currentDiv.value == 'defaultCriteria_div'){
        <%=flexg.generateFormValidation(flexType, true)%>
        }
        return true;
    }

    function pickNewType(){
        document.MAINFORM.activity.value = '<%=searchActivity%>';
        document.MAINFORM.action.value = 'INIT';
        document.MAINFORM.type.value = '';
        document.MAINFORM.viewId.value = '';
        submitForm();
    }

   function doLoad() {
        objTree.buildDOM(treeDiv, "objTree");
        //build dom of the tree only when available, i.e 
        //due to access checks a obj tree might not be available
        //for a user.
        if (("objTree2" in window) && objTree2){
        	objTree2.buildDOM(treeDiv2, "objTree2");
        }
		if (("objTree3" in window) && objTree3){
			objTree3.buildDOM(treeDiv3, "objTree3");
		}
		if (("objTree4" in window) && objTree4){
			objTree4.buildDOM(treeDiv4, "objTree4");
		}
    }

    function changeView(selectList){
        //don't do anything
    }

    function newView(){
        document.MAINFORM.returnActivity.value =   document.REQUEST.activity;
        document.MAINFORM.activity.value = 'CREATE_REPORT_VIEW';
        document.MAINFORM.action.value = 'INIT';
        document.MAINFORM.returnAction.value = document.REQUEST.action;
        submitForm();
    }

    function updateView(){
        if(document.MAINFORM.viewId.value == null || document.MAINFORM.viewId.value == '' || document.MAINFORM.viewId.value == ' '){
        	alert('<%=FormatHelper.encodeForJavascript(youMustSelectAViewToUpdateAlrt)%>');
            return;
        }
        document.MAINFORM.returnActivity.value =   document.REQUEST.activity;
        document.MAINFORM.returnAction.value = document.REQUEST.action;

        document.MAINFORM.activity.value = 'UPDATE_REPORT_VIEW';
        document.MAINFORM.action.value = 'INIT';
        document.MAINFORM.oid.value=document.MAINFORM.viewId.value;
        submitForm();
    }

    function changeFilter(selectList){
        var divName = 'defaultCriteria_div';
        if(selectList.value.indexOf('FiltersList') > -1){
            divName = selectList.value + '_div';
        }

        document.MAINFORM.currentDiv.value = divName;

        if(hasContent(divHash[divName])){
            switchDisplayDiv(divName);
        }
        else{
            if(selectList.value.indexOf('FiltersList') > -1){
                runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_REPORT_FILTER&action=AJAXSEARCH&filterId' + "=" + selectList.value, 'loadFilterToPage');
            }
            else{
                switchDisplayDiv('defaultCriteria_div');
            }
        }
    }

    function loadFilterToPage(xml, text){
        if(hasContent(text)){
            var divName = document.MAINFORM.currentDiv.value;
            divHash[divName] = text;
            switchDisplayDiv(divName);
            parseScripts(text);
        }
    }

    function switchDisplayDiv(divName){
        criteriaDisplay.innerHTML = divHash[divName];
        parseCalendarTags(criteriaDisplay.innerHTML);
    }

    function parseCalendarTags(ihtml){
        var sBegin = "Calendar.setup";
        var sEnd = ");";

        var beginTag = ihtml.indexOf(sBegin);
        if(beginTag < 0) return;

        ihtml = ihtml.substring(beginTag);
        var endTag = ihtml.indexOf(sEnd);

        var evalStr = ihtml.substring(0, endTag+1);
        evalStr.evalScripts();

        var newBegin = endTag +1;
        ihtml = ihtml.substring(newBegin);
        parseCalendarTags(ihtml);
    }


    function newFilter(){
        document.MAINFORM.returnActivity.value =   document.REQUEST.activity;
        document.MAINFORM.returnAction.value = document.REQUEST.action;
        document.MAINFORM.activity.value = 'CREATE_REPORT_FILTER';
        document.MAINFORM.action.value = 'INIT';
        submitForm();
    }

    function updateFilter(){
        if(document.MAINFORM.filterId.value == null || document.MAINFORM.filterId.value == '' || document.MAINFORM.filterId.value == ' '){
        	 alert('<%=FormatHelper.encodeForJavascript(youMustSelectAFilterToUpdateAlrt)%>');
            return;
        }
        document.MAINFORM.returnActivity.value = document.REQUEST.activity;
        document.MAINFORM.returnAction.value = document.REQUEST.action;

        document.MAINFORM.activity.value = 'UPDATE_REPORT_FILTER';
        document.MAINFORM.action.value = 'INIT';
        document.MAINFORM.oid.value=document.MAINFORM.filterId.value;
        submitForm();
    }


	function chooseType(typeId, typeName, reload){


			if(!<%=chooser%>) {
				var text = '<br><br><b><center>' + pleaseWaitWhileProcessingRequestMSG + '<br><br><img src=\'' +urlContext +'/images/blue-loading.gif\' border="0"></center><br><br>';

				var link = document.getElementById(typeId + "_link");
				var menu = document.createElement("div");
				menu.style.display = 'block';
				menu.id = 'waitMessage';
				menu.style.zIndex = 200;
				menu.style.position = 'absolute';
				menu.style.left = getDivAbsolutePos(link).x + 10;
				menu.style.top = getDivAbsolutePos(link).y + 10;
				menu.className = 'actionOptions';
				menu.innerHTML = text;
				document.body.appendChild(menu);
			}

		    var reloadViewFilter = false;
			if(reload){
				runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_MATERIAL_SEARCH_CRITERIA&action=AJAXSEARCH&rootTypeId=' + typeId + '&materialTypeId=' + selectedMaterialNode, 'appendSearchCriteria');

				if(typeName== "Material" && document.MAINFORM.searchBySupplier.value == "true"){
					runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_MATERIAL_SEARCH_CRITERIA&action=AJAXSEARCH&rootTypeId=' + document.MAINFORM.supplierType.value + '&materialTypeId=' + typeId, 'appendSearchCriteria');
				}
			}
			selectNode(typeId);
			if(typeName == "Material" && selectedMaterialNode!=typeId){
				deselectNode(selectedMaterialNode);
				selectedMaterialNode = typeId;
		        document.MAINFORM.type.value = typeId;
				document.MAINFORM.lastType.value = typeId;
				reloadViewFilter = true;
				if(document.MAINFORM.searchBySupplier.value == "true"){
					if(reload){
					   chooseType(selectedSupplierNode, "Supplier");
					}
				}
			}

			if(typeName == "Supplier" && selectedSupplierNode!=typeId){
				deselectNode(selectedSupplierNode);
				selectedSupplierNode = typeId;
		        document.MAINFORM.supplierType.value = typeId;
				reloadViewFilter = true;

			}

			if(typeName == "Color" && selectedColorNode!=typeId){
				deselectNode(selectedColorNode);
				selectedColorNode = typeId;
		        document.MAINFORM.colorType.value = typeId;
				reloadViewFilter = true;

			}

			if(typeName == "Sample" && selectedSampleNode!=typeId){
				deselectNode(selectedSampleNode);
				selectedSampleNode = typeId;
		        document.MAINFORM.sampleType.value = typeId;
				reloadViewFilter = true;

				if(typeId == '<%=colorDevelopmentSampleTypeId%>'){
					var targetCheckbox = document.getElementById("includeColorCheckBox");
					targetCheckbox.checked = true;
					targetCheckbox.disabled = true;
					document.MAINFORM.includeColor.value = "true";
				}else{
					var targetCheckbox = document.getElementById("includeColorCheckBox");
					if(targetCheckbox.disabled == true){
						targetCheckbox.disabled = false;
					}
				}
			}

			var typesString = document.MAINFORM.typesString.value;
            typesString = "Sample|" + selectedSampleNode + DELIMITER + "Material|" + selectedMaterialNode + DELIMITER + "Supplier|" + selectedSupplierNode + DELIMITER + "Color|" + selectedColorNode + DELIMITER + "Material Color|<%=FormatHelper.getObjectId(materialColorRoot)%>";
			            
			document.MAINFORM.typesString.value = typesString;
			if(reloadViewFilter){
			    var f = divHash['defaultCriteria_div'];
				divHash = new Array();
				divHash['defaultCriteria_div'] = f;
				runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_REPORT_VIEW&relevantActivity=' + document.MAINFORM.relevantActivity.value + '&typeId=' + selectedMaterialNode + '&typesString=' + typesString, 'loadViews');

			}else{
				closeDiv("waitMessage");

			}
	}

	function selectNode(typeId){
			var bb = document.getElementById(typeId + "_td");
			bb.className = "SELECTED-NODE";

			var cc = document.getElementById(typeId + "_link");
			cc.className = "SELECTED-NODE-LINK";
	}

	function deselectNode(node){
			var dd = document.getElementById(node + "_td");
			dd.className = "LABEL";
			var ee = document.getElementById(node + "_link");
			ee.className = "LABEL";
	}

	function loadViews(xml, text){
			var viewSelect = document.getElementById("viewId");

			if(hasContent(text)){
			   var columnListArray = new Array;
			   var rawText = trim(text.substring(0, text.indexOf("|FILTER|")));
			   var viewText = trim(rawText.substring(0, rawText.indexOf("|DEFVIEW|")));
			   var defaultViewId = trim(rawText.substring(rawText.indexOf("|DEFVIEW|") + 9));
			   if(hasContent(viewText)){
					columnListArray = moaToArray(viewText);
			   }
			   if(columnListArray.length >0 && viewSelect == null){
					var viewDiv = document.getElementById("viewDiv");
					viewDiv.innerHTML = viewFilterWidgets['loadedViewDiv'];
			   }else if(viewSelect && columnListArray.length >0){
					emptyList(viewSelect);
					addOptionValue(viewSelect, "", " ", "true", true);
			   }else if(columnListArray.length ==0 && viewSelect){
					var viewDiv = document.getElementById("viewDiv");
					viewDiv.innerHTML = viewFilterWidgets['emptyViewDiv'];				
			   }
			   viewSelect = document.getElementById("viewId");
			   if(viewSelect){
				   var key;
				   var value;
				   for(var j=0; j<columnListArray.length; j++){
						key = columnListArray[j].substring(0, columnListArray[j].indexOf("|"));
						if(key == defaultViewId){
							
						}
						value = columnListArray[j].substring(columnListArray[j].indexOf("|") +1);
						addOptionValue(viewSelect, value, key, "true", true)
				   }
				   if(hasContent(defaultViewId)){
					   var selectedIndex = getKeyIndex(viewSelect, defaultViewId);
					   if(selectedIndex > -1){
							viewSelect.options[selectedIndex].selected = true;
					   }
				   }
			   }

			}

	
			var filterSelect = document.getElementById("filterId");
			if(hasContent(text)){
			   var filterListArray = new Array;
			   var rawText = trim(text.substring(text.indexOf("|FILTER|") + 8));
			   var filterText = trim(rawText.substring(0, rawText.indexOf("|DEFFILTER|")));
			   var defaultFilterId = trim(rawText.substring(rawText.indexOf("|DEFFILTER|") + 11));

			   if(hasContent(filterText)){
					filterListArray = moaToArray(filterText);
			   }
			   if(filterListArray.length >0 && filterSelect == null){
					var filterDiv = document.getElementById("filterDiv");
					filterDiv.innerHTML = viewFilterWidgets['loadedFilterDiv'];
			   }else if(filterSelect && filterListArray.length >0){
					emptyList(filterSelect);
					addOptionValue(filterSelect, "", " ", "true", true);
			   }else if(filterListArray.length ==0 && filterSelect){
					var filterDiv = document.getElementById("filterDiv");
					filterDiv.innerHTML = viewFilterWidgets['emptyFilterDiv'];				
			   }
			   filterSelect = document.getElementById("filterId");
			   if(filterSelect){
				   var key;
				   var value;
				   for(var j=0; j<filterListArray.length; j++){
						key = filterListArray[j].substring(0, filterListArray[j].indexOf("|"));
						value = filterListArray[j].substring(filterListArray[j].indexOf("|") +1);
						addOptionValue(filterSelect, value, key, "true", true)
				   }
				   if(hasContent(defaultFilterId)){
					   var selectedIndex = getKeyIndex(filterSelect, defaultFilterId);
					   if(selectedIndex > -1){
							filterSelect.options[selectedIndex].selected = true;
							changeFilter(filterSelect);
					   }
				   }

			   }
		   }
		   //switchDisplayDiv('defaultCriteria_div');
		   closeDiv("waitMessage");

	}


   function appendSearchCriteria(xml, text){
        //alert("xml: " + xml);
		var currentCriteriaDiv;
		var filterSelect = document.getElementById("filterId");
		if(filterSelect && filterSelect.selectedIndex>0){
			currentCriteriaDiv = document.getElementById('criteriaDisplay').innerHTML;
        }
		switchDisplayDiv('defaultCriteria_div');
        if(hasContent(text)){
			var hh = text.substring(text.indexOf("<div id=\"typeName\">") + 19, text.indexOf("</div>"));
			var ff = document.getElementById(typeArray[hh]);
			ff.innerHTML = text.substring(text.indexOf("</div>") + 6);
			parseCalendarTags(ff.innerHTML);
			parseScripts(text);
			divHash['defaultCriteria_div'] = document.getElementById('criteriaDisplay').innerHTML;

        }
		if(currentCriteriaDiv){
			document.getElementById('criteriaDisplay').innerHTML = currentCriteriaDiv;
		}
    }

	function setSearchCriteria(typeName, typeId, expand, reload){


	    var supplierCheckbox = document.getElementById("includeSupplierCheckBox");
	    var colorCheckbox = document.getElementById("includeColorCheckBox");
	    var sampleCheckbox = document.getElementById("includeSampleCheckBox");

		if(typeName=='supplier' && expand == 'true'){
			document.MAINFORM.searchBySupplier.value = 'true';
			if(hasContent(typeId)){
				chooseType(typeId, 'Supplier', reload);
			}
			supplierCheckbox.checked = true;
			supplierCheckbox.disabled = true;
			document.MAINFORM.includeSupplier.value = "true";
		}else if(typeName=='supplier' && expand == 'false') {
			switchDisplayDiv('defaultCriteria_div');
			document.MAINFORM.searchBySupplier.value = 'false';
			var div = document.getElementById(typeArray['Supplier']);
			div.innerHTML = "";
			// if color or sample checkbox is not present or presnet and enabled
			if((!colorCheckbox || !colorCheckbox.disabled) && (!sampleCheckbox || !sampleCheckbox.disabled)){
				<%if (!lcsContext.isVendor) {%>
				supplierCheckbox.disabled = false;
				<%}%>
			}
			deselectNode(selectedSupplierNode);
			selectedSupplierNode = '<%=supplierRootId%>';

		}

		if(typeName=='color' && expand == 'true'){
			document.MAINFORM.searchByColor.value = 'true';
			if(hasContent(typeId)){
				chooseType(typeId, 'Color', reload);
			}
			supplierCheckbox.checked = true;
			supplierCheckbox.disabled = true;
			document.MAINFORM.includeSupplier.value = "true";
			colorCheckbox.checked = true;
			colorCheckbox.disabled = true;
			document.MAINFORM.includeColor.value = "true";

		}else if(typeName=='color' && expand == 'false'){
			switchDisplayDiv('defaultCriteria_div');
			document.MAINFORM.searchByColor.value = 'false';
			var div = document.getElementById(typeArray['Color']);
			div.innerHTML = "";
			if(selectedSampleNode != '<%=colorDevelopmentSampleTypeId%>'){
				colorCheckbox.disabled = false;
			}
			if(document.MAINFORM.searchBySupplier.value != 'true' && (!sampleCheckbox || !sampleCheckbox.disabled)){
					<%if (!lcsContext.isVendor) {%>
					supplierCheckbox.disabled = false;
					<%}%>
			}
			deselectNode(selectedColorNode);
			selectedColorNode = '<%=colorRootId%>';
		}	
	
		if(typeName=='sample' && expand == 'true'){
			document.MAINFORM.searchBySample.value = 'true';
			if(hasContent(typeId)){
				chooseType(typeId, 'Sample', reload);
			}
			supplierCheckbox.checked = true;
			supplierCheckbox.disabled = true;

			document.MAINFORM.includeSupplier.value = "true";
/**not sure what to do with this yet

			colorCheckbox.checked = true;
			colorCheckbox.disabled = true;

			document.MAINFORM.includeColor.value = "true";
****/

			sampleCheckbox.checked = true;
			sampleCheckbox.disabled = true;

			document.MAINFORM.includeSample.value = "true";

		}else if(typeName=='sample' && expand == 'false'){
			switchDisplayDiv('defaultCriteria_div');
			document.MAINFORM.searchBySample.value = 'false';
			var div = document.getElementById(typeArray['Sample']);
			div.innerHTML = "";
			if(document.MAINFORM.searchBySupplier.value == 'false' && document.MAINFORM.searchByColor.value == 'false'){
				<%if (!lcsContext.isVendor) {%>
				supplierCheckbox.disabled = false;
				<%}%>
			}
			if(document.MAINFORM.searchByColor.value == 'false'){
				if ( colorCheckbox ){
					colorCheckbox.disabled = false;
				}
			}
			sampleCheckbox.disabled = false;
			deselectNode(selectedSampleNode);
			selectedSampleNode = '<%=materialSampleRootId%>';
		}	

		if(expand == 'false'){
            var typesString = "Sample|" + selectedSampleNode + DELIMITER + "Material|" + selectedMaterialNode + DELIMITER + "Supplier|" + selectedSupplierNode + DELIMITER + "Color|" + selectedColorNode + DELIMITER + "Material Color|<%=FormatHelper.getObjectId(materialColorRoot)%>";			            
			document.MAINFORM.typesString.value = typesString;
			runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_REPORT_VIEW&relevantActivity=' + document.MAINFORM.relevantActivity.value + '&typeId=' + selectedMaterialNode + '&typesString=' + typesString, 'loadViews');
			divHash['defaultCriteria_div'] = document.getElementById('criteriaDisplay').innerHTML;
		}

        <%if (INDEX_ENABLED) {%>
            setIndexSearchClass();
        <%}%>
       

	}

	function respondToClick(inputCheckbox, input){
		if(inputCheckbox.checked){
			input.value = "true";
		}else if(!inputCheckbox.checked){
			input.value = "false";

		}
		if(inputCheckbox.name=="includeColorCheckBox" && inputCheckbox.checked){
			var targetCheckbox = document.getElementById("includeSupplierCheckBox");
			targetCheckbox.checked = true;
			document.MAINFORM.includeSupplier.value = "true";

		}else if(inputCheckbox.name=="includeSupplierCheckBox" && !inputCheckbox.checked){
			document.MAINFORM.includeSupplier.value = "false";
			var targetCheckbox = document.getElementById("includeColorCheckBox");
			if (targetCheckbox){
				targetCheckbox.checked = false;
			}
			document.MAINFORM.includeColor.value = "false";
		    targetCheckbox = document.getElementById("includeSampleCheckBox");
		    if (targetCheckbox){ 
				targetCheckbox.checked = false;
		    }
			document.MAINFORM.includeSample.value = "false";

		}else if(inputCheckbox.name=="includeColorCheckBox" && !inputCheckbox.checked){
			document.MAINFORM.includeColor.value = "false";
		    //targetCheckbox = document.getElementById("includeSampleCheckBox");
			//targetCheckbox.checked = false;
			//document.MAINFORM.includeSample.value = "false";

		}else if(inputCheckbox.name=="includeSampleCheckBox" && inputCheckbox.checked){
			document.MAINFORM.includeSample.value = "true";
			var targetCheckbox = document.getElementById("includeSupplierCheckBox");
			targetCheckbox.checked = true;
			document.MAINFORM.includeSupplier.value = "true";	
		/**not sure what to do with this yet
	
		    targetCheckbox = document.getElementById("includeColorCheckBox");
			targetCheckbox.checked = true;
			document.MAINFORM.includeColor.value = "true";
		***/

		}
	
        <%if (INDEX_ENABLED) {%>
            setIndexSearchClass();
        <%}%>
	}

    ////////////////////////////////////////////////////////////////////////////
    var objTree = new jsTree("objTree");
    //create the root
    objTree.createRoot("", "<%=loader.getRootNode().getName()%>", "", "", "objTree");

   <%TreeNode rootNode = (TreeNode) loader.getRootNode().getChildren().iterator().next();
			if (FormatHelper.hasContent(rootTypeId)) {
				rootNode = (TreeNode) loader.getAllNodesTable().get(FormatHelper.getNumericFromOid(rootTypeId));
			}%>
    <%=toTree(rootNode, "objTree.root", nodeName, "Material", "objTree")%>
    <% if (hasAccessToSearchMatSuppliers){
    	loader = new FlexTypeClassificationTreeLoader("com.lcs.wc.supplier.LCSSupplier");
			loader.constructTree();
			//rootNode = (TreeNode) loader.getRootNode().getChildren().iterator().next();
			rootNode = (TreeNode) loader.getAllNodesTable().get(FormatHelper.getNumericFromOid(supplierRootId));%>

		 var objTree2 = new jsTree("objTree2");
		//create the root
		objTree2.createRoot("", "<%=loader.getRootNode().getName()%>", "", "", "objTree2");
		<%=toTree(rootNode, "objTree2.root", nodeName2, "Supplier", "objTree2")%>
		<%}%>

	<% if (hasAccessToSearchMatColors){
		loader = new FlexTypeClassificationTreeLoader("com.lcs.wc.color.LCSColor");
			loader.constructTree();
			rootNode = (TreeNode) loader.getRootNode().getChildren().iterator().next();%>

		 var objTree3 = new jsTree("objTree3");
		//create the root
		objTree3.createRoot("", "<%=loader.getRootNode().getName()%>", "", "", "objTree3");
		<%=toTree(rootNode, "objTree3.root", nodeName3, "Color", "objTree3")%>
	<%}%>

	<%  if (hasAccessToSearchMatSamples){
	      loader = new FlexTypeClassificationTreeLoader("com.lcs.wc.sample.LCSSample");
			loader.setRootType(materialSampleRoot);
			loader.constructTree();
			//rootNode = (TreeNode) loader.getRootNode().getChildren().iterator().next();
			rootNode = (TreeNode) loader.getAllNodesTable().get(FormatHelper.getNumericFromOid(materialSampleRootId));%>

		 var objTree4 = new jsTree("objTree4");
		//create the root
		objTree4.createRoot("", "<%=loader.getRootNode().getName()%>", "", "", "objTree4");
		<%=toTree(rootNode, "objTree4.root", nodeName4, "Sample", "objTree4")%>
		<%}%>


    ////////////////////////////////////////////////////////////////////////////
    //  Used only when index search is enabled
    ////////////////////////////////////////////////////////////////////////////
    function setIndexSearchClass(){

        <%if (INDEX_ENABLED) {%>
	        var indexSearchClassDiv = document.getElementById("indexSearchClassDiv");

            var selectString = '<select name="searchTypeClass" id="searchTypeClass" parent="searchTypeClass" onkeydown="typeAhead()">';
            selectString = selectString + '<option value="com.lcs.wc.material.LCSMaterial" selected ><%=FormatHelper.encodeForJavascript(materialLabel)%></option>';
            
            if (document.MAINFORM.includeSupplier.value == 'true') {
                selectString = selectString + '<option value="com.lcs.wc.material.LCSMaterialSupplier" ><%=FormatHelper.encodeForJavascript(materialSupplierLabel)%></option>';
                selectString = selectString + '<option value="com.lcs.wc.supplier.LCSSupplier" ><%=FormatHelper.encodeForJavascript(supplierLabel)%></option>';
            }
    
            if (document.MAINFORM.includeColor.value == 'true') {
                selectString = selectString + '<option value="com.lcs.wc.color.LCSColor" ><%=FormatHelper.encodeForJavascript(colorLabel)%></option>';
                selectString = selectString + '<option value="com.lcs.wc.material.LCSMaterialColor" ><%=FormatHelper.encodeForJavascript(materialColorLabel)%></option>';
            }
    
            if (document.MAINFORM.includeSample.value == 'true') {
                selectString = selectString + '<option value="com.lcs.wc.sample.LCSSample" ><%=FormatHelper.encodeForJavascript(sampleLabel)%></option>';
                selectString = selectString + '<option value="com.lcs.wc.sample.LCSSampleRequest" ><%=FormatHelper.encodeForJavascript(sampleRequestLabel)%></option>';
            }
    
            selectString = selectString + '</select>';
            indexSearchClassDiv.innerHTML = selectString;
        <%}%>
    }
    ////////////////////////////////////////////////////////////////////////////

</script>
<input type="hidden" name="mode" value="">
<input type="hidden" name="fromIndex"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("fromIndex"))%>">
<input type="hidden" name="toIndex"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("toIndex"))%>">
<input type="hidden" name="showAll"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("showAll"))%>">
<input type="hidden" name="sortBy1"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("sortBy1"))%>">

<input type="hidden" name="lastType"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(type)%>">
<input type="hidden" name="userId" value="">

<input type="hidden" name="nameAttribute"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(nameAttribute)%>">
<input type="hidden" name="displayAttribute"
	value="<%=displayAttribute%>">
<!--<input type="hidden" name="viewId" value="<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("viewId"))%>">-->
<input type='hidden' name="typesString"
	value='<%=FormatHelper.encodeAndFormatForHTMLContent(types)%>'>
<input type='hidden' name="relevantActivity"
	value='<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("activity"))%>'>
<input type="hidden" name="selectParameter"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("selectParameter"))%>">
<input type="hidden" name="currentDiv" value="">
<input type="hidden" name="includeSupplier"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(includeSupplier)%>">
<input type="hidden" name="includeColor"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(includeColor)%>">
<input type="hidden" name="includeSample"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(includeSample)%>">
<input type="hidden" name="sampleType"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(sampleTypeId)%>">
<input type="hidden" name="colorType"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(colorTypeId)%>">
<input type="hidden" name="supplierType"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(supplierTypeId)%>">
<input type="hidden" name="searchBySupplier"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(searchBySupplier)%>">
<input type="hidden" name="searchByColor"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(searchByColor)%>">
<input type="hidden" name="searchBySample"
	value="<%=FormatHelper.encodeAndFormatForHTMLContent(searchBySample)%>">



<table width="100%" align="center">
	<tr>
		<td class="PAGEHEADING" colspan="2"><span
			class="PAGEHEADINGTITLE"><%=findPgHead%></td>
	</tr>
	<tr>
		<td class="button" colspan="1" align="left"><a id="SearchButton1"
			class="button" href="javascript:search()"><%=searchLbl%></a></td>
		<!-- Include the buttons/selector for views -->
		<%
		    boolean useUpdate = true;
		%>


		<!--------------Views-------------->
		<td>
			<table>
				<tr>
					<td class="LABEL" nowrap>
						<div id="viewDiv">

							<%
							    if (useViews && !chooser) {
							%>
							<%
							    if (columnOptions != null && columnOptions.size() > 0) {
							%>
							<%=viewsLabel%>:&nbsp;&nbsp;&nbsp;
							<%=fg.createDropDownList(columnOptions, "viewId", viewId, "changeView(this)")%>
							<%
							    if (useUpdate || view != null) {
							%>
							<a id="ViewButton2"
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(updateTableLayoutMoueseOver)%>');"
								onmouseout="return nd();" href="javascript:updateView()"><img
								align="absmiddle" border="0"
								src="<%=WT_IMAGE_LOCATION%>/customize_tablebutton.gif"></a>
							<%
							    }
							%>
							<%
							    } else {
							%>
							<%=viewNoAvailLabel%>
							<%
							    }
							%>
							<a id="ViewButton"
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(createNewTableLayoutMouseOver)%>');"
								onmouseout="return nd();" href="javascript:newView()"><img
								align="absmiddle" border="0"
								src="<%=WT_IMAGE_LOCATION%>/object_new.png"></a>
							<%
							    }
							%>
						</div>
					</td>
					<td>
						<%
						    if (useViews && useFilters && !chooser) {
						%> &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp; <%
     } else {
 %> &nbsp;&nbsp;&nbsp; <%
     }
 %>
					</td>
					<td class="LABEL" nowrap>
						<div id="filterDiv">
							<%
							    if (useFilters && !chooser) {
							%>
							<%
							    if (filterOptions != null && filterOptions.size() > 0) {
							%>
							<%=filtersLabel%>:&nbsp;&nbsp;&nbsp;
							<%=fg.createDropDownList(filterOptions, "filterId", filterId, "changeFilterPreMethod(this)")%>

							<%
							    if (useUpdate || filter != null) {
							%>
							<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(updateFilterMouseOver)%>');"
								onmouseout="return nd();" id="FilterButton2"
								href="javascript:updateFilter()"><img align="absmiddle"
								border="0"
								src="<%=WT_IMAGE_LOCATION%>/customize_tablebutton.gif"></a>
							<%
							    }
							%>
							<%
							    } else {
							%>
							<%=filterNoAvailLabel%>
							<%
							    }
							%>

							<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(createNewFilterMouseOver)%>');"
								onmouseout="return nd();" id="FilterButton"
								href="javascript:newFilter()"><img align="absmiddle"
								border="0" src="<%=WT_IMAGE_LOCATION%>/object_new.png"></a>
							<%
							    if (filter != null) {
												if (FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
							%>
							&nbsp;&nbsp;&nbsp;&nbsp;<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(clearAllMouseOver)%>');"
								onmouseout="return nd();" class="button"
								style="display:; vertical-align: bottom" id="clearAllButton"
								href="javascript:clearAllCriteriaVals()"><%=clearAllButton%></a>
							&nbsp;<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(resetDefaultsMouseOver)%>');"
								onmouseout="return nd();" class="button"
								style="display:; vertical-align: bottom"
								id="resetDefaultsButton"
								href="javascript:resetDefaultValsForTempFilter(document.getElementById('filterId').value)"><%=resetDefaultsButton%></a>
							<%
							    } else {
							%>
							&nbsp;&nbsp;&nbsp;&nbsp;<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(clearAllMouseOver)%>');"
								onmouseout="return nd();" class="button"
								style="display: none; vertical-align: bottom"
								id="clearAllButton" href="javascript:clearAllCriteriaVals()"><%=clearAllButton%></a>
							&nbsp;<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(resetDefaultsMouseOver)%>');"
								onmouseout="return nd();" class="button"
								style="display: none; vertical-align: bottom"
								id="resetDefaultsButton"
								href="javascript:resetDefaultValsForTempFilter(document.getElementById('filterId').value)"><%=resetDefaultsButton%></a>
							<%
							    }
											} else {
							%>
							&nbsp;&nbsp;&nbsp;&nbsp;<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(clearAllMouseOver)%>');"
								onmouseout="return nd();" class="button"
								style="display:; vertical-align: bottom" id="clearAllButton"
								href="javascript:clearAllCriteriaVals()"><%=clearAllButton%></a>
							&nbsp;<a
								onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(resetDefaultsMouseOver)%>');"
								onmouseout="return nd();" class="button"
								style="display: none; vertical-align: bottom"
								id="resetDefaultsButton"
								href="javascript:resetDefaultValsForTempFilter(document.getElementById('filterId').value)"><%=resetDefaultsButton%></a>
							<%
							    }
							%>
						</div>
					</td>
					<%
					    }
					%>
				</tr>
			</table>
		</td>




		<!--------------Views-------------->


	</tr>
	<tr>
		<td></td>
		<td valign="top">
			<table>
				<tr>
					<%
						if ( hasAccessToSearchMatSuppliers ) {
					%>
					<td class="LABEL"><%=includeSupplierLabel%>:</td>
					<td><input id="includeSupplierCheckBox"
						name="includeSupplierCheckBox" type=checkbox
						onClick="respondToClick(this, document.MAINFORM.includeSupplier)"></td>
					<% } 
						if ( hasAccessToSearchMatColors ) {
					%>
					<td class="LABEL">&nbsp;&nbsp;&nbsp;<%=includeColorLabel%>:
					</td>
					<td><input id="includeColorCheckBox"
						name="includeColorCheckBox" type=checkbox
						onClick="respondToClick(this, document.MAINFORM.includeColor)"></td>
					<% }
						if (  hasAccessToSearchMatSamples ) {
					%>
					<td class="LABEL">&nbsp;&nbsp;&nbsp;<%=includeSampleLabel%>:
					</td>
					<td><input id="includeSampleCheckBox"
						name="includeSampleCheckBox" type=checkbox
						onClick="respondToClick(this, document.MAINFORM.includeSample)"></td>
					<% } %>
				</tr>
			</table>
		</td>
	</tr>

	<tr>
		<td valign="top" width="20%">
			<table>
				<tr>
					<td><%=tg.startGroupBorder()%> <%=tg.startTable()%> <%=tg.startGroupTitle()%><%=materialLabel%>&nbsp;<%=typeGrpTle%><%=tg.endTitle()%>
						<%=tg.startGroupContentTable()%>
				<tr>
					<td bgcolor="white" align="left" colspan="2">
						<div id="treeDiv"></div>
					</td>
				</tr>
				<%=tg.endContentTable()%>
				<%=tg.endTable()%>
				<%=tg.endBorder()%>
				</td>
				</tr>
				<% if ( hasAccessToSearchMatSuppliers ) {%>
				<tr>
					<td class="SUBSECTION">
						<div id='supplierTree_plus'>
							<a
								href="javascript:changeDiv('supplierTree');setSearchCriteria('supplier', '<%=supplierRootId%>', 'true', true);"><img
								src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0"
								align="absmiddle"></a>&nbsp;&nbsp; <a
								href="javascript:changeDiv('supplierTree');setSearchCriteria('supplier', '<%=supplierRootId%>', 'true', true);"><%=searchBySupplierLabel%></a>
						</div>
						<div id='supplierTree_minus'>
							<a
								href="javascript:changeDiv('supplierTree');setSearchCriteria('supplier', '', 'false');"><img
								src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0"
								align="absmiddle"></a>&nbsp;&nbsp; <a
								href="javascript:changeDiv('supplierTree');setSearchCriteria('supplier', '', 'false');"><%=searchBySupplierLabel%></a>
						</div>
					</td>

				</tr>

				<tr>
					<td>
						<div id='supplierTree'>

							<%=tg.startGroupBorder()%>
							<%=tg.startTable()%>
							<%=tg.startGroupTitle()%><%=supplierLabel%>&nbsp;<%=typeGrpTle%><%=tg.endTitle()%>
							<%=tg.startGroupContentTable()%>
							<tr>
								<td bgcolor="white" align="left" colspan="2">
									<div id="treeDiv2"></div>
								</td>
							</tr>
							<%=tg.endContentTable()%>
							<%=tg.endTable()%>
							<%=tg.endBorder()%>
						</div>

					</td>
				</tr>
				<% }
				if ( hasAccessToSearchMatColors ) {%>
				<tr>
					<td class="SUBSECTION">
						<div id='colorTree_plus'>
							<a
								href="javascript:changeDiv('colorTree');setSearchCriteria('color', '<%=colorRootId%>', 'true', true);"><img
								src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0"
								align="absmiddle"></a>&nbsp;&nbsp; <a
								href="javascript:changeDiv('colorTree');setSearchCriteria('color', '<%=colorRootId%>', 'true', true);"><%=searchByColorLabel%></a>
						</div>
						<div id='colorTree_minus'>
							<a
								href="javascript:changeDiv('colorTree');setSearchCriteria('color', '', 'false');"><img
								src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0"
								align="absmiddle"></a>&nbsp;&nbsp; <a
								href="javascript:changeDiv('colorTree');setSearchCriteria('color', '', 'false');"><%=searchByColorLabel%></a>
						</div>
					</td>

				</tr>

				<tr>
					<td>
						<div id='colorTree'>

							<%=tg.startGroupBorder()%>
							<%=tg.startTable()%>
							<%=tg.startGroupTitle()%><%=colorLabel%>&nbsp;<%=typeGrpTle%><%=tg.endTitle()%>
							<%=tg.startGroupContentTable()%>
							<tr>
								<td bgcolor="white" align="left" colspan="2">
									<div id="treeDiv3"></div>
								</td>
							</tr>
							<%=tg.endContentTable()%>
							<%=tg.endTable()%>
							<%=tg.endBorder()%>
						</div>

					</td>
				</tr>
				<% } 
				if ( hasAccessToSearchMatSamples ) {%>
				<tr>
				
					<td class="SUBSECTION">
						<div id='sampleTree_plus'>
							<a
								href="javascript:changeDiv('sampleTree');setSearchCriteria('sample', '<%=materialSampleRootId%>', 'true', true);"><img
								src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt="" border="0"
								align="absmiddle"></a>&nbsp;&nbsp; <a
								href="javascript:changeDiv('sampleTree');setSearchCriteria('sample', '<%=materialSampleRootId%>', 'true', true);"><%=searchBySampleLabel%></a>
						</div>
						<div id='sampleTree_minus'>
							<a
								href="javascript:changeDiv('sampleTree');setSearchCriteria('sample', '', 'false');"><img
								src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt="" border="0"
								align="absmiddle"></a>&nbsp;&nbsp; <a
								href="javascript:changeDiv('sampleTree');setSearchCriteria('sample', '', 'false');"><%=searchBySampleLabel%></a>
						</div>
					</td>
					<%=fg.createPlaceholder()%>

				</tr>
			

				<tr>
					<td>
						<div id='sampleTree'>

							<%=tg.startGroupBorder()%>
							<%=tg.startTable()%>
							<%=tg.startGroupTitle()%><%=sampleLabel%>&nbsp;<%=typeGrpTle%><%=tg.endTitle()%>
							<%=tg.startGroupContentTable()%>
							<tr>
								<td bgcolor="white" align="left" colspan="2">
									<div id="treeDiv4"></div>
								</td>
							</tr>
							<%=tg.endContentTable()%>
							<%=tg.endTable()%>
							<%=tg.endBorder()%>
						</div>

					</td>
				</tr>
					<% } %>


			</table>

		</td>
		<td valign="top">
			<div id='keywordDisplay'>
				<%
				    // Added for KEYWORD Index Search
				%>
				<%@ include file="../../../jsp/indexsearch/IndexSearchCriteria.jspf"%>
			</div>
			<div id='quickSearchDisplay'>
				<%@ include file="../main/AgronConfigurableQuickSearch.jspf"%>
			</div>

			<div id='criteriaDisplay'></div>
			<div id='defaultCriteria_div'>
				<%=tg.startGroupBorder()%>
				<%=tg.startTable()%>
				<%=tg.startGroupTitle()%><%=criteriaGrpTle%><%=tg.endTitle()%>
				<%=tg.startGroupContentTable()%>
				<col width="15%"></col>
				<col width="35%"></col>
				<col width="15%"></col>
				<col width="35%"></col>
				<%
				    if (FormatHelper.hasContent(plugIn)) {
				%>
				<jsp:include page="<%=plugIn%>" flush="true">
					<jsp:param name="none" value="" />
				</jsp:include>

				<%
				    }
				%>

				<tr>
					<td colspan='4'>
						<div id="sc1">
							<table>

								<col width="15%">
								<col width="35%">
								<col width="15%">
								<col width="35%">
								<tr>
									<td colspan='4' class='SUBSECTION'><%=materialLabel%></td>
								</tr>
								<tr>

									<%
									    if (FormatHelper.hasContent(nameAttribute)) {
									%>
									<%
									    //firstFieldName = nameAttribute;
									%>
									<%=fg.createTextInput(nameAttribute, nameLabel, request.getParameter(nameAttribute), 30, 30,
						false)%>
									<%
									    }
									%>
									<%
									    if (FormatHelper.hasContent(displayAttribute)) {
									%>
									<%
									    //firstFieldName = flexType.getAttribute(displayAttribute).getSearchCriteriaIndex();
									%>
									<%=flexg.createSearchCriteriaWidget(flexType.getAttribute(displayAttribute), flexType, request)%>
									<%
									    }
									%>
									<%=fg.createPlaceholder()%>
								</tr>

								<tr>
									<%
									    flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
												if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
													out.print(flexg.generateSearchCriteriaInput(flexType, new Hashtable(), false));
												} else {
													out.print(flexg.generateSearchCriteriaInput(flexType, request, false));
												}
									%>
								</tr>

								<tr>
									<td colspan=4 class="SUBSECTION">
										<div id='meta_material_plus'>
											<a href="javascript:changeDiv('meta_material')"><img
												src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_material')"><%=metaLabel%></a>
										</div>
										<div id='meta_material_minus'>
											<a href="javascript:changeDiv('meta_material')"><img
												src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_material')"><%=metaLabel%></a>
										</div>
									</td>
									<%=fg.createPlaceholder()%>

								</tr>

								<tr>
									<td colspan=4>
										<div id='meta_material'>
											<table width=100%>
												<tr>
													<%
													    if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
																	out.print(SearchFormGenerator.createHardAttributeCriteriaInput(flexType,
																			MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, new Hashtable()));
																} else {
																	out.print(SearchFormGenerator.createHardAttributeCriteriaInput(flexType,
																			MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, request));
																}
													%>
												</tr>
											</table>
										</div>
									</td>
								</tr>
							</table>
						</div>
					</td>
				</tr>
				<!-- ////////////////////////////////////////////////-->





				<tr>
					<td colspan='4'>
						<div id="sc2">
							<%
							    if (searchBySupplier.equals("true")) {
							%>
							<table>



								<tr>
									<td colspan=4><hr></td>
								</tr>
								<col width="15%">
								<col width="35%">
								<col width="15%">
								<col width="35%">
								<tr>
									<td colspan='4' class='SUBSECTION'><%=supplierLabel%></td>
								</tr>
								<tr>

									<%
									    if (FormatHelper.hasContent(nameAttribute)) {
									%>
									<%
									    //firstFieldName = nameAttribute;
									%>
									<%=fg.createTextInput(nameAttribute, nameLabel, request.getParameter(nameAttribute), 30, 30,
							false)%>
									<%
									    }
									%>
									<%
									    if (FormatHelper.hasContent(displayAttribute)) {
									%>
									<%
									    //firstFieldName = flexType.getAttribute(displayAttribute).getSearchCriteriaIndex();
									%>
									<%=flexg.createSearchCriteriaWidget(supplierType.getAttribute(displayAttribute),
							supplierType, request)%>
									<%
									    }
									%>
									<%=fg.createPlaceholder()%>
								</tr>

								<tr>
									<%
									    flexg.setScope(null);
													if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
														out.print(flexg.generateSearchCriteriaInput(supplierType, new Hashtable(), false));
													} else {
														out.print(flexg.generateSearchCriteriaInput(supplierType, request, false));
													}
									%>
								</tr>

								<tr>
									<td colspan=4 class="SUBSECTION">
										<div id='meta_supplier_plus'>
											<a href="javascript:changeDiv('meta_supplier')"><img
												src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_supplier')"><%=metaLabel%></a>
										</div>
										<div id='meta_supplier_minus'>
											<a href="javascript:changeDiv('meta_supplier')"><img
												src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_supplier')"><%=metaLabel%></a>
										</div>
									</td>
									<%=fg.createPlaceholder()%>
								</tr>


								<tr>
									<td colspan=4>
										<div id='meta_supplier'>
											<table width=100%>
												<tr>
													<%
													    if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
																		out.print(SearchFormGenerator.createHardAttributeCriteriaInput(supplierType, null, null,
																				new Hashtable()));
																	} else {
																		out.print(SearchFormGenerator.createHardAttributeCriteriaInput(supplierType, null, null, request));
																	}
													%>
												</tr>
											</table>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan=4><hr></td>
								</tr>

								<td colspan='4' class='SUBSECTION'><%=materialSupplierLabel%></td>
								</tr>
								<col width="15%">
								<col width="35%">
								<col width="15%">
								<col width="35%">


								<tr>
									<%
									    if (flexType != null) {
														flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);
														if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
															out.print(flexg.generateSearchCriteriaInput(flexType, new Hashtable(), false));
														} else {
															out.print(flexg.generateSearchCriteriaInput(flexType, request, false));
														}
													}
									%>
								</tr>

								<tr>
									<td colspan=4 class="SUBSECTION">
										<div id='meta_materialSupplier_plus'>
											<a href="javascript:changeDiv('meta_materialSupplier')"><img
												src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_materialSupplier')"><%=metaLabel%></a>
										</div>
										<div id='meta_materialSupplier_minus'>
											<a href="javascript:changeDiv('meta_materialSupplier')"><img
												src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_materialSupplier')"><%=metaLabel%></a>
										</div>
									</td>
									<%=fg.createPlaceholder()%>
								</tr>

								<tr>
									<td colspan=4>
										<div id='meta_materialSupplier'>
											<table width=100%>
												<tr>
													<%
													    if (flexType != null) {
																		if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
																			out.print(SearchFormGenerator.createHardAttributeCriteriaInput(flexType,
																					MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, new Hashtable()));
																		} else {
																			out.print(SearchFormGenerator.createHardAttributeCriteriaInput(flexType,
																					MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, request));

																		}
																	}
													%>
												</tr>
											</table>
										</div>
									</td>
								</tr>
							</table>


							<%
							    }
							%>

						</div>
					</td>
				</tr>


				<!-- ////////////////////////////////////////////////-->



				<tr>
					<td colspan='4'>
						<div id="sc3">


							<%
							    if (searchByColor.equals("true")) {
							%>
							<table>



								<tr>
									<td colspan=4><hr></td>
								</tr>
								<col width="15%">
								<col width="35%">
								<col width="15%">
								<col width="35%">
								<tr>
									<td colspan='4' class='SUBSECTION'><%=colorLabel%></td>
								</tr>
								<tr>

									<%
									    if (FormatHelper.hasContent(nameAttribute)) {
									%>
									<%
									    //firstFieldName = nameAttribute;
									%>
									<%=fg.createTextInput(nameAttribute, nameLabel, request.getParameter(nameAttribute), 30, 30,
							false)%>
									<%
									    }
									%>
									<%
									    if (FormatHelper.hasContent(displayAttribute)) {
									%>
									<%
									    //firstFieldName = flexType.getAttribute(displayAttribute).getSearchCriteriaIndex();
									%>
									<%=flexg.createSearchCriteriaWidget(colorType.getAttribute(displayAttribute), colorType,
							request)%>
									<%
									    }
									%>
									<%=fg.createPlaceholder()%>
								</tr>

								<tr>
									<%
									    flexg.setScope(null);
													if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
														out.print(flexg.generateSearchCriteriaInput(colorType, new Hashtable(), false));
													} else {
														out.print(flexg.generateSearchCriteriaInput(colorType, request, false));
													}
									%>
								</tr>

								<tr>
									<td colspan=4 class="SUBSECTION">
										<div id='meta_color_plus'>
											<a href="javascript:changeDiv('meta_color')"><img
												src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_color')"><%=metaLabel%></a>
										</div>
										<div id='meta_color_minus'>
											<a href="javascript:changeDiv('meta_color')"><img
												src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_color')"><%=metaLabel%></a>
										</div>
									</td>
									<%=fg.createPlaceholder()%>
								</tr>


								<tr>
									<td colspan=4>
										<div id='meta_color'>
											<table width=100%>
												<tr>
													<%
													    if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
																		out.print(SearchFormGenerator.createHardAttributeCriteriaInput(colorType, null, null,
																				new Hashtable()));
																	} else {
																		out.print(SearchFormGenerator.createHardAttributeCriteriaInput(colorType, null, null, request));
																	}
													%>
												</tr>
											</table>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan=4><hr></td>
								</tr>

								<td colspan='4' class='SUBSECTION'><%=materialColorLabel%></td>
								</tr>
								<col width="15%">
								<col width="35%">
								<col width="15%">
								<col width="35%">


								<tr>
									<%
									    if (flexType != null) {
														flexg.setScope(null);
														if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
															out.print(flexg.generateSearchCriteriaInput(materialColorRoot, new Hashtable(), false));
														} else {
															out.print(flexg.generateSearchCriteriaInput(materialColorRoot, request, false));
														}
													}
									%>
								</tr>

								<tr>
									<td colspan=4 class="SUBSECTION">
										<div id='meta_materialColor_plus'>
											<a href="javascript:changeDiv('meta_materialColor')"><img
												src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_materialColor')"><%=metaLabel%></a>
										</div>
										<div id='meta_materialColor_minus'>
											<a href="javascript:changeDiv('meta_materialColor')"><img
												src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_materialColor')"><%=metaLabel%></a>
										</div>
									</td>
									<%=fg.createPlaceholder()%>
								</tr>

								<tr>
									<td colspan=4>
										<div id='meta_materialColor'>
											<table width=100%>
												<tr>
													<%
													    if (flexType != null) {
																		if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
																			out.print(SearchFormGenerator.createHardAttributeCriteriaInput(materialColorRoot, null, null,
																					new Hashtable()));
																		} else {
																			out.print(SearchFormGenerator.createHardAttributeCriteriaInput(materialColorRoot, null, null,
																					request));

																		}
																	}
													%>
												</tr>
											</table>
										</div>
									</td>
								</tr>
							</table>


							<%
							    }
							%>


						</div>
					</td>
				</tr>


				<!-- ////////////////////////////////////////////////-->

				<tr>
					<td colspan='4'>
						<div id="sc4">


							<%
							    if (searchBySample.equals("true")) {
							%>
							<table>



								<tr>
									<td colspan=4><hr></td>
								</tr>
								<col width="15%">
								<col width="35%">
								<col width="15%">
								<col width="35%">
								<tr>
									<td colspan='4' class='SUBSECTION'><%=sampleRequestLabel%>
									</td>
								</tr>
								<%
								    flexg.setScope(com.lcs.wc.sample.SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE);
								%>
								<tr>

									<%
									    if (FormatHelper.hasContent(nameAttribute)) {
									%>
									<%
									    //firstFieldName = nameAttribute;
									%>
									<%=fg.createTextInput(nameAttribute, nameLabel, request.getParameter(nameAttribute), 30, 30,
							false)%>
									<%
									    }
									%>
									<%
									    if (FormatHelper.hasContent(displayAttribute)) {
									%>
									<%
									    //firstFieldName = flexType.getAttribute(displayAttribute).getSearchCriteriaIndex();
									%>
									<%=flexg.createSearchCriteriaWidget(sampleType.getAttribute("requestName"), sampleType,
							request)%>
									<%
									    }
									%>
									<%=fg.createPlaceholder()%>
								</tr>
								<tr>
									<%
									    if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
														out.print(flexg.generateSearchCriteriaInput(sampleType, new Hashtable(), false));
													} else {
														out.print(flexg.generateSearchCriteriaInput(sampleType, request, false));
													}
									%>
								</tr>

								<tr>
									<td colspan=4 class="SUBSECTION">
										<div id='meta_sampleRequest_plus'>
											<a href="javascript:changeDiv('meta_sampleRequest')"><img
												src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_sampleRequest')"><%=metaLabel%></a>
										</div>
										<div id='meta_sampleRequest_minus'>
											<a href="javascript:changeDiv('meta_sampleRequest')"><img
												src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_sampleRequest')"><%=metaLabel%></a>
										</div>
									</td>
									<%=fg.createPlaceholder()%>
								</tr>


								<tr>
									<td colspan=4>
										<div id='meta_sampleRequest'>
											<table width=100%>
												<tr>
													<%
													    if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
																		out.print(SearchFormGenerator.createHardAttributeCriteriaInput(sampleType,
																				SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE, null, new Hashtable()));
																	} else {
																		out.print(SearchFormGenerator.createHardAttributeCriteriaInput(sampleType,
																				SampleRequestFlexTypeScopeDefinition.SAMPLEREQUEST_SCOPE, null, request));
																	}
													%>
												</tr>
											</table>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan=4><hr></td>
								</tr>

								<td colspan='4' class='SUBSECTION'><%=sampleLabel%></td>
								</tr>
								<col width="15%">
								<col width="35%">
								<col width="15%">
								<col width="35%">


								<tr>


									<%
									    flexg.setScope(com.lcs.wc.sample.SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE);
									%>
									<%=flexg.createSearchCriteriaWidget(sampleType.getAttribute("name"), sampleType, request)%>
									<%=fg.createPlaceholder()%>
								</tr>


								<tr>
									<%
									    if (flexType != null) {
														if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
															out.print(flexg.generateSearchCriteriaInput(sampleType, new Hashtable(), false));
														} else {
															out.print(flexg.generateSearchCriteriaInput(sampleType, request, false));
														}
													}
									%>
								</tr>

								<tr>
									<td colspan=4 class="SUBSECTION">
										<div id='meta_sample_plus'>
											<a href="javascript:changeDiv('meta_sample')"><img
												src="<%=WT_IMAGE_LOCATION%>/collapse_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_sample')"><%=metaLabel%></a>
										</div>
										<div id='meta_sample_minus'>
											<a href="javascript:changeDiv('meta_sample')"><img
												src="<%=WT_IMAGE_LOCATION%>/expand_tree.png" alt=""
												border="0" align="absmiddle"></a>&nbsp;&nbsp; <a
												href="javascript:changeDiv('meta_sample')"><%=metaLabel%></a>
										</div>
									</td>
									<%=fg.createPlaceholder()%>
								</tr>

								<tr>
									<td colspan=4>
										<div id='meta_sample'>
											<table width=100%>
												<tr>
													<%
													    if (flexType != null) {
																		if (FormatHelper.hasContent(lastType) && !lastType.equals(type)) {
																			out.print(SearchFormGenerator.createHardAttributeCriteriaInput(sampleType,
																					SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE, null, new Hashtable()));
																		} else {
																			out.print(SearchFormGenerator.createHardAttributeCriteriaInput(sampleType,
																					SampleRequestFlexTypeScopeDefinition.SAMPLE_SCOPE, null, request));

																		}
																	}
													%>
												</tr>
											</table>
										</div>
									</td>
								</tr>
							</table>


							<%
							    }
							%>

						</div>
					</td>
				</tr>

				<!-- ////////////////////////////////////////////////-->
				<tr>
					<td class="button" colspan="1" align="left"><a
						id="SearchButton2" class="button" href="javascript:search()"><%=searchButton%></a>
					</td>
				</tr>
				<%=tg.endContentTable()%>
				<%=tg.endTable()%>
				<%=tg.endBorder()%>
			</div>

		</td>
	</tr>
	<div id="pp"></div>


</table>
<%
    if (chooser) {
				if (!FormatHelper.hasContent(ractivity)) {
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
			lcsContext.filterCache.request = RequestHelper.hashRequest(request);
			if (!TemplateFilterHelper.firstTimeTemplateFilterSearch(request) && filter != null) {
				lcsContext.filterCache.request = TemplateFilterHelper.getTemplateFilterParameters(filter,
						RequestHelper.hashRequest(request));
			} else {
				lcsContext.filterCache.request = RequestHelper.hashRequest(request);
			}
%>

<script>
    doLoad();
    <%=nodeName%>.expand()
    objTree.root.expandAll()
    //Expand object trees only when they are available.
	if(("objTree2" in window) && objTree2){
		<%=nodeName2%>.expand()
		objTree2.root.expandAll()
		toggleDiv('supplierTree');
		toggleDiv('supplierTree_minus');
	}
	if(("objTree3" in window) && objTree3){
		<%=nodeName3%>.expand()
		objTree3.root.expandAll()
		toggleDiv('colorTree');
		toggleDiv('colorTree_minus');
	}
	if(("objTree4" in window) && objTree4){
		<%=nodeName4%>.expand()
	    objTree4.root.expandAll()
		toggleDiv('sampleTree');
		toggleDiv('sampleTree_minus');
	}
<%if (FormatHelper.hasContent(type)) {%>
//   chooseType('<%=type%>', 'Material');

<%}%>



<%if (searchBySupplier.equals("true")) {%>
    //chooseType('<%=FormatHelper.encodeForJavascript(supplierTypeId)%>', 'Supplier');
	changeDiv('supplierTree');
	setSearchCriteria('supplier', '<%=FormatHelper.encodeForJavascript(supplierTypeId)%>', 'true', false);
	toggleDiv('meta_supplier_minus');
	toggleDiv('meta_supplier');
	toggleDiv('meta_materialSupplier_minus');
	toggleDiv('meta_materialSupplier');
<%} else if (includeSupplier.equals("true")) {%>
  	   var supplierCheckbox = document.getElementById("includeSupplierCheckBox");
	   supplierCheckbox.checked = true;
	   <%if (lcsContext.isVendor) {%>
		supplierCheckbox.disabled = true;
	   <%}%>
<%}

			if (searchByColor.equals("true")) {%>
    //chooseType('<%=FormatHelper.encodeForJavascript(colorTypeId)%>', 'Color');
	changeDiv('colorTree');
	setSearchCriteria('color', '<%=FormatHelper.encodeForJavascript(colorTypeId)%>','true', false);
	toggleDiv('meta_color_minus');
	toggleDiv('meta_color');
	toggleDiv('meta_materialColor_minus');
	toggleDiv('meta_materialColor');

<%} else if (includeColor.equals("true")) {%>
		var colorCheckbox = document.getElementById("includeColorCheckBox");
		colorCheckbox.checked = true;

<%}

			if (searchBySample.equals("true")) {%>
    //chooseType('<%=FormatHelper.encodeForJavascript(sampleTypeId)%>', 'Sample');
	changeDiv('sampleTree');
	setSearchCriteria('sample', '<%=FormatHelper.encodeForJavascript(sampleTypeId)%>', 'true', false);
	toggleDiv('meta_sample_minus');
	toggleDiv('meta_sample');


	toggleDiv('meta_sampleRequest_minus');
	toggleDiv('meta_sampleRequest');

	if(selectedSampleNode == '<%=colorDevelopmentSampleTypeId%>'){

		var targetCheckbox = document.getElementById("includeColorCheckBox");
		targetCheckbox.checked = true;
		targetCheckbox.disabled = true;
		document.MAINFORM.includeColor.value = "true";
    }   

<%} else if (includeSample.equals("true")) {%>
  	   var sampleCheckbox = document.getElementById("includeSampleCheckBox");
	   sampleCheckbox.checked = true;

<%}%>

var materialDiv = document.getElementById("meta_material_minus");
if(materialDiv){
   toggleDiv('meta_material_minus');
}

materialDiv = document.getElementById("meta_material");

if(materialDiv){
   toggleDiv('meta_material');
}


</script>
<script>
    divHash['defaultCriteria_div'] = document.getElementById('defaultCriteria_div').innerHTML;
    document.getElementById('defaultCriteria_div').innerHTML = '';
</script>


<script>

        <%String divId = "defaultCriteria_div";%>

        <%if (filter != null) {
				divId = FormatHelper.getObjectId(filter) + "_div";%>
            runPostAjaxRequest(location.protocol + '//' + location.host + urlContext_masterController_url, 'activity=FIND_REPORT_FILTER&action=AJAXSEARCH&filterId=<%=FormatHelper.getObjectId(filter)%>', 'loadFilterToPage');

        <%} else {%>
            criteriaDisplay.innerHTML = divHash['defaultCriteria_div'];
            parseCalendarTags(criteriaDisplay.innerHTML);
        <%}%>


        showDiv('criteriaDisplay');
        document.MAINFORM.currentDiv.value='<%=divId%>';
    <%if ("defaultCriteria_div".equals(divId) && !"nothing".equals(firstFieldName)) {%>
        document.MAINFORM.<%=firstFieldName%>.focus();
        document.MAINFORM.<%=firstFieldName%>.focus();
    <%}%>

</script>
<div id="loadedViewDiv" style='display: none;'>
	<%
	    if (useViews && !chooser) {
	%>
	<%=viewsLabel%>:&nbsp;&nbsp;&nbsp;
	<%=fg.createDropDownList(new HashMap(), "viewId", viewId, "changeView(this)")%>
	<%
	    if (useUpdate || view != null) {
	%>
	<a id="ViewButton2"
		onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(updateTableLayoutMoueseOver)%>');"
		onmouseout="return nd();" href="javascript:updateView()"><img
		align="absmiddle" border="0"
		src="<%=WT_IMAGE_LOCATION%>/customize_tablebutton.gif"></a>
	<%
	    }
	%>

	<a id="ViewButton"
		onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(createNewTableLayoutMouseOver)%>');"
		onmouseout="return nd();" href="javascript:newView()"><img
		align="absmiddle" border="0"
		src="<%=WT_IMAGE_LOCATION%>/object_new.png"></a>
	<%
	    }
	%>
</div>

<div id="emptyViewDiv" style='display: none;'>
	<%
	    if (useViews && !chooser) {
	%>
	<%=viewNoAvailLabel%>
	<a id="ViewButton"
		onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(createNewTableLayoutMouseOver)%>');"
		onmouseout="return nd();" href="javascript:newView()"><img
		align="absmiddle" border="0"
		src="<%=WT_IMAGE_LOCATION%>/object_new.png"></a>
	<%
	    }
	%>

</div>

<div id="loadedFilterDiv" style='display: none;'>
	<%
	    if (useFilters && !chooser) {
	%>
	<%=filtersLabel%>:&nbsp;&nbsp;&nbsp;
	<%=fg.createDropDownList(new HashMap(), "filterId", filterId, "changeFilterPreMethod(this)")%>

	<%
	    if (useUpdate || filter != null) {
	%>
	<a
		onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(updateFilterMouseOver)%>');"
		onmouseout="return nd();" id="FilterButton2"
		href="javascript:updateFilter()"><img align="absmiddle" border="0"
		src="<%=WT_IMAGE_LOCATION%>/customize_tablebutton.gif"></a>
	<%
	    }
	%>
	<%
	    }
	%>
	<a
		onmouseover="return overlib('<%=FormatHelper.encodeForJavascript(createNewFilterMouseOver)%>');"
		onmouseout="return nd();" id="FilterButton"
		href="javascript:newFilter()"><img align="absmiddle" border="0"
		src="<%=WT_IMAGE_LOCATION%>/object_new.png"></a>
	&nbsp;&nbsp;&nbsp;&nbsp;<a
		onmouseover="return overlib('<%=FormatHelper.formatJavascriptString(clearAllMouseOver)%>');"
		onmouseout="return nd();" class="button" style="display: none"
		id="clearAllButton" href="javascript:clearAllCriteriaVals()"><%=clearAllButton%></a>
	&nbsp;<a
		onmouseover="return overlib('<%=FormatHelper.formatJavascriptString(resetDefaultsMouseOver)%>');"
		onmouseout="return nd();" class="button" style="display: none"
		id="resetDefaultsButton"
		href="javascript:resetDefaultValsForTempFilter(document.getElementById('filterId').value)"><%=resetDefaultsButton%></a>

</div>

<div id="emptyFilterDiv" style='display: none;'>
	<%
	    if (useFilters && !chooser) {
	%>

	<%=filterNoAvailLabel%>
	<%
	    }
	%>
	<a
		onmouseover="return overlib('<%=FormatHelper.encodeForHTMLThenJavascript(createNewFilterMouseOver)%>');"
		onmouseout="return nd();" id="FilterButton"
		href="javascript:newFilter()"><img align="absmiddle" border="0"
		src="<%=WT_IMAGE_LOCATION%>/object_new.png"></a>
	&nbsp;&nbsp;&nbsp;&nbsp;<a
		onmouseover="return overlib('<%=FormatHelper.formatJavascriptString(clearAllMouseOver)%>');"
		onmouseout="return nd();" class="button" style="display: none"
		id="clearAllButton" href="javascript:clearAllCriteriaVals()"><%=clearAllButton%></a>
	&nbsp;<a
		onmouseover="return overlib('<%=FormatHelper.formatJavascriptString(resetDefaultsMouseOver)%>');"
		onmouseout="return nd();" class="button" style="display: none"
		id="resetDefaultsButton"
		href="javascript:resetDefaultValsForTempFilter(document.getElementById('filterId').value)"><%=resetDefaultsButton%></a>

</div>

<script>
var loadedViewDiv = document.getElementById("loadedViewDiv");
var emptyViewDiv = document.getElementById("emptyViewDiv");
var loadedFilterDiv = document.getElementById("loadedFilterDiv");
var emptyFilterDiv = document.getElementById("emptyFilterDiv");

var viewFilterWidgets =  new Array;
viewFilterWidgets["loadedViewDiv"] = loadedViewDiv.innerHTML;
loadedViewDiv.innerHTML = "";

viewFilterWidgets["emptyViewDiv"] = emptyViewDiv.innerHTML;
emptyViewDiv.innerHTML = "";

viewFilterWidgets["loadedFilterDiv"] = loadedFilterDiv.innerHTML;
loadedFilterDiv.innerHTML = "";

viewFilterWidgets["emptyFilterDiv"] = emptyFilterDiv.innerHTML;
emptyFilterDiv.innerHTML = "";

<%if (INDEX_ENABLED) {%>
    setIndexSearchClass();
<%}%>
<%if (FormatHelper.hasContent(request.getParameter("searchTypeClass"))) {%>
	var selectedOption = '<%=FormatHelper.encodeForJavascript(request.getParameter("searchTypeClass"))%>';
	var searchTypeClassSelect = document.MAINFORM.searchTypeClass;
	for (var j = 0; j < searchTypeClassSelect.length; j++) {
		if (searchTypeClassSelect.options[j].value == selectedOption) {
			searchTypeClassSelect.options[j].selected = true;
			break;
		}
	}
<%}%>
	
</script>
