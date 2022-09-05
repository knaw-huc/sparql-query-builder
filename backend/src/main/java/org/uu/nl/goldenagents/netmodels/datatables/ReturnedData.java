package org.uu.nl.goldenagents.netmodels.datatables;

/**
 * Once DataTables makes a request, it expects this object as a return type
 *
 * See https://datatables.net/manual/server-side
 */
public class ReturnedData {

    /**
     * The draw counter that this object is a response to - from the draw parameter sent as part of the data request.
     */
    private int draw;

    /**
     * Total records, before filtering
     */
    private int recordsTotal;

    /**
     * Total records, after filtering
     */
    private int recordsFiltered;

    /**
     * The data to be displayed in the table. This is an array of data source objects, one for each row,
     * which will be used by DataTables.
     */
    // TODO array<type> data?

    /**
     * Optional: If an error occurs during the running of the server-side processing script, you can inform the user of
     * this error by passing back the error message to be displayed using this parameter. Do not include if there is
     * no error.
     */
    private String error;
}
