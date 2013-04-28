package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.jena.util.QuadUtils;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_IsNotNull;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpFilter;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.QuadPatternUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * 
 * Create {
 *   ?s rdfs:label ?n . 
 *   ?s ex:age ?a .
 * }
 * With
 *   ?s =
 *   ?n = plainLiteral(?name)
 *   ?a = typedLiteral(?age, xsd:int)
 * From
 *   Student
 * 
 * --> SELECT ... WHERE 
 * 
 * id | name | age
 *  1 | Foo  | 20
 *  2 | Bar  | null
 *  3 | Baz  | 40
 *  
 * We can't just do "WHERE NOT NULL" ...
 * A simple refactoring would be
 * 
 * Create { ?s rdfs:label ?n . }
 * With
 *   ?s =
 *   ?n = plainLiteral(?name)
 * From
 *   [[SELECT * FROM Student WHERE name IS NOT NULL]] --> if we joined that, we could do JOIN(FILTER(r, a), FILTER(s, b)) -> FILTER(JOIN(a, b), rxs)
 *   Take care to rename column references as needed.   
 *
 * Create { ?s rdfs:label ?n . }
 * With
 *   ?s =
 *   ?n = plainLiteral(?name)
 * From
 *   [[SELECT * FROM Student WHERE name IS NOT NULL]]
 *   
 * 
 * 
 * Note: coalesce wouldn't work: The arguments could be NULL so the condidition would be
 * COALESCE(a, b, c) IS NOT NULL... this could be further transformed to a IS NOT NULL OR b IS NOT NULL OR c IS NOT NULL
 * 
 * 
 * @author raven
 *
 *
 * TODO Combine view ViewRefactor class
 */
public class EffectiveViewGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(EffectiveViewGenerator.class);

	/**
	 * FIXME Improve this method
	 * 
	 * A simple nullable check for expressions.
	 * It returns true if any of the referenced columns is nullable
	 * 
	 * 
	 * @param expr
	 * @param schema
	 * @return
	 */
	public static boolean isNullable(Expr expr, Schema  schema) {
		
		boolean result = false;
		// If any of the referenced variables is nullable, the whole expressions might be NULL
		// so we actually need to perform a nullable check... have to append != NULL to the expression
		// ok: we exclude something like: (if(a IS NOT NULL) then NULL else a)
		Set<Var> exprVars = expr.getVarsMentioned();
		
		for(Var columnRef : exprVars) {
			String columnName = columnRef.getName();
			boolean isNullable = schema.isNullable(columnName);
			if(isNullable) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public List<ViewDefinition> transformDummy(ViewDefinition viewDef) {
		
		Collection<ViewDefinition> tmp = Collections.singleton(viewDef);
		List<ViewDefinition> result = new ArrayList<ViewDefinition>(tmp);
		return result;
	}
	
	
	/**
	 * Adds "columnName" IS NOT NULL constraints to the view definitions according to the schema.
	 * 
	 * Note: Optimization of expressions such as (?x IS NOT NULL) AND (?x = 1) -> (?x = 1)
	 * have to happen at a later stage. 
	 * 
	 * 
	 * @param viewDef
	 * @return
	 */
	public List<ViewDefinition> transform(ViewDefinition viewDef) {
		
		List<ViewDefinition> result = new ArrayList<ViewDefinition>();

		boolean disableNullableTransform = true;
		if(disableNullableTransform) {
			result.add(viewDef);
			return result;
		}
		

		Mapping mapping = viewDef.getMapping();
		VarDefinition varDef = mapping.getVarDefinition();
		SqlOp sqlOp = mapping.getSqlOp();
		Schema schema = sqlOp.getSchema();
		
		// For all variable bindings check whether they could be NULL
		Map<Var, Collection<RestrictedExpr>> map = varDef.getMap().asMap();

		/*
		 * Identify nullable SPARQL variables 
		 */
		Set<Var> nullableVars = new HashSet<Var>();
		Set<Expr> nullableExprs = new HashSet<Expr>();
		for(Entry<Var, Collection<RestrictedExpr>> entry : map.entrySet()) {
			
			Var var = entry.getKey();
			Collection<RestrictedExpr> restExprs = entry.getValue();
			
			// If any expression is nullable, the var is nullable 
			boolean areExprsNullable = false;
			for(RestrictedExpr restExpr : restExprs) {
				Expr expr = restExpr.getExpr();
				
				
				boolean testNullable = isNullable(expr, schema);
				if(testNullable) {
					nullableExprs.add(expr);
					
					areExprsNullable = true;
					break;
				}
			}	

			if(areExprsNullable) {
				nullableVars.add(var);
			}
		}
		
		
		/**
		 * For each nullable expressions create the corresponding conditions that must apply
		 * to remove the NULLs
		 * 
		 * 
		 */
		Map<Expr, List<SqlExpr>> exprToConds = new HashMap<Expr, List<SqlExpr>>();
		for(Expr nullableExpr : nullableExprs) {

			List<SqlExpr> sqlExprs = new ArrayList<SqlExpr>();
			
			Set<Var> columnVars = nullableExpr.getVarsMentioned();
			for(Var columnVar : columnVars) {

				String columnName = columnVar.getName();
				
				if(!schema.isNullable(columnName)) {
					continue;
				}
				
				
				TypeToken typeToken = sqlOp.getSchema().getColumnType(columnName);
				//TypeToken typeToken = schema.getColumnType(varName);
				
				S_ColumnRef columnRef = new S_ColumnRef(typeToken, columnName);
				
				SqlExpr sqlExpr = new S_IsNotNull(columnRef);
				sqlExprs.add(sqlExpr);
			}
			
			exprToConds.put(nullableExpr, sqlExprs);
			
//			Collection<RestrictedExpr> restExprs = newVarDef.getDefinitions(nullVar);
//			for(RestrictedExpr restExpr : restExprs) {
//				Expr expr = restExpr.getExpr();
//				Collection<Var> columnVars = expr.getVarsMentioned();			
		}
		
		
		/*
		 * Now: for each quad determine the null-condition
		 *  
		 */
		//List<QuadCondition> quadConds = new ArrayList<QuadCondition>();
		
		Multimap<Set<Var>, Quad> groups = HashMultimap.create();
		
		QuadPattern template = viewDef.getTemplate();
		for(Quad quad : template) {
			
			
			Set<Var> groupNullableVars = new HashSet<Var>();
			for(int i = 0; i < 4; ++i) {
				Node node = QuadUtils.getNode(quad, i);

				if(node.isVariable()) {
					Var var = (Var)node;
					
					if(nullableVars.contains(var)) {
						groupNullableVars.add(var);
						//Expr cond = new E_Bound(new ExprVar(var));
					}
				}
				
				//Collection<Quad> group = varsToQuads.get(nullables);
				/*
				if(group == null) {
					
				}*/
				
			}
			groups.put(groupNullableVars, quad);
		}

		/*
		 * For each group create a new view with the appropriate conditions attached 
		 * 
		 */
		for(Entry<Set<Var>, Collection<Quad>> group : groups.asMap().entrySet()) {
			Set<Var> nullables = group.getKey();
			Collection<Quad> quads = group.getValue();

			QuadPattern newTemplate = QuadPatternUtils.create(quads); 
			
			
			String newName = viewDef.getName() + "?nullables=" + Joiner.on(",").join(nullables);
			
			// Copy all referenced variable definitions
			Set<Var> templateVars = QuadUtils.getVarsMentioned(newTemplate);
			Multimap<Var, RestrictedExpr> newVarDefMap = HashMultimap.create();
			for(Var templateVar : templateVars) {
				Collection<RestrictedExpr> restExprs = varDef.getDefinitions(templateVar);

				newVarDefMap.putAll(templateVar, restExprs);
			}
			VarDefinition newVarDef = new VarDefinition(newVarDefMap);


			// Determine the IS NOT NULL conditions
			Set<Var> notNullVars = new HashSet<Var>();
			//Collection<Clause>()

			//x Set<Collection<SqlExpr>> ors = new HashSet<Collection<SqlExpr>>();
			Set<SqlExpr> ands = new HashSet<SqlExpr>();
			for(Var nullVar : nullables) {
				Collection<RestrictedExpr> restExprs = newVarDef.getDefinitions(nullVar);

				
				for(RestrictedExpr restExpr : restExprs) {
					Expr expr = restExpr.getExpr();
					
					List<SqlExpr> conds = exprToConds.get(expr);
					ands.addAll(conds);
				}

				//x ors.add(ands);
			}
			
			//xList<SqlExpr> conds = SqlExprUtils.toDnf(ors);
			List<SqlExpr> conds = new ArrayList<SqlExpr>(ands);
			
			
//			for(RestrictedExpr restExpr : restExprs) {
//				Expr expr = restExpr.getExpr();
//				
//				Collection<Var> exprVars = expr.getVarsMentioned();
//			}

			SqlOp newSqlOp;
			if(!conds.isEmpty()) {
				newSqlOp = SqlOpFilter.createIfNeeded(sqlOp, conds);
			} else {
				newSqlOp = sqlOp;
			}
			
			Mapping newMapping = new Mapping(newVarDef, newSqlOp);
			
			
			ViewDefinition newViewDef = new ViewDefinition(newName, newTemplate, null, newMapping, viewDef);
			result.add(newViewDef);
		}
		
		for(ViewDefinition viewDefinition : result) {
			logger.debug("Effective View:\n" + viewDefinition);
		}
		
		return result;
	}

}


class QuadCondition {
	private Set<Expr> condiditons;
	private Set<Quad> quads;
}
