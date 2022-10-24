package org.uu.nl.goldenagents.netmodels.fipa;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.uu.nl.goldenagents.netmodels.datatables.DataTableRequest;
import org.uu.nl.goldenagents.netmodels.datatables.DataTableResult;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryResult implements FIPASendableObject {

	private static final long serialVersionUID = 1L;

	private byte[] results;
	private int resultSize;
	private Map<String, Set<AgentID>> mappedSources;
	private transient JsonArray headers;
	private transient JsonArray resultJson;
	private Integer targetAqlQueryID;
	private String queryID;
	
	/**
	 * Empty constructor not to get an error from the methods of this class
	 */
	public QueryResult() {
		this.results = new byte[0];
		this.resultSize = 0;
	}

	public QueryResult(ByteArrayOutputStream byteArrayOutputStream, int numberOfRows) {
		this.results = byteArrayOutputStream.toByteArray();
		this.resultSize = numberOfRows;
		convertByteArrayToJson();
	}
	
	public QueryResult(ByteArrayOutputStream byteArrayOutputStream, int numberOfRows, Map<String, Set<AgentID>> mappedSources) {
		this.results = byteArrayOutputStream.toByteArray();
		this.resultSize = numberOfRows;
		this.mappedSources = mappedSources;
		convertByteArrayToJson();
	}

	private void convertByteArrayToJson() {
		JsonObject jsonResults = JSON.parse(getResultsAsString());
		this.headers = jsonResults.getObj("head").get("vars").getAsArray();
		this.resultJson = jsonResults.getObj("results").get("bindings").getAsArray();
		addSourceInfoToHeaders();
	}
	
	/**
	 * This function maps the sources to the result columns
	 * 
	 */
	private void addSourceInfoToHeaders() {
		//Better practice might be writing a class for headers. We are parsing it as type of "any" in the frontend now
		JsonArray replacedHeaders = new JsonArray();
		for(JsonValue jValue : this.headers) {
			//This is a bit hackish because varilables we get from Jena object are without ? and enclosed with ""
			//TODO When we move to jena classes in query info this fuction should be improved
			String varStr = "?" + jValue.toString().replaceAll("\"", "");
			JsonObject jo = new JsonObject();
			jo.put("name", jValue);
			//It can find source of every variable written in the where clause
			if(mappedSources.containsKey(varStr)) {	
				List<String> sources = mappedSources.get(varStr).stream().map(source -> source.getName().getFragment()).collect(Collectors.toList());
				jo.put("sources", sources.toString());
			}
			else { //This problem occurs since the binds (AS Operator) in the header has not parsed yet
				jo.put("sources", "Source is untraceable");
			}
			replacedHeaders.add(jo);
		}
		this.headers = replacedHeaders;
	}
	
	public byte[] getResults() {
		return this.results;
	}

	public int getResultSize() {
		return this.resultSize;
	}

	public String getHeaderJson() {return this.headers.toString(); }

	/**
	 * Get a subset of the query result
	 *
	 * @return			String containing JSON encoded subset of data
	 */
	public DataTableResult getResults(DataTableRequest request) {

		final String f = request.getSearch().toLowerCase();

		final JsonArray arrayCopy = new JsonArray();

		// Use only results that contain a specific string (case insensitive)
		if(request.isGlobalSearch()) {
			this.resultJson.forEach((value) -> {
				if (f.isEmpty() || value.toString().toLowerCase().contains(f)) {
					arrayCopy.add(value);
				}
			});
		} else {
			arrayCopy.addAll(this.resultJson); // O(n) ?
		}

		// Sort the results according to one column, if a sort column is given
		if(request.getOrder() != null) {
			String sort = request.getOrder().getName();

			arrayCopy.sort((v1, v2) -> { // O(n log(n)) ?
				int compVal = v1.getAsObject().get(sort).getAsObject().get("value").getAsString().value().compareTo(
						v2.getAsObject().get(sort).getAsObject().get("value").getAsString().value()
				);

				return request.getOrder().getSortDir().equals("asc") ? compVal : -1 * compVal;
			});
		}

		int start = Integer.min(request.getStart(), arrayCopy.size() - 1);
		int end = Integer.min(start + request.getLength(), arrayCopy.size() - 1);

		List<Map<String, String>> resultData = new ArrayList<>();
		if(start <= end && start >= 0 && end < arrayCopy.size()) {
			for(int i = start; i <= end; i++) {
				JsonObject rowObject = arrayCopy.get(i).getAsObject();
				Map<String, String> row = new HashMap<>();
				rowObject.forEach((k, v) -> {
					String stringValue = v.getAsObject().get("value").getAsString().value();
					try {
						new URL(stringValue);
						stringValue = String.format("<a href=\"%1$s\" target=\"_blank\">%1$s</a>", stringValue);
					} catch (MalformedURLException e) {
						// Not a URL. Don't do anything
					}
					row.put(k, stringValue);
				});
				resultData.add(row);
			}
		}

		return new DataTableResult(
				request.getUniqueId(),
				request.getDraw(),
				arrayCopy.size(),
				this.resultSize,
				resultData
		);
	}
	
	public String getResultsAsString() {
		return new String(this.results, StandardCharsets.UTF_8);
	}

	public ByteArrayOutputStream getResultsAsCSV() {
		ResultSet tempResultSet = getResultAsTemporaryResultSet();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsCSV(outputStream, tempResultSet);
		return outputStream;
	}

	public ByteArrayOutputStream getResultsAsXML() {
		ResultSet tempResultSet = getResultAsTemporaryResultSet();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsXML(outputStream, tempResultSet);
		return outputStream;
	}

	private ResultSet getResultAsTemporaryResultSet() {
		return ResultSetFactory.fromJSON(new ByteArrayInputStream(this.results));
	}

	public void setQueryID(String queryID) {
		this.queryID = queryID;
	}

	public void setQueryID(Integer targetAqlQueryID) {
		this.targetAqlQueryID = targetAqlQueryID;
	}

	public Integer getTargetAqlQueryID() {
		return targetAqlQueryID;
	}

	public String getQueryID() {
		return queryID;
	}

	@Override
	public String toString() {
		JsonObject jo = new JsonObject();
		jo.put("resultsize", this.resultSize);
		jo.put("headers", this.headers);
		return jo.toString();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		convertByteArrayToJson();
	}
}
