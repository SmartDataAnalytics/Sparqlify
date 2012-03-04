package org.aksw.sparqlify.algebra.sparql.nodes;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.sse.Tags;


public class OpFilter
	extends OpBase1
{
    private ExprList expressions ;
    
    public static Op filter(Expr expr, Op op)
    {
        OpFilter f = filter(op) ;
        f.getExprs().add(expr) ;
        return f ;
    }
    
    public static OpFilter filter(Op op)
    {
        if ( op instanceof OpFilter )
           return (OpFilter)op ;
        else
           return new OpFilter(op) ;  
    }
    
    public static Op filter(ExprList exprs, Op op)
    {
        if ( exprs.isEmpty() )
            return op ;
        OpFilter f = filter(op) ;
        f.getExprs().addAll(exprs) ;
        return f ;
    }
    
    
//    public static Op filterRaw(ExprList exprs, Op op)
//    {
//        if ( exprs.isEmpty() )
//            return op ;
//        OpFilter f = new OpFilter(exprs, op) ;
//        return f ;
//    }

    /** Make a OpFilter - guarantteed to return an OpFilter */
    public static OpFilter filterDirect(ExprList exprs, Op op)
    {
        return new OpFilter(exprs, op) ;
    }

    
    private OpFilter(Op sub)
    { 
        super(sub) ;
        expressions = new ExprList() ;
    }
    
    private OpFilter(ExprList exprs , Op sub)
    { 
        super(sub) ;
        expressions = exprs ;
    }
    
    /** Compress multiple filters:  (filter (filter (filter op)))) into one (filter op) */ 
    public static OpFilter tidy(OpFilter base)
    {
        ExprList exprs = new ExprList() ;
        
        Op op = base ; 
        while ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            exprs.addAll(f.getExprs()) ;
            op = f.getSubOp() ;
        }
        return new OpFilter(exprs, op) ;
    }
    
    public ExprList getExprs() { return expressions ; }
    
    public String getName() { return Tags.tagFilter ; }

    /*
    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }

    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    
    @Override
    public Op copy(Op subOp)                { return new OpFilter(expressions, subOp) ; }
    */
    @Override
    public int hashCode()
    {
        return expressions.hashCode() ;
    }
    
    /*
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpFilter) ) return false ;
        OpFilter opFilter = (OpFilter)other ;
        if ( ! expressions.equals(opFilter.expressions) )
            return false ;
        
        return getSubOp().equalTo(opFilter.getSubOp(), labelMap) ;
    }
    */
}

