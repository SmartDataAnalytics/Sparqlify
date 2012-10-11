package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

import org.aksw.sparqlify.core.datatypes.XMethod;
import org.openjena.atlas.io.IndentedWriter;


public class S_Method
	extends SqlExprN
{
	private XMethod method;
	private List<SqlExpr> args;
	
	public S_Method(XMethod method, List<SqlExpr> args) {
		super(method.getSignature().getReturnType().getToken(), method.getName(), args);
		
		this.method = method;
	}

	@Override
	public void asString(IndentedWriter writer) {
		writer.print("Method");
		writeArgs(writer);
	}

	/*
	public S_Method(TypeToken datatype) {
		super(datatype);
	}
	*/

	
	public S_Method create(XMethod method, List<SqlExpr> args) {
		S_Method result = new S_Method(method, args);
		
		return result;
	}

	public XMethod getMethod() {
		return method;
	}
	
	@Override
	public List<SqlExpr> getArgs() {
		return args;
	}	
}
