package org.uu.nl.goldenagents.agent.trigger.goal;

import org.uu.nl.goldenagents.agent.context.registration.DFRegistrationContext;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Goal;

public class RegisterWithDFGoal extends Goal {

    private AgentID[] directoryFacilitators;

    public RegisterWithDFGoal(AgentID... directoryFacilitators) {
        this.directoryFacilitators = directoryFacilitators;
    }

    public AgentID[] getDirectoryFacilitators() {
        return directoryFacilitators;
    }

    public void setDirectoryFacilitators(AgentID[] directoryFacilitators) {
        this.directoryFacilitators = directoryFacilitators;
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
        boolean finished = true;
        DFRegistrationContext context = contextInterface.getContext(DFRegistrationContext.class);
        if(context != null) {
            for(AgentID df : this.directoryFacilitators) {
                finished &= context.hasContacted(df);
            }
        } else {
            finished = false;
        }

        return finished;
    }
}
