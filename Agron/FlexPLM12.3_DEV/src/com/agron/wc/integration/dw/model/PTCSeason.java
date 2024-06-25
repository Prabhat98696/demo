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
@Entity (name = "PTC_Season_Staging")
@Table (name = "PTC_Season_Staging")

public class PTCSeason {
	
	@Id
	@Column(name = "PTC_SeasonKey")
	private int PTC_SeasonKey;
	
	@Column(name = "SeasonName", nullable = false)
	private String seasonName;

	@Column(name = "SeasonYear", nullable = true)
	private String SeasonYear;

	@Column(name = "CreateDateTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date CreateDateTime;
	
	
	@Column(name = "LastProcessDateTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date LastProcessDateTime;

	@Column
	private String ProcessStatus;
	
	
	
	
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

	public String getSeasonYear() {
		return SeasonYear;
	}

	public Date getCreateDateTime() {
		return CreateDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		CreateDateTime = createDateTime;
	}

	public void setSeasonYear(String seasonYear) {
		SeasonYear = seasonYear;
	}

	public int getPTC_SeasonKey() {
		return PTC_SeasonKey;
	}

	public void setPTC_SeasonKey(int pTC_SeasonKey) {
		PTC_SeasonKey = pTC_SeasonKey;
	}

	public String getSeasonName() {
		return seasonName;
	}

	public void setSeasonName(String seasonName) {
		this.seasonName = seasonName;
	}

	
}
