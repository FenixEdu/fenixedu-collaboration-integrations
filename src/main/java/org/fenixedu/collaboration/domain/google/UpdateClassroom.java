package org.fenixedu.collaboration.domain.google;

import com.google.gson.JsonObject;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.fenixedu.collaboration.domain.Collaborator;
import org.fenixedu.collaboration.domain.Utils;

import java.util.function.Consumer;
import java.util.function.Function;

public class UpdateClassroom {

    public static void updateMembers(final CollaborationGroup group) {
        final Function<String, JsonObject> listOwners = Client::listTeachers;
        final Consumer<Collaborator> addOwner = c -> Client.addTeacher(group.getGoogleId(), c.getGoogleId());
        final Consumer<String> removeOwner = id -> {
            try {
                Client.removeTeacher(group.getGoogleId(), id);
            } catch (final Throwable t) {
            }
        };
        Utils.updateMembers(group.getGoogleId(), listOwners, "teachers", group::setGoogleOwnerCount,
                group.getOwnersSet(), addOwner, removeOwner, c -> c.getGoogleId(), "userId");

        final Function<String, JsonObject> listMembers = Client::listStudents;
        final Consumer<Collaborator> addMember = c -> Client.addStudent(group.getGoogleId(), group.getGoogleEnrollmentCode(), c.getGoogleId());
        final Consumer<String> removeMember = id -> {
            try {
                Client.removeStudent(group.getGoogleId(), id);
            } catch (final Throwable t) {
            }
        };
        Utils.updateMembers(group.getGoogleId(), listMembers, "students", group::setGoogleMemberCount,
                group.getMembersSet(), addMember, removeMember, c -> c.getGoogleId(), "userId");

        final String teacherGroupId = Client.readTeacherGroupId();
        group.getOwnersSet().stream()
                .map(c -> c.getGoogleId())
                .filter(id -> id != null && !id.isEmpty())
                .forEach(id -> {
                    try {
                        Client.removeGroupMember(teacherGroupId, id);
                    } catch (final Throwable t) {
                    }
                });
    }

}
