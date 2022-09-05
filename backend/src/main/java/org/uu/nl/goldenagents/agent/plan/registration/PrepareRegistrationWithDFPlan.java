package org.uu.nl.goldenagents.agent.plan.registration;

import org.uu.nl.goldenagents.agent.trigger.goal.RegisterWithDFGoal;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PrepareRegistrationWithDFPlan extends RunOncePlan {

    private boolean contactAllAvailable;
    private Set<AgentID> directoryFacilitators = new HashSet<>();

    public PrepareRegistrationWithDFPlan(boolean contactAllAvailable, AgentID... directoryFacilitators) {
        this.contactAllAvailable = contactAllAvailable;
        this.directoryFacilitators.addAll(Arrays.asList(directoryFacilitators));
    }

    /**
     * Execute the business logic of the plan. Make sure that when you implement this
     * method that the method will return. Otherwise it will hold up other agents that
     * are executed in the same thread. Also, if the plan should only be executed once,
     * then make sure that somewhere in the method it calls the setFinished(true) method.
     *
     * @param planInterface
     * @throws PlanExecutionError If you throw this error than it will be automatically adopted as an internal trigger.
     */
    @Override
    public void executeOnce(PlanToAgentInterface planInterface) throws PlanExecutionError {
        if(this.contactAllAvailable) {
            try {
                this.directoryFacilitators.addAll(planInterface.getAgent().getPlatform().getLocalDirectoryFacilitators());
            } catch(PlatformNotFoundException ex) {
                Platform.getLogger().log(getClass(), ex);
            }
        }

        planInterface.getAgent().adoptGoal(new RegisterWithDFGoal(this.directoryFacilitators.toArray(new AgentID[0])));

        Platform.getLogger().log(PrepareRegistrationWithDFPlan.class, String.format(
                "Agent %s adopted goal to register with %d directory facilitators",
                planInterface.getAgent().getAID().toString(),
                this.directoryFacilitators.size()
        ));
    }
}
