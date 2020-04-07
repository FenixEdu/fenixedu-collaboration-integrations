package org.fenixedu.collaboration.domain.limesurvey;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;
import org.fenixedu.bennu.core.domain.User;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class Client implements Closeable {

    private static JsonObject basicRemoteCall(final String method, final Consumer<JsonArray> paramSetter) {
        final JsonObject body = new JsonObject();
        final JsonArray params = new JsonArray();
        body.addProperty("jsonrpc", "2.0");
        body.addProperty("method", method);
        body.add("params", params);
        paramSetter.accept(params);
        body.addProperty("id", UUID.randomUUID().toString());

        final String url = "https://surveys.tecnico.ulisboa.pt/index.php/admin/remotecontrol";
        final HttpResponse<String> response = Unirest.post(url)
                .header("Content-Type", "application/json")
                .body(body)
                .asString();

        if (response.getStatus() == 200) {
            return new JsonParser().parse(response.getBody()).getAsJsonObject();
        }
        throw new Error(response.getBody());
    }

    private static String getSessionKey() {
        return basicRemoteCall("get_session_key", params -> {
            params.add(CollaborationIntegrationsConfiguration.getConfiguration().limeSurveyUsername());
            params.add(CollaborationIntegrationsConfiguration.getConfiguration().limeSurveyPassword());
        }).get("result").getAsString();
    }

    private JsonObject releaseSessionKey() {
        return basicRemoteCall("release_session_key", params -> {
            params.add(getSessionKey());
        });
    }

    private final String session_key;

    public Client() {
        session_key = basicRemoteCall("get_session_key", params -> {
            params.add("admin");
            params.add("x9H8xPRKG29xEqU8Fkbj8abn");
        }).get("result").getAsString();
    }

    @Override
    public void close() throws IOException {
        releaseSessionKey();
    }

    private JsonObject remoteCall(final String method, final Consumer<JsonArray> paramSetter) {
        return basicRemoteCall(method, params -> {
            params.add(getSessionKey());
            paramSetter.accept(params);
        });
    }

    public void listUsers(final Consumer<JsonObject> consumer) {
        final JsonObject call = remoteCall("list_users", params -> {});
        for (final JsonElement e : call.get("result").getAsJsonArray()) {
            consumer.accept(e.getAsJsonObject());
        }
    }

    public String userIdFor(final String username) {
        String[] result = new String[1];
        listUsers(u -> {
            if (username.equals(u.get("users_name").getAsString())) {
                result[0] = u.get("uid").getAsString();
            }
        });
        return result[0];
    }

    public int addSurvey(final String title) {
        final JsonObject call = remoteCall("add_survey", params -> {
            params.add(-1); // $iSurveyID
            params.add(title); // $sSurveyTitle
            params.add("en"); // $sSurveyLanguage
            params.add("G"); // Format
        });
        return call.get("result").getAsInt();
    }

    public JsonObject setSurveyProperties(final int surveyId, final JsonObject properties) {
        return remoteCall("set_survey_properties", params -> {
            params.add(surveyId);
            params.add(properties);
        });
    }

    public JsonObject setSurveyProperties(final int surveyId, final String ownerId, final String start, final String end) {
        final JsonObject properties = new JsonObject();
        properties.addProperty("owner_id", Integer.parseInt(ownerId));
        properties.addProperty("bounce_email", "no-reply@tecnico.ulisboa.pt");
        properties.addProperty("sendconfirmation", "N");
        properties.addProperty("assessments", "Y");
        properties.addProperty("startdate", start);
        properties.addProperty("expires", end);
        properties.addProperty("usecookie", "Y");
        properties.addProperty("datestamp", "Y");
        properties.addProperty("savetimings", "Y");
        properties.addProperty("tokenanswerspersistence", "Y");
        properties.addProperty("usetokens", "Y");
        properties.addProperty("tokenlength", 35);
        return setSurveyProperties(surveyId, properties);
    }

    public JsonObject getSurveyProperties(final int surveyId) {
        return remoteCall("get_survey_properties", params -> {
            params.add(surveyId); // $iSurveyID
        });
    }

    public JsonObject activateTokens(final int surveyId) {
        return remoteCall("activate_tokens", params -> {
            params.add(surveyId);
            final JsonArray attributes = new JsonArray();
            params.add(attributes);
        });
    }

    public JsonObject addParticipants(final int surveyId, final Set<User> users, final String validFrom, final String validUntil) {
        return remoteCall("add_participants", params -> {
            params.add(surveyId);
            final JsonArray participants = new JsonArray();
            for (final User user : users) {
                final JsonObject participant = new JsonObject();
                participant.addProperty("email", user.getUsername() + "@tecnico.ulisboa.pt");
                participant.addProperty("firstname", user.getProfile().getGivenNames());
                participant.addProperty("lastname", user.getProfile().getFamilyNames());
                participant.addProperty("validfrom", validFrom);
                participant.addProperty("validuntil", validUntil);
                participants.add(participant);
            }
            params.add(participants);
            params.add(true);
        });
    }

    public JsonObject deleteSurvey(final int surveyId) {
        return remoteCall("delete_survey", params -> {
            params.add(surveyId);
        });
    }

}
