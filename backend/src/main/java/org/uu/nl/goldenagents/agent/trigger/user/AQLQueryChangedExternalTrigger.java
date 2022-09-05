package org.uu.nl.goldenagents.agent.trigger.user;

import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import java.util.UUID;

public class AQLQueryChangedExternalTrigger implements Trigger, FIPASendableObject {
    private AQLQuery query;
    private AQLTree queryTree;
    private UUID focus;

    public AQLQueryChangedExternalTrigger(AQLQuery query) {
        this.query = query;
        this.queryTree = query.getQueryTree();
        this.focus = query.getFocusName();
    }

    public AQLTree getQueryTree() {
        return queryTree;
    }

    public void setQueryTree(AQLTree queryTree) {
        this.queryTree = queryTree;
    }

    public UUID getFocus() {
        return focus;
    }

    public void setFocus(UUID focus) {
        this.focus = focus;
    }

    public AQLQuery getQuery() {
        return query;
    }

    public void setQuery(AQLQuery query) {
        this.query = query;
    }
}
