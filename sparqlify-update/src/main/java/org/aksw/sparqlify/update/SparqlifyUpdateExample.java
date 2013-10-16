package org.aksw.sparqlify.update;

import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.ViewQuad;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.database.Clause;
import org.aksw.sparqlify.database.NestedNormalForm;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.RDF;

public class SparqlifyUpdateExample {
	private static final Logger logger = LoggerFactory.getLogger(SparqlifyUpdateExample.class);
	
	
	public static void main(String[] args)
		throws Exception
	{
		/*
		 * Register some custom functions to the Jena framework
		 */
		RdfViewSystemOld.initSparqlifyFunctions();
		
		
		//TypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
		SqlTranslator sqlTranslator = SparqlifyUtils.createSqlRewriter(); //new SqlTranslatorImpl(datatypeSystem);
		//ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();

		
		/*
		 * Create a simple test database
		 */
		DataSource dataSource = SparqlifyUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();
		
		/*
		 * Retrieve 
		 * 
		 */
		Schema schema = Schema.create(conn);
		System.out.println(schema.getPrimaryKeys());

		
		/*
		 *  typeAliases for the H2 datatype
		 *  
		 *  This is somewhat hacky, the idea is to map database specific types to database independent ones
		 */
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		
		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		ViewDefinition personView = vdf.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?id)) ?t = plainLiteral(?name) From person");
		ViewDefinition deptView = vdf.create("Prefix ex:<http://ex.org/> Create View dept As Construct { ?s a ex:Department ; ex:name ?t } With ?s = uri(concat('http://ex.org/dept/', ?id) ?t = plainLiteral(?name) From dept");
		ViewDefinition personToDeptView = vdf.create("Prefix ex:<http://ex.org/> Create View person_to_dept As Construct { ?p ex:worksIn ?d } With ?p = uri(concat('http://ex.org/person/', ?person_id) ?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) From person_to_dept");

		
		
		CandidateViewSelectorImpl candidateSelector = new CandidateViewSelectorImpl();
		candidateSelector.addView(personView);
		candidateSelector.addView(deptView);
		candidateSelector.addView(personToDeptView);

		
		/*
		 * Let's assume the following quad should be inserted.
		 * The procedure is as follows:
		 * - For every view, find all the quads which could yield the one being inserted.
		 * - Make sure all the views make use of table names (supporting inserts on queries is tough)
		 * - Based on the view's variable definition, try to figure out, what the SQL columns were
		 * 
		 */
		Quad insertQuad = new Quad(Quad.defaultGraphNodeGenerated, Node.createURI("http://ex.org/person/5"), RDF.type.asNode(), Node.createURI("http://ex.org/Person"));
		
		Set<ViewQuad<ViewDefinition>> viewQuads = getCandidateViews(candidateSelector, insertQuad);
		
		
		/*
		 * Print out which view's quad-patterns may yield the quad being modified. 
		 */
		System.out.println("# View quads: " + viewQuads.size());
		System.out.println("View quads: " + viewQuads);
		
		
		
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
				throw new Exception("Not supported: " + tmpTable);
			}
			SqlOpTable table = (SqlOpTable)tmpTable;
			
			//table.g
			
			
			Quad quad = viewQuad.getQuad();
			
			ExprList exprs = new ExprList();

			for(int i = 0; i < 4; ++i) {
				Node n = QuadUtils.getNode(quad, i);
				if(!n.isVariable()) {
					continue;
				}
				
				Var v = (Var)n;
				
				Node insertNode = QuadUtils.getNode(insertQuad, i);
			
			
				//exprs.add(new E_Equals(NodeValue.makeNode(quad.getSubject()), NodeValue.makeNode(insertQuad.getSubject())));
				exprs.add(new E_Equals(new ExprVar(v), NodeValue.makeNode(insertNode)));
			}
			
			Expr condition = ExprUtils.andifyBalanced(exprs);
			System.out.println("Condition: " + condition);
			System.out.println("VarDef   : " + varDef);

			
			
			/*
			 * 
			 */
			SqlExpr sqlExpr = MappingOpsImpl.createSqlCondition(condition, varDef, typeMap, sqlTranslator);

			
			//SqlTranslatorImpl2.asSqlExpr(sqlExpr);
			
			System.out.println("Result: " + sqlExpr);

			
			
			
			//VarBinding binding = VarBinding.create(quad, insertQuad);
			
			//System.out.println(binding);
		}
		
		//SqlTranslationUtils.splitEqualsConcat(la, lb)
		
			
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
		clauses.add(new Clause(new E_Equals(new ExprVar(g), NodeValue.makeNode(gv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(s), NodeValue.makeNode(sv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(p), NodeValue.makeNode(pv))));
		clauses.add(new Clause(new E_Equals(new ExprVar(o), NodeValue.makeNode(ov))));

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

