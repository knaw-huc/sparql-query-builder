package org.uu.nl.goldenagents.exceptions;

public class InvalidIdException extends RuntimeException {

	private static final long serialVersionUID = 1638716998633038844L;

	public InvalidIdException() {
		this("Invalid ID");
	}
	
	public InvalidIdException(String message) {
		super(message);
	}
}
