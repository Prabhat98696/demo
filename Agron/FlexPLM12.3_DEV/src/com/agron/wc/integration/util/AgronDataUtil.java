package com.agron.wc.integration.util;

import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.*;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;
import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import wt.util.WTException;

/**
 * @author Mallikarjuna Savi
 *
 */
public class AgronDataUtil {

	/**
	 * Defined to store the constant value.
	 */
	private static final Logger LOGGER = LogManager.getLogger("com.agron.wc.integration.erp");

	public AgronDataUtil() {
	}

	/**
	 * This method is used to get the price of the attribute.
	 * 
	 * @param product is LCSProduct object
	 * @param key     is String object
	 * @param seasons is Collection object
	 * @return String type
	 */
	public String getPrice(LCSProduct product, String key, Collection<?> seasons, String dateKey,
			Collection<?> skuSeasonCol) throws ParseException, WTException {
		String price = "";
		float priceFloat = 0;
		LCSSeason season = null;
		LCSSeasonProductLink spl = null;
		String date = null;
		Date dateObj = null;
		Date currentRunDateObj = null;
		DateFormat df = null;
		List<Date> list = new ArrayList<Date>();
		Map<String, String> priceSeasonMap = new HashMap<String, String>();
		Date nearestDate = null;
		String currentRunDate = null;
		String nearest = null;

		Iterator<?> seasonIter;
		seasonIter = seasons.iterator();
		try {
			currentRunDate = AgronDataHelper.getCurrentTimeStamp();
			df = new SimpleDateFormat("dd/MM/yyyy");
			currentRunDateObj = df.parse(currentRunDate);
			LOGGER.info((new StringBuilder("Current Date : ")).append(currentRunDateObj).toString());
			if (skuSeasonCol.size() == 1) {
				LCSSeason skuSeason = (LCSSeason) VersionHelper
						.latestIterationOf((LCSSeasonMaster) skuSeasonCol.iterator().next());
				spl = LCSSeasonQuery.findSeasonProductLink(product, skuSeason);
				price = AgronCSVGenerator.getData(spl, key, false);
				LOGGER.info((new StringBuilder("price : ")).append(price).toString());
				return price;
			}
			
			/*while (seasonIter.hasNext()) {
				season = (LCSSeason) seasonIter.next();
				LOGGER.info("seasonNAME :::::::::>>"+season.getName());
				spl = LCSSeasonQuery.findSeasonProductLink(product, season);
				price = AgronCSVGenerator.getData(spl, key, false);
				if (FormatHelper.hasContent(price)) {
					priceFloat = Float.valueOf(price).floatValue();
					if (priceFloat != 0.0F) {
						date = AgronCSVGenerator.getData(spl, dateKey, false);
						if (date != null && !(date.equals(""))) {
							dateObj = df.parse(date);
							LOGGER.info((new StringBuilder()).append(dateObj).append(" : ")
									.append(currentRunDateObj.compareTo(dateObj)).toString());
							if (currentRunDateObj.compareTo(dateObj) >= 0) {
								list.add(dateObj);
								date = date.replaceAll("[-+.^:,]", "");
								priceSeasonMap.put(date, price);
							}
						}
					}
				}
			}*/
			LCSSeason latestSeason=new AgronDataHelper().getLatestFromSeasonColl(product, seasons);
			if(latestSeason == null) {return "";}
			LOGGER.info("latest Season Name :::::::::>>"+latestSeason.getName());
			spl = LCSSeasonQuery.findSeasonProductLink(product, latestSeason);
			price = AgronCSVGenerator.getData(spl, key, false);
			if (FormatHelper.hasContent(price)) {
				priceFloat = Float.valueOf(price).floatValue();
				if (priceFloat != 0.0F) {
					date = AgronCSVGenerator.getData(spl, dateKey, false);
					if (date != null && !(date.equals(""))) {
						dateObj = df.parse(date);
						LOGGER.info((new StringBuilder()).append(dateObj).append(" : ")
								.append(currentRunDateObj.compareTo(dateObj)).toString());
						if (currentRunDateObj.compareTo(dateObj) >= 0) {
							list.add(dateObj);
							date = date.replaceAll("[-+.^:,]", "");
							priceSeasonMap.put(date, price);
						}
					}
				}
			}
			LOGGER.info((new StringBuilder("priceSeasonMap : ")).append(priceSeasonMap).toString());
			if (priceFloat != 0.0F) {
				nearestDate = getDateNearest(list, currentRunDateObj);
				if (nearestDate!=null){
					nearest = df.format(nearestDate);
					nearest = nearest.replaceAll("[-+.^:,]", "");
					LOGGER.info((new StringBuilder("nearest : ")).append(nearest).toString());
					if (FormatHelper.hasContent((String) priceSeasonMap.get(nearest)))
						price = (String) priceSeasonMap.get(nearest);
					else
						price = "0";
				}else {
					price = "0";
				}
			}

			LOGGER.info((new StringBuilder("price : ")).append(price).toString());
		} catch (WTException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
		} catch (ParseException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
		}
		
		LOGGER.info("Retail Price::::>"+price);
		return price;
	}

	/**
	 * This method is used to get the season product link value of the attributes.
	 * 
	 * @param product
	 *            is LCSProduct object	
	 * @param seasons
	 *            is Collection object
	 * @param key
	 *            is String object
	 * @return String type
	 */
	public String getSPLValue(LCSProduct product, Collection<?> seasons, String key) {
		LCSSeason season = null;
		LCSSeasonProductLink spl = null;
		String splValue = null;
		try {
			for (Iterator<?> seasonIter = seasons.iterator(); seasonIter.hasNext();) {
				season = (LCSSeason) seasonIter.next();
				spl = LCSSeasonQuery.findSeasonProductLink(product, season);
				if (spl != null)
					splValue = AgronCSVGenerator.getData(spl, key, false);
				
			}

		} catch (WTException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
		}
		return splValue;
	}
	
	/**
	 * This method is used to get the season product link value of the attributes.
	 * 
	 * @param product
	 *            is LCSProduct object	
	 * @param seasons
	 *            is Collection object
	 * @param key
	 *            is String object
	 * @return String type
	 */
	public String getSPLinkValue(LCSProduct product, LCSSeason season, String key) {
		
		LCSSeasonProductLink spl = null;
		String splValue = null;
		try {

				spl = LCSSeasonQuery.findSeasonProductLink(product, season);
				if (spl != null)
					splValue = AgronCSVGenerator.getData(spl, key, false);
				
			}

		 catch (WTException e) {
			LOGGER.error("Error : " + e.fillInStackTrace());
		}
		return splValue;
	}
	/**Commented this code on 1/10/2020, because of Date sorting issue, and used the below method
	 * This method is used to get the nearest date from the list to the given
	 * date.
	 * 
	 * @param dates
	 *            is List object
	 * @param targetDate
	 *            is Date object
	 * @return Date type
	 */
	/*private Date getDateNearest(List<Date> dates, Date targetDate) {
		Date returnDate = targetDate;
		for (Iterator<Date> iterator = dates.iterator(); iterator.hasNext();) {
			Date date = (Date) iterator.next();
			if (date.compareTo(returnDate) <= 0 || date.compareTo(targetDate) <= 0)
				returnDate = date;
		}

		return returnDate;
	}*/


public static Date getDateNearest(Collection<Date> unsortedDates, Date originalDate) {
	    List<Date> dateList = new LinkedList<Date>(unsortedDates);
		LOGGER.info(":::dateList::::"+dateList);
		LOGGER.info(":::originalDate::::"+originalDate);
	    Collections.sort(dateList);
	    Iterator<Date> iterator = dateList.iterator();
	    Date previousDate = null;
	    while (iterator.hasNext()) {
	        Date nextDate = iterator.next();
	        if (nextDate.before(originalDate)) {
	            previousDate = nextDate;
	            continue;
	        } else if (nextDate.after(originalDate)) {
	            if (previousDate == null || isCloserToNextDate(originalDate, previousDate, nextDate)) {
	                return nextDate;
	            }
	        } else {
	            return nextDate;
	        }
	    }
	    LOGGER.info(":::getDateNearest::::"+previousDate);
	    return previousDate;
	}
	
	private static boolean isCloserToNextDate(Date originalDate, Date previousDate, Date nextDate) {
	    if(previousDate.after(nextDate))
	        throw new IllegalArgumentException("previousDate > nextDate");
	    return ((nextDate.getTime() - previousDate.getTime()) / 2 + previousDate.getTime() <= originalDate.getTime());
	}


}
