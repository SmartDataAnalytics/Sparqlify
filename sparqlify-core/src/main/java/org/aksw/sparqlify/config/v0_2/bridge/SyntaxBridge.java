package org.aksw.sparqlify.config.v0_2.bridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.config.lang.Constraint;
import org.aksw.sparqlify.config.lang.PrefixConstraint;
import org.aksw.sparqlify.config.lang.PrefixSet;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;
import org.aksw.sparqlify.restriction.RestrictionImpl;
import org.aksw.sparqlify.restriction.RestrictionSetImpl;
import org.aksw.sparqlify.trash.ExprCopy;
import org.aksw.sparqlify.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprVar;



class ExprTransformerUtils
{
	public static final Function<Expr, Expr> injectStrsIntoConcats = new Function<Expr, Expr>() {
		@Override
		public Expr apply(@Nullable Expr expr) {
			Expr result;
			
			if(expr.isFunction()) {
				
				ExprFunction fn = expr.getFunction();
				boolean isConcat = SqlTranslationUtils.isConcatExpr(fn);

				
				List<Expr> args = fn.getArgs();
				
				List<Expr> newArgs = new ArrayList<Expr>(args.size());
				for(Expr arg : args) {
					Expr tmpArg = apply(arg);
					
					Expr newArg;
					
					// FIXME
					if(isConcat && tmpArg.isVariable()) {
						ExprVar ev = tmpArg.getExprVar();					
						newArg = new E_Str(ev);
						
					} else {
						newArg = tmpArg;
					}
					
					
					newArgs.add(newArg);
				}
				
				result = ExprCopy.getInstance().copy(expr, newArgs);
			} else {
				
				result = expr;
				
			}
			
			return result;		
		};
	};
}
	

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

	// Remove trailing whitespaces and semicolons
	public static String normalizeQueryString(String queryString) {
		Pattern pattern = Pattern.compile("(\\s|;)+$", Pattern.MULTILINE);
		String result = pattern.matcher(queryString).replaceAll("");
		//String result = queryString.replaceAll(, "");
		
		
		return result;
	}

	
	
	public org.aksw.sparqlify.core.domain.input.ViewDefinition create(ViewDefinition viewDefinition) {
		
		String name = viewDefinition.getName();		
		
		QuadPattern template = new QuadPattern();
		
		for(Quad quad : viewDefinition.getConstructPattern().getList()) {
			//Quad quad = new Quad(Quad.defaultGraphNodeGenerated, triple);
			
			template.add(quad);
		}
		
		VarExprList varExprList = viewDefinition.getViewTemplateDefinition().getVarExprList();
		List<Constraint> constraints = viewDefinition.getConstraints();

		Map<Var, PrefixConstraint> varToPrefixConstraint = new HashMap<Var, PrefixConstraint>();
		if(constraints != null) {
			for(Constraint constraint : constraints) {
				if(constraint instanceof PrefixConstraint) {
					PrefixConstraint c = (PrefixConstraint)constraint;

					varToPrefixConstraint.put(c.getVar(), c);
				} else {
					logger.warn("Unknown constraint type: " + constraint.getClass() + " - " + constraint);
				}
			}
		}
			
		
		
		Multimap<Var, RestrictedExpr> varDefs = HashMultimap.create();
		
		for(Entry<Var, Expr> entry : varExprList.getExprs().entrySet()) {
			Var var = entry.getKey();
			Expr expr = entry.getValue();
			
			RestrictionSetImpl rs = null;
			
			PrefixConstraint c = varToPrefixConstraint.get(var);
			if(c != null) {
				PrefixSet ps = c.getPrefixes();
				RestrictionImpl r = new RestrictionImpl(ps);
				rs = new RestrictionSetImpl(r);
			}
			
			
			
			RestrictedExpr rexpr = new RestrictedExpr(expr, rs);
			
			varDefs.put(var, rexpr);
		}
		
		VarDefinition varDefinition = new VarDefinition(varDefs);

		
		
		// Add str to concat expressions in order to play safe with Sparqlify's type model
		varDefinition.applyExprTransform(ExprTransformerUtils.injectStrsIntoConcats);

		SqlOp relation = viewDefinition.getRelation();
		
		// TODO: I think the adapter should be able to resolve the schema at this stage,
		// Rather than leaving the schema on null.
		
		SqlOp sqlOp = null;
		if(relation == null) { 
			
			logger.warn("No relation given for view '" + name + "', using Select 1");
			Schema schema = schemaProvider.createSchemaForQueryString("SELECT 1");
			sqlOp = new SqlOpQuery(schema, "SELECT 1"); //;null;

		} else if(relation instanceof SqlOpQuery) {

			String rawQueryString = ((SqlOpQuery)relation).getQueryString();
			String queryString = normalizeQueryString(rawQueryString);
			Schema schema = schemaProvider.createSchemaForQueryString(queryString);
			sqlOp = new SqlOpQuery(schema, queryString);

		} else if(relation instanceof SqlOpTable){
			
			String relationName = ((SqlOpTable) relation).getTableName();
			Schema schema = schemaProvider.createSchemaForRelationName(relationName);
			sqlOp = new SqlOpTable(schema, relationName);
			
		} else {
			throw new RuntimeException("Unsupported relation type: " + relation);
		}
		
		Mapping mapping = new Mapping(varDefinition, sqlOp);
		
			
		org.aksw.sparqlify.core.domain.input.ViewDefinition result = new org.aksw.sparqlify.core.domain.input.ViewDefinition(name, template, null, mapping, viewDefinition);
		
		return result;
	}
	
	
	public static List<org.aksw.sparqlify.core.domain.input.ViewDefinition> bridge(SyntaxBridge bridge, Collection<ViewDefinition> viewDefinitions, Logger logger) {

		List<org.aksw.sparqlify.core.domain.input.ViewDefinition> result = new ArrayList<org.aksw.sparqlify.core.domain.input.ViewDefinition>();
		for(ViewDefinition item : viewDefinitions) {
			org.aksw.sparqlify.core.domain.input.ViewDefinition virtualGraph = bridge.create(item);
			
			if(logger != null) {
				Validation.validateView(virtualGraph, logger);
			}
			//candidateSelector.addView(virtualGraph);
			
			result.add(virtualGraph);
		}
		
		return result;
	}
}

