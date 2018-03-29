package org.aksw.r2rml.impl.jena;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.jena_sparql_api.exprs_ext.E_StrConcatPermissive;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.r2rml.api.LogicalTable;
import org.aksw.r2rml.api.ObjectMap;
import org.aksw.r2rml.api.PredicateObjectMap;
import org.aksw.r2rml.api.SubjectMap;
import org.aksw.r2rml.api.TermMap;
import org.aksw.r2rml.api.TriplesMap;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.sql.schema.SchemaImpl;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sdb.core.Gensym;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ImporterR2RML {

	private static org.apache.jena.rdf.model.Model r2rmlGraph = ModelFactory
			.createDefaultModel();
	private static ViewDefinition r2rmlViewDef;

	public static org.apache.jena.rdf.model.Model loadR2rmlGraph(
			String r2rmlFileName) {

		// Model model = ModelFactory.createDefaultModel();
		// /model.read(url)

		FileManager.get().readModel(r2rmlGraph, r2rmlFileName);
		return r2rmlGraph;
	}

	public static void main(String[] args) throws Exception {

		SparqlifyCoreInit.initSparqlifyFunctions();

//		TypeSystem datatypeSystem = SparqlifyUtils
//				.createDefaultDatatypeSystem();
		//SqlTranslator sqlTranslator = SparqlifyUtils.createSqlRewriter(); //new SqlTranslatorImpl(datatypeSystem);

		DataSource dataSource = SparqlifyUtils.createTestDatabase();
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File(
				"src/main/resources/type-map.h2.tsv"));

		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(
				conn, typeAlias);

		ViewDefinition personView = vdf
				.create("Prefix ex:<http://ex.org/>							"
						+ "Create View person As 								"
						+ "	Construct {										"
						+ "		?s a ex:Person ; ex:name ?t					" + "	}" + "With "
						+ "	?s = uri(concat('http://ex.org/person/', ?ID) 	"
						+ "	?t = plainLiteral(?NAME) From person			");
		ViewDefinition deptView = vdf.create("Prefix ex:<http://ex.org/>"
				+ "Create View dept As " + "	Construct { "
				+ "		?s a ex:Department ; ex:name ?t" + "	} " + "With "
				+ "	?s = uri(concat('http://ex.org/dept/', ?ID) "
				+ "	?t = plainLiteral(?NAME) From dept");
		ViewDefinition personToDeptView = vdf
				.create("Prefix ex:<http://ex.org/>"
						+ "Create View person_to_dept "
						+ "	As Construct { ?p ex:worksIn ?d } "
						+ "With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID) "
						+ "	?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) "
						+ "From person_to_dept");



		// FileManager.get().readModel(r2rmlGraph, args[0]);
		loadR2rmlGraph(args[0]);

		r2rmlGraph.write(System.out, "TTL");

		//R2RMLSpec myMapping = new R2RMLSpec(r2rmlGraph);
		Collection<TriplesMap> xtriplesMaps = r2rmlGraph.listSubjectsWithProperty(RR.logicalTable).mapWith(s -> s.as(TriplesMap.class)).toList();
		
		Multimap<LogicalTable, TriplesMap> tableToTm = HashMultimap
				.create();

		
		// Group triple maps by their logical table
		for (TriplesMap tm : xtriplesMaps) {
			LogicalTable lt = tm.getLogicalTable();

			tableToTm.put(lt, tm);
		}

		Set<ViewDefinition> result = new HashSet<ViewDefinition>();

		// Let's create view definitions
		for (Entry<LogicalTable, Collection<TriplesMap>> entry : tableToTm.asMap().entrySet()) {

			LogicalTable logicalTable = entry.getKey();
			Collection<TriplesMap> triplesMaps = entry.getValue();
			String name = logicalTable + "" + triplesMaps;
			QuadPattern template = new QuadPattern();

			for (TriplesMap tm : triplesMaps) {
				SubjectMap sm = tm.getSubjectMap();
				//RDFNode rrClass = sm.getRrClass();
				Set<Resource> types = sm.getTypes();
				
				Multimap<Var, RestrictedExpr> varToExprs = HashMultimap.create();
				String templateString = sm.getTemplate();
				E_StrConcatPermissive e = RRUtils.parseTemplate(templateString);
				Expr pkExpr = new E_URI(e);
				RestrictedExpr restExpr = new RestrictedExpr(pkExpr);
				Generator genS = Gensym.create("S");
				Var varS = Var.alloc(genS.next());
				Var subjectVar = varS;
				varToExprs.put(varS, restExpr);
				VarDefinition varDef = new VarDefinition(varToExprs);
				for(Resource type : types) {
					template.add(new Quad(Quad.defaultGraphNodeGenerated, varS, RDF.type.asNode(), type.asNode()));
				}
				Generator genO = Gensym.create("O");

				for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
					for (ObjectMap om : pom.getObjectMaps()) {
						
						varDef = getVarDefinition(template, varToExprs, subjectVar,	varDef, pom, om, genO);
					}
				}
				SqlOp op;
				// Create the table node
				if (logicalTable.getTableName() != null) { //logicalTable.isTableName()) {
					String tableName = logicalTable.getTableName();
					op = new SqlOpTable(new SchemaImpl(), tableName);
				} else {
					throw new RuntimeException("Not implemented");
				}
				Mapping mapping = new Mapping(varDef, op);

				ViewDefinition viewDef = new ViewDefinition(name,template, null, mapping, entry);

				result.add(viewDef);

			}
		}
		for (ViewDefinition viewDef : result) {
			System.out.println(viewDef);
		}

		if (true) {
			System.exit(0);
		}


	}



	/**
	 * @param template
	 * @param varToExprs
	 * @param subjectVar
	 * @param varDef
	 * @param genO
	 * @param pom
	 * @param tableExprList
	 * @param om
	 * @return
	 * @author sherif
	 */
	public static VarDefinition getVarDefinition(QuadPattern template,
			Multimap<Var, RestrictedExpr> varToExprs, Var subjectVar,
			VarDefinition varDef, PredicateObjectMap pom, TermMap om, Generator genO) {
		
		ExprList tableExprList = new ExprList();
		E_Function ef;
		RDFNode datatype = om.getDatatype();
		String languageTag = om.getLanguage();
		String term = getObjectMapTerm(om);
		if (term != null){

			tableExprList.add(new ExprVar(Var.alloc(term)));
			if (datatype != null) {
				tableExprList.add(NodeValue.makeNode(datatype.asNode())); // NodeValue.makeNode(Node.createURI(datatype.toString())));
				ef = new E_Function(SparqlifyConstants.typedLiteralLabel,tableExprList);
			} else if (languageTag != null) {
				tableExprList.add(NodeValue.makeString(languageTag)); 
				ef = new E_Function(SparqlifyConstants.typedLiteralLabel,tableExprList);
			} else {
				ef = new E_Function(SparqlifyConstants.plainLiteralLabel,tableExprList);
			}
			RestrictedExpr tableRestExpr = new RestrictedExpr(ef);
			Var varO = Var.alloc(genO.next());
			varToExprs.put(varO, tableRestExpr);
			varDef = new VarDefinition(varToExprs);
			for(Resource predicate : pom.getPredicates()) {
				template.add(new Quad(Quad.defaultGraphNodeGenerated , subjectVar,predicate.asNode() , varO));
			}
		}
		return varDef;
	}

	public static String getObjectMapTerm(TermMap om) {
//
//		switch (om.getTermSpec()) {
//		case COLUMN: {
//			return om.getColumnName();
//		}
//		case CONSTANT: {
//			return om.getConstant();
//		}
//		case TEMPLATE: {
//			return om.getTemplate();
//		}
//		case JOIN: {
//			return null;
//		} // NOT SUPPORTED YET
//		default: {
//			//			return null;
//			throw new RuntimeException("Not supported TermSpec");
//		}
//		}

		return "todo";
	}
	public Map<String, ViewDefinition> load(InputStream in) {
	
		Map<String, ViewDefinition> actuals= new HashMap<String, ViewDefinition>();
		return actuals;
	}
}
