package org.uu.nl.goldenagents.aql.feature;

import org.uu.nl.goldenagents.aql.AQLTree;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Feature extends AQLTree {

    /**
     * Convert this query to an AQL string recursively
     *
     * @return An AQL query string
     */
    @Override
    public String toAQLString() {
        return getAQLLabel();
    }

    /**
     * Convert this tree to a natural language representation
     *
     * @return NL representation of this query
     */
    @Override
    public String toNLQuery() {
        return getAQLLabel();
    }

    /**
     * Number of sub trees of this node type
     *
     * @return Integer indicating number of sub trees for this node type
     */
    @Override
    public int nSubtrees() {
        return 0;
    }

    /**
     * Get the subqueries for this tree. Subqueries are the edges of this node.
     *
     * @return List of subqueries (i.e. sub trees) for this node
     */
    @Override
    public List<AQLTree> getSubqueries() {
        return new ArrayList<>();
    }

    /**
     * Replace a child of this node with a new sub tree
     *
     * @param child    Child to be replaced
     * @param newChild New sub tree
     */
    @Override
    public void replaceChild(UUID child, AQLTree newChild) throws IllegalArgumentException {
        throw new IllegalArgumentException("Trying to replace a child of a leaf node");
    }
}
