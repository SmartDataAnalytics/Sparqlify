package org.aksw.sparqlify.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.sparqlify.algebra.sql.nodes.TermDef;
import org.aksw.sparqlify.restriction.Restriction;

import sparql.TwoWayBinding;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class RdfViewInstance {
	private Quad queryQuad; // The quad in the query this view was originally instanced for
	private Quad viewQuad;  // The quad in the parent view that originally matched
	
	private QuadPattern queryQuads = new QuadPattern(); // The quad pattern that the view now answeres
	private QuadPattern viewQuads  = new QuadPattern(); // The quad pattern that the view now answeres
	
	
	private int instanceId; // id of the view instance (instance for a quad)
	
	private int subId; // sub-id of the instance (in the process the "same" quad) TODO Not sure what i mean here 
	
	private RdfView instance; // The substituted view
	
	private BiMap<Node, Node> renamer; // The substitution that was used
	private RdfView parent; // The view this instance was created from
	
	private TwoWayBinding binding; // The binding from the variables of the view (actually one of the quads)
	                               // to the quad of the query
	
	private ConstraintContainer constraints;
	
	public RdfViewInstance copy() {
		return new RdfViewInstance(queryQuad, viewQuad, instanceId, subId, parent, binding.copySubstitute(null));
	}
	
	public RdfViewInstance(Quad queryQuad, Quad viewQuad, int instanceId, int subId, RdfView parent, TwoWayBinding binding)
	{
		super();
		this.queryQuad = queryQuad;
		this.viewQuad = viewQuad;
		
		queryQuads.add(queryQuad);
		viewQuads.add(viewQuad);
		
		this.instanceId = instanceId;
		this.subId = subId;
		this.parent = parent;

		this.renamer = RdfViewSystemOld.createVariableMappingInstance(parent, instanceId);
		
		// TODO ugly copying
		this.binding = binding.copySubstitute(renamer);			
		this.instance = parent.copySubstitute(renamer);
		
		this.constraints = parent.getConstraints().copySubstitute(renamer);
	}
	

	/**
	 * For a view variable, return its defining SQL expression.
	 * 
	 * @param var
	 * @return
	 */
	public Expr getDefiningExpr(Var var) {
		Node parentName = renamer.inverse().get(var);
		return parent.getBinding().get(parentName);
	}
	
	/**
	 * For a query variable, get all defining expressions,
	 * taking equivalences into account
	 * 
	 * The question is, whether it makes sense to 
	 * also treat constants as defining expressions.
	 * 
	 * I guess yes, since a view quad such as
	 * ?s rdf:type ?o
	 * could be rewritten as
	 * ?s ?p ?o . with ?p = rdf:type
	 * 
	 * 
	 * @param var
	 * @return
	 */
	public List<Expr> getInferredDefiningExprs(Var var) {
		List<Expr> result = new ArrayList<Expr>();
		
		for(Var e : binding.getEquiMap().getEquivalences(var, true)) {
			Expr definingExpr = getDefiningExpr(e);
			if(definingExpr != null) {
				result.add(definingExpr);
			}
		}
		
		Node constant = binding.getEquiMap().getKeyToValue().get(var);
		if(constant != null) {
			result.add(new E_Equals(new ExprVar(var), NodeValue.makeNode(constant)));
		}
		
		return result;
	}
	
	
	public boolean isViewVariable(Var var) {
		return var.getName().startsWith("view");
	}
	
	
	/**
	 * Returns a map from query level to the parent, so the instance level
	 * is omitted.
	 * 
	 * 
	 * @return
	 */
	public SetMultimap<Var, Var> getQueryToParentBinding()
	{			
		SetMultimap<Var, Var> result = HashMultimap.create();
		for(Entry<Var, Var> entry : binding.getEquiMap().getEquivalences().entries()) {
			Var back = (Var)renamer.inverse().get(entry.getValue());
			if(back == null) {
				continue;
			}
			
			result.put(entry.getKey(), back);
		}
		
		return result;
	}

	/**
	 * How the query variables map to the parent view
	 * Given a query with var a, and a virtual graph with ?s:
	 * 
	 * e.g. {?view1_s=[?a]}
	 * 
	 * 
	 * @return
	 */
	public SetMultimap<Var, Var> getParentToQueryBinding()
	{			
		SetMultimap<Var, Var> result = HashMultimap.create();
		for(Entry<Var, Var> entry : binding.getEquiMap().getEquivalences().entries()) {
			Var back = (Var)renamer.inverse().get(entry.getValue());
			if(back == null) {
				continue;
			}
			
			result.put(back, entry.getKey());
		}
		
		return result;
	}
	
	
	public QuadPattern getQueryQuads()
	{
		return queryQuads;
	}

	public QuadPattern getViewQuads()
	{
		return viewQuads;
	}

	public Quad getQueryQuad()
	{
		return queryQuad;
	}
	public Quad getViewQuad()
	{
		return queryQuad;
	}
	public int getInstanceId()
	{
		return instanceId;
	}
	public RdfView getInstance()
	{
		return instance;
	}
	public RdfView getParent()
	{
		return parent;
	}
	public BiMap<Node, Node> getRenamer()
	{
		return renamer;
	}
	public TwoWayBinding getBinding()
	{
		return binding;
	}
	

	public Multimap<Var, TermDef> getSqlBinding()
	{
		Multimap<Var, TermDef> result = HashMultimap.create();
		Map<Node, Expr> parentBinding = parent.getBinding();
		for(Entry<Node, Expr> entry : parentBinding.entrySet()) {
			Var node = (Var)renamer.get(entry.getKey());

			Restriction r = parent.getRestrictions().getRestriction((Var)entry.getKey());

			IBiSetMultimap<Var, Var> reverse = binding.getEquiMap().getEquivalences().getInverse();
			Set<Var> queryVars = reverse.get(node);
		
			for(Var queryVar : queryVars) {

				result.put(queryVar, new TermDef(entry.getValue(), r));
			}
		}
		
		return result;
	}

	/*
	public Multimap<Var, Expr> getSqlBinding()
	{
		Multimap<Var, Expr> result = HashMultimap.create();
		Map<Node, Expr> parentBinding = parent.getBinding();
		for(Entry<Node, Expr> entry : parentBinding.entrySet()) {
			Node node = renamer.get(entry.getKey());
			
			IBiSetMultimap<Var, Var> reverse = binding.getEquiMap().getEquivalences().getInverse();
			Set<Var> queryVars = reverse.get(node);
		
			for(Var queryVar : queryVars) {
				result.put(queryVar, entry.getValue());
			}
		}
		
		return result;
	}
	*/
	
	
	@Override
	public String toString()
	{
		//return "ViewBinding\n\tview=" + instance +"\n\tquad=" + queryQuad + "\n\tbinding=" + binding;
		return parent.getName() + " " + queryQuad + " " + binding;
		//return "ViewBinding\n\t" + instance +"\n\t" + queryQuad + "\n\t" + binding;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + instanceId;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + subId;
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
		RdfViewInstance other = (RdfViewInstance) obj;
		if (instanceId != other.instanceId)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (subId != other.subId)
			return false;
		return true;
	}	
}


