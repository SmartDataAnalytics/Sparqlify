package org.aksw.sparqlify.core;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.domain.SparqlSqlRewrite;
import org.aksw.sparqlify.core.domain.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.util.MapReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

import cornercases.TestUtils;
import cornercases.ViewDefinitionFactory;


public class SparqlifyTestFacade {

	private ViewDefinitionFactory vdFactory;
	private CandidateViewSelector cvs;
	private SparqlSqlRewriter rewriter;


	public SparqlifyTestFacade(ViewDefinitionFactory vdFactory, CandidateViewSelector cvs, SparqlSqlRewriter rewriter) {
		this.cvs = cvs;
		this.vdFactory = vdFactory;
		this.rewriter = rewriter;
	}
	
	
	public void addView(String viewDefStr) {
		ViewDefinition viewDef = vdFactory.create(viewDefStr);

		cvs.addView(viewDef);
	}
	
	
	public SparqlSqlRewrite rewrite(String queryStr) {
		Query query = new Query();
		QueryFactory.parse(query, queryStr, "http://ex.org/", Syntax.syntaxSPARQL_11);

		SparqlSqlRewrite result = rewriter.rewrite(query);

		return result;
	}
	
	
	/**
	 * Create a simple facade initialized with an embedded test database.
	 * 
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static SparqlifyTestFacade createWithTestDb()
			throws SQLException, IOException
	{
		DataSource dataSource = TestUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		
		ViewDefinitionFactory vdFactory = TestUtils.createViewDefinitionFactory(conn, typeAlias);
		
		CandidateViewSelectorImpl cvs = new CandidateViewSelectorImpl();

		SparqlSqlRewriter rewriter = TestUtils.createTestRewriter(cvs, vdFactory.getDatatypeSystem());
	
		SparqlifyTestFacade result = new SparqlifyTestFacade(vdFactory, cvs, rewriter);
			
		return result;
	}
}

