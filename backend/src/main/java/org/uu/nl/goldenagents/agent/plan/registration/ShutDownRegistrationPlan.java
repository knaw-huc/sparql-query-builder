package org.uu.nl.goldenagents.agent.plan.registration;

import java.util.logging.Level;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.Performative;
import org.uu.nl.net2apl.core.fipa.ams.DirectoryFacilitator;
import org.uu.nl.net2apl.core.fipa.mts.Envelope;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.plan.builtin.RunOncePlan;
import org.uu.nl.net2apl.core.platform.Platform;

public class ShutDownRegistrationPlan extends RunOncePlan {
	
	private static final Loggable logger = Platform.getLogger();
	
	@Override
	public void executeOnce(PlanToAgentInterface planInterface) throws PlanExecutionError {
		try {
			planInterface.getAgent().getPlatform().getLocalDirectoryFacilitators().forEach (
			    (aidDF) -> {
			    	goodbyeOnce(planInterface, aidDF);
			    }
			);
		} catch (Exception ex) {
			logger.log(ShutDownRegistrationPlan.class, Level.WARNING, ex.getMessage());
			//throw new PlanExecutionError(); //??
		}
	}
	
    private ACLMessage getLastMessage(AgentID aidMe, AgentID aidDF) {
    	Envelope envelope = new Envelope();
    	envelope.setFrom(aidMe);
		envelope.addTo(aidDF);
		envelope.addIntendedReceiver(aidDF);
    	
        ACLMessage message = new ACLMessage(Performative.CANCEL); // TODO: Is this really the right performative?
		message.addReceiver(aidDF);
	  //message.addReplyTo(aidMe);
		message.setSender(aidMe);
		message.setContent(DirectoryFacilitator.RequestType.KILL_AGENT.toString() + " " + aidMe.toString());
		message.setEnvelope(envelope);

		return message;
    }
    

    
    private void goodbyeOnce(PlanToAgentInterface planInterface, AgentID aidDF) {
    	AgentID aidMe = planInterface.getAgentID();
		try {
			planInterface.getAgent().sendMessage(getLastMessage(aidMe, aidDF));
		} catch (Exception ex) {
			logger.log(ShutDownRegistrationPlan.class, Level.WARNING, ex.getMessage());
		}
    }
}