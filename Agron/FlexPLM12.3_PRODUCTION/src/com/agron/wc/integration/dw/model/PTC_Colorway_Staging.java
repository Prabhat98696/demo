/**
 * 
 */
package com.agron.wc.integration.dw.model;

import java.util.Date;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.agron.wc.integration.dw.DWUtil;
import com.lcs.wc.util.LCSProperties;

/**
 * @author Acnovate
 *
 */
@Entity
@Table(name = "PTC_Colorway_Staging")

public class PTC_Colorway_Staging {

	@Id
	@Column(name = "PTC_ColorwayUniqueID")
	private int PTC_ColorwayUniqueID;
	
	@Column
	private int WorkNumber;

	@Column (length =10)
	private String RootArticleNumber = "";
	
	@Column (length =10)
	private String AdidasArticleNumber = "";
	
	@Column (length =500)
	private String CatalogColowayName = "";
	
	@Column (length =50)
	private String Color1 = "";
	
	@Column (length =50)
	private String Color2 = "";
	
	@Column (length =50)
	private String Color3 = "";
	
	@Column (length =2)
	private String ColorwayLetter = "";
	@Column (length =50)
	private String ColorModifier = "";
	@Column (length =500)
	private String FabricContent = "";
	
	@Column (length =5)
	private String NRFColorCode = "";
	@Column (length =25)
	private String NRFColorDescription = "";
	
	@Column (length =250)
	private String CatalogFabric = "";
	
	@Column (length =30)
	private String SageColorName = "";
	
	@Column (length =500)
	private String NRFCombinedColorDescription = "";
	
	@Column (length =3)
	private String Assigned = "";
	
	//////////////////Getters and Setters  ////////////////////
	
	
	
	public int getPTC_ColorwayUniqueID() {
		return PTC_ColorwayUniqueID;
	}

	public String getAssigned() {
		return Assigned;
	}

	public void setAssigned(String assigned) {
		Assigned = assigned;
	}

	public int getWorkNumber() {
		return WorkNumber;
	}

	public void setWorkNumber(int workNumber) {
		WorkNumber = workNumber;
	}

	public void setPTC_ColorwayUniqueID(int pTC_ColorwayUniqueID) {
		PTC_ColorwayUniqueID = pTC_ColorwayUniqueID;
	}

	public String getRootArticleNumber() {
		return RootArticleNumber;
	}

	public void setRootArticleNumber(String rootArticleNumber) {
		RootArticleNumber = rootArticleNumber;
	}

	public String getAdidasArticleNumber() {
		return AdidasArticleNumber;
	}

	public void setAdidasArticleNumber(String adidasArticleNumber) {
		AdidasArticleNumber = adidasArticleNumber;
	}

	public String getCatalogColowayName() {
	
		return CatalogColowayName;
	}

	public void setCatalogColowayName(String catalogColowayName) {
		CatalogColowayName = DWUtil.skipSpecialChars(catalogColowayName);
	}

	public String getColor1() {
		return Color1;
	}

	public void setColor1(String color1) {
		Color1 = DWUtil.skipSpecialChars(color1);
	}

	public String getColor2() {
		return Color2;
	}

	public void setColor2(String color2) {
		Color2 = DWUtil.skipSpecialChars(color2);
	}

	public String getColor3() {
		return Color3;
	}

	public void setColor3(String color3) {
		Color3 = DWUtil.skipSpecialChars(color3);
	}

	public String getColorwayLetter() {
		return ColorwayLetter;
	}

	public void setColorwayLetter(String colorwayLetter) {
		ColorwayLetter = colorwayLetter;
	}

	public String getColorModifier() {
		return ColorModifier;
	}

	public void setColorModifier(String colorModifier) {
		ColorModifier = DWUtil.skipSpecialChars(colorModifier);
	}

	public String getFabricContent() {
		
		return FabricContent;
	}

	public void setFabricContent(String fabricContent) {
		FabricContent = DWUtil.skipSpecialChars(fabricContent);
	}

	public String getNRFColorCode() {
		return NRFColorCode;
	}

	public void setNRFColorCode(String nRFColorCode) {
		NRFColorCode = DWUtil.skipSpecialChars(nRFColorCode);
	}

	public String getNRFColorDescription() {
		return NRFColorDescription;
	}

	public void setNRFColorDescription(String nRFColorDescription) {
		NRFColorDescription = DWUtil.skipSpecialChars(nRFColorDescription);
	}



	public String getCatalogFabric() {
		return CatalogFabric;
	}

	public void setCatalogFabric(String catalogFabric) {
		CatalogFabric = DWUtil.skipSpecialChars(catalogFabric);
	}

	public String getSageColorName() {
		return DWUtil.skipSpecialChars(SageColorName)  ;
	}

	public void setSageColorName(String sageColorName) {
		SageColorName = DWUtil.skipSpecialChars(sageColorName);
	}

	public String getNRFCombinedColorDescription() {
		return NRFCombinedColorDescription ;
	}

	public void setNRFCombinedColorDescription(String nRFCombinedColorDescription) {
		NRFCombinedColorDescription = DWUtil.skipSpecialChars(nRFCombinedColorDescription);
	}


	
}
