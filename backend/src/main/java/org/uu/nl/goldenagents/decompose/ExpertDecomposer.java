package org.uu.nl.goldenagents.decompose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.uu.nl.goldenagents.agent.plan.broker.splitquery.MissingExpertException;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.decompose.expertise.match.ComplexSourceMatcher;
import org.uu.nl.goldenagents.decompose.expertise.match.SourceMatcher;
import org.uu.nl.goldenagents.decompose.expertise.model.Assignment;
import org.uu.nl.goldenagents.decompose.expertise.model.ExpertiseGraph;
import org.uu.nl.goldenagents.exceptions.BadQueryException;
import org.uu.nl.goldenagents.netmodels.fipa.AgentQuery;
import org.uu.nl.goldenagents.sparql.QueryInfo;
import org.uu.nl.goldenagents.sparql.TripleInfo;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

/**
 *	A class that decomposes a query and generates AgentQuery
 *	by using different levels of expert data.
 *	@author Golden Agents Group, Utrecht University
 */
public class ExpertDecomposer extends QueryDecomposer {

	private static final Loggable LOGGER = Platform.getLogger();

	public ExpertDecomposer(QueryInfo queryInfo) {
		super(queryInfo);
	}

	/**
	 * Creates a sub-query for the given agent.
	 * It assumes queryInfo stores more information that are exposed by the QueryAnalyzer
	 * by analyzing the query and the expertise of DB Agents.
	 * Therefore, it does not take expertise as a parameter.
	 * @param	queryOwner	AgentID of the DB Agent that will answer the query.
	 * @return	AgentQuery that is ready to translate
	 */
	private AgentQuery fromAnalyzedQuery(AgentID queryOwner) {

		AgentQuery aq = initAgentQuery(queryOwner);
		this.qi.getTriples().forEach(t -> {
			if (t.getChosenSources().contains(queryOwner)) {
				addTriple(t);
			}
		});
		finalize(aq);
		return aq;
	}

	/**
	 * Decomposes a given query and creates a list of sub-queries specific to the DB agents.
	 * This is a complex function that decomposes a query by using expertise data 
	 * (statistical information obtained from DB agents) of DB Agents.
	 * The functions can assign more than one source for each triple pattern (one to N mapping).
	 * @param experts map of DB agent expertise
	 * @return list of sub-queries
	 */
	public List<AgentQuery> decompositionFromExpertise(Map<AgentID, DbAgentExpertise> experts) 
			throws MissingExpertException, BadQueryException {
		SourceMatcher analyzer = new SourceMatcher(experts);
		analyzer.analyzeQuery(this.qi);
		List<AgentQuery> queries = new ArrayList<>();
		for(AgentID aid : experts.keySet()) {
			AgentQuery agentQuery = fromAnalyzedQuery(aid);
			if(!agentQuery.isEmpty()) {
				queries.add(agentQuery);
			}
		}
		return queries;
	}

	/**
	 * Naive approach that assigns all the capable data sources for each triple pattern.
	 * This is also 1 to N approach, and improved version of this is that 
	 * using the statistical information of the DB Agents {@link #decompositionFromExpertise} 
	 * @param experts
	 * @return
	 */
	public List<AgentQuery> decompose(Map<AgentID, DbAgentExpertise> experts){
		List<AgentQuery> queries = new ArrayList<>();
		experts.forEach((agentID, expertise) -> {
			AgentQuery agentQuery = fromCapabilities(agentID, expertise.getCapabilities());
			if (!agentQuery.isEmpty()) { 
				queries.add(agentQuery);
			}
		});
		List<Assignment<Set<String>>> matches = new ArrayList<>();
		for(AgentQuery aq : queries) {
			Set<String> set = new HashSet<>();
			for(TripleInfo ti : aq.getTriples()) {
				for(String prop : ti.getOntologicalConcepts()) {
					set.add(prop);
				}
			}
			matches.add(new Assignment<Set<String>>(aq.getQueryOwner(), set));
		}
		LOGGER.log(this.getClass(), Level.INFO, matches);
		return queries;
	}
	
	/**
	 * The function uses expertise graph to select sources.
	 * It selects a single source for each triple pattern (one to one mapping).
	 * @param expertiseGraph expertise graph
	 * @param experts data sources that can be assigned
	 * @return queries
	 * @throws MissingExpertException
	 * @throws BadQueryException
	 */
	public List<AgentQuery> decomposeWithExpertiseGraph(ExpertiseGraph<String> expertiseGraph, 
			Map<AgentID, DbAgentExpertise> experts) 
			throws MissingExpertException, BadQueryException {
		SourceMatcher matcher = new ComplexSourceMatcher(expertiseGraph, experts);
		matcher.analyzeQuery(this.qi);
		List<AgentQuery> queries = new ArrayList<>();
		for(AgentID aid : experts.keySet()) {
			AgentQuery agentQuery = fromAnalyzedQuery(aid);
			if(!agentQuery.isEmpty()) {
				queries.add(agentQuery);
			}
		}
		return queries;
	}
}
