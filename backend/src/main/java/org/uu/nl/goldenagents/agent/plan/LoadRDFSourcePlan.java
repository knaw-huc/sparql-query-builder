package org.uu.nl.goldenagents.agent.plan;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.uu.nl.goldenagents.sparql.PreparedQueryExecution;
import org.uu.nl.goldenagents.util.SparqlUtils;
import org.uu.nl.goldenagents.util.agentconfiguration.RdfSourceConfig;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.List;
import java.util.logging.Level;

public abstract class LoadRDFSourcePlan extends RunOncePlan {

    protected void loadModel(PlanToAgentInterface planInterface, Model model, List<RdfSourceConfig> configList) {
        for(RdfSourceConfig config : configList) {
            if(!config.isLoaded()) {
                if (config.isLocal()) {
                    model.add(loadLocalModel(config));
                } else {
                    model.add(loadRemoteModel(planInterface, config));
                }
                config.setLoaded(true);
            }
        }
    }

    /**
     * Reads a locally stored file into a Jena RDF model
     * @param lc    Locally stored file
     * @return  RDF Model
     */
    private Model loadLocalModel(RdfSourceConfig lc) {
        Model m = ModelFactory.createDefaultModel();
        m.read(lc.getLocation());
        return m;
    }

    private Model loadRemoteModel(PlanToAgentInterface planInterface, RdfSourceConfig configuration) {

        final Model model = ModelFactory.createDefaultModel();

        boolean isRetrieved = true;
        int offset = 0;
        int limit = configuration.getDefaultPageSize();
        //loads linkset entries in windows of limit and offset operators of SPARQL
        while (isRetrieved) {
            isRetrieved = false;
            String queryExtension = " LIMIT " + limit + " OFFSET " + offset;
            try (PreparedQueryExecution ex = new PreparedQueryExecution(
                    SparqlUtils.ALL_TRIPLES_CONSTRUCT_QUERY + queryExtension, configuration)) {
                Model partialModel = null;
                try {
                    partialModel = ex.queryExecution.execConstruct();
                } catch (QueryExceptionHTTP e) {
                    Platform.getLogger().log(getClass(), Level.SEVERE, String.format(
                                    "%s failed to load an RDF source from remote:\n\t%s\n%s\n%s",
                                    planInterface.getAgentID().getName(),
                                    configuration,
                                    e.getMessage(),
                                    e.getResponseMessage()
                            )
                    );
                    if (e.getMessage().contains("java.net.SocketTimeoutException") && configuration.getTimeout() < 120000L) {
                        configuration.setTimeout(configuration.getTimeout() * 2);
                        Platform.getLogger().log(getClass(), "Increasing timeout to " + configuration.getTimeout() + " and trying again");
                        isRetrieved = true; // Make sure we try again with increased timeout
                    } else {
                        throw e;
                    }
                }
                if (partialModel != null && !partialModel.isEmpty()) {
                    model.add(partialModel);
                    isRetrieved = true;
                    offset += limit;
                }
            } catch (ResultSetException e) {
                Platform.getLogger().log(getClass(), Level.SEVERE, String.format(
                        e.getMessage(),
                        configuration.getLocation(),
                        planInterface.getAgentID().getShortLocalName()
                ));
            }
        }

        return model;
    }
}
