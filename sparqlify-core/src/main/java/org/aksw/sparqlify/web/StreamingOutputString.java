package org.aksw.sparqlify.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public class StreamingOutputString 
	implements StreamingOutput {

	String str;
	
	public StreamingOutputString(String str) {
		this.str = str;
	}
	
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {
		PrintStream out = new PrintStream(output);
		
		out.println(str);
		out.flush();
	}
	
	public static StreamingOutputString create(String str) {
		return new StreamingOutputString(str);
	}
	
}