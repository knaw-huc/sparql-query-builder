package org.uu.nl.goldenagents.agent.plan.broker.splitquery;

import org.uu.nl.goldenagents.decompose.QueryDecomposer;
import org.uu.nl.goldenagents.decompose.linkset.LinksetEntry;
import org.uu.nl.goldenagents.netmodels.fipa.AgentQuery;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;

import com.google.gson.Gson;
import org.uu.nl.net2apl.core.platform.Platform;

import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SplitQueryLinkset extends SplitQueryPlan {

    public SplitQueryLinkset(ACLMessage message, GAMessageHeader header, FIPASendableObject content) {
        super(message, header, content);
    }

    /**
     * Create AgentQuery objects for all agents that should be consulted to answer the current user query. All agents
     * in this list will be sent a message containing the subquery specified on the object.
     *
     * @return List of AgentQuery objects.
     */
    @Override
    @NotNull List<AgentQuery> createAgentQueries() {
    	LinksetEntry[] detailedLinkset = readLinkset();
    	List<AgentQuery> queries = null;
        QueryDecomposer decomposer = new QueryDecomposer(super.queryInfo);
        if(detailedLinkset != null) {
        	queries = decomposer.decompositionFromLinkset(selectCapablesFromList(), detailedLinkset);
        }
        return queries;
    }
    
    private LinksetEntry[] readLinkset() {
    	ClassLoader loader = Thread.currentThread().getContextClassLoader();
    	URL tableFile = loader.getResource("configs/table.json");
    	LinksetEntry[] linksetObjects = null;
    	try {
			File f = Paths.get(tableFile.toURI()).toFile();
			Reader reader = new FileReader(f);
			Gson gson = new Gson();
			linksetObjects = gson.fromJson(reader, LinksetEntry[].class);
		} catch (FileNotFoundException | URISyntaxException e) {
            Platform.getLogger().log(getClass(), Level.SEVERE, "Failed to read linksets from table");
            Platform.getLogger().log(getClass(), Level.SEVERE, e);
		}
		return linksetObjects;
    }
    
    /**
     * Select DB-Agents from a list of candidates
     * @return  List of DB-Agents filtered from {@code selectedSources}
     */
    private List<AgentID> selectCapablesFromList() {
        List<String> candidates = Arrays.asList(super.selectedSources);
        List<AgentID> agents = new ArrayList<>();
        this.context.getDbAgentCapabilities().forEach((agentID, capabilities) -> {
            if(candidates.contains(agentID.getUuID())) {
            	agents.add(agentID);
            }
        });
        return agents;
    }
}
