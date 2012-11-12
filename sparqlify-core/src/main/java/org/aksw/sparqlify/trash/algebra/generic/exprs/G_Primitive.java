package org.aksw.sparqlify.trash.algebra.generic.exprs;

import java.util.List;

public class G_Primitive<T>
	extends G_Expr0<T>
{
	private T value;
	
	public G_Primitive(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}

	@Override
	protected G_Primitive<T> _copy(List<G_Expr<T>> args) {
		return new G_Primitive<T>(value);
	}

	public static <T> G_Primitive<T> create(T value) {
		return new G_Primitive<T>(value);
	}
}
