package org.uu.nl.goldenagents.aql.complex;

import org.uu.nl.goldenagents.aql.AQLTree;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class BinaryAQLInfixOperator extends AQLTree {

    private static final int N_SUB_TREES = 2;

    AQLTree leftChild;
    AQLTree rightChild;

    BinaryAQLInfixOperator(AQLTree leftChild, AQLTree rightChild) {
        super();
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.leftChild.setParent(getFocusName());
        this.rightChild.setParent(getFocusName());
    }

    protected BinaryAQLInfixOperator(AQLTree leftChild, AQLTree rightChild, ID focusName, ID parent) {
        super(focusName, parent);
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.leftChild.setParent(getFocusName());
        this.rightChild.setParent(getFocusName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        AQLTree tree = (AQLTree) obj;
        if (!this.type.equals(tree.type)) return false;
        BinaryAQLInfixOperator t = (BinaryAQLInfixOperator) tree;

        return (this.leftChild.equals(t.leftChild) && this.rightChild.equals(t.rightChild)) ||
                (this.leftChild.equals(t.rightChild) && this.rightChild.equals(t.leftChild));
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getClass().getName(),
                this.leftChild,
                this.rightChild
        );
    }

    @Override
    public int nSubtrees() {
        return N_SUB_TREES;
    }

    @Override
    public String toAQLString() {
        return insertBrackets(leftChild) +
                getAQLLabel() +
                insertBrackets(rightChild);
    }

    @Override
    public List<AQLTree> getSubqueries() {
        return Arrays.asList(leftChild, rightChild);
    }

    public AQLTree getLeftChild() {
        return this.leftChild;
    }

    public AQLTree getRightChild() {
        return this.rightChild;
    }

    /**
     * Translate a subquery to an AQL string and add brackets if the subquery itself is also a complex query
     * @param t     The sub tree to translate and add brackets to
     * @return      String, may or may not include brackets.
     */
    private String insertBrackets(AQLTree t) {
        return String.format(t.nSubtrees() > 1 ? " (%s) " : " %s ", t.toAQLString());
    }

    /**
     * Replace a child of this node with a new sub tree
     *
     * @param child    Child to be replaced
     * @param newChild New sub tree
     */
    @Override
    public void replaceChild(ID child, AQLTree newChild) throws IllegalArgumentException {
        if(this.leftChild.getFocusName().equals(child)) {
            this.leftChild = newChild;
            this.leftChild.setParent(this.getFocusName());
        } else if (this.rightChild.getFocusName().equals(child)) {
            this.rightChild = newChild;
            this.rightChild.setParent(this.getFocusName());
        } else {
            throw new IllegalArgumentException("Child to be replaced does not exist on this node");
        }
    }
}
