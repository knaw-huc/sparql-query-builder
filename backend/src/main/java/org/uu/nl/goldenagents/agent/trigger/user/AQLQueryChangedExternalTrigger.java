package org.uu.nl.goldenagents.agent.trigger.user;

import org.uu.nl.goldenagents.agent.context.query.AQLQueryContext;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLQueryContainer;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import java.util.UUID;

public class AQLQueryChangedExternalTrigger implements Trigger, FIPASendableObject {
    private AQLQueryContainer queryContainer;
    private AQLQuery query;
    private AQLTree queryTree;
    private AQLTree.ID focus;
    private UUID conversationID;

    public AQLQueryChangedExternalTrigger(AQLQueryContext.QueryWrapper query) {
        this.queryContainer = query.queryContainer;
        this.conversationID = query.conversationID;
        this.query = query.query;
        this.queryTree = this.query.getQueryTree();
        this.focus = this.query.getFocusName();
    }

    public AQLTree getQueryTree() {
        return queryTree;
    }

    public void setQueryTree(AQLTree queryTree) {
        this.queryTree = queryTree;
    }

    public AQLTree.ID getFocus() {
        return focus;
    }

    public void setFocus(AQLTree.ID focus) {
        this.focus = focus;
    }

    public AQLQuery getQuery() {
        return query;
    }

    public void setQuery(AQLQuery query) {
        this.query = query;
    }

    public AQLQueryContainer getQueryContainer() {
        return queryContainer;
    }

    public void setQueryContainer(AQLQueryContainer queryContainer) {
        this.queryContainer = queryContainer;
    }

    public UUID getConversationID() {
        return conversationID;
    }

    public void setConversationID(UUID conversationID) {
        this.conversationID = conversationID;
    }
}
