package org.aksw.sparqlify.core.algorithms;


// Sigh, the point of the transformer IS to get rid of the RDF term ctors
// so this class is oxymoronic
//public class ExprTransformerRdfTermCtor
//	implements ExprTransformer
//{
//	
//	boolean hasRdfTermCtorArgument(ExprFunction fn) {
//		return hasRdfTermCtorArgument(fn.getArgs());
//	}
//	
//	boolean hasRdfTermCtorArgument(Iterable<Expr> exprs) {
//		for(Expr expr : exprs) {
//			if(expr instanceof E_RdfTerm) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
//	
//	@Override
//	public Expr transform(ExprFunction fn) {
//		
//		// Check if one argument is an RdfTermCtor
//		if(!hasRdfTermCtorArgument(fn)) {
//			return fn;
//		}
//
//		// Otherwise... TODO Somehow delegate to a function that can deal with it
//		
//		
//				
//		// TODO Auto-generated method stub
//		return null;
//	}
//}