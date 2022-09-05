package org.uu.nl.goldenagents.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.platform.Platform;

import ch.rasc.sse.eventbus.SseEventBus;

@RestController
@RequestMapping("api/sse")
public class SseController {

	private static final Loggable logger = Platform.getLogger();
	private final SseEventBus eventBus;

	public SseController(SseEventBus eventBus) {
		this.eventBus = eventBus;
	}

	@GetMapping("register/{clientId}")
	public SseEmitter register(@PathVariable("clientId") String clientId) {
		
		SseEmitter emitter = new SseEmitter(180_000L);
		emitter.onTimeout(emitter::complete);
		eventBus.registerClient(clientId, emitter);
		
		logger.log(SseController.class, "Client " + clientId + " registered");
		
		return emitter;
	}
	
	@GetMapping("subscribe/{clientId}/{event}")
	@ResponseBody
	public void subscribe(@PathVariable("clientId") String clientId, @PathVariable("event") String event) {
		logger.log(SseController.class, "Client " + clientId + " subscribed to " + event);
		eventBus.subscribe(clientId, event);
	}

}
