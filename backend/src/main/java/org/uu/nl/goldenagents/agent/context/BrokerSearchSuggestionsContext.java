package org.uu.nl.goldenagents.agent.context;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.SPARQLTranslation;
import org.uu.nl.goldenagents.decompose.expertise.ProvenanceTracer;
import org.uu.nl.goldenagents.netmodels.AqlDbTypeSuggestionWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.EntityList;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.goldenagents.sparql.CachedModel;
import org.uu.nl.goldenagents.sparql.QueryInfo;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Context;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.*;
import java.util.logging.Level;

/**
 * This class keeps track of all conversation IDs containing AQL queries.
 * A conversationID should be re-used of the AQL query is derived from another.
 * For a completely new query process, a new conversation ID should be used, in which case
 * the AQL query and its derivatives are tracked separately.
 */
public class BrokerSearchSuggestionsContext implements Context {

    private final Map<String, SearchSuggestionSubscription> subscriptions = new HashMap<>();

    public void addSubscription(ACLMessage receivedMessage, GAMessageHeader header, CachedModel model, QueryInfo queryInfo) {
        String conversationID = receivedMessage.getConversationId();
        if(!this.subscriptions.containsKey(receivedMessage.getConversationId())) {
            this.subscriptions.put(conversationID, new SearchSuggestionSubscription(receivedMessage, model));
        }
        SearchSuggestionSubscription subscription = getSubscription(conversationID);
        subscription.addQuery(receivedMessage, header, queryInfo);
    }

    public List<String> getSubscriptions() {
        return Arrays.asList(this.subscriptions.keySet().toArray(new String[0]));
    }

    public SearchSuggestionSubscription getSubscription(String conversationID) {
        return this.subscriptions.get(conversationID);
    }

    public void addDbSuggestionsForQuery(String conversationID, AQLQuery query, AgentID dbAID, AqlDbTypeSuggestionWrapper suggestions) {
        this.getSubscription(conversationID).getSearchSuggestions(query).addDbSuggestionsForQuery(dbAID, suggestions);
    }

    public Map<AgentID, AqlDbTypeSuggestionWrapper> getDbSuggestionsForQuery(String conversationID, AQLQuery query) {
        return this
                .subscriptions
                .get(conversationID)
                .getSearchSuggestions(query)
                .getDbSuggestionsForQuery();
    }

    /**
     * This class stores all AQL queries that are related to each other, and that share
     * a common CachedModel that allows reusing results.
     * These AQL queries should be shared with the broker using the same conversation ID
     */
    public static class SearchSuggestionSubscription {

        private final AgentID userAgentID;
        private final String conversationID;
        private Map<Integer, AQLQuery> queryHistory = new HashMap<>();
        private Set<Integer> executedQueries = new HashSet<>();
        private final Map<Integer, SearchSuggestion> suggestionHistory = new HashMap<>();
        final CachedModel model;

        public SearchSuggestionSubscription(ACLMessage receivedMessage, CachedModel model) {
            this.userAgentID = receivedMessage.getSender();
            this.conversationID = receivedMessage.getConversationId();
            this.model = model;
        }

        public void addQuery(ACLMessage receivedMessage, GAMessageHeader header, QueryInfo queryInfo) {
            UserQueryTrigger queryRequest = UserQueryTrigger.fromACLMessage(receivedMessage);
            if(
                    queryRequest != null &&
                    queryRequest.getAql() != null &&
                    !suggestionHistory.containsKey(queryRequest.getAql().hashCode())
            ) {
                this.suggestionHistory.put(
                        queryRequest.getAql().hashCode(),
                        new SearchSuggestion(
                            this,
                            receivedMessage,
                            queryInfo,
                            queryRequest,
                            header
                        )
                );
                this.executedQueries.add(queryRequest.getAql().getQueryTree().hashCode());
                queryHistory.put(queryRequest.getAql().hashCode(), queryRequest.getAql());
            }
        }

        public boolean hasResultForQuery(AQLQuery query) {
            return this.executedQueries.contains(query.getQueryTree().hashCode());
        }

        public SearchSuggestion getSearchSuggestions(int queryID) {
            return this.suggestionHistory.get(queryID);
        }

        public SearchSuggestion getSearchSuggestions(AQLQuery query) {
            return getSearchSuggestions(query.hashCode());
        }

        public AgentID getUserAgentID() {
            return userAgentID;
        }

        public String getConversationID() {
            return conversationID;
        }
    }

    /**
     * This class keeps track of a specific AQL query and stores all relevant data
     */
    public static class SearchSuggestion {
        SearchSuggestionSubscription subscription;
        ACLMessage receivedMessage;
        UserQueryTrigger userQueryTrigger;
        GAMessageHeader userQueryTriggerMessageHeader;
        CachedModel model;
        QueryInfo queryInfo;
        private List<AgentID> contactedAgents;
        private EntityList serializableFocusEntities;
        private final Map<AgentID, AqlDbTypeSuggestionWrapper> aqlSuggestionMap = new HashMap<>();

        public SearchSuggestion(
                SearchSuggestionSubscription subscription,
                ACLMessage receivedMessage,
                QueryInfo queryInfo,
                UserQueryTrigger userQueryTrigger,
                GAMessageHeader userQueryTriggerMessageHeader
        ) {
            this.subscription = subscription;
            this.receivedMessage = receivedMessage;
            this.userQueryTrigger = userQueryTrigger;
            this.userQueryTriggerMessageHeader = userQueryTriggerMessageHeader;
            this.contactedAgents = new ArrayList<>();
            this.model = CachedModel.reuseFrom(
                    subscription.model,
                    queryInfo,
                    userQueryTrigger
            );
            this.model.setSuggestionsExpected(true);
            this.queryInfo = queryInfo;
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

        public EntityList getSerializableFocusEntities() {
            ensureEntitiesExtracted();
            return this.serializableFocusEntities;
        }

        public CachedModel getModel() {
            return model;
        }

        private void ensureEntitiesExtracted() {
            if(this.serializableFocusEntities == null) {
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
        public HashMap<AgentID, EntityList> getEntitiesAtFocus(CachedModel model) {
            SPARQLTranslation sparqlTranslation = this.userQueryTrigger.getAql().getSparqlAlgebra();
            this.serializableFocusEntities = new EntityList(this.userQueryTrigger.getAql().hashCode());

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
            HashMap<AgentID, EntityList> relevantEntities = new HashMap<>();
            relevantAgents.iterator().forEachRemaining(x -> relevantEntities.put(x, new EntityList(this.userQueryTrigger.getAql().hashCode())));

            // Start query process
            Query q = sparqlTranslation.getQuery();
            q.setDistinct(true);
            q.addProjectVars(Collections.singleton(focusVarName));
            try(QueryExecution exec = QueryExecutionFactory.create(q, model.getModel())) {
                ResultSet r = exec.execSelect();
                while(r.hasNext()) {
                    RDFNode n = r.next().get(focusVarName);
                    try {
                        EntityList.Entity entity = new EntityList.Entity(n);
                        this.serializableFocusEntities.addEntity(entity);
                        for (AgentID aid : relevantAgents) {
                            if (model.getPartialAgentModel(aid).containsResource(n)) {
                                relevantEntities.get(aid).addEntity(entity);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        Platform.getLogger().log(getClass(), e);
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
