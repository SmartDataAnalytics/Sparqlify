package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.openjena.atlas.io.IndentedWriter;

public interface SqlExpr {
	List<SqlExpr> getArgs();

	SqlDatatype getDatatype();	
	
	
	public void asString(IndentedWriter writer);

}
