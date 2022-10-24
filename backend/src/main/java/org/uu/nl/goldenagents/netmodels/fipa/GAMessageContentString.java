package org.uu.nl.goldenagents.netmodels.fipa;

import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

/**
 * Used for sending a string via the ACLMessage.setContentObject system. This is done for uniformity, all message content can be retrieved by accessing
 * the <code>content</code> parameter in <code>MessagePlan.executeOnce()</code>
 * @author jbaas
 * @see org.uu.nl.goldenagents.agent.plan.MessagePlan#executeOnce(org.uu.nl.net2apl.core.agent.PlanToAgentInterface, org.uu.nl.net2apl.core.fipa.acl.ACLMessage, GAMessageHeader, FIPASendableObject)
 */
public class GAMessageContentString extends GaMessageContentObjectContainer<String> {

	public GAMessageContentString(String content) {
		super(content);
	}
}
