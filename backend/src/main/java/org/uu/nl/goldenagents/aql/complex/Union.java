package org.uu.nl.goldenagents.aql.complex;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.VariableController;

import java.util.HashMap;

public class Union extends BinaryAQLInfixOperator {

    private static final String AQL_LABEL = "or";

    public Union(AQLTree leftChild, AQLTree rightChild) {
        super(leftChild, rightChild);
    }

    private Union(AQLTree leftChild, AQLTree rightChild, ID focusName, ID parent) {
        super(leftChild, rightChild, focusName, parent);
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
        Op left = leftChild.toARQ(var, controller);
        Op right = rightChild.toARQ(var, controller);
        if (left != null && right != null) {
            return new OpUnion(left, right);
        } else if (left != null) {
            return left;
        } else return right; // May be null as well (for namedResource or namedLiteral)
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

    @Override
    public AQLTree copy(ID parent, HashMap<ID, AQLTree> foci) {
        AQLTree leftChild = getLeftChild().copy(this.getFocusName(), foci);
        AQLTree rightChild = getRightChild().copy(this.getFocusName(), foci);

        Union copy = new Union(leftChild, rightChild, this.getFocusName(), parent);
        foci.put(copy.getFocusName(), copy);

        return copy;
    }
}
