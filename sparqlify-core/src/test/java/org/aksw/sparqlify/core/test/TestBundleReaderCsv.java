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


class TestBundleReaderCsv
{
	private static final Logger logger = LoggerFactory.getLogger(TestBundleReaderCsv.class);	
	private static final Comparator<Resource> resourceComparator = new ResourceComparator();	
	private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private String testsBasePath = "/org/aksw/sparqlify/csv/";

	
	public List<TestBundleCsv> getTestBundles() throws IOException {
		List<TestBundleCsv> result = new ArrayList<TestBundleCsv>();
	
		Resource[] resources = resolver.getResources(testsBasePath + "*");
		Arrays.sort(resources, resourceComparator);
		
		for (Resource r : resources) {
			List<TestBundleCsv> tmp = process(r);
			result.addAll(tmp);
		}

		return result;
	}
	
	// Note treat file pattern r2rml(*).ttl
	public List<TestBundleCsv> process(Resource r) throws IOException {
		//System.out.println(basePath + r.getFilename() + "/create.sql");

		List<TestBundleCsv> result = new ArrayList<TestBundleCsv>();
		String testPathStr = testsBasePath + r.getFilename() + "/";
		
		Resource testPathRes = resolver.getResource(testPathStr);
		if(!testPathRes.exists()) {
			logger.warn("Resource does not exist " + testPathRes);
			return result;
		}

		
		Resource csv = resolver.getResource(testPathStr + "data.csv");
		
		//Pattern pattern = Pattern.compile("r2rml(.*).ttl");
		Pattern pattern = Pattern.compile("sparqlify(.*).txt");
		
		//Resource[] r2rmls = resolver.getResources(testBaseSpy + "r2rml*.ttl");
		Resource[] r2rmls = resolver.getResources(testPathStr + "sparqlify*.txt");
		Arrays.sort(r2rmls, resourceComparator);
		
		
		for(Resource mapping : r2rmls) {
			//System.out.println("Got resource: " + m.getURI());
			
			String name = mapping.getFilename();
			
			Matcher matcher = pattern.matcher(name);
			boolean isFind = matcher.find();
			if(!isFind) {
				throw new RuntimeException("Should not happen"); // The regex pattern must match the resource pattern
			}

			String subTest = matcher.group(1);
			
			
			String configStr = testPathStr + "config" + subTest + ".properties"; 
			Resource config = resolver.getResource(configStr);
			
			// get the expected result
			String mappingStr = testPathStr + "mapped" + subTest + ".nq";
			Resource expected = resolver.getResource(mappingStr);
			
			if(!expected.exists()) {
				logger.warn("No expected result found for " + name + " skipping test");
				continue;
			}
			
//			System.out.println("        " + expected.getFilename());
//			
//			System.out.println("    " + m.getFilename());
			
			TestBundleCsv bundle = new TestBundleCsv(csv, config, mapping, expected);
			result.add(bundle);
		}
		
		return result;
		//System.out.println(createRes.getURI());
		//System.out.println(createRes.getFilename());
		//System.out.println("create exists? " + createRes.exists());
		
	}
}