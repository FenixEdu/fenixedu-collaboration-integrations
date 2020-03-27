package org.fenixedu.collaboration.domain.azure;

import com.google.gson.JsonObject;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.fenixedu.collaboration.domain.Collaborator;
import org.fenixedu.collaboration.domain.Utils;

import java.util.function.Consumer;
import java.util.function.Function;

public class UpdateTeam {

    public static void updateMembers(final CollaborationGroup group) {
        final Function<String, JsonObject> listOwners;
        final Consumer<Collaborator> addOwner;
        final Consumer<String> removeOwner;
        if (group.getExecutionCourse() == null) {
            listOwners = Client::listOwners;
            addOwner = c -> Client.addOwnerToGroup(group.getAzureId(), c.getAzureId());
            removeOwner = id -> Client.removeOwner(group.getAzureId(), id);
        } else {
            listOwners = Client::listTeachers;
            addOwner = c -> Client.addTeacher(group.getAzureId(), c.getAzureId());
            removeOwner = id -> Client.removeTeacher(group.getAzureId(), id);
        }
        Utils.updateMembers(group.getAzureId(), listOwners, "value", group::setAzureOwnerCount,
                group.getOwnersSet(), addOwner, removeOwner);

        final Function<String, JsonObject> listMembers;
        final Consumer<Collaborator> addMember;
        final Consumer<String> removeMember;
        if (group.getExecutionCourse() == null) {
            listMembers = Client::listMembers;
            addMember = c -> Client.addMemberToGroup(group.getAzureId(), c.getAzureId());
            removeMember = id -> Client.removeMember(group.getAzureId(), id);
        } else {
            listMembers = Client::listStudents;
            addMember = c -> Client.addStudent(group.getAzureId(), c.getAzureId());
            removeMember = id -> Client.removeStudent(group.getAzureId(), id);
        }
        Utils.updateMembers(group.getAzureId(), listMembers, "value", group::setAzureMemberCount,
                group.getOwnersSet(), addMember, removeMember);
    }

}
