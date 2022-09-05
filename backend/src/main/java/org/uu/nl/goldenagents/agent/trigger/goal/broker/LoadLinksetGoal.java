package org.uu.nl.goldenagents.agent.trigger.goal.broker;

import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Goal;

public class LoadLinksetGoal extends Goal {

	private boolean isAchieved = false;
	
	@Override
	public boolean isAchieved(AgentContextInterface contextInterface) {
		return this.isAchieved;
	}
	
    public void setAchieved(boolean achieved) {
        this.isAchieved = achieved;
    }

}
