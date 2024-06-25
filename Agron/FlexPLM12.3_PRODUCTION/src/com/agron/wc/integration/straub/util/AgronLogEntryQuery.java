package com.agron.wc.integration.straub.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.foundation.LCSLogEntry;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.report.FiltersList;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.RequestHelper;

import wt.util.WTException;

public class AgronLogEntryQuery extends LCSQuery{

	public SearchResults findLogEntriesByCriteria(Map request, FlexType type, Collection attCols,
			FiltersList filter, Collection oidList) throws WTException {
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable(LCSLogEntry.class);
		statement.appendSelectColumn(new QueryColumn(LCSLogEntry.class, "thePersistInfo.theObjectIdentifier.id"));
		this.determineRanges(request, statement);
		addFlexTypeInformation(request, type, attCols, filter, statement, LCSLogEntry.class,
				"LCSLogEntry");
		if (filter != null && !FiltersList.TEMPLATE_FILTER_TYPE.equals(filter.getFilterType())) {
			filter.addFilterCriteria(statement);
		}

		String results;
		if (oidList != null) {
			Iterator it = oidList.iterator();
			statement.appendAndIfNeeded();
			statement.appendOpenParen();

			while (it.hasNext()) {
				results = FormatHelper.getNumericFromOid((String) it.next());
				statement.appendOrIfNeeded();
				statement.appendCriteria(
						new Criteria(new QueryColumn(LCSLogEntry.class, "thePersistInfo.theObjectIdentifier.id"), "?",
								"="),
						new Long(results));
			}

			statement.appendClosedParen();
		}

		statement.appendSortBy(new QueryColumn(LCSLogEntry.class, "thePersistInfo.updateStamp"), "desc");
		
		results = null;
		SearchResults results2 = runDirectQuery(statement);
		return results2;
	}
}
