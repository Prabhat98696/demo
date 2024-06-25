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
 * @author PTC-Service
 *
 */
@Entity(name = "PTC_Model_Staging")
@Table(name = "PTC_Model_Staging")

public class PTC_Model_Staging {

	@Id
	@Column(name = "WorkNumber")
	private int WorkNumber;

	@Column( length = 50)
	private String AdidasModelNumber = "";
	@Column (length = 15)
	private String BagStrapDropLengthInch = "";
	@Column (length = 16)
	private String Brand = "";
	@Column
	private String Bullet01 = "";
	@Column
	private String Bullet02 = "";
	@Column
	private String Bullet03 = "";
	@Column
	private String Bullet04 = "";
	@Column
	private String Bullet05 = "";
	@Column
	private String Bullet06 = "";
	@Column
	private String Bullet07 = "";
	@Column
	private String Bullet08 = "";
	@Column
	private String Bullet09 = "";
	@Column
	private String Bullet10 = "";

	@Column (length = 200)
	private String CareInstructions = "";
	@Column (length = 100)
	private String CatalogFabric = "";
	@Column (length = 25)
	private String Collection = "";
	@Column (length = 10)
	private String PackSize = "";
	
	@Column (length = 50)
	private String Silhouette = ""; 
	@Column (length = 100)
	private String Ecom_Name = "";
	@Column (length = 50)
	private String Family = "";
	@Column (length = 20)
	private String Gender = "";
	@Column (length = 6)
	private String HandleDropLengthInch = "";

	@Column (length = 100)
	private String MASStyleName = "";
	@Column (length = 30)
	private String OverrideMASStyleName = "";
	@Column (length = 25)
	private String PackagingType = "";
	@Column (length = 8)
	private String ParentWorkNumber = "";

	@Column(name = "ProductDescription", length = 750)
	private String ProductDescription = "";
	@Column (length = 8)
	private String ProductActualHeightInch = "";
	@Column (length = 8)
	private String ProductActualLengthInch = "";
	@Column (length = 50)
	private String ProductActualWidthInch = "";
	@Column (length = 50)
	private String ProductLine = "";
	@Column (length = 100)
	private String ProductName = "";

	@Column (length = 8)
	private String ReferenceWorkNumber = "";
	@Column (length = 20)
	private String SizeRange = "";
	
	@Column (length = 50)
	private String Type = "";

	@Column(name = "MasterCartonCasePack", columnDefinition = "Decimal(6,2)")
	private double MasterCartonCasePack = 0;

	@Column(name = "MasterCartonDepthInch", columnDefinition = "Decimal(6,2)")
	private Double MasterCartonDepthInch;

	@Column(name = "MasterCartonLengthInch", columnDefinition = "Decimal(6,2)")
	private Double MasterCartonLengthInch;

	@Column(name = "MasterCartonWeightLB", columnDefinition = "Decimal(6,2)")
	private Double MasterCartonWeightLB;

	@Column(name = "MasterCartonWidthInch", columnDefinition = "Decimal(6,2)")
	private Double MasterCartonWidthInch;

	@Column(name = "PackagedProductHeightInch", columnDefinition = "Decimal(6,2)")
	private Double PackagedProductHeightInch;
	@Column(name = "PackagedProductLengthInch", columnDefinition = "Decimal(6,2)")
	private Double PackagedProductLengthInch;
	@Column(name = "PackagedProductWeightLB", columnDefinition = "Decimal(6,2)")
	private Double PackagedProductWeightLB;
	@Column(name = "PackagedProductWidthInch", columnDefinition = "Decimal(6,2)")
	private Double PackagedProductWidthInch;
	
	@Column
	private Integer MasterInnerCartonQty;
	
	@Column
	private Integer CatalogSortOrder;
	
	@Column (length = 20)
	private String Capacity = "";
	
	@Column (length = 50)
	private String OrderMultiples = "";

	@Column (length = 100)
	private String StyleNameRoot = "";
	
	
	@Column (length = 50)
	private String AdidasProductType = "";
	

	public String getStyleNameRoot() {
		return StyleNameRoot;
	}

	public void setStyleNameRoot(String styleNameRoot) {
		StyleNameRoot = DWUtil.skipSpecialChars(styleNameRoot);
	}

	public String getOrderMultiples() {
		return OrderMultiples;
	}

	public void setOrderMultiples(String orderMultiples) {
		OrderMultiples = orderMultiples;
	}

	public Double getPackagedProductHeightInch() {
		return PackagedProductHeightInch;
	}

	public void setPackagedProductHeightInch(Double packagedProductHeightInch) {
		PackagedProductHeightInch = packagedProductHeightInch;
	}

	public Double getPackagedProductLengthInch() {
		return PackagedProductLengthInch;
	}

	public void setPackagedProductLengthInch(Double packagedProductLengthInch) {
		PackagedProductLengthInch = packagedProductLengthInch;
	}



	public Double getPackagedProductWeightLB() {
		return PackagedProductWeightLB;
	}

	public void setPackagedProductWeightLB(Double packagedProductWeightLB) {
		PackagedProductWeightLB = packagedProductWeightLB;
	}

	public Double getPackagedProductWidthInch() {
		return PackagedProductWidthInch;
	}

	public void setPackagedProductWidthInch(Double packagedProductWidthInch) {
		PackagedProductWidthInch = packagedProductWidthInch;
	}

	public Double getMasterCartonDepthInch() {
		return MasterCartonDepthInch;
	}

	public void setMasterCartonDepthInch(Double masterCartonDepthInch) {
		MasterCartonDepthInch = masterCartonDepthInch;
	}

	public Double getMasterCartonLengthInch() {
		return MasterCartonLengthInch;
	}

	public void setMasterCartonLengthInch(Double masterCartonLengthInch) {
		MasterCartonLengthInch = masterCartonLengthInch;
	}

	public Double getMasterCartonWeightLB() {
		return MasterCartonWeightLB;
	}

	public void setMasterCartonWeightLB(Double masterCartonWeightLB) {
		MasterCartonWeightLB = masterCartonWeightLB;
	}

	public Double getMasterCartonWidthInch() {
		return MasterCartonWidthInch;
	}

	public void setMasterCartonWidthInch(Double masterCartonWidthInch) {
		MasterCartonWidthInch = masterCartonWidthInch;
	}



	public String toString() {

		return String.valueOf(this.getWorkNumber());
	}



	public String getAdidasModelNumber() {
		return AdidasModelNumber;
	}

	public void setAdidasModelNumber(String adidasModelNumber) {
		AdidasModelNumber = DWUtil.skipSpecialChars(adidasModelNumber);
	}

	public String getBagStrapDropLengthInch() {
		return BagStrapDropLengthInch;
	}

	public void setBagStrapDropLengthInch(String bagStrapDropLengthInch) {
		BagStrapDropLengthInch = DWUtil.skipSpecialChars(bagStrapDropLengthInch);
	}

	public String getBrand() {
		return Brand;
	}

	public void setBrand(String brand) {
		Brand = brand;
	}

	public String getBullet01() {
		return Bullet01;
	}

	public void setBullet01(String bullet01) {
		Bullet01 = DWUtil.skipSpecialChars(bullet01);
	}

	public String getBullet02() {
		return Bullet02;
	}

	public void setBullet02(String bullet02) {
		Bullet02 = DWUtil.skipSpecialChars(bullet02);
	}

	public String getBullet03() {
		return Bullet03;
	}

	public void setBullet03(String bullet03) {
		Bullet03 = DWUtil.skipSpecialChars(bullet03);
	}

	public String getBullet04() {
		return Bullet04;
	}

	public void setBullet04(String bullet04) {
		Bullet04 = DWUtil.skipSpecialChars(bullet04);
	}

	public String getBullet05() {
		return Bullet05;
	}

	public void setBullet05(String bullet05) {
		Bullet05 = DWUtil.skipSpecialChars(bullet05);
	}

	public String getBullet06() {
		return Bullet06;
	}

	public void setBullet06(String bullet06) {
		Bullet06 = DWUtil.skipSpecialChars(bullet06);
	}

	public String getBullet07() {
		return Bullet07;
	}

	public void setBullet07(String bullet07) {
		Bullet07 = DWUtil.skipSpecialChars(bullet07);
	}

	public String getBullet08() {
		return Bullet08;
	}

	public void setBullet08(String bullet08) {
		Bullet08 = DWUtil.skipSpecialChars(bullet08);
	}

	public String getBullet09() {
		return Bullet09;
	}

	public void setBullet09(String bullet09) {
		Bullet09 = DWUtil.skipSpecialChars(bullet09);
	}

	public String getBullet10() {
		return Bullet10;
	}

	public void setBullet10(String bullet10) {
		Bullet10 = DWUtil.skipSpecialChars(bullet10);
	}



	public String getCareInstructions() {
		return CareInstructions;
	}

	public void setCareInstructions(String careInstructions) {
		CareInstructions = DWUtil.skipSpecialChars(careInstructions);
	}

	public String getCatalogFabric() {
		return CatalogFabric;
	}

	public void setCatalogFabric(String catalogFabric) {
		CatalogFabric = DWUtil.skipSpecialChars(catalogFabric);
	}

	public String getCollection() {
		return Collection;
	}

	public void setCollection(String collection) {
		Collection = DWUtil.skipSpecialChars(collection);
	}

	public String getFamily() {
		return Family;
	}

	public void setFamily(String family) {
		Family = family;
	}

	public String getGender() {
		return Gender;
	}

	public void setGender(String gender) {
		Gender = gender;
	}

	public String getHandleDropLengthInch() {
		return HandleDropLengthInch;
	}

	public void setHandleDropLengthInch(String handleDropLengthInch) {
		HandleDropLengthInch = DWUtil.skipSpecialChars(handleDropLengthInch);
	}



	public String getMASStyleName() {
		return MASStyleName;
	}

	public void setMASStyleName(String mASStyleName) {
		MASStyleName = DWUtil.skipSpecialChars(mASStyleName);
	}

	public String getOverrideMASStyleName() {
		return OverrideMASStyleName;
	}

	public void setOverrideMASStyleName(String overrideMASStyleName) {
		OverrideMASStyleName = DWUtil.skipSpecialChars(overrideMASStyleName);
	}

	public String getPackagingType() {
		return PackagingType;
	}

	public void setPackagingType(String packagingType) {
		PackagingType = packagingType;
	}

	public String getParentWorkNumber() {
		return ParentWorkNumber;
	}

	public void setParentWorkNumber(String parentWorkNumber) {
		ParentWorkNumber = parentWorkNumber;
	}

	public String getProductDescription() {
		return ProductDescription;
	}

	public void setProductDescription(String productDescription) {
		ProductDescription = DWUtil.skipSpecialChars(productDescription);
	}

	public String getProductActualHeightInch() {
		return ProductActualHeightInch;
	}

	public void setProductActualHeightInch(String productActualHeightInch) {
		ProductActualHeightInch = DWUtil.skipSpecialChars(productActualHeightInch);
	}

	public String getProductActualLengthInch() {
		return ProductActualLengthInch;
	}

	public void setProductActualLengthInch(String productActualLengthInch) {
		ProductActualLengthInch = DWUtil.skipSpecialChars(productActualLengthInch);
	}

	public String getProductActualWidthInch() {
		return ProductActualWidthInch;
	}

	public void setProductActualWidthInch(String productActualWidthInch) {
		ProductActualWidthInch = DWUtil.skipSpecialChars(productActualWidthInch);
	}

	public String getProductLine() {
		return ProductLine;
	}

	public void setProductLine(String productLine) {
		ProductLine = productLine;
	}

	public String getProductName() {
		return ProductName;
	}

	public void setProductName(String productName) {
		ProductName = DWUtil.skipSpecialChars(productName);
	}



	public String getReferenceWorkNumber() {
		return ReferenceWorkNumber;
	}

	public void setReferenceWorkNumber(String referenceWorkNumber) {
		ReferenceWorkNumber = referenceWorkNumber;
	}

	public String getSizeRange() {
		return SizeRange;
	}

	public void setSizeRange(String sizeRange) {
		SizeRange = DWUtil.skipSpecialChars(sizeRange);
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public double getMasterCartonCasePack() {
		return MasterCartonCasePack;
	}

	public void setMasterCartonCasePack(double masterCartonCasePack) {
		MasterCartonCasePack = masterCartonCasePack;
	}


	public int getWorkNumber() {
		return WorkNumber;
	}

	public void setWorkNumber(int workNumber) {
		WorkNumber = workNumber;
	}


	public String getEcom_Name() {
		return Ecom_Name;
	}

	public void setEcom_Name(String ecom_Name) {
		Ecom_Name = DWUtil.skipSpecialChars(ecom_Name);
	}

	public Integer getMasterInnerCartonQty() {
		return MasterInnerCartonQty;
	}

	public void setMasterInnerCartonQty(Integer masterInnerCartonQty) {
		MasterInnerCartonQty = masterInnerCartonQty;
	}

	public String getCapacity() {
		return Capacity;
	}

	public void setCapacity(String capacity) {
		Capacity = capacity;
	}

	public Integer getCatalogSortOrder() {
		return CatalogSortOrder;
	}

	public void setCatalogSortOrder(Integer catalogSortOrder) {
		CatalogSortOrder = catalogSortOrder;
	}
	
	public String getPackSize() {
		return PackSize;
	}

	public void setPackSize(String packSize) {
		PackSize = packSize;
	}

	public String getSilhouette() {
		return Silhouette;
	}

	public void setSilhouette(String silhouette) {
		Silhouette = silhouette;
	}

	public String getAdidasProductType() {
		return AdidasProductType;
	}

	public void setAdidasProductType(String adidasProductType) {
		AdidasProductType = adidasProductType;
	}


}
