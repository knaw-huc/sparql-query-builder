package org.uu.nl.goldenagents.agent.planscheme;

import org.springframework.core.ConfigurableObjectInputStream;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentString;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Context;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanScheme;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class MessagePlanScheme implements PlanScheme {

	private static final Loggable logger = Platform.getLogger();
	private AgentContextInterface agentContextInterface;

	@Override
	public Plan instantiate(Trigger trigger, AgentContextInterface contextInterface) {
		this.agentContextInterface = contextInterface;
		if (trigger instanceof ACLMessage) {
				ACLMessage receivedMessage = (ACLMessage) trigger;
				
				if(receivedMessage.hasByteSequenceContent()) {
					try(ConfigurableObjectInputStream input = new ConfigurableObjectInputStream(new ByteArrayInputStream(receivedMessage.getByteSequenceContent()), Thread.currentThread().getContextClassLoader())){
						GAMessageContentWrapper contentWrapper = (GAMessageContentWrapper) input.readObject();
						return handleMessage((ACLMessage) trigger, contentWrapper.getHeader(), contentWrapper.getContent());

					} catch (IOException | ClassNotFoundException e) {
						logger.log(MessagePlanScheme.class, e);
					}
				} else {
					return handleMessage(receivedMessage, null, new GAMessageContentString(receivedMessage.getContent()));
				}
		}

		return Plan.UNINSTANTIATED;
	}

	public abstract Plan handleMessage(ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content);

	protected <T extends Context> T getContext(Class<T> klass) {
		return this.agentContextInterface.getContext(klass);
	}
}
