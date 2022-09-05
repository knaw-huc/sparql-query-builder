package org.uu.nl.goldenagents.agent.context.registration;

import org.uu.nl.net2apl.core.agent.Context;

public abstract class MinimalFunctionalityContext implements Context {

    private boolean notifiedMinimalState = false;
    private boolean notifiedFullState = false;

    /**
     * This method checks if minimal functionality required by the agent implementing this
     * class is ready. Without this functionality, this agent cannot function, but even
     * with this functionality, some other functionality not required for basic functioning may
     * not be ready yet
     *
     * @return True iff all minimal functionality required for this agent to function is ready
     */
    public abstract boolean minimalFunctionalityReady();

    /**
     * Check if all functionality, both minimal required for this agent to function, and additional
     * functionality, is ready, so this agent can function to its full potential.
     *
     * @return  True iff all functionality potentially provided by this agent is ready
     */
    public abstract boolean fullFunctionalityReady();

    /**
     * The announcement that the minimal functionality is ready should only be sent once. This method should return true
     * iff that announcement has been sent.
     *
     * @return True iff notification of minimal ready state has been sent
     */
    public boolean hasNotifiedMinimalState() {
        return notifiedMinimalState;
    }

    /**
     * If the announcement that the minimal functionality is ready has been sent, this should be stored in the agents
     * belief base using this method.
     *
     * @param hasNotifiedMinimalState   True iff minimal functionality ready notification has been sent
     */
    public void setHasNotifiedMinimalState(boolean hasNotifiedMinimalState) {
        this.notifiedMinimalState = hasNotifiedMinimalState;
    }

    /**
     * The announcement that the full functionality is ready should only be sent once. This method should return true
     * iff that announcement has been sent.
     *
     * @return True iff notification of fully ready state has been sent
     */
    public boolean hasNotifiedFullState() {
        return notifiedFullState;
    }

    /**
     * If the announcement that the full functionality is ready has been sent, this should be stored in the agents
     * belief base using this method.
     *
     * @param hasNotifiedFullState   True iff full functionality ready notification has been sent
     */
    public void setHasNotifiedFullState(boolean hasNotifiedFullState) {
        this.notifiedFullState = hasNotifiedFullState;
    }
}
