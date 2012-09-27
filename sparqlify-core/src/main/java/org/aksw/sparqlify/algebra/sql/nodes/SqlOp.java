package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;


/**
 * An SqlNode corresponds to an operator in the relational algebra.
 * Each SqlNode owns a (immutable) description of its corresponding schema.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface SqlOp {
	Schema getSchema();
	
	boolean isEmpty(); // Whether the op indicates a zero-row-count relation
}
