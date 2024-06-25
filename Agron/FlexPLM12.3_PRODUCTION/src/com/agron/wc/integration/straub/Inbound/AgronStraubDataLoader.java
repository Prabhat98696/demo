package com.agron.wc.integration.straub.Inbound;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.agron.wc.integration.straub.outbound.AgronStraubDataProcessor;
import com.lcs.wc.util.LCSProperties;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class AgronStraubDataLoader {
 
	private static final Logger log = LogManager.getLogger("com.agron.wc.integration.straub.inbound");
	String loadType = LCSProperties.get("com.agron.wc.integration.straub.Inbound.loadType");
	/**
	 * load - This Method Parses user input Excel Data file and Loads
	 * @param user
	 * @param dataFileName
	 * @param loadType
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public String load(WTUser user, String dataFileName, boolean blankValue) throws WTException{
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
			AgronStraubInboundExcelParser agronExcelParser = new AgronStraubInboundExcelParser();
			agronExcelParser.setVariables(user, dataFileName, loadType);
			 message = agronExcelParser.parseAndLoad(blankValue);
		}catch (WTException wt) {
			wt.printStackTrace();
			log.error("Error in Parsing Data File. "+wt.getLocalizedMessage());
			message = "Error in Parsing Data File";
		}catch (Exception e) {
			e.printStackTrace();
			log.error("Error in Loading Data. "+e.getLocalizedMessage());
			message = "Error in Loading Data";
		}
		return message;
	}
}
