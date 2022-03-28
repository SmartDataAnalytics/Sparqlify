package org.aksw.sparqlify.trash;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.obda.domain.api.LogicalTable;
import org.aksw.obda.domain.impl.LogicalTableQueryString;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.obda.jena.r2rml.impl.R2rmlExporter;
import org.aksw.obda.jena.r2rml.impl.R2rmlImporter;
import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;
import org.aksw.sparqlify.config.lang.ViewDefinitionParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.validation.LoggerCount;
import org.aksw.sparqlify.web.SparqlifyCliHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExporterR2RMLExample {
    private static final Logger logger = LoggerFactory.getLogger(ExporterR2RMLExample.class);


    private static final Options cliOptions = new Options();

    public static void main(String[] args)
        throws Exception
    {
//        LogicalTable test = new LogicalTableQueryString("test");
//        test.tryAs(LogicalTableQueryString.class).ifPresent(name -> System.out.println("Got: " + name));

        Option mappingSourceOption = new Option("m", "mapping", true, "Sparqlify mapping file (can be specified multiple times)");
        mappingSourceOption.setArgs(Option.UNLIMITED_VALUES);
        cliOptions.addOption(mappingSourceOption);

        LoggerCount loggerCount = new LoggerCount(logger);


        CommandLineParser cliParser = new GnuParser();



        CommandLine commandLine = cliParser.parse(cliOptions, args);

        SparqlifyCliHelper.onErrorPrintHelpAndExit(cliOptions, loggerCount, -1);

        Config config = SparqlifyCliHelper.parseSmlConfigs(commandLine, loggerCount);


        // FIXME: commented out because this would cause errors
        //ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
//        ViewDefinitionParser parser = new ViewDefinitionParser();
//
//        ViewDefinition personView = parser.parse("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID)) ?t = plainLiteral(?NAME) From person");
//        ViewDefinition deptView = parser.parse("Prefix ex:<http://ex.org/> Create View dept As Construct { ?s a ex:Department ; ex:name ?t } With ?s = uri(concat('http://ex.org/dept/', ?ID)) ?t = plainLiteral(?NAME) From dept");
//        ViewDefinition personToDeptView = parser.parse("Prefix ex:<http://ex.org/> Create View person_to_dept As Construct { ?p ex:worksIn ?d } With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID)) ?d = uri(concat('http://ex.org/dept/', ?DEPT_ID)) From person_to_dept");
//
//        System.out.println(personView);
//
//        Collection<ViewDefinition> viewDefs= Arrays.asList(personView, deptView, personToDeptView);

        Collection<ViewDefinition> viewDefs = config.getViewDefinitions();
        /*
        System.out.println("test"
        System.out.println(personView);

        Collection<ViewDefinition> viewDefs= Arrays.asList(personView, deptView, personToDeptView);

        /*
        System.out.println("test");
        */

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rr", R2RMLXStrings.uri);

        // FIXME: commented out because this would cause errors
        //exportR2RML(viewDefs, model);
        new R2rmlExporter().export(model, viewDefs);

        System.out.println("R2R-ML Output:");
        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);


        System.out.println("R2R-ML Import:");
        Collection<org.aksw.obda.jena.domain.impl.ViewDefinition> vds = new R2rmlImporter().read(model, SqlCodecUtils.createSqlCodecDoubleQuotes());


        for(org.aksw.obda.jena.domain.impl.ViewDefinition vd : vds) {
            System.out.println(vd);
        }

    }

}

