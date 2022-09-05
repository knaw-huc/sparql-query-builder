package org.uu.nl.goldenagents.decompose.linkset;

import java.util.List;

/**
 * A class that stores matches in the format of type and properties for a URI 
 * that is known by a source
 * @author Golden Agents Group, Utrecht University
 */
public class SourceInfo {
	/**
	 * Source name
	 */
	private String name;
	/**
	 * URI that is stored in the source
	 */
	private String uri;
	/**
	 * All matches of the URI
	 */
	private List<MatchingInfo> matches;

	/**
	 * Default constructor
	 */
	public SourceInfo() {

	}

	/**
	 * Returns the name of the source
	 * @return the name of the source
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Returns the URI of the entity
	 * @return the URI of the entity
	 */
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	/**
	 * Returns MatchingInfo for a given type (rdf:type)
	 * @param type requested type
	 * @return MatchingInfo if it possible. Otherwise, null.
	 */
	public MatchingInfo getMatch(String type) {
		for(MatchingInfo mi : matches) {
			if(mi.getType().equals(type)) {
				return mi;
			}
		}
		return null;
	}
	/**
	 * Returns list of all matches
	 * @return list of all matches
	 */
	public List<MatchingInfo> getMatches() {
		return matches;
	}
	public void setMatches(List<MatchingInfo> matches) {
		this.matches = matches;
	}
	@Override
	public String toString() {
		return "SourceInfo [name=" + name + ", uri=" + uri + ", matches=" + matches + "]";
	}

}
