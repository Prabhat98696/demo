<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- ///AGRON FLEXPLM 11.1 UPGRADE CODE CSP :-Image Page Autopopulation of Type and Year//--%>
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
	System.out.println("oid ======> "+oid);
   
	String argDocSeasonType = LCSProperties.get("com.agron.wc.document.seasonType");
	String argDocYear = LCSProperties.get("com.agron.wc.document.year");

	String argSeaSeasonType = LCSProperties.get("com.agron.wc.season.seasonType");
	String argSeaYear = LCSProperties.get("com.agron.wc.season.year");
    
    ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
//	FlexType csType = FlexTypeCache.getFlexTypeFromPath("Document\\Images Page");
	FlexType csType = csph.getFlexType();
	
	String attColumnSeasonType = csType.getAttribute(argDocSeasonType).getSearchCriteriaIndex();
	String attColumnYear = csType.getAttribute(argDocYear).getSearchCriteriaIndex();

	attColumnSeasonType=attColumnSeasonType.substring(attColumnSeasonType.indexOf("_")+1,attColumnSeasonType.length());
	attColumnYear=attColumnYear.substring(attColumnYear.indexOf("_")+1,attColumnYear.length());
	

    String flextypeName = csph.getFlexTypeName();

	//spec and season  selected
	if(oid.contains("FlexSpecToSeasonLink"))
	
	{
		FlexSpecToSeasonLink specSeasonLink = (FlexSpecToSeasonLink)LCSQuery.findObjectById(oid);
		season = (LCSSeason)VersionHelper.latestIterationOf(specSeasonLink.getSeasonMaster());
	}

	//season and source
	else if(oid.contains("LCSSourceToSeasonLink"))
	{
		LCSSourceToSeasonLink sourceSeasonLink = (LCSSourceToSeasonLink)LCSQuery.findObjectById(oid);
		season = (LCSSeason)VersionHelper.latestIterationOf(sourceSeasonLink.getSeasonMaster());
	}
	
	else if(oid.contains("LCSProductSeasonLink"))
	{
		LCSProductSeasonLink spl = (LCSProductSeasonLink)LCSQuery.findObjectById(oid);
		season = (LCSSeason)VersionHelper.latestIterationOf(spl.getSeasonMaster());
	}

	else if(oid.contains("LCSProduct"))
	{
		LCSProduct product = (LCSProduct)LCSQuery.findObjectById(oid);
		season = product.findSeasonUsed();
	}

	if(season !=null){
		seasonType =(String) season.getValue(argSeaSeasonType);
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

    function setValuesOnImagePage(){
		
	var formObjSeasonType = eval("document.MAINFORM."+'<%= attColumnSeasonType%>');
		var formObjYear = eval("document.MAINFORM."+'<%= attColumnYear%>');
		if(formObjSeasonType){
				  formObjSeasonType.value = '<%= seasonType %>';
			   }

			   if(formObjYear){
					formObjYear.value = '<%= year %>';
			   }
		
    }
   
    function runLoadFunctions(){ 
		
		var season = '<%= season %>';
		if(season != null ){
			setValuesOnImagePage(); 
		}
		
       
    }


</script>

<script>runLoadFunctions();</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

