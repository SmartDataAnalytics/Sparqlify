package org.aksw.sparqlify.core.test;

import java.io.ByteArrayInputStream;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.jolbox.bonecp.BoneCPDataSource;

public class ConnectionCloseTest {

	
	@Test
	public void test()
		throws Exception
	{
		DataSource tmp = SparqlifyUtils.createTestDatabase();
		BoneCPDataSource ds = new BoneCPDataSource();
		ds.setDatasourceBean(tmp);
		ds.setCloseConnectionWatch(true);
		ds.setPartitionCount(1);
		ds.setMinConnectionsPerPartition(1);
		ds.setMaxConnectionsPerPartition(2);
		
		String str = "Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID)) ?t = plainLiteral(?NAME) From PERSON";
			
		
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
		
		Config config = SparqlifyUtils.readConfig(in);
		//DataSource ds = SparqlifyUtils.createDefaultDatabase("test", bundle.getSql().getInputStream());
		final QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(ds, config, null, null);

		final Runnable test = new Runnable() {
			
			@Override
			public void run() {
				String queryStr = "Describe <http://ex.org/person/1>";
				//String queryStr = "Construct { ?s ?p ?o } { ?s ?p ?o . Filter(?s = <http://ex.org/person/1>) }";
				
				QueryExecution qe = qef.createQueryExecution(queryStr);
				Model result = qe.execDescribe();
				//Model result = qe.execConstruct();
				result.write(System.out, "TTL");				
			}
		};
		
		final Runnable loop = new Runnable() {
			@Override
			public void run() {
				for(int i = 0; i < 50; ++i) {
					System.out.println("Run #" + i);
					test.run();
				}
			}
		};
		
		Thread t = new Thread(loop);
		t.start();
		t.join(20000); // wait 20 seconds

		
		Assert.assertFalse(t.isAlive());
		
//		
//		ExecutorService es = Executors.newFixedThreadPool(5);
//		for(int i = 0; i < 1000; ++i) {
//			es.execute(test);
//		}
//
//		es.w
		
		SparqlifyUtils.shutdownH2(ds);
	}
}
