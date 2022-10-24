package org.uu.nl.goldenagents.sparql;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.uu.nl.net2apl.core.agent.AgentID;

/**
 * This class represents a single triple pattern in a SPARQL query.
 * 
 * @author Golden Agents Group, Utrecht University
 */
public class TripleInfo implements Serializable {

	public enum NodeType {
		VARIABLE, URI, BLANK, LITERAL, PATH;
		static NodeType fromString(String s) {
			if(s.startsWith("?")) return NodeType.VARIABLE;
			if(s.startsWith("_:")) return NodeType.BLANK;
			if(s.contains(":") || (s.startsWith("<") && s.endsWith(">"))) return NodeType.URI;
			return NodeType.LITERAL;
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	private String subject, object;
	private PathInfo predicate;
	private NodeType subjectType, predicateType, objectType;
	private Set<AgentID> possibleSources = new HashSet<>();
	private Set<AgentID> chosenSources = new HashSet<>();
	private Set<String> ontologicalConcepts = new HashSet<>();
	
	public TripleInfo(String subject, PathInfo predicate, String object) {
		this.subject = subject;
		this.subjectType = NodeType.fromString(this.subject);
		this.object = object;
		this.objectType = NodeType.fromString(this.object);

		for(String predicateElement : predicate.getPredicates()) {
			if(predicateElement.equals("a")) {
				predicate.update(predicateElement, "rdf:type");
			}
		}

		this.predicate = predicate;
		this.predicateType = NodeType.PATH;
	}
	
	private TripleInfo() {}
	
	public TripleInfo createCopy() {
		TripleInfo copy = new TripleInfo();
		copy.subject = subject;
		copy.predicate = new PathInfo(predicate.toString());
		copy.predicate.setAlias(predicate.getAlias());
		copy.object = object;
		copy.subjectType = NodeType.fromString(copy.subject);
		copy.predicateType = NodeType.PATH;
		copy.objectType = NodeType.fromString(copy.object);
		copy.possibleSources = new HashSet<>(possibleSources);
		copy.chosenSources = new HashSet<>(chosenSources);
		copy.ontologicalConcepts = new HashSet<>(ontologicalConcepts);
		return copy;
	}

	public boolean isPath() {
		return predicateType == NodeType.PATH;
	}
	
	public boolean isSimplePath() {
		return predicate.isSimple();
	}
	
	public String toConstructHeader() {
		return subject + " " + predicate.toConstructHeader() + " " + object;
	}
	
	public boolean hasPredicateType() {
		return (predicate.toString().equals("rdf:type"));
	}
	
	@Override
	public String toString() {
		return subject + " " + predicate + " " + object;
	}
	
	public String getSubject() {
		return subject;
	}

	public String getPredicate() {
		return predicate.toString();
	}

	public PathInfo getPredicatePath() {
		return predicate;
	}

	public String getObject() {
		return object;
	}
	
	public NodeType getSubjectType() {
		return subjectType;
	}

	public NodeType getPredicateType() {
		return predicateType;
	}

	public NodeType getObjectType() {
		return objectType;
	}

	public void setSubject(String subject) {
		this.subject = subject;
		this.subjectType = NodeType.fromString(subject);
	}
	
	public void setObject(String object) {
		this.object = object;
		this.objectType = NodeType.fromString(object);
	}

	public boolean contains(String uri) {
		return this.subject.equals(uri) || this.object.equals(uri) || this.predicate.contains(uri);
	}

	public Set<AgentID> getPossibleSources() {
		return possibleSources;
	}

	public void setPossibleSources(Set<AgentID> possibleSources) {
		this.possibleSources = possibleSources;
	}

	public Set<AgentID> getChosenSources() {
		//If there is no chosen source, then return all the possible sources 
		if (chosenSources.size() == 0){
			return possibleSources;
		}
		return chosenSources;
	}
	
	public void setChosenSource(AgentID chosenSource) {
		this.chosenSources.add(chosenSource);
	}

	public void setChosenSources(Set<AgentID> chosenSources) {
		this.chosenSources = chosenSources;
	}
	
	public Set<String> getOntologicalConcepts() {
		if(ontologicalConcepts.isEmpty()) {
			learnOntologicalConcepts();
		}
		return ontologicalConcepts;
	}

	public void learnOntologicalConcepts(){
		if(subject.startsWith("ga:")) {
			ontologicalConcepts.add(subject);
		}
		if(object.startsWith("ga:")) {
			ontologicalConcepts.add(object);
		}
		for(String p : predicate.getPredicates()) {
			if(p.startsWith("ga:")) {
				ontologicalConcepts.add(p);
			}
		}
	}
	
}