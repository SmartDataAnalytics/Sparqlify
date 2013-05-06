package trash.org.aksw.sparqlify.viewfinder;

import java.util.List;

public class G_LogicalAnd<T> extends G_Expr2<T> {
	public G_LogicalAnd(G_Expr<T> left, G_Expr<T> right) {
		super(left, right);
	}

	@Override
	protected G_LogicalAnd<T> _copy(List<G_Expr<T>> args) {
		return new G_LogicalAnd<T>(args.get(0), args.get(1));
	}
}
