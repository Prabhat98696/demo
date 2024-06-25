package com.agron.wc.integration.dw.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "PTC_ModelSeasonVendor_Staging")
public class PTC_ModelSeasonVendor_Staging {

	@Id
	@Column
	private int PTC_ModelSeasonVendorKey;

	@Column
	private int PTC_SeasonKey;
	
	@Column
	private int WorkNumber;
	
	@Column
	private int PTC_VendorUniqueID;
	
	
	@Column( columnDefinition="BIT")
	private Boolean IsPrimaryVendor; 
	
	
	@Column(name = "CreateDateTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date CreateDateTime;
	
	
	@Column(name = "LastProcessDateTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date LastProcessDateTime;

	@Column
	private String ProcessStatus;

	public int getPTC_ModelSeasonVendorKey() {
		return PTC_ModelSeasonVendorKey;
	}

	public void setPTC_ModelSeasonVendorKey(int pTC_ModelSeasonVendorKey) {
		PTC_ModelSeasonVendorKey = pTC_ModelSeasonVendorKey;
	}

	public int getPTC_SeasonKey() {
		return PTC_SeasonKey;
	}

	public void setPTC_SeasonKey(int pTC_SeasonKey) {
		PTC_SeasonKey = pTC_SeasonKey;
	}

	public int getWorkNumber() {
		return WorkNumber;
	}

	public void setWorkNumber(int workNumber) {
		WorkNumber = workNumber;
	}

	public int getPTC_VendorUniqueID() {
		return PTC_VendorUniqueID;
	}

	public void setPTC_VendorUniqueID(int pTC_VendorUniqueID) {
		PTC_VendorUniqueID = pTC_VendorUniqueID;
	}

	public Boolean getIsPrimaryVendor() {
		return IsPrimaryVendor;
	}

	public void setIsPrimaryVendor(Boolean isPrimaryVendor) {
		IsPrimaryVendor = isPrimaryVendor;
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
	
	
	
}
