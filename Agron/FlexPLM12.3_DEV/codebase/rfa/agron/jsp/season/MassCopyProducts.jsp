<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>
<%-- Doug Ford 02/07/07 Liz Mass Copy  --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.season.LCSSeason,
                com.lcs.wc.season.SeasonProductLocator,
                com.lcs.wc.foundation.LCSQuery,
				wt.util.*,
                com.lcs.wc.product.*,
                com.lcs.wc.specification.*,
                java.util.*"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="productModel" scope="request" class="com.lcs.wc.product.LCSProductClientModel" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>


<%!

public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
public static FlexType bagFlexType;
public static String backPackFlexTypeKey="";
public static String BAKCPACK_FLEXTYPE_DISPLAY="Product\\Adidas\\Backpacks";
public static String BAG_FLEXTYPE_DISPLAY="Product\\Adidas\\Bags";

	public static String createNameField(String prodName, int count, FormGenerator fg){
        String massCopyNewProdName = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_NEW_PRODUCT_LBL",RB.objA ) ;
        String massCopyCopyOf = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_COPY_OF_LBL",RB.objA ) ;

		StringBuffer buffer = new StringBuffer();

		buffer.append("<table>");
		buffer.append("<tr>");
		buffer.append(fg.createTextInput("newNameField" + count,massCopyNewProdName + ": ",massCopyCopyOf + " " + prodName, 60, true));
		buffer.append("</tr>");
		buffer.append("</table>");

		return buffer.toString();
	}
	
    public static Map findChildren(FlexType productType)  {
	
		Map productTypesMap = new HashMap();
        try {
            //String rootName = productType.getFullName();
            if(productType.isTypeActive()){
				String rootName = productType.getFullNameDisplay(true);
	            String rootId = FormatHelper.getNumericObjectIdFromObject(productType);
	            productTypesMap.put(rootId, rootName);
            }
 
            Collection children = productType.getAllChildren();
            Iterator childIter = children.iterator();
            while(childIter.hasNext()){
                FlexType fChild = (FlexType)childIter.next();
                if(!fChild.isTypeActive()){
                	continue;
                }

                String childName = fChild.getFullNameDisplay(true);
	        	String newcOid = FormatHelper.getNumericObjectIdFromObject(fChild);
	        	//In latest season hide backpack in  product type drop down and make BAG type default
	            if (childName.contains(BAG_FLEXTYPE_DISPLAY)) {
					bagFlexType = fChild;
				} else if (childName.contains(BAKCPACK_FLEXTYPE_DISPLAY)) {
					backPackFlexTypeKey = newcOid;
				}
				productTypesMap.put(newcOid, childName);
			}

		} catch (WTException e) {
			e.printStackTrace();
		}

		return productTypesMap;
	}%>
<%
    flexg.setCreate(true);
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
//setting up which RBs to use

String copyProductPgHead = WTMessage.getLocalizedMessage ( RB.PRODUCT, "copyProduct_PG_HEAD", RB.objA ) ;
String productIdGrpTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "productId_GRP_TLE", RB.objA ) ;
String typeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "type_LBL", RB.objA ) ;
String saveButton = WTMessage.getLocalizedMessage ( RB.MAIN, "next_BTN", RB.objA ) ;
String cancelButton = WTMessage.getLocalizedMessage ( RB.MAIN, "cancel_Btn", RB.objA ) ;
String additionalCopyOptionsGrpTle = WTMessage.getLocalizedMessage ( RB.PRODUCT, "additionalCopyOptions_GRP_TLE", RB.objA ) ;
String fromLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "from_LBL",RB.objA ) ;
String addToLabel = WTMessage.getLocalizedMessage ( RB.PRODUCT, "addTo_LBL", RB.objA ) ;

String massCopySrcProdType = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_SOURCE_TYPE_LBL",RB.objA ) ;
String massCopyDestinationProdType = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_DESTINATION_TYPE_LBL",RB.objA ) ;
String massCopyCopyMode = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_COPY_MODE_LBL",RB.objA ) ;
String massCopySourceProdName = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_SOURCE_NAME_LBL",RB.objA ) ;
String massCopyNewProdName = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_NEW_PRODUCT_LBL",RB.objA ) ;
String massCopyCopyOf = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_COPY_OF_LBL",RB.objA ) ;

String massCopyProducts = WTMessage.getLocalizedMessage ( RB.SEASON, "massCopyProduct_PG_TLE",RB.objA ) ;

String selectButton2 = WTMessage.getLocalizedMessage ( RB.MAIN, "select_Btn", RB.objA ) ;
String cancelButton2 = WTMessage.getLocalizedMessage ( RB.MAIN, "cancel_Btn", RB.objA ) ;

   LCSSeason season = (LCSSeason)(new LCSProductQuery()).findObjectById(request.getParameter("seasonId"));
   FlexObject productData = new FlexObject();
   
	
   flexg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
   flexg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);

   String testOids = request.getParameter("oids");
	
	// this code separates the individual oids being passed in then puts the name and type into the data row
	Collection ids = MOAHelper.getMOACollection(testOids);
	FlexObject dataRow = null;
	Collection data = new ArrayList();
    Iterator iterator = LCSQuery.getObjectsFromCollection(ids,2).iterator();
	LCSProduct prod = new LCSProduct();
	FlexType productType = new FlexType();
	String prodName = "";
	String productID;
	String workNumber = "";
	int count = 0;
    while(iterator.hasNext()){
		
		prod = (LCSProduct) iterator.next();
        
		dataRow = new FlexObject();
		// need to get the flextype of the current product to get the full flex type path
		productType = prod.getFlexType();
		
		// get the full flextype of the current product and put it into the datarow
		dataRow.setData("type", productType.getFullNameDisplay(true));
		// get the name of the current product and put into the datarow
        prodName = prod.getName();
        dataRow.setData("name", prodName);
		dataRow.setData("oid", FormatHelper.getVersionId(prod));
		LCSProduct latestProdObject = SeasonProductLocator.getProductARev(prod);
		workNumber="";
		Long workNum = (Long) latestProdObject.getValue("agrWorkOrderNoProduct");
						if(workNum!=null){
						 workNumber = String.valueOf(workNum);
						}
	        dataRow.setData("workNumber", workNumber);
		//  have to check to see if the product name is derived, if so, user can't change it, if not then user can specify a new name
		FlexTypeAttribute attributeCheck = productType.getAttribute("productName");
		String attType = attributeCheck.getAttVariableType();
		if("derivedString".equals(attType)){
			//  create a hidden input field to keep the derived products names in txt boxes to keep in sync with the others to build the MOA's later
			%><%= fg.createHiddenInput("newNameField" + count, prodName) %><% 
			dataRow.setData("newName", prodName + " (Derived Name)");
		}else{
			//  makes a call to create a textbox defaulted to "Copy of (current name)" then adds it to the datarow
			dataRow.setData("newName", createNameField(prodName, count, fg));
		}
        data.add(dataRow);
		
		count ++;
}

		// setting up the columns

		Collection columnsNew = new ArrayList();
        TableColumn columnNew;
    	//Add Work# Column  START
	    columnNew = new TableColumn();
        columnNew.setDisplayed(true);
        columnNew.setTableIndex("workNumber");
        columnNew.setHeaderLabel("Work #");
		columnNew.setFormatHTML(true);
        columnsNew.add(columnNew);
	    //Add Work # Column END
	    columnNew = new TableColumn();
        columnNew.setDisplayed(true);
        columnNew.setTableIndex("name");
        columnNew.setHeaderLabel(massCopySourceProdName);
		columnNew.setFormatHTML(true);
        columnsNew.add(columnNew);
	
		columnNew = new TableColumn();
        columnNew.setDisplayed(true);
        columnNew.setTableIndex("newName");
        columnNew.setHeaderLabel(massCopyNewProdName);
		columnNew.setFormatHTML(false);
		columnNew.setAlign("left");
        columnsNew.add(columnNew);
		
		// setting up some logic for reset copy modes javascript function
	
		FlexType seasonTypes = season.getProductType();
		Collection children = seasonTypes.getAllChildren();
		children.add(season.getProductType());

%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////  JAVASCRITPT  /////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<script language="JavaScript" src="<%=URL_CONTEXT%>/javascript/javascript.js"></script>
<script language="JavaScript" src="<%=URL_CONTEXT%>/javascript/jsViews.js"></script>
<script language="JavaScript" src="<%=URL_CONTEXT%>/javascript/util.js"></script>
<script language="JavaScript" src="<%=URL_CONTEXT%>/javascript/moa.js"></script>
<script language="JavaScript" src="<%=URL_CONTEXT%>/javascript/copy_product.js"></script>

<script>
setMasterControllerURL('<%=PageManager.getMasterControllerUrl(true)%>');

function validateCopyRelationshipType(){
    var isValid = true;
	  var originalProductType=document.MAINFORM.sourceType.value;
	  var targetProductTypeLabel =document.MAINFORM.productTypeData.options[document.MAINFORM.productTypeData.selectedIndex].label;
	   var relationshipTypeValue = document.MAINFORM.copyModeData.options[document.MAINFORM.copyModeData.selectedIndex].value;
	   if(hasContent(originalProductType)  && hasContent(targetProductTypeLabel)){
		  if(((originalProductType.indexOf("Backpacks") >-1 && targetProductTypeLabel.indexOf("Bags")>-1) || (originalProductType.indexOf("SMU & Clearance") >-1 && targetProductTypeLabel.indexOf("Adidas")>-1)) && !hasContent(relationshipTypeValue) ){
			   isValid=false;
			   alert("You must select a value for: Relationship Type"); 
		 }	      
      } 
         return isValid;  		   
	   }	
       	 


function collectFormData(){
	
	//used to validate the relationship type
	if(!validateCopyRelationshipType()){
		return ;
	}  
    //clear values to prevent problems with hitting the back button
    document.MAINFORM.copyMode.value = '';
    document.MAINFORM.productTypeDataVal.value = '';
    document.MAINFORM.productNameDataVal.value = '';

	// used to validate user enters names for all new products
	var submitFlag = 0;

  <% for (int i = 0; i <  count; i++) { %>
  
  		if (hasContentNoZero(document.MAINFORM.newNameField<%= i %>.value))	{
  
  			var valueCheck = document.MAINFORM.productTypeData.options[document.MAINFORM.productTypeData.selectedIndex].value
		
			// this if accounts for the fact that when the user changes the product type in the destination dropdown, the "com.lcs.wc.flextype.FlexType:" gets chomped
			if (valueCheck.length < 29)  {
				valueCheck = "com.lcs.wc.flextype.FlexType:" + valueCheck;
			}

			document.MAINFORM.productTypeDataVal.value = buildMOA(document.MAINFORM.productTypeDataVal.value, valueCheck);
			document.MAINFORM.productNameDataVal.value = buildMOA(document.MAINFORM.productNameDataVal.value, document.MAINFORM.newNameField<%= i %>.value);
			if (document.MAINFORM.copyModeData.value == "")	{
		
				document.MAINFORM.copyMode.value = buildMOA(document.MAINFORM.copyMode.value, "");
				document.MAINFORM.blankCopyModeFlag.value = "true";
		
			}else{
		
				document.MAINFORM.copyMode.value = buildMOA(document.MAINFORM.copyMode.value, document.MAINFORM.copyModeData.value);
			
			}
			
			submitFlag = submitFlag + 1;
			
		}else{
		
			alert('<%= LCSMessage.getJavascriptMessage(RB.SEASON, "pleaseEnterNameForAllProducts_ALRT", RB.objA, false)%>');
		
		}

  <% } %>

  	if(submitFlag == <%= count %>)	{
  		//alert(document.MAINFORM.productTypeDataVal.value);
  		//alert(document.MAINFORM.productNameDataVal.value);
  		//alert(document.MAINFORM.copyMode.value);
  		//alert("Oids: " + document.MAINFORM.productOidsVal.value);

  		document.MAINFORM.activity.value = 'MASS_COPY_PRODUCTS';
  		document.MAINFORM.action.value = 'COMPONENTS_OPTION';
	  	submitForm();

  	}

}


function buildMOA(value1, value2){
    var newVal = value1 + "|~*~|" + value2;
    return newVal;
}


function resetCopyMode(typeName){
	var modeTypeObj = document.getElementById("copyModeData");
	var sourceProductType = '<%= FormatHelper.getNumericObjectIdFromObject(prod.getFlexType()) %>';
	
	<% 
	Iterator childIter = children.iterator();
	while(childIter.hasNext()){
		FlexType fChild = (FlexType)childIter.next();
		if(!fChild.isTypeActive()){
			continue;
		}
	%>

		var currentSeasonType = '<%= FormatHelper.getNumericObjectIdFromObject(fChild)%>';
		var destinationType = replaceAll(typeName,'\\','');

		if (currentSeasonType == destinationType)	{

<%
            String propertyCopyModes2 = CopyModeUtil.getModesString(prod.getFlexType().getFullName(true), fChild.getFullName(true));
            String defaultCopyMode = CopyModeUtil.getDefaultModeForTypes(prod.getFlexType().getFullName(true), fChild.getFullName(true));
%>

            modeTypeObj.options.length = 0;
            addCopyModeOptions(modeTypeObj, '<%=FormatHelper.formatJavascriptString(propertyCopyModes2, false)%>', '<%= FormatHelper.formatJavascriptString(defaultCopyMode, false)%>');
		}
  <%	} //end of while loop %>
	
}

</script>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<head>
</head>

<%= tg.startTable() %>
        <td class="PAGEHEADING">
                    <table width="100%">
                	<tr>
	                    <td class="PAGEHEADINGTITLE">
                                <%= massCopyProducts %>
                                <% if(season != null){ %>
                                    &nbsp;&nbsp;>>&nbsp;&nbsp; <%= fromLabel%>: <%= FormatHelper.encodeForHTMLContent(request.getParameter("copyFromSeason"))%>&nbsp;&nbsp;&nbsp;>>&nbsp;&nbsp;<%= addToLabel %>: <%= FormatHelper.encodeForHTMLContent(season.getName())   %>
                                <% } %>
                            </td>
                            <td class="button" align="right" nowrap>
                                <a class="button" href="javascript:collectFormData()"><%= saveButton %></a>&nbsp;&nbsp;|&nbsp;&nbsp;
                                <a class="button" href="javascript:backCancel()"><%= cancelButton2 %></a>
                            </td>
               		</tr>
                    </table>
       		</td>



	<%= tg.startGroupBorder() %>
	<%= tg.startTable() %>
	<%= tg.startGroupContentTable() %>




	          <table width="100%" border="0" cellspacing="1" cellpadding="1">
			    <col width="5%"></col><col width="20%"></col>
                            <col width="6%"></col><col width="15%"></col>
                            <col width="6%"></col><col width="65%"></col>
                            <tr>
 
					<%
                        
						Map productSeasonTypes = findChildren(season.getProductType());
                        String typeNumeric = FormatHelper.getNumericObjectIdFromObject(prod.getFlexType());
                        String sourceProductType = prod.getFlexType().getFullName(true);
                        String toProductType = prod.getFlexType().getFullName(true);
                        System.out.println("MASS_COPY>>>>Season::"+season.getName()+">>>Season Type::"+season.getFlexType().getTypeDisplayName()+">>BACKPACK_KEY::"+backPackFlexTypeKey+">>sourceProductType::"+sourceProductType+""+">>bagFlexType::"+bagFlexType);
                        //In latest Seasom If Original Product type is Backpack then  by default select the BAG type
                        if (sourceProductType.contains(BAKCPACK_FLEXTYPE_DISPLAY) && "Agron".equalsIgnoreCase(season.getFlexType().getTypeDisplayName())  && bagFlexType !=null) {
                      		//set BAG as  Default Product type
	                	    toProductType = bagFlexType.getFullName(true);
                            typeNumeric = FormatHelper.getNumericObjectIdFromObject(bagFlexType); 
                            //remove the BACKPACK from Product Type Dropdown
                      	  	if(productSeasonTypes.containsKey(backPackFlexTypeKey)){
                      			productSeasonTypes.remove(backPackFlexTypeKey);
                      	  	}
                         }
                        //If we don't have the existing product type, select the season's product type
                       	else if(!productSeasonTypes.containsValue(prod.getFlexType().getFullNameDisplay(true))){
                            toProductType = seasonTypes.getFullName(true);
                            typeNumeric = FormatHelper.getNumericObjectIdFromObject(seasonTypes);
                        }
						Map copyModes = CopyModeUtil.getModesDropDownMap(sourceProductType, toProductType);
						String defaultMode = CopyModeUtil.getDefaultModeForTypes(sourceProductType, toProductType);
					%>

					<%= fg.createTextInput("sourceType",massCopySrcProdType + ": ", prod.getFlexType().getFullNameDisplay(true), 40,0,0, false, true)   %></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	
					<%= fg.createDropDownListWidget(massCopyDestinationProdType + ": ", productSeasonTypes, "productTypeData", typeNumeric, "javascript:resetCopyMode(this.options[this.selectedIndex].value)", false, false)%>
                                        <%= fg.createDropDownListWidget(massCopyCopyMode + ": ", copyModes, "copyModeData", defaultMode, "", false, true)%>



                            </tr>
                            <tr>
                                <td>&nbsp;
                                </td>
				<td>&nbsp;
				</td>
				<td>&nbsp;
				</td>
                            </tr>

	</table>



	<%= tg.endContentTable() %>
	<%= tg.endTable() %>
	<%= tg.endBorder() %>






	<%= tg.drawTable(data,columnsNew,"", true, true, true) %>




<%= tg.endTable() %>

<input type="hidden" name="componentSelections" value="">
<input type="hidden" name="productNameDataVal" value="">
<input type="hidden" name="productTypeDataVal" value="">
<input type="hidden" name="productOidsVal" value="<%=FormatHelper.encodeForHTMLAttribute(request.getParameter("oids"))%>">
<input type="hidden" name="copyMode" value="">
<input type="hidden" name="blankCopyModeFlag" value="false">
<input type="hidden" name="copyFromSeason" value="<%= FormatHelper.encodeForHTMLAttribute(request.getParameter("copyFromSeason"))%>">
<input type="hidden" name="copyFromSeasonId" value="<%=  FormatHelper.encodeForHTMLAttribute(request.getParameter("copyFromSeasonId"))%>">


<table width="100%">
    <tr>
        <td class="button" align="right" nowrap>
	   <a class="button" href="javascript:collectFormData()"><%= saveButton %></a>&nbsp;&nbsp;|&nbsp;&nbsp;
           <a class="button" href="javascript:backCancel()"><%= cancelButton2 %></a>
        </td>
    </tr>
</table>
