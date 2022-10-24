package org.uu.nl.goldenagents.agent.plan.broker.suggestions;

import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.netmodels.AqlDbTypeSuggestionWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;

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

        BrokerSearchSuggestionsContext.SearchSuggestionSubscription sub = suggestionsContext.getSubscription(message.getConversationId());
        BrokerSearchSuggestionsContext.SearchSuggestion cachedSearchSuggestions =
                sub.getSearchSuggestions(suggestions.getTargetAqlQueryId());

        cachedSearchSuggestions.getModel().setAgentSuggestionsReceived(message.getSender(), true);

        AQLQuery query = cachedSearchSuggestions.getQuery();
        suggestionsContext.addDbSuggestionsForQuery(message.getConversationId(), query, receivedMessage.getSender(), suggestions);
        BrokerSearchSuggestionsContext.SearchSuggestion suggestion = sub.getSearchSuggestions(query);
        try {
            planInterface.adoptPlan(
                    new DbBasedSuggestSearchOptionsPlan(
                            suggestion.getReceivedMessage(),
                            suggestion.getUserQueryTriggerMessageHeader(),
                            suggestion.getReceivedMessage().getContentObject()
                    )
            );
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }

    private void contactOtherAgents() {
        /*
        Get entity list.
        Merge all entities over all agents from list
        for each dbAgent in dbAgentExpertises:
            Clone merged list
            Remove all entities from entityList for dbAgent
            For remaining, find sameAs entities
            Send those entities to DB agent using:
                - m.setPerformative(Performative.REQUEST);
                - m.setContentObject(new GAMessageContentWrapper(GAMessageHeader.REQUEST_SUGGESTIONS, entities));
         */
    }
}
