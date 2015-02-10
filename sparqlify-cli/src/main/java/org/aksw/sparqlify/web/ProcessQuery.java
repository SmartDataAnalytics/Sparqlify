package org.aksw.sparqlify.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.jena_sparql_api.utils.SparqlFormatterUtils;
import org.aksw.jena_sparql_api.utils.Writer;
import org.aksw.sparqlify.core.sparql.QueryEx;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.core.sparql.QueryFactoryEx;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ProcessQuery {

    public static <T> StreamingOutput wrapWriter(final QueryExecution qe, final Writer<T> writer, final T obj) {
        return new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				writer.write(baos, obj);
//
//				String str = baos.toString();
//				System.out.println(str);

                writer.write(output, obj);
                qe.close();

                output.flush();
            }
        };
    }

    public static StreamingOutput processQuery(String queryString, String format, QueryExecutionFactoryEx qeFactory)
        throws Exception
    {
        QueryEx queryEx = QueryFactoryEx.create(queryString); //QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
        StreamingOutput result = processQuery(queryEx, format, qeFactory);

        return result;
    }


    public static StreamingOutput processQuery(QueryEx queryEx, String format, QueryExecutionFactoryEx qeFactory)
            throws Exception
    {
        QueryExecution qe = qeFactory.createQueryExecution(queryEx);
        StreamingOutput result = processQuery(queryEx, format, qe);

        return result;
    }

    public static StreamingOutput processQuery(QueryEx query, String format, QueryExecution qe)
            throws Exception
    {
        try {

            if (query.isAskType()) {

                Writer<Boolean> writer = SparqlFormatterUtils
                        .getBooleanWriter(format);
                if (writer == null) {
                    throw new RuntimeException("No writer found: Boolean -> "
                            + format);
                }

                boolean value = qe.execAsk();
                return wrapWriter(qe, writer, value);

            } else if (query.isConstructType()) {

                Writer<Iterator<Triple>> writer = SparqlFormatterUtils.getTripleWriter(format);
                if (writer == null) {
                    throw new RuntimeException("No writer found: Model -> "
                            + format);
                }

                Iterator<Triple> it = qe.execConstructTriples();
                return wrapWriter(qe, writer, it);

            } else if (query.isSelectType()) {

                Writer<ResultSet> writer = SparqlFormatterUtils
                        .getResultSetWriter(format);
                if (writer == null) {
                    throw new RuntimeException("No writer found: ResultSet -> "
                            + format);
                }

                ResultSet resultSet = qe.execSelect();
                return wrapWriter(qe, writer, resultSet);

            } else if (query.isDescribeType()) {

                Writer<Model> writer = SparqlFormatterUtils.getModelWriter(format);
                if (writer == null) {
                    throw new RuntimeException("No formatter found: Model -> "
                            + format);
                }

                // TODO: Get the prefixes from the sparqlify config
                Model model = ModelFactory.createDefaultModel();
                model.setNsPrefix("lgd-owl", "http://linkedgeodata.org/ontology/");
                model.setNsPrefix("lgd-node",
                        "http://linkedgeodata.org/resource/node/");
                model.setNsPrefix("lgd-way",
                        "http://linkedgeodata.org/resource/way/");

                qe.execDescribe(model);

                // Tested what pubby does if there are multiple subjects
                // Result: Pubby does not display that in the HTML, although such
                // triples are in the RDF serializations
                // model.add(RDF.type, RDF.type, RDF.Property);

                // model.getNsPrefixMap().put("lgdo",
                // "http://linkedgeodata.org/ontology/");

                return wrapWriter(qe, writer, model);

            } else {

                throw new RuntimeException("Unknown query type");
            }
        }
        catch(Exception e) {
            if(qe != null) {
                qe.close();
            }

            throw e;
        }
    }

}
