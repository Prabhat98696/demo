
<%-- //////////////////////////////// JSP HEADERS ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%@page language="java"
       import="com.lcs.wc.classification.ClassificationTreeLoader,
                com.lcs.wc.client.Activities,
                com.lcs.wc.client.web.*,
                com.lcs.wc.util.*,
                java.util.*,
				wt.util.*,
				com.lcs.wc.season.LCSSeason,
				com.lcs.wc.foundation.LCSQuery,
                com.lcs.wc.flextype.*"
%>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- //////////////////////////////// BEAN INITIALIZATIONS ///////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<jsp:useBean id="fg" scope="request" class="com.lcs.wc.client.web.FormGenerator" />
<jsp:useBean id="tg" scope="request" class="com.lcs.wc.client.web.TableGenerator" />
<jsp:useBean id="flexg"	scope="request"	class="com.lcs.wc.client.web.FlexTypeGenerator"	/>
<jsp:useBean id="lcsContext" class="com.lcs.wc.client.ClientContext" scope="session"/>
<%
    flexg.setCreate(true);
%>
<%!
	public static final String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
	public static final String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset","UTF-8");
%>

<%
	String createNewDocPgHead = "Load Data";
	String primaryContentGrpTle ="Load Data";
	String fileLabel = "File";
	String channelLabel = "*Channel";
	String URL_CONTEXT = LCSProperties.get("flexPLM.urlContext.override");
	String subURLFolder = LCSProperties.get("flexPLM.windchill.subURLFolderLocation");

	String returnActivity = request.getParameter("returnActivity");
   if (!FormatHelper.hasContent(returnActivity)) {
      returnActivity = "";
   }
    String returnOid = request.getParameter("returnOid");

   if (!FormatHelper.hasContent(returnOid)) {
      returnOid = "";
   }

 String message = (String)request.getAttribute("uploadMessage");
   if(message!= null){
   out.write("<script>");
   out.write("alert(\"" + message + "\")");
   out.write("</script>");
   }
%>

<script language="javascript">

function loadData()
{
	
	document.MAINFORM.activity.value="LOAD_EXCEL_DATA"; //This is passed to custom.activityControllerMappings.properties
	document.MAINFORM.action.value="LOAD";

	if(validate()) 
	{
		submitForm();
		alert("Your request is being processed.");
	}
	
}

function respondToClick(inputCheckbox, input){
	if(inputCheckbox.checked){
			ignoreBlankVal=true;
			input.value = "true";
		}else if(!inputCheckbox.checked){
			ignoreBlankVal=false;
			input.value = "false";
		}

}
function validate() 
{

	var fileName = document.MAINFORM.loadDataFile.value;
	var fileExt = fileName.substr(fileName.lastIndexOf('.')+1);
	var dataLoader = document.MAINFORM.LoaderProgramme.value;
	
	var select = document.getElementById('LoaderProgramme');
	var opt = select.options[select.selectedIndex];
	
	if(dataLoader == "null" || dataLoader == ""){
	alert("Please select any Data Loader");
	return false;
	}

	  if (fileName == "null" || fileName == "") 
      {
			alert("No Load File Selected");
			document.MAINFORM.loadDataFile.focus();
			return false;
	  }
	  
	 else if("xlsx" != fileExt && "xls" != fileExt) 
	  {
			alert("File must be a .xlsx extension only");
			document.MAINFORM.loadDataFile.focus();
			return false;
	  }
	  	
	
	  else
	  {
			return true;
	  }
}

</script>

<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<%-- /////////////////////////////////////// HTML ////////////////////////////////////////--%>
<%-- /////////////////////////////////////////////////////////////////////////////////////--%>
<title> Choose an excel file to load data </title>

<link href="<%=URL_CONTEXT%>/css/boxes.css" rel="stylesheet">
<link href="<%=URL_CONTEXT%>/css/lang/cssButtonLocale.css" rel="stylesheet">
<body>

<form NAME="MAINFORM" ENCTYPE="multipart/form-data" METHOD="POST" >
<table width="100%">
    <tr>
        <td class="PAGEHEADING">
            <table width="100%">
                <tr>
                    <td class="PAGEHEADINGTITLE">
                        Data Loader
                    </td>
                    <td class='button' class="PAGEHEADINGTEXT" align="right">
                        <a class='button' href="javascript:loadData()">Load</a>&nbsp;&nbsp;|&nbsp;&nbsp;
                        <a class='button' href="javascript:backCancel()">Cancel</a>
                    </td>
               </tr>
           </table>
       </td>
    </tr>
 <tr>
      <td>
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle() %>Guidelines<%= tg.endTitle() %>
         <%= tg.startGroupContentTable() %>
         <col width="15%"></col><col width="35%"></col>
         <col width="15%"></col><col width="35%"></col>
		<p style="font-size:12px;">1. Data loader templates are available at location Retail Document\Data Loader.</br>
		 2. Download the template and enter the data as per the format specified in the excel template. DO NOT make any changes in the column headers. Do NOT add any extra columns. Use the exact format as given in the template. Column order must be same as template. Save the template once the data entry is done.</br>
		 3. Navigate to Data Loader-->Excel Uploader from side navigation. To load the finished data set template, select correct data set option from the dropdownand choose the file.</br>
		 4. Once the data set is loaded, confirmation email will be sent with the data loader summary. Success and failure records are updated in the csv file.</br>
		 5. In the data loader template date format should be "MM/dd/yyyy".</br>
		 6. Error message will be displayed if wrong data template is selected.</p>
       
<tr>
<td>
&nbsp;&nbsp;&nbsp;
</td>
<td>
&nbsp;&nbsp;&nbsp;
</td>

</tr>

<col width="10%"></col><col width="60%"></col>
<col width="10%"></col><col width="60%"></col>

</tr>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
      </td>
    </tr>
	
	<!--////////////////// -->
    <tr>
      <td>
         <%= tg.startGroupBorder() %>
         <%= tg.startTable() %>
         <%= tg.startGroupTitle() %>Load Excel File<%= tg.endTitle() %>
         <%= tg.startGroupContentTable() %>
         <col width="15%"></col><col width="35%"></col>
         <col width="15%"></col><col width="35%"></col>
         <tr>
                 <td class="FORMLABEL" nowrap valign=middle>
                  &nbsp;&nbsp;<%= "Data Set" %></td>
                <td class="FORMLABEL" nowrap valign=middle>
                <select id = "LoaderProgramme" name = "LoaderProgramme"> 
	<%if(lcsContext.isVendor){%>
	
	<option value=""></option>
	<option value="DHLTracking">DHL Tracking</option>
	<%}else{%>
	<option value=""></option>
	<option value="SKUSeasonConsolidatedBuyDate">Consolidated Buy Date</option>
	<option value="PRODAdidasModel">Adidas Model ProductType</option>
	<option value="SKUAdidasArticle">Adidas Article</option>
	<option value="ProductDimension">Product Dimension</option>
	<option value="ForecastData">Forecast Data</option>
	<option value="ClearanceSellPrice">Clearance Sell Price</option>
	<option value="StraubAttribute">Straub Photography Attributes</option>
	<option value="MinimumOrderQty">Minimum Order Qty</option>
	<option value="DHLTracking">DHL Tracking</option>
	<option value="SustainabilityAttribute">Sustainability Attributes</option>
	<option value="ColorwayStatusAttribute">Colorway Status</option>
	<option value="ReassignArticleNumbers">Reassign Article Numbers</option>
	<option value="AgronFOBLoader">FOB Loader</option>	
	<%}%>
	</select>
      </td> 
		<td class="FORMLABEL" nowrap colspan="2">
		<input id="ignoreBlankValue" name="ignoreBlankValue"  value="true" type=checkbox onClick="respondToClick(this, document.MAINFORM.ignoreBlankValue)" checked><%="Ignore blank cell"%></td>
		
         </tr>
<tr>
<td>
&nbsp;&nbsp;&nbsp;
</td>
<td>
&nbsp;&nbsp;&nbsp;
</td>

</tr>

<col width="10%"></col><col width="60%"></col>
<col width="10%"></col><col width="60%"></col>
<tr>
<td class="FORMLABEL" nowrap valign=middle>
&nbsp;&nbsp;<%= fileLabel %>&nbsp;
</td>
<td class="FORMELEMENT" nowrap valign=middle>
<input size = "50" name="loadDataFile" type="FILE" >
</td>
</tr>
         <%= tg.endContentTable() %>
         <%= tg.endTable() %>
         <%= tg.endBorder() %>
      </td>
    </tr>


</table>
</form>
