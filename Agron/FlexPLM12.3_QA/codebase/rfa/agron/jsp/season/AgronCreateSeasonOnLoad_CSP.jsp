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
				com.lcs.wc.flextype.FlexTypeCache,
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
	
	FlexType product_FT = null;
	product_FT = FlexTypeCache.getFlexTypeRoot("Product");
	String productId=product_FT.getIdNumber();

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

    // ***** Product Type on Season Creation page*************************************************

    function setValuesOnSeasonCreate(){ 
	
		document.MAINFORM.productTypeId.value = 'VR:com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition:'+'<%= productId %>';
		
    	var productTypeDisplay = document.getElementById("productTypeDisplay");
			productTypeDisplay.innerHTML = 'Product';
		
  }
   
    function runLoadFunctions(){ 
		
			setValuesOnSeasonCreate();
		
       
    }


</script>

<script>runLoadFunctions();</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of onLoad Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>

