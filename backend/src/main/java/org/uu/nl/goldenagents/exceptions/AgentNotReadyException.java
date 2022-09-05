package org.uu.nl.goldenagents.exceptions;

public class AgentNotReadyException extends RuntimeException {
	
	private static final long serialVersionUID = 7183128075789895115L;

	public AgentNotReadyException() {
		this("Agent not yet ready");
	}
	
	public AgentNotReadyException(String exception) {
		super(exception);
	}
}
