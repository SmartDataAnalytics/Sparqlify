package org.aksw.sparqlify.trash.algebra.generic.exprs;

import java.util.List;

public class G_Add<T> extends G_Expr2<T> {
	public G_Add(G_Expr<T> left, G_Expr<T> right) {
		super(left, right);
	}

	@Override
	protected G_Add<T> _copy(List<G_Expr<T>> args) {
		return new G_Add<T>(args.get(0), args.get(1));
	}
	
	public static <T> G_Add<T> create(G_Expr<T> left, G_Expr<T> right) {
		return new G_Add<T>(left, right);
	}
}
