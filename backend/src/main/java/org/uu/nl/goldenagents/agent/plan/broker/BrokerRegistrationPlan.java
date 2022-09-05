package org.uu.nl.goldenagents.agent.plan.broker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.uu.nl.goldenagents.agent.context.BrokerPrefixNamespaceContext;
import org.uu.nl.goldenagents.agent.plan.registration.SubmitRegistrationPlan;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentString;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.jena.RDFNameSpaceMap;
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

public class BrokerRegistrationPlan extends SubmitRegistrationPlan {

	private static Loggable logger = Platform.getLogger();
	
	public BrokerRegistrationPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
		super(message, header, content);
	}

	@Override
	public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
		
		// Execute the default registration plan
		super.executeOnce(planInterface, receivedMessage, header, content);

		BrokerPrefixNamespaceContext prefixContext = planInterface.getContext(BrokerPrefixNamespaceContext.class);

		final String messageContent = ((GAMessageContentString) content).getContent();

		Arrays.stream(messageContent.split(" ")).skip(1).forEach(uri -> {		
			try {			
				AgentID dbAID = new AgentID(new URI(uri));
				prefixContext.addAgentToContact(dbAID);
				
				logger.log(BrokerRegistrationPlan.class,
						"Asking DB agent " + dbAID + " for its expertise information.");
				
				ACLMessage response = receivedMessage.createForward(Performative.QUERY_REF, planInterface.getAgentID(), dbAID);
				response.setContentObject(new GAMessageContentWrapper(
						GAMessageHeader.DB_EXPERTISE,
						new RDFNameSpaceMap(prefixContext.getOntologyPrefixes())
				));
				planInterface.getAgent().sendMessage(response);
				prefixContext.setAgentContacted(dbAID, prefixContext.getOntologyPrefixes());
				
			} catch (MessageReceiverNotFoundException | PlatformNotFoundException | URISyntaxException | IOException ex) {
				logger.log(BrokerRegistrationPlan.class, ex);
			}
	
		});
	}	
}