package org.fenixedu.collaboration.task;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.fenixedu.collaboration.domain.Collaborator;
import org.fenixedu.collaboration.domain.azure.Client;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Task(readOnly = true, englishTitle = "Activate Azure Microsoft Teams")
public class ActivateAllClasses extends CronTask {

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    @Override
    public void runTask() throws Exception {
        ExecutionSemester.readActualExecutionSemester().getAssociatedExecutionCoursesSet().stream()
                .forEach(ec -> processTx(ec));
        taskLog("Done");
    }

    private void processTx(final ExecutionCourse executionCourse) {
        try {
            FenixFramework.atomic(() -> {
                process(executionCourse);
            });
        } catch (final Throwable t) {
            taskLog("Skipping %s (%s) due to error: %s%n", executionCourse.getNome(),
                    executionCourse.getExternalId(), t.getMessage());
        }
    }

    private void process(final ExecutionCourse executionCourse) {
        CollaborationGroup group = executionCourse.getCollaborationGroup();
        if (group == null) {
//            taskLog("Creating group %s%n", executionCourse.getNome());
//            group = CollaborationGroup.create(executionCourse);
        } else {
            if (group.getAzureUrl() == null || group.getAzureUrl().isEmpty()) {
                taskLog("Creating team %s (%s)%n", executionCourse.getNome(), executionCourse.getExternalId());
                group.createTeam();
                taskLog("   team url: %s%n", group.getAzureUrl());
            } else {
                taskLog("Updateing members for %s%n", executionCourse.getNome());
                updateMembers(group);
                count("Teachers", Client.listTeachers(group.getAzureId()), group.getOwnersSet());
                count("Students", Client.listStudents(group.getAzureId()), group.getMembersSet());
            }
        }
    }

    private void count(final String prefix, JsonObject list, final Set<Collaborator> set) {
        final long count = set.stream().filter(c -> c.getAzureId() != null && !c.getAzureId().isEmpty()).count();
        final JsonElement value = list.get("value");
        if (value != null && !value.isJsonNull()) {
            taskLog("   %s: %s - with azureId: %s%n", prefix, value.getAsJsonArray().size(), count);
        } else {
            taskLog("   %s: %s - with azureId: %s%n", prefix, " -- none -- ", count);
        }
    }

    private static void add(final Set<Collaborator> collaborators, final Set<User> users) {
        users.stream()
                .map(user -> user.getCollaborator())
                .filter(c -> c != null && c.getAzureId() != null && !c.getAzureId().isEmpty())
                .forEach(c -> collaborators.add(c));
    }

    public void updateMembers(final CollaborationGroup group) {
        final ExecutionCourse executionCourse = group.getExecutionCourse();
        if (executionCourse != null) {
            final Set<User> owners = executionCourse.getProfessorshipsSet().stream()
                    .map(p -> p.getPerson().getUser())
                    .collect(Collectors.toSet());
            add(group.getOwnersSet(), owners);
            group.getOwnersSet().stream()
                    .filter(c -> !owners.contains(c.getUser()))
                    .forEach(c -> group.getOwnersSet().remove(c));
            final Set<User> members = executionCourse.getAttendsSet().stream()
                    .map(a -> a.getRegistration().getPerson().getUser())
                    .collect(Collectors.toSet());
            add(group.getMembersSet(), members);
            group.getMembersSet().stream()
                    .filter(c -> !members.contains(c.getUser()))
                    .forEach(c -> group.getMembersSet().remove(c));
        }

        final Set<String> owners = new HashSet<>();
        final JsonObject ownerObject = executionCourse == null ? Client.listOwners(group.getAzureId())
                : Client.listTeachers(group.getAzureId());
        for (final JsonElement jsonElement : ownerObject.get("value").getAsJsonArray()) {
            final String id = jsonElement.getAsJsonObject().get("id").getAsString();
            owners.add(id);
            if (group.getOwnersSet().stream().noneMatch(c -> id.equals(c.getAzureId()))) {
                if (executionCourse == null) {
                    Client.removeOwner(group.getAzureId(), id);
                } else {
                    Client.removeTeacher(group.getAzureId(), id);
                }
            }
        }
        group.getOwnersSet().stream()
                .filter(c -> !owners.contains(c.getAzureId()))
                .forEach(c -> {
                    if (executionCourse == null) {
                        Client.addOwnerToGroup(group.getAzureUrl(), c.getAzureId());
                    } else {
                        Client.addTeacher(group.getAzureId(), c.getAzureId());
                    }
                });

        final Set<String> members = new HashSet<>();
        final JsonObject memberObject = executionCourse == null ? Client.listMembers(group.getAzureId())
                : Client.listStudents(group.getAzureId());
        for (final JsonElement jsonElement : memberObject.get("value").getAsJsonArray()) {
            final String id = jsonElement.getAsJsonObject().get("id").getAsString();
            members.add(id);
            if (group.getMembersSet().stream().noneMatch(c -> id.equals(c.getAzureId()))) {
                if (executionCourse == null) {
                    Client.removeMember(group.getAzureId(), id);
                } else {
                    Client.removeStudent(group.getAzureId(), id);
                }
            }
        }
        group.getMembersSet().stream()
                .filter(c -> !members.contains(c.getAzureId()))
                .forEach(c -> {
                    try {
                        if (executionCourse == null) {
                            Client.addMemberToGroup(group.getAzureUrl(), c.getAzureId());
                        } else {
                            Client.addStudent(group.getAzureId(), c.getAzureId());
                        }
                        Thread.sleep(1000);
                    } catch (final Throwable t) {
                        taskLog("   unable to add member %s (%s) to group %s%n", c.getUser().getUsername(), c.getAzureId(), group.getName());
                    }
                });
    }

}
