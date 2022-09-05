package org.uu.nl.goldenagents.netmodels.angular.aql;

import java.util.ArrayList;
import java.util.List;

public class AQLQueryJsonRow {
    int indentation;
    List<AQLQueryJsonObject> elements;

    public AQLQueryJsonRow() {
        this(0);
    }

    public AQLQueryJsonRow(int indentation) {
        this.indentation = indentation;
        this.elements = new ArrayList<>();
    }

    public int getIndentation() {
        return indentation;
    }

    public List<AQLQueryJsonObject> getElements() {
        return elements;
    }

    public void addElement(AQLQueryJsonObject element) {
        this.elements.add(element);
    }

    public void insertElement(AQLQueryJsonObject element, int position) {
        this.elements.add(position, element);
    }
}
