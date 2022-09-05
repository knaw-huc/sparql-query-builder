package org.uu.nl.goldenagents.agent.plan.user.queryhandler;

import org.uu.nl.goldenagents.agent.context.registration.DFRegistrationContext;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.net.URISyntaxException;

/**
 * This class finds a random agent of the BrokerAgent type on the platform.
 * <TODO> In the future, this (simple) implementation
 * should be replaced so the UserAgent selects the broker from a list of registered broker agents it requested from
 * the DF earlier </TODO>
 */
public class SimpleBrokerQueryHandlerPlan extends QueryHandlerPlan {

    private UserQueryTrigger trigger;

    public SimpleBrokerQueryHandlerPlan(UserQueryTrigger trigger) {
        super(trigger);
    }

    /**
     * Select a broker to handle this query.
     *
     * @param planInterface PlanToAgentInterface reference
     * @return Broker agent object which contains all information to send a message
     */
    @Override
    protected Agent selectBroker(PlanToAgentInterface planInterface) {
        Agent broker = null;
        Platform platform = null;

        try {
            platform = planInterface.getAgent().getPlatform();
        } catch (PlatformNotFoundException e) {
            Platform.getLogger().log(getClass(), e);
        }

        if(platform != null) {
            DFRegistrationContext dfRegistrationContext = planInterface.getContext(DFRegistrationContext.class);

            if(dfRegistrationContext.getSubscriptions().size() > 0) {
                AgentID brokerID = (AgentID) dfRegistrationContext.getSubscriptions().toArray()[0];
                try {
                    broker = platform.getLocalAgent(brokerID);
                } catch (URISyntaxException e) {
                    Platform.getLogger().log(getClass(), e);
                }
            }
        }

        return broker;
    }
}
