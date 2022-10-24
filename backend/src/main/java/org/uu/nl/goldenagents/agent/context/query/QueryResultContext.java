package org.uu.nl.goldenagents.agent.context.query;

import org.uu.nl.goldenagents.netmodels.angular.CachedQueryInfo;
import org.uu.nl.goldenagents.netmodels.fipa.QueryResult;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.net2apl.core.agent.Context;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class QueryResultContext implements Context {

	// Key: QueryID
	private Map<String, CachedQueryInfo> queryHistory = new LinkedHashMap<>();

	// Key: Conversation ID, Vale: (UUID or, in the case of AQL, unsigned integer)
	private Map<String, String> conversationIDQueryHashMap = new HashMap<>();

	// Key: QueryID (UUID or, in the case of AQL, unsigned integer)
	private Map<String, QueryResult> resultMap = new HashMap<>();

	// Key: QueryID (UUID or, in the case of AQL, unsigned integer)
	private Map<String, UserQueryTrigger> queryMap = new HashMap<>();

	// ID for last query that was initiated
	private String lastQueryID;

	// ID for last query for which results have been fully processed (may be same as lastQueryID).
	private String lastResultsID;
	
	public CachedQueryInfo addResults(String queryID, QueryResult result) {
		this.resultMap.put(queryID, result);
		lastResultsID = queryID;
		if(queryHistory.containsKey(queryID)) {
			queryHistory.get(queryID).setFinished(result.getHeaderJson());
		}
		return queryHistory.get(queryID);
	}

	/**
	 * Store a query by its query hash. The hash can be used to recall the query later
	 */
	public void addQuery(UserQueryTrigger queryTrigger) {
		this.queryMap.put(queryTrigger.getQueryID(), queryTrigger);
		this.lastQueryID = queryTrigger.getQueryID();
		this.queryHistory.put(queryTrigger.getQueryID(), new CachedQueryInfo(queryTrigger));
	}

	/**
	 * Get a query for a given hash. May be null of query cannot be found
	 * @param queryID 	MD5 hash of the query
	 * @return				Raw query string
	 */
	public String getQueryString(String queryID) {
		if(this.queryHistory.containsKey(queryID)) {
			return this.queryHistory.get(queryID).getQueryString();
		}

		return null;
	}

	/**
	 * Get the last query that was asked of the user agent
	 * @return 		Raw query string or null if no queries were handled by this agent yet
	 */
	public String getLastQueryString() {
		return getQueryString(lastQueryID);
	}

	/**
	 * Get the last query that was completely handled by this user agent (meaning results were also received)
	 * @return 		Raw query string or null if no queries with results are in the history
	 */
	public String getLastFinishedQueryString() {
		return getQueryString(lastResultsID);
	}

	/**
	 * Get the query result for the last finished query
	 * @return 	Query result for last fully handled query, or null if no results are available yet
	 */
	public QueryResult getLastQueryResult() {
		return getResult(getLastFinishedQueryString());
	}

	/**
	 * Store the conversation ID of the conversation between a broker and a user agent where this query was discussed
	 * @param queryID 		MD5 hash of the query
	 * @param conversationID 	Conversation ID of conversation in which query was sent
	 */
	public void addConversationIDForQueryID(String queryID, String conversationID) {
		if(!this.queryMap.containsKey(queryID)) {
			throw new IllegalStateException("Query has not been stored for this query hash");
		} else {
			conversationIDQueryHashMap.put(conversationID, queryID);
			this.queryHistory.get(queryID).setConversationID(conversationID);
		}
	}

	public String getQueryIDForConversation(String conversationID) {
		return this.conversationIDQueryHashMap.get(conversationID);
	}

	/**
	 * Get the result for a a query
	 * @param queryID 	MD5 hash of the query
	 * @return 				Query result, or null if no results exist for this query
	 */
	public QueryResult getResult(String queryID) {
		return this.resultMap.get(queryID);
	}

	/**
	 * Get an output stream encoding the results as a CSV format for the given query
 	 * @param queryID 	ID of the query for which output should be streamed as CSV
	 * @return			ByteArrayOutputStream encoding results in CSV format
	 */
	public ByteArrayOutputStream getCSVStreamForQuery(String queryID) {
		if(this.resultMap.containsKey(queryID)) {
			return this.resultMap.get(queryID).getResultsAsCSV();
		}
		return null;
	}

	/**
	 * Get an output stream encoding the results as a XML format for the given query
	 * @param queryID 	ID of the query for which output should be streamed as XML
	 * @return			ByteArrayOutputStream encoding results in XML format
	 */
	public ByteArrayOutputStream getXMLStreamForQuery(String queryID) {
		if(this.resultMap.containsKey(queryID)) {
			return this.resultMap.get(queryID).getResultsAsXML();
		}
		return null;
	}

	public Collection<CachedQueryInfo> queryHistory() {
		return this.queryHistory.values();
	}
}
