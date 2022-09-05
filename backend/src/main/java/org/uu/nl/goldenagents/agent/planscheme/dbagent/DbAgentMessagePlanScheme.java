package org.uu.nl.goldenagents.agent.planscheme.dbagent;

import org.uu.nl.goldenagents.agent.plan.dbagent.PublishExpertisePlan;
import org.uu.nl.goldenagents.agent.plan.dbagent.QueryDbPlan;
import org.uu.nl.goldenagents.agent.plan.dbagent.SparklisTypeSuggestionsPlan;
import org.uu.nl.goldenagents.agent.planscheme.MessagePlanScheme;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.plan.Plan;

public class DbAgentMessagePlanScheme extends MessagePlanScheme {

    public Plan handleMessage(ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) {

        switch (header) {
            default:
                return Plan.UNINSTANTIATED;
            case BROKER_ACK:
            case BROKER_QUERY:
                return new QueryDbPlan(receivedMessage, header, content);
            case DB_EXPERTISE:
                return new PublishExpertisePlan(receivedMessage, header, content);
			case REQUEST_SUGGESTIONS:
				return new SparklisTypeSuggestionsPlan(receivedMessage, header, content);
        }
    }
}
