package org.aksw.sparqlify.database;

import org.aksw.sparqlify.restriction.RestrictionManager;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpFilterIndexed
	extends Op1
{
	protected RestrictionManager restrictions;
	
	public RestrictionManager getRestrictions() {
		return restrictions;
	}
	
	public OpFilterIndexed(Op subOp, RestrictionManager restrictions) {
		super(subOp);
		this.restrictions = restrictions;
	}
	
	

	/*
	@Override
	public void output(IndentedWriter out)
    {
		super.output(out);
		//OpFilter x;
		//out.println(getName());
		//QueryOutputUtils.output(this, out) ;
    }*/


	@Override
	public void visit(OpVisitor opVisitor) {
		System.out.println("[HACK] Replace OpFilterIndexed with OpExtFilterIndexed");
		OpExtFilterIndexed tmp = new OpExtFilterIndexed(this.getSubOp(), this.getRestrictions());
		tmp.visit(opVisitor);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Op apply(Transform transform, Op subOp) {
		//return transform.transform(this, subOp) ;
		return null;
	}

	@Override
	public Op copy(Op subOp) {
		return OpFilterIndexed.filter(new RestrictionManager(restrictions), subOp);
	}

	@Override
	public int hashCode() {
		return 17 * getSubOp().hashCode() + 13 * restrictions.hashCode();
	}

	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( ! (other instanceof OpFilterIndexed) ) return false ;
        OpFilterIndexed opFilter = (OpFilterIndexed)other ;
        if ( ! restrictions.equals(opFilter.restrictions) )
            return false ;
        
        return getSubOp().equalTo(opFilter.getSubOp(), labelMap) ;
	}
	
	
	public static OpFilterIndexed filter(RestrictionManager restrictions, Op subOp) {
		return new OpFilterIndexed(subOp, restrictions);
	}
}