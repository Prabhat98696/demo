package com.agron.wc.sftpImageExport;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;


public class AgronSKUPrepareQuery {
	
	static final Logger log = LogManager.getLogger("com.agron.wc.sftpImageExport");
	
	public SearchResults findSKUPreparedQueryNC(String sDate, String eDate) {
		 
		//String sdate ="08-25-2020 11:14:23 PM";
		//String edate="08-27-2020 01:14:23 PM";
		log.info("::findSKUPreparedQueryNC:::");
		SimpleDateFormat convertTodate = new SimpleDateFormat("MM-dd-yyyy:hh:mm:ss a");
		PreparedQueryStatement statement = new PreparedQueryStatement();
		SearchResults results = null ;
		Date startdate=null;
		Date enddate=null;
	    try{
			startdate = convertTodate.parse(sDate);
			enddate = convertTodate.parse(eDate);
			
			statement.appendSelectColumn("LCSSKU", "ptc_str_6typeInfoLCSSKU");
			statement.appendSelectColumn("LCSSKU", "ptc_str_4typeInfoLCSSKU");
			statement.appendSelectColumn("LCSSKU", "PARTPRIMARYIMAGEURL");
			statement.appendSelectColumn("LCSProduct", "ptc_lng_4typeInfoLCSProduct");
			statement.appendSelectColumn("LCSSKU", "UPDATESTAMPA2");
			statement.appendSelectColumn("LCSSKU", "modifyStampA2");
			statement.appendSelectColumn("LCSSKU", "ptc_str_14typeInfoLCSSKU");
			statement.appendSelectColumn("LCSSKU", "BRANCHIDITERATIONINFO");
			statement.appendSelectColumn("LCSSKU", "PRODUCTAREVID");
			
			statement.appendFromTable(LCSProduct.class, "LCSProduct");
			statement.appendFromTable(LCSSKU.class, "LCSSKU");

			statement.appendJoin(new QueryColumn("LCSProduct", "BRANCHIDITERATIONINFO"), new QueryColumn("LCSSKU", "PRODUCTAREVID"));
			
			statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "1", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, "iterationInfo.latest"), "1", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria( new Criteria(new QueryColumn(LCSSKU.class, "versionInfo.identifier.versionId"), "A", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "versionInfo.identifier.versionId"), "A", "="));
			 statement.appendAndIfNeeded();
			 statement.appendCriteria(new Criteria("LCSSKU", "ptc_str_6typeInfoLCSSKU", "NULL", "<>"));
			 //statement.appendAndIfNeeded();
			// statement.appendCriteria(new Criteria("LCSSKU", "ptc_str_6typeInfoLCSSKU", " ", "<>"));
			 if(!"".equals(sDate) && sDate!=null){
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSSKU", "modifyStampA2", startdate, Criteria.GREATER_THAN));
			 }
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSSKU", "modifyStampA2", enddate, Criteria.LESS_THAN_EQUAL));
			log.info("!!Query FOR NC!!!::"+statement);
		
		 results = LCSQuery.runDirectQuery(statement);
		
		}
		catch(Exception e){
			System.out.println("AgronSKUPrepareQuery NC :"+e);
		}
		return results;
		
	}
	
	/**
	 * 
	 * @param sDate
	 * @param eDate
	 * @return
	 */
	public SearchResults findSKUPreparedQueryFR(String sDate, String eDate) {
		 
		log.info("::findSKUPreparedQueryFR:::");
		//08-31-2020:11:49:29 AM
		SimpleDateFormat convertTodate = new SimpleDateFormat("MM-dd-yyyy:hh:mm:ss a");
		PreparedQueryStatement statement = new PreparedQueryStatement();
		SearchResults results = null ;
		Date startdate=null;
		Date enddate=null;
		
		try{
	    	if(!"".equals(sDate) && sDate != null){
	    		startdate = convertTodate.parse(sDate);
	    	}
	    		enddate = convertTodate.parse(eDate);
	    	
			statement.appendSelectColumn("LCSSKU", "ptc_str_6typeInfoLCSSKU");
			statement.appendSelectColumn("LCSSKU", "ptc_str_4typeInfoLCSSKU");
			statement.appendSelectColumn("LCSSKU", "PARTPRIMARYIMAGEURL");
			statement.appendSelectColumn("LCSProduct", "ptc_lng_4typeInfoLCSProduct");
			statement.appendSelectColumn("LCSSKU", "UPDATESTAMPA2");
			statement.appendSelectColumn("LCSSKU", "modifyStampA2");
			statement.appendSelectColumn("LCSSKU", "ptc_str_14typeInfoLCSSKU");
			statement.appendSelectColumn("LCSSKU", "BRANCHIDITERATIONINFO");
			statement.appendSelectColumn("LCSSKU", "PRODUCTAREVID");
			
			statement.appendFromTable(LCSProduct.class, "LCSProduct");
			statement.appendFromTable(LCSSKU.class, "LCSSKU");

			statement.appendJoin(new QueryColumn("LCSProduct", "BRANCHIDITERATIONINFO"), new QueryColumn("LCSSKU", "PRODUCTAREVID"));
			
			statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "1", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSSKU.class, "iterationInfo.latest"), "1", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria( new Criteria(new QueryColumn(LCSSKU.class, "versionInfo.identifier.versionId"), "A", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "versionInfo.identifier.versionId"), "A", "="));
			statement.appendAndIfNeeded();
			 //statement.appendCriteria(new Criteria("LCSSKU", "PARTPRIMARYIMAGEURL", " ", "IS NOT NULL"));
			 statement.appendCriteria(new Criteria("LCSSKU", "PARTPRIMARYIMAGEURL", "NULL", "<>"));
			statement.appendAndIfNeeded();
			 statement.appendCriteria(new Criteria("LCSSKU", "PARTPRIMARYIMAGEURL", " ", "<>"));
			 if(!"".equals(sDate) && sDate!=null){
				 statement.appendAndIfNeeded();
				 statement.appendCriteria(new Criteria("LCSSKU", "CREATESTAMPA2", startdate, Criteria.GREATER_THAN));
			 }
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria("LCSSKU", "CREATESTAMPA2", enddate, Criteria.LESS_THAN_EQUAL));
			log.info("!!Query FOR FR!!!::"+statement);
		
		 results = LCSQuery.runDirectQuery(statement);
		
		}
		catch(Exception e){
			log.info("AgronSKUPrepareQuery  FR:"+e);
		}
		return results;
		
	}
	
}
