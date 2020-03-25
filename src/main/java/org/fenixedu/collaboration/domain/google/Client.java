package org.fenixedu.collaboration.domain.google;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.DefaultSignerFactory;
import io.jsonwebtoken.impl.crypto.Signer;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

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

public class Client {

    private static final String KEY_DIR = "/afs/ist.utl.pt/ciist/fenix/fenix015/ist/google";

    private static String accessToken = null;
    private static LocalDateTime accessTokenValidUnit = LocalDateTime.now().minusSeconds(1);

    private static String jwt() {
        long nowMillis = System.currentTimeMillis();

        final JsonObject header = new JsonObject();
        header.addProperty("alg", "RS256");
        header.addProperty("typ", "JWT");
        final JsonObject claim = new JsonObject();
        claim.addProperty("iss", "fenixedu-tecnico@fenixedu-tecnico.iam.gserviceaccount.com");
        claim.addProperty("sub", "fenixedu-tecnico@fenixedu-tecnico.iam.gserviceaccount.com");
        claim.addProperty("scope", scopes());
        claim.addProperty("aud", "https://oauth2.googleapis.com/token");
        claim.addProperty("iat", Long.toString(nowMillis / 1000));
        claim.addProperty("exp", Long.toString((nowMillis + 3600000l) / 1000));
        final String jwtWithoutSignature = encode(header) + "." + encode(claim);
        return jwtWithoutSignature + "." + sign(jwtWithoutSignature);
    }

    private static String sign(final String jwtWithoutSignature) {
        final byte[] bytesToSign = jwtWithoutSignature.getBytes(Charset.forName("US-ASCII"));

        final File keyFile = new File(KEY_DIR + File.separator + "private_key.der");
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
        final File file = new File(KEY_DIR + File.separator + "scopes.txt");
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
            synchronized (KEY_DIR) {
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

}
