package com.agron.wc.material;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.TemplateFilterHelper;
import com.lcs.wc.collection.FlexCollection;
import com.lcs.wc.collection.FlexCollectionMaster;
import com.lcs.wc.collection.MatToCollectionLinkMaster;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.color.LCSPalette;
import com.lcs.wc.color.LCSPaletteMaterialLink;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.flextype.AttributeGroup;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.flextype.FlexTypeQueryStatement;
import com.lcs.wc.flextype.FlexTypeScopeDefinition;
import com.lcs.wc.flextype.FlexTypeScopeDefinitionFactory;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.foundation.LCSQueryPluginManager;
import com.lcs.wc.material.LCSMaterial;
import com.lcs.wc.material.LCSMaterialColor;
import com.lcs.wc.material.LCSMaterialMaster;
import com.lcs.wc.material.LCSMaterialSupplier;
import com.lcs.wc.material.LCSMaterialSupplierMaster;
import com.lcs.wc.report.FiltersList;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.sample.LCSSampleRequest;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.supplier.LCSSupplierMaster;
import com.lcs.wc.util.ACLHelper;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.MultiCharDelimStringTokenizer;
import com.lcs.wc.util.RequestHelper;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import java.io.Externalizable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.org.WTUser;
import wt.util.WTException;

public class AgronMaterialQuery extends LCSQuery implements Externalizable {
	public static final Logger LOGGER = LogManager.getLogger(AgronMaterialQuery.class.getName());
	private static final String CLASSNAME = AgronMaterialQuery.class.getName();
	private static final boolean CASE_INSENSITIVE_SORT = LCSProperties
			.getBoolean("com.lcs.wc.material.LCSMaterialQuery.caseInsensitiveSort");
	public static final String PLACEHOLDER_NAME = "material_placeholder";
	public static String PLACEHOLDERID = "";
	public static final String DELIM = "|~*~|";
	public static String PLACEHOLDERNUMERIC = "";
	public static Long PLACEHOLDER_NUMERIC_LONG;
	public static LCSMaterialMaster PLACEHOLDER = null;
	public static String COLOR_DEV_ROOT_TYPE = "";
	public static String MATERIAL_NAME_ATTR_COLUMN;
	public static String MATERIAL_NAME_ATTR_COLUMNDESCRIPTOR;
	public static String SUPPLIER_NAME_ATTR_COLUMN;
	public static String SUPPLIER_NAME_ATTR_COLUMNDESCRIPTOR;
	public static String COLOR_NAME_ATTR_COLUMNDESCRIPTOR;
	public static String MATERIAL_SUPPLIER_ROOT_TYPE = LCSProperties.get("com.lcs.wc.supplier.MaterialSupplierRootType",
			"Supplier");
	static final Object[] objA = new Object[0];
	
	public static void queryPlaceholder() {
		try {
			PLACEHOLDER = getPlaceholder();
			PLACEHOLDERID = FormatHelper.getObjectId(PLACEHOLDER);
			PLACEHOLDERNUMERIC = FormatHelper.getNumericObjectIdFromObject(PLACEHOLDER);
			PLACEHOLDER_NUMERIC_LONG = Long.valueOf(PLACEHOLDERNUMERIC);
			MATERIAL_NAME_ATTR_COLUMN = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name").getColumnName();
			MATERIAL_NAME_ATTR_COLUMNDESCRIPTOR = FlexTypeCache.getFlexTypeRoot("Material").getAttribute("name")
					.getColumnDescriptorName();
			SUPPLIER_NAME_ATTR_COLUMN = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("name").getColumnName();
			SUPPLIER_NAME_ATTR_COLUMNDESCRIPTOR = FlexTypeCache.getFlexTypeRoot("Supplier").getAttribute("name")
					.getColumnDescriptorName();
			COLOR_NAME_ATTR_COLUMNDESCRIPTOR = FlexTypeCache.getFlexTypeRoot("Color").getAttribute("name")
					.getColumnDescriptorName();
		} catch (Exception var1) {
			System.out.println("WARNING!!!!!!!!!!!!!!!!!!!!: PLACEHOLDER MATERIAL IS MISSING: OK IF SYSTEMSETUP.");
			PLACEHOLDER_NUMERIC_LONG = Long.valueOf("0");
		}

	}

	public SearchResults findMaterialsByCriteria(HttpServletRequest request, FlexType type) throws WTException {
		return this.findMaterialsByCriteria(request, type, (Collection) null);
	}

	public SearchResults findMaterialsByCriteria(HttpServletRequest request, FlexType type, Collection attCols)
			throws WTException {
		return this.findMaterialsByCriteria((HttpServletRequest) request, type, attCols, (FiltersList) null);
	}

	public SearchResults findMaterialsByCriteria(HttpServletRequest request, FlexType type, Collection attCols,
			FiltersList filter) throws WTException {
		return this.findMaterialsByCriteria((HttpServletRequest) request, type, attCols, filter, (Collection) null);
	}

	public SearchResults findMaterialsByCriteria(HttpServletRequest request, FlexType type, Collection attCols,
			FiltersList filter, Collection oidList) throws WTException {
		return this.findMaterialsByCriteria((Map) RequestHelper.hashRequest(request), type, attCols, filter, oidList,
				0);
	}

	public SearchResults findMaterialsByCriteria(HttpServletRequest request, FlexType type, Collection attCols,
			FiltersList filter, Collection oidList, int queryLimit) throws WTException {
		return this.findMaterialsByCriteria((Map) RequestHelper.hashRequest(request), type, attCols, filter, oidList,
				queryLimit);
	}

	public SearchResults findMaterialsByCriteria(Map criteria, FlexType type, Collection attCols, FiltersList filter)
			throws WTException {
		return this.findMaterialsByCriteria((Map) criteria, type, attCols, filter, (Collection) null);
	}

	public SearchResults findMaterialsByCriteria(Map criteria, FlexType type, Collection attCols, FiltersList filter,
			Collection oidList) throws WTException {
		return this.findMaterialsByCriteria((Map) criteria, type, attCols, filter, oidList, 0);
	}
	
	public SearchResults findMaterialsByCriteria(Map criteria, FlexType type, Collection attCols, FiltersList filter,
			Collection oidList, int queryLimit) throws WTException {
		
		boolean includeSupplier = false;
		boolean includeColor = false;
		boolean includeSample = false;
		boolean searchBySupplier = false;
		boolean searchByColor = false;
		boolean searchBySample = false;
		FlexType colorType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.color.LCSColor");
		FlexType supplierType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.supplier.LCSSupplier");
		FlexType sampleType = FlexTypeCache.getFlexTypeFromPath(COLOR_DEV_ROOT_TYPE);
		FlexType materialColorType = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterialColor");
		
		List<String> keyWordsList = new ArrayList<String>();
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

		
		if (FormatHelper.hasContent("" + criteria.get("colorType"))) {
			colorType = FlexTypeCache.getFlexType("" + criteria.get("colorType"));
		}

		if (FormatHelper.hasContent("" + criteria.get("supplierType"))) {
			supplierType = FlexTypeCache.getFlexType("" + criteria.get("supplierType"));
		}

		if (FormatHelper.hasContent("" + criteria.get("sampleType"))) {
			sampleType = FlexTypeCache.getFlexType("" + criteria.get("sampleType"));
		}

		if (FormatHelper.hasContent("" + criteria.get("includeSupplier"))) {
			includeSupplier = new Boolean("" + criteria.get("includeSupplier"));
		}

		if (FormatHelper.hasContent("" + criteria.get("includeColor"))) {
			includeColor = new Boolean("" + criteria.get("includeColor"));
		}

		if (FormatHelper.hasContent("" + criteria.get("includeSample"))) {
			includeSample = new Boolean("" + criteria.get("includeSample"));
		}

		if (FormatHelper.hasContent("" + criteria.get("searchBySupplier"))) {
			searchBySupplier = new Boolean("" + criteria.get("searchBySupplier"));
		}

		if (FormatHelper.hasContent("" + criteria.get("searchByColor"))) {
			searchByColor = new Boolean("" + criteria.get("searchByColor"));
		}

		if (FormatHelper.hasContent("" + criteria.get("searchBySample"))) {
			searchBySample = new Boolean("" + criteria.get("searchBySample"));
		}

		FlexTypeAttribute nameAtt = type.getAttribute("name");
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterial.class);
		if (includeSupplier || searchBySupplier) {
			statement.appendFromTable(LCSSupplier.class);
			statement.appendFromTable(LCSMaterialSupplier.class);
			statement.appendFromTable(LCSMaterialSupplierMaster.class);
			statement.appendFromTable("V_LCSMaterialSupplier");
		}

		if (includeColor || searchByColor) {
			statement.appendFromTable(LCSMaterialColor.class);
			statement.appendFromTable(LCSColor.class);
		}

		if (includeSample || searchBySample) {
			statement.appendFromTable(LCSSample.class);
			statement.appendFromTable(LCSSampleRequest.class);
		}

		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, nameAtt.getColumnDescriptorName()));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "typeDisplay"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "primaryImageURL"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "state.state"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, TYPED_BRANCH_ID));
		if (includeSupplier || searchBySupplier) {
			statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, SUPPLIER_NAME_ATTR_COLUMNDESCRIPTOR));
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
			statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
					new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
					new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
			statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
					new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
		}

		if (includeColor || searchByColor) {
			statement.appendSelectColumn(
					new QueryColumn(LCSMaterialColor.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "state.state"));
			statement.appendSelectColumn(new QueryColumn(LCSColor.class, COLOR_NAME_ATTR_COLUMNDESCRIPTOR));
			statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorName"));
			statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorHexidecimalValue"));
			statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thumbnail"));
			statement.appendAndIfNeeded();
			statement.appendJoin(
					new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"),
					new QueryColumn(LCSMaterialColor.class, "materialSupplierMasterReference.key.id(+)"));
			statement.appendJoin(new QueryColumn(LCSMaterialColor.class, "colorReference.key.id"),
					new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id(+)"));
			statement.appendAndIfNeeded();
		}

		String attColumn1;
		String listId;
		if (includeSample || searchBySample) {
			attColumn1 = sampleType.getAttribute("name").getColumnDescriptorName();
			listId = sampleType.getAttribute("requestName").getColumnDescriptorName();
			statement.appendSelectColumn(new QueryColumn(LCSSample.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendSelectColumn(new QueryColumn(LCSSample.class, attColumn1));
			statement.appendSelectColumn(new QueryColumn(LCSSampleRequest.class, listId));
			statement.appendSelectColumn(
					new QueryColumn(LCSSampleRequest.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendAndIfNeeded();
			if (!searchByColor && !includeColor) {
				statement.appendJoin(new QueryColumn(LCSSample.class, "sourcingMasterReference.key.id(+)"),
						new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"));
			} else {
				statement.appendJoin(new QueryColumn(LCSSample.class, "colorReference.key.id(+)"),
						new QueryColumn(LCSMaterialColor.class, "thePersistInfo.theObjectIdentifier.id"));
			}

			statement.appendJoin(new QueryColumn(LCSSample.class, "sampleRequestReference.key.id"),
					new QueryColumn(LCSSampleRequest.class, "thePersistInfo.theObjectIdentifier.id(+)"));
			statement.appendFromTable(WTTypeDefinition.class, "SAMPLEFLEXTYPE");
			statement.appendJoin(new QueryColumn(LCSSample.class, TYPED_BRANCH_ID),
					new QueryColumn("SAMPLEFLEXTYPE", WTTypeDefinition.class, "iterationInfo.branchId(+)"));
			statement.appendSelectColumn(
					new QueryColumn("SAMPLEFLEXTYPE", WTTypeDefinition.class, "iterationInfo.branchId"));
		}

		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterial.class, type.getAttribute("name").getColumnDescriptorName()),
						"material_placeholder", "<>"));
		if (includeSupplier || searchBySupplier) {
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
			statement.appendAndIfNeeded();
			statement
					.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.latest"), "1", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterialSupplier.class, "active"), "1", "="));
			if (searchBySupplier) {
				statement.appendAndIfNeeded();
				statement.appendCriteria(
						new Criteria(new QueryColumn(LCSMaterialSupplierMaster.class, "placeholder"), "0", "="));
			}
		}

		this.determineRanges(criteria, statement);
		String numeric;
		if (criteria != null) {
			if (filter == null) {
				this.addPossibleSearchCriteria(new QueryColumn(LCSMaterial.class, nameAtt.getColumnDescriptorName()),
						"" + criteria.get("name"), statement);
			}

			if (!keyWordsList.isEmpty() && keyWordsList.size() == 1) {
				// //Task #1746 If only 1 keyword in quicksearchcriteria then return OOTB statement
				addQuickSearchCriteria(statement, type, "Material", "name", keyWordsList.get(0));
			}
			else if (!keyWordsList.isEmpty() && keyWordsList.size() > 1) {
				// Task #1746 If multiple keywords in quicksearchcriteria then return statement
				// with append criteria for multiple keyword
				addQuickSearchCriteriaForCommaSeparated(statement, type, "Material", "name", (String) criteria.get("quickSearchCriteria"), keyWordsList);
			}
			
			addFlexTypeInformation(criteria, type, attCols, filter, statement, LCSMaterial.class, "LCSMaterial",
					(String) null, "MATERIAL");
			if (type != null) {
				ClientContext lcsContext = ClientContext.getContext();
				if (lcsContext.isVendor) {
					if (!includeSupplier && !searchBySupplier) {
						statement.appendFromTable(LCSMaterialSupplierMaster.class);
						statement.appendFromTable(LCSSupplier.class);
						statement.appendJoin(
								new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
								new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
						statement.appendJoin(
								new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
								new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
						criteria.put("excludeSupplierId", "true");
					}

					statement = LCSQueryPluginManager.handleEvent(
							"LCSMaterialSupplierQuery.findMaterialSuppliersByCriteria_VendorCriteria", statement,
							criteria);
				}

				Map params;
				FlexTypeGenerator flexg;
				if (searchBySupplier || includeSupplier) {
					flexg = new FlexTypeGenerator();
					flexg.setScope("MATERIAL-SUPPLIER");
					flexg.setLevel((String) null);
					statement = flexg.createSearchResultsQueryColumns(attCols, type, statement);
					statement.appendSelectColumn(
							new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.creator.key.id"));
					statement.appendSelectColumn(
							new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.modifier.key.id"));
					statement.appendSelectColumn(
							new QueryColumn(LCSMaterialSupplier.class, "thePersistInfo.createStamp"));
					statement.appendSelectColumn(
							new QueryColumn(LCSMaterialSupplier.class, "thePersistInfo.modifyStamp"));
					statement.appendFromTable(WTUser.class, "MSCREATOR");
					statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.creator.key.id"),
							new QueryColumn("MSCREATOR", "idA2A2(+)"));
					statement.appendFromTable(WTUser.class, "MSMODIFIER");
					statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.modifier.key.id"),
							new QueryColumn("MSMODIFIER", "idA2A2(+)"));
					statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "state.state"));
					if (searchBySupplier || includeSupplier && filter != null) {
						if (filter == null) {
							statement = flexg.generateSearchCriteria(type, statement, criteria);
						} else if (FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
							params = TemplateFilterHelper.getTemplateFilterParameters(filter,
									Collections.unmodifiableMap(criteria));
							statement = flexg.generateSearchCriteria(type, statement, params);
						} else {
							filter.addFilterCriteria(statement, FormatHelper.getObjectId(type), "MATERIAL-SUPPLIER");
						}
					} else {
						flexg.appendAttributeRules(type, statement, "LCSMaterialSupplier");
					}

					flexg = new FlexTypeGenerator();
					flexg.setScope((String) null);
					flexg.setLevel((String) null);
					statement = flexg.createSearchResultsQueryColumns(attCols, supplierType, statement);
					if (searchBySupplier || includeSupplier && filter != null
							&& !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
						if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
							filter.addFilterCriteria(statement, FormatHelper.getObjectId(supplierType));
							if (searchBySupplier) {
								statement = flexg.addFlexTypeCriteria(statement, supplierType, "LCSSupplier");
							}
						} else {
							statement = flexg.generateSearchCriteria(supplierType, statement, criteria);
						}
					}
				}

				if (searchByColor || includeColor) {
					flexg = new FlexTypeGenerator();
					flexg.setScope((String) null);
					flexg.setLevel((String) null);
					statement = flexg.createSearchResultsQueryColumns(attCols, colorType, statement);
					if (searchByColor || includeColor && filter != null
							&& !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
						if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
							filter.addFilterCriteria(statement, FormatHelper.getObjectId(colorType));
							if (searchByColor) {
								statement = flexg.addFlexTypeCriteria(statement, colorType, "LCSColor");
							}
						} else {
							statement = flexg.generateSearchCriteria(colorType, statement, criteria);
						}
					}

					flexg = new FlexTypeGenerator();
					flexg.setScope((String) null);
					flexg.setLevel((String) null);
					statement = flexg.createSearchResultsQueryColumns(attCols, materialColorType, statement);
					statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "creator.key.id"));
					statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "modifier.key.id"));
					statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "thePersistInfo.createStamp"));
					statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "thePersistInfo.modifyStamp"));
					statement.appendFromTable(WTUser.class, "MCCREATOR");
					statement.appendJoin(new QueryColumn(LCSMaterialColor.class, "creator.key.id"),
							new QueryColumn("MCCREATOR", "idA2A2(+)"));
					statement.appendFromTable(WTUser.class, "MCMODIFIER");
					statement.appendJoin(new QueryColumn(LCSMaterialColor.class, "modifier.key.id"),
							new QueryColumn("MCMODIFIER", "idA2A2(+)"));
					statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "state.state"));
					if (searchByColor || includeColor && filter != null
							&& !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
						if (filter == null) {
							statement = flexg.generateSearchCriteria(materialColorType, statement, criteria);
						} else if (FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
							params = TemplateFilterHelper.getTemplateFilterParameters(filter,
									Collections.unmodifiableMap(criteria));
							statement = flexg.generateSearchCriteria(materialColorType, statement, params);
						} else {
							filter.addFilterCriteria(statement, FormatHelper.getObjectId(materialColorType));
						}
					}
				}

				if (searchBySample || includeSample) {
					flexg = new FlexTypeGenerator();
					flexg.setScope("SAMPLE");
					flexg.setLevel((String) null);
					statement = flexg.createSearchResultsQueryColumns(attCols, sampleType, statement);
					if (searchBySample || includeSample && filter != null
							&& !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
						if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
							filter.addFilterCriteria(statement, FormatHelper.getObjectId(sampleType), "SAMPLE");
						} else {
							statement = flexg.generateSearchCriteria(sampleType, statement, criteria);
						}
					}

					flexg = new FlexTypeGenerator();
					flexg.setScope("SAMPLE-REQUEST");
					flexg.setLevel((String) null);
					statement = flexg.createSearchResultsQueryColumns(attCols, sampleType, statement);
					if (searchBySample || includeSample && filter != null
							&& !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
						if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
							filter.addFilterCriteria(statement, FormatHelper.getObjectId(sampleType), "SAMPLE-REQUEST");
							if (searchBySample) {
								statement = flexg.addFlexTypeCriteria(statement, sampleType, "LCSSampleRequest");
							}
						} else {
							statement = flexg.generateSearchCriteria(sampleType, statement, criteria);
						}
					}
				}
			}

			if (FormatHelper.hasContent((String) criteria.get("sortBy1")) && CASE_INSENSITIVE_SORT) {
				attColumn1 = (String) criteria.get("sortBy1");
				MultiCharDelimStringTokenizer tokenizer = new MultiCharDelimStringTokenizer(attColumn1, "|~*~|");

				while (tokenizer.hasMoreTokens()) {
					numeric = tokenizer.nextToken();
					if (numeric.indexOf(":") > -1) {
						numeric = numeric.substring(0, numeric.indexOf(":"));
					}

					if (numeric.indexOf(".") > -1) {
						String tableName = numeric.substring(0, numeric.indexOf("."));
						String columnName = numeric.substring(numeric.indexOf(".") + 1);
						statement.appendSelectColumn(new QueryColumn(tableName, columnName), "UPPER");
					}
				}
			}

			this.addPossibleSort("" + criteria.get("sortBy1"), statement, CASE_INSENSITIVE_SORT);
		}

		if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
			filter.addFilterCriteria(statement, FormatHelper.getObjectId(type), "MATERIAL");
		}

		if (oidList != null && oidList.size() > 0) {
			Iterator<?> it = oidList.iterator();
			statement.appendAndIfNeeded();
			statement.appendOpenParen();

			while (true) {
				while (it.hasNext()) {
					listId = (String) it.next();
					numeric = FormatHelper.getNumericFromOid(listId);
					if (listId.indexOf(".LCSMaterial:") <= -1 && listId.indexOf(".") >= 0) {
						if (listId.indexOf(".LCSMaterialMaster:") > -1) {
							statement.appendOrIfNeeded();
							statement.appendCriteria(
									new Criteria(new QueryColumn(LCSMaterial.class, "masterReference.key.id"), "?",
											"="),
									new Long(numeric));
						} else if (listId.indexOf(".LCSMaterialSupplier:") > -1) {
							statement.appendOrIfNeeded();
							statement.appendCriteria(
									new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"),
											"?", "="),
									new Long(numeric));
						} else if (listId.indexOf(".LCSSupplier:") > -1) {
							statement.appendOrIfNeeded();
							statement.appendCriteria(
									new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.branchId"), "?",
											"="),
									new Long(numeric));
						} else if (listId.indexOf(".LCSMaterialColor:") > -1) {
							statement.appendOrIfNeeded();
							statement
									.appendCriteria(
											new Criteria(new QueryColumn(LCSMaterialColor.class,
													"thePersistInfo.theObjectIdentifier.id"), "?", "="),
											new Long(numeric));
						} else if (listId.indexOf(".LCSColor:") > -1) {
							statement.appendOrIfNeeded();
							statement
									.appendCriteria(
											new Criteria(new QueryColumn(LCSColor.class,
													"thePersistInfo.theObjectIdentifier.id"), "?", "="),
											new Long(numeric));
						} else if (listId.indexOf(".LCSSampleRequest:") > -1) {
							statement.appendOrIfNeeded();
							statement
									.appendCriteria(
											new Criteria(new QueryColumn(LCSSampleRequest.class,
													"thePersistInfo.theObjectIdentifier.id"), "?", "="),
											new Long(numeric));
						} else if (listId.indexOf(".LCSSample:") > -1) {
							statement.appendOrIfNeeded();
							statement
									.appendCriteria(
											new Criteria(new QueryColumn(LCSSample.class,
													"thePersistInfo.theObjectIdentifier.id"), "?", "="),
											new Long(numeric));
						}
					} else {
						statement.appendOrIfNeeded();
						statement.appendCriteria(
								new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"), "?", "="),
								new Long(numeric));
					}
				}

				statement.appendClosedParen();
				break;
			}
		}

		if (CASE_INSENSITIVE_SORT) {
			statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, nameAtt.getColumnDescriptorName()),
					"UPPER");
		}

		statement.appendSortBy(nameAtt.getSearchResultIndex(), CASE_INSENSITIVE_SORT);
		if (searchBySupplier || includeSupplier) {
			if (CASE_INSENSITIVE_SORT) {
				attColumn1 = supplierType.getAttribute("name").getColumnDescriptorName();
				statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, attColumn1), "UPPER");
			}

			statement.appendSortBy(supplierType.getAttribute("name").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
		}

		if (searchByColor || includeColor) {
			if (CASE_INSENSITIVE_SORT) {
				attColumn1 = colorType.getAttribute("name").getColumnDescriptorName();
				statement.appendSelectColumn(new QueryColumn(LCSColor.class, attColumn1), "UPPER");
			}

			statement.appendSortBy(colorType.getAttribute("name").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
		}

		if (searchBySample || includeSample) {
			if (CASE_INSENSITIVE_SORT) {
				attColumn1 = sampleType.getAttribute("requestName").getColumnDescriptorName();
				statement.appendSelectColumn(new QueryColumn(LCSSampleRequest.class, attColumn1), "UPPER");
				listId = sampleType.getAttribute("name").getColumnDescriptorName();
				statement.appendSelectColumn(new QueryColumn(LCSSample.class, listId), "UPPER");
			}

			statement.appendSortBy(sampleType.getAttribute("requestName").getSearchResultIndex(),
					CASE_INSENSITIVE_SORT);
			statement.appendSortBy(sampleType.getAttribute("name").getSearchResultIndex(), CASE_INSENSITIVE_SORT);
		}

		if (type != null) {
			criteria.put(FlexTypeQuery.FLEXTYPE, type);
		} else {
			criteria.put(FlexTypeQuery.FLEXTYPE, FlexTypeCache.getFlexTypeRoot("Material"));
		}

		statement = LCSQueryPluginManager.handleEvent("LCSMaterialQuery.findMaterialsByCriteria", statement, criteria);
		statement.setQueryLimit(queryLimit);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(CLASSNAME + ".findMaterialsByCriteria 1: statement = ");
			printStatement(statement);
		}

		listId = null;
		statement.setDistinct(true);
		SearchResults results = runDirectQuery(statement);
		return results;
	}
	
	private static LCSMaterialMaster getPlaceholder() throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterialMaster.class);
		statement.appendSelectColumn(new QueryColumn(LCSMaterialMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialMaster.class, "name"), "material_placeholder", "="));
		LCSMaterialMaster master = (LCSMaterialMaster) getObjectFromResults(statement,
				"OR:com.lcs.wc.material.LCSMaterialMaster:", "LCSMATERIALMASTER.IDA2A2");
		return master;
	}

	public LCSMaterial findMaterialByNameType(String name, FlexType type) throws WTException {
		LCSMaterial material = null;
		PreparedQueryStatement statement = new PreparedQueryStatement();
		String nameAtt = FlexTypeCache.getFlexTypeRoot("Material").getAttribute(name).getAttributeName();
		statement.appendFromTable(LCSMaterial.class);
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, nameAtt), "?", "="), name);
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
		if (type != null) {
			String id = String.valueOf(type.getPersistInfo().getObjectIdentifier().getId());
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, TYPED_BRANCH_ID), "?", "="),
					new Long(id));
		}

		SearchResults results = runDirectQuery(statement);
		if (results.getResultsFound() > 0) {
			FlexObject obj = (FlexObject) results.getResults().elementAt(0);
			material = (LCSMaterial) findObjectById(
					"OR:com.lcs.wc.material.LCSMaterial:" + obj.getString("LCSMATERIAL.IDA2A2"));
		}

		return material;
	}

	public SearchResults findMaterialColorData(LCSMaterial material) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSMaterialColor.class);
		statement.appendFromTable(LCSColor.class);
		statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "state.state"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialColor.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorName"));
		statement.appendSelectColumn(new QueryColumn(LCSColor.class, "colorHexidecimalValue"));
		statement.appendJoin(new QueryColumn(LCSMaterialColor.class, "colorReference.key.id"),
				new QueryColumn(LCSColor.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialColor.class, "materialMasterReference.key.id"), "?", "="),
				new Long(getNumericFromOid(FormatHelper.getObjectId(material.getMaster()))));
		FlexType type = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.material.LCSMaterialColor");
		FlexTypeGenerator flexg = new FlexTypeGenerator();
		statement = flexg.createSearchResultsQueryColumns(type, statement);
		type = FlexTypeCache.getFlexTypeRootByClass("com.lcs.wc.color.LCSColor");
		statement = flexg.appendQueryColumns(type, statement);
		statement.appendSortBy(new QueryColumn(LCSColor.class, "colorName"), CASE_INSENSITIVE_SORT);
		SearchResults results = null;
		results = runDirectQuery(statement);
		return results;
	}

	public static SearchResults findMaterialInfoByPalette(LCSPalette palette, Map<?, ?> criteria) throws WTException {
		return findMaterialInfoByPalette(palette, criteria, (Collection) null);
	}

	public static SearchResults findMaterialInfoByPalette(LCSPalette palette, Map criteria, Collection attCols)
			throws WTException {
		return findMaterialInfoByPalette(palette, criteria, attCols, (FiltersList) null);
	}

	public static SearchResults findMaterialInfoByPalette(LCSPalette palette, Map criteria, Collection<?> attCols,
			FiltersList filter) throws WTException {
		Long paletteId = new Long(FormatHelper.getNumericObjectIdFromObject(palette));
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSPaletteMaterialLink.class);
		statement.appendFromTable(LCSMaterial.class);
		statement.appendFromTable(LCSSupplier.class);
		statement.appendFromTable(LCSMaterialSupplier.class);
		statement.appendFromTable(LCSMaterialSupplierMaster.class);
		statement.appendSelectColumn(
				new QueryColumn(LCSPaletteMaterialLink.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.branchId"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, TYPED_BRANCH_ID));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "typeDisplay"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "primaryImageURL"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "state.state"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.branchId"));
		statement.appendSelectColumn(
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterialSupplierMaster.class, "placeholder"));
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSPaletteMaterialLink.class, "roleBObjectRef.key.id"), "?", "="),
				paletteId);
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, MATERIAL_NAME_ATTR_COLUMNDESCRIPTOR));
		statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, SUPPLIER_NAME_ATTR_COLUMNDESCRIPTOR));
		statement.appendSelectColumn(new QueryColumn(LCSSupplier.class, "iterationInfo.branchId"));
		statement.appendJoin(new QueryColumn(LCSMaterial.class, "masterReference.key.id"),
				new QueryColumn(LCSPaletteMaterialLink.class, "roleAObjectRef.key.id"));
		statement.appendJoin(new QueryColumn(LCSSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSPaletteMaterialLink.class, "supplierMasterReference.key.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplier.class, "masterReference.key.id"),
				new QueryColumn(LCSMaterialSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "materialMasterReference.key.id"),
				new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
		statement.appendJoin(new QueryColumn(LCSMaterialSupplierMaster.class, "supplierMasterReference.key.id"),
				new QueryColumn(LCSSupplier.class, "masterReference.key.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "thePersistInfo.createStamp"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.creator.key.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "thePersistInfo.modifyStamp"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "iterationInfo.modifier.key.id"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSMaterial.class, MATERIAL_NAME_ATTR_COLUMNDESCRIPTOR),
				"material_placeholder", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(
				new Criteria(new QueryColumn(LCSMaterialSupplier.class, "iterationInfo.latest"), "1", "="));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "checkoutInfo.state"), "wrk", "<>"));
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSupplier.class, "iterationInfo.latest"), "1", "="));
		addFlexTypeInformation(statement, LCSMaterial.class);
		FlexType supplierRootType = FlexTypeCache.getFlexTypeFromPath(MATERIAL_SUPPLIER_ROOT_TYPE);
		generateSearchCriteriaAppendQueryColumns(criteria, supplierRootType, attCols, filter, statement, (String) null,
				(String) null);
		FlexType materialRootType = FlexTypeCache.getFlexTypeFromPath("Material");
		generateSearchCriteriaAppendQueryColumns(criteria, materialRootType, attCols, filter, statement, "MATERIAL",
				(String) null);
		generateSearchCriteriaAppendQueryColumns(criteria, materialRootType, attCols, filter, statement,
				"MATERIAL-SUPPLIER", (String) null);
		FlexType paletteRootType = palette.getFlexType();
		generateSearchCriteriaAppendQueryColumns(criteria, paletteRootType, attCols, filter, statement,
				"PALETTE_MATERIAL_SCOPE", (String) null);
		if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
			filter.addFilterCriteria(statement);
		}

		if (ClientContext.getContext().isVendor) {
			statement = LCSQueryPluginManager.handleEvent(
					"LCSMaterialSupplierQuery.findMaterialSuppliersByCriteria_VendorCriteria", statement, criteria);
		}

		if (criteria != null && FormatHelper.hasContent((String) criteria.get("sortBy1"))) {
			String sortIndx = (String) criteria.get("sortBy1");
			if (!FormatHelper.hasContent(sortIndx)) {
				statement.appendSortBy(new QueryColumn(LCSMaterial.class, MATERIAL_NAME_ATTR_COLUMNDESCRIPTOR),
						CASE_INSENSITIVE_SORT);
			} else {
				String table = sortIndx.substring(0, sortIndx.indexOf("."));
				String attColumn = sortIndx.contains(":")
						? sortIndx.substring(sortIndx.indexOf(".") + 1, sortIndx.indexOf(":"))
						: sortIndx.substring(sortIndx.indexOf(".") + 1);
				if (CASE_INSENSITIVE_SORT) {
					statement.appendSelectColumn(new QueryColumn(table, attColumn), "UPPER");
				}

				statement.appendSortBy(sortIndx, CASE_INSENSITIVE_SORT);
				if (attColumn.equals(MATERIAL_NAME_ATTR_COLUMN)) {
					statement.appendSortBy(new QueryColumn(LCSSupplier.class, SUPPLIER_NAME_ATTR_COLUMNDESCRIPTOR),
							CASE_INSENSITIVE_SORT);
				}
			}
		} else {
			if (CASE_INSENSITIVE_SORT) {
				statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, MATERIAL_NAME_ATTR_COLUMNDESCRIPTOR),
						"UPPER");
			}

			statement.appendSortBy(new QueryColumn(LCSMaterial.class, MATERIAL_NAME_ATTR_COLUMNDESCRIPTOR),
					CASE_INSENSITIVE_SORT);
			statement.appendSortBy(new QueryColumn(LCSSupplier.class, SUPPLIER_NAME_ATTR_COLUMNDESCRIPTOR),
					CASE_INSENSITIVE_SORT);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("statement = " + statement);
		}

		return LCSQuery.runDirectQuery(statement);
	}

	public Collection getMaterialImages(LCSMaterial material) throws WTException {
		return this.getMaterialImages(FormatHelper.getVersionId(material));
	}

	public Collection getMaterialImages(String id) throws WTException {
		FlexTypeQueryStatement statement = new FlexTypeQueryStatement();
		statement.setType("Document\\Images Page");
		statement.appendSelectColumn(new QueryColumn(LCSDocument.class, "iterationInfo.branchId"));
		statement.appendFlexSelectColumn("pageLayout");
		statement.appendFlexSelectColumn("pageType");
		statement.appendFlexSelectColumn("name");
		statement.appendAndIfNeeded();
		statement.appendFlexCriteria("ownerReference", id, "=");
		statement.appendAndIfNeeded();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSDocument.class, "iterationInfo.latest"), "1", "="));
		return getObjectsFromResults(statement, "VR:com.lcs.wc.document.LCSDocument:",
				"LCSDOCUMENT.BRANCHIDITERATIONINFO");
	}

	public static Collection findMaterialsForAIPlugin(FlexType flexType, Map map, Collection attCols)
			throws WTException {
		FlexTypeQueryStatement statement = new FlexTypeQueryStatement();
		statement.setType(flexType, "MATERIAL", (String) null);
		statement.appendFromTable(LCSPalette.class);
		statement.appendFromTable(LCSSeason.class);
		statement.appendFromTable(LCSMaterial.class);
		statement.appendFromTable(LCSPaletteMaterialLink.class);
		statement.appendFromTable(LCSSupplierMaster.class);
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "masterReference.key.id"));
		statement.appendSelectColumn(new QueryColumn(LCSMaterial.class, "primaryImageURL"));
		statement.appendSelectColumn(new QueryColumn(LCSSupplierMaster.class, "supplierName"));
		statement.appendAnd();
		statement.appendCriteria(new Criteria(new QueryColumn(LCSSeason.class, "masterReference.key.id"),
				(String) map.get("seasonMasterReference.key.id"), "="));
		statement.appendAnd();
		statement.appendJoin(new QueryColumn(LCSPalette.class, "thePersistInfo.theObjectIdentifier.id"),
				new QueryColumn(LCSSeason.class, "paletteReference.key.id"));
		statement.appendJoin(new QueryColumn(LCSPalette.class, "thePersistInfo.theObjectIdentifier.id"),
				new QueryColumn(LCSPaletteMaterialLink.class, "roleBObjectRef.key.id"));
		statement.appendJoin(new QueryColumn(LCSMaterial.class, "masterReference.key.id"),
				new QueryColumn(LCSPaletteMaterialLink.class, "roleAObjectRef.key.id"));
		statement.appendJoin(new QueryColumn(LCSMaterial.class, "flexTypeReference.key.id"),
				new QueryColumn(FlexType.class, "thePersistInfo.theObjectIdentifier.id"));
		statement.appendJoin(new QueryColumn(LCSSupplierMaster.class, "thePersistInfo.theObjectIdentifier.id"),
				new QueryColumn(LCSPaletteMaterialLink.class, "supplierMasterReference.key.id"));
		addFlexTypeInformation(statement, LCSMaterial.class);
		statement.addLatestIterationClause(LCSMaterial.class);
		FlexTypeGenerator flexg = new FlexTypeGenerator();
		flexg.setScope("MATERIAL");
		statement = (FlexTypeQueryStatement) flexg.appendQueryColumns(attCols, flexType, statement, "LCSMaterial");
		statement = (FlexTypeQueryStatement) flexg.generateSearchCriteria(flexType, statement, map);
		FlexTypeAttribute attr = flexType.getAttribute("name");
		String nameColumn = attr.getSearchResultIndex();
		statement.appendSortBy(nameColumn, CASE_INSENSITIVE_SORT);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(statement);
		}

		return LCSQuery.runDirectQuery(statement).getResults();
	}

	public Collection findPalettesForMaterial(LCSMaterial material) throws WTException {
		Collection<LCSPalette> vector = new Vector();
		QueryResult result = PersistenceHelper.manager.navigate(material.getMaster(), "palette",
				LCSPaletteMaterialLink.class, true);
		LCSPalette palette = null;

		while (result.hasMoreElements()) {
			palette = (LCSPalette) result.nextElement();
			if (!vector.contains(palette)) {
				vector.add(palette);
			}
		}

		return vector;
	}

	public Collection findPalettesForMaterial(String oid) throws WTException {
		return this.findPalettesForMaterial((LCSMaterial) findObjectById(oid));
	}

	public Collection findMaterialsTrackerForMaterial(LCSMaterial material) throws WTException {
		FlexType materialsTrackerFlexType = FlexTypeCache.getFlexTypeFromPath("Collection\\MaterialDevTracker");
		if (!ACLHelper.hasViewAccess(materialsTrackerFlexType)) {
			return new Vector();
		} else {
			LCSMaterialMaster materialMaster = material.getMaster();
			String materialMasterId = FormatHelper.getNumericObjectIdFromObject(materialMaster);
			PreparedQueryStatement statement = new PreparedQueryStatement();
			statement.appendFromTable(MatToCollectionLinkMaster.class);
			statement.appendFromTable(FlexCollectionMaster.class);
			statement.appendFromTable(FlexCollection.class);
			statement
					.appendSelectColumn(new QueryColumn(FlexCollection.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(MatToCollectionLinkMaster.class, "materialMasterReference.key.id"),
							"?", "="),
					new Long(materialMasterId));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(FlexCollection.class, "checkoutInfo.state"), "wrk", "<>"));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn(FlexCollection.class, "iterationInfo.latest"), "1", "="));
			statement.appendJoin(new QueryColumn(MatToCollectionLinkMaster.class, "collectionMasterReference.key.id"),
					new QueryColumn(FlexCollectionMaster.class, "thePersistInfo.theObjectIdentifier.id"));
			statement.appendJoin(new QueryColumn(FlexCollection.class, "masterReference.key.id"),
					new QueryColumn(FlexCollectionMaster.class, "thePersistInfo.theObjectIdentifier.id"));
			Collection vector = LCSQuery.getObjectsFromResults(statement, "OR:com.lcs.wc.collection.FlexCollection:",
					"FlexCollection.IDA2A2");
			return vector;
		}
	}

	public Collection findMaterialsTrackerForMaterial(String oid) throws WTException {
		return this.findMaterialsTrackerForMaterial((LCSMaterial) findObjectById(oid));
	}

	static {
		COLOR_DEV_ROOT_TYPE = LCSProperties.get("com.lcs.wc.sample.LCSSample.Material.ColorDevelopement.Root");
		queryPlaceholder();
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
