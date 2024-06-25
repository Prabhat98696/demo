<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- ///AGRON FLEXPLM 11.1 UPGRADE CODE CSP :-Populate Season Type and Year on Samples//--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   onLoad  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    AgronImagePagesCreateOnLoad_CSP.jsp:   
  
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////  Add to the import package/class list as required  ////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,                
                com.lcs.wc.client.web.*,    
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.sourcing.*,
                wt.part.WTPartMaster,
                com.lcs.wc.product.LCSProduct,
 				com.lcs.wc.flextype.FlexType,
 				com.lcs.wc.foundation.LCSQuery,
 				com.lcs.wc.util.VersionHelper,
                java.util.*,
				com.lcs.wc.season.*,
				com.lcs.wc.specification.FlexSpecToSeasonLink"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="appContext" class="com.lcs.wc.client.ApplicationContext" scope="session"/>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////// INITIALIZATION JSP CODE and CSP environment ///////////////////--%>
<%-- ///// This section is generic and is included in all the Client Side Plugins ////////--%>
<%-- ////// Do not alter this section unless instructed to do so via the comments  ///////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%     

    String activity = request.getParameter("activity");
    String action = request.getParameter("action");
    String oid = request.getParameter("oid");
    String type = request.getParameter("type");
	String seasonType ="";
	String year="";
	LCSSeason season = null;
	//LCSSeason season = (LCSSeason) LCSQuery.findObjectById(appContext.getSeasonId());
	
	String argSampleSeasonType = LCSProperties.get("com.agron.wc.samplerequest.seasonType");
	String argSampleYear = LCSProperties.get("com.agron.wc.samplerequest.year");

	String argSeaSeasonType = LCSProperties.get("com.agron.wc.season.seasonType");
	String argSeaYear = LCSProperties.get("com.agron.wc.season.year");
    
    ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
	//FlexType csType = (FlexType) LCSQuery.findObjectById(type);
	FlexType csType = csph.getFlexType();
	
	String attColumnSeasonType = csType.getAttribute(argSampleSeasonType).getSearchCriteriaIndex();
	String attColumnYear = csType.getAttribute(argSampleYear).getSearchCriteriaIndex();

	attColumnSeasonType=attColumnSeasonType.substring(attColumnSeasonType.indexOf("_")+1,attColumnSeasonType.length());
	attColumnYear=attColumnYear.substring(attColumnYear.indexOf("_")+1,attColumnYear.length());

    String flextypeName = csph.getFlexTypeName();
	if(oid.contains("LCSSourceToSeasonLink"))
	{
		LCSSourceToSeasonLink sourceSeasonLink = (LCSSourceToSeasonLink)LCSQuery.findObjectById(oid);
		season = (LCSSeason)VersionHelper.latestIterationOf(sourceSeasonLink.getSeasonMaster());
	}

	if(season!=null){
		
		seasonType = (String) season.getValue(argSeaSeasonType);
		year =(String) season.getValue(argSeaYear);
	}

%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JSP METHODS /////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%! 
    public static final String JSPNAME = "ClientSidePlugin";
    public static final boolean DEBUG = true;

%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ///////////////////////////////// JAVSCRIPT PLUGIN LOGIC ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

<script>

    // ***** Begining of function pattern *************************************************

    function setValuesOnSamplePage(){ 

		var formObjSeasonType = eval("document.MAINFORM.LCSSAMPLEREQUEST_"+'<%= attColumnSeasonType%>');
		var formObjYear = eval("document.MAINFORM.LCSSAMPLEREQUEST_"+'<%= attColumnYear%>');

			  if(formObjSeasonType){
					formObjSeasonType.value = '<%= seasonType %>';
			   }

			   if(formObjYear){
					formObjYear.value = '<%= year %>';
			   }

		
    }
   
    function runLoadFunctions(){ 
		var season = '<%= season %>';
		if(season != null){
			setValuesOnSamplePage();
		}
       
    }


</script>

<script>runLoadFunctions();</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

