package org.uu.nl.goldenagents.netmodels.angular;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.uu.nl.goldenagents.netmodels.fipa.EntityList;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AQLSuggestions implements Serializable, FIPASendableObject {

    private List<TypeSuggestion> classList;
    private List<TypeSuggestion> propertyList;
    private List<InstanceSuggestion> instanceList;
    private int targetAqlQueryId;

    /**
     * Default constructor for AQL Query Suggestions
     * @param classList     List of classes to suggest
     * @param propertyList  List of properties to suggest
     * @param instanceList  List of instances to suggest
     * @param queryHashCode Hashcode of query to which suggestions apply
     */
    public AQLSuggestions(List<TypeSuggestion> classList, List<TypeSuggestion> propertyList, List<InstanceSuggestion> instanceList, int queryHashCode) {
        this.targetAqlQueryId = queryHashCode;
        this.classList = classList;
        this.propertyList = propertyList;
        this.instanceList = instanceList;
    }

    public List<TypeSuggestion> getClassList() {
        return classList;
    }

    public void setClassList(List<TypeSuggestion> classList) {
        this.classList = classList;
    }

    public List<TypeSuggestion> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<TypeSuggestion> propertyList) {
        this.propertyList = propertyList;
    }

    public List<InstanceSuggestion> getInstanceList() {
        return instanceList;
    }

    public void setInstanceList(List<InstanceSuggestion> instanceList) {
        this.instanceList = instanceList;
    }

    public int getTargetAqlQueryId() {
        return targetAqlQueryId;
    }

    public void setTargetAqlQueryId(int targetAqlQueryId) {
        this.targetAqlQueryId = targetAqlQueryId;
    }

    public static class TypeSuggestion implements Serializable {
        private boolean forwardjump;
        private String url;
        private String type;
        private String id;
        private String label;
        private List<TypeSuggestion> subitems = new ArrayList<>();

        public TypeSuggestion(OntClass ontClass, Set<OntClass> allowedClasses) {
            populateFromResource(ontClass);
            this.forwardjump = false;
            this.type = "class";
            addSubitems(ontClass, allowedClasses);
        }

        private void addSubitems(OntClass currentItem, Set<OntClass> allowedClasses) {
            currentItem.listSubClasses(true).forEachRemaining(subclass -> {
                if(allowedClasses.isEmpty() || allowedClasses.contains(subclass))
                    this.subitems.add(new TypeSuggestion(subclass, allowedClasses));
                else
                    addSubitems(subclass, allowedClasses);
            });
        }

        private void addSubitems(OntProperty currentItem, boolean forwards, Set<OntProperty> allowedProperties) {
            currentItem.listSubProperties(true).forEachRemaining(subProperty -> {
                if(allowedProperties.isEmpty() || allowedProperties.contains(subProperty))
                    this.subitems.add(new TypeSuggestion(subProperty.asProperty(), forwards, allowedProperties));
                else
                    addSubitems(subProperty.asProperty(), forwards, allowedProperties);
            });
        }

        /**
         * Construct a new Type Suggestion from an OntProperty
         * @param ontProperty           OntProperty that can be suggested
         * @param forwardjump              Boolean indicating if the property is forward crossing (p of q) or backward
         *                              crossing (p : q)
         * @param allowedProperties     A set of properties occurring in the ontology (i.e. allowed properties that
         *                              the system can handle
         */
        public TypeSuggestion(OntProperty ontProperty, boolean forwardjump, Set<OntProperty> allowedProperties) {
            populateFromResource(ontProperty);
            this.forwardjump = forwardjump;
            this.type = "prop";

            addSubitems(ontProperty, forwardjump, allowedProperties);
        }

        private void populateFromResource(OntResource resource) {
            this.url = resource.getURI();
            this.label = resource.getLabel("en");
            this.label = this.label == null ? resource.getLocalName() : this.label;
        }

        public boolean isForwardjump() {
            return forwardjump;
        }

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public List<TypeSuggestion> getSubitems() {
            return subitems;
        }
    }

    public static class InstanceSuggestion implements Serializable {
        private String type = "instance";
        private String dataTypeURI;
        private String label;
        private String target;
        private String title;

        // Empty constructor required for deserialization
        public InstanceSuggestion() { }

        public InstanceSuggestion(EntityList.Entity individual) {
            this.dataTypeURI = individual.getType();
            this.target = individual.getValue();
            this.label = individual.getLabel();
            this.title = this.target;
        }

        public String getLabel() {
            return label;
        }

        public String getTarget() {
            return target;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public String getDataTypeURI() {
            return dataTypeURI;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDataTypeURI(String dataTypeURI) {
            this.dataTypeURI = dataTypeURI;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
