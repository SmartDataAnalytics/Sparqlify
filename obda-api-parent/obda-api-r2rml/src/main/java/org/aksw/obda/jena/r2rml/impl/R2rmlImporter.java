package org.aksw.obda.jena.r2rml.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.obda.domain.api.Constraint;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

public class R2rmlImporter {
//	public static SqlOp logicalTableToSqlOp(LogicalTable logicalTable) {
//		SqlOp result;
//
//		String str;
//		if((str = logicalTable.getTableName()) != null) {
//			result = new SqlOpTable(new SchemaImpl(), str);
//		} else if((str = logicalTable.getQueryString()) != null) {
//			result = new SqlOpQuery(new SchemaImpl(), str);
//		} else {
//			throw new RuntimeException("Unsupported logical table type: " + logicalTable);
//		}
//		
//		return result;
//	}
	
	public Collection<ViewDefinition> read(Model model) {
		List<TriplesMap> triplesMaps = model.listSubjectsWithProperty(RR.logicalTable).mapWith(r -> r.as(TriplesMap.class)).toList();
	
		List<ViewDefinition> result = triplesMaps.stream()
				.map(tm -> read(tm))
				.collect(Collectors.toList());
	
		return result;
	}

	public Node allocateVar(TermMap tm, BiMap<Node, Expr> nodeToExpr, Generator<Var> varGen) {
		Node result;
		//VarGeneratorBlacklist.
		BiMap<Expr, Node> exprToNode = nodeToExpr.inverse();
		
		Expr expr = termMapToExpr(tm);
		result = exprToNode.get(expr);
		
		if(result == null) {
			// If the expr is a constant, just use its node as the result; no need to track this in the map
			if(expr.isConstant()) {
				result = expr.getConstant().asNode();
			} else {				
				// Allocate a new variable
				result = varGen.next();
				nodeToExpr.put(result, expr);
			}
		}
		
		return result;
	}
	
	// https://www.w3.org/TR/r2rml/#generated-triples
	// Note on graphs: the spec states: "If sgm and pogm are empty: rr:defaultGraph; otherwise: union of subject_graphs and predicate-object_graphs"
	public ViewDefinition read(TriplesMap tm) {
		// Construct triples by creating the cartesian product between g, s, p, and o term maps
		SubjectMap sm = tm.getSubjectMap();
		Set<GraphMap> sgms = sm.getGraphMaps();
		
		// Mapping of expressions to allocated variables
		BiMap<Node, Expr> nodeToExpr = HashBiMap.create();
		
		//ViewDefinition result = new ViewDefinition(name, template, viewReferences, mapping, source)
		List<Quad> template = new ArrayList<>();
				
		Generator<Var> varGen = new VarGeneratorImpl2("v", 1);

		for(PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
			Set<GraphMap> pogms = pom.getGraphMaps();
			
			// egms = effective graph maps
			Set<GraphMap> egms = Sets.union(sgms, pogms);
						
			if(egms.isEmpty()) {
				egms = Collections.singleton(null);
			}

			Set<PredicateMap> pms = pom.getPredicateMaps();
			Set<ObjectMap> oms = pom.getObjectMaps();

			for(GraphMap gm : egms) {			
				for(PredicateMap pm : pms) {
					for(ObjectMap om : oms) {
						Node g = gm == null ? Quad.defaultGraphNodeGenerated : allocateVar(gm, nodeToExpr, varGen);
						Node s = allocateVar(sm, nodeToExpr, varGen);
						Node p = allocateVar(pm, nodeToExpr, varGen);
						Node o = allocateVar(om, nodeToExpr, varGen);
						
						Quad quad = new Quad(g, s, p, o);
						template.add(quad);
					}
				}
			}
		}
		
		//ViewDefinition x;
		//x.getVarDefinition().
		//VarDefinition vd = new VarDefinition();
		Map<Var, Expr> varDefs = new LinkedHashMap<>();
		
		// Add the var definitions
		for(Entry<Node, Expr> entry : nodeToExpr.entrySet()) {
			Node n = entry.getKey();
			
			if(n.isVariable()) {
				Var v = (Var)n;
				Expr e = entry.getValue();

				varDefs.put(v, e);
			}			
		}

		
//		List<Expr> varBindings = varDefs.entrySet().stream()
//				.map(e -> new E_Equals(new ExprVar(e.getKey()), e.getValue()))
//				.collect(Collectors.toList());

		//SqlOp relation = logicalTableToSqlOp(tm.getLogicalTable());
		
//		ViewTemplateDefinition vtd = new ViewTemplateDefinition();
//		vtd.setConstructTemplate(template);
//		
//		vtd.setVarBindings(varBindings);
		
		Map<Var, Constraint> varConstraints = new LinkedHashMap<>();
		
		// Derive name
		String name = Optional.ofNullable(tm.getProperty(RDFS.label))
				.map(Statement::getString)
				.orElseGet(() -> tm.isURIResource() ? tm.getLocalName() : "" + tm);
		
		ViewDefinition result = new ViewDefinition(name, template, varDefs, varConstraints, tm.getLogicalTable());
		
		return result;
	}
	
	
	public static Expr parseTemplate(String str) {
		List<Expr> exprs = parseTemplateCore(str);
		
		Expr result = exprs.size() == 1
				? exprs.get(0)
				: new E_StrConcat(new ExprList(exprs))
				;

		return result;
	}
	
	public static List<Expr> parseTemplateCore(String str) {
		List<Expr> result = new ArrayList<>();

		char cs[] = str.toCharArray();
		
		boolean isInVarName = false;
		boolean escaped = false;
		
		int i = 0;

		StringBuilder builder = new StringBuilder();
		
		boolean repeat = true;
		while(repeat) {
			char c = i < cs.length ? cs[i++] : (char)-1;
			if(escaped) {
				builder.append(c);
				escaped = false;
			} else {
	
				switch(c) {
				case '\\':
					escaped = true;
					continue;
				case '{':
					if(isInVarName) {
						throw new RuntimeException("Unescaped '{' in var name not allowed");
					} else {
						result.add(NodeValue.makeString(builder.toString()));
						builder  = new StringBuilder();
						isInVarName = true;
					}
					break;
	
				case '}':
					if(isInVarName) {
						result.add(new ExprVar(builder.toString()));
						builder  = new StringBuilder();
						isInVarName = false;
					} else {
						throw new RuntimeException("Unescaped '}' not allowed");
					}
					break;
	
				case (char)-1:
					if(isInVarName) {
						throw new RuntimeException("End of string reached, but '}' missing");
					} else {
						if(builder.length() > 0) {
							result.add(NodeValue.makeString(builder.toString()));
						}
					}
					repeat = false;
					break;
				
				default:
					builder.append(c);
				}
			}
		}
		
		return result;
	}
	
	public static Expr termMapToExpr(TermMap tm) {
		Expr result;
		
		String template;
		RDFNode constant;
		if((template = tm.getTemplate()) != null) {
			Expr arg = parseTemplate(template);
			
			// TODO Support rr:termType rr:BlankNode
			result = new E_URI(arg);
			
		} else if((constant = tm.getConstant()) != null) {
			result = NodeValue.makeNode(constant.asNode());
		} else {
			String colName;
			if((colName = tm.getColumn()) != null) {
			
				ExprVar column = new ExprVar(colName);
				Resource dtype = tm.getDatatype();
				if(dtype != null || XSD.xstring.equals(dtype)) {
					result = new E_StrDatatype(column, NodeValue.makeNode(dtype.asNode()));
				} else {
					String language = Optional.ofNullable(tm.getLanguage()).orElse("");
					result = new E_StrLang(column, NodeValue.makeString(language));
				}
			} else {
				throw new RuntimeException("TermMap does neither define rr:template, rr:constant nor rr:column " + tm);
			}
		}
		
		return result;
	}
}
