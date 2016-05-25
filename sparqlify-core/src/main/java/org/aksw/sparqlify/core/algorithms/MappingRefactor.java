package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprAggregator;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Coalesce;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.Projection;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpExtend;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpGroupBy;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.MappingUnion;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.trash.ExprCommonFactor;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sdb.core.Gensym;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.Aggregator;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;



/**
 * Groups RDF Terms by their signature.
 * 
 * @author raven
 *
 */
//class RdfTermGrouper {
//	public add(E_RdfTerm rdfTerm) {
//		rdfTerm.getSignature
//		
//	}
//}

public class MappingRefactor {


	/*
	public static List<Mapping> refactorToUnion(List<Mapping> ms, Var var) {
		List<Mapping> result = new ArrayList<Mapping>();
		
		for(Mapping m : ms) {
			refactorToUnion(m, var, result);
		}
	
		return result;
	}

	
	group by ?s ?p 
	
	public static List<Mapping> refactorToUnion(Mapping m, Var var, List<Mapping> result) {
		
		if(result == null) {
			result = new ArrayList<Mapping>();
		}
		
		
		VarDefinition varDef = m.getVarDefinition();

		Collection<RestrictedExpr> defs = varDef.getDefinitions(var);
			
		if(defs.size() > 1) {
			for(RestrictedExpr restExpr : defs) {
				
				Multimap<Var, RestrictedExpr> map = HashMultimap.create(varDef.getMap());
				map.removeAll(var);
				map.put(var, restExpr);
				
				VarDefinition newVd = new VarDefinition(map);
				Mapping newM = new Mapping(newVd, m.getSqlOp());
				
				result.add(newM);
			}
		} else {
			result.add(m);
		}

		return result;
	}
	*/

	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param ms
	 * @param groupExprs
	 * @return
	 */
	public static ListMultimap<String, Mapping> groupBy(List<Mapping> ms, List<Expr> groupExprs) {
		
	
		return null;
	}
	
	
	/**
	 * 
	 * 
	 * @param ms
	 * @param groupExpr
	 * @return A list of MappingUnion that can be further grouped.
	 */
	public static ListMultimap<String, Mapping> groupBy(List<Mapping> ms, Expr groupExpr) {
		
		return null;
	}


	/*
	public static Mapping groupBy(Mapping m, VarExprList groupVars, List<ExprAggregator> aggregators, SqlTranslator sqlTranslator, TypeSystem typeSystem, ExprDatatypeNorm exprNormalizer) {
		

		Mapping a = m;
		for(Var var : groupVars.getVars()) {
			Expr expr = groupVars.getExpr(var);
			if(expr == null) {
				expr = new ExprVar(var);
			}
			
			groupBy(a, var, expr, sqlTranslator, exprNormalizer, typeSystem);
			
		}
		
		return null;
	}
	*/
	
	

	
	// For each rdfTerm signature cluster, cluster the contexts by their datatype
	

	
	// If there are multiple rewrites of the group expression, the question is whether we have to
	// duplicate the relation or whether these rewrites have compatible types so that we can
	// coalesce them
	
	// type groups are characterized by a set of super types
	// Any sub type of Int will be placed in the group for 'Int'
	
	public static String createClusterKey(List<SqlExprContext> contexts, ExprDatatypeNorm exprNormalizer)
	{

		
		String result = "";
		for(SqlExprContext context : contexts) {
			
			E_RdfTerm e = context.getRewrite().asConstRdfTerm();
			//context.getRewrite().getProjection().

			//Map<String, TypeToken> typeMap = context.getRewrite().getProjection().getTypeMap();
			// TODO We should group types such as int, byte, short, etc together
			//TypeToken groupType = getTypeGroup(context.getSqlExpr().getDatatype(),);
			
			//Expr valueType = exprNormalizer.normalize(e.getLexicalValue(), typeMap);
			//result += "" + e.getType() + valueType + e.getDatatype();
			result += "| " + e.getType() + e.getDatatype();
		}
		
		return result;
	}
	
	
	public static TypeToken getTypeGroup(TypeToken type, TypeSystem typeSystem) {
		Set<TypeToken> typeGroupTypes = new HashSet<TypeToken>(Arrays.asList(TypeToken.Int));

		TypeToken result = null;
		
		// Check if the type is compatible with any of the groups
		for(TypeToken superType : typeGroupTypes) {
			
			
			boolean belongsToGroup = typeSystem.isSuperClassOf(type, superType);
			if(belongsToGroup) {
				if(result != null) {
					throw new RuntimeException("Type " + type + " already reassigned to group " + superType + " although already a member of " + result);
				}
				
				result = superType;
			}
		}
		
		if(result == null) {
			result = type;
		}
		
		return result;
	}
	
	
	
	public static <T, U extends Collection<T>> List<List<T>> transpose(Collection<U> items) {
		List<List<T>> result = new ArrayList<List<T>>();

		// Compute maximum length of a row
		for(U c : items) {
			//n = Math.max(n, c.size());
			List<T> tmp = new ArrayList<T>(c.size());
			result.add(tmp);
		}

//		for(int i = 0; i < n; ++i) {
//			List<T> tmp = new ArrayList<T>();
//			result.add(null);
//		}
		
		int y = 0;
		for(U c : items) {
			int x = 0;
			
			List<T> col = result.get(x);
			for(T t : c) {
				
				col.set(y, t);

				++x;
			}			
			++y;
		}
		
		return result;
	}		

	
//	ExprBindingSubstitutor subst = new ExprBindingSubstitutorImpl();
//	e1 = subst.substitute(sparqlExpr, binding);
	//Map<List<SqlExprContext>> varContexts = new ArrayList<List<>>

	class RdfTermTypeGroup {
		private String groupKey;

		// Map Cluster
		private Map<TypeToken, SqlTypeGroup> groupTypeToMembers;
	}
	
	class SqlTypeGroup  {
		RdfTermTypeGroup parent;

		TypeToken groupType;
		
		private List<SqlExprContext> contexts;
	}
	
	
	
	/**
	 * 
	 * 
	 * @param ms
	 * @param expr
	 * @return
	 */
	public static MappingUnion groupBy(Mapping m, VarExprList groupVars, List<ExprAggregator> aggregators, SqlTranslator sqlTranslator, TypeSystem typeSystem, ExprDatatypeNorm exprNormalizer) {
		MappingUnion result;

		
		if(groupVars.isEmpty()) {
			
			Generator generator = Gensym.create("X");
			Mapping mapping = applyAggregators(m, aggregators, generator, typeSystem, sqlTranslator);
			result = new MappingUnion();
			result.add(mapping);
			
		} else {
			//public static MappingUnion groupBy(Mapping m, VarExprList groupVars, List<ExprAggregator> aggregators, SqlTranslator sqlTranslator, TypeSystem typeSystem, ExprDatatypeNorm exprNormalizer) {

			result = groupByWithExprs(m, groupVars, aggregators, sqlTranslator, typeSystem, exprNormalizer);
		}
		
		return result;
	}


	public static MappingUnion groupByWithExprs(Mapping m, VarExprList groupVars, List<ExprAggregator> aggregators, SqlTranslator sqlTranslator, TypeSystem typeSystem, ExprDatatypeNorm exprNormalizer) {
		MappingUnion result = new MappingUnion();

		List<Var> vars = groupVars.getVars();
		
		/*
		 * Rewrite all involved group by expressions to SQL
		 */
		List<List<SqlExprContext>> contextsLists = new ArrayList<List<SqlExprContext>>();//ArrayListMultimap.create();
		for(Var var : vars) {
			Expr expr = groupVars.getExpr(var);
			if(expr == null) {
				expr = new ExprVar(var);
			}

			List<SqlExprContext> contexts = MappingOpsImpl.createExprSqlRewrites(expr, m, sqlTranslator);

			contextsLists.add(contexts);
		}

		
		/*
		 * Group the SQL rewritten expressions by their RDF term signature
		 * i.e. uri(...), plainLiteral(...), typedLiteral(..., some:type)
		 */
		Multimap<String, List<SqlExprContext>> sigClusters = ArrayListMultimap.create();

		CartesianProduct<SqlExprContext> cart = CartesianProduct.create(contextsLists);
		for(List<SqlExprContext> c : cart) {
			
			// The cartesion product is just a view - but we want copies!
			// TODO This is dangerous, make the view behaviour require a specific flag
			c = new ArrayList<SqlExprContext>(c);
			
			String clusterKey = createClusterKey(c, exprNormalizer);
			sigClusters.put(clusterKey, c);
		}
		

		Set<String> columnNameBlacklist = new HashSet<String>(m.getSqlOp().getSchema().getColumnNames()); //MappingOpsImpl.getReferencedColumnNames(mappings);
		Generator aliasGenUnion = GeneratorBlacklist.create("X", columnNameBlacklist);
		ExprCommonFactor factorizer = new ExprCommonFactor(aliasGenUnion);


		
		
		
		/*
		 * Within each of the groups, we group again by the
		 * involved SQL types
		 * 
		 * Types within a group can be coalesced
		 * e.g. int, byte, short belong to the group int 
		 */
		Map<String, Multimap<String, List<SqlExprContext>>> groups = new HashMap<String, Multimap<String, List<SqlExprContext>>>();
		for(Entry<String, Collection<List<SqlExprContext>>> sigEntry : sigClusters.asMap().entrySet()) {
		
			String sigKey = sigEntry.getKey();
			Collection<List<SqlExprContext>> sigCluster = sigEntry.getValue();
			
			Multimap<String, List<SqlExprContext>> typeGroup = ArrayListMultimap.create();
			groups.put(sigKey, typeGroup);
			
			for(List<SqlExprContext> rewriteEntry : sigCluster) {
				
				String typeKey = "";
				for(SqlExprContext context : rewriteEntry) {
				
					TypeToken type = context.getSqlExpr().getDatatype(); 
					TypeToken groupType = getTypeGroup(type, typeSystem);
					
					typeKey += "| " + groupType;
				}
				
				typeGroup.put(typeKey, rewriteEntry);
			}
		}

		/*
		 * Process the groups.
		 * 
		 */
		for(Multimap<String, List<SqlExprContext>> sigEntry : groups.values()) {
			Collection<List<SqlExprContext>> typeGroup = sigEntry.values();

			// Within a type group, vertically group the expressions
			List<E_RdfTerm> grouped = new ArrayList<E_RdfTerm>();
			for(int i = 0; i < vars.size(); ++i) {
				grouped.add(null);
			}
			
			// Coalesce all the sql exprs
//			List<List<Projection>> sqlExprss = new  ArrayList<List<Projection>>();
//			for(int i = 0; i < vars.size(); ++i) {
//				sqlExprss.add(new ArrayList<Projection>());
//			}

			
			// Vertically iterate each expression
			// First pass: create the E_RdfTerm object
			for(List<SqlExprContext> rewriteEntry : typeGroup) {
				for(int i = 0; i < vars.size(); ++i) {
					SqlExprContext rewrite = rewriteEntry.get(i);
					
					E_RdfTerm gTerm = grouped.get(i);
					E_RdfTerm rdfTerm = rewrite.getRewrite().asConstRdfTerm();
					
					E_RdfTerm tmp;
					if(gTerm == null) {
						tmp = rdfTerm;
					} else {
						tmp = (E_RdfTerm)factorizer.transformMM(gTerm, rdfTerm);
					}
					grouped.set(i, tmp);
				}
			}

//			Table<Integer, Integer, Integer> x;
//			x.r
			// Second pass: Map the Sql expressions
			
			//rowToColsToProj
			// colToRowsToProjs
			List<List<Projection>> sqlExprss = new  ArrayList<List<Projection>>(vars.size());
			for(int i = 0; i < vars.size(); ++i) {
				E_RdfTerm gTerm = grouped.get(i);

				List<Projection> sqlExprs = new ArrayList<Projection>(typeGroup.size());

				for(List<SqlExprContext> rewriteEntry : typeGroup) {
					SqlExprContext rewrite = rewriteEntry.get(i);
					//List<Projection> sqlExprs = sqlExprss.get(i);
					sqlExprss.add(sqlExprs);
				

					
					Projection proj = new Projection();
					List<Expr> args = gTerm.getArgs();
					for(int j = 0; j < args.size(); ++j) {
						Expr arg = args.get(j);
						
						if(arg.isVariable()) {
							Var var = arg.asVar();
							
							ExprSqlRewrite r = rewrite.getRewrite();
							E_RdfTerm t = r.getRdfTermExpr();
							Expr e = t.getArg(j + 1);
							Var v = e.asVar();

							SqlExpr sqlExpr = r.getProjection().getNameToExpr().get(v.getVarName());
							

							proj.put(var.getName(), sqlExpr);
						} else if(arg.isConstant()) {
							// Nothing to do
						} else {
							throw new RuntimeException("Should not happen");
						}
						
						
					}
					sqlExprs.add(proj);					
//					List<Expr> args = tmp.getArgs();
//					for(int j = 0; j gTerm.getArgs())
//					sqlExprss.get(i).add(rewrite.getSqlExpr());
//
//					
//					
//					E_RdfTerm rdfTerm = rewrite.getRewrite().asConstRdfTerm();
				}
			}
			
							
			// TODO We must preserve order in the coalesce expressions:
			// E.g. if we are grouping (?label, ?lang) (?x, ?y), then we
			// need coalesce(?label, ?lang) (?x, ?y) and not e.g. (?y, ?x)
			// so that ?label matches again with ?x
			
			
			Projection proj = new Projection();
			List<String> groupColumnNames = new ArrayList<String>();
			//SqlOpExtend 
			
			//List<SqlExpr> sqlGroup = new ArrayList<SqlExpr>(vars.size());
			Multimap<Var, RestrictedExpr> varDef = HashMultimap.create();
			for(int i = 0; i < vars.size(); ++i) {
				Var var = vars.get(i);
				E_RdfTerm group = grouped.get(i);
				
				List<Projection> sqlExprs = sqlExprss.get(i);
				
				List<Expr> args = group.getArgs();
				List<Expr> newArgs = new ArrayList<Expr>(args.size());
				for(Expr arg : args) {
					Expr newArg;
					if(arg.isConstant()) {
						newArg = arg;
					} else if(arg.isVariable()) {

						String varName = arg.getVarName();
						
						List<SqlExpr> ss = new ArrayList<SqlExpr>();
						for(Projection p : sqlExprs) {
							SqlExpr s = p.getNameToExpr().get(varName);
							ss.add(s);
						}

						SqlExpr sqlCoalesce = S_Coalesce.create(ss);
						//sqlGroup.add(sqlCoalesce);
						String columnName = aliasGenUnion.next();
						groupColumnNames.add(columnName);
						proj.put(columnName, sqlCoalesce);
						
						newArg = new ExprVar(columnName);
						
					} else {
						throw new RuntimeException("Should not happen");
					}
					
					newArgs.add(newArg);
				}
				
				E_RdfTerm groupTerm = new E_RdfTerm(newArgs);				
				varDef.put(var, new RestrictedExpr(groupTerm));
				
				//System.out.println("" + group + " --- " + sqlExprs);
			}

			
			
			
			
			// Get the variables referenced by the aggregators
			Set<Var> varRefs = new HashSet<Var>();
			for(ExprAggregator exprAgg : aggregators) {
				Aggregator agg = exprAgg.getAggregator();
//				Expr expr = agg.getExpr();
//				if(expr == null) {
//					continue;
//				}
//				
//				Set<Var> tmp = expr.getVarsMentioned();
//				varRefs.addAll(tmp);
				for(Expr expr : agg.getExprList()) {
	              Set<Var> tmp = expr.getVarsMentioned();
	              varRefs.addAll(tmp);				    
				}
			}
			
			// Remove the group variables
			varRefs.removeAll(varDef.keySet());
			
			// Add the new variables to the def
			for(Var varRef : varRefs) {
				Collection<RestrictedExpr> tmp = m.getVarDefinition().getDefinitions(varRef);
				varDef.putAll(varRef, tmp);
			}
			
			
			// Deal with the aggregators
			//for()
			SqlOp subOp = m.getSqlOp();

			List<SqlExpr> groupByExprs = new ArrayList<SqlExpr>(groupColumnNames.size());
			for(String columnName : groupColumnNames) {
				SqlExpr sqlExpr = proj.getNameToExpr().get(columnName);
				groupByExprs.add(sqlExpr);
			}
			
			SqlOpGroupBy sqlOp = SqlOpGroupBy.create(subOp, groupByExprs, new ArrayList<SqlExprAggregator>());

			
			VarDefinition vd = new VarDefinition(varDef);
			SqlOpExtend sqlOpExtend = SqlOpExtend.create(sqlOp, proj);
			// TODO: Project to only the referenced columns
			
			Mapping mapping = new Mapping(vd, sqlOpExtend);
			
			
			Mapping done = applyAggregators(mapping, aggregators, aliasGenUnion, typeSystem, sqlTranslator);
			
//			
//			Multimap<Var, RestrictedExpr> varDef2 = HashMultimap.create();
//			Projection proj2 = new Projection();
//			for(ExprAggregator exprAgg : aggregators) {
//				Var var = exprAgg.getVar();
//				Aggregator agg = exprAgg.getAggregator();
//				
//				ExprSqlRewrite rewrite = MappingOpsImpl.rewrite(mapping, agg, aliasGenUnion, typeSystem, sqlTranslator);
//				
//				E_RdfTerm rdfTerm = rewrite.getRdfTermExpr();
//				
//				varDef2.put(var, new RestrictedExpr(rdfTerm));
//				proj2.add(rewrite.getProjection());
//			}
//			
//			SqlOpExtend sqlOpExtend2 = SqlOpExtend.create(sqlOpExtend, proj2);
//			VarDefinition vd2 = new VarDefinition(varDef2);
//			Mapping done = new Mapping(vd2, sqlOpExtend2);
			
			
			
//			Map<String, TypeToken> typeMap = u.getSqlOp().getSchema().getTypeMap();
//			List<SqlExpr> columnRefs = new ArrayList<SqlExpr>();
//			for(String columnName : columnNames) {
//				TypeToken type = typeMap.get(columnName);
//				
//				SqlExpr expr = new S_ColumnRef(type, columnName);
//				columnRefs.add(expr);
//			}
//			
//			List<SqlExprAggregator> sqlAggregators = new ArrayList<SqlExprAggregator>();
//			
//			SqlOpGroupBy sqlOpGroupBy = SqlOpGroupBy.create(u.getSqlOp(), columnRefs, sqlAggregators);
//			

			//public ExprSqlRewrite rewrite(Mapping mapping, Aggregator agg, Generator generator) {

			
			
		

			
			// TODO Clone the mapping
			// TODO Project only to the referenced columns
			// TODO Apply aggregators to the mapping
		
			result.add(done);
			//System.out.println("Added group");
		}
	
		return result;
	}		

	public static Mapping applyAggregators(Mapping mapping, List<ExprAggregator> aggregators, Generator generator, TypeSystem typeSystem, SqlTranslator sqlTranslator) {
		
		Multimap<Var, RestrictedExpr> varDef2 = HashMultimap.create();
		varDef2.putAll(mapping.getVarDefinition().getMap());
		
		Projection proj2 = new Projection();
		for(ExprAggregator exprAgg : aggregators) {
			Var var = exprAgg.getVar();
			Aggregator agg = exprAgg.getAggregator();
			
			ExprSqlRewrite rewrite = MappingOpsImpl.rewrite(mapping, agg, generator, typeSystem, sqlTranslator);
			
			E_RdfTerm rdfTerm = rewrite.getRdfTermExpr();
			
			varDef2.put(var, new RestrictedExpr(rdfTerm));
			proj2.add(rewrite.getProjection());
		}
		
		
		
		//SqlOpExtend sqlOpExtend2 = SqlOpExtend.create(sqlOpExtend, proj2);
		SqlOpExtend sqlOpExtend2 = SqlOpExtend.create(mapping.getSqlOp(), proj2);
		VarDefinition vd2 = new VarDefinition(varDef2);
		Mapping result = new Mapping(vd2, sqlOpExtend2);
		return result;
	}
	
//		for(List<SqlExprContext> rewriteEntry : typeGroup) {
//		
//			System.out.println("::: " + rewriteEntry);
//		}
//		System.out.println("---");
		
		/*
		 * 
		 * 
		 */
//		for(Collection<List<SqlExprContext>> cluster : sigClusters.asMap().values()) {
//			
//			List<E_RdfTerm> grouped = new ArrayList<E_RdfTerm>();
//			for(int i = 0; i < vars.size(); ++i) {
//				grouped.add(null);
//			}
//			
//			// Coalesce all the sql exprs
//			List<List<SqlExpr>> sqlExprss = new  ArrayList<List<SqlExpr>>();
//			for(int i = 0; i < vars.size(); ++i) {
//				sqlExprss.add(new ArrayList<SqlExpr>());
//			}
//
//			
//			// Vertically group each expression
//			for(List<SqlExprContext> member : cluster) {
//				for(int i = 0; i < vars.size(); ++i) {
//					SqlExprContext rewrite = member.get(i);
//					
//					E_RdfTerm gTerm = grouped.get(i);
//					E_RdfTerm rdfTerm = rewrite.getRewrite().asConstRdfTerm();
//					
//					E_RdfTerm tmp;
//					if(gTerm == null) {
//						tmp = rdfTerm;
//					} else {						
//						tmp = (E_RdfTerm)factorizer.transformMM(gTerm, rdfTerm);
//					}
//					grouped.set(i, tmp);
//					
//					sqlExprss.get(i).add(rewrite.getSqlExpr());
//				}
//			}
//
//
////			for(int i = 0; i < vars.size(); ++i) {
////				for(List<SqlExprContext> member : cluster) {
////			}
////			
////			
//			// Now we only have 1 grouped expression for each variable
//			for(int i = 0; i < vars.size(); ++i) {
//				Var var = vars.get(i);
//				E_RdfTerm group = grouped.get(i);
//				
//				List<SqlExpr> sqlExprs = sqlExprss.get(i);
//				
//				SqlExpr sqlCoalesce = S_Coalesce.create(sqlExprs);
//				
//				System.out.println(group + " --- " + sqlCoalesce);
//			}
//
//		}
//		
		
//		
//			//List<SqlExprContext> 
//			
//			// For each signature there is now a list of context rewrites		
//			// Cluster the contexts first by the rdfTerm signature
//	
//
//			// For each exprCluster, coalesce the expressions
//			
//			//contextexprCluster.values();
//			
//			List<SqlExpr> sqlExprs = new ArrayList<SqlExpr>(cs.size());
//			for(SqlExprContext context : exprCluster.values()) {
//				sqlExprs.add(context.getSqlExpr());
//
////				sqlCoalesce = S_Coalesce.create(coalesceArgs);			
////				System.out.println(context);
//			}
//			
//			SqlExpr sqlCoalesce = S_Coalesce.create(sqlExprs);
//			System.out.println("COALESCE: " + sqlCoalesce);
//			
//			
//			// Clone the mapping
//			Multimap<Var, RestrictedExpr> newVarDef = HashMultimap.create(m.getVarDefinition().getMap());
//			
//			// Replace the mapping for the grouping variable
//			newVarDef.removeAll(var);
//
//			newVarDef.put(var, new RestrictedExpr(E_RdfTerm.createUri(NodeValue.makeString("test"))));
//			
//			
//			// TODO I think we need to filter the sqlOp to those rows for which the expression applies?
//			Mapping newMapping = new Mapping(new VarDefinition(newVarDef), m.getSqlOp());
//
//			result.add(newMapping);			
//		}
//		
//		
//		System.out.println(result);
//		
//		// Each of the cluster's entries can be coalesced

		
		
		// Important: If we clone the mapping by an expression, we need to make sure we do not duplicate/lose any
		// of the NULL values
		
	

	
	/**
	 * Given a list of mappings, create groups of them with the following properties:
	 * All members of a group must have the same datatypes for their term constructor args
	 * e.g. rdfTerm(int, string, string, string), rdfTerm(int, float, string, string) 
	 * 
	 * 
	 */
	public static ListMultimap<String, Mapping> groupByOld(ExprDatatypeNorm exprNormalizer, List<Mapping> ms, List<Var> vars) {
		
		//Multimap<String, ArgExpr> cluster = HashMultimap.create();
		ListMultimap<String, Mapping> cluster = LinkedListMultimap.create();
		
		for(Mapping m : ms) {

			Map<String, TypeToken> tmpTypeMap = m.getSqlOp().getSchema().getTypeMap();

			List<String> hashes = new ArrayList<String>(vars.size());
			for(Var var : vars) {

				Collection<RestrictedExpr> defs = m.getVarDefinition().getDefinitions(var);
				
				Expr expr;


				if(defs.size() > 1) {
					throw new RuntimeException("Encountered multiple variable definitions during group by. Var: " + var + " vardef: " + m.getVarDefinition()); // + ", Mapping: " );
				}
				else if(defs.isEmpty()) {

					// Example If only name is defined, but we request "GROUP BY name, age"
					// then this is the same as grouping by name only, since age is implicitely NULL

					// TODO Do we have to bind name to NULL?
					
					//expr = 
					//throw new RuntimeException("Cannot group by " + var + " because mapping does not define it. Mapping: " + m);
					expr = null;
				}
				else {
					RestrictedExpr restExpr = defs.iterator().next();
					expr = restExpr.getExpr();					
				}
				
				
				String hash;
				if(expr == null) {
					hash = "null";
				} else {
					Expr datatypeNorm = exprNormalizer.normalize(expr, tmpTypeMap);
					hash = datatypeNorm.toString();
				}
				
				hashes.add(hash);
			}
			
			String key = Joiner.on(",").join(hashes);
			
		
			cluster.put(key, m);
		}

		return cluster;
	}

	
	
	/**
	 * If there are multiple definitions for any of the specified variables,
	 * each definition results in a new mapping.
	 * The result is then a union of these mappings.
	 * 
	 * Note that this should only be used for corner cases where for some reason
	 * the union of mappings got lost, and we have a single mapping with multiple
	 * var definitions.
	 * 
	 * @param m
	 */
	public static List<Mapping> refactorToUnion(Mapping m, List<Var> tmpVars) {
		
				
		//if(true) throw new RuntimeException("test");
		
		
		
		List<Mapping> result = new ArrayList<Mapping>();
		
		VarDefinition varDef = m.getVarDefinition();
		
		List<Var> vars = new ArrayList<Var>(tmpVars.size());
		List<Collection<RestrictedExpr>> c = new ArrayList<Collection<RestrictedExpr>>(tmpVars.size());

		
		if(tmpVars.isEmpty()) {
			result.add(m);
			return result;
		}
		
		for(Var var : tmpVars) {
			
			Collection<RestrictedExpr> defs = varDef.getDefinitions(var);
			if(defs.isEmpty()) {
				continue;
			}
			
			vars.add(var);
			c.add(defs);
		}

		Multimap<Var, RestrictedExpr> baseMap = HashMultimap.create(varDef.getMap());		
		baseMap.keySet().removeAll(vars);

		CartesianProduct<RestrictedExpr> cart = CartesianProduct.create(c);

		
		for(List<RestrictedExpr> item : cart) {
			
	
			Multimap<Var, RestrictedExpr> map = HashMultimap.create(varDef.getMap());
			for(int i = 0; i < vars.size(); ++i) {
				Var var = vars.get(i);
				RestrictedExpr restExpr = item.get(i);
				
				map.put(var, restExpr);
			}
			
			VarDefinition newVd = new VarDefinition(map);
			Mapping newM = new Mapping(newVd, m.getSqlOp());
			
			result.add(newM);
		}
		

		/*
		OpDisjunction result = null;
		return result;
		*/
		return result;
	}
	
	
	
	
//	public static void trySplitUnion(SqlOp sqlOp, Set<String> columnNames) {
//		if(sqlOp instanceof SqlOpUnionN) {
//			splitUnion((SqlOpUnionN)sqlOp, columnNames);
//		}
//	}
//	
//	public static Map<String, SqlExpr> filterNotNull(Map<String, SqlExpr> map) {
//		Map<String, SqlExpr> result = new HashMap<String, SqlExpr>();
//		
//		for(Entry<String, SqlExpr> entry : map.entrySet()) {
//			String name = entry.getKey();
//			SqlExpr sqlExpr = entry.getValue();
//			
//			if(sqlExpr.isConstant()) {
//				Object obj = sqlExpr.asConstant().getValue().getValue();
//				if(obj == null) {
//					continue;
//				}
//			}
//			
//			//sqlExpr instanceof S_Constant.
//			
//			result.put(name, sqlExpr);
//		}
//		
//		return result;
//	}
//	
//	public static void splitUnion(SqlOpUnionN union, Set<String> columnNames) {
//
//		SqlOpSelectBlockCollector collector = new SqlOpSelectBlockCollectorImpl();
//		
//		for(SqlOp member : union.getSubOps()) {
//		
//			SqlOpSelectBlock block = (SqlOpSelectBlock)collector.transform(member);
//			
//			Projection proj = block.getProjection();
//			
//			Map<String, SqlExpr> tmp = proj.getNameToExpr();
//			Map<String, SqlExpr> nameToExpr = filterNotNull(tmp);
//			
//			System.out.println(nameToExpr);
////			SqlOpProject project = (SqlOpProject)member;
////			
////			project.get
//			
//			
//		}
//	}

}
