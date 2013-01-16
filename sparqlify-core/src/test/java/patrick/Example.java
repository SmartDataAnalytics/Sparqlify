package patrick;

import java.io.File;
import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.aksw.sparqlify.validation.LoggerCount;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class Example {
	
	private static final Logger logger = LoggerFactory.getLogger(Example.class);
	
	@Test
	public void test() throws Exception {
		
		ConfigParser parser = new ConfigParser();

		LoggerCount errorLogger = new LoggerCount(logger);
		
		// Either put a string or an input stream here
		Config config = parser.parse("", errorLogger);
		System.out.println("Warnings: " + errorLogger.getWarningCount() + ", Errors: " + errorLogger.getWarningCount());
		
		if(errorLogger.getErrorCount() != 0) {
			throw new RuntimeException("Fix the errors please");
		}
		
		System.out.println(config.getPrefixMapping());
		
		DataSource dataSource = SparqlifyUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		
		ViewDefinitionFactory vdFactory = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		String testView = "Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) From person";
		ViewDefinition vd = vdFactory.create(testView);

		Model model = ModelFactory.createDefaultModel();
		/*
		for(ViewDefinition def : viewDefs) {
			for(Quad quad : vd.getTemplate()) {
				// Generate R2R-ML Triple Maps (Quad Maps)
			}
			SqlOp = vd.getMapping().getSqlOp();
			
		}
		*/
	}
}
