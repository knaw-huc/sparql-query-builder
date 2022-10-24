package org.uu.nl.goldenagents.netmodels.fipa;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class EntityList implements FIPASendableObject {
    private final HashSet<Entity> entities;

    private final int targetAQLQueryID;

    public EntityList(int targetAQLQueryID) {
        this.targetAQLQueryID = targetAQLQueryID;
        this.entities = new HashSet<>();
    }

    public List<Entity> getSublist(int size) {
        Iterator<Entity> entityIterator = this.entities.iterator();
        List<Entity> entitySubList = new ArrayList<>();
        while(entityIterator.hasNext() && size > 0) {
            entitySubList.add(entityIterator.next());
            size--;
        }
        return entitySubList;
    }

    public void addEntity(Entity entity) throws IllegalArgumentException {
        this.entities.add(entity);
    }

    public ArrayList<Entity> getEntities() {
        return new ArrayList<>(this.entities);
    }

    public int getTargetAQLQueryID() {
        return targetAQLQueryID;
    }

    public static EntityList fromACLMessage(ACLMessage receivedMessageWithEntityList) {
        try {
            GAMessageContentWrapper contentWrapper = (GAMessageContentWrapper) receivedMessageWithEntityList.getContentObject();
            FIPASendableObject content = contentWrapper.getContent();
            return (EntityList) content;
        } catch (UnreadableException e) {
            Platform.getLogger().log(UserQueryTrigger.class, e);
            return null;
        }
    }

    public static class Entity implements Serializable {
        private static transient final long serialVersionUID = -299482035708790407L;

        public static final String RDFDataTypeUriClass = XSDDatatype.XSDanyURI.getURI();

        private static transient TypeMapper _tm = null;

        private final String label;
        private final String value;
        private final String type;
        private final String language;

        public Entity(RDFNode node) throws IllegalArgumentException {
            if (node.canAs(Resource.class)) {
                Resource r = node.asResource();
                this.value = r.getURI();
                this.label = SerializableResourceImpl.getLabel(r);
                this.type = RDFDataTypeUriClass;
                this.language = null;
            } else if (node.canAs(Literal.class)) {
                Literal l = node.asLiteral();
                this.value = l.getString();
                this.label = this.value;
                this.type = l.getDatatypeURI();
                this.language = l.getLanguage();
            } else {
                throw new IllegalArgumentException("Can't convert node" + node + " to Individual.class");
            }
        }

        public Entity(AQLSuggestions.InstanceSuggestion entity) {
            this.label = entity.getLabel();
            this.value = entity.getTarget();
            this.type = entity.getDataTypeURI();
            this.language = ""; // TODO, is this going to be an issue?
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public RDFDatatype getRdfDataType() {
            return getTypeMapper().getTypeByName(this.type);
        }

        public String getLabel() {
            return label;
        }

        public String getLanguage() {
            return language;
        }

        public boolean isUri() {
            return RDFDataTypeUriClass.equals(this.type);
        }

        public Node getAsNode() {
            if (isUri()) {
                return NodeFactory.createURI(this.value);
            } else {
                return NodeFactory.createLiteral(this.value, this.language, getRdfDataType());
            }
        }

        private TypeMapper getTypeMapper() {
            if (Entity._tm == null) {
                Entity._tm = new TypeMapper();
                XSDDatatype.loadXSDSimpleTypes(Entity._tm);
            }
            return Entity._tm;
        }
    }
}
