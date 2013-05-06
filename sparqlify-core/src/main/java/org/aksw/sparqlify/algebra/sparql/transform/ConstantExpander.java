package org.aksw.sparqlify.algebra.sparql.transform;


import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;
import org.aksw.sparqlify.trash.ExprCopy;
import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class ConstantExpander {

	public static Expr transform(Expr expr) {
		return expr == null ? null : (Expr) MultiMethod.invokeStatic(ConstantExpander.class,
				"_transform", expr);
	}

	public static Expr _transform(ExprFunction expr) {
		ExprList args = transformList(expr.getArgs());

		Expr result = ExprCopy.getInstance().copy(expr, args);
		return result;
	}

	protected static ExprList transformList(Iterable<Expr> exprs) {
		ExprList result = new ExprList();

		for (Expr expr : exprs) {
			result.add(transform(expr));
		}

		return result;
	}

	public static Expr _transform(ExprFunctionOp funcOp, ExprList args, Op opArg) {
		throw new NotImplementedException();
	}

	public static Expr _transform(Expr expr) {
		return expr;
	}

	public static Expr _transform(NodeValue nv)
	{
		return SqlTranslationUtils.expandConstant(nv.asNode());
	}

	public static Expr _transform(ExprAggregator eAgg) {
		throw new NotImplementedException();
	}
}
