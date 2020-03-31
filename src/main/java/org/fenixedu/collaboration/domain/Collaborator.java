package org.fenixedu.collaboration.domain;

import org.fenixedu.bennu.core.domain.Singleton;
import org.fenixedu.bennu.core.domain.User;
import pt.ist.fenixframework.Atomic;

import java.util.function.Consumer;
import java.util.function.Function;

public class Collaborator extends Collaborator_Base {
    
    public Collaborator(final User user) {
        setUser(user);
    }

    static Collaborator getCollaborator(final User user) {
        return Singleton.getInstance(() -> user.getCollaborator(), () -> new Collaborator(user));
    }

    private static void setUserId(final User user, final String userId, final Function<Collaborator, String> getter,
                                  final Consumer<Collaborator> setter) {
        if (userId == null || userId.isEmpty()) {
            throw new Error("userId cannot be null");
        }
        final Collaborator collaborator = getCollaborator(user);
        if (!userId.equals(getter.apply(collaborator))) {
            setter.accept(collaborator);
        }
    }

    @Atomic
    public static void setUserAzureId(final User user, String azureId) {
        setUserId(user, azureId, Collaborator::getAzureId, c -> c.setAzureId(azureId));
    }

    @Atomic
    public static void setUserGoogleId(final User user, String azureId) {
        setUserId(user, azureId, Collaborator::getGoogleId, c -> c.setGoogleId(azureId));
    }

}
