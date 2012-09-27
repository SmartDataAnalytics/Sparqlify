package org.aksw.sparqlify.algebra.generic.exprs;

import java.util.List;

public class G_Equals<T>
	extends G_Expr2<T>
{
	public G_Equals(G_Expr<T> left, G_Expr<T> right) {
		super(left, right);
	}

	@Override
	protected G_Equals<T> _copy(List<G_Expr<T>> args) {
		return new G_Equals<T>(args.get(0), args.get(1));
	}
}
