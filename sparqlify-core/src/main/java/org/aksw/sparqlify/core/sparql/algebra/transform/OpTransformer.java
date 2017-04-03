package org.aksw.sparqlify.core.sparql.algebra.transform;


/*
 * TODO Implemet this:
 * A separation between traversal and transformation application.
 * For the traversal we could actually use one of Jena's visitors.
 */
/*
public class OpTransformer {
	
	private Object transformer;
	
	public static Op _transform(Op op, Object transformer, String methodName, Object ...args) {
		OpTransformer t = new OpTransformer(transformer);
	}
	
	public OpTransformer(Object transformer) {
		this.transformer = transformer;
	}
	
	public Op _apply(Op op, Object ...args) {
		return (Op)MultiMethod.invoke(op, "apply", args);
	}
	
	
	public static Op apply(Op op)
	{
		return MultiMethod.invokeStatic(ReplaceConstants.class, "_replace", op);
	}
		
	public void applyTransform() {
		
	}
		
	public static Op visit(OpExtend op) {
		return OpExtend.extend(replace(op.getSubOp()), op.getVarExprList());
	}
		
	public static Op visit(OpSlice op)
	{
		return new OpSlice(replace(op.getSubOp()), op.getStart(), op.getLength());
	}
		
	public static Op visit(OpOrder op) {
		// FIXME Might need to do deep-copy
		// Nothing to do
		return op;
	}

		
	public static Op visit(OpProject op) {
		return new OpProject(replace(op.getSubOp()), op.getVars());
	}

	public static Op visit(OpDistinct op) {
		return new OpDistinct(replace(op.getSubOp()));
	}
		
	public static Op visit(OpLeftJoin op) {
		return OpLeftJoin.create(replace(op.getLeft()), replace(op.getRight()), op.getExprs());
	}
		
	public static Op visit(OpJoin op) {
		return OpJoin.create(replace(op.getLeft()), replace(op.getRight()));
	}
		
	public static Op visit(OpUnion op) {
		return OpUnion.create(replace(op.getLeft()), replace(op.getRight()));
	}
		
	public static Op visit(OpFilter op) {
		return OpFilter.filter(op.getExprs(), replace(op.getSubOp()));
	}
		
	public static Op visit(OpQuadPattern op) {
		
	}

}
*/
