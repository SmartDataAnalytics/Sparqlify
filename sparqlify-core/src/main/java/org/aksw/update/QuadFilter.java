package org.aksw.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.QuadUtils;

import sparql.FilterUtils;
import sparql.ValueSet;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.ExprUtils;


/**
 * A quad with meta information.
 * 
 * TODO do we somehow need to keep track of the scope here?
 * 
 * 
 * @author raven
 *
 */
public class QuadFilter
{
	private Set<Set<Expr>> filterDnf;
	private Quad pattern;

	private Map<Var, ValueSet<NodeValue>> varToValueSet; 

	public Set<Var> getVarsMentioned()
	{
		return QuadUtils.getVarsMentioned(pattern);
	}
	
	public QuadFilter(Quad pattern, Set<Set<Expr>> filterDnf)
	{
		//this.pattern = Utils.quadToList(pattern);
		this.pattern = pattern;
		this.filterDnf = filterDnf;
		
		this.varToValueSet = FilterUtils.extractValueConstraintsDnf(filterDnf);
		
		System.out.println("Extracted contstraints: " + varToValueSet);
	}
	
	public QuadFilter(Quad pattern, List<ExprList> filter)
	{
		//this.pattern = Utils.quadToList(pattern);
		this.pattern = pattern;
		this.filterDnf = FilterUtils.toSets(filter);
		
		this.varToValueSet = FilterUtils.extractValueConstraintsDnf(filterDnf);

		System.out.println("Extracted contstraints: " + varToValueSet);
	}
	
	

	public Quad getPattern()
	{
		return pattern;
	}



	public boolean doesAccept(Quad quad) {
		
		/**
		 * If the pattern is ?a ?a ?b
		 * then a b c will not match (since ?a would have to be mapped to a and b)
		 * 
		 */
		if(null == QuadUtils.getVarMapping(pattern, quad)) {
			return false;
		}

		// If there is no filter, then we accept.
		// Note an empty Dnf-clause equals false
		if(filterDnf == null) {
			return true;
		}
		
		
		List<Node> p = QuadUtils.quadToList(pattern); 
		List<Node> nodes = QuadUtils.quadToList(quad); 
		
		
		// At least one of the filter's clauses must yield true
		for(Set<Expr> clause : filterDnf) {

			boolean and = true;
			for(Expr expr : clause) {
			
				BindingMap binding = new BindingHashMap();

				
				for(int i = 0; i < 4; ++i) {
					Node n = p.get(i);
					if(n.isVariable()) {
						binding.add((Var)n, nodes.get(i));
					}
				}
				
				and = and && ExprUtils.eval(expr, binding).getBoolean();
				if(and == false) {
					break;
				}
			}
			
			if(and == true) {
				return true;
			}
		}
		
		return false;
	}

}