package org.uu.nl.goldenagents.agent.trigger.goal.broker;

import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Goal;

import java.util.Map;

public class LoadConceptsGoal extends Goal {

    private boolean isAchieved = false;
    private final Map<String, String> usedPrefixes;

    public LoadConceptsGoal(Map<String, String> usedPrefixes) {
        this.usedPrefixes = usedPrefixes;
    }

    public LoadConceptsGoal() {
        usedPrefixes = null;
    }

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
        return this.isAchieved;
    }

    public void setAchieved(boolean achieved) {
        this.isAchieved = achieved;
    }

    public Map<String, String> getUsedPrefixes() {
        return usedPrefixes;
    }
}
