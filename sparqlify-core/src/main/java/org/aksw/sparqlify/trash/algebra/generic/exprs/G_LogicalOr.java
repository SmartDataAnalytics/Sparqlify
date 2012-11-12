package org.aksw.sparqlify.trash.algebra.generic.exprs;

import java.util.List;

public class G_LogicalOr<T> extends G_Expr2<T> {
	public G_LogicalOr(G_Expr<T> left, G_Expr<T> right) {
		super(left, right);
	}

	@Override
	protected G_LogicalOr<T> _copy(List<G_Expr<T>> args) {
		return new G_LogicalOr<T>(args.get(0), args.get(1));
	}
}
