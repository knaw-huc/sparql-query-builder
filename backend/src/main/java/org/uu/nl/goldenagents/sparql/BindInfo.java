package org.uu.nl.goldenagents.sparql;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BindInfo extends ConstraintInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Pattern findVariablePattern = Pattern.compile("\\?[a-zA-Z]*");
	private final Set<String> variables = new HashSet<>();
	private final String rawBindString;
	
	public BindInfo(String rawBindString) {
		this.rawBindString = rawBindString;
		final Matcher matcher = findVariablePattern.matcher(rawBindString);
	    while (matcher.find())
	        variables.add(matcher.group());
	}

	public Set<String> getVariables() {
		return variables;
	}

	@Override
	public String toString() {
		return rawBindString;
	}
}