package org.uu.nl.goldenagents.agent.plan.user;

import org.uu.nl.goldenagents.agent.context.DirectSsePublisher;
import org.uu.nl.goldenagents.agent.context.query.AQLQueryContext;
import org.uu.nl.goldenagents.agent.context.query.QueryResultContext;
import org.uu.nl.goldenagents.agent.context.registration.DFRegistrationContext;
import org.uu.nl.goldenagents.agent.trigger.user.AQLQueryChangedExternalTrigger;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.MostGeneralQuery;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.mts.Envelope;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

/**
 * This plan is executed when the user changes a query using the AQL query builder in the frontend.
 * It requests suggestions from the BROKER agent for the given query.
 */
public class RequestSuggestionsPlan extends RunOncePlan {

    private static final boolean FORCE_IGNORE_DATA = false; // For hacky debugging purposes

    AQLQueryChangedExternalTrigger trigger;

    private Set<AgentID> availableBrokers;

    public RequestSuggestionsPlan(AQLQueryChangedExternalTrigger trigger){
        this.trigger = trigger;
    }

    @Override
    public void executeOnce(PlanToAgentInterface planInterface) throws PlanExecutionError {
        boolean suggestionsDone = getSuggestionsFromCache(planInterface);
        if (suggestionsDone) {
            Platform.getLogger().log(getClass(), "Suggestions for same query already present! Skipping broker for now");
        } else {
            Platform.getLogger().log(getClass(), "Suggestions for query not yet available. Contacting brokers");
            requestFromBroker(planInterface);
        }
    }

    private boolean getSuggestionsFromCache(PlanToAgentInterface planInterface) {
        AQLQueryContext context = planInterface.getContext(AQLQueryContext.class);
        AQLQuery query = context.getCurrentQuery().queryContainer.getQuery(trigger.getQuery().hashCode());
        if (query != null && query.getSuggestions() != null) {
            AQLSuggestions suggestions = query.getSuggestions();
            DirectSsePublisher publisher = planInterface.getContext(DirectSsePublisher.class);
            publisher.publishSuggestionsReady(planInterface.getAgentID(), suggestions);
            return true;
        } else {
            return false;
        }
    }

    private void requestFromBroker(PlanToAgentInterface planInterface) {
        // Find available brokers
        DFRegistrationContext context = planInterface.getContext(DFRegistrationContext.class);
        this.availableBrokers = context.getSubscriptions();

        GAMessageHeader header;

        if (FORCE_IGNORE_DATA) {
            header = GAMessageHeader.REQUEST_SUGGESTIONS;
        } else {
            header = this.trigger.getQueryTree() instanceof MostGeneralQuery ?
                    GAMessageHeader.REQUEST_SUGGESTIONS :
                    GAMessageHeader.REQUEST_DATA_BASED_SUGGESTIONS;
        }

        UserQueryTrigger t = new UserQueryTrigger(
                this.trigger.getQuery(),
                GAMessageHeader.REQUEST_SUGGESTIONS,
                Integer.toString(this.trigger.getQuery().hashCode())
        );

        planInterface.getContext(QueryResultContext.class).addQuery(t);

        ACLMessage msg = new ACLMessage(Performative.REQUEST);
        msg.setConversationId(this.trigger.getConversationID().toString());
        Envelope envelope = new Envelope();
        envelope.setFrom(planInterface.getAgentID());
        availableBrokers.forEach(envelope::addTo);
        msg.setEnvelope(envelope);
        msg.setReceivers(availableBrokers);

        GAMessageContentWrapper messageContent = new GAMessageContentWrapper(header, t);

        try {
            msg.setContentObject(messageContent);
            Platform.getLogger().log(getClass(), "Set content object for suggestion request");
        } catch (IOException e) {
            Platform.getLogger().log(getClass(), Level.SEVERE, "Error serializing object of type " + messageContent.getContent().getClass());
            Platform.getLogger().log(getClass(), e);
        }

        try {
            planInterface.getAgent().sendMessage(msg);
            Platform.getLogger().log(getClass(), String.format(
                    " suggestions request to %d brokers",
                    this.availableBrokers.size()
            ));
        } catch (MessageReceiverNotFoundException | PlatformNotFoundException e) {
            Platform.getLogger().log(getClass(), e);
        }
    }

}
