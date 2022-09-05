package org.uu.nl.goldenagents.agent.args;

import org.uu.nl.goldenagents.agent.context.registration.DFRegistrationContext;
import org.uu.nl.goldenagents.agent.plan.registration.PrepareRegistrationWithDFPlan;
import org.uu.nl.goldenagents.agent.plan.registration.ShutDownRegistrationPlan;
import org.uu.nl.goldenagents.agent.planscheme.RegistrationGoalPlanScheme;
import org.uu.nl.goldenagents.agent.planscheme.RegistrationPlanScheme;
import org.uu.nl.net2apl.core.agent.AgentArguments;

public class DFRegistration extends AgentArguments {
	
    // 'subscribeInstead' (can) change(s) the /meaning/ (if not the implementation) of the class, so TODO: change name?
    // also TODO: this only subscribes the agent to a single service, and it can't really be a service on it's own, since contexts are unique by class (another design-flaw)
    /**
     * Create the Directory-Facilitator plans that are necessary to:
     * <ul>
     * 	<li>Initiate contact with the DF to publish services that are offered and also to query for services that are needed.</li>
     * 	<li>Deregister when the agent is killed.</li>
     *  <li>Handle messages that are related to the subscription system, so that an agent can know which other agents are subscribed to it.</li>
     * </ul>
     * @param registerAsServiceName
     * @param subscribeToServiceName
     */
    public DFRegistration(String registerAsServiceName, String subscribeToServiceName) {
        super.addContext(new DFRegistrationContext(registerAsServiceName, subscribeToServiceName));
		super.addShutdownPlan(new ShutDownRegistrationPlan());
		super.addMessagePlanScheme(new RegistrationPlanScheme());
		super.addGoalPlanScheme(new RegistrationGoalPlanScheme());
        super.addInitialPlan(new PrepareRegistrationWithDFPlan(true));
    }
    
    /**
     * Create the Directory-Facilitator plans that are necessary to:
     * <ul>
     * 	<li>Initiate contact with the DF to publish services that are offered and also to query for services that are needed.</li>
     * 	<li>Deregister when the agent is killed.</li>
     *  <li>Handle messages that are related to the subscription system, so that an agent can know which other agents are subscribed to it.</li>
     * </ul>
     * This constructor also enables the programmer to insert a custom planscheme so an agent can do additional actions, such as to query
     * an agent for more information when that agent subscribes.
     * @param registerAsServiceName
     * @param subscribeToServiceName
     * @param scheme
     */
    public DFRegistration(String registerAsServiceName, String subscribeToServiceName, RegistrationPlanScheme scheme) {
        super.addContext(new DFRegistrationContext(registerAsServiceName, subscribeToServiceName));
		super.addShutdownPlan(new ShutDownRegistrationPlan());
		super.addMessagePlanScheme(scheme);
        super.addGoalPlanScheme(new RegistrationGoalPlanScheme());
        super.addInitialPlan(new PrepareRegistrationWithDFPlan(true));
    }

}
