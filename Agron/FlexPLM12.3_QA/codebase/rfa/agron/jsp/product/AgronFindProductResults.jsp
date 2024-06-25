<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.db.FlexObject,
                com.lcs.wc.client.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.product.*,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                wt.indexsearch.*,
                wt.fc.WTObject,
                com.lcs.wc.foundation.*,
                wt.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,
                com.lcs.wc.report.*,
                java.util.*,
				com.agron.wc.product.AgronProductQuery"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="flexg2" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator2" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />


<%!
    public static final String JSPNAME = "FindProductResults";
    public static final boolean DISABLE_TABLE_EDIT_MAPPED_PRODUCT_ATTRIBUTES = LCSProperties.getBoolean("com.lcs.wc.placeholder.disableTableEditMappedProductAttributes");

    public static final String getCellClass(boolean b){
       if(b){
            return "TABLEBODYDARK";
        } else {
            return "TABLEBODYLIGHT";
        }
    }
    private static final Logger logger = LogManager.getLogger("rfa.jsp.product.FindProductResults");
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

String imageLabel = WTMessage.getLocalizedMessage ( RB.PRODUCT, "image_LBL", RB.objA ) ;
String chooseLabel = WTMessage.getLocalizedMessage ( RB.MAIN, "choose_LBL",RB.objA ) ;
String isSkuSizeActiveLbl = WTMessage.getLocalizedMessage(RB.SKUSIZE, "isSkuSizeActive_LBL", RB.objA);
String tableLayout = FormatHelper.format(request.getParameter("layout"));
String errorQuickSearchMessage = "";
boolean cqsError = false;


   flexg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
   flexg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);

   FlexType flexType = null;
   String type = FormatHelper.encodeForHTMLContent(request.getParameter("type"));
   if(FormatHelper.hasContent(type)){
      flexType = FlexTypeCache.getFlexType(type);
   }
    String types = "Product|" + type;
%>

<%@ include file="/rfa/jsp/reports/ViewLookup.jspf" %>

<%
    boolean darkRow = false;

    String returnAction = request.getParameter("returnAction");
    boolean chooser = FormatHelper.parseBoolean(request.getParameter("chooser"));
    boolean multiple = FormatHelper.parseBoolean(request.getParameter("multiple"));
    boolean idOnly = FormatHelper.parseBoolean(request.getParameter("idOnly"));
    String objectIdType = FormatHelper.format(request.getParameter("objectIdType"));

    String chooserSelectFunction = request.getParameter("chooserSelectFunction");

    String  SEARCH_CLASS_DISPLAY = flexType.getFullNameDisplay(true);

    String idColumn = "LCSPRODUCT.BRANCHIDITERATIONINFO";
    String bulkActivity = "FIND_PRODUCT";
    String bulkAction = "SEARCH";

    boolean updateMode = "true".equals(request.getParameter("updateMode"));
    //boolean updateMode = true;

    if(updateMode){
        FTSLHolder hldr = new FTSLHolder();
        ArrayList keys = new ArrayList();
        String nameCriteriaKey = FlexTypeCache.getFlexTypeRoot("Product").getAttribute("productName").getSearchCriteriaIndex();
        keys.add(nameCriteriaKey);

        hldr.add(type, null, null, keys);

        session.setAttribute("BU_FTSLHOLDER", hldr);
        if(DISABLE_TABLE_EDIT_MAPPED_PRODUCT_ATTRIBUTES ){
            tg.setDisableMappedAttribute(true);
            String phRefColumn = IntrospectionHelper.getTableAndColumn(LCSProduct.class, "thePersistInfo.theObjectIdentifier.id");
            tg.addEnableMappedAttCriteria(phRefColumn, "0");
        }
    }


    HashMap columnMap = new HashMap();

    flexg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
    flexg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);
    flexg.createTableColumns(flexType, columnMap, flexType.getAllAttributes(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE, FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL, false), updateMode, false, "Product.", null, true, null);

    //flexg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
    //flexg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL);
    flexg.createTableColumns(flexType   , columnMap,    flexType.getAllAttributes(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE, FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL, true) , updateMode                                                        , false, "Product."        , null, true, null);

    TableColumn column = null;

    FlexTypeAttribute att = flexType.getAttribute("productName");
  //Build potentially required "Hard attribute" columns
   columnMap.putAll(flexg.createHardColumns(flexType, "LCSProduct", "Product"));

    //if(FormatHelper.parseBoolean(request.getParameter("showThumbs")) || (attributes != null && attributes.contains("Product.thumbnail"))){

        if(updateMode){


            UpdateFileTableColumn fcolumn = new UpdateFileTableColumn();
            fcolumn.setHeaderLabel(imageLabel);
            fcolumn.setDisplayed(true);
            fcolumn.setTableIndex("LCSPRODUCT.PARTPRIMARYIMAGEURL");
            fcolumn.setColumnWidth("10%");
            fcolumn.setClassname("LCSPRODUCT");
            fcolumn.setWorkingIdIndex("LCSPRODUCT.IDA2A2");
            fcolumn.setFormElementName("partPrimaryImageURL");
            fcolumn.setFormatHTML(false);
            fcolumn.setColumnWidth("10%");
            fcolumn.setWrapping(true);
            fcolumn.setImage(true);
            columnMap.put("Product.thumbnail", fcolumn);
        }  else {
            column = new TableColumn();
            column.setDisplayed(true);
            column.setHeaderLabel(imageLabel);
            column.setHeaderAlign("left");
            column.setLinkMethod("launchImageViewer");
            column.setLinkTableIndex("LCSPRODUCT.PARTPRIMARYIMAGEURL");
            column.setTableIndex("LCSPRODUCT.PARTPRIMARYIMAGEURL");
            column.setColumnWidth("1%");
            column.setLinkMethodPrefix("");
            column.setImage(true);
            column.setShowFullImage(FormatHelper.parseBoolean(request.getParameter("showThumbs")));
            column.setImageWidth(75);
            columnMap.put("Product.thumbnail", column);

        }
    //}

    column = new TableColumn();
    column.setDisplayed(true);
    column.setHeaderLabel(att.getAttDisplay());
    column.setHeaderAlign("left");
    column.setLinkMethod("viewObject");
    column.setLinkTableIndex("LCSPRODUCT.BRANCHIDITERATIONINFO");
    column.setTableIndex(att.getSearchResultIndex());
    column.setLinkMethodPrefix("VR:com.lcs.wc.product.LCSProduct:");
    column.setHeaderLink("javascript:resort('" + att.getSearchResultIndex() + "')");
    column.setUseQuickInfo(true);
    columnMap.put("Product.productName", column);


     //Build the collection of Columns to use in the report
    Collection columnList = new ArrayList();

    String prefix = "VR:com.lcs.wc.product.LCSProduct:";
    if("master".equals(objectIdType)){
        prefix = "OR:com.lcs.wc.part.LCSPartMaster:";
        idColumn = "LCSPRODUCT.IDA3MASTERREFERENCE";
    }


    if(chooser){
        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel("");
        column.setHeaderAlign("left");

        column.setTableIndex(att.getSearchResultIndex());


        if(multiple){
            column.setHeaderLabel(flexg2.drawSelectAllCheckbox());
            column.setFormat("UNFORMATTED_HTML");
            TableFormElement fe = new CheckBoxTableFormElement();
            fe.setValueIndex(idColumn);
            fe.setValuePrefix(prefix);
            fe.setName("selectedIds");
            column.setFormElement(fe);
        } else {
            if(FormatHelper.hasContent(chooserSelectFunction)){
                column.setLinkMethod(chooserSelectFunction);
            }
            else{
                column.setLinkMethod("opener.choose");
            }
            column.setLinkTableIndex(idColumn);
            if(!idOnly){
                column.setLinkMethodPrefix(prefix);
            }
            column.setLinkTableIndex2(att.getSearchResultIndex());
            column.setConstantDisplay(true);
            column.setWrapping(false);
            column.setConstantValue(chooseLabel);
        }

        column.setColumnWidth("1%");
        columnList.add(column);

    } else if(!"thumbnails".equals(tableLayout) && !"filmstrip".equals(tableLayout)) {

        column = new TableColumn();
        column.setDisplayed(true);
        column.setHeaderLabel(flexg2.drawSelectAllCheckbox());
        column.setFormat("UNFORMATTED_HTML");
        column.setHeaderAlign("left");
        TableFormElement fe1 = new CheckBoxTableFormElement();
        fe1.setValueIndex(idColumn);
        fe1.setValuePrefix(prefix);
        fe1.setName("selectedIds");
        column.setFormElement(fe1);
        //column.setValue("<input type=\"checkbox\" id=\"selectedIds\" value=\"false\" onClick=\"javascript:toggleAllItems()\">");
        column.setHeaderLink("javascript:toggleAllItems()");
        columnList.add(column);

        column = new ActionsTableColumn();
        column.setHeaderLabel("");
        column.setDisplayed(true);
        column.setLinkTableIndex("LCSPRODUCT.BRANCHIDITERATIONINFO");
        column.setLinkMethodPrefix("VR:com.lcs.wc.product.LCSProduct:");
        column.setColumnWidth("10%");
        columnList.add(column);

    }
    if("thumbnails".equals(tableLayout) || "filmstrip".equals(tableLayout)) {
        DefaultTileCellRenderer cellRenderer = new DefaultTileCellRenderer();
        cellRenderer.setImageIndex("LCSPRODUCT.PartPrimaryImageURL");
        cellRenderer.setIdIndex(idColumn);
        cellRenderer.setNameIndex(flexType.getAttribute("productName").getSearchResultIndex());
        cellRenderer.setIdPrefix("VR:com.lcs.wc.product.LCSProduct:");
        request.setAttribute("tileCellRenderer", cellRenderer);
    }

    //Setup the attlist/columns
    //attList is the list of attribute keys...required for the query to get the attributes
    Collection attList = new ArrayList();
    //columns is the list of Columns in the order they should be displayed from the column map
    Collection columns = new ArrayList(columnList);

    if("thumbnails".equals(tableLayout) || "filmstrip".equals(tableLayout)){
        //columnMap.remove("Product.thumbnail");

    }

    TableDataModel tdm = null;
    if(FormatHelper.hasContent(viewId)){
        tdm = new TableDataModel(columnMap, new ArrayList(), columns, viewId);
        attList = tdm.attList;
        //logger.debug(tdm.toString());
    }
    else{
        Collection searchColumns = new HashSet();

        flexg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
        flexg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_LEVEL);
        flexg.setSingleLevel(false);
        searchColumns.addAll(flexg.createSearchResultColumnKeys(flexType, "Product."));

        flexg.setScope(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SCOPE);
        flexg.setLevel(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL);
        flexg.setSingleLevel(false);
        searchColumns.addAll(flexg.createSearchResultColumnKeys(flexType, "Product."));

        columns.add(columnMap.get("Product.thumbnail"));
        columns.add(columnMap.get("Product.productName"));
        columns.add(columnMap.get("Product.state"));
        columns.add(columnMap.get("Product.typename"));
        columns.add(columnMap.get("Product.creator"));
        flexg.extractColumns(searchColumns, columnMap, attList, columns);
        attList.add("productName");
    }

    // BEGIN INDEX SEARCH - Set Up and Run the query via this plugin
    Collection oidList = null;
    String searchterm = null;
    if (FormatHelper.hasContent(request.getParameter("indexSearchKeyword"))) {
        searchterm = (request.getParameter("indexSearchKeyword")).trim();
    }
    String typeclass = null;
    if (FormatHelper.hasContent(request.getParameter("typeClass"))) {
        typeclass =request.getParameter("typeClass");
    }
%>
    <%@ include file="/rfa/jsp/indexsearch/IndexSearchLibraryPlugin.jspf" %>
<%
    // oidList should be populated now if user specified a keyword to search against, otherwise size=0
    // END INDEX SEARCH filter
/*	*	Agron custom code added.
	*/
    AgronProductQuery query = new AgronProductQuery();
    SearchResults results = new SearchResults();
    results.setResults(new Vector());
    results.setResultsFound(0);
	String attKey = request.getParameter("displayAttribute");
	try{
		if ( FormatHelper.hasContent(searchterm)) {
			if (oidList != null) {
				results = query.findProductsByCriteria(((Map) (RequestHelper.hashRequest(request))), flexType, attList, filter, oidList, attKey);
			} // no else - no findby criteria needed if a keyword search was done and no hits occured
		} else {
			results = query.findProductsByCriteria(((Map) (RequestHelper.hashRequest(request))), flexType, attList, filter, oidList, attKey);
		}
		   

	}catch(LCSException ex){
		cqsError =true;
		errorQuickSearchMessage = ex.getLocalizedMessage();
	}
	if(cqsError){%>
		 <table>
			<tr>
				<td class="ERROR">
					<%= errorQuickSearchMessage %> 
				</td>
			</tr>
		</table>
	<%}

    boolean RTS = FormatHelper.parseBoolean(request.getParameter("RETURN_TO_SEARCH_FROM"));
    if(results.getResultsFound() == 1 && !chooser && !RTS){
        // IF ONLY ONE PRODUCT IS FOUND, TAKE THE USER TO THE DETAILS PAGE
        FlexObject productData = (FlexObject) results.getResults().elementAt(0);
        %>
        <script>
            viewObject('VR:com.lcs.wc.product.LCSProduct:<%= productData.getString("LCSPRODUCT.BRANCHIDITERATIONINFO") %>');
        </script>
        <%
    }
//tg.setDisableMappedAttribute(true);
%>
<%@ include file="/rfa/jsp/main/SearchResultsTable.jspf" %>


<script language=Javascript>



    function validate(){
        <%if(updateMode){%>
            <%= flexg.generateFormValidation((Collection)columns) %>
        <%}%>
        return true;
    }


</script>
