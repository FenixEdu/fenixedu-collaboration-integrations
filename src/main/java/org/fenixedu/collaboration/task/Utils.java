package org.fenixedu.collaboration.task;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Utils {

    public static void prettyPrint(final String s, final Consumer<String> logger) {
        prettyPrint(new JsonParser().parse(s).getAsJsonObject(), 0, logger);
    }

    public static void prettyPrint(final JsonObject jo, final int o, final Consumer<String> logger) {
        pad(o, "{", logger);
        for (final Map.Entry<String, JsonElement> e : jo.entrySet()) {
            final JsonElement v = e.getValue();
            if (v.isJsonNull()) {
                pad(o + 1, e.getKey() + ": " + null, logger);
            } else if (v.isJsonPrimitive()) {
                pad(o + 1, e.getKey() + ": " + v.getAsString(), logger);
            } else if (v.isJsonObject()) {
                pad(o + 1, e.getKey() + ": ", logger);
                prettyPrint(v.getAsJsonObject(), o + 1, logger);
            } else if (v.isJsonArray()) {
                pad(o + 1, e.getKey() + ": [", logger);
                for (final JsonElement je : v.getAsJsonArray()) {
                    if (je.isJsonNull()) {
                        pad(o + 2, "null", logger);
                    } else if (je.isJsonPrimitive()) {
                        pad(o + 2, je.getAsString(), logger);
                    } else if (je.isJsonObject()) {
                        prettyPrint(je.getAsJsonObject(), o + 2, logger);
                    } else if (je.isJsonArray()) {
                        logger.accept("Array of Arrays!");
                    }
                }
                pad(o + 1, "]", logger);
            }
        }
        pad(o, "}", logger);
    }

    public static void pad(final int i, final String s, final Consumer<String> logger) {
        final StringBuilder builder = new StringBuilder();
        for (int j = 0; j < i; j++) {
            builder.append(" ");
        }
        builder.append(s);
        logger.accept(builder.toString());
    }

    public static Map<String, User> emailUserMap() {
        final Map<String, User> userMap = new HashMap<>();
        Bennu.getInstance().getUserSet().stream()
                .forEach(user -> {
                    final Person person = user.getPerson();
                    if (person != null) {
                        final String username = user.getUsername();
                        final String domain = CollaborationIntegrationsConfiguration.getConfiguration().organizationDomain();
                        userMap.put(username + domain, user);
                        person.getEmailAddressStream()
                                .map(ea -> ea.getValue())
                                .filter(v -> v.endsWith(domain))
                                .forEach(e -> userMap.put(e, user));
                    }
                });
        return userMap;
    }

}
