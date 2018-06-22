package org.aksw.sparqlify.platform.config;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.exprs_ext.E_StrConcatPermissive;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.obda.domain.api.Constraint;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.database.PrefixConstraint;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fuberlin.wiwiss.pubby.Configuration;
import de.fuberlin.wiwiss.pubby.DataSourceRegistry;
import de.fuberlin.wiwiss.pubby.vocab.CONF;


public class PubbyConfigFactory {


    public static Resource pubbySparqlEndpoint = ResourceFactory.createResource("urn://sparqlify/platform/pubby/sparql");

    private static final Logger logger = LoggerFactory.getLogger(PubbyConfigFactory.class);


    private String baseUri;
    private String contextPath;

    private String projectName = "";
    private String projectHomepage = "";
    private File baseConfigFile;
    private Config sparqlifyConfig;
    private QueryExecutionFactory queryExecutionFactory;

    //"BaseServlet.serverConfiguration"

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public PubbyConfigFactory() {
    }

    public QueryExecutionFactory getQueryExecutionFactory() {
        return queryExecutionFactory;
    }

    public void setQueryExecutionFactory(QueryExecutionFactory queryExecutionFactory) {
        this.queryExecutionFactory = queryExecutionFactory;
    }

    public void setBaseConfigFile(File file) {
        this.baseConfigFile = file;
    }

    public File getBaseConfigFile() {
        return baseConfigFile;
    }



    /*
    public void setOverlayModel(Model model) {
        this.overlayModel = model;
    }

    public Model getOverlayModel() {
        return overlayModel;
    }
    */


    public Config getSparqlifyConfig() {
        return sparqlifyConfig;
    }

    public void setSparqlifyConfig(Config sparqlifyConfig) {
        this.sparqlifyConfig = sparqlifyConfig;
    }


    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectHomepage() {
        return projectHomepage;
    }

    public void setProjectHomepage(String projectHomepage) {
        this.projectHomepage = projectHomepage;
    }



    public String extractPrefix(Expr expr) {
        if(expr instanceof E_StrConcatPermissive || expr instanceof E_StrConcat) {
            ExprFunction fn = expr.getFunction();

            List<Expr> args = fn.getArgs();
            if(args.size() == 0) {
                logger.warn("Zero argument concat expression detected: " + expr);
            } else {

                Expr first = args.get(0);

                if(first.isConstant()) {
                    String value = first.getConstant().asUnquotedString();
                    return value;
                }
            }
        }
        return null;
    }


    public Set<String> getKnownPrefixes(Var var, ViewDefinition viewDef) {
        Set<String> result = new HashSet<String>();

        if(viewDef.getConstraints() != null) {

            for(Constraint constraint : viewDef.getConstraints().values()) {
                if(constraint instanceof PrefixConstraint) {
                    PrefixConstraint c = (PrefixConstraint)constraint;

                    if(var.equals(c.getVar())) {
                        result.addAll(c.getPrefixes().getSet());
                    }

                }
            }
        }

        Map<Var, Expr> vel = viewDef.getVarDefinition();
        Expr expr = vel.get(var);

        // We expect the expression to be a term constructor
        if(expr.isFunction()) {
            ExprFunction termCtor = expr.getFunction();
            if(termCtor.getFunctionIRI().equals(SparqlifyConstants.uriLabel)) {
                List<Expr> args = termCtor.getArgs();
                if(args.size() == 0) {
                    logger.warn("Zero length term constructor for var " + var + " in view definition" + viewDef.getName());
                } else {
                    Expr arg = args.get(0);

                    String prefix = extractPrefix(arg);
                    if(prefix != null) {
                        result.add(prefix);
                    }

                }

            }
        }






        return result;
    }

    public Set<String> getKnownPrefixes(ViewDefinition viewDef) {
        Set<String> result = new HashSet<String>();

        Set<Node> nodes = new HashSet<Node>();
        for(Quad quad : viewDef.getConstructTemplate()) {
            nodes.add(quad.getSubject());
        }

        for(Node node : nodes) {
            if(node.isVariable()) {
                Set<String> prefixes = getKnownPrefixes((Var)node, viewDef);
                result.addAll(prefixes);
            } else if(node.isURI()) {
                result.add(node.getURI());
            }
        }

        return result;
    }

    public Set<String> getKnownPrefixes(Config sparqlifyConfig) {
        Set<String> result = new HashSet<String>();

        for(ViewDefinition viewDef : sparqlifyConfig.getViewDefinitions()) {
            Set<String> prefixes = getKnownPrefixes(viewDef);

            result.addAll(prefixes);
        }

        return result;
    }

    public static Set<String> extractHostNames(Set<String> prefixes) {
        Set<String> result = new HashSet<String>();
        for(String prefix : prefixes) {
            try {
                URL url = new URL(prefix);
                String hostname = url.getProtocol() + "://" + url.getHost(); // + "/";
                result.add(hostname);
            } catch(Exception e) {
                logger.warn("Failed to extract hostname from: [" + prefix +"]");
            }
        }

        return result;
    }

    public void autoconfigure(Model model, Resource config) {
        Set<String> prefixes = getKnownPrefixes(sparqlifyConfig);
        Set<String> hostnames = PubbyConfigFactory.extractHostNames(prefixes);

        System.out.println("Prefixes: " + prefixes);
        System.out.println("Hostnames: " + hostnames);


        for(String hostname : hostnames) {
            writeDatasetDesc(model, config, hostname);
        }
    }


    public void writeDatasetDesc(Model model, Resource parentConfig, String prefix) {

        Resource dataset = model.createResource(new AnonId());
        Resource datasetBase = model.createResource(prefix + contextPath);

        model.add(parentConfig, CONF.dataset, dataset);


        //Resource pubbySparqlEndpoint = model.createResource(baseUri + "sparql");
        //PubbyConfigFactory.

        model.add(dataset, CONF.sparqlEndpoint, pubbySparqlEndpoint);
        model.add(dataset, CONF.datasetBase, datasetBase);
        model.add(dataset, CONF.fixUnescapedCharacters, model.createLiteral("(),'!$&*+;=@"));

        /*
        #   conf:dataset [
        #        conf:sparqlEndpoint <http://localhost:7531/sparql>;
        #        conf:sparqlDefaultGraph <http://example.org>;
        #        conf:datasetBase <http://your-dataset-namespace.org/>;
        #        conf:fixUnescapedCharacters "(),'!$&*+;=@";
        #       ];
*/



    }


    // TODO Do not call this method more than once; it will register a datasource with the same name again
    public Configuration create() {
        //Model baseModel = FileManager.get().loadModel(baseConfigFile.getAbsoluteFile().toURI().toString());

        // TODO: We need to register a new sparql datasource with each call!
        QefDataSource dataSource = new QefDataSource(queryExecutionFactory, pubbySparqlEndpoint.getURI());
        DataSourceRegistry.getInstance().put(pubbySparqlEndpoint.getURI(), dataSource);


        /*
        for(int i = 0; i < 15; ++i) {
            System.out.println("__________________________________________________________" + this.getClass().getName());
        }
        */

        //Model model = baseModel;


        Model model = ModelFactory.createDefaultModel();

        model.setNsPrefixes(sparqlifyConfig.getPrefixMapping());

        Resource config = model.createResource("urn://sparqlify/platform/pubby/config");

        model.add(config, RDF.type, CONF.Configuration);
        model.add(config, CONF.webBase, model.createResource(baseUri));
        model.add(config, CONF.projectName, model.createLiteral(projectName));
        model.add(config, CONF.projectHomepage, model.createResource(projectHomepage));


        autoconfigure(model, config);

        System.out.println("Pubby configuration:");
        System.out.println("-----------------------------------------------");
        model.write(System.out, "TURTLE");
        System.out.println("-----------------------------------------------");

        Configuration result = new Configuration(model);


        return result;
    }
}
