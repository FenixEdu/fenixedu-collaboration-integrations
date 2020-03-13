package org.fenixedu.collaboration.domain;

import org.fenixedu.bennu.core.domain.Singleton;
import org.fenixedu.bennu.core.domain.User;
import pt.ist.fenixframework.Atomic;

public class Collaborator extends Collaborator_Base {
    
    public Collaborator(final User user) {
        setUser(user);
    }

    @Atomic
    public static void setUserAzureId(final User user, String azureId) {
        if (azureId == null || azureId.isEmpty()) {
            throw new Error("azureId cannot be null");
        }
        final Collaborator collaborator = Singleton.getInstance(() -> user.getCollaborator(), () -> new Collaborator(user));
        if (!azureId.equals(collaborator.getAzureId())) {
            collaborator.setAzureId(azureId);
        }
    }

}
