package com.agron.wc.load;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronDataLoader {
	public WTUser user = null;
	public String dataFileName = null;
	public String loadType = null;
	public boolean ignoreBlankValue = false;
	public static final Logger log = LogManager.getLogger(AgronDataLoader.class);
	/**
	 * setVariables -  This Method used to initialize the user, dataFileName and loadType
	 * @param user
	 * @param dataFileName
	 * @param loadType
	 */
	public void setVariables(WTUser userObj, String dataFile, String dataLoadType, boolean ignoreBlankVal){
		log.info("Inside setVariables Method");
		this.user = userObj;
		this.dataFileName = dataFile;
		this.loadType = dataLoadType;
		this.ignoreBlankValue =ignoreBlankVal;
		log.debug("userID: "+this.user.getName());
		log.debug("dataFileName: "+this.dataFileName);
		log.debug("loadType: "+this.loadType);	
		log.debug("ignoreBlankValue: "+this.ignoreBlankValue);	
	}
	/**
	 * loadData - This Method loads data Method server
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public String loadData() throws WTException{
			log.debug("loadData() - Loading Data in Method Server.");
			AgronDataLoader loader = new AgronDataLoader();
			String message = loader.load(user, dataFileName, loadType, ignoreBlankValue);
			return message;
		}

	/**
	 * load - This Method Parses user input Excel Data file and Loads
	 * @param user
	 * @param dataFileName
	 * @param loadType
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public String load(WTUser user, String dataFileName, String loadType, boolean blankValue) throws WTException{
		log.info("parsing Data File...");
		File loadFile;
		String message;
		String loadFileName = null;
		//User can not be null if user is null throw exception
		if(user == null){
			throw new WTException("load() - Error in Loading User can not be null");
		}
		try {
			log.info("parsing Data File...");
			loadFile = new File(dataFileName);
			loadFileName = loadFile.getName();
			log.debug("load() loadFileName - "+loadFileName); 
			AgronExcelParser agronExcelParser = new AgronExcelParser();
			agronExcelParser.setVariables(user, dataFileName, loadType);
			//Method call to parse the Excel and load into PLM
			 message = agronExcelParser.parseAndLoad(blankValue);
		}catch (WTException wt) {
			wt.printStackTrace();
			log.error("Error in Parsing Data File. "+wt.getLocalizedMessage());
			message = "Error in Parsing Data File";
			//DataLoadUtil.notifyDataLoadError(user, "Error in Data Load ",wt.getLocalizedMessage(), loadFileName);		
		}catch (Exception e) {
			e.printStackTrace();
			log.error("Error in Loading Data. "+e.getLocalizedMessage());
			message = "Error in Loading Data";
			//AgronDataLoadUtil.notifyDataLoadError(user, "Error in Data Load ", e.getLocalizedMessage(), loadFileName);		
		}
		return message;
	}


}
