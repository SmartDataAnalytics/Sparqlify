package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.expr.Expr;


/**
 * A maybe hacky filter that allows SQL ops to be constrained by SPARQL
 * expressions. Basically the idea was to reuse the SPARQL expr stack
 * rather than to create it again for SQL.
 * 
 * @author raven
 *
 */
public class SqlOpFilterExpr
	extends SqlOpBase1
{
	private List<Expr> exprs;

	public SqlOpFilterExpr(Schema schema, SqlOp subOp, Expr expr) {
		this(schema, subOp, new ArrayList<Expr>(Collections.singleton(expr)));
	}
	
	public SqlOpFilterExpr(Schema schema, SqlOp subOp, List<Expr> exprs) {
		super(schema, subOp);
	
		assert exprs != null : "Null pointer exception";
		
		this.exprs = exprs;
	}
	
	public List<Expr> getExprs() {
		return exprs;
	}
	
	public static SqlOp createIfNeeded(SqlOp op, List<Expr> exprs) {
		SqlOp result;
		
		if(exprs.isEmpty()) {
			result = op;
		} else {
			result = create(op, exprs);
		}
		
		return result;
	}
	
	
	public static SqlOpFilterExpr create(SqlOp op, List<Expr> exprs) {
		SqlOpFilterExpr result = new SqlOpFilterExpr(op.getSchema(), op, exprs); 
		return result;
	}
	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpFilterExpr" + exprs + "(");
		
		writer.incIndent();
		subOp.write(writer);
		writer.println();
		writer.decIndent();
		
		writer.print(")");
	}
	
	public boolean isEmpty() {
		return subOp.isEmpty();
		/*
		boolean containsFalse = SqlExprUtils.containsFalse(exprs, true);
		
		boolean result = containsFalse || subOp.isEmpty();
		
		return result;
		*/
	}
}