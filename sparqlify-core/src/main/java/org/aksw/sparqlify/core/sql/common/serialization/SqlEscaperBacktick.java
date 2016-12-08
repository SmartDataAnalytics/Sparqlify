package org.aksw.sparqlify.core.sql.common.serialization;

public class SqlEscaperBacktick
    extends SqlEscaperBase
{
    public SqlEscaperBacktick() {
        super("`", "`");
    }
}
