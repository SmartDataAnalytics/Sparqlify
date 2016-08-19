package org.aksw.sparqlify.core.sql.common.serialization;

public class SqlEscaperDoubleQuote
    extends SqlEscaperBase
{
    public SqlEscaperDoubleQuote() {
        super("\"", "\"");
    }
}
