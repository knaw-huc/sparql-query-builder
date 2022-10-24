package org.uu.nl.goldenagents.aql.complex;

import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class CrossingOperator extends AQLTree {

    protected SerializableResourceImpl resource;
    protected AQLTree subquery;
    protected String label;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        AQLTree tree = (AQLTree) obj;
        if (!(tree.getClass().isInstance(this) && getClass().equals(tree.getClass()))) return false;
        if (!tree.type.equals(this.type)) return false;
        CrossingOperator t = (CrossingOperator) tree;
        if (!this.label.equals(t.label)) return false;
        return this.subquery.equals(t.getSubquery());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getClass().getName(),
                subquery,
                label,
                resource.getURI()
        );
    }

    public CrossingOperator(SerializableResourceImpl resource, AQLTree subquery) {
        super();
        this.resource = resource;
        this.subquery = subquery;
        this.label = resource.getLocalName();
        this.subquery.setParent(getFocusName());
        this.type = TYPE.PROPERTY;
    }

    public CrossingOperator(SerializableResourceImpl resource, AQLTree subquery, String label) {
        super();
        this.resource = resource;
        this.subquery = subquery;
        this.label = label;
        this.subquery.setParent(getFocusName());
        this.type = TYPE.PROPERTY;
    }

    protected CrossingOperator(SerializableResourceImpl resource, AQLTree subquery, String label, ID focusName, ID parent) {
        super(focusName, parent);
        this.resource = resource;
        this.subquery = subquery;
        this.label = label;
        this.subquery.setParent(getFocusName());
        this.type = TYPE.PROPERTY;
    }

    @Override
    public String getAQLLabel() {
        return this.label;
    }

    /**
     * Number of sub trees of this node type
     *
     * @return Integer indicating number of sub trees for this node type
     */
    @Override
    public int nSubtrees() {
        return 1;
    }

    /**
     * Replace a child of this node with a new sub tree
     *
     * @param child    Child to be replaced
     * @param newChild New sub tree
     */
    @Override
    public void replaceChild(ID child, AQLTree newChild) throws IllegalArgumentException {
        if(this.subquery.getFocusName().equals(child)) {
            this.subquery = newChild;
            this.subquery.setParent(getFocusName());
        }
        else throw new IllegalArgumentException("Child to be replaced does not exist on this node");
    }

    /**
     * Get the subqueries for this tree. Subqueries are the edges of this node.
     *
     * @return List of subqueries (i.e. sub trees) for this node
     */
    @Override
    public List<AQLTree> getSubqueries() {
        return Collections.singletonList(this.subquery);
    }

    public AQLTree getSubquery() {
        return this.subquery;
    }

    public SerializableResourceImpl getResource() {
        return this.resource;
    }

    @Override
    public String getFirstResourceLabel() {
        return this.label;
    }
}
