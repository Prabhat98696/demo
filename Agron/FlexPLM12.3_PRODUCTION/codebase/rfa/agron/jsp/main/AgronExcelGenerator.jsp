<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
          import="com.lcs.wc.client.web.*,
                java.util.*,
                com.lcs.wc.util.*,
                org.apache.logging.log4j.Logger,
                org.apache.logging.log4j.LogManager,

                com.lcs.wc.season.*,
                com.lcs.wc.report.TableDataModel,
                wt.util.*"
%>
<jsp:useBean id="excelg" scope="request" class="com.lcs.wc.client.web.ExcelGenerator" />
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="repeatedDataColumnMap" scope="request" class="java.util.Hashtable" />
<%!
    private static final Logger logger = LogManager.getLogger("rfa.jsp.main.ExcelGenerator");
    public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
    public static final String WT_IMAGE_LOCATION = LCSProperties.get("flexPLM.windchill.ImageLocation");

    public static String getUrl = "";
    public static String WindchillContext = "/Windchill";
     public static String wt_home =  "";

    static
    {
        try
        {
            WTProperties wtproperties = WTProperties.getLocalProperties();
            getUrl = wtproperties.getProperty("wt.server.codebase","");
            WindchillContext = "/" + wtproperties.getProperty("wt.webapp.name");
            wt_home =  wtproperties.getProperty("wt.home");

        } catch(Exception e){
            e.printStackTrace();
        }
    }
%>
<%
    Object[] objA = new Object[0];
    String RB_MAIN = "com.lcs.wc.resource.MainRB";
    String excelTooltip = WTMessage.getLocalizedMessage ( RB_MAIN, "excelTooltip", objA ) ;
    boolean iconOnly = FormatHelper.parseBoolean(request.getParameter("iconOnly"));
    String fileEncoding = com.lcs.wc.load.LoadCommon.getFileEncoding();
    String layout = FormatHelper.format(request.getParameter("layout"));
    boolean skipIconRendering = FormatHelper.parseBoolean(request.getParameter("skipIconRendering"));
    if(!skipIconRendering) {
%>
<a id='excelButton' onmouseover="return overlib('<%= FormatHelper.formatJavascriptString(excelTooltip) %>');" onmouseout="return nd();" class="" href="javascript:generateExcel()"><img border="0" src="<%=WT_IMAGE_LOCATION%>/file_excel.gif"></a>&nbsp;
<%
    }
if(!iconOnly){

    String reportName = request.getParameter("reportName");

    Collection<com.lcs.wc.db.FlexObject> results = (Collection<com.lcs.wc.db.FlexObject>)request.getAttribute("data");
    Collection<TableColumn> columns = (Collection<TableColumn>)request.getAttribute("columns");
    TableDataModel tdm = (TableDataModel) request.getAttribute("tdm");
    if(tdm!=null)
    {
        excelg.setTableDataModel(tdm);
    }
 //   logger.debug("tdm-----"+tdm);

    ExcelTableHeaderGenerator ethg = (ExcelTableHeaderGenerator)request.getAttribute("ExcelTableHeaderGenerator");
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

    Hashtable hMapTest = new Hashtable();
    hMapTest = (Hashtable)request.getAttribute("columnKey");
    boolean isColorType = false;

    if(isCSV && columns != null){
        int i = 1;
        String tableId = request.getParameter("HC_Table");
        if (FormatHelper.hasContent(tableId)){
            for (TableColumn column : columns) {
                if(column.isDisplayed()){
                    // Do not include any hidden columns or Image columns in output
                    // Add the "=" in the column.size() as condition, since i starts from 1 not 0 ,it should include max boundary.
                    // otherwise , the last column would not process no matter user show/hide last column. For SPR 2083870
                    if (!FormatHelper.parseBoolean(request.getParameter("deActivateHidden"))&&!FormatHelper.hasContent(request.getParameter("HC"+i))&&
                            i<=columns.size()/* || (column.isImage()) || (column.getActions() != null) || column instanceof MultiObjectTableColumn */) {
                        //it.remove();
                        column.setDisplayed(false);
                    }
                }
                //Check added for SPR 6179357, columns were getting removed while excel file generation
                if(hMapTest != null){
                    TableColumn colorType =  (TableColumn)hMapTest.get("Color.typename");
                    if(colorType != null) {
                        isColorType =  colorType.equals(column);
                    }
                }
                // FOR SPR 2098000 adjust the HC count with current count
                if(!groupByColumns.contains(column) || isColorType)
                   i = i + 1;
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

    String objOid = (String)request.getParameter("oid");
    String userActivity = "VIEW_LINE_PLAN";
    if (FormatHelper.hasContent(objOid) && objOid.indexOf("FlexCollection") > 0 ){
        userActivity = "VIEW_FLEXCOLLECTION";
    }
 %>


    <script>
            function generateExcel(){
                <%if("filmstrip".equals(layout)){%>
                    if(document.MAINFORM.showThumbs){
                        document.MAINFORM.showThumbs.value = "true";
                    }
                <%} else if("freezepane".equals(layout)){%>
                    generateAJAXExcel();
                    return;
                <%}%>
               document.MAINFORM.activity.value = document.REQUEST.activity;
               document.MAINFORM.action.value = document.REQUEST.action;
                if (document.MAINFORM.updateMode ) {
                  document.MAINFORM.updateMode.value='false';
                }
                document.MAINFORM.csv.value = 'true' ;
                submitForm();

            }

            function generateAJAXExcel() {
                showExporting(true);
                var filters;
                var gridColumns;
                var gridHiddenColumns = '';
                var flexExtGrid = Ext.ComponentMgr.get('lineSheetGrid');
                if (flexExtGrid) {
                    filters = flexExtGrid.getController().getFilters();
                    gridColumns= flexExtGrid.columns;
                }
                else {
                    filters = extJSGetFilters();
                }
                if (gridColumns){
                    for (var i = 0; i < gridColumns.length; i++){
                        if (gridColumns[i].hidden){
                            gridHiddenColumns = gridHiddenColumns + ' ' +gridColumns[i].dataIndex;
                        }
                    }
                }
                var objActivity='<%=userActivity%>';
                var url = '<%=PageManager.getContextOverrideUrlAndMasterContorller()%>';
                var sOptions = 'action=AJAX_LINE_PLAN_EXCEL&activity='+objActivity+'&ajaxRequest=true&csv=true&oid='+document.MAINFORM.oid.value
                + '&returnOid='+ document.MAINFORM.returnOid.value
                + '&reportTitle=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("reportTitle"))%>'
                + '&reportName=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("reportName"))%>'
                + '&userName=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("userName"))%>'
                + '&reportDate=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("reportDate"))%>'
                + '&groupByAttribute=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("groupByAttribute"))%>'
                + '&discreteRows=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("discreteRows"))%>'
                + '&showTotals=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("showTotals"))%>'
                + '&showSubTotals=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("showSubTotals"))%>'
                + '&spaceBetweenGroups=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("spaceBetweenGroups"))%>'
                + '&cellClassDark=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("cellClassDark"))%>'
                + '&cellClassLight=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("cellClassLight"))%>'
                + '&tableHeaderClass=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("tableHeaderClass"))%>'
                + '&tableTotalsClass=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("tableTotalsClass"))%>'
                + '&csv=true'
                + '&fontSize=' + '<%=FormatHelper.encodeForJavascript(request.getParameter("fontSize"))%>'
                + '&extJSFilter=' + filters
                + '&gridHiddenColumns=' + gridHiddenColumns
                + '&layout=freezepane';
                if (document.MAINFORM.updateMode ) {
                    sOptions = sOptions + '&updateMode=false';
                }
                <% session.setAttribute("columns", columns);
                   session.setAttribute("tdm", tdm);
                   session.setAttribute("groupByColumns", groupByColumns);
                   session.setAttribute("ExcelTableHeaderGenerator", ethg);
                %>

                runPostAjaxRequest(location.protocol + '//' + location.host + url, sOptions, 'openPDFFile');
            }
    </script>
<% if (isCSV && columns != null){
    String csvExportFilePath = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.client.web.ExcelGenerator.exportLocation", "none"));
    String timeToLive = LCSProperties.get("time.before.temp.files.are.deleted", "1");

    DeleteFileHelper dFH = new DeleteFileHelper();
    dFH.deleteOldFiles(wt_home + csvExportFilePath,timeToLive);

    int i=1;
    for (TableColumn column : columns){
        if (column != null) {
            if (column instanceof LineSheetActionsTableColumn ||
                column instanceof ActionsTableColumn ||
                column instanceof MultiObjectTableColumn ||
                column instanceof ExpandRowTableColumn ||
                column.getFormElement() != null ||
                (column.getActions() != null)
            ){
                //Take column.isImage out when ready to put images into Excel.. Waiting on POI support. Only in Alpha today.
                //it.remove();
                column.setDisplayed(false);
            }
        }
        i = i + 1;
    }


    String downloadFileKey = String.valueOf(new java.util.Date().getTime());
    String oid = (String)request.getParameter("oid");
    String seasonName = null;
    if (FormatHelper.hasContent(oid) && oid.indexOf("LCSSeason") > 0) {
        LCSSeason season = (LCSSeason)com.lcs.wc.foundation.LCSQuery.findObjectById(oid);
        //seasonName = season == null ? null : season.getName();
		seasonName = season == null ? null : season.getName().contains("/")? season.getName().replace("/"," "):season.getName() ;
    }
    String fileLocation = excelg.drawTable(results, columns, lcsContext, reportName + "_" + downloadFileKey, seasonName);
    lcsContext.requestedDownloadFile.put(downloadFileKey, fileLocation);

 %>

<script>
    document.MAINFORM.importCSVFile.value='false';
    document.MAINFORM.csv.value='false';
    var w=flexWindowOpenAsPost('<%=PageManager.getContextOverrideUrlAndMasterContorller()%>' + '?forwardedFileForDownload=true&forwardedFileForDownloadKey=<%=downloadFileKey%>');
</script>
<%}
    if(isCSV && columns != null){
        int i=1;
        for (TableColumn column : columns){
            if (!FormatHelper.hasContent(request.getParameter("HC"+i)) || (column.isImage()) || (column.getActions() != null) || column instanceof MultiObjectTableColumn || column instanceof ExpandRowTableColumn || column instanceof LineSheetActionsTableColumn || column instanceof ActionsTableColumn || column.getFormElement() != null){
                column.setDisplayed(true);
            }
            //Check added for SPR 6179357, columns were getting removed while excel file generation
            if(hMapTest != null){
                TableColumn colorType =  (TableColumn)hMapTest.get("Color.typename");
                if(colorType != null) {
                    isColorType =  colorType.equals(column);
                }
            }
            // FOR SPR 2098000 adjust the HC count with current count
            if(!groupByColumns.contains(column) || isColorType)
               i = i + 1;
        }
    }
} %>
