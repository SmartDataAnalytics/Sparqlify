package org.aksw.sparqlify.algebra.sparql.nodes;


public abstract class OpBase2
	extends OpBase
{
    private Op left ;
    private Op right ;

    
    public OpBase2(Op left, Op right)
    {
        this.left = left ; this.right = right ;
    }
    
    public Op getLeft() { return left ; }
    public Op getRight() { return right ; }

    public void setLeft(Op op)  { left = op ; }
    public void setRight(Op op) { right = op ; }
    

    @Override
    public int hashCode()
    {
        return left.hashCode()<<1 ^ right.hashCode() ^ getName().hashCode() ;
    }
}
