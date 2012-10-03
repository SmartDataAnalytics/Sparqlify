package cornercases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.domain.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.util.MapReader;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

public class CandidateViewSelectionTests {

	@Test
	public void creationTest() throws RecognitionException, SQLException, IOException {

		DataSource dataSource = TestUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		
		ViewDefinitionFactory vdFactory = TestUtils.createViewDefinitionFactory(conn, typeAlias);
		
		String testView = "Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) From person";
		ViewDefinition coreVd = vdFactory.create(testView);
	
		
		CandidateViewSelector system = new CandidateViewSelectorImpl();		
		system.addView(coreVd);
		
		
		String queryString = "Select * { ?s ?p ?o }";
		
		Query query = new Query();
		QueryFactory.parse(query, queryString, "http://ex.org/", Syntax.syntaxSPARQL_11);
		system.getApplicableViews(query);
		
		
		
	}
}
