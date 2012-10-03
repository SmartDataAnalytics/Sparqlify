package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_SqlColumnRef;
import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;
import org.aksw.sparqlify.algebra.sql.nodes.Projection;
import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpExtend;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpFilter;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpProject;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpRename;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;


class JoinContext {
	// The effective op, i.e. table, query, join or union.
	private SqlOp op;
	private ExprList conditions = new ExprList();
	private Projection projection = new Projection();

	
	public JoinContext(SqlOp op) {
		this.op = op;
	}

	public SqlOp getOp() {
		return op;
	}

	public ExprList getConditions() {
		return conditions;
	}

	public Projection getProjection() {
		return projection;
	}
}


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
	
	
	public static SqlOp _makeSelect(SqlOp sqlOp) {
		SqlOp result = MultiMethod.invokeStatic(SqlOpSelectBlockCollector.class, "makeSelect", sqlOp);
		
		return result;
	}

	
	public static SqlOp makeSelect(SqlOpUnionN op) {
		
		List<SqlOp> newMembers = new ArrayList<SqlOp>();
		for(SqlOp member : op.getSubOps()) {
			SqlOp subOp = _makeSelect(member);
			newMembers.add(subOp);
		}
		
		String aliasName = aliasGenerator.next();
		SqlOpUnionN result = new SqlOpUnionN(op.getSchema(), newMembers, aliasName); // makeSelectOrTable(op);
		
		//SqlOpSelectBlock result = SqlOpSelectBlock.create(opTable);

		/*
		for(String columnName : op.getSchema().getColumnNames())  {

			//SqlDatatype datatype = result.getSchema().getColumnType(columnName);			
			//result.getProjection().put(columnName, new SqlExprColumn(opTable.getAliasName(), columnName, datatype)); //ExprVar(aliasName + "." + columnName));

			result.getProjection().put(columnName, new ExprVar(opTable.getAliasName() + "." + columnName));
		}
		*/

		
		return result;
		
	}
	
	public static SqlOp makeSelect(SqlOpTable node) {

		SqlOpTable opTable = makeSelectOrTable(node);
		
		SqlOpSelectBlock result = SqlOpSelectBlock.create(opTable);

		for(String columnName : opTable.getSchema().getColumnNames())  {

			//SqlDatatype datatype = result.getSchema().getColumnType(columnName);			
			//result.getProjection().put(columnName, new SqlExprColumn(opTable.getAliasName(), columnName, datatype)); //ExprVar(aliasName + "." + columnName));

			result.getProjection().put(columnName, new ExprVar(opTable.getAliasName() + "." + columnName));
		}

		
		return result;
	}
	
	public static SqlOp makeSelect(SqlOpQuery node) {

		SqlOpQuery result = makeSelectOrTable(node);

		return result;
	}		


	public static SqlOpSelectBlock requireSelectBlock(SqlOp op) {
		SqlOpSelectBlock result = (op instanceof SqlOpSelectBlock) ? (SqlOpSelectBlock) op : SqlOpSelectBlock.create(op);
		
		return result;
	}

	
	public static Expr transformToAliasedReferences(Expr expr, Projection projection) {
		Map<String, Expr> map = projection.getNameToExpr();
		NodeExprSubstitutor substitutor = NodeExprSubstitutor.create(map);
		Expr result = substitutor.transformMM(expr);

		return result;
	}
	
	public static ExprList adjustConditions(ExprList exprs, Projection projection) {
		Map<String, Expr> map = projection.getNameToExpr();
		NodeExprSubstitutor substitutor = NodeExprSubstitutor.create(map);
		ExprList result = substitutor.transformList(exprs);

		return result;
	}
	
	public static SqlOp makeSelect(SqlOpFilter op) {
		
		SqlOp subOp = _makeSelect(op.getSubOp());
		
		SqlOpSelectBlock result = requireSelectBlock(subOp);
		result.setSchema(op.getSchema());
		
		ExprList transformed = adjustConditions(op.getExprs(), result.getProjection());		
		result.getConditions().addAll(transformed);
		
		return result;
	}
	
	public static SqlOp makeSelect(SqlOpExtend op) {
		
		SqlOp subOp = _makeSelect(op.getSubOp());
		
		SqlOpSelectBlock result = requireSelectBlock(subOp);
		result.setSchema(op.getSchema());

		Projection extendedProj = new Projection();
		for(Entry<String, Expr> entry : op.getProjection().getNameToExpr().entrySet()) {
			String columnName = entry.getKey();
			Expr originalExpr = entry.getValue();
			Expr aliasedExpr = transformToAliasedReferences(originalExpr, result.getProjection());
			
			extendedProj.getNames().add(columnName);
			extendedProj.getNameToExpr().put(columnName, aliasedExpr);
		}
		
		result.getProjection().extend(extendedProj);
		
		
		return result;
	}
	
	public static SqlOp makeSelect(SqlOpProject op) {
		
		SqlOp subOp = _makeSelect(op.getSubOp());
		
		SqlOpSelectBlock result = requireSelectBlock(subOp);
		result.setSchema(op.getSchema());
		
		result.getProjection().project(op.getColumnNames());
		
		//result.getProjection().project(op.get$)
		
		//ExprList transformed = adjustConditions(op.getExprs(), result.getProjection());		
		//result.getConditions().addAll(transformed);
		
		return result;
	}

	
	
	public static SqlOp makeSelect(SqlOpJoin op) {

		JoinContext context = collectJoins(op);
		
		SqlOpSelectBlock block = SqlOpSelectBlock.create();
		
		/*
		JoinContext left = _collectJoins(op.getLeft());
		JoinContext right = _collectJoins(op.getRight());
		
		SqlOpJoin join = SqlOpJoin.create(op.getJoinType(), left.getOp(), right.getOp());
		join.getConditions().addAll(op.getConditions());
		
		context.getProjection().add(left.getProjection());
		context.getProjection().add(right.getProjection());
		
		//join.getConditions().addAll(left.getConditions());
		context.getConditions().addAll(left.getConditions());
		context.getConditions().addAll(right.getConditions());
		//copyProjection(join, node);
		 */

		block.setSubOp(context.getOp());
		block.setSchema(op.getSchema());
		block.getProjection().add(context.getProjection());
		block.getConditions().addAll(context.getConditions());
		
		return block;

	}


	public static JoinContext _collectJoins(SqlOp sqlOp) {
		JoinContext result = MultiMethod.invokeStatic(SqlOpSelectBlockCollector.class, "collectJoins", sqlOp);
		
		return result;
	}

	
	public static JoinContext collectJoins(SqlOpJoin op) {
		
		JoinContext left = _collectJoins(op.getLeft());
		JoinContext right = _collectJoins(op.getRight());
		
		SqlOpJoin join = SqlOpJoin.create(op.getJoinType(), left.getOp(), right.getOp());
		join.getConditions().addAll(op.getConditions());

		JoinContext context = new JoinContext(join);

		context.getProjection().add(left.getProjection());
		context.getProjection().add(right.getProjection());
		
		//join.getConditions().addAll(left.getConditions());
		context.getConditions().addAll(left.getConditions());
		context.getConditions().addAll(right.getConditions());
		//copyProjection(join, node);

		return context;		
	}

	public static JoinContext collectJoins(SqlOpFilter op) {

		JoinContext result = _collectJoins(op.getSubOp());
		
		ExprList transformed = adjustConditions(op.getExprs(), result.getProjection());		
		result.getConditions().addAll(transformed);
		
		return result;
	}

	
	/**
	 * 
	 * We need to map new-name to old name, such as:
	 * a1.id -> id
	 *     becomes SELECT a1.id AS id
	 * 
	 * otherwise we could create clashes such as:
	 * id -> a1.id
	 * id -> a2.id
	 * 
	 * Even if we temporarily have
	 * a1.id -> id
	 * a2.id -> id
	 *
	 * there can be a rename
	 * a2.id -> h_1
	 * 
	 * 
	 * @param op
	 * @param context
	 * @return
	 */
	public static JoinContext collectJoins(SqlOpRename op) {

		JoinContext result = _collectJoins(op.getSubOp());
		
		result.getProjection().renameAll(op.getRename());
		
		return result;
	}

	public static void initProjection(Projection projection, Schema schema, String aliasName) {
		for(String oldName : schema.getColumnNames())  {
			
			String newName = oldName;
			
			/*
			if(renames != null) {
				String renamed = renames.get(oldName);
				if(renamed != null) {
					newName = renamed;
				}
			}*/
			
			//SqlDatatype datatype = schema.getColumnType(columnName);			
			//context.getProjection().put(columnName, new SqlExprColumn(aliasName, columnName, datatype)); //ExprVar(aliasName + "." + columnName));
			projection.put(newName, new E_SqlColumnRef(oldName, aliasName));
		}
	}
	
	
	public static JoinContext collectJoins(SqlOpTable op) {
		SqlOpTable table = makeSelectOrTable(op);
		
		JoinContext result = new JoinContext(table);
		
		initProjection(result.getProjection(), op.getSchema(), table.getAliasName());
		
		return result;
	}

	public static JoinContext collectJoins(SqlOpQuery op) {
		SqlOpQuery query = makeSelectOrTable(op);
		
		JoinContext result = new JoinContext(query);
		initProjection(result.getProjection(), op.getSchema(), query.getAliasName());
		
		return result;
	}

	
	

	/*
	public static SqlOpUnionN makeSelectOrTable(SqlOpUnionN op) {
		
		String alias = aliasGenerator.next();
		SqlOpUnionN result = new SqlOpUnionN(op.getSchema(), op.getSubOps(), alias);

		
		return result;
	}
	*/

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

		/*
		SqlOpSelectBlock result = SqlOpSelectBlock.create(opQuery);

		
		for(String columnName : opQuery.getSchema().getColumnNames())  {
			result.getProjection().put(columnName, new ExprVar(opQuery.getAliasName() + "." + columnName));
		}
				
		return result;
		* /
	}
	*/
	
	/*
	public static SqlOpSelectBlock makeSelect(SqlOpFilter node) {
		
		
		
		SqlOpSelectBlock result = _makeSelect(node.getSubOp());
		result.getConditions().addAll(node.getExprs());
		
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
	
	/*
	public static SqlOpSelectBlock makeSelect(SqlOpProject node) {
		SqlOpSelectBlock result = _makeSelect(node.getSubOp());

		// If the node is distinct, or if it already has a projection set,
		// we must create a subselect
		if(result.isDistinct()) { // || result.isResultStar()
			//SqlOpSelectBlock wrapped = new 
			
		}
				
		
		return result;
	}
	

	public static SqlOpSelectBlock makeSelect(SqlOpRename op) {
	
		SqlOpSelectBlock result = _makeSelect(op.getSubOp());

		result.getProjection().renameAll(op.getRename());
		
		return result;		
	}
	
	public static SqlOpSelectBlock makeSelect(SqlOpJoin op) {
		
		SqlOpSelectBlock result = SqlOpSelectBlock.create(op);
		SqlOp subOp = makeSelect(op, result);
		
		result.setSubOp(subOp);
		
		return result;
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
    		* /
    		
    		
    		wrapped.add(argSelect);
    	}
    	    	
    	SqlOpUnionN union = SqlOpUnionN.create(wrapped); //, SqlOpSelectBlock.getAliasName(node));


    	SqlOpSelectBlock result = SqlOpSelectBlock.create(union);
    	
    	return result;
	}
			
	/*
	public static SqlOp _makeSelectOrTable(SqlOp node) {
		return MultiMethod.invokeStatic(SqlOpSelectBlockCollectorOld.class, "makeSelectOrTable", node);		
	}

	public static SqlOp makeSelectOrTable(SqlOpEmpty node) {
		// Should never come here
		return node;
	}

	public static SqlOp makeSelectOrTable(SqlOpProject node) {
		return _makeSelect(node);
	}

	/*
	public static SqlOp makeSelectOrTable(SqlAlias node) {
    	SqlOp tmp = _makeSelectOrTable(node.getSubOp());    	
		
    	SqlAlias result = new SqlAlias(node.getAliasName(), tmp);
    	copyProjection(result, node);
    	
    	return result;
	}
	* /
	
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

	
	/*
	public static SqlOp makeSelectOrTable(SqlOpJoin node) {
		SqlOp left = _makeSelectOrTable(node.getLeft());
		SqlOp right = _makeSelectOrTable(node.getRight());
		
		SqlOpJoin join = SqlOpJoin.create(node.getJoinType(), left, right);
		join.getConditions().addAll(node.getConditions());
		//copyProjection(join, node);

		return join;
	}
	* /
	
	public static SqlOp makeSelectOrTable(SqlOpUnionN node) {
		return makeSelect(node);
	}
	*/
	
}

