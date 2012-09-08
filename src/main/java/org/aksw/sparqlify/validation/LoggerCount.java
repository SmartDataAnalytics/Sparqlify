package org.aksw.sparqlify.validation;

import org.slf4j.Logger;
import org.slf4j.ext.LoggerWrapper;

/**
 * TODO There just has to be some class which already does the counting...
 * but I could'nt find it yet
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class LoggerCount
	extends LoggerWrapper
{
	public LoggerCount(Logger logger) {
		super(logger, "Counting " + logger.getName());
	}

	public LoggerCount(Logger logger, String fqcn) {
		super(logger, fqcn);
	}

	private int errorCount = 0;
	private int warningCount = 0;

	@Override
	public void error(String message) {
		System.err.println(message);
		++errorCount; 
	}

	//@Override
	public void warn(String message) {
		System.err.println(message);
		++warningCount; 
	}

	
	public int getErrorCount() {
		return this.errorCount;
	}
	
	public int getWarningCount() {
		return this.warningCount;
	}
}