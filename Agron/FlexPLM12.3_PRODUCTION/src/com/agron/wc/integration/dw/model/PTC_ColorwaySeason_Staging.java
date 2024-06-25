/**
 * 
 */
package com.agron.wc.integration.dw.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.agron.wc.integration.dw.DWUtil;

/**
 * @author Acnovate
 *
 */
@Entity
@Table(name = "PTC_ColorwaySeason_Staging")

public class PTC_ColorwaySeason_Staging {

	@Id
	@Column
	private int PTC_ColorwaySeasonKey;

	@Column
	private int PTC_ColorwayUniqueID;

	@Column
	private int PTC_SeasonKey;

	@Column(name = "AvailabilityDate", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date AvailabilityDate;
	
	@Column (length =3)
	private String BaseColor = "";
	
	
	@Column (length =25)
	private String Capsule = "";
	
	@Column (length =15)
	private String CarryoverColorway = "";
	
	@Column (length =150)
	private String Catalogs = "";
	@Column (length =50)
	private String ColorwayStatus = "";
	
	@Column(name = "ConsolidatedBuyDate1", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date ConsolidatedBuyDate1;
	
	@Column(name = "ConsolidatedBuyDate2", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date ConsolidatedBuyDate2;
	
	@Column(name = "ConsolidatedBuyDate3", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date ConsolidatedBuyDate3;
	
	@Column(name = "ConsolidatedBuyDate4", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date ConsolidatedBuyDate4;
	
	@Column(name = "DateDropped", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date DateDropped;
	
	@Column (length =3)
	private String IsSMU = "";
	
	@Column
	private Integer MinimumOrderQuantity;
	
	@Column (length =50)
	private String PrimaryRootColor = "";
	
	@Column (length =3)
	private String ReqConsolidation = "";
	
	
	@Column (length =100)
	private String SMUAccount = "";
	
	@Column (length =100)
	private String Sustainable = "";
	
	@Column (length =3)
	private String SMUSpecialHandling = "";
	
	@Column 
	private Integer Forecast;
	
	@Column(length =50)
	private String BagsOnMannequin; 

	@Column(columnDefinition="BIT")
	private Boolean B2Bonly = false; 

	@Column(length =30)
	private String CatalogImageStatus; 
	
	@Column (length =100)
	private String DhlAgron = "";
	
	@Column (length =100)
	private String DhlStraub = "";
	@Column (length =30)
	private String LaydownImageStatus = "";
	@Column (length =30)
	private String MannequinImageStatus = "";
	@Column (length =30)
	private String OnModelImageStatus = "";

	@Column(columnDefinition="BIT")
	private Boolean Priority = false;
	
	@Column (length =200)
	private String PriorityComments = "";
	@Column(columnDefinition="BIT")
	private Boolean ProductionCancelled = false;
	
	@Column (length =30)
	private String ProductReceiptStatus = "";

	@Column(name = "ProductReceiptStatusDate", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date ProductReceiptStatusDate;
	
	@Column(name = "SampleEtaToStraub", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date SampleEtaToStraub;

	@Column (length =1000)
	private String SpecialPhotographyDirections = "";
	
	@Column(columnDefinition="BIT")
	private Boolean ReturnSampleToAgron = false;

	@Column(columnDefinition="BIT")
	private Boolean StraubImageCompletion = false;
	


	//////////////////Getters and Setters  ////////////////////

	public int getPTC_ColorwaySeasonKey() {
		return PTC_ColorwaySeasonKey;
	}

	public String getBagsOnMannequin() {
		return BagsOnMannequin;
	}

	public void setBagsOnMannequin(String bagsOnMannequin) {
		BagsOnMannequin = bagsOnMannequin;
	}


	public Date getSampleEtaToStraub() {
		return SampleEtaToStraub;
	}

	public void setSampleEtaToStraub(Date sampleEtaToStraub) {
		SampleEtaToStraub = sampleEtaToStraub;
	}

	public void setPTC_ColorwaySeasonKey(int pTC_ColorwaySeasonKey) {
		PTC_ColorwaySeasonKey = pTC_ColorwaySeasonKey;
	}

	public int getPTC_ColorwayUniqueID() {
		return PTC_ColorwayUniqueID;
	}

	public void setPTC_ColorwayUniqueID(int pTC_ColorwayUniqueID) {
		PTC_ColorwayUniqueID = pTC_ColorwayUniqueID;
	}

	public int getPTC_SeasonKey() {
		return PTC_SeasonKey;
	}

	public void setPTC_SeasonKey(int pTC_SeasonKey) {
		PTC_SeasonKey = pTC_SeasonKey;
	}

	public Date getAvailabilityDate() {
		return AvailabilityDate;
	}

	public void setAvailabilityDate(Date availabilityDate) {
		AvailabilityDate = availabilityDate;
	}

	public String getBaseColor() {
		return BaseColor;
	}

	public void setBaseColor(String baseColor) {
		BaseColor = baseColor;
	}

	public String getCapsule() {
		return Capsule;
	}

	public void setCapsule(String capsule) {
		Capsule = capsule;
	}

	public String getCarryoverColorway() {
		return CarryoverColorway;
	}

	public void setCarryoverColorway(String carryoverColorway) {
		CarryoverColorway = carryoverColorway;
	}

	public String getCatalogs() {
		return Catalogs;
	}

	public void setCatalogs(String catalogs) {
		Catalogs = catalogs;
	}

	public String getColorwayStatus() {
		return ColorwayStatus;
	}

	public void setColorwayStatus(String colorwayStatus) {
		ColorwayStatus = colorwayStatus;
	}

	public Date getConsolidatedBuyDate1() {
		return ConsolidatedBuyDate1;
	}

	public void setConsolidatedBuyDate1(Date consolidatedBuyDate1) {
		ConsolidatedBuyDate1 = consolidatedBuyDate1;
	}

	public Date getConsolidatedBuyDate2() {
		return ConsolidatedBuyDate2;
	}

	public void setConsolidatedBuyDate2(Date consolidatedBuyDate2) {
		ConsolidatedBuyDate2 = consolidatedBuyDate2;
	}

	public Date getConsolidatedBuyDate3() {
		return ConsolidatedBuyDate3;
	}

	public void setConsolidatedBuyDate3(Date consolidatedBuyDate3) {
		ConsolidatedBuyDate3 = consolidatedBuyDate3;
	}

	public Date getConsolidatedBuyDate4() {
		return ConsolidatedBuyDate4;
	}

	public void setConsolidatedBuyDate4(Date consolidatedBuyDate4) {
		ConsolidatedBuyDate4 = consolidatedBuyDate4;
	}

	public Date getDateDropped() {
		return DateDropped;
	}

	public void setDateDropped(Date dateDropped) {
		DateDropped = dateDropped;
	}

	public String getIsSMU() {
		return IsSMU;
	}

	public void setIsSMU(String isSMU) {
		IsSMU = isSMU;
	}

	public Integer getMinimumOrderQuantity() {
		return MinimumOrderQuantity;
	}

	public void setMinimumOrderQuantity(Integer minimumOrderQuantity) {
		MinimumOrderQuantity = minimumOrderQuantity;
	}

	public String getPrimaryRootColor() {
		return PrimaryRootColor;
	}

	public void setPrimaryRootColor(String primaryRootColor) {
		PrimaryRootColor = primaryRootColor;
	}

	public String getReqConsolidation() {
		return ReqConsolidation;
	}

	public void setReqConsolidation(String reqConsolidation) {
		ReqConsolidation = reqConsolidation;
	}


	public String getSMUAccount() {
		return SMUAccount;
	}

	public void setSMUAccount(String sMUAccount) {
		SMUAccount = sMUAccount;
	}

	public String getSustainable() {
		return Sustainable;
	}

	public void setSustainable(String sustainable) {
		Sustainable = sustainable;
	}

	public String getSMUSpecialHandling() {
		return SMUSpecialHandling;
	}

	public void setSMUSpecialHandling(String sMUSpecialHandling) {
		SMUSpecialHandling = sMUSpecialHandling;
	}

	public Integer getForecast() {
		return Forecast;
	}

	public void setForecast(Integer forecast) {
		Forecast = forecast;
	}



	public Boolean getB2Bonly() {
		return B2Bonly;
	}

	public void setB2Bonly(Boolean b2Bonly) {
		B2Bonly = b2Bonly;
	}


	public String getDhlAgron() {
		return DhlAgron;
	}

	public void setDhlAgron(String dhlAgron) {
		DhlAgron = dhlAgron;
	}

	public String getDhlStraub() {
		return DhlStraub;
	}

	public void setDhlStraub(String dhlStraub) {
		DhlStraub = dhlStraub;
	}

	public String getLaydownImageStatus() {
		return LaydownImageStatus;
	}

	public void setLaydownImageStatus(String laydownImageStatus) {
		LaydownImageStatus = laydownImageStatus;
	}

	public String getMannequinImageStatus() {
		return MannequinImageStatus;
	}

	public void setMannequinImageStatus(String mannequinImageStatus) {
		MannequinImageStatus = mannequinImageStatus;
	}

	public String getOnModelImageStatus() {
		return OnModelImageStatus;
	}

	public void setOnModelImageStatus(String onModelImageStatus) {
		OnModelImageStatus = onModelImageStatus;
	}


	public String getPriorityComments() {
		
		return DWUtil.skipSpecialChars(PriorityComments) ;
	}

	public void setPriorityComments(String priorityComments) {
		PriorityComments = priorityComments;
	}



	public String getProductReceiptStatus() {
		return ProductReceiptStatus;
	}

	public void setProductReceiptStatus(String productReceiptStatus) {
		ProductReceiptStatus = productReceiptStatus;
	}

	public Date getProductReceiptStatusDate() {
		return ProductReceiptStatusDate;
	}

	public void setProductReceiptStatusDate(Date productReceiptStatusDate) {
		ProductReceiptStatusDate = productReceiptStatusDate;
	}

	

	public String getSpecialPhotographyDirections() {
		return DWUtil.skipSpecialChars(SpecialPhotographyDirections) ;
	}

	public void setSpecialPhotographyDirections(String specialPhotographyDirections) {
		SpecialPhotographyDirections = specialPhotographyDirections;
	}

	
	public Boolean getReturnSampleToAgron() {
		return ReturnSampleToAgron;
	}

	public void setReturnSampleToAgron(Boolean returnSampleToAgron) {
		ReturnSampleToAgron = returnSampleToAgron;
	}

	public Boolean getStraubImageCompletion() {
		return StraubImageCompletion;
	}

	public void setStraubImageCompletion(Boolean straubImageCompletion) {
		StraubImageCompletion = straubImageCompletion;
	}


	public Boolean getPriority() {
		return Priority;
	}

	public void setPriority(Boolean priority) {
		Priority = priority;
	}

	public Boolean getProductionCancelled() {
		return ProductionCancelled;
	}

	public void setProductionCancelled(Boolean productionCancelled) {
		ProductionCancelled = productionCancelled;
	}

	public String getCatalogImageStatus() {
		return CatalogImageStatus;
	}

	public void setCatalogImageStatus(String catalogImageStatus) {
		CatalogImageStatus = catalogImageStatus;
	}

	
	

}
