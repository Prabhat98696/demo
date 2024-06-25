<%@page import="com.lcs.wc.util.LCSProperties"%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>

<% 
	 
  	String fileLocation = LCSProperties.get("com.agron.wc.integration.straub.logFileName");
	
 	String downloadFileKey = new java.util.Date().getTime() + "";
 	lcsContext.requestedDownloadFile.put(downloadFileKey, fileLocation);
	response.getWriter().print(downloadFileKey);	
%>
  