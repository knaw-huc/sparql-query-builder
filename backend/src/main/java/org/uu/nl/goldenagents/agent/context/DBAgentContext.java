package org.uu.nl.goldenagents.agent.context;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.uu.nl.goldenagents.agent.context.registration.MinimalFunctionalityContext;
import org.uu.nl.goldenagents.decompose.expertise.DbAgentExpertise;
import org.uu.nl.goldenagents.sparql.CachedQuery;
import org.uu.nl.goldenagents.util.DatabaseConfig;
import org.uu.nl.goldenagents.util.agentconfiguration.RdfSourceConfig;
import org.uu.nl.net2apl.core.agent.AgentID;

import java.util.*;

public class DBAgentContext extends MinimalFunctionalityContext {

	private final DatabaseConfig config;
	private final Model ontologyModel;
	private final List<RdfSourceConfig> mappingFiles;
	private Dataset localModel;
	private DbAgentExpertise expertise;
	private final Map<String, CachedQuery> cachedQueries;

	// Entity loading takes a long time. Don't load entities when building expertise for larger sources
	private boolean loadEntitiesForExpertise;

	public DBAgentContext(DatabaseConfig dbConfig, List<RdfSourceConfig> mappingFiles) {
		this.config = dbConfig;
		this.mappingFiles = mappingFiles;
		this.ontologyModel = ModelFactory.createDefaultModel();
		this.cachedQueries = new HashMap<>();
	}

	public String getRdfDataURI() {
		return config.getLocation();
	}

	public boolean isLocalDatasetReady() {
		return localModel != null;
	}

	public DatabaseConfig getConfig() {
		return this.config;
	}

	public void addMapping(Model m) {
		//We should consider keeping the linksets separate
		try {
			this.ontologyModel.add(m);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: JENA throws interesting errors related to model, handle exception
		}
	}

	public void setLocalModel(Dataset model) {
		this.localModel = model;
	}

	/**
	 * Prepares {@code DbQuery} for a given query string
	 * If the local model exists, then uses the local model.
	 * Otherwise, calls Sparql service of the endpoint
	 * @param query String that represent a query
	 * @return DbQuery
	 */
	public DbQuery getDbQuery(String query) {
		return new DbQuery(query);
	}

	/**
	 * Prepares {@code DbQuery} for a given query object
	 * If the local model exists, then uses the local model.
	 * Otherwise, calls Sparql service of the endpoint
	 * @param query Query
	 * @return DbQuery
	 */
	public DbQuery getDbQuery(Query query) {
		return new DbQuery(query);
	}

	/**
	 * TODO the following class can be replaced with the PreparedQueryExecution
	 * Used to enforce the proper closing of all transactions on local data sources
	 */
	public class DbQuery implements AutoCloseable {

		public final QueryExecution queryExecution;

		public DbQuery(String query) {
			this.queryExecution = getQueryExecution(QueryFactory.create(query));
		}

		public DbQuery(Query query) {
			this.queryExecution = getQueryExecution(query);
		}

		private QueryExecution getQueryExecution(Query query) {
			if(config.getMethod().isLocal()) {
				localModel.begin(ReadWrite.READ);
				return QueryExecutionFactory.create(query, localModel);

			} else {
				final QueryEngineHTTP ex =
						QueryExecutionFactory.createServiceRequest(config.getLocation(), query);

				if(config.getDefaultGraph() != null)
					ex.setDefaultGraphURIs(Collections.singletonList(config.getDefaultGraph()));

				ex.setTimeout(10000L);
				return ex;
			}
		}

		private void closeQueryExecution() {
            queryExecution.close();
            if(config.getMethod().isLocal()) localModel.end();
		}


		@Override
		public void close() {
			closeQueryExecution();
		}
	}

	public Model getOntologyModel() {
		return ontologyModel;
	}

	public CachedQuery getCachedQuery(String conversationId) {
		return cachedQueries.get(conversationId);
	}

	public void addCachedQuery(String conversationId, CachedQuery query) {
		this.cachedQueries.put(conversationId, query);
	}

	public void removeCachedQuery(String conversationId) {
		this.cachedQueries.remove(conversationId);
	}

	public int getDbLimit() {
		return config.getDefaultPageSize();
	}

	public void setDbLimit(int dbLimit) {
		this.config.setDefaultPageSize(dbLimit);
	}

	public DbAgentExpertise getExpertise() {
		return expertise;
	}

	public void setExpertise(DbAgentExpertise expertise) {
		this.expertise = expertise;
	}

	public List<RdfSourceConfig> getMappingFiles() {
		return mappingFiles;
	}

	public boolean isLoadEntitiesForExpertise() {
		return loadEntitiesForExpertise;
	}

	public void setLoadEntitiesForExpertise(boolean loadEntitiesForExpertise) {
		this.loadEntitiesForExpertise = loadEntitiesForExpertise;
	}

	/**
	 * This method checks if minimal functionality required by the agent implementing this
	 * class is ready. Without this functionality, this agent cannot function, but even
	 * with this functionality, some other functionality not required for basic functioning may
	 * not be ready yet
	 *
	 * @return True iff all minimal functionality required for this agent to function is ready
	 */
	@Override
	public boolean minimalFunctionalityReady() {
		return this.getDbLimit() >= 0 && this.isMappingsLoaded() && !(config.getMethod().isLocal() && localModel == null);
	}

	/**
	 * Check if all functionality, both minimal required for this agent to function, and additional
	 * functionality, is ready, so this agent can function to its full potential.
	 *
	 * @return True iff all functionality potentially provided by this agent is ready
	 */
	@Override
	public boolean fullFunctionalityReady() {
		return minimalFunctionalityReady() && this.expertise != null;
	}

	public boolean isMappingsLoaded() {
		return this.mappingFiles.stream().allMatch(RdfSourceConfig::isLoaded);
	}
}