package org.aksw.sparqlify.algebra.sparql.expr.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

/**
 * Should supersede E_RdfTerm.
 *
 *
 * SqlExprs must of the following types:
 * type: integer
 * value: any
 * languageTag: string
 * datatype: string
 * 
 * OUTDATED statement was: "SqlExprs may be of arbitrary datatype, except for type which must be integer cast-able"
 * 
 * @author raven
 *
 */
public class E_RdfTerm2
	extends ExprFunction0
{
	/*
	private SqlExpr type;
	private SqlExpr value;
	private SqlExpr language;
	private SqlExpr datatype;
	*/
	
	private List<SqlExpr> sqlExprs;
	
	
	public E_RdfTerm2(List<SqlExpr> sqlExprs) {
		super(SparqlifyConstants.rdfTermLabel);
		this.sqlExprs = sqlExprs;
		//this(sqlExprs.get(0), sqlExprs.get(1), sqlExprs.get(2), sqlExprs.get(3));
	}

	public E_RdfTerm2(E_RdfTerm2 other) {
		this(new ArrayList<SqlExpr>(other.getSqlExprs()));
		//this(other.getType(), other.getValue(), other.getLanguage(), other.getDatatype());
	}

	public E_RdfTerm2(SqlExpr type, SqlExpr value, SqlExpr language, SqlExpr datatype) {
		this(new ArrayList<SqlExpr>(Arrays.asList(type, value, language, datatype)));
		/*
		this.type = type;
		this.value = value;
		this.language = language;
		this.datatype = datatype;
		*/
	}
	
	public List<SqlExpr> getSqlExprs()
	{
		return sqlExprs;
	}
	
	public SqlExpr getType()
	{
		return sqlExprs.get(0);
	}

	public SqlExpr getValue()
	{
		return sqlExprs.get(1);
	}
	
	public SqlExpr getLanguage()
	{
		return sqlExprs.get(2);
	}
	
	public SqlExpr getDatatype()
	{
		return sqlExprs.get(3);
	}

	/*
	@Override
	protected NodeValue eval(List<NodeValue> args) {
		return RdfTerm.eval(args.get(0), args.get(1), args.get(2), args.get(3));
		//RdfTerm
		//throw new RuntimeException("Should not happen");
		// TODO Auto-generated method stub
		//return null;
	}
	*/

	//@Override
	protected E_RdfTerm2 copy(SqlExprList args) {
		return new E_RdfTerm2(args.asList());
	}

	@Override
	public NodeValue eval(FunctionEnv env) {
		throw new NotImplementedException();
	}

	@Override
	public Expr copy() {
		return new E_RdfTerm2(this);
	}

}
