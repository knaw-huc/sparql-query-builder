package org.uu.nl.goldenagents.agent.plan.broker.splitquery;

/**
 * This class is used to pass errors related to decomposing a query to one or more data source agents. This error should
 * be thrown if no such mapping can be found
 */
public class MissingExpertException extends Exception {

	private static final long serialVersionUID = 1L;

	private String errorMessage;

    /**
     * Construct a new MissingExpertException
     * @param errorMessage  Human readable error message hinting problem for finding agents to answer a query
     */
    public MissingExpertException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
