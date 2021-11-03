package org.aksw.sparqlify.core.test.bsbm;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.connection.core.SparqlQueryConnectionJsa;
import org.aksw.sparqlify.core.builder.FluentSparqlifyFactory;
import org.antlr.runtime.RecognitionException;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdf.model.Model;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import benchmark.common.TestDriverParams;
import benchmark.generator.Generator;
import benchmark.serializer.SQLSerializer;
import benchmark.testdriver.LocalSPARQLParameterPool;
import benchmark.testdriver.SPARQLConnection2;
import benchmark.testdriver.TestDriver;
import benchmark.testdriver.TestDriverUtils;

public class MainSparqlifyBsbm {
    protected Connection conn;

    public static void createTestSetup() throws SQLException, IOException, RecognitionException {
        //SerializerModel serializer = new SerializerModel();
        File dataDir = Files.createTempDir();
        System.out.println("BSBM SQL output directory: " + dataDir);
        //dataDir.deleteOnExit();

        Generator.init(new String[] {});
        Generator.setSerializer(new SQLSerializer(dataDir, true, false, "benchmark"));
        Generator.run();

        EmbeddedDatabaseBuilder edb = new EmbeddedDatabaseBuilder();
        edb
            .setType(EmbeddedDatabaseType.H2)
            .setName("mem:testdb;DATABASE_TO_UPPER=false;MODE=PostgreSQL;")
            .addScript("classpath:org/aksw/bsbm/bsbm-schema.sql");

        // Add the generated script files
        for(File file : dataDir.listFiles(v -> v.getName().toLowerCase().endsWith(".sql"))) {
            System.out.println("File: " + file.getAbsolutePath());
            edb.addScript("file://" + file.getAbsolutePath());
        }


        EmbeddedDatabase ed = edb.build();



        TestDriverParams testDriverParams = Generator.getTestDriverParams();

        QueryExecutionFactory qef = new FluentSparqlifyFactory()
            .setDataSource(ed)
            .addResource("org/aksw/bsbm/bsbm.sml")
            .create();


        TestDriver testDriver = new TestDriver();
        testDriver.processProgramParameters(new String[]{"http://example.org/foobar/sparql", "-w", "0", "-runs", "1"});
        testDriver.setParameterPool(new LocalSPARQLParameterPool(testDriverParams, testDriver.getSeed()));
        testDriver.setServer(new SPARQLConnection2(new SparqlQueryConnectionJsa(qef)));

        testDriver.init();

        Model chartModel = TestDriverUtils.runWithCharts(testDriver, "http://example.org/my-bsbm-experiment/");

//    	List<Entry<StatisticalBarChart, Model>> chartSpecs = ChartTransform.transform(chartModel);
//
//    	for(Entry<StatisticalBarChart, Model> chartSpec : chartSpecs) {
////            CategoryChart xChart = ChartModelConfigurerXChart.toChart(chartSpec.getValue(), chartSpec.getKey());
//
////            new SwingWrapper<CategoryChart>(xChart).displayChart();
////            System.in.read();
//    	}



//        Model model = serializer.getModel();
//        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(model).create();


        ed.shutdown();
    }

    public static void main(String[] args) throws Exception {
        createTestSetup();

    }
}
