package org.fenixedu.collaboration.domain.google;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.SignatureAlgorithm;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;
import org.fenixedu.jwt.Tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

public class Client {

    private static String accessToken = null;
    private static LocalDateTime accessTokenValidUnit = LocalDateTime.now().minusSeconds(1);

    public static void clearAccessToken() {
        accessTokenValidUnit = LocalDateTime.now().minusSeconds(1);
    }

    private static String jwt() {
        final JsonObject claim = new JsonObject();
        claim.addProperty("iss", CollaborationIntegrationsConfiguration.getConfiguration().googleServiceClientISS());
        claim.addProperty("sub", CollaborationIntegrationsConfiguration.getConfiguration().googleServiceClientSubject());
        claim.addProperty("scope", scopes());
        claim.addProperty("aud", "https://oauth2.googleapis.com/token");
        return sign(claim);
    }

    private static String sign(final JsonObject claim) {
        final String privateKeyPathname = CollaborationIntegrationsConfiguration.getConfiguration().googleDir()
                + File.separator + "private_key.der";
        return Tools.sign(SignatureAlgorithm.RS256, privateKeyPathname, claim);
    }

    private static String scopes() {
        final File file = new File(CollaborationIntegrationsConfiguration.getConfiguration().googleDir()
                + File.separator + "scopes.txt");
        try {
            return new String(Files.readAllBytes(file.toPath())).replaceAll("\n", " ");
        } catch (final IOException e) {
            throw new Error(e);
        }
    }

    public static String getAccessToken() {
        final LocalDateTime now = LocalDateTime.now();
        if (accessTokenValidUnit.isBefore(now)) {
            synchronized (accessTokenValidUnit) {
                if (accessTokenValidUnit.isBefore(now)) {
                    final LocalDateTime expires = LocalDateTime.now().plusSeconds(3600);
                    final String urlPost = "https://oauth2.googleapis.com/token";
                    final HttpResponse<String> post = Unirest.post(urlPost).multiPartContent()
                            .field("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                            .field("assertion", jwt())
                            .asString();

                    if (post.getStatus() != 200) {
                        throw new Error("Error " + post.getStatus() + " getting access token: " + post.getBody());
                    }
                    final JsonObject response = new JsonParser().parse(post.getBody()).getAsJsonObject();
                    accessToken = response.get("access_token").getAsString();
                    accessTokenValidUnit = expires;
                }
            }
        }
        return accessToken;
    }

    private static void get(final String url, final Function<HttpRequest, HttpRequest> requestBuilder,
                            final Consumer<JsonObject> consumer, final String objectArrayField,
                            final String nextPageToken) {
        HttpRequest request = Unirest.get(url).header("Authorization", "Bearer " + getAccessToken());
        if (requestBuilder != null) {
            request = requestBuilder.apply(request);
        }
        if (nextPageToken != null) {
            request = request.queryString("pageToken", nextPageToken);
        }
        final HttpResponse<String> get = request.asString();
        if (get.getStatus() != 200) {
            throw new Error("url " + get.getStatus() + " " + get.getBody());
        }
        final JsonObject result = new JsonParser().parse(get.getBody()).getAsJsonObject();
        final JsonElement array = result.get(objectArrayField);
        if (array != null && !array.isJsonNull() && array.isJsonArray()) {
            for (final JsonElement o : array.getAsJsonArray()) {
                consumer.accept(o.getAsJsonObject());
            }
        }
        final JsonElement nextPageTokenElement = result.get("nextPageToken");
        if (nextPageTokenElement != null && !nextPageTokenElement.isJsonNull()) {
            get(url, null, consumer, objectArrayField, nextPageTokenElement.getAsString());
        }
    }

    private static JsonObject get(final String url) {
        final HttpResponse<String> get = Unirest.get(url)
                .header("Authorization", "Bearer " + getAccessToken())
                .asString();
        if (get.getStatus() != 200) {
            throw new Error("url " + get.getStatus() + " " + get.getBody());
        }
        return new JsonParser().parse(get.getBody()).getAsJsonObject();
    }

    public static JsonObject post(final String url, final JsonObject body) {
        final HttpResponse<String> get = Unirest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAccessToken())
                .body(body.toString())
                .asString();
        if (get.getStatus() != 200) {
            throw new Error("url " + get.getStatus() + " " + get.getBody());
        }
        return new JsonParser().parse(get.getBody()).getAsJsonObject();
    }

    public static void delete(final String url) {
        final HttpResponse<String> get = Unirest.delete(url)
                .header("Authorization", "Bearer " + getAccessToken())
                .asString();
        if (get.getStatus() != 200) {
            throw new Error("url " + get.getStatus() + " " + get.getBody());
        }
    }

    public static void listGroupMembers(final String groupId, final Consumer<JsonObject> courseConsumer) {
        get("https://www.googleapis.com/admin/directory/v1/groups/" + groupId + "/members",
                null, courseConsumer, "members", null);
    }

    public static void listGroups(final Consumer<JsonObject> courseConsumer) {
        get("https://www.googleapis.com/admin/directory/v1/groups?customer=" + CollaborationIntegrationsConfiguration.getConfiguration().googleOrganizationId(),
                null, courseConsumer, "groups", null);
    }

    public static void listUsers(final Consumer<JsonObject> courseConsumer) {
        get("https://www.googleapis.com/admin/directory/v1/users?customer=" + CollaborationIntegrationsConfiguration.getConfiguration().googleOrganizationId(),
                null, courseConsumer, "users", null);
    }

    public static JsonObject classroomProfile(final String email) {
        return get("https://classroom.googleapis.com/v1/userProfiles/" + email);
    }

    public static void listCourses(final Consumer<JsonObject> courseConsumer) {
        get("https://classroom.googleapis.com/v1/courses", null, courseConsumer, "courses", null);
    }

    public static void listCourses(final String teacherId, final String studentId, final Consumer<JsonObject> courseConsumer) {
        final Function<HttpRequest, HttpRequest> paramBuilder = r -> {
            if (teacherId != null && !teacherId.isEmpty()) {
                return r.queryString("teacherId", teacherId);
            }
            if (studentId != null && !studentId.isEmpty()) {
                return r.queryString("studentId", studentId);
            }
            return r;
        };
        get("https://classroom.googleapis.com/v1/courses", paramBuilder, courseConsumer, "courses", null);
    }

    public static void listStudents(final String courseId, final Consumer<JsonObject> courseConsumer) {
        get("https://classroom.googleapis.com/v1/courses/" + courseId + "/students", null, courseConsumer, "students", null);
    }

    public static JsonObject listStudents(final String courseId) {
        final JsonObject result = new JsonObject();
        final JsonArray students = new JsonArray();
        listStudents(courseId, student -> students.add(student));
        result.add("students", students);
        return result;
    }

    public static void listTeachers(final String courseId, final Consumer<JsonObject> courseConsumer) {
        get("https://classroom.googleapis.com/v1/courses/" + courseId + "/teachers", null, courseConsumer, "teachers", null);
    }

    public static JsonObject listTeachers(final String courseId) {
        final JsonObject result = new JsonObject();
        final JsonArray teachers = new JsonArray();
        listTeachers(courseId, teacher -> teachers.add(teacher));
        result.add("teachers", teachers);
        return result;
    }

    public static JsonObject course(final String courseId) {
        return get("https://classroom.googleapis.com/v1/courses/" + courseId);
    }

    public static JsonObject createCourse(final String ownerId, final String name, final String section) {
        final JsonObject course = new JsonObject();
        course.addProperty("name", name);
        course.addProperty("section", section);
        course.addProperty("ownerId", ownerId);
        return post("https://classroom.googleapis.com/v1/courses", course);
    }

    public static JsonObject addTeacher(final String courseId, final String userId) {
        final JsonObject teacher = new JsonObject();
        teacher.addProperty("userId", userId);
        return post("https://classroom.googleapis.com/v1/courses/" + courseId + "/teachers", teacher);
    }

    public static JsonObject addStudent(final String courseId, final String enrollmentCode, final String userId) {
        final JsonObject student = new JsonObject();
        student.addProperty("userId", userId);
        return post("https://classroom.googleapis.com/v1/courses/" + courseId + "/students", student);
    }

    public static void removeTeacher(String courseId, String userId) {
        delete("https://classroom.googleapis.com/v1/courses/" + courseId + "/teachers/" + userId);
    }

    public static void removeStudent(String courseId, String userId) {
        delete("https://classroom.googleapis.com/v1/courses/" + courseId + "/students/" + userId);
    }

    public static void deleteCourse(final String courseId) {
        delete("https://classroom.googleapis.com/v1/courses/" + courseId);
    }

    public static void removeGroupMember(final String groupId, final String memberId) {
        delete("https://www.googleapis.com/admin/directory/v1/groups/" + groupId + "/members/" + memberId);
    }

    public static String readTeacherGroupId() {
        final String[] result = new String[1];
        Client.listGroups(g -> {
            final String name = g.get("name").getAsString();
            if ("Classroom Teachers".equals(name)) {
                result[0] = g.get("id").getAsString();
            }
        });
        return result[0];
    }

}
