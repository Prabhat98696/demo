/**
 * 
 */
package com.agron.wc.integration.dw.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * @author PTC-Service
 *
 */
@Entity
@Table (name = "PTC_LoadHistory")

public class PTC_LoadHistory {
	
	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	@Column (nullable=false, updatable = false)
	private Integer PTCLoadKey;
	

	@Column(name = "StagingTableLoadStartTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date StagingTableLoadStartTime;
	
	
	@Column(name = "StagingTableLoadEndTime", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date StagingTableLoadEndTime;

	@Column( columnDefinition="BIT")
	private Boolean StagingTableLoadComplete;

	public int getPTCLoadKey() {
		return PTCLoadKey;
	}

	public void setPTCLoadKey(int pTCLoadKey) {
		PTCLoadKey = pTCLoadKey;
	}

	public Date getStagingTableLoadStartTime() {
		return StagingTableLoadStartTime;
	}

	public void setStagingTableLoadStartTime(Date stagingTableLoadStartTime) {
		StagingTableLoadStartTime = stagingTableLoadStartTime;
	}

	public Date getStagingTableLoadEndTime() {
		return StagingTableLoadEndTime;
	}

	public void setStagingTableLoadEndTime(Date stagingTableLoadEndTime) {
		StagingTableLoadEndTime = stagingTableLoadEndTime;
	}

	public Boolean getStagingTableLoadComplete() {
		return StagingTableLoadComplete;
	}

	public void setStagingTableLoadComplete(Boolean stagingTableLoadComplete) {
		StagingTableLoadComplete = stagingTableLoadComplete;
	} 
	
	
}
