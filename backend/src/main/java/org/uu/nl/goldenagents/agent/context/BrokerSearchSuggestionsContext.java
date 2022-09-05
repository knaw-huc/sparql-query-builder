package org.uu.nl.goldenagents.agent.context;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.SPARQLTranslation;
import org.uu.nl.goldenagents.decompose.expertise.ProvenanceTracer;
import org.uu.nl.goldenagents.netmodels.AqlDbTypeSuggestionWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.EntityList;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.goldenagents.sparql.CachedModel;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Context;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.*;
import java.util.logging.Level;

public class BrokerSearchSuggestionsContext implements Context {

    private final Map<String, SearchSuggestionSubscription> subscriptions = new HashMap<>();

    public void addSubscription(ACLMessage receivedMessage, GAMessageHeader header) {
        String conversationID = receivedMessage.getConversationId();
        if(!this.subscriptions.containsKey(receivedMessage.getConversationId())) {
            this.subscriptions.put(conversationID, new SearchSuggestionSubscription(receivedMessage));
        }
        SearchSuggestionSubscription subscription = getSubscription(conversationID);
        subscription.addQuery(receivedMessage, header);
    }

    public List<String> getSubscriptions() {
        String[] type = new String[0];
        return Arrays.asList(this.subscriptions.keySet().toArray(type));
    }

    public SearchSuggestionSubscription getSubscription(String conversationID) {
        return this.subscriptions.get(conversationID);
    }

    public void addDbSuggestionsForQuery(String conversationID, AQLQuery query, AgentID dbAID, AqlDbTypeSuggestionWrapper suggestions) {
        this.getSubscription(conversationID).getSearchSuggestions(query).addDbSuggestionsForQuery(dbAID, suggestions);
    }

    public Map<AgentID, AqlDbTypeSuggestionWrapper> getDbSuggestionsForQuery(String conversationID, AQLQuery query) {
        return this.subscriptions.get(conversationID).getSearchSuggestions(query).getDbSuggestionsForQuery();
    }

    public static class SearchSuggestionSubscription {

        private final AgentID userAgentID;
        private final String conversationID;
        private List<AQLQuery> queryHistory = new ArrayList<>();
        private final Map<AQLQuery, SearchSuggestion> suggestionHistory = new HashMap<>();
        CachedModel model;

        public SearchSuggestionSubscription(ACLMessage receivedMessage) {
            this.userAgentID = receivedMessage.getSender();
            this.conversationID = receivedMessage.getConversationId();
        }

        public void addQuery(ACLMessage receivedMessage, GAMessageHeader header) {
            UserQueryTrigger queryRequest = UserQueryTrigger.fromACLMessage(receivedMessage);
            if(!suggestionHistory.containsKey(queryRequest.getAql())) {
                this.suggestionHistory.put(
                        queryRequest.getAql(),
                        new SearchSuggestion(
                            this,
                            receivedMessage,
                            queryRequest,
                            header
                        )
                );
                queryHistory.add(queryRequest.getAql());
            }
        }

        public List<AQLQuery> getQueryHistory() {
            return new ArrayList<>(this.queryHistory);
        }

        public AQLQuery getLastQuery() {
            if (!this.queryHistory.isEmpty()) {
                return this.queryHistory.get(this.queryHistory.size() - 1);
            } else {
                return null;
            }
        }

        public SearchSuggestion getSearchSuggestions(AQLQuery query) {
            return this.suggestionHistory.get(query);
        }

        public void setModel(CachedModel model) {
            this.model = model;
        }

        public CachedModel getModel() {
            return model;
        }

        public AgentID getUserAgentID() {
            return userAgentID;
        }

        public String getConversationID() {
            return conversationID;
        }
    }

    public static class SearchSuggestion {
        SearchSuggestionSubscription subscription;
        ACLMessage receivedMessage;
        UserQueryTrigger userQueryTrigger;
        GAMessageHeader userQueryTriggerMessageHeader;
        private List<AgentID> contactedAgents;
        private ArrayList<Resource> focusEntities;
        private EntityList<String> serializableFocusEntities;
        private final Map<AgentID, AqlDbTypeSuggestionWrapper> aqlSuggestionMap = new HashMap<>();

        public SearchSuggestion(
                SearchSuggestionSubscription subscription,
                ACLMessage receivedMessage,
                UserQueryTrigger userQueryTrigger,
                GAMessageHeader userQueryTriggerMessageHeader
        ) {
            this.subscription = subscription;
            this.receivedMessage = receivedMessage;
            this.userQueryTrigger = userQueryTrigger;
            this.userQueryTriggerMessageHeader = userQueryTriggerMessageHeader;
            this.contactedAgents = new ArrayList<>();
        }

        public ACLMessage getReceivedMessage() {
            return receivedMessage;
        }

        public UserQueryTrigger getUserQueryTrigger() {
            return userQueryTrigger;
        }

        public GAMessageHeader getUserQueryTriggerMessageHeader() {
            return userQueryTriggerMessageHeader;
        }

        public AQLQuery getQuery() {
            return this.userQueryTrigger.getAql();
        }

        public ArrayList<Resource> getEntitiesAtFocus() {
            ensureEntitiesExtracted();
            return this.focusEntities;
        }
        public EntityList<String> getSerializableFocusEntities() {
            ensureEntitiesExtracted();
            return this.serializableFocusEntities;
        }

        private void ensureEntitiesExtracted() {
            if(this.serializableFocusEntities == null || this.focusEntities == null) {
                getEntitiesAtFocus(subscription.model);
            }
        }

        void addDbSuggestionsForQuery(AgentID dbAID, AqlDbTypeSuggestionWrapper suggestions) {
            this.aqlSuggestionMap.put(dbAID, suggestions);
        }

        Map<AgentID, AqlDbTypeSuggestionWrapper> getDbSuggestionsForQuery() {
            return this.aqlSuggestionMap;
        }

        /**
         * Add hoc and slow method to find entities that actually occur in participating sources.
         * Probably a more general approach is required that makes clever use of SPARQL SERVICE keyword, to query over
         * all the partial models. This approach could potentially also be used for detailed provenance information, but
         * it requires some extra thinking on my part for which the capacity is currently missing
         *
         * @return Map of entities per DB agent that participated in the query. Entities can occur for multiple DB
         * agents.
         */
        public HashMap<AgentID, EntityList<String>> getEntitiesAtFocus(CachedModel model) {
            SPARQLTranslation sparqlTranslation = this.userQueryTrigger.getAql().getSparqlAlgebra();
            this.focusEntities = new ArrayList<>();
            this.serializableFocusEntities = new EntityList<>(this.userQueryTrigger.getAql().getFocusName());

            // Find participating agents from provenance tracer
            ProvenanceTracer t = model.getProvenanceTracer();
            Map<String, Set<AgentID>> mappedSources = t.getMappedSources();

            // TODO, check if question mark is included/excluded properly
            String focusVarName = "?" + sparqlTranslation.getFocusVar().getVarName();
            Set<AgentID> relevantAgents = mappedSources.get(focusVarName);

            if(relevantAgents == null)  {
                Platform.getLogger().log(getClass(), Level.SEVERE,"No relevant agents found. This has to be an error");
                relevantAgents = new HashSet<>();
            }

            // Create aggregator for results
            HashMap<AgentID, EntityList<String>> relevantEntities = new HashMap<>();
            relevantAgents.iterator().forEachRemaining(x -> relevantEntities.put(x, new EntityList<>(this.userQueryTrigger.getAql().getFocusName())));

            // Start query process
            Query q = sparqlTranslation.getQuery();
            q.setDistinct(true);
            try(QueryExecution exec = QueryExecutionFactory.create(q, model.getModel())) {
                ResultSet r = exec.execSelect();
                while(r.hasNext()) {
                    RDFNode n = r.next().get(sparqlTranslation.getFocusVar().getVarName()); // TODO, check if question mark is included/excluded properly
                    if(n.canAs(ResourceImpl.class)) {
                        this.focusEntities.add(n.as(ResourceImpl.class));
                        this.serializableFocusEntities.addEntity(n.asResource().getURI());
                    } else if (n.canAs(Literal.class)) {
                        Platform.getLogger().log(getClass(), Level.WARNING, "Can't convert node " + n + " to Individual.class but adding as Literal");
                        this.serializableFocusEntities.addEntity(n.asLiteral().getString());
                    } else {
                        Platform.getLogger().log(getClass(), Level.WARNING, "Can't convert node " + n + " to Individual.class");
                    }
                    for(AgentID aid : relevantAgents) {
                        if(model.getPartialAgentModel(aid).containsResource(n)) {
                            try {
                                if (n.isResource() && n.canAs(ResourceImpl.class)) {
                                    relevantEntities.get(aid).addEntity(n.asResource().getURI());
                                } else if (n.isLiteral() && n.canAs(Literal.class)) {
                                    relevantEntities.get(aid).addEntity(n.asLiteral().getString());
                                }
                            } catch (Exception e) {
                                Platform.getLogger().log(getClass(), e);
                            }
                        }
                    }
                }
            }

            return relevantEntities;
        }
    }

    public static class ContactedAgent {
        AgentID agentID;
        String conversationID;
        boolean isInitial;
        boolean isDone;
    }
}
