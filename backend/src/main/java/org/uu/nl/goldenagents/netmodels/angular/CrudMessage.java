package org.uu.nl.goldenagents.netmodels.angular;

import java.beans.Transient;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentString;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageContentWrapper;
import org.uu.nl.goldenagents.netmodels.fipa.GAMessageHeader;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.fipa.MessageLog;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.fipa.mts.Envelope;

public class CrudMessage {
	
	private String messageID;
	private String time;
	private boolean received;
	private String conversationID;
	private String senderUUID;
	private String senderNickname;
	private String senderHost;
	private int senderPort;
	private String receiverUUID;
	private String receiverNickname;
	private int receiverPort;
	private String receiverHost;
	private String performative;
	private String content;
	private String header;
	private String encoding;
	private String language;
	private String ontology;
	private Map<String, String> params = new HashMap<>();

	//Crud Message should only serve the message objects we are sending from backend to the frontend
	public CrudMessage() {}
	
	/**
	 * TODO This function should be removed
	 * Used for converting messages to be consumed by the front-end.
	 * @param log
	 * @param receiver
	 * @return
	 */
	public CrudMessage(MessageLog log, AgentID receiver) {
		
		ACLMessage msg = (ACLMessage) log.getMessage();
		
		setMessageID(log.getID().toString());
		setTime(log.getTime().format(formatter));
		setReceived(log.isReceived());
		setConversationID(msg.getConversationId());
		
		setSenderUUID(msg.getSender().getName().getUserInfo());
		setSenderNickname(msg.getSender().getName().getFragment());
		setSenderHost(msg.getSender().getName().getHost());
		setSenderPort(msg.getSender().getName().getPort());
		
		setReceiverUUID(receiver.getName().getUserInfo());
		setReceiverNickname(receiver.getName().getFragment());
		setReceiverHost(receiver.getName().getHost());
		setReceiverPort(receiver.getName().getPort());
		
		setPerformative(msg.getPerformative().name());
		
		if(msg.hasByteSequenceContent()) {
			try {
				GAMessageContentWrapper contentWrapper = (GAMessageContentWrapper) msg.getContentObject();
				if(contentWrapper.getContent() != null) {
					setContent(contentWrapper.getContent().toString());
				}
				else {
					setContent("No content");
				}
				setHeader(contentWrapper.getHeader().toString());
			} catch (UnreadableException ex) {
				setContent("Unreadable object: " + ex);
			}
		} else {
			setContent(msg.getContent());
		}
		
		setEncoding(msg.getEncoding());
		setLanguage(msg.getLanguage());
		setOntology(msg.getOntology());
		setParams(
			msg.getAllUserDefinedParameters()
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
				e -> e.getKey().toString(),
				e -> e.getValue().toString()
			))
		);
	}

	
	public String getSenderUUID() {
		return senderUUID;
	}

	public void setSenderUUID(String senderUUID) {
		this.senderUUID = senderUUID;
	}

	public String getSenderNickname() {
		return senderNickname;
	}

	public void setSenderNickname(String senderNickname) {
		this.senderNickname = senderNickname;
	}

	public String getSenderHost() {
		return senderHost;
	}

	public void setSenderHost(String senderHost) {
		this.senderHost = senderHost;
	}

	public int getSenderPort() {
		return senderPort;
	}

	public void setSenderPort(int senderPort) {
		this.senderPort = senderPort;
	}

	public String getReceiverUUID() {
		return receiverUUID;
	}

	public void setReceiverUUID(String receiverUUID) {
		this.receiverUUID = receiverUUID;
	}

	public String getReceiverNickname() {
		return receiverNickname;
	}

	public void setReceiverNickname(String receiverNickname) {
		this.receiverNickname = receiverNickname;
	}

	public int getReceiverPort() {
		return receiverPort;
	}

	public void setReceiverPort(int receiverPort) {
		this.receiverPort = receiverPort;
	}

	public String getReceiverHost() {
		return receiverHost;
	}

	public void setReceiverHost(String receiverHost) {
		this.receiverHost = receiverHost;
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public String getPerformative() {
		return performative;
	}

	public void setPerformative(String performative) {
		this.performative = performative;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	};

	
	public String getConversationID() {
		return conversationID;
	}

	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public boolean isReceived() {
		return received;
	}

	public void setReceived(boolean received) {
		this.received = received;
	}

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
	
	/**
	 * Used for converting messages to be consumed by the front-end.
	 * Will create one message for each receiver.
	 * @param log
	 * @return
	 */
	public static List<CrudMessage> fromMessageLog(MessageLog log) {
		
		ACLMessage msg = (ACLMessage) log.getMessage();
		
		return msg.getReceiver()
			.stream()
			.map(receiver -> new CrudMessage(log, receiver))
			.collect(Collectors.toList());
	}
	
	/**
	 * Used for converting a message coming in from the front-end.
	 * @param msg
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException 
	 */
	public ACLMessage toACLMessage() throws URISyntaxException, IOException {
		
		AgentID from = new AgentID(getSenderURI());
		AgentID to = new AgentID(getReceiverURI());
		
		ACLMessage message = new ACLMessage(Performative.INFORM);
		if(header != null && header.equals("USER_INTELLIGENT_SEARCH")) {
			message.setContentObject(new GAMessageContentWrapper(GAMessageHeader.USER_INTELLIGENT_SEARCH, new GAMessageContentString(content)));
		}
		else {
			message.setContentObject(new GAMessageContentWrapper(GAMessageHeader.USER_QUERY, new GAMessageContentString(content)));
		}
		message.setPerformative(Performative.valueOf(getPerformative()));
		message.setSender(from);
		message.addReceiver(to);
		message.addReplyTo(from);
		getParams().forEach((k,v) -> { message.addUserDefinedParameter(k, v); });
		
		Envelope envelope = new Envelope();
		envelope.setFrom(from);
		envelope.addTo(to);
		envelope.addIntendedReceiver(to);
		
		message.setEnvelope(envelope);
		
		return message;
	}

	@Transient 
	public URI getReceiverURI() throws URISyntaxException {
		return new URI(null, getReceiverUUID(), getReceiverHost(), getReceiverPort(), null, null, getReceiverNickname());
	}

	@Transient
	private URI getSenderURI() throws URISyntaxException {
		return new URI(null, getSenderUUID(), getSenderHost(), getSenderPort(), null, null, getSenderNickname());
	}
	
	@Transient
	public void setReceiverURI(URI uri) {
		setReceiverUUID(uri.getUserInfo());
		setReceiverHost(uri.getHost());
		setReceiverPort(uri.getPort());
		setReceiverNickname(uri.getFragment());
	}
	
	@Transient
	public void setSenderURI(URI uri) {
		setSenderUUID(uri.getUserInfo());
		setSenderHost(uri.getHost());
		setSenderPort(uri.getPort());
		setSenderNickname(uri.getFragment());
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}
	
}
