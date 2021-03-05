package org.aksw.sparqlify.util;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;

public class SqlBackendConfig {
    protected DatatypeToString typeSerializer;
    protected SqlCodec sqlEscaper;

    // E.g. functions.xml under src/main/resources
    // protected List<String> functionDefinitions;
    
//    public SqlBackendConfig(DatatypeToString typeSerializer,
//            SqlEscaper sqlEscaper) {
//    	this(typeSerializer, sqlEscaper, Collections.singletonList("functions.xml"));
//    }

    public SqlBackendConfig(DatatypeToString typeSerializer,
    		SqlCodec sqlEscaper /*, List<String> functionDefinitions */) {
        super();
        this.typeSerializer = typeSerializer;
        this.sqlEscaper = sqlEscaper;
        // this.functionDefinitions = functionDefinitions;
    }

    public DatatypeToString getTypeSerializer() {
        return typeSerializer;
    }
    
    public SqlCodec getSqlEscaper() {
        return sqlEscaper;
    }

//	public List<String> getFunctionDefinitions() {
//		return functionDefinitions;
//	}
}
