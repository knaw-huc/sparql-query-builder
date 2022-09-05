package org.uu.nl.goldenagents.agent.plan.broker;

import org.uu.nl.goldenagents.agent.context.BrokerPrefixNamespaceContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.jena.RDFNameSpaceMap;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.mts.Envelope;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.util.logging.Level;

public class SendPreferredNamespacePrefixesPlan extends MessagePlan {

    private static final Loggable LOGGER = Platform.getLogger();


    public SendPreferredNamespacePrefixesPlan() {
        super(null, null, null);
    }

    public SendPreferredNamespacePrefixesPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    @Override
    public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
        BrokerPrefixNamespaceContext context = planInterface.getContext(BrokerPrefixNamespaceContext.class);

        if(receivedMessage != null) {
            context.addAgentToContact(receivedMessage.getSender());
        }

        ACLMessage msg = new ACLMessage(Performative.QUERY_REF);
        Envelope envelope = new Envelope();
        envelope.setFrom(planInterface.getAgentID());
        context.getAgentsToInformOfUpdatedPrefixes().forEach(envelope::addTo);
        msg.setEnvelope(envelope);

        msg.setReceivers(context.getAgentsToInformOfUpdatedPrefixes());

        try {
            msg.setContentObject(new GAMessageContentWrapper(
                    GAMessageHeader.DB_EXPERTISE,
                    new RDFNameSpaceMap(context.getOntologyPrefixes())
            ));
        } catch (IOException e) {
            LOGGER.log(getClass(), Level.SEVERE, "Failed to package default namespace of the broker in message");
            LOGGER.log(getClass(), Level.SEVERE, e);
        }

        try {
            planInterface.getAgent().sendMessage(msg);
            for(AgentID aid : context.getAgentsToInformOfUpdatedPrefixes()) {
                context.setAgentContacted(aid, context.getOntologyPrefixes());
            }
        } catch (MessageReceiverNotFoundException | PlatformNotFoundException e) {
            LOGGER.log(getClass(), Level.SEVERE, "Failed to send message with default namespaces to one or more agents");
            LOGGER.log(getClass(), Level.SEVERE, e);
        }
    }
}
