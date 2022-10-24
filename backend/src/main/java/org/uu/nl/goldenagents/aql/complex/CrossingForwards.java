package org.uu.nl.goldenagents.aql.complex;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.VariableController;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.HashMap;

/**
 * Forward crossing properties are those properties p ( S , . )
 * This means for a feature <i>feature</i>, the triple becomes { ?value prop feature }, or
 *
 * GP(x, p of q1) = ?y p ?x . GP(y, q1)             where y is a fresh variable
 *
 */
public class CrossingForwards extends CrossingOperator {

    private static final String AQL_LABEL = "of";

    public CrossingForwards(SerializableResourceImpl resource, AQLTree subquery) {
        super(resource, subquery);
    }

    public CrossingForwards(SerializableResourceImpl resource, AQLTree subquery, String label) {
        super(resource, subquery, label);
    }

    private CrossingForwards(SerializableResourceImpl resource, AQLTree subquery, String label, ID focusName, ID parent) {
        super(resource, subquery, label, focusName, parent);
    }

    public Op toARQ(Var var, VariableController controller) {
        checkIfFocus(var, controller);
        Var freshVar = controller.getVariableForLabel(this.resource.getLocalName());
        Triple t = new Triple(freshVar, this.resource.asNode(), var);
        BasicPattern bp = new BasicPattern();
        bp.add(t);
        Op child = subquery.toARQ(freshVar, controller);
        return OpJoin.createReduce(new OpBGP(bp), child);
    }

    /**
     * Convert this query to an AQL string recursively
     *
     * @return An AQL query string
     */
    @Override
    public String toAQLString() {
        return null;
    }

    /**
     * Convert this tree to a natural language representation
     *
     * @return NL representation of this query
     */
    @Override
    public String toNLQuery() {
        // TODO
        return "";
    }

    @Override
    public AQLTree copy(ID parent, HashMap<ID, AQLTree> foci) {
        AQLTree child = getSubquery().copy(getFocusName(), foci);
        CrossingForwards copy = new CrossingForwards(this.resource, child, label, this.getFocusName(), parent);
        foci.put(copy.getFocusName(), copy);
        return copy;
    }
}
