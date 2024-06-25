package com.agron.wc.integration.dw.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PTC_SizeSeasonVendor_Staging")
public class PTC_SizeSeasonVendor_Staging {

	public String toString() {
		return  " Season: "+this.getPTC_SeasonKey() + " Size: " +this.getPTC_SizeUniqueID() + " Vendor: " + this.getPTC_VendorUniqueID()+" Staus :" +this.getSKUSizeStatus() +" PTC_SizeVendorSeasonKey: " + this.getPTC_SizeVendorSeasonKey();
		
		
	}
	
	@Id
	@Column (nullable=false, updatable = false)
	private Integer PTC_SizeVendorSeasonKey;

	@Column
	private int PTC_SeasonKey;
	
	@Column 
	private long PTC_SizeUniqueID;
	
	@Column
	private int PTC_VendorUniqueID;
	
	@Column (length =10)
	private String SKUSizeStatus = "";
	
	@Column(name = "FOB", columnDefinition = "Decimal(6,2)")
	private double FOB = 0;
	
	@Column (length =255)
	private String CostSheet = "";
	
	@Column (length =50)
	private String SourcingConfig = "";
	
	@Column( columnDefinition="BIT")
	private Boolean IsPrimaryCostSheet; 
	
	@Column(name = "AgencyFee", columnDefinition = "Decimal(6,2)")
	private double AgencyFee = 0;
	
	@Column(name = "Duty", columnDefinition = "Decimal(6,2)")
	private double  Duty = 0;
	
	@Column(name = "Freight", columnDefinition = "Decimal(6,2)")
	private double Freight = 0;
	
	@Column(name = "PackagingCost", columnDefinition = "Decimal(6,2)")
	private double PackagingCost = 0;
	
	@Column(name = "RoyaltyRate", columnDefinition = "Decimal(6,2)")
	private double RoyaltyRate = 0;
	
	@Column(name = "SubRoyaltyRate", columnDefinition = "Decimal(6,2)")
	private double SubRoyaltyRate = 0;
	
	@Column(name = "Tariff", columnDefinition = "Decimal(6,2)")
	private double Tariff = 0;
	
	@Column(name = "Warehouse", columnDefinition = "Decimal(6,2)")
	private double Warehouse = 0;
	

	public int getPTC_SizeVendorSeasonKey() {
		return PTC_SizeVendorSeasonKey;
	}

	public void setPTC_SizeVendorSeasonKey(int pTC_SizeVendorSeasonKey) {
		PTC_SizeVendorSeasonKey = pTC_SizeVendorSeasonKey;
	}
	
	

	public int getPTC_SeasonKey() {
		return PTC_SeasonKey;
	}

	public void setPTC_SeasonKey(int pTC_SeasonKey) {
		PTC_SeasonKey = pTC_SeasonKey;
	}

	public long getPTC_SizeUniqueID() {
		return PTC_SizeUniqueID;
	}

	public void setPTC_SizeUniqueID(long pTC_SizeUniqueID) {
		PTC_SizeUniqueID = pTC_SizeUniqueID;
	}

	public int getPTC_VendorUniqueID() {
		return PTC_VendorUniqueID;
	}

	public void setPTC_VendorUniqueID(int pTC_VendorUniqueID) {
		PTC_VendorUniqueID = pTC_VendorUniqueID;
	}

	public String getSKUSizeStatus() {
		return SKUSizeStatus;
	}

	public void setSKUSizeStatus(String sKUSizeStatus) {
		SKUSizeStatus = sKUSizeStatus;
	}

	public double getFOB() {
		return FOB;
	}

	public void setFOB(double fOB) {
		FOB = fOB;
	}

	public String getCostSheet() {
		return CostSheet;
	}

	public void setCostSheet(String costSheet) {
		CostSheet = costSheet;
	}

	public String getSourcingConfig() {
		return SourcingConfig;
	}

	public void setSourcingConfig(String sourcingConfig) {
		SourcingConfig = sourcingConfig;
	}

	public Boolean getIsPrimaryCostSheet() {
		return IsPrimaryCostSheet;
	}

	public void setIsPrimaryCostSheet(Boolean isPrimaryCostSheet) {
		IsPrimaryCostSheet = isPrimaryCostSheet;
	}

	public double getAgencyFee() {
		return AgencyFee;
	}

	public void setAgencyFee(double agencyFee) {
		AgencyFee = agencyFee;
	}

	public double getDuty() {
		return Duty;
	}

	public void setDuty(double duty) {
		Duty = duty;
	}

	public double getFreight() {
		return Freight;
	}

	public void setFreight(double freight) {
		Freight = freight;
	}

	public double getPackagingCost() {
		return PackagingCost;
	}

	public void setPackagingCost(double packagingCost) {
		PackagingCost = packagingCost;
	}

	public double getRoyaltyRate() {
		return RoyaltyRate;
	}

	public void setRoyaltyRate(double royaltyRate) {
		RoyaltyRate = royaltyRate;
	}

	public double getSubRoyaltyRate() {
		return SubRoyaltyRate;
	}

	public void setSubRoyaltyRate(double subRoyaltyRate) {
		SubRoyaltyRate = subRoyaltyRate;
	}

	public double getTariff() {
		return Tariff;
	}

	public void setTariff(double tariff) {
		Tariff = tariff;
	}

	public double getWarehouse() {
		return Warehouse;
	}

	public void setWarehouse(double warehouse) {
		Warehouse = warehouse;
	}


	

}
