package exp.cornercases;

import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.commons.lang.time.StopWatch;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;

public class RemoteTest {
    public static void main(String[] args)
        throws Exception
    {
        System.out.println("Starting simple test");
        StopWatch sw = new StopWatch();
        sw.start();
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://localhost:8890/sparql", "http://bsbm.org/1m");

        QueryExecution qe = qef.createQueryExecution("Select ?s { { ?s ?p ?o . } Union { ?s ?p ?o . } Union { ?s ?p ?o . } }");
        ResultSet rs = qe.execSelect();


        int i = 0;
        while(rs.hasNext()) {
            rs.next();

            ++i;

            if(i % 10000 == 0) {
                System.out.println("i: " + i);
            }
        }

        sw.stop();

        System.out.println("Result:  " + i + " Time: " + (sw.getTime() / 1000.0) + " sec");
    }
}
