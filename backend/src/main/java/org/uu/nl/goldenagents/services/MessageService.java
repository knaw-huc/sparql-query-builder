package org.uu.nl.goldenagents.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.nl.goldenagents.netmodels.angular.CrudMessage;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.logging.MessageLogContext;
import org.uu.nl.net2apl.core.platform.Platform;
import org.uu.nl.net2apl.core.platform.PlatformNotFoundException;

import ch.rasc.sse.eventbus.SseEventBus;

@Service
public class MessageService {
	
	private static final Loggable logger = Platform.getLogger();

	
	@Autowired
	private Platform platform;
	public MessageService(SseEventBus sseEventBus) {
	}
	
	public Map<String, Integer> performatives() {
		return Arrays.asList(Performative.values())
				.stream()
				.collect(Collectors.toMap(
					p -> p.name(),
					p -> p.index()
				));
	}
	
	public CrudMessage sendMessage(CrudMessage msg) throws MessageReceiverNotFoundException, PlatformNotFoundException, URISyntaxException, IOException {
		Agent receiverAgent = platform.getLocalAgent(msg.getReceiverURI().getUserInfo());
		Agent senderAgent = platform.getLocalAgent(msg.getSenderUUID());
		
		if(msg.getParams() == null) {
			msg.setParams(new HashMap<>());
		}

		msg.setSenderURI(senderAgent.getName());

		ACLMessage message = msg.toACLMessage();
		logger.log(this.getClass(), "Sending message in conversation " + message.getConversationId());
		// Create a new CrudMessage with the messageID included
		return new CrudMessage(senderAgent.sendMessage(receiverAgent.getAID(), message), receiverAgent.getAID());
	}
	
	public List<CrudMessage> agentHistory(UUID agentID) {
		Agent agent = platform.getLocalAgent(agentID);
		MessageLogContext context = agent.getContext(MessageLogContext.class);
		return context.getMessageHistory()
				.stream()
				.map(log -> CrudMessage.fromMessageLog(log))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

}
