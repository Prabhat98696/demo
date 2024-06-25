<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.db.FlexObject,
                com.lcs.wc.client.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                wt.util.*,
                wt.indexsearch.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.material.*,
                com.lcs.wc.report.*,
                java.util.*,
                com.lcs.wc.foundation.LCSQuery,
				com.agron.wc.materialSupplier.AgronMaterialSupplierQuery"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />

<%!
    public static final String JSPNAME = "FindMaterialSupplierResults";
    public static final boolean DEBUG = true;
    private static final boolean showRepeatedData = LCSProperties.getBoolean("com.lcs.wc.client.web.TableGenerator.showRepeatedData");


    public static final String getCellClass(boolean b){
       if(b){
            return "TABLEBODYDARK";
        } else {
            return "TABLEBODYLIGHT";
        }
    }
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
//setting up which RBs to use

String imageLabel = WTMessage.getLocalizedMessage ( RB.IMAGE, "image_LBL", RB.objA ) ;
String materialLabel = WTMessage.getLocalizedMessage ( RB.MATERIALSUPPLIER, "material_LBL", RB.objA ) ;
String supplierLabel = WTMessage.getLocalizedMessage ( RB.MATERIALSUPPLIER, "supplier_LBL", RB.objA ) ;
String viewLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "view_LBL", RB.objA ) ;
String viewWithParenthLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "viewWithParenth_LBL", RB.objA ) ;
String chooseLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "choose_LBL",RB.objA ) ;

String modifierLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "lastUpdatedBy_LBL", RB.objA ) ;
String creatorLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "creator_LBL", RB.objA ) ;
String createdLbl = WTMessage.getLocalizedMessage( RB.MAIN, "created_LBL" , RB.objA );
String lastUpdatedLbl = WTMessage.getLocalizedMessage( RB.MAIN, "lastUpdated_LBL" , RB.objA );
String typeLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "type_LBL", RB.objA ) ;
String materialSupplierLifecycleStateLabel = WTMessage.getLocalizedMessage ( RB.MATERIALSUPPLIER,  "materialSupplierLifecycleState_LBL", RB.objA ) ;
String supplierLifecycleStateLabel = WTMessage.getLocalizedMessage ( RB.SUPPLIER,  "supplierLifecycleState_LBL", RB.objA ) ;
String materialLifecycleStateLabel = WTMessage.getLocalizedMessage ( RB.MATERIAL,  "materialLifecycleState_LBL", RB.objA ) ;
String allLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL", RB.objA ) ;

String errorQuickSearchMessage = "";
boolean cqsError = false;
%>
<%

    FlexType supplierType = FlexTypeCache.getFlexTypeRoot("Supplier");

	FlexType flexType = null;
    String type = request.getParameter("type");
    


    if(FormatHelper.hasContent(type)){
        flexType = FlexTypeCache.getFlexType(type);
    }
    String types = "Material|" + type;

    types = MOAHelper.addValue(types, "Supplier|" + FormatHelper.getObjectId(FlexTypeCache.getFlexTypeRoot("Supplier")));


%>

<%@ include file="/rfa/jsp/reports/ViewLookup.jspf" %>

<%
    boolean darkRow = false;

    String returnAction = request.getParameter("returnAction");
    boolean chooser = FormatHelper.parseBoolean(request.getParameter("chooser"));
    boolean multiple = FormatHelper.parseBoolean(request.getParameter("multiple"));
    boolean idOnly = FormatHelper.parseBoolean(request.getParameter("idOnly"));
    boolean detailRequest = FormatHelper.parseBoolean(request.getParameter("detailRequest"));
    String objectIdType = FormatHelper.format(request.getParameter("objectIdType"));
    String tableLayout = FormatHelper.format(request.getParameter("layout"));


    String  SEARCH_CLASS_DISPLAY = WTMessage.getLocalizedMessage ( RB.MATERIALSUPPLIER, "materialSupplier_LBL", RB.objA ) ;

    String idColumn = "LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO";
    String bulkActivity = "FIND_MATERIAL_SUPPLIER";
    String bulkAction = "SEARCH";

    boolean updateMode = "true".equals(request.getParameter("updateMode"));
    //boolean updateMode = true;

    if(updateMode){
        String nameCriteriaKey = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getSearchCriteriaIndex();

        FTSLHolder hldr = new FTSLHolder();
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(nameCriteriaKey );
        keys.add("MATERIALMASTERID");
        keys.add("SUPPLIERMASTERID");
        keys.add("COLORID");

        hldr.add(type, MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, keys);
        hldr.add(type, MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, null);

        hldr.add(type, null, null, keys);

        session.setAttribute("BU_FTSLHOLDER", hldr);
    }




    HashMap<String,TableColumn> columnMap = new HashMap<String,TableColumn>();
    FlexTypeAttribute materialNameAtt = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name");

    flexg.setScope(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
    flexg.setLevel(null);
    flexg.createTableColumns(flexType, columnMap, flexType.getAllAttributes(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, false), updateMode, false, "Material.", null, true, null);

    TableColumn column = null;


    if(!"thumbnails".equals(tableLayout) && !"filmstrip".equals(tableLayout)){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(imageLabel);
        column.setHeaderAlign("left");
        column.setLinkMethod("launchImageViewer");
        column.setLinkTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        column.setTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
        column.setColumnWidth("9%");
        column.setLinkMethodPrefix("");
        column.setImage(true);
        column.setImageWidth(75);
		column.setShowFullImage(FormatHelper.parseBoolean(request.getParameter("showThumbs")));
        columnMap.put("Material.thumbnail",column);
    }


    flexg.setScope(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);
    flexg.setLevel(null);
    flexg.createTableColumns(flexType, columnMap, flexType.getAllAttributes(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE, null, false), updateMode, false, "Material.", null, true, null);

    flexg = new FlexTypeGenerator();
    flexg.createTableColumns(supplierType, columnMap, supplierType.getAllAttributes(null, null, false), false, false, "Supplier.", null, true, null);


    column = new UserTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(creatorLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("LCSMATERIALSUPPLIER.IDA3D2ITERATIONINFO");
    column.setHeaderLink("javascript:resort('CREATOR.name')");
    columnMap.put("Material.materialsuppliercreator", column);

    column = new UserTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(modifierLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("LCSMATERIALSUPPLIER.IDA3B2ITERATIONINFO");
    column.setHeaderLink("javascript:resort('MODIFIER.name')");
    columnMap.put("Material.materialsuppliermodifier", column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(materialSupplierLifecycleStateLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("LCSMATERIALSUPPLIER.STATESTATE");
    column.setHeaderLink("javascript:resort('LCSMaterialSupplier.statestate')");
    columnMap.put("Material.materialsupplierstate", column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(supplierLifecycleStateLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("LCSSUPPLIER.STATESTATE");
    column.setHeaderLink("javascript:resort('LCSSupplier.statestate')");
    columnMap.put("Material.supplierstate", column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(materialLifecycleStateLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("LCSMATERIAL.STATESTATE");
    column.setHeaderLink("javascript:resort('LCSMaterial.statestate')");
    columnMap.put("Material.materialstate", column);


    String index = "LCSMATERIALSUPPLIER.CREATESTAMPA2";
    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(createdLbl);
    column.setHeaderAlign("left");
    column.setTableIndex(index.toUpperCase());
    column.setHeaderLink("javascript:resort('" + index + "')");
    column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
    columnMap.put("Material.materialsuppliercreatedOn", column);

    index = "LCSMATERIALSUPPLIER.MODIFYSTAMPA2";
    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(lastUpdatedLbl);
    column.setHeaderAlign("left");
    column.setTableIndex(index.toUpperCase());
    column.setHeaderLink("javascript:resort('" + index + "')");
    column.setFormat(FormatHelper.GMT_TO_LOCAL_FORMAT);
    columnMap.put("Material.materialsuppliermodifiedOn", column);


    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel("");
    column.setHeaderAlign("left");
    column.setLinkMethod("viewObject");
    column.setUseQuickInfo(true);
    column.setLinkTableIndex("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
    column.setTableIndex("V_LCSMATERIALCOLOR.MATERIALSUPPLIERNAME");
    column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterialSupplier:");
    column.setHeaderLink("javascript:resort('V_LCSMaterialColor.materialSupplierName')");
    column.setConstantDisplay(true);
    column.setConstantValue(viewWithParenthLabel);
    column.setColumnWidth("1%");
    columnMap.put("Material.materialSupplierName", column);


    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(materialLabel);
    column.setHeaderAlign("left");
    column.setLinkMethod("viewObject");
    column.setUseQuickInfo(true);
    column.setLinkTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
    column.setTableIndex(materialNameAtt.getSearchResultIndex());
	column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterial:");
    column.setHeaderLink("javascript:resort('"+ materialNameAtt.getSearchResultIndex() + "')");
    columnMap.put("Material.ptcmaterialName", column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(supplierLabel);
    column.setHeaderAlign("left");
    column.setLinkMethod("viewObject");
    column.setUseQuickInfo(true);
    column.setLinkTableIndex("LCSSUPPLIERMASTER.IDA2A2");
    column.setTableIndex("LCSSUPPLIERMASTER.SUPPLIERNAME");
    column.setLinkMethodPrefix("OR:com.lcs.wc.supplier.LCSSupplierMaster:");
    column.setHeaderLink("javascript:resort('LCSSupplierMaster.supplierName')");
    columnMap.put("Supplier.name", column);

    column = new FlexTypeDescriptorTableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(typeLabel);
    column.setHeaderAlign("left");
    column.setTableIndex("WTTYPEDEFINITION.BRANCHIDITERATIONINFO");
    column.setHeaderLink("javascript:resort('WTTypeDefinition.branchIdIterationInfo')");
    columnMap.put("Material.typename", column);


    //Build the collection of Columns to use in the report
    Collection<TableColumn> columnList = new ArrayList<TableColumn>();

    if(chooser){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");

        column.setTableIndex("V_LCSMATERIALCOLOR.MATERIALSUPPLIERNAME");

        String prefix = "VR:com.lcs.wc.material.LCSMaterialSupplier:";
        if("master".equals(objectIdType)){
            prefix = "OR:com.lcs.wc.material.LCSMaterialSupplierMaster:";
            idColumn = "LCSMATERIALSUPPLIERMASTER.IDA2A2";
        }

        if(multiple){
            column.setHeaderLabel("<input type=\"checkbox\" id=\"selectAllCheckBox1\" value=\"false\" onClick=\"javascript:toggleAllItems('selectAllCheckBox1')\">" + allLabel);
            column.setFormat("UNFORMATTED_HTML");
            TableFormElement fe = new CheckBoxTableFormElement();
            fe.setValueIndex(idColumn);
            fe.setValuePrefix(prefix);
            fe.setName("selectedIds");
            column.setFormElement(fe);
			if("addMaterialsToColorLink".equals(request.getParameter("returnMethod"))){
				column.setShowCriteriaTarget("LCSMATERIALSUPPLIERMASTER.PLACEHOLDER");
				column.setShowCriteria("0");
                column.setShowCriteriaNumericCompare(true);
			}
        } else {
            column.setLinkMethod("opener.choose");
            column.setWrapping(false);
            if(detailRequest){
                column.setLinkTableIndex("LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
                column.setLinkMethod("chooseDetail");
                column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterialSupplier:");
                column.setConstantDisplay(true);
                column.setConstantValue(chooseLabel);

            } else {
                column.setLinkTableIndex(idColumn);
                if(!idOnly){
                    column.setLinkMethodPrefix(prefix);
                }
                column.setLinkTableIndex2("V_LCSMATERIALSUPPLIER.MATERIALSUPPLIERNAME");
                column.setConstantDisplay(true);
                column.setConstantValue(chooseLabel);
            }
        }

        column.setColumnWidth("10%");
        columnList.add(column);
    }


    //Setup the attlist/columns
    //attList is the list of attribute keys...required for the query to get the attributes
    Collection<Object> attList = new ArrayList<Object>();
    //columns is the list of Columns in the order they should be displayed from the column map
    Collection<TableColumn> columns = new ArrayList<TableColumn>(columnList);

    Collection<TableColumn> materialSupplierColumns = new ArrayList<TableColumn>();
	TableDataModel tdm = null;


    if(attributes != null) {
        flexg.extractColumns(attributes, columnMap, attList, columns);
    } else {

        columns.add(columnMap.get("Material.thumbnail"));
        columns.add(columnMap.get("Material.ptcmaterialName"));

        flexg.setScope(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
        flexg.setLevel(null);
        flexg.setSingleLevel(true);
        Collection<?> searchColumns = flexg.createSearchResultColumnKeys(flexType, "Material.");

        flexg.extractColumns(searchColumns, columnMap, attList, columns);


        flexg.setScope(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);
        flexg.setLevel(null);
        flexg.setSingleLevel(false);
        searchColumns = flexg.createSearchResultColumnKeys(flexType, "Material.");
		materialSupplierColumns = flexg.createSearchResultColumnKeys(flexType, "Material.");


        columns.add(columnMap.get("Material.materialSupplierName"));
        columns.add(columnMap.get("Supplier.name"));
        flexg.extractColumns(searchColumns, columnMap, attList, columns);
    }

	Hashtable<String,String> repeatedDataColumnMap = new Hashtable<String,String>();
	repeatedDataColumnMap.put("LCSMATERIAL", "LCSMATERIAL.BRANCHIDITERATIONINFO");
	repeatedDataColumnMap.put("LCSSUPPLIER", "LCSSUPPLIERMASTER.IDA2A2");


	String tableName;
	  for(String key : columnMap.keySet()) {
		column = (TableColumn)columnMap.get(key); 
		if(column.getAttributeType()!=null && (column.getAttributeType().equals("object_ref") || column.getAttributeType().equals("object_ref_list"))){
			tableName = column.getTableIndex();
			tableName = tableName.substring(0, tableName.indexOf("."));
			if(key.startsWith("Material.")){
				if(materialSupplierColumns.contains(key)){
					repeatedDataColumnMap.put(tableName, "LCSMATERIALSUPPLIER.IDA2A2");
				}else{
					repeatedDataColumnMap.put(tableName, "LCSMATERIAL.BRANCHIDITERATIONINFO");
				}
			}else if(key.startsWith("Supplier.")){
				repeatedDataColumnMap.put(tableName, "LCSSUPPLIERMASTER.IDA2A2");
			}
		}
	}

	tg.setRepeatedDataColumnMap(repeatedDataColumnMap);
	tg.setShowRepeatedData(showRepeatedData);


%>
<%

    // BEGIN INDEX SEARCH - Set Up and Run the query via this plugin
    Collection oidList = null;
    String searchterm = null;
    if (FormatHelper.hasContent(request.getParameter("indexSearchKeyword"))) {
        searchterm = (request.getParameter("indexSearchKeyword")).trim();
    }
    String typeclass = null;


%>
    <%@ include file="/rfa/jsp/indexsearch/IndexSearchLibraryPlugin.jspf" %>

<%
        //LCSMaterialSupplierQuery query = new LCSMaterialSupplierQuery(); //Agron Customization
	AgronMaterialSupplierQuery query = new AgronMaterialSupplierQuery();
	SearchResults results = SearchResults.emptySearchResults();
	results.setResults(new Vector());
	results.setResultsFound(0);
	boolean outOfQueryLimit=false;
	try{
		if ( FormatHelper.hasContent(searchterm)) {
            if (oidList != null) {
                results = query.findMaterialSuppliersByCriteria(RequestHelper.hashRequest(request), flexType, attList, filter, 0, oidList);
            } // no else - no findby criteria needed if a keyword search was done and no hits occured
        } else {
            results = query.findMaterialSuppliersByCriteria(request, flexType, attList, filter);
        }	
    } catch (OutOfQueryLimitException ooqe) {
        outOfQueryLimit=true;
    }catch(LCSException ex){
		cqsError =true;
		errorQuickSearchMessage = ex.getLocalizedMessage();
	}
	if(cqsError){%>
		<table>
			<tr>
				<td class="ERROR">
					<%= FormatHelper.encodeAndFormatForHTMLContent(errorQuickSearchMessage) %> 
				</td>
			</tr>
		</table>
	<%}
    //SearchResults results = query.findMaterialSuppliersByCriteria(request, flexType, attList, filter);

 %>
<%
        flexg.setScope(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
        flexg.setLevel(null);
%>
<%= flexg.generateSearchCriteriaPlaceholders(flexType, request) %>
<%
        flexg.setScope(com.lcs.wc.material.MaterialSupplierFlexTypeScopeDefinition.MATERIALSUPPLIER_SCOPE);
        flexg.setLevel(null);
%>

<%@ include file="/rfa/jsp/main/SearchResultsTable.jspf" %>

<input type="hidden" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("MATERIALMASTERID")) %>" name="MATERIALMASTERID">
<input type="hidden" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("SUPPLIERMASTERID")) %>" name="SUPPLIERMASTERID">
<input type="hidden" value="<%= FormatHelper.encodeAndFormatForHTMLContent(request.getParameter("COLORID")) %>" name="COLORID">
