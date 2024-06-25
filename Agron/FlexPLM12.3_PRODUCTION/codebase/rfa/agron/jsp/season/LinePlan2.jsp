<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>
<%-- April 2005, this file is used for the Mass Copy functionality --%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: View
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.*,
            com.lcs.wc.client.web.*,
            com.lcs.wc.client.*,
            com.lcs.wc.util.*,
            com.lcs.wc.product.*,
            com.lcs.wc.season.*,
            com.lcs.wc.season.query.*,
            com.lcs.wc.infoengine.client.web.*,
            com.lcs.wc.flextype.*,
            com.lcs.wc.foundation.*,
            com.lcs.wc.report.*,
            wt.util.*,
            wt.workflow.work.WfAssignedActivity,
			com.lcs.wc.season.SeasonProductLocator,
            com.google.inject.Guice,
            com.google.inject.Inject,
            com.google.inject.Injector,
            java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="seasonModel" scope="request" class="com.lcs.wc.season.LCSSeasonClientModel" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="linePlanConfig" scope="session" class="com.lcs.wc.season.LinePlanConfig" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIC JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

   String noSelectedItemsFromAvailableList = WTMessage.getLocalizedMessage ( RB.MAIN, "noSelectedItemsFromAvailableList_ALRT",RB.objA ) ;
   String selectLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "select_LBL",RB.objA ) ;
   String cancelButton = WTMessage.getLocalizedMessage ( RB.MAIN, "cancel_Btn",RB.objA ) ;
   String youCanOnlySelectOneProductAtATimeAlrt = WTMessage.getLocalizedMessage ( RB.SEASON, "youCanOnlySelectOneProductAtATime_ALRT" , RB.objA );
   String fromLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "from_LBL",RB.objA ) ;
   String massCopyProductName = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_PRODUCT_NAME_LBL",RB.objA ) ;
   String massCopyProductType = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_PRODUCT_TYPE_LBL",RB.objA ) ;
   String allLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL",RB.objA ) ;
%>
<%!

	public static String getUrl = "";
    public static String WindchillContext = "/Windchill";
    public static boolean newBOM = LCSProperties.getBoolean("com.lcs.wc.product.PDFProductSpecificationGenerator.newPrintBOM");

    static 
    {
        try
        {
            WTProperties wtproperties = WTProperties.getLocalProperties();
			getUrl = wtproperties.getProperty("wt.server.codebase","");
			WindchillContext = "/" + wtproperties.getProperty("wt.webapp.name");
            instance = wt.util.WTProperties.getLocalProperties().getProperty ("wt.federation.ie.VMName");
        }
        catch(Throwable throwable)
        {
            throw new ExceptionInInitializerError(throwable);
        }
    }
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String JSPNAME = "LinePlan";
    public static final boolean VIEW_SEASONPRODUCT_IN_SAME_WINDOW = LCSProperties.getBoolean("jsp.season.LinePlan.viewProductSeasonInSameWindow");
    public static final boolean FILTERS_ON_NEW_VIEW = !LCSProperties.getBoolean("jsp.season.LinePlan.skipFiltersOnNewView");
    public static String instance = "";
    public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");


%>

<%!

//////  Needed for Mass Copy
    public static Map findChildren(FlexType productType)  {
	
		Map productTypesMap = new HashMap();
        try {
			String rootName = productType.getFullNameDisplay(true);
            String rootId = productType.getIdNumber();
            //add code to judge whether the type is selectable or not
            if(productType.isTypeActive())  productTypesMap.put(rootId, rootName);
            Collection children = productType.getAllChildren();
            Iterator childIter = children.iterator();
            while(childIter.hasNext()){
                FlexType fChild = (FlexType)childIter.next();
				String childName = fChild.getFullNameDisplay(true);
        	String newcOid = fChild.getIdNumber();
			//add code to judge whether the type is selectable or not
			if(fChild.isTypeActive())  	productTypesMap.put(newcOid,childName);
            }
        
             
        }  catch (WTException e) {
              e.printStackTrace();
        }     
		
		return productTypesMap;
	}
	

%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

	String typeSelectionId = request.getParameter("typeSelectionId");

    String errorMessage = request.getParameter("errorMessage");


    LCSSeason season = seasonModel.getBusinessObject();
    FlexType productType = season.getProductType();

    String pTypeName = LinePlanConfig.getLinePlanConfigKey(request, productType);
    boolean linePlanChooser = FormatHelper.parseBoolean(request.getParameter("linePlanChooser"));

    FlexTypeAttribute att = null;

    ///////////////////////////////////////////////////
    // RETRIEVE CONFIGURATION INFORMATION FROM THE
    // REQUEST
    ///////////////////////////////////////////////////
    boolean runLinePlan = true;//"true".equals(request.getParameter("runLinePlan"));
    boolean showFilters = false;//(!runLinePlan || "true".equals(request.getParameter("showFilters")));
    boolean editLinePlan = "true".equals(request.getParameter("editLinePlan"));
    String editLinePlanScope = request.getParameter("editLinePlanScope");
    String linePlanLevel = (String) linePlanConfig.get(pTypeName, "linePlanLevel");
    if(!FormatHelper.hasContent(linePlanLevel) && "true".equals(request.getParameter("csv"))){
        linePlanLevel = request.getParameter("linePlanLevelHold");
    }
    String linePlanView = (String) linePlanConfig.get(pTypeName, "viewId");
    String linePlanChooserTitle = request.getParameter("linePlanChooserTitle");
    String linePlanChooserType = request.getParameter("linePlanChooserType");
    String linePlanChooserReturnMethod = request.getParameter("linePlanChooserReturnMethod");
    boolean copyProductFromSeasonCheck = "completeCopyProductFromSeason".equalsIgnoreCase(linePlanChooserReturnMethod);


    boolean skuChooser = "SKU".equals(linePlanChooserType);
    boolean sourcingChooser = "SOURCING".equals(linePlanChooserType);
    if(skuChooser){
        linePlanLevel = FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL;
    } else if(linePlanChooser){
        linePlanLevel = FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL;
    } 
 


	///////////////////Mass Copy///////////////////////////////////////////////////////////////////
	Collection data2 = new ArrayList();
    com.lcs.wc.season.query.LineSheetQuery lsqn = null;
    LineSheetQueryOptions options = new LineSheetQueryOptions();
    options.setSeason(season);
    
    
    Injector injector = Guice.createInjector(new LSQModule());
    lsqn = injector.getInstance(com.lcs.wc.season.query.LineSheetQuery.class);
	String workNumber="";
    Collection productFlexObjects = lsqn.getLineSheetResults(options);

	Iterator productFlexObjectsItr = productFlexObjects.iterator();
		while(productFlexObjectsItr.hasNext()){
			FlexObject productFlexObject = (FlexObject)productFlexObjectsItr.next();
			
			FlexType productFlexType = FlexTypeCache.getFlexType("" + productFlexObject.get("LCSPRODUCT.BRANCHIDA2TYPEDEFINITIONREFE"));//currentProduct.getFlexType();	
            
			String currentTypeId = productFlexType.getIdNumber();
            
			if(currentTypeId.equalsIgnoreCase(typeSelectionId)){
	            FlexObject dataRow2 = new FlexObject();
	            String oid = "VR:com.lcs.wc.product.LCSProduct:" + FormatHelper.applyFormat((String)productFlexObject.get("LCSPRODUCTSEASONLINK.PRODUCTSEASONREVID"), FormatHelper.LONG_FORMAT);
				
					workNumber="";
				LCSProduct latestProdObject = (LCSProduct)LCSQuery.findObjectById(oid);
				latestProdObject = SeasonProductLocator.getProductARev(latestProdObject);
					Long workNum = (Long) latestProdObject.getValue("agrWorkOrderNoProduct");
				     if(workNum!=null){
						 workNumber = String.valueOf(workNum);
						}
                        //SKIP already copied product 						
						if(workNumber.startsWith("-")){
								continue;								
							}
			      dataRow2.setData("workNumber", workNumber);
				
				// get the full flextype of the current product and put it into the datarow
				dataRow2.setData("type", productFlexType.getFullNameDisplay(true));
			
				dataRow2.setData("name", productFlexObject.getData(productFlexType.getAttribute("productName").getSearchResultIndex()) );
				dataRow2.setData("oid", oid);
				data2.add(dataRow2);
				
			}
			
		}
		
		Collection columnsNew = new ArrayList();
	       		
		TableColumn acolumn = new TableColumn(); 
        acolumn.setDisplayed(true); 
    	acolumn.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel)); 
    	acolumn.setFormat("UNFORMATTED_HTML");
    	acolumn.setLinkTableIndex("oid"); 
    	acolumn.setConstantDisplay(true); 
    	CheckBoxTableFormElement fe = new CheckBoxTableFormElement(); 
    	fe.setValueIndex("oid"); 
    	fe.setName("selectedIds"); 
    	acolumn.setFormElement(fe); 
    	acolumn.setFormatHTML(false); 
    	acolumn.setColumnWidth("1%"); 
    	columnsNew.add(acolumn);
		
       TableColumn columnNew = new TableColumn();
       //Add Work# Column START
        columnNew.setDisplayed(true);
        columnNew.setTableIndex("workNumber");
        columnNew.setHeaderLabel("Work #");
        columnNew.setFormatHTML(true);
        columnsNew.add(columnNew);
      //Add Work# Column END
      
        columnNew = new TableColumn();
        columnNew.setDisplayed(true);
        columnNew.setTableIndex("name");
		columnNew.setLinkMethod("viewObject");
    	columnNew.setLinkTableIndex("oid");
        columnNew.setHeaderLabel(massCopyProductName);
		columnNew.setFormatHTML(true);
        columnsNew.add(columnNew);
		
        columnNew = new TableColumn();
        columnNew.setDisplayed(true);
        columnNew.setTableIndex("type");
        columnNew.setHeaderLabel(massCopyProductType);
        columnNew.setFormatHTML(true);
        columnsNew.add(columnNew);		

		
	//////////////////////////////  Mass Copy end   //////////////////////////////////////////////////////////////////////////
%>
		




<%-- LINE PLAN JAVASCRIPT METHODS --%>
<script>

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



   function select1(){
   	 
	  var fromSeasonName = document.MAINFORM.copyFromSeason.value;

      var id='noneSelected';

      if(document.MAINFORM.selectedIds.checked){
         id = document.MAINFORM.selectedIds.value;
      }
      if(id == 'noneSelected') {
            alert('<%= FormatHelper.formatJavascriptString(noSelectedItemsFromAvailableList ,false)%>');
      } else {
            // EVALUATE THE RETURN CALL
            var returnMethod = '<%= FormatHelper.encodeForJavascript(linePlanChooserReturnMethod) %>';
            var methodCall = 'opener.' + returnMethod + '(id, fromSeasonName)';
            eval(methodCall);
      }

   }

   function choseOne(oid) {


				  
      var returnMethod = '<%= FormatHelper.encodeForJavascript(linePlanChooserReturnMethod) %>';
      var methodCall = 'opener.' + returnMethod + "('|~*~|' + oid)";
      eval(methodCall);
      if(chooserWindow){
        chooserWindow.close();
      }
   }

    <% if(linePlanChooser){ %>


   function select(){
   	
	  var fromSeasonName = document.MAINFORM.copyFromSeason.value;
      var group = document.MAINFORM.selectedIds;
      if(!hasContent(group.length)){
        return select1();
      }
      var checked = false;
      var ids = 'noneSelected';

      // GROUP THE SELECED IDS INTO A MOA STRING
      for (var k =0; k < group.length; k++)  {
         if (group[k].checked)   {
            if (ids=='noneSelected') {
              ids='';
            }
            ids = buildMOA(ids, group[k].value);
         }
      }

      if(ids == 'noneSelected') {
            alert('<%= FormatHelper.formatJavascriptString(noSelectedItemsFromAvailableList ,false)%>');
      } else {
            // EVALUATE THE RETURN CALL
        var checkedCount = 0;
        for (var k =0; k < group.length; k++)  {
            if (group[k].checked)   {
                checkedCount ++;
            }
        }
        <%
        //boolean copyProductFromSeasonCheck = "completeCopyProductFromSeason".equalsIgnoreCase(linePlanChooserReturnMethod);

        %>
        if(<%=copyProductFromSeasonCheck%> && (checkedCount > 1)){
            alert('<%= FormatHelper.formatJavascriptString(youCanOnlySelectOneProductAtATimeAlrt, false) %>');
        }else{
            var returnMethod = '<%= FormatHelper.encodeForJavascript(linePlanChooserReturnMethod) %>';
            <% if(sourcingChooser && FormatHelper.hasContent(request.getParameter("specPages"))){ %>
            var chosenPages = '<%= FormatHelper.encodeForJavascript(request.getParameter("specPages")) %>';
            var methodCall = 'opener.' + returnMethod + '(ids, chosenPages)';
            <% } else { %>
            var methodCall = 'opener.' + returnMethod + '(ids, fromSeasonName, document.MAINFORM.copyFromSeasonId.value)';
            <% } %>
            eval(methodCall);
        }
      }
   }

   <% } // END IF linePlanChooser %>
   
function resetTable(typeSelectionId)	{
	
	document.MAINFORM.typeSelectionId.value = typeSelectionId;
	submitForm();

}

function newCancel()	{

	window.close();

}

</script>
<input type="hidden" name="typeSelectionId" value="">
<input type="hidden" name="editLinePlan" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("editLinePlan"))%>">
<input type="hidden" name="editLinePlanScope" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("editLinePlanScope"))%>">
<input type="hidden" name="runLinePlan" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("runLinePlan")) %>">
<input type="hidden" name="linePlanView" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("linePlanView")) %>">
<input type="hidden" name="showFilters" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("showFilters")) %>">
<input type="hidden" name="pTypeName" value="<%= pTypeName %>">
<input type="hidden" name="sortBy1" value="<%= FormatHelper.format((String)linePlanConfig.get(pTypeName, "sortBy1")) %>">
<input type="hidden" name="rootTypeId" value="">
<input type="hidden" name="multiple" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("multiple")) %>">
<input type="hidden" name="newIds" value="">
<input type="hidden" name="setLinePlan" value="true">
<input type="hidden" name="linePlanLevelHold" value="<%= FormatHelper.encodeForHTMLAttribute(linePlanLevel) %>">
<input type="hidden" name="setWorkMode" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("setWorkMode")) %>">
<input type="hidden" name="linePlanChooser" value="<%= linePlanChooser %>">
<input type="hidden" name="linePlanChooserTitle" value="<%= FormatHelper.encodeForHTMLAttribute(linePlanChooserTitle) %>">
<input type="hidden" name="linePlanChooserType" value="<%= FormatHelper.encodeForHTMLAttribute(linePlanChooserType) %>">
<input type="hidden" name="linePlanChooserReturnMethod" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("linePlanChooserReturnMethod")) %>">
<input type="hidden" name="maintainHiddenColumns" value="">
<input type="hidden" name="sortColumn" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("sortColumn"))%>">
<input type="hidden" name="scrollX" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("scrollX"))%>">
<input type="hidden" name="scrollY" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("scrollY"))%>">
<input type="hidden" name="pagingStart" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("pagingStart"))%>">
<input type="hidden" name="pagingEnd" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("pagingEnd"))%>">
<input type="hidden" name="contextSKUId" value="">
<input type="hidden" name="copyFromSeason" value="<%=FormatHelper.encodeForHTMLAttribute(season.getName())%>">
<input type="hidden" name="copyFromSeasonId" value="<%=FormatHelper.encodeForHTMLAttribute(FormatHelper.getVersionId(season))%>">


<table width="100%" cellpadding="1" cellspacing="1">

    <% if(FormatHelper.hasContent(errorMessage)) { %>
        <tr>
            <td class="ERROR" colspan="4">
                <%= FormatHelper.encodeForHTMLContent(java.net.URLDecoder.decode(errorMessage, defaultCharsetEncoding)) %>
            </td>
        </tr>
    <% } %>


<%
////////////////////////////////////////////////////////////////////////
// HEADER DISPLAYS ALL SPACE ABOVE THE TABLE SECTION
////////////////////////////////////////////////////////////////////////
%>
<td nowrap class="PAGEHEADINGTITLE">
<%= FormatHelper.encodeAndFormatForHTMLContent(java.net.URLDecoder.decode(linePlanChooserTitle, defaultCharsetEncoding)) %>:&nbsp;&nbsp; <%= fromLabel %> <%= FormatHelper.encodeAndFormatForHTMLContent(season.getValue("seasonName").toString()) %>
</td>
<td align="right">
<% 
    Map productSeasonTypes = findChildren(season.getProductType());
%>
<%= fg.createDropDownListWidget(massCopyProductType + ":  ", productSeasonTypes, "productTypeData", typeSelectionId, "javascript:resetTable(this.options[this.selectedIndex].value)", false, true)  %>

</td>


    <tr> 
        <td class="button" align="left">
	   <a class="button" href="javascript:select()"><%= selectLabel %></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	   <a class="button" href="javascript:newCancel()"><%= cancelButton %></a>
        </td>
    </tr> 
	
</table>

<%= tg.drawTable(data2,columnsNew,"", false, false, false) %>

<table>
 <tr> 
        <td class="button" align="left">
       <a class="button" href="javascript:select()"><%= selectLabel %></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
       <a class="button" href="javascript:newCancel()"><%= cancelButton %></a>
        </td>
    </tr> 
    </table>