package org.aksw.sparqlify.core.transformations;



//public abstract class ExprTransformerBase
//	implements ExprTransformer
//{
//	//protected ExprEvaluator exprEvaluator;
//	
//	public ExprTransformerBase() {
//	}
//
//	/**
//	 * Default processing is to pass the arguments to the evaluator
//	 * 
//	 */
//	@Override
//	public Expr transform(ExprFunction fn) {
//		List<Expr> args = fn.getArgs();
//		List<Expr> newArgs = new ArrayList<Expr>(args.size());
//		
//		for(Expr arg : args) {
//			//Expr tmp = exprEvaluator.transform(arg);
//			Expr newArg = transformArg(arg); // SqlTranslationUtils.getLexicalValueOrExpr(arg);
//			newArgs.add(newArg);
//		}
//		
//		Expr result = buildExpr(fn, newArgs);
//		
//		
//		return result;
//	}
//	
//	protected Expr transformArg(Expr expr) {
//		return expr;
//	}
//
//	protected Expr buildExpr(ExprFunction originalExpr, List<Expr> newArgs) {
//		Expr result = ExprCopy.getInstance().copy(originalExpr, newArgs);
//		
//		return result;		
//	}
//}
