package org.uu.nl.goldenagents.agent.args;

import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.agent.context.registration.MinimalFunctionalityContext;
import org.uu.nl.goldenagents.agent.plan.dbagent.DiscoverDbLimitPlan;
import org.uu.nl.goldenagents.agent.planscheme.dbagent.DBAgentGoalPlanScheme;
import org.uu.nl.goldenagents.agent.planscheme.dbagent.DbAgentMessagePlanScheme;
import org.uu.nl.goldenagents.util.AgentUtils;

public class DBAgentArguments extends DFRegistration {

	public DBAgentArguments(DBAgentContext dbAgentContext) {
		super(AgentUtils.SPARQL_SERVICE_NAME, null);
		super.addContext(dbAgentContext, DBAgentContext.class, MinimalFunctionalityContext.class);
		super.addMessagePlanScheme(new DbAgentMessagePlanScheme());
		super.addGoalPlanScheme(new DBAgentGoalPlanScheme());
		super.addInitialPlan(new DiscoverDbLimitPlan());
	}
}