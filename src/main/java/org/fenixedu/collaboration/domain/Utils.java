package org.fenixedu.collaboration.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
                                      final Consumer<String> removeCollaborator,
                                      final Function<Collaborator, String> toCollaboratorId) {
        if (groupId == null || groupId.isEmpty()) {
            return;
        }
//        System.out.println();
//        System.out.println();
//        System.out.println("Updating group: " + groupId);
        final Set<String> set = new HashSet<>();
        final JsonObject list = listCollaborator.apply(groupId);
//        System.out.println("   members: " + collaborators.size());
//        System.out.println("   list: " + list.toString());
//        System.out.println("   getter: " + listGetter);
        int ownerDiff[] = new int[] { 0 };
        final JsonElement listElement = list.get(listGetter);
        if (listElement != null && !listElement.isJsonNull()) {
            for (final JsonElement jsonElement : listElement.getAsJsonArray()) {
                final String id = jsonElement.getAsJsonObject().get("id").getAsString();
//                System.out.println("      adding: " + id);
                set.add(id);
                if (collaborators.stream().noneMatch(c -> id.equals(toCollaboratorId.apply(c)))) {
                    removeCollaborator.accept(id);
                    ownerDiff[0]++;
                }
            }
        }
        collaborators.stream()
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

}
