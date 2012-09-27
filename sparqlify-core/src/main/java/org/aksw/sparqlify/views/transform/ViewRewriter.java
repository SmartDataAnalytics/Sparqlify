package org.aksw.sparqlify.views.transform;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeEmpty;
import org.aksw.sparqlify.core.ColRelGenerator;
import org.aksw.sparqlify.core.RdfViewInstance;
import org.aksw.sparqlify.core.SqlNodeBinding;
import org.aksw.sparqlify.database.Clause;
import org.aksw.sparqlify.database.OpFilterIndexed;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * Interface for generating ids.
 * 
 * NOT NEEDED - USE JENA'S ColRelGenerator / Gensym instead
 */
interface IIdColRelGenerator {
	String next();
}

class IdColRelGenerator {
	private String prefix;
	private int current = 0;
}

/**
 * TODO It seems as if introducing vector objects causes quite some troubles, so
 * better not use it.
 * 
 */

/**
 * Given a query, a set of views, and a set of bindings, rewrites the query
 * using the views.
 * 
 * Essentially what happens it this stage is that quads in the query are
 * replaced with view:
 * 
 * LeftJoin({a, b, c}, {x, y ,z}) becomes something like
 * 
 * VarMap(LeftJoin(View(id, quadPattern, patternVarsToOriginal), View(id,
 * quadPattern) , etc... )
 * 
 * id: Id of the view quadPattern: The specific quad pattern from the view (with
 * variables replaced according to the binding).
 * 
 * The overall VapMap maps each of the variables back to a varible of the query.
 * 
 * The probably most important problem which needs to be solved is how to create
 * the join conditions on the SQL level from bindings.
 * 
 * - A single sparql variable may correspond to (multiple) columns in multiple
 * table instances (-> alias) within a SQL query.
 * 
 * - The aliases
 * 
 * @author raven
 * 
 *         ?s = Uri(concat('prefix', id))
 * 
 * 
 * 
 *         Determining join conditions: Whenever there exists an expression such
 *         as (not) equals(?s, ?x), and the corresponding variables belong to
 *         different view instances, then the expression can be turned into a
 *         join condition.
 * 
 * 
 * 
 * 
 */
public class ViewRewriter {
	private static final Logger logger = LoggerFactory
			.getLogger(ViewRewriter.class);


	/*
	 * public OpSql pushDown(Expr a) {
	 * 
	 * }
	 */

	// private Multimap<Var, >

	/*
	 * public SqlNodeBinding rewrite(Op op) {
	 * 
	 * //logger.debug("Starting sql rewrite");
	 * 
	 * 
	 * 
	 * System.out.println(op.getClass()); System.out.println(op.toString());
	 * 
	 * return rewriteMM(op); }
	 */

	/*
	public SqlNode rewrite(ColRelGenerator generator, OpUnion op) {
		SqlNode a = rewriteMM(generator, op.getLeft());
		SqlNode b = rewriteMM(generator, op.getRight());


		SqlNode result = SqlNodeBinding.union(generator, Arrays.asList(a, b));

		return result;
	}
	*/

	public SqlNodeOld rewrite(ColRelGenerator generator, OpLeftJoin op) {

		
		// Pull filters up: Join(Filter(x)) = Filter(Join(x))
		// (Without pulling they result in sub-selects)
		
		
		// Given x LEFT JOIN y, we must ensure that x and y have aliases
		// (so x as a LEFT JOIN y as b)
		
		SqlNodeOld a = rewriteMM(generator, op.getLeft());
		if(a instanceof SqlNodeEmpty) {
			return a;
		}
		
		SqlNodeOld b = rewriteMM(generator, op.getRight());
		if(b instanceof SqlNodeEmpty) {
			return a;
		}

		
		/*
		if(a.getAliasName() == null) {
			
			String newAlias = generator.next();
			SqlNode newProj = new SqlProjection(newAlias, a);
			newProj.getAliasToColumn().putAll(a.getAliasToColumn());
			newProj.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
			a = newProj;
		}
		*/
		if(b.getAliasName() == null) {
			
			String newAlias = generator.nextRelation();


			/*
			SqlNode newProj = SqlNodeBinding.createNewAlias(newAlias, b, generator);
			b = new SqlProjection(newAlias, b);
			b.getAliasToColumn().putAll(newProj.getAliasToColumn());
			b.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
			*/


			/*
			SqlNode newProj = new SqlProjection(newAlias, b);
			newProj.getAliasToColumn().putAll(b.getAliasToColumn());
			newProj.getSparqlVarToExprs().putAll(b.getSparqlVarToExprs());			
			b = newProj;
			*/



			/*
			SqlNode newProj = SqlNodeBinding.createNewAlias(newAlias, b, generator);
			b = new SqlProjection(newAlias, b);
			b.getAliasToColumn().putAll(newProj.getAliasToColumn());
			b.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
			*/
		}


		a = SqlNodeBinding.join(generator, a, b, JoinType.LEFT);

		return a;
	}

	/*
	 * public SqlNodeBinding rewrite(OpFilter op) { for(Expr expr :
	
	 * op.getExprs()) {
	 * 
	 * } }
	 */

	public SqlNodeOld rewrite(ColRelGenerator generator, OpDisjunction op) {
		List<SqlNodeOld> bindings = new ArrayList<SqlNodeOld>();
		for (Op item : op.getElements()) {
			SqlNodeOld binding = rewriteMM(generator, item);
			
			if(!(binding instanceof SqlNodeEmpty)) {
				bindings.add(binding);	
			}			
		}
		
		SqlNodeOld result = SqlNodeBinding.union(generator, bindings);
		return result;
	}

	public SqlNodeOld rewrite(ColRelGenerator generator, OpRdfViewPattern op) {
		
		SqlNodeOld a = null;
		for (RdfViewInstance inst : op.getConjunction().getViewBindings()) {
			SqlNodeOld b = SqlNodeBinding.create(generator, inst);

			if(b instanceof SqlNodeEmpty) {
				return new SqlNodeEmpty();
			}
			
			if (a == null) {
				a = b;
				continue;
			}

			a = SqlNodeBinding.join(generator, a, b, JoinType.INNER);

			if(a instanceof SqlNodeEmpty) {
				return new SqlNodeEmpty();
			}


			// Via the complete query binding we can determine which variables
			// must be equal to each other.
			// In a further step via the SPARQL-SQL bindings we can determine
			// on which SQL colums to join:
			// { ?a p ?x . ?x ?b ?c } with ?x = Uri(concat('blah', ?id))
			// Note: this implies that the sql variables need to be instanced

			// all sparql-sql var bindings,
			// together with the sparql

			// inst.getSqlBindings

		}

		return a;
	}

	public SqlNodeOld rewrite(ColRelGenerator generator, OpJoin op) {
		SqlNodeOld a = rewriteMM(generator, op.getLeft());
		SqlNodeOld b = rewriteMM(generator, op.getRight());
		
		/*
		if(b.getAliasName() == null) {
			
			System.out.println("hmm");
			
			//b = SqlSelectBlockCollector.makeSqlBlock(generator, b);
		}*/
		
		SqlNodeOld result = SqlNodeBinding.join(generator, a, b, JoinType.INNER);		
		return result;
	}

	public SqlNodeOld rewrite(ColRelGenerator generator, OpDistinct op) {
		SqlNodeOld subNode = rewriteMM(generator, op.getSubOp());
		if(subNode instanceof SqlNodeEmpty) {
			return subNode;
		}
		
		SqlNodeOld result =  SqlNodeBinding.distinct(subNode);
		return result;
	}
	
	public SqlNodeOld rewrite(ColRelGenerator generator, OpSlice op) {
		SqlNodeOld subNode = rewriteMM(generator, op.getSubOp());
		if(subNode instanceof SqlNodeEmpty) {
			return subNode;
		}

		SqlNodeOld result = SqlNodeBinding.slice(subNode, generator, op.getStart(), op.getLength());
		return result;
	}

	
	public SqlNodeOld rewrite(ColRelGenerator generator, OpFilterIndexed filter) {
		SqlNodeOld subNode = rewriteMM(generator, filter.getSubOp());		
		if(subNode instanceof SqlNodeEmpty) {
			return subNode;
		}
		
		ExprList exprs = new ExprList();
		for(Clause clause : filter.getRestrictions().getCnf()) {
			exprs.add(ExprUtils.orifyBalanced(clause.getExprs()));
		}
		
		SqlNodeOld result = SqlNodeBinding.filter(subNode, exprs, generator);
		return result;
	}
	
	public SqlNodeOld rewrite(ColRelGenerator generator, OpFilter filter) {
		SqlNodeOld subNode = rewriteMM(generator, filter.getSubOp());
		SqlNodeOld result = SqlNodeBinding.filter(subNode, filter.getExprs(), generator);
		return result;
	}

	public SqlNodeOld rewrite(ColRelGenerator generator, OpProject op) {
		SqlNodeOld subNode = rewriteMM(generator, op.getSubOp());
		if(subNode instanceof SqlNodeEmpty) {
			return subNode;
		}


		SqlNodeOld result = SqlNodeBinding.project(subNode, op.getVars(), generator);
		
		return result;
	}
	
	public SqlNodeOld rewrite(ColRelGenerator generator, OpGroup op) {
		SqlNodeOld subNode = rewriteMM(generator, op.getSubOp());
		SqlNodeOld result = SqlNodeBinding.group(subNode, op.getGroupVars(), op.getAggregators(), generator.forColumn());
		
		return result;
	}
	
	public SqlNodeOld rewrite(ColRelGenerator generator, OpOrder order) {
		SqlNodeOld subNode = rewriteMM(generator, order.getSubOp());
		if(subNode instanceof SqlNodeEmpty) {
			return subNode;
		}

		SqlNodeOld result = SqlNodeBinding.order(subNode, order.getConditions(), generator);
		return result;
	}
	
	
	public SqlNodeOld rewriteMM(Op op)
		throws EmptyRewriteException
	{
		if(op instanceof OpNull) {
			throw new EmptyRewriteException();
		}
		
		ColRelGenerator generator = new ColRelGenerator();
		
		
		return rewriteMM(generator, op);
	}
	
	
	public SqlNodeOld rewrite(ColRelGenerator generator, OpExtend op) {		
		SqlNodeOld subNode = rewriteMM(generator, op.getSubOp());
		if(subNode instanceof SqlNodeEmpty) {
			return subNode;
		}

		SqlNodeOld result = SqlNodeBinding.extend(subNode, op.getVarExprList());
		return result;
		
	}
	
	public SqlNodeOld rewriteMM(ColRelGenerator generator, Op op) {

		SqlNodeOld result = (SqlNodeOld) MultiMethod.invoke(this, "rewrite", generator, op);
		/*
		if(result instanceof SqlNodeEmpty) {
			throw new RuntimeException("empty rewrite");
		}*/

		return result;
		
		/*
		 * if(op instanceof OpLeftJoin) { return rewrite((OpLeftJoin)op); } else
		 * if (op instanceof OpRdfUnionViewPattern) { return
		 * rewrite((OpRdfUnionViewPattern)op); } else { throw new
		 * RuntimeException("blah"); }
		 */
	}
}
/*
 * class ColumnAlias { }
 */
