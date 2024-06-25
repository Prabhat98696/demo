package com.agron.wc.materialSupplier;

import com.google.gwt.thirdparty.guava.common.collect.Maps;
import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.TemplateFilterHelper;
import com.lcs.wc.collection.FlexCollection;
import com.lcs.wc.collection.FlexCollectionMaster;
import com.lcs.wc.collection.MatSupToCollectionLinkMaster;
import com.lcs.wc.color.LCSPalette;
import com.lcs.wc.color.LCSPaletteMaterialLink;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.AttributeGroup;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeScopeDefinition;
import com.lcs.wc.flextype.FlexTypeScopeDefinitionFactory;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.foundation.LCSQueryPluginManager;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialQuery;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierMaster;
import com.lcs.wc.report.FiltersList;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleRequest;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.supplier.LCSSupplierQuery;
import com.lcs.wc.testing.TestSpecification;
import com.lcs.wc.testing.TestSpecificationMaster;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.RequestHelper;
import java.io.Externalizable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.log4j.LogR;
import wt.util.WTException;

public class AgronMaterialSupplierQuery  extends LCSQuery implements Externalizable {
	public static final Logger LOGGER = LogManager.getLogger(AgronMaterialSupplierQuery.class.getName());
	private static final String CLASSNAME = AgronMaterialSupplierQuery.class.getName();
	private static final boolean CASE_INSENSITIVE_SORT = LCSProperties
			.getBoolean("com.lcs.wc.material.LCSMaterialSupplierQuery.caseInsensitiveSort");
	public static LCSMaterialSupplierMaster PLACEHOLDER = null;
	public static String PLACEHOLDERID = "";
	public static final String MATERIAL_TEST_REQ_ROOT_TYPE = LCSProperties
			.get("com.lcs.wc.sample.LCSSample.Material.TestingRequest.Root");
	public static String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialSupplierRootType",
			"Supplier");
	static final Object[] objA = new Object[0];
	
	public static LCSMaterialSupplierMaster getPlaceholder() {
		try {
			if (PLACEHOLDER == null) {
				LCSMaterialSupplier matSup = findMaterialSupplier(LCSMaterialQuery.PLACEHOLDER,
						LCSSupplierQuery.PLACEHOLDER);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("PLACEHOLDER == null: found placeholder " + matSup);
				}

				PLACEHOLDER = matSup.getMaster();
				PLACEHOLDERID = FormatHelper.getObjectId(PLACEHOLDER);
			}
		} catch (Exception var1) {
			LOGGER.debug("WARNING!!!!!!!!!!!!!!!!!!!!: PLACEHOLDER MATERIALSUPPLIER IS MISSING: OK IF SYSTEMSETUP.");
		}

		return PLACEHOLDER;
	}

	public SearchResults findMaterialSuppliersByCriteria(HttpServletRequest request, FlexType type) throws WTException {
		return this.findMaterialSuppliersByCriteria(request, type, (Collection) null);
	}

	public SearchResults findMaterialSuppliersByCriteria(HttpServletRequest request, FlexType type, Collection attCols)
			throws WTException {
		return this.findMaterialSuppliersByCriteria((HttpServletRequest) request, type, attCols, (FiltersList) null);
	}

	public SearchResults findMaterialSuppliersByCriteria(HttpServletRequest request, FlexType type, Collection attCols,
			FiltersList filter) throws WTException {
		return this.findMaterialSuppliersByCriteria((Map) RequestHelper.hashRequest(request), type, attCols, filter);
	}

	public SearchResults findMaterialSuppliersByCriteria(Map criteria, FlexType type, Collection attCols,
			FiltersList filter) throws WTException {
		return this.findMaterialSuppliersByCriteria(criteria, type, attCols, filter, 0);
	}

	public SearchResults findMaterialSuppliersByCriteria(Map criteria, FlexType type, Collection attCols,
			FiltersList filter, int queryLimit) throws WTException {
		return this.findMaterialSuppliersByCriteria(criteria, type, attCols, filter, queryLimit, (Collection) null);
	}

	public SearchResults findMaterialSuppliersByCriteria(Map criteria, FlexType type, Collection attCols,
			FiltersList filter, int queryLimit, Collection oidList) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement = this.appendMaterialSupplierByCriteriaFilter(statement, criteria, type, attCols, filter, oidList);
		statement.setQueryLimit(queryLimit);
		SearchResults results = runDirectQuery(statement);
		return results;
	}

	public PreparedQueryStatement appendMaterialSupplierByCriteriaFilter(PreparedQueryStatement statement)
			throws WTException {
		FlexType type = FlexTypeCache.getFlexTypeRoot("Material");
		Map criteria = Maps.newHashMap();
		statement = this.appendMaterialSupplierByCriteriaFilter(statement, criteria, type, (Collection) null,
				(FiltersList) null, (Collection) null);
		statement.clearSelects();
		statement.clearSorts();
		statement.clearSortColumns();
		return statement;
	}

	public PreparedQueryStatement appendMaterialSupplierByCriteriaFilter(PreparedQueryStatement statement, Map criteria,
			FlexType type, Collection attCols, FiltersList filter, Collection oidList) throws WTException {
		
		List keyWordsList = new ArrayList();
		String tempString = "";
		if (FormatHelper.hasContent(String.valueOf(criteria.get("quickSearchCriteria")))) {
			
			String value = String.valueOf(criteria.get("quickSearchCriteria"));
			//Task #1746 Quick Search with comma (,) separated value
			// Check whether string contains (,) if contains the split into , separated 
			String[] elements = value.split(",");
			for(int i = 0; i < elements.length; i++) {
				keyWordsList.add(elements[i].trim());
			}	
		}
		
		FlexTypeAttribute nameAtt = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name");
		statement.appendFromTable(LCSMaterialMaster.class, "MATERIALMASTER");
		statement.appendFromTable(LCSMaterial.class);
		statement.appendFromTable(LCSSupplier.class);
		statement.appendFromTable(LCSSupplierMaster.class);
		statement.appendFromTable(LCSMaterialSupplier.class);
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendFromTable("V_LCSMaterialSupplier");
		statement.appendSelectColumn(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "name"));
		statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "supplierName"));
		statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(
				new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, nameAtt.getColumnDescriptorName()));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "typeDisplay"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "primaryImageURL"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "state.state"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, TYPED_BRANCH_ID));
		statement.appendSelectColumn(new QueryColumn("V_LCSMaterialSupplier", "materialSupplierName"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "state.state"));
		statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "state.state"));
		statement.appendSelectColumn(
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplierMaster.class, "placeholder"));
		statement.appendSelectColumn(
				new QueryColumn(LCSMaterialSupplier.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "iterationInfo.branchId"));
		statement.appendJoin(new QueryColumn("V_LCSMaterialSupplier", "idA2A2"),
				new QueryColumn(LCSMaterialSupplier.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSMaterial.class, "masterReference.key.id"),
				new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
				new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
				new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn("MATERIALMASTER", LCSMaterialMaster.class, "name"),
				"material_placeholder", "<>"));
		if (!FormatHelper.hasContent((String) criteria.get("LCSMATERIALSUPPLIER_BRANCHIDITERATIONINFO"))
				&& !FormatHelper.hasContent((String) criteria.get("LCSMATERIALSUPPLIER_IDA3MASTERREFERENCE"))) {
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "active"), "1", "="));
		}

		statement.appendAndIfNeeded();
		this.addPossibleSearchCriteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"),
				"" + criteria.get("LCSMATERIALSUPPLIER_BRANCHIDITERATIONINFO"), statement);
		statement.appendAndIfNeeded();
		this.addPossibleSearchCriteria(
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"),
				"" + criteria.get("LCSMATERIALSUPPLIER_IDA3MASTERREFERENCE"), statement);
		ClientContext lcsContext = ClientContext.getContext();
		if (lcsContext.isVendor) {
			statement = LCSQueryPluginManager.handleEvent(
					"LCSMaterialSupplierQuery.findMaterialSuppliersByCriteria_VendorCriteria", statement, criteria);
		}

		this.determineRanges(criteria, statement);
		if (criteria != null) {
			String parentPaletteId = (String) criteria.get("parentPaletteId");
			if (FormatHelper.hasContent(parentPaletteId)) {
				statement.appendFromTable(LCSPaletteMaterialLink.class);
				statement.appendJoin(new QueryColumn(LCSPaletteMaterialLink.class, "roleAObjectRef.key.id"),
						new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"));
				statement.appendJoin(new QueryColumn(LCSPaletteMaterialLink.class, "supplierMasterReference.key.id"),
						new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"));
				statement.appendAndIfNeeded();
				statement.appendCriteria(
						new Criteria(new QueryColumn(LCSPaletteMaterialLink.class, "roleBObjectRef.key.id"), "?", "="),
						new Long(FormatHelper.getNumericFromOid(parentPaletteId)));
			}

			if (filter == null) {
				this.addPossibleSearchCriteria(new QueryColumn(LCSMaterial.class, nameAtt.getColumnDescriptorName()),
						"" + criteria.get("name"), statement);
				this.addPossibleSearchCriteria(
						new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
						"" + criteria.get("SUPPLIERMASTERID"), statement);
			}

			if (type != null) {
				appendHardColumns(type, statement, "LCSMaterial", "MATERIAL", (String) null, false, false);
				
				//addQuickSearchCriteria(statement, type, "Material", "name", (String) criteria.get("quickSearchCriteria"));
				
				if (!keyWordsList.isEmpty() && keyWordsList.size() == 1) {
					// //Task #1746 If only 1 keyword in quicksearchcriteria then return OOTB statement
					addQuickSearchCriteria(statement, type, "Material", "name", (String)keyWordsList.get(0));
				}
				else if (!keyWordsList.isEmpty() && keyWordsList.size() > 1) {
					// Task #1746 If multiple keywords in quicksearchcriteria then return statement
					// with append criteria for multiple keyword
					addQuickSearchCriteriaForCommaSeparated(statement, type, "Material", "name", (String) criteria.get("quickSearchCriteria"), keyWordsList);
				}
				
				addFlexTypeInformation(statement, LCSMaterial.class);
				FlexTypeGenerator flexg = new FlexTypeGenerator();
				flexg.setScope("MATERIAL");
				Map params;
				if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
					flexg.addFlexTypeCriteria(statement, type, (String) null);
				} else {
					params = criteria;
					if (filter != null) {
						params = TemplateFilterHelper.getTemplateFilterParameters(filter,
								Collections.unmodifiableMap(criteria));
					}

					if (criteria.get("sectionConstraint") != null) {
						FlexType materialType = type;
						String sectionConstraint = (String) criteria.get("sectionConstraint");
						String typeKeyBase = "com.lcs.wc.material.BOMSection.constraint." + sectionConstraint;
						String tp = type.getFullName(true);
						String typeKey = typeKeyBase + "." + tp;

						String typeString;
						for (typeString = LCSProperties.get(typeKey); !FormatHelper.hasContent(typeString)
								&& !materialType.isTypeRoot(); typeString = LCSProperties.get(typeKey)) {
							materialType = materialType.getTypeParent();
							tp = materialType.getFullName(true);
							typeKey = typeKeyBase + "." + tp;
						}

						if (!FormatHelper.hasContent(typeString)) {
							typeString = LCSProperties.get(typeKeyBase);
						}

						if (FormatHelper.hasContent(typeString)) {
							flexg.addFlexTypeCriteria(statement, typeString, (String) null);
							statement = flexg.generateSearchCriteria(type, statement, params, false);
						} else {
							statement = flexg.generateSearchCriteria(type, statement, params);
						}
					} else {
						statement = flexg.generateSearchCriteria(type, statement, criteria);
					}
				}

				statement = flexg.createSearchResultsQueryColumns(attCols, type, statement);
				flexg.setScope("MATERIAL-SUPPLIER");
				flexg.setLevel((String) null);

				try {
					flexg.appendHardColumns(statement, type, "LCSMaterialSupplier", true, true);
				} catch (ClassNotFoundException var18) {
					var18.printStackTrace();
				}

				if (filter == null) {
					statement = flexg.generateSearchCriteria(type, statement, criteria);
				} else if (FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
					params = TemplateFilterHelper.getTemplateFilterParameters(filter,
							Collections.unmodifiableMap(criteria));
					statement = flexg.generateSearchCriteria(type, statement, params);
				} else {
					flexg.addFlexTypeCriteria(statement, type, (String) null);
				}

				statement = flexg.createSearchResultsQueryColumns(attCols, type, statement);
				flexg.setScope((String) null);
				flexg.setLevel((String) null);
				if (filter == null) {
					statement.appendAndIfNeeded();
					statement.appendOpenParen();
					statement.appendOpenParen();
					statement = flexg.generateSearchCriteria(
							FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE), statement, criteria);
					statement.appendClosedParen();
					statement.appendOrIfNeeded();
					statement.appendCriteria(
							new Criteria(new QueryColumn(LCSSupplierMaster.class, "supplierName"), "placeholder", "="));
					statement.appendClosedParen();
				} else if (FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
					params = TemplateFilterHelper.getTemplateFilterParameters(filter,
							Collections.unmodifiableMap(criteria));
					statement.appendAndIfNeeded();
					statement.appendOpenParen();
					statement.appendOpenParen();
					statement = flexg.generateSearchCriteria(
							FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE), statement, params);
					statement.appendClosedParen();
					statement.appendOrIfNeeded();
					statement.appendCriteria(
							new Criteria(new QueryColumn(LCSSupplierMaster.class, "supplierName"), "placeholder", "="));
					statement.appendClosedParen();
				}

				statement = flexg.createSearchResultsQueryColumns(attCols,
						FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE), statement);
			}

			this.addPossibleSort("" + criteria.get("sortBy1"), statement, CASE_INSENSITIVE_SORT);
		}

		if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
			filter.addFilterCriteria(statement);
		}

		if (oidList != null && oidList.size() > 0) {
			Iterator<?> it = oidList.iterator();
			statement.appendAndIfNeeded();
			statement.appendOpenParen();

			while (it.hasNext()) {
				String listId = (String) it.next();
				String numeric = FormatHelper.getNumericFromOid(listId);
				if (listId.indexOf(".LCSMaterialSupplier:") > -1) {
					statement.appendOrIfNeeded();
					statement.appendCriteria(
							new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"), "?",
									"="),
							new Long(numeric));
				}
			}

			statement.appendClosedParen();
		}

		statement.appendSortBy(nameAtt.getSearchResultIndex(), CASE_INSENSITIVE_SORT);
		return statement;
	}

	public static LCSMaterialSupplier findPlaceholder(LCSMaterialMaster materialMaster) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterialSupplier.class);
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendSelectColumn(
				new QueryColumn(LCSMaterialSupplier.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement
				.appendCriteria(
						new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
								"?", "="),
						new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(materialMaster))));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "placeholder"), "1", "="));
		LCSMaterialSupplier matSup = (LCSMaterialSupplier) getObjectFromResults(statement,
				"VR:com.lcs.wc.material.LCSMaterialSupplier:", "LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
		return matSup;
	}

	public static SearchResults findMaterialSuppliers(LCSMaterialMaster materialMaster, FlexType type)
			throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterialSupplier.class);
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendFromTable(LCSSupplierMaster.class);
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
		statement.appendSelectColumn(
				new QueryColumn(LCSMaterialSupplier.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "state.state"));
		statement.appendSelectColumn(
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "supplierName"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
				new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement
				.appendCriteria(
						new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
								"?", "="),
						new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(materialMaster))));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "active"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "placeholder"), "0", "="));
		if (type != null) {
			FlexTypeGenerator ftg = new FlexTypeGenerator();
			ftg.setScope("MATERIAL-SUPPLIER");
			ftg.appendQueryColumns(type, statement);
		}

		ClientContext lcsContext = ClientContext.getContext();
		if (lcsContext.isVendor) {
			statement = LCSQueryPluginManager.handleEvent(
					"LCSMaterialSupplierQuery.findMaterialSuppliersByCriteria_VendorCriteria", statement, (Map) null);
		}

		SearchResults results = null;
		results = runDirectQuery(statement);
		return results;
	}

	public static int findMaterialSuppliersCount(LCSMaterialMaster materialMaster) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		String distinct_count_column_name = statement
				.appendSelectCountDistinct(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
		statement.appendFromTable(LCSMaterialSupplier.class);
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendFromTable(LCSSupplierMaster.class);
		statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
				new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement
				.appendCriteria(
						new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
								"?", "="),
						new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(materialMaster))));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "active"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "placeholder"), "0", "="));
		ClientContext client_context = ClientContext.getContext();
		if (client_context.isVendor) {
			Map<String, String> criteria = new HashMap();
			criteria.put("excludeSupplierId", "true");
			statement = LCSQueryPluginManager.handleEvent(
					"LCSMaterialSupplierQuery.findMaterialSuppliersByCriteria_VendorCriteria", statement, criteria);
		}

		SearchResults results = runDirectQuery(statement);
		FlexObject result = (FlexObject) results.getResults().get(0);
		int count = result.getInt(distinct_count_column_name);
		return count;
	}

	public static SearchResults findMaterialSuppliers(LCSMaterial material) throws WTException {
		return findMaterialSuppliers(material.getMaster(), material.getFlexType());
	}

	public Hashtable getSupplierList(LCSMaterial material) throws WTException {
		SearchResults results = findMaterialSuppliers(material);
		return createTableList(results, "LCSSUPPLIERMASTER.IDA2A2", "LCSSUPPLIERMASTER.SUPPLIERNAME",
				"OR:com.lcs.wc.supplier.LCSSupplierMaster:");
	}

	public Hashtable getSupplierList(LCSMaterialMaster materialMaster) throws WTException {
		SearchResults results = findMaterialSuppliers(materialMaster, (FlexType) null);
		return createTableList(results, "LCSSUPPLIERMASTER.IDA2A2", "LCSSUPPLIERMASTER.SUPPLIERNAME",
				"OR:com.lcs.wc.supplier.LCSSupplierMaster:");
	}

	public static LCSMaterialSupplier findMaterialSupplier(LCSMaterialMaster lcsMaterialMaster,
			LCSSupplierMaster supplierMaster) throws WTException {
		String matNumId = FormatHelper.getNumericFromOid(FormatHelper.getObjectId(lcsMaterialMaster));
		String supNumId = FormatHelper.getNumericFromOid(FormatHelper.getObjectId(supplierMaster));
		return findMaterialSupplier(matNumId, supNumId);
	}

	public static LCSMaterialSupplier findMaterialSupplier(String materialMasterNumId, String supplierMasterNumId)
			throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterialSupplier.class);
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"), "?",
						"="),
				new Long(materialMasterNumId));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"), "?",
						"="),
				new Long(supplierMasterNumId));
		LCSMaterialSupplier matSup = (LCSMaterialSupplier) getObjectFromResults(statement,
				"VR:com.lcs.wc.material.LCSMaterialSupplier:", "LCSMATERIALSUPPLIER.BRANCHIDITERATIONINFO");
		return matSup;
	}

	public static Collection findMaterials(LCSSupplierMaster supplierMaster) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterialSupplier.class);
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendFromTable(LCSMaterial.class);
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
				new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement
				.appendCriteria(
						new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
								"?", "="),
						new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(supplierMaster))));
		Collection materials = getObjectsFromResults(statement, "VR:com.lcs.wc.material.LCSMaterial:",
				"LCSMATERIAL.BRANCHIDITERATIONINFO");
		return materials;
	}

	public static SearchResults findSamples(LCSMaterialSupplierMaster materialSupplierMaster) throws WTException {
		return findSamples(materialSupplierMaster, new Hashtable());
	}

	public static SearchResults findSamples(LCSMaterialSupplierMaster materialSupplierMaster, Hashtable criteria)
			throws WTException {
		return findSamples(materialSupplierMaster, criteria, (String) null);
	}

	public static SearchResults findSamples(LCSMaterialSupplierMaster materialSupplierMaster, Hashtable criteria,
			String materialColorId) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendFromTable(LCSSample.class);
		statement.appendFromTable(LCSSampleRequest.class);
		statement.appendFromTable("V_LCSMaterialColor", "LCSMATERIALCOLOR");
		statement.appendFromTable(TestSpecificationMaster.class);
		statement.appendFromTable(TestSpecification.class);
		statement.appendSelectColumn(new QueryColumn(LCSSample.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSSampleRequest.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSSampleRequest.class, "state.state"));
		statement.appendSelectColumn(new QueryColumn("LCSMATERIALCOLOR", "idA2A2"));
		statement.appendSelectColumn(new QueryColumn("LCSMATERIALCOLOR", "colorName"));
		statement.appendSelectColumn(new QueryColumn(TestSpecification.class, "iterationInfo.branchId"));
		statement.appendSelectColumn(new QueryColumn(TestSpecification.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(TestSpecification.class, "masterReference.key.id"));
		statement.appendJoin(new QueryColumn(LCSSample.class, "sourcingMasterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSSample.class, "sampleRequestReference.key.id"),
				new QueryColumn(LCSSampleRequest.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(TestSpecificationMaster.class, "sampleReference.key.id"),
				new QueryColumn(LCSSample.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(TestSpecification.class, "masterReference.key.id"),
				new QueryColumn(TestSpecificationMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSSample.class, "colorReference.key.id"),
				new QueryColumn("LCSMATERIALCOLOR", "idA2A2(+)"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"),
						"?", "="),
				new Long(FormatHelper.getNumericFromOid(FormatHelper.getObjectId(materialSupplierMaster))));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(TestSpecification.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(TestSpecification.class, "checkoutInfo.state"), "wrk", "<>"));
		if (FormatHelper.hasContent(materialColorId)) {
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSSample.class, "colorReference.key.id"), "?", "="),
					new Long(FormatHelper.getNumericFromOid(materialColorId)));
		}

		FlexTypeGenerator flexg = new FlexTypeGenerator();
		FlexType type = FlexTypeCache.getFlexTypeFromPath(MATERIAL_TEST_REQ_ROOT_TYPE);
		flexg.setScope("SAMPLE");
		flexg.setLevel((String) null);
		statement = flexg.appendQueryColumns(type, statement);
		if (criteria.get("sortBy1") != null && FormatHelper.hasContent((String) criteria.get("sortBy1"))) {
			statement.appendSortBy((String) criteria.get("sortBy1"), CASE_INSENSITIVE_SORT);
		}

		flexg.setScope("SAMPLE-REQUEST");
		flexg.setLevel((String) null);
		statement = flexg.appendQueryColumns(type, statement);
		return LCSQuery.runDirectQuery(statement);
	}

	public Collection findPalettesForMaterialSupplier(LCSMaterialSupplier materialSupplier) throws WTException {
		LCSMaterialMaster materialMaster = materialSupplier.getMaterialMaster();
		LCSSupplierMaster supplierMaster = materialSupplier.getSupplierMaster();
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSPaletteMaterialLink.class);
		statement.appendFromTable(LCSPalette.class);
		statement.appendSelectColumn(new QueryColumn(LCSPalette.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSPaletteMaterialLink.class, "roleAObjectRef.key.id"), "?", "="),
				new Long(FormatHelper.getNumericObjectIdFromObject(materialMaster)));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSPaletteMaterialLink.class, "supplierMasterReference.key.id"), "?", "="),
				new Long(FormatHelper.getNumericObjectIdFromObject(supplierMaster)));
		statement.appendJoin(new QueryColumn(LCSPaletteMaterialLink.class, "roleBObjectRef.key.id"),
				new QueryColumn(LCSPalette.class, "thePersistInfo.theObjectIdentifier.id"));
		Collection vector = LCSQuery.getObjectsFromResults(statement, "OR:com.lcs.wc.color.LCSPalette:",
				"LCSPALETTE.IDA2A2");
		return vector;
	}

	public Collection findPalettesForMaterialSupplier(String oid) throws WTException {
		return this.findPalettesForMaterialSupplier((LCSMaterialSupplier) findObjectById(oid));
	}

	public Collection findMaterialsTrackerForMaterialSupplier(LCSMaterialSupplier materialSupplier) throws WTException {
		FlexType materialsTrackerFlexType = FlexTypeCache.getFlexTypeFromPath("Collection\\MaterialDevTracker");
		if (!ACLHelper.hasViewAccess(materialsTrackerFlexType)) {
			return new Vector();
		} else {
			LCSMaterialSupplierMaster materialSupplierMaster = materialSupplier.getMaster();
			String materialSupplierMasterId = FormatHelper.getNumericObjectIdFromObject(materialSupplierMaster);
			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.appendFromTable(MatSupToCollectionLinkMaster.class);
			statement.appendFromTable(FlexCollectionMaster.class);
			statement.appendFromTable(FlexCollection.class);
			statement
					.appendSelectColumn(new QueryColumn(FlexCollection.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendAndIfNeeded();
			statement
					.appendCriteria(
							new Criteria(new QueryColumn(MatSupToCollectionLinkMaster.class,
									"materialSupplierMasterReference.key.id"), "?", "="),
							new Long(materialSupplierMasterId));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(FlexCollection.class, "checkoutInfo.state"), "wrk", "<>"));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(FlexCollection.class, "iterationInfo.latest"), "1", "="));
			statement.appendJoin(
					new QueryColumn(MatSupToCollectionLinkMaster.class, "collectionMasterReference.key.id"),
					new QueryColumn(FlexCollectionMaster.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendJoin(new QueryColumn(FlexCollection.class, "masterReference.key.id"),
					new QueryColumn(FlexCollectionMaster.class, "thePersistInfo.theObjectIdentifier.id"));
			Collection vector = LCSQuery.getObjectsFromResults(statement, "OR:com.lcs.wc.collection.FlexCollection:",
					"FlexCollection.IDA2A2");
			return vector;
		}
	}

	public Collection findMaterialsTrackerForMaterialSupplier(String oid) throws WTException {
		return this.findMaterialsTrackerForMaterialSupplier((LCSMaterialSupplier) findObjectById(oid));
	}

	static {
		getPlaceholder();
	}

	
	
	/**
	 * @Override method addQuickSearchCriteriaForCommaSeparated
	 * If Search Criteria contain with comm separated value of referance no then Search by this method 
	 * 
	 * @param PreparedQueryStatement 
	 *				- creating the PreparedQueryStatement statement
	 * @param FlexType 
	 *				- flexType of material object
	 * @param objectName 
	 *				- material
	 * @param defaultAttributeName 
	 *				- creating the PreparedQueryStatement statement
	 * @param quickSearchCriteria 
	 *				- quick Search Criteria String 
	 * @param List of keyWordsList 
	 *				- List of keyword slipt by (,) seprated  
	 * @return - statement with appending all keywords in Criteria
	 
	 * @throws LCSException
	 */
	public static PreparedQueryStatement addQuickSearchCriteriaForCommaSeparated(PreparedQueryStatement statement, FlexType type,
			String objectName, String defaultAttributeName, String quickSearchCriteria, List<String> keyWordsList) throws LCSException {
		if (!FormatHelper.hasContent(quickSearchCriteria)) {
			return statement;
		} else {
			String configurableAttributes = LCSProperties.get("jsp.main.header.quickSearchAttributes." + objectName);
			if (!FormatHelper.hasContent(configurableAttributes)) {
				LOGGER.info(
						"No configurable search field defined in property file. Adding name field as default for search criteria");
				configurableAttributes = defaultAttributeName;
			}

			if (configurableAttributes != null && !"".equalsIgnoreCase(configurableAttributes)) {
				try {
					StringTokenizer attKeys = new StringTokenizer(configurableAttributes, ",");
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Configurable Attributes : " + configurableAttributes);
					}

					if (FormatHelper.hasContent(quickSearchCriteria)) {
						statement.appendAndIfNeeded();
						boolean firstPass = true;
						boolean isParenOpen = false;

						while (true) {
							if (!attKeys.hasMoreTokens()) {
								if (isParenOpen) {
									statement.appendClosedParen();
								}
								break;
							}

							String attKey = attKeys.nextToken();
							FlexTypeAttribute attribute = null;

							String attributeType;
							String typeName;
							try {
								attribute = type.getAttribute(attKey);
								String attScope = attribute.getAttScope();
								String attLevel = attribute.getAttObjectLevel();
								attributeType = attribute.getAttVariableType();
								FlexTypeScopeDefinition scopeDef = FlexTypeScopeDefinitionFactory
										.getDefinition(type.getTypeScopeDefinition());
								if (FormatHelper.hasContent(attScope) || FormatHelper.hasContent(attLevel)) {
									label182 : {
										if (objectName.equalsIgnoreCase("PLACEHOLDER")
												&& !attScope.equalsIgnoreCase("PLACEHOLDER")) {
											throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
													"attNotConfigured_MSG", objA);
										}

										if (!objectName.equalsIgnoreCase("SKU") || attScope.equalsIgnoreCase("PRODUCT")
												&& (attLevel.equalsIgnoreCase("SKU")
														|| attLevel.equalsIgnoreCase("PRODUCT-SKU"))) {
											if (!objectName.equalsIgnoreCase("PRODUCT")
													|| attScope.equalsIgnoreCase("PRODUCT")
															&& (attLevel.equalsIgnoreCase("PRODUCT")
																	|| attLevel.equalsIgnoreCase("PRODUCT-SKU"))) {
												if (objectName.equalsIgnoreCase("MATERIAL")
														&& !attScope.equalsIgnoreCase("MATERIAL")) {
													throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
															"attNotConfigured_MSG", objA);
												}

												if (objectName.equalsIgnoreCase("ORDERCONFIRMATION")
														&& !attScope.equalsIgnoreCase("HEADER_SCOPE")) {
													throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
															"attNotConfigured_MSG", objA);
												}

												if (objectName.equalsIgnoreCase("PLAN")
														&& !attScope.equalsIgnoreCase("PLAN_SCOPE")) {
													throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
															"attNotConfigured_MSG", objA);
												}

												if (objectName.equalsIgnoreCase("RFQ")
														&& !attScope.equalsIgnoreCase("REQUEST_HEADER_SCOPE")) {
													throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
															"attNotConfigured_MSG", objA);
												}

												if ((objectName.equalsIgnoreCase("SAMPLE")
														|| objectName.equalsIgnoreCase("SAMPLEPRODUCT")
														|| objectName.equalsIgnoreCase("SAMPLEMATERIAL"))
														&& !attScope.equalsIgnoreCase("SAMPLE")) {
													throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
															"attNotConfigured_MSG", objA);
												}

												if (objectName.equalsIgnoreCase("SIZING")
														&& !attScope.equalsIgnoreCase("SIZING_SCOPE")) {
													throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
															"attNotConfigured_MSG", objA);
												}
												break label182;
											}

											throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
													"attNotConfigured_MSG", objA);
										}

										throw new LCSException("com.lcs.wc.resource.QuickSearchRB",
												"attNotConfigured_MSG", objA);
									}
								}

								typeName = scopeDef.getTableName(attScope, attLevel, type);
							} catch (WTException var18) {
								LOGGER.error(" Attribute with key " + attKey + "is not present for type "
										+ type.getTypeDisplayName() + ".");
								throw new LCSException("com.lcs.wc.resource.QuickSearchRB", "attNotConfigured_MSG",
										objA);
							}

							String groupName = attribute.getAttributeGroup() == null
									? "General Attributes"
									: attribute.getAttributeGroup().getGroupName(false);
							AttributeGroup group = type.getAttributeGroupObject(groupName, false);
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Configurable Attribute Name : " + attribute.getAttributeName()
										+ " -AttributeType : " + attributeType + " -Group : " + groupName);
							}

							if (ACLHelper.hasViewAccess(group)) {
								if (!attributeType.equals("textArea") && !attributeType.equals("text")
										&& !attributeType.equals("derivedString")) {
									LOGGER.error("CQS not supported for the configurable Attribute Name : "
											+ attribute.getAttDisplay() + " -AttributeType : " + attributeType);
									throw new LCSException("com.lcs.wc.resource.QuickSearchRB", "attNotConfigured_MSG",
											objA);
								}

								if (firstPass) {
									statement.appendAndIfNeeded();
									statement.appendOpenParen();
									isParenOpen = true;
								} else {
									statement.appendOrIfNeeded();
								}

								firstPass = false;
									//START Materail Ref no SEARCH Logic for Pass keyword one by one for append in statement
								for (int i = 0; i < keyWordsList.size(); i++) {
								statement.addPossibleSearchCriteria(
										new QueryColumn(typeName, attribute.getColumnName()), keyWordsList.get(i),
										true);
											if (i != keyWordsList.size() - 1) {
										statement.appendOrIfNeeded();
									}
								}
								// END 
							}
						}
					}
				} catch (WTException var19) {
					LOGGER.error("Error occurred while building configurable quick search critera :"
							+ var19.getLocalizedMessage());
					throw new LCSException(var19.getLocalizedMessage());
				}
			}

			return statement;
		}
	}

}
