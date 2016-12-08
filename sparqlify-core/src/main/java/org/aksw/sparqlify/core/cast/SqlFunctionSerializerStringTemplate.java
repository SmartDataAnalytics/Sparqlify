package org.aksw.sparqlify.core.cast;

import java.util.List;

import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializer;
import org.aksw.sparqlify.type_system.MethodDeclaration;
import org.aksw.sparqlify.type_system.MethodSignature;
import org.stringtemplate.v4.ST;

public class SqlFunctionSerializerStringTemplate
	implements SqlFunctionSerializer
{
	// TODO Optimize with some precompiled template
	private String patternStr;
	private MethodDeclaration<?> decl;
	
	public SqlFunctionSerializerStringTemplate(String patternStr, MethodDeclaration<?> decl) {
		this.patternStr = patternStr;
		this.decl = decl;
	}
	

	@Override
	public String serialize(List<String> args) {
		MethodSignature<?> sig = decl.getSignature();
		List<?> params = sig.getParameterTypes();
		
		int numArgs = args.size();
		int numParams = params.size(); 
		
		if(numArgs < numParams) {
			throw new RuntimeException("Too few arguments provided. Got: " + args + " for " + decl);
		}
		
		int numVarArgs = numArgs - numParams;
		
		List<String> rest = args.subList(numParams, numArgs);
		if(!sig.isVararg()) {
			if(numVarArgs > 0) {
				throw new RuntimeException("Too many arguments provided. Got: " + args + " for " + decl);
			}
		}
		
		ST st = new ST(patternStr, '$', '$');
		st.add("name", decl.getName());
		
		for(int i = 0; i < numParams; ++i) {
			String s = args.get(i);
			st.add(Integer.toString(i + 1), s);
		}
		
		st.add("rest", rest);
		
		String result = st.render();
		return result;
	}
	
	
	public static SqlFunctionSerializerStringTemplate create(String patternStr, MethodDeclaration<?> decl) {
		SqlFunctionSerializerStringTemplate result = new SqlFunctionSerializerStringTemplate(patternStr, decl);
		return result;
	}
}