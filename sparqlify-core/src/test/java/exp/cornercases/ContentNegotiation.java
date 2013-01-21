package exp.cornercases;

import java.net.URL;
import java.net.URLConnection;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import org.junit.Test;

public class ContentNegotiation {

	// TODO Start a test instance of the server using an embedded DB

	public void test(String mimeType)
			throws Exception
	{
		System.out.println("----------------------------");
		System.out.println("mimeType:" + mimeType);
		
		String qs = "Select * { ?s ?p ?o . } Limit 1";
		
		URL test = new URL("http://localhost:9999/sparql?query=" + StringUtils.urlEncode(qs));
        
        URLConnection connection = test.openConnection();
        if(mimeType != null) {
        	connection.setRequestProperty("Accept", mimeType); //"application/rdf+xml");
        }
        
        //connection.setRequestProperty("Accept", "text/plain");
        
        StreamUtils.copyThenClose(test.openStream(), System.out);
	}
	
	@Test
	public void testJson()
		throws Exception
	{
		test("application/json");
	}
	
	@Test
	public void testPlain()
		throws Exception
	{
		test("text/plain");
	}

	@Test
	public void testRdfXml()
		throws Exception
	{
		test("application/rdf+xml");
	}
	
	
}
