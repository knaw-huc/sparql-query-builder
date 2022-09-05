package org.uu.nl.goldenagents.decompose.expertise.model;

import org.uu.nl.net2apl.core.agent.AgentID;

public class ExpertiseEdge<P> {		
	
	/** Source Assignment */
	private Assignment<P> sourceAssignment;
	/**	Target Assignment */
	private Assignment<P> targetAssignment;
	/** Edge value */
	private int value = -1;
	private Type type;

	private ExpertiseEdge(Assignment<P> sourceAssignment, Assignment<P> targetAssignment) {
		this.sourceAssignment = sourceAssignment;
		this.targetAssignment = targetAssignment;
	}
	
	public ExpertiseEdge(AgentID sourceAgentID, AgentID targetAgentID, P sourceProperty, P targetProperty) {
		this(new Assignment<>(sourceAgentID, sourceProperty), new Assignment<>(targetAgentID, targetProperty));
		assignType();
	}
	
	private void assignType() {
		if(sourceAssignment.getAgent().equals(targetAssignment.getAgent())) {
			setType(Type.C);
		}
		else if(sourceAssignment.getProperty().equals(targetAssignment.getProperty())) {
			setType(Type.S);
		}
		else {
			setType(Type.J);
		}
	}

	public ExpertiseEdge<P> reverseCopy() {
		ExpertiseEdge<P> e = new ExpertiseEdge<>(targetAssignment.getAgent(), sourceAssignment.getAgent(),
				targetAssignment.getProperty(), sourceAssignment.getProperty());
		e.setValue(value);
		return e;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public AgentID getSource() {
		return sourceAssignment.getAgent();
	}

	public AgentID getTarget() {
		return targetAssignment.getAgent();
	}

	public Assignment<P> getSourceAssignment() {
		return sourceAssignment;
	}

	public P getSourceProperty() {
		return sourceAssignment.getProperty();
	}

	public P getTargetProperty() {
		return targetAssignment.getProperty();
	}

	public Assignment<P> getTargetAssignment() {
		return targetAssignment;
	}
	
	public Type getType() {
		return type;
	}

	private void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Edge [source=" + sourceAssignment + ", target=" + targetAssignment 
				+ ", count=" + value + ", type=" + type +"]";
	}

	public enum Type{
		/** Competent, self-work edge */
		C,
		/** Supportive-work edge */
		S,
		/** Joint-work edge */
		J;
	}
}

