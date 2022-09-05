
package org.uu.nl.goldenagents.netmodels.angular;

import java.util.Date;

public class ApiError {

	private final Date timestamp;
	private final String message;
	private final String details;
	private final String[] trace;

	public ApiError(Date timestamp, String message, String details, String[] trace) {
		this.timestamp = timestamp;
		this.message = message;
		this.details = details;
		this.trace = trace;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public String getDetails() {
		return details;
	}

	public String[] getTrace() {
		return trace;
	}
}
