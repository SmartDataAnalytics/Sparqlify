package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.factory.Factory1;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_SqlColumnRef;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.core.SqlDatatype;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Cast;
import com.hp.hpl.jena.sparql.expr.E_Divide;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;

public abstract class SqlExprSerializerDefault
	implements SqlExprSerializer
{

	public DatatypeAssigner datatypeAssigner;
	
	protected DatatypeToString datatypeSerializer;
	
	public SqlExprSerializerDefault(DatatypeAssigner datatypeAssigner, DatatypeToString datatypeSerializer)
	{
		this.datatypeAssigner = datatypeAssigner;
		this.datatypeSerializer = datatypeSerializer;
	}
	
	
	
	
	public String serialize(Expr expr) {
		try {
			String result = (String)MultiMethod.invoke(this, "_serialize", expr);
		
			return result;
		} catch(Exception e) {
			throw new RuntimeException("Error serializing expression" + expr, e);
		}
	}
	
	/*
	public String _serialize(ExprAggregator expr) {
		String result = this._serializeAgg(expr.getAggregator());
		return result;
	}
	
	
	public String _serializeAgg(SqlAggregator aggregator) {
		String result = MultiMethod.invoke(this, "serializeAgg", aggregator);
		return result;
	}
	
	public String serializeAgg(SqlAggregatorCount aggregator) {
		return "COUNT(*)";
	}
	*/
	
	
	
	
		
	public List<String> serializeArgs(List<Expr> exprs) {
		List<String> result = new ArrayList<String>();
		for(Expr expr : exprs) {
			String tmp = serialize(expr);
			result.add(tmp);
		}
		
		return result;
	}

	public String _serialize(E_Cast expr) {		
		
		throw new RuntimeException("fix this");
		
		/*
		String tmp = serialize(expr.getExpr());
		
		SqlDatatype datatype = datatypeAssigner.assign(expr);
		Factory1<String> caster = datatypeSerializer.asString(datatype);

		String result = caster.create(tmp);

		return result;
		*/
		
	}
	
	
	/*
	public String _serialize(E_Function expr) {
		List<String> args = serializeArgs(expr.getArgs());
		
		SqlStringTransformer transformer = null; //expr.getTransformer();
		
		if(transformer == null) {
			return expr.getFuncName() + "(" + Joiner.on(", ").join(serializeArgs(expr.getArgs())) + ")";
		} else {
			return transformer.transform(expr, args);
		}
	}
	*/

	
	/*
	public static String _serialize(Expr expr) {
		return expr.asSQL();
	}
	*/
	/*
	public String _serialize(ExprColumn expr) 
	{
		String columnAlias = SqlAlgebraToString.escapeAlias(expr.getColumnName()); 
		
		if(expr.getTableName() == null) {
			return columnAlias;
		} else {
			return expr.getTableName() + "." + columnAlias;
		}

		
		//return expr.asString();
	}
	*/

	
		
	/*
	public static String _serialize(SqlConstant expr) {
		return serializeConstant(expr.getValue(), expr.getDatatype());
	}*/
	
	
	public abstract String serializeConstant(Object value, SqlDatatype datatype);

	
	/*
	public String _serialize(E_String expr) {
		return expr.getSqlString();
	}*/

	/*
	// unused
	public String _serialize(ExprNode expr) {
		//return serializeConstant(expr);
		return serializeConstant(expr.getObject(), expr.getDatatype());
	}*/

	/*
	public String _serialize(E_GeographyFromText expr)
	{
		return "ST_GeographyFromText(" + serialize(expr.getExpr()) + ")"; 
	}

	public String _serialize(E_GeometryFromText expr)
	{
		return "ST_GeomFromText(" + serialize(expr.getExpr()) + ", 4326)"; 
	}
	
	public String _serialize(E_Intersects expr)
	{
		// Did I have some argument against ST_Intersects???
		//return "(" + serialize(expr.getArg1()) + " && " + serialize(expr.getArg2()) + ")";
		
		
		return "ST_Intersects(" + serialize(expr.getArg1()) + ", " + serialize(expr.getArg2()) + ")"; 
	}
	*/
	
	
	public String _serialize(E_LessThan expr) {
		return "(" + serialize(expr.getArg1()) + " < " + serialize(expr.getArg2()) + ")";
	}

	public String _serialize(E_LessThanOrEqual expr) {
		return "(" + serialize(expr.getArg1()) + " <= " + serialize(expr.getArg2()) + ")";
	}
	
	public String _serialize(E_Equals expr) {
		return "(" + serialize(expr.getArg1()) + " = " + serialize(expr.getArg2()) + ")";
	}

	public String _serialize(E_GreaterThanOrEqual expr) {
		return "(" + serialize(expr.getArg1()) + " >= " + serialize(expr.getArg2()) + ")";
	}

	public String _serialize(E_GreaterThan expr) {
		return "(" + serialize(expr.getArg1()) + " > " + serialize(expr.getArg2()) + ")";
	}

	public String _serialize(E_LogicalAnd expr) {
		return "(" + serialize(expr.getArg1()) + " AND " + serialize(expr.getArg2()) + ")";
	}

	public String _serialize(E_LogicalOr expr) {
		return "(" + serialize(expr.getArg1()) + " OR " + serialize(expr.getArg2()) + ")";
	}

	public String _serialize(E_LogicalNot expr) {
		return "(NOT " + serialize(expr.getExpr()) + ")";
	}
	
	public String _serialize(E_Add expr) {
		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
	}
	
	public String _serialize(E_Subtract expr) {
		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
	}
	
	public String _serialize(E_Multiply expr) {
		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
	}

	public String _serialize(E_Divide expr) {
		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
	}

	
	// IsNotNull
	public String _serialize(E_Bound expr) {
		String arg = serialize(expr.getExpr());
		String result = "(" + arg + " IS NOT NULL)";
		
		return result;
	}

	public String _serialize(E_StrConcat concat) {
		List<String> args = serializeArgs(concat.getArgs());
		String result = serializeConcat(args);
		return result;
	}
	
	public String _serialize(E_StrConcatPermissive concat) {
		List<String> args = serializeArgs(concat.getArgs());
		String result = serializeConcat(args);
		return result;
	}
	
	public String serializeConcat(List<String> args) {	
		String result = "(" + Joiner.on(" || ").join(args) + ")";

		return result;
	}
	
	public String _serialize(E_Regex expr) {
	
		List<Expr> args = expr.getArgs();
		Expr varArg = args.get(0);
		Expr patternArg = args.get(1);
		
		
		if(!patternArg.isConstant()) {
			throw new RuntimeException("Pattern for regex must be a constant, encountered: " + patternArg);
		}
		
		String arg = serialize(varArg);
		String pattern = patternArg.getConstant().asUnquotedString(); //serialize(patternArg);
		String result = arg + " ~* " + "'" + pattern + "'";

		return result;
	}
	
	public String _serialize(E_SqlColumnRef expr) {
		//System.out.println("ColumnRef: " + expr);
		//System.err.println("Should not use variables but rather something like E_ColumnRef.");
		return expr.getAliasName() + ".\"" + expr.getColumnName() + "\"";
	}
	

	public String _serialize(ExprVar expr) {
		System.err.println("Should not use variables but E_ColumnRef - Something most likely went wrong");
		return expr.getVarName();
	}

}