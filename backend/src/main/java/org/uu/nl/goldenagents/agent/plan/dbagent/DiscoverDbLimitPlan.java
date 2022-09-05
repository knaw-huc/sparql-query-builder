package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.sparql.PreparedQueryExecution;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.logging.Level;

public class DiscoverDbLimitPlan extends Plan {

	private Long timeout = null;

	@Override
	public void execute(PlanToAgentInterface planInterface) throws PlanExecutionError {
		final DBAgentContext context = planInterface.getContext(DBAgentContext.class);

		if (timeout == null) {
			this.timeout = context.getConfig().getTimeout();
		}

		try (PreparedQueryExecution ex = new PreparedQueryExecution(
				SparqlUtils.ALL_TRIPLES_SELECT_QUERY,
				context.getConfig())) {
			ex.setTimeout(this.timeout);
			final ResultSet result = ex.queryExecution.execSelect();

			int resultCount = 0;
			while (result.hasNext()) {
				result.next();
				resultCount++;
			}
			// Choose the lower bound if it turns out we can serve fewer results than the imposed limit
			context.setDbLimit(Math.min(resultCount, context.getDbLimit()));
			setFinished(true);
		} catch (ResultSetException e) {
			error(
					planInterface.getAgentID().getName() + " failed to find the DB limit for",
					context.getConfig().toString(),
					e.getMessage()
			);
			throw new PlanExecutionError();
		} catch (QueryExceptionHTTP e) {
			error(
					planInterface.getAgentID().getName() + " failed to find the DB limit for",
					context.getConfig().toString(),
					e.getMessage(),
					e.getResponse()
			);
			if (e.getMessage().contains("java.net.SocketTimeoutException") && this.timeout < 120000L) {
				this.timeout*=2;
				Platform.getLogger().log(getClass(), Level.INFO,
						"Increasing timeout to " + this.timeout + " and trying again");
			} else {
				throw new PlanExecutionError();
			}
		}
	}

	private void error(String... msg) {
		for (String msg_ : msg) {
			if (msg_ == null) msg_ = "NULL";
			Platform.getLogger().log(getClass(), Level.SEVERE, msg_);
		}
	}
}
