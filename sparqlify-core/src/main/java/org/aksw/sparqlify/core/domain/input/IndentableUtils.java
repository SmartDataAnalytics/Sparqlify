package org.aksw.sparqlify.core.domain.input;

import java.io.ByteArrayOutputStream;

import org.apache.jena.atlas.io.IndentedWriter;

public class IndentableUtils {
	public static String toString(Indentable obj) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IndentedWriter writer = new IndentedWriter(out);
		obj.asString(writer);
		writer.flush();
		writer.close();
		return out.toString();
	}
}