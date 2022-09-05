package org.uu.nl.goldenagents.decompose.linkset;

import java.util.List;

/**
 * This class represents matching information of a matching entity (URI).
 * It includes the type and properties of the match
 * @author Golden Agents Group, Utrecht University
 */
public class MatchingInfo {
	
	/**
	 * Type (rdf:type) of an matching entity
	 */
	private String type;
	/**
	 * Properties of the entity match
	 */
	private List<String> properties;
	
	/**
	 * Default constructor
	 */
	public MatchingInfo() {
		
	}
	
	/**
	 * Returns the type of the matching entity
	 * @return the type of the matching entity
	 */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Returns the properties of the matching entity
	 * @return the properties of the matching entity
	 */
	public List<String> getProperties() {
		return properties;
	}
	
	/**
	 * Checks if the matching properties includes the given property
	 * @param property to check
	 * @return true if it includes. Otherwise, false.
	 */
	public boolean hasProperty(String property) {
		if(properties.contains(property)) {
			return true;
		}
		return false;
	}
	
	public void setProperties(List<String> properties) {
		this.properties = properties;
	}
	
	@Override
	public String toString() {
		return "MatchingInfo [type=" + type + ", properties=" + properties.toString() + "]";
	}
	
}