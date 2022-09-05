package org.uu.nl.goldenagents.netmodels.jena;

import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import java.util.HashMap;
import java.util.Map;

public class RDFNameSpaceMap implements FIPASendableObject {

    private final Map<String, String> namespaceMap;

    public RDFNameSpaceMap(Map<String, String> namespaceMap) {
        this.namespaceMap = new HashMap<>(namespaceMap);
    }

    public Map<String, String> getNamespaceMap() {
        return namespaceMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("These are the namespaces I know:\n");
        for(String prefix : this.namespaceMap.keySet()) {
            sb.append(String.format("\t%s: <%s>\n", prefix, this.namespaceMap.get(prefix)));
        }
        return sb.toString();
    }
}
