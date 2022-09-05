package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.agent.context.query.DbTranslationContext;
import org.uu.nl.goldenagents.agent.plan.LoadRDFSourcePlan;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;

public class LoadMappingsPlan extends LoadRDFSourcePlan {

    @Override
    public void executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        DBAgentContext context = planToAgentInterface.getAgent().getContext(DBAgentContext.class);
        updateModel(planToAgentInterface, context);
        updateTranslationContext(planToAgentInterface, context);
    }

    private void updateModel(PlanToAgentInterface planToAgentInterface, DBAgentContext context) {
        Model model = ModelFactory.createDefaultModel();
        this.loadModel(planToAgentInterface, model, context.getMappingFiles());
        context.addMapping(model);
    }

    private void updateTranslationContext(PlanToAgentInterface planToAgentInterface, DBAgentContext context) {
        DbTranslationContext translationContext = planToAgentInterface.getContext(DbTranslationContext.class);
        StmtIterator it = context.getOntologyModel().listStatements();
        while(it.hasNext()) {
            translationContext.processStatement(it.nextStatement());
        }
    }
}
