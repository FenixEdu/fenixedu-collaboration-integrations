package org.fenixedu.collaboration.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.collaboration.domain.limesurvey.Client;
import org.joda.time.DateTime;
import pt.ist.fenixframework.Atomic;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class LimeSurvey extends LimeSurvey_Base {

    private static final String DT_FORMAT = "yyyy-MM-dd HH:mm";

    public LimeSurvey(final ExecutionCourse executionCourse, final String title,
                      final DateTime start, final DateTime end) {
        setTitle(title);
        setExecutionCourse(executionCourse);
        setStartSurvey(start);
        setEndSurvey(end);
        createRemoteSurvey();
    }

    private void createRemoteSurvey() {
        try (final Client client = new Client()) {
            final int surveyId = client.addSurvey(getTitle());
            setSurveyId(surveyId);
            final User user = Authenticate.getUser();
            final String surveyUserId = client.userIdFor(user.getUsername());
            final JsonObject props = client.setSurveyProperties(surveyId, surveyUserId, getStartSurvey().toString(DT_FORMAT),
                    getEndSurvey().toString(DT_FORMAT));
            System.out.println("Survey " + surveyId + " " + surveyUserId + " " + props.toString());
            client.activateTokens(surveyId);
            final Set<User> users = getExecutionCourse().getAttendsSet().stream()
                    .map(a -> a.getRegistration().getPerson().getUser())
                    .collect(Collectors.toSet());
            final JsonObject participants = client.addParticipants(surveyId, users, getStartSurvey().toString(DT_FORMAT), getEndSurvey().toString(DT_FORMAT));
            for (final JsonElement participantElement : participants.get("result").getAsJsonArray()) {
                final JsonObject participant = participantElement.getAsJsonObject();
                final String email = participant.get("email").getAsString();
                final String token = participant.get("token").getAsString();
                final User participantUser = users.stream()
                        .filter(u -> email.equals(u.getUsername() + "@tecnico.ulisboa.pt"))
                        .findAny().orElseThrow(() -> new Error("Unexpected user in survey: " + email));
                new LimeSurveyParticipant(this, participantUser, token);
            }
        } catch (final IOException ex) {
            throw new Error(ex);
        }
    }

    @Atomic
    public static LimeSurvey createSurvey(final ExecutionCourse executionCourse, final String title,
                                          final DateTime start, final DateTime end) {
        return new LimeSurvey(executionCourse, title, start, end);
    }

    @Atomic
    public void delete() {
        try (final Client client = new Client()) {
            client.deleteSurvey(getSurveyId());
        } catch (final IOException ex) {
            throw new Error(ex);
        }
        getLimeSurveyParticipantSet().forEach(LimeSurveyParticipant::delete);
        setExecutionCourse(null);
        deleteDomainObject();
    }

}
