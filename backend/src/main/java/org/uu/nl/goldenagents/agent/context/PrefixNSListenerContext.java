package org.uu.nl.goldenagents.agent.context;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Context;

import java.util.*;

/**
 * This class stores the prefixes from the broker agent, and is different from the broker prefix context for that reason
 */
public class PrefixNSListenerContext implements Context {

    private Map<AgentID, Map<String, String>> prefixMap;

    public boolean setPrefixMap(AgentID aid, Map<String, String> prefixMap) {
        boolean prefixesChanged = false;

        if(this.prefixMap == null || this.prefixMap.get(aid) == null || prefixMap.size() != this.prefixMap.get(aid).size()) {
            if(this.prefixMap == null) {
                this.prefixMap = new HashMap<>();
            }
            this.prefixMap.put(aid, new HashMap<>(prefixMap));
            prefixesChanged = true;
        } else {
            for (String prefix : prefixMap.keySet()) {
                if (!this.prefixMap.get(aid).containsKey(prefix) || !this.prefixMap.get(aid).get(prefix).equals(prefixMap.get(prefix))) {
                    this.prefixMap.get(aid).put(prefix, prefixMap.get(prefix));
                    prefixesChanged = true;
                }
            }
            for (String prefix : this.prefixMap.get(aid).keySet()) {
                if (!prefixMap.containsKey(prefix)) {
                    this.prefixMap.get(aid).remove(prefix);
                    prefixesChanged = true;
                }
            }
        }

        return prefixesChanged;
    }

    public Map<String, String> getPrefixMap() {
        Map<String, String> prefixMap = new HashMap<>();
        if(this.prefixMap != null) {
            for (Map<String, String> agentPrefixMap : this.prefixMap.values()) {
                prefixMap.putAll(agentPrefixMap);
            }
        }
        return prefixMap;
    }

    public Map<String, String> getPrefixMapForAgent(AgentID aid) {
        if(this.prefixMap != null && this.prefixMap.containsKey(aid)) {
            return this.prefixMap.get(aid);
        } else {
            return Collections.emptyMap();
        }
    }

    public Set<String> getNamespacesForAgent(AgentID aid) {
        return new HashSet<>(this.getPrefixMapForAgent(aid).values());
    }

    public Set<String> getNamespaces() {
        Set<String> namespaces = new HashSet<>();
        if(this.prefixMap != null) {
            for(Map<String, String> prefixMap : this.prefixMap.values()) {
                namespaces.addAll(prefixMap.values());
            }
        }
        return namespaces;
    }

}
