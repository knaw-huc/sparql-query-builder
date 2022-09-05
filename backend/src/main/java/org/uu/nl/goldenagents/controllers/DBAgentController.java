package org.uu.nl.goldenagents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.uu.nl.goldenagents.exceptions.AgentNotFoundException;
import org.uu.nl.goldenagents.exceptions.InvalidIdException;
import org.uu.nl.goldenagents.netmodels.angular.ExpertiseModel;
import org.uu.nl.goldenagents.services.DBAgentService;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.UUID;

@RestController
@RequestMapping("api/agent/db")
public class DBAgentController {

    @Autowired
    private DBAgentService dbAgentService;

    @GetMapping("expertise")
    public ExpertiseModel[] getExpertise(@RequestParam("agentID") String agentIDString) throws AgentNotFoundException, Exception {
        Platform.getLogger().log(getClass(), "Trying to get expertise from DB agent " + agentIDString);
        try {
            UUID agentID = UUID.fromString(agentIDString);
            return dbAgentService.getExpertise(agentID);
        } catch(IllegalArgumentException e) {
            throw new InvalidIdException();
        }
    }
}
