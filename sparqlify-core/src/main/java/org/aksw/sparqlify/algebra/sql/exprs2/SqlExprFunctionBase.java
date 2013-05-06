package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;


public abstract class SqlExprFunctionBase
	extends SqlExprBase
	implements SqlExprFunction
{
	protected final String name;
	
	public SqlExprFunctionBase(TypeToken datatype, String name) {
		super(datatype);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public SqlExprType getType() {
		return SqlExprType.Function;
	}
	
	public SqlExprFunction asFunction() {
		return this;
	}
	
	public void writeArgs(IndentedWriter writer) {
		writer.print(" (");
		writer.incIndent();
		boolean isFirst = true;
		
		List<SqlExpr> args = this.getArgs();
		for(SqlExpr arg : args) {
			if(isFirst) {
				isFirst = false;
				writer.println();
			} else {
				writer.println(", ");
			}
			
			arg.asString(writer);
		}
		writer.println();
		writer.decIndent();
		writer.print(")");
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(name);
		writeArgs(writer);
	}


}