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
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSlice;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;


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
public class SqlOpSelectBlockCollectorOps {

	
	public static SqlOpSelectBlock _makeSelect(SqlOp sqlOp) {
		SqlOpSelectBlock result = MultiMethod.invokeStatic(SqlOpSelectBlockCollectorOps.class, "makeSelect", sqlOp);
		
		return result;
	}
	
	/*
	public static SqlOpSelectBlock makeSelect(SqlGroup sqlOp) {
		SqlOpSelectBlock result;
		
		if(sqlOp.getSubOp() instanceof SqlOpSlice) {
			
			SqlOpSelectBlock tmp = _makeSelect(sqlOp.getSubOp());

			result = new SqlOpSelectBlock(tmp.getAliasName(), tmp);
			copyProjection(result, result);

			
		}
		else {
			result = _makeSelect(sqlOp.getSubOp());
		}

		System.err.println("TODO Handle group by vars if present");
		
		return result;
	}
	*/
	
	public static SqlOpSelectBlock makeSelect(SqlOpTable sqlOp) {
		return new SqlOpSelectBlock(sqlOp);
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpQuery sqlOp) {
		return new SqlOpSelectBlock(sqlOp);
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpFilter sqlOp) {
		SqlOpSelectBlock result = _makeSelect(sqlOp.getSubOp());
		result.getConditions().addAll(sqlOp.getExprs());
		
		return result;
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpSlice sqlOp) {
		SqlOpSelectBlock result = _makeSelect(sqlOp.getSubOp());
    	SqlOpSelectBlock.slice(null, result, sqlOp.getOffset(), sqlOp.getLimit());
		
		return result;
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpDistinct sqlOp) {
		SqlOpSelectBlock result = _makeSelect(sqlOp.getSubOp());
		result.setDistinct(true);
		
		return result;
	}
	
	/*
	public static SqlOpSelectBlock makeSelect(SqlAlias sqlOp) {
		
		if(true) {
			SqlOpSelectBlock result = _makeSelect(sqlOp.getSubOp());
			//result.setAliasName(sqlOp.getAliasName());
			
			SqlOpSelectBlock wrap = new SqlOpSelectBlock(sqlOp.getAliasName(), result);
			copyProjection(wrap, result);
			
			return wrap;

			/*
			copyProjection(result, sqlOp);
			result.setAliasName(sqlOp.getAliasName());
			return result;
			* /
		}
		//throw new RuntimeException("Should not come here");
		

    	SqlOp result = _makeSelectOrTable(sqlOp.getSubOp());    	
    	

		SqlOpSelectBlock wrap = new SqlOpSelectBlock(sqlOp.getAliasName(), result);
		copyProjection(wrap, result);
		
		return wrap;
		
		
		/*
		wrap.getSparqlVarToExprs().putAll(sqlOp.getSparqlVarToExprs());
		wrap.getAliasToColumn().putAll(sqlOp.getAliasToColumn());

    	result.setAliasName(sqlOp.getAliasName());
    	
    	return result;
    	* /
	}
	*/
	
	/*
	public static SqlOpSelectBlock makeSelect(SqlOpOrder sqlOp) {
    	SqlOpSelectBlock result = _makeSelect(sqlOp.getSubOp());    	
    	result.getSortConditions().addAll(sqlOp.getConditions());
    	
    	copyProjection(result, sqlOp);
    	
    	return result;
	}
	*/
	
	public static SqlOpSelectBlock makeSelect(SqlOpProject sqlOp) {
		SqlOpSelectBlock result = _makeSelect(sqlOp.getSubOp());

		return result;
		
		// If the sqlOp is distinct, or if it already has a projection set,
		// we must create a subselect
		/*
		if(result.isDistinct()) { // || result.isResultStar()
			//SqlOpSelectBlock wrapped = new 
			
		}
		*/
		
		/*
		SqlOpSelectBlock wrap = new SqlOpSelectBlock(sqlOp.getAliasName(), result);
		wrap.getSparqlVarToExprs().putAll(sqlOp.getSparqlVarToExprs());
		wrap.getAliasToColumn().putAll(sqlOp.getAliasToColumn());
		
		return wrap;
		*/
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpJoin sqlOp) {
		SqlOp join = makeSelectOrTable(sqlOp);
		
		return new SqlOpSelectBlock(join);
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpUnionN sqlOp) {
    	// Wrap all elements of the union
    	List<SqlOp> wrapped = new ArrayList<SqlOp>();
    	for(SqlOp arg : sqlOp.getSubOps()) {
    		SqlOpSelectBlock argSelect = _makeSelect(arg);
    		
    		/*
    		argSelect.getSparqlVarToExprs().clear();
    		argSelect.getAliasToColumn().clear();
    		
    		
    		argSelect.getSparqlVarToExprs().putAll(arg.getSparqlVarToExprs());
    		argSelect.getAliasToColumn().putAll(arg.getAliasToColumn());
    		*/
    		
    		
    		wrapped.add(argSelect);
    	}
    	    	
    	SqlOpUnionN union = SqlOpUnionN.create(wrapped); //, sqlOp.getAliasName());

    	SqlOpSelectBlock result = new SqlOpSelectBlock(union);
    	//copyProjection(result, union);
    	
    	return result;
	}
		
	public static SqlOp _makeSelectOrTable(SqlOp sqlOp) {
		return MultiMethod.invokeStatic(SqlOpSelectBlockCollector.class, "makeSelectOrTable", sqlOp);		
	}

	public static SqlOp makeSelectOrTable(SqlOpEmpty sqlOp) {
		// Should never come here
		return sqlOp;
	}

	public static SqlOp makeSelectOrTable(SqlOpProject sqlOp) {
		return _makeSelect(sqlOp);
	}

	public static SqlOp makeSelectOrTable(SqlOpTable sqlOp) {
		return sqlOp;
	}
	
	public static SqlOp makeSelectOrTable(SqlOpQuery sqlOp) {
		return sqlOp;
	}
	
	/*
	public static SqlOp makeSelectOrTable(SqlAlias sqlOp) {
    	SqlOp tmp = _makeSelectOrTable(sqlOp.getSubOp());    	
		
    	SqlAlias result = new SqlAlias(sqlOp.getAliasName(), tmp);
    	copyProjection(result, sqlOp);
    	
    	return result;
	}
	*/
	
	public static SqlOp makeSelectOrTable(SqlOpFilter sqlOp) {
		return _makeSelect(sqlOp);		
	}

	public static SqlOp makeSelectOrTable(SqlOpSlice sqlOp) {		
		return _makeSelect(sqlOp);
	}

	public static SqlOp makeSelectOrTable(SqlOpDistinct sqlOp) {		
		return _makeSelect(sqlOp);
	}

	
	public static SqlOp makeSelectOrTable(SqlOpJoin sqlOp) {
		SqlOp left = _makeSelectOrTable(sqlOp.getLeft());
		SqlOp right = _makeSelectOrTable(sqlOp.getRight());
		
		SqlOpJoin join = SqlOpJoin.create(sqlOp.getJoinType(), left, right);
		join.getConditions().addAll(sqlOp.getConditions());
		//copyProjection(join, sqlOp);

		return join;
	}
	
	public static SqlOp makeSelectOrTable(SqlOpUnionN sqlOp) {
		return makeSelect(sqlOp);
	}
	
}

