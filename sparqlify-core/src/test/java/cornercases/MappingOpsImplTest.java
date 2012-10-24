package cornercases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.sparqlify.algebra.sparql.expr.old.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.ExprDatatypeNorm;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.SqlExprSerializerPostgres;
import org.aksw.sparqlify.core.algorithms.SqlOpSelectBlockCollectorImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.algorithms.SqlTranslationUtils;
import org.aksw.sparqlify.core.algorithms.SqlTranslatorImpl;
import org.aksw.sparqlify.core.algorithms.VarBinding;
import org.aksw.sparqlify.core.algorithms.ViewInstance;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.util.MapReader;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;




public class MappingOpsImplTest {

	private static final Logger logger = LoggerFactory.getLogger(MappingOpsImplTest.class);

	@Test
	public void creationTest() throws RecognitionException, SQLException, IOException {

		RdfViewSystemOld.initSparqlifyFunctions();
		
		
		DatatypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
		SqlTranslator sqlTranslator = new SqlTranslatorImpl(datatypeSystem);

		
		DataSource dataSource = SparqlifyUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		
		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		ViewDefinition personView = vdf.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID) ?t = plainLiteral(?NAME) From person");
		ViewDefinition deptView = vdf.create("Prefix ex:<http://ex.org/> Create View dept As Construct { ?s a ex:Department ; ex:name ?t } With ?s = uri(concat('http://ex.org/dept/', ?ID) ?t = plainLiteral(?NAME) From dept");
		ViewDefinition personToDeptView = vdf.create("Prefix ex:<http://ex.org/> Create View person_to_dept As Construct { ?p ex:worksIn ?d } With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID) ?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) From person_to_dept");

		CandidateViewSelector candidateViewSelector = new CandidateViewSelectorImpl();		
		candidateViewSelector.addView(personView);
		candidateViewSelector.addView(deptView);
		candidateViewSelector.addView(personToDeptView);
		

		
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
		SparqlSqlRewriter rewriter = SparqlifyUtils.createTestRewriter(candidateViewSelector, datatypeSystem);
		QueryExecutionFactory qef = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);


		//System.out.println(sqlQueryString);
		
		
		QueryExecution qe = qef.createQueryExecution("Select * { ?s ?p ?o . Filter(?s = <http://ex.org/person/1> || ?s = <http://ex.org/person/2>) . }");
		ResultSet rs = qe.execSelect();
		String rsStr = ResultSetFormatter.asText(rs);
		System.out.println(rsStr);
		
		
		//SqlSelectBlock x;
		
		//ViewDefinition vd = new ViewDefinition(name, );
		
	}
}
