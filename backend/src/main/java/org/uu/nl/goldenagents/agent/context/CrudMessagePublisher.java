package org.uu.nl.goldenagents.agent.context;

import org.uu.nl.goldenagents.netmodels.angular.CrudMessage;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.Context;
import org.uu.nl.net2apl.core.fipa.MessageLog;
import org.uu.nl.net2apl.core.logging.MessageLogContext;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.concurrent.Flow;
import java.util.logging.Level;

/**
 * This publisher is used to convert the source event (of type {@code MessageLog}) to multiple generic {@code PublishedEvent} events.
 * It is also responsible for setting the correct {@code event ID}
 * @author Jurian Baas
 *
 */
public class CrudMessagePublisher implements Context, Flow.Subscriber<MessageLog>{

	private Flow.Subscription subscription;
	private final Agent agent;
	private final DirectSsePublisher directSsePublisher;

	public CrudMessagePublisher(Agent agent) {
		this.agent = agent;
		this.agent.getContext(MessageLogContext.class).subscribe(this);
		this.directSsePublisher = agent.getContext(DirectSsePublisher.class);
		if(this.directSsePublisher == null)
			throw new NullPointerException("No SsePublisher context found on agent " + this.agent);
	}

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
	    // Note, we assume there is only one subscription active for this instance at one time
        this.subscription = subscription;
        subscription.request(1L);
    }

    @Override
    public void onNext(MessageLog messageLog) {
        CrudMessagePublisher.this.processMessageLogEvent(messageLog);
        this.subscription.request(1L);
    }

    @Override
    public void onError(Throwable throwable) {
        Platform.getLogger().log(CrudMessagePublisher.class, throwable);
    }

    @Override
    public void onComplete() {
        Platform.getLogger().log(CrudMessagePublisher.class, Level.INFO, "Subscription finished");
    }

	private void processMessageLogEvent(MessageLog log) {
		if(log.isReceived()) {
            // If the agent received this message we only need the specific message, not those that other
            // agents received (if the same message had multiple recipients)
            CrudMessage message = new CrudMessage(log, agent.getAID());
			directSsePublisher.publishCrudMessage(agent.getAID().getUuID(), message);
		} else {
            // In case the message was sent by this agent we need all the receivers, so we can properly list them
			CrudMessage.fromMessageLog(log).forEach(message -> {
				directSsePublisher.publishCrudMessage(message.getSenderUUID(), message);
			});
		}
	}
}

