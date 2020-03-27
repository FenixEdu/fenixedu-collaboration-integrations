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
        final Map<String, User> userMap = Utils.emailUserMap();
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
