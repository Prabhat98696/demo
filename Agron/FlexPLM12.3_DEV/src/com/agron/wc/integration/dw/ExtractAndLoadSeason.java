package com.agron.wc.integration.dw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.agron.wc.integration.dw.model.PTCSeason;
import com.agron.wc.integration.dw.util.HibernateUtil;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.FlexObjectSorter;

public class ExtractAndLoadSeason {
	
	public static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.dw");

	public ArrayList<LCSSeason>  processSeason() throws Exception{

		ArrayList<LCSSeason> seasonList = new ArrayList<LCSSeason>();

		boolean txState = false;
		Session session = null;
		try {

			// Get Data from Flex
			LCSSeasonQuery seasonQuery = new LCSSeasonQuery();
			SearchResults activeSeasonsList =  seasonQuery.findActiveSeasons();
			FlexObjectSorter objSorter = new FlexObjectSorter();
			List<String> sortList = new ArrayList<String>();
			sortList.add("LCSSEASON.PTC_TMS_1TYPEINFOLCSSEASON:DESC"); // Sort in a way toprocess the latest season the last
			Collection<FlexObject> sortedSeasons = objSorter.sortFlexObjects(activeSeasonsList.getResults(), sortList);
			// Load them to SQL Table
			Iterator<FlexObject> activeSeasonsListItr = sortedSeasons.iterator();
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			FlexType seasonType = null;
			FlexTypeAttribute att = null;
			while(activeSeasonsListItr.hasNext()) {

				FlexObject obj = activeSeasonsListItr.next();

				long ida2a2 = obj.getLong("LCSSEASON.IDA2A2");
				LCSSeason season =  (LCSSeason) LCSQuery.findObjectById("OR:com.lcs.wc.season.LCSSeason:"+ida2a2);
				if(season != null) {
					
					// SKIP this season if Downstream Integration is disabled.
					String isagrDownstreamIntegrated = (String) season.getValue("agrDownstreamIntegrated");
					if("agrNo".equals(isagrDownstreamIntegrated)) {
						LOGGER.info("isagrDownstreamIntegrated = false :  Skipping Season " + season.getName());
						continue;
					}
									
					String flextypeName = season.getFlexType().getFullNameDisplay();
					if(!flextypeName.contains("Agron")) {
						continue; // No need to load non Agron Type seasons.
					}
					
					String seasonYear = (String) season.getValue("year");
					if(seasonType == null  ) {
						seasonType = season.getFlexType();
						att = seasonType.getAttribute("year");
					}

					String SeasonYear = att.getDisplayValue(seasonYear);
					PTCSeason _season = new PTCSeason();
					_season.setSeasonName(season.getName());
					_season.setPTC_SeasonKey(Long.valueOf(season.getBranchIdentifier()).intValue());
					_season.setSeasonYear(SeasonYear);
					session.save(_season);
				
					seasonList.add(season);
				}
			}	


			Query<PTCSeason> q = session.createQuery("From PTC_Season_Staging", PTCSeason.class);
			List<PTCSeason> resultList = q.list();
			LOGGER.info("Seasons Loaded " + resultList.size());
			session.getTransaction().commit();


			txState = true;
		} catch (HibernateException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}finally {

			if(txState == false && session != null) {
				session.getTransaction().rollback();
				if(session.isOpen()){
						session.close();
					}
			}
		}
		
		return seasonList; // List of seasons to process.

	}

}


