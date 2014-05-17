package org.aksw.sparqlify.inverse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.database.Clause;
import org.aksw.sparqlify.database.NestedNormalForm;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class SparqlSqlInverseMapperImpl
	implements SparqlSqlInverseMapper
{
	private CandidateViewSelectorImpl candidateViewSelector;
	private SqlTranslator sqlTranslator;


	public SparqlSqlInverseMapperImpl(CandidateViewSelectorImpl candidateViewSelector, SqlTranslator sqlTranslator) {
		this.candidateViewSelector = candidateViewSelector;
		this.sqlTranslator = sqlTranslator;
	}

//	public static List<Sq$lExpr> swapVarFirst(List<SqlExpr> sqlExpr) {
//		
//	}

	@Override
	public List<SparqlSqlInverseMap> map(Quad quad) {
		Set<ViewQuad<ViewDefinition>> viewQuads = getCandidateViews(candidateViewSelector, quad);
		
		
		List<SparqlSqlInverseMap> result = new ArrayList<SparqlSqlInverseMap>();
		
		/*
		 * For each view-quad: Try to figure out, what values the
		 * underlying table needs no have in order to yield the quad.
		 */
		for(ViewQuad<ViewDefinition> viewQuad : viewQuads) {
			ViewDefinition viewDef = viewQuad.getView();
			VarDefinition varDef = viewDef.getMapping().getVarDefinition();
			
			Map<String, TypeToken> typeMap = viewDef.getMapping().getSqlOp().getSchema().getTypeMap();
			
			//ViewDefinitionNormalizer viewDefNormalizer = new ViewDefinitionNormalizer();
			//ViewDefinition viewDef = viewDefNormalizer.normalize(tmpViewDef);		

			
			// Retrieve the view's table
			SqlOp tmpTable = viewDef.getMapping().getSqlOp();
			if(!(tmpTable instanceof SqlOpTable)) {
				throw new RuntimeException("Not supported: " + tmpTable);
			}
			//SqlOpTable table = (SqlOpTable)tmpTable;
			
			//table.g
			
			
			Quad q = viewQuad.getQuad();
			
			ExprList exprs = new ExprList();

			for(int i = 0; i < 4; ++i) {
				Node n = QuadUtils.getNode(q, i);
				if(!n.isVariable()) {
					continue;
				}
				
				Var v = (Var)n;
				
				Node insertNode = QuadUtils.getNode(quad, i);
				if(insertNode.isVariable() || insertNode.isBlank()) {
				    continue;
				}
			
				//exprs.add(new E_Equals(NodeValue.makeNode(quad.getSubject()), NodeValue.makeNode(insertQuad.getSubject())));
				exprs.add(new E_Equals(new ExprVar(v), NodeValue.makeNode(insertNode)));
			}
			
			Expr condition = ExprUtils.andifyBalanced(exprs);
//			System.out.println("Condition: " + condition);
//			System.out.println("VarDef   : " + varDef);

			
			
			/*
			 * 
			 */
			List<SqlExpr> sqlExprs = MappingOpsImpl.createSqlConditionItems(condition, varDef, typeMap, sqlTranslator);

			Map<S_ColumnRef, SqlValue> columnToValue = new HashMap<S_ColumnRef, SqlValue>();
			
			for(SqlExpr sqlExpr : sqlExprs) {
				List<SqlExpr> args = sqlExpr.getArgs();
				SqlExpr left = args.get(0);
				SqlExpr right = args.get(1);

				// Get rid of redundant expression items (such as conversion from string to string) 
                // TODO Move this to a generic optimization function
				if(left.isFunction()) {
				    SqlExprFunction fn = left.asFunction();
				    if(fn.getName().equals("str@str")) {
				        left = fn.getArgs().get(0);
				    }
				}
			
				
				
				if(left.isConstant()) {
					SqlExpr tmp = right;
					right = left;
					left = tmp;
				}
				
				if(!left.isVariable()) {
					throw new RuntimeException("Variable expected, instead got: " + left);
				}

				if(!right.isConstant()) {
					throw new RuntimeException("Variable expected, instead got: " + right);
				}
				
				S_ColumnRef columnRef = (S_ColumnRef)left.asVariable();
				SqlValue sqlValue = right.asConstant().getValue();
				
				//System.out.println(" " + left.getClass() + ", " + right.getClass());
				columnToValue.put(columnRef, sqlValue);
			}
			
			
			SparqlSqlInverseMap inverseMap = new SparqlSqlInverseMap(quad, viewDef, viewQuad.getQuad(), columnToValue);

			result.add(inverseMap);
		}

		return result;
	}
	
	
	
	/**
	 * Utility function that returns the set of candidate views for
	 * a variable-free quad.
	 * 
	 * @param candidateSelector
	 * @param quad
	 * @return
	 */
	public static Set<ViewQuad<ViewDefinition>> getCandidateViews(CandidateViewSelectorImpl candidateSelector, Quad quad) {
		Var g = Var.alloc("g");
		Var s = Var.alloc("s");
		Var p = Var.alloc("p");
		Var o = Var.alloc("o");
		
		Node gv = quad.getGraph();
		Node sv = quad.getSubject();
		Node pv = quad.getPredicate();
		Node ov = quad.getObject();
		
		Quad tmpQuad = new Quad(g, s, p, o);
		
		RestrictionManagerImpl r = new RestrictionManagerImpl();
		/*
		ExprList exprs = new ExprList();
		exprs.add(new E_Equals(new ExprVar(g), NodeValue.makeNode(gv)));
		exprs.add(new E_Equals(new ExprVar(s), NodeValue.makeNode(sv)));
		exprs.add(new E_Equals(new ExprVar(p), NodeValue.makeNode(pv)));
		exprs.add(new E_Equals(new ExprVar(o), NodeValue.makeNode(ov)));

		Clause clause = new Clause(new HashSet<Expr>(exprs.getList()));
		*/
	
		
		/*
		 * Create a conjunctive normal form (literals ORed, clauses ANDed),
		 * with one clause for constraint of {g, s, p, o}
		 */
		Set<Clause> clauses = new HashSet<Clause>();
		
		Var[] vars = new Var[]{g, s, p, o};
		Node[] nodes = new Node[]{gv, sv, pv, ov};
		
		for(int i = 0; i < 4; ++i) {
		    Var v = vars[i];
		    Node n = nodes[i];

		    if(!n.isVariable() && !n.isBlank()) {		    
		      clauses.add(new Clause(new E_Equals(new ExprVar(v), NodeValue.makeNode(n))));
		    }

		}

		/*
		clauses.add(new Clause(new E_Equals(new ExprVar(g), NodeValue.makeNode(gv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(s), NodeValue.makeNode(sv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(p), NodeValue.makeNode(pv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(o), NodeValue.makeNode(ov))));
		*/
		NestedNormalForm nnf = new NestedNormalForm(clauses);
		
		r.stateCnf(nnf);
		
		
		/*
		 * BUG: stating a Node does not change the CNF (should it do that?)
		r.stateNode(g, gv);
		r.stateNode(s, sv);
		r.stateNode(p, pv);
		r.stateNode(o, ov);
		*/
		
		// TODO The quad may only consist of variables....
		Set<ViewQuad<ViewDefinition>> result = candidateSelector.findCandidates(tmpQuad, r);
		return result;
	}
}