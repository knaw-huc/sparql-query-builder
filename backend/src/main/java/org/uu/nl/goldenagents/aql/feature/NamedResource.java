package org.uu.nl.goldenagents.aql.feature;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.VariableController;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class NamedResource extends hasResource {

    public NamedResource(SerializableResourceImpl resource) {
        super(resource, resource.getLabel());
    }

    public NamedResource(SerializableResourceImpl resource, String label) {
        super(resource, label);
    }

    private NamedResource(SerializableResourceImpl resource, String label, ID focusName, ID parent) {
        super(resource, label, focusName, parent);
    }

    public Op toARQ(Var var, VariableController controller) {
        checkIfFocus(var, controller);
        controller.addFilterOnVariable(var, this.resource.asNode());
        return null;
    }

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
        return null;
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
        return new LinkedList<>();
    }

    @Override
    public AQLTree copy(ID parent, HashMap<ID, AQLTree> foci) {
        NamedResource copy = new NamedResource(this.resource, this.label, this.getFocusName(), parent);
        foci.put(copy.getFocusName(), copy);
        return copy;
    }
}
