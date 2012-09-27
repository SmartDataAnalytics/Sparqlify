package org.aksw.sparqlify.compile.sparql;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.commons.factory.Factory1;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.aksw.sparqlify.algebra.sql.exprs.S_String;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlSortCondition;
import org.aksw.sparqlify.algebra.sql.nodes.SqlAlias;
import org.aksw.sparqlify.algebra.sql.nodes.SqlJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNode;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlUnion;
import org.aksw.sparqlify.algebra.sql.nodes.SqlUnionN;
import org.aksw.sparqlify.algebra.sql.nodes.VarDef;
import org.aksw.sparqlify.core.SqlNodeBinding;
import org.openjena.atlas.io.IndentedWriter;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;



public class SqlAlgebraToString
{
	//private static SqlExprSerializer sqlExprSerializer = new SqlExprSerializerMySql();
	private static SqlExprSerializer sqlExprSerializer = new SqlExprSerializerPostgres();
	
	private static DatatypeToStringPostgres castFactory = new DatatypeToStringPostgres();
	
	public static String makeString(SqlNode node) {
		
		SqlAlgebraToString transformer = new SqlAlgebraToString();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IndentedWriter writer = new IndentedWriter(out);
				
		transformer.asString(node, writer);
		
		return out.toString();
		//return (String)MultiMethod.invokeStatic(SqlAlgebraToString.class, "_asString", node);
	}
	
	public void asString(SqlNode node, IndentedWriter writer) {
		
		SqlAlgebraToString transformer = new SqlAlgebraToString();
		
		//return (String)
		MultiMethod.invoke(this, "_asString", node, writer);
	}
	
	/*
	public static String projection(Map<Var, Expr> map)
	{
    	String result = "";

    	List<String> strs = new ArrayList<String>();
    	for(Entry<Var, Expr> entry : map.entrySet()) {
    		
    		String keyStr = "";
    		String exprStr = "";
    		if(entry.getValue() != null) {
    			SqlExpr sqlExpr = SqlExprTranslator.translateMM(entry.getValue());
    			exprStr = sqlExpr.asSQL() + " ";

    			SqlExpr sqlKey = SqlExprTranslator.translateVar(entry.getKey());
    			keyStr = sqlKey.asSQL();
    		}

    		strs.add(exprStr + keyStr);
    	}

    	result = Joiner.on(", ").join(strs);
    	return result;
	}
	*/
	
	
	public static String projection(Map<String, SqlExpr> map)
	{
		// Empty projections can occur if a query response is determined by static triples
		if(map.isEmpty()) {
			return "true";
		}
		
    	String result = "";

    	List<String> strs = new ArrayList<String>();
    	
    	// When writing the projection as an SQL string, the column names will be sorted
    	// in order to make sure that the projections within unions are correctly aligned.
    	SortedSet<String> columnNames = new TreeSet<String>(map.keySet());
    	for(String columnName : columnNames) {
    	//for(Entry<String, SqlExpr> entry : map.entrySet()) {
    		
    		SqlExpr value = map.get(columnName);
    		//String keyStr = entry.getKey();
    		String exprStr = "";
    		
    		/*
    		if(keyStr.equals("h_3")) {
    			System.out.println("here");
    		}*/
    		
    		if(value != null) {
    			//SqlExpr sqlExpr = entry.getValue();
    			exprStr = sqlExprSerializer.serialize(value); //sqlExpr.asSQL() + " ";

    			//SqlExpr sqlKey = SqlExprTranslator.translateVar(entry.getKey());
    			//keyStr = sqlKey.asSQL();
    		} 
    		/*
    		else {
    			// TODO Not sure if it is ok to simply assume empty string or null
    			exprStr = "NULL";
    		}*/

    		//strs.add(exprStr + " " + columnName);
    		
    		strs.add(exprStr + " " + escapeAlias(columnName));
    	}

    	result = Joiner.on(", ").join(strs);
    	return result;
	}
	
	/**
	 * Column names that clash with keywords need to be escaped.
	 * TODO Make this properly. Also, maybe we need to do a renaming earlier.
	 * 
	 * @param columnName
	 * @return
	 */
	public static String escapeAlias(String columnName)
	{
		return "\"" + columnName + "\"";
	}
	
	
	
	
	
	
	
	
	
	
	public void _asString(SqlNodeEmpty node, IndentedWriter writer) {
		writer.print("EMPTY_SQL_NODE");
	}

	
	public void _asString(SqlAlias node, IndentedWriter writer)
	{
		writer.print("(");
		asString(node.getSubNode(), writer);
		writer.print(") " + node.getAliasName()); 
	}
	
	public void _asString(SqlQuery node, IndentedWriter writer)
	{
		// FIXME: Actually the parent node must determine whether to put the expression into parenthesis
		//String result;
		if(node.getAliasName() == null) {
			writer.print(node.getQueryString());
			
		} else {
			writer.print("(" + node.getQueryString() + ") " + node.getAliasName());			
		}
		
		//String result = "(SELECT " + projection(node.getColumnToSqlExpr()) + " FROM (" + node.getQueryString() + ") " + node.getInnerAlias() + ")" + node.getAliasName();
		//return result;
	}
	
	

	
	/**
	 * Merge all possible combinations of sparql vars into a single column 
	 * 
	 * @param node
	 */
	public static void groupBy(Var var, SqlNode target, SqlNode node, Generator generator) {
		Collection<VarDef> tmp = node.getSparqlVarToExprs().get(var);
		
		// We need to group even if there is just a single expression for the var
		// WRONG: If there is just a single expression, we can do a soft grouping
		// (i.e. ignore conversions to string (such as uris build from id int columns)

		if(tmp.size() <= 1) {
			return;
		}
		
		
		
		// Create a copy of the exprs and sort by number of variables
		List<VarDef> defs = new ArrayList<VarDef>(tmp);
		Collections.sort(defs, new Comparator<VarDef>(){
			@Override
			public int compare(VarDef arg0, VarDef arg1) {
				return arg1.getExpr().getVarsMentioned().size() - arg1.getExpr().getVarsMentioned().size();
			}});

		
		NodeExprSubstitutor substitutor = SqlNodeBinding.createSubstitutor(node.getAliasToColumn());
		// Now for each component of the E_RdfTerm, create the projection
		
		
		/*
		if(defs.isEmpty()) {
			System.out.println("debug");
		}*/
		
		
		
		/*
		// Rearrange exprs by args 
		List<Map<Set<Var>, Expr>> argToExprs = new ArrayList<Map<Set<Var>, Expr>>();
		for(int i = 0; i < 4; ++i) {
			Set<Expr> newExprs = new HashSet<Expr>();
			argToExprs.add(newExprs);
		
			for(Expr expr : exprs) {
				E_RdfTerm term = (E_RdfTerm)expr;
		
				newExprs.add(term.getArgs().get(i));
			}
		}*/
			
		List<Expr> newArgs = new ArrayList<Expr>();
		
		
		String datatype;
		for(int i = 0; i < 4; ++i) {

			// Dependency on the columns
			List<SqlExpr> columnDeps = new ArrayList<SqlExpr>();

			
			datatype = (i == 0) 
					? "::int"   //DatatypeSystemDefault._INTEGER
					: "::text"; //DatatypeSystemDefault._STRING;
			
			
			//Factory1<String> caster = castFactory.formatString(sqlExpr.getDatatype());
			
			
			List<String> exprStrs = new ArrayList<String>();
			for(VarDef def : defs) {
				
				Expr e = def.getExpr();
				
				E_RdfTerm args = (E_RdfTerm)e;
				Expr arg = args.getArgs().get(i);			
				
				//SqlExpr sqlExpr = SqlNodeBinding.forcePushDown(arg, substitutor);//(arg, node);
				SqlExpr sqlExpr = SqlNodeBinding.forcePushDown(arg, substitutor);
				
				Factory1<String> formatter = castFactory.formatString(sqlExpr.getDatatype());
				
				String exprStr = sqlExprSerializer.serialize(sqlExpr);
				
				exprStr = formatter.create(exprStr);
				
				
				exprStrs.add(exprStr);
			}

			
			String replacement = "";
			/* TODO We need the dependencies to any column (see a few lines below)
			if(exprs.size() == 1) {
				replacement = exprStrs.get(0);
				
			} else*/ {
			
				String caseStr = "CASE\n";
				String elseStr = "NULL" + datatype;
				
				for(int j = 0; j < defs.size(); ++j) {
					VarDef def = defs.get(j);
					Expr expr = def.getExpr();
					
					String exprStr = exprStrs.get(j);

					if(expr.getVarsMentioned().isEmpty()) {
						elseStr = exprStr;
					} else {
					
						List<String> columnNames = new ArrayList<String>();
						for(Var v : expr.getVarsMentioned()) {
							

							// Keep the dependency to the original columns
							// If an expression does not depend on other columns, it will be treated as a constant.
							if(true) {
								String depName = v.getName();
								SqlExpr depSqlExpr = node.getAliasToColumn().get(depName);
								SqlDatatype depDatatype = depSqlExpr.getDatatype();
								
								columnDeps.add(new SqlExprColumn(target.getAliasName(), depName, depDatatype));
							}
								
							
							
							//columnNames.add(v.getName() + " IS NOT NULL");
							columnNames.add(target.getAliasName() + "." + v.getName() + " IS NOT NULL");
						}
				
						caseStr += "    WHEN (" + Joiner.on(" AND ").join(columnNames) + ") THEN " + "(" + exprStr + ")" + datatype + "\n";
					}
				}
				
				
				caseStr += "    ELSE " + elseStr + "\n";
				caseStr += "END ";

				replacement = caseStr;
			}


			String columnAlias = generator.next();
			newArgs.add(new ExprVar(columnAlias));

			
			S_String c = new S_String(replacement, DatatypeSystemDefault._STRING, columnDeps);
			target.getAliasToColumn().put(columnAlias, c);
			//node.getAliasToColumn().put(columnAlias, c);
			
			
			/*
			System.out.println("Group By " + var);
			System.out.println(SqlAlgebraToString.makeString(node));//asString(node, new IndentedWriter(System.out));
			System.out.println("-----------------------------");
			*/
		}


		E_RdfTerm replacement = new E_RdfTerm(newArgs);


		// Replace the projection
		target.getSparqlVarToExprs().removeAll(var);
		target.getSparqlVarToExprs().put(var, new VarDef(replacement));
	}
	
	
	public static String getAliasName(SqlNode node) {
		if(node.getAliasName() == null || node.getAliasName().isEmpty()) {
			return "";
		} else {
			return " " + node.getAliasName();
		}		
	}
	
    public void _asString(SqlSelectBlock node, IndentedWriter writer)
    {    	
    	writer.print("SELECT ");
    	
    	// Distinct
    	//String distinctStr = "";
    	if(node.isDistinct()) {
    		writer.print("DISTINCT ");
    		//distinctStr += " DISTINCT";
    	}
    	
    	// Projection
    	writer.println(projection(node.getAliasToColumn()));
    	
    	writer.println("FROM");

    	boolean isUnion = node.getSubNode() instanceof SqlUnionN || node.getSubNode() instanceof SqlSelectBlock;
    	
    	if(isUnion) {
    		writer.print("(");
    	}

    	
    	writer.incIndent();
    	// Joins
    	
    	asString(node.getSubNode(), writer);
    	writer.decIndent();
    
    	if(isUnion) {
    		writer.print(") " + getAliasName(node));
    	}
    	
    	if(!writer.atLineStart()) {
    		writer.println();
    	}
    	
    	/*
    	if(!joinStr.isEmpty()) {
    		joinStr = "FROM " + joinStr;
    	}
    	*/
    	
/*
    	if(node.getSubNode() instanceof SqlUnionN) {
    		joinStr = "(" + joinStr + ") " + node.getAliasName(); 
    	}
*/  	
    	
    	// Selection
    	//String selectionStr = "";
    	{
	    	List<String> strs = new ArrayList<String>();
	    	for(SqlExpr expr : node.getConditions()) {
	    		String str = sqlExprSerializer.serialize(expr);
	    		//String str = expr.asSQL();
	    		
	    		strs.add(str);
	    	}
	    	
	    	if(!strs.isEmpty()) {
	    		writer.print("WHERE ");
	    		//selectionStr += " WHERE ";
	    	}
	    	
	    	writer.println(Joiner.on(" AND ").join(strs));
	    	//selectionStr += Joiner.on(" AND ").join(strs);
    	}    	
    	
    	
		List<String> sortColumnExprStrs = new ArrayList<String>();
    	for(SqlSortCondition condition : node.getSortConditions()) {
    		String dirStr = null;
    		if(condition.getDirection() == Query.ORDER_ASCENDING) {
    			dirStr = "ASC";
    		} else if(condition.getDirection() == Query.ORDER_DESCENDING) {
    			dirStr = "DESC";
    		}
    		
    		
    		// TODO This is not working properly: If a sparql variable is made up
    		// from multiple sql columns, we need to settle for an ordering -
    		// right now we get: c1 OR c2 OR ... cn
    		String exprStr = sqlExprSerializer.serialize(condition.getExpression());
    					
			if(dirStr != null) {
				//exprStr = dirStr + "(" + exprStr + ")";
				exprStr = exprStr + " " + dirStr;
			}
    					
    		sortColumnExprStrs.add(exprStr);
    	}
    	String orderStr = "";
    	if(!sortColumnExprStrs.isEmpty()) {
        	orderStr = "ORDER BY " + Joiner.on(", ").join(sortColumnExprStrs);
    		writer.println(orderStr);
    	}

    	
    	//String limitStr = "";
    	if(node.getLimit() != null) {
    		writer.println("LIMIT " + node.getLimit());
    		//limitStr = " LIMIT " + node.getLimit();
    	}
    	
    	
    	String offsetStr = "";
    	if(node.getOffset() != null) {
    		writer.println("OFFSET " + node.getOffset());
    		//offsetStr = " OFFSET " + node.getOffset();
    	}
    	


    	/*
		List<String> sortColumnExprStrs = new ArrayList<String>();
    	for(SqlSortCondition condition : node.getSortConditions()) {
    		String dirStr = null;
    		if(condition.getDirection() == Query.ORDER_ASCENDING) {
    			dirStr = "ASC";
    		} else if(condition.getDirection() == Query.ORDER_DESCENDING) {
    			dirStr = "DESC";
    		}
    		
    		for(Var var : condition.getExpression().getVarsMentioned()) {
    			for(Expr expr : node.getSparqlVarToExprs().asMap().get(var)) {
    				for(Var columnName : expr.getVarsMentioned()) {
    					SqlExpr sqlExpr = node.getAliasToColumn().get(columnName.getName());
    					
    					String exprStr = sqlExprSerializer.serialize(sqlExpr);
    					
    					if(dirStr != null) {
    						//exprStr = dirStr + "(" + exprStr + ")";
    						exprStr = exprStr + " " + dirStr;
    					}
    					
    					sortColumnExprStrs.add(exprStr);
    				}
    			}
    		}
    	}
    	String orderStr = "";
    	if(!sortColumnExprStrs.isEmpty()) {
        	orderStr = " ORDER BY " + Joiner.on(", ").join(sortColumnExprStrs);
    	}
    	*/


    	
    	//String result = "SELECT " + distinctStr + projectionStr + " FROM\n" + joinStr + selectionStr + orderStr + limitStr + offsetStr;
    	
    	//return result;
    }


    /*
    public static String _asString(SqlDisjunction node) {

    	
    	String left =  asString(node.getLeft());
    	String right = asString(node.getRight());
    			
    	String result = left + " UNION " + right;
    	return result;
    }*/

    public void _asString(SqlUnion node, IndentedWriter writer) {
    	throw new RuntimeException("SqlUnion is deprecated. Use SqlUnionN instead.");
    	/*
    	String left =  asString(node.getLeft());
    	String right = asString(node.getRight());
    			
    	String result = left + " UNION ALL " + right;
    	return result;
    	*/
    }
    
    public void _asString(SqlUnionN node, IndentedWriter writer) {
    	//writer.println("(");
		writer.incIndent();
    	
    	List<String> parts = new ArrayList<String>();
    	
    	for(int i = 0; i < node.getArgs().size(); ++i) {
    		SqlNode arg = node.getArgs().get(i);
    	//for(SqlNode arg : node.getArgs()) {
    		
    		//String part = "SELECT " + projection(arg.getColumnToSqlExpr()) + " FROM " + asString(arg) + " " + arg.getAliasName() + "";
    		//String sub = asString(arg);
    		writer.incIndent();
    		asString(arg, writer);
    		writer.decIndent();
    		
    		if(i != node.getArgs().size() - 1) {
    			writer.println("UNION ALL");
    		}
    		
    		/*
    		if(arg instanceof SqlQuery) {
    			String innerAlias = ((SqlQuery) arg).getInnerAlias();
    			sub += " " + innerAlias;
    		}*/
    		
    		//String part = "SELECT " + projection(arg.getColumnToSqlExpr()) + " FROM " + sub;
    		//String part = sub;
    		
    		//parts.add(part + " ");
    	}
    	
    	writer.decIndent();
    	//writer.print(") " + node.getAliasName());
    	
    	//String result = "(" + Joiner.on(" UNION ALL ").join(parts) + ") " + node.getAliasName();
    	//return result;
    }

    /*
    public static String _asString(SqlJoinN node) {
    	List<String> parts = new ArrayList<String>();
    	for(SqlNode arg : node.getArgs()) {
    		parts.add(asString(arg));
    	}
    	
    	String result = Joiner.on(" JOIN ").join(parts);
    	return result;
    }*/
    
    
    public void asStringJoinU(SqlNode node, String aliasName, IndentedWriter writer) {
    	asString(node, writer);


    	/*
    	boolean isSubSelect = node instanceof SqlSelectBlock || node instanceof SqlUnionN;
    	
    	if(isSubSelect) {
    		writer.print("(");
    		writer.incIndent();
    	}

    	asString(node, writer);

    	if(isSubSelect) {
    		writer.decIndent();    		
    		if(!writer.atLineStart()) {
    			writer.println();
    		}
			writer.print(")" + aliasName);
    	} */
    }

    

    public void _asString(SqlJoin node, IndentedWriter writer) {
    	//throw new RuntimeException("SqlUnion is deprecated. Use SqlUnionN instead.");

    	//writer.print("(");
    	
    	asStringJoinU(node.getLeft(), node.getLeft().getAliasName(), writer);
    	//asStringJoinU(node.getLeft(), node.getLeftAlias(), writer);
    	
    	//writer.print(") AS " + node.getLeft().getAliasName());
    	
    	writer.println();
    	
    	String joinOp = "";
    	
    	if(node.getJoinType().equals(JoinType.INNER)) {
    		joinOp = "JOIN ";
    	} else if(node.getJoinType().equals(JoinType.LEFT)) {
    		joinOp = "LEFT JOIN ";
    	} else {
    		throw new RuntimeException("Join type not supported");
    	}

    	writer.print(joinOp);
    	
    	
    	//writer.print("(");
    	
    	asStringJoinU(node.getRight(), node.getRight().getAliasName(), writer);
    	//asStringJoinU(node.getRight(), node.getRightAlias(), writer);
    	
    	//writer.print(") AS " + node.getRight().getAliasName());
    	
    	String restrictionStr = "";
    	List<String> strs = new ArrayList<String>();
    	for(SqlExpr expr : node.getConditions()) {
    		strs.add(sqlExprSerializer.serialize(expr));
    	}
    	restrictionStr = Joiner.on(" AND ").join(strs);
    	
    	if(!restrictionStr.isEmpty()) {
    		restrictionStr = " ON (" + restrictionStr + ")";
    	} else {
    		restrictionStr = " ON (TRUE)";
    	}
    	
    	writer.println(restrictionStr);
    	
    	
    	//writer.print(") AS " + node.getAliasName());

    	
    	//String result = left + " " + node.getLeft().getAliasName() + " JOIN " + right + " " + node.getRight().getAliasName() + restrictionStr;
    	//String result = left + joinOp + right + restrictionStr;
    	//String result = left + joinOp + right + restrictionStr;
    	//return result;
    }

    public static void _asString(SqlTable node, IndentedWriter writer)
    {
    	writer.print(node.getTableName());
    	writer.print(getAliasName(node));
    }
}