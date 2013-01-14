package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

/**
 * TODO: Serialization expressions should not be part of the SqlExpr hierarchy! Instead, this should be a completely separate process.
 * 
 * An expression whose purpose is to provide a means
 * of serializing its arguments.
 * 
 * This class is intended to be used at the end of the SQL translation process,
 * where we no longer care about the semantics of the expression nodes; i.e. at the time where all
 * semantic stuff has already been processed, and now we only want to generate valid SQL.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class S_Serialize
	extends SqlExprN
{
	private SqlFunctionSerializer serializer;

	public S_Serialize(TypeToken datatype, String name, List<SqlExpr> exprs, SqlFunctionSerializer serializer) {
		super(datatype, name, exprs);
		this.serializer = serializer;
	}

	public SqlFunctionSerializer getSerializer() {
		return serializer;
	}
	
	@Override
	public S_Serialize copy(List<SqlExpr> args) {
		S_Serialize result = new S_Serialize(getDatatype(), getName(), args, serializer);
		return result;
	}

	@Override
	public void asString(IndentedWriter writer) {
		writer.print("Serialize [" + getName() + "]");
		writeArgs(writer);		
	}
}
