package org.uu.nl.goldenagents.agent.trigger.goal.dbagent;

import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Goal;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

/**
 * This goal should be adopted if a broker sends a request to publish expertise, but expertise is not net available.
 * That way, the plan can be scheduled for later.
 */
public class PublishExpertiseGoal extends Goal {

    private ACLMessage message;
    private GAMessageHeader header;
    private FIPASendableObject content;
    private boolean isAchieved = false;

    public PublishExpertiseGoal(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        this.message = message;
        this.header = header;
        this.content = content;
    }

    public ACLMessage getMessage() {
        return message;
    }

    public GAMessageHeader getHeader() {
        return header;
    }

    public FIPASendableObject getContent() {
        return content;
    }

    /**
     * Implement to check whether the goal is achieved according to the information
     * that is available to the agent. If a goal is achieved, it will be automatically
     * removed. Re-adopt the goal if the goal should be achieved again.
     *
     * @param contextInterface Interface that exposes the context container of the agent.
     * @return True iff the goal should be considered achieved.
     */
    @Override
    public boolean isAchieved(AgentContextInterface contextInterface) {
        return this.isAchieved;
    }

    public void setAchieved(boolean achieved) {
        this.isAchieved = achieved;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PublishExpertiseGoal) {
            PublishExpertiseGoal otherGoal = (PublishExpertiseGoal) obj;
            boolean messageEqual = this.message == null && otherGoal.message == null || this.message.equals(otherGoal.message);
            boolean headerEqual = this.header == null && otherGoal.header == null || this.header.equals(otherGoal.header);
            boolean contentEqual = this.content == null && otherGoal.content == null || this.content.equals(otherGoal.content);

            return messageEqual && headerEqual && contentEqual;
        } else {
            return super.equals(obj);
        }
    }
}
