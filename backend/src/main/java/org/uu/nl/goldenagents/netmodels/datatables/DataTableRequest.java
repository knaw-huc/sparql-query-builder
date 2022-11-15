package org.uu.nl.goldenagents.netmodels.datatables;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

public class DataTableRequest {
    private static final Loggable logger = Platform.getLogger();

    /** The Constant PAGE_NO. */
    public static final String PAGE_NO = "start";

    /** The Constant PAGE_SIZE. */
    public static final String PAGE_SIZE = "length";

    /** The Constant DRAW. */
    public static final String DRAW = "draw";

    public String getUniqueId() {
        return uniqueId;
    }

    public String getDraw() {
        return draw;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getLength() {
        return length;
    }

    public String getSearch() {
        return search;
    }

    public List<ColumnSpec> getColumns() {
        return columns;
    }

    public ColumnSpec getOrder() {
        return order;
    }

    public boolean isGlobalSearch() {
        return isGlobalSearch;
    }

    public int getMaxParamsToCheck() {
        return maxParamsToCheck;
    }

    public UUID getAgentUUID() {
        return agentUUID;
    }

    public String getQueryID() {
        return queryID;
    }

    /** The UUID of the user agent **/
    private UUID agentUUID;

    /** The ID of the conversation the query was processed in **/
    private String queryID;

    /** The unique id. */
    private String uniqueId;

    /** The draw. */
    private String draw;

    /** The start. */
    private Integer start;

    /** The length. */
    private Integer length;

    /** The search. */
    private String search;

    /** The regex. */
    private boolean regex;

    /** The columns. */
    private List<ColumnSpec> columns;

    /** The order. */
    private ColumnSpec order;

    /** The is global search. */
    private boolean isGlobalSearch;

    /** The max params to check. */
    private int maxParamsToCheck = 0;

    public DataTableRequest(HttpServletRequest request) {
        prepareDataTableRequest(request);
    }

    private void prepareDataTableRequest(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();

        if(parameterNames.hasMoreElements()) {

            this.agentUUID = UUID.fromString(request.getParameter("agentUUID"));

            logger.log(DataTableRequest.class, "===========> " + request.getParameter("agentUUID"));

            this.queryID = request.getParameter("queryID");

            this.start = Integer.parseInt(request.getParameter(PAGE_NO));
            this.length = Integer.parseInt(request.getParameter(PAGE_SIZE));
            this.uniqueId = request.getParameter("_");
            this.draw = request.getParameter(DRAW);

            this.search = request.getParameter("search[value]");
            this.regex = Boolean.valueOf(request.getParameter("search[regex]"));

            int sortableCol = Integer.parseInt(request.getParameter("order[0][column]"));

            List<ColumnSpec> columns = new ArrayList<>();

            if(!this.search.isEmpty()) {
                this.isGlobalSearch = true;
            }

            maxParamsToCheck = getNumberOfColumns(request);

            for(int i = 0; i < maxParamsToCheck; i++) {
                String param = request.getParameter(String.format("columns[%d][data]", i));
                if(param != null && !"null".equalsIgnoreCase(param) && !param.isEmpty()) {
                    ColumnSpec spec = new ColumnSpec(request, i);
                    if(i == sortableCol) {
                        this.order = spec;
                    }
                    columns.add(spec);

                    if(!spec.getSearch().isEmpty()) {
                        this.isGlobalSearch = false;
                    }
                }
            }

            if(!columns.isEmpty()) {
                this.columns = columns;
            }

        } else {
            throw new IllegalArgumentException("No request body found");
        }
    }

    private int getNumberOfColumns(HttpServletRequest request) {
        Pattern p = Pattern.compile("columns\\[\\d+\\]\\[data\\]");

        Enumeration params = request.getParameterNames();

        // TODO no need to keep entire list if number is only thing we're interested in, right?
        List<String> lstOfParams = new ArrayList<String>();

        while(params.hasMoreElements()) {
            String paramName = (String)params.nextElement();
            Matcher matcher = p.matcher(paramName);

            if(matcher.matches()) {
                lstOfParams.add(paramName);
            }
        }

        return lstOfParams.size();
    }
}
