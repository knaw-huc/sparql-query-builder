package org.uu.nl.goldenagents.netmodels.angular.aql;

import org.uu.nl.goldenagents.aql.AQLTree;

public class AQLQueryJsonObject {
    // Unique name of the node
    private AQLTree.ID name;

    // Type of the node (one of class, instance, forward crossing or backward crossing
    private String type;

    // The human-readable label for presentation
    private String label;

    // A string prefixing the human-readable label
    private String prefix;

    // Is this node the root of the focus tree
    private boolean isFocus;

    // Is this node in the focus tree
    private boolean isInFocus;

    // The node to switch focus to when this object is selected (preferably using a parent)
    private AQLTree.ID focusTarget;

    public AQLQueryJsonObject(AQLTree.ID name, String type, String label, String prefix, boolean isFocus, boolean isInFocus, AQLTree.ID focusTarget) {
        this.name = name;
        this.type = type;
        this.label = label;
        this.prefix = prefix;
        this.isFocus = isFocus;
        this.isInFocus = isInFocus;
        this.focusTarget = focusTarget;
    }

    public AQLTree.ID getName() {
        return name;
    }

    public void setName(AQLTree.ID name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        this.isFocus = focus;
    }

    public boolean isInFocus() {
        return isInFocus;
    }

    public void setInFocus(boolean inFocus) {
        isInFocus = inFocus;
    }

    public AQLTree.ID getFocusTarget() {
        return focusTarget;
    }

    public void setFocusTarget(AQLTree.ID focusTarget) {
        this.focusTarget = focusTarget;
    }
}
