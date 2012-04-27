package org.aksw.sparqlify.sparqlview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mapping.RenamerNodes;

import org.aksw.sparqlify.algebra.sparql.transform.SparqlSubstitute;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNode;
import org.aksw.sparqlify.algebra.sql.nodes.SqlQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.config.lang.Constraint;
import org.aksw.sparqlify.core.RdfViewTemplate;
import org.aksw.sparqlify.restriction.RestrictionManager;
import org.aksw.sparqlify.views.transform.GetVarsMentioned;
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
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;



/**
 * An SparqlView definition.
 * 
 * Essentially a SPARQL construct query with constraints 
 * 
 * 
 * @author raven
 *
 */
public class SparqlView
	implements View
{
	private static final Logger logger = LoggerFactory.getLogger(SparqlView.class);
	
	private String name;
	private RdfViewTemplate template;
	private ExprList constraints;	
	private RestrictionManager restrictions;
	
	
	public RestrictionManager getRestrictions() {
		return restrictions;
	}
	
	public void setRestrictions(RestrictionManager restrictions) {
		this.restrictions = restrictions;
	}
	
	private Op op; // The operation this view corresponds to
	
	
	public Set<Var> getVarsMentioned()
	{
		Set<Var> result = new HashSet<Var>();
		
		result.addAll(template.getVarsMentioned());
		result.addAll(GetVarsMentioned.getVarsMentioned(op));
		
		return result;
	}
	
	
	public SparqlView copySubstitute(Map<Node, Node> map)
	{
		ExprList tmpFilter = new ExprList();
		
		NodeTransform rename = new RenamerNodes(map);
		for(Expr expr : constraints) {
			tmpFilter.add(expr.applyNodeTransform(rename));
		}
				
		
		BindingMap bindingMap = new BindingHashMap();
		for(Entry<Node, Node> entry : map.entrySet()) {
			bindingMap.add((Var)entry.getKey(), entry.getValue());
		}
		
		RenamerNodes renamer = new RenamerNodes(map);
		Op renamedOp = NodeTransformLib.transform(renamer, op);
		
		SparqlView result = new SparqlView(name, template.copySubstitute(map),
				constraints.copySubstitute(bindingMap),
				renamedOp);
		
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
	public static SparqlView create(String str) {
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

	public static SparqlView create(String name, Query query) {
		if(!query.isConstructType()) {
			throw new RuntimeException("Query must be a construct query");
		}

		
		Op tmp = Algebra.compile(query.getQueryPattern());
		Op op = Algebra.toQuadForm(tmp);
		
		QuadPattern quadPattern = new QuadPattern();
		for(Triple triple : query.getConstructTemplate().getTriples()) {
			quadPattern.add(new Quad(Quad.defaultGraphNodeGenerated, triple));	
		} 
		
		SparqlView result = new SparqlView(name, new RdfViewTemplate(quadPattern, new HashMap<Node, Expr>()), new ExprList(), op);
		return result;
	}
	
	/*
	public static SparqlView create(ViewDefinition definition) {
		return create(
				definition.getName(),
				definition.getViewTemplateDefinition().getConstructTemplate(),
				definition.getFilters(),
				definition.getViewTemplateDefinition().getVarBindings(),
				definition.getConstraints(),
				definition.getRelation()
				);
	}*/
	
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
	public static SparqlView create(String name, Template template, ExprList filters, List<Expr> bindings, List<Constraint> rawConstraints, Op op)
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
		ExprList constraints = new ExprList();
		

		//logger.warn("Sanity checking of given patterns against derived ones and vice versa not implemented yet.");
		logger.warn("Derivation of restrictions from expressions currently not implemented");
/*
		// Derive regex patterns for all expressions
		for(Entry<Node, Expr> entry : bindingMap.entrySet()) {

			// If a constraint was given, skip deriving one.

			if(!constraints.getVarPatternConstraints().containsKey(entry.getKey())) { //.getPattern((Var)entry.getKey());
				RdfTermPattern derivedPattern = RdfTermPatternDerivation.deriveRegex(entry.getValue());
				
				constraints.getVarPatternConstraints().put((Var)entry.getKey(), derivedPattern);
			}
		}
*/
		
		return new SparqlView(name, quadPattern, constraints, bindingMap, op);
	}

	
	public static SparqlView create(String name, String str, Map<String, String> defaultPrefixes) {		
		
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

		SqlNode sqlExpr;
		if(sqlStr.startsWith("select")) {
			sqlExpr = new SqlQuery(null, sqlStr);
		} else {
			sqlExpr = new SqlTable(sqlStr);
		}
		
		ExprList constraints = FilterUtils.collectExprs(op, new ExprList());
		return new SparqlView(name, quadPattern, constraints, binding, op);
	}


	public SparqlView(String name, RdfViewTemplate template, ExprList constraints, Op op)
	{
		super();
		this.name = name;
		this.template = template;
		this.constraints = constraints;
		this.op = op;
	}

	public SparqlView(String name, QuadPattern quadPattern, ExprList constraints, Map<Node, Expr> binding,
			Op sqlExpr)
	{
		super();
		this.name = name;
		this.template = new RdfViewTemplate(quadPattern, binding);
		this.constraints = constraints;
	}

	

	@Deprecated
	public QuadPattern getQuadPattern()
	{
		return template.getQuadPattern();
	}

	@Deprecated
	public Map<Node, Expr> getBinding()
	{
		return template.getBinding();
	}
	
	
	public Op getOp()
	{
		return op;
	}

	public ExprList getConstraints() {
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
		SparqlView other = (SparqlView) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
	
}
