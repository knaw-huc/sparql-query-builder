package org.uu.nl.goldenagents.netmodels.jena;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDFS;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Wrapper class for Jena's ResourceImpl, that allows serialization and thus sending across
 * a network
 */
public class SerializableResourceImpl implements Resource, Serializable {

    private ResourceImpl resource;

    public SerializableResourceImpl(ResourceImpl resource) {
        this.resource = resource;
    }

    public SerializableResourceImpl(String uri) {
        this.resource = new ResourceImpl(uri);
    }

    public SerializableResourceImpl(String nameSpace, String localName) {
        this.resource = new ResourceImpl(nameSpace, localName);
    }

    @Override
    public AnonId getId() {
        return this.resource.getId();
    }

    @Override
    public Resource inModel(Model m) {
        return this.resource.inModel(m);
    }

    @Override
    public boolean hasURI(String uri) {
        return false;
    }

    @Override
    public String getURI() {
        return this.resource.getURI();
    }

    @Override
    public String getNameSpace() {
        return this.resource.getNameSpace();
    }

    @Override
    public String getLocalName() {
        return this.resource.getLocalName();
    }

    @Override
    public Statement getRequiredProperty(Property p) {
        return this.resource.getRequiredProperty(p);
    }

    @Override
    public Statement getRequiredProperty(Property p, String lang) {
        return this.resource.getRequiredProperty(p, lang);
    }

    @Override
    public Statement getProperty(Property p) {
        return this.resource.getProperty(p);
    }

    @Override
    public Statement getProperty(Property p, String lang) {
        return this.resource.getProperty(p, lang);
    }

    @Override
    public StmtIterator listProperties(Property p) {
        return this.resource.listProperties(p);
    }

    @Override
    public StmtIterator listProperties(Property p, String lang) {
        return this.resource.listProperties(p, lang);
    }

    @Override
    public StmtIterator listProperties() {
        return this.resource.listProperties();
    }

    @Override
    public Resource addLiteral(Property p, boolean o) {
        return this.resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, long o) {
        return this.resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, char o) {
        return this.resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property value, double d) {
        return this.resource.addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property value, float d) {
        return this.resource.addLiteral(value, d);
    }

    @Override
    public Resource addLiteral(Property p, Object o) {
        return this.resource.addLiteral(p, o);
    }

    @Override
    public Resource addLiteral(Property p, Literal o) {
        return this.resource.addLiteral(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o) {
        return this.resource.addProperty(p, o);
    }

    @Override
    public Resource addProperty(Property p, String o, String l) {
        return this.resource.addProperty(p, o, l);
    }

    @Override
    public Resource addProperty(Property p, String lexicalForm, RDFDatatype datatype) {
        return this.resource.addProperty(p, lexicalForm, datatype);
    }

    @Override
    public Resource addProperty(Property p, RDFNode o) {
        return this.resource.addProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p) {
        return this.resource.hasProperty(p);
    }

    @Override
    public boolean hasLiteral(Property p, boolean o) {
        return this.resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, long o) {
        return this.resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, char o) {
        return this.resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, double o) {
        return this.resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, float o) {
        return this.resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasLiteral(Property p, Object o) {
        return this.resource.hasLiteral(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o) {
        return this.resource.hasProperty(p, o);
    }

    @Override
    public boolean hasProperty(Property p, String o, String l) {
        return this.resource.hasProperty(p, o, l);
    }

    @Override
    public boolean hasProperty(Property p, RDFNode o) {
        return this.resource.hasProperty(p, o);
    }

    @Override
    public Resource removeProperties() {
        return this.resource.removeProperties();
    }

    @Override
    public Resource removeAll(Property p) {
        return this.resource.removeAll(p);
    }

    @Override
    public Resource begin() {
        return this.resource.begin();
    }

    @Override
    public Resource abort() {
        return this.resource.abort();
    }

    @Override
    public Resource commit() {
        return this.resource.commit();
    }

    @Override
    public Resource getPropertyResourceValue(Property p) {
        return this.resource.getPropertyResourceValue(p);
    }

    @Override
    public boolean isAnon() {
        return this.resource.isAnon();
    }

    @Override
    public boolean isLiteral() {
        return this.resource.isLiteral();
    }

    @Override
    public boolean isURIResource() {
        return this.resource.isURIResource();
    }

    @Override
    public boolean isResource() {
        return this.resource.isResource();
    }

    @Override
    public <T extends RDFNode> T as(Class<T> view) {
        return this.resource.as(view);
    }

    @Override
    public <T extends RDFNode> boolean canAs(Class<T> view) {
        return this.resource.canAs(view);
    }

    @Override
    public Model getModel() {
        return this.resource.getModel();
    }

    @Override
    public Object visitWith(RDFVisitor rv) {
        return this.resource.visitWith(rv);
    }

    @Override
    public Resource asResource() {
        return this.resource.asResource();
    }

    @Override
    public Literal asLiteral() {
        return this.resource.asLiteral();
    }

    @Override
    public Node asNode() {
        return this.resource.asNode();
    }

    public String getLabel() {
        return SerializableResourceImpl.getLabel(this);
    }

    public static String getLabel(Resource resource) {
        // TODO ideally we use rdfs:label, but this property needs to be included in each DB query if we want to extract it
        // Moreover, hardly any entities are labeled with rdfs:label, and use semantics to extract the label (e.g. ga:hasName)
        // Although the latter could be solved in the mapping
        String label;
        if(resource.getModel() != null && resource.hasProperty(RDFS.label)) {
            label = resource.getProperty(RDFS.label).getString();
        } else if(!resource.getLocalName().isEmpty()) {
            label = resource.getLocalName();
        } else {
            String uri = resource.getURI();
            String nameSpace = resource.getNameSpace();
            if(nameSpace.equalsIgnoreCase(uri)) {
                label = uri.substring(uri.lastIndexOf("/") + 1);
            } else {
                label = uri.substring(nameSpace.length());
            }
        }
        return label;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeBoolean(this.resource.getModel() != null);
        out.writeObject(this.resource.getNameSpace());
        out.writeObject(this.resource.getLocalName());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        boolean hasModel = in.readBoolean();

        String namespace = (String) in.readObject();
        String localname = (String) in.readObject();

        ModelCom model = null;

        if (hasModel) {
            model = new ModelCom(Graph.emptyGraph);
            model.read(in, null, "RDF/XML");
        }

        this.resource = new ResourceImpl(namespace, localname, model);
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }
}
