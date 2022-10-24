package org.uu.nl.goldenagents.netmodels.fipa;

import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.platform.Platform;

public class GaMessageContentObjectContainer<T> implements FIPASendableObject {

    private static final long serialVersionUID = 1L;

    private final T content;

    public GaMessageContentObjectContainer(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content.toString();
    }

    public static <T> GaMessageContentObjectContainer<T> fromACLMessage(ACLMessage message) {
        try {
            GAMessageContentWrapper contentWrapper = (GAMessageContentWrapper) message.getContentObject();
            FIPASendableObject content = contentWrapper.getContent();
            return (GaMessageContentObjectContainer<T>) content;
        } catch (UnreadableException e) {
            Platform.getLogger().log(GaMessageContentObjectContainer.class, e);
            return null;
        }
    }

}
