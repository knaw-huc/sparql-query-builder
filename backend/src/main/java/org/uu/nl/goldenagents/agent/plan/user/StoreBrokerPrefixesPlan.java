package org.uu.nl.goldenagents.agent.plan.user;

import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.jena.RDFNameSpaceMap;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;

public class StoreBrokerPrefixesPlan extends MessagePlan {

    public StoreBrokerPrefixesPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    @Override
    public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
        planInterface.getContext(PrefixNSListenerContext.class)
                .setPrefixMap(receivedMessage.getSender(), ((RDFNameSpaceMap)content).getNamespaceMap());
    }
}
