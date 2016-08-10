package org.aksw.sparqlify.config.dialects;

public class SqlEscaperBacktick
    extends SqlEscaperBase
{
    public SqlEscaperBacktick() {
        super("`", "`");
    }
}
