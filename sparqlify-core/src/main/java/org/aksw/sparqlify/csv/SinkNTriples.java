package org.aksw.sparqlify.csv;

import java.io.PrintStream;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Triple;

@Deprecated // Use Jena's SinkTripleOutput instead.
public class SinkNTriples
	implements Sink<Triple>
{
	private PrintStream out;
	
	public SinkNTriples(PrintStream out) {
		this.out = out;
	}
	
	
	@Override
	public void close() {
	}

	@Override
	public void send(Triple triple) {

		String str;
		try {
			str = TripleUtils.toNTripleString(triple);
		} catch(Exception e) {
			throw new RuntimeException("Could not serialize triple: " + triple, e);
		}
		out.println(str);
	}

	@Override
	public void flush() {
		out.flush();
	}
	
}
