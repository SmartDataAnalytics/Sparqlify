package org.aksw.sparqlify.core.rewrite.expr.transform;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.ExprCopy;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.Expr;

public class ExprTransformerFunction
	implements ExprTransformer
{
	private Resource resultType;
	
	public ExprTransformerFunction(Resource resultType) {
		this.resultType = resultType;
	}
	
	@Override
	public E_RdfTerm transform(Expr fn, List<E_RdfTerm> exprs) {

		List<Expr> tmp = new ArrayList<Expr>(exprs.size());
		for(E_RdfTerm expr : exprs) {
			tmp.add(expr.getLexicalValue());
		}
		
		Expr newVal = ExprCopy.getInstance().copy(fn, tmp);
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(newVal, resultType);
		

		return result;
	}
}