package org.uu.nl.goldenagents.aql;

import org.uu.nl.goldenagents.agent.context.query.AQLQueryContext;
import org.uu.nl.goldenagents.netmodels.angular.AQLResource;

import java.util.UUID;

/**
 * This class corresponds to one query that can indefinitely be changed or extended by the user.
 * It maintains a history of all queries created by the user by performing a one-step transformation of
 * any other query in the history.
 */
public class AQLQueryContainer {

    private final UUID conversationID = UUID.randomUUID();

    AQLQueryHistoryTree queryHistoryTree;

    // Stores the hash code of the active query, which includes a reference to focus
    int activeQueryID;

    public AQLQueryContainer(AQLQuery query) {
        this.queryHistoryTree = new AQLQueryHistoryTree(query, null);
        activeQueryID = query.hashCode();
    }

    public UUID getConversationID() {
        return conversationID;
    }

    public AQLQuery getActiveQuery() {
        return getActiveNode().getNode();
    }

    public AQLQueryHistoryTree getActiveNode() {
        return this.queryHistoryTree.getQuery(this.activeQueryID);
    }

    public AQLQuery getQuery(int queryHashCode) {
        return this.queryHistoryTree.getQuery(queryHashCode).getNode();
    }

    /**
     * Changes the active query to the same query with a new focus, either by creating it, or finding it in
     * the query history tree
     * @param newFocus UUID of the node receiving the new focus
     */
    public AQLQueryContext.QueryWrapper setFocus(AQLTree.ID newFocus) {
        AQLQuery active = getActiveQuery();
        AQLQuery copy = active.copy();
        copy.setFocus(newFocus);
        AQLQuery updatedQuery = addOrGetSibling(copy);
        this.activeQueryID = updatedQuery.hashCode();
        return new AQLQueryContext.QueryWrapper(this, updatedQuery);
    }

    /**
     * Adds a new query as a sibling to the current query in the query history tree
     * @param query New sibling to add
     */
    private AQLQuery addOrGetSibling(AQLQuery query) {
        AQLQueryHistoryTree tree = queryHistoryTree.find(query);

        if (tree != null) {
            return tree.getNode();
        } else {
            AQLQueryHistoryTree parent = getActiveNode().getParent();
            parent.addChild(query);
            return query;
        }
    }

    private AQLQueryContext.QueryWrapper addOrGetChild(AQLQuery query) {
        AQLQueryHistoryTree tree = queryHistoryTree.find(query);
        AQLQuery updatedQuery;
        if (tree != null) {
            updatedQuery = tree.getNode();
        } else {
            getActiveNode().addChild(query);
            updatedQuery = query;
        }
        this.activeQueryID = updatedQuery.hashCode();
        return new AQLQueryContext.QueryWrapper(this, updatedQuery);
    }

    public AQLQueryContext.QueryWrapper intersection(AQLTree feature) {
        AQLQuery activeQuery = getActiveQuery();
        AQLQuery copy = activeQuery.copy();

        copy.intersection(feature);
        return addOrGetChild(copy);
    }

    public AQLQueryContext.QueryWrapper cross(AQLResource aqlResource, boolean crossForward) {
        AQLQuery activeQuery = getActiveQuery();
        AQLQuery copy = activeQuery.copy();

        copy.cross(aqlResource, crossForward);
        return addOrGetChild(copy);
    }

    public AQLQueryContext.QueryWrapper negativeLookup() {
        AQLQuery copy = getActiveQuery().copy();
        copy.negativeLookup();
        return addOrGetChild(copy);
    }

    public AQLQueryContext.QueryWrapper union() {
        AQLQuery copy = getActiveQuery().copy();
        copy.union();
        return addOrGetChild(copy);
    }

    public AQLQueryContext.QueryWrapper name() {
        AQLQuery copy = getActiveQuery().copy();
        copy.name();
        return addOrGetChild(copy);
    }

    public AQLQueryContext.QueryWrapper reference() {
        AQLQuery copy = getActiveQuery().copy();
        copy.reference();
        return addOrGetChild(copy);
    }

    public AQLQueryContext.QueryWrapper delete() {
        AQLQuery copy = getActiveQuery().copy();
        copy.delete();

        /*
        If B is added to A, and C is added to A, then B is removed form A, the resulting tree does not yet exist
        in the query history tree.
        The new tree should be added as a child of A
         */

        AQLQueryHistoryTree tree = queryHistoryTree.find(copy);

        if (tree != null) {
            activeQueryID = tree.getNode().hashCode();
            return new AQLQueryContext.QueryWrapper(this, tree.getNode());
        } else {
            getActiveNode().getParent().addChild(copy);
            activeQueryID = copy.hashCode();
            return new AQLQueryContext.QueryWrapper(this, copy);
        }
    }

    public AQLQueryContext.QueryWrapper delete(AQLTree.ID newFocus) {
        setFocus(newFocus);
        return delete();
    }

}
