package org.uu.nl.goldenagents.services;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.fipa.ams.DirectoryFacilitatorContext;
import org.uu.nl.net2apl.core.platform.Platform;

@Service
public class DfService {

	@Autowired
	private Platform platform;
	
	public Map<String, List<String>> getServiceMap(String agentID) throws URISyntaxException {
		Agent agent = platform.getLocalAgent(new AgentID(agentID));
		
		DirectoryFacilitatorContext context = agent.getContext(DirectoryFacilitatorContext.class);
		
		Map<String, Set<AgentID>> services = context.getServiceTypeToAgent();
		Map<String, List<String>> map = new HashMap<>();
		
		services.forEach((serv, set) -> {
			map.put(serv, set.stream().map(aid -> aid.toString()).collect(Collectors.toList()));
		});
		
		return map;
	}
	
}
