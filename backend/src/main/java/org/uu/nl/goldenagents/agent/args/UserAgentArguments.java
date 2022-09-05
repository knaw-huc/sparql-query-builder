package org.uu.nl.goldenagents.agent.args;

import org.uu.nl.goldenagents.agent.context.query.AQLQueryContext;
import org.uu.nl.goldenagents.agent.context.query.QueryResultContext;
import org.uu.nl.goldenagents.agent.planscheme.user.UserMessagePlanScheme;
import org.uu.nl.goldenagents.agent.planscheme.user.UserTriggerPlanScheme;
import org.uu.nl.goldenagents.util.AgentUtils;

public class UserAgentArguments extends DFRegistration {

	public UserAgentArguments() {
		super(null, AgentUtils.BROKER_SERVICE_TYPE, new UserMessagePlanScheme());
		super.addContext(new QueryResultContext());
		super.addContext(new AQLQueryContext());
		super.addExternalTriggerPlanScheme(new UserTriggerPlanScheme());
	}

}