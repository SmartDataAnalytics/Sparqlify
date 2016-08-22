package org.aksw.sparqlify.util;

import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;

public class SqlBackendConfig {
    protected DatatypeToString typeSerializer;
    protected SqlEscaper sqlEscaper;
    
    public SqlBackendConfig(DatatypeToString typeSerializer,
            SqlEscaper sqlEscaper) {
        super();
        this.typeSerializer = typeSerializer;
        this.sqlEscaper = sqlEscaper;
    }

    public DatatypeToString getTypeSerializer() {
        return typeSerializer;
    }
    
    public SqlEscaper getSqlEscaper() {
        return sqlEscaper;
    }
}
