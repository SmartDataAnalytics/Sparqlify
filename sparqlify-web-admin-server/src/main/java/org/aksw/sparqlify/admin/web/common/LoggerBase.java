package org.aksw.sparqlify.admin.web.common;

import org.slf4j.Logger;
import org.slf4j.ext.LoggerWrapper;

public abstract class LoggerBase
	extends LoggerWrapper
{

	public LoggerBase(Logger logger) {
		super(logger, "loggerMem");
	}
	
	abstract void process(String level, String message);
	
	@Override
	public void error(String msg) {
		super.error(msg);
		process("error", msg);
	}

	@Override
	public void warn(String msg) {
		super.warn(msg);
		process("warn", msg);
	}

	@Override
	public void info(String msg) {
		super.info(msg);
		process("info", msg);
	}

	@Override
	public void debug(String msg) {
		super.debug(msg);
		process("debug", msg);
	}
}