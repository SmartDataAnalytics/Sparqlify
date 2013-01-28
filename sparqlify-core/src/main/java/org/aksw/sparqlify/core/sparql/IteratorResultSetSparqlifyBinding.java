package org.aksw.sparqlify.core.sparql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.sparqlify.core.MakeExprPermissive;
import org.aksw.sparqlify.core.MakeNodeValue;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.openjena.riot.pipeline.normalize.CanonicalizeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.datatypes.RDFDatatype;
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
	
	// Canonicalize values, e.g. 20.0 -> 2.0e1
	private CanonicalizeLiteral canonicalizer = CanonicalizeLiteral.get();
	
	private ResultSet rs;
	//private NodeExprSubstitutor substitutor;// = new NodeExprSubstitutor(sparqlVarMap);
	private Multimap<Var, RestrictedExpr> sparqlVarMap;
	
	
	public IteratorResultSetSparqlifyBinding(ResultSet rs, Multimap<Var, RestrictedExpr> sparqlVarMap)
	{
		this.rs = rs;
		this.sparqlVarMap = sparqlVarMap;
		
//		ResultSetMetaData meta;
//		try {
//			rs.next();
//			System.out.println("h__4: " + rs.getObject("h__4"));
//			System.out.println("AGE: " + rs.getObject("AGE"));
//			
//			
//			meta = rs.getMetaData();
//			for(int i = 1; i <= meta.getColumnCount(); ++i) {
//				String colName = meta.getColumnName(i);
//				
//				System.out.println("Column [" + i + "]: " + colName);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

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
			String colName = meta.getColumnLabel(i);
			Object colValue = rs.getObject(i);

			NodeValue nodeValue = MakeNodeValue.makeNodeValue(colValue);
			if(nodeValue == null) {
				continue;
			}
			
			Node node = nodeValue.asNode();
			
			
			// FIXME We also add bindings that enable us to reference the columns by their index
			// However, indexes and column-names are in the same namespace here, so there might be clashes
			Var indexVar = Var.alloc("" + i);
			binding.add(indexVar, node);
			
			Var colVar = Var.alloc(colName);
			if(!binding.contains(colVar)) {
				binding.add(colVar, node);
			}
		}
		
		
		boolean debugMode = true;
		
		BindingMap result = new BindingHashMap();
		
		for(Entry<Var, Collection<RestrictedExpr>> entry : sparqlVarMap.asMap().entrySet()) {
			
			Var bindingVar = entry.getKey();
			Collection<RestrictedExpr> candidateExprs = entry.getValue();
			
			if(bindingVar.getName().equals("o")) {
				System.out.println("BindingVar o ");
			}
			
			//RDFNode rdfNode = null;
			NodeValue value = null;
			//Node value = Node.NULL;
			
			// We distinguish on how to create a varible by the columns that are used
			// We use the most specific rdfTerm constructor
			Set<Var> usedVars = new HashSet<Var>();
			for(RestrictedExpr def : candidateExprs) {
	
				Expr expr = def.getExpr();
				
				
				// Check if all variables are bound
				// Null columns may appear on left joins
				boolean allBound = true;
				Set<Var> exprVars = expr.getVarsMentioned();
				for(Var var : exprVars) {
					if(!binding.contains(var)) {
						allBound = false;
						break;
					}
				}
				
				if(allBound) {
					if(value != null) {
						// If the new rdfTerm constructor makes use of only a subset of the columns
						// from which the current node was created, we ignore the new bindings
						if(usedVars.containsAll(expr.getVarsMentioned())) {
							continue;
						} else if(usedVars.equals(expr.getVarsMentioned())) {
							throw new RuntimeException("Multiple expressions binding the variable (ambiguity) " + bindingVar + ": " + entry.getValue());							
						} else if(!expr.getVarsMentioned().containsAll(usedVars)) {
							throw new RuntimeException("Multiple expressions binding the variable (overlap) " + bindingVar + ": " + entry.getValue());
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
				logger.trace("Null node for variable " + bindingVar + " - Might be undesired.");
				//throw new RuntimeException("Null node for variable " + entry.getKey() + " - Should not happen.");
			} else {
			
				//result.add((Var)entry.getKey(), resultValue);

				boolean isDatatypeCanonicalization = false;
				
				Node canonResultValue = canonicalizer.convert(resultValue);
				//System.out.println("Canonicalization: " + resultValue + " -> " + canonResultValue);
				if(!isDatatypeCanonicalization) {
					
					if(canonResultValue.isLiteral()) {
						String lex = canonResultValue.getLiteralLexicalForm();
						
						if(resultValue.isLiteral()) {
							RDFDatatype originalType = resultValue.getLiteralDatatype();
							//String typeUri = resultValue.getLiteralDatatypeURI();
							canonResultValue = Node.createLiteral(lex, originalType);
						} else {
							throw new RuntimeException("Should not happen: Non-literal canonicalized to literal: " + resultValue + " became " + canonResultValue);
						}

						
					}
					
				}
				
				result.add(bindingVar, canonResultValue);
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