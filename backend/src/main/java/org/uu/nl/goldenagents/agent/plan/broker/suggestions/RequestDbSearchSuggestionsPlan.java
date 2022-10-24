package org.uu.nl.goldenagents.agent.plan.broker.suggestions;

import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.netmodels.fipa.EntityList;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.SubGraph;
import org.uu.nl.goldenagents.sparql.CachedModel;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

public class RequestDbSearchSuggestionsPlan extends RunOncePlan {

    private static final Loggable logger = Platform.getLogger();

    private final CachedModel model;
    private final ACLMessage lastDbAgentSubgraphResponse;
    private PlanToAgentInterface planToAgentInterface;
    private BrokerSearchSuggestionsContext.SearchSuggestion suggestionsSubscription;

    public RequestDbSearchSuggestionsPlan(CachedModel model, ACLMessage lastDbAgentSubgraphResponse) {
        this.model = model;
        this.lastDbAgentSubgraphResponse = lastDbAgentSubgraphResponse;
    }

    @Override
    public void executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        this.planToAgentInterface = planToAgentInterface;
        if(!this.model.isSuggestionsExpected() || this.model.getUserQueryTrigger().getSparqlTranslation() == null) {
            logger.log(getClass(), Level.WARNING, "Started suggestion plan when no suggestions are expected or available");
            return;
        }

        logger.log(getClass(), "Starting to aggregate properties suggestions for search");
        this.suggestionsSubscription = getSearchSubscription();
        if (this.suggestionsSubscription == null) return;
        HashMap<AgentID, EntityList> relevantEntities = suggestionsSubscription.getEntitiesAtFocus(this.model);

        Platform.getLogger().log(getClass(),"Contacting agents: " + relevantEntities.keySet());
        for(AgentID aid : relevantEntities.keySet()) {
            if(relevantEntities.get(aid).getEntities().size() > 0) {
                this.model.expectSuggestionsFrom(aid);
                requestSuggestionsToAgent(aid, relevantEntities.get(aid));
            }
        }
    }

    private BrokerSearchSuggestionsContext.SearchSuggestion getSearchSubscription() {
        SubGraph subGraph = SubGraph.fromACLMessage(this.lastDbAgentSubgraphResponse);
        if (subGraph == null) {
            Platform.getLogger().log(getClass(), Level.SEVERE, "No UserQueryTrigger on original query request");
            return null;
        }
        Integer targetAqlQueryID = subGraph.getTargetAqlQueryID();
        if (targetAqlQueryID == null) {
            Platform.getLogger().log(getClass(), Level.SEVERE, "No AQL Query or SPARQL translation present. Are you sure this query requires suggestions?");
            return null;
        }

        BrokerSearchSuggestionsContext context = this.planToAgentInterface.getContext(BrokerSearchSuggestionsContext.class);
        BrokerSearchSuggestionsContext.SearchSuggestionSubscription sub = context.getSubscription(this.lastDbAgentSubgraphResponse.getConversationId());
        return sub.getSearchSuggestions(targetAqlQueryID);
    }

    private void requestSuggestionsToAgent(AgentID dbAgent, EntityList entities) {
        ACLMessage m = this.lastDbAgentSubgraphResponse.createForward(this.planToAgentInterface.getAgentID(), dbAgent);
        m.setPerformative(Performative.REQUEST);
        try {
            m.setContentObject(new GAMessageContentWrapper(GAMessageHeader.REQUEST_SUGGESTIONS, entities));
        } catch (IOException e) {
            logger.log(getClass(), e);
        }

        try {
            this.planToAgentInterface.getAgent().sendMessage(m);
            logger.log(getClass(), Level.INFO, String.format(
                    "Requested suggestions for %d entities from db agent %s",
                    entities.getEntities().size(), dbAgent.getShortLocalName()));
        } catch (PlatformNotFoundException | MessageReceiverNotFoundException e) {
            logger.log(getClass(), e);
        }
    }
}
