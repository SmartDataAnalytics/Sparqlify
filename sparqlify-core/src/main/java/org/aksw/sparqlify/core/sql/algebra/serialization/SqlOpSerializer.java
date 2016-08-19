package org.aksw.sparqlify.core.sql.algebra.serialization;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;

public interface SqlOpSerializer {
	String serialize(SqlOp op);
}
