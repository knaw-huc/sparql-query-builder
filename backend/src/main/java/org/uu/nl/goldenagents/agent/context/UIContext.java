package org.uu.nl.goldenagents.agent.context;

import org.uu.nl.goldenagents.netmodels.angular.CrudAgent;
import org.uu.nl.net2apl.core.agent.Context;

public class UIContext implements Context {

	private String icon;
	private String nickname;
	private CrudAgent.AgentType type;
	
	public UIContext(String icon, String nickname, CrudAgent.AgentType type) {
		this.icon = icon;
		this.nickname = nickname;
		this.type = type;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public CrudAgent.AgentType getType() {
		return this.type;
	}
	
}
