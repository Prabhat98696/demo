<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
         import="com.lcs.wc.client.web.*,
                java.util.*,
                com.lcs.wc.util.*,
                com.lcs.wc.season.*,
                com.lcs.wc.report.TableDataModel,
                wt.util.*"
%>
<jsp:useBean id="excelg" scope="request" class="com.lcs.wc.client.web.ExcelGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="repeatedDataColumnMap" scope="request" class="java.util.Hashtable" />
<jsp:useBean id="linePlanDataCache" class="com.lcs.wc.season.LCSSeasonReportCache" scope="session"/>
<jsp:useBean id="mdtDataCache" class="com.lcs.wc.util.DataCollectionCache" scope="session"/>

<%!
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");
    public static final String CONTEXT_URL_MASTER_CONTROLLER = PageManager.getContextOverrideUrlAndMasterContorller();

    public static String wt_home =  "";

    static
    {
        try
        {
            WTProperties wtproperties = WTProperties.getLocalProperties();
            String getUrl = wtproperties.getProperty("wt.server.codebase","");
            String WindchillContext = "/" + wtproperties.getProperty("wt.webapp.name");
            wt_home =  wtproperties.getProperty("wt.home");

        } catch(Exception e){
            e.printStackTrace();
        }
    }
%>

<%
    repeatedDataColumnMap.put("LCSPRODUCT", "LCSPRODUCT.IDA3MASTERREFERENCE");
    repeatedDataColumnMap.put("LCSSKU", "LCSSKU.IDA3MASTERREFERENCE");
    repeatedDataColumnMap.put("PLACEHOLDER", "PLACEHOLDER.IDA2A2");
    repeatedDataColumnMap.put("LCSPRODUCTSEASONLINK", "LCSPRODUCTSEASONLINK.IDA2A2");
    repeatedDataColumnMap.put("LCSSKUSEASONLINK", "LCSSKUSEASONLINK.IDA2A2");
    repeatedDataColumnMap.put("LCSSOURCINGCONFIG", "LCSSOURCINGCONFIG.IDA3MASTERREFERENCE");
    repeatedDataColumnMap.put("LCSSKUSOURCINGLINK", "LCSSKUSOURCINGLINK.IDA2A2");
    repeatedDataColumnMap.put("LCSSOURCETOSEASONLINK", "LCSSOURCETOSEASONLINK.BRANCHIDITERATIONINFO");
    repeatedDataColumnMap.put("SEASONGROUP", "SEASONGROUP.IDA3MASTERREFERENCE");
    repeatedDataColumnMap.put("SEASONGROUPTOPRODUCTLINK", "SEASONGROUPTOPRODUCTLINK.IDA3MASTERREFERENCE");
    repeatedDataColumnMap.put("SEASONGROUPTOSKULINK", "SEASONGROUPTOSKULINK.IDA3MASTERREFERENCE");
    repeatedDataColumnMap.put("LCSMATERIAL", "LCSMATERIAL.BRANCHIDITERATIONINFO");
    repeatedDataColumnMap.put("LCSMATERIALSUPPLIER","LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
    repeatedDataColumnMap.put("FLEXTYPE", "LCSPRODUCT.IDA3MASTERREFERENCE");

    Object[] objA = new Object[0];
    String RB_MAIN = "com.lcs.wc.resource.MainRB";
    String excelTooltip = WTMessage.getLocalizedMessage ( RB_MAIN, "excelTooltip", objA ) ;
    boolean iconOnly = FormatHelper.parseBoolean(request.getParameter("iconOnly"));
    String fileEncoding = com.lcs.wc.load.LoadCommon.getFileEncoding();

if(!iconOnly){

    String reportName = request.getParameter("reportName");

    linePlanDataCache.refreshDataIfNeeded(); // A client side sort may have been done, causing the cache to be out of date with respect to sorting.
    Collection<com.lcs.wc.db.FlexObject> results = (Collection<com.lcs.wc.db.FlexObject>)linePlanDataCache.getData();
    String oid = (String)request.getParameter("oid");
    if(FormatHelper.hasContent(oid) && oid.indexOf("FlexCollection") > 0 ){
        results = (Collection<com.lcs.wc.db.FlexObject>)mdtDataCache.getData();
     }
    Collection<TableColumn> columns = (Collection<TableColumn>)session.getAttribute("columns");
    TableDataModel tdm = (TableDataModel) session.getAttribute("tdm");
    if(tdm!=null) {
        excelg.setTableDataModel(tdm);
    }
    String extJSFilter = request.getParameter("extJSFilter");
    System.out.println("extJSFilter>>>>>>>>>>>>>"+extJSFilter);
	if(extJSFilter !=null  && extJSFilter.contains("_QUOT_Adidas\\\\Socks Type") ){
			extJSFilter=extJSFilter.replace("_QUOT_Adidas\\\\Socks Type\\\\Socks_QUOT", "_QUOT_Adidas\\\\Socks\\\\Athletic_QUOT");
			extJSFilter=extJSFilter.replace("_QUOT_Adidas\\\\Socks Type\\\\Team_QUOT", "_QUOT_Adidas\\\\Socks\\\\Team_QUOT");
			System.out.println("modified extJSFilter>>>>>>>>>>>>>"+extJSFilter);
		}
		
    results = com.lcs.wc.util.json.JSONHelper.filterResults(results, columns, extJSFilter);
    boolean sameSeason = linePlanDataCache.isSameSeason (request.getParameter("oid"));

    ExcelTableHeaderGenerator ethg = (ExcelTableHeaderGenerator)session.getAttribute("ExcelTableHeaderGenerator");
    if(ethg != null){
        excelg.setExcelTableHeaderGenerator(ethg);
    }

    boolean isCSV = "true".equals(request.getParameter("csv"));

    Collection<TableColumn> groupByColumns = (Collection<TableColumn>)request.getAttribute("groupByColumns");
    if(groupByColumns == null){
        groupByColumns = new ArrayList<TableColumn>();
        String groupByAttribute = FormatHelper.format(""+request.getParameter("groupByAttribute"));
        if(FormatHelper.hasContent(groupByAttribute)){
            for (TableColumn column : columns) {
                if(column!=null) {
                    if(column.getTableIndex()!=null) {
                        if(column.getTableIndex().equals(groupByAttribute)) {
                            column.setDisplayed(false);
                            column.setShowGroupByHeader(true);
                            groupByColumns.add(column);
                        }
                    }
                }
            }
       }
    }


    if("true".equals(request.getParameter("discreteRows"))){
        excelg.setShowDiscreteRows(true);
    }

    if("true".equals(request.getParameter("showTotals"))){
        excelg.setShowTotals(true);
    }

    if("true".equals(request.getParameter("showSubTotals"))){
        excelg.setShowSubTotals(true);
    }

    if(groupByColumns != null && groupByColumns.size() > 0){
        excelg.setGroupByColumns(groupByColumns);
    }

    if("true".equals(request.getParameter("spaceBetweenGroups"))){
        excelg.setSpaceBetweenGroups(true);
    }else{
        excelg.setSpaceBetweenGroups(false);
    }


    if(FormatHelper.hasContent(request.getParameter("cellClassDark"))){
        excelg.cellClassDark = request.getParameter("cellClassDark");
    }
    if(FormatHelper.hasContent(request.getParameter("cellClassLight"))){
        excelg.cellClassLight = request.getParameter("cellClassLight");
    }
    if(FormatHelper.hasContent(request.getParameter("tableHeaderClass"))){
        excelg.tableSubHeaderClass = request.getParameter("tableHeaderClass");

    }
    if(FormatHelper.hasContent(request.getParameter("tableTotalsClass"))){
        excelg.totalsClass = request.getParameter("tableTotalsClass");

    }
    if(FormatHelper.hasContent(request.getParameter("fontSize"))){
        excelg.fontSize = request.getParameter("fontSize");
    }
    excelg.setShowRepeatedData(false);
    excelg.setRepeatedDataColumnMap(repeatedDataColumnMap);

    String layout = FormatHelper.format(request.getParameter("layout"));


 %>


 <% if (isCSV && columns != null){
    String csvExportFilePath = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.client.web.ExcelGenerator.exportLocation", "none"));
    String timeToLive = LCSProperties.get("time.before.temp.files.are.deleted", "1");

    DeleteFileHelper dFH = new DeleteFileHelper();
    dFH.deleteOldFiles(wt_home + csvExportFilePath,timeToLive);

    for (TableColumn column : columns){
        if (column != null) {
            if (column instanceof LineSheetActionsTableColumn ||
                column instanceof ActionsTableColumn ||
                column instanceof MultiObjectTableColumn ||
                column instanceof ExpandRowTableColumn ||
                column.getFormElement() != null ||
                (column.getActions() != null)
            ){
                column.setDisplayed(false);
            }
        }
    }


    String seasonName = null;
    LCSSeason season = null;
    if (FormatHelper.hasContent(oid) && oid.indexOf("LCSSeason") > 0) {
        season = (LCSSeason)com.lcs.wc.foundation.LCSQuery.findObjectById(oid);
        //seasonName = season == null ? null : season.getName();
		seasonName = season == null ? null : season.getName().contains("/")? season.getName().replace("/"," "):season.getName() ;
    }
    String gridHiddenColumns = request.getParameter("gridHiddenColumns");
    if (FormatHelper.hasContent(gridHiddenColumns)){
        columns = com.lcs.wc.util.json.JSONHelper.filterHiddenColumns(columns, gridHiddenColumns);
    }

    if (sameSeason ){
        String downloadFileKey = String.valueOf(new java.util.Date().getTime());
        String fileLocation = excelg.drawTable(results, columns, lcsContext, reportName + "_" + downloadFileKey, seasonName);
        lcsContext.requestedDownloadFile.put(downloadFileKey, fileLocation);
        out.append(CONTEXT_URL_MASTER_CONTROLLER + "?forwardedFileForDownload=true&forwardedFileForDownloadKey=" + downloadFileKey);
    }else if (FormatHelper.hasContent(oid) && oid.indexOf("FlexCollection") > 0 ){
        com.lcs.wc.collection.FlexCollection collection = (com.lcs.wc.collection.FlexCollection)com.lcs.wc.foundation.LCSQuery.findObjectById(oid);
        String collectionName = collection == null ? null : collection.getName();
        String downloadFileKey = String.valueOf(new java.util.Date().getTime());
        String fileLocation = excelg.drawTable(results, columns, lcsContext, reportName + "_" + downloadFileKey, collectionName);
        lcsContext.requestedDownloadFile.put(downloadFileKey, fileLocation);
        out.append(CONTEXT_URL_MASTER_CONTROLLER + "?forwardedFileForDownload=true&forwardedFileForDownloadKey=" + downloadFileKey);
    } else {
        season = linePlanDataCache.getLineSheetQueryOptions ().getSeason ();
        String name2 = season.getName();
        out.append(CONTEXT_URL_MASTER_CONTROLLER + "?activity=VIEW_LINE_PLAN&action=TWO_SEASONS&name1=" + seasonName + "&name2=" + name2 + "&oid=" + request.getParameter("oid"));

    }
 }
} %>
