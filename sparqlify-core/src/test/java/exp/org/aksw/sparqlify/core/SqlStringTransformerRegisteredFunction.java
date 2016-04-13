package exp.org.aksw.sparqlify.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.config.syntax.FunctionTemplate;
import org.aksw.sparqlify.config.syntax.ParamType;
import org.aksw.sparqlify.core.algorithms.RegisteredFunction;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;

class SqlStringTransformerRegisteredFunction
//	implements SqlStringTransformer
{
	private RegisteredFunction regFn;
	
	public SqlStringTransformerRegisteredFunction(RegisteredFunction regFn) {
		this.regFn = regFn;
	}

	
	
	//@Override
	public SqlExpr transform(S_Function function, List<SqlExpr> args) {

		List<ParamType> paramTypes = regFn.getDeclaration().getSignature().getParamTypeList();
		
		// Based on the given arguments, create a map Var->SqlExpr
		Map<Var, SqlExpr> map = new HashMap<Var, SqlExpr>();
		for(int i = 0; i < paramTypes.size(); ++i) {
			ParamType paramType = paramTypes.get(i);
			Var var = paramType.getVar();
			
			SqlExpr sqlExpr = args.get(i);
			
			map.put(var, sqlExpr);
		}

		
		FunctionTemplate template = regFn.getDeclaration().getTemplate();

		// Evaluate each of the given template arguments
		ExprList templateExprs = template.getExprList();
		
		// !!! TODO The whole method should probably be integrated in the ExprEvaluatorPartial !!!
		
		
		String result = template.getName() + "(" + ")";
		
		return null;
	}
	
}