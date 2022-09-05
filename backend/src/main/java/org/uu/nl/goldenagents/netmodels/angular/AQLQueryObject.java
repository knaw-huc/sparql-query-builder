package org.uu.nl.goldenagents.netmodels.angular;

import org.springframework.lang.Nullable;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.complex.BinaryAQLInfixOperator;
import org.uu.nl.goldenagents.aql.complex.CrossingOperator;

import java.util.UUID;

public class AQLQueryObject {
    private String verbalization;
    private String AQL;
    private boolean focus;
    private AQLQueryObject[] subqueries;
    private UUID name;
    private String type;
    private boolean newline = false;

    private AQLQueryObject(AQLQuery query, AQLTree t) {
        this.verbalization = t.toNLQuery();
        this.AQL = t.getAQLLabel();
        this.name = t.getFocusID();
        this.focus = this.name.equals(query.getFocusName());
        // TODO maybe move FRONTEND filters to here? idk
        subqueries = t.getSubqueries().stream().map(s -> new AQLQueryObject(query, s)).toArray(AQLQueryObject[]::new);
        this.type = t.type.toString();
        // Everything here can be optimized significantly
        AQLTree parent = getParent(query, t);
        if(parent != null) {
            this.newline = leftTreeBranches(parent);
        }
//        this.newline = t instanceof BinaryAQLInfixOperator && containsCrossing(((BinaryAQLInfixOperator)t).getLeftChild());

//        if(t instanceof Intersection) {
//            Intersection i = (Intersection) t;
//            AQLTree left = i.getLeftChild();
//            AQLTree right = i.getRightChild();

            // TODO line end if Left-Tree contains crossing operator
//            if(!(left instanceof Intersection || right instanceof Intersection)) {
//                for(AQLQueryObject sq : this.subqueries) {
//                    if(sq.subqueries.length > 0) {
//                        sq.newline = true;
//                    }
//                }
//            }
//        }
    }

    public AQLQueryObject(AQLQuery query) {
        this(query, query.getQueryTree());
    }

    private boolean containsCrossing(AQLTree query) {
        if(query.getSubqueries().isEmpty()) return false;
        else {
            for(AQLTree child : query.getSubqueries()) {
                if(child instanceof CrossingOperator) return true;
                else if (containsCrossing(child)) return true;
            }
        }
        return false;
    }

    @Nullable private AQLTree getParent(AQLQuery query, AQLTree node) {
        AQLTree parent = null;
        if(node.getParentID() != null) parent = query.getNode(node.getParentID());
        return parent;
    }

    private boolean leftTreeBranches(AQLTree tree) {
        return tree instanceof BinaryAQLInfixOperator && ((BinaryAQLInfixOperator)tree).getLeftChild().nSubtrees() > 0;
    }

    private boolean isRightChild(AQLTree parent, AQLTree tree) {
        return parent instanceof BinaryAQLInfixOperator && ((BinaryAQLInfixOperator)parent).getRightChild().equals(tree);
    }

    private boolean isRightChildOfLeftChild(AQLTree parent, AQLTree tree) {
        return parent instanceof BinaryAQLInfixOperator &&
                ((BinaryAQLInfixOperator) parent).getRightChild() instanceof BinaryAQLInfixOperator &&
                isRightChild(((BinaryAQLInfixOperator) parent).getRightChild(), tree);
    }

    public String getVerbalization() {
        return verbalization;
    }

    public void setVerbalization(String verbalization) {
        this.verbalization = verbalization;
    }

    public String getAQL() {
        return AQL;
    }

    public void setAQL(String AQL) {
        this.AQL = AQL;
    }

    public boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    public AQLQueryObject[] getSubqueries() {
        return subqueries;
    }

    public void setSubqueries(AQLQueryObject[] subqueries) {
        this.subqueries = subqueries;
    }

    public String getName() {
        return this.name.toString();
    }

    public void setName(String name) {
        this.name = UUID.fromString(name);
    }

    public void setName(UUID name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNewline() {
        return newline;
    }

    public void setNewline(boolean newline) {
        this.newline = newline;
    }
}
