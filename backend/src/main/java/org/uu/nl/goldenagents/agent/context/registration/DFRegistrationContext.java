package org.uu.nl.goldenagents.agent.context.registration;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Context;

import java.util.HashSet;
import java.util.Set;

public class DFRegistrationContext implements Context {

	protected Set<AgentID> contactedDFs = new HashSet<>();

	protected String regServiceName;
	protected String subServiceName;
	protected Set<AgentID> subscriptions = new HashSet<>();
	
	public DFRegistrationContext(String registerAsServiceName, String subscribeToServiceName) {
		this.regServiceName = registerAsServiceName;
		this.subServiceName = subscribeToServiceName;
	}

	/**
	 * Verify if a Directory Facilitator (DF) has been contacted before.
	 * @param df AgentID of the DF to verify
	 * @return True if df has been contacted before
	 */
	public boolean hasContacted(AgentID df) { return contactedDFs.contains(df); }

	/**
	 * Add a Directory Facilitator (DF) agent to the list of contacted agents, indicating the agent of this context has
	 * already contacted this DF.
	 * @param df 	AgentID of the contacted Directory Facilitator
	 */
	public void setContacted(AgentID df) { contactedDFs.add(df); }

	/**
	 * When deregistering from the platform, a shutdown message needs to be sent to all contacted Directory Facilitators
	 * (DF). When this message is sent, remove it from the list of contacted DFs using this method
	 * @param df 	AgentID of the DF who is no longer contacted
	 * @return 		True iff df has been removed from the list, false if it was not on there in the first place
	 */
	public boolean setNoLongerContacted(AgentID df) { return contactedDFs.remove(df); }

	/**
	 * Get the name of the service the owner of this context provides
	 * @return Name of the service the owner of this context provides
	 */
	public String getRegisterAs() {
		return regServiceName;
	}

	/**
	 * Get the name of the type of service the owner of this context subscribes to
	 * @return	Name of the service the owner of this context subscribes to
	 */
	public String getSubscribeTo() {
		return subServiceName;
	}

	/**
	 * Verify whether the owner of this context proclaims a service
	 * @return	True iff the owner of this context proclaims itself as providing a service
	 */
	public boolean isService() {
		return regServiceName != null && ! regServiceName.equals("");
	}

	/**
	 * Verify whether the owner of this context requires a service from another agent
	 * @return	True iff the owner of this context subscribes to a service of another agent
	 */
	public boolean isSubscriber() {
		return subServiceName != null && ! subServiceName.equals("");
	}

	/**
	 * Add an agent that provides a service the owner of this context makes use of
	 * @param aidServ 	AgentID of the agent that provides the service
	 */
	public void addSubscription(AgentID aidServ) {
		subscriptions.add(aidServ);
	}
	
	public void removeSubscription(AgentID aidServ) {
		subscriptions.remove(aidServ);
	}
	
	public Set<AgentID> getSubscriptions() {
		return subscriptions;
	}
}
