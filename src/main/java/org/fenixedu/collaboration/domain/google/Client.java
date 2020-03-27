package org.fenixedu.collaboration.domain.google;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.DefaultSignerFactory;
import io.jsonwebtoken.impl.crypto.Signer;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class Client {

    private static String accessToken = null;
    private static LocalDateTime accessTokenValidUnit = LocalDateTime.now().minusSeconds(1);

    private static String jwt() {
        long nowMillis = System.currentTimeMillis();

        final JsonObject header = new JsonObject();
        header.addProperty("alg", "RS256");
        header.addProperty("typ", "JWT");
        final JsonObject claim = new JsonObject();
        claim.addProperty("iss", CollaborationIntegrationsConfiguration.getConfiguration().googleServiceClientISS());
        claim.addProperty("sub", CollaborationIntegrationsConfiguration.getConfiguration().googleServiceClientSubject());
        claim.addProperty("scope", scopes());
        claim.addProperty("aud", "https://oauth2.googleapis.com/token");
        claim.addProperty("iat", Long.toString(nowMillis / 1000));
        claim.addProperty("exp", Long.toString((nowMillis + 3600000l) / 1000));
        final String jwtWithoutSignature = encode(header) + "." + encode(claim);
        return jwtWithoutSignature + "." + sign(jwtWithoutSignature);
    }

    private static String sign(final String jwtWithoutSignature) {
        final byte[] bytesToSign = jwtWithoutSignature.getBytes(Charset.forName("US-ASCII"));

        final File keyFile = new File(CollaborationIntegrationsConfiguration.getConfiguration().googleDir()
                + File.separator + "private_key.der");
        try {
            final byte[] key = Files.readAllBytes(keyFile.toPath());

            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            final Key signingKey = kf.generatePrivate(spec);

            final Signer signer = DefaultSignerFactory.INSTANCE.createSigner(SignatureAlgorithm.RS256, signingKey);
            final byte[] signature = signer.sign(bytesToSign);
            return TextCodec.BASE64URL.encode(signature);
        } catch (final IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new Error(e);
        }
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

    private static String encode(final JsonObject jo) {
        return TextCodec.BASE64URL.encode(jo.toString());
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

    private static void get(final String url, final Consumer<JsonObject> consumer, final String objectArrayField,
                     final String nextPageToken) {
        HttpRequest request = Unirest.get(url).header("Authorization", "Bearer " + getAccessToken());
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
        if (nextPageToken != null && !nextPageTokenElement.isJsonNull()) {
            get(url, consumer, objectArrayField, nextPageTokenElement.getAsString());
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

    public static void listUsers(final Consumer<JsonObject> courseConsumer) {
        get("https://www.googleapis.com/admin/directory/v1/users?customer=" + CollaborationIntegrationsConfiguration.getConfiguration().googleOrganizationId(),
                courseConsumer, "users", null);
    }

    public static JsonObject classroomProfile(final String email) {
        return get("https://classroom.googleapis.com/v1/userProfiles/" + email);
    }

    public static void listCourses(final Consumer<JsonObject> courseConsumer) {
        get("https://classroom.googleapis.com/v1/courses", courseConsumer, "courses", null);
    }

    public static void listStudents(final String courseId, final Consumer<JsonObject> courseConsumer) {
        get("https://classroom.googleapis.com/v1/courses/" + courseId + "/students", courseConsumer, "students", null);
    }

    public static JsonObject listStudents(final String courseId) {
        final JsonObject result = new JsonObject();
        final JsonArray students = new JsonArray();
        listTeachers(courseId, student -> students.add(student));
        result.add("students", students);
        return result;
    }

    public static void listTeachers(final String courseId, final Consumer<JsonObject> courseConsumer) {
        get("https://classroom.googleapis.com/v1/courses/" + courseId + "/teachers", courseConsumer, "teachers", null);
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
        delete("https://classroom.googleapis.com/v1/courses/" + courseId + "/teachers" + userId);
    }

    public static void removeStudent(String courseId, String userId) {
        delete("https://classroom.googleapis.com/v1/courses/" + courseId + "/students" + userId);
    }

}
