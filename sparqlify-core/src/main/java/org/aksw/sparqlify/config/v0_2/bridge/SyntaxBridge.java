package org.aksw.sparqlify.config.v0_2.bridge;

import java.util.List;
import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.config.lang.Constraint;
import org.aksw.sparqlify.config.syntax.QueryString;
import org.aksw.sparqlify.config.syntax.Relation;
import org.aksw.sparqlify.config.syntax.RelationRef;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * Takes a syntactic ViewDefinition and creates one for the Sparlqify Core package
 * 
 * Adapter for Sparqlify-ML 0.1
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SyntaxBridge {
	
	private static final Logger logger = LoggerFactory.getLogger(SyntaxBridge.class);

	/**
	 * We need the schema provider for resolving the column types
	 * 
	 */
	private SchemaProvider schemaProvider;
	
	public SyntaxBridge(SchemaProvider schemaProvider) {
		this.schemaProvider = schemaProvider;
	}
	
	public SchemaProvider getSchemaProvider() {
		return schemaProvider;
	}
	
	public org.aksw.sparqlify.core.domain.input.ViewDefinition create(ViewDefinition viewDefinition) {
		
		String name = viewDefinition.getName();		
		
		QuadPattern template = new QuadPattern();
		
		for(Triple triple : viewDefinition.getConstructPattern().getList()) {
			Quad quad = new Quad(Quad.defaultGraphNodeGenerated, triple);
			
			template.add(quad);
		}
		
		VarExprList varExprList = viewDefinition.getViewTemplateDefinition().getVarExprList();
		List<Constraint> constraints = viewDefinition.getConstraints();
		
		if(constraints != null) {
			for(Constraint constraint : constraints) {
				// TODO
				//constraint.
			}
		}
			
		
		
		Multimap<Var, RestrictedExpr> varDefs = HashMultimap.create();
		
		for(Entry<Var, Expr> entry : varExprList.getExprs().entrySet()) {
			Var var = entry.getKey();
			Expr expr = entry.getValue();
			
			RestrictedExpr rexpr = new RestrictedExpr(expr);
			
			varDefs.put(var, rexpr);
		}
		
		VarDefinition varDefinition = new VarDefinition(varDefs);
		

		Relation relation = viewDefinition.getRelation();
		
		// TODO: I think the adapter should be able to resolve the schema at this stage,
		// Rather than leaving the schema on null.
		
		SqlOp sqlOp = null;
		if(relation == null) { 
			
			logger.warn("No relation given for view '" + name + "', using Select 1");
			Schema schema = schemaProvider.createSchemaForQueryString("SELECT 1");
			sqlOp = new SqlOpQuery(schema, "SELECT 1"); //;null;

		} else if(relation instanceof QueryString) {

			String queryString = ((QueryString)relation).getQueryString();
			Schema schema = schemaProvider.createSchemaForQueryString(queryString);
			sqlOp = new SqlOpQuery(schema, queryString);

		} else if(relation instanceof RelationRef){
			
			String relationName = ((RelationRef) relation).getRelationName();
			Schema schema = schemaProvider.createSchemaForRelationName(relationName);
			sqlOp = new SqlOpTable(schema, relationName);
			
		} else {
			throw new RuntimeException("Unsupported relation type: " + relation);
		}
		
		Mapping mapping = new Mapping(varDefinition, sqlOp);
		
			
		org.aksw.sparqlify.core.domain.input.ViewDefinition result = new org.aksw.sparqlify.core.domain.input.ViewDefinition(name, template, null, mapping, viewDefinition);
		
		return result;
	}
}

