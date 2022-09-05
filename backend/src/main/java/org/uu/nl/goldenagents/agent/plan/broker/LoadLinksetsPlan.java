package org.uu.nl.goldenagents.agent.plan.broker;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.uu.nl.goldenagents.agent.context.BrokerContext;
import org.uu.nl.goldenagents.agent.plan.LoadRDFSourcePlan;
import org.uu.nl.goldenagents.agent.trigger.goal.broker.LoadLinksetGoal;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;

public class LoadLinksetsPlan extends LoadRDFSourcePlan {

	private final LoadLinksetGoal goal;
	
	public LoadLinksetsPlan(LoadLinksetGoal goal) {
		this.goal = goal;
	}
	
	@Override
	public void executeOnce(PlanToAgentInterface planInterface) throws PlanExecutionError {
		BrokerContext c = planInterface.getAgent().getContext(BrokerContext.class);
		Model model = ModelFactory.createDefaultModel();
		this.loadModel(planInterface, model, c.getLinksetConfigs());
		c.addLinkset(model);

		// Done!
		this.goal.setAchieved(true);
	}


}
