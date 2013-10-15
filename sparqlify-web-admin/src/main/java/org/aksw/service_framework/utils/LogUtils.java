package org.aksw.service_framework.utils;

import org.aksw.sparqlify.admin.model.LogMessage;
import org.aksw.sparqlify.admin.web.common.LogEvent;

import com.google.common.base.Function;

public class LogUtils {
	public static final Function<LogEvent, LogMessage> convertLog = new Function<LogEvent, LogMessage>() {
		@Override
		public LogMessage apply(LogEvent ev) {
			LogMessage result = new LogMessage();
			result.setLevel(ev.getLevel());
			result.setText(ev.getMessage());
			
			return result;
		}
	};
}