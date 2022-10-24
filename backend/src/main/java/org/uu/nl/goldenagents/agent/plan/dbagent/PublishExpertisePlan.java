package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.goldenagents.agent.context.query.DbTranslationContext;
import org.uu.nl.goldenagents.agent.plan.MessagePlan;
import org.uu.nl.goldenagents.agent.planscheme.dbagent.DBAgentGoalPlanScheme;
import org.uu.nl.goldenagents.agent.trigger.goal.dbagent.DiscoverExpertiseGoal;
import org.uu.nl.goldenagents.agent.trigger.goal.dbagent.PublishExpertiseGoal;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.jena.RDFNameSpaceMap;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.util.Map;

public class PublishExpertisePlan extends MessagePlan {

	private static Loggable logger = Platform.getLogger();
	private PublishExpertiseGoal goal;
	
	public PublishExpertisePlan(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
		super(message, header, content);
	}

	public PublishExpertisePlan(PublishExpertiseGoal goal) {
		super(goal.getMessage(), goal.getHeader(), goal.getContent());
		this.goal = goal;
	}

	@Override
	public void executeOnce(PlanToAgentInterface planInterface, ACLMessage receivedMessage, GAMessageHeader header, FIPASendableObject content) throws PlanExecutionError {
		
		DBAgentContext context = planInterface.getContext(DBAgentContext.class);
		PrefixNSListenerContext prefixContext = planInterface.getContext(PrefixNSListenerContext.class);

		Map<String, String> prefixMap = ((RDFNameSpaceMap)content).getNamespaceMap();
		if(prefixContext.setPrefixMap(receivedMessage.getSender(), prefixMap)) {
			// With the next call, this ensures that expertise will be updated before being sent to broker
			context.setExpertise(null);
			SparqlUtils.updatePrefixesInModel(context.getOntologyModel(), prefixContext.getPrefixMap());
			planInterface.getAgent().addContext(new DbTranslationContext(context.getOntologyModel(), prefixContext));
		}

		if(context.getExpertise() == null) {
			planInterface.adoptPlan(new DBAgentGoalPlanScheme.LoadGoalPlan(
					new DiscoverExpertiseGoal(),
					DBAgentGoalPlanScheme.LoadGoalPlan.LOAD_WHEN.NOT_PURSUING_SAME_TYPE)
			);
			planInterface.adoptPlan(new DBAgentGoalPlanScheme.LoadGoalPlan(
					new PublishExpertiseGoal(receivedMessage, header, content),
					DBAgentGoalPlanScheme.LoadGoalPlan.LOAD_WHEN.NOT_PURSUING_SAME_TYPE)
			);
		} else {
			try {
				logger.log(PublishExpertisePlan.class, "Publishing " + planInterface.getAgentID() + " expertise information to broker");
				ACLMessage response = receivedMessage.createReply(planInterface.getAgentID(), Performative.INFORM_REF);

				response.setContentObject(new GAMessageContentWrapper(GAMessageHeader.DB_EXPERTISE, context.getExpertise()));
				planInterface.getAgent().sendMessage(response);

				if(this.goal != null) {
					this.goal.setAchieved(true);
				}

			} catch (IOException | MessageReceiverNotFoundException | PlatformNotFoundException ex) {
				logger.log(PublishExpertisePlan.class, ex);
				throw new PlanExecutionError();
			}
		}
	}
}
