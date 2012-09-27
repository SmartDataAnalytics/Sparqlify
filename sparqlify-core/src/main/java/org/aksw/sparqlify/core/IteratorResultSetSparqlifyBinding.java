package org.aksw.sparqlify.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.sparqlify.algebra.sql.nodes.VarDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.ExprUtils;


public class IteratorResultSetSparqlifyBinding
	extends SinglePrefetchIterator<Binding>
{
	private static final Logger logger = LoggerFactory.getLogger(IteratorResultSetSparqlifyBinding.class);
	
	private ResultSet rs;
	//private NodeExprSubstitutor substitutor;// = new NodeExprSubstitutor(sparqlVarMap);
	private Multimap<Var, VarDef> sparqlVarMap;
	
	
	public IteratorResultSetSparqlifyBinding(ResultSet rs, Multimap<Var, VarDef> sparqlVarMap)
	{
		this.rs = rs;
		this.sparqlVarMap = sparqlVarMap;
	}
	

	@Override
	protected Binding prefetch() throws Exception {
		if(!rs.next()) {
			return super.finish();
		}

		ResultSetMetaData meta = rs.getMetaData();

		// OPTIMIZE refactor these to attributes
		//NodeExprSubstitutor substitutor = new NodeExprSubstitutor(sparqlVarMap);
		BindingMap binding = new BindingHashMap();

		
		/*
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			binding.add(Var.alloc("" + i), node)
		}*/	
		
		// Substitute the variables in the expressions
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			String colName = meta.getColumnName(i);
			Object colValue = rs.getObject(i);

			NodeValue nodeValue = MakeNodeValue.makeNodeValue(colValue);
			if(nodeValue == null) {
				continue;
			}
			
			Node node = nodeValue.asNode();
			
			
			// FIXME We also add bindings that enable us to reference the columns by their index
			// However, we indexes and column-names are in the same namespace here, so there might be clashes
			Var indexVar = Var.alloc("" + i);
			binding.add(indexVar, node);
			
			Var colVar = Var.alloc(colName);
			if(!binding.contains(colVar)) {
				binding.add(colVar, node);
			}
		}
		
		
		boolean debugMode = true;
		
		BindingMap result = new BindingHashMap();
		
		for(Entry<Var, Collection<VarDef>> entry : sparqlVarMap.asMap().entrySet()) {
			
			//RDFNode rdfNode = null;
			NodeValue value = null;
			//Node value = Node.NULL;
			
			// We distinguish on how to create a varible by the columns that are used
			// We use the most specific rdfTerm constructor
			Set<Var> usedVars = new HashSet<Var>();
			for(VarDef def : entry.getValue()) {
	
				Expr expr = def.getExpr();
				
				
				// Check if all variables are bound
				// Null columns may appear on left joins
				boolean allBound = true;
				for(Var var : expr.getVarsMentioned()) {
					if(!binding.contains(var)) {
						allBound = false;
						continue;
					}
				}
				
				if(allBound) {
					if(value != null) {
						// If the new rdfTerm constructor makes use of only a subset of the columns
						// from which the current node was created, we ignore the new bindings
						if(usedVars.containsAll(expr.getVarsMentioned())) {
							continue;
						} else if(usedVars.equals(expr.getVarsMentioned())) {
							throw new RuntimeException("Multiple expressions binding the variable (ambiguity) " + entry.getKey() + ": " + entry.getValue());							
						} else if(!expr.getVarsMentioned().containsAll(usedVars)) {
							throw new RuntimeException("Multiple expressions binding the variable (overlap) " + entry.getKey() + ": " + entry.getValue());
						}
					}
					
				
					expr = MakeExprPermissive.getInstance().deepCopy(expr);
					
					value = ExprUtils.eval(expr, binding);
					//rdfNode = ModelUtils.convertGraphNodeToRDFNode(value.asNode(), null);

					if(!debugMode) {
						break;
					}
				}
			}

			//qs.add(entry.getKey().getName(), rdfNode);
			// TODO Add a switch for this warning/debugging message (also decide on the logging level)
			Node resultValue = value == null ? null : value.asNode();
			
			if(resultValue == null) {
				logger.trace("Null node for variable " + entry.getKey() + " - Might be undesired.");
				//throw new RuntimeException("Null node for variable " + entry.getKey() + " - Should not happen.");
			} else {
			
				result.add((Var)entry.getKey(), resultValue);
			}
		}

		return result;
	}
	
	
	public void close()
	{
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}