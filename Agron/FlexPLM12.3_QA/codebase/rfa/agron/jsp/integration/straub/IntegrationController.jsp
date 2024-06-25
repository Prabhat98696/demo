<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

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
                java.util.*,
            com.lcs.wc.util.*,
			com.agron.wc.integration.straub.outbound.AgronStraubIntegrationJob,
			com.agron.wc.integration.reports.pdx.PDXReportJob,
			com.agron.wc.sftpImageExport.AgronSKUImageExport"
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INCLUDED FILES  //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="request"/>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="session"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// STATIS JSP CODE //////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%!
	public static final String JSPNAME = "IntegrationController";
	public static final String MAINTEMPLATE = PageManager.getPageURL("MAINTEMPLATE", null);
	public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
	public static final String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%
	lcsContext.setCacheSafe(true);
    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
	String startDate = request.getParameter("startDate");
	String endDate = request.getParameter("endDate");
	String user = request.getParameter("sesUser");
    String ownerId = request.getParameter("ownerId");
    String returnActivity = request.getParameter("returnActivity");
    String returnAction = request.getParameter("returnAction");
    String returnOid = request.getParameter("returnOid");
    boolean checkReturn = false;

    String title = "";
    String errorMessage = request.getParameter("errorMessage");
    String infoMessage = request.getParameter("infoMessage");
    String view = null;
   	String type = "";

    String dashboard = "";
    String dashboardName = "";
    String TEMPLATETYPE = request.getParameter("templateType");

	if("VIEW_INTEGRATION".equals(activity)){ 
		if ("INIT".equals(action)) 
		{
			view = "INTEGRATION_VIEW_PAGE";
			title = "Integration Data";
		} 
		else if("RUN".equals(action)) 
		{
			view = "INTEGRATION_VIEW_PAGE";
			title = "Integration Data";
			try { 	
				AgronStraubIntegrationJob.executeStraubIntegartion("NC", null, null);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if("RUNREPORT".equals(action)) 
		{
			view = "INTEGRATION_VIEW_PAGE";
			title = "Integration Data";
			try { 	
				System.out.println("0001 ---Controller oid  ---sesUser"+user);
				PDXReportJob.executePDXReport(oid,startDate,endDate,user);
				System.out.println("0001 ---Controller after calling  ---PDXReportJob");
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if("RUNDATAWAREHOUSEEXPORT".equals(action)) 
		{
			view = "INTEGRATION_VIEW_PAGE";
			title = "Integration Data";
			try { 	

				com.agron.wc.integration.dw.ETLProcessor.process();
				//processor = new com.agron.wc.integration.dw.ETLProcessor();
				//processor.process();

			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if("RUNCOLORWAYTHUMBNAILEXPORT".equals(action)) 
		{
			view = "INTEGRATION_VIEW_PAGE";
			title = "Integration Data";
			try { 	

				AgronSKUImageExport.processImages("NC",null,null);


			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	 
    String contentPage = null;
    if(view != null){
        contentPage = PageManager.getPageURL(view, null);
    } else {
        contentPage = "";
    }

%>

<jsp:forward page="<%=subURLFolder+ MAINTEMPLATE %>">
	<jsp:param name="title" value="<%= java.net.URLEncoder.encode(title, defaultCharsetEncoding)%>" />
   	<jsp:param name="infoMessage" value="<%= infoMessage %>" />
   	<jsp:param name="errorMessage" value="<%= errorMessage %>" />
   	<jsp:param name="requestedPage" value="<%= contentPage %>" />
   	<jsp:param name="contentPage" value="<%= contentPage %>" />
    <jsp:param name="oid" value="<%= oid %>" />
    <jsp:param name="action" value="<%= action %>" />
    <jsp:param name="activity" value="<%= activity %>" />
   	<jsp:param name="objectType" value="Season" />
   	<jsp:param name="typeClass" value="com.lcs.wc.season.LCSSeason" />
    <jsp:param name="dashboard" value="<%= dashboard %>" />
    <jsp:param name="dashboardName" value="<%= dashboardName %>" />
    <jsp:param name="templateType" value="<%= TEMPLATETYPE%>" />
</jsp:forward>
