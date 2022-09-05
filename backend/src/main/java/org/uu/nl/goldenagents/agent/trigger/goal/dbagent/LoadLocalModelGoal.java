package org.uu.nl.goldenagents.agent.trigger.goal.dbagent;

import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Goal;

public class LoadLocalModelGoal extends Goal {
    @Override
    public boolean isAchieved(AgentContextInterface agentContextInterface) {
        DBAgentContext c = agentContextInterface.getContext(DBAgentContext.class);
        return !c.getConfig().getMethod().isLocal() || c.isLocalDatasetReady();
    }
}
