package org.uu.nl.goldenagents.agent.planscheme;

import org.uu.nl.goldenagents.agent.plan.registration.RegisterWithDFPlan;
import org.uu.nl.goldenagents.agent.trigger.goal.RegisterWithDFGoal;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanScheme;

public class RegistrationGoalPlanScheme implements PlanScheme {
    /**
     * Try to instantiate the plan scheme. Must return Plan.uninstantiated() if the plan scheme is not
     * relevant or applicable for the given trigger and context. If the return value is
     * not null then it will be adopted as a current plan and can be executed by a deliberation
     * step.
     *
     * @param trigger          Trigger that must be processed.
     * @param contextInterface An interface to obtain the context of the agent.
     * @return Plan.uninstantiated() iff the plan scheme is not relevant and applicable, otherwise the plan to be scheduled for execution in the current deliberation cycle.
     */
    @Override
    public Plan instantiate(Trigger trigger, AgentContextInterface contextInterface) {
        Plan plan = Plan.UNINSTANTIATED;

        if(trigger instanceof RegisterWithDFGoal) {
            plan = new RegisterWithDFPlan((RegisterWithDFGoal) trigger);
        }

        return plan;
    }
}
