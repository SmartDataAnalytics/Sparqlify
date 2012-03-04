package org.aksw.sparqlify.database;

import org.aksw.sparqlify.restriction.RestrictionManager;
import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpExtFilterIndexed
	extends OpExt
{
	protected Op subOp;
	protected RestrictionManager restrictions;

	public Op getSubOp() {
		return subOp;
	}
	
	public RestrictionManager getRestrictions() {
		return restrictions;
	}

	public OpExtFilterIndexed(Op subOp, RestrictionManager restrictions) {
		super("OpExtFilterIndexed");
		this.subOp = subOp;
		this.restrictions = restrictions;
	}

	@Override
	public Op effectiveOp() {
		return OpFilter.filter(restrictions.getExprs(), subOp);
	}

	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
		return null;
	}

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        int line = out.getRow() ;
        out.println(restrictions);
        WriterOp.output(out, this.subOp, sCxt) ;
        if ( line != out.getRow() )
            out.ensureStartOfLine() ;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( ! (other instanceof OpExtFilterIndexed) ) return false ;
        OpExtFilterIndexed opFilter = (OpExtFilterIndexed)other ;
        if ( ! restrictions.equals(opFilter.restrictions) )
            return false ;
        
        return getSubOp().equalTo(opFilter.getSubOp(), labelMap) ;
	}
	
}