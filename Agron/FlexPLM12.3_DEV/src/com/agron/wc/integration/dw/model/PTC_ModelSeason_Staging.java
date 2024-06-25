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

/**
 * @author PTC-Service
 *
 */
@Entity
@Table(name = "PTC_ModelSeason_Staging")

public class PTC_ModelSeason_Staging {

	@Id
	@Column
	private int PTC_ModelSeasonKey;


	@Column
	private int WorkNumber;

	@Column
	private int PTC_SeasonKey;

	@Column(columnDefinition = "Decimal(6,2)")
	private double OverrideRetailPrice = 0;

	@Column(columnDefinition = "Decimal(6,2)")
	private double OverrideWholeSalePrice = 0;

	@Column(columnDefinition = "Decimal(6,2)")
	private double RetailPrice = 0;
	@Column(columnDefinition = "Decimal(6,2)")
	private double WholesalePrice = 0;
	@Column
	private Integer CatalogSKUCount;

	@Column(columnDefinition="BIT")
	private Boolean OverrideWholesaleDate; 

	@Column( columnDefinition="BIT")
	private Boolean OverrideRetailDate; 

	@Column(name = "WholesaleEffectiveDate", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date WholesaleEffectiveDate;

	@Column(name = "RetailEffectiveDate", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date RetailEffectiveDate;

	@Column(length = 3)
	private String Global = "";

	@Column(length = 10)
	private String DeliverySeasons = "";

	@Column(length = 50)
	private String Sustainable = "";

	@Column(length = 100)
	private String Technologies = "";

	@Column  (length = 150)
	private String Sport = "";

	@Column (length = 16)
	private String HTSCode = "";

	@Column (length = 150)
	private String RecycledPercent = "";

	@Column (length =30)
	private String StraubManager = "";

	@Column(columnDefinition="BIT")
	private Boolean FranchiseStyle; 
	
	public Boolean getFranchiseStyle() {
		return FranchiseStyle;
	}

	public void setFranchiseStyle(Boolean franchiseStyle) {
		FranchiseStyle = franchiseStyle;
	}

	public String getStraubManager() {
		return StraubManager;
	}

	public void setStraubManager(String straubManager) {
		StraubManager = straubManager;
	}

	public String getRecycledPercent() {
		return RecycledPercent;
	}

	public void setRecycledPercent(String recycledPercent) {
		RecycledPercent = recycledPercent;
	}

	public double getOverrideRetailPrice() {
		return OverrideRetailPrice;
	}

	public void setOverrideRetailPrice(double overrideRetailPrice) {
		OverrideRetailPrice = overrideRetailPrice;
	}

	public double getOverrideWholeSalePrice() {
		return OverrideWholeSalePrice;
	}


	public void setOverrideWholeSalePrice(double overrideWholeSalePrice) {
		OverrideWholeSalePrice = overrideWholeSalePrice;
	}




	public double getRetailPrice() {
		return RetailPrice;
	}




	public void setRetailPrice(double retailPrice) {
		RetailPrice = retailPrice;
	}




	public double getWholesalePrice() {
		return WholesalePrice;
	}




	public void setWholesalePrice(double wholesalePrice) {
		WholesalePrice = wholesalePrice;
	}




	public Integer getCatalogSKUCount() {
		return CatalogSKUCount;
	}




	public void setCatalogSKUCount(Integer catalogSKUCount) {
		CatalogSKUCount = catalogSKUCount;
	}




	public Date getWholesaleEffectiveDate() {
		return WholesaleEffectiveDate;
	}




	public void setWholesaleEffectiveDate(Date wholesaleEffectiveDate) {
		WholesaleEffectiveDate = wholesaleEffectiveDate;
	}




	public Date getRetailEffectiveDate() {
		return RetailEffectiveDate;
	}




	public void setRetailEffectiveDate(Date retailEffectiveDate) {
		RetailEffectiveDate = retailEffectiveDate;
	}





	public String getGlobal() {
		return Global;
	}




	public void setGlobal(String global) {
		Global = global;
	}




	public String getDeliverySeasons() {
		return DeliverySeasons;
	}




	public void setDeliverySeasons(String deliverySeasons) {
		DeliverySeasons = deliverySeasons;
	}




	public String getSustainable() {
		return Sustainable;
	}




	public void setSustainable(String sustainable) {
		Sustainable = sustainable;
	}




	public String getTechnologies() {
		return Technologies;
	}




	public void setTechnologies(String technologies) {
		Technologies = technologies;
	}




	public int getPTC_ModelSeasonKey() {
		return PTC_ModelSeasonKey;
	}




	public void setPTC_ModelSeasonKey(int pTC_ModelSeasonKey) {
		PTC_ModelSeasonKey = pTC_ModelSeasonKey;
	}




	public int getWorkNumber() {
		return WorkNumber;
	}




	public void setWorkNumber(int workNumber) {
		WorkNumber = workNumber;
	}




	public int getPTC_SeasonKey() {
		return PTC_SeasonKey;
	}




	public void setPTC_SeasonKey(int pTC_SeasonKey) {
		PTC_SeasonKey = pTC_SeasonKey;
	}



	public String getSport() {
		return Sport;
	}
	public void setSport(String sport) {
		Sport = sport;
	}




	public Boolean getOverrideWholesaleDate() {
		return OverrideWholesaleDate;
	}




	public void setOverrideWholesaleDate(Boolean overrideWholesaleDate) {
		OverrideWholesaleDate = overrideWholesaleDate;
	}




	public Boolean getOverrideRetailDate() {
		return OverrideRetailDate;
	}




	public void setOverrideRetailDate(Boolean overrideRetailDate) {
		OverrideRetailDate = overrideRetailDate;
	}

	public String getHTSCode() {
		return HTSCode;
	}

	public void setHTSCode(String hTSCode) {
		HTSCode = hTSCode;
	}


}
