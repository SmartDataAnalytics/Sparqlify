package org.aksw.sparqlify.algebra.sparql.transform;


import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;
import org.aksw.sparqlify.trash.ExprCopy;
import org.apache.commons.lang.NotImplementedException;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

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
