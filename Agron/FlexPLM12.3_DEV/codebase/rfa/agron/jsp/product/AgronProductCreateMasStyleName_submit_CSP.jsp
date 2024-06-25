<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////   submit  PAGE DOCUMENTATION    //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%--
    JSP Type: ClientSidePlugin

    ProductCreateMasStyleName_submit_CSP.jsp:   // Replace with actual filename //
  
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
    //System.out.println("submit..type = " + type);   
    //System.out.println("submit...activity = " + activity);   
    //System.out.println("submit...action = " + action);   
    
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
    //functionsToHandle.add("masStyleNameCharacterLimit");
  
    // if required add java code after this line   
	String argProductAttribute="";
	
   if(flextypeName.equals("Backpacks"))
		{	
		    argProductAttribute = LCSProperties.get("com.agron.wc.product.Backpacks");   
		}
	else if(flextypeName.equals("Bags"))
		{	
		    argProductAttribute = LCSProperties.get("com.agron.wc.product.Bags");   
		}
	else if(flextypeName.equals("Hats"))
		{
			argProductAttribute = LCSProperties.get("com.agron.wc.product.Hats"); 
		}
	else if(flextypeName.equals("Knits"))
		{
			argProductAttribute = LCSProperties.get("com.agron.wc.product.Knits"); 
		}
	else if(flextypeName.equals("Athletic"))
		{
			argProductAttribute = LCSProperties.get("com.agron.wc.product.Socks");
		}
	else if(flextypeName.equals("Team"))
		{
			argProductAttribute = LCSProperties.get("com.agron.wc.product.Team");
		}	
	else if(flextypeName.equals("Sport Accessories"))
		{
			argProductAttribute = LCSProperties.get("com.agron.wc.product.SportAccessories");
		}
	else if(flextypeName.equals("Underwear"))
		{
			argProductAttribute = LCSProperties.get("com.agron.wc.product.Underwear");
		}
		
	 String[] productAttList = argProductAttribute.split("~~");
	 functionsToHandle.add("masStyleNameCharacterLimit");
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
    function masStyleNameCharacterLimit(){
	 var eventsComplete = true;
	 var masStyleName= "";
	
	var FTName =  '<%=FormatHelper.encodeForJavascript(String.valueOf(flextypeName))%>';

	  if(FTName !='SMU & Clearance'){
	 <%for(int i=0; i<productAttList.length; i++){%>

		if(document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.value.includes("agr") && !document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.value.includes("agrStyleName")){
			
			<%if("agrSilhouetteProd".equals(productAttList[i])){%>
			
			 var selectOptionKey = document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.name;
			 var optionValueElement = document.getElementById(selectOptionKey+"select");
			 var optionValue = optionValueElement.options[optionValueElement.selectedIndex].text;
			 
			if(optionValue!=null){
				masStyleName = masStyleName + optionValue+" ";
			}else{
				masStyleName = masStyleName + document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.options[document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.selectedIndex].label+" ";
			}
			<%}else{%>
				 masStyleName = masStyleName + document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.options[document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.selectedIndex].label+" ";
			
			<%}%>
		}else{
			 masStyleName = masStyleName + document.MAINFORM.<%= csph.getKeyElement(productAttList[i])%>.value+" ";
		}
	<%}%>
		// i have added one extra space at masStyleName so add 31 
		if((masStyleName.length > 31) && !(FTName =='SMU & Clearance'))
		{
		var okOrCancel = confirm("MAS Style Name is more than 30 characters");
		if (okOrCancel == true) {
		 	return true;
		} else {
			return false;
		}
		
			return eventsComplete;    
		}
	 }
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
