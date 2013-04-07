package org.aksw.sparqlify.csv;

import java.io.IOException;
import java.io.Reader;

/**
 * AbstractBase class for turning anything that returns strings into a reader.
 * 
 * @author raven
 *
 */
public abstract class ReaderStringBase
	extends Reader
{	
	private String line;
	private int offset = 0; // set to -1 when done
	
	@Override
	public int read(char[] cbuf, int off, int len)
			throws IOException
	{	
		if(offset < 0) {
			return -1;
		}
		
		int initOff = off;
		
		while(len > 0) {
			int lineLen = line == null ? 0 : line.length();
			int lineAvailLen = lineLen - offset;

			int readLen = Math.min(lineAvailLen, len);
			if(readLen > 0) {
				line.getChars(offset, offset + readLen, cbuf, off);
				off += readLen;
				len -= readLen;
				lineAvailLen -= readLen;
			}
			
			if(len > 0 && lineAvailLen <= 0) {
				line = nextString();
				if(line == null) {
					offset = -1;
					break;
				}
				offset = 0;
			}
		}
		
		int result = off - initOff;

		return result;
	}
	
	abstract protected String nextString();
}