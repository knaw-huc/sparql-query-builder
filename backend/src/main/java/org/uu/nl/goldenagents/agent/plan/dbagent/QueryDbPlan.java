package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.agent.context.query.DbTranslationContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.netmodels.fipa.*;
import org.uu.nl.goldenagents.sparql.CachedQuery;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.util.logging.Level;

public class QueryDbPlan extends MessagePlan  {

	private PlanToAgentInterface planInterface;
	private DBAgentContext context;

	private String query;
	private AgentQuery agentQuery;
	private Integer targetAqlQueryID = null;

	public QueryDbPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
		super(message, header, content);
	}

	@Override
	public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
		this.planInterface = planInterface;
		this.context = planInterface.getContext(DBAgentContext.class);

		// Obtain agent query object and CONSTRUCT query
		prepareQuery();

		if(agentQuery != null && agentQuery.getPrefixError() != null) {
			// Inform broker agent of ambiguous use of prefixes in query
			try {
				sendErrorMessage(
						planInterface,
						new SubGraph(targetAqlQueryID, agentQuery.getPrefixError())
				);
			} catch (IOException e) {
				logError(e);
			}
		} else {
			// Execute query on encapsulated data base
			executeQuery();
		}
	}

	/**
	 * Create or obtain an AgentQuery object based on the request from the broker agents and create
	 * a construct query
	 *
	 * @throws PlanExecutionError 	If broker agent request was unknown
	 */
	private void prepareQuery() throws PlanExecutionError {
		switch(header) {
			default:
				// Unknown header
				logger.log(QueryDbPlan.class, Level.SEVERE, "Unexpected header: " + header);
				throw new PlanExecutionError();
			case BROKER_QUERY:
				// This header indicates a new query process should be started. Create new AgentQuery object
				this.agentQuery = (AgentQuery) content;
				this.agentQuery.translate(planInterface.getContext(DbTranslationContext.class));
				this.targetAqlQueryID = this.agentQuery.getTargetAqlQueryID();

				// Create the CONSTRUCT query and cache it in context
				String constructQuery = this.agentQuery.createConstruct(planInterface.getAgentID());
				this.context.addCachedQuery(this.message.getConversationId(), this.targetAqlQueryID, new CachedQuery(constructQuery,
						this.agentQuery.getTriples().length, this.context.getDbLimit()));
				this.query = this.context.getCachedQuery(this.message.getConversationId(), this.targetAqlQueryID).getNextQuery();

				logger.log(QueryDbPlan.class, "\n" + constructQuery);
				break;
			case BROKER_ACK:
				// Broker process has previously started. Get CONSTRUCT query from context object
				GaMessageContentObjectContainer<Integer> contentObject = ((GaMessageContentObjectContainer<Integer>) content);
				this.targetAqlQueryID = contentObject == null ? null : contentObject.getContent();
				this.query = context.getCachedQuery(this.message.getConversationId(), this.targetAqlQueryID).getNextQuery();
				break;
		}
	}

	/**
	 * Execute the query for this request and handle the results
	 */
	private void executeQuery() {
		try {
			// Perform query on encapsulated database
			Model model = null;
			try(DBAgentContext.DbQuery dbQuery = context.getDbQuery(query)) {
				model = dbQuery.queryExecution.execConstruct();
			} catch (Exception e) {
				Platform.getLogger().log(getClass(), e);
			}

			if(model == null || model.isEmpty()) {
				// Empty model after query. Means all results have been collected
				handleEmptyModel();
			} else {
				// Results found. Send as next batch to broker agent
				sendResults(model);
			}
		} catch (Exception e) {
			String reason = logError(e);
			try {
				sendErrorMessage(planInterface, new SubGraph(this.targetAqlQueryID, reason));
			} catch (IOException ex) {
				logError(e);
			}
		}
	}

	/**
	 * Log error to log and extract reason from error
	 * @param e		Thrown error to log
	 * @return		Reason error was thrown
	 */
	private String logError(Exception e) {
		DBAgentContext c = this.planInterface.getContext(DBAgentContext.class);
		String dataUri = c.getRdfDataURI();
		String endpoint = c.getConfig().getDefaultGraph() == null ?
				dataUri : dataUri + "::" + c.getConfig().getDefaultGraph();
		String reason = e.getMessage() == null ? e.toString() : e.getMessage();

		logger.log(QueryDbPlan.class, Level.SEVERE,
				String.format("Unable to execute query on %s! Because: %s", endpoint, reason));
		logger.log(QueryDbPlan.class, Level.SEVERE, e);
		logger.log(QueryDbPlan.class, Level.SEVERE, query);

		return reason;
	}

	/**
	 * Send the results to the broker agent after succesful query execution
	 * @param model 	The Model in which query results are temporarily stored
	 */
	private void sendResults(Model model) {
		try {
			SubGraph subGraph = new SubGraph(model, SubGraph.Status.INTERMEDIATE, this.targetAqlQueryID);

			CachedQuery cachedQuery = context.getCachedQuery(this.message.getConversationId(), this.targetAqlQueryID);
			ACLMessage response = this.message.createReply(planInterface.getAgentID(), Performative.INFORM_REF);

			if(header == GAMessageHeader.BROKER_QUERY) {
				// New query request; inform broker this is the first batch of data
				response.setContentObject(new GAMessageContentWrapper(GAMessageHeader.DB_DATA_START, subGraph));
			} else {
				// Query process already started, inform broker this result set is the next batch
				response.setContentObject(new GAMessageContentWrapper(GAMessageHeader.DB_DATA_CONTINUE, subGraph));
			}

			logger.log(QueryDbPlan.class, "Sending partial model " +
					cachedQuery.getIteration() + " to broker in conversation " +
					response.getConversationId());
			planInterface.getAgent().sendMessage(response);

		} catch (IOException | MessageReceiverNotFoundException | PlatformNotFoundException e) {
			logger.log(QueryDbPlan.class, e);
		}
	}

	/**
	 * Handle an empty model after query execution. This indicates all results have been collected and the query
	 * negotiation process can end
	 */
	private void handleEmptyModel() {
		logger.log(QueryDbPlan.class,
				"There is not any more result for the query. Cached query is removed for conversation " +
						this.message.getConversationId());

		try {
			SubGraph subGraph = new SubGraph(targetAqlQueryID);
			ACLMessage response = this.message.createReply(planInterface.getAgentID(), Performative.INFORM_REF);
			response.setContentObject(new GAMessageContentWrapper(GAMessageHeader.DB_DATA_END, subGraph));

			logger.log(QueryDbPlan.class, "Notifying broker of end of data " + response.getConversationId());
			planInterface.getAgent().sendMessage(response);

		} catch(IOException | MessageReceiverNotFoundException | PlatformNotFoundException e) {
			logger.log(QueryDbPlan.class, e);
		} finally {
			context.removeCachedQuery(this.message.getConversationId());
		}
	}
}
