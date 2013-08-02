package org.aksw.sparqlify.algebra.sparql.domain;


import org.aksw.sparqlify.core.RdfViewConjunction;
import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;


public class OpRdfViewPattern
	extends OpExt
{
	private RdfViewConjunction conjunction;

	public OpRdfViewPattern(RdfViewConjunction conjunction) {
		super(OpRdfViewPattern.class.getSimpleName());
		this.conjunction = conjunction;
	}

	/*
	public OpRdfViewPattern() {
		super(OpRdfUnionViewPattern.class.getName());
		this.conjunction = new ArrayList<RdfViewConjunction>();
	}*/

	public RdfViewConjunction getConjunction() {
		return conjunction;
	}

	@Override
	public Op effectiveOp() {
		return null;
	}

	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
		/*
		 * out.print("'''") ; sqlNode.output(out) ; out.print("'''") ;
		 */
		out.print(conjunction.getViewNames() + " " + conjunction.getRestrictions());
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conjunction == null) ? 0 : conjunction.hashCode());
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
		OpRdfViewPattern other = (OpRdfViewPattern) obj;
		if (conjunction == null) {
			if (other.conjunction != null)
				return false;
		} else if (!conjunction.equals(other.conjunction))
			return false;
		return true;
	}	
    
}

