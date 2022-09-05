package org.uu.nl.goldenagents.decompose.expertise.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.uu.nl.goldenagents.agent.plan.broker.splitquery.MissingExpertException;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.exceptions.BadQueryException;
import org.uu.nl.goldenagents.sparql.QueryInfo;
import org.uu.nl.goldenagents.sparql.TripleInfo;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

/**
 * Query analyzer for expertise-based query decomposition.
 * It has the Expertise info of all DB Agents and
 * thus can compare matching sources to select better one(s).
 * 
 * @author Golden Agents Group, Utrecht University
 */
public class SourceMatcher {

	protected final Map<AgentID, DbAgentExpertise> expertises;
	protected Map<String, Set<TripleInfo>> keyConstraints;
	private Map<String, Map<AgentID, Integer>> importanceCount;
	protected static final Loggable LOGGER = Platform.getLogger();

	public SourceMatcher(Map<AgentID, DbAgentExpertise> expertises) {
		super();
		this.expertises = expertises;
	}

	/**
	 * Analyzes the query and adds extra information about selected sources, 
	 * which is required by the expertise based query decomposition, into the query info.
	 * If there is at least one triple that cannot be assigned to any DB Agent, then
	 * throws an exception.
	 * @param qInfo QueryInfo
	 * @throws MissingExpertException Exception that shows triples missing an expert
	 */
	public void analyzeQuery(QueryInfo qInfo) throws MissingExpertException, BadQueryException  {
		this.keyConstraints = new HashMap<>();
		this.importanceCount = new HashMap<>();
		findMatchingAgents(qInfo.getTriples());
		checkTriples(qInfo.getTriples());
		findKeyConstraints(qInfo.getVarTripleInfoMapping());
		selectSources(qInfo.getVarTripleInfoMapping());
	}

	/**
	 * Finds agents that can answer triples (constraints) for each one
	 * @param triples Set of TripleInfos to be checked
	 */
	private void findMatchingAgents(Set<TripleInfo> triples) {
		for(TripleInfo ti : triples) {
			this.expertises.forEach((aID, expertise) -> {
				if(isCapable(expertise.getCapabilities(), ti)) {
					ti.getPossibleSources().add(aID);
				}
			});
		}
	}

	/**
	 * Checks whether the given expertise of DB agent is able to answer the given triple (constraint).
	 * @param caps Capabilities of DB Agent to be checked
	 * @param ti TripleInfo of the constraint to be checked 
	 * @return true if the DB agent can answer, otherwise false
	 */
	private boolean isCapable(List<String> caps, TripleInfo ti){
		//Can a triple have more than one concept of the general ontology in it?
		for(String concept : ti.getOntologicalConcepts()) {
			if(!caps.contains(concept)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if every triple has at least one available expert to assign 
	 * and at least one concept that is mapped to general ontology.
	 * @param triples set of triples to check
	 * @throws MissingExpertException	exception of triples missing expert. 
	 * @throws BadQueryException exception of triples missing mapping (concepts from the general ontology)
	 */
	private void checkTriples(Set<TripleInfo> triples) throws MissingExpertException, BadQueryException {
		Set<TripleInfo> unavailableTriples = new HashSet<>();
		Set<TripleInfo> unmappedTriples = new HashSet<>();
		for(TripleInfo ti : triples) {
			if(ti.getPossibleSources().size() == 0) {
				unavailableTriples.add(ti);
			}
			if(ti.getOntologicalConcepts().isEmpty()) {
				unmappedTriples.add(ti);
			}
		}
		if(!unmappedTriples.isEmpty()) {
			throw constructBadQueryException(unmappedTriples);
		}
		if(!unavailableTriples.isEmpty()) {
			throw constructMissingExpertException(unavailableTriples);
		}
	}


	/**
	 * Finds key constraints in the query. 
	 * Key constraint means a constraint that can be answered by only one agent.
	 * @param varToTriplesMapping mapping from variable names to the map of triples to the sets of agents
	 */
	private void findKeyConstraints(Map<String, Set<TripleInfo>> varToTriplesMapping) {

		varToTriplesMapping.forEach((var, set) -> {
			Set<TripleInfo> keytTiSet = new LinkedHashSet<>();
			Map<AgentID, Integer> freqOfSources = new HashMap<>();
			for(TripleInfo ti : set) {
				if(ti.getPossibleSources().size() == 1) {
					keytTiSet.add(ti);
					AgentID aID = ti.getPossibleSources().iterator().next();
					if (freqOfSources.containsKey(aID)) {
						freqOfSources.put(aID, (freqOfSources.get(aID) + 1));
					}
					else {
						freqOfSources.put(aID, 1);
					}
				}
			}
			this.keyConstraints.put(var, keytTiSet);
			this.importanceCount.put(var, freqOfSources);
		});
		printInfo(varToTriplesMapping);
	}

	protected void selectSources(Map<String, Set<TripleInfo>> varToTriplesMapping) {
		varToTriplesMapping.forEach((var, set) -> {
			//if there are some key constraints for the variable
			if(this.keyConstraints.containsKey(var)) {
				Set<TripleInfo> nonKeyConstraints = new LinkedHashSet<>(set);
				nonKeyConstraints.removeAll(this.keyConstraints.get(var));
				selectPerfectMatch(var, nonKeyConstraints);
			}
			/* If there is not any key constraint for the variable, 
			 * specific sources will not be selected for the constraints of the variable.
			 * Later, the constraints of the variable will be send to all matching data sources.
			 * TODO Can we improve the logic here?
			 */
		});
		this.keyConstraints.forEach((var, set) -> {
			LOGGER.log(this.getClass(), Level.FINE, "For " + var + " :");
			this.importanceCount.get(var).forEach((aID, count) -> {
				LOGGER.log(this.getClass(), Level.FINE, aID + " -> " + count + " different triples");
			});
		});
	}
	
	/**
	 * For a given triple (constraint), selects the perfect match (if there is any)
	 * from the matching sources as the source to send the constraint.
	 * 
	 * @param var
	 * @param nonKeyConstraints
	 */
	private void selectPerfectMatch(String var, Set<TripleInfo> nonKeyConstraints) {
		//Iterate over triples which are not key
		for(TripleInfo tiNot : nonKeyConstraints) {
			//TODO property paths need better implementation
			if(tiNot.isSimplePath()) {
				List<SourceMatch> perfectMatchings = new ArrayList<>();
				//Can a triple have more than one concept of the general ontology in it?
				for(String nonKeyConcept : tiNot.getOntologicalConcepts()) {
					List<SourceMatch> matchings = findMatchings(var, tiNot, nonKeyConcept);
					perfectMatchings.addAll(
							matchings.stream().filter(p -> p.isPerfect()).collect(Collectors.toList()));
					if(!perfectMatchings.isEmpty()) {
						perfectMatchings.sort(SourceMatch.BY_NUMBER_OF_KEY_CONCEPTS);
						tiNot.setChosenSource(perfectMatchings.get(0).getMatchingAgent());
					}
				}
			}
		}
	}
	
	/**
	 * If a given TripleInfo shares the same variable with one of 
	 * the key constraints (which has a single source option to send),
	 * adds the source of the of the key constraint as a match.  
	 *  
	 * @param var
	 * @param tiNot
	 * @param nonKeyConcept
	 * @return list of sources that are the only source for of the constraints sharing the same variable
	 */
	private List<SourceMatch> findMatchings(String var, TripleInfo tiNot, String nonKeyConcept) {
		List<SourceMatch> matchings = new ArrayList<>();
		//iterate over triples which are key
		for(TripleInfo tiKey : this.keyConstraints.get(var)) {
			//Get the agent ID of agent that is the only source for the key constraint
			AgentID aID = tiKey.getPossibleSources().iterator().next();
			//check whether the agent that answers the key constraint also can answer the non-key constraint
			if(tiNot.getPossibleSources().contains(aID)) {
				float keyConceptRatio = this.expertises.get(aID)
						.getStarCombinationRatio(tiKey.getOntologicalConcepts().iterator().next(), nonKeyConcept);
				int countOfNonKeyConcept = this.expertises.get(aID).getCountOfConcept(nonKeyConcept);
				SourceMatch mInfo = new SourceMatch(aID, this.importanceCount
						.get(var).get(aID), keyConceptRatio, countOfNonKeyConcept);
				matchings.add(mInfo);
			}
		}
		return matchings;
	}
	
	private BadQueryException constructBadQueryException(Set<TripleInfo> unmappedTriples) {
		String problematicTripleList = 
				unmappedTriples.stream().map(TripleInfo::toString).collect(Collectors.joining(";"));
		
		return new BadQueryException(
				"The following triple pattern could not be mapped to any database," + 
						"as none of used concepts occur in any mapping:\n" + problematicTripleList);
	}

	/**
	 * Construct an error message for when no suitable experts can be found for one or more specific triples in the
	 * given user query
	 * @param	unavailableTriples set of all triples that could not be assigned to any expert
	 * @return 	A MissingExpertException
	 */
	private MissingExpertException constructMissingExpertException(Set<TripleInfo> unavailableTriples) {
		String problematicTripleList = 
				unavailableTriples.stream().map(TripleInfo::toString).collect(Collectors.joining(";"));

		return new MissingExpertException(
				String.format("No expert could be found for the following triples: \n%s\nFor this reason, the query" +
						"could not be answered", problematicTripleList));
	}

	public void printInfo(Map<String, Set<TripleInfo>> varToTriplesMapping) {

		StringBuilder sb = new StringBuilder();
		sb.append("Triples of variables and the data sources that can answer triples:").append(System.lineSeparator());
		varToTriplesMapping.forEach((var, set) -> {
			sb.append(var);
			for(TripleInfo ti : set) {
				sb.append(ti.toString() + ": " + ti.getPossibleSources().toString()).append(System.lineSeparator());
			}
		});

		sb.append("Key constraints in the query:").append(System.lineSeparator());
		keyConstraints.forEach((var, set) -> {
			sb.append(var).append(System.lineSeparator());
			for(TripleInfo ti : set) {			
				sb.append(ti.toString() + ": " + ti.getPossibleSources().toString()).append(System.lineSeparator());
			};
		});
		LOGGER.log(this.getClass(), Level.FINE, sb.toString());
	}

}
