/**
 * 
 */
package org.aksw.r2rml.impl.jena;

import java.util.Set;

import org.aksw.jena_sparql_api.exprs_ext.E_StrConcatPermissive;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

public class RRUtils {
	public static <T> T getFirst(Iterable<T> set) {		
		T result = set.iterator().next();

		return result;
	}

	
	public static Resource getResourceFromSet(Set<RDFNode> set) {
		if(set.isEmpty() || set.size() > 1) {
			throw new RuntimeException("Need exactly one element");
		}
		
		RDFNode item = getFirst(set);
		
		Resource result = (Resource)item;
		return result;
	}
	
	
	public static E_StrConcatPermissive parseTemplate(String str) {
		//int beginIndex;
		ExprList args = new ExprList();
		
		String pkUri = str.substring(0, str.indexOf('{')).replace("=","");
		String pkName = str.substring(str.indexOf('{')+1, str.indexOf('}'));
		args.add(NodeValue.makeString(pkUri));
		args.add(new ExprVar(Var.alloc(pkName)));
		
		str= str.substring(str.indexOf('}')+1);

		while(!str.equals("") && str.contains("{") && str.contains("}")){
			
			pkName = str.substring(str.indexOf('{')+1, str.indexOf('}'));
			args.add(NodeValue.makeString(pkUri));
			args.add(new ExprVar(Var.alloc(pkName)));
			str= str.substring(str.indexOf('}')+1);
		}

		E_StrConcatPermissive result = new E_StrConcatPermissive(args);

		Set<Var> vars = result.getVarsMentioned();

		return result;
	}
}