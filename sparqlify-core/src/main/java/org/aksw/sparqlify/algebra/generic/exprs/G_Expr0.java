package org.aksw.sparqlify.algebra.generic.exprs;

import java.util.ArrayList;
import java.util.List;

public abstract class G_Expr0<T>
	implements G_Expr<T>
{	
	public G_Expr0() {
		super();
	}

	public G_Expr<T> copy(List<G_Expr<T>> args) {
		if(args.size() != 0) {
			throw new IllegalArgumentException("0 argument expected but got " + args.size() + ": " + args);
		}
		
		return _copy(args);
	}

	protected abstract G_Expr<T> _copy(List<G_Expr<T>> args);
	
	@Override
	public int getArgCount() {
		return 1;
	}
	
	@Override
	public G_Expr<T> getArg(int index) {
		throw new IndexOutOfBoundsException("The method must not be called because there set of valid indexes is empty, got: " + index);
	}
	
	
	@Override
	public List<G_Expr<T>> getArgs() {
		List<G_Expr<T>> result = new ArrayList<G_Expr<T>>();
		return result;
	}
}
