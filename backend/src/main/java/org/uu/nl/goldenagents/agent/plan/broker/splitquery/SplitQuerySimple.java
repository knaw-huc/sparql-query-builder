package org.uu.nl.goldenagents.agent.plan.broker.splitquery;

import org.uu.nl.goldenagents.decompose.QueryDecomposer;
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

public class SplitQuerySimple extends SplitQueryPlan {

    public SplitQuerySimple(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    /**
     * Create AgentQuery objects for all agents that should be consulted to answer the current user query. 
     * All agents in this list will be sent a message containing the sub-query specified on the object.
     *
     * @return List of AgentQuery objects.
     */
    @Override
    @NotNull List<AgentQuery> createAgentQueries() {
    	Map<AgentID, List<String>> capables = selectCapablesFromList();
        QueryDecomposer decomposer = new QueryDecomposer(super.queryInfo);
        List<AgentQuery> queries = decomposer.decompositionFromCapabilities(capables);
        return queries;
    }
    
    /**
     * Select compatible DB-Agents from a list of candidates
     * @return  List of DB-Agents filtered from {@code selectedSources}
     */
    private Map<AgentID, List<String>> selectCapablesFromList() {
        List<String> candidates = Arrays.asList(super.selectedSources);
        Map<AgentID, List<String>> capabilityMap = new HashMap<>();
        this.context.getDbAgentCapabilities().forEach((agentID, capabilities) -> {
            if(candidates.contains(agentID.getUuID())) {
            	capabilityMap.put(agentID, capabilities);
            }
        });
        return capabilityMap;
    }
}
