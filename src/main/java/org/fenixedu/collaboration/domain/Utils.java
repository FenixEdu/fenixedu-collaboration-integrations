package org.fenixedu.collaboration.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Utils {

    public static void updateMembers(final String groupId,
                                      final Function<String, JsonObject> listCollaborator,
                                      final String listGetter,
                                      final Consumer<Integer> countSetter,
                                      final Set<Collaborator> collaborators,
                                      final Consumer<Collaborator> addCollaborator,
                                      final Consumer<String> removeCollaborator,
                                      final Function<Collaborator, String> toCollaboratorId,
                                      final String memberIdGetter) {
        if (groupId == null || groupId.isEmpty()) {
            return;
        }
        final Set<String> set = new HashSet<>();
        final JsonObject list = listCollaborator.apply(groupId);
        int ownerDiff[] = new int[] { 0 };
        final JsonElement listElement = list.get(listGetter);
        if (listElement != null && !listElement.isJsonNull()) {
            for (final JsonElement jsonElement : listElement.getAsJsonArray()) {
                final String id = jsonElement.getAsJsonObject().get(memberIdGetter).getAsString();
                set.add(id);
                if (collaboratorStream(collaborators, toCollaboratorId).noneMatch(c -> id.equals(toCollaboratorId.apply(c)))) {
                    removeCollaborator.accept(id);
                    ownerDiff[0]++;
                }
            }
        }
        collaboratorStream(collaborators, toCollaboratorId)
                .filter(c -> !set.contains(toCollaboratorId.apply(c)))
                .forEach(c -> {
                    try {
                        addCollaborator.accept(c);
                        ownerDiff[0]--;
                    } catch (final Throwable t) {
                    }
                });
        countSetter.accept(set.size() - ownerDiff[0]);
    }

    private static Stream<Collaborator> collaboratorStream(final Set<Collaborator> collaborators, final Function<Collaborator, String> toCollaboratorId) {
        return collaborators.stream().filter(c -> isValid(toCollaboratorId.apply(c)));
    }

    private static boolean isValid(final String id) {
        return id != null && !id.isEmpty();
    }

}
