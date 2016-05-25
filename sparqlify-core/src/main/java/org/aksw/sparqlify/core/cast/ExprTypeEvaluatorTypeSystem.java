package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.type_system.MethodSignature;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;

public class ExprTypeEvaluatorTypeSystem
	implements ExprTypeEvaluator
{
	private TypeSystem typeSystem;

	public ExprTypeEvaluatorTypeSystem(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}
	
	@Override
	public TypeToken evaluateType(Expr expr) {
		
		TypeToken result;
		
		if(expr.isFunction()) {
			ExprFunction fn = expr.getFunction();
			String fnId = org.aksw.sparqlify.expr.util.ExprUtils.getFunctionId(fn);
			
			SparqlFunction f = typeSystem.getSparqlFunction(fnId);
			MethodSignature<TypeToken> signature = f.getSignature();
			result = signature.getReturnType();

		} else if(expr.isVariable()) {
			// TODO: If we have a column reference, we can indeed get a datatype.
			throw new RuntimeException("Cannot obtain datatype of a variable");

		} else if(expr.isConstant()) {
			NodeValue nodeValue = expr.getConstant();
			Node node = nodeValue.getNode();
			result = TypeToken.alloc(node.getLiteralDatatypeURI());
		} else {
			result = null;
		}
		
		return result;
	}
}