package org.aksw.sparqlify.core.sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

public class QueryFactoryEx {
	public static QueryEx create(String queryString) {
		String explainStr = "explain";

		String tmp = queryString.trim();
		int n = explainStr.length() + 1;
		String start = tmp.substring(0, n).trim();

		boolean isExplain = false;
		if (start.equalsIgnoreCase(explainStr)) {
			isExplain = true;
			tmp = tmp.substring(n);
		}

		Query query = QueryFactory.create(tmp, null,
				Syntax.syntaxSPARQL_11);

		QueryEx result = new QueryEx(query, isExplain);
		return result;
	}
}
