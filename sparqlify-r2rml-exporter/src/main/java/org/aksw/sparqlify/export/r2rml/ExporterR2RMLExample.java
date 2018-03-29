package org.aksw.sparqlify.export.r2rml;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.aksw.sparqlify.validation.LoggerCount;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExporterR2RMLExample {
	private static final Logger logger = LoggerFactory.getLogger(ExporterR2RMLExample.class);
	
	public static void main(String[] args)
		throws Exception
	{
		//RdfViewSystemOld.initSparqlifyFunctions();
		SparqlifyCoreInit.initSparqlifyFunctions();
		
		TypeSystem datatypeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();

		
		DataSource dataSource = SparqlifyUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();

		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");//.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		// FIXME: commented out because this would cause errors
		ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		ViewDefinition personView = vdf.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID) ?t = plainLiteral(?NAME) From person");
		ViewDefinition deptView = vdf.create("Prefix ex:<http://ex.org/> Create View dept As Construct { ?s a ex:Department ; ex:name ?t } With ?s = uri(concat('http://ex.org/dept/', ?ID) ?t = plainLiteral(?NAME) From dept");
		ViewDefinition personToDeptView = vdf.create("Prefix ex:<http://ex.org/> Create View person_to_dept As Construct { ?p ex:worksIn ?d } With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID) ?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) From person_to_dept");

		System.out.println(personView);

		Collection<ViewDefinition> viewDefs= Arrays.asList(personView, deptView, personToDeptView);
				
		/*
		System.out.println("test"
		System.out.println(personView);

		Collection<ViewDefinition> viewDefs= Arrays.asList(personView, deptView, personToDeptView);
				
		/*
		System.out.println("test");
		*/
		
		Model model = ModelFactory.createDefaultModel();
				
		// FIXME: commented out because this would cause errors
		exportR2RML(viewDefs, model);

		System.out.println("R2R-ML Output:");
		model.write(System.out, "TURTLE");
		
		
	}

	static void exportR2RML(Collection<ViewDefinition> viewDefs, Model result) {
		Model r = new R2RMLExporter(viewDefs).export();
		result.add(r);
		// TODO This the heart to be implemented
//		Resource foo = result.createResource("http://foo.bar");
//		result.add(foo, RDF.type, OWL.Class);
	}
	
	
	/**
	 * !!! Ignore this for now as it is not done yet, I want to simply this a bit !!!
	 * 
	 * @throws Exception
	 */
	public static void thisCodeIsForProcessingFiles()
		throws Exception
	{
		
		ConfigParser parser = new ConfigParser();
		
		/**
		 * The schema provider returns the datatype for colmn names.
		 * For rewriting queries, we need to know the datatypes, however for the R2RML export we do not need it.
		 * 
		 * Since we don't want to require  a database for the R2RML export, we need to
		 * implement a dummy SchemaProvider, which just returns 
		 * TypeToken.Special for every column name.
		 * (A TypeToken is just a wrapper for a String referring to the type name).
		 * 
		 * 
		 */
		TypeToken datatype = TypeToken.Special;
		
		// TODO Implement one which uses TypoToken.Special.
		SchemaProvider schemaProvider = null;
			
		// Wrap the logger with LoggerCount; this allows us to check how many errors/warnings were emitted.
		LoggerCount loggerCount = new LoggerCount(logger);
		
		// TODO Your source file goes here
		InputStream in = null; 
		
		// The config already contains view definitions
		Config config = parser.parse(in, loggerCount);

		
		SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

		

	}
}

