package org.aksw.sparqlify.algebra.sql.nodes;

import java.io.ByteArrayOutputStream;

import org.apache.jena.atlas.io.IndentedWriter;

public class SqlOpFormatter {

	
	public static String format(SqlOp op) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IndentedWriter writer = new IndentedWriter(baos);
		
		op.write(writer);
		writer.flush();
		
		//writer.println();
		
		String result = baos.toString();
		
		return result;
	}

}
