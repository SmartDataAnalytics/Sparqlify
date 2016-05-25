package exp.cornercases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.views.CandidateViewSelector;
import org.aksw.jena_sparql_api.views.ViewQuad;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.ViewDefinitionNormalizerImpl;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.util.ExprRewriteSystem;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;
import org.antlr.runtime.RecognitionException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDF;

public class CandidateViewSelectionTests {

    //@Test
    public void creationTest() throws RecognitionException, SQLException, IOException {

        DataSource dataSource = SparqlifyUtils.createTestDatabase();
        Connection conn = dataSource.getConnection();

        // typeAliases for the H2 datatype
        Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));


        ViewDefinitionFactory vdFactory = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);

        String testView = "Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) From person";
        ViewDefinition coreVd = vdFactory.create(testView);

        System.out.println("VD: " + coreVd);

        //TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
        ExprRewriteSystem ers = SparqlifyUtils.createExprRewriteSystem();
        //OpMappingRewriter opMappingRewriter = SparqlifyUtils.createDefaultOpMappingRewriter(typeSystem);
        MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(ers);

        CandidateViewSelector<ViewDefinition> system = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());
        system.addView(coreVd);


        String queryString = "Select * { ?s ?p ?o }";

        Query query = new Query();
        QueryFactory.parse(query, queryString, "http://ex.org/", Syntax.syntaxSPARQL_11);
        Op op = system.getApplicableViews(query);

        System.out.println(op);

    }


    //@Test
    public void test2() throws Exception {
        RdfViewSystemOld.initSparqlifyFunctions();


        TypeSystem datatypeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();


        DataSource dataSource = SparqlifyUtils.createTestDatabase();
        Connection conn = dataSource.getConnection();

        // typeAliases for the H2 datatype
        Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));


        ViewDefinitionFactory vdf = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);

        ViewDefinition personView = vdf.create("Prefix ex:<http://ex.org/> Create View person As Construct { ?s a ex:Person ; ex:name ?t } With ?s = uri(concat('http://ex.org/person/', ?ID) ?t = plainLiteral(?NAME) From person");
        ViewDefinition deptView = vdf.create("Prefix ex:<http://ex.org/> Create View dept As Construct { ?s a ex:Department ; ex:name ?t } With ?s = uri(concat('http://ex.org/dept/', ?ID) ?t = plainLiteral(?NAME) From dept");
        ViewDefinition personToDeptView = vdf.create("Prefix ex:<http://ex.org/> Create View person_to_dept As Construct { ?p ex:worksIn ?d } With ?p = uri(concat('http://ex.org/person/', ?PERSON_ID) ?d = uri(concat('http://ex.org/dept/', ?DEPT_ID) From person_to_dept");

        ExprRewriteSystem ers = SparqlifyUtils.createExprRewriteSystem();

        MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(ers);

        CandidateViewSelectorImpl candidateSelector = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());
        candidateSelector.addView(personView);
        candidateSelector.addView(deptView);
        candidateSelector.addView(personToDeptView);

        Var g = Var.alloc("g");
        Var s = Var.alloc("s");
        Var p = Var.alloc("p");
        Var o = Var.alloc("o");
        Node gv = Quad.defaultGraphNodeGenerated; //Quad.defaultGraphIRI; //urn:x-arq:DefaultGraphNode
        Node sv = NodeFactory.createURI("http://ex.org/person/5");
        Node pv = RDF.type.asNode();
        Node ov = NodeFactory.createURI("http://ex.org/Person");
        Quad quad = new Quad(g, s, p, o);

        RestrictionManagerImpl r = new RestrictionManagerImpl();
        r.stateExpr(new E_Equals(new ExprVar(g), NodeValue.makeNode(gv)));
        r.stateExpr(new E_Equals(new ExprVar(s), NodeValue.makeNode(sv)));
        r.stateExpr(new E_Equals(new ExprVar(p), NodeValue.makeNode(pv)));
        r.stateExpr(new E_Equals(new ExprVar(o), NodeValue.makeNode(ov)));

        /*
        r.stateNode(g, gv);
        r.stateNode(s, sv);
        r.stateNode(p, pv);
        r.stateNode(o, ov);
        */

        Set<ViewQuad<ViewDefinition>> viewQuads = candidateSelector.findCandidates(quad, r);

        // If the constraints are working, there should be only 1 candidate


        System.out.println("# View quads: " + viewQuads.size());
        System.out.println("View quads: " + viewQuads);


        /*
        Query query = QueryFactory.create("Prefix ex:<http://ex.org/> Select * { <http://ex.org/person/123> a ex:Person }");
        Op op = candidateSelector.getApplicableViews(query);

        System.out.println(op);


        System.out.println(personView);

        Collection<ViewDefinition> viewDefs= Arrays.asList(personView, deptView, personToDeptView);
        */
    }
}
