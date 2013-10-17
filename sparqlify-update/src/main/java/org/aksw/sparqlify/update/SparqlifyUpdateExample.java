package org.aksw.sparqlify.update;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMap;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMapper;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMapperImpl;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
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

		
		
		CandidateViewSelectorImpl candidateViewSelector = new CandidateViewSelectorImpl();
		candidateViewSelector.addView(personView);
		candidateViewSelector.addView(deptView);
		candidateViewSelector.addView(personToDeptView);

		
		/*
		 * Let's assume the following quad should be inserted.
		 * The procedure is as follows:
		 * - For every view, find all the quads which could yield the one being inserted.
		 * - Make sure all the views make use of table names (supporting inserts on queries is tough)
		 * - Based on the view's variable definition, try to figure out, what the SQL columns were
		 * 
		 */
	
		
		SparqlSqlInverseMapper inverseMapper = new SparqlSqlInverseMapperImpl(candidateViewSelector, sqlTranslator);

		
		Quad insertQuad = new Quad(Quad.defaultGraphNodeGenerated, Node.createURI("http://ex.org/person/5"), RDF.type.asNode(), Node.createURI("http://ex.org/Person"));
		List<SparqlSqlInverseMap> tmp = inverseMapper.map(insertQuad);
		System.out.println(tmp);
	}
}

