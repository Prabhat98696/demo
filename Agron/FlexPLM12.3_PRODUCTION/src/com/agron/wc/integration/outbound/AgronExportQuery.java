package com.agron.wc.integration.outbound;

import com.lcs.wc.db.*;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.LCSProperties;
import java.text.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.util.WTException;
import java.util.Vector;

/**
 * 
 * @author Vinod Dalai
 * @version 1.0
 * 
 * @author Mallikarjuna Savi
 * @version 2.0
 * 
 */

public class AgronExportQuery
{
	/**
	 * Defined to store the constant value.
	 */
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");
    private static final String BRANCHIDITERATIONINFO = "BRANCHIDITERATIONINFO";
    private static final String EFFECT_OUT_DATE = "effectoutdate";
    private static final String FLEXTYPE = "FLEXTYPE";
    private static final String IDA3A12 = "IDA3A12";
    private static final String IDA3A5 = "IDA3A5";
    private static final String IDA3MASTERREFERENCE = "IDA3MASTERREFERENCE";
    private static final String LATESTITERATIONINFO = "LATESTITERATIONINFO";
    private static final String LCSMOAOBJECT = "LCSMOAOBJECT";
    private static final String LCSPRODUCT = "LCSPRODUCT";
    private static final String LCSSEASONPRODUCTLINK = "LCSSEASONPRODUCTLINK";
    private static final String MODIFY_STAMP_A2 = "modifystampa2";
    private static final String PRODAREV = "PRODAREV";
    private static final String PRODUCTAREVID = "PRODUCTAREVID";
    private static final String TYPEIDPATH = "TYPEIDPATH";
    private static final String FLEXTYPEIDPATH = "FLEXTYPEIDPATH";
    private static final String SKUAREV = "SKUAREV";
    private static final String TYPENAME = "TYPENAME";
    private static final String MOA_ADOPTED_KEY = LCSProperties.get("com.agron.wc.interface.attributes.colorway.moa.adoptedKey");
    private static final String COLORWAY_MOA_PATH = LCSProperties.get("com.agron.wc.interface.attributes.colorway.moaPath");
    private static final String UPCMOA_FLEXTYPE = LCSProperties.get("com.agron.wc.interface.attributes.colorway.moa.flextype", "UPC-Article Number Table");
   
	/**
	 * This constructor is used to set the boolean value
	 * @param isErrorOccure is Boolean object.
	 */
    public AgronExportQuery()
    {
    }

	/**
	 * This method is used to get the collection of data for the given object.
	 * @param curentTimeStamp is String object
	 * @param lastRunTimeStamp is String object
	 * @param tableName is String object
	 * @return Collection type
	 */
    public Collection<String> getObjects(String curentTimeStamp, String lastRunTimeStamp, String tableName)
    {
        
        ArrayList<String> objectIdCol = new ArrayList<String>();
        try
        {
            FlexType flextype = FlexTypeCache.getFlexTypeFromPath(COLORWAY_MOA_PATH);
            String moaAdoptedColumn = flextype.getAttribute(MOA_ADOPTED_KEY).getColumnName();
            LOGGER.info((new StringBuilder()).append("lastRunTimeStamp : ").append(lastRunTimeStamp).toString());
            TimeZone timezone = TimeZone.getTimeZone("GMT");
            SimpleDateFormat simpledateformat = new SimpleDateFormat("MM-dd-yyyy:hh:mm:ss a");
            simpledateformat.setTimeZone(timezone);
            Date currentrunDate = simpledateformat.parse(curentTimeStamp);
            PreparedQueryStatement preparedquerystatement = new PreparedQueryStatement();
            preparedquerystatement.setDistinct(true);
            preparedquerystatement.appendFromTable(tableName);
            if(PRODAREV.equals(tableName))
            {
                preparedquerystatement.appendSelectColumn(tableName, BRANCHIDITERATIONINFO);
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(tableName, LATESTITERATIONINFO, "1", "="));
            } else
            if(SKUAREV.equals(tableName))
            {
                preparedquerystatement.appendFromTable(PRODAREV);
                preparedquerystatement.appendSelectColumn(PRODAREV, BRANCHIDITERATIONINFO);
                preparedquerystatement.appendJoin(PRODAREV, IDA3MASTERREFERENCE, SKUAREV, IDA3A12);
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(PRODAREV, LATESTITERATIONINFO, "1", "="));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(SKUAREV, LATESTITERATIONINFO, "1", "="));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(SKUAREV, "PLACEHOLDER", "0", "="));
            } else
            if(LCSSEASONPRODUCTLINK.equals(tableName))
                preparedquerystatement = appendSPLQuery(preparedquerystatement);
            else
            if(LCSMOAOBJECT.equals(tableName))
            {
                preparedquerystatement.appendFromTable(PRODAREV);
                preparedquerystatement.appendFromTable(FLEXTYPE);
                preparedquerystatement.appendFromTable(SKUAREV);
                preparedquerystatement.appendSelectColumn(PRODAREV, BRANCHIDITERATIONINFO);
                preparedquerystatement.appendJoin(SKUAREV, IDA3MASTERREFERENCE, LCSMOAOBJECT, IDA3A5);
                preparedquerystatement.appendJoin(FLEXTYPE, TYPEIDPATH, LCSMOAOBJECT, FLEXTYPEIDPATH);
                preparedquerystatement.appendJoin(PRODAREV, IDA3MASTERREFERENCE, SKUAREV, IDA3A12);
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(FLEXTYPE, TYPENAME, UPCMOA_FLEXTYPE, "="));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(PRODAREV, LATESTITERATIONINFO, "1", "="));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(SKUAREV, LATESTITERATIONINFO, "1", "="));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(LCSMOAOBJECT, EFFECT_OUT_DATE, "NULL", "IS NULL"));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(tableName, moaAdoptedColumn, "1", "="));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(tableName, "DROPPED", "0", "="));
            } else
            if("LCSSOURCINGCONFIG".equals(tableName))
            {
                preparedquerystatement.appendFromTable(PRODAREV);
                preparedquerystatement.appendFromTable("LCSSourcingConfigMaster");
                preparedquerystatement.appendSelectColumn(PRODAREV, BRANCHIDITERATIONINFO);
                preparedquerystatement.appendJoin("LCSSourcingConfig", PRODUCTAREVID, PRODAREV, BRANCHIDITERATIONINFO);
                preparedquerystatement.appendJoin("LCSSourcingConfig", IDA3MASTERREFERENCE, "LCSSourcingConfigMaster", "IDA2A2");
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(PRODAREV, LATESTITERATIONINFO, "1", "="));
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria("LCSSourcingConfig", LATESTITERATIONINFO, "1", "="));
            }
            preparedquerystatement.appendAndIfNeeded();
            preparedquerystatement.appendCriteria(new Criteria(tableName, MODIFY_STAMP_A2, currentrunDate, "<="));
            if(lastRunTimeStamp != null)
            {
                Date date = simpledateformat.parse(lastRunTimeStamp);
                preparedquerystatement.appendAndIfNeeded();
                preparedquerystatement.appendCriteria(new Criteria(tableName, MODIFY_STAMP_A2, date, ">"));
            }
            LOGGER.info(preparedquerystatement);
            SearchResults searchresults = LCSQuery.runDirectQuery(preparedquerystatement);
            Vector<?> vector = searchresults.getResults();
            Iterator<?> iterator = vector.iterator();
            //int i = 0;
            FlexObject flexobject;
            for(; iterator.hasNext(); objectIdCol.add(flexobject.getData("PRODAREV.BRANCHIDITERATIONINFO")))
               
            	{
            	//i++;
            	flexobject = (FlexObject)iterator.next();
            	//if(i==10)
            		//break;
            	}

        
	} catch (WTException e) {
		LOGGER.error("Error : " + e.fillInStackTrace());
	} catch (ParseException e) {
		LOGGER.error("Error : " + e.fillInStackTrace());
	}
        return objectIdCol;
    }
	/**
	 * This method is used to append the spl query.
	 * @param pqs is PreparedQueryStatement object
	 * @return PreparedQueryStatement type
	 */
    public static PreparedQueryStatement appendSPLQuery(PreparedQueryStatement preparedquerystatement)
    {
        preparedquerystatement.appendFromTable(PRODAREV);
        preparedquerystatement.appendFromTable(LCSPRODUCT);
        preparedquerystatement.appendSelectColumn(PRODAREV, "BRANCHIDITERATIONINFO");
        preparedquerystatement.appendJoin(PRODAREV, "BRANCHIDITERATIONINFO", LCSPRODUCT, PRODUCTAREVID);
        preparedquerystatement.appendJoin(LCSPRODUCT, PRODUCTAREVID, LCSSEASONPRODUCTLINK, PRODUCTAREVID);
        preparedquerystatement.appendAndIfNeeded();
        preparedquerystatement.appendCriteria(new Criteria(LCSSEASONPRODUCTLINK, "EFFECTLATEST", "1", "="));
        preparedquerystatement.appendAndIfNeeded();
        preparedquerystatement.appendCriteria(new Criteria(LCSPRODUCT, LATESTITERATIONINFO, "1", "="));
        preparedquerystatement.appendAndIfNeeded();
        preparedquerystatement.appendCriteria(new Criteria(PRODAREV, LATESTITERATIONINFO, "1", "="));
        preparedquerystatement.appendAndIfNeeded();
        preparedquerystatement.appendCriteria(new Criteria(LCSSEASONPRODUCTLINK, EFFECT_OUT_DATE, "NULL", "IS NULL"));
        preparedquerystatement.appendAndIfNeeded();
        preparedquerystatement.appendCriteria(new Criteria(LCSSEASONPRODUCTLINK, "SEASONREMOVED", "0", "="));
        return preparedquerystatement;
    }



}
