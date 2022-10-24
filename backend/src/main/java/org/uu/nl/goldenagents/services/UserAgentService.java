package org.uu.nl.goldenagents.services;

import ch.rasc.sse.eventbus.SseEventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.uu.nl.goldenagents.agent.context.PrefixNSListenerContext;
import org.uu.nl.goldenagents.agent.context.query.AQLQueryContext;
import org.uu.nl.goldenagents.agent.context.query.QueryResultContext;
import org.uu.nl.goldenagents.agent.trigger.user.AQLQueryChangedExternalTrigger;
import org.uu.nl.goldenagents.aql.AQLTree;
import org.uu.nl.goldenagents.aql.SPARQLTranslation;
import org.uu.nl.goldenagents.exceptions.AgentNotFoundException;
import org.uu.nl.goldenagents.netmodels.angular.*;
import org.uu.nl.goldenagents.netmodels.angular.aql.AQLJsonObject;
import org.uu.nl.goldenagents.netmodels.datatables.DataTableRequest;
import org.uu.nl.goldenagents.netmodels.datatables.DataTableResult;
import org.uu.nl.goldenagents.netmodels.fipa.QueryResult;
import org.uu.nl.goldenagents.netmodels.fipa.UserQueryTrigger;
import org.uu.nl.goldenagents.util.TomlConfigurationParser;
import org.uu.nl.goldenagents.util.agentconfiguration.UiAgentConfig;
import org.uu.nl.goldenagents.util.agentconfiguration.UserAgentConfig;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentArguments;
import org.uu.nl.net2apl.core.agent.AgentCreationFailedException;
import org.uu.nl.net2apl.core.platform.Platform;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

@Service
public class UserAgentService {

    private final Platform platform;
    private final SseEventBus serverEventBus;

    @Autowired
    public UserAgentService(Platform platform, SseEventBus serverEventBus) {
        this.platform = platform;
        this.serverEventBus = serverEventBus;
    }

    public UserQueryTrigger initiateQuery(UUID agentID, UserQueryTrigger request) throws AgentNotFoundException{
        Agent agent = getUserAgentFromUUID(agentID);
        agent.addExternalTrigger(request);
        return request;
    }

    public CrudAgent getUserAgent(@Nullable UUID agentID) throws AgentCreationFailedException {
        if (agentID != null) {
            Agent agent = this.platform.getLocalAgent(agentID);
            if (agent != null) {
                Platform.getLogger().log(this.getClass(), String.format(
                        "Found previously used user agent with ID %s",
                        agentID
                ));
                return new CrudAgent(agent, true);
            } else {
                Platform.getLogger().log(this.getClass(), String.format(
                        "Agent with UUID %s no longer exists",
                        agentID
                ));
            }
        } else {
            Platform.getLogger().log(this.getClass(), Level.INFO, "User requested a user agent but did not previously use one");
        }

        // If we reach here, either the agentID was null, or no corresponding agent exists on the platform.
        // Let's not disappoint the user and create one!
        Platform.getLogger().log(getClass(), "Creating a new User agent");

        AgentArguments arguments = new AgentArguments();
        UiAgentConfig uiAgentConfig = new UiAgentConfig("User", "person", CrudAgent.AgentType.USER, null, null);
        uiAgentConfig.addConfigurationToArguments(arguments);

        new UserAgentConfig(null).addConfigurationToArguments(arguments);

        Agent agent = TomlConfigurationParser.createAgent(platform, arguments, "User", false, serverEventBus);

        return new CrudAgent(agent, true);
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
        if(context.getCurrentQueryID() == null) {
            // TODO, ideally only use the prefixes of the broker agent that will answer this query?
            AQLQueryContext.QueryWrapper newQuery = context.createQuery(getPrefixContextForAgentID(agentID).getPrefixMap());
            notifyAQLQueryChanged(agentID, newQuery);
        }
        return context.serializeCurrentQuery();
    }

    public AQLJsonObject getCurrentQueryAsJson(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        if(context.getCurrentQueryID() == null) {
            AQLQueryContext.QueryWrapper newQuery = context.createQuery(getPrefixContextForAgentID(agentID).getPrefixMap());
            notifyAQLQueryChanged(agentID, newQuery);
        }
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery();
        return wrapper.query.getJson(wrapper.conversationID);
    }

    public AQLSuggestions getSuggestions(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        return context.getCurrentQuery().query.getSuggestions();
    }

    public String getSparqlTranslation(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        SPARQLTranslation translation = context.getCurrentQuery().query.getSparqlAlgebra();
        return translation.getQueryString();
    }

    public AQLJsonObject intersect(UUID agentID, AQLTree transformation) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery().queryContainer.intersection(transformation);
        notifyAQLQueryChanged(agentID, wrapper);
        return wrapper.query.getJson(wrapper.conversationID);
    }

    public AQLJsonObject cross(UUID agentID, AQLResource property, boolean crossForward) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery().queryContainer.cross(property, crossForward);
        notifyAQLQueryChanged(agentID, wrapper);
        return wrapper.query.getJson(wrapper.conversationID);
    }

    public AQLJsonObject exclude(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery().queryContainer.negativeLookup();
        notifyAQLQueryChanged(agentID, wrapper);
        return wrapper.query.getJson(wrapper.conversationID);
    }

    public AQLJsonObject union(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery().queryContainer.union();
        notifyAQLQueryChanged(agentID, wrapper);
        return wrapper.query.getJson(wrapper.conversationID);
    }

    public AQLJsonObject changeFocus(UUID agentID, AQLTree.ID focusID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery().queryContainer.setFocus(focusID);
        notifyAQLQueryChanged(agentID, wrapper);
        return wrapper.query.getJson(wrapper.conversationID);
    }

    public AQLJsonObject deleteCurrentFocus(UUID agentID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery().queryContainer.delete();
        notifyAQLQueryChanged(agentID, wrapper);
        return wrapper.query.getJson(wrapper.conversationID);
    }

    public AQLJsonObject deleteQueryFocus(UUID agentID, AQLTree.ID focusID) {
        AQLQueryContext context = getAQLQueryContextForAgentID(agentID);
        AQLQueryContext.QueryWrapper wrapper = context.getCurrentQuery().queryContainer.delete(focusID);
        notifyAQLQueryChanged(agentID, wrapper);
        return wrapper.query.getJson(wrapper.conversationID);
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

    private void notifyAQLQueryChanged(UUID agentID, AQLQueryContext.QueryWrapper query) {
        platform.getLocalAgent(agentID).addExternalTrigger(
                new AQLQueryChangedExternalTrigger(query)
        );
    }

    // TODO create methods to intersect with classes, and to apply forward and backward crossing
}
