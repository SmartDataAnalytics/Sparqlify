package org.aksw.sparqlify.compile.sparql;

import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;

public class SqlGenerator
{
	/*
	public static String generateMM(SqlNode node)
	{
		return (String)MultiMethod.invokeStatic(SqlGenerator.class, "generate", node);
	}
	
	public static String generate(SqlProjection node)
	{
		return "";
	}
	
	public static String generate(SqlJoin node)
	{
		String result = "";
		
		//if(node.getConditions())
		// TODO generate ON part
		
		String onPart = "";

		/*
		for(SqlExpr expr : node.getConditions()) {
			SqlExprTranslator.translateMM(node.getConditions());
		}* /
		
		
		String left = generateMM(node.getLeft());
		String right = generateMM(node.getRight());
		
		
		result = left + " JOIN " + right + onPart; 
	
		return result;
	}

	public static String generate(SqlUnion node)
	{
		String left = generateMM(node.getLeft());
		String right = generateMM(node.getRight());

		return left + " UNION " + right;
	}
	
	public static String generate(SqlTable node)
	{
		return node.getTableName() + " " + node.getAliasName();
	}
	
	public static String generate(SqlQuery node)
	{
		return node.getQueryString() + " " + node.getAliasName();		
	}
	
	public static boolean isSqlQuery(SqlQuery node) {
		return (node instanceof SqlQuery);
	}
	*/
	
	/**
	 * Generate the final SQL Statement (or rather: query execution plan?)
	 * 
	 * Note: The binding of columns to sparql variables is part of the nodeBinding object.
	 * With this binding it is possible to bind all sparql variables used in the user query.
	 * 
	 * 
	 * 
	 * @param nodeBinding
	 * @return
	 */
	public String generateMM(SqlNodeOld node)
	{
		/*
		if(node instanceof SqlNodeEmpty) {
			return "Select null Limit 0";
		}*/
		
		/*
		GenerateSQLMySQL sqlGenerator = new GenerateSQLMySQL();

		String sqlPart = sqlGenerator.generatePartSQL(nodeBinding.getSqlNode());
		
		
		// add the conditions to the statement
		for(SqlExpr sqlExpr : nodeBinding.getConditions()) {
			String exprStr = SqlExprBase.asSQL(sqlExpr);
			System.out.println(exprStr);
			
		}
		*/
		
		//SqlSelectBlock block = new SqlSelectBlock("root", node);
		
		//Generator generator = Gensym.create("a");
		SqlNodeOld block = SqlSelectBlockCollector._makeSelect(node);
		
		
		block.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
		
		
		String result = SqlAlgebraToString.makeString(block);
		return result;
	}
}