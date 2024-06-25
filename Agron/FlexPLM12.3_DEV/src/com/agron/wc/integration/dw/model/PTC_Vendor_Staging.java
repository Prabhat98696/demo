package com.agron.wc.integration.dw.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "PTC_Vendor_Staging")
public class PTC_Vendor_Staging {

	@Id
	@Column
	private int PTC_VendorUniqueID;
	
	@Column (length = 50)
	private String VendorNumber;
	
	
	@Column (length = 50)
	private String FactoryAgent;
	
	
	@Column (length = 20)
	private String CountryOfOrigin;
	
	@Column(name = "CreateDateTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date CreateDateTime;
	
	
	@Column(name = "LastProcessDateTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date LastProcessDateTime;

	@Column
	private String ProcessStatus;


	public int getPTC_VendorUniqueID() {
		return PTC_VendorUniqueID;
	}

	public void setPTC_VendorUniqueID(int pTC_VendorUniqueID) {
		PTC_VendorUniqueID = pTC_VendorUniqueID;
	}

	public String getVendorNumber() {
		return VendorNumber;
	}

	public void setVendorNumber(String vendorNumber) {
		VendorNumber = vendorNumber;
	}

	public String getFactoryAgent() {
		return FactoryAgent;
	}

	public void setFactoryAgent(String factoryAgent) {
		FactoryAgent = factoryAgent;
	}

	public String getCountryOfOrigin() {
		return CountryOfOrigin;
	}

	public void setCountryOfOrigin(String countryOfOrigin) {
		CountryOfOrigin = countryOfOrigin;
	}

	public Date getCreateDateTime() {
		return CreateDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		CreateDateTime = createDateTime;
	}

	public Date getLastProcessDateTime() {
		return LastProcessDateTime;
	}

	public void setLastProcessDateTime(Date lastProcessDateTime) {
		LastProcessDateTime = lastProcessDateTime;
	}

	public String getProcessStatus() {
		return ProcessStatus;
	}

	public void setProcessStatus(String processStatus) {
		ProcessStatus = processStatus;
	}
	
	
	/*

	---------------------------------
	, VARCHAR(50) --VendorName v7 modified by sandeep
	, VARCHAR(10) --v7 modified by sandeep
	---------------------------------
	--Auditing Attributes
	---------------------------------
	,CreateDateTime DATETIME
	---------------------------------
	--ETL Processing Attributes
	---------------------------------
	,HashValue AS hashbytes('SHA2_256', CONCAT(PTC_VendorPK,'|', PTC_VendorUniqueID,'|', VendorNumber,'|', FactoryAgent,'|', CountryOfOrigin))
	,ProcessStatus INTEGER
	,LastProcessDateTime DATETIME 
	 * */
	
}
