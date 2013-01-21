package org.aksw.sparqlify.core.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class ResourceComparator
	implements Comparator<Resource>
{
	@Override
	public int compare(Resource a, Resource b) {
		return a.getFilename().compareTo(b.getFilename());
	}
}

public class R2rmlTests {

	private Logger logger = LoggerFactory.getLogger(R2rmlTests.class);
	
	private Comparator<Resource> resourceComparator = new ResourceComparator();
	
	private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	private String basePath = "/org/w3c/r2rml/test_suite/";

	public R2rmlTests() {
	}

	@Test
	public void runTests() throws IOException {

		Resource[] resources = resolver.getResources(basePath + "*");

		// Order resources
		Arrays.sort(resources, new Comparator<Resource>() {
			@Override
			public int compare(Resource a, Resource b) {
				return a.getFilename().compareTo(b.getFilename());
			}
		});

		for (Resource r : resources) {
			process(r);
		}
	}

	// Note treat file pattern r2rml(*).ttl
	public void process(Resource r) throws IOException {
		//System.out.println(basePath + r.getFilename() + "/create.sql");
		String testBase = basePath + r.getFilename() + "/";
		Resource create = resolver.getResource(testBase + "create.sql");
		
		Pattern pattern = Pattern.compile("r2rml(.*).ttl");
		
		Resource[] r2rmls = resolver.getResources(testBase + "r2rml*.ttl");
		Arrays.sort(r2rmls, resourceComparator);
		
		for(Resource m : r2rmls) {
			String name = m.getFilename();
			
			Matcher matcher = pattern.matcher(name);
			boolean isFind = matcher.find();
			if(!isFind) {
				throw new RuntimeException("Should not happen"); // The regex pattern must match the resource pattern
			}

			String subTest = matcher.group(1);
			
			
			
			// get the expected result
			String mappingStr = testBase + "mapped" + subTest + ".nq";
			Resource mapping = resolver.getResource(mappingStr);
			
			if(!mapping.exists()) {
				logger.warn("No expected result found for " + name + " skipping test");
				continue;
			}
			
			System.out.println("        " + mapping.getFilename());
			
			System.out.println("    " + m.getFilename());
		}
		
		
		//System.out.println(createRes.getURI());
		//System.out.println(createRes.getFilename());
		//System.out.println("create exists? " + createRes.exists());
		
	}
}
