package org.aksw.sparqlify.util;

import java.io.InputStream;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

import org.apache.jena.sparql.core.Quad;

public class NQuadUtils {
	public static Set<Quad> readNQuads(InputStream in) {

		SinkQuadsToSet quadSink = new SinkQuadsToSet();
		StreamRDF streamRdf = StreamRDFLib.sinkQuads(quadSink);
		RDFDataMgr.parse(streamRdf, in, Lang.NQUADS);

		Set<Quad> result = quadSink.getQuads();
		return result;
	}

}
