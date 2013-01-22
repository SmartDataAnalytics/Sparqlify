package org.aksw.sparqlify.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.aksw.commons.jena.util.QuadUtils;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangNQuads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.GraphMatcher;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


class SinkQuadsToSet
	implements Sink<Quad>
{
	private Set<Quad> quads = new HashSet<Quad>();
	
	@Override
	public void close() {
	}

	@Override
	public void send(Quad item) {
		quads.add(item);
	}

	@Override
	public void flush() {
	}

	public Set<Quad> getQuads() {
		return quads;
	}
}


class ResourceComparator
	implements Comparator<Resource>
{
	@Override
	public int compare(Resource a, Resource b) {
		return a.getFilename().compareTo(b.getFilename());
	}
}


class TestBundle {
	private Resource sql;
	private Resource mapping;
	private Resource expected;
	private Resource manifest;
	
	public TestBundle(Resource sql, Resource mapping, Resource expected, Resource manifest) {
		super();
		this.sql = sql;
		this.mapping = mapping;
		this.expected = expected;
	}

	public Resource getSql() {
		return sql;
	}

	public Resource getMapping() {
		return mapping;
	}

	public Resource getExpected() {
		return expected;
	}

	public Resource getManifest() {
		return manifest;
	}

	@Override
	public String toString() {
		return "TestBundle [sql=" + sql + ", mapping=" + mapping
				+ ", expected=" + expected + ", manifest=" + manifest + "]";
	}
}


class TestBundleReader
{
	private static final Logger logger = LoggerFactory.getLogger(R2rmlTest.class);	
	private static final Comparator<Resource> resourceComparator = new ResourceComparator();	
	private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private String spyBasePath = "/org/aksw/sparqlify/test_suite/";
	private String r2rmlBasePath = "/org/w3c/r2rml/test_suite/";

	
	public List<TestBundle> getTestBundles() throws IOException {
		List<TestBundle> result = new ArrayList<TestBundle>();
	
		Resource[] resources = resolver.getResources(r2rmlBasePath + "*");
		Arrays.sort(resources, resourceComparator);
		
		for (Resource r : resources) {
			List<TestBundle> tmp = process(r);
			result.addAll(tmp);
		}

		return result;
	}
	
	// Note treat file pattern r2rml(*).ttl
	public List<TestBundle> process(Resource r) throws IOException {
		//System.out.println(basePath + r.getFilename() + "/create.sql");

		List<TestBundle> result = new ArrayList<TestBundle>();
		
		
		String spyPathStr = spyBasePath + r.getFilename() + "/";
		String r2rPathStr = r2rmlBasePath + r.getFilename() + "/";
		
		Resource spyPathRes = resolver.getResource(spyPathStr);
		Resource r2rPathRes = resolver.getResource(r2rPathStr);
		if(!spyPathRes.exists()) {
			logger.warn("Resource does not exist " + spyPathStr);
			return result;
		}

		if(!r2rPathRes.exists()) {
			logger.warn("Resource does not exist " + r2rPathStr);
			return result;
		}
		
		
		
		Resource create = resolver.getResource(r2rPathStr + "create.sql");
		
		//Pattern pattern = Pattern.compile("r2rml(.*).ttl");
		Pattern pattern = Pattern.compile("sparqlify(.*).txt");
		
		//Resource[] r2rmls = resolver.getResources(testBaseSpy + "r2rml*.ttl");
		Resource[] r2rmls = resolver.getResources(spyPathStr + "sparqlify*.txt");
		Arrays.sort(r2rmls, resourceComparator);
		
		Resource manifest = null;
		
		for(Resource m : r2rmls) {
			//System.out.println("Got resource: " + m.getURI());
			
			String name = m.getFilename();
			
			Matcher matcher = pattern.matcher(name);
			boolean isFind = matcher.find();
			if(!isFind) {
				throw new RuntimeException("Should not happen"); // The regex pattern must match the resource pattern
			}

			String subTest = matcher.group(1);
			
			
			
			// get the expected result
			String mappingStr = r2rPathStr + "mapped" + subTest + ".nq";
			Resource expected = resolver.getResource(mappingStr);
			
			if(!expected.exists()) {
				logger.warn("No expected result found for " + name + " skipping test");
				continue;
			}
			
//			System.out.println("        " + expected.getFilename());
//			
//			System.out.println("    " + m.getFilename());
			
			TestBundle bundle = new TestBundle(create, m, expected, manifest);
			result.add(bundle);
		}
		
		return result;
		//System.out.println(createRes.getURI());
		//System.out.println(createRes.getFilename());
		//System.out.println("create exists? " + createRes.exists());
		
	}
}

class CompareUtils {
	
	public static Map<Node, Graph> indexAsGraphs(Collection<Quad> quads) {
		Map<Node, Graph> result = new HashMap<Node, Graph>();
		for(Quad q : quads) {
			Graph graph = result.get(q.getGraph());
			if(graph == null) {
				graph = GraphFactory.createDefaultGraph();
				result.put(q.getGraph(), graph);
			}
			
			graph.add(q.asTriple());
		}
		
		return result;
	}
	
	//public Set<Quad> 
	public static Set<Quad> alignActualQuads(Set<Quad> expected, Set<Quad> actual) {
		Map<Node, Graph> e = indexAsGraphs(expected);
		Map<Node, Graph> a = indexAsGraphs(actual);
		
		Set<Quad> result = alignActualQuads(e, a);
		return result;
	}
	
	
	public static Set<Quad> toQuads(Node g, Graph graph) {
		Set<Quad> result = new HashSet<Quad>();
		ExtendedIterator<Triple> it = graph.find(null, null, null);
		try {
			while(it.hasNext()) {
				Triple t = it.next();
				
				Quad quad = new Quad(g, t);					
				result.add(quad);
			}
		} finally {
			it.close();
		}

		return result;
	}

	public static Set<Quad> toQuads(Node g, Graph graph, Map<Node, Node> subst) {
		Set<Quad> result = new HashSet<Quad>();
		ExtendedIterator<Triple> it = graph.find(null, null, null);
		try {
			while(it.hasNext()) {
				Triple t = it.next();
				
				Quad tmp = new Quad(g, t);					
				Quad quad = QuadUtils.copySubstitute(tmp, subst);

				result.add(quad);
			}
		} finally {
			it.close();
		}

		return result;
	}

	
	/**
	 * Per graph alignment of quads.
	 * Equivalent blank node objects in distinct graphs may be mapped differently.
	 * Result of this method is not necessarily deterministic; see GraphMacher.match()
	 * 
	 * @param expected
	 * @param actual
	 * @return
	 */
	public static Set<Quad> alignActualQuads(Map<Node, Graph> expected, Map<Node, Graph> actual) {

		Set<Quad> result = new HashSet<Quad>();
		

//		Set<Quad> excessiveQuads = new HashSet<Quad>();
//		Set<Quad> missingQuads = new HashSet<Quad>();
		
		Set<Node> expectedGs = expected.keySet();
		Set<Node> actualGs = actual.keySet();
		
		Set<Node> excessiveGs = Sets.difference(actualGs, expectedGs);
		Set<Node> commonGs = Sets.intersection(expectedGs, actualGs);
//		Set<Node> missingGs = Sets.difference(expectedGs, actualGs);

		
		for(Node g : excessiveGs) {
			Graph graph = actual.get(g);
			Set<Quad> tmp = toQuads(g, graph);
			
			result.addAll(tmp);
		}
		
		for(Node g : commonGs) {
			Graph expectedGraph = expected.get(g);
			Graph actualGraph = actual.get(g);

			Node[][] rawMapping = GraphMatcher.match(actualGraph, expectedGraph);

			Map<Node, Node> mapping = new HashMap<Node, Node>();
			for(int i = 0; i < rawMapping.length; ++i) {
				Node source = rawMapping[i][0];
				Node target = rawMapping[i][1];
				mapping.put(source, target);
			}
			
			Set<Quad> tmp = toQuads(g, actualGraph, mapping);
			result.addAll(tmp);
		}

		return result;
	}
	
}

@RunWith(value = Parameterized.class)
public class R2rmlTest {

	private Logger logger = LoggerFactory.getLogger(R2rmlTest.class);
	
	//private Comparator<Resource> resourceComparator = new ResourceComparator();	
	//private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	private TestBundle testBundle;
	
	
	public R2rmlTest(TestBundle testBundle) {
		this.testBundle = testBundle;
	}
	
	@Parameters
	public static Collection<Object[]> data()
			throws IOException
	{	
		TestBundleReader testBundleReader = new TestBundleReader();
		List<TestBundle> testBundles = testBundleReader.getTestBundles();
		
		Object data[][] = new Object[testBundles.size()][1];
		
		
		for(int i = 0; i < testBundles.size(); ++i) {
			data[i][0] = testBundles.get(i);
		}

		Collection<Object[]> result = Arrays.asList(data); 
		
		return result;
	}

//	@Test
//	public void runTests() throws Exception {
//
//		List<TestBundle> bundles = process();
//		
//		System.out.println("Final: " + bundles);
//		
//		
//		runBundles(bundles);
//	}
//
//	
//	public void runBundles(List<TestBundle> bundles)
//			throws Exception
//	{
//		for(TestBundle bundle : bundles) {
//			runBundle(bundle);
//		}
//	}


	@Test
	public void runBundle()
			throws Exception
	{
		runBundle(testBundle);
	}

	public Set<Quad> readNQuads(InputStream in) {

		SinkQuadsToSet quadSink = new SinkQuadsToSet();
		LangNQuads parser = RiotReader.createParserNQuads(in, quadSink);
		parser.parse();

		Set<Quad> result = quadSink.getQuads();
		return result;
	}

	/**
	 * - Create database from resource
	 * - Create SparqlSqlRewriterSparqlify from resource 
	 * - Load nquads file from resource (how?)
	 * 
	 * 
	 * - Run dump on the rewriter (just create a set of quads)
	 * - Compare the results
	 * 
	 * @param bundle
	 * @throws IOException 
	 */
	public void runBundle(TestBundle bundle)
			throws Exception
	{
		System.out.println("Bundle: " + bundle);
		Set<Quad> expected = readNQuads(bundle.getExpected().getInputStream());
		Config config = SparqlifyUtils.readConfig(bundle.getMapping().getInputStream());
		DataSource ds = SparqlifyUtils.createDefaultDatabase("test", bundle.getSql().getInputStream());
		QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(ds, config, null, null);
		
		Set<Quad> actual = SparqlifyUtils.createDumpNQuads(qef);

		Set<Quad> alignedActual = CompareUtils.alignActualQuads(expected, actual);
		
		SparqlifyUtils.shutdownH2(ds);

		Set<Quad> excessive = Sets.difference(alignedActual, expected);
		Set<Quad> missing = Sets.difference(expected, alignedActual);

		System.out.println("Expected: " + expected);
		System.out.println("Actual  : " + alignedActual);

		System.out.println("Excessive: " + excessive);
		System.out.println("Missing: " + missing);
		
		
		System.out.println("---------------------------------");
		//String StreamUtils.toStringSafe();
		//ConfigP
		
		
	}
}
