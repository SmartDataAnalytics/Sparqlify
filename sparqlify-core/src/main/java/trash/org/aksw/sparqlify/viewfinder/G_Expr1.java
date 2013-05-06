package trash.org.aksw.sparqlify.viewfinder;

import java.util.ArrayList;
import java.util.List;

public abstract class G_Expr1<T>
	implements G_Expr<T>
{
	private G_Expr<T> arg;
	
	public G_Expr1(G_Expr<T> arg) {
		super();
		this.arg = arg;
	}

	
	public G_Expr<T> getArg() {
		return arg;
	}
	
	@Override
	public String toString() {
		return "G_Expr1 [arg=" + arg + "]";
	}


	public G_Expr<T> copy(List<G_Expr<T>> args) {
		if(args.size() != 1) {
			throw new IllegalArgumentException("1 argument expected but got " + args.size() + ": " + args);
		}
		
		return _copy(args);
	}
	
	@Override
	public int getArgCount() {
		return 1;
	}
	
	@Override
	public G_Expr<T> getArg(int index) {
		switch(index) {
		case 0: return arg;
		default: throw new IndexOutOfBoundsException("Valid range: [0..1], got: " + index);
		}
	}
	
	
	@Override
	public List<G_Expr<T>> getArgs() {
		List<G_Expr<T>> result = new ArrayList<G_Expr<T>>();
		result.add(arg);
		return result;
	} 


	protected abstract G_Expr<T> _copy(List<G_Expr<T>> args);


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg == null) ? 0 : arg.hashCode());
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
		G_Expr1<?> other = (G_Expr1<?>) obj;
		if (arg == null) {
			if (other.arg != null)
				return false;
		} else if (!arg.equals(other.arg))
			return false;
		return true;
	}
}
