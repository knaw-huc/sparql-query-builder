package org.uu.nl.goldenagents.agent.plan.dbagent;

import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb2.TDB2Factory;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;

public class LoadLocalModelPlan extends RunOncePlan {

    @Override
    public void executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {

        final DBAgentContext context = planToAgentInterface.getContext(DBAgentContext.class);

        if(context.getConfig().getMethod().isLocal()) {

            final Dataset ds;

            switch (context.getConfig().getMethod()) {
                case TDB2:
                    ds = TDB2Factory.connectDataset(context.getRdfDataURI());
                    break;
                case FILE:
                    ds = RDFDataMgr.loadDataset(context.getRdfDataURI());
                    break;
                default:
                    ds = null;
                    break;
            }

            context.setLocalModel(ds);
        }

    }
}
