package org.aksw.sparqlify.trash.algebra.generic.exprs;

import java.util.ArrayList;
import java.util.List;

public abstract class G_Expr2<T>
	implements G_Expr<T>
{
	private G_Expr<T> left;
	private G_Expr<T> right;
	
	public G_Expr2(G_Expr<T> left, G_Expr<T> right) {
		super();
		this.left = left;
		this.right = right;
	}

	
	public G_Expr<T> getLeft() {
		return left;
	}

	public G_Expr<T> getRight() {
		return right;
	}
	
	@Override
	public String toString() {
		return "Expr2 [left=" + left + ", right=" + right + "]";
	}
	
	
	public G_Expr<T> copy(List<G_Expr<T>> args) {
		if(args.size() != 2) {
			throw new IllegalArgumentException("2 arguments expected but got " + args.size() + ": " + args);
		}
		
		return _copy(args);
	}
	
	@Override
	public int getArgCount() {
		return 2;
	}
	
	@Override
	public G_Expr<T> getArg(int index) {
		switch(index) {
		case 0: return left;
		case 1: return right;
		default: throw new IndexOutOfBoundsException("Valid range: [0..1], got: " + index);
		}
	}
	
	
	@Override
	public List<G_Expr<T>> getArgs() {
		List<G_Expr<T>> result = new ArrayList<G_Expr<T>>();
		result.add(left);
		result.add(right);
		return result;
	} 


	protected abstract G_Expr<T> _copy(List<G_Expr<T>> args);
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		G_Expr2<?> other = (G_Expr2<?>) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

}
