package org.aksw.sparqlify.admin.web.common;

import java.util.Calendar;

public class LogEvent {
	private Calendar timeStamp;
	private String level;
	private String message;

	public LogEvent(Calendar timeStamp, String level, String message) {
		super();
		this.timeStamp = timeStamp;
		this.level = level;
		this.message = message;
	}

	public Calendar getTimeStamp() {
		return timeStamp;
	}

	public String getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "LogEvent [timeStamp=" + timeStamp + ", level=" + level
				+ ", message=" + message + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogEvent other = (LogEvent) obj;
		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.equals(other.timeStamp))
			return false;
		return true;
	}	
}