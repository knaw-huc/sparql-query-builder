package org.uu.nl.goldenagents.exceptions;

public class BadQueryException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2494577688044436070L;

	private int line;
	private int column;
	private int length;


	public BadQueryException() {
		super("Query could not be parsed");
	}
	
	public BadQueryException(String message) {
		super(message);
	}

	public BadQueryException(String message, int line) {
		super(message);
		this.line = line;
	}

	public BadQueryException(String message, int line, int column) {
		super(message);
		this.line = line;
		this.column = column;
	}

	public BadQueryException(String message, int line, int column, int length) {
		super(message);
		this.line = line;
		this.column = column;
		this.length = length;
	}
}

