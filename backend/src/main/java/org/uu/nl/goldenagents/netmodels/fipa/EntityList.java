package org.uu.nl.goldenagents.netmodels.fipa;

import org.uu.nl.net2apl.core.fipa.acl.ACLMessage;
import org.uu.nl.net2apl.core.fipa.acl.FIPASendableObject;
import org.uu.nl.net2apl.core.fipa.acl.UnreadableException;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.ArrayList;
import java.util.UUID;

public class EntityList<T> implements FIPASendableObject {
    private final ArrayList<T> entities;

    private final UUID forFocus;

    public EntityList(UUID forFocus, ArrayList<T> entities) {
        this.forFocus = forFocus;
        this.entities = entities;
    }

    public EntityList(UUID forFocus) {
        this.forFocus = forFocus;
        this.entities = new ArrayList<>();
    }

    public void addEntity(T entity) {
        if(!this.entities.contains(entity)) this.entities.add(entity);
    }

    public ArrayList<T> getEntities() {
        return this.entities;
    }

    public UUID getForFocus() {
        return forFocus;
    }

    public static <T> EntityList<T> fromACLMessage(ACLMessage receivedMessageWithEntityList) {
        try {
            GAMessageContentWrapper contentWrapper = (GAMessageContentWrapper) receivedMessageWithEntityList.getContentObject();
            FIPASendableObject content = contentWrapper.getContent();
            return (EntityList<T>) content;
        } catch (UnreadableException e) {
            Platform.getLogger().log(UserQueryTrigger.class, e);
            return null;
        }
    }
}
