package org.uu.nl.goldenagents.agent.planscheme.embedding;

import org.uu.nl.goldenagents.agent.planscheme.MessagePlanScheme;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.plan.Plan;

public class EmbeddingAgentMessagePlanScheme extends MessagePlanScheme {

	@Override
	public Plan handleMessage(ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) {
		// TODO Auto-generated method stub
		return Plan.UNINSTANTIATED;
	}

}
