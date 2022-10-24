package org.uu.nl.goldenagents.sparql;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

public enum MappingPropertyType {
	
	/**
	 * owl:subClassOf relation is not included in the owl class of Jena
	 */
	OWL_SUBCLASS(ResourceFactory.createProperty(OWL.getURI(), "subClassOf")),
	OWL_EQ_PROPERTY(OWL.equivalentProperty),
	RDFS_SUBCLASS(RDFS.subClassOf),
	OWL_INVERSE(OWL.inverseOf),
	OWL_EQ_CLASS(OWL.equivalentClass),
	RDFS_SUBPROPERTY(RDFS.subPropertyOf);
	
	private final Property property;

	public boolean isClass() {
		return OWL_SUBCLASS.equals(this) || OWL_EQ_CLASS.equals(this) || RDFS_SUBCLASS.equals(this);
	}

	public boolean isEquivalentProperty() {
		return OWL_EQ_PROPERTY.equals(this) || RDFS_SUBPROPERTY.equals(this);
	}

	public boolean isInverseProperty() {
		return OWL_INVERSE.equals(this);
	}
	
	MappingPropertyType(Property property) {
		this.property = property;
	}

	public Property getProperty() {
		return this.property;
	}
	
	public static MappingPropertyType fromProperty(Property property) {
		for(MappingPropertyType type : MappingPropertyType.values()) {
			if(type.getProperty().equals(property)) {
				return type;
			}
		}
		return null;
	}
	
	public static MappingPropertyType fromNode(Node node) {
		for(MappingPropertyType type : MappingPropertyType.values()) {
			if(type.getProperty().asNode().equals(node)) {
				return type;
			}
		}
		return null;
	}
	
}
