package org.uu.nl.goldenagents.sparql;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.uu.nl.goldenagents.util.agentconfiguration.RdfSourceConfig;

import java.util.Collections;

/**
 * Used to enforce the proper closing of all transactions on local data sources
 */
public class PreparedQueryExecution implements AutoCloseable {

	public final QueryEngineHTTP queryExecution;
	private final RdfSourceConfig source;

	public PreparedQueryExecution(String query, RdfSourceConfig source) {
		this.source = source;
		this.queryExecution = QueryExecutionFactory.createServiceRequest(source.getLocation(), QueryFactory.create(query));
		if (source.getDefaultGraph() != null)
			this.queryExecution.setDefaultGraphURIs(Collections.singletonList(source.getDefaultGraph()));
		setTimeout(source.getTimeout());
	}

	public void setTimeout(long timeout) {
		this.queryExecution.setTimeout(timeout);
	}

	private void closeQueryExecution() {
       queryExecution.close();
	}

	@Override
	public void close() {
		closeQueryExecution();
	}
}
