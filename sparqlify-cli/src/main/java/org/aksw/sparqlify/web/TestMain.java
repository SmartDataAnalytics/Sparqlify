package org.aksw.sparqlify.web;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class TestMain {

	public static void main(String[] args) throws InterruptedException {
		//String serviceUrl = "http://localhost:3333/sparql";
		String serviceUrl = "http://localhost:7531/sparql";
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(serviceUrl);
		
		
		//String queryString = StringUtils.urlDecode("PREFIX+bsbm-inst%3A+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2F%3E%0APREFIX+bsbm%3A+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Fvocabulary%2F%3E%0APREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0APREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0A%0ASELECT+%3Flabel+%3Fcomment+%3Fproducer+%3FproductFeature+%3FpropertyTextual1+%3FpropertyTextual2+%3FpropertyTextual3%0A+%3FpropertyNumeric1+%3FpropertyNumeric2+%3FpropertyTextual4+%3FpropertyTextual5+%3FpropertyNumeric4+%0AWHERE+%7B%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+rdfs%3Alabel+%3Flabel+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+rdfs%3Acomment+%3Fcomment+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3Aproducer+%3Fp+.%0A++++%3Fp+rdfs%3Alabel+%3Fproducer+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+dc%3Apublisher+%3Fp+.+%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductFeature+%3Ff+.%0A++++%3Ff+rdfs%3Alabel+%3FproductFeature+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyTextual1+%3FpropertyTextual1+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyTextual2+%3FpropertyTextual2+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyTextual3+%3FpropertyTextual3+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyNumeric1+%3FpropertyNumeric1+.%0A++++%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyNumeric2+%3FpropertyNumeric2+.%0A++++OPTIONAL+%7B+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyTextual4+%3FpropertyTextual4+%7D%0A++++OPTIONAL+%7B+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyTextual5+%3FpropertyTextual5+%7D%0A++++OPTIONAL+%7B+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FdataFromProducer2%2FProduct72%3E+bsbm%3AproductPropertyNumeric4+%3FpropertyNumeric4+%7D%0A%7D%0A");
		String queryString = StringUtils.urlDecode("PREFIX+bsbm-inst%3A+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2F%3E%0APREFIX+bsbm%3A+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Fvocabulary%2F%3E%0APREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0APREFIX+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0A%0ASELECT+DISTINCT+%3Fproduct+%3Flabel%0AWHERE+%7B+%0A++++%3Fproduct+rdfs%3Alabel+%3Flabel+.%0A++++%3Fproduct+a+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FProductType8%3E+.%0A++++%3Fproduct+bsbm%3AproductFeature+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FProductFeature9%3E+.+%0A++++%3Fproduct+bsbm%3AproductFeature+%3Chttp%3A%2F%2Fwww4.wiwiss.fu-berlin.de%2Fbizer%2Fbsbm%2Fv01%2Finstances%2FProductFeature5%3E+.+%0A++++%3Fproduct+bsbm%3AproductPropertyNumeric1+%3Fvalue1+.+%0A%09FILTER+%28%3Fvalue1+%3E+136%29+%0A%09%7D%0AORDER+BY+%3Flabel%0ALIMIT+10%0A");
		//queryString = "Select * { ?s ?p ?o } Limit 10";
		
		QueryExecution qe = qef.createQueryExecution(queryString);
		//QueryExecution qe = qef.createQueryExecution("Select * { ?s ?p ?o } Limit 10");
		
		ResultSet rs = qe.execSelect();
		
		/*
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			System.out.println(qs);	
		}
		*/
		
		ResultSetFormatter.out(System.out, rs);
		
		qe.close();
		
	}
	
}
