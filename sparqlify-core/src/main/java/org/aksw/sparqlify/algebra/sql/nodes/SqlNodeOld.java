package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.core.ConstraintContainer;
import org.openjena.atlas.io.IndentedWriter;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.Var;


/**
 * 
 * 
 * @author raven
 *
 */
public interface SqlNodeOld {

	ConstraintContainer getConstraints();
	
	// The expression of the sparql var may reference column names
	// A sparql variable may be mapped to multiple expressions, because
	// the expressions may make use of different (on sql level incompatible) datatypes.
	// For instance, postgres does not support a union of a int and string column
	// Mysql allows that, but the resulting column is then a varbinary, and therefore
	// the semantics of the underlying objects get lost.
	//Multimap<Node, Expr> getSparqlVarToExprs();
	Multimap<Var, VarDef> getSparqlVarToExprs();
	
	// The order in which the variables should appear in the projection, may be null
	// Not needed for now - this is done when creating the result set iterator
	// Not sure if sub queries change anything
	//List<Node> getOrder();
	//void setOrder(List<Node> order);
	
	
	// A mapping of column names to definitions
	// E.g (id, a.id) for select a.id as id from (...) a
	// The dataype of the column alias is also stored
	Map<String, SqlExpr> getAliasToColumn();

	
	// This is basically the projection of this node
	// Note that Var is actually the name of an Sql Column
	
	// This is the map for assigning alias to columns and expressions over columns
	// e.g. id -> concat(a1.name, a2.number)
	// variables on the left hand side must not have an alias, where
	// variables on the right hand side should use one.
	// FIXME: Rather than referring to the alias directly, it might be better to
	// refer to the alias indirectly, so it can be easily changed.
	// TODO I think this mapping should only be available in 
	// SqlProjection nodes (which are collected into SqlSelectBlocks) - only they are capable of renaming variables
	//Map<Var, Expr> getSqlVarToExpr();
	
	// This is the map of how the sql columns bind to sparql variables
	Set<Var> getSparqlVarsMentioned();

	//Set<String> getSqlColumnsMentioned();
	
	String getAliasName();

	List<SqlNodeOld> getArgs();
	
	abstract SqlNodeOld copy(SqlNodeOld ...nodes);
	
	
	void write(IndentedWriter writer);

}
