package com.agron.wc.sample;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.flextype.FlexType;

import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialColorLogic;
import com.lcs.wc.material.LCSMaterialSupplierMaster;
import com.lcs.wc.material.LCSMaterialSupplierQuery;
import com.lcs.wc.measurements.LCSFitTest;
import com.lcs.wc.measurements.LCSFitTestQuery;
import com.lcs.wc.measurements.LCSMeasurements;
import com.lcs.wc.measurements.LCSMeasurementsLogic;
import com.lcs.wc.measurements.LCSMeasurementsMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleClientModel;
import com.lcs.wc.sample.LCSSampleLogic;
import com.lcs.wc.sample.LCSSampleRequest;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;



/**
 * @author ACNOVATE
 * Class moved for FLexPLM upgrade to 11.1. Fox release 
 * May 2020
 * 
 */
public final class AgronSampleRecreation {
	/**
	 * Default Constructor.
	 */
	private AgronSampleRecreation() {

	}

	/** AGR_SAMPLE_STATUS_KEY read from property entry. */
	public static final String AGR_SAMPLE_STATUS_KEY = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.SampleStatus");
	/** AGR_LABDIP_STATUS_KEY read from property entry. */
	public static final String AGR_LABDIP_STATUS_KEY = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.LabDipStatus");
	/** AGR_STATUS_INPROCESS read from property entry. */
	public static final String AGR_STATUS_INPROCESS = LCSProperties
			.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.StatusInProcess");
	
	public static final String AGR_STATUS_APPROVED = LCSProperties.get("com.agron.wc.sample.LCSSample.AgronSamplePlugin.StatusApproved");
	
	public static final String PRODUCT_FIT_SAMPLE_TYPE_NAME = LCSProperties	.get("com.lcs.wc.sample.LCSSample.Product.Fit.Root");
	public static final Logger logger = LogManager.getLogger(AgronSampleRecreation.class);
	public static final boolean DEBUG = LCSProperties.getBoolean("com.agron.wc.sample.AgronSamplePlugin.debug");
	
	/**
	 * @param obj - WTObject.
	 * @throws WTException the WTException.
	 * @throws WTPropertyVetoException the WTPropertyVetoException.
	 */
	public static void recreateSample(WTObject obj) throws WTException,WTPropertyVetoException {
			/*
			 * Type Casting the Generic Object to Sample Object
			 */
	
		if(obj instanceof LCSSample){
			try {
				LCSSample sampleObj = (LCSSample) obj;
				logger.debug("Class:AgronSampleRecreation:"+sampleObj);
				LCSSample preVersionSample = (LCSSample) VersionHelper.getPreSavePersistable(sampleObj);
				if(!(preVersionSample !=null && "agrRejected".equalsIgnoreCase((String)preVersionSample.getValue(AGR_SAMPLE_STATUS_KEY)))) {
					//Checking the Status of Sample
					if("agrRejected".equalsIgnoreCase((String)sampleObj.getValue(AGR_SAMPLE_STATUS_KEY))){
						if(DEBUG)logger.debug("recreateSample START");
						// Obtaining Product from Sample
						LCSProduct product =(LCSProduct) VersionHelper.latestIterationOf(sampleObj.getOwnerMaster());
						if(DEBUG)logger.debug("product:"+product+"::"+product.getName());
						// Obtaining source from Sample
						LCSSourcingConfig source = (LCSSourcingConfig)VersionHelper.latestIterationOf((LCSSourcingConfigMaster)sampleObj.getSourcingMaster());
						if(DEBUG)logger.debug("source:"+source+"::"+source.getName());
						// Obtaining spec from Sample
						FlexSpecification spec = (FlexSpecification) VersionHelper.latestIterationOf(sampleObj.getSpecMaster());
						if(DEBUG)logger.debug("spec:"+spec+"::"+spec.getName());
						LCSSampleLogic sampleLogic = new LCSSampleLogic();
						//Recreating Sample
						Long matSampleSequenceValue = (Long)sampleObj.getValue("sampleSequence");
						if(DEBUG)logger.debug("matSampleSequenceValue:"+matSampleSequenceValue);
						LCSSample reincarnatedSample = copySample(sampleObj, spec, source, product);
						//Changing the Status of Sample
						reincarnatedSample.setValue(AGR_SAMPLE_STATUS_KEY,"agrInProcess");
						reincarnatedSample.setValue("sampleSequence",matSampleSequenceValue+1);					
						reincarnatedSample.setValue("comments","");				
						DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");    
						Date date = new Date(); 				
						LCSSampleRequest sampleReq = (LCSSampleRequest)reincarnatedSample.getSampleRequest();
						if(DEBUG)logger.debug("sampleReq:"+sampleReq);
						sampleReq.setValue("sampleRequestRequestDate", dateFormat.format(date).toString().trim());
						//Saving the Sample
						sampleLogic.saveSample(reincarnatedSample,sampleReq);
						if(DEBUG)logger.debug("recreateSample END");
					}
				}
			} catch(Exception e) {
				logger.error("Error while recreate Product Sample"+e.getMessage());
				e.printStackTrace();
			}
		}	

		
	}
	
	public static void recreateSampleMaterial(WTObject obj) throws WTException,WTPropertyVetoException {
		if(obj instanceof LCSSample){
			try {
				logger.info("recreateSampleMaterial START>>>>"+obj);
				LCSSample sampleObj = (LCSSample) obj;
				LCSSample preVersionSample = (LCSSample) VersionHelper.getPreSavePersistable(sampleObj);
				String sampleStatus = (String) sampleObj.getValue(AGR_SAMPLE_STATUS_KEY);
				logger.info("sampleStatus>>"+sampleStatus+"::preVersionSample>>"+preVersionSample);
				if("agrRejected".equalsIgnoreCase(sampleStatus) && !(preVersionSample !=null && "agrRejected".equalsIgnoreCase((String)preVersionSample.getValue(AGR_SAMPLE_STATUS_KEY)))) {
					// Obtaining Product from Sample
					logger.info("Logic for recreate material sample START>>>>");
					String sampleOID = (String)FormatHelper.getObjectId(sampleObj);  
					LCSMaterialColor materialColor = (LCSMaterialColor) sampleObj.getColor();
					LCSSampleClientModel  sampleModel = new LCSSampleClientModel();
					FlexType sampleFlexType = null;
					sampleModel.load(sampleOID);
					//WTPrincipalReference wtPrincipalReference = null;
					//wtPrincipalReference = sampleObj.getCreator();
					//WTUser creator =(WTUser) wtPrincipalReference.getPrincipal();
					LCSSample sample = LCSSample.newLCSSample();
					LCSSampleRequest sampleReq = (LCSSampleRequest)sampleObj.getSampleRequest();	
					sampleFlexType = (FlexType) sampleReq.getFlexType();
					Long matSampleSequenceValue = (Long)sampleObj.getValue("sampleSequence");
					sample.setFlexType(sampleFlexType);
					sample.setOwnerMaster(materialColor.getMaterialMaster());
					LCSMaterialSupplierMaster matSupMaster = (LCSMaterialSupplierMaster)LCSMaterialSupplierQuery.findMaterialSupplier(materialColor.getMaterialMaster(), materialColor.getSupplierMaster()).getMaster();
					sample.setSourcingMaster(matSupMaster); 
					sample.setSampleRequest(sampleReq);
					sample.setValue(AGR_SAMPLE_STATUS_KEY,"agrInProcess");
					sample.setValue("sampleSequence",matSampleSequenceValue+1);
					sample.setValue("comments","");
					DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");    
					Date date = new Date(); 
					sampleReq.setValue("sampleRequestRequestDate", dateFormat.format(date).toString().trim()); 
					sample.setColor(sampleObj.getColor());
					logger.info("saveSample to Database>>sample::"+sample+"::sampleReq>>"+sampleReq+"::matSupMaster>>>"+matSupMaster+"::matSampleSequenceValue>>"+matSampleSequenceValue);
					sample = new LCSSampleLogic().saveSample(sample);
					logger.info("Logic for recreate material sample END>>>>");
				}else {
					if("Color Development".equals(sampleObj.getFlexType().getTypeDisplayName())) {
						applyLabdipStatusInformation(sampleObj,sampleStatus);	
					}
				}

			}catch(Exception e) {
				logger.error("Error in recreateSampleMaterial:"+e.getMessage()+"::Sample>>"+obj);
				e.printStackTrace();	
			}
			logger.info("recreateSampleMaterial END>>>>"+obj);
		}
}
	
	
	public static LCSSample copySample(LCSSample oldSample, FlexSpecification destSpec, LCSSourcingConfig destSource,
			LCSProduct destProduct) throws WTException {
		try {
			if(DEBUG)logger.debug("copySample START>>>"+oldSample);
			LCSSampleLogic sampleLogic = new LCSSampleLogic();
			LCSSample newSample = LCSSample.newLCSSample();
			oldSample.copyState(newSample);
			newSample.setSourcingMaster(destSource.getMaster());
			newSample.setSpecMaster(destSpec.getMaster());
			newSample.setSourcingMasterReference(destSource.getMasterReference());
			newSample.setSpecMasterReference(destSpec.getMasterReference());
			newSample.setOwnerMaster(destProduct.getMaster());
			newSample.setOwnerMasterReference(destProduct.getMasterReference());
			newSample.setCopiedFrom(oldSample);
			if(DEBUG)logger.debug("LCSSampleRequest  Copy");
			LCSSampleRequest newSampleRequest = sampleLogic.copySampleRequest(oldSample.getSampleRequest(), destSpec,destSource, destProduct);
			if(DEBUG)logger.debug("LCSSampleRequest newSampleRequest::"+newSampleRequest);
			newSample.setSampleRequest(newSampleRequest);
			newSample = (LCSSample) sampleLogic.save(newSample);
			if(DEBUG)logger.debug("sampleLogic.save DONE>>>"+newSample);
			if (newSample.getFlexType().getFullName(true).startsWith(PRODUCT_FIT_SAMPLE_TYPE_NAME)) {
				LCSMeasurementsLogic logic = new LCSMeasurementsLogic();
				Collection fitTestCollection = LCSFitTestQuery.findFitTests(oldSample);
				if(DEBUG)logger.debug("copyFitTest START:: fitTestCollection::"+fitTestCollection);
				Iterator fitTestCollectionItr = fitTestCollection.iterator();
				while (fitTestCollectionItr.hasNext()) {
					LCSFitTest current = (LCSFitTest) fitTestCollectionItr.next();
					if(DEBUG)logger.debug("LCSFitTest Copy START>>>"+current);
					LCSMeasurementsMaster currentMeasurementMaster = current.getMeasurementsMaster();
					if(currentMeasurementMaster !=null) {
						LCSMeasurements currentMeasurement = (LCSMeasurements) VersionHelper.latestIterationOf(currentMeasurementMaster);
						if(DEBUG)logger.debug("currentMeasurement::"+currentMeasurement+":::"+currentMeasurement.getName());
						if(currentMeasurement !=null) {
							//logic.createFitTest(wtpve, currentMeasurement, current.getSampleSize());
							logic.copyFitTest(current, newSample, currentMeasurement);
							if(DEBUG)logger.debug("LCSFitTest Copy END>>>"+current);
						}
					}
					
				}
				if(DEBUG)logger.debug("copyFitTest END");
			}
			if(DEBUG)logger.debug("copySample END:: New Sample : "+newSample);
			return newSample;
		} catch (WTPropertyVetoException ex) {
			ex.printStackTrace();
			throw new WTException(ex);
		}
	}
	
	

	public static void applyLabdipStatusInformation(LCSSample sampleObj,String sampleStatus) {
		logger.info("ApplyLabdipStatusInformation START>>>");
		try {
			boolean persist=false;
			LCSMaterialColor materialColor = (LCSMaterialColor) sampleObj.getColor();
			logger.info("LCSMaterialColor Before Refresh>>>"+materialColor+"::status>>"+materialColor.getValue(AGR_LABDIP_STATUS_KEY));
			materialColor=(LCSMaterialColor) PersistenceHelper.manager.refresh(materialColor);
			logger.info("LCSMaterialColor After Refresh>>>"+materialColor+"::status>>"+materialColor.getValue(AGR_LABDIP_STATUS_KEY));
			
			String materialColorLabDipStatus=(String)materialColor.getValue(AGR_LABDIP_STATUS_KEY);
			logger.info("sampleStatus>>"+ sampleStatus+"::materialColorLabDipStatus>>"+materialColorLabDipStatus+"::materialColor>>"+materialColor+"::sampleObj>>"+sampleObj);
			if (AGR_STATUS_APPROVED.equals(sampleStatus)) {
				logger.info("Set  Approved State>>>>>>>>>>");
				if(!AGR_STATUS_APPROVED.equalsIgnoreCase(materialColorLabDipStatus)) {
					materialColor.setValue(AGR_LABDIP_STATUS_KEY, sampleStatus);
					persist=true;   
				}
			}else if(!AGR_STATUS_INPROCESS.equalsIgnoreCase(materialColorLabDipStatus)) {
				logger.info("Set  INPROCESS State>>>>>>>>>>");
				materialColor.setValue(AGR_LABDIP_STATUS_KEY,AGR_STATUS_INPROCESS);
				persist=true;	
			}
			/*
			 * Saving the Material Color Object to Database
			 */
			if(persist) {
				logger.info("saveMaterialColor START>>>>STATUS::"+materialColor.getValue(AGR_LABDIP_STATUS_KEY));
				LCSMaterialColorLogic mcl = new LCSMaterialColorLogic();
				mcl.saveMaterialColor(materialColor);	
				logger.info("saveMaterialColor END>>>>");
			}
		}
		catch (Exception e) {
			logger.error("Error while ApplyLabdipStatusInformation:"+e.getMessage()+"::sampleObj>>"+sampleObj);
			e.printStackTrace();
		} 
		logger.info("ApplyLabdipStatusInformation END>>>");
	}
	
	}

