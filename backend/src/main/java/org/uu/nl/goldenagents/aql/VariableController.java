package org.uu.nl.goldenagents.aql;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.uu.nl.goldenagents.netmodels.jena.SerializableResourceImpl;

import java.util.*;

/**
 * The VariableController is used when translating an AQL Query to ARQ, to construct fresh variables
 */
public class VariableController {

    private HashMap<String, Integer> labelStore;
    private Map<Var, List<Node>> variableFilterMap;
    private int labellessIndex;
    private Var focusVariable;
    private AQLTree focus; // for debugging
    private boolean hasMostGenericQueryAtFocus = false;

    /**
     * Default constructor
     */
    public VariableController() {
        this.labelStore = new HashMap<>();
        this.variableFilterMap = new HashMap<>();
        labellessIndex = 0;
    }

    /**
     * Create the next variable name for a given label, which has not been used before
     *
     * @param label     Label to create variable for
     * @return          ARQ Var for label which has not been used before
     */
    public Var getVariableForLabel(String label) {
        if(!this.labelStore.containsKey(label)) {
            this.labelStore.put(label, 0);
        }
        int index = this.labelStore.get(label);
        this.labelStore.put(label, index+1);
        return getVariableForLabel(label, index);
    }

    /**
     * Create the n'th (index) variable name for a given label
     *
     * @param label     Given label
     * @param index     Given index (n)
     * @return          ARQ Var
     */
    public Var getVariableForLabel(String label, int index) {
        try {
            if (index == 0) {
                return Var.alloc(label);
            } else {
                return Var.alloc(String.format("%s_%s", label, indexToVariable(index)));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return Var.alloc("a");
        }
    }

    /**
     * Create the next unlabeled ARQ Var.
     * This method automatically increases the index, so is not without side effects
     * @return  The next ARQ Var
     */
    public Var getVariable() {
        int index = this.labellessIndex;
        this.labellessIndex++;
        return this.getVariableForIndex(index);
    }

    /**
     * Create an ARQ variable object for a given index
     * @param index     Index used for deterministic variable name creation
     * @return          ARQ Var
     */
    public Var getVariableForIndex(int index) {
        return Var.alloc(indexToVariable(index));
    }

    /**
     * Explicitly set a variable as having the focus
     * @param var   ARQ Var that has the active focus
     */
    public void setFocusVariable(Var var) {
        this.focusVariable = var;
    }

    /**
     * Get the ARQ Var with the current focus. May be null
     * @return  ARQ Var
     */
    public Var getFocusVariable() {
        return this.focusVariable;
    }

    /**
     * Create a deterministic string of a letter and a number based on some index. Used as affix to distinguish variable
     * names for the same resource
     * @param index     Index to use for variable affix creation
     * @return          Deterministic string of letter and number
     */
    private static String indexToVariable(int index) {
        int letter = Math.floorMod(index, 26);
        int number = Math.floorDiv(index, 26);
        return String.format("%s%s", (char) (letter + 97), number > 0 ? String.valueOf(number) : "");
    }

    public void setQueryFocus(AQLTree focus) {
        this.focus = focus;
    }

    public int getFocusName() {
        return this.focus.hashCode();
    }

    public void addFilterOnVariable(Var variable, Node filter) {
        List<Node> filters = this.variableFilterMap.get(variable);
        if (filters == null) {
            filters = new ArrayList<>();
        }
        filters.add(filter);
        this.variableFilterMap.put(variable, filters);
    }

    public Map<Var, List<Node>> getVariableFilterMap() {
        return variableFilterMap;
    }

    /**
     * We use this to understand whether to apply filters (e.g., named resource or named literal).
     * The reason is that, contrary to every other AQLTree, which only affects the selection of its children,
     * these filters are applied globally. However, if there is a MostGeneralQuery at the focus (i.e., the variable
     * for that MostGeneralQuery is the same for the focus AQLTree), than the semantics is "select everything that
     * matches the resource, *or* everything else", i.e., no filter should be applied in that case.
     * @return
     */
    public boolean isHasMostGenericQueryAtFocus() {
        return hasMostGenericQueryAtFocus;
    }

    public void setHasMostGenericQueryAtFocus(boolean hasMostGenericQueryAtFocus) {
        this.hasMostGenericQueryAtFocus = hasMostGenericQueryAtFocus;
    }
}
