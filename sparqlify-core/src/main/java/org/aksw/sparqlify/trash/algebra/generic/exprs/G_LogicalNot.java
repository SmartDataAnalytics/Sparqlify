package org.aksw.sparqlify.trash.algebra.generic.exprs;

import java.util.List;

public class G_LogicalNot<T> extends G_Expr1<T> {
	public G_LogicalNot(G_Expr<T> arg) {
		super(arg);
	}

	@Override
	protected G_LogicalNot<T> _copy(List<G_Expr<T>> args) {
		return new G_LogicalNot<T>(args.get(0));
	}
}
