package org.aksw.sparqlify.demos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.lang.ConfiguratorRdfViewSystem;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.QueryExecutionFactorySparqlify;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.apache.log4j.PropertyConfigurator;
import org.postgresql.ds.PGSimpleDataSource;

import com.clarkparsia.owlapiv3.XSD;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;


/*
class GeomFromText
	extends FunctionBase2
{
	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		// TODO Auto-generated method stub
		return null;
	}	
}
*/


public class DemoLinkedGeoData {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");

		
		
		if(true) {
			String str = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> Select * {?s ?p ?o . } Order By xsd:double(str(?o))";
			Query q = QueryFactory.create(str);
			Op op = Algebra.compile(q);
			Algebra.toQuadForm(op);
			System.out.println(op);

			return;
		}

		Connection conn = getConnection();
		RdfViewSystemOld system = configureSystem(conn);
		
		QueryExecutionFactory sparqler = new QueryExecutionFactorySparqlify(system, conn);

		
		
		String polygon = "POLYGON((8.59 53.57, 8.6 53.57, 8.6 53.58, 8.59 53.58, 8.59 53.57))";
		
//		String str = "Prefix ogc:<http://www.opengis.net/rdf#> Prefix geo:<http://ex.org/> Select * { ?s geo:geometry ?geo . ?s ?p ?o . Filter(?s = <http://linkedgeodata.org/resource/node/445397207>) .} limit 1000";
//		String str = "Prefix ogc:<http://www.opengis.net/rdf#> Prefix geo:<http://ex.org/> Select * { ?s geo:geometry ?geo . ?s ?p ?o . Filter(?o > 0 && ?o < 5) .} limit 1000";

		String prefixes = "";
		prefixes += "Prefix wso:<http://aksw.org/wortschatz/ontology/> ";
		prefixes += "Prefix owl:<" + OWL.getURI() + "> ";
		prefixes += "Prefix rdfs:<" + RDFS.getURI() + "> ";

		//String str = prefixes + "Select * { ?s rdfs:label ?o .} limit 1000";
		String str = prefixes + "Select * {?s ?p ?o . } limit 10";
		
		ResultSet rs = sparqler.createQueryExecution(str).execSelect();
		//Model m = sparqler.executeConstruct(prefixes + "Construct { ?s rdfs:label ?o } { ?s rdfs:label ?o }");
		
		
		System.out.println(ResultSetFormatter.asText(rs));
		//m.write(System.out, "TURTLE");
		
		//FunctionRegistry.get().put("http://www.opengis.net#geomFromText", RdfTerm.class);
		//FunctionRegistry.get().put("http://www.opengis.net#intersects", RdfTerm.class);

		//FunctionRegistry.get().put("http://aksw.org/spy/geomFromText", GeomFromText.class);
	}
	
	
	public static Connection getConnection() throws SQLException {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		
		dataSource.setDatabaseName("lgd_bremen");
		dataSource.setUser("postgres");
		dataSource.setPassword("postgres");
		dataSource.setServerName("localhost");
		dataSource.setPortNumber(5432);

		Connection conn = dataSource.getConnection();

		return conn;
	}

	public static RdfViewSystemOld configureSystem(Connection conn) throws Exception {
		return configureSystem(new File("examples/linkedgeodata.sparqlify"), conn);
	}
	
	public static RdfViewSystemOld configureSystem(File configFile, Connection conn) throws Exception {
		ConfigParser parser = new ConfigParser();
		
		InputStream in = new FileInputStream(configFile);
		Config config;
		try {
			config = parser.parse(in);
		} finally {
			in.close();
		}
		
		RdfViewSystemOld system = new RdfViewSystemOld();
		ConfiguratorRdfViewSystem.configure(config, system);

		system.loadDatatypes(conn);
		
		return system;
	}	
	
	public static RdfViewSystemOld configureSystemOld(Connection conn) throws Exception {
		RdfViewSystemOld system = new RdfViewSystemOld();
		
		// Specific Classes and Object Properties
		//system.addView(RdfView.create("{ ?s ?p ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id)) ; ?p = spy:uri(concat(?property)); ?o = spy:uri(concat(?object)); node_specific_resources"));

		// Specific Classes and Object Properties (ObjectAsPrefix = true)
		//system.addView(RdfView.create("{ ?s ?p ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id)) ; ?p = spy:uri(concat(?property)); ?o = spy:uri(concat(?object, ?v)); resource_map_prefixed"));
		
		// General Classes and Object Properties (key, null)
		//select key, value, property, object from lgd_tag_mapping_simple_base a JOIN lgd_tag_mapping_simple_object_property b On (a.id = b.id) where key = 'shop' AND value IS NULL;
		//system.addView(RdfView.create("{ ?s ?p ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id)) ; ?p = spy:uri(concat(?property)); ?o = spy:uri(concat(?object)); node_generic_resources"));

		// class labels
		//system.addView(RdfView.create("{ ?s a owl:Class . ?s rdfs:label ?o . } with ?s = spy:uri(?resource) ; ?o = spy:plainLiteral(?label, ?language); lgd_resource_labels"));

		System.out.println(XSD.BOOLEAN);
		
		// Typed Literals (null, null)
		system.addView(RdfView.create("{ ?s ?p ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id)) ; ?p = spy:uri(concat('http://linkedgeodata.org/property/', ?k)); ?o = spy:typedLiteral(concat(?v), " + XSD.BOOLEAN + "); node_tags_boolean"));
		//system.addView(RdfView.create("{ ?s ?p ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id)) ; ?p = spy:uri(concat('http://linkedgeodata.org/property/', ?k)); ?o = spy:typedLiteral(concat(?v), " + XSD.INT + "); node_tags_int"));
		//system.addView(RdfView.create("{ ?s ?p ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?node_id)) ; ?p = spy:uri(concat('http://linkedgeodata.org/property/', ?k)); ?o = spy:typedLiteral(concat(?v), " + XSD.FLOAT + "); node_tags_float"));

		// Node geometries
		//system.addView(RdfView.create("{ ?s geo:geometry ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?id)) ; ?o = spy:plainLiteral(?geom); select id, geom from nodes"));
		
		// TODO The following query causes an equal between incompatible types
		//system.addView(RdfView.create("{ ?s a geo:Node . ?s geo:geometry ?o . } with ?s = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?id)) ; ?o = spy:plainLiteral(?geom); nodes"));
		
		
		system.loadDatatypes(conn);

		return system;
	}
	
	/*
	public static ResultSet executeSelect(String str)
				throws Exception
	{
		
		

		//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s <http://linkedgeodata.org/property/highway> ?o . filter(?s = <http://linkedgeodata.org/resource/node/20958816>) . }");
		//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s ?p ?o . filter(?s = <http://linkedgeodata.org/resource/node/20958816>) . Filter(?p = <http://linkedgeodata.org/property/highway>) . }");
		String polygon = "POLYGON((8.59 53.57, 8.6 53.57, 8.6 53.58, 8.59 53.58, 8.59 53.57))";
		
		//Query query = QueryFactory.create("Prefix ogc:<http://www.opengis.net/rdf#> Prefix geo:<http://ex.org/> Select * { ?s geo:geometry ?geo . ?s ?p ?o . Filter(?s = <http://linkedgeodata.org/resource/node/445397207>) .} limit 1000");

		// TODO Not working; causes an equal between incompatible types
		//Query query = QueryFactory.create("Prefix ogc:<http://www.opengis.net/rdf#> Prefix geo:<http://ex.org/> Select * { ?s geo:geometry ?geo . ?s a ?t . ?s ?p ?o . Filter(?t = <http://linkedgeodata.org/ontology/Shop>) . Filter(ogc:intersects(?geo, ogc:geomFromText('" + polygon + "'))) .} limit 100");
		Query query = QueryFactory.create(str);
		
		Op view = system.getApplicableViews(query);
		
		//
		ViewRewriter sqlRewriter = new ViewRewriter();
		SqlNode sqlNode = sqlRewriter.rewriteMM(view);
		
		System.out.println("Final sparql var mapping = " + sqlNode.getSparqlVarToExprs());
		
		SqlGenerator sqlGenerator = new SqlGenerator();
		String sqlQuery = sqlGenerator.generateMM(sqlNode);
		System.out.println(sqlQuery);

		double cost = RdfViewDatabase.getCostPostgres(conn, sqlQuery);
		
		
		
		if(cost > 4000) {
			System.out.println("Aborted due to high query cost: " + cost);
			return null;
		}
		
		System.out.println("Query cost ok (" + cost +")");
		
		
		
		
		ResultSet rs = ResultSetFactory.create(conn, sqlQuery, sqlNode.getSparqlVarToExprs());
		return rs;
	}	
	
	
	public static void printResult(ResultSet rs)
	{
		
		System.out.println("Result");
		System.out.println("--------------------------------------");
		List<String> columns = new ArrayList<String>();
		columns.add("s");
		columns.add("p");
		columns.add("o");
		columns.add("t");
		
		
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			
			//System.out.println(qs.get("s") + "\t" + qs.get("p") + "\t" + qs.get("o"));

			if(columns == null) {
				columns = new ArrayList<String>(rs.getResultVars());
			}
			
			for(String a : columns) {
				RDFNode node  = qs.get(a);
				System.out.print(a + ":" + node + "\t");				
			}
			
			System.out.println();
			
		}
		
		//conn.close();
	}
	
*/	
}

	
//Map<String, Integer> columnToType = RdfViewDatabase.getRawTypes(conn, "SELECT * FROM node_tags LIMIT 0");
//System.out.println(columnToType);
