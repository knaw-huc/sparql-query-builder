package org.uu.nl.goldenagents.decompose.expertise.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.decompose.expertise.ExpertiseGraphHandler;
import org.uu.nl.goldenagents.decompose.expertise.model.Assignment;
import org.uu.nl.goldenagents.decompose.expertise.model.ExpertiseEdge;
import org.uu.nl.goldenagents.decompose.expertise.model.ExpertiseGraph;
import org.uu.nl.goldenagents.decompose.expertise.model.ExpertiseNode;
import org.uu.nl.goldenagents.sparql.TripleInfo;
import org.uu.nl.goldenagents.util.CollectionUtils;
import org.uu.nl.goldenagents.util.CollectionUtils.SortingOrder;
import org.uu.nl.net2apl.core.agent.AgentID;

/**
 * A class that provides many functions using {@link ExpertiseGraph} 
 * to find matching sources for a given query.
 * 
 * @author Golden Agents Group, Utrecht University
 */
public class ComplexSourceMatcher extends SourceMatcher {
	
	private final ExpertiseGraph<String> expertiseGraph;

	public ComplexSourceMatcher(ExpertiseGraph<String> expertiseGraph, Map<AgentID, DbAgentExpertise> experts) {
		super(experts);
		this.expertiseGraph = expertiseGraph;
	}

	@Override
	protected void selectSources(Map<String, Set<TripleInfo>> varToTriplesMapping) {
		varToTriplesMapping.forEach((var, set) -> {
			for(TripleInfo ti : set) {
				/*TODO Currently, the most cooperative ones are assigned but 
				 * this can be altered with any of the other assignment functions */
				assignMostConnective(ti);
			}
		});
	}
	
	/**
	 * This function starts from one triple patter and assigns the source 
	 * that has the highest cooperativeness value for the triple pattern.
	 * For the remaining triple patterns, it selects the sources based on 
	 * their edge value with the initial assignment. 
	 * It is hard to select the initial assignment and therefore, it does
	 * the same operation by starting from each of the assignments.
	 * Finally, it selects the set of assignments that achieves highest
	 * expected performance (sum of minimum and maximum number of expected results).
	 * P.S.: This solution is not a part of ECAI paper, hopefully will be a part of a new paper
	 * @param triples
	 */
	private void assignSTARTeam(Set<TripleInfo> triples) {
		int highest = 0;
		Map<TripleInfo, Set<Assignment<String>>>  selectedAssignments = null;
		for(TripleInfo ti : triples) {
			for(String concept : ti.getOntologicalConcepts()) {
				ExpertiseNode<String> star = expertiseGraph.getNodes().stream()
						.filter(node -> ti.getPossibleSources().contains(node.getId()))
						.max(Comparator.comparing(node -> node.getStats().get(concept).cpt)).orElseThrow();
				Assignment<String> starAssignment = new Assignment<String>(star.getId(), concept);
				Map<TripleInfo, Set<Assignment<String>>> assignmentMap = selectAssignmentsForStar(triples, ti, starAssignment);
				List<Assignment<String>> assignments = new ArrayList<>();
				assignmentMap.values().stream().forEach(set -> assignments.addAll(set));
				int max = max(expertiseGraph, assignments);
				int min = min(expertiseGraph, assignments);
				if(max + min > highest) {
					highest = max + min;
					selectedAssignments = assignmentMap;
				}
			}
		}
		selectedAssignments.forEach((k,v) -> {
			v.forEach(assignment -> k.setChosenSource(assignment.agentID));
		});
	}
	
	/**
	 * For the given triples, finds the sources that have 
	 * the highest edge values with the given assignment
	 * @param triples triple info that the sources will be selected
	 * @param starTriple triple that the source is selected (star)
	 * @param starAssignment assignment of the selected source
	 * @return mapping from triples to selected sources
	 */
	private Map<TripleInfo, Set<Assignment<String>>> selectAssignmentsForStar(
			Set<TripleInfo> triples, TripleInfo starTriple, Assignment<String> starAssignment) {
		Map<TripleInfo, Set<Assignment<String>>> assignments = new HashMap<>();
		for(TripleInfo ti : triples) {
			Set<Assignment<String>> set = new HashSet<>();
			assignments.put(ti, set);
			for(String concept : ti.getOntologicalConcepts()) {
				if(ti.equals(starTriple) && concept.equals(starAssignment.property)) {
					set.add(starAssignment);
				} else {
					ExpertiseEdge<String> edge = expertiseGraph.edgeOfMaxValue(starAssignment, concept);
					set.add(edge.getTargetAssignment());
				}
			}
		}
		return assignments;
	}
	

	/**
	 * Assigns the source that provides the highest number of entities for 
	 * the given triple pattern
	 * @param ti
	 */
	private void assignHighestPerforming(TripleInfo ti) {
		for(String concept : ti.getOntologicalConcepts()) {
			ExpertiseNode<String> selected = expertiseGraph.getNodes().stream()
				.filter(node -> ti.getPossibleSources().contains(node.getId()))
				.max(Comparator.comparing(node -> node.getCount(concept))).orElseThrow();
			ti.setChosenSource(selected.getId());
		}
	}
	
	/**
	 * Assigns the source that provides the most occurring entities (in the other sources) 
	 * for the given triple pattern. That means the entities of this source for the given triple pattern
	 * overlap most with the entities of other sources
	 * @param ti
	 */
	private void assignMostConnective(TripleInfo ti) {
		for(String concept : ti.getOntologicalConcepts()) {
			ExpertiseNode<String> selected = expertiseGraph.getNodes().stream()
				.filter(node -> ti.getPossibleSources().contains(node.getId()))
				.max(Comparator.comparing(node -> node.getStats().get(concept).cpt)).orElseThrow();
			ti.setChosenSource(selected.getId());
		}
	}
	
	/**
	 * Assigns the source that provides the most occurring entities (in itself) for 
	 * the given triple pattern. That means the entities of this source for the given triple pattern
	 * overlap most with the entities of itself for other triple patterns 
	 * @param ti
	 */
	private void assignMostVersatile(TripleInfo ti) {
		for(String concept : ti.getOntologicalConcepts()) {
			ExpertiseNode<String> selected = expertiseGraph.getNodes().stream()
				.filter(node -> ti.getPossibleSources().contains(node.getId()))
				.max(Comparator.comparing(node -> node.getStats().get(concept).vst)).orElseThrow();
			ti.setChosenSource(selected.getId());
		}
	}
	
	/**
	 * Finds the maximum expected number of results that the given assignments can produce
	 * @param <T> 	Type of Capability Identifier
	 * @param assignments	list of assignments that shows which agent is assigned to which property
	 * @return		the maximum expected number of results
	 */
	private <T> int max(ExpertiseGraph<T> expertiseGraph, List<Assignment<T>> assignments) {
		int max = Integer.MAX_VALUE;
		Set<ExpertiseEdge<T>> edges = expertiseGraph.getAssignmentEdges(assignments);
		/*
		 * Take a different assignment as the pivot at each turn 
		 * TODO this function is inefficient for bidirectional edges because each edge is visited two times.
		 */
		outer: for(Assignment<T> pivot : assignments) {
			int totalEdgeValue = Integer.MAX_VALUE;	
			int knownEdges = 0;		//Number of intersections (edges with known count values)
			int unknownEdges = 0;	//Number of edges without count value
			//TODO Iterate over edges instead of the assignments
			/* Iterate over all the other nodes (matches) */
			for(Assignment<T> target : assignments) {
				/* Skip itself */
				if(pivot.equals(target)) {
					continue;
				}
				int edgeValue = getEdgeValue(edges, pivot, target);
				/* If the edge does not have a known values, it takes the minimum of node values as the edge value */
				if(edgeValue < 0) {
					LOGGER.log(this.getClass(), Level.FINE, "Edge value is unknown");
					/*
					 * The number of entities that the data sources can provide for the property
					 * TODO Probabilistic model?
					 */
					int pivotCount = expertiseGraph.getNode(pivot.getAgent()).getCount(pivot.getProperty());
					int targetCount = expertiseGraph.getNode(target.getAgent()).getCount(target.getProperty());
					/* It can be at most the minimum of their count values */
					edgeValue = Math.min(pivotCount, targetCount);
					unknownEdges++;
				}
				/* The edge of value 0 means they do not intersect */
				else if(edgeValue == 0) {
					max = 0;
					break outer;
				}
				else {
					knownEdges++;
				}
				totalEdgeValue = Math.min(edgeValue, totalEdgeValue);
			}
			LOGGER.log(this.getClass(), Level.FINE, String.format("Maximum number of results is %d for assignment %s "
					+ "based on %d known and %d unknown edges!",
					totalEdgeValue, pivot, knownEdges, unknownEdges));
			max = Math.min(max, totalEdgeValue);
		}
		return max;
	}
	
	/**
	 * Finds the minimum expected number of results that the given assignments can produce
	 * @param <T> 	Type of Capability Identifier
	 * @param 	graph	expertise graph
	 * @param 	assignments list of assignments that shows which agent is assigned to which property
	 * @return 	the minimum expected number of results
	 */
	private <T> int min(ExpertiseGraph<T> graph, List<Assignment<T>> assignments) {
		
		int[] results = new int[assignments.size()];
		Arrays.fill(results, -1);//Fill the array with the unknown state value
		boolean isGuaranteed = false;
		int min = 0;
		Set<ExpertiseEdge<T>> edges = graph.getAssignmentEdges(assignments);
		OptionalInt minEdgeValue = edges.stream().mapToInt(ExpertiseEdge::getValue).min();
		int minPossibleValue = Integer.MAX_VALUE;
		if(minEdgeValue.isPresent()) {
			minPossibleValue = minEdgeValue.getAsInt();
		}
		/* Take a different assignment as the pivot at each turn */
		outer: for(Assignment<T> pivot : assignments) {
			int totalEdgeValue = 0; 
			int knownEdges = 0; //Number of intersections (edges with known count values)
			int unknownEdges = 0; //Number of edges without a known edge value
			
			//Value of any edge of the pivot assignment cannot exceed its own value
			minPossibleValue = Math.min(minPossibleValue, graph.getNode(pivot.getAgent()).getCount(pivot.getProperty()));
			/* Iterate over all the other assignments */
			for(Assignment<T> target : assignments) {
				/* Skip itself */
				if(pivot.equals(target)) {
					continue;
				}
				/*
				 * Get the edge between the source assignment and the target assignment.
				 * Even though summing function is used, there is expected to be only one edge.
				 */
				int edgeValue = getEdgeValue(edges, pivot, target);
				/* Counts the edges that do not have a value */
				if(edgeValue < 0) {
					LOGGER.log(this.getClass(), Level.FINE, "Edge value is unknown");
					unknownEdges++;
				}
				/* If the edge has value of 0, this means they do not intersect */
				else if(edgeValue == 0) {
					isGuaranteed = true;
					min = 0;
					break outer;
				}
				/* If the value is known add it */
				else {
					totalEdgeValue += edgeValue;
					knownEdges++;
				}
			}
			LOGGER.log(this.getClass(), Level.FINE, String.format("Minimum number of results is %d for assignment %s "
					+ "based on %d known and %d unknown edges!",
					totalEdgeValue, pivot, knownEdges, unknownEdges));
			/* The number of entities that the data source can provide for the property */
			int pivotCount = graph.getNode(pivot.getAgent()).getCount(pivot.getProperty());
			
			/*
			 * The minimum number of entities that intersect (common) after collecting data from multiple sources
			 * Min intersection = total of edge values - (number of intersections - 1) * pivot count
			 * For example, edges are {<s1,s2>,<s1,s3>,<s1,s4>} and s1 is the pivot.
			 * Source is s1, number of intersections (edges) is 3.
			 * If s1 count is 10 and the total of intersections is 24 (9+8+7) 
			 * then the minimum of intersection value is 4 (24-10*(3-1)).
			 */
			totalEdgeValue -= (knownEdges - 1) * pivotCount;
			int result = Math.max(0, totalEdgeValue);
			/* If all of the edges have a known value, then this guarantees the minimum value */
			if(unknownEdges == 0) {
				isGuaranteed = true;
				min = Math.max(min, result);
			}
			/* If there is an edge without a known value, then store the result in a separate array
			 * based on the number of unknown edges */
			results[unknownEdges] = Math.max(results[unknownEdges], result);
		}
		/* If the estimated value is not guaranteed (there is not a node whose all edges have a known value),
		 * then take the estimated value that is estimated with the least unknown edge values */
		if(!isGuaranteed) {
			min = takeMostCertain(results, minPossibleValue);
		}
		return min;
	}
	
	private <T> int getEdgeValue(Set<ExpertiseEdge<T>> edges, Assignment<T> pivot, Assignment<T> target) { 
		List<ExpertiseEdge<T>> edgeBetween = edges.stream()
				.filter(e -> e.getSourceAssignment().equals(pivot) &&
					e.getTargetAssignment().equals(target))
				.collect(Collectors.toList());
		if(edgeBetween.size() == 1) {
			return edgeBetween.get(0).getValue();
		} else if (edgeBetween.size() > 1) {
			System.err.println(String.format("More than expected edges: %s", edgeBetween));
			System.exit(0);
		}
		return -1;
	}
	
	protected int takeMostCertain(int[] results, int minPossibleValue) {
		int min = 0;
		for (int value : results) {
			if (value != -1) {
				min = Math.min(minPossibleValue, value);
				break;
			}
		}
		return min;
	}
	
	/**
	 * Improves given assignments based on the cooperativeness values of the assignments.
	 * If it changes any of the assignments, returns the list of improved assignments.
	 * Otherwise, it returns null. 
	 * This function can be called multiple times to improve iteratively. 
	 * @param assignments list of assignments
	 * @return the list of improved assignments if any improvement has been done; otherwise, null
	 */
	private List<Assignment<String>> improve(final List<Assignment<String>> assignments) {
		Map<Assignment<String>, Double> cptValues = getCPTValues(assignments);
		List<Entry<Assignment<String>, Double>> sorted = 
				CollectionUtils.sortByValue(cptValues, SortingOrder.ASCENDING);
		double avg = sorted.stream().mapToDouble(e -> e.getValue()).sum() / sorted.size();
		List<Assignment<String>> improved = new ArrayList<>(assignments);
		boolean check = false;
		for(Entry<Assignment<String>,Double> entry : sorted) {
			if(entry.getValue() < avg) {
				Assignment<String> pair = entry.getKey();
				Assignment<String> alt = findAlt(assignments, avg, pair);
				if (alt != null ) {
					improved.remove(entry.getKey());
					improved.add(alt);
					check = true;
				}
			}
		}
		if(check && isPerformingBetter(improved, assignments)) {
			return improved;
		}
		return null;
	}
	
	/**
	 * Compares estimated performance of given assignments and returns
	 * true if the improved assignments performs better than
	 * the initial assignments.
	 * @param improved list of assignments after the improvement
	 * @param initial list of initial assignments
	 * @return true if the improved assignments performs better than
	 * the initial assignments.
	 */
	private boolean isPerformingBetter(List<Assignment<String>> improved, 
			List<Assignment<String>> initial) {
		int improvedMin = min(expertiseGraph, improved);
		int improvedMax = max(expertiseGraph, improved);
		
		int initialMin = min(expertiseGraph, initial);
		int initialMax = max(expertiseGraph, initial);
		
		/* This performance comparison can be alternated based on the objective. 
		 * For example, only min number of results can be used 
		 * if max number of results is not important */
		if(improvedMin + improvedMax > initialMin + initialMax) {
			return true;
		}
		return false;
	}

	/**
	 * Calculates cooperativeness between the given list of assignments
	 * @param list assignments
	 * @return cooperativeness values of the assignments
	 */
	private Map<Assignment<String>, Double> getCPTValues(List<Assignment<String>> list) {
		Set<ExpertiseEdge<String>> tEdges = this.expertiseGraph.getAssignmentEdges(list);
		ExpertiseGraphHandler<String> egh = new ExpertiseGraphHandler<>(expertiseGraph);
		Map<Assignment<String>, Double> values = new HashMap<>();
		for(Assignment<String> asn : list) {
			values.put(asn, egh.calculateCPT(tEdges, asn.getAgent(), asn.getProperty()));
		}
		return values;
	}
	
	/**
	 * Finds an alternative assignment that is expected to perform better than the given assignment
	 * (which will be replaced) in the given list of assignment. It finds an alternative assignment 
	 * based on the versatility if the agent (of the assignment) is already assigned or
	 * the cooperativeness values if the agent is not assigned yet.
	 * @param assignments list of assignment
	 * @param avg average cooperativeness of the assignments
	 * @param toReplace assignment to replace
	 * @return alternative assignment if found, null otherwise
	 */
	private Assignment<String> findAlt(List<Assignment<String>> assignments, double avg, 
			Assignment<String> toReplace){

		Set<ExpertiseNode<String>> caps = expertiseGraph.getCapables(toReplace.getProperty());
		caps.removeIf(n -> n.getId().equals(toReplace.getAgent())); //remove itself
		Map<ExpertiseNode<String>, Double> alts = new HashMap<>();
		for(ExpertiseNode<String> n : caps) {
			//If agent is not assigned yet, take cpt value
			if(!isAlreadyAssigned(assignments, n.getId()) && 
					n.getStats().get(toReplace.getProperty()).cpt > avg) {
				alts.put(n, n.getStats().get(toReplace.getProperty()).ncpt());
			} //If agent is already assigned, take vst value
			else if(!isAlreadyAssigned(assignments, n.getId()) && 
					n.getStats().get(toReplace.getProperty()).vst > avg) {
				alts.put(n, n.getStats().get(toReplace.getProperty()).nvst());
			}
		}
		if(!alts.isEmpty()) {
			ExpertiseNode<String> n = CollectionUtils.getKeyOfMaxValue(alts);
			return new Assignment<String>(n.getId(), toReplace.getProperty());
		}
		return null;
	}
	
	/**
	 * Checks if the given agentId has any assignment in the given assignment list
	 * @param assignments list of assignments
	 * @param aid agent ID
	 * @return true if the given agentId has any assignment in the given assignment list
	 */
	private boolean isAlreadyAssigned(List<Assignment<String>> assignments, AgentID aid) {
		return !(assignments.stream().filter(a -> a.getAgent().equals(aid)).count() == 0L);
	}
}
