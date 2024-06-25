package com.agron.wc.integration.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agron.wc.integration.util.AgronCSVGenerator;
import com.agron.wc.integration.util.AgronIntegrationUtil;
import com.agron.wc.sftpImageExport.AgronSFTPServerConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.infoengine.util.FileUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.CSVGenerator;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.country.LCSCountry;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.ReferencedTypeKeys;
import com.lcs.wc.report.ColumnList;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.query.LSQModule;
import com.lcs.wc.season.query.LineSheetQuery;
import com.lcs.wc.season.query.LineSheetQueryOptions;
import com.lcs.wc.sizing.ProductSizeCategory;
import com.lcs.wc.sourcing.LCSCostSheetMaster;
import com.lcs.wc.sourcing.LCSCostSheetQuery;
import com.lcs.wc.sourcing.LCSProductCostSheet;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FlexObjectSorter;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.queue.ProcessingQueue;
import wt.queue.QueueEntry;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;


public class AgronLineSheetDataExtractor implements RemoteAccess  {
	

	//windchill com.agron.wc.integration.reports.AgronLineSheetDataExtractor
	private static final String FLEX_TIME_FORMAT = "MM-dd-yyyy:hh:mm:ss a";
	
	private static String REPORT_COLUMNS=LCSProperties.get("com.agron.wc.integration.reports.AgronLineSheetDataExtractor.columns","LCSSOURCINGCONFIG.PTC_STR_1TYPEINFOLCSSOURCING:Sourcing Config Name,T0764061.PTC_STR_1TYPEINFOLCSSUPPLIER,LCSPRODUCT.PTC_STR_1TYPEINFOLCSPRODUCT:Product Name,LCSSKU.PTC_STR_1TYPEINFOLCSSKU:Colorway Name,LCSPRODUCTCOSTSHEET.PTC_STR_1TYPEINFOLCSPRODUCTC:CostSheet Name,LCSPRODUCTSEASONLINK.PRODSTATE:Lifecyle State");
	private static final Logger LOGGER = LogManager.getLogger(AgronLineSheetDataExtractor.class);
	private static final String USERNAME = LCSProperties.get("com.agron.wc.integration.username", "wcadmin");
	private static final String PASSWORD = LCSProperties.get("com.agron.wc.integration.password", "wcadmindev"); 
    private final static String LS_DE_QUEUE_NAME =LCSProperties.get("com.agron.wc.integration.reports.AgronLineSheetDataExtractor.queue.name", "AgronMaterialAggregationReportQueue");
    private static final String REPORT_VIEW = LCSProperties.get("com.agron.wc.integration.reports.AgronLineSheetDataExtractor.queue.name","VIBE IQ ADMIN");
    private final static String LS_DE_METHOD_NAME ="addLSExtractQueueEntry";
	
    private static  String REPORT_ID_DATE_FORMAT=LCSProperties.get("com.agron.wc.integration.reports.AgronLineSheetDataExtractor.fileNameDateFormat","MMddyyyy");
		
	final static String defaultCharsetEncoding = LCSProperties.get("com.lcs.wc.util.CharsetFilter.Charset", "UTF-8");
	
	final static String REPORT_NAME=LCSProperties.get("com.agron.wc.reports.AgronLineSheetDataExtractor.reportnName", "AgronLineSheetDataExtract");;
	static String remoteDir =LCSProperties.get("com.agron.wc.integration.reports.AgronLineSheetDataExtractor.sftpRemote.dir");
	private static Map<String, String> listAtts=new HashMap<String, String>();
	
	private static  String CSV_REPORT_FILE = "";
	private static String FILERPATH = LCSProperties.get("com.agron.wc.reports.AgronMaterialAggregationReport.FILERPATH");
	private static String NUMERIC_COLUMNS_KEYS=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.numericColumns","LCSPRODUCTCOSTSHEET.PTC_DBL_12TYPEINFOLCSPRODUCT:DUTY");
	

	static SftpATTRS attrs=null;

	private static List<String> colorwayUIDList;
	private static String colorwayUIDAttColumn;
	private static final String AGR_COLORWAY_UNIQUE_ID = LCSProperties.get("com.agron.wc.reports.AgronLineSheetDataExtractor.attKey.agrColorwayUniqueID","agrColorwayUniqueID");
    private static final String legacyColorwayUIDs = LCSProperties.get("com.agron.wc.reports.AgronLineSheetDataExtractor.data.colorwayUIDs","25660398,25660838,25660313,25662077,25662518,25661992,25662918,25663359,25662833,25661152,25661678,25661237");
	private static boolean excludeColorwayUIDs = LCSProperties.getBoolean("com.agron.wc.reports.AgronLineSheetDataExtractor.excludeColorwayUIDs", true);
	
		 static {
		try {
			String tempDownloadLocation = FormatHelper.formatOSFolderLocation(LCSProperties.get("com.lcs.wc.client.web.CSVGenerator.exportLocation"));
			String wt_home = WTProperties.getLocalProperties().getProperty("wt.home");
			CSV_REPORT_FILE = (new StringBuilder()).append(wt_home).append(tempDownloadLocation).toString();
			LOGGER.info("CSV_REPORT_FILE Download Path>>>>"+CSV_REPORT_FILE);
			
			
			configurecolorwayUIDList();
		}catch(Exception e) {
			LOGGER.error("Error in configuring Linesheet Extract  ",e);
		}
	
	}
	
	public static void configurecolorwayUIDList() {
		LOGGER.info("configurecolorwayUIDList>>>>>");
		colorwayUIDList = Arrays.asList(legacyColorwayUIDs.trim().split(","));
		LOGGER.info("colorwayUIDList >>>>"+colorwayUIDList);
		
		/*List<String> integerColumns=new ArrayList(Arrays.asList(NUMERIC_COLUMNS_KEYS.toUpperCase().trim().split(",")));
		
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
			
           if(integerColumns.contains(columnKey)) {
        	   tColumn.setAttributeType("NUMERIC");
			}else {
				 tColumn.setAttributeType("STRING");
			}
			//tableColumns.add(tColumn);

		});	*/
	}
	
	public static void main(String args[]) {
		LOGGER.info("Agron Linesheet Extract START");
		try {
			if(args !=null && args.length>0) {
				executeRMI(args[0]);
			}else {
				executeRMI(LS_DE_METHOD_NAME);	
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("Agron Linesheet Extract Completed");

	}
		
     public static void executeRMI(String method) {
    	 	LOGGER.info("RMI Execution START>>method::"+method);
		    RemoteMethodServer rmsObj = RemoteMethodServer.getDefault();
			rmsObj.setUserName(USERNAME);
			rmsObj.setPassword(PASSWORD);
			Class<?> cs[] = new Class[0];
			Object args[] = new Object[0];
			try {
				rmsObj.invoke(method, "com.agron.wc.integration.reports.AgronLineSheetDataExtractor", null, cs, args);
			} catch (RemoteException e) {
				e.printStackTrace();
				LOGGER.error("RMI Execution RemoteException");
			} catch (InvocationTargetException e) {
				LOGGER.error("RMI Execution InvocationTargetException");
				e.printStackTrace();
			}
			LOGGER.info("RMI Execution COMPLETED");
		}
	 
	
     public static void addLSExtractQueueEntry() {
    	 LOGGER.info("Add to queue>>>"+LS_DE_QUEUE_NAME);
    	 //AgronReportLogEntry logEntry=null;
    	 try {
				/*
				 * logEntry = new AgronReportLogEntry();
				 * logEntry.setQueueName(LS_DE_QUEUE_NAME); logEntry.setStatus("WAIT_IN_QUEUE");
				 * logEntry = logEntry.save(); String logEntryId=logEntry.getFlexObjectId();
				 * LOGGER.info("logEntryId>>>>>>>>>>"+logEntryId+"::"+logEntry);
				 */
    		 QueueEntry queueEntry=addQueueEntry(LS_DE_QUEUE_NAME, "extractLinesheetData","com.agron.wc.integration.reports.AgronLineSheetDataExtractor");
    		 LOGGER.info("Linesheet Extract job added to queue::");
    	 } catch (Exception e) {
    		 LOGGER.error("Error while adding job to queue",e.getMessage());
				/*
				 * if(logEntry !=null) { try { logEntry.setStatus("FAILED"); logEntry =
				 * logEntry.save(); }catch(Exception ex) {
				 * LOGGER.info("Error while saving logentry :"+e.getMessage(),ex); } }
				 */
    		// sendEmail("FAILED","Error:"+e.getMessage(),"");
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
		public static QueueEntry addQueueEntry(String queueName, String method, String className) throws WTException {
			
				ProcessingQueue processQueue = AgronIntegrationUtil.getProcessQueue(queueName);
				wt.org.WTPrincipal user = SessionHelper.manager.getPrincipal();
				Class<?> argTypes[] = {};
				Object args[] = {};	
				QueueEntry queueEntry=processQueue.addEntry(user, method, className, argTypes, args);
		
			return queueEntry;
		}
		
		
		public static Collection getReportData(LCSSeason season, Collection<String> attList) throws Exception {
			LOGGER.info("getReportData START>>>>>>>>>>>>>>>>");
			Instant start= Instant.now();
			
			
			
			Collection<FlexObject> tempData = new ArrayList();
			
			
			LOGGER.info("AttList  :"+attList.toString());
			//*******NEW LSQ*******
            LineSheetQuery lsqn = null;
            LineSheetQueryOptions options = new LineSheetQueryOptions();
            //*********************
            
            
            options.skus = true;
            options.costing = true;
            options.skuCosting = true;
            options.setIncludeSourcing(true);
            options.secondarySourcing = false;
            options.primaryCostOnly = false;
            options.whatifCosts = false;
            options.usedAttKeys = attList;
            options.includeRemoved = false;
            options.includePlaceHolders = false;
            options.includeCostSpec = options.costing;
            options.setCostSpec(null);
            options.statement = null;
            options.excludeInActiveSKUSource = false;
            options.includedProductsWithFilteredColorways = false;
            //*********************
            
            
            Injector injector = Guice.createInjector(new LSQModule());
            lsqn = injector.getInstance(com.lcs.wc.season.query.LineSheetQuery.class);
            
            
            options.setSeason(season);
    		//LOGGER.info("Linesheet Query Options to string >>>:"+options.toString());
    		//LOGGER.info("Linesheet Query >>>:"+lsqn.getLineSheetQuery(options));
    		Collection<FlexObject> results = lsqn.getLineSheetResults(options);
    		LOGGER.info("TOTAL Season ROWS>>>"+results.size()+"::Time Taken>>"+Duration.between(start, Instant.now()).toMinutes()+" minutes");
 
    		if(results!=null) {
				tempData = parseLinesheetData(results);
				LOGGER.info("TOTAL tempData ROWS>>>"+tempData.size()+"::Time Taken>>"+Duration.between(start, Instant.now()).toMinutes()+" minutes");
				//LOGGER.info(" tempData >>>"+tempData);
			}
			
			LOGGER.info("getReportData END>>>>>>>>>>>>>>>>");
			return tempData;
		}

		private static Collection<FlexObject> parseLinesheetData(Collection<FlexObject> allResults) {

			Collection<FlexObject> finalData = new Vector();
			
			for(FlexObject flexObj: allResults) {
				
				if(FormatHelper.hasContent(flexObj.getData("LCSProductCostSheet.branchIditerationInfo"))) {
					Long attData = flexObj.getLong("LCSProductCostSheet.branchIditerationInfo");
					//LOGGER.info("attData :"+attData);
					try {
						LCSProductCostSheet csObj = (LCSProductCostSheet) LCSQuery.findObjectById("VR:com.lcs.wc.sourcing.LCSProductCostSheet"+":"+attData);
						//String attRefValue = seasonObj.getName();
						csObj = VersionHelper.latestIterationOf(csObj.getMaster());
						LCSCostSheetMaster csm = (LCSCostSheetMaster) csObj.getMaster();
						ProductSizeCategory psc = LCSCostSheetQuery.getReferencedProductSizeCategory(csm);
						
						if(psc!=null ) {
							
							if (csObj.getApplicableSizes().contains(psc.getBaseSize())){
								//LOGGER.info("Base Size Costsheet >>"+csObj.getApplicableSizes());
							}else {
								continue;
							}
							
						}
						
					} catch (WTException e) {
						e.printStackTrace();
					}
					
				}
				
				if(FormatHelper.hasContent(flexObj.getData(colorwayUIDAttColumn))) {
					long attData = flexObj.getLong(colorwayUIDAttColumn);
					String attDataStr = Long.toString(attData);
					if(colorwayUIDList.contains(attDataStr)) {
						LOGGER.info("Colorway Excluded >> :"+attDataStr);
						continue;
					}
					
					
				}
				
				Iterator listAttsItr = listAtts.keySet().iterator();
				while(listAttsItr.hasNext()) {
					String attColumnKey = (String) listAttsItr.next();
					String attColArray[]=attColumnKey .split(":"); 
					String attType = attColArray[0].trim();
					String attColumn = attColArray[1].trim();
					//LOGGER.info("attType :"+attType);
					//LOGGER.info("attColumn :"+attColumn);
					if((attType.equalsIgnoreCase("choice") || attType.equalsIgnoreCase("driven") )&& FormatHelper.hasContent(flexObj.getString(attColumn))) {
						String attData = flexObj.getString(attColumn);
						//LOGGER.info("attData :"+attData);
						String attListValue = getAttListValue(attData, attColumnKey);
						flexObj.replace(attColumn.toUpperCase(), attData, attListValue);
						
					}else if (attType.equalsIgnoreCase("boolean") && FormatHelper.hasContentAllowZero(flexObj.getData(attColumn)) ){
						String attData = "" + flexObj.getData(attColumn);
						String attBooleanValue ;
						if("1".equals(attData) || "true".equals(attData)){
							attBooleanValue = "Yes";
						}else{
							attBooleanValue = "No";
						}
						//LOGGER.info("attBooleanValue :"+attBooleanValue);
						flexObj.replace(attColumn.toUpperCase(), attData, attBooleanValue);
					}else if(attType.equalsIgnoreCase("moaList") && FormatHelper.hasContent(flexObj.getString(attColumn))) {
						String attData = flexObj.getString(attColumn);
						
						String attListValue = getMOAListValue(attData, attColumnKey);
						//attListValue = "\""+attListValue+"\"";
						//LOGGER.info("attListValue :"+attListValue);
						flexObj.replace(attColumn.toUpperCase(), attData, attListValue);
						
					} else if(attType.equalsIgnoreCase("object_ref") && FormatHelper.hasContent(flexObj.getData(attColumn)) && flexObj.getData(attColumn)!=null) {
						String attData = flexObj.getString(attColumn);
						//LOGGER.info("attData :"+attData);
						//LOGGER.info("attColumn :"+attColumn);
						String attRefValue = getObjRefValue(attData, attColumnKey);
						//LOGGER.info("attListValue :"+attListValue);
						flexObj.replace(attColumn.toUpperCase(), attData, attRefValue);
						
					}else if(attType.equalsIgnoreCase("float") && FormatHelper.hasContent(flexObj.getData(attColumn))) {
						double attData = flexObj.getDouble(attColumn);
						String attValue = FormatHelper.formatWithPrecisionNoParens(attData/100.0, 4);
						
						//String attListValue = getMOAListValue(attData, attColumnKey);
						//LOGGER.info("attValue :"+attValue);
						flexObj.replace(attColumn.toUpperCase(), flexObj.getData(attColumn), attValue);
						
					}/*else if(attType.equalsIgnoreCase("text") && FormatHelper.hasContent(flexObj.getString(attColumn))) {
						String attData = flexObj.getString(attColumn);
						if(attData.isEmpty() || attData.isBlank())
							continue;
						//LOGGER.info("attData :"+attData);
						//String attListValue = attData.replace("%", "\'%'");
						//attListValue = attListValue.replace(",", "\','");
						String attListValue = CSVGenerator.formatForCSV(attData);
						//String attListValue = "\""+attData+"\"";
						//String attListValue = FormatHelper.encodeForJavascriptInDoubleQuote(attData);
						//LOGGER.info("attListValue :"+attListValue);
						flexObj.replace(attColumn.toUpperCase(), attData, attListValue);
						
					}else if(attType.equalsIgnoreCase("uom") && FormatHelper.hasContent(flexObj.getData(attColumn)) && flexObj.getData(attColumn)!=null) {
						String attData = (String)flexObj.getData(attColumn);
						if(attData.isEmpty() || attData.isBlank())
							continue;
						//LOGGER.info("attData :"+attData);
						
						//String valueUMOMAtt = MetricFormatter.parseNumberFromInputUomToOutputUom(attData, PRECISION, defaultUnitOfMeasure, convertUnitOfMeasure);
						String valueUMOMAtt = MetricFormatter.uomConvertor(attData, defaultUnitOfMeasure);
						//LOGGER.info("valueUMOMAtt :"+valueUMOMAtt);
						//LOGGER.info("valueUMOMAtt UOM :"+MetricFormatter.parseNumber(attData, PRECISION, defaultUnitOfMeasure));
						flexObj.replace(attColumn.toUpperCase(), attData, valueUMOMAtt);
						
					}*/
					
				}
				
				if(FormatHelper.hasContent(flexObj.getData("WTTypeDefinition.branchIditerationInfo"))) {
					long attData = flexObj.getLong("WTTypeDefinition.branchIditerationInfo");
					//LOGGER.info("attData :"+attData);
					try {
						String attRefValue = FlexTypeCache.getFlexType(attData).getTypeDisplayName();
						if(attRefValue.equalsIgnoreCase("Team")) 
							attRefValue = "Socks-Team"; 
							attRefValue = CSVGenerator.formatForCSV(attRefValue);
						//LOGGER.info("attRefValue :"+attRefValue);
						flexObj.replace("WTTYPEDEFINITION.BRANCHIDITERATIONINFO", flexObj.getData("WTTypeDefinition.branchIditerationInfo"), attRefValue);
					} catch (WTException e) {
						e.printStackTrace();
					}
					
				}
				
				if(FormatHelper.hasContent(flexObj.getData("LCSSeason.branchIditerationInfo"))) {
					long attData = flexObj.getLong("LCSSeason.branchIditerationInfo");
					//LOGGER.info("attData :"+attData);
					try {
						LCSSeason seasonObj = (LCSSeason) LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason"+":"+attData);
						String attRefValue = seasonObj.getName();
						//LOGGER.info("attRefValue :"+attRefValue);
						flexObj.replace("LCSSEASON.BRANCHIDITERATIONINFO", flexObj.getData("LCSSeason.branchIditerationInfo"), attRefValue);
					} catch (WTException e) {
						e.printStackTrace();
					}
					
				}
				
				
				finalData.add(flexObj);
			}
			
			return finalData;
		}

		
		public static void extractLinesheetData() throws WTException {
			extractLinesheetData(null);
		}
		
		public static void extractLinesheetData(String logId) throws WTException   {
			
			 Instant start= Instant.now();
			 //AgronReportLogEntry logEntry=AgronReportLogEntry.load(logId);
			 String message="";
			 boolean success=false;
			 int totalRecords=0;
			 String documentName="";
			 LOGGER.info("****************************************************************");
			 LOGGER.info("GENERATE REPORT START*****************************************");
			 LOGGER.info("****************************************************************");
			  try {
					// logEntry.setStatus("Work in Process");
					// logEntry.save();
					
					 
					 Collection records ;
					 boolean sendToVibeIQFlag = false;
					 ClientContext lcsContext = ClientContext.getContext();
					 AgronCSVGenerator csvg = new AgronCSVGenerator();
					 //Get View Columns
					 Collection<String> columnList = getColumnList();
			         Collection<String> attList = getUsedAttList(columnList);
			         Set<TableColumn> reportColumns = configureTableColumnList(columnList);
					 LOGGER.info("reportColumns >>>>"+reportColumns);
					// Get Active Seasons
		            LCSSeasonQuery seasonQuery = new LCSSeasonQuery();
		            SearchResults activeSeasonsList =  seasonQuery.findActiveSeasons();

		            LCSSeason season = null;
		            FlexObjectSorter objSorter = new FlexObjectSorter();
		            List<String> sortList = new ArrayList<String>();
		            sortList.add("LCSSEASON.PTC_TMS_1TYPEINFOLCSSEASON:DESC"); // Sort in a way to process the latest season the last
		            Collection<FlexObject> sortedSeasons = objSorter.sortFlexObjects(activeSeasonsList.getResults(), sortList);
		            Iterator<FlexObject> activeSeasonsListItr = sortedSeasons.iterator();
		            while(activeSeasonsListItr.hasNext()) {

		            	FlexObject obj = activeSeasonsListItr.next();

		            	long ida2a2 = obj.getLong("LCSSEASON.IDA2A2");
		            	season =  (LCSSeason) LCSQuery.findObjectById("OR:com.lcs.wc.season.LCSSeason:"+ida2a2);
		            	sendToVibeIQFlag = false;
		            	if(season.getValue("agrSendToVibeIQ")!=null) {
		            		sendToVibeIQFlag = (boolean) season.getValue("agrSendToVibeIQ");
		            	}
		            	LOGGER.info("seasonName>>>>"+season.getName());
		            	LOGGER.info("sendToVibeIQFlag>>>>"+sendToVibeIQFlag);
		            	
		            	if(season != null && sendToVibeIQFlag) {
		            		 records=getReportData(season, attList);
							 totalRecords=records.size();
							 if(totalRecords==0)
								 continue;
							 String comments="Records: "+totalRecords;
							 message=comments;
							 
							 
							 String reportName = uniqueDocSeasonName(season.getName());
							 reportName = FormatHelper.formatRemoveProblemFileNameChars(reportName);
							 String oldFilePath = CSV_REPORT_FILE + File.separator + reportName + ".csv";
							 LOGGER.info("oldFilePath>>>>"+oldFilePath);
							 File oldFile = new File(oldFilePath);
							 if(oldFile.exists()) {
								 LOGGER.info("oldFile>>>>"+oldFile);
								 FileUtils.removeFile(oldFile);
							 }
							 String csvFilePath = csvg.drawTable(records, reportColumns, lcsContext, reportName, true, false);
							 LOGGER.info("csvFilePath>>>>"+csvFilePath);
							 File csvFile =new File(csvFilePath);
							 LOGGER.info("csvFile>>>>"+csvFile);
								  if(csvFile!=null && csvFile.exists() && csvFile.isFile() && csvFile.canRead()) {
								  String fileName =  csvFile.getName();
								  LOGGER.info("fileName>>>>"+fileName);

									// COPY FILE TO SHARED LOCATION START 
									  InputStream sourceFis = new FileInputStream(csvFile); 
									  Session session = AgronSFTPServerConfig.getSFTPSession(); 
									  if (session != null) { 
										  Channel channel = session.openChannel("sftp"); 
										  if (channel != null) {
											  channel.connect(); 
											  LOGGER.debug("channel >>>>" + channel); 
												
												  boolean fileExists = fileStatsCheck(channel, remoteDir,
												  fileName);
												  if(fileExists) { 
													  LOGGER.debug("fileExists >>>>");
													  ChannelSftp sftpChannel = (ChannelSftp) channel;
													  sftpChannel.rm(remoteDir + fileName);
													  LOGGER.debug("file removed >>>>"); 
													  String uploadFileStatus = uploadFileContent(channel, sourceFis, fileName);
													  LOGGER.debug("uploadFileStatus >>>>" + uploadFileStatus);
												  }else {
													  String uploadFileStatus = uploadFileContent(channel, sourceFis, fileName);
													  LOGGER.debug("uploadFileStatus >>>>" + uploadFileStatus);
												  }
												 
											  
											  AgronSFTPServerConfig.closeSession(); 
										  }
									 }
									 
								  
								  }else { 
									  LOGGER.error("BAD File>>>>"+csvFile);
									  message="Error: Bad File!File is not readable:"+csvFile; 
									  }
		            	}
		            }
					 
						 
			  }catch(Exception e) {
				  e.printStackTrace();
					LOGGER.error("Exception >>>>"+e.getLocalizedMessage(),e);
				  message="Error: "+e.getMessage();
				  
			  }finally{
				  String status="FAILED";
				  if(success) {
					  status="SUCCESS"; 
				  } 
					/*
					 * logEntry.setMessage(message); logEntry.setStatus(status); try {
					 * logEntry.save(); }catch(Exception e) {
					 * LOGGER.error("Exception while saving logentry>>>>"+e.getMessage(),e); }
					 */
				  //sendEmail(status,message,documentName);
			  }
			
			 LOGGER.info("****************************************************************");
			 LOGGER.info("GENERATE REPORT COMPLETED ::"+Duration.between(start, Instant.now()).toMinutes()+" Minutes**************");
			 LOGGER.info("****************************************************************");
		}
		
		/**
		 * 	
		 * @param localFile
		 * @param fileName
		 * @return
		 */

		public static String uploadFileContent(Channel channel, InputStream fis, String fileName){
			String message="FAILED";
			ChannelSftp sftpChannel = null;

			try {
				sftpChannel = (ChannelSftp) channel;
				LOGGER.debug("sftpChannel.getHome()::"+sftpChannel.getHome());
				//LOGGER.debug("sftpChannel.pwd()::"+sftpChannel.pwd());
				LOGGER.debug("remoteDir >>"+remoteDir);
				sftpChannel.put(fis, remoteDir + fileName);
				message="SUCCESS";

			} catch (SftpException e) {
				e.printStackTrace();
				message="FAILED";
			}
			return message;
		} 
		
		/**
		 * 
		 * @param channel
		 * @param fileName
		 * @return
		 */

		public static Boolean fileStatsCheck(Channel channel, String remoteDir, String fileName){
			boolean isFileExist=false;
			ChannelSftp sftpChannel = null;
			try {
				sftpChannel = (ChannelSftp) channel;
				attrs = sftpChannel.stat(sftpChannel.pwd()+"/"+remoteDir+fileName);
			} catch (Exception e) {
				attrs = null;
				LOGGER.debug("File not found to the server "+fileName);
			}
			LOGGER.info("---attrs------"+attrs);
			if (attrs != null) {
				isFileExist=true;

			}
			return isFileExist;
	}
		
		private static String getMOAListValue(String attData, String attColumnKey) {
			String attStrArray[]=listAtts.get(attColumnKey).split(":"); 
			String attKey = attStrArray[0].trim();
			String flexTypeId = attStrArray[1].trim();
			FlexType flexType;
			String selectedValue = null;
			//LOGGER.info("attData :"+attData);
			try {
				flexType = FlexTypeCache.findFlexTypeByTypeIdPath(flexTypeId);
				
				selectedValue = MOAHelper.parseOutDelimsLocalized(""+ attData, ",", flexType.getAttribute(attKey).getAttValueList(), Locale.getDefault());
				selectedValue = selectedValue.replace(", ", ",");
				//LOGGER.info("selectedValue :"+selectedValue);
			} catch (WTException e) {
				e.printStackTrace();
			}			

			return selectedValue;
		}
		
		private static String getCompositeValue(String attData, String attColumnKey) {
			String attStrArray[]=listAtts.get(attColumnKey).split(":"); 
			String attKey = attStrArray[0].trim();
			String flexTypeId = attStrArray[1].trim();
			FlexType flexType;
			String selectedValue = null;
			//LOGGER.info("attData :"+attData);
			try {
				flexType = FlexTypeCache.findFlexTypeByTypeIdPath(flexTypeId);
				selectedValue = MOAHelper.parseCompositeString(""+ attData, flexType.getAttribute(attKey).getAttValueList(), Locale.getDefault(), MOAHelper.DELIM);
				//LOGGER.info("selectedValue :"+selectedValue);
			} catch (WTException e) {
				e.printStackTrace();
			}			

			return selectedValue;
		}

		private static String getAttListValue(String attData, String attColumnKey) {
			
			String attStrArray[]=listAtts.get(attColumnKey).split(":"); 
			String attKey = attStrArray[0].trim();
			String flexTypeId = attStrArray[1].trim();
			FlexType flexType;
			String selectedValue = null;
			//LOGGER.info("attData :"+attData);
			try {
				flexType = FlexTypeCache.findFlexTypeByTypeIdPath(flexTypeId);
			
				selectedValue = flexType.getAttribute(attKey).getAttValueList().getValue(attData, Locale.getDefault());
				//LOGGER.info("selectedValue :"+selectedValue);
			} catch (WTException e) {
				e.printStackTrace();
			}
			return selectedValue;
		}
		
		private static String getObjRefValue(String attData, String attColumnKey) {
			
			String attStrArray[]=listAtts.get(attColumnKey).split(":"); 
			String attKey = attStrArray[0].trim();
			String refClass = attStrArray[1].trim();
			String selectedValue = attData;
			//LOGGER.info("attData :"+attData);
			try {
				Object obj = LCSQuery.findObjectById("VR:"+refClass+":"+attData);
				
				if(obj !=null && obj instanceof LCSCountry) {
					LCSCountry country = (LCSCountry) obj;
					selectedValue = country.getName();
				}else if (obj !=null && obj instanceof LCSSupplier ) {
					LCSSupplier supplier = (LCSSupplier) obj;
					selectedValue = supplier.getName();
				}
				//LOGGER.info("selectedValue :"+selectedValue);
			} catch (WTException e) {
				e.printStackTrace();
			}
			return selectedValue;
		}

		private static Collection<String> getColumnList() throws WTException {
			//Get View
			ColumnList reportView = null;
			Collection columnList = new ArrayList();
			Collection reportColumns = new ArrayList();
			ClientContext lcsContext = ClientContext.getContext();
			FlexType productType = FlexTypeCache.getFlexTypeRoot("Product");
			reportColumns = lcsContext.viewCache.getViews(FormatHelper.getObjectId(productType), "VIEW_LINE_PLAN");
			//LOGGER.info("Views :"+reportColumns.toString());
			for (Object cl: reportColumns) {
				FlexObject flexObj = (FlexObject)cl;
				String viewName = (String)flexObj.getString("COLUMNLIST.DISPLAYNAME");
				//LOGGER.info("View Display Name :"+viewName);
				if(viewName.equalsIgnoreCase(REPORT_VIEW)){
					
					String viewId = (String)flexObj.getString("COLUMNLIST.IDA2A2");
					String stViewID = "OR:com.lcs.wc.report.ColumnList:"+viewId;
					//LOGGER.info("View ID >:"+viewId);
					reportView = lcsContext.getViewCache().getColumnList(stViewID, productType, "VIEW_LINE_PLAN");
					LOGGER.info("View selected :"+reportView.getDisplayName());
					//LOGGER.info("View attributes :"+reportView.getAttributes());
					columnList = reportView.getAttributes(); 
				}
				
			}

			//Collection<String> attList = getUsedAttList(columnList);
			return columnList;
		}
		

		
		 private static Collection<String> getUsedAttList(Collection clist){
		        Iterator i = clist.iterator();
		        Set attList = new HashSet();
		        String key = "";
		        try {
					FlexType prodFlexType = FlexTypeCache.getFlexTypeRoot("Product");
					colorwayUIDAttColumn = prodFlexType.getAttribute(AGR_COLORWAY_UNIQUE_ID).getSearchResultIndex();
					LOGGER.info("colorwayUIDAttColumn >>>>>"+colorwayUIDAttColumn);
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        while(i.hasNext()){
		            key = (String)i.next();
		            if(key.indexOf(".") > -1){
		                key = key.substring(key.indexOf(".") + 1);
		            }

		            attList.add(key);
		        }
		       
		        //configureTableColumnList(clist);

		        return attList;
		    }
			private static Set<TableColumn> configureTableColumnList(Collection<String> attList) {
				LOGGER.info("configureTableColumnList>>>>>");
				
				 Set<TableColumn> tableColumns=new LinkedHashSet<TableColumn>();
				
				
				TableColumn  aColumn = new TableColumn();
				aColumn.setDisplayed(true);
				aColumn.setHeaderLabel("SeasonName");
				aColumn.setTableIndex("LCSSEASON.BRANCHIDITERATIONINFO");
				tableColumns.add(aColumn);
				
				LOGGER.info("attList >>>>>"+attList);
				attList.forEach(column->{
					//LOGGER.info("column >>>>>"+column);
					String tableColumArray[]=column.split("\\.");
					
					String tableKey = tableColumArray[0].trim().toUpperCase();
					String attKey =null;
					if(tableColumArray.length>1 && FormatHelper.hasContent(tableColumArray[1]) ){
						attKey = tableColumArray[1];
					}
					
					//LOGGER.info("tableKey>>>>>"+tableKey);
					//LOGGER.info("attKey>>>>>"+attKey);
					if(attKey.equalsIgnoreCase("typename")) {
						TableColumn  ftColumn = new TableColumn();
						ftColumn.setDisplayed(true);
						ftColumn.setHeaderLabel("ProductType");
						ftColumn.setTableIndex("WTTYPEDEFINITION.BRANCHIDITERATIONINFO");
						ftColumn.setFormat(FormatHelper.STRING_FORMAT);
						tableColumns.add(ftColumn);
					}
					FlexType flexType = null ;
					FlexType productType;
					try {
						productType = FlexTypeCache.getFlexTypeRoot("Product");
					

					switch(tableKey) {
					
					case "COST SHEET": flexType = productType.getReferencedFlexType(ReferencedTypeKeys.COST_SHEET_TYPE); break;
					case "SOURCING CONFIGURATION" : flexType = productType.getReferencedFlexType(ReferencedTypeKeys.SOURCING_CONFIG_TYPE);break;
					case "PRODUCT" : flexType = productType;break;
					case "SEASON" : flexType = FlexTypeCache.getFlexTypeRoot("Season"); break;
					default : LOGGER.debug("Type Does not match");
					break;
					}
					if(flexType!=null) {
						//LOGGER.info("flexType >>>>>"+flexType.getFullNameDisplay());
						FlexTypeAttribute fta = flexType.getAttribute(attKey);
						String attColumn = fta.getSearchResultIndex();
						//LOGGER.info("attColumn >>>>>"+attColumn);
						//String attDisplay = fta.getAttDisplay();
						//LOGGER.info("attDisplay >>>>>"+attDisplay);
						String attVariableType = fta.getAttVariableType();
						if (attVariableType.equalsIgnoreCase("choice") || attVariableType.equalsIgnoreCase("driven") || 
								attVariableType.equalsIgnoreCase("moaList") || attVariableType.equalsIgnoreCase("boolean") ) {
							listAtts.put(attVariableType.concat(":").concat(attColumn), attKey.concat(":").concat(flexType.getIdPath()));
						}else if(attVariableType.equalsIgnoreCase("object_ref")){
							
					    	String refClass = flexType.getAttribute(attKey).getRefDefinition().getRefClass();
	
							listAtts.put(attVariableType.concat(":").concat(attColumn), attKey.concat(":").concat(refClass));
						}else if(attVariableType.equalsIgnoreCase("float") && fta.getAttDecimalFigures() !=0) {
							listAtts.put(attVariableType.concat(":").concat(attColumn), attKey);
						}
						
						TableColumn tColumn=new TableColumn(attColumn.toUpperCase(),attKey);
						if(attVariableType.equalsIgnoreCase("currency")){
							tColumn.setFormat(FormatHelper.FLOAT_FORMAT_NO_SYMBOLS);
							tColumn.setDecimalPrecision(fta.getAttDecimalFigures());
							tColumn.setAttributeType("float");
						}
						else {
							tColumn.setAttributeType(attVariableType);
						}
	
						tableColumns.add(tColumn);
					}
				
					} catch (WTException e) {
						e.printStackTrace();
					}

					
				});	
				LOGGER.info("listAtts >>"+listAtts.toString());
				return tableColumns; 
				
				
			}
		
	
	
	
	
	
	
	
	
	public static String uniqueDocName() {
		SimpleDateFormat sdfDestination = new SimpleDateFormat(REPORT_ID_DATE_FORMAT);
		String documentName= REPORT_NAME.trim()+"-"+sdfDestination.format(new Date());
		return documentName;
	}
	
	public static String uniqueDocSeasonName(String seasonName) {
		SimpleDateFormat sdfDestination = new SimpleDateFormat(REPORT_ID_DATE_FORMAT);
		String documentName= seasonName.trim()+"-"+sdfDestination.format(new Date());
		return documentName;
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
 
    
   
    
}
