package org.aksw.sparqlify.config.dialects;

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

    public String escapeDefault(String name) {
        String result = prefix + name + suffix;
        return result;
    }

    @Override
    public String escapeTableName(String tableName) {
        String result = escapeDefault(tableName);
        return result;
    }

    @Override
    public String escapeColumnName(String columnName) {
        String result = escapeDefault(columnName);
        return result;
    }

    @Override
    public String escapeAliasName(String aliasName) {
        String result = escapeDefault(aliasName);
        return result;
    }
}
