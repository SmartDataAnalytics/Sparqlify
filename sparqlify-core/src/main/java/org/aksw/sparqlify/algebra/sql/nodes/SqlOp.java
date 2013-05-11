package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Set;

import org.openjena.atlas.io.IndentedWriter;


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
	
	public List<SqlOp> getSubOps();

	
	//String getIndentedString();
	void write(IndentedWriter writer);
	
	
	/**
	 * Create a copy of the SqlOp with a projected schema.
	 * The new schema will only hold information of the selected columns.
	 * Used to filter an Op to only the referenced columns.
	 * 
	 * This is different from a projection!
	 */
	SqlOp copy(Schema schema, List<SqlOp> newOps);
}
