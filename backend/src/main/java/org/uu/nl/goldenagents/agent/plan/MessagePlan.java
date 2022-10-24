package org.uu.nl.goldenagents.agent.plan;

import java.io.IOException;
import java.util.logging.Level;

import org.uu.nl.goldenagents.agent.plan.dbagent.QueryDbPlan;
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
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

public abstract class MessagePlan extends RunOncePlan {
	
	protected final ACLMessage message;
	protected final GAMessageHeader header;
	protected final FIPASendableObject content;
	protected static final Loggable logger = Platform.getLogger();
	
	public MessagePlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
		this.message = message;
		this.header = header;
		this.content = content;
	}

	@Override
	public final void executeOnce(PlanToAgentInterface planInterface) throws PlanExecutionError {
		Platform.getLogger().log(getClass(), String.format("Starting message plan %s for agent %s (%s)", getClass(), planInterface.getAgent().getAID(), planInterface.getAgent().getName()));
		executeOnce(planInterface, message, header, content);
	}
	
	/**
	 * This method is executed when a message has been received
	 * @param planInterface An interface to the agent in order to access context, etc
	 * @param receivedMessage The message that triggered this plan
	 * @param header The header of the message
	 * @param content The content of the message
	 * @throws PlanExecutionError Exception thrown when executing this plan goes totally awry
	 */
	public abstract void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError;

	/**
	 * Sends an error message back to the sender
	 * @param planInterface An interface to the agent in order to access context, etc
	 * @param errorMessage	Error occurred in the execution of the plan.
	 */
	protected void sendErrorMessage(PlanToAgentInterface planInterface, String errorMessage) {
		FIPASendableObject contentObject = new GAMessageContentString("Unable to execute query! Reason: " + errorMessage);
		sendErrorMessage(planInterface, contentObject);
	}

	/**
	 * Sends an error with a custom object back to the sender
	 * @param planInterface An interface to the agent in order to access context, etc
	 * @param contentObject	Error object.
	 */
	protected void sendErrorMessage(PlanToAgentInterface planInterface, FIPASendableObject contentObject) {
		try {
			ACLMessage response = this.message.createReply(planInterface.getAgentID(), Performative.FAILURE);
			response.setContentObject(new GAMessageContentWrapper(GAMessageHeader.DB_ERROR, contentObject));

			AgentID aid = this.message.getSender();
			logger.log(QueryDbPlan.class, Level.WARNING, "Sending error message to " + aid.getUuID()
					+ " in conversation " + response.getConversationId());
			planInterface.getAgent().sendMessage(response);
		} catch (MessageReceiverNotFoundException | PlatformNotFoundException | IOException ex) {
			logger.log(QueryDbPlan.class, ex);
		}
	}
}