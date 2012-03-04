package org.aksw.sparqlify.config.syntax;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.lang.Parser;



public class ConstructViewDefinition {
	private String name;
	private Query query;
	//private String queryString;
	
	public ConstructViewDefinition() {
	}
	
	/*
	public ConstructViewDefinition(String name, Query query) {
		this.name = name;
		this.query = query;
	}*/
	
	public ConstructViewDefinition(String name, String queryString, PrefixMapping prefixMapping) {
		this.name = name;
		this.query = new Query();
		
		query.setPrefixMapping(prefixMapping);
		Parser parser = Parser.createParser(Syntax.syntaxSPARQL_11);
		parser.parse(query, queryString);
		
		
		//query = QueryFactory.create("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Construct { ?s a ft:LabeledThing . } { ?s <"  + RDFS.label + "> ?x }", Syntax.syntaxSPARQL_11))) 
	}
	
	public ConstructViewDefinition(String name, Query query) {
		this.name = name;
		this.query = query;
		//this.queryString = queryString;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}*/
	
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}

	@Override
	public String toString() {
		return "ConstructViewDefinition [name=" + name + ", queryString="
				+ query + "]";
	}

}
