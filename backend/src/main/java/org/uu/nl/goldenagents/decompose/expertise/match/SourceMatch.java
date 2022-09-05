package org.uu.nl.goldenagents.decompose.expertise.match;

import java.util.Comparator;

import org.uu.nl.net2apl.core.agent.AgentID;

public class SourceMatch {

	private AgentID matchingAgent;
	private int numberOfKeyConcepts;
	private float keyConceptRatio;
	private int countOfNonKeyConcept;
	private final static float PERFECT_RATIO = 1f;
	
	public static final Comparator<SourceMatch> BY_NUMBER_OF_KEY_CONCEPTS = new Comparator<SourceMatch>() {
		public int compare(SourceMatch mInfo1, SourceMatch mInfo2) {
			return mInfo2.getNumberOfKeyConcepts() - mInfo1.getNumberOfKeyConcepts();
		}
	};
	
	public SourceMatch() {}
	
	public SourceMatch(AgentID matchingAgent, int numberOfKeyConcepts, float keyConceptRatio,
			int countOfNonKeyConcept) {
		super();
		this.matchingAgent = matchingAgent;
		this.numberOfKeyConcepts = numberOfKeyConcepts;
		this.keyConceptRatio = keyConceptRatio;
		this.countOfNonKeyConcept = countOfNonKeyConcept;
	}

	public AgentID getMatchingAgent() {
		return matchingAgent;
	}

	public void setMatchingAgent(AgentID matchingAgent) {
		this.matchingAgent = matchingAgent;
	}

	public int getNumberOfKeyConcepts() {
		return numberOfKeyConcepts;
	}

	public void setNumberOfKeyConcepts(int numberOfKeyConcepts) {
		this.numberOfKeyConcepts = numberOfKeyConcepts;
	}

	public float getKeyConceptRatio() {
		return keyConceptRatio;
	}

	public void setKeyConceptRatio(float keyConceptRatio) {
		this.keyConceptRatio = keyConceptRatio;
	}

	public int getCountOfNonKeyConcept() {
		return countOfNonKeyConcept;
	}

	public void setCountOfNonKeyConcept(int countOfNonKeyConcept) {
		this.countOfNonKeyConcept = countOfNonKeyConcept;
	}
	
	/**
	 * if the agent can answer all the combinations of key constraint and the other one, 
	 * then the {@code keyConceptRatio} becomes 1.
	 * This is a perfect matching for the constraint. 
	 * @return true if perfect matching, otherwise false
	 */
	public boolean isPerfect() {
		return keyConceptRatio == PERFECT_RATIO;
	}

	@Override
	public String toString() {
		return "SourceMatch [matchingAgent=" + matchingAgent + ", numberOfKeyConcepts=" + numberOfKeyConcepts
				+ ", keyConceptRatio=" + keyConceptRatio + ", countOfNonKeyConcept=" + countOfNonKeyConcept + "]";
	}
}
