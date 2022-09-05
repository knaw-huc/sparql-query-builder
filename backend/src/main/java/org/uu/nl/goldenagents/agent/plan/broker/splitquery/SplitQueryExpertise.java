package org.uu.nl.goldenagents.agent.plan.broker.splitquery;

import org.uu.nl.goldenagents.decompose.ExpertDecomposer;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.exceptions.BadQueryException;
import org.uu.nl.goldenagents.netmodels.fipa.AgentQuery;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import javax.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitQueryExpertise extends SplitQueryPlan {

    public SplitQueryExpertise(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    /**
     * Create AgentQuery objects for all agents that should be consulted to answer the current user query. All agents
     * in this list will be sent a message containing the subquery specified on the object.
     *
     * @return List of AgentQuery objects.
     * @throws MissingExpertException if no list of agents can be constructed such that the query is
     * decomposed completely
     */
    @Override
    @NotNull List<AgentQuery> createAgentQueries() throws MissingExpertException, BadQueryException {
        Map<AgentID, DbAgentExpertise> experts = selectExpertsFromList();
        
		ExpertDecomposer decomposer = new ExpertDecomposer(this.queryInfo);
		//List<AgentQuery> queries = decomposer.decompositionFromExpertise(experts);
		/* If a problem occurs with the expertise graph, use the above function, which uses only the statistics of DB agents */
		List<AgentQuery> queries = decomposer.decomposeWithExpertiseGraph(this.context.getExpertiseGraph(), experts);
        return queries;
    }

    /**
     * Select compatible experts from a list of candidates
     * @return  List of experts filtered from {@code selectedSources}
     */
    private Map<AgentID, DbAgentExpertise> selectExpertsFromList() {
        List<String> candidates = Arrays.asList(this.selectedSources);
        Map<AgentID, DbAgentExpertise> expertiseMap = new HashMap<>();

        this.context.getDbAgentExpertises().forEach((agentID, expertise) -> {
            if(candidates.isEmpty() || candidates.contains(agentID.getUuID())) {
                expertiseMap.put(agentID, expertise);
            }
        });
        return expertiseMap;
    }
    
}
