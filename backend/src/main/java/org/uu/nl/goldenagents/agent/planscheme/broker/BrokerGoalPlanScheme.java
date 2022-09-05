package org.uu.nl.goldenagents.agent.planscheme.broker;

import org.uu.nl.goldenagents.agent.plan.broker.DiscoverBrokerExpertisePlan;
import org.uu.nl.goldenagents.agent.plan.broker.FindOntologyConceptsPlan;
import org.uu.nl.goldenagents.agent.plan.broker.LoadLinksetsPlan;
import org.uu.nl.goldenagents.agent.trigger.goal.broker.DiscoverBrokerExpertiseGoal;
import org.uu.nl.goldenagents.agent.trigger.goal.broker.LoadConceptsGoal;
import org.uu.nl.goldenagents.agent.trigger.goal.broker.LoadLinksetGoal;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanScheme;

public class BrokerGoalPlanScheme implements PlanScheme {
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

        if(trigger instanceof LoadConceptsGoal) {
            plan = new FindOntologyConceptsPlan((LoadConceptsGoal) trigger);
        } else if(trigger instanceof LoadLinksetGoal) {
            plan = new LoadLinksetsPlan((LoadLinksetGoal) trigger);
        } else if (trigger instanceof DiscoverBrokerExpertiseGoal) {
            plan = new DiscoverBrokerExpertisePlan();
        }

        plan.setPlanGoal(trigger);

        return plan;
    }
}

