package org.aksw.sparqlify.core.sql.algebra.transform;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;

public interface SqlOpSelectBlockCollector {
	SqlOp transform(SqlOp op);
}
