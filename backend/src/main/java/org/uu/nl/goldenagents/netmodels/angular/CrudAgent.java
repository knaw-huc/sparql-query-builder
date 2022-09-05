package org.uu.nl.goldenagents.netmodels.angular;

import org.uu.nl.goldenagents.agent.context.UIContext;
import org.uu.nl.goldenagents.agent.context.registration.MinimalFunctionalityContext;
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.fipa.ams.DirectoryFacilitator;

import java.beans.Transient;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CrudAgent {
	
	public enum AgentType {
		
		USER("User"),
		BROKER("Broker"),
		DF("DF"),
		EMBEDDING("Embedding"),
		DB("DB");
		
		final String typeName;
		
		public String getType() {
			return typeName;
		}
		
		AgentType(String typeName) {
			this.typeName = typeName;
		}
		
		public static AgentType fromString(String typeName) {
			return AgentType.valueOf(typeName.toUpperCase());
		}
	}
	
	private transient AgentType _type;
	private String icon;
	private String nickname;
	private String uuid;
	private String host;
	private int port;
	private Map<String, String> additionals;
	private boolean ready = false;
	
	public CrudAgent() {}
	
	public CrudAgent(Agent agent) {
		this(agent, false);
	}
	
	public CrudAgent(Agent agent, boolean additionals) {
		String icon;
		UIContext context = agent.getContext(UIContext.class);
		if(context == null && agent instanceof DirectoryFacilitator) {
			icon = "import_contacts";
			this._type = AgentType.DF;
		} else if (context == null) {
			icon = "person";
			this._type = AgentType.USER;
		} else {
			icon = context.getIcon();
			this._type = context.getType();
		}
		this.icon = icon;

		MinimalFunctionalityContext minFunContext = agent.getContext(MinimalFunctionalityContext.class);
		if(minFunContext != null) {
			this.ready = minFunContext.fullFunctionalityReady();
		} else {
			// Assume each agent that does not have the right context is ready
			ready = true;
		}
		
		URI uri = agent.getAID().getName();
		this.nickname = uri.getFragment();
		this.host = uri.getHost();
		this.port = uri.getPort();
		this.uuid = uri.getUserInfo();
		
		if(additionals) {
			addAgentSpecificData(agent);
		}
	}

	@Transient
	public AgentType get_type() {
		return this._type;
	}

	public String getAgentType() {
		return _type.getType();
	}

	public void setAgentType(String agentType) {
		this._type = AgentType.fromString(agentType);
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Transient
	public URI createURI() throws URISyntaxException {
		return new URI(null, uuid, host, port, null, null, nickname);
	}

	public String getNickname() {
		return nickname;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean getReady() {
		return this.ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}


	@Override
	public String toString() {
		return 	"CRUD-Agent: \n" + 
				"\tID: " + this.getUuid() + "\n" + 
				"\tType: " + this.getAgentType() + "\n" + 
				"\tNickname: " + this.getNickname() + "\n" + 
				"\tURL: " + this.host + ":" + this.port;
	}

	public Map<String, String> getAdditionals() {
		return additionals;
	}

	public void setAdditionals(Map<String, String> additionals) {
		this.additionals = additionals;
	}
	
	private void addAgentSpecificData(Agent agent) {
		
		this.additionals = new HashMap<>();
		
		switch(AgentType.fromString(getAgentType())) {
		case BROKER:
			break;
		case DB:
			break;
		case DF:
			break;
		case USER:
			break;
		}
	}
	
}
