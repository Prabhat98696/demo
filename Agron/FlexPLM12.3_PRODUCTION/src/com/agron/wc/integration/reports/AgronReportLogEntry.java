package com.agron.wc.integration.reports;

import java.util.Collection;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.QueryStatement;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSLogEntryLogic;
import com.lcs.wc.foundation.LCSLogEntryQuery;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.fc.PersistenceHelper;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronReportLogEntry {
	
	      private String queueName=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReport.queue.name", "AgronMaterialAggregationReportQueue");;
	      private String message="";
	      private String status="";
	      private String documentNumericId="";
	      private String documentName="";
	      private String flexObjectId = "";
	      private static  String STATUS_KEY=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReportLogEntry.statusKey","agrStatus");;
	      private static  String MESSAGE_KEY=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReportLogEntry.messageKey","agrMessage"); 
	      private static  String DOCUMENT_KEY=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReportLogEntry.documentKey");
	      private static  String QUEUE_NAME_KEY=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReportLogEntry.queueNameKey");
	      private static  String LOG_ENTRY_FLEX_TYPE_PATH=LCSProperties.get("com.agron.wc.integration.reports.AgronMaterialAggregationReportLogEntry.flextypeIdPath","Log Entry\\materialAggregationReportLogEntry");
	      
	      private static  boolean DOCUMENT_IS_OBJECT_REFERENCE=LCSProperties.getBoolean("com.agron.wc.integration.reports.AgronMaterialAggregationReportLogEntry.document.isObjectReference",true);
		  
	      private static final Logger logger = LogManager.getLogger(AgronReportLogEntry.class);
	    
	      public AgronReportLogEntry() {
	  
	      }
	      
	      protected AgronReportLogEntry(LCSLogEntry entry) throws WTException {
	    	  if (entry != null) {
	    		  try {
	    			  if (PersistenceHelper.isPersistent(entry)) {
		    			  this.flexObjectId = FormatHelper.getObjectId(entry);
		    		  } else {
		    			  this.flexObjectId = null;
		    		  }

		    		  this.message = (String)entry.getValue(MESSAGE_KEY);
		    		  this.status =(String)entry.getValue(STATUS_KEY);
		    			if(FormatHelper.hasContent(QUEUE_NAME_KEY)) {
				    		  this.queueName = (String)entry.getValue(QUEUE_NAME_KEY);
		    			}
		    		  if(FormatHelper.hasContent(DOCUMENT_KEY)) {
		    			  if(DOCUMENT_IS_OBJECT_REFERENCE) {
		    				  if(entry.getValue(DOCUMENT_KEY) !=null) {
		    					  this.documentNumericId = FormatHelper.getNumericVersionIdFromObject((LCSDocument)entry.getValue(DOCUMENT_KEY));
		    				  }  
		    			  }else {
		    				  this.documentName= (String)entry.getValue(DOCUMENT_KEY);
		    			  }      
		    		  }
	    		  }catch(Exception e) {
	    			  e.printStackTrace();
	    			  logger.error("AgronReportLogEntry error "+e.getMessage(),e);
	    		  }
	    	  
	    	  }
	      }
	      
	    
		public String getQueueName() {
			return queueName;
		}

		public void setQueueName(String queueName) {
			this.queueName = queueName;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}


		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getdocumentNumericId() {
			return documentNumericId;
		}

		public void setdocumentNumericId(String documentNumericId) {
			this.documentNumericId = documentNumericId;
		}

		public String getFlexObjectId() {
			return flexObjectId;
		}

		public void setFlexObjectId(String flexObjectId) {
			this.flexObjectId = flexObjectId;
		}

		public String getDocumentName() {
			return documentName;
		}

		public void setDocumentName(String documentName) {
			this.documentName = documentName;
		}
		

		public LCSLogEntry asLogEntry() throws WTException {
			LCSLogEntry entry = null;
			if (FormatHelper.hasContent(this.flexObjectId)) {
				entry = (LCSLogEntry)LCSQuery.findObjectById(this.flexObjectId,false);
				 if(PersistenceHelper.isPersistent(entry)) {
					 entry =(LCSLogEntry) PersistenceHelper.manager.refresh(entry); 
		    	  }
			}
			try {
				if (entry == null) {
					entry = LCSLogEntry.newLCSLogEntry();
					FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(LOG_ENTRY_FLEX_TYPE_PATH);
					entry.setFlexType(logEntryType);
				}
				entry.setValue(MESSAGE_KEY, this.message);
				if(FormatHelper.hasContent(QUEUE_NAME_KEY)) {
					entry.setValue(QUEUE_NAME_KEY, this.queueName);
				}
				if(FormatHelper.hasContent(DOCUMENT_KEY)) {
					if(DOCUMENT_IS_OBJECT_REFERENCE) {
						if (FormatHelper.hasContent(this.documentNumericId)) {
							entry.setValue(DOCUMENT_KEY, this.documentNumericId);	
						}
					}else {
						if (FormatHelper.hasContent(this.documentName)) {
							entry.setValue(DOCUMENT_KEY, this.documentName);	
						}	
					}	
				}	
				entry.setValue(STATUS_KEY, this.status);
				logger.info("entry>>>>"+entry);
				return entry;
			} catch (WTPropertyVetoException e) {
  			  logger.error("Error in asLogEntry "+e.getMessage(),e);
				throw new WTException(e);
			}
}

	      public AgronReportLogEntry save() throws WTException {
	    	  LCSLogEntry entry = (new LCSLogEntryLogic()).saveLog(this.asLogEntry());
	    	  this.flexObjectId = FormatHelper.getObjectId(entry);
	    	  return this;
	      }
	      
	      public static AgronReportLogEntry load(String flexLogEntryId) throws WTException {
	    	  LCSLogEntry entry =null;
	    	  
	    	  if(FormatHelper.hasContent(flexLogEntryId)) {
	    		  try {
	    			  entry= (LCSLogEntry)LCSQuery.findObjectById(flexLogEntryId);	  
	    		  }catch(Exception e) {
	    			  e.printStackTrace();
	    			  logger.error("Error while load  logentry "+flexLogEntryId+" ::"+e.getMessage(),e);
	    		  }
	    	  }
	          return entry == null ? new AgronReportLogEntry() : new AgronReportLogEntry(entry);
	       }

	      
	      public LCSDocument getDocument() {
	    	  if (DOCUMENT_IS_OBJECT_REFERENCE && FormatHelper.hasContent(this.documentNumericId)) {
	    		  try {
	    			  LCSDocument document = (LCSDocument)LCSQuery.findObjectById("VR:com.lcs.wc.document.LCSDocument:" + this.documentNumericId);  
	    			  return document;
	    		  }catch(WTException e) {
	    			  logger.error("Error while getting document object "+e.getMessage(),e);
	    			  e.printStackTrace();
	    		  }
	    	  }
	    	  return null;
	      }
	
	      public void remove() throws WTException {
	    	  Transaction tr = new Transaction();
	    	  try {
	    		  LCSLogEntry entry = this.asLogEntry();
	    		  if (PersistenceHelper.isPersistent(entry)) {
	    			  PersistenceHelper.manager.delete(entry);
	    			  tr.commit();
	    		  }
	    	  } catch (Exception var5) {
	    		  try {
	    			  logger.error("Error while remove logentry"+var5.getMessage(),var5);
	    			  tr.rollback();
	    		  } catch (Exception var4) {
	    			  var4.printStackTrace();
	    			  logger.error("Error while rollback remove logentry"+var4.getMessage(),var4);
	    		  }
	    	  }
	      }
 
	      public AgronReportLogEntry changeStatus(String newStatus) throws PersistenceException {
	          return this.changeStatus(newStatus, (String)null);
	       }
	    
	      
	    
	      
	      public AgronReportLogEntry changeStatus(String newStatus, String message) {
	    	  Transaction tr = new Transaction();
	    	  try {
	    		  tr.start();
	    		  if(FormatHelper.hasContent(newStatus)) {
	    			  this.setStatus(newStatus); 
	    		  }
	    		  if (FormatHelper.hasContent(message)) {
	    			  this.setMessage(message);
	    		  }
	    		  AgronReportLogEntry entry = this.save();
	    		  tr.commit();
	    		  return entry;
	    	  } catch (Exception var7) {
	    		  try {
	    			  tr.rollback();
	    			  logger.error("Error while changing logentry status"+var7.getMessage(),var7);
	    		  } catch (Exception var6) {
	    			  var6.printStackTrace();
	    			  logger.error("Error while rollback  logentry status"+var6.getMessage(),var6);
	    		  }
	    		  return null;
	    	  }
	      }

		@Override
		public String toString() {
			return "AgronReportLogEntry [queueName=" + queueName + ", message=" + message + ", status=" + status
					+ ", documentNumericId=" + documentNumericId + ", documentName=" + documentName + ", flexObjectId="
					+ flexObjectId + "]";
		}
	       
		
		public static void manageLogEntries(String status,boolean isDelete) {
			try {
				FlexType logEntryType = FlexTypeCache.getFlexTypeFromPath(LOG_ENTRY_FLEX_TYPE_PATH);//Log Entry\\materialAggregationReportLogEntry
				System.out.println("TypeID>>"+logEntryType.getTypeIdPath());
				QueryStatement statement = new QueryStatement();
				statement.appendFromTable(LCSLogEntry.class);
				statement.appendSelectColumn(new QueryColumn(LCSLogEntry.class, "thePersistInfo.theObjectIdentifier.id"));
				statement.appendSelectColumn(new QueryColumn(LCSLogEntry.class, logEntryType.getAttribute(STATUS_KEY).getColumnDescriptorName()));
				statement.appendSelectColumn(new QueryColumn(LCSLogEntry.class, logEntryType.getAttribute(MESSAGE_KEY).getColumnDescriptorName()));
				statement.appendCriteria(new Criteria(new QueryColumn("LCSLOGENTRY", "FLEXTYPEIDPATH"), logEntryType.getTypeIdPath(), "="));

				if(FormatHelper.hasContent(status)) {
					statement.appendAndIfNeeded();
					statement.appendCriteria(new Criteria(new QueryColumn(LCSLogEntry.class, logEntryType.getAttribute(STATUS_KEY).getColumnDescriptorName()), status, "="));	
				}


				Collection results=LCSQuery.runDirectQuery(statement).getResults();
				logger.info("results>>>"+results.size()+"::"+results);
				Collection<LCSLogEntry> logEntries=LCSLogEntryQuery.getObjectsFromResults(results, "OR:com.lcs.wc.foundation.LCSLogEntry:", "LCSLogEntry.IDA2A2");
				logger.info("logEntries>>>"+logEntries.size()+"::"+logEntries);
				if(isDelete && logEntries.size()>0) {
					Transaction tr = new Transaction();
					logger.info("Delete entry Transection Start>>>");
					try
					{
						logEntries.forEach(entry->{
							logger.info("Delete entry>>>"+entry);
							if (PersistenceHelper.isPersistent(entry)) {
								try {
									PersistenceHelper.manager.delete(entry);
								} catch (WTException e) {
									logger.error("Error while delete  logentry"+entry+"::"+e.getMessage());
									e.printStackTrace();
								}
							}
						});
						logger.info("Transection Commit>>>");
						tr.commit();
						logger.info("Delete entry Transection Commited>>>");
					}catch(Exception e) {
						try {
							logger.error("Error while delete  logentry"+e.getMessage());
							tr.rollback();
						} catch (Exception var4) {
							var4.printStackTrace();
							logger.error("Error while rollback delete logentry"+var4.getMessage(),var4);
						}
					}

				}
			}catch(Exception e) {
				logger.error("Error >>>>>"+e.getMessage(),e);
			}


		}		
		
	    
	      
}