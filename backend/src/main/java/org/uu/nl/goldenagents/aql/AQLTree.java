package org.uu.nl.goldenagents.aql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.uu.nl.goldenagents.util.AQLTreeIdDeserializer;
import org.uu.nl.goldenagents.util.AQLTreeIdSerializer;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import java.util.*;

/**
 * Node for an Agent Query Language (AQL) tree. The AQL query is built up of these nodes
 */
public abstract class AQLTree implements FIPASendableObject {

    // TODO, we need to go back to using UUID. But use the above object to distinguish from conversationID or agentID, because that is just confusing!
    private final ID focusName;

    private ID parent = null;

    public ID getParentID() {
        return this.parent;
    }

    public TYPE type = TYPE.UNMARKED;

    protected AQLTree() {
        this.focusName = ID.random();
    }

    protected AQLTree(ID focusName, ID parent) {
        this.focusName = focusName;
        this.parent = parent;
    }

    public void setParent(ID parent) {
        this.parent = parent;
        if(this.parent == this.focusName) try {
            throw new Exception("Setting this random ID as parent of itself!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AQL label representing this node in the AQL query
     * @return  String
     */
    public abstract String getAQLLabel();

    /**
     * Construct Sparql Algebra for this query (sub) tree recursively
     * @param var   An ARQ variable for this (sub) tree
     * @return      Jena Sparql Algebra Op object
     */
    public abstract Op toARQ(Var var, VariableController controller);

    /**
     * Convert this query to an AQL string recursively
     * @return An AQL query string
     */
    public abstract String toAQLString();

    /**
     * Convert this tree to a natural language representation
     * @return NL representation of this query
     */
    public abstract String toNLQuery();

    /**
     * Number of sub trees of this node type
     * @return  Integer indicating number of sub trees for this node type
     */
    public abstract int nSubtrees();

    /**
     * Replace a child of this node with a new sub tree
     * @param childFocusName         FocusID of child to be replaced
     * @param newChild      New sub tree
     */
    public abstract void replaceChild(ID childFocusName, AQLTree newChild) throws IllegalArgumentException;

    /**
     * Get the subqueries for this tree. Subqueries are the edges of this node.
     * @return List of subqueries (i.e. sub trees) for this node
     */
    public abstract List<AQLTree> getSubqueries();

    public ID getFocusName() {
        return focusName;
    }

    /**
     * Recursively try to find the first resource label that could describe a variable
     * @return
     */
    public String getFirstResourceLabel() {
        for(AQLTree subquery : this.getSubqueries()) {
             String label = subquery.getFirstResourceLabel();
             if(label != null) return label;
        }
        return null;
    }

    public enum TYPE {

        CLASS("class"),
        PROPERTY("prop"),
        MODIFIER("modifier"),
        UNMARKED("");

        private final String label;

        TYPE(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    protected void checkIfFocus(Var var, VariableController controller) {
        if(this.hashCode() == controller.getFocusName()) {
            controller.setFocusVariable(var);
        }
    }

    @Override
    public int hashCode() {
        int result = getClass().getName().hashCode();
        result += 20 * type.hashCode();

        for (int i = 0; i < getSubqueries().size(); i++) {
            result += Math.pow(10, i+1) * getSubqueries().get(i).hashCode();
        }
        return result;
    }

    public abstract AQLTree copy(ID parent, HashMap<ID, AQLTree> foci);

    @JsonDeserialize(using = AQLTreeIdDeserializer.class)
    @JsonSerialize(using = AQLTreeIdSerializer.class)
    public static final class ID implements FIPASendableObject {

        private final UUID _id;

        private ID(UUID _id) {
            this._id = _id;
        }

        public static ID random() {
            return new ID(UUID.randomUUID());
        }

        public static ID fromString(String string) {
            return new ID(UUID.fromString(string));
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof ID)) return false;
            return this._id.equals(((ID) obj)._id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this._id);
        }

        @Override
        public String toString() {
            return _id.toString();
        }
    }
}
