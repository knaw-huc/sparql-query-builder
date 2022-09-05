package org.uu.nl.goldenagents.sparql;

import java.io.Serializable;
import java.util.Set;

public abstract class ConstraintInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public abstract Set<String> getVariables();

}
