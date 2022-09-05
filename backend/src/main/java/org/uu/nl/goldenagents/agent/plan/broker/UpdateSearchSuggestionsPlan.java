package org.uu.nl.goldenagents.agent.plan.broker;

import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.agent.plan.broker.suggestions.DbBasedSuggestSearchOptionsPlan;
import org.uu.nl.goldenagents.agent.plan.broker.suggestions.SimpleSuggestSearchOptionsPlan;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.MostGeneralQuery;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;

public class UpdateSearchSuggestionsPlan extends RunOncePlan {

    @Override
    public void executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        BrokerSearchSuggestionsContext context = planToAgentInterface.getContext(BrokerSearchSuggestionsContext.class);
        for(String conversationID : context.getSubscriptions()) {
            BrokerSearchSuggestionsContext.SearchSuggestionSubscription subscription = context.getSubscription(conversationID);
            AQLQuery lastQuery = subscription.getLastQuery();
            if (lastQuery != null) {
                BrokerSearchSuggestionsContext.SearchSuggestion suggestion = subscription.getSearchSuggestions(lastQuery);
                if (lastQuery.getQueryTree() instanceof MostGeneralQuery) {
                    planToAgentInterface.adoptPlan(new SimpleSuggestSearchOptionsPlan(
                            suggestion.getReceivedMessage(),
                            suggestion.getUserQueryTriggerMessageHeader(),
                            suggestion.getUserQueryTrigger())
                    );
                } else {
                    planToAgentInterface.adoptPlan(new DbBasedSuggestSearchOptionsPlan(
                            suggestion.getReceivedMessage(),
                            suggestion.getUserQueryTriggerMessageHeader(),
                            suggestion.getUserQueryTrigger())
                    );
                }
            }
        }

    }
}
