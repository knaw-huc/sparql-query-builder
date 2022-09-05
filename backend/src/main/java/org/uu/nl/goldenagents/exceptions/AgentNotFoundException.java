package org.uu.nl.goldenagents.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AgentNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1066261803617625445L;

	public AgentNotFoundException() {
		this("Agent not found");
	}
	
	public AgentNotFoundException(String exception) {
		super(exception);
	}
}
