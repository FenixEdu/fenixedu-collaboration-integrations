package org.fenixedu.collaboration.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.collaboration.domain.azure.Client;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Utils {

    public static void updateMembers(final String groupId,
                                      final Function<String, JsonObject> listCollaborator,
                                      final String listGetter,
                                      final Consumer<Integer> countSetter,
                                      final Set<Collaborator> collaborators,
                                      final Consumer<Collaborator> addCollaborator,
                                      final Consumer<String> removeCollaborator) {
        if (groupId == null || groupId.isEmpty()) {
            return;
        }
        final Set<String> set = new HashSet<>();
        final JsonObject list = listCollaborator.apply(groupId);
        int ownerDiff = 0;
        for (final JsonElement jsonElement : list.get(listGetter).getAsJsonArray()) {
            final String id = jsonElement.getAsJsonObject().get("id").getAsString();
            set.add(id);
            if (collaborators.stream().noneMatch(c -> id.equals(c.getAzureId()))) {
                removeCollaborator.accept(id);
                ownerDiff++;
            }
        }
        countSetter.accept(set.size() - ownerDiff);
        collaborators.stream()
                .filter(c -> !set.contains(c.getAzureId()))
                .forEach(c -> {
                    try {
                        addCollaborator.accept(c);
                    } catch (final Throwable t) {
                    }
                });
    }

}
