package org.uu.nl.goldenagents.agent.plan.registration;

import org.uu.nl.goldenagents.agent.context.registration.DFRegistrationContext;
import org.uu.nl.goldenagents.agent.context.registration.MinimalFunctionalityContext;
import org.uu.nl.goldenagents.agent.trigger.goal.RegisterWithDFGoal;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.ams.DirectoryFacilitator;
import org.uu.nl.net2apl.core.fipa.mts.Envelope;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;

public class RegisterWithDFPlan extends Plan {

    private RegisterWithDFGoal goal;
    private AgentID[] directoryFacilitators;

    public RegisterWithDFPlan(RegisterWithDFGoal goal) {
        this.goal = goal;
        this.directoryFacilitators = goal.getDirectoryFacilitators();
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
    public void execute(PlanToAgentInterface planInterface) throws PlanExecutionError {
        boolean success = true;

        if(!checkMinimalFunctionalityReady(planInterface)) {
            // Do not yet register in this cycle!
            return;
        }

        for (AgentID directoryFacilitator : this.directoryFacilitators) {
            success &= contactOnce(planInterface, directoryFacilitator);
        }

        setFinished(success);
    }

    private boolean checkMinimalFunctionalityReady(PlanToAgentInterface planInterface) {
        // Assume agents not implementing the MinimalFunctionalityContext interface on any of their contexts
        // are ready as soon as the initial plans are executed.
        boolean minimalFunctionalityReady = true;
        MinimalFunctionalityContext context = planInterface.getAgent().getContext(MinimalFunctionalityContext.class);

        if(context != null) {
            minimalFunctionalityReady = context.minimalFunctionalityReady();
        }

        return minimalFunctionalityReady;
    }

    private boolean contactOnce(PlanToAgentInterface planInterface, AgentID df) {
        DFRegistrationContext context = planInterface.getContext(DFRegistrationContext.class);

        // Do not register twice with the same agent, but plan should not fail because of this
        if(context.hasContacted(df)) {
            return true;
        }

        AgentID me = planInterface.getAgentID();
        try {
            if(context.isService()) {
                planInterface.getAgent().sendMessage(createDfMessage(me, df, context.getRegisterAs(), false));
            }
            if(context.isSubscriber()) {
                planInterface.getAgent().sendMessage(createDfMessage(me, df, context.getSubscribeTo(), true));
            }
            context.setContacted(df);
            return true;
        } catch(Exception ex) {
            Platform.getLogger().log(PrepareRegistrationWithDFPlan.class, ex);
            return false;
        }
    }

    private ACLMessage createDfMessage(AgentID aidMe, AgentID aidDF, String serviceName, boolean subscribeInstead) {
        String messageType = subscribeInstead ? DirectoryFacilitator.RequestType.SUBSCRIBER_ADD.toString() : DirectoryFacilitator.RequestType.SERVICE_ADD.toString();

        Envelope envelope = new Envelope();
        envelope.setFrom(aidMe);
        envelope.addTo(aidDF);
        envelope.addIntendedReceiver(aidDF);

        ACLMessage message = new ACLMessage(Performative.SUBSCRIBE);
        message.addReceiver(aidDF);
        message.addReplyTo(aidMe);
        message.setSender(aidMe);
        message.setContent(messageType + " " + aidMe.toString() + " " + serviceName);
        message.setEnvelope(envelope);

        return message;
    }

}
