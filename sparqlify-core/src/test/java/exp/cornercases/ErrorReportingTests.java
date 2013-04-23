package exp.cornercases;

import java.io.File;
import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.aksw.sparqlify.validation.LoggerCount;
import org.aksw.sparqlify.validation.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ErrorReportingTests {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorReportingTests.class);
	

	@Test
	public void excessiveVarDefTest()
			throws Exception
	{
		DataSource ds = SparqlifyUtils.createTestDatabase();
		Connection conn = ds.getConnection();
		
		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		ViewDefinitionFactory vdFactory = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		String testViewStr = "Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) ?x = uri(?ID) From PERSON";

		ViewDefinition vd = vdFactory.create(testViewStr);
		
		LoggerCount loggerCount = new LoggerCount(logger);

		Validation.validateView(vd, loggerCount);

		Assert.assertEquals(1, loggerCount.getWarningCount());
	}
	
	@Test
	public void unboundPatternVarTest()
			throws Exception
	{
		DataSource ds = SparqlifyUtils.createTestDatabase();
		Connection conn = ds.getConnection();
		
		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		ViewDefinitionFactory vdFactory = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		String testViewStr = "Create View testview As Construct { ?s a ?t } With ?t = uri(?NAME) From PERSON";

		ViewDefinition vd = vdFactory.create(testViewStr);
		
		LoggerCount loggerCount = new LoggerCount(logger);

		Validation.validateView(vd, loggerCount);

		Assert.assertEquals(1, loggerCount.getErrorCount());

		
//		ConfigParser parser = new ConfigParser();
//		Config config = parser.parse(testView, loggerCount);

		

		/*
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		//OpMappingRewriter opMappingRewriter = SparqlifyUtils.createDefaultOpMappingRewriter(typeSystem);
		MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(typeSystem);
		
		CandidateViewSelector<ViewDefinition> system = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());		
		system.addView(coreVd);

		SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, config, maxResultSetSize, maxQueryExecutionTime)
		
		SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, config, maxResultSetSize, maxQueryExecutionTime);
		*/
		
	}
}
