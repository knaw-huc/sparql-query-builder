package org.uu.nl.goldenagents.agent.trigger.goal.broker;

import org.uu.nl.goldenagents.agent.context.BrokerContext;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Goal;

/** Goal of discovering expertise for Broker Agents **/
public class DiscoverBrokerExpertiseGoal extends Goal {

	@Override
	public boolean isAchieved(AgentContextInterface contextInterface) {
		BrokerContext context = contextInterface.getContext(BrokerContext.class);
        if(context.getExpertiseGraph() != null) {
        	return context.getExpertiseGraph().getNodes().size() 
        			== context.getDbAgentExpertises().size();
        }
		return false;
	}

}
