package com.agron.wc.season;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.util.FormatHelper;


public class AgronSeasonSortHelper {

	public static final Logger logger = LogManager.getLogger(AgronSeasonSortHelper.class);
	//SeasonProductHeader
	public static Vector sortedSeasonOrder(HashMap table) {
		logger.debug("sortedSeasonOrder START>>>>");
		logger.debug("table>>>"+table);
		Vector sortedKeys=new Vector();
		try {
			if(table !=null && table.size() !=0) {
				HashMap reversed = new HashMap();
				Iterator keys = table.keySet().iterator();
				String key = "";
				String seasonName="";
				while (keys.hasNext()) {
					key = (String) keys.next();
					seasonName= (String)table.get(key);
				    reversed.put(getSortableSeasonName(seasonName), key);
				}
				logger.debug("reversed MAP>>>>"+reversed);
				Vector values=new Vector(reversed.keySet());
				Collections.sort(values,Collections.reverseOrder());
				logger.debug("Sorted values>>>>"+values);
				Iterator sortedValuesIterator = values.iterator();
				while (sortedValuesIterator.hasNext()) {
					sortedKeys.add(reversed.get(sortedValuesIterator.next()));
				}
				logger.debug("sortedKeys>>>>"+sortedKeys);	
				logger.debug("sortedSeasonOrder END>>>>");	
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		return sortedKeys;
	}


	public static String getSortableSeasonName(String seasonName) {
		if(FormatHelper.hasContent(seasonName)) {
			int firstIndexOfSpace= seasonName.indexOf(" ");
			String quarter="";
			if(seasonName.contains("Spring") && !seasonName.contains("Summer")) {
				quarter="Q1";
			}else  if(seasonName.contains("Spring") && seasonName.contains("Summer")) {
				quarter="Q2";
			}
			else if(seasonName.contains("Summer") && !seasonName.contains("Spring")) {
				quarter="Q3";
			}else if(seasonName.contains("Fall") && !seasonName.contains("Winter")) {
				quarter="Q4";
			}else if(seasonName.equals("2021 Fall / Winter")) {
				quarter="Q6";
			}else if(seasonName.contains("Fall") && seasonName.contains("Winter")) {
				quarter="Q5";
			}else if(seasonName.contains("Winter") && !seasonName.contains("Fall")) {
				quarter="Q7";
			}
			seasonName= seasonName.substring(0,firstIndexOfSpace)+quarter+seasonName.substring(firstIndexOfSpace+1);	            
		}
		return seasonName;
	}
}
