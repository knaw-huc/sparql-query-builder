package org.uu.nl.goldenagents.agent.plan.broker.mergeresult;

import org.uu.nl.goldenagents.agent.context.query.QueryProgressType;
import org.uu.nl.goldenagents.netmodels.angular.QueryProgress;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentString;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.SubGraph;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

/**
 * Plan that handles incoming messages from data source agents indicating an error occurred
 */
public class HandleDataErrorPlan extends MergeResultPlan {

    public HandleDataErrorPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    @Override
    protected void handleMessage(PlanToAgentInterface planInterface) {
        this.model.setErrorForAgent(this.datasourceAgent);
        SubGraph subGraph = SubGraph.fromACLMessage(message);
        String error = subGraph.getErrorReason();

        // Send error information to subscribed listeners
        QueryProgress<String> errorProgress = new QueryProgress<>(
                queryID, QueryProgressType.DATABASE_ERROR, error, false);
        QueryProgress.QueryProgressSubResult subErrorProgress = new QueryProgress.QueryProgressSubResult(
                message.getSender().getName().getFragment(), 0, true
        );
        errorProgress.addSubresult(subErrorProgress);
        this.publisher.publishQueryProgress(errorProgress);

        logUpdate(String.format("Datasource Agent %s replied with the following error: %s",
                        this.datasourceAgent,
                        error));
    }
}
