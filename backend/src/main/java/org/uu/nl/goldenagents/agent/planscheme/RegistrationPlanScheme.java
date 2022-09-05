package org.uu.nl.goldenagents.agent.planscheme;

import org.uu.nl.goldenagents.agent.plan.registration.PrepareRegistrationWithDFPlan;
import org.uu.nl.goldenagents.agent.plan.registration.SubmitRegistrationPlan;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.plan.Plan;

public class RegistrationPlanScheme extends MessagePlanScheme {

	@Override
	public Plan handleMessage(ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) {

		switch (receivedMessage.getPerformative()) {
		default:
			return Plan.UNINSTANTIATED;
		case PROPOSE:
			// The PROPOSE performative is sent by the DF upon creation to all agents on the platform and suggests
			// the receiving agent to register with it
			return new PrepareRegistrationWithDFPlan(false, receivedMessage.getSender());
		case INFORM:
			return new SubmitRegistrationPlan(receivedMessage, header, content);
		}
	}
}
