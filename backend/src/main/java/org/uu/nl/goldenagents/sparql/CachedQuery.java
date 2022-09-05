package org.uu.nl.goldenagents.sparql;

public class CachedQuery {
	
	private final String query;

	private final int queryLimit;
	private int lastOffset;
	private int iteration;
	
	public CachedQuery(String query, int statements, int dbLimit) {
		this.query = query;
		this.queryLimit = dbLimit / statements;
		this.lastOffset = 0;
		this.iteration = 0;
	}
	
	public String getNextQuery() {
		int offset = lastOffset;
		this.lastOffset += queryLimit;
		this.iteration++;
		return query + " LIMIT " + queryLimit + " OFFSET " + offset; 
	}
	
	public int getIteration() {
		return iteration;
	}
	
	@Override
	public String toString() {
		return "CachedQuery [query=" + query + ", limit=" + queryLimit + ", lastOffset=" + lastOffset + "]";
	}
	
}
