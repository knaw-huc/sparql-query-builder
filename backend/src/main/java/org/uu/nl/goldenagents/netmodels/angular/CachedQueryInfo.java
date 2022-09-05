package org.uu.nl.goldenagents.netmodels.angular;

import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;

import java.util.Date;

/**
 * Net model to encode all required information to display a cached query in a datatables enabled frontend
 */
public class CachedQueryInfo {
    private String headerJson;
    private String queryID;
    private String queryString;
    private String conversationID;
    private Date added;
    private Date resultsCollected;
    private String[] selectedSources;
    private boolean finished = false;

    public CachedQueryInfo(UserQueryTrigger trigger) {
        this.queryID = trigger.getQueryID();
        this.queryString = trigger.getQuery();
        this.selectedSources = trigger.getSelectedSources();
        finished = false;
        added = new Date();
    }

    /**
     * For queries that are not yet finished processing, this constructor is used
     * @param queryID       ID of the query
     * @param queryString   String containing original user query
     */
    public CachedQueryInfo(String queryID, String queryString) {
        this.queryID = queryID;
        this.queryString = queryString;
        finished = false;
        added = new Date();
    }

    /**
     * For queries that have results available and are thus finished processing, this constructor is used
     * @param queryID       ID of the query
     * @param headerJson    JSON encoded list of headers for results
     * @param queryString   String containing original user query
     */
    public CachedQueryInfo(String queryID, String queryString, String headerJson) {
        this.queryID = queryID;
        this.queryString = queryString;
        this.headerJson = headerJson;
        this.finished = true;
        this.added = new Date();
        this.resultsCollected = new Date();
    }

    /**
     * Notify this object that collecting results has finished
     *
     * @param headerJson    JSON encoded list of headers of result set
     */
    public void setFinished(String headerJson) {
        this.resultsCollected = new Date();
        this.finished = true;
        this.headerJson = headerJson;
    }

    public String getHeaders() {
        return this.headerJson;
    }

    public String getQueryID() {
        return queryID;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getConversationID() {
        return conversationID;
    }

    public Date getAdded() {
        return added;
    }

    public Date getResultsCollected() {
        return resultsCollected;
    }

    public boolean isFinished() {
        return finished;
    }

    public String[] getSelectedSources() {
        return this.selectedSources;
    }

    public void setSelectedSources(String[] selectedSources) {
        this.selectedSources = selectedSources;
    }
}
