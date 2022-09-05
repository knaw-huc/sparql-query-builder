package org.uu.nl.goldenagents.agent.plan.broker.mergeresult;

import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

/**
 * This class handles the end-of-results message that may be sent by a broker after all results in reply to a
 * subquery have been transmitted
 */
public class HandleDataEndPlan extends MergeResultPlan {
    public HandleDataEndPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    @Override
    protected void handleMessage(PlanToAgentInterface planInterface) {
        this.model.setAgentFinished(this.datasourceAgent);

        // Send an update to subscribed listeners
        this.publisher.publishQueryProgress(
                createQueryProgress(this.model.getTotalSize(), -1));

        // Logging
        logUpdate(String.format(
                "Received all parts from DB Agent %s for conversation %s",
                this.datasourceAgent.getName().getFragment(),
                this.conversationID));
    }
}
