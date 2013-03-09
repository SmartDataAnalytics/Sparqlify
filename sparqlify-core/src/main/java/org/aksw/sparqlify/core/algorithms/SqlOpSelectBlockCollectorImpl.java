package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.Projection;
import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpDistinct;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpExtend;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpFilter;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpGroupBy;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpProject;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpRename;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSlice;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.interfaces.SqlOpSelectBlockCollector;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;

interface JoinContext {
	SqlOp getOp();
	List<SqlExpr> getConditions();
	Projection getProjection();
}

/*
class JoinContextBlock
	implements JoinContext
{
	private SqlOpSelectBlock block;
	//private List<SqlExpr> conditions;
	
	public JoinContextBlock(SqlOpSelectBlock block) {
		this.block = block;
	}
	
	@Override
	public SqlOpSelectBlock getOp() {
		return block;
	}

	@Override
	public List<SqlExpr> getConditions() {
		return block.getConditions();
	}

	@Override
	public Projection getProjection() {
		return block.getProjection();
	}
}
*/

class JoinContextJoin
	implements JoinContext
{
	// The effective op, i.e. table, query, join or union.
	private SqlOp op;
	private List<SqlExpr> conditions = new ArrayList<SqlExpr>();
	private Projection projection = new Projection();

	
	public JoinContextJoin(SqlOp op) {
		this.op = op;
	}

	public SqlOp getOp() {
		return op;
	}

	public List<SqlExpr> getConditions() {
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
public class SqlOpSelectBlockCollectorImpl
	implements SqlOpSelectBlockCollector

{

	private static Generator aliasGenerator = Gensym.create("a");
	
	
	/**
	 * Turn an SqlOp into an OpSqlSelectBlock.
	 * Exception is SqlOpUnion, which does not need to be wrapped as such block.
	 * 
	 * 
	 * @param sqlOp
	 * @return
	 */
	public static SqlOp _makeSelect(SqlOp sqlOp) {
		SqlOp result = MultiMethod.invokeStatic(SqlOpSelectBlockCollectorImpl.class, "makeSelect", sqlOp);
		
		return result;
	}
	
	/**
	 * Create a dummy select query:
	 * 
	 * SELECT NULL WHERE FALSE;
	 * 
	 * If above is not cross db safe, we could change to:
	 * SELECT NULL c FROM (SELECT NULL) t WHERE FALSE;
	 * 
	 * @param op
	 * @return
	 */
	public static SqlOp makeSelect(SqlOpEmpty op) {
		SqlOpSelectBlock result = requireSelectBlock(op);

		return result;
	}
	

	public static SqlOp makeSelect(SqlOpGroupBy op) {
		SqlOp newOp = _makeSelect(op.getSubOp());
		SqlOpSelectBlock result = requireSelectBlock(newOp);
		
		List<SqlExpr> newExprs = adjustConditions(op.getGroupByExprs(), result.getProjection());
		
		result.getGroupByExprs().addAll(newExprs);
		
		
		return result;

	}

	
	public static SqlOp makeSelect(SqlOpSlice op) {
		SqlOp newOp = _makeSelect(op.getSubOp());
		SqlOpSelectBlock result = requireSelectBlock(newOp);
		
		
		SqlOpSelectBlock.slice(result, op.getOffset(), op.getLimit());
		
		return result;
	}
	
	public static SqlOp makeSelect(SqlOpDistinct op) {
		SqlOp newOp = _makeSelect(op.getSubOp());
		SqlOpSelectBlock result = requireSelectBlock(newOp);
		result.setDistinct(true);
		
		return result;
	}
	
	public static SqlOpUnionN makeSelect(SqlOpUnionN op) {
		
		List<SqlOp> newMembers = new ArrayList<SqlOp>();
		for(SqlOp member : op.getSubOps()) {
			SqlOp subOp = _makeSelect(member);
			newMembers.add(subOp);
		}
		
		String aliasName = aliasGenerator.next();
		SqlOpUnionN result = new SqlOpUnionN(op.getSchema(), newMembers, aliasName); // makeSelectOrTable(op);
		
		//SqlOpUnionN result = SqlOpUnionN.create(newMembers, aliasName);
		

		//SqlOpSelectBlock result = SqlOpSelectBlock.create(opTable);

		/*
		for(String columnName : op.getSchema().getColumnNames())  {

			//XClass datatype = result.getSchema().getColumnType(columnName);			
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

			TypeToken datatype = result.getSchema().getColumnType(columnName);			
			result.getProjection().put(columnName, new S_ColumnRef(datatype, columnName, opTable.getAliasName())); //ExprVar(aliasName + "." + columnName));

			//result.getProjection().put(columnName, new ExprVar(opTable.getAliasName() + "." + columnName));
		}

		
		return result;
	}
	
	public static SqlOp makeSelect(SqlOpQuery node) {

		SqlOpQuery result = makeSelectOrTable(node);

		return result;
	}		


	public static SqlOpSelectBlock requireSelectBlock(SqlOp op) {
		SqlOpSelectBlock result;
		if(op instanceof SqlOpSelectBlock) {
			result = (SqlOpSelectBlock) op;
		} else {
			result = SqlOpSelectBlock.create(op);
			initProjection(result.getProjection(), op.getSchema(), result.getAliasName());
		}
		
		return result;
	}

	/*
	public static SqlOpSelectBlock requireSelectBlock(SqlOp op, String aliasName) {
		SqlOpSelectBlock result = (op instanceof SqlOpSelectBlock) ? (SqlOpSelectBlock) op : SqlOpSelectBlock.create(op, aliasName);
		
		return result;
	}*/


	public static SqlExpr transformToAliasedReferences(SqlExpr expr, Projection projection) {
		
		Map<String, SqlExpr> map = projection.getNameToExpr();
		SqlExprSubstitutor substitutor = SqlExprSubstitutor.create(map);
		SqlExpr result = substitutor.substitute(expr);
		
		return result;
	}
	
	public static List<SqlExpr> adjustConditions(List<SqlExpr> exprs, Projection projection) {
		Map<String, SqlExpr> map = projection.getNameToExpr();
		//SqlExprSubstitute x = new SqlEx

		SqlExprSubstitutor substitutor = SqlExprSubstitutor.create(map);
		List<SqlExpr> result = substitutor.substitute(exprs);

		return result;
	}
	
	public static SqlOp makeSelect(SqlOpFilter op) {
		
		SqlOp subOp = _makeSelect(op.getSubOp());
		
		SqlOpSelectBlock result = requireSelectBlock(subOp);
		result.setSchema(op.getSchema());
		
		List<SqlExpr> transformed = adjustConditions(op.getExprs(), result.getProjection());		
		result.getConditions().addAll(transformed);
		
		return result;
	}
	
	public static SqlOp makeSelect(SqlOpExtend op) {
		
		SqlOp subOp = _makeSelect(op.getSubOp());
		
		SqlOpSelectBlock result = requireSelectBlock(subOp);
		result.setSchema(op.getSchema());

		Projection extendedProj = new Projection();
		for(Entry<String, SqlExpr> entry : op.getProjection().getNameToExpr().entrySet()) {
			String columnName = entry.getKey();
			SqlExpr originalExpr = entry.getValue();
			SqlExpr aliasedExpr = transformToAliasedReferences(originalExpr, result.getProjection());
			
			extendedProj.getNames().add(columnName);
			extendedProj.getNameToExpr().put(columnName, aliasedExpr);
		}
		
		result.getProjection().extend(extendedProj);
		
		
		return result;
	}
	
	public static SqlOp makeSelect(SqlOpProject op) {
		
		SqlOp subOp = _makeSelect(op.getSubOp());
		
		SqlOpSelectBlock result = requireSelectBlock(subOp);
		
		SqlOp effectiveOp = result.getSubOp();
		if(effectiveOp instanceof SqlOpUnionN) {
			initProjection(result.getProjection(), effectiveOp.getSchema(), SqlOpSelectBlock.getAliasName(effectiveOp));			
		}

		
		result.setSchema(op.getSchema());
		
		result.getProjection().project(op.getColumnNames());
		
		//result.getProjection().project(op.get$)
		
		//List<SqlExpr> transformed = adjustConditions(op.getExprs(), result.getProjection());		
		//result.getConditions().addAll(transformed);
		
		return result;
	}

	
	public static SqlOpSelectBlock contextToBlock(Schema schema, JoinContext context) {
		SqlOpSelectBlock block = SqlOpSelectBlock.create();
		
		/*
		JoinContext left = _$s(op.getLeft());
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
		//block.setSchema(op.getSchema());
		block.setSchema(schema);
		block.getProjection().add(context.getProjection());
		block.getConditions().addAll(context.getConditions());
		
		return block;
		
	}
	
	public static SqlOp makeSelect(SqlOpJoin op) {

		JoinContext context = collectJoins(op);
		SqlOpSelectBlock block = contextToBlock(op.getSchema(), context);

		return block;
	}


	public static JoinContext _collectJoins(SqlOp sqlOp) {
		JoinContext result = MultiMethod.invokeStatic(SqlOpSelectBlockCollectorImpl.class, "collectJoins", sqlOp);
		
		return result;
	}

	// TODO SqlOpEmpty needs an alias
	public static JoinContext collectJoins(SqlOpEmpty op) {
		SqlOpEmpty table = makeSelectOrTable(op);		
		JoinContextJoin result = new JoinContextJoin(table);		
		initProjection(result.getProjection(), op.getSchema(), table.getAliasName());
		
		return result;		
	}
	
	public static JoinContext collectJoins(SqlOpJoin op) {
		
		JoinContext left = _collectJoins(op.getLeft());
		JoinContext right = _collectJoins(op.getRight());
		
		SqlOpJoin join = SqlOpJoin.create(op.getJoinType(), left.getOp(), right.getOp());
		join.getConditions().addAll(op.getConditions());

		JoinContextJoin context = new JoinContextJoin(join);

		context.getProjection().add(left.getProjection());
		context.getProjection().add(right.getProjection());
		
		//join.getConditions().addAll(left.getConditions());
		context.getConditions().addAll(left.getConditions());
		context.getConditions().addAll(right.getConditions());
		//copyProjection(join, node);

		return context;		
	}


	public static JoinContext collectJoins(SqlOpUnionN op) {
		SqlOpUnionN newOp = makeSelect(op);

		//SqlOpSelectBlock resultOp = SqlOpSelectBlock.create(newOp, newOp.getAliasName());		
		//initProjection(resultOp.getProjection(), op.getSchema(), resultOp.getAliasName());
		SqlOpUnionN resultOp = newOp;
		
		////SqlOp resultOp = requireSelectBlock(newOp);
	
		//JoinContext result = new JoinContextJoin(resultOp);
		JoinContext result = new JoinContextJoin(newOp);
		initProjection(result.getProjection(), op.getSchema(), resultOp.getAliasName());
		return result;
	}
	
	public static JoinContext collectJoins(SqlOpFilter op) {

		JoinContext result = _collectJoins(op.getSubOp());
		
		List<SqlExpr> transformed = adjustConditions(op.getExprs(), result.getProjection());		
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

		JoinContext result;

		boolean useCodeThatCausesConflictsOnDuplicateNames = false;

		if (useCodeThatCausesConflictsOnDuplicateNames) {
			// FIXME: Can be removed it seems; the other part seems to be working now
			result = _collectJoins(op.getSubOp());		
			result.getProjection().renameAll(op.getRename());
		} else {
			// Create a sub select
			JoinContext context = _collectJoins(op.getSubOp());
			context.getProjection().renameAll(op.getRename());
			SqlOpSelectBlock selectBlock = contextToBlock(op.getSchema(), context); 
			
			String aliasName = aliasGenerator.next();
			selectBlock.setAliasName(aliasName);
			
			result = new JoinContextJoin(selectBlock);		
		}

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
			
			//XClass datatype = schema.getColumnType(columnName);			
			//context.getProjection().put(columnName, new SqlExprColumn(aliasName, columnName, datatype)); //ExprVar(aliasName + "." + columnName));
			TypeToken datatype = schema.getColumnType(newName);
			
			assert datatype != null : "Datatype must not be null at this point";
			
			//projection.put(newName, new E_SqlColumnRef(oldName, aliasName, datatype));
			projection.put(newName, new S_ColumnRef(datatype, oldName, aliasName));
		}
	}
	
	
	public static JoinContextJoin collectJoins(SqlOpTable op) {
		SqlOpTable table = makeSelectOrTable(op);
		
		JoinContextJoin result = new JoinContextJoin(table);
		
		initProjection(result.getProjection(), op.getSchema(), table.getAliasName());
		
		return result;
	}

	public static JoinContextJoin collectJoins(SqlOpQuery op) {
		SqlOpQuery query = makeSelectOrTable(op);
		
		JoinContextJoin result = new JoinContextJoin(query);
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
	public static SqlOpEmpty makeSelectOrTable(SqlOpEmpty node) {
		
		String alias = aliasGenerator.next();
		SqlOpEmpty result = new SqlOpEmpty(node.getSchema(), alias);
		
		return result;
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


	@Override
	public SqlOp transform(SqlOp op) {
		
		return SqlOpSelectBlockCollectorImpl._makeSelect(op);
		
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

