package com.agron.wc.integration.dw.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PTC_Size_Staging")
public class PTC_Size_Staging {
	
	@Id
	@Column (nullable=false, updatable = false)
	private long PTC_SizeUniqueID;
	
	@Column (name="ArticleNumber", length =10)
	private String ItemCode = "";

	@Column
	private int PTC_ColorwayUniqueID;
	

	@Column (length =12)
	private String UPC = "";

	
	@Column (length =50)
	private String ProductSize1 = "";
	
	@Column (length =50)
	private String ProductSize2 = "";

	public long getPTC_SizeUniqueID() {
		return PTC_SizeUniqueID;
	}

	public void setPTC_SizeUniqueID(long pTC_SizeUniqueID) {
		PTC_SizeUniqueID = pTC_SizeUniqueID;
	}

	public String getItemCode() {
		return ItemCode;
	}

	public void setItemCode(String itemCode) {
		ItemCode = itemCode;
	}

	public int getPTC_ColorwayUniqueID() {
		return PTC_ColorwayUniqueID;
	}

	public void setPTC_ColorwayUniqueID(int pTC_ColorwayUniqueID) {
		PTC_ColorwayUniqueID = pTC_ColorwayUniqueID;
	}


	public String getUPC() {
		return UPC;
	}

	public void setUPC(String uPC) {
		UPC = uPC;
	}

	public String getProductSize1() {
		return ProductSize1;
	}

	public void setProductSize1(String productSize1) {
		ProductSize1 = productSize1;
	}

	public String getProductSize2() {
		return ProductSize2;
	}

	public void setProductSize2(String productSize2) {
		ProductSize2 = productSize2;
	}
	

	
	
}
