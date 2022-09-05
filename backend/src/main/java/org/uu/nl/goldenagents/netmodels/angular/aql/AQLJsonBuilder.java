package org.uu.nl.goldenagents.netmodels.angular.aql;

import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.MostGeneralQuery;
import org.uu.nl.goldenagents.aql.complex.*;
import org.uu.nl.goldenagents.aql.feature.NamedResource;
import org.uu.nl.goldenagents.aql.feature.TypeSpecification;
import org.uu.nl.goldenagents.aql.misc.Exclusion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder class to recursively convert an AQL query tree to a JSON object that can be used to display the query
 * in the browser.
 * Each row is represented by an object containing the indentation level, and a list of atomic objects representing
 * query elements
 */
public class AQLJsonBuilder {

    private final AQLQuery query;
    private final List<AQLQueryJsonRow> rows;
    private AQLQueryJsonRow activeRow;
    private boolean isBuilding;
    private int indentation = 1;
    private boolean inFocus = false;
    private UUID virtualFocus;

    // TODO, ideally, we want to pass all foci that a node is a sub tree off, so that we can highlight what the focus
    // will be on hover. However, due to the concept of virtual focus, we cannot just maintain this list as is. Can
    // we come up with a clever trick?
    private final List<UUID> subtreeOfFocus = new ArrayList<>();

    private boolean firstTypeAdded = false;

    /**
     * Constructs a new AQL Json Builder. The {@code AQLJsonBuilder.build()} method can immediately be called.
     *
     * @param query     The query to convert to JSON
     */
    public AQLJsonBuilder(AQLQuery query) {
        this.query = query;
        this.activeRow = new AQLQueryJsonRow();
        this.rows = new ArrayList<>();
        this.isBuilding = true;
        StringBuilder startFiller = new StringBuilder();
        startFiller.append("Give me ");
        if (this.query.getQueryTree() instanceof BinaryAQLInfixOperator) {
            BinaryAQLInfixOperator queryTree = (BinaryAQLInfixOperator) this.query.getQueryTree();
            if (hasTypeAtCurrentFocus(queryTree)) {
                startFiller.append("every");
            } else if (queryTree.getRightChild() instanceof CrossingOperator) {
                startFiller.append("everything");
            }
        }
        addFiller(new MostGeneralQuery(), startFiller.toString());
        addAqlTree(this.query.getQueryTree());
    }

    public void addAqlTree(AQLTree tree, boolean addRowToSecondChild) {
        if(hasFocus(tree)) {
            this.inFocus = true;
            if (this.virtualFocus == null) {
                virtualFocus = tree.getFocusID();
            }
        }

        this.subtreeOfFocus.add(tree.getFocusID());

        if (tree instanceof Intersection) {
            addAqlTree((Intersection) tree, addRowToSecondChild);
        } else if (tree instanceof Union) {
            addAqlTree((Union) tree, addRowToSecondChild);
        } else if (tree instanceof CrossingOperator) {
            addAqlTree((CrossingOperator) tree);
        } else if (tree instanceof Exclusion) {
            addAqlTree((Exclusion) tree);
        } else if (tree instanceof NamedResource) {
            addAqlTree((NamedResource) tree);
        } else if (tree instanceof TypeSpecification) {
            addAqlTree((TypeSpecification) tree);
        } else if (tree instanceof MostGeneralQuery) {
            addAqlTree((MostGeneralQuery) tree);
        } else {
            throw new IllegalArgumentException("Trying to convert AQL tree of subtype " + tree.getClass() + " to AQL, but subtype is unknown");
        }

        this.subtreeOfFocus.remove(tree.getFocusID());

        if (tree.getFocusID().equals(this.virtualFocus)) {
            this.inFocus = false;
        }
    }

    private boolean containsFocus(AQLTree tree) {
        if (hasFocus(tree)) {
            return true;
        } else if (tree instanceof CrossingOperator) {
            return false;
        } else {
            for (AQLTree subTree : tree.getSubqueries()) {
                if (containsFocus(subTree)) return true;
            }
        }
        return false;
    }

    public void addAqlTree(Intersection intersection, boolean addRowToSecondChild) {
        boolean newRow = intersection.getLeftChild().getSubqueries().size() > 1 && intersection.getLeftChild().getSubqueries().get(1) instanceof CrossingOperator &&
                intersection.getRightChild() instanceof CrossingOperator;

        if (containsFocus(intersection.getLeftChild()) && virtualFocus == null) {
            this.inFocus = true;
            this.virtualFocus = intersection.getFocusID();
        }

        if (newRow) {
            addAqlTree(intersection.getLeftChild(), true);
            addRow();
            addFiller(intersection, "and");
        } else {
            addAqlTree(intersection.getLeftChild());
        }

        if (addRowToSecondChild) addRow();

        addAqlTree(intersection.getRightChild());
    }

    public void addAqlTree(Union union, boolean addRowToSecondChild) {
        addAqlTree(union.getLeftChild());
        addFiller(union, "or");
        addAqlTree(union.getRightChild());
    }

    public void addAqlTree(CrossingOperator operator) {
        String type = operator instanceof CrossingBackwards ? "backward" : "forward";
        String prefix = operator instanceof CrossingBackwards ? "that has a" : "that is the";
        indentation++;
        if (hasFocus(operator.getSubquery()) && this.virtualFocus == null) {
            this.virtualFocus = operator.getFocusID();
            this.inFocus = true;
        }
        addObject(operator.getSubquery(), type, operator.getAQLLabel(), prefix);

        if (!(operator.getSubquery() instanceof MostGeneralQuery)) {
            addAqlTree(operator.getSubquery());
        }
        indentation--;
    }

    public void addAqlTree(Exclusion exclusion) {
        addRow();
        addFiller(exclusion, "and for which there is not");
        addAqlTree(exclusion.getNegatedQuery());
    }

    public void addAqlTree(NamedResource namedResource) {
        addObject(namedResource, "instance", namedResource.getAQLLabel(), "that is");
    }

    public void addAqlTree(TypeSpecification typeSpecification) {
        addObject(typeSpecification, "class", firstTypeAdded ? "that is a" : "");
        firstTypeAdded = true;
    }

    public void addAqlTree(MostGeneralQuery query) {
        if(hasFocus(query) && this.query.getQueryTree().getSubqueries().isEmpty()) {
            addObject(
                    query,
                    "class",
                    "everything",
                    ""
            );
        }
    }

    public void addAqlTree(AQLTree tree) {
        this.addAqlTree(tree, false);
    }



    private void addObject(AQLQueryJsonObject object) {
        verifyBuilding();
        this.activeRow.addElement(object);
    }

    private void addObject(AQLTree tree, String type, String prefix) {
        this.activeRow.addElement(
                new AQLQueryJsonObject(tree.getFocusID(), type, tree.getAQLLabel(), prefix, hasFocus(tree), inFocus)
        );
    }

    private void addObject(AQLTree tree, String type, String label, String prefix) {
        this.activeRow.addElement(
                new AQLQueryJsonObject(tree.getFocusID(), type, label, prefix, hasFocus(tree), inFocus)
        );
    }

    public void addFiller(AQLTree tree, String label) {
        this.addObject(new AQLQueryJsonObject(
                tree.getFocusID(),
                "filler",
                label,
                "",
                hasFocus(tree),
                inFocus
        ));
    }

    private void addRow() {
        verifyBuilding();
        this.rows.add(activeRow);
        this.activeRow = new AQLQueryJsonRow(this.indentation);
    }

    public AQLJsonObject build() {
        verifyBuilding();
        this.isBuilding = false;
        this.rows.add(activeRow);
        return new AQLJsonObject(this.rows, this.virtualFocus, query.getFocusName());
    }

    private void verifyBuilding() {
        if (!this.isBuilding) throw new IllegalStateException("Builder no longer open");
    }

    /**
     * Checks if at the current focus variable, there is a TypeSpecification object, indicating that the query should
     * start with "every"
     *
     * @param tree Tree to check
     * @return True iff the root focus variable includes a type specification
     */
    private boolean hasTypeAtCurrentFocus(AQLTree tree) {
        if (tree instanceof TypeSpecification) {
            return true;
        } else if (tree instanceof CrossingOperator) {
            return false;
        } else {
            for(AQLTree child : tree.getSubqueries()) {
                if (hasTypeAtCurrentFocus(child)) return true;
            }
            return false;
        }
    }

    private boolean hasFocus(AQLTree tree) {
        return this.query.getFocusName().equals(tree.getFocusID());
    }
}
