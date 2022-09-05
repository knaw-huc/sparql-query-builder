package org.uu.nl.goldenagents.agent.plan.broker.suggestions;

import org.uu.nl.goldenagents.agent.context.BrokerContext;
import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.netmodels.AqlDbTypeSuggestionWrapper;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLQueryJsonObject;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.sparql.CachedModel;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ReceiveDBSuggestionsPlan extends MessagePlan {

    public ReceiveDBSuggestionsPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    /**
     * This method is executed when a message has been received
     *
     * @param planInterface   An interface to the agent in order to access context, etc
     * @param receivedMessage The message that triggered this plan
     * @param header          The header of the message
     * @param content         The content of the message
     * @throws PlanExecutionError Exception thrown when executing this plan goes totally awry
     */
    @Override
    public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
        AqlDbTypeSuggestionWrapper suggestions;
        try {
            suggestions = (AqlDbTypeSuggestionWrapper) ((GAMessageContentWrapper) message.getContentObject()).getContent();
        } catch (UnreadableException e) {
            logger.log(getClass(), "Unable to parse AQL DB Type Suggestion Wrapper object. Plan failed");
            logger.log(getClass(), e);
            throw new PlanExecutionError();
        }

        BrokerSearchSuggestionsContext suggestionsContext = planInterface.getContext(BrokerSearchSuggestionsContext.class);

        // TODO is this even correct for storing the data? Is AQL ID same as QueryID? Should maybe use query trigger for this
        // TODO, propegate query we are talking about, because this is going nowhere

        BrokerSearchSuggestionsContext.SearchSuggestionSubscription sub = suggestionsContext.getSubscription(message.getConversationId());
        AQLQuery query = sub.getLastQuery();
        suggestionsContext.addDbSuggestionsForQuery(message.getConversationId(), query, planInterface.getAgentID(), suggestions);
        BrokerSearchSuggestionsContext.SearchSuggestion suggestion = sub.getSearchSuggestions(query);
        try {
            planInterface.adoptPlan(new DbBasedSuggestSearchOptionsPlan(suggestion.getReceivedMessage(), suggestion.getUserQueryTriggerMessageHeader(), suggestion.getReceivedMessage().getContentObject()));
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

//        AQLQuery query = context.getCachedModel(message.getConversationId()).getUserQueryTrigger().getAql();
//        suggestionsContext.addDbSuggestionsForQuery(message.getConversationId(), query, message.getSender(), suggestions);
//        CachedModel model = context.getCachedModel(receivedMessage.getConversationId());
//        model.setAgentSuggestionsReceived(message.getSender(), true);
//
//        if(model.querySuggestionsDone()) {
//            logger.log(getClass(), String.format(
//                    "Finished receiving %d suggestions, starting plan to aggregate and inform user",
//                    model.getNumExpectedSuggestionAgents()));
//
//            AQLTree t = context.getCachedModel(receivedMessage.getConversationId()).getUserQueryTrigger().getAql().getQueryTree();
//            this.message.addReplyTo(context.getConversationUser(receivedMessage.getConversationId()));
//            planInterface.adoptPlan(new DbBasedSuggestSearchOptionsPlan(receivedMessage, header, t));
//        } else {
//            logger.log(getClass(), Level.SEVERE, String.format("Received suggestions from %d data source agents, expecting %d in total",
//                    suggestionsContext.getDbSuggestionsForQuery(message.getConversationId(), query).size(),
//                    model.getNumExpectedSuggestionAgents()));
//        }
    }

    private void contactOtherAgents(List<AgentID> queriedAgents, BrokerContext context) {
        // TODO, after all participating data sources have finished responding, send all entities to other data sources
        List<AgentID> notContacted = getUncontactedAgents(queriedAgents, context);
        for(AgentID dbAgent : notContacted) {
//            contactAgent(dbAgent);
        }
    }

    private void contactAgent(CachedModel model, AgentID agentID) {

    }

    private List<AgentID> getUncontactedAgents(List<AgentID> queriedAgents, BrokerContext context) {
        List<AgentID> uncontactedAgents = new ArrayList<>();
        for(AgentID dbAgent : context.getDbAgentExpertises().keySet()) {
            if(!queriedAgents.contains(dbAgent)) {
                uncontactedAgents.add(dbAgent);
            }
        }
        return uncontactedAgents;
    }

}
