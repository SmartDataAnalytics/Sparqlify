package exp.cornercases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.jena_sparql_api.views.CandidateViewSelector;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorSparqlify;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SqlTranslatorImpl;
import org.aksw.sparqlify.core.algorithms.ViewDefinitionNormalizerImpl;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializer;
import org.aksw.sparqlify.util.ExprRewriteSystem;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.antlr.runtime.RecognitionException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class MappingOpsImplTest {

    private static final Logger logger = LoggerFactory.getLogger(MappingOpsImplTest.class);

    //@Test
    public void creationTest() throws RecognitionException, SQLException, IOException {

        SparqlifyCoreInit.initSparqlifyFunctions();


        //TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
        ExprRewriteSystem ers = SparqlifyUtils.createDefaultExprRewriteSystem();
        TypeSystem typeSystem = ers.getTypeSystem();

        SqlTranslator sqlTranslator = new SqlTranslatorImpl(typeSystem);


        DataSource dataSource = SparqlifyUtils.createTestDatabase();
        Connection conn = dataSource.getConnection();

        Schema databaseSchema = Schema.create(conn);

        // typeAliases for the H2 datatype
        Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));


        /* Some important interfaces for quick access */
        SqlExprSerializer serExpr = null;
        SqlFunctionSerializer serFn = null;



        ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);

        ViewDefinition personView = vdf.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID) ?t = plainLiteral(?NAME) From person");
        ViewDefinition deptView = vdf.create("Prefix ex:<http://ex.org/> Create View dept As Construct { ?s a ex:Department ; ex:name ?t } With ?s = uri(concat('http://ex.org/dept/', ?ID) ?t = plainLiteral(?NAME) From dept");
        ViewDefinition personToDeptView = vdf.create("Prefix ex:<http://ex.org/> Create View person_to_dept As Construct { ?p ex:worksIn ?d } With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID) ?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) From person_to_dept");

        ViewDefinition personAgeView = vdf.create("Prefix ex:<http://ex.org/> Prefix xsd:<http://www.w3.org/2001/XMLSchema#> Create View person As Construct { ?s ex:age ?a } With ?s = uri(concat('http://ex.org/person/', ?ID) ?a = typedLiteral(?AGE, xsd:int) From person");


        ViewDefinition personAndAgeView = vdf.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t ; ex:age ?a } With ?s = uri(concat('http://ex.org/person/', ?ID) ?t = plainLiteral(?NAME) ?a = typedLiteral(?AGE, xsd:int) From person");


        //OpMappingRewriter opMappingRewriter = SparqlifyUtils.createDefaultOpMappingRewriter(typeSystem);
        MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(ers);

        CandidateViewSelector<ViewDefinition> candidateViewSelector = new CandidateViewSelectorSparqlify(mappingOps, new ViewDefinitionNormalizerImpl());
        //candidateViewSelector.addView(personView);
        candidateViewSelector.addView(deptView);
        //candidateViewSelector.addView(personToDeptView);
        //candidateViewSelector.addView(personAgeView);
        candidateViewSelector.addView(personAndAgeView);





        /*
         * A test with (in)equalities in filter conditions of the query
         */
//		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
//
//
//		Expr expr = ExprUtils.parse("concat('http://ex.org/dept/', ?DEPT_ID) > 21");
//		Expr transExpr = exprTransformer.eval(expr, null);
//
//		System.out.println(transExpr);


//
//		Expr expr = ExprUtils.parse("<http://aksw.org/sparqlify/uri>(concat('foo', 'bar', ?v)) > <http://aksw.org/sparqlify/uri>('foobarx')");
//		Expr transExpr = exprTransformer.eval(expr, null);
//
//		System.out.println(transExpr);
//
//
//		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
//		typeMap.put("DEPT_ID", TypeToken.Int);
//		SqlExpr sqlExpr = sqlTranslator.translate(expr, null, typeMap);
//
//		System.out.println(sqlExpr);
//
//
//
//		//Expr transExpr = SqlTranslationUtils.optimizeOpConcat((ExprFunction2)expr);
//
//		if(true) {
//			System.exit(1);
//		}



//
//		Mapping m1 = personView.getMapping();
//
//
//		VarBinding binding = new VarBinding();
//		ViewInstance vi = new ViewInstance(personView, binding);
//
//		//DatatypeAssigner da = DatatypeAssignerMap.createDefaultAssignments(vd.getDatatypeSystem());
//
//
//		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
//		ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm(datatypeSystem);
//
//
//		MappingOps ops = new MappingOpsImpl(exprTransformer, sqlTranslator, exprNormalizer);
//
//		Mapping m2 = ops.join(m1, m1);
//		Mapping m3 = ops.join(m2, m1);
//		Mapping m4 = ops.union(Arrays.asList(m1, m3));
//
//		Mapping mTest = m4;
//		//System.out.println(m2);
//
//		System.out.println(mTest.getSqlOp());
//
//		SqlExprColumn x;
//
////		 Context ctx = new InitialContext();
////		 ctx.bind("jdbc/dsName", ds);
//
//		ExprSqlBridge b;
//
//		System.out.println(personView.getMapping().getSqlOp().getSchema());
//
//		SqlOp block = SqlOpSelectBlockCollectorImpl._makeSelect(mTest.getSqlOp());
//		System.out.println(block);
//
//
//		SqlExprSerializer exprSerializer = new SqlExprSerializerPostgres();//null /*da*/);
//
//		SqlOpSerializer serializer = new SqlOpSerializerImpl(exprSerializer);
//
//		String sqlQueryString = serializer.serialize(block);

        //SparqlSqlRewriter rewriter = new SparqlSqlRewriterImpl();
        OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
        SparqlSqlStringRewriter rewriter = null; //SparqlifyUtils.createTestRewriter(candidateViewSelector, opMappingRewriter, typeSystem, databaseSchema);
        QueryExecutionFactory qef = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);


        //System.out.println(sqlQueryString);


        {
            //QueryExecution qe = qef.createQueryExecution("Select ?s (Count(*) As ?c) { ?s ?p ?o . Filter(?s = <http://ex.org/person/1> || ?s = <http://ex.org/person/2>) . } Group By ?s");
            //QueryExecution qe = qef.createQueryExecution("Select ?s ?p (Count(*) As ?c) { ?s ?p ?o . } Group By ?s ?p");

            // Simple Optional Test
            //QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select * { ?x ?y ?z . ?s ?p ?o . Optional { ?s ex:name ?l } . Filter(!(?o > 21)) }");

            // Self Join elimination test
            //QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select * { ?s a ex:Person ; ex:name ?l ; ex:age ?a . }");

            // Count query
            QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select (Count(*) As ?c) { ?s a ex:Person ; ex:name ?l ; ex:age ?a . }");

            //QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select * { ?s a ?t . Optional { ?s ex:name ?n . } . ?s ex:age ?a . Filter(?a < 'foo') .}");
            //QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select * { ?s ex:age ?a . Filter(?a < 'foo') .}");

            //QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select * { ?s a ?t . Optional { ?s ex:name ?n . } . ?s ex:age ?a . Filter(?a > 20 || ?a < 'foo') .}");


            // Simple Optional Test With Scope [not working]
            //QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select * { ?s ?p ?o . Optional { ?s ex:name ?l . Filter(!(?o > 21)) }  }");

            //QueryExecution qe = qef.createQueryExecution("Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Select * { ?s rdfs:label ?l . ?s ?p ?o . Filter(!(?o > 21)) .} ");


            // Filter Scope Test
            //QueryExecution qe = qef.createQueryExecution("Prefix ex:<http://ex.org/> Select * { ?s a ?t . Optional { ?s ex:name ?l . Filter(?t = ex:Person) } . }");

            ResultSet rs = qe.execSelect();
            String rsStr = ResultSetFormatter.asText(rs);
            System.out.println(rsStr);
        }


//		{
//			QueryExecution qe = qef.createQueryExecution("Select * { ?s ?p ?o . Filter(?s = <http://ex.org/person/1> && ?s = <http://ex.org/person/2>) . }");
//			ResultSet rs = qe.execSelect();
//			String rsStr = ResultSetFormatter.asText(rs);
//			System.out.println(rsStr);
//		}

//		{
//			QueryExecution qe = qef.createQueryExecution("Select * { ?s ?p ?o . Filter(!(?s = <http://ex.org/person/1>)) . }");
//			ResultSet rs = qe.execSelect();
//			String rsStr = ResultSetFormatter.asText(rs);
//			System.out.println(rsStr);
//		}
//
//
//		{
//			QueryExecution qe = qef.createQueryExecution("Select * { ?s ?p ?o . ?x ?y ?z . Filter(?s = ?x)}");
//			ResultSet rs = qe.execSelect();
//			String rsStr = ResultSetFormatter.asText(rs);
//			System.out.println(rsStr);
//		}

        //SqlSelectBlock x;

        //ViewDefinition vd = new ViewDefinition(name, );

    }
}
