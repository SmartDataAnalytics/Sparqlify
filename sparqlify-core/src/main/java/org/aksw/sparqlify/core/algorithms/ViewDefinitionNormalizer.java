package org.aksw.sparqlify.core.algorithms;

import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.config.lang.PrefixSet;
import org.aksw.sparqlify.core.domain.Mapping;
import org.aksw.sparqlify.core.domain.RestrictedExpr;
import org.aksw.sparqlify.core.domain.VarDefinition;
import org.aksw.sparqlify.core.domain.ViewDefinition;
import org.aksw.sparqlify.expr.util.NodeValueUtils;
import org.aksw.sparqlify.restriction.RestrictionSet;
import org.aksw.sparqlify.restriction.Type;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

public class ViewDefinitionNormalizer {
	
	
	public RestrictedExpr normalize(RestrictedExpr restExpr) {

		RestrictionSet rs = restExpr.getRestrictions();
		if(rs == null) {
			rs = new RestrictionSet();
		} else {
			rs = restExpr.getRestrictions().clone();
		}
				
		Expr expr = restExpr.getExpr();
		Type type = deriveType(expr);
		if(type != null) {
			rs.stateType(type);
		}
		
		String prefix = derivePrefix(expr);
		if(prefix != null) {
			PrefixSet ps = new PrefixSet(prefix);
			rs.stateUriPrefixes(ps);
		}

		if(rs.isUnsatisfiable()) {
			System.err.println("Unsatisfiable restriction detected: " + restExpr);
		}
		
		RestrictedExpr result = new RestrictedExpr(expr, rs);
		return result;
	}

	
	public VarDefinition normalize(VarDefinition varDef) {
		Multimap<Var, RestrictedExpr> resultMap = HashMultimap.create();
		
		for(Entry<Var, RestrictedExpr> entry : varDef.getMap().entries()) {
			Var var = entry.getKey();
			RestrictedExpr old = entry.getValue();
			
			RestrictedExpr newRestExpr = normalize(old);
			
			resultMap.put(var, newRestExpr);
		}
		
		VarDefinition result = new VarDefinition(resultMap);
		return result;
	}


	public ViewDefinition normalize(ViewDefinition viewDefinition) {
		VarDefinition normVarDef = normalize(viewDefinition.getMapping().getVarDefinition());
		
		Mapping newMapping = new Mapping(normVarDef, viewDefinition.getMapping().getSqlOp());
		ViewDefinition result = new ViewDefinition(viewDefinition.getName(), viewDefinition.getTemplate(), viewDefinition.getViewReferences(), newMapping, viewDefinition);
		
		return result;
	}

	
	public static String derivePrefixConcat(ExprFunction concat) {
		
		// TODO If all arguments are constant, we could infer a constant constraint
		String prefix = "";
		for(Expr arg : concat.getArgs()) {
			if(arg.isConstant()) {
				prefix += arg.getConstant().asUnquotedString();
			} else {
				break;
			}
		}
		
		
		return prefix;			
	}

	
	public static String derivePrefix(E_RdfTerm termCtor) {
		String result;
		
		Expr expr = termCtor.getArgs().get(1);
		
		if(expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive) {			
			result = derivePrefixConcat(expr.getFunction());			
		} else {
			result = null;
		}

		return result;
	}
	

	public static String derivePrefix(ExprFunction fn) {
		E_RdfTerm termCtor = SqlTranslationUtils.expandRdfTerm(fn);
		
		String result; 
		if(termCtor != null) {
			result = derivePrefix(termCtor);
		} else {
			result = null; 
		}

		return result;
	
	}
	

	public static String derivePrefix(Node node) {
		
		String result;
		if(node.isURI()) {
			result = node.getURI();
		} else {
			result = null;
		}
		
		return result;
	}
	
	
	public static String derivePrefix(Expr expr) {

		String result;
		if(expr.isFunction()) {
			result = derivePrefix(expr.getFunction());			
		} else if(expr.isConstant()) {
			result = derivePrefix(expr.getConstant().getNode());
		} else {
			result = null;
		}

		return result;
	}
	

	public static Type deriveType(Node node) {
		if(node.isURI()) {
			return Type.URI;
		} else if(node.isLiteral()) {
			return Type.LITERAL;
		} else if(node.isBlank()) {
			throw new RuntimeException("Decide on what to return here.");
			//return Type.URI;
		} else {
			return Type.UNKNOWN;
		}
	}


	public static Type deriveType(E_RdfTerm termCtor) {				
		Expr arg = termCtor.getArg(1);
		if(arg.isConstant()) {
			Object o = NodeValueUtils.getValue(arg.getConstant());
			
			Number number = (Number)o;
			switch(number.intValue()) {
			case 1:
				return Type.URI;
			case 2:
			case 3:
				return Type.LITERAL;
			}
		}

		return Type.UNKNOWN;
	}
	

	public static Type deriveType(ExprFunction fn) {
		E_RdfTerm termCtor = SqlTranslationUtils.expandRdfTerm(fn);
		
		Type result; 
		if(termCtor != null) {
			result = deriveType(termCtor);
		} else {
			result = Type.UNKNOWN; 
		}

		return result;
	}
	

	public static Type deriveType(Expr expr) {
		
		Type result = null;
		if(expr.isConstant()) {
			result = deriveType(expr.getConstant().asNode());
		} else if(expr.isFunction()) {
			result = deriveType(expr.getFunction());
		}
		
		return result;
	}
}
