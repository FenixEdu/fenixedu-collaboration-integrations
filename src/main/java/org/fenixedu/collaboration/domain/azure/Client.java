package org.fenixedu.collaboration.domain.azure;

import com.google.gson.*;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.commons.stream.StreamUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Consumer;

public class Client {

    private static String accessToken = null;
    private static LocalDateTime accessTokenValidUnit = LocalDateTime.now().minusSeconds(1);
    private static String schoolId = CollaborationIntegrationsConfiguration.getConfiguration().organizationId();

    public static void clearAccessToken() {
        accessTokenValidUnit = LocalDateTime.now().minusSeconds(1);
    }

    public static String getAccessToken() {
        final LocalDateTime now = LocalDateTime.now();
        if (accessTokenValidUnit.isBefore(now)) {
            synchronized (Client.class) {
                if (accessTokenValidUnit.isBefore(now)) {
                    HttpResponse<String> response = Unirest.post("https://login.microsoftonline.com/"
                                + CollaborationIntegrationsConfiguration.getConfiguration().tenentId()
                                + "/oauth2/token")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .field("client_id", CollaborationIntegrationsConfiguration.getConfiguration().clientId())
                            .field("grant_type", "client_credentials")
                            .field("client_secret", CollaborationIntegrationsConfiguration.getConfiguration().clientSecret())
                            .field("resource", "https://graph.microsoft.com/")
                            .asString();
                    final JsonObject jo = new JsonParser().parse(response.getBody()).getAsJsonObject();
                    accessToken = jo.get("access_token").getAsString();
                    accessTokenValidUnit = LocalDateTime.now().plusSeconds(jo.get("expires_in").getAsInt());
                }
            }
        }
        return accessToken;
    }

    public static JsonObject createSchool(final JsonObject body) {
        final String url = "https://graph.microsoft.com/v1.0/education/schools";
        final HttpResponse<String> response = Unirest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        final JsonObject result = new JsonParser().parse(response.getBody()).getAsJsonObject();
        schoolId = result.get("id").getAsString();
        return result;
    }

    public static void users(final Consumer<JsonObject> userConsumer) {
        users(userConsumer, null);
    }

    private static void users(final Consumer<JsonObject> userConsumer, final String skiptoken) {
        final HttpResponse<String> response = getUsersRequest(skiptoken).asString();
        final JsonObject responseJson = new JsonParser().parse(response.getBody()).getAsJsonObject();
        for (final JsonElement user : responseJson.get("value").getAsJsonArray()) {
            userConsumer.accept(user.getAsJsonObject());
        }
        final JsonElement nextLink = responseJson.get("@odata.nextLink");
        if (nextLink != null && !nextLink.isJsonNull()) {
            users(userConsumer, nextLink.getAsString());
        }
    }

    private static GetRequest getUsersRequest(final String nextString) {
        final String url = nextString == null ? "https://graph.microsoft.com/v1.0/users" : nextString;
        final GetRequest get = Unirest.get(url).header("Authorization", "Bearer " + Client.getAccessToken());
        return nextString == null ? get.queryString("$select", "id,userPrincipalName") : get;
    }

    public static JsonObject getEductionUser(final String userId) {
        final HttpResponse<String> response = Unirest.get("https://graph.microsoft.com/v1.0/education/users/" + userId)
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        return new JsonParser().parse(response.getBody()).getAsJsonObject();
    }

    public static JsonObject addUserToSchool(final String userId) {
        final String url = "https://graph.microsoft.com/v1.0/education/schools/" + schoolId + "/users/$ref";
        final JsonObject body = new JsonObject();
        body.addProperty("@odata.id", "https://graph.microsoft.com/v1.0/education/users/" + userId);
        final HttpResponse<String> response = Unirest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        return new JsonParser().parse(response.getBody()).getAsJsonObject();
    }

    public static JsonObject createClass(final JsonObject body) {
        final String url = "https://graph.microsoft.com/v1.0/education/classes";
        final HttpResponse<String> response = Unirest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        return new JsonParser().parse(response.getBody()).getAsJsonObject();
    }

    public static void addTeacher(final String classId, final String userId) {
        final String url = "https://graph.microsoft.com/v1.0/education/classes/" + classId + "/teachers/$ref";
        final JsonObject body = new JsonObject();
        body.addProperty("@odata.id", "https://graph.microsoft.com/v1.0/education/users/" + userId);
        final HttpResponse response = Unirest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

    public static void removeTeacher(final String classId, final String userId) {
        final String url = "https://graph.microsoft.com/v1.0/education/classes/" + classId + "/teachers/" + userId + "/$ref";
        final HttpResponse response = Unirest.delete(url)
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

    public static void addStudent(final String classId, final String userId) {
        final String url = "https://graph.microsoft.com/v1.0/education/classes/" + classId + "/members/$ref";
        final JsonObject body = new JsonObject();
        body.addProperty("@odata.id", "https://graph.microsoft.com/v1.0/education/users/" + userId);
        final HttpResponse response = Unirest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

    public static void removeStudent(final String classId, final String userId) {
        final String url = "https://graph.microsoft.com/v1.0/education/classes/" + classId + "/members/" + userId + "/$ref";
        final HttpResponse response = Unirest.delete(url)
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

    public static void deleteClass(final String classId) {
        final String url = "https://graph.microsoft.com/v1.0/education/classes/" + classId;
        final HttpResponse response = Unirest.delete(url)
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

    public static JsonObject createGrouo(final String name, final String description, final Set<String> owners,
                                         final Set<String> members) {
        final JsonObject body = new JsonObject();
        body.addProperty("displayName", name.toUpperCase());
        body.addProperty("mailNickname", clean(name).toLowerCase());
        body.addProperty("description", description);
        body.addProperty("visibility", "Private");
        final JsonArray groupTypes = new JsonArray();
        groupTypes.add("Unified");
        body.add("groupTypes", groupTypes);
        body.addProperty("mailEnabled", true);
        body.addProperty("securityEnabled", true);
        body.add("owners@odata.bind", toUserArray(owners));
        //body.add("members@odata.bind", toUserArray(members));

        if (body.get("owners@odata.bind").getAsJsonArray().size() < 1) {
            throw new Error("Cannot create team without any owners");
        }

        final HttpResponse<String> response = Unirest.post("https://graph.microsoft.com/v1.0/groups")
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        final JsonObject result = new JsonParser().parse(response.getBody()).getAsJsonObject();
        final JsonElement error = result.get("error");
        if (error != null && !error.isJsonNull()) {
            throw new Error("Error creating group: " + response.getBody());
        }
        return result;
    }

    public static String clean(String string) {
        StringNormalizer.normalize(string.replaceAll(" ", "")
                .replaceAll(",", ""));
    }

    private static JsonArray toUserArray(final Set<String> members) {
        return members.stream()
                .filter(id -> id != null && !id.isEmpty())
                .map(id -> "https://graph.microsoft.com/v1.0/users/" + id)
                .map(link -> new JsonPrimitive(link))
                .collect(StreamUtils.toJsonArray());
    }

    public static JsonObject createTeam(final String groupId) {
        final HttpResponse<String> response = Unirest.put("https://graph.microsoft.com/v1.0/groups/" + groupId + "/team")
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body("{}")
                .asString();
        final JsonObject put = new JsonParser().parse(response.getBody()).getAsJsonObject();
        final JsonElement error = put.get("error");
        if (error != null && !error.isJsonNull()) {
            final HttpResponse<String> get = Unirest.get("https://graph.microsoft.com/v1.0/groups/" + groupId + "/team")
                    .header("Authorization", "Bearer " + Client.getAccessToken())
                    .asString();
            return new JsonParser().parse(get.getBody()).getAsJsonObject();
        }
        return put;
    }

    public static JsonObject addMemberToGroup(final String groupId, final String member) {
        final JsonObject body = new JsonObject();
        body.addProperty("@odata.id", "https://graph.microsoft.com/v1.0/directoryObjects/" + member);
        final HttpResponse<String> response = Unirest.post("https://graph.microsoft.com/v1.0/groups/" + groupId + "/members/$ref")
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        return new JsonParser().parse(response.getBody()).getAsJsonObject();
    }

    public static JsonObject addOwnerToGroup(final String groupId, final String member) {
        final JsonObject body = new JsonObject();
        body.addProperty("@odata.id", "https://graph.microsoft.com/v1.0/directoryObjects/" + member);
        final HttpResponse<String> response = Unirest.post("https://graph.microsoft.com/v1.0/groups/" + groupId + "/owners/$ref")
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .body(body.toString())
                .asString();
        return new JsonParser().parse(response.getBody()).getAsJsonObject();
    }

    private static JsonObject listCollaborators(final String url) {
        final JsonObject result = new JsonObject();
        final JsonArray list = new JsonArray();
        listCollaborators(url, list);
        result.add("value", list);
        return result;
    }

    private static void listCollaborators(final String url, final JsonArray list) {
        final HttpResponse<String> response = Unirest.get(url)
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        final JsonObject result = new JsonParser().parse(response.getBody()).getAsJsonObject();
        final JsonElement value = result.get("value");
        if (value != null && !value.isJsonNull()) {
            list.addAll(value.getAsJsonArray());
        }
        final JsonElement nextLinkElement = result.get("@odata.nextLink");
        if (nextLinkElement != null && !nextLinkElement.isJsonNull()) {
            listCollaborators(nextLinkElement.getAsString(), list);
        }
    }

    public static JsonObject listMembers(final String groupId) {
        return listCollaborators("https://graph.microsoft.com/v1.0/groups/" + groupId + "/members");
    }

    public static JsonObject listOwners(final String groupId) {
        return listCollaborators("https://graph.microsoft.com/v1.0/groups/" + groupId + "/owners");
    }

    public static JsonObject listStudents(final String groupId) {
        return listCollaborators("https://graph.microsoft.com/v1.0/education/classes/" + groupId + "/members");
    }

    public static JsonObject listTeachers(final String groupId) {
        return listCollaborators("https://graph.microsoft.com/v1.0/education/classes/" + groupId + "/teachers");
    }

    public static void removeOwner(final String groupId, final String owner) {
        final String url = "https://graph.microsoft.com/v1.0/groups/" + groupId + "/owners/" + owner +"/$ref";
        final HttpResponse response = Unirest.delete(url)
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

    public static void removeMember(final String groupId, final String member) {
        final String url = "https://graph.microsoft.com/v1.0/groups/" + groupId + "/members/" + member +"/$ref";
        final HttpResponse response = Unirest.delete(url)
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

    public static void deleteGroup(final String groupId) {
        final HttpResponse response = Unirest.delete("https://graph.microsoft.com/v1.0/groups/" + groupId)
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + Client.getAccessToken())
                .asString();
        if (response.getStatus() != 204) {
            throw new Error("Error code: " + response.getStatus());
        }
    }

}
