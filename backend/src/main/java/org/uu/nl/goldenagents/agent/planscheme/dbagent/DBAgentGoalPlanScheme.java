package org.uu.nl.goldenagents.agent.planscheme.dbagent;

import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.agent.plan.dbagent.*;
import org.uu.nl.goldenagents.agent.trigger.goal.dbagent.DiscoverDatabaseLimitGoal;
import org.uu.nl.goldenagents.agent.trigger.goal.dbagent.DiscoverExpertiseGoal;
import org.uu.nl.goldenagents.agent.trigger.goal.dbagent.LoadLocalModelGoal;
import org.uu.nl.goldenagents.agent.trigger.goal.dbagent.PublishExpertiseGoal;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Goal;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.PlanScheme;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;

import java.util.List;

/** Goal plan scheme for Database agents **/
public class DBAgentGoalPlanScheme implements PlanScheme {
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

        DBAgentContext context = contextInterface.getContext(DBAgentContext.class);

        if (trigger instanceof DiscoverDatabaseLimitGoal) {
            if (context.getConfig().isLocal() && !context.isLocalDatasetReady()) {
                plan = new LoadGoalPlan(new LoadLocalModelGoal(), LoadGoalPlan.LOAD_WHEN.NOT_PURSUING_SAME_TYPE);
            } else {
                plan = new DiscoverDbLimitPlan();
            }
        } else if (trigger instanceof DiscoverExpertiseGoal) {
            if (context.getDbLimit() <= 0) {
                plan = new LoadGoalPlan(new DiscoverDatabaseLimitGoal(), LoadGoalPlan.LOAD_WHEN.NOT_PURSUING_SAME_TYPE);
            } else if (context.isMappingsLoaded()) {
                // It is an initial plan, so we should be able to sit this one out if not the case
                if(context.isLoadEntitiesForExpertise()) {
                    plan = new DiscoverEntitiesPlan();
                } else {
                    plan = new DiscoverExpertisePlan();
                }
            }
        } else if (trigger instanceof PublishExpertiseGoal) {
            if(context.getExpertise() == null) {
                plan = new LoadGoalPlan(new DiscoverExpertiseGoal(), LoadGoalPlan.LOAD_WHEN.NOT_PURSUING_SAME_TYPE);
            } else {
                plan = new PublishExpertisePlan((PublishExpertiseGoal) trigger);
            }
        } else if (trigger instanceof LoadLocalModelGoal) {
            plan = new LoadLocalModelPlan();
        }

        if(plan != Plan.UNINSTANTIATED) plan.setPlanGoal(trigger);

        return plan;
    }

    public static class LoadGoalPlan extends RunOncePlan {

        private final Goal goal;
        private final LOAD_WHEN whenToLoad;

        public LoadGoalPlan(Goal goal, LOAD_WHEN whenToLoad) {
            this.goal = goal;
            this.whenToLoad = whenToLoad;
        }

        @Override
        public void executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
            if(LOAD_WHEN.NOT_PURSUING.equals(this.whenToLoad) && planToAgentInterface.hasGoal(this.goal)) {
                return;
            } else if (LOAD_WHEN.NOT_PURSUING_SAME_TYPE.equals(this.whenToLoad) && hasGoalOfType(planToAgentInterface)) {
                return;
            } else {
                planToAgentInterface.adoptGoal(goal);
            }
        }

        private boolean hasGoalOfType(PlanToAgentInterface planToAgentInterface) {
            List<Goal> goals = planToAgentInterface.getAgent().getGoals();
            for(Goal goal : goals) {
                if(goal.getClass().equals(this.goal.getClass())) {
                    return true;
                }
            }
            return false;
        }

        public enum LOAD_WHEN {
            ALWAYS, // Always adopt the goal
            NOT_PURSUING, // Only adopt the goal if this specific instance is not already being pursued
            NOT_PURSUING_SAME_TYPE // Only adopt the goal if no other goal of the same class is already pursued (regardless of instance)
        }
    }
}
