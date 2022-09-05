package org.uu.nl.goldenagents.agent.plan.broker.suggestions;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.ParserARQ;
import org.uu.nl.goldenagents.agent.context.BrokerSearchSuggestionsContext;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.SPARQLTranslation;
import org.uu.nl.goldenagents.decompose.expertise.ProvenanceTracer;
import org.uu.nl.goldenagents.netmodels.fipa.EntityList;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.goldenagents.sparql.CachedModel;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class RequestDbSearchSuggestionsPlan extends RunOncePlan {

    private static final Loggable logger = Platform.getLogger();

    private final CachedModel model;
    private final ACLMessage userAgentSuggestionsRequest;
    private PlanToAgentInterface planToAgentInterface;

    public RequestDbSearchSuggestionsPlan(CachedModel model, ACLMessage userAgentSuggestionsRequest) {
        this.model = model;
        this.userAgentSuggestionsRequest = userAgentSuggestionsRequest;
    }

    @Override
    public void executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        this.planToAgentInterface = planToAgentInterface;
        if(!this.model.isSuggestionsExpected() || this.model.getUserQueryTrigger().getSparqlTranslation() == null) {
            logger.log(getClass(), Level.WARNING, "Started suggestion plan when no suggestions are expected or available");
            return;
        }

        logger.log(getClass(), "Starting to aggregate properties suggestions for search");
        HashMap<AgentID, EntityList<String>> relevantEntities = getRelevantEntities();

        Platform.getLogger().log(getClass(), Level.SEVERE, "Contacting agents: " + relevantEntities.keySet());
        for(AgentID aid : relevantEntities.keySet()) {
            if(relevantEntities.get(aid).getEntities().size() > 0) {
                this.model.expectSuggestionsFrom(aid);
                requestSuggestionsToAgent(aid, relevantEntities.get(aid));
            }
        }
    }

    private HashMap<AgentID, EntityList<String>> getRelevantEntities() {
        Var focus = this.model.getUserQueryTrigger().getSparqlTranslation().getFocusVar();
        Query q = new Query();
        // Parse
        ParserARQ.createParser(Syntax.syntaxSPARQL_11).parse(q, this.model.getOriginalQuery());
        q.setDistinct(true);
        q.addProjectVars(Collections.singleton(focus));

        // TODO this doesn't allow including new data sources

        BrokerSearchSuggestionsContext context = this.planToAgentInterface.getContext(BrokerSearchSuggestionsContext.class);
        BrokerSearchSuggestionsContext.SearchSuggestionSubscription sub = context.getSubscription(this.userAgentSuggestionsRequest.getConversationId());

        return sub.getSearchSuggestions(sub.getLastQuery()).getEntitiesAtFocus(sub.getModel());
    }

    private void requestSuggestionsToAgent(AgentID dbAgent, EntityList<String> entities) {
        ACLMessage m = this.userAgentSuggestionsRequest.createForward(this.planToAgentInterface.getAgentID(), dbAgent);
        m.setPerformative(Performative.REQUEST);
        try {
            m.setContentObject(new GAMessageContentWrapper(GAMessageHeader.REQUEST_SUGGESTIONS, entities));
        } catch (IOException e) {
            logger.log(getClass(), e);
        }

        try {
            this.planToAgentInterface.getAgent().sendMessage(m);
            logger.log(getClass(), Level.INFO, String.format(
                    "Requested suggestions for %d entities from db agent %s",
                    entities.getEntities().size(), dbAgent.getShortLocalName()));
        } catch (PlatformNotFoundException | MessageReceiverNotFoundException e) {
            logger.log(getClass(), e);
        }
    }
}
