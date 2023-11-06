package org.aksw.sparqlify.cli.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.obda.jena.r2rml.impl.R2rmlExporter;
import org.aksw.r2rmlx.common.vocab.R2RMLXStrings;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.web.SparqlifyCliHelper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "sml-to-r2rml",
        // versionProvider = VersionProviderRdfProcessingToolkit.class,
        description = "Convert SML to R2RML")
public class CmdExportSmlAsR2rml
    implements Callable<Integer>
{
    private static final Logger logger = LoggerFactory.getLogger(CmdExportSmlAsR2rml.class);

    @Option(names = { "-h", "--help" }, usageHelp = true)
    public boolean help = false;

    @Option(names = { "-v", "--version" }, versionHelp = true)
    public boolean version = false;


    @Parameters(arity = "0..*", description = "File names with RDF/SPARQL content and/or SPARQL statements")
    public List<String> nonOptionArgs = new ArrayList<>();

    @Override
    public Integer call() throws Exception {

        LoggerCount loggerCount = new LoggerCount(logger);

        List<Resource> resources = SparqlifyCliHelper.resolveFiles(nonOptionArgs, true, loggerCount);

//        		nonOptionArgs.stream()
//                .map(SparqlifyCliHelper.resourceLoader::getResource)
//                .collect(Collectors.toList());

        Config config = SparqlifyCliHelper.parseSmlConfigs(resources, loggerCount);
        Collection<ViewDefinition> viewDefs = config.getViewDefinitions();

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rr", R2RMLXStrings.uri);

        new R2rmlExporter().export(model, viewDefs);

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

        return 0;

//        System.out.println("R2R-ML Import:");
//        Collection<org.aksw.obda.jena.domain.impl.ViewDefinition> vds = new R2rmlImporter().read(model);
//
//
//        for(org.aksw.obda.jena.domain.impl.ViewDefinition vd : vds) {
//            System.out.println(vd);
//        }
    }
}
