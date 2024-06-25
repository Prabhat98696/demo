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
                com.lcs.wc.flextype.*,
                com.lcs.wc.material.*,
                com.lcs.wc.report.*,
                java.util.*,
		com.agron.wc.material.AgronMaterialQuery"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />

<%!
    public static final String JSPNAME = "FindMaterialResults";
    public static final boolean DEBUG = true;

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

String imageLabel = WTMessage.getLocalizedMessage ( RB.IMAGE, "image_LBL", RB.objA ) ;
String nameLabel = WTMessage.getLocalizedMessage(RB.QUERYDEFINITION, "materialName", RB.objA);
nameLabel = nameLabel.substring(nameLabel.indexOf('\\') +1);
String chooseLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "choose_LBL",RB.objA ) ;
String checkLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL",RB.objA ) ;
String allLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "all_LBL", RB.objA ) ;
String errorQuickSearchMessage = "" ;
boolean cqsError = false;

    FlexType flexType = null;
    String type = request.getParameter("type");
    if(FormatHelper.hasContent(type)){
      flexType = FlexTypeCache.getFlexType(type);
    }
    String types = "Material|" + type;

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

    String  SEARCH_CLASS_DISPLAY = flexType.getFullNameDisplay(true);

    String idColumn = "LCSMATERIAL.BRANCHIDITERATIONINFO";
    String bulkActivity = "FIND_MATERIAL";
    String bulkAction = "SEARCH";

    boolean updateMode = "true".equals(request.getParameter("updateMode"));
    //boolean updateMode = true;

    if(updateMode){
        FTSLHolder hldr = new FTSLHolder();
        ArrayList<String> keys = new ArrayList<String>();
        String nameCriteriaKey = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getSearchCriteriaIndex();
        keys.add(nameCriteriaKey);

        hldr.add(type, MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, keys);

        session.setAttribute("BU_FTSLHOLDER", hldr);
    }




    HashMap<String, TableColumn> columnMap = new HashMap<String, TableColumn>();
    String nameColumnIndex = flexType.getAttribute("name").getSearchResultIndex();

    flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
    flexg.setLevel(null);
    flexg.createTableColumns(flexType, columnMap, flexType.getAllAttributes(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE, null, false), updateMode, false, "Material.", null, true, null);

    TableColumn column = null;
   //Build potentially required "Hard attribute" columns
    columnMap.putAll(flexg.createHardColumns(flexType, "LCSMaterial", "Material"));

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(nameLabel);
    column.setUseQuickInfo(true);
    column.setHeaderAlign("left");
    column.setLinkMethod("viewObject");
    column.setLinkTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
    column.setTableIndex(nameColumnIndex);
    column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterial:");
    column.setHeaderLink("javascript:resort('" +nameColumnIndex +"')");
    columnMap.put("Material.ptcmaterialName",column);

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel("");
    column.setHeaderAlign("left");
    column.setLinkMethod("launchImageViewer");
    column.setLinkTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
    column.setTableIndex("LCSMATERIAL.PRIMARYIMAGEURL");
    column.setColumnWidth("1%");
    column.setLinkMethodPrefix("");
    column.setImage(true);
    column.setImageWidth(75);
    column.setHeaderLink("javascript:resort('" + nameColumnIndex + "')");
    column.setShowFullImage(FormatHelper.parseBoolean(request.getParameter("showThumbs")));
    columnMap.put("Material.thumbnail", column);

    String prefix = "VR:com.lcs.wc.material.LCSMaterial:";
    if("master".equals(objectIdType)){
        prefix = "OR:com.lcs.wc.material.LCSMaterialMaster:";
        idColumn = "LCSMATERIAL.IDA3MASTERREFERENCE";
    }

    Collection<TableColumn> columnList = new ArrayList<TableColumn>();


    if(chooser){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");

        column.setTableIndex(nameColumnIndex);

       // String prefix = "VR:com.lcs.wc.material.LCSMaterial:";
        if("master".equals(objectIdType)){
            prefix = "OR:com.lcs.wc.material.LCSMaterialMaster:";
            idColumn = "LCSMATERIAL.IDA3MASTERREFERENCE";
        }

        if(multiple){
            column.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel));
            column.setFormat("UNFORMATTED_HTML");
            TableFormElement fe = new CheckBoxTableFormElement();
            fe.setValueIndex(idColumn);
            fe.setValuePrefix(prefix);
            fe.setName("selectedIds");
            column.setFormElement(fe);
        } else {

            column.setLinkMethod("opener.choose");
            column.setWrapping(false);
            if(detailRequest){
                column.setLinkTableIndex("LCSMATERIAL.BRANCHIDITERATIONINFO");
                column.setLinkMethod("chooseDetail");
                column.setLinkMethodPrefix("VR:com.lcs.wc.material.LCSMaterial:");
                column.setConstantDisplay(true);
                column.setConstantValue(chooseLabel);

            } else {
                column.setLinkTableIndex(idColumn);
                if(!idOnly){
                    column.setLinkMethodPrefix(prefix);
                }
                column.setLinkTableIndex2(nameColumnIndex);
                column.setConstantDisplay(true);
                column.setConstantValue(chooseLabel);
            }
        }

        column.setColumnWidth("1%");
        columnList.add(column);
    } else {
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(flexg2.drawSelectAllCheckbox(allLabel));
        column.setFormat("UNFORMATTED_HTML");
        column.setHeaderAlign("left");
        TableFormElement fe1 = new CheckBoxTableFormElement();
        fe1.setValueIndex(idColumn);
        fe1.setValuePrefix(prefix);
        fe1.setName("selectedIds");
        column.setFormElement(fe1);
        column.setHeaderLink("javascript:toggleAllItems()");
        columnList.add(column);

    }

    //Setup the attlist/columns
    //attList is the list of attribute keys...required for the query to get the attributes
    Collection attList = new ArrayList();
    //columns is the list of Columns in the order they should be displayed from the column map
    Collection<TableColumn> columns = new ArrayList<TableColumn>(columnList);
	
	TableDataModel tdm = null;
	if(FormatHelper.hasContent(viewId)){
        if(!FormatHelper.parseBoolean(request.getParameter("showThumbs")) && columnMap.get("Material.thumbnail") != null){
            columnMap.remove("Material.thumbnail");

        }
		tdm = new TableDataModel(columnMap, new ArrayList(), columns, viewId);
		attList = tdm.attList;
	}
    else{

        flexg.setScope(MaterialSupplierFlexTypeScopeDefinition.MATERIAL_SCOPE);
        flexg.setLevel(null);
        flexg.setSingleLevel(false);
        Collection<?> searchColumns = flexg.createSearchResultColumnKeys(flexType, "Material.");

        columns.add((TableColumn)columnMap.get("Material.thumbnail"));
        columns.add((TableColumn)columnMap.get("Material.ptcmaterialName"));
        columns.add((TableColumn)columnMap.get("Material.typename"));

        flexg.extractColumns(searchColumns, columnMap, attList, columns);

    }


   //LCSMaterialQuery query = new LCSMaterialQuery();
   AgronMaterialQuery query = new AgronMaterialQuery();
	SearchResults results = new SearchResults();
	try{
		results = query.findMaterialsByCriteria(request, flexType, attList, filter);
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
	boolean RTS = FormatHelper.parseBoolean(request.getParameter("RETURN_TO_SEARCH_FROM"));
    if(results.getResultsFound() == 1 && !chooser && !RTS){
        // IF ONLY ONE MATERIAL IS FOUND, TAKE THE USER TO THE DETAILS PAGE
        FlexObject materialData = (FlexObject) results.getResults().elementAt(0);
        %>
        <script>
            viewMaterial('VR:com.lcs.wc.material.LCSMaterial:<%= materialData.getString("LCSMATERIAL.BRANCHIDITERATIONINFO") %>');
        </script>
        <%
    }
%>

<%@ include file="/rfa/jsp/main/SearchResultsTable.jspf" %>
