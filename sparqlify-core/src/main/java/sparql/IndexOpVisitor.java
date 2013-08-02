package sparql;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.algebra.OpVisitorByType;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.Op2;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpN;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.Quad;


public class IndexOpVisitor
	extends OpVisitorByType
{
	public List<Quad> quads = new ArrayList<Quad>();

	@Override
	protected void visitN(OpN op)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void visit2(Op2 op)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void visit1(Op1 op)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void visit0(Op0 op)
	{
		if(op instanceof OpQuadPattern) {
			quads.addAll(((OpQuadPattern)op).getPattern().getList());
		}
	}

	@Override
	protected void visitExt(OpExt op)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void visitFilter(OpFilter op) {
		// TODO Auto-generated method stub
		
	}

}