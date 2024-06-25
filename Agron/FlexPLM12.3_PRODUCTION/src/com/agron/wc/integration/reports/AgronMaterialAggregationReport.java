package com.agron.wc.integration.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.agron.wc.integration.util.AgronIntegrationUtil;
import com.agron.wc.util.AgronSendMail;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.document.LCSDocumentClientModel;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.document.LCSDocumentQuery;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.queue.ProcessingQueue;
import wt.queue.QueueEntry;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;


public class AgronMaterialAggregationReport implements RemoteAccess  {
	private static final String FLEX_TIME_FORMAT = "MM-dd-yyyy:hh:mm:ss a";
	private static Collection tableColumns=new ArrayList();
	private static String REPORT_COLUMNS=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.columns","PRODUCTTYPE:TYPE,SEASON,PRODUCT,WORK#,SKU,ARTICLE#,SOURCE,FACTORY,BOM,section,partName,agrMaterialRefNumber,materialDescription,supplierDescription,agrSupplierRefNumber,colorDescription,msUnitOfMeasure,materialPrice,quantity,agrForecastedQtySKU:Forecasted Qy,ConsolidatedQty:Consolidated Qty,agrMOQGreige:MOQ - Greige");
	private static final Logger LOGGER = LogManager.getLogger(AgronMaterialAggregationReport.class);
	private static final String USERNAME = LCSProperties.get("com.agron.wc.integration.username", "wcadmin");
	private static final String PASSWORD = LCSProperties.get("com.agron.wc.integration.password", "wcadmindev"); 
    private final static String BOM_REPORT_QUEUE_NAME =LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.queue.name", "AgronMaterialAggregationReportQueue");
	private static  String REPORT_ID_DATE_FORMAT=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.fileNameDateFormat","yyyyMMddHHmmss");
	
	private static String MATERIAL_WHEREUSED_REPORTS_FLEXTYPE_PATH=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.documentFlexTypeIdPath","Document\\agronReports\\agrMaterialAggregationReport");
	
	final static String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset", "UTF-8");
	
	final static String REPORT_NAME=LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.reportnName", "Material Aggregation Report");;
	
	private static String DOUCMENT_NAME_KEY=LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.documentNameKey", "ptcdocumentName");;
	
	private static String DOUCMENT_NUMERIC_ID_KEY=LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.documentIdKey", "agrReportId");;
	private static  String CSV_REPORT_FILE = "";
	private static String FILERPATH = LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.FILERPATH");
	private static String NUMERIC_COLUMNS_KEYS=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.numericColumns","WORK#,materialPrice,quantity,agrForecastedQtySKU,ConsolidatedQty");;
	
	//EMAIL Config
	
	private static final String MAIL_HEADER = LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.MailHeader","MATERIAL AGGREGATION REPORT");
    private static String MAIL_SUBJECT = LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.mailTo.Subject","PTC MATERIAL AGGREGATION REPORT");
	private static final String MAIL_TO_IDS = LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.ToMailIds");
	private static final String MAIL_FROM_ID = LCSProperties.get("com.agron.wc.integration.FromMailId");

	private static String SMTP_HOST = LCSProperties.get("com.agron.wc.integration.SMTPHost");
	 static boolean enableEmail = LCSProperties.getBoolean("com.agron.wc.integration.reports.AgronMaterialAggregationReport.Enable.Emails",false);
	
	 static int DELETE_REPORTS_BEFORE_DAYS = LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.deleteReportsBeforeDays",15);
	 static boolean DELETE_OLD_REPORT_DOCS= LCSProperties.getBoolean("com.agron.wc.integration.reports.AgronMaterialAggregationReport.deleteOldReportDocuments.enable",true);
	 static {
		try {
			String tempDownloadLocation = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.client.web.CSVGenerator.exportLocation"));
			String wt_home = WTProperties.getLocalProperties().getProperty("wt.home");
			CSV_REPORT_FILE = (new StringBuilder()).append(wt_home).append(tempDownloadLocation).toString();
			LOGGER.info("CSV_REPORT_FILE Download Path>>>>"+CSV_REPORT_FILE);
			configureColumnList();
		}catch(Exception e) {
			LOGGER.error("Error in configuring Matterial Aggregation Report  ",e);
		}
	
	}
	
	public static void configureColumnList() {
		LOGGER.info("configureColumnList>>>>>");
	
		List<String> integerColumns=new ArrayList(Arrays.asList(NUMERIC_COLUMNS_KEYS.toUpperCase().trim().split(",")));
		
		List<String> columns=new ArrayList(Arrays.asList(REPORT_COLUMNS.toUpperCase().trim().split(",")));
		LOGGER.info("columns Array>>>>>"+columns);
		columns.forEach(column->{
			String tableColumArray[]=column.split(":");
			String columnKey=tableColumArray[0].trim().toUpperCase();
			String columnDispalyValue=columnKey;
			if(tableColumArray.length>1 && FormatHelper.hasContent(tableColumArray[1]) ){
				columnDispalyValue=tableColumArray[1];
			}
			TableColumn tColumn=new TableColumn(columnKey,columnDispalyValue);
			if(columnKey.toUpperCase().indexOf("THUMBNAIL")>-1) {
				tColumn.setImage(true);
			}
           if(integerColumns.contains(columnKey)) {
        	   tColumn.setAttributeType("NUMERIC");
			}else {
				 tColumn.setAttributeType("STRING");
			}
			tableColumns.add(tColumn);

		});	
	}
	
	public static void main(String args[]) {
		System.out.println("Agron Material Aggregation Report START");
		try {
			if(args !=null && args.length>0) {
				executeRMI(args[0]);
			}else {
				executeRMI("addReportJobQueueEntry");	
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Agron Material Aggregation Report Completed");

	}
		
     public static void executeRMI(String method) {
			System.out.println("RMI Execution START>>method::"+method);
		    RemoteMethodServer rmsObj = RemoteMethodServer.getDefault();
			rmsObj.setUserName(USERNAME);
			rmsObj.setPassword(PASSWORD);
			Class<?> cs[] = new Class[0];
			Object args[] = new Object[0];
			try {
				rmsObj.invoke(method, "com.agron.wc.integration.reports.AgronMaterialAggregationReport", null, cs, args);
			} catch (RemoteException e) {
				e.printStackTrace();
				System.out.println("RMI Execution RemoteException");
			} catch (InvocationTargetException e) {
				System.out.println("RMI Execution InvocationTargetException");
				e.printStackTrace();
			}
			System.out.println("RMI Execution COMPLETED");
		}
	 
	
     public static void addReportJobQueueEntry() {
    	 LOGGER.info("Add report job to queue>>>"+BOM_REPORT_QUEUE_NAME);
    	 AgronReportLogEntry logEntry=null;
    	 try {
    		 logEntry = new AgronReportLogEntry();
    		 logEntry.setQueueName(BOM_REPORT_QUEUE_NAME);
    		 logEntry.setStatus("WAIT_IN_QUEUE");
    		 logEntry = logEntry.save();
    		 String logEntryId=logEntry.getFlexObjectId();
    		 LOGGER.info("logEntryId>>>>>>>>>>"+logEntryId+"::"+logEntry);
    		 QueueEntry queueEntry=addQueueEntry(BOM_REPORT_QUEUE_NAME, "generateReport","com.agron.wc.integration.reports.AgronMaterialAggregationReport",logEntryId);
    		 LOGGER.info("Report  job added to queue::");
    	 } catch (Exception e) {
    		 LOGGER.error("Error while adding job to queue",e);
    		 if(logEntry !=null) {
    			 try {
    				 logEntry.setStatus("FAILED");
    				 logEntry = logEntry.save();	
    			 }catch(Exception ex) {
    				 LOGGER.info("Error while saving logentry :"+e.getMessage(),ex);
    			 }
    		 }
    		 sendEmail("FAILED","Error:"+e.getMessage(),"");
    		 e.printStackTrace();
    	 }

     }
		
		
		/**
		 * This method is used to add the batch process to the queue.
		 * 
		 * @param queueName is String object
		 * @param method    is String object
		 * @param className is String object
		 * @throws WTException 
		 */
		public static QueueEntry addQueueEntry(String queueName, String method, String className,String logEntryId) throws WTException {
			
				ProcessingQueue processQueue = AgronIntegrationUtil.getProcessQueue(queueName);
				wt.org.WTPrincipal user = SessionHelper.manager.getPrincipal();
				Class<?> argTypes[] = {String.class};
				Object args[] = {logEntryId};	
				QueueEntry queueEntry=processQueue.addEntry(user, method, className, argTypes, args);
		
			return queueEntry;
		}
		
		
		public static Collection getReportData() throws Exception {
			LOGGER.info("getReportData START>>>>>>>>>>>>>>>>");
			Instant start= Instant.now();
			FlexType productflexType = FlexTypeCache.getFlexTypeFromPath("Product");
			Collection<LCSProduct> products=getProductsByFlexType(productflexType);
			LOGGER.info("PRODUCTS FOUND::"+products.size());
			Collection<FlexBOMPart>  boms=AgronBOMUtil.findAllBoms(products);
			LOGGER.info("PRODUCT BOM FOUND::"+boms.size() +"::FOR PRODUCTS>>>"+products.size());
			Collection bomLinkRows=AgronBOMUtil.getAllBomLinkRows(boms);
			LOGGER.info("TOTAL BOMLINK ROWS>>>"+bomLinkRows.size()+"::Time Taken>>"+Duration.between(start, Instant.now()).toMinutes()+" minutes");
			LOGGER.info("getReportData END>>>>>>>>>>>>>>>>");
			return bomLinkRows;
		}
		public static void generateReport() throws WTException {
			generateReport(null);
		}
		public static void deleteDocsBeforeDays() throws WTException 
		{
			 deleteDocsBeforeDays(DELETE_REPORTS_BEFORE_DAYS);	
		}
		public static void generateReport(String logId) throws WTException   {
			//delete the old report documents 
			 LOGGER.info("DELETE_OLD_REPORT_DOCS>>>"+DELETE_OLD_REPORT_DOCS+":::DAYS>>"+DELETE_REPORTS_BEFORE_DAYS);
			 if(DELETE_OLD_REPORT_DOCS) {
				 deleteDocsBeforeDays(DELETE_REPORTS_BEFORE_DAYS); 
			 }
			 Instant start= Instant.now();
			 AgronReportLogEntry logEntry=AgronReportLogEntry.load(logId);
			 String message="";
			 boolean success=false;
			 int totalRecords=0;
			 String documentName="";
			 LOGGER.info("****************************************************************");
			 LOGGER.info("GENERATE REPORT START*****************************************");
			 LOGGER.info("****************************************************************");
			  try {
					 logEntry.setStatus("Work in Process");
					 logEntry.save();
					 String jobId= uniqueDocName();
					 String fileName= jobId+".xlsx";
					 String  filePath=CSV_REPORT_FILE;
					 if(CSV_REPORT_FILE.endsWith(File.separator)) {
						 filePath=filePath+fileName;
					 }else {
						 filePath=filePath+File.separator+fileName;
					 }
					 Collection records=getReportData();
					 totalRecords=records.size();
					 String comments="Records: "+totalRecords;
					 message=comments;
					 filePath=AgronExcelReportGenerator.generateExcel(filePath.trim(),records,tableColumns);
					 File file =new File(filePath);
					 LOGGER.info("file>>>>"+file);
					 
					 if(file!=null && file.exists() && file.isFile() && file.canRead()) {
						 						 
					// COPY FILE TO SHARED LOCATION START
						 InputStream sourceFile = new FileInputStream(file);
						 Path dest = Paths.get(FILERPATH);
						 Files.copy(sourceFile, dest,StandardCopyOption.REPLACE_EXISTING);
						 LOGGER.info("COPYING FILE TO >>>>"+dest);
						 
						 // COPY FILE TO SHARED LOCATION END
						 LCSDocument  document=createNewReportDocument(jobId);
						 if(document !=null) {
							 documentName=(String)document.getValue(DOUCMENT_NAME_KEY);
							 logEntry.setDocumentName(documentName);
							 logEntry.setdocumentNumericId(FormatHelper.getNumericVersionIdFromObject(document));
							 success=attachReportToDocument(document,filePath,comments);
						 }else {
								message="Error: Error while creating document";	 
						 }
					 }else {
						LOGGER.error("BAD File>>>>"+file);
						message="Error: Bad File!File is not readable:"+file;
					}
			  }catch(Exception e) {
				  e.printStackTrace();
					LOGGER.error("Exception >>>>"+e.getMessage(),e);
				  message="Error: "+e.getMessage();
				  
			  }finally{
				  String status="FAILED";
				  if(success) {
					  status="SUCCESS"; 
				  } 
				  logEntry.setMessage(message);
				  logEntry.setStatus(status);
				  try {
					  logEntry.save();
				  }catch(Exception e) {
					  LOGGER.error("Exception while saving logentry>>>>"+e.getMessage(),e);  
				  }
				  sendEmail(status,message,documentName);
			  }
			
			 LOGGER.info("****************************************************************");
			 LOGGER.info("GENERATE REPORT COMPLETED ::"+Duration.between(start, Instant.now()).toMinutes()+" Minutes**************");
			 LOGGER.info("****************************************************************");
		}
		
	
		
	public static Collection getProductsByFlexType(FlexType productflexType) throws Exception {
		Map<String, String> map=new HashMap<String, String>();
		map.put("name", "*");
		map.put("skipRanges", "true");
		if(productflexType==null) {
		 productflexType = FlexTypeCache.getFlexTypeFromPath("Product");
		}
		LOGGER.info("getAllProductsByFlexTpe START>>"+productflexType.getTypeDisplayName());
		LCSProductQuery produQuery=new LCSProductQuery();
		SearchResults prodResults = produQuery.findProductsByCriteria(map, productflexType, null, null, null);
		Collection<LCSProduct> allProducts= LCSProductQuery.getObjectsFromResults(prodResults.getResults(), "VR:com.lcs.wc.product.LCSProduct:", "LCSPRODUCT.BRANCHIDITERATIONINFO");
		LOGGER.info("PRODUCTS FOUND>>>"+productflexType.getTypeDisplayName()+"::"+allProducts.size());

		return allProducts;
	}
	
	
	public static boolean  attachReportToDocument(LCSDocument document, String filePath,String comments)  throws WTException, WTPropertyVetoException, UnsupportedEncodingException {
		LOGGER.info("attachReportToDocument START");
		boolean success=false;
		if(document !=null) {
				document = (LCSDocument)VersionHelper.getVersion((LCSDocument) VersionHelper.latestIterationOf(document.getMaster()), "A");
				LOGGER.info("document>>>>>"+document);
				if(!VersionHelper.isCheckedOutByUser(document)) {
					LOGGER.info("document checkout>>>>>");
					document=VersionHelper.checkout(document);	
					LOGGER.info("document checkedout >>>>>"+document);
				}
				filePath= URLDecoder.decode(filePath, defaultCharsetEncoding);
				LOGGER.info("encoded filePath>>>>>"+filePath);
				LCSDocumentClientModel documentModel=new LCSDocumentClientModel();
				documentModel.load(FormatHelper.getObjectId(document));
				documentModel.setContentFile(filePath);
				String[] contentFiles = {};
				String[] contentComments = {};
				documentModel.setSecondaryContentFiles(contentFiles);
				documentModel.setSecondaryContentComments(contentComments);
				documentModel.setContentComment(comments);
				documentModel.save();
				LOGGER.info("DOCUMENT SAVED >>>>>");
				VersionHelper.checkin(document);
				success=true;
				LOGGER.info("DOCUMENT CHECKIN >>>>>");
			}
		LOGGER.info("attachReportToDocument END");
		return success;
	}
	
	
	public static LCSDocument createNewReportDocument(String uniqueName) throws WTPropertyVetoException, WTException {
		LOGGER.info("createNewReportDocument start>>>");
		FlexType docType = FlexTypeCache.getFlexTypeFromPath(MATERIAL_WHEREUSED_REPORTS_FLEXTYPE_PATH);
		LCSDocument document=LCSDocument.newLCSDocument();
		document.setFlexType(docType);
		document.setName(uniqueName);	
		document.setValue(DOUCMENT_NAME_KEY, uniqueName);
		document= new LCSDocumentLogic().save(document,true);
		LOGGER.info("Document Created>>>"+uniqueName+"::"+document);
		document = (LCSDocument)VersionHelper.getVersion((LCSDocument) VersionHelper.latestIterationOf(document.getMaster()), "A");
		String name=REPORT_NAME+"_"+String.valueOf(document.getValue(DOUCMENT_NUMERIC_ID_KEY));
		document.setValue(DOUCMENT_NAME_KEY, name);
		//document.setName(name);	
		new LCSDocumentLogic().save(document);
		LOGGER.info("Document Name Updated>>>"+name+"::"+document);
	
		return document;
	}
	
	public static String uniqueDocName() {
		SimpleDateFormat sdfDestination = new SimpleDateFormat(REPORT_ID_DATE_FORMAT);
		String documentName= REPORT_NAME.trim()+"_"+sdfDestination.format(new Date());
		return documentName;
	}
    
    /**
     * This method is used to send the mail notification to the given mail id's.
     */
    public static void sendEmail(String status, String message,String documentName)
     
    {
    	LOGGER.info("STATUS>>>>>"+status+"::documentName>>"+documentName+"::Message>>"+message);
    	 
    	if(enableEmail) {
    		try
    		{   
    			LOGGER.info("sendEmail START>>>>>>>>>");
    			StringBuffer sb = new StringBuffer();
    			sb.append("Dear PLM User, ");
    			sb.append(System.getProperty("line.separator"));
    			sb.append(MAIL_HEADER);
    			sb.append(System.getProperty("line.separator"));
    			sb.append("------------------------------------------------------");
    			sb.append(System.getProperty("line.separator"));
    			sb.append("Status: "+status);
    			sb.append(System.getProperty("line.separator"));
    			if(FormatHelper.hasContent(message)) {
    				sb.append(message);
    				sb.append(System.getProperty("line.separator"));
    			}
    			if(FormatHelper.hasContent(documentName)) {
    				sb.append("Reports are available at Library-> Document\\Reports\\Material Aggregation Report");
    				sb.append(System.getProperty("line.separator"));
    				sb.append("Document: "+documentName);
    				sb.append(System.getProperty("line.separator"));
    			}
    			sb.append("------------------------------------------------------");
    			sb.append(System.getProperty("line.separator"));
    			sb.append("Thanks,");
    			sb.append(System.getProperty("line.separator"));
    			sb.append("PLM Admin");
    			String emailContent  = sb.toString();
    			(new AgronSendMail()).send(MAIL_FROM_ID, MAIL_TO_IDS, SMTP_HOST, MAIL_SUBJECT, emailContent, "", "", "", "");
    			
    			LOGGER.info("sendEmail END>>>>>>>>>");
    			
    		}
    		catch(WTException e)
    		{
    			LOGGER.error((new StringBuilder()).append("Error in EMAIL#: ").append(e.getMessage()).toString(),e);
    		}
    	}
     	
    }
    
    public static void printAllLogEntries() {
    	AgronReportLogEntry.manageLogEntries("", false);
    }
    public static void deleteAllLogEntries() {
    	AgronReportLogEntry.manageLogEntries("", true);
    }
   
    public static void deleteWaitLogEntries() {
    	AgronReportLogEntry.manageLogEntries("WAIT_IN_QUEUE", true);
    }
    public static void deleteInProcessEntries() {
    	AgronReportLogEntry.manageLogEntries("Work in Process", true);
    }
 
    
    /**
     * method to delete Material Aggregation Report documents created before days (provided in parameter)
     * @param int deleteBeforeDay
     */
    public static void deleteDocsBeforeDays(int deleteBeforeDay){
    	LOGGER.info("START deleteDocsBeforeDays::"+deleteBeforeDay);
    	try {
    	  	FlexType docType = FlexTypeCache.getFlexTypeFromPath(MATERIAL_WHEREUSED_REPORTS_FLEXTYPE_PATH);
    	  	if(docType !=null && deleteBeforeDay>0) {
    	  		Date beforeDate = DateUtils.addDays(new Date(), -deleteBeforeDay);
    	  		LOGGER.info("docType >>"+docType.getTypeDisplayName()+":: Delete beforeDate>>"+beforeDate);
    	  		Map<String, String> map=new HashMap<String, String>();
    	  		map.put("name", "*");
    	  		map.put("skipRanges", "true");
    	  		LCSDocumentQuery query=new LCSDocumentQuery();
    	  		SearchResults results = query.findByCriteria(map, docType, null, null, null);
    	  		Collection<LCSDocument> documents= LCSDocumentQuery.getObjectsFromResults(results.getResults(), "VR:com.lcs.wc.document.LCSDocument:", "LCSDocument.BRANCHIDITERATIONINFO");
    	  		LOGGER.info("documents size>>>"+documents.size());
    	  		documents.forEach(document->{
    	  			if(document !=null) {
    	  				try {
    	  					document = (LCSDocument)VersionHelper.getVersion((LCSDocument) VersionHelper.latestIterationOf(document.getMaster()), "A");
    	  					if(document.getCreateTimestamp().compareTo(beforeDate)<0) {
    	  						if(!VersionHelper.isCheckedOutByUser(document)) {
    	  							LOGGER.info("DELETE DOC>>>>"+document+"::"+document.getValue(DOUCMENT_NAME_KEY)+"::CREATED>>"+document.getCreateTimestamp());
    	  							LCSDocumentLogic.deleteObject(document);
    	  						}
    	  					}

    	  				} catch (WTException e) {
    	  					LOGGER.error("Exception while deleting document:"+document, e);
    	  				}


    	  			}
    	  		});
    	  	}	
    	}catch(Exception e) {
    		LOGGER.error("Exception while deleteDocsBeforeDays:", e);
    	}
  
    	LOGGER.info("END deleteDocsBeforeDays");
    }

    
}
