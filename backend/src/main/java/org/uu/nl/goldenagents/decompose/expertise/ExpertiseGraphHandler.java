package org.uu.nl.goldenagents.decompose.expertise;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.uu.nl.goldenagents.decompose.expertise.model.ExpertiseEdge;
import org.uu.nl.goldenagents.decompose.expertise.model.ExpertiseGraph;
import org.uu.nl.goldenagents.decompose.expertise.model.ExpertiseNode;
import org.uu.nl.net2apl.core.agent.AgentID;

public class ExpertiseGraphHandler<P> {
	
	private final ExpertiseGraph<P> eg;
		
	public ExpertiseGraphHandler(ExpertiseGraph<P> eg) {
		this.eg = eg;
	}

	public void fullAnalysis() {
		for(ExpertiseNode<P> n : eg.getNodes()) {
			n.getStats().forEach((property, stats) -> {
				stats.cpt = calculateCPT(null, n.getId(), property);
				stats.spt = calculateSPT(null, n.getId(), property);
				stats.vst = calculateVST(null, n.getId(), property);
			});
		}
	}
	
	public double edgeAverage(Set<ExpertiseEdge<P>> edgesToCalc, 
			AgentID sourceId, P property, ExpertiseEdge.Type ... types) {
		Set<ExpertiseEdge<P>> edges = eg.getEdgesOf(sourceId, property);
		List<ExpertiseEdge.Type> tList = Arrays.asList(types);
		edges.removeIf(e -> !tList.contains(e.getType()));
		if(edgesToCalc != null) {
				edges.retainAll(edgesToCalc);
		}
		if(edges.isEmpty()) {
			return 0;
		}
		double total = edges.stream().mapToDouble(edge -> edge.getValue()).sum();
		return total/edges.size();
	}
	
	public double calculateCPT(Set<ExpertiseEdge<P>> edgesToCalc,
			AgentID sourceId, P property) {
		return edgeAverage(edgesToCalc, sourceId, property, ExpertiseEdge.Type.J);
	}

	public double calculateSPT(Set<ExpertiseEdge<P>> edgesToCalc,
			AgentID sourceId, P property) {
		Set<ExpertiseEdge<P>> edges = eg.getEdgesOf(sourceId, property);
		edges.removeIf(e -> e.getType() != ExpertiseEdge.Type.S);
		if(edgesToCalc != null) {
				edges.retainAll(edgesToCalc);
		}
		if(edges.isEmpty()) {
			return 0;
		}
		double count = eg.getNode(sourceId).getCount(property);
		double total = edges.stream().mapToDouble(edge -> (edge.getValue()-count)).sum();
		return total/edges.size();
	}

	public double calculateVST(Set<ExpertiseEdge<P>> edgesToCalc,
			AgentID sourceId, P property) {
		return edgeAverage(edgesToCalc, sourceId, property, ExpertiseEdge.Type.C);
	}
}
