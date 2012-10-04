package org.aksw.sparqlify.core;

import java.io.IOException;
import java.sql.SQLException;

import org.aksw.sparqlify.core.domain.SparqlSqlRewrite;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

public class SparqlSqlRewriterTests {
	@Test
	public void creationTest() throws RecognitionException, SQLException, IOException {

		SparqlifyTestFacade facade = SparqlifyTestFacade.createWithTestDb();
		facade.addView("Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) From person");
		
		SparqlSqlRewrite rewrite = facade.rewrite("Select * { ?s ?p ?o }");
		
		System.out.println(rewrite);
	}

}
