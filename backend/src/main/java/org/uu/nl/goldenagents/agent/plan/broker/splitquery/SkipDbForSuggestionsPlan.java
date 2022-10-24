package org.uu.nl.goldenagents.agent.plan.broker.splitquery;

import org.uu.nl.goldenagents.agent.context.DirectSsePublisher;
import org.uu.nl.goldenagents.agent.context.query.QueryProgressType;
import org.uu.nl.goldenagents.agent.plan.broker.suggestions.RequestDbSearchSuggestionsPlan;
import org.uu.nl.goldenagents.exceptions.BadQueryException;
import org.uu.nl.goldenagents.netmodels.angular.QueryProgress;
import org.uu.nl.goldenagents.netmodels.fipa.*;
import org.uu.nl.goldenagents.sparql.CachedModel;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class SkipDbForSuggestionsPlan extends SplitQueryExpertise {

    public SkipDbForSuggestionsPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    @Override
    public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
        // Update plan interface and broker context on every execution cycle
        this.setSuggestionsRequested(true);

        if(!preprocess(planInterface)) {
            return;
        }

        DirectSsePublisher publisher = this.planInterface.getContext(DirectSsePublisher.class);

        CachedModel model = ensureModelPresent();
        publisher.publishQueryProgress(
                new QueryProgress<>(queryID, QueryProgressType.DATA_COLLECTED, model.getTotalSize(), true));

        pretendResultsCollected(model, publisher);

        try {
            List<AgentQuery> agentQueries = createAgentQueries();
            for(AgentQuery agentQuery : agentQueries) {
                model.addParticipatingAgent(agentQuery.getQueryOwner());
            }
        } catch (MissingExpertException | BadQueryException e) {
            e.printStackTrace();
        }

        try {
            ACLMessage msg = ACLMessage.getEmpty();
            msg.setConversationId(message.getConversationId());
            SubGraph fakeSubgraph = new SubGraph(this.queryRequest.getAql().hashCode());
            msg.setContentObject(new GAMessageContentWrapper(GAMessageHeader.DB_DATA_END, fakeSubgraph));
            msg.setPerformative(Performative.INFORM_REF);
            planInterface.adoptPlan(new RequestDbSearchSuggestionsPlan(model, msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void pretendResultsCollected(CachedModel model, DirectSsePublisher publisher) {
        try {
            QueryResult result = model.querySelect();

            // Inform user agent that all results have been collected
            ACLMessage forward = this.message.createForward(this.planInterface.getAgentID(), this.userAgent);
            forward.setPerformative(Performative.INFORM_REF);
            forward.setContentObject(new GAMessageContentWrapper(GAMessageHeader.BROKER_RESULTSET, result));
            this.planInterface.getAgent().sendMessage(forward);

            publisher.publishQueryProgress(
                    new QueryProgress<Long>(queryID, QueryProgressType.RESULTS_RETURNED, true));
        } catch (PlatformNotFoundException | MessageReceiverNotFoundException | IOException e) {
            Platform.getLogger().log(getClass(), Level.SEVERE, "Failed to inform user of results");
            Platform.getLogger().log(getClass(), e);
        }
    }
}
