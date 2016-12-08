package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpDistinct;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpFilter;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpProject;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpRename;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSlice;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;

import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sdb.core.Gensym;
import org.apache.jena.sparql.expr.ExprVar;


/**
 * Pitfalls:
 *     (distinct (project ...)) -> First project then make distinct
 *         -> Select Distinct [project]
 *     But
 *     (project (distinct ...)) -> First make distinct (with potentially some different projection),
 *     then project:
 *     Select [projection] FROM (Select Distinct * From ...)    
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlOpSelectBlockCollector {

	private static Generator aliasGenerator = Gensym.create("a");
	
	
	/*
	public static boolean isModifier(SqlOp sqlOp) {
		
		
	}
	*/
	
	public static SqlOpSelectBlock _makeSelect(SqlOp sqlOp) {
		SqlOpSelectBlock result = MultiMethod.invokeStatic(SqlOpSelectBlockCollector.class, "makeSelect", sqlOp, null);
		
		return result;
	}

	public static SqlOpSelectBlock _makeSelect(SqlOp sqlOp, SqlOpSelectBlock context) {
		SqlOpSelectBlock result = MultiMethod.invokeStatic(SqlOpSelectBlockCollector.class, "makeSelect", sqlOp, null);
		
		return result;
	}

	/*
	public static SqlOpSelectBlock makeSelect(SqlGroup node) {
		SqlOpSelectBlock result;
		
		if(node.getSubOp() instanceof SqlOpSlice) {
			
			SqlOpSelectBlock tmp = _makeSelect(node.getSubOp());

			result = new SqlOpSelectBlock(tmp.getAliasName(), tmp);
			copyProjection(result, result);

			
		}
		else {
			result = _makeSelect(node.getSubOp());
		}

		System.err.println("TODO Handle group by vars if present");
		
		return result;
	}
	*/
	
	public static SqlOpSelectBlock makeSelect(SqlOpTable node) {

		SqlOpTable opTable = makeSelectOrTable(node);
		
		SqlOpSelectBlock result = SqlOpSelectBlock.create(opTable);

		for(String columnName : opTable.getSchema().getColumnNames())  {
			result.getProjection().put(columnName, new ExprVar(opTable.getAliasName() + "." + columnName));
		}

		
		return result;
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpQuery node) {

		SqlOpQuery opQuery = makeSelectOrTable(node);

		
		SqlOpSelectBlock result = SqlOpSelectBlock.create(opQuery);

		
		for(String columnName : opQuery.getSchema().getColumnNames())  {
			result.getProjection().put(columnName, new ExprVar(opQuery.getAliasName() + "." + columnName));
		}
				
		return result;
	}
	
	public static SqlOpSelectBlock requireContext(SqlOp node, SqlOpSelectBlock context) {
		if(context != null) {
			return context;
		}
		
		SqlOpSelectBlock result = SqlOpSelectBlock.create(node);
		return result;
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpFilter op) {
		
		SqlOp effectiveOp = _makeSelect(op.getSubOp());
		
//		SqlOpSelectBlock result = null; requireSelectBlock(effectiveOp, op);
				
		result.getConditions().addAll(op.getExprs());
		
		return result;
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpSlice node) {
		SqlOpSelectBlock result = _makeSelect(node.getSubOp());
    	SqlOpSelectBlock.slice(null, result, node.getOffset(), node.getLimit());
		
		return result;
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpDistinct node) {
		SqlOpSelectBlock result = _makeSelect(node.getSubOp());
		result.setDistinct(true);
		
		return result;
	}
	
	/*
	public static SqlOpSelectBlock makeSelect(SqlAlias node) {
		
		if(true) {
			SqlOpSelectBlock result = _makeSelect(node.getSubOp());
			//result.setAliasName(node.getAliasName());
			
			SqlOpSelectBlock wrap = new SqlOpSelectBlock(node.getAliasName(), result);
			copyProjection(wrap, result);
			
			return wrap;

			/*
			copyProjection(result, node);
			result.setAliasName(node.getAliasName());
			return result;
			* /
		}
		//throw new RuntimeException("Should not come here");
		

    	SqlOp result = _makeSelectOrTable(node.getSubOp());    	
    	

		SqlOpSelectBlock wrap = new SqlOpSelectBlock(node.getAliasName(), result);
		copyProjection(wrap, result);
		
		return wrap;
		
		
		/*
		wrap.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
		wrap.getAliasToColumn().putAll(node.getAliasToColumn());

    	result.setAliasName(node.getAliasName());
    	
    	return result;
    	* /
	}
	*/

	
	/*
	public static SqlOpSelectBlock makeSelect(SqlOpOrder node) {
    	SqlOpSelectBlock result = _makeSelect(node.getSubOp());    	
    	result.getSortConditions().addAll(node.getConditions());
    	
    	copyProjection(result, node);
    	
    	return result;
	}
	*/
	
	public static SqlOpSelectBlock makeSelect(SqlOpProject node) {
		SqlOpSelectBlock result = _makeSelect(node.getSubOp());

		// If the node is distinct, or if it already has a projection set,
		// we must create a subselect
		if(result.isDistinct()) { // || result.isResultStar()
			//SqlOpSelectBlock wrapped = new 
			
		}
				
		
		return result;
	}

//	public static priority
	
	
	public static SqlOpSelectBlock makeSelect(SqlOpRename op) {
	
		SqlOp tmp = _makeSelectOrTable(op.getSubOp());

		SqlOpSelectBlock result = requireSelectBlock(tmp, op);
		
		result.getProjection().renameAll(op.getRename());
		
		return result;		
	}

	
	public static SqlOpSelectBlock makeSelect(SqlOpJoin node, SqlOpSelectBlock context) {
		
		SqlOpSelectBlock result = requireContext(node, context);
		
		
		//SqlOp join = makeSelectOrTable(node, result);
		
		return new SqlOpSelectBlock(null);
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpUnionN node) {
    	// Wrap all elements of the union
    	List<SqlOp> wrapped = new ArrayList<SqlOp>();
    	for(SqlOp arg : node.getSubOps()) {
    		SqlOpSelectBlock argSelect = _makeSelect(arg);
    		
    		/*
    		argSelect.getSparqlVarToExprs().clear();
    		argSelect.getAliasToColumn().clear();
    		
    		
    		argSelect.getSparqlVarToExprs().putAll(arg.getSparqlVarToExprs());
    		argSelect.getAliasToColumn().putAll(arg.getAliasToColumn());
    		*/
    		
    		
    		wrapped.add(argSelect);
    	}
    	    	
    	SqlOpUnionN union = SqlOpUnionN.create(wrapped); //, SqlOpSelectBlock.getAliasName(node));


    	SqlOpSelectBlock result = SqlOpSelectBlock.create(union);
    	
    	return result;
	}
		
	public static SqlOp _makeSelectOrTable(SqlOp node) {
		return MultiMethod.invokeStatic(SqlOpSelectBlockCollector.class, "makeSelectOrTable", node);		
	}

	public static SqlOp makeSelectOrTable(SqlOpEmpty node) {
		throw new RuntimeException("Empty SQL node");
		// Should never come here
		return node;
	}

	public static SqlOp makeSelectOrTable(SqlOpProject node) {
		return _makeSelect(node);
	}

	public static SqlOpTable makeSelectOrTable(SqlOpTable node) {
		
		String alias = aliasGenerator.next();
		SqlOpTable result = new SqlOpTable(node.getSchema(), node.getTableName(), alias);
		
		return result;
	}
	
	public static SqlOpQuery makeSelectOrTable(SqlOpQuery node) {
		String alias = aliasGenerator.next();
		
		SqlOpQuery result = new SqlOpQuery(node.getSchema(), node.getQueryString(), alias);

		return result;
	}

	/*
	public static SqlOp makeSelectOrTable(SqlAlias node) {
    	SqlOp tmp = _makeSelectOrTable(node.getSubOp());    	
		
    	SqlAlias result = new SqlAlias(node.getAliasName(), tmp);
    	copyProjection(result, node);
    	
    	return result;
	}
	*/
	
	public static SqlOp makeSelectOrTable(SqlOpFilter node) {
		return _makeSelect(node);		
	}

	public static SqlOp makeSelectOrTable(SqlOpSlice node) {		
		return _makeSelect(node);
	}

	public static SqlOp makeSelectOrTable(SqlOpDistinct node) {		
		return _makeSelect(node);
	}

	public static SqlOp makeSelectOrTable(SqlOpRename op) {		
		return _makeSelect(op);
	}

	
	public static SqlOp makeSelectOrTable(SqlOpJoin node) {
		SqlOp left = _makeSelectOrTable(node.getLeft());
		SqlOp right = _makeSelectOrTable(node.getRight());
		
		SqlOpJoin join = SqlOpJoin.create(node.getJoinType(), left, right);
		join.getConditions().addAll(node.getConditions());
		//copyProjection(join, node);

		return join;
	}
	
	public static SqlOp makeSelectOrTable(SqlOpUnionN node) {
		return makeSelect(node);
	}
	
}

