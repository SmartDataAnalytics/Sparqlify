package org.aksw.sparqlify.config.dialects;

public class SqlEscaperDoubleQuote
    extends SqlEscaperBase
{
    public SqlEscaperDoubleQuote() {
        super("\"", "\"");
    }
}
