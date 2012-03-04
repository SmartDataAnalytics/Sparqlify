package org.aksw.sparqlify.algebra.sparql.domain;

import java.util.ArrayList;
import java.util.List;


import org.aksw.sparqlify.core.RdfViewConjunction;
import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;




/**
 * Associates a pattern with its owning view and contsraints.
 * 
 * To be replaced with (or renamed to) a generic n-ary union 'OpUnionN'
 * 
 * @author raven
 *
 */
@Deprecated
public class OpRdfUnionViewPattern
	extends OpExt
{
	/**
	 * Convert the RdfViewPattern to unions of RdfViewConjunctions.
	 * The rationale is, that for optimization reasons, filter expressions need to
	 * be pushed into the unions.
	 * 
	 * 
	 * @return
	 */
	public Op asUnions() {
		if(conjunctions.isEmpty())	{
			return OpNull.create();
		}
		
		Op a = null;
		for(RdfViewConjunction item : conjunctions) {
			Op b = new OpRdfViewPattern(item);
			
			if(a != null) {
				a = new OpUnion(a, b);
			}
			
			a = b;
		}
		
		return a;
	}
	
	
	private List<RdfViewConjunction> conjunctions;
	
	public OpRdfUnionViewPattern(List<RdfViewConjunction> conjunctions) {
		super(OpRdfUnionViewPattern.class.getName());
		this.conjunctions = conjunctions;
	}
	
	

	public OpRdfUnionViewPattern()
	{
		super(OpRdfUnionViewPattern.class.getName());
		this.conjunctions = new ArrayList<RdfViewConjunction>();
	}

	public List<RdfViewConjunction> getConjunctions()
	{
		return conjunctions;
	}



	@Override
	public Op effectiveOp()
	{
		return null;
	}

	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt)
	{
		/*
        out.print("'''") ;
        sqlNode.output(out) ;
        out.print("'''") ;
        */
		out.print("outputArgs");
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conjunctions == null) ? 0 : conjunctions.hashCode());
		return result;
	}

	@Override
	public boolean equalTo(Op obj, NodeIsomorphismMap labelMap)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OpRdfUnionViewPattern other = (OpRdfUnionViewPattern) obj;
		if (conjunctions == null) {
			if (other.conjunctions != null)
				return false;
		} else if (!conjunctions.equals(other.conjunctions))
			return false;
		return true;
	}
	
}

/*
class RdfViewPattern
	extends OpQuadPattern
{
	private RdfView owner;
	private Op parentOp;
	private Quad pattern;
	private Expr filter;

	public RdfViewPattern(RdfView owner, Op parentOp, Quad pattern, Expr filter)
	{
		super();
		this.owner = owner;
		this.parentOp = parentOp;
		this.pattern = pattern;
		this.filter = filter;
	}

	public RdfView getOwner()
	{
		return owner;
	}
	public Op getParentOp()
	{
		return parentOp;
	}
	public Quad getPattern()
	{
		return pattern;
	}
	public Expr getFilter()
	{
		return filter;
	}
}*/