package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.sparqlify.core.algorithms.SqlTranslatorImpl;
import org.aksw.sparqlify.core.datatypes.Invocable;
import org.aksw.sparqlify.core.datatypes.SqlMethodCandidate;
import org.aksw.sparqlify.core.datatypes.XMethod;
import org.openjena.atlas.io.IndentedWriter;


public class S_Method
	extends SqlExprN
{
	private XMethod method;
	//private List<SqlExpr> args;
	
	public S_Method(XMethod method, List<SqlExpr> args) {
		super(method.getSignature().getReturnType().getToken(), method.getName(), args);
		
		this.method = method;
	}

	@Override
	public void asString(IndentedWriter writer) {
		writer.print("SqlFunction " + "[" + method.getName() + "]");
		writeArgs(writer);
	}

	/*
	public S_Method(TypeToken datatype) {
		super(datatype);
	}
	*/

	
	public static S_Method create(XMethod method, List<SqlExpr> args) {
		S_Method result = new S_Method(method, args);
		
		return result;
	}

	
	public static SqlExpr createOrEvaluate(SqlMethodCandidate candidate, List<SqlExpr> args) {

		SqlExpr result;
		if(SqlTranslatorImpl.isConstantsOnlySql(args)) {
			Invocable invocable = candidate.getInvocable();
			if(invocable != null) {
				Object[] as = new Object[args.size()];
				
				for(int i = 0; i < args.size(); ++i) {
					as[i] = args.get(i).asConstant().getValue();
				}
				
				Object value = invocable.invoke(as);
				
				result = new S_Constant(value, candidate.getMethod().getSignature().getReturnType().getToken());
				
				return result;
			}
		}
		
			
		result = create(candidate, args);
		
		return result;
	}
	
	public static S_Method create(SqlMethodCandidate candidate, List<SqlExpr> args) {
				
		List<XMethod> argCoercions = candidate.getArgCoercions();

		List<SqlExpr> newArgs;
		
		if(argCoercions == null) {
			newArgs = args;
		} else {
			
			newArgs = new ArrayList<SqlExpr>(argCoercions.size());
			for(int i = 0; i < argCoercions.size(); ++i) {
				SqlExpr arg = args.get(i);
				XMethod argCoercion = argCoercions.get(i);
				
				SqlExpr newArg;
				if(argCoercion == null) {
					newArg = arg;
				} else {				
					newArg = S_Method.create(argCoercion, Collections.singletonList(arg));
				}
				
				newArgs.add(newArg);
			}
		}		
		
		XMethod main = candidate.getMethod();
		S_Method result = S_Method.create(main, newArgs);
		
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
