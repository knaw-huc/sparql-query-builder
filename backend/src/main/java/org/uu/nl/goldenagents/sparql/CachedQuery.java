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
		String q = "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
			"PREFIX ga: <https://data.goldenagents.org/ontology/>" +
			"PREFIX ns2: <http://schema.org/>" +
			"PREFIX fabi: <http://purl.org/spar/fabio/>" +
			"PREFIX ns4: <http://purl.org/vocab/vann/>" +
			"CONSTRUCT {" +
			"	?sub rdf:type ga:Person ." +
			"	?sub rdfs:label ?name ." +
			"}" +
			"WHERE {" +
			"	?sub rdf:type ns2:Person ." +
			"	?sub rdfs:label ?name ." +
			"}";
		q = query;
		if (offset > 0) {
			q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX ga: <https://data.goldenagents.org/ontology/> CONSTRUCT { ?sub rdf:type ga:Person . } WHERE { filter(false) }";
		};
		return q + " LIMIT " + queryLimit + " OFFSET " + offset; 
	}
	
	public int getIteration() {
		return iteration;
	}
	
	@Override
	public String toString() {
		return "CachedQuery [query=" + query + ", limit=" + queryLimit + ", lastOffset=" + lastOffset + "]";
	}
	
}
