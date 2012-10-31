package org.aksw.sparqlify.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mapping.RenamerNodes;

import org.aksw.sparqlify.algebra.sparql.transform.SparqlSubstitute;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;
import org.aksw.sparqlify.algebra.sql.nodes.SqlQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.config.lang.Constraint;
import org.aksw.sparqlify.config.syntax.QueryString;
import org.aksw.sparqlify.config.syntax.Relation;
import org.aksw.sparqlify.config.syntax.RelationRef;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.aksw.sparqlify.sparqlview.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparql.FilterUtils;
import sparql.PatternUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;



/**
 * An RdfView definition.
 * 
 * Essentially it corresponds to the following pseudo-sparql query:
 * 
 * Contruct {
 * 	?s a Person . // quadPattern
 *  ?s ?p ?o .
 *  
 *  Filter(?p In (...set of uris)) . // filter which both describes and contrains the underlying sql data
 *                                   // Ideally this filter is both pushed down to an sql where clause and
 *                                   // helps rejecting view candidates for queries with non-compatible filters
 * }
 * Binding {
 *   ?s := Uri(Concat('prefix', id)
 *   ?p := Uri(pred)
 *   ?o := Literal(value, dataType) 
 * }
 * Sql {
 *   Select id, pred, value, 'xsd:int' as dataType From ...
 * }
 * 
 * 
 * 
 * @author raven
 *
 */
public class RdfView
	implements View
	//extends RdfViewTemplate
{
	private static final Logger logger = LoggerFactory.getLogger(RdfView.class);
	
	private String name;
	
	private RdfViewTemplate template;
	//private QuadPattern quadPattern;
	private ExprList filter;
	
	
	// FIXME Maybe the list of constraints should go into the constraint container
	//private List<Constraint> rawConstraints;

	private ConstraintContainer constraints;
	
	// This field is intended to be set by the RdfViewSystem
	private RestrictionManagerImpl restrictions;
	
	public RestrictionManagerImpl getRestrictions() {
		return restrictions;
	}
	
	public void setRestrictions(RestrictionManagerImpl restrictions) {
		this.restrictions = restrictions;
	}
	
	
	//private Map<Node, Expr> binding;
	private SqlNodeOld sqlNode;	

	private Map<String, SqlDatatype> columnToDatatype = new HashMap<String, SqlDatatype>();

	
	private Map<Var, RdfTermPattern> varToPattern = new HashMap<Var, RdfTermPattern>();
	/*
	// A map for hinting the regex pattern that matches all the values of a column.
	private Map<String, String> columnToPattern = new HashMap<String, String>();

	
	public Map<String, String> getColumnToPattern() {
		return columnToPattern;
	}
	*/
	
	public Map<Var, RdfTermPattern> getVarToPattern() {
		return varToPattern;
	}
	
	public Set<Var> getVarsMentioned()
	{
		return template.getVarsMentioned();
	}

	
	public Map<String, SqlDatatype> getColumnToDatatype() {
		return columnToDatatype;
	}
	
	
	
	public RdfView copySubstitute(Map<Node, Node> map)
	{
		ExprList tmpFilter = new ExprList();
		
		NodeTransform rename = new RenamerNodes(map);
		for(Expr expr : filter) {
			tmpFilter.add(expr.applyNodeTransform(rename));
		}
		
		
		/*
		BindingMap tmp = new BindingMap();
		for(Entry<Node, Node> entry : map.entrySet()) {
			tmp.add((Var)entry.getKey(), entry.getValue());
		}*/
		
		
		RdfView result = new RdfView(name, template.copySubstitute(map),
				tmpFilter,
				constraints.copySubstitute(map),
				sqlNode);
		
		return result;
	}
	
	public String getName() 
	{
		return name;
	}
	
	/*
	public RdfView getParent()
	{
		return parent;
	}*/
	
	/**
	 * Constructs a view definition
	 * 
	 * syntax: [From graph] { ?s ?p ?o . Filter(?o = ...) . } with ?s = expr; ?p = ; select ...;
	 * 
	 * Currently this is just some string hack
	 * 
	 * 
	 * @param pattern
	 */
	public static RdfView create(String str) {
		Map<String, String> defaultPrefixes = new HashMap<String, String>();
		defaultPrefixes.put("bif", "http://bif/");
		defaultPrefixes.put("rdf", RDF.getURI());
		defaultPrefixes.put("rdfs", RDFS.getURI());
		defaultPrefixes.put("geo", "http://ex.org/");
		defaultPrefixes.put("xsd", XSD.getURI());
		//defaultPrefixes.put("beef", "http://aksw.org/beef/");
		defaultPrefixes.put("spy", "http://aksw.org/sparqlify/");
		
		defaultPrefixes.put("wso", "http://aksw.org/wortschatz/ontology/");
		//defaultPrefixes.put("beef", "http://aksw.org/beef/");
		defaultPrefixes.put("rdf", RDF.getURI());
		defaultPrefixes.put("owl", OWL.getURI());
		
		return create("unnamed", str, defaultPrefixes);
	}

	public static RdfView create(ViewDefinition definition) {
		return create(
				definition.getName(),
				definition.getViewTemplateDefinition().getConstructTemplate(),
				definition.getFilters(),
				definition.getViewTemplateDefinition().getVarBindings(),
				definition.getConstraints(),
				definition.getRelation()
				);
	}
	
	/**
	 * Meh.... Didn't notice that template does not have support for graphs.
	 * Therefore need to change that...
	 * 
	 * @param template
	 * @param filters
	 * @param bindings
	 * @param relation
	 * @return
	 */
	public static RdfView create(String name, Template template, ExprList filters, List<Expr> bindings, List<Constraint> rawConstraints, Relation relation)
	{
		if(bindings == null) {
			bindings = new ArrayList<Expr>();
		}
		
		QuadPattern quadPattern = new QuadPattern();

		for(Triple triple : template.getTriples()) {
			quadPattern.add(new Quad(Quad.defaultGraphNodeGenerated, triple));
		}
		
		Map<Node, Expr> bindingMap = new HashMap<Node, Expr>();
		
		for(Expr expr : bindings) {
			if(!(expr instanceof E_Equals)) {
				throw new RuntimeException("Binding expr must have form ?var = ... --- instead got: " + expr);
			}
			
			// Do macro expansion
			// TODO Keep track of a non-macro-expanded version for human readability
			// and easier debugging
			
			Expr definition = expr.getFunction().getArg(2);
			definition = SparqlSubstitute.substituteExpr(definition);
			
			Var var = expr.getFunction().getArg(1).asVar();
			bindingMap.put(var, definition);
		}
		
		
		//System.out.println("Binding = " + bindingMap);

		if(rawConstraints == null) {
			rawConstraints = Collections.emptyList();
		}
		ConstraintContainer constraints = new ConstraintContainer(rawConstraints);
		

		//logger.warn("Sanity checking of given patterns against derived ones and vice versa not implemented yet.");
		// Derive regex patterns for all expressions
		for(Entry<Node, Expr> entry : bindingMap.entrySet()) {

			// If a constraint was given, skip deriving one.

			if(!constraints.getVarPatternConstraints().containsKey(entry.getKey())) { //.getPattern((Var)entry.getKey());
				RdfTermPattern derivedPattern = RdfTermPatternDerivation.deriveRegex(entry.getValue());
				
				constraints.getVarPatternConstraints().put((Var)entry.getKey(), derivedPattern);
			}
		}

		
		// TODO Make this extensible
		SqlNodeOld sqlNode;
		if(relation == null) { 
			logger.warn("No relation given for view '" + name + "', using Select 1");
			sqlNode = new SqlQuery(null, "SELECT 1"); //;null;
		} else if(relation instanceof QueryString) {
			sqlNode = new SqlQuery(null, ((QueryString) relation).getQueryString());
		} else if(relation instanceof RelationRef){
			sqlNode = new SqlTable(((RelationRef) relation).getRelationName());
		} else {
			throw new RuntimeException("Unsupported relation type: " + relation);
		}
		
		
		return new RdfView(name, quadPattern, filters, bindingMap, constraints, sqlNode);
	}

	
	public static RdfView create(String name, String str, Map<String, String> defaultPrefixes) {		
		
		PrefixMapping defaultPrefixMapping = new PrefixMappingImpl();
		defaultPrefixMapping.setNsPrefixes(defaultPrefixes);		
		
		String parts1[] = str.split("\\swith\\s", 2);
		//String parts2[] = parts1[1].split("\\sselect\\s", 2);
		String parts2[] = parts1[1].split(";");
		
		String sqlStr = parts2[parts2.length - 1].trim();
		
		String queryStr = "Select * " + parts1[0];
		String bindingStrs[] = Arrays.copyOf(parts2, parts2.length - 1);
		//String sqlStr = "SELECT " + parts2[1];
		
		
		Query query = new Query();
		query.setPrefixMapping(defaultPrefixMapping);
		QueryFactory.parse(query, queryStr, null, Syntax.syntaxSPARQL);
		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);
		
		QuadPattern quadPattern = new QuadPattern();

		for(Quad quad : PatternUtils.collectQuads(op)) {
			quadPattern.add(quad);
		}
		
		//PatternUtils.
		
		
		Map<Node, Expr> binding = new HashMap<Node, Expr>();
		
		for(String bindingStr : bindingStrs) {
			Expr expr = ExprUtils.parse(bindingStr, defaultPrefixMapping);
			
			if(!(expr instanceof E_Equals)) {
				throw new RuntimeException("Binding expr must have form ?var = ... --- instead got: " + bindingStr);
			}
			
			// Do macro expansion
			// TODO Keep track of a non-macro-expanded version for human readability
			// and easier debugging
			
			Expr definition = expr.getFunction().getArg(2);
			definition = SparqlSubstitute.substituteExpr(definition);

			
			
			Var var = expr.getFunction().getArg(1).asVar();
			binding.put(var, definition);
		}
		
		
		//System.out.println("Binding = " + binding);

		SqlNodeOld sqlExpr;
		if(sqlStr.startsWith("select")) {
			sqlExpr = new SqlQuery(null, sqlStr);
		} else {
			sqlExpr = new SqlTable(sqlStr);
		}
		
		ExprList filter = FilterUtils.collectExprs(op, new ExprList());
		return new RdfView(name, quadPattern, filter, binding, new ConstraintContainer(), sqlExpr);
	}


	public RdfView(String name, RdfViewTemplate template, ExprList filter, ConstraintContainer constraints, SqlNodeOld sqlExpr)
	{
		super();
		this.name = name;
		this.template = template;
		this.filter = filter;
		this.constraints = constraints;
		this.sqlNode = sqlExpr;
	}

	public RdfView(String name, QuadPattern quadPattern, ExprList filter, Map<Node, Expr> binding,
			ConstraintContainer constraints, SqlNodeOld sqlExpr)
	{
		super();
		this.name = name;
		this.template = new RdfViewTemplate(quadPattern, binding);
		this.filter = filter;
		this.constraints = constraints;
		this.sqlNode = sqlExpr;
	}

	

	@Deprecated
	public QuadPattern getQuadPattern()
	{
		return template.getQuadPattern();
	}
	
	public ExprList getFilter()
	{
		return filter;
	}

	@Deprecated
	public Map<Node, Expr> getBinding()
	{
		return template.getBinding();
	}
	
	
	public SqlNodeOld getSqlNode()
	{
		return sqlNode;
	}

	public ConstraintContainer getConstraints() {
		return constraints;
	}


	@Override
	public String toString()
	{
		return name;
		//return "RdfView [template=" + template + ", filter=" + filter
		//		+ ", sqlExpr=" + sqlNode + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RdfView other = (RdfView) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
	
}
