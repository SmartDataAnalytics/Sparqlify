package org.aksw.sparqlify.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.FilterUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.ValueSetOld;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;


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

	private Map<Var, ValueSetOld<NodeValue>> varToValueSet; 

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