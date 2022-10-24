package org.uu.nl.goldenagents.aql;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.uu.nl.goldenagents.sparql.OpAsQueryWithDisjunction;
import org.uu.nl.net2apl.core.platform.Platform;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SPARQLTranslation implements Serializable {

    private AQLQuery query;
    private transient Op sparqlTranslation;
    private Var focusVar;

    public SPARQLTranslation(AQLQuery query) {
        this.query = query;
        VariableController controller = new VariableController();
        controller.setQueryFocus(query.getFocus());
        String firstLabel = query.getQueryTree().getFirstResourceLabel();
        Var firstVar = firstLabel == null ? controller.getVariable() : controller.getVariableForLabel(firstLabel);
        Op algebra = this.query.getQueryTree().toARQ(firstVar, controller);
        this.focusVar = controller.getFocusVariable();

        Platform.getLogger().log(getClass(), String.format("Focus variable is now \"?%s\"", this.focusVar.getVarName()));

        this.sparqlTranslation = applyFilters(algebra, controller);

        if (controller.getVariableFilterMap().isEmpty()) {
            // Warning: Optimization somehow breaks the custom OpAsQuery implementation, so that one is gone
            this.sparqlTranslation = Algebra.optimize(algebra);
        }
    }

    private Op applyFilters(Op algebra, VariableController controller) {
        Map<Var, List<Node>> variableFilterMap =
                controller.getVariableFilterMap();

        if (!variableFilterMap.isEmpty()) {
            for (Var var : variableFilterMap.keySet()) {
                if(!(controller.isHasMostGenericQueryAtFocus() && controller.getFocusVariable().equals(var))) {
                    ExprList filters = new ExprList(
                            variableFilterMap.get(var)
                                    .stream()
                                    .map(NodeValueNode::new)
                                    .collect(Collectors.toList())
                    );
                    algebra = OpFilter.filter(
                            new E_OneOf(
                                    new ExprVar(var),
                                    filters
                            ),
                            algebra
                    );
                }
            }
        }

        return algebra;
    }

    public Query getQuery() {
        Query q = new OpAsQueryWithDisjunction(this.sparqlTranslation)
                .convertMaintainingDisjunction();
        PrefixMapping mapping = this.query.getPrefixMapping();
        q.setPrefixMapping(mapping);
        return q;
    }

    public String getQueryString() {
        return this.getQuery().toString(Syntax.syntaxSPARQL_11);
    }

    public Op getSparqlTranslation() {
        return sparqlTranslation;
    }

    public Var getFocusVar() {
        return focusVar;
    }
}
