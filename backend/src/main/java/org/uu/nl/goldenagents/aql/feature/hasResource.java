package org.uu.nl.goldenagents.aql.feature;

import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.Objects;

/**
 * An IRI Reference is anything that can be denoted with an IRI in an RDF graph
 */
public abstract class hasResource extends Feature {

    protected SerializableResourceImpl resource;

    /**
     * An optional NL label for easy representation. Provided by the graph as rdfs:label. If none is present,
     * the formal name of this resource will be used.
     */
    protected String label;

    public SerializableResourceImpl getResource() {
        return this.resource;
    }

    public hasResource(SerializableResourceImpl resource) {
        super();
        this.resource = resource;
    }

    public hasResource(SerializableResourceImpl resource, String label) {
        super();
        this.resource = resource;
        this.label = label;
    }

    protected hasResource(SerializableResourceImpl resource, String label, ID focusName, ID parent) {
        super(focusName, parent);
        this.resource = resource;
        this.label = label;
    }

    @Override
    public String getFirstResourceLabel() {
        return this.label;
    }

    /**
     * AQL label representing this node in the AQL query
     *
     * @return String
     */
    @Override
    public String getAQLLabel() {
        return label == null ? resource.getLabel() : label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if(!getClass().equals(obj.getClass())) return false;
        if(!obj.getClass().isInstance(this)) return false;
        hasResource t = (hasResource) obj;
        if(!this.resource.getURI().equals(t.resource.getURI())) return false;
        if(!this.label.equals(t.label)) return false;
        return this.type.equals(t.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass().getName(), resource.getURI(), label);
    }
}
