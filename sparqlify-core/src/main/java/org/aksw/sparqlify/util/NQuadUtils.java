package org.aksw.sparqlify.util;

import java.io.InputStream;
import java.util.Set;

import org.apache.jena.riot.RiotReader;
import org.apache.jena.riot.lang.LangNQuads;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

import com.hp.hpl.jena.sparql.core.Quad;

public class NQuadUtils {
	public static Set<Quad> readNQuads(InputStream in) {

		SinkQuadsToSet quadSink = new SinkQuadsToSet();
		StreamRDF streamRdf = StreamRDFLib.sinkQuads(quadSink);
		LangNQuads parser = RiotReader.createParserNQuads(in, streamRdf);
		parser.parse();

		Set<Quad> result = quadSink.getQuads();
		return result;
	}

}
