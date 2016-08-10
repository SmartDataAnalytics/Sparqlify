package org.aksw.sparqlify.config.dialects;

public interface SqlEscaper {
    String escapeAliasName(String aliasName);
    String escapeTableName(String tableName);
    String escapeColumnName(String columnName);
}
