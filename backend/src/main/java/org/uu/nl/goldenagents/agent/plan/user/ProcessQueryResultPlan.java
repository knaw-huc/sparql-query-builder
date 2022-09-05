package org.uu.nl.goldenagents.agent.plan.user;

import org.uu.nl.goldenagents.agent.context.DirectSsePublisher;
import org.uu.nl.goldenagents.agent.context.query.QueryProgressType;
import org.uu.nl.goldenagents.agent.context.query.QueryResultContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.netmodels.angular.CachedQueryInfo;
import org.uu.nl.goldenagents.netmodels.angular.QueryProgress;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.QueryResult;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.logging.Level;

public class ProcessQueryResultPlan extends MessagePlan {
	
	public ProcessQueryResultPlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
		super(message, header, content);
	}

	@Override
	public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
		
		// Store results in a separate context
		QueryResultContext results = planInterface.getContext(QueryResultContext.class);
		String queryID = results.getQueryIDForConversation(receivedMessage.getConversationId());

		QueryResult result = (QueryResult) content;
		
		CachedQueryInfo cachedQueryInfo = results.addResults(queryID, result);
		
		DirectSsePublisher publisher = planInterface.getAgent().getContext(DirectSsePublisher.class);
		if(publisher != null) {
			QueryProgress<CachedQueryInfo> update = new QueryProgress<>(queryID, QueryProgressType.RESULTS_COLLECTED, true);
			update.setValue(cachedQueryInfo);
			publisher.publishQueryProgress(update);
		} else {
			Platform.getLogger().log(getClass(), Level.SEVERE, "No publisher context found on user agent");
		}

	}

}
