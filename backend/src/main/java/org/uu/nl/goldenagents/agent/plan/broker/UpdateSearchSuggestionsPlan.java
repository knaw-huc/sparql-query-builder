package org.uu.nl.goldenagents.agent.plan.broker;

import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.agent.plan.broker.suggestions.DbBasedSuggestSearchOptionsPlan;
import org.uu.nl.goldenagents.agent.plan.broker.suggestions.SimpleSuggestSearchOptionsPlan;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.MostGeneralQuery;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.HashMap;

public class UpdateSearchSuggestionsPlan extends RunOncePlan {

    @Override
    public void executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        BrokerSearchSuggestionsContext context = planToAgentInterface.getContext(BrokerSearchSuggestionsContext.class);
        Platform.getLogger().log(getClass(), String.format(
                "%d user agents are subscribed for suggestions. Updating",
                context.getSubscriptions().size()
        ));
        for(String conversationID : context.getSubscriptions()) {
            BrokerSearchSuggestionsContext.SearchSuggestionSubscription subscription = context.getSubscription(conversationID);
            updateBaseSuggestions(planToAgentInterface, subscription);

            // TODO If we can track what query is currently being processed, maybe we can update suggestions for that query,
            //      but it would not happen often, because it is only an issue at system startup
//            updateSuggestionsForLastQuery(planToAgentInterface, subscription);
        }
    }

    void updateBaseSuggestions(PlanToAgentInterface planToAgentInterface, BrokerSearchSuggestionsContext.SearchSuggestionSubscription subscription) {
        BrokerSearchSuggestionsContext.SearchSuggestion suggestion = subscription.getSearchSuggestions((new AQLQuery(new HashMap<>()).hashCode()));

        if (suggestion != null) {
            planToAgentInterface.adoptPlan(new SimpleSuggestSearchOptionsPlan(
                    suggestion.getReceivedMessage(),
                    suggestion.getUserQueryTriggerMessageHeader(),
                    suggestion.getUserQueryTrigger())
            );
            Platform.getLogger().log(getClass(), String.format(
                    "Planning to update base level suggestions for user agent %s",
                    suggestion.getReceivedMessage().getSender()
            ));
        }
    }

    void updateSuggestionsForLastQuery(PlanToAgentInterface planToAgentInterface, BrokerSearchSuggestionsContext.SearchSuggestionSubscription subscription) {
        BrokerSearchSuggestionsContext.SearchSuggestion suggestion = null;
        if (suggestion != null) {
            if (!(suggestion.getQuery().getQueryTree() instanceof MostGeneralQuery)) {
                planToAgentInterface.adoptPlan(new DbBasedSuggestSearchOptionsPlan(
                        suggestion.getReceivedMessage(),
                        suggestion.getUserQueryTriggerMessageHeader(),
                        suggestion.getUserQueryTrigger())
                );
            }
        }
    }
}
