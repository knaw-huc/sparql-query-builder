package org.uu.nl.goldenagents.agent.context.query;

import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.netmodels.angular.AQLQueryObject;
import org.uu.nl.goldenagents.netmodels.angular.AQLResource;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;
import org.uu.nl.net2apl.core.agent.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// User agent
public class AQLQueryContext implements Context {
    private final List<AQLQuery> queries;
    private int currentQueryIndex;

    public AQLQueryContext() {
        this.queries = new ArrayList<>();
        this.currentQueryIndex = -1;
    }

    public synchronized AQLQuery createQuery(Map<String, String> prefixMap) {
        AQLQuery newQuery = new AQLQuery(prefixMap);
//        constructSampleAQLQuery(newQuery);
        synchronized (this.queries) {
            this.queries.add(newQuery);
            this.currentQueryIndex = this.queries.lastIndexOf(newQuery);
        }
        return newQuery;
    }

    private void constructSampleAQLQuery(AQLQuery query) {
        SerializableResourceImpl author = new SerializableResourceImpl("https://goldenagents.com/ontology#Author");
        SerializableResourceImpl book = new SerializableResourceImpl("https://goldenagents.com/ontology#Book");

        query.intersection(new TypeSpecification(author));
        UUID startFocus = query.getFocusName();

        AQLResource authorOf = new AQLResource();
        authorOf.uri = "https://goldenagents.com/ontology#authorOf";
        authorOf.label = "authorOf";
        query.cross(authorOf, false);

        query.intersection(new TypeSpecification(book));

        AQLResource hasPublished = new AQLResource();
        hasPublished.uri = "https://goldenagents.com/ontology#hasPublished";
        hasPublished.label = "hasPublished";
        query.cross(hasPublished, false);

        query.setFocus(startFocus);
        AQLResource hasName = new AQLResource();
        hasName.uri = "https://goldenagents.com/ontology#hasName";
        hasName.label = "hasName";
        query.cross(hasName, false);


    }

    public synchronized int getCurrentQueryIndex() {
        return this.currentQueryIndex;
    }

    public synchronized boolean changeCurrentQuery(AQLQuery query) {
        synchronized (this.queries) {
            if (this.queries.contains(query)) {
                this.currentQueryIndex = this.queries.indexOf(query);
                return true;
            }
        }
        return false;
    }

    public synchronized AQLQuery getCurrentQuery() {
        synchronized (this.queries) {
            return this.queries.get(this.currentQueryIndex);
        }
    }

    public AQLQuery getQueryForIndex(int index) {
        synchronized (this.queries) {
            if(index < 0 || index > this.queries.size())
                throw new IndexOutOfBoundsException();
            return this.queries.get(index);
        }
    }

    @Deprecated
    public AQLQueryObject serializeCurrentQuery() {
        return new AQLQueryObject(getCurrentQuery());
    }

    public AQLQueryObject serializeQuery(int index) {
        AQLQuery q = this.getQueryForIndex(index);
        return new AQLQueryObject(q);
    }

    /**
     * Find the query object for the root node ID
     * @param nodeID
     * @return
     */
    public AQLQuery getQueryObjectForNodeID(UUID nodeID) {
        AQLQuery currentQuery = getCurrentQuery();
        AQLTree tree = currentQuery.getQueryTree();
        UUID focus = currentQuery.getFocusName();

        if (focus.equals(nodeID)) {
            return this.getCurrentQuery();
        } else {
            for(AQLQuery q : this.queries) {
                tree = q.getQueryTree();
                focus = tree.getFocusID();
                if(focus.equals(nodeID))
                    return q;
            }
        }

        return null;
    }

    public void setSuggestionsForNodeID(AQLSuggestions suggestions) {
        this.getQueryObjectForNodeID(suggestions.getFocusID()).setSuggestions(suggestions);
    }
}
