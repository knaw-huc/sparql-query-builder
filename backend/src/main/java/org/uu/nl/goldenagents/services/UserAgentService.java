package org.uu.nl.goldenagents.services;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.goldenagents.agent.context.query.AQLQueryContext;
import org.uu.nl.goldenagents.agent.context.query.QueryResultContext;
import org.uu.nl.goldenagents.agent.trigger.user.AQLQueryChangedExternalTrigger;
import org.uu.nl.goldenagents.aql.AQLQuery;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.SPARQLTranslation;
import org.uu.nl.goldenagents.exceptions.AgentNotFoundException;
import org.uu.nl.goldenagents.netmodels.angular.AQLQueryObject;
import org.uu.nl.goldenagents.netmodels.angular.AQLResource;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.angular.CachedQueryInfo;
import org.uu.nl.goldenagents.netmodels.angular.CrudAgent;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLJsonObject;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLQueryJsonRow;
import org.uu.nl.goldenagents.netmodels.datatables.DataTableRequest;
import org.uu.nl.goldenagents.netmodels.datatables.DataTableResult;
import org.uu.nl.goldenagents.netmodels.fipa.QueryResult;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.platform.Platform;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@Service
public class UserAgentService {

    @Autowired
    private Platform platform;
    private UUID mainUserAgent;

    public UserQueryTrigger initiateQuery(UUID agentID, UserQueryTrigger request) throws AgentNotFoundException{
        Agent agent = getUserAgentFromUUID(agentID);
        agent.addExternalTrigger(request);
        return request;
    }

    public UUID getUserAgent() {
        if(this.mainUserAgent == null) {
            for(AgentID aid : this.platform.getLocalAgentsList()) {
                try {
                    CrudAgent agent = new CrudAgent(this.platform.getLocalAgent(aid));
                    if(CrudAgent.AgentType.USER.equals(agent.get_type())) {
                        this.mainUserAgent = UUID.fromString(agent.getUuid());
                        break;
                    }
                } catch (URISyntaxException e) {
                    Platform.getLogger().log(this.getClass(), Level.SEVERE, "Failed to get agent " + aid.toString());
                    Platform.getLogger().log(this.getClass(), e);
                }
            }
        }
        return this.mainUserAgent;
    }

    /*
            QUERY HISTORY
     */

    public String getLastQueryID(UUID agentID) {
        QueryResultContext context = getQueryResultContextForAgentID(agentID);
        if(context == null) return null;

        return context.getLastFinishedQueryString();
    }

    public String getLastQuery(UUID agentID) {
        QueryResultContext context = getQueryResultContextForAgentID(agentID);
        if(context == null) return null;

        return context.getLastQueryString();
    }

    public String getQueryForQueryID(UUID agentID, String queryID) {
        QueryResultContext context = getQueryResultContextForAgentID(agentID);
        if(context == null) return null;

        return context.getQueryString(queryID);
    }

    /*
            QUERY RESULTS
    */

    public ByteArrayOutputStream getResutlsAsCSV(UUID agentID, String queryID) {
        QueryResultContext context = getQueryResultContextForAgentID(agentID);
        if(context == null) return null;

        return context.getCSVStreamForQuery(queryID);
    }

    public ByteArrayOutputStream getResutlsAsXML(UUID agentID, String queryID) {
        QueryResultContext context = getQueryResultContextForAgentID(agentID);
        if(context == null) return null;

        return context.getXMLStreamForQuery(queryID);
    }

    public DataTableResult getPaginatedQueryResults(HttpServletRequest request) {
        DataTableRequest dtRequest = new DataTableRequest(request);

        Agent agent = getUserAgentFromUUID(dtRequest.getAgentUUID());
        QueryResultContext context = getQueryResultContextForAgentID(dtRequest.getAgentUUID());
        if(context == null) throw new IllegalArgumentException();

        QueryResult queryResult = context.getResult(dtRequest.getQueryID());
        if(queryResult == null) throw new IllegalArgumentException();

        return queryResult.getResults(dtRequest);
    }

    public Collection<CachedQueryInfo> queryHistory(UUID agentID) {
        QueryResultContext context = getQueryResultContextForAgentID(agentID);
        return context.queryHistory();
    }

    @Deprecated
    public AQLQueryObject getCurrentQuery(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        if(context.getCurrentQueryIndex() < 0) {
            // TODO, ideally only use the prefixes of the broker agent that will answer this query?
            AQLQuery newQuery = context.createQuery(getPrefixContextForAgentID(agentID).getPrefixMap());
            notifyAQLQueryChanged(agentID, newQuery);
        }
        return context.serializeCurrentQuery();
    }

    public AQLJsonObject getCurrentQueryAsJson(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        if(context.getCurrentQueryIndex() < 0) {
            AQLQuery newQuery = context.createQuery(getPrefixContextForAgentID(agentID).getPrefixMap());
            notifyAQLQueryChanged(agentID, newQuery);
        }
        return context.getCurrentQuery().getJson();
    }

    public AQLSuggestions getSuggestions(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        return context.getCurrentQuery().getSuggestions();
    }

    public String getSparqlTranslation(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        SPARQLTranslation translation = context.getCurrentQuery().getSparqlAlgebra();
        return translation.getQueryString();
    }

    public AQLJsonObject intersect(UUID agentID, AQLTree transformation) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        context.getCurrentQuery().intersection(transformation);
        notifyAQLQueryChanged(agentID, context.getCurrentQuery());
        return context.getCurrentQuery().getJson();
    }

    public AQLJsonObject cross(UUID agentID, AQLResource property, boolean crossForward) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        context.getCurrentQuery().cross(property, crossForward);
        notifyAQLQueryChanged(agentID, context.getCurrentQuery());
        return context.getCurrentQuery().getJson();
    }

    public AQLJsonObject exclude(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        context.getCurrentQuery().negativeLookup();
        notifyAQLQueryChanged(agentID, context.getCurrentQuery());
        return context.getCurrentQuery().getJson();
    }

    public AQLJsonObject union(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        context.getCurrentQuery().union();
        notifyAQLQueryChanged(agentID, context.getCurrentQuery());
        return context.getCurrentQuery().getJson();
    }

    public AQLJsonObject changeFocus(UUID agentID, UUID focusID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        context.getCurrentQuery().setFocus(focusID);
        notifyAQLQueryChanged(agentID, context.getCurrentQuery());
        return context.getCurrentQuery().getJson();
    }

    public AQLJsonObject deleteCurrentFocus(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        context.getCurrentQuery().delete();
        notifyAQLQueryChanged(agentID, context.getCurrentQuery());
        return context.getCurrentQuery().getJson();
    }

    public AQLJsonObject deleteQueryFocus(UUID agentID, UUID focusID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        context.getCurrentQuery().delete(focusID);
        notifyAQLQueryChanged(agentID, context.getCurrentQuery());
        return context.getCurrentQuery().getJson();
    }

    /*
            HELPERS
     */
    private Agent getUserAgentFromUUID(UUID agentID) throws AgentNotFoundException {
        Agent agent = platform.getLocalAgent(agentID);
        if(agent == null) throw new AgentNotFoundException("This user agent could not be found on this platform");
        return agent;
    }

    private QueryResultContext getQueryResultContextForAgentID(UUID agentID) {
        Agent agent = getUserAgentFromUUID(agentID);
        return agent.getContext(QueryResultContext.class);
    }

    private AQLQueryContext getAQLQueryContextForAgentID(UUID agentID) {
        Agent agent = getUserAgentFromUUID(agentID);
        return agent.getContext(AQLQueryContext.class);
    }

    private PrefixNSListenerContext getPrefixContextForAgentID(UUID agentID) {
        Agent agent = getUserAgentFromUUID(agentID);
        return agent.getContext(PrefixNSListenerContext.class);
    }

    private void notifyAQLQueryChanged(UUID agentID, AQLQuery query) {
        platform.getLocalAgent(agentID).addExternalTrigger(
                new AQLQueryChangedExternalTrigger(query)
        );
    }

    // TODO create methods to intersect with classes, and to apply forward and backward crossing
}
