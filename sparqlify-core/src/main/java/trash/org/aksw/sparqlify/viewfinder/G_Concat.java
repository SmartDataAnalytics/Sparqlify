package trash.org.aksw.sparqlify.viewfinder;

import java.util.List;

public class G_Concat<T>
	extends G_ExprN<T>
{
	public G_Concat(List<G_Expr<T>> args) {
		super(args);
	}

	@Override
	public G_Expr<T> copy(List<G_Expr<T>> args) {
		return new G_Concat<T>(args);
	}
}
