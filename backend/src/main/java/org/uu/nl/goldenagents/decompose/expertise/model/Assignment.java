package org.uu.nl.goldenagents.decompose.expertise.model;

import org.uu.nl.net2apl.core.agent.AgentID;

public class Assignment<P> {
    
	public final AgentID agentID;
    public final P property;

    public Assignment (AgentID agentID, P property) {
		this.agentID = agentID;
		this.property = property;
	}
    
	public AgentID getAgent() {
		return agentID;
	}

	public P getProperty() {
		return property;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentID == null) ? 0 : agentID.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
    public boolean equals(Object other) {
		if (other instanceof Assignment) {
            Assignment<?> otherAssignment = (Assignment<?>) other;
            return 
            ((  this.agentID == otherAssignment.agentID ||
                ( this.agentID != null && otherAssignment.agentID != null &&
                  this.agentID.equals(otherAssignment.agentID))) &&
             (  this.property == otherAssignment.property ||
                ( this.property != null && otherAssignment.property != null &&
                  this.property.equals(otherAssignment.property))) );
        }
        return false;
    }

	@Override
	public String toString() {
		return "Assignment [Agent Name=" + agentID.getName().getFragment() + ", property(s)=" + property + "]";
	}
}