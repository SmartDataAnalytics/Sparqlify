package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Agg;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Serialize;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.apache.jena.sparql.expr.E_Cast;
import org.apache.jena.sparql.expr.NodeValue;

public abstract class SqlExprSerializerDefault
	implements SqlExprSerializer
{

	//public DatatypeAssigner datatypeAssigner;
	
	protected DatatypeToString datatypeSerializer;
	
	public SqlExprSerializerDefault(DatatypeToString datatypeSerializer)
	{
		//this.datatypeAssigner = datatypeAssigner;
		this.datatypeSerializer = datatypeSerializer;
	}
	
		
	public String _serialize(NodeValue nodeValue) {
		
		if(nodeValue.equals(NodeValue.nvNothing)) {
			return "NULL ";
		}
		
		// TODO Handle this correctly
		String result = nodeValue.asUnquotedString();
		
		return result;
	}
	
	
	@Override
	public String serialize(SqlExpr expr) {
		try {
			String result = (String)MultiMethod.invoke(this, "_serialize", expr);
		
			return result;
		} catch(Exception e) {
			throw new RuntimeException("Error serializing expression" + expr, e);
		}
	}

	public String _serialize(S_Agg expr) {
		System.err.println("Hack used for serializing aggregators.");
		String result = expr.getAggregator().toString();  ////this._serializeAgg(expr.getAggregator());
		return result;
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
	
	
	public String _serialize(S_Serialize expr) {
		List<String> argStrs = serializeArgs(expr.getArgs());
		
		String result = expr.getSerializer().serialize(argStrs);
		return result;
	}
	
	
		
	public List<String> serializeArgs(List<SqlExpr> exprs) {
		List<String> result = new ArrayList<String>();
		for(SqlExpr expr : exprs) {
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

	
		
	public String _serialize(S_Constant expr) {
		return serializeConstant(expr.getValue(), expr.getDatatype());
	}
	
	
	public abstract String serializeConstant(Object value, TypeToken datatype);

	
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
	
//
//	public String _serialize(E_LessThan expr) {
//		return "(" + serialize(expr.getArg1()) + " < " + serialize(expr.getArg2()) + ")";
//	}
//
//	public String _serialize(E_LessThanOrEqual expr) {
//		return "(" + serialize(expr.getArg1()) + " <= " + serialize(expr.getArg2()) + ")";
//	}
//	
//	public String _serialize(E_Equals expr) {
//		return "(" + serialize(expr.getArg1()) + " = " + serialize(expr.getArg2()) + ")";
//	}
//
//	public String _serialize(E_GreaterThanOrEqual expr) {
//		return "(" + serialize(expr.getArg1()) + " >= " + serialize(expr.getArg2()) + ")";
//	}
//
//	public String _serialize(E_GreaterThan expr) {
//		return "(" + serialize(expr.getArg1()) + " > " + serialize(expr.getArg2()) + ")";
//	}
//
//	public String _serialize(E_LogicalAnd expr) {
//		return "(" + serialize(expr.getArg1()) + " AND " + serialize(expr.getArg2()) + ")";
//	}
//
//	public String _serialize(E_LogicalOr expr) {
//		return "(" + serialize(expr.getArg1()) + " OR " + serialize(expr.getArg2()) + ")";
//	}
//
//	public String _serialize(E_LogicalNot expr) {
//		return "(NOT " + serialize(expr.getExpr()) + ")";
//	}
//	
//	public String _serialize(E_Add expr) {
//		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
//	}
//	
//	public String _serialize(E_Subtract expr) {
//		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
//	}
//	
//	public String _serialize(E_Multiply expr) {
//		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
//	}
//
//	public String _serialize(E_Divide expr) {
//		return "(" + serialize(expr.getArg1()) + " + " + serialize(expr.getArg2()) + ")";
//	}
//
//	
//	// IsNotNull
//	public String _serialize(E_Bound expr) {
//		String arg = serialize(expr.getExpr());
//		String result = "(" + arg + " IS NOT NULL)";
//		
//		return result;
//	}
//
//	public String _serialize(E_StrConcat concat) {
//		List<String> args = serializeArgs(concat.getArgs());
//		String result = serializeConcat(args);
//		return result;
//	}
//	
//	public String _serialize(E_StrConcatPermissive concat) {
//		List<String> args = serializeArgs(concat.getArgs());
//		String result = serializeConcat(args);
//		return result;
//	}
//	
//	public String serializeConcat(List<String> args) {	
//		String result = "(" + Joiner.on(" || ").join(args) + ")";
//
//		return result;
//	}
//	
//	public String _serialize(E_Regex expr) {
//	
//		List<Expr> args = expr.getArgs();
//		Expr varArg = args.get(0);
//		Expr patternArg = args.get(1);
//		
//		
//		if(!patternArg.isConstant()) {
//			throw new RuntimeException("Pattern for regex must be a constant, encountered: " + patternArg);
//		}
//		
//		String arg = serialize(varArg);
//		String pattern = patternArg.getConstant().asUnquotedString(); //serialize(patternArg);
//		String result = arg + " ~* " + "'" + pattern + "'";
//
//		return result;
//	}
//	
	public String _serialize(S_ColumnRef expr) {
		//System.out.println("ColumnRef: " + expr);
		//System.err.println("Should not use variables but rather something like E_ColumnRef.");
		
		String result;
		if(expr.getRelationAlias() == null) {
			result = "\"" + expr.getColumnName() + "\"";
		} else {
			result = expr.getRelationAlias() + ".\"" + expr.getColumnName() + "\"";
		}

		
		
		//String result = expr.getVarName();
		
		//String result = expr.getRelationAlias() + ".\"" + expr.getColumnName() + "\"";
		return result;
	}
//	
//
//	public String _serialize(ExprVar expr) {
//		System.err.println("Should not use variables but E_ColumnRef - Something most likely went wrong");
//		return expr.getVarName();
//	}

}