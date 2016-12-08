package org.aksw.sparqlify.core.sql.common.serialization;

import org.apache.commons.lang.StringEscapeUtils;

public class SqlEscaperBase
    implements SqlEscaper
{
    // TODO hibernate uses char openQuote, closeQuote ; maybe use the same concept
    protected String prefix;
    protected String suffix;

    public SqlEscaperBase(String prefix, String suffix) {
        super();
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String escapeIdentifier(String name) {
        String result = prefix + name + suffix;
        return result;
    }

    @Override
    public String escapeTableName(String tableName) {
        String result = escapeIdentifier(tableName);
        return result;
    }

    @Override
    public String escapeColumnName(String columnName) {
        String result = escapeIdentifier(columnName);
        return result;
    }

    @Override
    public String escapeAliasName(String aliasName) {
        String result = escapeIdentifier(aliasName);
        return result;
    }

    // TODO Make this configurable
    @Override
    public String escapeStringLiteral(String str) {
        return "'" + StringEscapeUtils.escapeSql(str) + "'";
    }
    
}
