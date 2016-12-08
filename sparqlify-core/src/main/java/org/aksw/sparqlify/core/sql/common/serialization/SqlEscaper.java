package org.aksw.sparqlify.core.sql.common.serialization;

/**
 * Class for escaping certain syntactic SQL elements.
 * TODO Where to perform SQL escaping of literals? - This should be part of the SqlExpr serializers I suppose
 * 
 * 
 * 
 * @author raven
 *
 */
public interface SqlEscaper {
    String escapeAliasName(String aliasName);
    String escapeTableName(String tableName);
    String escapeColumnName(String columnName);
    
    /**
     * Escapes a string literal.
     * Note that serializing non-string types may still use string escaping as part
     * of the serialization process. E.g. a Date may be serialized as;
     * 
     * DATE 'str'
     * 
     * http://dev.mysql.com/doc/refman/5.7/en/date-and-time-literals.html
     * 
     * @param str
     * @return
     */
    String escapeStringLiteral(String str);
}
