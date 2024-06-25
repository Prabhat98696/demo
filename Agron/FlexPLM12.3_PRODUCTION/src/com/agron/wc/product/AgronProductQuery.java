package com.agron.wc.product;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.client.web.FlexTypeGenerator;
import com.lcs.wc.client.web.TemplateFilterHelper;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.foundation.LCSQueryPluginManager;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.report.FiltersList;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import java.io.Externalizable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import wt.part.WTPartMaster;
import wt.util.WTException;

public class AgronProductQuery extends LCSQuery implements Externalizable {
	private static final String CLASSNAME = AgronProductQuery.class.getName();
	private static final boolean DEBUG = LCSProperties.getBoolean("com.lcs.wc.product.LCSProductQuery.verbose");
	private static final boolean CASE_INSENSITIVE_SORT = LCSProperties
			.getBoolean("com.lcs.wc.product.LCSProductQuery.caseInsensitiveSort");
	public static final String PRIMARYVENDORREFERENCE = LCSProperties.get("com.lcs.wc.sourcing.PrimaryVendorReference",
			"vendor");

	public SearchResults findProductsByCriteria(Map criteria, FlexType type, Collection attCols, FiltersList filter,
			Collection oidList, String attKey) throws WTException {
				
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable("prodarev", "LCSProduct");
		statement.appendSelectColumn(new QueryColumn("LCSProduct", "idA3masterReference"));
		statement.appendSelectColumn(new QueryColumn("LCSProduct", "branchIditerationInfo"));
		statement.appendSelectColumn(new QueryColumn("LCSProduct", "idA2A2"));
		statement.appendSelectColumn(new QueryColumn("LCSProduct", "typeDisplay"));
		statement.appendSelectColumn(new QueryColumn("LCSProduct", "partPrimaryImageURL"));
		this.determineRanges(criteria, statement);
		FlexTypeAttribute displayAttribute = type.getAttribute(attKey);
		String results;
		if (criteria != null) {
			if (filter == null) {
				statement.addPossibleSearchCriteria(displayAttribute.getSearchResultsTableName(),
						displayAttribute.getColumnName(),
						"" + criteria.get(displayAttribute.getSearchCriteriaIndex()), true);
			}

			if (filter == null) {
				statement.addPossibleSearchCriteria(new QueryColumn("LCSProduct", "ptc_str_1typeInfoLCSProduct"),
						(String) criteria.get("name"), true);
			}

			statement.addPossibleSearchCriteria(
					new QueryColumn("LCSProduct", type.getAttribute(attKey).getColumnName()),
					(String) criteria.get("quickSearchCriteria"), true);
			if (type != null) {
				FlexTypeGenerator flexg = new FlexTypeGenerator();
				flexg.setScope("PRODUCT");
				flexg.setLevel("PRODUCT");

				try {
					flexg.appendHardColumns(statement, type, "LCSProduct", true, true);
				} catch (ClassNotFoundException var15) {
					var15.printStackTrace();
				}
				
				/*FlexTypeGenerator flexTypeGenerator = new FlexTypeGenerator();
				flexTypeGenerator.setScope("PRODUCT");
				flexTypeGenerator.setLevel("PRODUCT");*/
				flexg.addFlexTypeCriteria(statement, type, "LCSProduct");
				
//				statement.appendFromTable("FlexType");
				
//				statement.appendJoin(new QueryColumn("LCSProduct", "ida3a11"), new QueryColumn("FlexType", "idA2A2"));
				addFlexTypeInformation(statement, LCSProduct.class);
				/*statement.appendSelectColumn(new QueryColumn("FlexType", "typeName"));
				statement.appendSelectColumn(new QueryColumn("FlexType", "idA2A2"));*/
				results = (String) criteria.get("childFlexType");
				FlexType childFlexType = FlexTypeCache.getFlexType(results);
				if (filter == null) {
					statement = flexg.generateSearchCriteria(type, statement, criteria, true, (String) null, true,
							childFlexType);
				} else if (FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
					Map params = TemplateFilterHelper.getTemplateFilterParameters(filter,
							Collections.unmodifiableMap(criteria));
					statement = flexg.generateSearchCriteria(type, statement, params, true, (String) null, true,
							childFlexType);
				} else {
					flexg.addFlexTypeCriteria(statement, type, (String) null, childFlexType);
				}

				statement = flexg.createSearchResultsQueryColumns(attCols, type, statement);
			}

			this.addPossibleSort((String) criteria.get("sortBy1"), statement, CASE_INSENSITIVE_SORT);
		}

		if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
			filter.addFilterCriteria(statement);
		}

		if (oidList != null) {
			Iterator it = oidList.iterator();
			statement.appendAndIfNeeded();
			statement.appendOpenParen();

			while (it.hasNext()) {
				results = FormatHelper.getNumericFromOid((String) it.next());
				statement.appendOrIfNeeded();
				statement.appendCriteria(new Criteria(new QueryColumn("LCSProduct", "branchIditerationInfo"), "?", "="),
						new Long(results));
			}

			statement.appendClosedParen();
		}

		if (criteria.containsKey("orderSourceId")) {
			FlexType scFlexType = FlexTypeCache.getFlexTypeFromPath("Sourcing Configuration");
			FlexTypeAttribute primaryVendorAtt = scFlexType.getAttribute(PRIMARYVENDORREFERENCE);
			statement.appendFromTable("LCSSourcingConfig");
			statement.appendJoin(new QueryColumn("LCSProduct", "iterationInfo.branchId"),
					new QueryColumn("LCSSourcingConfig", "productARevId"));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn("LCSSourcingConfig", primaryVendorAtt.getColumnName()), "?", "="),
					new Long((String) criteria.get("orderSourceId")));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn("LCSSourcingConfig", "iterationInfo.latest"), "?", "="), "1");
			statement.setDistinct(true);
			if (criteria.containsKey("seasonVersionId")) {
				String seasonVersionId = (String) criteria.get("seasonVersionId");
				LCSSeason season = (LCSSeason) LCSQuery
						.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + seasonVersionId);
				LCSSeasonMaster seasonMaster = (LCSSeasonMaster) season.getMaster();
				statement.appendFromTable("LCSSourceToSeasonLink");
				statement.appendFromTable("LCSSourceToSeasonLinkMaster");
				statement.appendJoin(new QueryColumn("LCSSourceToSeasonLink", "idA3masterReference"),
						new QueryColumn("LCSSourceToSeasonLinkMaster", "idA2A2"));
				statement.appendJoin(new QueryColumn("LCSSourceToSeasonLinkMaster", "idA3A6"),
						new QueryColumn("LCSSourcingConfig", "idA3masterReference"));
				statement.appendAndIfNeeded();
				statement.appendCriteria(
						new Criteria(new QueryColumn("LCSSourceToSeasonLinkMaster", "idA3A9"), "?", "="),
						new Long(FormatHelper.getNumericObjectIdFromObject(seasonMaster)));
			}
		} else if (criteria.containsKey("seasonVersionId") && !criteria.containsKey("orderSourceId")) {
			
			String seasonVersionId = (String) criteria.get("seasonVersionId");
			LCSSeason season = (LCSSeason) LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + seasonVersionId);
			LCSSeasonMaster seasonMaster = (LCSSeasonMaster) season.getMaster();
			//statement.appendFromTable("LCSSeasonProductLink");
			statement.appendFromTable(LCSProductSeasonLink.class, "LCSSeasonProductLink");
		//	statement.appendJoin(new QueryColumn("LCSSourcingConfig", "productMasterId"),
			statement.appendJoin(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "productMasterId"),
					new QueryColumn("LCSProduct", "idA3masterReference"));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "idA3B5"), "?", "="),
					new Long(FormatHelper.getNumericObjectIdFromObject(seasonMaster)));
			statement.appendAndIfNeeded();
			statement.appendCriteria(new Criteria(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "effectLatest"), "?", "="),
					new Long(1L));
			statement.appendAndIfNeeded();
			statement.appendCriteria(
					new Criteria(new QueryColumn("LCSSeasonProductLink", LCSProductSeasonLink.class, "seasonLinkType"), "PRODUCT", "="));
			statement.setDistinct(true);
		}

		if (ClientContext.getContext().isVendor) {
			statement = LCSQueryPluginManager.handleEvent("LCSProductQuery.findProductsByCriteria_VendorCriteria",
					statement, (Map) null);
		}

		if (type != null) {
			criteria.put(FlexTypeQuery.FLEXTYPE, type);
		} else {
			criteria.put(FlexTypeQuery.FLEXTYPE, FlexTypeCache.getFlexTypeRoot("Product"));
		}

		statement = LCSQueryPluginManager.handleEvent("LCSProductQuery.findProductsByCriteria", statement, criteria);
		statement.appendSortBy(new QueryColumn("LCSProduct", displayAttribute.getColumnName()),
				CASE_INSENSITIVE_SORT);
		//LCSLog.debug(CLASSNAME + ".findByCriteria: statement = " + statement);
		results = null;
		SearchResults finalResults = runDirectQuery(statement);
		return finalResults;
	}
}