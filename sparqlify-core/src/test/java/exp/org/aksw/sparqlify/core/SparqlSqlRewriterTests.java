package exp.org.aksw.sparqlify.core;

import java.io.IOException;
import java.sql.SQLException;

import org.aksw.sparqlify.core.domain.input.SparqlSqlRewrite;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

public class SparqlSqlRewriterTests {

	/*
	@Test
	public void singleViewTest() throws RecognitionException, SQLException, IOException {

		SparqlifyTestFacade facade = SparqlifyTestFacade.createWithTestDb();
		facade.addView("Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) From person");
		
		SparqlSqlRewrite rewrite = facade.rewrite("Select * { ?s ?p ?o }");
		
		System.out.println(rewrite);
	}

	
	@Test
	public void doubleViewTest() throws RecognitionException, SQLException, IOException {

		SparqlifyTestFacade facade = SparqlifyTestFacade.createWithTestDb();
		facade.addView("Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) From person");
		facade.addView("Create View testview2 As Construct { ?s ?x ?y } With ?s = uri(?ID) ?x = uri(?ID) ?y = plainLiteral(?NAME) From person");
		
		SparqlSqlRewrite rewrite = facade.rewrite("Select * { ?s ?p ?o }");
		
		System.out.println(rewrite);
	}
	*/

	
	/**
	 * Standard test case with nested optionals
	 * 
	 * 
	 * @throws RecognitionException
	 * @throws SQLException
	 * @throws IOException
	 */
	//@Test
	public void nestedOptionalsTest() throws RecognitionException, SQLException, IOException {
		
		SparqlifyTestFacade facade = SparqlifyTestFacade.createWithTestDb();
		facade.addView("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?pn } With ?s = uri(?ID) ?pn = plainLiteral(?NAME) From person");
		facade.addView("Prefix ex:<http://ex.org/> Create View person_to_dept As Construct { ?s ex:dept ?d } With ?s = uri(?PERSON_ID) ?d = uri(?DEPT_ID) From person_to_dept");
		//facade.addView("Prefix ex:<http://ex.org/> Create View dept As Construct { ?s a ex:Dept ; ex:name ?dn } With ?s = uri(?ID) ?dn = uri(?NAME) From dept");
		
//		SparqlSqlRewrite rewrite = facade.rewrite("Prefix ex:<http://ex.org/> Select * { ?s ex:name ?pn . Optional { ?s ex:dept ?d . Optional { ?d ex:name ?dn . Filter(?dn = ?pn) } } }");
//		SparqlSqlRewrite rewrite = facade.rewrite("Prefix ex:<http://ex.org/> Select * { ?s ex:name ?pn . Optional { ?s ex:dept ?d . Optional { ?d ex:name ?dn } } }");
		SparqlSqlRewrite rewrite = facade.rewrite("Prefix ex:<http://ex.org/> Select Distinct * { ?s ex:name ?pn . Optional { ?s ex:dept ?d } } Limit 10 Offset 20	");
//		SparqlSqlRewrite rewrite = facade.rewrite("Prefix ex:<http://ex.org/> Select * { ?s ex:name ?pn }");
//		SparqlSqlRewrite rewrite = facade.rewrite("Prefix ex:<http://ex.org/> Select * { ?s ?p ?pn }");

		System.out.println(rewrite);
	}

	
	
}
