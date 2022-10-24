package org.uu.nl.goldenagents.agent.context.query;

import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.hibernate.validator.internal.xml.binding.PropertyType;
import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.goldenagents.sparql.MappingPropertyType;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Context;
import org.uu.nl.net2apl.core.platform.Platform;

import javax.annotation.RegEx;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class stores the mapping model in a way that allows translating concepts quickly
 */
public class DbTranslationContext implements Context {

    private Model mappingModel;
    private HashMap<String, List<Translation>> localToGlobalMapping = new HashMap<>();
    private HashMap<String, List<Translation>> globalToLocalMapping = new HashMap<>();
    private final PrefixNSListenerContext prefixContext;

    public DbTranslationContext(Model mappingModel, PrefixNSListenerContext prefixContext) {
        this.mappingModel = mappingModel;
        this.prefixContext = prefixContext;
        StmtIterator it = mappingModel.listStatements();
        while(it.hasNext()) {
            processStatement(it.nextStatement());
        }
    }

    public void processStatement(Statement stmt) {
        MappingPropertyType mappingPropertyType = MappingPropertyType.fromNode(stmt.getPredicate().asNode());
        if (mappingPropertyType == null) {
            Platform.getLogger().log(getClass(), Level.INFO, String.format(
                    "Skipping property %s mapping %s to %s",
                    stmt.getPredicate(),
                    stmt.getSubject(),
                    stmt.getObject()
            ));
        }
        try {
            for(Translation translation : Translation.fromStatement(stmt, this.prefixContext, this.mappingModel)) {
                List<Translation> local = this.localToGlobalMapping.get(translation.localConceptShortform);
                if (local == null) local = new ArrayList<>();
                local.add(translation);
                this.localToGlobalMapping.put(translation.localConceptShortform, local);

                List<Translation> global = this.globalToLocalMapping.get(translation.globalConceptShortform);
                if (global == null) global = new ArrayList<>();
                global.add(translation);
                this.globalToLocalMapping.put(translation.globalConceptShortform, global);
            }
        } catch (ResourceRequiredException e) {
            Platform.getLogger().log(getClass(), "Did not understand " + stmt);
        }
    }

    public PrefixNSListenerContext getPrefixContext() {
        return this.prefixContext;
    }

    public boolean addMappedConceptsToPrefixListenerMap(AgentID aid) {
        Map<String, String> prefixMapping = prefixContext.getPrefixMap();

        // First, we add all prefixes from translation statements
        for(String conceptKey : localToGlobalMapping.keySet()) {
            List<Translation> translations = localToGlobalMapping.get(conceptKey);
            for(Translation translation : translations) {
                String uri = translation.getLocalConcept().getNameSpace();
                String prefix = mappingModel.getNsURIPrefix(uri);
                if (prefix == null || prefix.startsWith("ns")) {
                    String _prefix = extractPrefix(uri);
                    if (_prefix != null) {
                        prefix = _prefix;
                    }
                }
                if (prefix != null && !prefixMapping.containsKey(prefix)) {
                    prefixMapping.put(prefix, uri);
                } else if (!uri.equals(prefixMapping.get(prefix))) {
                    Platform.getLogger().log(getClass(), Level.SEVERE, String.format(
                            "Prefix %s is already set to %s but used here for %s",
                            prefix,
                            prefixMapping.get(prefix),
                            uri
                    ));
                }
            }
        }

        // Next, we try to replace all generic nsX prefixes with something derived from the URL
        for(String prefix : this.mappingModel.getNsPrefixMap().keySet()) {
            if (prefix.startsWith("ns")) {
                String uri = this.mappingModel.getNsPrefixURI(prefix);
                if(!prefixMapping.containsValue(uri)) {
                    String _prefix = extractPrefix(uri);
                    if (!prefixMapping.containsKey(_prefix)) {
                        prefixMapping.put(_prefix, uri);
                        this.mappingModel.removeNsPrefix(prefix);
                    }
                }
            }
        }

        // Update both prefix context and model
        boolean changed = prefixContext.setPrefixMap(aid, prefixMapping);
        this.mappingModel.setNsPrefixes(prefixContext.getPrefixMap());
        return changed;
    }

    private String extractPrefix(String uri) {
        Matcher matcher = Pattern.compile(".*/([\\w\\d\\-_]+)[/#]?$").matcher(uri);
        if (matcher.find()) {
            String _prefix = matcher.group(1);
            if (_prefix.length() > 4) {
                _prefix = _prefix.substring(0, 4);
            }
            return _prefix;
        }
        return null;
    }

    /**
     * Translate a concept as used in the local data source to its equivalent concept in the General Ontology
     * @param resource  Resource from local model to translate to General Ontology
     * @return          Equivalent resource from the General Ontology
     */
    public List<Resource> translateLocalToGlobal(@NotNull Resource resource) {
        return translateLocalToGlobal(resource.getURI());
    }

    public List<Resource> translateLocalToGlobal(@NotNull String uri) {
        List<Resource> resources = new ArrayList<>();
        List<Translation> translations = this.getGlobalToLocalTranslation(uri);
        if (translations != null) {
            for(Translation translation : translations) {
                resources.add(translation.getGlobalConcept());
            }
        } else {
            Platform.getLogger().log(getClass(), Level.FINE,"No translation found for local concept " + uri);
            return null;
        }
        return resources;
    }

    public List<Translation> getLocalToGlobalTranslation(Resource resource) {
        return this.localToGlobalMapping.get(shortForm(resource));
    }

    public List<Translation> getLocalToGlobalTranslation(String uri) {
        return this.localToGlobalMapping.get(shortForm(uri));
    }

    /**
     * Translate a concept as used in the General Ontology to its equivalent concept in the local data source
     * @param resource  Resource from General Ontology to translate to local model
     * @return          Equivalent resource from the local model
     */
    public List<Resource> translateGlobalToLocal(Resource resource) {
        return translateGlobalToLocal(resource.getURI());
    }

    public List<Resource> translateGlobalToLocal(String uri) {
        List<Resource> resources = new ArrayList<>();
        List<Translation> translations = getGlobalToLocalTranslation(uri);
        if(translations != null) {
            for(Translation translation : translations) {
                resources.add(translation.getLocalConcept());
            }
        } else {
            Platform.getLogger().log(getClass(), Level.FINE, "No translation found for global concept " + uri);
            return null;
        }
        return resources;
    }

    public List<Translation> getGlobalToLocalTranslation(Resource resource) {
        return this.globalToLocalMapping.get(shortForm(resource));
    }

    public List<Translation> getGlobalToLocalTranslation(String uri) {
        String shortForm = shortForm(uri);
        return this.globalToLocalMapping.get(shortForm);
    }

    /**
     * Get the short form of a resource URL
     * @param resource  Resource
     * @return          Short form (i.e. prefixed local name)
     */
    public String shortForm(Resource resource) {
        return this.mappingModel.shortForm(resource.getURI());
    }

    public String shortForm(String uri) {
        return this.mappingModel.shortForm(uri);
    }

    /**
     * This class contains tuples of concepts, one from the local data source, the other from the global ontology,
     * that share an equivalence-type relation
     */
    public static class Translation {
        private final Resource localConcept;
        private final Resource globalConcept;
        private final String localConceptShortform;
        private final String globalConceptShortform;
        private final Property originalEquivalenceRelation;
        private final MappingPropertyType mappingPropertyType;
        private final PrefixNSListenerContext prefixContext;
        private final boolean inverse;

        private Translation(Resource globalConcept, Resource localConcept, Property predicate, PrefixNSListenerContext prefixContext, Model mappingModel) {
            this.prefixContext = prefixContext;
            this.originalEquivalenceRelation = predicate;
            this.mappingPropertyType = MappingPropertyType.fromProperty(predicate);
            this.inverse = this.mappingPropertyType.isInverseProperty();
            this.localConcept = localConcept;
            this.globalConcept = globalConcept;
            this.localConceptShortform = mappingModel.shortForm(localConcept.getURI());
            this.globalConceptShortform = mappingModel.shortForm(globalConcept.getURI());
        }

        public static List<Translation> fromStatement(Statement statement, PrefixNSListenerContext prefixContext, Model mappingModel) {
            Resource subject = statement.getSubject();
            Resource object = statement.getObject().asResource();

            ArrayList<Translation> translations = new ArrayList<>();
            boolean subjectIsGlobal = prefixContext.getNamespaces().contains(subject.getNameSpace());
            boolean objectIsGlobal = prefixContext.getNamespaces().contains(object.getNameSpace());

            if (MappingPropertyType.fromProperty(statement.getPredicate()) != null) {
                if (subjectIsGlobal && !objectIsGlobal) {
                    translations.add(new Translation(subject, object, statement.getPredicate(), prefixContext, mappingModel));
                } else if (objectIsGlobal && !subjectIsGlobal) {
                    translations.add(new Translation(object, subject, statement.getPredicate(), prefixContext, mappingModel));
                }
            }

            return translations;
        }

        /**
         * Get the concept used in the local data source
         * @return  Resource
         */
        public Resource getLocalConcept() {
            return localConcept;
        }

        /**
         * Get the concept used in the global ontology
         * @return Resource
         */
        public Resource getGlobalConcept() {
            return globalConcept;
        }

        /**
         * Are these properties the inverse of each other?
         * @return  True if global and local property are each others inverses
         */
        public boolean isInverse() {
            return inverse;
        }

        public boolean isClassEquivalence() {
            return localConcept instanceof OntClass && globalConcept instanceof OntClass;
        }

        public boolean isPropertyEquivalence() {
            return localConcept instanceof OntProperty && globalConcept instanceof OntProperty;
        }

        public Property getOriginalEquivalenceRelation() {
            return originalEquivalenceRelation;
        }

        /**
         * Test of a concept belongs to the General Ontology at use
         * @param node  Node to verify
         * @return      True iff Node is concept from the General Ontology at use
         */
        private boolean isGeneralConcept(Node node) {
            Set<String> namespaces = this.prefixContext.getNamespaces();
            return namespaces.contains(node.getNameSpace());
        }

        public String getLocalConceptShortform() {
            return this.getLocalConceptShortform();
        }

        public String getGlobalConceptShortform() {
            return this.globalConceptShortform;
        }

        public MappingPropertyType getMappingPropertyType() {
            return mappingPropertyType;
        }
    }

}
