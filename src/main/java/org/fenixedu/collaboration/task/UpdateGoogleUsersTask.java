package org.fenixedu.collaboration.task;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.collaboration.domain.Collaborator;
import org.fenixedu.collaboration.domain.google.Client;

import java.util.Map;

@Task(readOnly = true, englishTitle = "Read Google Users and store their remote IDs")
public class UpdateGoogleUsersTask extends CronTask {

    @Override
    public void runTask() throws Exception {
        final Map<String, User> userMap = Utils.emailUserMap();
        taskLog("Loaded %s user keys to UserMap.%n", userMap.size());
        Client.listUsers(userJson -> {
            final String id = userJson.get("id").getAsString();
            final String emailAddress = userJson.get("primaryEmail").getAsString();
            final User user = userMap.get(emailAddress);
            if (user != null) {
                Collaborator.setUserGoogleId(user, id);
            }
        });
        taskLog("Clearing teachers");
        final String teacherGroupId = Client.readTeacherGroupId();
        if (teacherGroupId != null) {
            Client.listGroupMembers(teacherGroupId, m -> {
                Client.removeGroupMember(teacherGroupId, m.get("id").getAsString());
            });
        }
    }

}
