<%-- Copyright (c) 2002 Aptavis Technologies Corporation   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@ page language="java"
        import="com.lcs.wc.db.SearchResults,
                com.lcs.wc.client.web.PageManager,
                com.lcs.wc.client.web.WebControllers,
                com.lcs.wc.client.Activities,
                com.lcs.wc.flextype.*,
                com.lcs.wc.db.*,
				wt.util.*,
                com.lcs.wc.season.*,
                java.util.*,
                com.lcs.wc.util.*,
				java.net.URL,
				java.io.File,
				com.lcs.wc.document.LCSDocumentClientModel,
				java.sql.Timestamp,
				java.text.DateFormat,
				java.text.ParseException,
				java.text.SimpleDateFormat,
				java.util.Calendar,
				java.util.Date,
				java.util.List,
				java.util.Map,
				java.util.TimeZone,
				com.lcs.wc.document.FileRenamer,
				wt.org.WTUser,
				com.agron.wc.load.AgronDataLoader"
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="session"/>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>"/>
<% wt.util.WTContext.getContext().setLocale(request.getLocale());%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
    public static final String JSPNAME = "Data Loader";
    public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
	public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
	public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
    public static final String adminGroup = LCSProperties.get("jsp.main.administratorsGroup", "Administrators");
	String formType = "csvLoadFile";
	StringBuilder errorLog = new StringBuilder();
	String errorMsg = "";

%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%

String workitemExceptionReportTitle = WTMessage.getLocalizedMessage ( RB.WF, "workitemExceptionReport_PG_TLE", RB.objA ) ;
String flexPDMAttributeDefinitionsPgTle = WTMessage.getLocalizedMessage ( RB.REPORTS, "flexPDMAttributeDefinitions_PG_TLE", RB.objA ) ;
String flexPDMReportsPgTle = WTMessage.getLocalizedMessage ( RB.REPORTS, "flexPDMReports_PG_TLE", RB.objA ) ;
String costingReportPgTle = WTMessage.getLocalizedMessage ( RB.REPORTS, "costingReport_PG_TLE", RB.objA ) ;
String seasonLabel = WTMessage.getLocalizedMessage ( RB.REPORTS, "season_LBL", RB.objA ) ;
String sampleDashboard = WTMessage.getLocalizedMessage ( RB.REPORTS, "sampleDashboards_LBL", RB.objA ) ;
String noAccessToTypeManagerFunctionsAlrt = WTMessage.getLocalizedMessage (RB.FLEXTYPE, "noAccessToTypeManagerFunctions_ALRT", RB.objA ) ;
%>
<%
    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String returnActivity = request.getParameter("returnActivity");
    String returnAction = request.getParameter("returnAction");
    String returnOid = request.getParameter("returnOid");
    boolean checkReturn = false;

    String title = "";
    String errorMessage = (FormatHelper.hasContent(request.getParameter("errorMessage")))?
		java.net.URLDecoder.decode(request.getParameter("errorMessage"), defaultCharsetEncoding):"";
    String infoMessage = request.getParameter("infoMessage");
    String view = null;
    String type = "";

    String dashboard = "";
    String dashboardName = "";
    String TEMPLATETYPE = request.getParameter("templateType");

    boolean hasAdminAccess = (FormatHelper.hasContent(adminGroup) && lcsContext.inGroup(adminGroup.toUpperCase()));


	System.out.println("ACTION--"+action);

	if("LOAD_EXCEL_DATA".equals(activity))
	{
		if ("INIT".equals(action)) 
		{
			view = "VIEW_EXCEL_UPLOAD";
			title = "Load Data";
		} 
		else if("LOAD".equals(action)) 
		{
			view = "VIEW_EXCEL_UPLOAD";
			title = "Load Data";
			String dataFileName = "";
			try
			{ 	
				boolean ignoreBlankValue=false;
				System.out.println("ignoreBlankValue-first-"+request.getParameter("ignoreBlankValue"));				
				if(FormatHelper.hasContent(request.getParameter("ignoreBlankValue")) && "true".equals(request.getParameter("ignoreBlankValue"))){
					ignoreBlankValue = true;
				}
				System.out.println("ignoreBlankValue--"+ignoreBlankValue);
				dataFileName = (String)request.getAttribute("loadDataFile");
				String loaderType = (String)request.getParameter("LoaderProgramme");
				String fileLabelText = (String)request.getAttribute("fileLabelText");
				System.out.println("dataFileName::"+dataFileName+", fileLabelText::"+fileLabelText+", loaderType::"+loaderType);
				WTUser user = lcsContext.getUser();
				AgronDataLoader dataLoader = new AgronDataLoader();
				dataLoader.setVariables(user, dataFileName, loaderType, ignoreBlankValue);
				String message = dataLoader.loadData();
				request.setAttribute("uploadMessage",message);
				
			}
			catch(Exception e) 
			{
				e.printStackTrace();
			}
		}
		
	}
		 	 
    String contentPage = null;
    if(view != null)
		{
        contentPage = PageManager.getPageURL(view, null);
    } 
	else 
	{
        contentPage = "";
    }


    ////////////////////////////////////////////////////////////////////////////
    // CHECK RETURN ACTIVITY..
    ////////////////////////////////////////////////////////////////////////////
    if(FormatHelper.hasContent(returnActivity) && checkReturn){
        activity = returnActivity;
        action = returnAction;
        oid = returnOid;
        returnActivity = "";
        returnAction = "";
        returnOid = "";
        %>
        <jsp:forward page="<%=subURLFolder+ WebControllers.MASTER_CONTROLLER %>">
            <jsp:param name="action" value="<%= action %>" />
            <jsp:param name="activity" value="<%= activity %>" />
            <jsp:param name="oid" value=""/>
            <jsp:param name="returnOid" value="" />
            <jsp:param name="returnAction" value="" />
            <jsp:param name="returnActivity" value="" />
            <jsp:param name="returnActivity" value="" />
        </jsp:forward>
        <%
    }

%>
<jsp:forward page="<%=subURLFolder+ MAINTEMPLATE %>">
    <jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
    <jsp:param name="infoMessage" value="<%= infoMessage %>" />
    <jsp:param name="errorMessage" value="<%= java.net.URLEncoder.encode(errorMessage, defaultCharsetEncoding) %>" />
    <jsp:param name="requestedPage" value="<%= contentPage %>" />
    <jsp:param name="contentPage" value="<%= contentPage %>" />
    <jsp:param name="oid" value="<%= oid %>" />
    <jsp:param name="action" value="<%= action %>" />
    <jsp:param name="activity" value="<%= activity %>" />
    <jsp:param name="formType" value="<%= formType %>" />

</jsp:forward>
