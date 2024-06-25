package com.agron.wc.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialColorLogic;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;
import wt.util.WTException;

/**
 * @author ACNOVATE
 * Class moved for FLexPLM upgrade to 11.1. Fox release 
 * May 2020
 * 
 */
public final class AgronSamplePlugin {
	/**
	 * Default Constructor.
	 */
	private AgronSamplePlugin() {

	}

	/** AGR_SAMPLE_STATUS_KEY read from property entry. */
	public static final String AGR_SAMPLE_STATUS_KEY = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.SampleStatus");
	/** AGR_LABDIP_STATUS_KEY read from property entry. */
	public static final String AGR_LABDIP_STATUS_KEY = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.LabDipStatus");
	/** AGR_STATUS_APPROVED read from property entry. */
	public static final String AGR_STATUS_APPROVED = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.StatusApproved");
	/** AGR_STATUS_INPROCESS read from property entry. */
	public static final String AGR_STATUS_INPROCESS = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.StatusInProcess");
	/** AGR_CHECK read from property entry. */
	public static final Boolean AGR_CHECK = LCSProperties
			.getBoolean("com.agron.wc.sample.LCSSample.AgronSamplePlugin.Check");
	/** AGR_LIFECYCLE_APPROVED read from property entry. */
	public static final String AGR_LIFECYCLE_APPROVED = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.LifecycleApproved");
	/** AGR_LIFECYCLE_INPROCESS read from property entry. */
	public static final String AGR_LIFECYCLE_INPROCESS = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.LifecycleInProcess");
	
	public static final Logger logger = LogManager.getLogger(AgronSamplePlugin.class);


	/**
	 * @param obj - WTObject.
	 */
	public static void applyLabdipStatusInformation(WTObject obj) {
        if(obj instanceof LCSSample ) {
        	try {
        		logger.debug("Class:AgronSamplePlugin");
    			LCSSample sampleObj = (LCSSample) obj;
    			// Obtaining Material Color from Sample
    			LCSMaterialColor materialColor = (LCSMaterialColor) sampleObj.getColor();
    			// Getting Sample Status Attribute Value
    			String sampleStatus = (String) sampleObj.getValue(AGR_SAMPLE_STATUS_KEY);
    			logger.debug("<<<<<<<<<Sample Status:" + sampleStatus);

    			/*
    			 * Checking the Required Condition
    			 */
    			if (AGR_STATUS_APPROVED.equals(sampleStatus)) {
    				logger.debug("<<<<<<<<<<Entering Approved State>>>>>>>>>>");
    				// Setting the Value of Material Color
    				materialColor.setValue(AGR_LABDIP_STATUS_KEY, sampleStatus);
    				/*
    				 * Setting the Value of LifeCycle State
    				 */
    		/*		if (AGR_CHECK) {
    					LCSLog.debug("<<<<<<<<Setting the value of LifeCycle to Approved");
    					WfEngineHelper.service
    							.terminateObjectsRunningWorkflows(materialColor);
    					wt.lifecycle.State state = wt.lifecycle.State
    							.toState(AGR_LIFECYCLE_APPROVED);
    					LifeCycleHelper.service.setLifeCycleState(materialColor,
    							state, true);
    				}*/

    			}else{
    				logger.debug("<<<<<<<<<<Entering Other the approved State");
    				materialColor.setValue(AGR_LABDIP_STATUS_KEY,
    						AGR_STATUS_INPROCESS);
    				/*
    				 * Setting the Value of LifeCycle State
    				 */
    			/*	if (AGR_CHECK) {
    					LCSLog.debug("<<<<<<<<Setting the value of LifeCycle to In Process");
    					WfEngineHelper.service
    							.terminateObjectsRunningWorkflows(materialColor);
    					wt.lifecycle.State state = wt.lifecycle.State
    							.toState(AGR_LIFECYCLE_INPROCESS);
    					LifeCycleHelper.service.setLifeCycleState(materialColor,
    							state, true);
    				}*/
    			}
    			/*
    			 * Saving the Material Color Object to Database
    			 */
    			LCSMaterialColorLogic mcl = new LCSMaterialColorLogic();
    			mcl.saveMaterialColor(materialColor);
    		}
    		catch (WTException e) {
    			logger.debug(e.getMessage());
    		} 	
        }
		

	}

}