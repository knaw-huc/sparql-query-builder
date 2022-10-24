package org.uu.nl.goldenagents.agent.context.query;

import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLQueryContainer;
import org.uu.nl.goldenagents.netmodels.angular.AQLQueryObject;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.net2apl.core.agent.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// User agent
public class AQLQueryContext implements Context {
    private final Map<UUID, AQLQueryContainer> queries;
    private UUID currentQuery;

    public AQLQueryContext() {
        this.queries = new HashMap<>();
        this.currentQuery = null;
    }

    public synchronized QueryWrapper createQuery(Map<String, String> prefixMap) {
        AQLQuery query = new AQLQuery(prefixMap);
//        AQLQuery.constructSampleAQLQuery(query);
        AQLQueryContainer container = new AQLQueryContainer(query);
        this.queries.put(container.getConversationID(), container);
        this.currentQuery = container.getConversationID();
        return new QueryWrapper(container, query);
    }

    public UUID getCurrentQueryID() {
        return this.currentQuery;
    }

    public synchronized boolean changeCurrentQuery(UUID queryID) {
        synchronized (this.queries) {
            if (this.queries.containsKey(queryID)) {
                this.currentQuery = queryID;
                return true;
            }
        }
        return false;
    }

    public synchronized QueryWrapper getCurrentQuery() {
        AQLQueryContainer activeContainer = this.queries.get(this.currentQuery);
        if (activeContainer == null) return null;
        return new QueryWrapper(
                activeContainer,
                activeContainer.getActiveQuery()
        );
    }

    public synchronized QueryWrapper getQuery(UUID conversationID) {
        AQLQueryContainer container = this.queries.get(conversationID);
        if (container == null) return null;

        return new QueryWrapper(
                container,
                container.getActiveQuery()
        );
    }

    @Deprecated
    public AQLQueryObject serializeCurrentQuery() {
        return new AQLQueryObject(getCurrentQuery().query);
    }

    public static class QueryWrapper {
        public final AQLQueryContainer queryContainer;
        public final AQLQuery query;
        public final UUID conversationID;

        public QueryWrapper(AQLQueryContainer container, AQLQuery query) {
            this.queryContainer = container;
            this.query = query;
            this.conversationID = container.getConversationID();
        }
    }
}
