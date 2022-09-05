package org.uu.nl.goldenagents.decompose.expertise.model;

import java.util.HashMap;
import java.util.Map;

import org.uu.nl.net2apl.core.agent.AgentID;

public class ExpertiseNode<P> {

	private final AgentID aid;
	private final Map<P, Stat> stats;
	private double degree = 0;

	public ExpertiseNode(AgentID aid) {
		this.aid = aid;
		this.stats = new HashMap<>();
	}
	
	public AgentID getId() {
		return aid;
	}

	public void setCount(P property, int count) {
		if(!stats.containsKey(property)) {
			stats.put(property, new Stat());
		}
		stats.get(property).count = count;
	}
	
	public int getCount(P property) {
		if(stats.containsKey(property)) {
			return stats.get(property).count;
		}
		return 0;
	}
	
	public int getTotalCount() {
		int total = stats.values().stream().mapToInt(stat -> stat.count).sum();
		return total;
	}

	public Map<P,Stat> getStats() {
		return stats;
	}

	public double getDegree() {
		return degree;
	}

	public void setDegree(double degree) {
		this.degree = degree;
	}

	@Override
	public String toString() {
		return "Node [id=" + aid + ", degree=" + degree + ", stats=" + stats + "]";
	}
	
	/**
	 * A class to store node values,
	 * such as cpt, spt, vst, count (capability performance), 
	 * count of how many times the node is consulted
	 * @author cankurtan
	 *
	 */
	 public class Stat {
		
		 /** Capability (property) performance, i.e., the number of results for a property*/
		public int count; 
		/** The number of times the agent is assigned to the capability */
		public int consulted;
		public double cpt, spt, vst;
		
		public Stat() {}
		
		/**
		 * Returns CPT value normalized by the count
		 * @return CPT value normalized by the count
		 */
		public double ncpt() {
			return cpt/count;
		}
		
		/**
		 * Returns VST value normalized by the count
		 * @return VST value normalized by the count
		 */
		public double nvst() {
			return vst/count;
		}
		
		@Override
		public String toString() {
			return String.format("[count=%d, cpt=%.2f, spt=%.2f, vst=%.2f]", count, cpt, spt, vst);
		}
	}
}
