package org.uu.nl.goldenagents.decompose.expertise.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.uu.nl.goldenagents.decompose.expertise.ExpertiseGraphHandler;
import org.uu.nl.goldenagents.util.CollectionUtils;
import org.uu.nl.net2apl.core.agent.AgentID;

/**
 * Expertise graph for Broker Agent
 * 
 * @author Golden Agents Group, Utrecht University
 */
public class ExpertiseGraph<P> {

	private final Set<ExpertiseNode<P>> nodes = new HashSet<>();
	private final Set<ExpertiseEdge<P>> edges = new HashSet<>();

	public ExpertiseGraph() {}
	
	public ExpertiseGraph(Set<ExpertiseNode<P>> nodes, Set<ExpertiseEdge<P>> edges) {
		super();
		this.nodes.addAll(nodes);
		this.edges.addAll(edges);
	}
	
	public void analyze() {
		ExpertiseGraphHandler<P> handler = new ExpertiseGraphHandler<>(this);
		handler.fullAnalysis();
	}

	public void addNode(ExpertiseNode<P> node) {
		this.nodes.add(node);
	}

	public void addEdge(ExpertiseEdge<P> edge) {
		this.edges.add(edge);
	}

	/**
	 * Returns all edges of the given agent
	 * @param agentID
	 * @return
	 */
	public Set<ExpertiseEdge<P>> getEdgesOf(AgentID agentID) {
		return this.edges.stream().filter(edge -> 
		edge.getSource().equals(agentID))
				.collect(Collectors.toSet());
	}

	/**
	 * Return edges of the given agent only for the given property
	 * @param agentID
	 * @param property
	 * @return
	 */
	public Set<ExpertiseEdge<P>> getEdgesOf(AgentID agentID, P property) {
		return this.edges.stream().filter(edge -> 
		edge.getSource().equals(agentID) && 
		edge.getSourceProperty().equals(property))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns nodes that are assignments for the given property
	 * @param property
	 * @return
	 */
	public Set<ExpertiseNode<P>> getCapables(P property) {
		return this.nodes.stream().filter(node -> node.getCount(property) > 0)
				.collect(Collectors.toSet());
	}

	/**
	 * 
	 * @param agentID
	 * @return
	 */
	public ExpertiseNode<P> getNode(AgentID agentID) {
		Set<ExpertiseNode<P>> temp = this.nodes.stream()
				.filter(node -> node.getId().equals(agentID))
				.collect(Collectors.toSet());
		if(temp.isEmpty()) {
			return null;
		} else {
			return temp.iterator().next();
		}
	}

	/**
	 * Returns the node that has the highest cooperativeness value for the given property
	 * @param property
	 * @return the node that has the highest cooperativeness value for the given property
	 */
	public ExpertiseNode<P> nodeOfMaxCPT(P property) {
		ExpertiseNode<P> maxNode = getCapables(property).stream().max(
				Comparator.comparing(node -> node.getStats().get(property).cpt)).orElseThrow();
		return maxNode;
	}

	/**
	 * Returns the node that has the highest versatility value for the given property
	 * @param property
	 * @return the node that has the highest versatility value for the given property
	 */
	public ExpertiseNode<P> nodeOfMaxVST(P property) {
		ExpertiseNode<P> maxNode = getCapables(property).stream().max(
				Comparator.comparing(node -> node.getStats().get(property).vst)).orElseThrow();
		return maxNode;
	}

	/**
	 * Returns the node that has the least support need value for the given property
	 * @param property
	 * @return the node that has the least support need value for the given property
	 */
	public ExpertiseNode<P> nodeOfLeastSPT(P property) {
		ExpertiseNode<P> minNode = getCapables(property).stream().min(
				Comparator.comparing(node -> node.getStats().get(property).spt)).orElseThrow();
		return minNode;
	}

	/**
	 * Returns the node that has the highest number of entities for the given property
	 * @param property
	 * @return the node that has the highest number of entities for the given property
	 */
	public ExpertiseNode<P> nodeOfHIP(P property) {
		ExpertiseNode<P> maxNode = getCapables(property).stream().max(
				Comparator.comparing(node -> node.getCount(property))).orElseThrow();
		return maxNode;
	}

	/**
	 * Returns the edge that has the highest value and connected to 
	 * the given node (assignment) for the given given property.
	 * If the node does not have an edge for the given property, 
	 * throws an error.
	 * @param assignment assignment of the node
	 * @param property property of the edge
	 * @return edge with the highest value
	 * @throws NoSuchElementException
	 */
	public ExpertiseEdge<P> edgeOfMaxValue(Assignment<P> assignment, P property) throws NoSuchElementException {
		Set<ExpertiseEdge<P>> edgesOfNode = getEdgesOf(assignment.getAgent(), assignment.getProperty());
		return edgesOfNode.stream()
				.filter(e -> e.getTargetProperty().equals(property))
				.max(Comparator.comparing(ExpertiseEdge::getValue)).orElseThrow();
	}
	
	/**
	 * Returns the list of edges between the nodes of the given assignments
	 * @param list assignments
	 * @return the list of edges between the nodes of the given assignments
	 */
	public Set<ExpertiseEdge<P>> getAssignmentEdges(List<Assignment<P>> list) {
		return this.edges.stream().filter(e -> list.contains(e.getSourceAssignment()) 
				&& list.contains(e.getTargetAssignment())).collect(Collectors.toSet());
	}
	
	public Set<AgentID> getAgents() {
		return getNodes().stream().map(node -> node.getId()).collect(Collectors.toSet());
	}

	public Set<ExpertiseNode<P>> getNodes() {
		return nodes;
	}

	public Set<ExpertiseEdge<P>> getEdges() {
		return edges;
	}
	
	/**
	 * Calculates average of the edge values for a given node and edge type
	 */
	private double getAverageOfEdges(ExpertiseNode<P> node, ExpertiseEdge.Type type) {
		Set<ExpertiseEdge<P>> edgesOfNode = getEdgesOf(node.getId());
		double value = edgesOfNode.stream().filter(e -> e.getType() == type)
				.mapToDouble(e -> e.getValue()).sum() / edgesOfNode.stream()
				.filter(e -> e.getType() == type && e.getValue() != 0).count();
		return value;
	}

	/**
	 * Table format of the expertise graph
	 * @return table format of the expertise graph
	 */
	public String tableFormat() {
		
		StringBuilder sb = new StringBuilder();
		String format = "%-10s | %5s | %10s | %10s | %10s | %10s | %10s | %10s | %10s | %10s | %10s%n";
		String header = String.format(format, "Agent ID", "#Caps", "Tot Count", "Avg Count", 
				"Avg J-edge", "Avg C-edge", "Avg S-edge", "Degree", "Avg CPT", "Avg VST", "Avg SPT");
		int length = header.length();
		sb.append(header + "=".repeat(length) + "\n");
		format = "%-10s | %5d | %10d | %10.2f | %10.2f | %10.2f | %10.2f | %10.2f | %10.2f | %10.2f | %10.2f%n";
		int cap = 0, total = 0;
		double j_tot = 0, c_tot = 0, s_tot = 0, degree = 0, avc = 0;
		for(ExpertiseNode<P> node : getNodes()) {
			int ntot =  node.getTotalCount();
			int ncap = node.getStats().size();
			double ndeg = node.getDegree();
			double cpt = node.getStats().values().stream().mapToDouble(stat -> stat.cpt).sum();
			double vst = node.getStats().values().stream().mapToDouble(stat -> stat.vst).sum();
			double spt = node.getStats().values().stream().mapToDouble(stat -> stat.spt).sum();

			double j_edge = getAverageOfEdges(node, ExpertiseEdge.Type.J);
			double c_edge = getAverageOfEdges(node, ExpertiseEdge.Type.C);
			double s_edge = getAverageOfEdges(node, ExpertiseEdge.Type.S);

			sb.append(String.format(format, node.getId(), ncap, ntot, 
					(1.0 * ntot / ncap), j_edge, c_edge, s_edge, ndeg, 
					cpt / ncap, vst / ncap, spt / ncap));
			cap += ncap;
			total += ntot;
			degree += ndeg;
			j_tot += j_edge;
			c_tot += c_edge;
			s_tot += s_edge;
			avc += 1.0 * ntot / ncap;
		}
		sb.append("-".repeat(length) + "\n");
		//sb.append(String.format(format, "Total", cap, total, avc, j_tot, c_tot, s_tot, degree));
		int n = nodes.size();
		format = "%-10s | %5.2f | %10.2f | %10.2f | %10.2f | %10.2f | %10.2f | %10.2f%n";
		sb.append(String.format(format, "Average", 1.0*cap/n, 1.0*total/n, avc/n,
				j_tot/n, c_tot/n, s_tot/n, degree/n));
		return sb.toString();
	}
	
	public String summarizeSize() {
		return String.format("Expertise graph has %d nodes and %d edges.", 
				nodes.size(), edges.size());
	}

	public String presentNodes() {
		StringBuilder sb = new StringBuilder();
		for(ExpertiseNode<P> node : nodes) {
			sb.append("\n->" + node.toString() + "\n");
			node.getStats().forEach((property,stat) -> {
				sb.append(String.format("--> %s  ~cpt=%.2f  ~spt=%.2f  ~vst=%.2f \n", 
						property, stat.cpt, stat.spt, stat.vst));
				getEdgesOf(node.getId(), property).stream()
				.forEach(edge -> sb.append(
						edge.getTargetProperty() + "#" + edge.getTarget() + ": " + edge.getValue() + ", "));
				sb.append("\n");
			});
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "ExpertGraph [nodes=" + CollectionUtils.printify(this.nodes, System.lineSeparator()) + 
				", edges=" + CollectionUtils.printify(this.edges, System.lineSeparator()) + "]";
	}
}
