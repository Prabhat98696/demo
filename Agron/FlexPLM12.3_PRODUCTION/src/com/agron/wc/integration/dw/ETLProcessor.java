package com.agron.wc.integration.dw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.StoredProcedureQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import com.agron.wc.integration.dw.model.PTCSeason;
import com.agron.wc.integration.dw.model.PTC_ColorwaySeason_Staging;
import com.agron.wc.integration.dw.model.PTC_Colorway_Staging;
import com.agron.wc.integration.dw.model.PTC_LoadHistory;
import com.agron.wc.integration.dw.model.PTC_ModelSeasonVendor_Staging;
import com.agron.wc.integration.dw.model.PTC_ModelSeason_Staging;
import com.agron.wc.integration.dw.model.PTC_Model_Staging;
import com.agron.wc.integration.dw.model.PTC_SizeSeasonVendor_Staging;
import com.agron.wc.integration.dw.model.PTC_Size_Staging;
import com.agron.wc.integration.dw.model.PTC_Vendor_Staging;
import com.agron.wc.integration.dw.util.HibernateUtil;
import com.agron.wc.integration.util.AgronIntegrationUtil;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.variabletype.VariableType;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.util.LCSProperties;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.queue.ProcessingQueue;
import wt.queue.QueueEntry;
import wt.queue.QueueHelper;
import wt.queue.ScheduleQueue;
import wt.session.SessionHelper;
import wt.util.WTException;

public class ETLProcessor implements RemoteAccess  {

	public static  Integer PTCLoadKey;
	public static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.dw");
	private static final String USERNAME = LCSProperties.get("com.agron.wc.integration.username", "wcadmin");
	private static final String PASSWORD = LCSProperties.get("com.agron.wc.integration.password", "wcadmindev"); 
    private final static String DW_REPORT_QUEUE_NAME =LCSProperties.get("com.agron.wc.integration.dw.ETLProcessor.queue.name", "AgronStagingExportQueue");
	private static final String recipientAddress = LCSProperties.get("com.agron.wc.integration.dw.toEmail", "agronsupport@acnovate.com");
	private static final String successMessage = LCSProperties.get("com.agron.wc.integration.dw.successMessage", "PTC - DW export successfully completed");
	private static final String failureMsg = LCSProperties.get("com.agron.wc.integration.dw.failureMessage", "PTC - DW export FAILED");

    private final static String[] recipients = {recipientAddress};
	public static void main(String[] args){		

		ETLProcessor.executeRMI("addJobQueueEntry");	
		
	}
	public static PTC_LoadHistory process(String args) {
		return process(); 
	}
	
	public static PTC_LoadHistory processAttValues() {
		LOGGER.info(" START EXPORTING TYPE MANAGER  = ");
		PTC_LoadHistory _loadHistory = new PTC_LoadHistory();
		
		 try {
			 File myObj = new File("D:\\attvalues.txt");
			 FileWriter myWriter1 = new FileWriter(myObj);
			 BufferedWriter myWriter = new BufferedWriter(myWriter1);
			FlexType flexTypeRoot = FlexTypeCache.getFlexTypeRoot("Material");
			Collection flexTypeMaterial = flexTypeRoot.getAllChildren();
			Iterator i = flexTypeMaterial.iterator();
			while(i.hasNext()){
				FlexType type = (FlexType) i.next();
				String typename= type.getFullNameDisplay();
				if(!typename.contains("Fabric")){
					continue;
				}
				LOGGER.info("TYPE NAME " + type.getFullNameDisplay());
				myWriter.write("TYPE NAME " + type.getFullNameDisplay());
				myWriter.newLine();
				
				Collection attColl = type.getAllAttributes();
				Iterator itr = attColl.iterator();
				
			
			
			while(itr.hasNext()) {
				FlexTypeAttribute  att = (FlexTypeAttribute) itr.next();			
				String key = att.getAttKey();				
				String display = att.getAttDisplay();
				String attVarType = att.getAttVariableType();

				
				AttributeValueList attList = att.getAttValueList();
				if(attList == null ) {
					continue;
				}
				LOGGER.info(" key = " + key + " display = " + display+ " attVarType = " + attVarType);
				 myWriter.write(" key = " + key + " display = " + display+ " attVarType = " + attVarType+"\\n");
					myWriter.newLine();

				LOGGER.info("WITH TRUE");
				Collection selectableValues = attList.getSelectableValues(Locale.getDefault(), true);
				Iterator sItr = selectableValues.iterator();
				
			
				
				LOGGER.info("WITH False");
				Collection selectableValues1 = attList.getSelectableValues(Locale.getDefault(), false);
				Iterator i1 = selectableValues1.iterator();
				LOGGER.info(display+" - "+selectableValues1);
				//myWriter.write(display+" - "+selectableValues1);
				myWriter.write(display);
				myWriter.newLine();
				while(i1.hasNext()) {
					
					myWriter.write((String) i1.next());	
					myWriter.newLine();
				}
				
				myWriter.newLine();
				}
			}
			
			
			 myWriter.close();
			
		} catch (WTException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _loadHistory;
		
	}
	
 
	public static PTC_LoadHistory process() {
		LOGGER.info("Data Warehouse export for PTC Staging Tables  Start");
		Session session = null;
		boolean txState = false;
		PTC_LoadHistory _loadHistory = new PTC_LoadHistory();
		
		try {

    		 session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			
			_loadHistory.setStagingTableLoadStartTime(new Date());

			NativeQuery ps = session.createSQLQuery("DELETE FROM  PTC_SizeSeasonVendor_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_SizeSeasonVendor_Staging");

			ps = session.createSQLQuery("DELETE FROM  PTC_ModelSeasonVendor_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_ModelSeasonVendor_Staging");

			ps = session.createSQLQuery("DELETE FROM  PTC_Vendor_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_Vendor_Staging");

			ps = session.createSQLQuery("DELETE FROM  PTC_Size_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_Size_Staging");

			ps = session.createSQLQuery("DELETE FROM  PTC_ColorwaySeason_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_ColorwaySeason_Staging");

			ps = session.createSQLQuery("DELETE FROM  PTC_Colorway_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_Colorway_Staging");

			ps = session.createSQLQuery("DELETE FROM  PTC_ModelSeason_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_ModelSeason_Staging");


			ps = session.createSQLQuery("DELETE FROM PTC_Model_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM  PTC_Model_Staging");

			ps = session.createSQLQuery("DELETE FROM  PTC_Season_Staging");
			ps.executeUpdate();
			LOGGER.info("DELETE FROM PTC_Season_Staging");

			Query<PTCSeason> q1 = session.createQuery("From PTC_Season_Staging", PTCSeason.class);
			List<PTCSeason> resultList1 = q1.list();
			LOGGER.info("Total Records in PTC_Season_Staging " + resultList1.size());

			Query<PTC_Model_Staging> q2 = session.createQuery("From PTC_Model_Staging", PTC_Model_Staging.class);
			List<PTC_Model_Staging> resultList2 = q2.list();
			LOGGER.info("Total Records in PTC_Model_Staging " + resultList2.size());

			Query<PTC_ColorwaySeason_Staging> q3 = session.createQuery("From PTC_ColorwaySeason_Staging", PTC_ColorwaySeason_Staging.class);
			List<PTC_ColorwaySeason_Staging> resultList3 = q3.list();
			LOGGER.info("Total Records in PTC_ColorwaySeason_Staging " + resultList3.size());

			Query<PTC_ModelSeason_Staging> q4 = session.createQuery("From PTC_ModelSeason_Staging", PTC_ModelSeason_Staging.class);
			List<PTC_ModelSeason_Staging> resultList4 = q4.list();
			LOGGER.info("Total Records in PTC_ModelSeason_Staging " + resultList4.size());

			Query<PTC_Colorway_Staging> q5 = session.createQuery("From PTC_Colorway_Staging", PTC_Colorway_Staging.class);
			List<PTC_Colorway_Staging> resultList5 = q5.list();
			LOGGER.info("Total Records in PTC_Colorway_Staging " + resultList5.size());

			Query<PTC_Size_Staging> q6 = session.createQuery("From PTC_Size_Staging", PTC_Size_Staging.class);
			List<PTC_Size_Staging> resultList6 = q6.list();
			LOGGER.info("Total Records in PTC_Size_Staging " + resultList6.size());

			Query<PTC_Vendor_Staging> q7 = session.createQuery("From PTC_Vendor_Staging", PTC_Vendor_Staging.class);
			List<PTC_Vendor_Staging> resultList7 = q7.list();
			LOGGER.info("Total Records in PTC_Vendor_Staging " + resultList7.size());

			Query<PTC_ModelSeasonVendor_Staging> q8 = session.createQuery("From PTC_ModelSeasonVendor_Staging", PTC_ModelSeasonVendor_Staging.class);
			List<PTC_ModelSeasonVendor_Staging> resultList8 = q8.list();
			LOGGER.info("Total Records in PTC_Season_Staging " + resultList8.size());

			Query<PTC_SizeSeasonVendor_Staging> q9 = session.createQuery("From PTC_SizeSeasonVendor_Staging", PTC_SizeSeasonVendor_Staging.class);
			List<PTC_SizeSeasonVendor_Staging> resultList9 = q9.list();
			LOGGER.info("Total Records in PTC_SizeSeasonVendor_Staging " + resultList9.size());
						
			session.getTransaction().commit();
			session.close();
			
			
			// Get a list of all the seasons.	
			ExtractAndLoadSeason seasonETL = new ExtractAndLoadSeason();
			ArrayList<LCSSeason> seasonList = seasonETL.processSeason();

			// Process all the information seasonally
			ExtractAndLoadModelData modelETL = new     ExtractAndLoadModelData();
			modelETL.processSeason(seasonList,_loadHistory);
	

			
			txState = true;
			
		} catch (Exception e) {
			LOGGER.error("Error in DW Export ",e);
			e.printStackTrace();
			
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			_loadHistory.setStagingTableLoadEndTime(new Date());
			_loadHistory.setStagingTableLoadComplete(false);
			session.save(_loadHistory);
			LOGGER.error("PTC_Loadhistory ERROR  Entry created");

			session.getTransaction().commit();
			session.close();
			try {
				HibernateUtil.sendEmail("",recipients,failureMsg,e.getMessage(), null);
			} catch (WTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}finally {
			
			if(txState == false && session != null) {
				session.getTransaction().rollback();
				if(session.isOpen()){
						session.close();
					}
				LOGGER.error("DW Export program Ended With Error .....");
				
			}
			try {			
				HibernateUtil.sendEmail("",recipients, successMessage,"", null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.error("DW Export program FAILED HERE .....");
			}
			LOGGER.info("DW Export program Ends Sucesfully .....");
		}


		return _loadHistory;


	}
	
	
	 public static void executeRMI(String method) {
			System.out.println("RMI Execution START>>method::"+method);
		    RemoteMethodServer rmsObj = RemoteMethodServer.getDefault();
			rmsObj.setUserName(USERNAME);
			rmsObj.setPassword(PASSWORD);
			Class<?> cs[] = new Class[0];
			Object args[] = new Object[0];
			try {
				rmsObj.invoke(method, "com.agron.wc.integration.dw.ETLProcessor", null, cs, args);
			} catch (RemoteException e) {
				e.printStackTrace();
				System.out.println("RMI Execution RemoteException");
			} catch (InvocationTargetException e) {
				System.out.println("RMI Execution InvocationTargetException");
				e.printStackTrace();
			}
			System.out.println("RMI Execution COMPLETED");
		}
	 
	
  public static void addJobQueueEntry() {
 	 LOGGER.info("Add report job to queue>>>"+DW_REPORT_QUEUE_NAME);

 	 try {
 		
 		 QueueEntry queueEntry=addQueueEntry(DW_REPORT_QUEUE_NAME, "process","com.agron.wc.integration.dw.ETLProcessor",null);

 		 LOGGER.info("PTC Staging export  job added to queue::");
 	 } catch (Exception e) {
 		 LOGGER.error("PTC Staging export - Error while adding job to queue",e);
 		 try {
			HibernateUtil.sendEmail("",recipients, failureMsg,e.getMessage(), null);
		} catch (WTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
			if(! "STARTED".equals(processQueue.getQueueState())) {
				QueueHelper.manager.startQueue(processQueue);
			}
				
				wt.org.WTPrincipal user = SessionHelper.manager.getPrincipal();
				Class<?> argTypes[] = {String.class};
				Object args[] = {logEntryId};	
				QueueEntry queueEntry=processQueue.addEntry(user, method, className, argTypes, args);
		
			return queueEntry;
		}
}
