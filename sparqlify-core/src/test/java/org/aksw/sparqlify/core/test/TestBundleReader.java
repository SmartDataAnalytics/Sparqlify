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
			
			TestBundle bundle = new TestBundle(r.getFilename(), create, m, expected, manifest);
			result.add(bundle);
		}
		
		return result;
		//System.out.println(createRes.getURI());
		//System.out.println(createRes.getFilename());
		//System.out.println("create exists? " + createRes.exists());
		
	}
}