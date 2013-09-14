package org.aksw.sparqlify.core.transformations;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.trash.ExprCopy;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.expr.Expr;

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