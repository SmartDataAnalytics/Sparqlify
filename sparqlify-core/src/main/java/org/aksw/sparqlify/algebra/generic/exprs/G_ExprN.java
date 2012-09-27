package org.aksw.sparqlify.algebra.generic.exprs;

import java.util.List;

public abstract class G_ExprN<T>
	implements G_Expr<T>
{
	private List<G_Expr<T>> args;
	
	public G_ExprN(List<G_Expr<T>> args) {
		super();
		this.args = args;
	}
	
	@Override
	public int getArgCount() {
		return args.size();
	}
	
	@Override
	public G_Expr<T> getArg(int index) {
		return args.get(index);
/*
		switch(index) {
		case 0: return left;
		case 1: return right;
		default: throw new IndexOutOfBoundsException("Valid range: [0..1], got: " + index);
		}
*/
	}
	
	
	@Override
	public List<G_Expr<T>> getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return "G_ExprN [args=" + args + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		G_ExprN<?> other = (G_ExprN<?>) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		return true;
	} 
}
