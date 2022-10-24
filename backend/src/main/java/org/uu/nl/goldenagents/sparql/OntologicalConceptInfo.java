package org.uu.nl.goldenagents.sparql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.jena.graph.Node;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

public class OntologicalConceptInfo implements FIPASendableObject {
	
	private static final long serialVersionUID = 1L;
	private String label;
	private boolean isClass;
	private int count;
	private LinkedHashMap<String, Integer> starCombinationCounts = 
			new LinkedHashMap<String, Integer>();
	private LinkedHashMap<MappingPropertyType, ArrayList<Node>> ontologicalMappings = 
			new LinkedHashMap<MappingPropertyType, ArrayList<Node>>();
	private HashSet<String> entities = new HashSet<>();

	public OntologicalConceptInfo(String label) {
		super();
		this.label = label;
	}
	
	public OntologicalConceptInfo(String label, boolean isClass) {
		super();
		this.label = label;
		this.isClass = isClass;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isClass() {
		return isClass;
	}

	public void setClassCheck(boolean isClass) {
		this.isClass = isClass;
	}

	public boolean isComplete() {
		return !starCombinationCounts.isEmpty();
	}
	
	public int getStarCombination(String concept) {
		if(!starCombinationCounts.containsKey(concept)) {
			return 0;
		}
		return starCombinationCounts.get(concept);
	}
	
	public void addStarCombination(String concept, int combCount) {
		starCombinationCounts.put(concept, combCount);
	}
	
	public float getStarCombinationRatio(String concept) {
		if(!starCombinationCounts.containsKey(concept)) {
			return 0f;
		}
		return 1f*starCombinationCounts.get(concept)/count;
	}
	
	public LinkedHashMap<String, Integer> getStarCombinationCounts() {
		return starCombinationCounts;
	}
	
	public void addMapping(MappingPropertyType mappingPropertyType, Node node) {
		if(!ontologicalMappings.containsKey(mappingPropertyType)) {
			ontologicalMappings.put(mappingPropertyType, new ArrayList<Node>());
		}
		ontologicalMappings.get(mappingPropertyType).add(node);
	}
	
	public ArrayList<Node> getOntologicalMapping(MappingPropertyType relation) {
		if(ontologicalMappings.containsKey(relation)) {
			return ontologicalMappings.get(relation);
		}
		return null;
	}

	public LinkedHashMap<MappingPropertyType, ArrayList<Node>> getOntologicalMappings() {
		return ontologicalMappings;
	}
	
	public int getTotalNumberOfMappings() {
		int count =  ontologicalMappings.values().stream().map(ArrayList::size).reduce(0, Integer::sum);
		return count;
	}
	
	public HashSet<String> getEntities() {
		return entities;
	}

	public void setEntities(HashSet<String> entities) {
		this.entities = entities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OntologicalConceptInfo other = (OntologicalConceptInfo) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	@Override
	public String toString() {
		List<String> nodeValues = new ArrayList<>();
		for(List<Node> nodes : this.getOntologicalMappings().values()) {
			for(Node n : nodes) {
				nodeValues.add(n.getURI());
			}
		}
		return String.format(
				"%s -> %s",
				this.label,
				nodeValues.size() == 1 ? nodeValues.get(0) : "(" + String.join(", ", nodeValues)
		);
	}
}
