package org.aksw.sparqlify.admin.web.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;

public class LoggerMem
	extends LoggerBase
{
	private List<LogEvent> logEvents = new ArrayList<LogEvent>();
	
	public LoggerMem(Logger logger) {
		super(logger);
	}
	
	public List<LogEvent> getLogEvents() {
		return logEvents;
	}

	@Override
	void process(String level, String message) {
		Calendar cal =  GregorianCalendar.getInstance();
		LogEvent logEvent = new LogEvent(cal, level, message);

		logEvents.add(logEvent);
	}
}