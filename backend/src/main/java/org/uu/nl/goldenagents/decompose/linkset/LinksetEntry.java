package org.uu.nl.goldenagents.decompose.linkset;

import java.util.ArrayList;
import java.util.List;

import org.uu.nl.net2apl.core.agent.AgentID;

/**
 * This class represents a linkset entry in a detailed linkset file,
 * which has URI, source, and matching type and properties info
 * @author Golden Agents Group, Utrecht University
 */
public class LinksetEntry {

	/**
	 * id of an entry
	 */
	private int id;
	/**
	 * sources that provide information for the entry
	 */
	private List<SourceInfo> sources;

	public LinksetEntry() {
		super();
	}

	/**
	 * Collects all the properties that can be provided for a given type
	 * @param type is the domain of properties
	 * @return
	 */
	public List<String> getAllProperties(String type) {
		List<String> allprop = new ArrayList<>();
		for(SourceInfo si : sources) {
			if(si.getMatch(type) != null)
				allprop.addAll(si.getMatch(type).getProperties());
		}
		return allprop;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns SourceInfo for a given source name
	 * @param source source name 
	 * @return SourceInfo for a given source name
	 */
	public SourceInfo getSource(String source) {
		for(SourceInfo si : sources) {
			if(si.getName().equals(source)) {
				return si;
			}
		}
		return null;
	}
	
	/**
	 * When an AgentID is given takes {@code Fragment} of the {@code URI}
	 * as source name and returns source info for the name.
	 * If the source name is mapped to something else than the fragment,
	 * use {@code getSource(String)}.
	 * @param	aid AgentID
	 * @return	SourceInfo for a given AgentID
	 * @see org.uu.nl.net2apl.core.agent.AgentID#getName
	 * @see java.net.URI#getFragment
	 */
	public SourceInfo getSource(AgentID aid) {
		return getSource(aid.getName().getFragment());
	}

	/**
	 * Returns list of source info
	 * @return list of source info
	 */
	public List<SourceInfo> getSources() {
		return sources;
	}

	public void setSources(List<SourceInfo> sources) {
		this.sources = sources;
	}
	@Override
	public String toString() {
		return "LinksetEntry [id=" + id + ", sources=" + sources.toString() + "]";
	}

}
