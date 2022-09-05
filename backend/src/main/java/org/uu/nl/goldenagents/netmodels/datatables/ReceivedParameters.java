package org.uu.nl.goldenagents.netmodels.datatables;

import java.util.List;

public class ReceivedParameters<K> {

    private int draw;

    private int start;

    private int length;

    private String search;

    private String regex;

    private List<ColumnSpec> columns;
}
