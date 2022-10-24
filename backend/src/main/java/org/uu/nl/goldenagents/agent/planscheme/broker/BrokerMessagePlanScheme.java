package org.uu.nl.goldenagents.agent.planscheme.broker;

import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.agent.plan.broker.AddDbExpertisePlan;
import org.uu.nl.goldenagents.agent.plan.broker.BrokerRegistrationPlan;
import org.uu.nl.goldenagents.agent.plan.broker.SendPreferredNamespacePrefixesPlan;
import org.uu.nl.goldenagents.agent.plan.broker.mergeresult.HandleDataEndPlan;
import org.uu.nl.goldenagents.agent.plan.broker.mergeresult.HandleDataErrorPlan;
import org.uu.nl.goldenagents.agent.plan.broker.mergeresult.HandleDataReceivedPlan;
import org.uu.nl.goldenagents.agent.plan.broker.splitquery.SkipDbForSuggestionsPlan;
import org.uu.nl.goldenagents.agent.plan.broker.suggestions.ReceiveDBSuggestionsPlan;
import org.uu.nl.goldenagents.agent.plan.broker.suggestions.SimpleSuggestSearchOptionsPlan;
import org.uu.nl.goldenagents.agent.plan.broker.splitquery.SplitQueryExpertise;
import org.uu.nl.goldenagents.agent.plan.broker.splitquery.SplitQuerySimple;
import org.uu.nl.goldenagents.agent.plan.registration.PrepareRegistrationWithDFPlan;
import org.uu.nl.goldenagents.agent.planscheme.RegistrationPlanScheme;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;

public class BrokerMessagePlanScheme extends RegistrationPlanScheme {

	public Plan handleMessage(ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) {
		Platform.getLogger().log(getClass(), String.format(
				"Received message from %s with header %s",
				receivedMessage.getSender(),
				header
		));
		switch (receivedMessage.getPerformative()) {
			default:
				break;
			case PROPOSE:
				return new PrepareRegistrationWithDFPlan(false, receivedMessage.getSender());
			case INFORM:
				return new BrokerRegistrationPlan(receivedMessage, header, content);
		}

		switch (header) {
			default:
				break;
			case DB_DATA_START: // WARNING! Intentionally cascades over!
			case DB_DATA_CONTINUE:
				return new HandleDataReceivedPlan(receivedMessage, header, content);
			case DB_DATA_END:
				return new HandleDataEndPlan(receivedMessage, header, content);
			case DB_ERROR:
				return new HandleDataErrorPlan(receivedMessage, header, content);
			case DB_EXPERTISE:
				return new AddDbExpertisePlan(receivedMessage, header, content);
			case USER_QUERY:
				return new SplitQuerySimple(receivedMessage, header, content);
			case USER_INTELLIGENT_SEARCH:
				return new SplitQueryExpertise(receivedMessage, header, content);
			case REQUEST_SUGGESTIONS:
				return new SimpleSuggestSearchOptionsPlan(receivedMessage, header, content);
			case REQUEST_IMPROVE_SUGGESTIONS:
				// TODO. At some point. I guess.
				break;
			case REQUEST_PREFIX_MAPPING:
				return new SendPreferredNamespacePrefixesPlan(receivedMessage, header, content);
			case REQUEST_DATA_BASED_SUGGESTIONS:
				UserQueryTrigger trigger = UserQueryTrigger.fromACLMessage(receivedMessage);
				BrokerSearchSuggestionsContext context = getContext(BrokerSearchSuggestionsContext.class);
				BrokerSearchSuggestionsContext.SearchSuggestionSubscription sub =
						context.getSubscription(receivedMessage.getConversationId());

				if (sub != null && sub.hasResultForQuery(trigger.getAql())) {
					return new SkipDbForSuggestionsPlan(receivedMessage, header, content);
				} else {
					SplitQueryExpertise plan = new SplitQueryExpertise(receivedMessage, header, content);
					plan.setSuggestionsRequested(true);
					return plan;
				}
			case INFORM_SUGGESTIONS:
				return new ReceiveDBSuggestionsPlan(receivedMessage, header, content);
		}

		return Plan.UNINSTANTIATED;
	}

}
