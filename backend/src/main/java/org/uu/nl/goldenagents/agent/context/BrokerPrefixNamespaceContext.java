package org.uu.nl.goldenagents.agent.context;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A context object that helps the broker keep track of the preferred prefixes for namespaces of the ontologies
 * it uses
 */
public class BrokerPrefixNamespaceContext implements Context {

    private final Map<String, String> ontologyPrefixes = new HashMap<>();
    private final Map<AgentID, Map<String, String>> dependentAgents = new HashMap();

    public Map<String, String> getOntologyPrefixes() {
        return new HashMap<>(this.ontologyPrefixes);
    }

    public boolean removePrefix(String prefix) {
        this.ontologyPrefixes.remove(prefix);
        boolean agentsOutOfSync = false;
        for(AgentID aid : dependentAgents.keySet()) {
            if(this.dependentAgents.get(aid).containsKey(prefix)) {
                agentsOutOfSync = true;
                this.dependentAgents.put(aid, null);
            }
        }
        return agentsOutOfSync;
    }

    public boolean removePrefixForIRI(String iri) {
        String toRemove = this.getPrefixForIRI(iri);
        if(toRemove != null) {
            return this.removePrefix(toRemove);
        }
        return false;
    }

    public String getPrefix(String prefix) {
        return this.ontologyPrefixes.get(prefix);
    }

    public String getPrefixForIRI(String iri) {
        if(this.ontologyPrefixes.containsValue(iri)) {
            for(String prefix : this.ontologyPrefixes.keySet()) {
                if(this.ontologyPrefixes.get(prefix).equals(prefix)) {
                    return prefix;
                }
            }
        }
        return null;
    }

    public boolean setPrefix(String prefix, String iri) {
        this.ontologyPrefixes.put(prefix, iri);
        boolean agentsOutOfSync = false;
        for(AgentID aid : this.dependentAgents.keySet()) {
            if(this.dependentAgents.get(aid) == null || !this.dependentAgents.get(aid).containsKey(prefix)) {
                agentsOutOfSync = true;
                this.dependentAgents.put(aid, null);
            }
        }
        return agentsOutOfSync;
    }

    public boolean addAllPrefixes(Map<String, String> prefixMap) {
        boolean agentsOutOfSync = false;
        for(String prefix : prefixMap.keySet()) {
            agentsOutOfSync |= setPrefix(prefix, prefixMap.get(prefix));
        }
        return agentsOutOfSync;
    }

    /**
     * Get the set of AgentIDs of agents who have not been sent the latest set of prefixes used
     * @return Set of agentIDs
     */
    public Set<AgentID> getAgentsToInformOfUpdatedPrefixes() {
        return this.dependentAgents.keySet().stream().filter(x -> this.dependentAgents.get(x) == null).collect(Collectors.toSet());
    }

    public void addAgentToContact(AgentID agentID) {
        this.dependentAgents.put(agentID, null);
    }

    public void setAgentContacted(AgentID agentID, Map<String, String> sentPrefixes) {
        this.dependentAgents.put(agentID, new HashMap<>(sentPrefixes));
    }
}
