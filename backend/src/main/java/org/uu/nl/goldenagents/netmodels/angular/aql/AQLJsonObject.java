package org.uu.nl.goldenagents.netmodels.angular.aql;

import org.uu.nl.goldenagents.aql.AQLTree;

import java.util.List;
import java.util.UUID;

public class AQLJsonObject {

    private final UUID conversationID;
    private final List<AQLQueryJsonRow> rows;
    private final AQLTree.ID virtualFocus;
    private final AQLTree.ID focus;
    private final String queryID;

    public AQLJsonObject(List<AQLQueryJsonRow> rows, UUID conversationID, AQLTree.ID virtualFocus, AQLTree.ID focus, String queryID) {
        this.rows = rows;
        this.conversationID = conversationID;
        this.virtualFocus = virtualFocus;
        this.focus = focus;
        this.queryID = queryID;
    }

    public List<AQLQueryJsonRow> getRows() {
        return rows;
    }

    public UUID getConversationID() {
        return conversationID;
    }

    public AQLTree.ID getVirtualFocus() {
        return virtualFocus;
    }

    public AQLTree.ID getFocus() {
        return focus;
    }

    public String getQueryID() {
        return queryID;
    }
}
