/**
 * 
 */
package org.aksw.sparqlify.config.syntax.r2rml;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.aksw.r2rml.impl.jena.ImporterR2RML;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.junit.Test;

/**
 * @author sherif
 *
 */
@Deprecated
public class OfficialTests {

	@Test
	public void runTests()
		throws Exception
	{
		
		String fileNames[] = {
				"R2RMLTC",
		}; 
		
		for(String name : fileNames) {
			InputStream inActual = this.getClass().getResourceAsStream("/" + name + ".ttl");
			
			ImporterR2RML importer = new ImporterR2RML();
			Map<String, ViewDefinition> actuals = importer.load(inActual);
			

			Map<String, ViewDefinition> expecteds = new HashMap<String, ViewDefinition>();
			/*
			InputStream inExpected = this.getClass().getResourceAsStream("/" + name + ".sparqlify");
			ConfiguratorCandidateSelector
			*/

			Set<String> actualViewNames = actuals.keySet();
			Set<String> expectedViewNames = expecteds.keySet();
			//Set<String> expectedViewNames = Collections.singleton("foo");
			
			Assert.assertEquals(expectedViewNames, actualViewNames);
			
			for(String viewName : expectedViewNames) {
				ViewDefinition expected = expecteds.get(viewName);
				ViewDefinition actual = actuals.get(viewName);

				Assert.assertEquals(expected, actual);
			}			
		}
	}

}
