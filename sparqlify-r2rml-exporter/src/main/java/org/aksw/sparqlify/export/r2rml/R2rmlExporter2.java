package org.aksw.sparqlify.export.r2rml;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformConcatMergeConstants;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformFlattenFunction;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformSubstituteWithArgument;
import org.aksw.jena_sparql_api.exprs_ext.E_StrConcatPermissive;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.r2rml.api.GraphMap;
import org.aksw.r2rml.api.ObjectMap;
import org.aksw.r2rml.api.PredicateMap;
import org.aksw.r2rml.api.PredicateObjectMap;
import org.aksw.r2rml.api.SubjectMap;
import org.aksw.r2rml.api.TermMap;
import org.aksw.r2rml.api.TriplesMap;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprTransformer;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;


public class R2rmlExporter2 {

	/**
	 * Applies escaping according to https://www.w3.org/TR/r2rml/#from-template
	 * 
	 * @param str
	 * @return
	 */
	public static String escapeForTemplate(String str) {
		String result = str
				.replace("\\", "\\\\")
				.replace("{", "\\{")
				.replace("}", "\\}");
		return result;
	}
	
	/**
	 * Apply unnesting of concats
	 * 
	 * @param exprs
	 * @return
	 */
	public static String toTemplate(Expr concatExpr) {
		Expr expr = normalizeConcatExpressions(concatExpr);
		String result;
		if(expr.isFunction()) {
			result = toTemplateCore(expr.getFunction().getArgs());
		} else {
			throw new RuntimeException("Concat expr required; instead got " + concatExpr);
		}
		
		return result;
	}
	
	public static Expr normalizeConcatExpressions(Expr expr) {
		Predicate<Expr> isConcatExpr = e -> e instanceof E_StrConcat || e instanceof E_StrConcatPermissive;
		
		Predicate<Expr> isOmittedExpr = e -> e instanceof E_Str;
		Expr e1 = ExprTransformer.transform(new ExprTransformFlattenFunction(isConcatExpr), expr);
		Expr e2 = ExprTransformer.transform(new ExprTransformConcatMergeConstants(isConcatExpr), e1);

		Expr result = ExprTransformer.transform(new ExprTransformSubstituteWithArgument(isOmittedExpr), e2);

		return result;
	}
	
	public static String toTemplateCore(List<Expr> exprs) {
		StringBuilder b = new StringBuilder();
		
		for(Expr expr : exprs) {
			if(expr.isVariable()) {
				String varName = expr.getVarName();
				String escapedVarName = escapeForTemplate(varName);
				b.append("{" + escapedVarName + "}");
			} else if(expr.isConstant()) {
				String str = expr.getConstant().asUnquotedString();
				String escapedStr = escapeForTemplate(str);
				b.append(escapedStr);
			} else {
				throw new RuntimeException("Unexpected expression: " + expr);
			}
		}
		
		String result = b.toString();
		return result;
	}
	
	public Model export(Model result, Collection<ViewDefinition> viewDefs) {
		
		for(ViewDefinition viewDef : viewDefs) {
			export(result, viewDef);
		}
		
		return result;
	}
	
	
	public Model export(Model result, ViewDefinition viewDef) {
		// Partition view definitions by graph and subject		
		
		List<Quad> template = viewDef.getTemplate().getList();
		Multimap<Entry<Node, Node>, Quad> gsToQuadsIndex = Multimaps.index(template, q -> new SimpleEntry<>(q.getGraph(), q.getSubject()));
		
		for(Entry<Entry<Node, Node>, Collection<Quad>> gsToQuads : gsToQuadsIndex.asMap().entrySet()) {
			Entry<Node, Node> key = gsToQuads.getKey();
			Node g = key.getKey();
			Node s = key.getValue();
			
			Collection<Quad> quads = gsToQuads.getValue();
			
			exportTriplesMapSameGraphAndSubject(result, viewDef, g, s, quads);
		}
		
		
		return result;
	}
	
	public static String createR2rmlIriTemplateString(Expr expr) {
		//Expr expr = new E_StrConcatPermissive(new ExprList()).copy(new ExprList(fn.getArgs()));
		String result = toTemplate(expr);
		return result;
	}

	
	public static String getColumnName(Expr expr) {
		String result;
		if(expr.isVariable()) {
			result = expr.asVar().getName();
		} else {
			throw new RuntimeException("plainLiteral: first argument must be a column reference");
		}

		return result;
	}
	
	public static ExprFunction normalizeRdfTerm(E_RdfTerm rdfTerm) {
		int termTypeId = rdfTerm.getType().getConstant().getDecimal().intValue();

		ExprFunction result;
		switch(termTypeId) {
		case 0: // blank node
			result = new E_BNode(rdfTerm.getLexicalValue());
			break;
		case 1: // uri
			result = new E_URI(rdfTerm.getLexicalValue());
			break;
		case 2: // plain literal
			result = new E_StrLang(rdfTerm.getLexicalValue(), rdfTerm.getLanguageTag());
			break;
		case 3: // typed literal
			result = new E_StrDatatype(rdfTerm.getLexicalValue(), rdfTerm.getDatatype());
			break;
		default:
			throw new RuntimeException("Unsupported term type: " + rdfTerm);
		}
	
		return result;
	}
	
	public static void updateTermMapFromExpression(TermMap result, Expr expr) {
		// Check the top level type
		if(expr.isFunction()) {
			ExprFunction fn = expr.getFunction();
			String fnIri = fn.getFunctionIRI();
			
			if(fnIri.equals(SparqlifyConstants.rdfTermLabel)) {
				List<Expr> args = fn.getArgs();

				if(args.size() != 4) {
					throw new RuntimeException("E_RdfTerm requires exactly 4 arguments");
				}

				ExprFunction normExpr = normalizeRdfTerm(new E_RdfTerm(args));
				
				if(normExpr instanceof E_URI) {
					String templateStr = createR2rmlIriTemplateString(normExpr.getArg(1));
					result.setTemplate(templateStr);
				} else if(normExpr instanceof E_StrLang) {
					
					result.setColumn(getColumnName(normExpr.getArg(1)));				
					
					Expr langExpr = normExpr.getArg(2);
					if(langExpr.isConstant()) {
						String lang = langExpr.getConstant().getString();
						if(!lang.isEmpty()) {
							result.setLanguage(lang);
						}
					}
				} else if(normExpr instanceof E_StrDatatype) {
					result.setColumn(getColumnName(normExpr.getArg(1)));				

					Node dtype = args.get(1).getConstant().asNode();
					result.setDatatype(result.getModel().asRDFNode(dtype).asResource());
				} else {
					throw new RuntimeException("Unknow term constructor: " + expr);
				}
			}
		} else {
			throw new RuntimeException("Term constructor expression expected, but expression was not even a function");
		}
	}
	
	public static <T extends TermMap> T processTermMap(T result, Node node, ViewDefinition viewDef) {
		if(node.isConcrete()) {
			result.setConstant(result.getModel().asRDFNode(node));
		} else if(node.isVariable()) {
			Collection<RestrictedExpr> rexprs = viewDef.getVarDefinition().getDefinitions((Var)node);
			if(rexprs.size() != 1) {
				throw new RuntimeException("No definition for " + node);
			}

			Expr expr = rexprs.iterator().next().getExpr();
			
			updateTermMapFromExpression(result, expr);
			
			
		} else {
			throw new RuntimeException("Unexpected case - should not happen");
		}

		return result;
	}
	
	public Model exportTriplesMapSameGraphAndSubject(Model result, ViewDefinition viewDef, Node g, Node s, Collection<Quad> quads) {
		TriplesMap tm = result.createResource().as(TriplesMap.class);
		
		// Process graph and subject		
		SubjectMap sm = processTermMap(result.createResource().as(SubjectMap.class), s, viewDef);
		tm.setSubjectMap(sm);
		
		if(!g.equals(Quad.defaultGraphNodeGenerated)) {
			GraphMap gm = processTermMap(result.createResource().as(GraphMap.class), g, viewDef);

			sm.setGraphMap(gm);
		}

		// Index remaining quads by predicate
		Multimap<Node, Quad> pToQuadsIndex = Multimaps.index(quads, Quad::getPredicate);
		
		for(Entry<Node, Collection<Quad>> pToQuads : pToQuadsIndex.asMap().entrySet()) {
			Node p = pToQuads.getKey();
			Collection<Quad> pQuads = pToQuads.getValue();
			
			PredicateObjectMap pom = result.createResource().as(PredicateObjectMap.class);
			tm.getPredicateObjectMaps().add(pom);
			
			PredicateMap pm = processTermMap(result.createResource().as(PredicateMap.class), p, viewDef);
			pom.getPredicateMaps().add(pm);
			
			
			for(Quad quad : pQuads) {
				Node o = quad.getObject();
				ObjectMap om = processTermMap(result.createResource().as(ObjectMap.class), o, viewDef);
				
				pom.getObjectMaps().add(om);
			}
		}
		
		return result;
	}
}
