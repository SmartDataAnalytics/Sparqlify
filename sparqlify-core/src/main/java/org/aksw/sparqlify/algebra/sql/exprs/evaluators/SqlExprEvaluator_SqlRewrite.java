package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Method;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.SqlTranslatorImpl;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.aksw.sparqlify.core.datatypes.SqlMethodCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_SqlRewrite
	implements SqlExprEvaluator
{
	private static final Logger logger = LoggerFactory.getLogger(SqlExprEvaluator_SqlRewrite.class);
	
	private DatatypeSystem datatypeSystem;

	private String functionId;
	
	public SqlExprEvaluator_SqlRewrite(DatatypeSystem datatypeSystem, String functionId) {
		this.datatypeSystem = datatypeSystem;
		this.functionId = functionId;
	}
	
	
	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		// If one of the arguments is a type error, we must return a type error.
		if(SqlTranslatorImpl.containsTypeError(args)) {
			return S_Constant.TYPE_ERROR;
		}
		
		List<TypeToken> argTypes = SqlTranslatorImpl.getTypes(args);

		SqlMethodCandidate castMethod = datatypeSystem.lookupMethod(functionId, argTypes);
		
		if(castMethod == null) {
			//throw new RuntimeException("No method found for " + fn);
			logger.debug("No method found for " + functionId);
			
			return S_Constant.TYPE_ERROR;
		}
		
		// TODO: Invoke the SQL method's invocable if it exists and all arguments are constants
		
		SqlExpr result = S_Method.createOrEvaluate(castMethod, args);

		logger.debug("[Result] " + result);
		
		return result;
	}
}
