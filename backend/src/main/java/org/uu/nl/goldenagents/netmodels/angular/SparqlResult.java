package org.uu.nl.goldenagents.netmodels.angular;

public class SparqlResult {

	private int firstIndex;
	private int lastIndex;
	private String results;
	private String conversationId;
	private String uuid;
	
	public SparqlResult() {};
	
	public SparqlResult(int firstIndex, int lastIndex, String results, String conversationId, String uuid) {
		super();
		this.firstIndex = firstIndex;
		this.lastIndex = lastIndex;
		this.results = results;
		this.conversationId = conversationId;
		this.uuid = uuid;
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public void setFirstIndex(int firstIndex) {
		this.firstIndex = firstIndex;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
