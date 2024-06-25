package com.agron.wc.flexbom;

import com.lcs.wc.client.ApplicationContext;
import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.TableDataUtil;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flexbom.BOMOwner;
import com.lcs.wc.flexbom.FlexBOMLink;
import com.lcs.wc.flexbom.FlexBOMPart;
import com.lcs.wc.flexbom.FlexBOMPartMaster;
import com.lcs.wc.flexbom.LCSFlexBOMQuery;
import com.lcs.wc.flexbom.MaterialColorInfo;
import com.lcs.wc.flextype.AttributeValueList;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialColorQuery;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierMaster;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.CopyModeUtil;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.sourcing.SourceComponentNumberPlugin;
import com.lcs.wc.specification.FlexSpecDestination;
import com.lcs.wc.specification.FlexSpecMaster;
import com.lcs.wc.specification.FlexSpecQuery;
import com.lcs.wc.specification.FlexSpecToComponentLink;
import com.lcs.wc.specification.FlexSpecToSeasonLink;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.supplier.LCSSupplierQuery;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.CollectionUtil;
import com.lcs.wc.util.FlexObjectUtil;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.RB;
import com.lcs.wc.util.SortHelper;
import com.lcs.wc.util.VersionHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wt.enterprise.RevisionControlled;

import wt.fc.PersistenceHelper;

import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.identity.IdentityFactory;

import wt.part.WTPart;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;

import wt.session.SessionServerHelper;

import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTPropertyVetoException;


public class AgronFlexBOMQuery
  extends LCSQuery
{
  public static final Logger LOGGER = LogManager.getLogger(AgronFlexBOMQuery.class.getName());
  private static final String CLASSNAME = AgronFlexBOMQuery.class.getName();
  public static String BOM_OVR_ORDER = LCSProperties.get("com.lcs.wc.flexbom.bomOverrideOrder");
  public static final boolean WCPART_ENABLED = LCSProperties.getBoolean("com.lcs.wc.specification.parts.Enabled");
  private static final boolean CASE_INSENSITIVE_SORT = LCSProperties.getBoolean("com.lcs.wc.flexbom.LCSFlexBOMQuery.caseInsensitiveSort");
  public static final String BOM_NUM_KEY = LCSProperties.get("com.lcs.wc.flexbom.FlexBOMPart.numberAttributeKey", "number");
  public static final String BOM_NAME_KEY = LCSProperties.get("com.lcs.wc.flexbom.FlexBOMPart.nameAttributeKey", "name");
  public static String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialSupplierRootType", "Supplier");
  
  public static SearchResults findProductWhereUsedData(LCSMaterial material)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.setDistinct(true);
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendFromTable(LCSProductSeasonLink.class, "LCSSeasonProductLink");
    statement.appendFromTable(LCSPartMaster.class, "PRODUCTMASTER");
    statement.appendFromTable(LCSSeason.class);
    
    statement.appendFromTable(LCSSupplierMaster.class);
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "childReference.key.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"));
    
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "parentRev"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "wip"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "branchId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "inDate"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "outDate"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "sequence"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "yield"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "dropped"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "sortingNumber"));
    statement.appendSelectColumn(new QueryColumn("PRODUCTMASTER", LCSPartMaster.class, "name"));
    statement.appendSelectColumn(new QueryColumn("PRODUCTMASTER", LCSPartMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    
    String seasonName = FlexTypeCache.getFlexTypeRoot("Season").getAttribute("seasonName").getColumnDescriptorName();
    statement.appendSelectColumn(new QueryColumn(LCSSeason.class, seasonName));
    statement.appendSelectColumn(new QueryColumn(LCSSeason.class, "masterReference.key.id"));
    statement.appendSelectColumn(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "productSeasonRevId"));
    statement.appendSelectColumn(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "seasonRevId"));
    statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "supplierName"));
    
    statement.addLatestIterationClause(LCSSeason.class);
    

    statement.appendJoin(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "productMasterId"), new QueryColumn(FlexBOMLink.class, "parentReference.key.id"));
    



    statement.appendJoin(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "seasonProductRevision"), new QueryColumn(FlexBOMLink.class, "parentRev"));
    


    statement.appendJoin(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "productMasterId"), new QueryColumn("PRODUCTMASTER", LCSPartMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    




    statement.appendJoin(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "roleBObjectRef.key.id"), new QueryColumn(LCSSeason.class, "masterReference.key.id"));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromReference(material.getMasterReference())));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "wip"), "0", "="));
    



    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    



    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "effectOutDate"), "", "IS NULL"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "seasonRemoved"), "0", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "seasonLinkType"), "PRODUCT", "="));
    

    statement.appendJoin(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"), new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"));
    
    statement.appendSortBy(new QueryColumn("PRODUCTMASTER", LCSPartMaster.class, "name"), CASE_INSENSITIVE_SORT);
    
    statement.appendSortBy(new QueryColumn(LCSSeason.class, seasonName), CASE_INSENSITIVE_SORT);
    
    statement.appendSortBy(new QueryColumn(LCSSupplierMaster.class, "supplierName"), CASE_INSENSITIVE_SORT);
    
    return LCSQuery.runDirectQuery(statement);
  }
  
  public static PreparedQueryStatement findWhereUsedInMaterialData(Map criteria, String materialMasterId, String supplierMasterId, String colorId, int queryLimit)
    throws WTException
  {
    PreparedQueryStatement statement = getWhereUsedBaseQuery(criteria, materialMasterId, supplierMasterId, colorId, queryLimit);
    
    statement.appendFromTable(LCSMaterial.class);
    
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
    statement.appendJoin(new QueryColumn(LCSMaterial.class, "masterReference.key.id(+)"), new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.id"));
    
    String MATERIAL_NAME_COLUMN = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnName();
    String MATERIAL_NAME_COLUMN_DESCRIPTOR = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnDescriptorName();
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, MATERIAL_NAME_COLUMN_DESCRIPTOR));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "checkoutInfo.state"), "wrk", "<>"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "versionInfo.identifier.versionId"), "A", "="));
    
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");
    if ((criteria != null) && (FormatHelper.hasContent("" + criteria.get("bomFlexTypeId")))) {
      bomType = FlexTypeCache.getFlexType("" + criteria.get("bomFlexTypeId"));
    }
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.classname"), "com.lcs.wc.material.LCSMaterialMaster", "="));
    
    statement.appendSortBy("LCSMaterial." + MATERIAL_NAME_COLUMN, CASE_INSENSITIVE_SORT);
    statement.appendSortBy(bomType.getAttribute("name").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
    statement.appendSortBy(bomType.getAttribute("partName").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
    return statement;
  }
  
  public static PreparedQueryStatement findWhereUsedInProductData(Map criteria, String materialMasterId, String supplierMasterId, String colorId, int queryLimit)
    throws WTException
  {
    PreparedQueryStatement statement = getWhereUsedBaseQuery(criteria, materialMasterId, supplierMasterId, colorId, queryLimit);
    
    FlexTypeGenerator flexg = new FlexTypeGenerator();
    if ((criteria != null) && ("PRODUCT".equals(criteria.get("ownerOption"))))
    {
      String ownerFlexTypeId = "" + criteria.get("ownerFlexTypeId");
      FlexType productType = FlexTypeCache.getFlexType(ownerFlexTypeId);
      

      statement.appendFromTable(LCSProduct.class);
      statement.appendSelectColumn(new QueryColumn(LCSProduct.class, LCSQuery.TYPED_BRANCH_ID));
      statement.appendSelectColumn(new QueryColumn(LCSProduct.class, "masterReference.key.id"));
      
      String PRODUCT_NAME_COLUMN = FlexTypeCache.getFlexTypeRoot("Product").getAttribute("productName").getColumnDescriptorName();
      String WORKNUMBER_COLUMN = FlexTypeCache.getFlexTypeRoot("Product").getAttribute("agrWorkOrderNoProduct").getColumnDescriptorName();
      statement.appendSelectColumn(new QueryColumn(LCSProduct.class, PRODUCT_NAME_COLUMN));
      statement.appendSelectColumn(new QueryColumn(LCSProduct.class, WORKNUMBER_COLUMN));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "checkoutInfo.state"), "wrk", "<>"));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "1", "="));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "versionInfo.identifier.versionId"), "A", "="));
      
      statement.appendJoin(new QueryColumn(LCSProduct.class, "masterReference.key.id"), new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.id"));
      
      flexg.setScope("PRODUCT");
      flexg.setLevel("PRODUCT");
      if ((criteria != null) && (productType != null)) {
        statement = flexg.generateSearchCriteria(productType, statement, criteria, true);
      }
      if ((FormatHelper.hasContent("" + criteria.get("bomSeasonId"))) || (CollectionUtil.doesCollectionContainSubstring(criteria.keySet(), "FLEXSPECIFICATION")))
      {
        statement.appendFromTable(FlexSpecification.class);
        statement.appendFromTable(FlexSpecToComponentLink.class);
        statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn("BOMPARTMASTER", FlexBOMPartMaster.class, "thePersistInfo.theObjectIdentifier.id"));
        statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), new QueryColumn(FlexSpecification.class, "masterReference.key.id"));
        
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecification.class, "checkoutInfo.state"), "wrk", "<>"));
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecification.class, "iterationInfo.latest"), "1", "="));
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecification.class, "versionInfo.identifier.versionId"), "A", "="));
        flexg.setScope(null);
        flexg.setLevel(null);
        if ((criteria != null) && (productType != null))
        {
          FlexType specType = productType.getReferencedFlexType("SPEC_TYPE_ID");
          statement = flexg.generateSearchCriteria(specType, statement, criteria, true);
        }
        if (FormatHelper.hasContent("" + criteria.get("bomSeasonId")))
        {
          statement.appendFromTable(FlexSpecToSeasonLink.class);
          statement.appendFromTable(LCSSeason.class);
          statement.appendJoin(new QueryColumn(FlexSpecToSeasonLink.class, "roleAObjectRef.key.id"), new QueryColumn(FlexSpecification.class, "masterReference.key.id"));
          statement.appendJoin(new QueryColumn(FlexSpecToSeasonLink.class, "roleBObjectRef.key.id"), new QueryColumn(LCSSeason.class, "masterReference.key.id"));
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, "checkoutInfo.state"), "wrk", "<>"));
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, "iterationInfo.latest"), "1", "="));
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, "versionInfo.identifier.versionId"), "A", "="));
          

          String seasonId = "" + criteria.get("bomSeasonId");
          seasonId = FormatHelper.getNumericFromOid(seasonId);
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, "iterationInfo.branchId"), "?", "="), new Long(seasonId));
        }
      }
    }
    else
    {
      FlexType productType = FlexTypeCache.getFlexTypeFromPath("Product");
      
      statement.appendFromTable(LCSProduct.class);
      statement.appendSelectColumn(new QueryColumn(LCSProduct.class, "masterReference.key.id"));
      String PRODUCT_NAME_COLUMN = FlexTypeCache.getFlexTypeRoot("Product").getAttribute("productName").getColumnDescriptorName();
      String WORKNUMBER_COLUMN = FlexTypeCache.getFlexTypeRoot("Product").getAttribute("agrWorkOrderNoProduct").getColumnDescriptorName();
      statement.appendSelectColumn(new QueryColumn(LCSProduct.class, PRODUCT_NAME_COLUMN));
      statement.appendSelectColumn(new QueryColumn(LCSProduct.class, WORKNUMBER_COLUMN));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "checkoutInfo.state"), "wrk", "<>"));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "1", "="));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "versionInfo.identifier.versionId"), "A", "="));
      statement.appendJoin(new QueryColumn(LCSProduct.class, "masterReference.key.id(+)"), new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.id"));
      
      flexg.setScope("PRODUCT");
      flexg.setLevel("PRODUCT");
      
      statement = flexg.addFlexTypeCriteriaIncludeNull(statement, productType, "");
    }
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.classname"), "com.lcs.wc.part.LCSPartMaster", "="));
    
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");
    if ((criteria != null) && (FormatHelper.hasContent("" + criteria.get("bomFlexTypeId")))) {
      bomType = FlexTypeCache.getFlexType("" + criteria.get("bomFlexTypeId"));
    }
    String PRODUCT_NAME_COLUMN = FlexTypeCache.getFlexTypeRoot("Product").getAttribute("productName").getColumnName();
    statement.appendSortBy("LCSProduct." + PRODUCT_NAME_COLUMN, CASE_INSENSITIVE_SORT);
    statement.appendSortBy(bomType.getAttribute("name").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
    statement.appendSortBy(bomType.getAttribute("partName").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
    
    return statement;
  }
  
  public static PreparedQueryStatement getWhereUsedBaseQuery(Map criteria, String materialMasterId, String supplierMasterId, String colorId, int queryLimit)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.setDistinct(true);
    statement.appendFromTable(FlexBOMLink.class, "FlexBOMLink");
    statement.appendFromTable(FlexBOMPart.class);
    statement.appendFromTable(FlexBOMPartMaster.class, "BOMPARTMASTER");
    statement.appendFromTable(LCSSupplierMaster.class);
    
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "parentReference.key.id"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "childReference.key.id"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "colorDimensionReference.key.id"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "sourceDimensionReference.key.id"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "colorReference.key.id"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "parentRev"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "wip"));
    
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "branchId"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "inDate"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "outDate"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "sequence"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "yield"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "dropped"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "sortingNumber"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "dimensionId"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "masterBranchId"));
    statement.appendSelectColumn(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "masterBranch"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "state.state"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, TYPED_BRANCH_ID));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, FlexTypeCache.getFlexTypeFromPath("BOM").getAttribute(BOM_NAME_KEY).getColumnDescriptorName()));
    
    statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "supplierName"));
    
    statement.appendSelectColumn(new QueryColumn("BOMPARTMASTER", FlexBOMPartMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"));
    

    statement.appendJoin(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "parentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
    statement.appendJoin(new QueryColumn("BOMPARTMASTER", FlexBOMPartMaster.class, "thePersistInfo.theObjectIdentifier.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"), "wrk", "<>"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "iterationInfo.latest"), "1", "="));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "wip"), "0", "="));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "dropped"), "0", "="));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "dimensionName"), "", "IS NULL"));
    



    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("FlexBOMLink", FlexBOMLink.class, "outDate"), "", "IS NULL"));
    

    statement.appendJoin(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"), new QueryColumn("FlexBOMLink", FlexBOMLink.class, "supplierReference.key.id"));
    
    FlexTypeGenerator flexg = new FlexTypeGenerator();
    flexg.setScope("LINK_SCOPE");
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");
    if ((criteria != null) && (FormatHelper.hasContent("" + criteria.get("bomFlexTypeId")))) {
      bomType = FlexTypeCache.getFlexType("" + criteria.get("bomFlexTypeId"));
    }
    flexg.setScope("BOM_SCOPE");
    flexg.setLevel(null);
    statement = flexg.appendQueryColumns(bomType, statement);
    if ((criteria != null) && (bomType != null)) {
      statement = flexg.generateSearchCriteria(bomType, statement, criteria, true);
    }
    flexg.setScope("LINK_SCOPE");
    flexg.setLevel(null);
    statement = flexg.appendQueryColumns(bomType, statement);
    if ((criteria != null) && (bomType != null)) {
      statement = flexg.generateSearchCriteria(bomType, statement, criteria, true);
    }
    if (FormatHelper.hasContent(materialMasterId))
    {
      statement.appendFromTable(FlexBOMLink.class, "MATERIALLINK");
      
      statement.appendJoin(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "parentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
      statement.appendJoin(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "branchId"), new QueryColumn("FlexBOMLink", FlexBOMLink.class, "branchId"));
      statement.appendJoin(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "parentReference.key.id"), new QueryColumn("FlexBOMLink", FlexBOMLink.class, "parentReference.key.id"));
      statement.appendJoin(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "parentRev"), new QueryColumn("FlexBOMLink", FlexBOMLink.class, "parentRev"));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "childReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(materialMasterId)));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "outDate"), "", "IS NULL"));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "wip"), "0", "="));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "dropped"), "0", "="));
    }
    if (FormatHelper.hasContent(supplierMasterId)) {
      if (FormatHelper.hasContent(materialMasterId))
      {
        if (!LCSSupplierQuery.PLACEHOLDERID.equals(supplierMasterId))
        {
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn("MATERIALLINK", FlexBOMLink.class, "supplierReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(supplierMasterId)));
        }
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(supplierMasterId)));
      }
    }
    if (FormatHelper.hasContent(colorId))
    {
      statement.appendFromTable(FlexBOMLink.class, "COLORLINK");
      
      statement.appendJoin(new QueryColumn("COLORLINK", FlexBOMLink.class, "parentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
      statement.appendJoin(new QueryColumn("COLORLINK", FlexBOMLink.class, "branchId"), new QueryColumn("FlexBOMLink", FlexBOMLink.class, "branchId"));
      statement.appendJoin(new QueryColumn("COLORLINK", FlexBOMLink.class, "parentReference.key.id"), new QueryColumn("FlexBOMLink", FlexBOMLink.class, "parentReference.key.id"));
      statement.appendJoin(new QueryColumn("COLORLINK", FlexBOMLink.class, "parentRev"), new QueryColumn("FlexBOMLink", FlexBOMLink.class, "parentRev"));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("COLORLINK", FlexBOMLink.class, "colorReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(colorId)));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("COLORLINK", FlexBOMLink.class, "outDate"), "", "IS NULL"));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("COLORLINK", FlexBOMLink.class, "wip"), "0", "="));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("COLORLINK", FlexBOMLink.class, "dropped"), "0", "="));
    }
    statement.setQueryLimit(queryLimit);
    
    return statement;
  }
  
  public static Collection findWhereUsedData(LCSMaterial material)
    throws WTException
  {
    return findWhereUsedData(new HashMap(), FormatHelper.getObjectId(material.getMaster()), null, null);
  }
  
  public static Collection findWhereUsedData(Map criteria, String materialMasterId, String supplierMasterId, String colorId)
    throws WTException
  {
    return findWhereUsedData(criteria, materialMasterId, supplierMasterId, colorId, 0);
  }
  
  public static Collection findWhereUsedData(Map criteria, String materialMasterId, String supplierMasterId, String colorId, int queryLimit)
    throws WTException
  {
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");
    if ((criteria != null) && (FormatHelper.hasContent("" + criteria.get("bomFlexTypeId")))) {
      bomType = FlexTypeCache.getFlexType("" + criteria.get("bomFlexTypeId"));
    }
    PreparedQueryStatement productQuery = findWhereUsedInProductData(criteria, materialMasterId, supplierMasterId, colorId, queryLimit);
    
    Collection<FlexObject> results = LCSQuery.runDirectQuery(productQuery).getResults();
    
    String PRODUCT_NAME_COLUMN = ("LCSProduct." + FlexTypeCache.getFlexTypeRoot("Product").getAttribute("productName").getColumnName()).toUpperCase();
   // Agron customization to show work number on the material where used.
    String WORKNUMBER_COLUMN = ("LCSProduct." + FlexTypeCache.getFlexTypeRoot("Product").getAttribute("agrWorkOrderNoProduct").getColumnName()).toUpperCase();

    String PRODUCT_ID_COLUMN = "LCSPRODUCT.IDA3MASTERREFERENCE";
    String PRODUCT_CLASSNAME = "com.lcs.wc.part.LCSPartMaster";
    for (FlexObject obj : results)
    {
      obj.setData("OWNERMASTER.NAME", obj.getData(PRODUCT_NAME_COLUMN));
      obj.setData("WORKNUMBER", obj.getData(WORKNUMBER_COLUMN));
      obj.setData("OWNERMASTER.IDA2A2", obj.getData("LCSPRODUCT.IDA3MASTERREFERENCE"));
      obj.setData("OWNERMASTER.CLASSNAME", "com.lcs.wc.part.LCSPartMaster");
    }
    PreparedQueryStatement materialQuery = findWhereUsedInMaterialData(criteria, materialMasterId, supplierMasterId, colorId, queryLimit);
    
    Collection<FlexObject> matResults = LCSQuery.runDirectQuery(materialQuery).getResults();
    
    String MATERIAL_NAME_COLUMN = ("LCSMaterial." + FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnName()).toUpperCase();
    String MATERIAL_ID_COLUMN = "LCSMATERIAL.IDA3MASTERREFERENCE";
    String MATERIAL_CLASSNAME = "com.lcs.wc.material.LCSMaterialMaster";
    for (FlexObject obj : matResults)
    {
      obj.setData("OWNERMASTER.NAME", obj.getData(MATERIAL_NAME_COLUMN));
      obj.setData("OWNERMASTER.IDA2A2", obj.getData("LCSMATERIAL.IDA3MASTERREFERENCE"));
      obj.setData("OWNERMASTER.CLASSNAME", "com.lcs.wc.material.LCSMaterialMaster");
    }
    results.addAll(matResults);
    
    Object sortList = new Vector();
    ((List)sortList).add("OWNERMASTER.NAME");
    ((List)sortList).add(bomType.getAttribute("name").getSearchResultIndex());
    ((List)sortList).add(bomType.getAttribute("partName").getSearchResultIndex());
    
    results = FlexObjectUtil.sortFlexObjects(results, (List)sortList);
    return results;
  }
  
  public static SearchResults findPrimaryMaterialWhereUsed(LCSMaterialSupplier primaryMaterial)
    throws WTException
  {
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");
    String materialMasterId = FormatHelper.getNumericObjectIdFromObject(primaryMaterial.getMaterialMaster());
    
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMPart.class);
    statement.appendFromTable(LCSMaterial.class);
    statement.appendFromTable(LCSProduct.class);
    
    statement.appendFromTable(FlexBOMPartMaster.class, "BOMPARTMASTER");
    
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(LCSProduct.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn("BOMPARTMASTER", FlexBOMPartMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    
    String MATERIAL_NAME_COLUMN = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnDescriptorName();
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, MATERIAL_NAME_COLUMN));
    String PRODUCT_NAME_COLUMN = FlexTypeCache.getFlexTypeRoot("Product").getAttribute("productName").getColumnDescriptorName();
    statement.appendSelectColumn(new QueryColumn(LCSProduct.class, PRODUCT_NAME_COLUMN));
    
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "state.state"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "typeDefinitionReference.key.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"));
    

    statement.addLatestIterationClause(FlexBOMPart.class);
    
    statement.appendAndIfNeeded();
    statement.appendOpenParen();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "1", "="));
    statement.appendOrIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "iterationInfo.latest"), "", "IS NULL"));
    statement.appendClosedParen();
    statement.appendAndIfNeeded();
    statement.appendOpenParen();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "checkoutInfo.state"), "wrk", "<>"));
    statement.appendOrIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "checkoutInfo.state"), "", "IS NULL"));
    statement.appendClosedParen();
    
    statement.appendAndIfNeeded();
    statement.appendOpenParen();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "versionInfo.identifier.versionId"), "A", "="));
    statement.appendOrIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSProduct.class, "versionInfo.identifier.versionId"), "", "IS NULL"));
    statement.appendClosedParen();
    
    statement.appendJoin(new QueryColumn(FlexBOMPart.class, "masterReference.key.id"), new QueryColumn(LCSProduct.class, "masterReference.key.id(+)"));
    statement.appendJoin(new QueryColumn("BOMPARTMASTER", FlexBOMPartMaster.class, "thePersistInfo.theObjectIdentifier.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
    

    statement.appendAndIfNeeded();
    String PRIMARY_MATERIAL_COLUMN = FlexTypeCache.getFlexTypeFromPath("BOM\\Materials").getAttribute("primaryMaterial").getColumnDescriptorName();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, PRIMARY_MATERIAL_COLUMN), "?", "="), Long.valueOf(primaryMaterial.getBranchIdentifier()));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "masterReference.key.id"), "?", "="), FormatHelper.getNumericFromOid(materialMasterId));
    

    statement.addLatestIterationClause(LCSMaterial.class);
    
    statement.appendAndIfNeeded();
    statement.appendOpenParen();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "versionInfo.identifier.versionId"), "A", "="));
    statement.appendOrIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "versionInfo.identifier.versionId"), "", "IS NULL"));
    statement.appendClosedParen();
    

    statement.appendSortBy(new QueryColumn(LCSProduct.class, PRODUCT_NAME_COLUMN), CASE_INSENSITIVE_SORT);
    statement.appendSortBy(bomType.getAttribute("name").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
    
    printStatement(statement);
    return LCSQuery.runDirectQuery(statement);
  }
  
  public static SearchResults findMaterialColorDataForBOM(RevisionControlled part)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.setDistinct(true);
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendFromTable(LCSSupplierMaster.class);
    statement.appendFromTable(LCSMaterialMaster.class, "MATERIALMASTER");
    statement.appendFromTable(LCSColor.class);
    statement.appendFromTable("V_LCSMaterialColor", "LCSMATERIALCOLOR");
    statement.appendSelectColumn(new QueryColumn("LCSMATERIALCOLOR", "statestate"));
    statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "supplierName"));
    statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorName"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorHexidecimalValue"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thumbnail"));
    statement.appendSelectColumn(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "name"));
    statement.appendSelectColumn(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId((WTObject)part.getMaster()))));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentRev"), "?", "="), part.getVersionIdentifier().getValue());
    

    statement.appendJoin(new QueryColumn("LCSMATERIALCOLOR", "idA3A10"), new QueryColumn(FlexBOMLink.class, "childReference.key.id"));
    
    statement.appendJoin(new QueryColumn("LCSMATERIALCOLOR", "idA3C10"), new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"));
    
    statement.appendJoin(new QueryColumn("LCSMATERIALCOLOR", "idA3B10"), new QueryColumn(FlexBOMLink.class, "colorReference.key.id"));
    statement.appendJoin(new QueryColumn("LCSMATERIALCOLOR", "idA3A10"), new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendJoin(new QueryColumn("LCSMATERIALCOLOR", "idA3C10"), new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendJoin(new QueryColumn("LCSMATERIALCOLOR", "idA3B10"), new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "name"), "material_placeholder", "<>"));
    

    FlexType materialColorRootType = FlexTypeCache.getFlexTypeFromPath("Material Color");
    
    addFlexTypeInformation(statement, LCSMaterialColor.class);
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "wip"), "0", "="));
    
    FlexTypeGenerator flexg = new FlexTypeGenerator();
    
    statement = flexg.appendQueryColumns(materialColorRootType, statement);
    
    statement.appendSortBy(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "name"), CASE_INSENSITIVE_SORT);
    
    statement.appendSortBy(new QueryColumn(LCSSupplierMaster.class, "supplierName"), CASE_INSENSITIVE_SORT);
    
    statement.appendSortBy(new QueryColumn(LCSColor.class, "colorName"), CASE_INSENSITIVE_SORT);
    
    return LCSQuery.runDirectQuery(statement);
  }
  
  public static Collection findFlexBOMLinks(boolean usedByComparison, FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, String dimensionMode, String skuMode, String sourceMode, String sizeMode)
    throws WTException
  {
    SearchResults bomData = findFlexBOMData(usedByComparison, part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, true, dimensionMode, skuMode, sourceMode, sizeMode, null, null);
    return getObjectsFromResults(bomData, "OR:com.lcs.wc.flexbom.FlexBOMLink:", "FLEXBOMLINK.IDA2A2");
  }
  
  public static Collection findFlexBOMLinks(FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, String dimensionMode, String skuMode, String sourceMode, String sizeMode)
    throws WTException
  {
    SearchResults bomData = findFlexBOMData(part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, true, dimensionMode, skuMode, sourceMode, sizeMode);
    return getObjectsFromResults(bomData, "OR:com.lcs.wc.flexbom.FlexBOMLink:", "FLEXBOMLINK.IDA2A2");
  }
  
  public static SearchResults findFlexBOMData(FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, boolean linkDataOnly, String dimensionMode, String skuMode, String sourceMode, String sizeMode)
    throws WTException
  {
    return findFlexBOMData(part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, linkDataOnly, dimensionMode, skuMode, sourceMode, sizeMode, null);
  }
  
  public static SearchResults findFlexBOMData(FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, boolean linkDataOnly, String dimensionMode, String skuMode, String sourceMode, String sizeMode, FlexType materialType)
    throws WTException
  {
    return findFlexBOMData(false, part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, linkDataOnly, dimensionMode, skuMode, sourceMode, sizeMode, materialType, null);
  }
  
  public static SearchResults findFlexBOMData(FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, boolean linkDataOnly, String dimensionMode, String skuMode, String sourceMode, String sizeMode, FlexType materialType, Collection usedAttKeys)
    throws WTException
  {
    return findFlexBOMData(false, part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, linkDataOnly, dimensionMode, skuMode, sourceMode, sizeMode, materialType, usedAttKeys);
  }
  
  public static SearchResults findFlexBOMData(boolean usedByComparison, FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, boolean linkDataOnly, String dimensionMode, String skuMode, String sourceMode, String sizeMode, FlexType materialType, Collection usedAttKeys)
    throws WTException
  {
    PreparedQueryStatement statement = getFlexBOMQuery(usedByComparison, part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, linkDataOnly, dimensionMode, skuMode, sourceMode, sizeMode, materialType, usedAttKeys);
    statement.appendSortBy(new QueryColumn(FlexBOMLink.class, "sortingNumber"));
    statement.appendSortBy(new QueryColumn(FlexBOMLink.class, "sequence"));
    




    SearchResults results = runDirectQuery(statement);
    

    boolean origEnforce = SessionServerHelper.manager.setAccessEnforced(false);
    try
    {
      if (WCPART_ENABLED)
      {
        Iterator wtPartIter = LCSQuery.getObjectsFromResults(results.getResults(), "VR:wt.part.WTPart:", "WTPART.BRANCHIDITERATIONINFO").iterator();
        
        HashMap tempMap = new HashMap();
        while (wtPartIter.hasNext())
        {
          WTPart wtPart = (WTPart)wtPartIter.next();
          tempMap.put(Long.valueOf(wtPart.getBranchIdentifier()), getDisplayIdentifier(wtPart));
        }
        Iterator iterator = results.getResults().iterator();
        FlexObject obj = null;
        while (iterator.hasNext())
        {
          obj = (FlexObject)iterator.next();
          if ((obj.get("WTPART.BRANCHIDITERATIONINFO") != null) && (FormatHelper.hasContent(obj.get("WTPART.BRANCHIDITERATIONINFO").toString()))) {
            obj.put("WTPART.DISPLAYIDENTIFIER", tempMap.get(Long.valueOf(obj.get("WTPART.BRANCHIDITERATIONINFO").toString())));
          }
        }
      }
    }
    catch (WTException wte)
    {
      throw wte;
    }
    finally
    {
      SessionServerHelper.manager.setAccessEnforced(origEnforce);
    }
    if ("WIP_ONLY".equals(wipMode)) {
      results.setResults(filterWIPOnly(results.getResults()));
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Results = " + results);
    }
    return results;
  }
  
  protected static String getDisplayIdentifier(WTPart part)
  {
    return IdentityFactory.getDisplayIdentifier(part).getLocalizedMessage(WTContext.getContext().getLocale()).toString();
  }
  
  public static Collection joinInLinkedBOMs(Collection bomLinks)
    throws WTException
  {
    ArrayList fullBOM = new ArrayList();
    LCSFlexBOMQuery fq = new LCSFlexBOMQuery();
    String materialId = "";
    String supplierId = "";
    LCSMaterial material = null;
    Collection bomList = null;
    Collection bpData = null;
    Iterator bpdi = null;
    
    String branchId = "";
    String subBranchId = "";
    
    FlexObject row = null;
    FlexObject subRow = null;
    FlexBOMPart bomPart = null;
    

    Collection<BOMOwner> matMasters = new HashSet();
    for (Iterator i = bomLinks.iterator(); i.hasNext();)
    {
      row = (FlexObject)i.next();
      
      materialId = row.getString("FLEXBOMLINK.IDA3B5");
      if ((FormatHelper.hasContent(materialId)) && (!LCSMaterialQuery.PLACEHOLDERNUMERIC.equals(materialId)) && (!"1".equals(row.getString("FLEXBOMLINK.MASTERBRANCH"))) && (!FormatHelper.hasContent(row.getString("FLEXBOMLINK.MASTERBRANCHID"))))
      {
        materialId = "OR:com.lcs.wc.material.LCSMaterialMaster:" + materialId;
        LCSMaterialMaster matMaster = (LCSMaterialMaster)LCSQuery.findObjectById(materialId);
        matMasters.add(matMaster);
      }
    }
    Hashtable materialOwner_BOMLink_map = new Hashtable();
    if (!matMasters.isEmpty())
    {
      Collection<FlexBOMPart> bomLists = fq.findBOMPartsForOwners(matMasters, "A", "MAIN", null, null);
      for (FlexBOMPart bomOwnedByMat : bomLists) {
        materialOwner_BOMLink_map.put(bomOwnedByMat.getOwnerMaster(), bomOwnedByMat);
      }
    }
    for (Iterator i = bomLinks.iterator(); i.hasNext();)
    {
      row = (FlexObject)i.next();
      
      branchId = row.getString("FLEXBOMLINK.BRANCHID");
      materialId = row.getString("FLEXBOMLINK.IDA3B5");
      if ((!FormatHelper.hasContent(materialId)) || (LCSMaterialQuery.PLACEHOLDERNUMERIC.equals(materialId)) || ("1".equals(row.getString("FLEXBOMLINK.MASTERBRANCH"))) || (FormatHelper.hasContent(row.getString("FLEXBOMLINK.MASTERBRANCHID"))))
      {
        fullBOM.add(row);
      }
      else
      {
        materialId = "OR:com.lcs.wc.material.LCSMaterialMaster:" + materialId;
        LCSMaterialMaster matMaster = (LCSMaterialMaster)LCSQuery.findObjectById(materialId);
        material = (LCSMaterial)VersionHelper.latestIterationOf(matMaster);
        
        bomPart = (FlexBOMPart)materialOwner_BOMLink_map.get(matMaster);
        if (bomPart != null)
        {
          row.put("FLEXBOMLINK.LINKEDBOM", "1");
          fullBOM.add(row);
          
          supplierId = "OR:com.lcs.wc.supplier.LCSSupplierMaster:" + row.getString("LCSSUPPLIERMASTER.IDA2A2");
          try
          {
            bpData = findFlexBOMData(bomPart, supplierId, null, null, null, null, "WIP_ONLY", null, false, false, "ALL_DIMENSIONS", null, null, null, material.getFlexType()).getResults();
          }
          catch (Exception e)
          {
            throw new WTException(e);
          }
          bpData = bomSort(bpData);
          
          bpdi = bpData.iterator();
          String sectionAtt = FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("section").getColumnName();
          String currentPartName = "";
          String placeholderId = FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER);
          while (bpdi.hasNext())
          {
            subRow = (FlexObject)bpdi.next();
            currentPartName = subRow.getData("FLEXBOMLINK." + FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("partName").getColumnName().toUpperCase());
            if ((FormatHelper.hasContent(currentPartName)) || (!placeholderId.equals(subRow.getData("FLEXBOMLINK.IDA3B5"))))
            {
              subBranchId = row.getString("FLEXBOMLINK.BRANCHID");
              subBranchId = branchId + "-" + subBranchId;
              subRow.put("FLEXBOMLINK.BRANCHID", subBranchId);
              

              subRow.put("FLEXBOMLINK." + sectionAtt, row.getString("FLEXBOMLINK." + sectionAtt));
              subRow.put("FLEXBOMLINK.MASTERBRANCHID", branchId);
              subRow.put("FLEXBOMLINK.MASTERBRANCH", "0");
              
              fullBOM.add(subRow);
            }
          }
        }
        else
        {
          fullBOM.add(row);
        }
      }
    }
    return fullBOM;
  }
  
  public static Collection bomSort(Collection bom)
    throws WTException
  {
    return bomSort(bom, FlexTypeCache.getFlexTypeRoot("BOM"));
  }
  
  public static Collection bomSort(Collection bom, FlexType bomPartType)
    throws WTException
  {
    Collection sortedBom = new ArrayList();
    
    HashMap groupMap = new HashMap();
    
    Iterator i = bom.iterator();
    Collection sGroup = null;
    if (bomPartType == null) {
      bomPartType = FlexTypeCache.getFlexTypeRoot("BOM");
    }
    FlexTypeAttribute sectionAtt = bomPartType.getAttribute("section");
    String sectionKey = sectionAtt.getColumnName();
    FlexObject obj = null;
    String section = "";
    while (i.hasNext())
    {
      obj = (FlexObject)i.next();
      section = obj.getString("FLEXBOMLINK." + sectionKey);
      sGroup = (Collection)groupMap.get(section);
      if (sGroup == null) {
        sGroup = new ArrayList();
      }
      sGroup.add(obj);
      
      groupMap.put(section, sGroup);
    }
    Collection sections = sectionAtt.getAttValueList().getSelectableKeys(null, true);
    Iterator s = sections.iterator();
    int count = 1;
    while (s.hasNext())
    {
      section = (String)s.next();
      sGroup = (Collection)groupMap.get(section);
      if (sGroup != null)
      {
        sGroup = SortHelper.sortFlexObjectsByNumber(sGroup, "FLEXBOMLINK.SORTINGNUMBER");
        i = sGroup.iterator();
        while (i.hasNext())
        {
          obj = (FlexObject)i.next();
          obj.setData("FLEXBOMLINK.SORTINGNUMBER", "" + count);
          sortedBom.add(obj);
          count++;
        }
      }
    }
    i = bom.iterator();
    while (i.hasNext())
    {
      obj = (FlexObject)i.next();
      section = obj.getString("FLEXBOMLINK." + sectionKey);
      if (!FormatHelper.hasContent(section)) {
        sortedBom.add(obj);
      }
    }
    return sortedBom;
  }
  
  public static PreparedQueryStatement getFlexBOMQuery(FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, boolean linkDataOnly, String dimensionMode, String skuMode, String sourceMode, String sizeMode, FlexType materialType)
    throws WTException
  {
    return getFlexBOMQuery(part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, linkDataOnly, dimensionMode, skuMode, sourceMode, sizeMode, materialType, null);
  }
  
  public static PreparedQueryStatement getFlexBOMQuery(FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, boolean linkDataOnly, String dimensionMode, String skuMode, String sourceMode, String sizeMode, FlexType materialType, Collection usedAttKeys)
    throws WTException
  {
    return getFlexBOMQuery(false, part, scMasterId, skuMasterId, size1, size2, destDimId, wipMode, effectiveDate, dropped, linkDataOnly, dimensionMode, skuMode, sourceMode, sizeMode, materialType, usedAttKeys);
  }
  
  public static PreparedQueryStatement getFlexBOMQuery(boolean usedByComparison, FlexBOMPart part, String scMasterId, String skuMasterId, String size1, String size2, String destDimId, String wipMode, Date effectiveDate, boolean dropped, boolean linkDataOnly, String dimensionMode, String skuMode, String sourceMode, String sizeMode, FlexType materialType, Collection usedAttKeys)
    throws WTException
  {
    if ("ALL_SKUS".equals(skuMasterId)) {
      skuMasterId = null;
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("LCSFlexBOMQuery.findFlexBOMData: \npart = " + part + " \nscMasterId = " + scMasterId + " \nskuMasterId = " + skuMasterId + " \nsize1 = " + size1 + " \nsize2 " + size2 + "\ndestDimId = " + destDimId + "\nwipMode = " + wipMode + " \neffectiveDate = " + effectiveDate + " \ndropped = " + dropped + " \ndimensionMode = " + dimensionMode + " \nskuMode = " + skuMode + "\nsourceMode = " + sourceMode + "\nsizeMode = " + sizeMode);
    }
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "childReference.key.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "materialColorReference.key.id"));
    
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "parentRev"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "wip"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "branchId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "inDate"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "outDate"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "sequence"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "yield"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"));
    

    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "sourceDimensionReference.key.id"));
    

    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "destinationDimensionReference.key.id"));
    

    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "colorReference.key.id"));
    if (WCPART_ENABLED) {
      statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "wcPartReference.key.branchId"));
    }
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "size1"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "size2"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "sortingNumber"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "dropped"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "dimensionId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "dimensionName"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "masterBranchId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "masterBranch"));
    
    FlexType type = part.getFlexType();
    if (type != null)
    {
      FlexTypeGenerator flexg = new FlexTypeGenerator();
      flexg.setScope("LINK_SCOPE");
      flexg.setLevel(null);
      
      statement = flexg.appendQueryColumns(null, type, statement, "FlexBOMLink", usedAttKeys);
    }
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(part.getMaster()))));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentRev"), "?", "="), part.getVersionIdentifier().getValue());
    if (effectiveDate != null)
    {
      statement.appendAndIfNeeded();
      statement.appendOpenParen();
      
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "inDate"), "?", "<="), FormatHelper.roundupToNextSec(effectiveDate));
      




      statement.appendAndIfNeeded();
      statement.appendOpenParen();
      if (!usedByComparison) {
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "?", ">="), effectiveDate);
      } else {
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "?", ">"), effectiveDate);
      }
      statement.appendOr();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
      statement.appendClosedParen();
      statement.appendClosedParen();
    }
    else
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    }
    if ("EFFECTIVE_ONLY".equals(wipMode))
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "wip"), "0", "="));
    }
    if ((!dropped) && (!"WIP_ONLY".equals(wipMode)))
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    }
    if (FormatHelper.hasContent(skuMasterId))
    {
      if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(skuMasterId)));
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendOpenParen();
        
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(skuMasterId)));
        statement.appendOr();
        
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"), "0", "="));
        statement.appendClosedParen();
      }
    }
    else if (!"ALL_DIMENSIONS".equals(dimensionMode)) {
      if ("ALL_SKUS".equals(skuMode))
      {
        if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
        {
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"), "0", ">"));
        }
        else if (!"ALL_APPLICABLE_TO_DIMENSION".equals(dimensionMode)) {}
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"), "0", "="));
      }
    }
    if (FormatHelper.hasContent(scMasterId))
    {
      if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "sourceDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(scMasterId)));
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendOpenParen();
        
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "sourceDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(scMasterId)));
        statement.appendOr();
        
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "sourceDimensionReference.key.id"), "0", "="));
        statement.appendClosedParen();
      }
    }
    else if (!"ALL_DIMENSIONS".equals(dimensionMode))
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "sourceDimensionReference.key.id"), "0", "="));
    }
    if (FormatHelper.hasContent(destDimId))
    {
      if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "destinationDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(destDimId)));
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendOpenParen();
        
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "destinationDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(destDimId)));
        statement.appendOr();
        
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "destinationDimensionReference.key.id"), "0", "="));
        statement.appendClosedParen();
      }
    }
    else if (!"ALL_DIMENSIONS".equals(dimensionMode))
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "destinationDimensionReference.key.id"), "0", "="));
    }
    if (FormatHelper.hasContentAllowZero(size1))
    {
      if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size1"), "?", "="), size1);
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendOpenParen();
        
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size1"), "?", "="), size1);
        statement.appendOr();
        
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size1"), "", "IS NULL"));
        statement.appendClosedParen();
      }
    }
    else if (!"ALL_DIMENSIONS".equals(dimensionMode)) {
      if (("ALL_SIZE1_AND_2".equals(sizeMode)) || ("ALL_SIZE1".equals(sizeMode)))
      {
        if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
        {
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size1"), "", "IS NOT NULL"));
        }
        else if (!"ALL_APPLICABLE_TO_DIMENSION".equals(dimensionMode)) {}
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size1"), "", "IS NULL"));
      }
    }
    if (FormatHelper.hasContentAllowZero(size2))
    {
      if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size2"), "?", "="), size2);
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendOpenParen();
        
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size2"), "?", "="), size2);
        statement.appendOr();
        
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size2"), "", "IS NULL"));
        statement.appendClosedParen();
      }
    }
    else if (!"ALL_DIMENSIONS".equals(dimensionMode)) {
      if (("ALL_SIZE1_AND_2".equals(sizeMode)) || ("ALL_SIZE2".equals(sizeMode)))
      {
        if ("DIMENSION_OVERRIDES_ONLY".equals(dimensionMode))
        {
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size2"), "", "IS NOT NULL"));
        }
        else if (!"ALL_APPLICABLE_TO_DIMENSION".equals(dimensionMode)) {}
      }
      else
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size2"), "", "IS NULL"));
      }
    }
    if (!linkDataOnly)
    {
      statement.appendFromTable("LatestIterLCSMaterial", "LCSMaterial");
      

      statement.appendJoin(new QueryColumn("LCSMaterial", "idA3masterReference(+)"), new QueryColumn(FlexBOMLink.class, "childReference.key.id"));
      statement.appendSelectColumn(new QueryColumn("LCSMaterial", "idA3masterReference"));
      statement.appendSelectColumn(new QueryColumn("LCSMaterial", "primaryImageURL"));
      statement.appendSelectColumn(new QueryColumn("LCSMaterial", "statestate"));
      statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
      statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "typeDefinitionReference.key.branchId"));
      statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "flexTypeIdPath"));
      if (materialType == null) {
        materialType = FlexTypeCache.getFlexTypeFromPath("Material");
      }
      statement.appendSelectColumn(new QueryColumn("LCSMaterial", materialType.getAttribute("ptcmaterialName").getColumnName()));
      
      FlexTypeGenerator flexg = new FlexTypeGenerator();
      flexg.setScope("MATERIAL");
      flexg.setLevel(null);
      

      statement = flexg.appendQueryColumns(materialType, statement, usedAttKeys);
      



      statement.appendFromTable(LCSSupplierMaster.class);
      statement.appendFromTable(LCSSupplier.class);
      statement.appendJoin(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"), new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"));
      statement.appendJoin(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"), new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
      statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
      statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "supplierName"));
      statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "iterationInfo.branchId"));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.latest"), "1", "="));
      
      flexg.setScope(null);
      flexg.setLevel(null);
      

      statement = flexg.appendQueryColumns(FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE), statement, usedAttKeys);
      
      statement.appendFromTable(LCSMaterialSupplier.class);
      statement.appendFromTable(LCSMaterialSupplierMaster.class);
      statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "state.state"));
      statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
      statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
      statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"), new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
      statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"), new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"));
      
      statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"), new QueryColumn(FlexBOMLink.class, "childReference.key.id"));
      
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
      
      flexg.setScope("MATERIAL-SUPPLIER");
      flexg.setLevel(null);
      

      statement = flexg.appendQueryColumns(materialType, statement, usedAttKeys);
      

      statement.appendFromTable(LCSColor.class);
      statement.appendJoin(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id(+)"), new QueryColumn(FlexBOMLink.class, "colorReference.key.id"));
      statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
      statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorName"));
      statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorHexidecimalValue"));
      statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thumbnail"));
      
      FlexType colorRoot = FlexTypeCache.getFlexTypeFromPath("Color");
      flexg.setScope(null);
      flexg.setLevel(null);
      
      statement = flexg.appendQueryColumns(colorRoot, statement, usedAttKeys);
      



      statement.appendFromTable(LCSMaterialColor.class);
      statement.appendJoin(new QueryColumn(LCSMaterialColor.class, "thePersistInfo.theObjectIdentifier.id(+)"), new QueryColumn(FlexBOMLink.class, "materialColorReference.key.id"));
      statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "thePersistInfo.theObjectIdentifier.id"));
      statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "state.state"));
      FlexType materialColorRoot = FlexTypeCache.getFlexTypeFromPath("Material Color");
      flexg.setScope(null);
      flexg.setLevel(null);
      
      statement = flexg.appendQueryColumns(materialColorRoot, statement, usedAttKeys);
      if (WCPART_ENABLED)
      {
        statement.appendFromTable("LatestIterWTPart", "WTPart");
        statement.appendJoin(new QueryColumn("WTPart", "branchIditerationInfo(+)"), new QueryColumn(FlexBOMLink.class, "wcPartReference.key.branchId"));
        statement.appendSelectColumn(new QueryColumn("WTPart", "branchIditerationInfo"));
      }
    }
    printStatement(statement);
    
    return statement;
  }
  
  private static Vector filterWIPOnly(Collection linkData)
  {
    Iterator iter = linkData.iterator();
    FlexObject obj = null;
    Hashtable table = new Hashtable();
    
    List droppedWipList = new ArrayList();
    while (iter.hasNext())
    {
      obj = (FlexObject)iter.next();
      if (!obj.getBoolean("FLEXBOMLINK.WIP")) {
        table.put(obj.getString("FLEXBOMLINK.DIMENSIONID"), obj);
      }
      if (obj.getBoolean("FLEXBOMLINK.DROPPED")) {
        table.remove(obj.getString("FLEXBOMLINK.DIMENSIONID"));
      }
      if ((obj.getBoolean("FLEXBOMLINK.DROPPED")) && (obj.getBoolean("FLEXBOMLINK.WIP"))) {
        droppedWipList.add(obj.getString("FLEXBOMLINK.BRANCHID"));
      }
    }
    iter = linkData.iterator();
    while (iter.hasNext())
    {
      obj = (FlexObject)iter.next();
      if (droppedWipList.contains(obj.getString("FLEXBOMLINK.BRANCHID"))) {
        table.remove(obj.getString("FLEXBOMLINK.DIMENSIONID"));
      } else if (obj.getBoolean("FLEXBOMLINK.WIP")) {
        table.put(obj.getString("FLEXBOMLINK.DIMENSIONID"), obj);
      }
    }
    return new Vector(table.values());
  }
  
  public static Collection mergeDimensionBOM(Collection data)
    throws WTException
  {
    return mergeDimensionBOM(data, null, null);
  }
  
  public static Collection mergeDimensionBOM(Collection data, Collection dimensionExclusions, FlexType materialType)
    throws WTException
  {
    Collection ovrOptions = MOAHelper.getMOACollection(BOM_OVR_ORDER);
    if (dimensionExclusions == null) {
      dimensionExclusions = new ArrayList();
    }
    Map dimensionMap = FlexObjectUtil.groupIntoCollections(data, "FLEXBOMLINK.DIMENSIONNAME");
    
    Collection productRows = (Collection)dimensionMap.get("");
    if (productRows == null) {
      return data;
    }
    Map productRowMap = FlexObjectUtil.hashCollection(productRows, "FLEXBOMLINK.BRANCHID");
    



    Iterator ovrIt = ovrOptions.iterator();
    String ovr = "";
    
    Collection overrideRows = null;
    while (ovrIt.hasNext())
    {
      ovr = (String)ovrIt.next();
      overrideRows = (Collection)dimensionMap.get(ovr);
      if ((overrideRows != null) && (!dimensionExclusions.contains(ovr))) {
        applyDimensionChanges(overrideRows, productRowMap, materialType);
      }
    }
    return productRowMap.values();
  }
  
  private static void applyDimensionChanges(Collection overrideRows, Map currentBOM, FlexType materialType)
    throws WTException
  {
    if (materialType == null) {
      materialType = FlexTypeCache.getFlexTypeRoot("Material");
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Applying Dimension Changes: " + overrideRows);
    }
    Iterator overrideIter = overrideRows.iterator();
    



    Collection materialKeys = new ArrayList();
    materialKeys.add("FLEXBOMLINK.IDA3B5");
    materialKeys.add("LCSMATERIAL.IDA3MASTERREFERENCE");
    materialKeys.add("LCSMATERIAL.PRIMARYIMAGEURL");
    materialKeys.add("LCSMATERIAL.STATESTATE");
    materialKeys.add("LCSSUPPLIERMASTER.IDA2A2");
    materialKeys.add("LCSSUPPLIERMASTER.SUPPLIERNAME");
    materialKeys.add("LCSMATERIALSUPPLIER.STATESTATE");
    materialKeys.add("LCSMATERIALSUPPLIERMASTER.IDA2A2");
    if (materialType == null) {
      materialType = FlexTypeCache.getFlexTypeFromPath("Material");
    }
    Collection matAttTableNames = new ArrayList();
    Collection matAttKeys = materialType.getAttributeKeyList();
    Iterator maki = matAttKeys.iterator();
    String attKey = "";
    while (maki.hasNext())
    {
      attKey = (String)maki.next();
      materialKeys.add(materialType.getAttribute(attKey).getSearchResultIndex().toUpperCase());
      matAttTableNames.add(materialType.getAttribute(attKey).getSearchResultsTableName().toUpperCase());
    }
    materialKeys.addAll(matAttKeys);
    
    Collection colorKeys = new ArrayList();
    colorKeys.add("LCSCOLOR.IDA2A2");
    colorKeys.add("LCSCOLOR.COLORHEXIDECIMALVALUE");
    colorKeys.add("LCSCOLOR.colorName");
    colorKeys.add("FLEXBOMLINK.IDA3D5");
    
    FlexType colorType = FlexTypeCache.getFlexTypeFromPath("Color");
    colorKeys.addAll(colorType.getAttributeKeyList());
    
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");
    
    String placeholderMaterialId = FormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID);
    while (overrideIter.hasNext())
    {
      FlexObject overrideRow = (FlexObject)overrideIter.next();
      FlexObject resultRow = (FlexObject)currentBOM.get(overrideRow.get("FLEXBOMLINK.BRANCHID"));
      if (resultRow != null)
      {
        boolean sameMaterial = overrideRow.getString("LCSMATERIAL.IDA3MASTERREFERENCE").equals(resultRow.getString("LCSMATERIAL.IDA3MASTERREFERENCE"));
        boolean sameSupplier = overrideRow.getString("LCSMATERIALSUPPLIERMASTER.IDA2A2").equals(resultRow.getString("LCSMATERIALSUPPLIERMASTER.IDA2A2"));
        boolean sameColor = overrideRow.getString("LCSCOLOR.IDA2A2").equals(resultRow.getString("LCSCOLOR.IDA2A2"));
        










        Collection keySet = overrideRow.keySet();
        



        String matDes = "FLEXBOMLINK." + FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("materialDescription").getColumnName();
        if ((placeholderMaterialId.equals(overrideRow.getString("FLEXBOMLINK.IDA3B5"))) && (!FormatHelper.hasContent(overrideRow.getString(matDes))))
        {
          keySet.removeAll(materialKeys);
          keySet.remove(matDes.toUpperCase());
        }
        if ((!FormatHelper.hasContent(overrideRow.getString("LCSCOLOR.IDA2A2"))) && 
          (FormatHelper.hasContent(overrideRow.getString(bomType.getAttribute("colorDescription").getSearchResultIndex()))))
        {
          Iterator keys = colorKeys.iterator();
          while (keys.hasNext())
          {
            String key = (String)keys.next();
            resultRow.put(key, "");
          }
        }
        if (keySet.contains("FLEXBOMLINK.SORTINGNUMBER")) {
          keySet.remove("FLEXBOMLINK.SORTINGNUMBER");
        }
        Iterator keys = keySet.iterator();
        while (keys.hasNext())
        {
          String key = (String)keys.next();
          String val = overrideRow.getString(key);
          String val_toplevel = resultRow.getString(key);
          if (((key.startsWith("FLEXBOMLINK.SIZE")) && (FormatHelper.hasContentAllowZero(val))) || 
            (key.equalsIgnoreCase("LCSCOLOR.THUMBNAIL")) || 
            (FormatHelper.hasContent(val))) {
            resultRow.put(key, val);
          }
          if ((!FormatHelper.hasContentAllowZero(val)) && (FormatHelper.hasContentAllowZero(val_toplevel))) {
            clearValueForOverrideRow(resultRow, key, sameMaterial, sameSupplier, sameColor, matAttTableNames);
          }
        }
      }
    }
  }
  
  private static void clearValueForOverrideRow(FlexObject resultRow, String key, boolean sameMaterial, boolean sameSupplier, boolean sameColor, Collection matAttTableNames)
  {
    if (!sameMaterial)
    {
      if ((matAttTableNames.contains(key.substring(0, key.indexOf(".")))) || (key.startsWith("LCSMATERIAL.")) || (key.startsWith("LCSMATERIALCOLOR.")) || (key.startsWith("LCSSUPPLIER.")) || (key.startsWith("LCSMATERIALSUPPLIER."))) {
        resultRow.put(key, "");
      }
    }
    else if (sameMaterial) {
      if (!sameSupplier)
      {
        if ((key.startsWith("LCSMATERIALCOLOR.")) || (key.startsWith("LCSSUPPLIER.")) || (key.startsWith("LCSMATERIALSUPPLIER."))) {
          resultRow.put(key, "");
        }
      }
      else if ((sameSupplier) && (!sameColor) && 
        (key.startsWith("LCSMATERIALCOLOR."))) {
        resultRow.put(key, "");
      }
    }
  }
  
  public Collection findBOMPartsForOwner(RevisionControlled owner)
    throws WTException
  {
    return findBOMPartsForOwner(owner, "MAIN");
  }
  
  public Collection findBOMPartsForOwner(RevisionControlled owner, String bomType)
    throws WTException
  {
    return findBOMPartsForOwner(owner, "A", bomType);
  }
  
  public Collection findBOMPartsForOwner(RevisionControlled owner, String version, String bomType)
    throws WTException
  {
    return findBOMPartsForOwner(owner, version, bomType, null);
  }
  
  public Collection findBOMPartsForOwner(RevisionControlled owner, String version, String bomType, FlexSpecification specification)
    throws WTException
  {
    return findBOMPartsForOwner(owner, version, bomType, specification, null);
  }
  
  public Collection findBOMPartsForOwner(RevisionControlled owner, String version, String bomType, FlexSpecification specification, String bomName)
    throws WTException
  {
    PreparedQueryStatement statement = findBOMPartsForOwnerQuery(owner, version, bomType, specification, bomName);
    return LCSQuery.getObjectsFromResults(statement, "VR:com.lcs.wc.flexbom.FlexBOMPart:", "FLEXBOMPART.BRANCHIDITERATIONINFO");
  }
  
  public Collection findBOMPartsForOwners(Collection<BOMOwner> owners, String version, String bomType, FlexSpecification specification, String bomName)
    throws WTException
  {
    PreparedQueryStatement statement = findBOMPartsForOwnersQuery(owners, version, bomType, null, specification, false, bomName);
    return LCSQuery.getObjectsFromResults(statement, "VR:com.lcs.wc.flexbom.FlexBOMPart:", "FLEXBOMPART.BRANCHIDITERATIONINFO");
  }
  
  public Collection findBOMPartsForOwner(RevisionControlled owner, String version, String bomType, LCSSourcingConfig source, FlexSpecification specification)
    throws WTException
  {
    PreparedQueryStatement statement = null;
    if (specification != null)
    {
      if ((owner instanceof LCSProduct)) {
        statement = findBOMPartsForOwnerQuery((LCSProduct)owner, version, bomType, source, specification);
      } else {
        statement = findBOMPartsForOwnerQuery((LCSMaterial)owner, version, bomType, source, specification);
      }
    }
    else if ((owner instanceof LCSProduct)) {
      statement = findBOMPartsForOwnerQuery((LCSProduct)owner, version, bomType, source, null);
    } else {
      statement = findBOMPartsForOwnerQuery((LCSMaterial)owner, version, bomType, source, null);
    }
    return LCSQuery.getObjectsFromResults(statement, "VR:com.lcs.wc.flexbom.FlexBOMPart:", "FLEXBOMPART.BRANCHIDITERATIONINFO");
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(RevisionControlled owner, String version, String bomType, FlexSpecification specification)
    throws WTException
  {
    return findBOMPartsForOwnerQuery((BOMOwner)owner.getMaster(), version, bomType, null, specification, false, null);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(RevisionControlled owner, String version, String bomType, FlexSpecification specification, String bomName)
    throws WTException
  {
    return findBOMPartsForOwnerQuery((BOMOwner)owner.getMaster(), version, bomType, null, specification, false, bomName);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(BOMOwner ownerMaster, String version, String bomType, FlexSpecification specification)
    throws WTException
  {
    return findBOMPartsForOwnerQuery(ownerMaster, version, bomType, null, specification, false);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(LCSProduct owner, String version, String bomType, LCSSourcingConfig source, FlexSpecification specification)
    throws WTException
  {
    return findBOMPartsForOwnerQuery(owner.getMaster(), version, bomType, source, specification, false);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(LCSMaterial owner, String version, String bomType, LCSSourcingConfig source, FlexSpecification specification)
    throws WTException
  {
    return findBOMPartsForOwnerQuery(owner.getMaster(), version, bomType, source, specification, false);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(BOMOwner ownerMaster, String version, boolean filterAccess)
    throws WTException
  {
    return findBOMPartsForOwnerQuery(ownerMaster, null, null, null, null, filterAccess);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(BOMOwner ownerMaster, String version, String bomType, LCSSourcingConfig source, FlexSpecification specification)
    throws WTException
  {
    return findBOMPartsForOwnerQuery(ownerMaster, version, bomType, source, specification, false);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(BOMOwner ownerMaster, String version, String bomType, LCSSourcingConfig source, FlexSpecification specification, boolean filterAccess)
    throws WTException
  {
    return findBOMPartsForOwnerQuery(ownerMaster, version, bomType, source, specification, filterAccess, null);
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnerQuery(BOMOwner ownerMaster, String version, String bomType, LCSSourcingConfig source, FlexSpecification specification, boolean filterAccess, String bomName)
    throws WTException
  {
    FlexType bomFlexType = FlexTypeCache.getFlexTypeFromPath("BOM");
    
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMPart.class);
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, bomFlexType.getAttribute("name").getColumnDescriptorName()));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "bomType"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"), "wrk", "<>"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "iterationInfo.latest"), "1", "="));
    statement.appendAndIfNeeded();
    if (FormatHelper.hasContent(bomName))
    {
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, bomFlexType.getAttribute("name").getColumnDescriptorName()), bomName, "="));
      statement.appendAndIfNeeded();
    }
    if (FormatHelper.hasContent(version))
    {
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "versionInfo.identifier.versionId"), version, "="));
      statement.appendAndIfNeeded();
    }
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject((WTObject)ownerMaster)));
    if (FormatHelper.hasContent(bomType))
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "bomType"), "?", "="), bomType);
    }
    if (specification != null)
    {
      statement.appendFromTable(FlexSpecToComponentLink.class);
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), "?", "="), FormatHelper.getNumericFromReference(specification.getMasterReference()));
    }
    else if (source != null)
    {
      statement.appendFromTable(FlexSpecToComponentLink.class);
      statement.appendFromTable("LatestIterFlexSpecification");
      
      statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn(FlexBOMPart.class, "versionInfo.identifier.versionId"), new QueryColumn(FlexSpecToComponentLink.class, "componentVersion"));
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), new QueryColumn("LatestIterFlexSpecification", "idA3masterReference"));
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn("LatestIterFlexSpecification", "versionIdA2versionInfo"), new QueryColumn(FlexSpecToComponentLink.class, "specVersion"));
      
      String sourceMasterId = FormatHelper.getNumericFromReference(source.getMasterReference());
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("LatestIterFlexSpecification", FlexSpecification.class, "specSourceReference.key.id"), "?", "="), Long.valueOf(sourceMasterId));
    }
    if (filterAccess)
    {
      FlexTypeGenerator ftg = new FlexTypeGenerator();
      ftg.setScope("BOM_SCOPE");
      ftg.addFlexTypeCriteria(statement, bomFlexType, null);
    }
    return statement;
  }
  
  public static PreparedQueryStatement findBOMPartsForOwnersQuery(Collection<BOMOwner> ownerMasters, String version, String bomType, LCSSourcingConfig source, FlexSpecification specification, boolean filterAccess, String bomName)
    throws WTException
  {
    FlexType bomFlexType = FlexTypeCache.getFlexTypeFromPath("BOM");
    
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMPart.class);
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, bomFlexType.getAttribute("name").getColumnDescriptorName()));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "bomType"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"), "wrk", "<>"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "iterationInfo.latest"), "1", "="));
    statement.appendAndIfNeeded();
    if (FormatHelper.hasContent(bomName))
    {
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, bomFlexType.getAttribute("name").getColumnDescriptorName()), bomName, "="));
      statement.appendAndIfNeeded();
    }
    if (FormatHelper.hasContent(version))
    {
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "versionInfo.identifier.versionId"), version, "="));
      statement.appendAndIfNeeded();
    }
    statement.appendOpenParen();
    for (BOMOwner ownerMaster : ownerMasters)
    {
      statement.appendOrIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(ownerMaster)));
    }
    statement.appendClosedParen();
    if (FormatHelper.hasContent(bomType))
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "bomType"), "?", "="), bomType);
    }
    if (specification != null)
    {
      statement.appendFromTable(FlexSpecToComponentLink.class);
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), "?", "="), FormatHelper.getNumericFromReference(specification.getMasterReference()));
    }
    else if (source != null)
    {
      statement.appendFromTable(FlexSpecToComponentLink.class);
      statement.appendFromTable("LatestIterFlexSpecification");
      
      statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn(FlexBOMPart.class, "versionInfo.identifier.versionId"), new QueryColumn(FlexSpecToComponentLink.class, "componentVersion"));
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), new QueryColumn("LatestIterFlexSpecification", "idA3masterReference"));
      statement.appendAndIfNeeded();
      statement.appendJoin(new QueryColumn("LatestIterFlexSpecification", "versionIdA2versionInfo"), new QueryColumn(FlexSpecToComponentLink.class, "specVersion"));
      
      String sourceMasterId = FormatHelper.getNumericFromReference(source.getMasterReference());
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn("LatestIterFlexSpecification", FlexSpecification.class, "specSourceReference.key.id"), "?", "="), Long.valueOf(sourceMasterId));
    }
    if (filterAccess)
    {
      FlexTypeGenerator ftg = new FlexTypeGenerator();
      ftg.setScope("BOM_SCOPE");
      ftg.addFlexTypeCriteria(statement, bomFlexType, null);
    }
    return statement;
  }
  
  public Map findAvailableMaterialColorMapForBOM(FlexBOMPart part)
    throws WTException
  {
    return findAvailableMaterialColorMapForBOM(part, true, false);
  }
  
  public Map findAvailableMaterialColorMapForBOM(FlexBOMPart part, boolean applyFlexCriteria, boolean returnOnlyInBOM)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.setDistinct(true);
    statement.appendFromTable(FlexBOMLink.class);
    
    statement.appendFromTable(LCSMaterialMaster.class, "MATERIALMASTER");
    statement.appendFromTable(LCSColor.class);
    statement.appendFromTable(LCSMaterialSupplierMaster.class);
    statement.appendFromTable("V_LCSMaterialColor", "LCSMaterialColor");
    
    statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn("LCSMaterialColor", "idA2A2"));
    statement.appendSelectColumn(new QueryColumn("LCSMaterialColor", "statestate"));
    

    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorName"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorHexidecimalValue"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thumbnail"));
    statement.appendSelectColumn(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "name"));
    statement.appendSelectColumn(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(part.getMaster()))));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentRev"), "?", "="), part.getVersionIdentifier().getValue());
    

    statement.appendJoin(new QueryColumn("LCSMaterialColor", "idA3A10"), new QueryColumn(FlexBOMLink.class, "childReference.key.id"));
    


    statement.appendJoin(new QueryColumn("LCSMaterialColor", "idA3A10"), new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendJoin(new QueryColumn("LCSMaterialColor", "idA3B10"), new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
    if (returnOnlyInBOM) {
      statement.appendJoin(new QueryColumn(LCSMaterialColor.class, "thePersistInfo.theObjectIdentifier.id"), new QueryColumn(FlexBOMLink.class, "materialColorReference.key.id"));
    }
    statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"), new QueryColumn("LCSMaterialColor", "idA3A10"));
    statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"), new QueryColumn("LCSMaterialColor", "idA3C10"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "name"), "material_placeholder", "<>"));
    

    FlexType materialColorRootType = FlexTypeCache.getFlexTypeFromPath("Material Color");
    addFlexTypeInformation(statement, LCSMaterialColor.class);
    if (applyFlexCriteria) {
      new FlexTypeGenerator().addFlexTypeCriteria(statement, materialColorRootType, null);
    }
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    


    FlexTypeGenerator flexg = new FlexTypeGenerator();
    
    statement = flexg.appendQueryColumns(materialColorRootType, statement);
    
    SearchResults results = LCSQuery.runDirectQuery(statement);
    Map map = FlexObjectUtil.groupIntoCollections(results.getResults(), "LCSMATERIALSUPPLIERMASTER.IDA2A2");
    return map;
  }
  
  public static FlexBOMLink findTopLevelBranch(FlexBOMLink link)
    throws WTException
  {
    return findTopLevelBranch(link, false);
  }
  
  public static FlexBOMLink findTopLevelBranch(FlexBOMLink link, boolean includeDropped)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromReference(link.getParentReference())));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentRev"), "?", "="), link.getParentRev());
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "branchId"), "?", "="), new Integer(link.getBranchId()));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    if (!includeDropped)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    }
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dimensionName"), "0", "IS NULL"));
    
    statement.appendSortBy(new QueryColumn(FlexBOMLink.class, "wip"), "DESC");
    
    return (FlexBOMLink)LCSQuery.getObjectFromResults(statement, "OR:com.lcs.wc.flexbom.FlexBOMLink:", "FLEXBOMLINK.IDA2A2");
  }
  
  public static FlexBOMLink findTopLevelBranch2(FlexBOMLink link, boolean includeDropped)
    throws WTException
  {
    QuerySpec lQS = new QuerySpec(FlexBOMLink.class);
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "parentReference.key.id", "=", link.getParent().getPersistInfo().getObjectIdentifier().getId()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "parentRev", "=", link.getParentRev()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "branchId", "=", link.getBranchId()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "outDate", true), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "dropped", "FALSE"), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "dimensionName", true), new int[] { 0 });
    lQS.appendOrderBy(new OrderBy(new ClassAttribute(FlexBOMLink.class, "wip"), true), new int[] { 0 });
    
    QueryResult result = PersistenceHelper.manager.find(lQS);
    if (result.hasMoreElements()) {
      return (FlexBOMLink)result.nextElement();
    }
    return null;
  }
  
  private static String SOURCE = ":SOURCE";
  private static String SKU = ":SKU";
  private static String SIZE1 = ":SIZE1";
  private static String SIZE2 = ":SIZE2";
  private static String DESTINATON = ":DESTINATON";
  
  public static FlexBOMLink findNextApplicableLink(FlexBOMLink link)
    throws WTException
  {
    Vector<String> bomOverrideVector = getOrderedBOMOverride(link.getDimensionName(), getOrderedBOMOverride());
    for (String dimensionName : bomOverrideVector)
    {
      Long scMasterId = null;
      Long skuMasterId = null;
      String size1 = null;
      String size2 = null;
      Long destDimId = null;
      if (dimensionName.contains(SOURCE)) {
        scMasterId = Long.valueOf(link.getSourceDimension().getPersistInfo().getObjectIdentifier().getId());
      }
      if (dimensionName.contains(SKU)) {
        skuMasterId = Long.valueOf(link.getColorDimension().getPersistInfo().getObjectIdentifier().getId());
      }
      if (dimensionName.contains(SIZE1)) {
        size1 = link.getSize1();
      }
      if (dimensionName.contains(SIZE2)) {
        size2 = link.getSize2();
      }
      if (dimensionName.contains(DESTINATON)) {
        destDimId = Long.valueOf(link.getDestinationDimension().getPersistInfo().getObjectIdentifier().getId());
      }
      FlexBOMLink overrideLink = findFlexBOMLinkForBOMOverride(link, false, dimensionName, scMasterId, skuMasterId, size1, size2, destDimId);
      if (overrideLink != null) {
        return overrideLink;
      }
    }
    return findTopLevelBranch(link, false);
  }
  
  private static Vector<String> orderedBOMOverride = (Vector)MOAHelper.getMOACollection(BOM_OVR_ORDER);
  
  public static Vector<String> getOrderedBOMOverride()
  {
    return orderedBOMOverride;
  }
  
  private static HashMap<String, Vector<String>> bomOverrideMap = null;
  
  public static Vector<String> getOrderedBOMOverride(String dimensionName, Vector<String> orderedBOMOverride)
  {
    if ((dimensionName == null) || (dimensionName.isEmpty())) {
      return null;
    }
    int dim_name_pos = orderedBOMOverride.indexOf(dimensionName);
    if (dim_name_pos < 0) {
      return null;
    }
    if (bomOverrideMap != null) {
      return (Vector)bomOverrideMap.get(dimensionName);
    }
    bomOverrideMap = processBOMOverrideMap(orderedBOMOverride);
    return (Vector)bomOverrideMap.get(dimensionName);
  }
  
  private static String DIMENSION_NAME_TOKEN = ":";
  private static final String COLORDIMCOLUMN = "IDA3E5";
  private static final String SOURCEDIMCOLUMN = "IDA3F5";
  private static final String MATERIALREFID = "IDA3B5";
  private static final String SUPPLIERREFID = "IDA3C5";
  private static final String COLORREFID = "IDA3D5";
  private static final String DESTINATIONDIMCOLUMN = "IDA3H5";
  public static final String WIP_ONLY = "WIP_ONLY";
  public static final String WIP_AND_EFFECTIVE = "WIP_AND_EFFECTIVE";
  public static final String EFFECTIVE_ONLY = "EFFECTIVE_ONLY";
  public static final String ALL_DIMENSIONS = "ALL_DIMENSIONS";
  public static final String ALL_APPLICABLE_TO_DIMENSION = "ALL_APPLICABLE_TO_DIMENSION";
  public static final String DIMENSION_OVERRIDES_ONLY = "DIMENSION_OVERRIDES_ONLY";
  public static final String ALL_SKUS = "ALL_SKUS";
  public static final String ALL_SOURCES = "ALL_SOURCES";
  public static final String ALL_SIZE1 = "ALL_SIZE1";
  public static final String ALL_SIZE2 = "ALL_SIZE2";
  public static final String ALL_SIZE1_AND_2 = "ALL_SIZE1_AND_2";
  public static final String ALL_SIZE1_OR_2 = "ALL_SIZE1_OR_2";
  
  private static HashMap<String, Vector<String>> processBOMOverrideMap(Vector<String> orderedBOMOverride)
  {
    Vector<Vector<String>> processsedBOMOverrideVector = processBOMOverride(orderedBOMOverride);
    

    HashMap<String, Vector<String>> processsedBOMOverrideMap = new HashMap(processsedBOMOverrideVector.size());
    for (int posToProcess = 0; posToProcess < processsedBOMOverrideVector.size(); posToProcess++)
    {
      Vector<String> bomOverrideVector = (Vector)processsedBOMOverrideVector.get(posToProcess);
      

      Vector<String> bomOverrideNumberVector = new Vector();
      for (int pos = posToProcess - 1; pos >= 0; pos--)
      {
        Vector<String> currentBOMOverrideVector = (Vector)processsedBOMOverrideVector.get(pos);
        if (bomOverrideVector.containsAll(currentBOMOverrideVector)) {
          bomOverrideNumberVector.add(orderedBOMOverride.get(pos));
        }
      }
      processsedBOMOverrideMap.put(orderedBOMOverride.get(posToProcess), bomOverrideNumberVector);
    }
    return processsedBOMOverrideMap;
  }
  
  private static Vector<Vector<String>> processBOMOverride(Vector<String> orderedBOMOverride)
  {
    Vector<Vector<String>> processsedBOMOverrideVector = new Vector(orderedBOMOverride.size());
    for (String dimensionMame : orderedBOMOverride)
    {
      StringTokenizer st = new StringTokenizer(dimensionMame, DIMENSION_NAME_TOKEN);
      Vector<String> bomOverrideVector = new Vector(st.countTokens());
      while (st.hasMoreTokens()) {
        bomOverrideVector.add(st.nextToken());
      }
      processsedBOMOverrideVector.add(bomOverrideVector);
    }
    return processsedBOMOverrideVector;
  }
  
  public static FlexBOMLink findFlexBOMLinkForBOMOverride2(FlexBOMLink link, boolean includeDropped, String dimensionName, Long scMasterId, Long skuMasterId, String size1, String size2, Long destDimId)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMLink.class);
    QueryColumn oidColumn = new QueryColumn(FlexBOMLink.class, "iterationInfo.branchId");
    statement.appendSelectColumn(oidColumn);
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), Long.valueOf(link.getParent().getPersistInfo().getObjectIdentifier().getId()));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentRev"), "?", "="), link.getParentRev());
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "branchId"), "?", "="), new Integer(link.getBranchId()));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    if (!includeDropped)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    }
    statement.appendAndIfNeeded();
    if ((dimensionName != null) && (!dimensionName.isEmpty())) {
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dimensionName"), dimensionName, "="));
    } else {
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dimensionName"), "0", "IS NULL"));
    }
    if (scMasterId != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "sourceDimensionReference.key.id"), "?", "="), scMasterId);
    }
    if (skuMasterId != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"), "?", "="), skuMasterId);
    }
    if (size1 != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size1"), "?", "="), size1);
    }
    if (size2 != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size2"), "?", "="), size2);
    }
    if (destDimId != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "destinationDimensionReference.key.id"), "?", "="), destDimId);
    }
    statement.appendSortBy(new QueryColumn(FlexBOMLink.class, "wip"), "DESC");
    
    return (FlexBOMLink)LCSQuery.getObjectFromResults(statement, FlexBOMLink.class, oidColumn);
  }
  
  public static FlexBOMLink findFlexBOMLinkForBOMOverride(FlexBOMLink link, boolean includeDropped, String dimensionName, Long scMasterId, Long skuMasterId, String size1, String size2, Long destDimId)
    throws WTException
  {
    QuerySpec lQS = new QuerySpec(FlexBOMLink.class);
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "parentReference.key.id", "=", link.getParent().getPersistInfo().getObjectIdentifier().getId()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "parentRev", "=", link.getParentRev()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "branchId", "=", link.getBranchId()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "outDate", true), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "dropped", "FALSE"), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "dimensionName", "=", dimensionName), new int[] { 0 });
    if (scMasterId != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "sourceDimensionReference.key.id", "=", scMasterId), new int[] { 0 });
    }
    if (skuMasterId != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "colorDimensionReference.key.id", "=", skuMasterId), new int[] { 0 });
    }
    if (size1 != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "size1", "=", size1), new int[] { 0 });
    }
    if (size2 != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "size2", "=", size2), new int[] { 0 });
    }
    if (destDimId != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "destinationDimensionReference.key.id", "=", destDimId), new int[] { 0 });
    }
    lQS.appendOrderBy(new OrderBy(new ClassAttribute(FlexBOMLink.class, "wip"), true), new int[] { 0 });
    
    QueryResult result = PersistenceHelper.manager.find(lQS);
    if (result.hasMoreElements()) {
      return (FlexBOMLink)result.nextElement();
    }
    return null;
  }
  
  public static Collection<FlexBOMLink> findAllFlexBOMLinkRelatedToBOMOverride(FlexBOMLink link, boolean includeDropped, String dimensionName, Long scMasterId, Long skuMasterId, String size1, String size2, Long destDimId)
    throws WTException
  {
    QuerySpec lQS = new QuerySpec(FlexBOMLink.class);
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "parentReference.key.id", "=", link.getParent().getPersistInfo().getObjectIdentifier().getId()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "parentRev", "=", link.getParentRev()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "branchId", "=", link.getBranchId()), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "outDate", true), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "dropped", "FALSE"), new int[] { 0 });
    lQS.appendAnd();
    lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "dimensionName", "LIKE", "%" + dimensionName + "%"), new int[] { 0 });
    if (scMasterId != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "sourceDimensionReference.key.id", "=", scMasterId), new int[] { 0 });
    }
    if (skuMasterId != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "colorDimensionReference.key.id", "=", skuMasterId), new int[] { 0 });
    }
    if (size1 != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "size1", "=", size1), new int[] { 0 });
    }
    if (size2 != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "size2", "=", size2), new int[] { 0 });
    }
    if (destDimId != null)
    {
      lQS.appendAnd();
      lQS.appendWhere(new SearchCondition(FlexBOMLink.class, "destinationDimensionReference.key.id", "=", destDimId), new int[] { 0 });
    }
    lQS.appendOrderBy(new OrderBy(new ClassAttribute(FlexBOMLink.class, "wip"), true), new int[] { 0 });
    
    QueryResult result = PersistenceHelper.manager.find(lQS);
    Collection<FlexBOMLink> bomLinkCollection = new ArrayList();
    while (result.hasMoreElements()) {
      bomLinkCollection.add((FlexBOMLink)result.nextElement());
    }
    return bomLinkCollection;
  }
  
  public static Collection getAllLinksForBranch(FlexBOMLink link)
    throws WTException
  {
    PreparedQueryStatement statement = getAllLinksForBranchQuery(link);
    return LCSQuery.getObjectsFromResults(statement, "OR:com.lcs.wc.flexbom.FlexBOMLink:", "FLEXBOMLINK.IDA2A2");
  }
  
  private static PreparedQueryStatement getAllLinksForBranchQuery(FlexBOMLink link)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromReference(link.getParentReference())));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentRev"), "?", "="), link.getParentRev());
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "branchId"), "?", "="), new Integer(link.getBranchId()));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    return statement;
  }
  
  public static FlexBOMLink getToplevelLinkForBranch(FlexBOMLink link)
    throws WTException
  {
    if (!FormatHelper.hasContentAllowZero(link.getDimensionName())) {
      return link;
    }
    PreparedQueryStatement statement = getAllLinksForBranchQuery(link);
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dimensionName"), "", "IS NULL"));
    
    return (FlexBOMLink)((Vector)LCSQuery.getObjectsFromResults(statement, "OR:com.lcs.wc.flexbom.FlexBOMLink:", "FLEXBOMLINK.IDA2A2")).firstElement();
  }
  
  public static Collection getMissingColorsForOverridesForBranch(FlexBOMLink link, LCSMaterialMaster material, LCSSupplierMaster supplier)
    throws WTException
  {
    PreparedQueryStatement colorStatement = new PreparedQueryStatement();
    colorStatement.appendFromTable(LCSMaterialColor.class);
    
    colorStatement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "colorReference.key.id"));
    
    colorStatement.appendAndIfNeeded();
    colorStatement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialColor.class, "materialMasterReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(material)));
    
    colorStatement.appendAndIfNeeded();
    colorStatement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialColor.class, "supplierMasterReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(supplier)));
    






    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMLink.class);
    
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "colorReference.key.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromReference(link.getParentReference())));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentRev"), "?", "="), link.getParentRev());
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "branchId"), "?", "="), new Integer(link.getBranchId()));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dimensionName"), "", "IS NOT NULL"));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorReference.key.id"), "", "IS NOT NULL"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorReference.key.id"), "0", "<>"));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), LCSMaterialQuery.PLACEHOLDERNUMERIC, "="));
    



    statement.appendNotInCriteria(new QueryColumn(FlexBOMLink.class, "colorReference.key.id"), colorStatement);
    
    return LCSQuery.runDirectQuery(statement).getResults();
  }
  
  public SearchResults getUniqueColorsUsedInBOM(FlexBOMPart part)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.setDistinct(true);
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendFromTable(LCSColor.class);
    
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorHexidecimalValue"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorName"));
    statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thumbnail"));
    
    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "colorReference.key.id"), new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromReference(part.getMasterReference())));
    
    return LCSQuery.runDirectQuery(statement);
  }
  
  public static boolean existDuplicatedBOMName(FlexBOMPart bom)
    throws WTException
  {
    String bomName = null;
    String bomNameWithNewNumber = null;
    boolean isNewBOM;

    if (!PersistenceHelper.isPersistent(bom)) {
      isNewBOM = Boolean.TRUE.booleanValue();
    } else {
      isNewBOM = Boolean.FALSE.booleanValue();
    }
    BOMOwner ownerMaster = bom.getOwnerMaster();
    FlexTypeAttribute numAtt = bom.getFlexType().getAttribute(BOM_NUM_KEY);
    FlexTypeAttribute nameAtt = bom.getFlexType().getAttribute(BOM_NAME_KEY);
    String numColumnDescriptor = numAtt.getColumnDescriptorName();
    String numAttColumn = numAtt.getColumnName();
    String nameAttColumn = nameAtt.getColumnName();
    
    PreparedQueryStatement statement = findBOMPartsForOwnerQuery(ownerMaster, "A", bom.getBomType(), null);
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, numColumnDescriptor));
    statement.appendSortBy(new QueryColumn(FlexBOMPart.class, numColumnDescriptor));
    statement.appendSortBy(new QueryColumn(FlexBOMPart.class, "thePersistInfo.createStamp"));
    
    Vector<FlexObject> results = runDirectQuery(statement).getResults();
    if (results.size() > 0)
    {
      bomName = bom.getName();
      bomNameWithNewNumber = getNextNumberForBOM(bom) + SourceComponentNumberPlugin.NUM_NAME_DELIM + bomName;
      for (FlexObject flexObj : results)
      {
        String tempBOMName = flexObj.getString("FLEXBOMPART." + nameAttColumn.toUpperCase());
        if ((isNewBOM) || (!bom.getValue(BOM_NUM_KEY).equals(flexObj.getString("FLEXBOMPART." + numAttColumn.toUpperCase())))) {
          if ((tempBOMName.equals(bomName)) || ((isNewBOM) && (tempBOMName.equals(bomNameWithNewNumber))))
          {
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Duplicated BOM name found '" + tempBOMName + "'!");
            }
            Object[] objB = { bomName };
            throw new LCSException("com.lcs.wc.resource.ExceptionRB", "mayNotCreateTwoBOMsForProduct_MSG", objB);
          }
        }
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("No conflict with BOM name '" + bomName + "' for this product");
      }
    }
    return false;
  }
  
  public static String getNextNumberForBOM(FlexBOMPart bom)
    throws WTException
  {
    BOMOwner ownerMaster = bom.getOwnerMaster();
    
    FlexTypeAttribute numAtt = bom.getFlexType().getAttribute(BOM_NUM_KEY);
    String attColumn = numAtt.getColumnName();
    String attColumnDescriptor = numAtt.getColumnDescriptorName();
    
    PreparedQueryStatement statement = findBOMPartsForOwnerQuery(ownerMaster, "A", bom.getBomType(), null);
    
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, attColumnDescriptor));
    
    statement.appendSortBy(new QueryColumn(FlexBOMPart.class, attColumnDescriptor));
    
    statement.appendSortBy(new QueryColumn(FlexBOMPart.class, "thePersistInfo.createStamp"));
    
    Vector results = runDirectQuery(statement).getResults();
    String lastNum = "";
    if (results.size() > 0)
    {
      FlexObject last = (FlexObject)results.lastElement();
      lastNum = last.getString("FLEXBOMPART." + attColumn.toUpperCase());
      if (!FormatHelper.hasContent(lastNum)) {
        while ((results.size() > 0) && (!FormatHelper.hasContent(lastNum)))
        {
          results.removeElementAt(results.size() - 1);
          if (results.size() > 0)
          {
            last = (FlexObject)results.lastElement();
            lastNum = last.getString("FLEXBOMPART." + attColumn.toUpperCase());
          }
        }
      }
    }
    int nextNum = 1;
    if (FormatHelper.hasContent(lastNum)) {
      nextNum = Integer.parseInt(lastNum) + 1;
    }
    return FormatHelper.prefixZeros(nextNum, 3);
  }
  
  public Collection getMaterialColorStatusForProduct(LCSProduct product, FlexSpecification specification, ApplicationContext appContext)
    throws WTException
  {
    String unInitiated = WTMessage.getLocalizedMessage("com.lcs.wc.resource.FlexBOMRB", "unInitiated_LBL", RB.objA);
    Collection materialColorsUsed = new ArrayList();
    Set distinctMatColsUsed = new HashSet();
    Collection colorsUsed = new ArrayList();
    
    Collection skus = appContext.getSKUs();
    ClientContext cc = ClientContext.getContext();
    if ((cc.isVendor) && (specification == null)) {
      return materialColorsUsed;
    }
    Collection sources = appContext.getSources();
    Collection skuMasterIds = new ArrayList();
    Collection sourceMasterIds = new ArrayList();
    Collection size1Options = new ArrayList();
    
    size1Options.add("30");
    size1Options.add("32");
    size1Options.add("34");
    
    Iterator iter = skus.iterator();
    while (iter.hasNext())
    {
      LCSSKU sku = (LCSSKU)iter.next();
      skuMasterIds.add(FormatHelper.getNumericFromReference(sku.getMasterReference()));
    }
    iter = sources.iterator();
    while (iter.hasNext())
    {
      LCSSourcingConfig sourceConfig = (LCSSourcingConfig)iter.next();
      sourceMasterIds.add(FormatHelper.getNumericFromReference(sourceConfig.getMasterReference()));
    }
    new LCSFlexBOMQuery();Collection productBOMs = findBOMObjects(product, appContext.getSourcingConfig(), specification, "MAIN");
    productBOMs = SortHelper.sortAllowDup(productBOMs, "name");
    Iterator bomIter = productBOMs.iterator();
    while (bomIter.hasNext())
    {
      FlexBOMPart bomPart = (FlexBOMPart)bomIter.next();
      colorsUsed.addAll(new LCSFlexBOMQuery().getUniqueColorsUsedInBOM(bomPart).getResults());
      
      Map materialColorCombos = new LCSFlexBOMQuery().getUniqueMaterialColorCombinationForBOM(bomPart, skuMasterIds, sourceMasterIds, size1Options, null);
      
      Iterator matColComboIter = materialColorCombos.keySet().iterator();
      while (matColComboIter.hasNext())
      {
        String brId = "" + matColComboIter.next();
        Collection materialColors = (Collection)materialColorCombos.get(brId);
        Iterator materialColorIter = materialColors.iterator();
        while (materialColorIter.hasNext())
        {
          MaterialColorInfo matColInfo = (MaterialColorInfo)materialColorIter.next();
          if ((matColInfo != null) && 
            (FormatHelper.hasContent(matColInfo.colorId))) {
            if ((FormatHelper.hasContent(matColInfo.materialSupplierId)) && (FormatHelper.hasContent(matColInfo.materialId))) {
              if (!distinctMatColsUsed.contains(matColInfo.materialId + "_" + matColInfo.materialSupplierId + "_" + matColInfo.colorId))
              {
                FlexObject obj = new FlexObject();
                obj.put("LCSCOLOR.NAME", matColInfo.colorName);
                obj.put("LCSCOLOR.IDA2A2", matColInfo.colorId);
                obj.put("LCSMATERIAL.IDA3MASTERREFERENCE", matColInfo.materialId);
                obj.put("MATERILANAME", matColInfo.materialName);
                obj.put("LCSMATERIALSUPPLIERMASTER.IDA2A2", matColInfo.materialSupplierId);
                obj.put("LCSSUPPLIER.SUPPLIERNAME", matColInfo.supplierName);
                obj.put("LCSMATERIALCOLOR.IDA2A2", "--");
                obj.put("LCSMATERIALCOLOR.STATESTATE", unInitiated);
                
                distinctMatColsUsed.add(matColInfo.materialId + "_" + matColInfo.materialSupplierId + "_" + matColInfo.colorId);
                materialColorsUsed.add(obj);
              }
            }
          }
        }
      }
    }
    TableDataUtil.appendConstantString(materialColorsUsed, "LCSMATERIALSUPPLIERMASTER.IDA2A2", "_", "MATERIALSUPPLIERID_COLORID");
    TableDataUtil.concatIndexes(materialColorsUsed, "MATERIALSUPPLIERID_COLORID", "LCSCOLOR.IDA2A2", "MATERIALSUPPLIERID_COLORID");
    Collection existingMaterialColors = new LCSMaterialColorQuery().getMaterialColorDataForFromMaterialSupplierColorList(materialColorsUsed).getResults();
    
    TableDataUtil.appendConstantString(existingMaterialColors, "LCSMATERIALCOLOR.ida3d10", "_", "MATERIALSUPPLIERID_COLORID");
    TableDataUtil.concatIndexes(existingMaterialColors, "MATERIALSUPPLIERID_COLORID", "LCSMATERIALCOLOR.ida3b10", "MATERIALSUPPLIERID_COLORID");
    
    TableDataUtil.join(materialColorsUsed, existingMaterialColors, "MATERIALSUPPLIERID_COLORID", "MATERIALSUPPLIERID_COLORID", true);
    TableDataUtil.join(materialColorsUsed, colorsUsed, "LCSCOLOR.IDA2A2", "LCSCOLOR.IDA2A2", true);
    materialColorsUsed = SortHelper.sortFlexObjects(materialColorsUsed, "COLORNAME");
    materialColorsUsed = SortHelper.sortFlexObjects(materialColorsUsed, "MATERILANAME");
    
    return materialColorsUsed;
  }
  
  public Map getUniqueMaterialColorCombinationForBOM(FlexBOMPart bomPart, Collection colorDimOptions, Collection sourceDimOptions, Collection size1DimOptions, Collection size2DimOptions)
    throws WTException
  {
    Collection allLinks = findFlexBOMData(bomPart, null, null, null, null, null, "WIP_ONLY", null, false, false, "ALL_DIMENSIONS", null, null, null).getResults();
    
    return getUniqueMaterialColorCombinationForBOM(allLinks, colorDimOptions, sourceDimOptions, size1DimOptions, size2DimOptions);
  }
  
  public Map getUniqueMaterialColorCombinationForBOM(Collection bomData, Collection colorDimOptions, Collection sourceDimOptions, Collection size1DimOptions, Collection size2DimOptions)
    throws WTException
  {
    Map materialColorCombos = new HashMap();
    
    Map branchMap = FlexObjectUtil.groupIntoCollections(bomData, "FLEXBOMLINK.BRANCHID");
    
    Iterator branchIter = branchMap.keySet().iterator();
    while (branchIter.hasNext())
    {
      String branchId = (String)branchIter.next();
      Collection branchCollection = (Collection)branchMap.get(branchId);
      if (branchCollection.size() > 1)
      {
        materialColorCombos.put(branchId, getUniqueMaterialColorCombinationsForBranch(branchCollection, colorDimOptions, sourceDimOptions, size1DimOptions, size2DimOptions));
      }
      else
      {
        FlexObject link = (FlexObject)branchCollection.iterator().next();
        if (((FormatHelper.hasContent(link.getString("FLEXBOMLINK." + "IDA3B5"))) && (!FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER).equals(link.getString("FLEXBOMLINK." + "IDA3B5")))) || 
          (FormatHelper.hasContent(link.getString("FLEXBOMLINK." + "IDA3D5"))))
        {
          Collection productLinkCombo = new ArrayList();
          productLinkCombo.add(bomInfoToMaterialColorInfo(link));
          materialColorCombos.put(branchId, productLinkCombo);
        }
      }
    }
    return materialColorCombos;
  }
  
  public Set getUniqueMaterialColorCombinationsForBranch(Collection branchCollection, Collection colorDimOptions, Collection sourceDimOptions, Collection size1DimOptions, Collection size2DimOptions)
    throws WTException
  {
    boolean debug = false;
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: colorDimOptions = " + colorDimOptions);
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: sourceDimOptions = " + sourceDimOptions);
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: size1DimOptions = " + size1DimOptions);
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: size2DimOptions = " + size2DimOptions);
    }
    Set materialColorCombos = new HashSet();
    
    Set colorDimsWithOvers = new HashSet();
    Set sourceDimsWithOvers = new HashSet();
    Set size1DimsWithOvers = new HashSet();
    Set size2DimsWithOvers = new HashSet();
    


    Iterator allLinksIter = branchCollection.iterator();
    
    Set relevantLinks = new HashSet();
    
    FlexObject link = null;
    FlexObject topLevelLink = null;
    while (allLinksIter.hasNext())
    {
      link = (FlexObject)allLinksIter.next();
      if (((FormatHelper.hasContent(link.getString("FLEXBOMLINK." + "IDA3B5"))) && (!FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER).equals(link.getString("FLEXBOMLINK." + "IDA3B5")))) || 
        (FormatHelper.hasContent(link.getString("FLEXBOMLINK." + "IDA3D5"))))
      {
        if (FormatHelper.hasContent(link.getString("FLEXBOMLINK." + "IDA3E5"))) {
          colorDimsWithOvers.add(link.getString("FLEXBOMLINK." + "IDA3E5"));
        }
        if (FormatHelper.hasContent(link.getString("FLEXBOMLINK." + "IDA3F5"))) {
          sourceDimsWithOvers.add(link.getString("FLEXBOMLINK." + "IDA3F5"));
        }
        if (FormatHelper.hasContentAllowZero(link.getString("FLEXBOMLINK.SIZE1"))) {
          size1DimsWithOvers.add(link.getString("FLEXBOMLINK.SIZE1"));
        }
        if (FormatHelper.hasContentAllowZero(link.getString("FLEXBOMLINK.SIZE2"))) {
          size2DimsWithOvers.add(link.getString("FLEXBOMLINK.SIZE2"));
        }
        relevantLinks.add(link);
      }
      if (!FormatHelper.hasContent(link.getString("FLEXBOMLINK.DIMENSIONNAME"))) {
        topLevelLink = link;
      }
    }
    if ((relevantLinks.size() == 0) || (topLevelLink == null))
    {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: no links found with material or color data for branch");
      }
      return materialColorCombos;
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: colorDimsWithOvers = " + colorDimsWithOvers);
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: sourceDimsWithOvers = " + sourceDimsWithOvers);
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: size1DimsWithOvers = " + size1DimsWithOvers);
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: size2DimsWithOvers = " + size2DimsWithOvers);
    }
    int dimsWithOvers = 0;
    if (colorDimsWithOvers.size() > 0) {
      dimsWithOvers++;
    }
    if (sourceDimsWithOvers.size() > 0) {
      dimsWithOvers++;
    }
    if (size1DimsWithOvers.size() > 0) {
      dimsWithOvers++;
    }
    if (size2DimsWithOvers.size() > 0) {
      dimsWithOvers++;
    }
    if (dimsWithOvers > 0)
    {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: adding one value for each dimension");
      }
      if ((colorDimOptions != null) && (colorDimsWithOvers.size() > 0) && (colorDimOptions.size() > 0))
      {
        colorDimsWithOvers = CollectionUtil.findIntersection(colorDimsWithOvers, colorDimOptions);
        if (colorDimsWithOvers.size() < colorDimOptions.size())
        {
          Set complement = CollectionUtil.findComplement(colorDimsWithOvers, colorDimOptions);
          String newVal = "" + complement.iterator().next();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: adding " + newVal + " to color dims");
          }
          colorDimsWithOvers.add(newVal);
        }
      }
      if ((sourceDimOptions != null) && (sourceDimsWithOvers.size() > 0) && (sourceDimOptions.size() > 0))
      {
        sourceDimsWithOvers = CollectionUtil.findIntersection(sourceDimsWithOvers, sourceDimOptions);
        if (sourceDimsWithOvers.size() < sourceDimOptions.size())
        {
          Set complement = CollectionUtil.findComplement(sourceDimsWithOvers, sourceDimOptions);
          String newVal = "" + complement.iterator().next();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: adding " + newVal + " to source dims");
          }
          sourceDimsWithOvers.add(newVal);
        }
      }
      if ((size1DimOptions != null) && (size1DimsWithOvers.size() > 0) && (size1DimOptions.size() > 0))
      {
        size1DimsWithOvers = CollectionUtil.findIntersection(size1DimsWithOvers, size1DimOptions);
        if (size1DimsWithOvers.size() < size1DimOptions.size())
        {
          Set complement = CollectionUtil.findComplement(size1DimsWithOvers, size1DimOptions);
          String newVal = "" + complement.iterator().next();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: adding " + newVal + " to size1 dims");
          }
          size1DimsWithOvers.add(newVal);
        }
      }
      if ((size2DimOptions != null) && (size2DimsWithOvers.size() > 0) && (size2DimOptions.size() > 0))
      {
        size2DimsWithOvers = CollectionUtil.findIntersection(size2DimsWithOvers, size2DimOptions);
        if (size2DimsWithOvers.size() < size2DimOptions.size())
        {
          Set complement = CollectionUtil.findComplement(size2DimsWithOvers, size2DimOptions);
          String newVal = "" + complement.iterator().next();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: adding " + newVal + " to size2 dims");
          }
          size2DimsWithOvers.add(newVal);
        }
      }
    }
    List activeDims = new ArrayList();
    List dimNameArray = new ArrayList();
    Map dimIdCollectionMap = new HashMap();
    int permutationDepth = 0;
    if (colorDimsWithOvers.size() > 0)
    {
      activeDims.add(colorDimsWithOvers);
      dimNameArray.add(":SKU");
      dimIdCollectionMap.put(":SKU", FlexObjectUtil.groupIntoCollections(relevantLinks, "FLEXBOMLINK." + "IDA3E5"));
      permutationDepth++;
    }
    if (sourceDimsWithOvers.size() > 0)
    {
      activeDims.add(sourceDimsWithOvers);
      dimNameArray.add(":SOURCE");
      dimIdCollectionMap.put(":SOURCE", FlexObjectUtil.groupIntoCollections(relevantLinks, "FLEXBOMLINK." + "IDA3F5"));
      permutationDepth++;
    }
    if (size1DimsWithOvers.size() > 0)
    {
      activeDims.add(size1DimsWithOvers);
      dimNameArray.add(":SIZE1");
      dimIdCollectionMap.put(":SIZE1", FlexObjectUtil.groupIntoCollections(relevantLinks, "FLEXBOMLINK.SIZE1"));
      permutationDepth++;
    }
    if (size2DimsWithOvers.size() > 0)
    {
      activeDims.add(size2DimsWithOvers);
      dimNameArray.add(":SIZE2");
      dimIdCollectionMap.put(":SIZE2", FlexObjectUtil.groupIntoCollections(relevantLinks, "FLEXBOMLINK.SIZE2"));
      permutationDepth++;
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getUniqueMaterialColorCombinationsForBranch: dimNameArray = " + dimNameArray);
    }
    if (permutationDepth > 0) {
      permutateBranchMaterialColors("", 0, topLevelLink, relevantLinks, activeDims, dimNameArray, materialColorCombos);
    }
    return materialColorCombos;
  }
  
  private void permutateBranchMaterialColors(String permString, int currentPermDepth, FlexObject topLevelLink, Set relevantLinks, List activeDims, List dimNameArray, Set materialColorCombos)
    throws WTException
  {
    boolean debug = false;
    
    Collection permDepthCollection = (Collection)activeDims.get(currentPermDepth);
    Iterator iterator = permDepthCollection.iterator();
    
    String dimName = (String)dimNameArray.get(currentPermDepth);
    while (iterator.hasNext())
    {
      String dimValue = (String)iterator.next();
      String currentPermString = permString + dimName + "=" + dimValue;
      if (currentPermDepth == activeDims.size() - 1)
      {
        relevantLinks.add(topLevelLink);
        Set relevantLinksCopy = new HashSet();
        relevantLinksCopy.addAll(FlexObjectUtil.cloneFlexObjects(relevantLinks));
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("permutateBranchMaterialColors: permString = " + currentPermString + " LEAF NODE");
        }
        Set applicableLinks = getApplicableLinks(currentPermString, relevantLinksCopy);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("permutateBranchMaterialColors: candidates = " + relevantLinksCopy.size() + " applicable = " + applicableLinks.size());
        }
        Collection mergedRows = mergeDimensionBOM(applicableLinks, null, null);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("permutateBranchMaterialColors: merged set.size() = " + mergedRows.size());
        }
        FlexObject mergedRow = (FlexObject)mergedRows.iterator().next();
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("permutateBranchMaterialColors: merged material = " + mergedRow.getString(FlexTypeCache.getFlexTypeFromPath("Material").getAttribute("name").getSearchResultIndex()));
        }
        if (debug) {
          LOGGER.debug("permutateBranchMaterialColors: merged color = " + mergedRow.getString("LCSCOLOR.COLORNAME"));
        }
        materialColorCombos.add(bomInfoToMaterialColorInfo(mergedRow));
      }
      else
      {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("permutateBranchMaterialColors: permString = " + currentPermString);
        }
        permutateBranchMaterialColors(currentPermString + "|", currentPermDepth + 1, topLevelLink, relevantLinks, activeDims, dimNameArray, materialColorCombos);
      }
    }
  }
  
  public Set getApplicableLinks(String dimString, Collection links)
  {
    boolean debug = false;
    String colorDimValue = "";
    String sourceDimValue = "";
    String size1DimValue = "";
    String size2DimValue = "";
    String destDimValue = "";
    



    Set applicableLinks = new HashSet();
    applicableLinks.addAll(links);
    StringTokenizer tokenizer = new StringTokenizer(dimString, "|");
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      String dimName = token.substring(0, token.indexOf("="));
      String dimValue = token.substring(token.indexOf("=") + 1, token.length());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("getApplicableLinks; Processing dimension: dimName = " + dimName + " dimValue = " + dimValue + " potentialLinks.size = " + links.size());
      }
      if (":SKU".equals(dimName)) {
        colorDimValue = dimValue;
      }
      if (":SOURCE".equals(dimName)) {
        sourceDimValue = dimValue;
      }
      if (":SIZE1".equals(dimName)) {
        size1DimValue = dimValue;
      }
      if (":SIZE2".equals(dimName)) {
        size2DimValue = dimValue;
      }
      if (":DESTINATION".equals(dimName)) {
        destDimValue = dimValue;
      }
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getApplicableLinks: colorDimValue = " + colorDimValue + " sourceDimValue = " + sourceDimValue + " size1DimValue = " + size1DimValue + " size2DimValue = " + size2DimValue + " destDimValue = " + destDimValue);
    }
    Iterator linkIter = links.iterator();
    while (linkIter.hasNext())
    {
      FlexObject link = (FlexObject)linkIter.next();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("getApplicableLinks: link = " + link.getString("FLEXBOMLINK.IDA2A2"));
      }
      String linkColorVal = link.getString("FLEXBOMLINK." + "IDA3E5");
      if (FormatHelper.hasContent(colorDimValue))
      {
        if ((!"0".equals(linkColorVal)) && (!colorDimValue.equals(linkColorVal)))
        {
          applicableLinks.remove(link);
          if (!LOGGER.isDebugEnabled()) {
            continue;
          }
          LOGGER.debug("getApplicableLinks: removed");
        }
      }
      else if (!"0".equals(linkColorVal))
      {
        applicableLinks.remove(link);
        if (!LOGGER.isDebugEnabled()) {
          continue;
        }
        LOGGER.debug("getApplicableLinks: removed"); continue;
      }
      String linkSourceVal = link.getString("FLEXBOMLINK." + "IDA3F5");
      if (FormatHelper.hasContent(sourceDimValue))
      {
        if ((!"0".equals(linkSourceVal)) && (!sourceDimValue.equals(linkSourceVal)))
        {
          applicableLinks.remove(link);
          if (!LOGGER.isDebugEnabled()) {
            continue;
          }
          LOGGER.debug("getApplicableLinks: removed");
        }
      }
      else if (!"0".equals(linkSourceVal))
      {
        applicableLinks.remove(link);
        if (!LOGGER.isDebugEnabled()) {
          continue;
        }
        LOGGER.debug("getApplicableLinks: removed"); continue;
      }
      String linkDestVal = link.getString("FLEXBOMLINK." + "IDA3H5");
      if (FormatHelper.hasContentAllowZero(sourceDimValue))
      {
        if ((!"0".equals(linkDestVal)) && (!destDimValue.equals(linkDestVal)))
        {
          applicableLinks.remove(link);
          if (!LOGGER.isDebugEnabled()) {
            continue;
          }
          LOGGER.debug("getApplicableLinks: removed");
        }
      }
      else if (!"0".equals(linkDestVal))
      {
        applicableLinks.remove(link);
        if (!LOGGER.isDebugEnabled()) {
          continue;
        }
        LOGGER.debug("getApplicableLinks: removed"); continue;
      }
      String linkSize1Val = link.getString("FLEXBOMLINK.SIZE1");
      if (FormatHelper.hasContentAllowZero(size1DimValue))
      {
        if ((FormatHelper.hasContentAllowZero(linkSize1Val)) && (!size1DimValue.equals(linkSize1Val)))
        {
          applicableLinks.remove(link);
          if (!LOGGER.isDebugEnabled()) {
            continue;
          }
          LOGGER.debug("getApplicableLinks: removed");
        }
      }
      else if (FormatHelper.hasContentAllowZero(linkSize1Val))
      {
        applicableLinks.remove(link);
        if (!LOGGER.isDebugEnabled()) {
          continue;
        }
        LOGGER.debug("getApplicableLinks: removed"); continue;
      }
      String linkSize2Val = link.getString("FLEXBOMLINK.SIZE2");
      if (FormatHelper.hasContentAllowZero(size2DimValue))
      {
        if ((FormatHelper.hasContentAllowZero(linkSize2Val)) && (!size2DimValue.equals(linkSize2Val)))
        {
          applicableLinks.remove(link);
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getApplicableLinks: removed");
          }
        }
      }
      else if (FormatHelper.hasContentAllowZero(linkSize2Val))
      {
        applicableLinks.remove(link);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("getApplicableLinks: removed");
        }
      }
    }
    return applicableLinks;
  }
  
  public static PreparedQueryStatement getProductBOMMaterialSupplierQuery(BOMOwner ownerMaster, FlexSpecMaster specMaster, FlexBOMPartMaster bomMaster, Date effectiveDate, LCSSourcingConfigMaster sourceMaster)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    
    String materialName = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnDescriptorName();
    String supplierName = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("name").getColumnDescriptorName();
    String bomName = FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("name").getColumnDescriptorName();
    
    statement.setDistinct(true);
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendFromTable(LCSMaterial.class);
    statement.appendFromTable(LCSSupplier.class);
    statement.appendFromTable(LCSMaterialSupplier.class);
    statement.appendFromTable(LCSMaterialSupplierMaster.class);
    statement.appendFromTable(FlexBOMPart.class);
    

    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, bomName));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "iterationInfo.branchId"));
    

    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "iterationInfo.latest"), "1", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"), "wrk", "<>"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "versionInfo.identifier.versionId"), "A", "="));
    


    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, materialName));
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "state.state"));
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "primaryImageURL"));
    
    statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "state.state"));
    
    statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, supplierName));
    statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "state.state"));
    
    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"), new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
    
    statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"), new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"), new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
    statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"), new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "wip"), "0", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "masterReference.key.id"), FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER), "<>"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.latest"), "1", "="));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
    if (bomMaster != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(bomMaster))));
    }
    else if ((specMaster != null) || (sourceMaster != null) || (ownerMaster != null))
    {
      if (specMaster != null)
      {
        statement.appendFromTable(FlexSpecToComponentLink.class);
        statement.appendSelectColumn(new QueryColumn(FlexSpecToComponentLink.class, "componentParentId"));
        
        statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(specMaster)));
      }
      else if (sourceMaster != null)
      {
        statement.appendFromTable(FlexSpecToComponentLink.class);
        statement.appendFromTable(FlexSpecification.class);
        
        statement.appendSelectColumn(new QueryColumn(FlexSpecToComponentLink.class, "componentParentId"));
        
        statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
        statement.appendJoin(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), new QueryColumn(FlexSpecification.class, "masterReference.key.id"));
        
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecification.class, "specSourceReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(sourceMaster)));
      }
      else if (ownerMaster != null)
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(ownerMaster)));
      }
    }
    if (effectiveDate != null)
    {
      statement.appendAndIfNeeded();
      statement.appendOpenParen();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "inDate"), "?", "<="), effectiveDate);
      
      statement.appendAndIfNeeded();
      statement.appendOpenParen();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "?", ">="), effectiveDate);
      statement.appendOr();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
      statement.appendClosedParen();
      statement.appendClosedParen();
    }
    else
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    }
    printStatement(statement);
    
    return statement;
  }
  
  public static PreparedQueryStatement getLinkedBOMMaterialSupplierQuery(BOMOwner ownerMaster, Date effectiveDate)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    
    String materialName = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnDescriptorName();
    String supplierName = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("name").getColumnDescriptorName();
    String bomName = FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("name").getColumnDescriptorName();
    
    statement.setDistinct(true);
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendFromTable(LCSMaterial.class);
    statement.appendFromTable(LCSSupplier.class);
    statement.appendFromTable(LCSMaterialSupplier.class);
    statement.appendFromTable(LCSMaterialSupplierMaster.class);
    statement.appendFromTable(FlexBOMPart.class);
    

    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMPart.class, bomName));
    

    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"));
    

    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "iterationInfo.latest"), "1", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "checkoutInfo.state"), "wrk", "<>"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "versionInfo.identifier.versionId"), "A", "="));
    


    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, materialName));
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "state.state"));
    statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "primaryImageURL"));
    
    statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "state.state"));
    
    statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, supplierName));
    statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "iterationInfo.branchId"));
    statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "state.state"));
    
    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"), new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
    
    statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"), new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"), new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
    statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"), new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "wip"), "0", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "masterReference.key.id"), FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER), "<>"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.latest"), "1", "="));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
    if (ownerMaster == null) {
      throw new WTException();
    }
    statement.appendFromTable(FlexSpecToComponentLink.class);
    statement.appendFromTable(FlexSpecification.class);
    
    statement.appendSelectColumn(new QueryColumn(FlexSpecToComponentLink.class, "componentParentId"));
    
    statement.appendAndIfNeeded();
    
    statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecToComponentLink.class, "componentReference.key.id"), new QueryColumn(FlexBOMPart.class, "masterReference.key.id"), "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecToComponentLink.class, "specificationMasterReference.key.id"), new QueryColumn(FlexSpecification.class, "masterReference.key.id"), "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexSpecification.class, "specOwnerReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(ownerMaster)));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "ownerMasterReference.key.id"), "?", "<>"), new Long(FormatHelper.getNumericObjectIdFromObject(ownerMaster)));
    if (effectiveDate != null)
    {
      statement.appendAndIfNeeded();
      statement.appendOpenParen();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "inDate"), "?", "<="), effectiveDate);
      
      statement.appendAndIfNeeded();
      statement.appendOpenParen();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "?", ">="), effectiveDate);
      statement.appendOr();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
      statement.appendClosedParen();
      statement.appendClosedParen();
    }
    else
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    }
    printStatement(statement);
    
    return statement;
  }
  
  public static Collection getProductBOMMaterialSupplier(BOMOwner ownerMaster, FlexSpecMaster specMaster, FlexBOMPartMaster bomMaster, Date effectiveDate)
    throws WTException
  {
    return getProductBOMMaterialSupplier(ownerMaster, specMaster, bomMaster, effectiveDate, null);
  }
  
  public static Collection getProductBOMMaterialSupplier(BOMOwner ownerMaster, FlexSpecMaster specMaster, FlexBOMPartMaster bomMaster, Date effectiveDate, LCSSourcingConfigMaster sourceMaster)
    throws WTException
  {
    PreparedQueryStatement statement = getProductBOMMaterialSupplierQuery(ownerMaster, specMaster, bomMaster, effectiveDate, sourceMaster);
    
    String materialName = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnDescriptorName();
    String supplierName = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("name").getColumnDescriptorName();
    
    statement.appendSortBy(new QueryColumn(LCSMaterial.class, materialName), CASE_INSENSITIVE_SORT);
    statement.appendSortBy(new QueryColumn(LCSSupplier.class, supplierName), CASE_INSENSITIVE_SORT);
    
    return runDirectQuery(statement).getResults();
  }
  
  public static Collection getLinkedBOMMaterialSupplier(BOMOwner ownerMaster, Date effectiveDate)
    throws WTException
  {
    PreparedQueryStatement statement = getLinkedBOMMaterialSupplierQuery(ownerMaster, effectiveDate);
    
    String materialName = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnDescriptorName();
    String supplierName = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("name").getColumnDescriptorName();
    
    statement.appendSortBy(new QueryColumn(LCSMaterial.class, materialName), CASE_INSENSITIVE_SORT);
    statement.appendSortBy(new QueryColumn(LCSSupplier.class, supplierName), CASE_INSENSITIVE_SORT);
    
    return runDirectQuery(statement).getResults();
  }
  
  public MaterialColorInfo bomInfoToMaterialColorInfo(FlexObject bomInfo)
    throws WTException
  {
    String matDes = "FLEXBOMLINK." + FlexTypeCache.getFlexTypeRoot("BOM").getAttribute("materialDescription").getColumnName();
    if ((FormatHelper.getNumericFromOid(LCSMaterialQuery.PLACEHOLDERID).equals(bomInfo.getString("FLEXBOMLINK.IDA3B5"))) && 
      (!FormatHelper.hasContent(bomInfo.getString(matDes)))) {
      return null;
    }
    return new MaterialColorInfo(bomInfo.getString(matDes), bomInfo
      .getString("FLEXBOMLINK.IDA3B5"), bomInfo
      .getString("LCSSUPPLIERMASTER.SUPPLIERNAME"), bomInfo
      .getString("LCSMATERIALSUPPLIERMASTER.IDA2A2"), bomInfo
      .getString("LCSCOLOR.COLORNAME"), bomInfo
      .getString("LCSCOLOR.IDA2A2"), bomInfo
      .getString("LCSMATERIALCOLOR.IDA2A2"), bomInfo
      .getString("FLEXBOMLINK.IDA3C5"), bomInfo
      .getString("FLEXBOMLINK." + FlexTypeCache.getFlexTypeFromPath("BOM").getAttribute("colorDescription").getColumnName()));
  }
  
  public static Collection materialSupplierBOMWhereUsed(LCSMaterialSupplier materialSupplier)
    throws WTException
  {
    String materialMasterId = FormatHelper.getNumericObjectIdFromObject(materialSupplier.getMaterialMaster());
    String supplierMasterId = FormatHelper.getNumericObjectIdFromObject(materialSupplier.getSupplierMaster());
    
    PreparedQueryStatement statement = new PreparedQueryStatement();
    statement.appendFromTable(FlexBOMLink.class);
    
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), "?", "="), new Double(materialMasterId));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"), "?", "="), new Double(supplierMasterId));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    
    return runDirectQuery(statement).getResults();
  }
  
  public static Collection getSyncableBOMs(FlexBOMPart sourceBOM)
    throws WTException
  {
    Collection syncBOMs = new ArrayList();
    FlexType bomType = FlexTypeCache.getFlexTypeRoot("BOM");
    String bNameCol = bomType.getAttribute("name").getColumnName();
    
    BOMOwner bomOwnerMaster = sourceBOM.getOwnerMaster();
    
    String prodIdCol = "CHILDPRODUCT.BRANCHIDITERATIONINFO";
    
    Collection linkedProds = LCSProductQuery.getLinkedProducts((LCSPartMaster)bomOwnerMaster, true, false);
    Iterator prods = linkedProds.iterator();
    
    FlexObject pObj = null;
    FlexObject tObj = null;
    
    LCSProduct product = null;
    FlexBOMPart bom = null;
    Collection productBOMs = null;
    Iterator pbi = null;
    
    String pName = "";
    String bName = "";
    String bId = "";
    while (prods.hasNext())
    {
      pObj = (FlexObject)prods.next();
      if (!"sibling".equalsIgnoreCase(CopyModeUtil.getRelationshipType((String)pObj.get("PRODUCTTOPRODUCTLINK.LINKTYPE"))))
      {
        product = (LCSProduct)LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSProduct:" + pObj.get(prodIdCol));
        pName = (String)product.getValue("productName");
        
        productBOMs = new LCSFlexBOMQuery().findBOMPartsForOwner(product, "A", "MAIN", null);
        pbi = productBOMs.iterator();
        while (pbi.hasNext())
        {
          bom = (FlexBOMPart)pbi.next();
          tObj = pObj.dup();
          bName = (String)bom.getValue("name");
          bId = FormatHelper.getVersionId(bom);
          
          tObj.put("FLEXBOMPART." + bNameCol, bName);
          tObj.put("FLEXBOMPART.BRANCHIDITERATIONINFO", FormatHelper.getNumericFromOid(bId));
          tObj.put("FLEXBOMPART.IDA3MASTERREFERENCE", FormatHelper.getNumericFromReference(bom.getMasterReference()));
          

          syncBOMs.add(tObj);
        }
      }
    }
    return syncBOMs;
  }
  
  public static Collection findBOMObjects(LCSProduct product, LCSSourcingConfig source, FlexSpecification spec, String bomType)
    throws WTException
  {
    Collection bomFobjs = findBOMs(product, source, spec, bomType);
    Collection boms = LCSQuery.getObjectsFromResults(bomFobjs, "VR:com.lcs.wc.flexbom.FlexBOMPart:", "FLEXBOMPART.BRANCHIDITERATIONINFO");
    return boms;
  }
  
  public static Collection findBOMs(LCSProduct product, LCSSourcingConfig source, FlexSpecification spec, String bomType)
    throws WTException
  {
    return findBOMs(product, source, spec, bomType, false);
  }
  
  public static Collection findBOMs(LCSProduct product, LCSSourcingConfig source, FlexSpecification spec, String bomType, boolean isDistinct)
    throws WTException
  {
    Collection results = new ArrayList();
    
    PreparedQueryStatement statement = FlexSpecQuery.getBOMSpecToComponentLinkQuery(product, source, spec);
    if (FormatHelper.hasContent(bomType))
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMPart.class, "bomType"), "?", "="), bomType);
    }
    results.addAll(runDirectQuery(statement).getResults());
    if ((spec == null) && (source == null))
    {
      statement = new PreparedQueryStatement();
      statement.appendFromTable("SpeclessBOM");
      statement.appendFromTable("LatestIterFlexBOMPart", "FlexBOMPart");
      statement.appendSelectColumn(new QueryColumn("SpeclessBOM", "branchId"));
      statement.appendSelectColumn(new QueryColumn("SpeclessBOM", "name"));
      statement.appendCriteria(new Criteria(new QueryColumn("SpeclessBOM", "prodMasterId"), "?", "="), new Long(FormatHelper.getNumericFromReference(product.getMasterReference())));
      if (FormatHelper.hasContent(bomType))
      {
        statement.appendAndIfNeeded();
        statement.appendCriteria(new Criteria(new QueryColumn("SpeclessBOM", "bomType"), "?", "="), bomType);
      }
      FlexType bomFType = FlexTypeCache.getFlexTypeFromPath("BOM");
      String nameAtt = bomFType.getAttribute("name").getColumnName();
      



      statement.appendJoin(new QueryColumn(FlexBOMPart.class, "iterationInfo.branchId"), new QueryColumn("SpeclessBOM", "branchId"));
      FlexTypeGenerator ftg = new FlexTypeGenerator();
      ftg.setScope("BOM_SCOPE");
      ftg.addFlexTypeCriteria(statement, bomFType, "");
      

      Collection qr = runDirectQuery(statement).getResults();
      Iterator i = qr.iterator();
      FlexObject fobj = null;
      while (i.hasNext())
      {
        fobj = (FlexObject)i.next();
        if (FormatHelper.hasContent(fobj.getString("SPECLESSBOM.BRANCHID")))
        {
          fobj.put("FLEXBOMPART.BRANCHIDITERATIONINFO", fobj.get("SPECLESSBOM.BRANCHID"));
          fobj.put("FLEXBOMPART." + nameAtt, fobj.get("SPECLESSBOM.NAME"));
          results.add(fobj);
        }
      }
    }
    if ((isDistinct) && (results.size() > 1)) {
      return removeDuplicatedObject(results, "FLEXBOMPART.BRANCHIDITERATIONINFO");
    }
    return results;
  }
  
  public static Collection getAllFlexBOMLinks(FlexBOMPart owner, WTObject colorDimension, WTObject sourceDimension, WTObject destinationDimension, String size1, String size2)
    throws WTException
  {
    PreparedQueryStatement statement = new PreparedQueryStatement();
    
    statement.appendFromTable(FlexBOMLink.class);
    
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    if (owner != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(owner.getMaster()))));
    }
    if (colorDimension != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "colorDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(colorDimension))));
    }
    if (sourceDimension != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "sourceDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(sourceDimension))));
    }
    if (destinationDimension != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "destinationDimensionReference.key.id"), "?", "="), new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(destinationDimension))));
    }
    if (size1 != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size1"), size1, "="));
    }
    if (size2 != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "size2"), size2, "="));
    }
    return LCSQuery.getObjectsFromResults(statement, "OR:com.lcs.wc.flexbom.FlexBOMLink:", "FLEXBOMLINK.IDA2A2");
  }
  
  public static Collection getAllTrueSkuOverrides(FlexBOMPart owner)
    throws WTException
  {
    FlexType bomType = FlexTypeCache.getFlexTypeFromPath("BOM");
    if (owner != null) {
      bomType = owner.getFlexType();
    }
    String matDescriptionName = bomType.getAttribute("materialDescription").getColumnDescriptorName();
    
    ArrayList<String> ignoredAttributes = new ArrayList();
    ignoredAttributes.add("highLight");
    ignoredAttributes.add("colorDescription");
    ignoredAttributes.add("materialDescription");
    ignoredAttributes.add("supplierDescription");
    
    PreparedQueryStatement statement = new PreparedQueryStatement();
    
    statement.appendFromTable(FlexBOMLink.class);
    statement.appendFromTable(FlexBOMLink.class, "toplevel");
    
    statement.appendSelectColumn(new QueryColumn("toplevel", FlexBOMLink.class, "dimensionId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "dimensionName"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "branchId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "thePersistInfo.theObjectIdentifier.id"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "dimensionId"));
    statement.appendSelectColumn(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"));
    
    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), new QueryColumn("toplevel", FlexBOMLink.class, "parentReference.key.id"));
    statement.appendJoin(new QueryColumn(FlexBOMLink.class, "branchId"), new QueryColumn("toplevel", FlexBOMLink.class, "branchId"));
    
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dimensionName"), ":SKU", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "dropped"), "0", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "outDate"), "", "IS NULL"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("toplevel", FlexBOMLink.class, "dimensionName"), "", "IS NULL"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("toplevel", FlexBOMLink.class, "dropped"), "0", "="));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn("toplevel", FlexBOMLink.class, "outDate"), "", "IS NULL"));
    if (owner != null)
    {
      statement.appendAndIfNeeded();
      statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "parentReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(owner.getMaster())));
    }
    statement.appendAndIfNeeded();
    statement.appendOpenParen();
    
    statement.appendOpenParen();
    
    statement.appendOpenParen();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), "?", "<>"), new Long(FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER)));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), new QueryColumn("toplevel", FlexBOMLink.class, "childReference.key.id"), "<>"));
    statement.appendClosedParen();
    statement.appendOr();
    statement.appendOpenParen();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "childReference.key.id"), "?", "="), new Long(FormatHelper.getNumericObjectIdFromObject(LCSMaterialQuery.PLACEHOLDER)));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, matDescriptionName), "", "IS NOT NULL"));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, matDescriptionName), new QueryColumn("toplevel", FlexBOMLink.class, matDescriptionName), "<>"));
    statement.appendClosedParen();
    statement.appendClosedParen();
    
    statement.appendOr();
    
    statement.appendOpenParen();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"), "?", "<>"), new Long(FormatHelper.getNumericObjectIdFromObject(LCSSupplierQuery.PLACEHOLDER)));
    statement.appendAndIfNeeded();
    statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, "supplierReference.key.id"), new QueryColumn("toplevel", FlexBOMLink.class, "supplierReference.key.id"), "<>"));
    statement.appendClosedParen();
    
    Collection<FlexTypeAttribute> attributes = bomType.getAllAttributes("LINK_SCOPE", null);
    Iterator itr = attributes.iterator();
    while (itr.hasNext())
    {
      FlexTypeAttribute att = (FlexTypeAttribute)itr.next();
      String attKey = att.getAttKey();
      if (!ignoredAttributes.contains(attKey)) {
        if (!"constant".equals(att.getAttVariableType()))
        {
          String attColumnDescriptor = att.getColumnDescriptorName();
          statement.appendOr();
          statement.appendOpenParen();
          statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, attColumnDescriptor), "", "IS NOT NULL"));
          statement.appendAndIfNeeded();
          statement.appendCriteria(new Criteria(new QueryColumn(FlexBOMLink.class, attColumnDescriptor), new QueryColumn("toplevel", FlexBOMLink.class, attColumnDescriptor), "<>"));
          statement.appendClosedParen();
        }
      }
    }
    statement.appendClosedParen();
    

    printStatement(statement);
    

    return LCSQuery.runDirectQuery(statement).getResults();
  }
  
  public static Collection<FlexBOMLink> navigateUsedBy(WTPart part)
    throws WTException
  {
    if (part == null) {
      throw new WTException("Part parameter cannot be null.");
    }
    long masterId = PersistenceHelper.getObjectIdentifier(part.getMaster()).getId();
    
    List<FlexBOMLink> datas = new ArrayList();
    
    QuerySpec qs = new QuerySpec();
    
    int partIndex = qs.appendClassList(WTPart.class, false);
    int bomLinkIndex = qs.appendClassList(FlexBOMLink.class, true);
    
    qs.appendWhere(new SearchCondition(WTPart.class, "masterReference.key.id", "=", masterId), new int[] { partIndex });
    qs.appendAnd();
    qs.appendWhere(new SearchCondition(WTPart.class, "iterationInfo.branchId", FlexBOMLink.class, "wcPartReference.key.branchId"), new int[] { partIndex, bomLinkIndex });
    
    List<String> existingBom = new ArrayList();
    
    QueryResult result = PersistenceHelper.manager.find(qs);
    while (result.hasMoreElements())
    {
      FlexBOMLink flexBomLink = (FlexBOMLink)((Object[])(Object[])result.nextElement())[0];
      if (ACLHelper.hasViewAccess(flexBomLink))
      {
        String bomParentString = VersionHelper.getVersion(flexBomLink.getParent(), flexBomLink.getParentRev()).toString();
        if (!existingBom.contains(bomParentString))
        {
          existingBom.add(bomParentString);
          datas.add(flexBomLink);
        }
      }
    }
    return datas;
  }
  
  public boolean hasWTPartUsedBy(WTPart part)
    throws WTException, WTPropertyVetoException
  {
    if (part == null) {
      throw new WTException("Part parameter cannot be null.");
    }
    long masterId = PersistenceHelper.getObjectIdentifier(part.getMaster()).getId();
    
    QuerySpec qs = new QuerySpec();
    
    int partIndex = qs.appendClassList(WTPart.class, false);
    int bomLinkIndex = qs.appendClassList(FlexBOMLink.class, true);
    qs.setDescendantsIncluded(false, partIndex);
    
    qs.appendWhere(new SearchCondition(WTPart.class, "masterReference.key.id", "=", masterId), new int[] { partIndex });
    qs.appendAnd();
    qs.appendWhere(new SearchCondition(WTPart.class, "iterationInfo.branchId", FlexBOMLink.class, "wcPartReference.key.branchId"), new int[] { partIndex, bomLinkIndex });
    qs.appendAnd();
    qs.appendWhere(new SearchCondition(FlexBOMLink.class, "outDate", true), new int[] { 1 });
    qs.appendAnd();
    qs.appendWhere(new SearchCondition(FlexBOMLink.class, "dropped", "FALSE"), new int[] { 1 });
    
    QueryResult result = PersistenceHelper.manager.find(qs);
    if (result.hasMoreElements()) {
      return true;
    }
    return false;
  }
  
  public Collection<FlexBOMLink> usedByOldIteration(WTPart part)
    throws WTException
  {
    if (part == null) {
      throw new WTException("Part parameter cannot be null.");
    }
    long masterId = PersistenceHelper.getObjectIdentifier(part.getMaster()).getId();
    
    List<FlexBOMLink> datas = new ArrayList();
    
    QuerySpec qs = new QuerySpec();
    
    int partIndex = qs.appendClassList(WTPart.class, false);
    int bomLinkIndex = qs.appendClassList(FlexBOMLink.class, true);
    
    qs.appendWhere(new SearchCondition(WTPart.class, "masterReference.key.id", "=", masterId), new int[] { partIndex });
    qs.appendAnd();
    qs.appendWhere(new SearchCondition(WTPart.class, "iterationInfo.branchId", FlexBOMLink.class, "wcPartReference.key.branchId"), new int[] { partIndex, bomLinkIndex });
    qs.appendAnd();
    qs.appendWhere(new SearchCondition(FlexBOMLink.class, "outDate", false), new int[] { 1 });
    
    List<String> existingBom = new ArrayList();
    
    QueryResult result = PersistenceHelper.manager.find(qs);
    while (result.hasMoreElements())
    {
      FlexBOMLink flexBomLink = (FlexBOMLink)((Object[])(Object[])result.nextElement())[0];
      

      String bomParentString = VersionHelper.getVersion(flexBomLink.getParent(), flexBomLink.getParentRev()).toString();
      if (!existingBom.contains(bomParentString))
      {
        existingBom.add(bomParentString);
        datas.add(flexBomLink);
      }
    }
    return datas;
  }
}
