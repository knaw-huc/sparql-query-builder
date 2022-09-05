package org.uu.nl.goldenagents.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.nl.goldenagents.agent.context.DBAgentContext;
import org.uu.nl.goldenagents.exceptions.AgentNotFoundException;
import org.uu.nl.goldenagents.netmodels.angular.ExpertiseModel;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.UUID;

@Service
public class DBAgentService {

    @Autowired
    private Platform platform;

    public ExpertiseModel[] getExpertise(UUID id) throws Exception{
        Platform.getLogger().log(getClass(), "Trying to get expertise from DB agent " + id);
        Agent agent = platform.getLocalAgent(id);
        if(agent == null) throw new AgentNotFoundException("Database agent could not be found on this platform");

        DBAgentContext context = agent.getContext(DBAgentContext.class);

        if(context == null) throw new Exception("Database agent context could not be found on agent");
        if(context.getExpertise() == null) return null;

        return context.getExpertise().toNetModel();
    }
}
