package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;

public interface SqlOpSerializer {
	String serialize(SqlOp op);
}
