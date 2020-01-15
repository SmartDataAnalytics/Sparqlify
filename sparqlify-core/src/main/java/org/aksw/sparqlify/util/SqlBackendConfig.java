package org.aksw.sparqlify.util;

import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;

public class SqlBackendConfig {
    protected DatatypeToString typeSerializer;
    protected SqlEscaper sqlEscaper;

    // E.g. functions.xml under src/main/resources
    // protected List<String> functionDefinitions;
    
//    public SqlBackendConfig(DatatypeToString typeSerializer,
//            SqlEscaper sqlEscaper) {
//    	this(typeSerializer, sqlEscaper, Collections.singletonList("functions.xml"));
//    }

    public SqlBackendConfig(DatatypeToString typeSerializer,
            SqlEscaper sqlEscaper /*, List<String> functionDefinitions */) {
        super();
        this.typeSerializer = typeSerializer;
        this.sqlEscaper = sqlEscaper;
        // this.functionDefinitions = functionDefinitions;
    }

    public DatatypeToString getTypeSerializer() {
        return typeSerializer;
    }
    
    public SqlEscaper getSqlEscaper() {
        return sqlEscaper;
    }

//	public List<String> getFunctionDefinitions() {
//		return functionDefinitions;
//	}
}
