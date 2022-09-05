package org.uu.nl.goldenagents.agent.planscheme.user;

import org.uu.nl.goldenagents.agent.plan.registration.PrepareRegistrationWithDFPlan;
import org.uu.nl.goldenagents.agent.plan.user.ProcessQueryResultPlan;
import org.uu.nl.goldenagents.agent.plan.user.ProcessSuggestionsPlan;
import org.uu.nl.goldenagents.agent.plan.user.StoreBrokerPrefixesPlan;
import org.uu.nl.goldenagents.agent.plan.user.UserRegistrationPlan;
import org.uu.nl.goldenagents.agent.planscheme.RegistrationPlanScheme;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.plan.Plan;

public class UserMessagePlanScheme extends RegistrationPlanScheme {

	public Plan handleMessage(ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) {

		switch (receivedMessage.getPerformative()) {
			default:
				break;
			case PROPOSE:
				return new PrepareRegistrationWithDFPlan(false, receivedMessage.getSender());
			case INFORM:
				return new UserRegistrationPlan(receivedMessage, header, content);
		}

		switch(header) {
			default: return Plan.UNINSTANTIATED;
			case BROKER_RESULTSET:
				return new ProcessQueryResultPlan(receivedMessage, header, content);
			case INFORM_SUGGESTIONS:
				return new ProcessSuggestionsPlan(receivedMessage, header, content);
			case DB_EXPERTISE: // Abuse message header that broker uses to request expertise from DB agent because it also contains the NS prefix map
				return new StoreBrokerPrefixesPlan(receivedMessage, header, content);
		}
	}

}
