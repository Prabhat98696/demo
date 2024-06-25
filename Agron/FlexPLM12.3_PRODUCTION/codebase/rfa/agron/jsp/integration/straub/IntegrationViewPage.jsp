<%-- Copyright (c) 2002 PTC Inc.   All Rights Reserved --%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// PAGE DOCUMENTATION//////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.classification.ClassificationTreeLoader,
                com.lcs.wc.client.Activities,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                com.lcs.wc.util.aclvalidator.*,
                com.lcs.wc.color.*,
                com.lcs.wc.calendar.*,
                com.lcs.wc.foundation.*,
                com.lcs.wc.season.*,
                com.lcs.wc.db.*,
                com.lcs.wc.flextype.*,
                wt.fc.*,
                wt.vc.*,
                java.util.*,
                wt.util.*,
                wt.part.*,
                com.lcs.wc.flextype.*,
				com.lcs.wc.util.LCSProperties,
				com.agron.wc.integration.straub.util.AgronLogEntryManagement"
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="flexg" scope="request" class="com.lcs.wc.client.web.FlexTypeGenerator" />
<jsp:useBean id="seasonModel" scope="request" class="com.lcs.wc.season.LCSSeasonClientModel" />
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- ////////////////////////////// INITIALIZATION JSP CODE //////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>


<%!
public static final String adminGroup = LCSProperties.get("jsp.main.administratorsGroup", "Administrators");
public static final String agrSeasonType = LCSProperties.get("com.agron.wc.season.seasonType");
%>

<%
String errorMessage = request.getParameter("errorMessage");
Object[] objA = new Object[0];
String RB_MAIN = "com.lcs.wc.resource.MainRB";
String actionsLbl = WTMessage.getLocalizedMessage ( RB_MAIN, "actions_LBL", objA ) ;
LCSSeason season = seasonModel.getBusinessObject();
LCSSeason season1;
String seasonLabel = WTMessage.getLocalizedMessage ( RB_MAIN, "season_LBL",objA ) ;
String userName=lcsContext.getUserName();
System.out.println("Session User:::::::::::::--> "+userName);
java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
java.util.Calendar cal = java.util.Calendar.getInstance();
cal.setTimeZone(FormatHelper.getStandardTimeZone());
%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// JAVSCRIPT ///////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<script language="Javascript">
   	
	function menualTriggerStartQueue(){ 
		document.MAINFORM.activity.value="VIEW_INTEGRATION"; //This is passed to custom.activityControllerMappings.properties
		document.MAINFORM.action.value="RUN";
		submitForm();
		alert("Your request is being processed.");

	}
	function pdxReportStartQueue(){ 
		document.MAINFORM.activity.value="VIEW_INTEGRATION"; //This is passed to custom.activityControllerMappings.properties
		document.MAINFORM.oid.value = document.MAINFORM.reportSeasonOid.options[document.MAINFORM.reportSeasonOid.selectedIndex].value;
		document.MAINFORM.sesUser.value = document.MAINFORM.sesUser.value;
		var vseasonOid = document.MAINFORM.oid.value;
		var vstartDate = document.MAINFORM.startDate.value;
		var vendDate = document.MAINFORM.endDate.value;
		//alert(vseasonOid);
		//alert(document.MAINFORM.oid.value.length);
		//alert(document.MAINFORM.startDate.value.length);
		//alert(vendDate);
		if(document.MAINFORM.endDate.value.length == 0 || document.MAINFORM.startDate.value.length == 0 || document.MAINFORM.oid.value.length == 1 ){
			alert(" Season Name, Start Date & End Date must be selected  to Execute the PDX Report");                
			return false;
		}
		alert("PDX Report will be generated and send as an email.");
		document.MAINFORM.action.value="RUNREPORT";
		submitForm();
		alert("PDX Report will be generated and send as an email.");
		
	}
	
	function dwStartQueue(){ 
		document.MAINFORM.activity.value="VIEW_INTEGRATION"; //This is passed to custom.activityControllerMappings.properties

		document.MAINFORM.action.value="RUNDATAWAREHOUSEEXPORT";
		submitForm();
		 alert("Datawarehouse export started");
		
	}
	
	function colorwayThumnailExportStartQueue(){ 
		document.MAINFORM.activity.value="VIEW_INTEGRATION"; //This is passed to custom.activityControllerMappings.properties

		document.MAINFORM.action.value="RUNCOLORWAYTHUMBNAILEXPORT";
		submitForm();
		 alert("Colorway Thumbnail Export Started");
		
	}
	 
	function accessPermissionDeny(){
		 alert("You are not authorized to run the job.");
	}
	
	function downloadLogFile(){
 		var url = '/Windchill/rfa/agron/jsp/integration/straub/DownloadLogFile.jsp';
		var text = '<br><br><b><center><%= FormatHelper.formatJavascriptString(LCSProperties.get("com.agron.wc.integration.straub.logFileName")) %><br><br><img src=\'' +urlContext +'/images/blue-loading.gif\' border="0"></center><br><br>';
 		var newDiv = document.createElement("div");
		
		newDiv.id = 'waitMessage';
		document.body.appendChild(newDiv);
		newDiv.style.display = 'block';
		newDiv.style.zIndex = 4;
		newDiv.style.position = 'center';		 
		newDiv.className = 'waitMessage';
		newDiv.innerHTML = text;
		
		runAjaxRequest(location.protocol + '//' + location.host + url, 'openDownloadFile');
	}

	function openDownloadFile(xml, text){
		closeDiv("waitMessage");

 		window.open(location.protocol + '//' + location.host+'/Windchill/rfa/jsp/main/Main.jsp?forwardedFileForDownload=true&forwardedFileForDownloadKey='+text);
	}

</script>
<input type="hidden" name="objectType" value="">
<input type="hidden" name="lastRunDate" value="">
<input type="hidden" name="sesUser" value="<%= userName %>">

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<table width="100%" class="BoxWrapper">
    <tr>
        <td class="BoxContent">
            <table width="100%" cellpadding="0" cellspacing="0">
                <tr>
                    <td class="PAGEHEADINGTITLE">
                        Jobs
                    </td>
                
               </tr>
           </table>
       </td>
    </tr>
	<tr>
	
		<td class="BoxContent"> <%= tg.startGroupBorder() %> <%= tg.startTable() %> </td>
			  
				<table class="table-wrapper TABLE_OUTLINE">
			   <tbody>
			   <tr>
					<td  align="left"  class="TABLESUBHEADER" width="15%">Description</td>
					<td  align="left"  class="TABLESUBHEADER" width="15%">Season</td>
					<td  align="left"  class="TABLESUBHEADER" width="15%">Start Time \Last Run Time</td>
					<td  align="left"  class="TABLESUBHEADER" width="15%">End Time \Scheduled Time</td>
					<td  align="left"  class="TABLESUBHEADER" width="15%">Status</td>
					<td  align="left"  class="TABLESUBHEADER" width="15%">Logs</td>
					<td  align="left"  class="TABLESUBHEADER" width="10%">Run</td>
				</tr> 
				<tr>
					<td  width="17%" class="TBLD"><%="Straub Integration"%></td>
					<td  align="left"  class="TBLD" width="15%">  </td>
					<td class="TBLD" valign=middle width="15%" nowrap style="text-align:left;" onmouseout="return nd();">
							<%
							AgronLogEntryManagement logEntryLastRuntime = new AgronLogEntryManagement();
							String lastRunTimeValue=logEntryLastRuntime.getLastRunTime();					 	
							%> <%=lastRunTimeValue%><%=" GMT"%></td>							
							<td class="TBLD" valign=middle width="15%" ><%=" 8:00 PM PST"%></a></td>
							<td class="TBLD" valign=middle width="17%" nowrap style="text-align:left;" onmouseover="return overlib('MM-DD-YYYY:HH:mm:ss GMT');"	onmouseout="return nd();">
							<%
							AgronLogEntryManagement logEntry = new AgronLogEntryManagement();
							String status=logEntry.getStatusOfLogEntry();
						 	%> <%=status%>
					</td>
					<td class="TBLD" valign=middle width="15" ><a href="javascript:downloadLogFile()">Download File</a></td>
					<td class="TBLD" valign=middle width="10%" >
						<% if(adminGroup.equals("ALL") || lcsContext.inGroup(adminGroup.toUpperCase() )){ %>
							<img id="searchButton" title="Run Job" onclick="menualTriggerStartQueue()" src="/Windchill/netmarkets/images/propagate_2.png" tabindex="8" style="margin-right: 33px;" disabled>
						<% } else {%>
							<img id="searchButton" title="Run Job" onclick="accessPermissionDeny()" src="/Windchill/netmarkets/images/propagate_2.png" tabindex="8" style="margin-right: 33px;" disabled>
							<% } %>	
					</td>
				</tr>
				
				<tr>
					<td  width="17%" class="TBLD"><%="Datawarehouse Export"%></td>
					<td  align="left"  class="TBLD" width="15%">  </td>
					<td  align="left"  class="TBLD" width="15%">  </td>
					<td  align="left"  class="TBLD" width="15%">  </td>
					<td class="TBLD" valign=middle width="15%" ></td>
					<td class="TBLD" valign=middle width="15"  ></td>
					
					<td class="TBLD" valign=middle width="10%" >
						<% if(adminGroup.equals("ALL") || lcsContext.inGroup(adminGroup.toUpperCase() )){ %>
							<img id="searchButton" title="Run Job" onclick="dwStartQueue()" src="/Windchill/netmarkets/images/shaded.gif" tabindex="8" style="margin-right: 33px;" disabled>
						<% } else {%>
							<img id="searchButton" title="Run Job" onclick="accessPermissionDeny()" src="/Windchill/netmarkets/images/shaded.gif" tabindex="8" style="margin-right: 33px;" disabled>
							<% } %>	
					</td>
				</tr>
				
								<tr>
					<td  width="17%" class="TBLD"><%="Colorway Thumbnail Export"%></td>
					<td  align="left"  class="TBLD" width="15%">  </td>
					<td  align="left"  class="TBLD" width="15%">  </td>
					<td  align="left"  class="TBLD" width="15%">  </td>
					<td class="TBLD" valign=middle width="15%" ></td>
					<td class="TBLD" valign=middle width="15"  ></td>
					
					<td class="TBLD" valign=middle width="10%" >
						<% if(adminGroup.equals("ALL") || lcsContext.inGroup(adminGroup.toUpperCase() )){ %>
							<img id="searchButton" title="Run Job" onclick="colorwayThumnailExportStartQueue()" src="/Windchill/netmarkets/images/change_design_review.png" tabindex="8" style="margin-right: 33px;" disabled>
						<% } else {%>
							<img id="searchButton" title="Run Job" onclick="accessPermissionDeny()" src="/Windchill/netmarkets/images/change_design_review.png" tabindex="8" style="margin-right: 33px;" disabled>
							<% } %>	
					</td>
				</tr>
				</tbody>
			</table>
		<td>
		<%= tg.endContentTable() %>
        <%= tg.endTable() %>
        <%= tg.endBorder() %>
		</td>
	</tr>
</table>
