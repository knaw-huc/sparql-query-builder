package org.uu.nl.goldenagents.netmodels.datatables;

import java.util.List;
import java.util.Map;

public class DataTableResult {
    public String getUniqueId() {
        return uniqueId;
    }

    public String getDraw() {
        return draw;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public List<Map<String, String>> getData() {
        return data;
    }

    /** The unique id. */
    private String uniqueId;

    private String draw;

    private int recordsFiltered;

    private int recordsTotal;

    private List<Map<String, String>> data;

    public DataTableResult(String uniqueId, String draw, int recordsFiltered, int recordsTotal, List<Map<String, String>> data) {
        this.uniqueId = uniqueId;
        this.draw = draw;
        this.recordsFiltered = recordsFiltered;
        this.recordsTotal = recordsTotal;
        this.data = data;
    }
}
