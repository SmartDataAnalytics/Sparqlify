package org.aksw.sparqlify.core.algorithms;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.Projection;
import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpJoinN;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;
import org.aksw.sparqlify.algebra.sql.nodes.SqlSortCondition;
import org.aksw.sparqlify.algebra.sql.nodes.SqlUnion;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.Query;
import org.apache.jena.sdb.core.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;



public class SqlOpSerializerImpl
	implements SqlOpSerializer
{
	private static final Logger logger = LoggerFactory.getLogger(SqlOpSerializerImpl.class);

	private SqlExprSerializer exprSerializer; //new SqlExprSerializerMySql();
	//private static SqlExprSerializer sqlExprSerializer = new SqlExprSerializerPostgres();


	public SqlOpSerializerImpl(SqlExprSerializer exprSerializer) {
		this.exprSerializer = exprSerializer;
	}

	// TODO The castFactory should most likely be part of the exprSerializer - we do not have to cast algebra ops.
	//private static DatatypeToStringPostgres castFactory = new DatatypeToStringPostgres();

	public String serialize(SqlOp op) {

		//SqlAlgebraToString transformer = new SqlAlgebraToString();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IndentedWriter writer = new IndentedWriter(out);

		serialize(op, writer);
		writer.flush();
		writer.close();

		String result = SparqlifyUtils.toUtf8String(out);
		return result;
	}

	public void serialize(SqlOp op, IndentedWriter writer) {


		//return (String)
		MultiMethod.invoke(this, "_serialize", op, writer);
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

	public String projection(Projection projection) {
		String result = projection(projection.getNames(), projection.getNameToExpr());

		return result;
	}

	public String projection(List<String> columnNames, Map<String, SqlExpr> map)
	{
		//System.out.println("Project column names: " + columnNames);

		// Empty projections can occur if a query response is determined by static triples
		if(columnNames.isEmpty()) {
			return "true";
		}

    	String result = "";

    	List<String> strs = new ArrayList<String>();

    	// When writing the projection as an SQL string, the column names will be sorted
    	// in order to make sure that the projections within unions are correctly aligned.
    	//SortedSet<String> columnNames = new TreeSet<String>(map.keySet());
    	for(String columnName : columnNames) {
    	//for(Entry<String, SqlExpr> entry : map.entrySet()) {

    		SqlExpr value = map.get(columnName);
    		//String keyStr = entry.getKey();
    		String exprStr = "";


    		if(value != null) {
    			exprStr = exprSerializer.serialize(value); //sqlExpr.asSQL() + " ";
    		}

    		//String asSeparator = " AS ";
    		String asSeparator = " ";

    		strs.add(exprStr + asSeparator + escapeAlias(columnName));
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



	public void _serialize(SqlOpEmpty node, IndentedWriter writer) {

		writer.print("(SELECT ");

		Map<String, SqlExpr> proj = new HashMap<String, SqlExpr>();
		SqlExpr sqlExpr = S_Constant.create(new SqlValue(TypeToken.Int, null));

		Schema schema = node.getSchema();
		List<String> columnNames = schema.getColumnNames();
		for(String columnName : columnNames) {
			proj.put(columnName, sqlExpr);
		}

		String projStr = projection(columnNames, proj);
		writer.print(projStr);

		writer.print(" WHERE FALSE)");


		if(node.getAliasName() != null) {
			writer.print(" " + node.getAliasName());
		}
	}


	public void _serializeOld(SqlOpEmpty node, IndentedWriter writer) {
		writer.print("(SELECT NULL WHERE FALSE)");

		if(node.getAliasName() != null) {
			writer.print(" " + node.getAliasName());
		}

		//writer.print("EMPTY_SQL_NODE");
	}



	public void _serialize(SqlOpJoinN node, IndentedWriter writer) {
		List<SqlOp> subOps = node.getSubOps();

		boolean isFirst = true;
		for(SqlOp subOp : subOps) {
			if(!isFirst) {
				writer.println(", ");
			}

			isFirst = false;

			boolean isSubSelect = subOp instanceof SqlOpSelectBlock;
            boolean isUnion = subOp instanceof SqlOpUnionN;

            boolean needsGrouping = isSubSelect || isUnion;

			if(needsGrouping) {
			    writer.print("(");
			    writer.incIndent();
			}

			serialize(subOp, writer);

			if(needsGrouping) {
                writer.decIndent();
                writer.print(") " + SqlOpSelectBlock.getAliasName(subOp));
            }

		}
		//writer.print("EMPTY_SQL_NODE");
	}


	public void _serialize(SqlNodeEmpty node, IndentedWriter writer) {
		writer.print("EMPTY_SQL_NODE");
	}


	public void _serialize(SqlOpQuery node, IndentedWriter writer)
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




	public static String getAliasNameNotNull(SqlOp op) {

		String aliasName = SqlOpSelectBlock.getAliasName(op);

		if(aliasName == null || aliasName.isEmpty()) {
			return "";
		} else {
			return " " + aliasName;
		}
	}

    public void _serialize(SqlOpSelectBlock op, IndentedWriter writer)
    {
    	writer.print("SELECT ");

    	// Distinct
    	//String distinctStr = "";
    	if(op.isDistinct()) {
    		writer.print("DISTINCT ");
    		//distinctStr += " DISTINCT";
    	}

    	// Projection
    	String projectionStr = projection(op.getProjection());
    	writer.println(projectionStr);

    	writer.println("FROM");

    	boolean needsGrouping = op.getSubOp() instanceof SqlOpUnionN || op.getSubOp() instanceof SqlOpSelectBlock;// || op.getSubOp() instanceof SqlOpQuery;

    	if(needsGrouping) {
    		writer.print("(");
    	}


    	writer.incIndent();
    	// Joins

    	serialize(op.getSubOp(), writer);
    	writer.decIndent();

    	if(needsGrouping) {
    		String aliasName = getAliasNameNotNull(op.getSubOp());
    		writer.print(")" + aliasName);
    	}


    	if(!writer.atLineStart()) {
    	//	writer.println();
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
	    	for(SqlExpr expr : op.getConditions()) {
	    		if(expr == null) {
	    			logger.error("Null expression in: " + op);
	    			continue;
	    		}

	    		String str = exprSerializer.serialize(expr);

	    		assert str != null : "An expression was serialized as null: " + expr;

	    		//String str = expr.asSQL();

	    		strs.add(str);
	    	}

	    	if(!strs.isEmpty()) {
	    		writer.println();
	    		writer.print("WHERE ");
	    		//selectionStr += " WHERE ";
	    	}

	    	writer.println(Joiner.on(" AND ").join(strs));
	    	//selectionStr += Joiner.on(" AND ").join(strs);
    	}


    	List<String> groupByExprStrs = new ArrayList<String>();
    	for(SqlExpr groupByExpr : op.getGroupByExprs()) {
    		String exprStr = exprSerializer.serialize(groupByExpr);

    		groupByExprStrs.add(exprStr);
    	}
    	if(!groupByExprStrs.isEmpty()) {
        	String groupByStr = "GROUP BY " + Joiner.on(", ").join(groupByExprStrs);
    		writer.println(groupByStr);
    	}



		List<String> sortColumnExprStrs = new ArrayList<String>();
    	for(SqlSortCondition condition : op.getSortConditions()) {
    		String dirStr = null;
    		if(condition.getDirection() == Query.ORDER_ASCENDING) {
    			dirStr = "ASC";
    		} else if(condition.getDirection() == Query.ORDER_DESCENDING) {
    			dirStr = "DESC";
    		}


    		// TODO This is not working properly: If a sparql variable is made up
    		// from multiple sql columns, we need to settle for an ordering -
    		// right now we get: c1 OR c2 OR ... cn
    		String exprStr = exprSerializer.serialize(condition.getExpression());

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
    	if(op.getLimit() != null) {
    		writer.println("LIMIT " + op.getLimit());
    		//limitStr = " LIMIT " + node.getLimit();
    	}


    	//String offsetStr = "";
    	if(op.getOffset() != null) {
    		writer.println("OFFSET " + op.getOffset());
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
    public static String _serialize(SqlDisjunction node) {


    	String left =  serialize(node.getLeft());
    	String right = serialize(node.getRight());

    	String result = left + " UNION " + right;
    	return result;
    }*/

    public void _serialize(SqlUnion node, IndentedWriter writer) {
    	throw new RuntimeException("SqlUnion is deprecated. Use SqlUnionN instead.");
    	/*
    	String left =  serialize(node.getLeft());
    	String right = serialize(node.getRight());

    	String result = left + " UNION ALL " + right;
    	return result;
    	*/
    }

    public void _serialize(SqlOpUnionN op, IndentedWriter writer) {
    	//writer.println("(");
		writer.incIndent();

    	List<String> parts = new ArrayList<String>();

    	List<SqlOp> members = op.getSubOps();
    	for(int i = 0; i < members.size(); ++i) {
    		SqlOp arg = members.get(i);
    	//for(SqlNode arg : node.getArgs()) {

    		//String part = "SELECT " + projection(arg.getColumnToSqlExpr()) + " FROM " + serialize(arg) + " " + arg.getAliasName() + "";
    		//String sub = serialize(arg);

    		boolean needsGrouping = false;
    		if(arg instanceof SqlOpSelectBlock) {
    		    SqlOpSelectBlock block = (SqlOpSelectBlock)arg;
    		    needsGrouping = block.getLimit() != null || block.getOffset() != null || !block.getSortConditions().isEmpty() || !block.getGroupByExprs().isEmpty();
    		}

            if(needsGrouping) {
                writer.print("(");
                writer.newline();
            }

    		writer.incIndent();

    		serialize(arg, writer);

            writer.decIndent();

            if(needsGrouping) {
                writer.print(")");
                writer.newline();
            }


    		if(i != op.getSubOps().size() - 1) {
    			writer.println("UNION ALL");
    		}

    		/*
    		if(arg instanceof SqlOpQuery) {
    			String innerAlias = ((SqlOpQuery) arg).getInnerAlias();
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
    public static String _serialize(SqlOpJoinN node) {
    	List<String> parts = new ArrayList<String>();
    	for(SqlNode arg : node.getArgs()) {
    		parts.add(serialize(arg));
    	}

    	String result = Joiner.on(" JOIN ").join(parts);
    	return result;
    }*/


    public void serializeJoinU(SqlOp op, String aliasName, IndentedWriter writer) {
    	//serialize(op, writer);



    	//boolean isSubSelect = node instanceof SqlSelectBlock || node instanceof SqlUnionN;
    	boolean isSubSelect = op instanceof SqlOpSelectBlock || op instanceof SqlOpUnionN;

    	if(isSubSelect) {
    		writer.println("(");
    		writer.incIndent();
    	}

    	serialize(op, writer);

    	if(isSubSelect) {
    		writer.decIndent();
    		if(!writer.atLineStart()) {
    			writer.println();
    		}
			writer.print(") " + aliasName);
    	}
    }



    public void _serialize(SqlOpJoin op, IndentedWriter writer) {
    	//throw new RuntimeException("SqlUnion is deprecated. Use SqlUnionN instead.");

    	//writer.print("(");

    	serializeJoinU(op.getLeft(), SqlOpSelectBlock.getAliasName(op.getLeft()), writer);
    	//serializeJoinU(node.getLeft(), node.getLeftAlias(), writer);

    	//writer.print(") AS " + node.getLeft().getAliasName());

    	String restrictionStr = "";
    	List<String> strs = new ArrayList<String>();
    	for(SqlExpr expr : op.getConditions()) {
    		strs.add(exprSerializer.serialize(expr));
    	}
    	restrictionStr = Joiner.on(" AND ").join(strs);

    	if(!restrictionStr.isEmpty()) {
    		restrictionStr = " ON (" + restrictionStr + ")";
    	} else {
        	if(op.getJoinType().equals(JoinType.LEFT)) {
        		//writer.println(" ON (TRUE) ");
        		restrictionStr = " ON (TRUE)";
        	}

    		//restrictionStr = " ON (TRUE)";
    		//restrictionStr = "";
    	}


    	String joinOp = "";

    	if(op.getJoinType().equals(JoinType.INNER)) {
        	if(strs.isEmpty()) {
        		joinOp = ",";
        		writer.println(joinOp);
        	} else {
        		joinOp = strs.isEmpty() ? ", " : "JOIN ";
        		writer.println();
            	writer.print(joinOp);
        	}
    	} else if(op.getJoinType().equals(JoinType.LEFT)) {
    		joinOp = "LEFT JOIN ";
    		writer.println();
        	writer.print(joinOp);
    	} else {
    		throw new RuntimeException("Join type not supported");
    	}


    	//writer.print("(");

    	serializeJoinU(op.getRight(), SqlOpSelectBlock.getAliasName(op.getRight()), writer);
    	//serializeJoinU(node.getRight(), node.getRightAlias(), writer);

    	//writer.print(") AS " + node.getRight().getAliasName());

    	if(!restrictionStr.isEmpty()) {
    		writer.println(restrictionStr);
    	}

    	//writer.print(") AS " + node.getAliasName());


    	//String result = left + " " + node.getLeft().getAliasName() + " JOIN " + right + " " + node.getRight().getAliasName() + restrictionStr;
    	//String result = left + joinOp + right + restrictionStr;
    	//String result = left + joinOp + right + restrictionStr;
    	//return result;
    }

    public static void _serialize(SqlOpTable op, IndentedWriter writer)
    {
    	String encTableName = "\"" + op.getTableName() + "\"";
    	writer.print(encTableName);
    	writer.print(getAliasNameNotNull(op));
    }

    /*
	@Override
	public String serialize(SqlOp op) {
		// TODO Auto-generated method stub
		return null;
	}*/
}
