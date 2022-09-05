package org.uu.nl.goldenagents.agent.context;

import ch.rasc.sse.eventbus.SseEvent;
import ch.rasc.sse.eventbus.SseEventBus;
import org.uu.nl.goldenagents.netmodels.angular.AQLSuggestions;
import org.uu.nl.goldenagents.netmodels.angular.CrudMessage;
import org.uu.nl.goldenagents.netmodels.angular.QueryProgress;
import org.uu.nl.goldenagents.util.PublishedEvent;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Context;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public class DirectSsePublisher implements Context {

    private static final Semaphore semaphore = new Semaphore(1);
    private final SseEventBus eventBus;
    private AgentID agentID;

    public DirectSsePublisher(SseEventBus eventBus, AgentID agentID) {
        this.eventBus = eventBus;
        this.agentID = agentID;
    }

    public synchronized boolean publishQueryProgress(QueryProgress progress) {
        PublishedEvent<QueryProgress> event = new PublishedEvent<>("query_progress_" + progress.getQueryID(), progress);
        log("Query Progress", event);
        return this.publishEvent(event);
    }

    public synchronized boolean publishCrudMessage(String involvedAgentUUID, CrudMessage message) {
        PublishedEvent<CrudMessage> event = new PublishedEvent<>("messages_" + involvedAgentUUID, message);
        log("Message", event);
        return this.publishEvent(event);
    }

    public synchronized boolean publishStateReady() {
        PublishedEvent<String> event = new PublishedEvent<>("agent_state_ready", this.agentID.getUuID());
        log("State Ready", event);
        return this.publishEvent(event);
    }

    public synchronized boolean publishSuggestionsReady(AgentID agentID, AQLSuggestions suggestions) {
        PublishedEvent<AQLSuggestions> event = new PublishedEvent<>("suggestions_" + agentID.getUuID(), suggestions);
        log("Suggestions ready", event);
        return this.publishEvent(event);
    }

    private synchronized boolean publishEvent(PublishedEvent<?> event) {
        boolean success = false;

        try {
            semaphore.acquire();
            eventBus.handleEvent(SseEvent.of(event.getEventId(), event.getPayload()));
            success = true;
        } catch(InterruptedException e) {
            Platform.getLogger().log(DirectSsePublisher.class, e);
        } finally {
            semaphore.release();
        }

        return success;
    }

    private void log(String type, PublishedEvent<?> event) {
        Platform.getLogger().log(DirectSsePublisher.class, Level.INFO, String.format(
                "Event sent\t|\tAgent %s\t|\tEvent type %s\t|\tEventID %s\t|\tPayload: %s",
                this.agentID,
                type,
                event.getEventId(),
                event.toString()
        ));
    }
}
