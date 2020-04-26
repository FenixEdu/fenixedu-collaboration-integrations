package org.fenixedu.collaboration.domain.discourse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Client {

    public static JsonElement get(final String urlPath) {
        final CollaborationIntegrationsConfiguration.ConfigurationProperties configuration =
                CollaborationIntegrationsConfiguration.getConfiguration();
        final HttpRequest request = Unirest.get(configuration.discourseUrl() + urlPath)
                .header("Api-Username",configuration.discourseApiUsername())
                .header("Api-Key", configuration.discourseApiKey());
        final HttpResponse<String> get = request.asString();
        return new JsonParser().parse(get.getBody());
    }

    public static String post(final String urlPath, final JsonElement body) {
        final CollaborationIntegrationsConfiguration.ConfigurationProperties configuration =
                CollaborationIntegrationsConfiguration.getConfiguration();
        final HttpRequest request = Unirest.post(configuration.discourseUrl() + urlPath)
                .header("Api-Username",configuration.discourseApiUsername())
                .header("Api-Key", configuration.discourseApiKey())
                .header("Content-Type", "application/json")
                .body(body);
        final HttpResponse<String> post = request.asString();
        return post.getBody();
    }

    public static String put(final String urlPath, final JsonObject body) {
        final CollaborationIntegrationsConfiguration.ConfigurationProperties configuration =
                CollaborationIntegrationsConfiguration.getConfiguration();
        final HttpRequest request = Unirest.put(configuration.discourseUrl() + urlPath)
                .header("Api-Username",configuration.discourseApiUsername())
                .header("Api-Key", configuration.discourseApiKey())
                .header("Content-Type", "application/json")
                .body(body);
        final HttpResponse<String> put = request.asString();
        return put.getBody();
    }

    public static String delete(final String urlPath, final JsonObject body) {
        final CollaborationIntegrationsConfiguration.ConfigurationProperties configuration =
                CollaborationIntegrationsConfiguration.getConfiguration();
        final HttpRequest request = Unirest.delete(configuration.discourseUrl() + urlPath)
                .header("Api-Username",configuration.discourseApiUsername())
                .header("Api-Key", configuration.discourseApiKey())
                .header("Content-Type", "application/json")
                .body(body);
        final HttpResponse<String> put = request.asString();
        return put.getBody();
    }

    private static JsonObject toCreateUser(final String username, final String email, final String name) {
        final JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("email", email);
        body.addProperty("username", username);
        body.addProperty("password", UUID.randomUUID().toString());
        body.addProperty("active", true);
        body.addProperty("approved", true);
        return body;
    }

    public static void createUser(final String username, final String email, final String name) {
        post("/users", toCreateUser(username, email, name));
    }

    public static void listUsers(final Consumer<JsonObject> consumer) {
        for (int i = 0; true; i++) {
            final JsonArray users = get("/admin/users/list.json?page=" + i).getAsJsonArray();
            if (users.size() == 0) {
                return;
            }
            users.forEach(user -> consumer.accept(user.getAsJsonObject()));
        }
    }

    private static void listGroups(final Consumer<JsonObject> consumer, final String url) {
        final JsonElement get = get(url == null ? "/groups.json" : url);
        final JsonArray groups;
        String next = null;
        if (get.isJsonArray()) {
            groups = get.getAsJsonArray();
        } else if (get.isJsonObject()) {
            final JsonObject o = get.getAsJsonObject();
            groups = o.get("groups").getAsJsonArray();
            final JsonElement nextE = o.get("load_more_groups");
            if (nextE != null && !nextE.isJsonNull()) {
                next = nextE.getAsString().replace("/groups?", "/groups.json?");
            }
        } else {
            throw new Error("Unexpected resonse: " + get);
        }
        for (final JsonElement group : groups) {
            consumer.accept(group.getAsJsonObject());
        }
        if (next != null && groups.size() > 0) {
            listGroups(consumer, next);
        }
    }

    public static void listGroups(final Consumer<JsonObject> consumer) {
        listGroups(consumer, null);
    }

    public static JsonObject createGroup(final String name, final String displayName) {
        final JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("full_name", displayName);
        body.addProperty("title", displayName);
        final String response = post("/admin/groups", body);
        return new JsonParser().parse(response).getAsJsonObject().get("basic_group").getAsJsonObject();
    }

    public static void listGroupMembers(final String groupId, final Consumer<JsonObject> consumer) {
        for (int i = 0; true; i++) {
            final String url = "/groups/" + groupId + "/members.json?limit=100&offset=" + (i * 100);
            final JsonObject result = get(url).getAsJsonObject();
            final JsonArray members = result.get("members").getAsJsonArray();
            if (members.size() == 0) {
                return;
            }
            members.forEach(member -> consumer.accept(member.getAsJsonObject()));
        }
    }

    private static JsonObject toUsernames(final Stream<String> usernames) {
        final String usernamesToAdd = usernames.collect(Collectors.joining( "," ));
        if (usernamesToAdd.isEmpty()) {
            return null;
        }
        final JsonObject result = new JsonObject();
        result.addProperty("usernames", usernamesToAdd);
        return result;
    }

    public static void removeMembers(final String groupId, final Stream<String> usernames) {
        final JsonObject body = toUsernames(usernames);
        if (body != null) {
            delete("/groups/" + groupId + "/members.json", body);
        }
    }

    public static void addMembers(final String groupId, final Stream<String> usernames) {
        final JsonObject body = toUsernames(usernames);
        if (body != null) {
            put("/groups/" + groupId + "/members.json", body);
        }
    }

    public static void updateMembers(final Set<User> remoteUsers, final JsonObject discourseGroup, final Group group) {
        final String groupId = discourseGroup.get("id").getAsString();

        final Set<User> members = new HashSet<>();
        final Set<String> usersToRemove = new HashSet<>();
        listGroupMembers(discourseGroup.get("name").getAsString(), m -> {
            final String username = m.get("username").getAsString();
            final User user = User.findByUsername(username);
            if (user != null) {
                members.add(user);
            }
            if (user == null || !group.isMember(user)) {
                usersToRemove.add(username);
            }
        });
        if (usersToRemove.size() > 0) {
            removeMembers(groupId, usersToRemove.stream());
        }

        addMembers(groupId, group.getMembers()
                .filter(user -> !members.contains(user))
                .filter(user -> remoteUsers.contains(user))
                .filter(user -> user.getEmail() != null && !user.getEmail().isEmpty())
                .map(u -> u.getUsername()));
    }

}
