package org.uu.nl.goldenagents.agent.plan.user.queryhandler;

import org.uu.nl.goldenagents.agent.context.query.QueryResultContext;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.mts.Envelope;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;

public abstract class QueryHandlerPlan extends Plan {

    protected UserQueryTrigger trigger;

    public QueryHandlerPlan(UserQueryTrigger trigger) {
        this.trigger = trigger;
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

        if(!verifyQuerySyntax()) {
            setFinished(true);
            return;
        }

        QueryResultContext context = planInterface.getAgent().getContext(QueryResultContext.class);
        if (context == null) {
            Platform.getLogger().log(getClass(), "No QueryResultContext found for this User Agent. Query cannot be processed");
            // TODO inform user

            // TODO plan recovery should be to add a query context and re-adopt the goal to execute the query
            throw new PlanExecutionError();
        }

        Agent broker = selectBroker(planInterface);
        if (broker == null) {
            Platform.getLogger().log(getClass(), "No suitable broker found to handle this query");
            // TODO inform user

            // TODO how to handle this?
            throw new PlanExecutionError();
        }

        String conversationID = contactBroker(planInterface, broker);
        if(conversationID != null) {
            context.addQuery(this.trigger);
            context.addConversationIDForQueryID(this.trigger.getQueryID(), conversationID);
            setFinished(true);
        } else {
            // TODO inform user
            Platform.getLogger().log(getClass(), "Could not send query to broker");
        }
    }

    private boolean verifyQuerySyntax() {
        // TODO query parsing and error handling should be done here, but for that we need a canonical representation of what syntax is allowed
        return true;
    }

    /**
     * Select a broker to handle this query.
     * @param planInterface PlanToAgentInterface reference
     * @return  Broker agent object which contains all information to send a message
     */
    protected abstract Agent selectBroker(PlanToAgentInterface planInterface);

    /**
     * Contact the broker agent to handle this query
     * @param planInterface PlanToAgentInterface reference
     * @param brokerAgent   Agent object of broker agent who can handle this query
     * @return              ConversationID of conversation the query was sent in,
     *                      or null if broker could not be contacted
     */
    protected String contactBroker(PlanToAgentInterface planInterface, Agent brokerAgent) {

        ACLMessage message = constructMessage(planInterface, brokerAgent);
        String conversationID = null;

        try {
            planInterface.getAgent().sendMessage(message);
            conversationID = message.getConversationId();
        } catch (MessageReceiverNotFoundException | PlatformNotFoundException e) {
            Platform.getLogger().log(getClass(), e);
        }

        return conversationID;
    }

    /**
     * Construct a message to a single broker agent with the query request
     * @param planInterface
     * @param brokerAgent
     * @return              ACLMessage to single broker agent
     */
    protected ACLMessage constructMessage(PlanToAgentInterface planInterface, Agent brokerAgent) {
        Envelope envelope = new Envelope();
        envelope.setFrom(planInterface.getAgentID());
        envelope.addTo(brokerAgent.getAID());
        envelope.addIntendedReceiver(brokerAgent.getAID());

        GAMessageContentWrapper wrapper = new GAMessageContentWrapper(
                trigger.getQueryType(), trigger);

        ACLMessage message = new ACLMessage(Performative.QUERY_REF);
        message.setEnvelope(envelope);

        try {
            message.setContentObject(wrapper);
        } catch(IOException e) {
            Platform.getLogger().log(getClass(), "Could not add query to message content");
            Platform.getLogger().log(getClass(), e);
            return null;
        }

        message.addReceiver(brokerAgent.getAID());
        message.addReplyTo(planInterface.getAgentID());
        message.setSender(planInterface.getAgentID());

        return message;
    }


}
