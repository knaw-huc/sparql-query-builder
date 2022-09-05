package org.uu.nl.goldenagents.agent.plan.registration;

import org.uu.nl.goldenagents.agent.context.registration.DFRegistrationContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentString;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class SubmitRegistrationPlan extends MessagePlan {

	private static final Loggable logger = Platform.getLogger();

	public SubmitRegistrationPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
		super(message, header, content);
	}

	@Override
	public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {

		final DFRegistrationContext regContext = planInterface.getContext(DFRegistrationContext.class);
		final String messageContent = ((GAMessageContentString) content).getContent();

		Arrays.stream(messageContent.split(" ")).skip(1).forEach(uri -> {

			try {
				AgentID dbAID = new AgentID(new URI(uri));
				regContext.addSubscription(dbAID);
				logger.log(SubmitRegistrationPlan.class, String.format(
						"Agent %s just subscribed to the service `%s` provided by agent %s",
						planInterface.getAgent().getAID().toString(),
						regContext.getSubscribeTo(),
						dbAID.toString()
				));
			} catch (URISyntaxException e) {
				logger.log(SubmitRegistrationPlan.class, e);
			}

		});
	}

}