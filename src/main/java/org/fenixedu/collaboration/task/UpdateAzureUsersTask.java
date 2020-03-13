package org.fenixedu.collaboration.task;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.collaboration.domain.Collaborator;
import org.fenixedu.collaboration.domain.azure.Client;

import java.util.HashMap;
import java.util.Map;

@Task(readOnly = true, englishTitle = "Read Azure Users and store their remote IDs")
public class UpdateAzureUsersTask extends CronTask {

    @Override
    public void runTask() throws Exception {
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
        taskLog("Loaded %s user keys to UserMap.%n", userMap.size());
        Client.users(userJson -> {
            final String id = userJson.get("id").getAsString();
            final String userPrincipalName = userJson.get("userPrincipalName").getAsString();
            final User user = userMap.get(userPrincipalName);
            if (user != null) {
                Collaborator.setUserAzureId(user, id);
            }
        });
    }

}
