<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   submit  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    ProductCopy_submit_CSP.jsp:   // Replace with actual filename //
  
--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.client.Activities,                
                com.lcs.wc.client.web.*,    
                com.lcs.wc.util.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                com.lcs.wc.foundation.*,
                wt.ownership.*,
                java.util.*"
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
    
    String flexTypeLabel = "";
    String flexTypePath = ""; 
    
    ClientSidePluginHelper csph = new ClientSidePluginHelper();
    csph.init(type);
    String flextypeName = csph.getFlexTypeName();
    //FlexType flextype = csph.getFlexType();
      //csph.listKeyElements();
      //csph.listKeyLabels();
      //csph.listKeyVariableTypes();
    
    ArrayList functionsToHandle = new ArrayList();
    //Add a line to add the javascript function to the attKeyNamesToHandle ArrayList
    //attKeyNamesToHandle.add("jsFunctionName");
    functionsToHandle.add("validateCopyRelationshipType");
    // if required add java code after this line   

   
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
    <%-- ###########################################################################################/--%>
    <%-- This is used to Automatically generate a handler routing function/--%>

   <%= csph.drawSubmitJsFunctionHandler(functionsToHandle)%>
    
    <%-- ###########################################################################################/--%>

    // ***** Begin function pattern *************************************************
    /////////////////////////////////////////////////////////////////////////////////////
    function validateCopyRelationshipType(){
       var eventsComplete = true;
	  var originalProductType='<%= flextypeName %>';
	  var targetProductTypeDoc = document.MAINFORM.productTypedata;
	 var relationshipTypeDoc = document.MAINFORM.copyModedata;
	  if(targetProductTypeDoc != undefined && relationshipTypeDoc !=undefined ){
	   var targetProductTypeLabel = targetProductTypeDoc.options[targetProductTypeDoc.selectedIndex].label;
	   if(originalProductType !=null  && targetProductTypeLabel !=null && targetProductTypeLabel !=undefined ){
		   if((originalProductType.indexOf("Backpacks") >-1 && targetProductTypeLabel.indexOf("Bags")>-1) || (originalProductType.indexOf("SMU & Clearance") >-1 && targetProductTypeLabel.indexOf("Adidas")>-1) ){
                
			   var relationshipTypeOptionElement = document.MAINFORM.copyModedata.options[relationshipTypeDoc.selectedIndex];
			   if(relationshipTypeOptionElement !=undefined){
				        var relationshipTypeValue = relationshipTypeOptionElement.value;
			   if(relationshipTypeValue == null || relationshipTypeValue==undefined || relationshipTypeValue.trim()==""){
			alert("You must select a value for: Relationship Type"); 
			eventsComplete=false;
		 }
			   }
	      
		   }   
	   }		
	  }
	       
       return eventsComplete;    
    }

    // ***** End Validation function pattern *************************************************


    //////////////////////////////////////////////////////////////////////////////////////    
    // This is provided for testing
    
    function submitEventFromJSPAlert(test) { 
       alert(" " + test + " Client Side " + "\r" +
             "  supporting the flextype [ <%= flextypeName %> ] " 
            ) 
    }

</script>


<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////// End of Submit Client Side Plugin ////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
