package org.uu.nl.goldenagents.agent.plan.user;

import org.uu.nl.goldenagents.agent.plan.registration.SubmitRegistrationPlan;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentString;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Level;

public class UserRegistrationPlan extends SubmitRegistrationPlan {

    private static Loggable logger = Platform.getLogger();

    public UserRegistrationPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    @Override
    public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
        super.executeOnce(planInterface, receivedMessage, header, content);

        final String messageContent = ((GAMessageContentString) content).getContent();
        Arrays.stream(messageContent.split(" ")).skip(1).forEach(uri -> {
            try {
                AgentID brokerAgent = new AgentID(new URI(uri));
                logger.log(getClass(), "Requesting prefixes from broker agent " + brokerAgent.toString());
                ACLMessage forward = receivedMessage.createForward(Performative.REQUEST, planInterface.getAgentID(), brokerAgent);
                forward.setContentObject(new GAMessageContentWrapper(GAMessageHeader.REQUEST_PREFIX_MAPPING));
                planInterface.getAgent().sendMessage(forward);
            } catch (URISyntaxException | IOException | MessageReceiverNotFoundException | PlatformNotFoundException e) {
                logger.log(getClass(), Level.SEVERE, "Failed to request prefix mapping from broker agent");
                logger.log(getClass(), Level.SEVERE, e);
            }
        });
    }
}
