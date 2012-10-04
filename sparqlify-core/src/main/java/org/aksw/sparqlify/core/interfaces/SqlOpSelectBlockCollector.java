package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;

public interface SqlOpSelectBlockCollector {
	SqlOp transform(SqlOp op);
}
