package org.uu.nl.goldenagents.netmodels.angular.aql;

import java.util.List;
import java.util.UUID;

public class AQLJsonObject {

    private final List<AQLQueryJsonRow> rows;
    private final UUID virtualFocus;
    private final UUID focus;

    public AQLJsonObject(List<AQLQueryJsonRow> rows, UUID virtualFocus, UUID focus) {
        this.rows = rows;
        this.virtualFocus = virtualFocus;
        this.focus = focus;
    }

    public List<AQLQueryJsonRow> getRows() {
        return rows;
    }

    public UUID getVirtualFocus() {
        return virtualFocus;
    }

    public UUID getFocus() {
        return focus;
    }
}
