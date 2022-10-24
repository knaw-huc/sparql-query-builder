package org.uu.nl.goldenagents.aql;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.Var;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLJsonBuilder;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLQueryJsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * The most general AQL query
 */
public class MostGeneralQuery extends AQLTree {

    private static final int N_SUB_TREES = 0;
    private static final String AQL_LABEL = "?";
    private static final String NL_LABEL = "anything";

    public MostGeneralQuery() { }

    private MostGeneralQuery(ID focusName, ID parent) {
        super(focusName, parent);
    }

    /**
     * AQL label representing this node in the AQL query
     *
     * @return String
     */
    @Override
    public String getAQLLabel() {
        return AQL_LABEL;
    }


    public Op toARQ(Var var, VariableController controller) {
        checkIfFocus(var, controller);
        if (var.equals(controller.getFocusVariable())) {
            controller.setHasMostGenericQueryAtFocus(true);
        }
        return controller.getFocusName() == this.hashCode() ? new OpBGP() : null;
    }

    /**
     * Convert this query to an AQL string recursively
     *
     * @return An AQL query string
     */
    @Override
    public String toAQLString() {
        return AQL_LABEL;
    }

    /**
     * Convert this tree to a natural language representation
     *
     * @return NL representation of this query
     */
    @Override
    public String toNLQuery() {
        return NL_LABEL;
    }

    /**
     * Number of sub trees of this node type
     *
     * @return Integer indicating number of sub trees for this node type
     */
    @Override
    public int nSubtrees() {
        return N_SUB_TREES;
    }

    /**
     * Replace a child of this node with a new sub tree
     *
     * @param child    Child to be replaced
     * @param newChild New sub tree
     */
    @Override
    public void replaceChild(ID child, AQLTree newChild) throws IllegalArgumentException {
        throw new IllegalArgumentException("The most general query does not have any children which can be replaced");
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MostGeneralQuery;
    }

    @Override
    public AQLTree copy(ID parent, HashMap<ID, AQLTree> foci) {
        MostGeneralQuery copy = new MostGeneralQuery(getFocusName(), parent);
        foci.put(copy.getFocusName(), copy);
        return copy;
    }
}
