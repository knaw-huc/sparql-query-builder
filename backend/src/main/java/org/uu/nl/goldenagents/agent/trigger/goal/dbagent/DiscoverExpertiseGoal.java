package org.uu.nl.goldenagents.agent.trigger.goal.dbagent;

import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Goal;

public class DiscoverExpertiseGoal extends Goal {

    /**
     * Implement to check whether the goal is achieved according to the information
     * that is available to the agent. If a goal is achieved, it will be automatically
     * removed. Re-adopt the goal if the goal should be achieved again.
     *
     * @param contextInterface Interface that exposes the context container of the agent.
     * @return True iff the goal should be considered achieved.
     */
    @Override
    public boolean isAchieved(AgentContextInterface contextInterface) {
        DBAgentContext context = contextInterface.getContext(DBAgentContext.class);
        return context.getExpertise() != null;
    }
}
