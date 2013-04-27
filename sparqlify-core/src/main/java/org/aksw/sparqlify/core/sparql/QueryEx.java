package org.aksw.sparqlify.core.sparql;

import com.hp.hpl.jena.query.Query;

public class QueryEx {
	private Query query;
	private boolean isExplain;

	public QueryEx() {
		this(null, false);
	}
	
	public QueryEx(Query query) {
		this(query, false);
	}
	
	public QueryEx(Query query, boolean isExplain) {
		this.query = query;
		this.isExplain = isExplain;
	}
	
	public Query getQuery() {
		return query;
	}
	public void setQuery(Query query) {
		this.query = query;
	}
	public boolean isExplain() {
		return isExplain;
	}
	public void setExplain(boolean isExplain) {
		this.isExplain = isExplain;
	}
	
	public int getQueryType() {
		int result;
		
		if(isExplain) {
			result = Query.QueryTypeSelect;
		} else {
			result = query.getQueryType();
		}
		
		return result;
	}
	
	public boolean isSelectType() {
		return getQueryType() == Query.QueryTypeSelect;
	}

	public boolean isConstructType() {
		return getQueryType() == Query.QueryTypeConstruct;
	}

	public boolean isDescribeType() {
		return getQueryType() == Query.QueryTypeDescribe;
	}

	public boolean isAskType() {
		return getQueryType() == Query.QueryTypeAsk;
	}

}
