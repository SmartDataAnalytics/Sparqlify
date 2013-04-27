package org.aksw.sparqlify.core.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;


public class QueryFactoryEx {
	
	public static void main(String[] args) {
		System.out.println(create("expLain sElect * { ?s ?p ?o \n\n }"));
		System.out.println(create("Prefix ex: <http://example.org/>\n\n expLain sElect * { ?s ?p ?o \n\n }"));
		System.out.println(create("Prefix ex: <http://example.org/>\n\n expLain\nconstruct { ?s ?p ?o \n\n } { ?s ?p ?o }"));
	}

	static String queryForms[] = new String[]{"ask", "construct", "select", "describe"};
	static Pattern explainPatterns[] = null;
	
	static {
		explainPatterns = new Pattern[queryForms.length];
		
		int i = 0;
		for(String queryForm : queryForms) {

			Pattern explainPattern = Pattern.compile("(^|\\s|\n)+explain(\\s|\n)+" + queryForm + "(\\s|\n)+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

			explainPatterns[i] = explainPattern;
			++i;
		}
	}
	
	public static QueryEx create(String queryString) {
		
		boolean isExplain = false;

		for(int i = 0; i < queryForms.length; ++i) {
			String queryForm = queryForms[i];
			Pattern pattern = explainPatterns[i];
			
			Matcher m = pattern.matcher(queryString);
			if(m.find()) {
				isExplain = true;

				queryString = m.replaceAll(" " + queryForm + " ");
				break;
			}
		}

		Query query = QueryFactory.create(queryString, null, Syntax.syntaxSPARQL_11);

		QueryEx result = new QueryEx(query, isExplain);
		return result;

		
		/*
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
		*/
	}
}
