package org.aksw.obda.jena.r2rml.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformConcatMergeConstants;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformFlattenFunction;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformSubstituteWithArgument;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.Multimaps;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


public class R2rmlExporter {
	
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
		// TODO We implicitly assume that the expression is a concat one - we should validate this
		Expr expr = normalizeConcatExpressions(concatExpr);
		List<Expr> args = expr.isFunction() ? expr.getFunction().getArgs() : Collections.singletonList(expr);
		String result = toTemplateCore(args);
		
		return result;
	}
	
	public static Expr normalizeConcatExpressions(Expr expr) {
		Predicate<Expr> isConcatExpr = e -> e instanceof E_StrConcat;// || e instanceof E_StrConcatPermissive;
		
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
		
		List<Quad> template = viewDef.getConstructTemplate();
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

	
	public static Optional<String> getIriOrString(Expr expr) {
		//String result = expr.isConstant() ? expr.getConstant()
		Optional<String> result = Optional.of(expr)
				.map(e -> e.isConstant() ? e.getConstant() : null)
				.map(e -> e.asString());
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
	
	
	public static void updateTermMapFromExpression(TermMap result, Expr expr) {
		E_RdfTerm rdfTerm = Optional.ofNullable(E_RdfTerm.expand(expr))
				.orElseThrow(() -> new RuntimeException("Not an RdfTerm: " + expr));
		
		ExprFunction normExpr = E_RdfTerm.normalize(rdfTerm);
		
		List<Expr> args = rdfTerm.getArgs();

		if(args.size() != 4) {
			throw new RuntimeException("E_RdfTerm requires exactly 4 arguments");
		}

		
		if(normExpr instanceof E_URI) {
			String templateStr = createR2rmlIriTemplateString(normExpr.getArg(1));
			result.setTemplate(templateStr);
		} else if(normExpr instanceof E_StrLang) {
			
			String columnName = getColumnName(normExpr.getArg(1));
			result.setColumn(columnName);				
			
			Expr langExpr = normExpr.getArg(2);
			if(langExpr.isConstant()) {
				String lang = langExpr.getConstant().getString();
				if(!lang.isEmpty()) {
					result.setLanguage(lang);
				}
			}
		} else if(normExpr instanceof E_StrDatatype) {
			String columnName = getColumnName(normExpr.getArg(1));
			result.setColumn(columnName);				

			Expr dtypeExpr = normExpr.getArg(2);
			String dtype = getIriOrString(dtypeExpr)
					.orElseThrow(() -> new RuntimeException("IRI or String expected"));

			result.setDatatype(result.getModel().createResource(dtype));
		} else {
			throw new RuntimeException("Unknow term constructor: " + expr);
		}
	}
	
	public static <T extends TermMap> T processTermMap(T result, Node node, ViewDefinition viewDef) {
		if(node.isConcrete()) {
			result.setConstant(result.getModel().asRDFNode(node));
		} else if(node.isVariable()) {
			Expr expr = viewDef.getVarDefinition().get(node);
//			if(rexprs.size() != 1) {
//				throw new RuntimeException("No definition for " + node);
//			}
//
//			Expr expr = rexprs.iterator().next().getExpr();
			
			updateTermMapFromExpression(result, expr);
			
			
		} else {
			throw new RuntimeException("Unexpected case - should not happen");
		}

		return result;
	}
	
	
	public Model exportTriplesMapSameGraphAndSubject(Model result, ViewDefinition viewDef, Node g, Node s, Collection<Quad> quads) {
		TriplesMap tm = result.createResource().as(TriplesMap.class);
		
		
		tm.addProperty(RDF.type, RR.TriplesMap);
		tm.addLiteral(RDFS.label, viewDef.getName());
		
		// Process graph and subject		
		SubjectMap sm = processTermMap(result.createResource().as(SubjectMap.class), s, viewDef);
		tm.setSubjectMap(sm);
		
		if(!Quad.isDefaultGraph(g)) {
			GraphMap gm = processTermMap(result.createResource().as(GraphMap.class), g, viewDef);

			
			sm.getGraphMaps().add(gm);
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
		
		org.aksw.r2rml.jena.domain.api.LogicalTable lt = result.createResource().as(org.aksw.r2rml.jena.domain.api.LogicalTable.class);
		tm.setLogicalTable(lt);

		org.aksw.obda.domain.api.LogicalTable srcLt = viewDef.getLogicalTable();
		if(srcLt.isTableName()) {
			lt.asBaseTableOrView().setTableName(srcLt.getTableName());
		} else if(srcLt.isQueryString()) {
			lt.asR2rmlView().setSqlQuery(srcLt.getQueryString());
		} else {
			throw new RuntimeException("Unknown logical table: " + srcLt);
		}
		
		
		return result;
	}
}
