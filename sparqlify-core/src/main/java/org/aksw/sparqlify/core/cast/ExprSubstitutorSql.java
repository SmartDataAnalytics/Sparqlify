package org.aksw.sparqlify.core.cast;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;



/**
 * Substitutes expressions with their SQL equivalent.
 * 
 * Substitutions are specified with expressions such as:
 * 
 * ogc:intersects(?a, ?b) :=
 *     typedLiteral(ST_INTERSECTS(?a, ?b), xsd:boolean)
 * 
 * or more abstract:
 * sparqlFunctionName(args)
 *     termCtorExpr(SQLFunctionName(args))
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ExprSubstitutorSql
	implements ExprSubstitutor
{
	private TypeSystem typeSystem;
	//private SqlFunctionCollection sqlFunctions = new SqlFunctionCollection();

	// A set of expressions of how this method may be implemented
	// At most one expression may apply without a yielding a type error
	// TODO nail down the rules for type errors
	private ExprRewriteCollection exprRewrites;

	/*
	public ExprSubstitutorSql(TypeSystem typeSystem) {
		this(typeSystem, new ExprRewriteCollection(paramVars, varArgVar))
	}
	*/
	
	public ExprSubstitutorSql(TypeSystem typeSystem, ExprRewriteCollection exprRewrites) {
		this.typeSystem = typeSystem;
		this.exprRewrites = exprRewrites;
	}
	
	
	/**
	 * The result of this method depends on the given arguments and the
	 * registered SQL functions.
	 * 
	 */
	public SqlExpr create(List<SqlExpr> expr) {
		System.err.println("work in progress");
		return null;
	}
	
//	/**
//	 * 
//	 * 
//	 * @return The set of SQL methods registered for this Sparql Function. Should be consistent with an evaluator.
//	 */
//	Collection<XMethod> getSqlFunctions() {
//		return sqlFunctions.getFunctions();
//	}
//	
//	
//	/**
//	 * 
//	 * 
//	 * @param argTypes
//	 * @return The SQL function registered with this SPARQL function that matches the given arguments best.
//	 */
//	SqlMethodCandidate lookup(List<TypeToken> argTypes) {
//		SqlMethodCandidate result = sqlFunctions.lookupMethod(argTypes);
//		return result;
//	}
//
//
//	public ExprSubstitutorSql create(TypeSystem typeSystem) {
//		ExprSubstitutorSql result = new ExprSubstitutorSql(typeSystem);
//		return result;
//	}
}