package org.uu.nl.goldenagents.aql;

import org.uu.nl.net2apl.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AQLQueryHistoryTree {

    // The children of a query live on the same level. These can be reached with one transformation step from
    // the current node.
    // However, some transformation steps will result in a query already somewhere in the query, in which case the
    // existing query tree will be re-used
    private final AQLQueryHistoryTree parent;
    private final List<AQLQueryHistoryTree> children = new ArrayList<>();

    private final AQLQuery node;

    public AQLQueryHistoryTree(AQLQuery node, AQLQueryHistoryTree parent) {
        this.node = node;
        this.parent = parent;
    }

    /**
     * @return This node's children
     */
    public List<AQLQueryHistoryTree> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     * Add a child to this node
     * @param child  Child to add
     */
    public void addChild(AQLQuery child) {
        this.children.add(new AQLQueryHistoryTree(child, this));
    }

    /**
     * Find a semantically equivalent query tree in the history
     *
     * Note that two query trees are equivalent, even if they have the same focus
     *
     * @param query Equivalent Query to find
     * @return The node in this tree holding the equivalent query
     */
    public AQLQueryHistoryTree find(AQLQuery query) {
        if (this.node.equals(query)) {
            Platform.getLogger().log(getClass(), Level.INFO, "Found equivalent query");
            return this;
        } else {
            for (AQLQueryHistoryTree child : this.children) {
                AQLQueryHistoryTree found = child.find(query);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public AQLQuery getNode() {
        return node;
    }

    public AQLQueryHistoryTree getQuery(int queryHashCode) {
        if (this.node.hashCode() == queryHashCode) {
            return this;
        } else {
            for(AQLQueryHistoryTree child : this.children) {
                AQLQueryHistoryTree query = child.getQuery(queryHashCode);
                if (query != null) return query;
            }
        }
        return null;
    }

    public AQLQueryHistoryTree getParent() {
        return parent;
    }
}
