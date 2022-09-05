package org.uu.nl.goldenagents.util;

public class PublishedEvent<T> {
	
	private String eventId;
	private T payload;
	
	public PublishedEvent(String eventId, T payload) {
		this.eventId = eventId;
		this.payload = payload;
	}

	public String getEventId() {
		return eventId;
	}

	public T getPayload() {
		return payload;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}
	
	
}