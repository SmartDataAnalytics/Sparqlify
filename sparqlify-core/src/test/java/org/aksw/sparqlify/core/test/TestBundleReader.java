package org.aksw.sparqlify.core.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class TestBundleReader
{
	private static final Logger logger = LoggerFactory.getLogger(R2rmlTest.class);	
	private static final Comparator<Resource> resourceComparator = new ResourceComparator();	
	private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private String smlBasePath = "/org/aksw/sml/r2rml_tests/";
	private String r2rmlBasePath = "/org/w3c/r2rml_tests/";

	
	public List<TestBundle> getTestBundles() throws IOException {
		List<TestBundle> result = new ArrayList<TestBundle>();
	
		Resource[] resources = resolver.getResources(r2rmlBasePath + "*");
		Arrays.sort(resources, resourceComparator);
		
		for (Resource r : resources) {
			//List<TestBundle> tmp = process(r);
			//result.addAll(tmp);
			
			TestBundle tmp = process(r);
			if(tmp != null) {
				result.add(tmp);
			}
		}

		return result;
	}
	
	// Note treat file pattern r2rml(*).ttl
	public TestBundle process(Resource r) throws IOException {
		//System.out.println(basePath + r.getFilename() + "/create.sql");

		//List<TestBundle> result = new ArrayList<TestBundle>();
		
		
		String spyPathStr = smlBasePath + r.getFilename() + "/";
		String r2rPathStr = r2rmlBasePath + r.getFilename() + "/";
		
		Resource spyPathRes = resolver.getResource(spyPathStr);
		Resource r2rPathRes = resolver.getResource(r2rPathStr);
		if(!spyPathRes.exists()) {
			logger.warn("Resource does not exist " + spyPathStr);
			return null;
		}

		if(!r2rPathRes.exists()) {
			logger.warn("Resource does not exist " + r2rPathStr);
			return null;
		}
		
		
		
		Resource create = resolver.getResource(r2rPathStr + "create.sql");
		
		//Pattern pattern = Pattern.compile("r2rml(.*).ttl");
		Pattern smlPattern = Pattern.compile("sparqlify(.*).txt");
		Pattern queryPattern = Pattern.compile("query(?!result)([a-z]*)([0-9]*).txt");
		
		// Query pattern: query{mapping-file-reference}{query-number}
		//Pattern queryPattern = Pattern.compile("query([a-z]*)([0-9]*).txt");
		
		
		//Resource[] r2rmls = resolver.getResources(testBaseSpy + "r2rml*.ttl");
		Resource[] r2rmls = resolver.getResources(spyPathStr + "sparqlify*.txt");		
		Arrays.sort(r2rmls, resourceComparator);
		
		Resource manifest = null;
		

		
		List<MappingBundle> mappingBundles = new ArrayList<MappingBundle>();
		for(Resource m : r2rmls) {
			//System.out.println("Got resource: " + m.getURI());
			
			String name = m.getFilename();
			
			Matcher matcher = smlPattern.matcher(name);
			boolean isFind = matcher.find();
			if(!isFind) {
				throw new RuntimeException("Should not happen"); // The regex pattern must match the resource pattern
			}

			String subTest = matcher.group(1);
			
			
			// Get the expected dump result
			String mappingStr = r2rPathStr + "mapped" + subTest + ".nq";
			Resource expected = resolver.getResource(mappingStr);
			
			if(!expected.exists()) {
				logger.warn("No expected result found for " + name + " skipping test");
				continue;
			}



			// For each mapping may be a set of queries
			List<QueryBundle> queryBundles = new ArrayList<QueryBundle>();

			Resource[] queries = resolver.getResources(spyPathStr + "query" + subTest + "*.txt");
			Arrays.sort(queries, resourceComparator);

			for(Resource q : queries) {
				
				String queryName = q.getFilename();
				
				Matcher queryMatcher = queryPattern.matcher(queryName);
				boolean isQueryFind = queryMatcher.find();
				if(!isQueryFind) {
					continue;
				}
				
				//String mappingRef = matcher.group(1);
				String queryNo = queryMatcher.group(2);
				
				String expectedResultStr = spyPathStr + "queryresult" + subTest + queryNo + ".xml";
				Resource expectedResult = resolver.getResource(expectedResultStr);
				
				if(!expectedResult.exists()) {
					logger.warn("No expected result found for " + name + " skipping test");
					continue;
				}
				
				QueryBundle queryBundle = new QueryBundle(queryName, q, expectedResult);
				queryBundles.add(queryBundle);
			}

			MappingBundle mappingBundle = new MappingBundle(subTest, m, expected, queryBundles);
			
//			System.out.println("        " + expected.getFilename());			
//			System.out.println("    " + m.getFilename());
			
			//TestBundle bundle = new TestBundle(r.getFilename(), create, m, expected, manifest, queryBundles);
			//result.add(bundle);
			mappingBundles.add(mappingBundle);
		}
		
		TestBundle result = new TestBundle(r.getFilename(), create, mappingBundles, manifest);
		
		
		return result;
		//System.out.println(createRes.getURI());
		//System.out.println(createRes.getFilename());
		//System.out.println("create exists? " + createRes.exists());
		
	}
}
