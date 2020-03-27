package org.fenixedu.collaboration.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.bennu.CollaborationIntegrationsConfiguration;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.collaboration.domain.azure.Client;
import pt.ist.fenixframework.Atomic;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollaborationGroup extends CollaborationGroup_Base {
    
    public CollaborationGroup(final String groupName, final Set<User> owners, final Set<User> members) {
        super();
        setName(groupName);
        add(getOwnersSet(), owners);
        add(getMembersSet(), members);
    }

    private static Set<String> getAzureIds(final Set<Collaborator> collaborators) {
        return collaborators.stream()
                .map(c -> c.getAzureId())
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());
    }

    private static void add(final Set<Collaborator> collaborators, final Set<User> users) {
        users.stream()
                .map(user -> user.getCollaborator())
                .filter(c -> c != null)
                .forEach(c -> collaborators.add(c));
    }

    private static CollaborationGroup createGroup(final ExecutionCourse executionCourse) {
        CollaborationGroup group = executionCourse.getCollaborationGroup();
        final String groupDescription = courseDescription(executionCourse);
        if (group == null) {
            final String groupName = CollaborationIntegrationsConfiguration.getConfiguration().organizationPrefix()
                    + "-course-" + courseSlug(executionCourse);
            final Set<User> owners = executionCourse.getProfessorshipsSet().stream()
                    .map(p -> p.getPerson().getUser())
                    .collect(Collectors.toSet());
            final Set<User> members = executionCourse.getAttendsSet().stream()
                    .map(a -> a.getRegistration().getPerson().getUser())
                    .collect(Collectors.toSet());
            group = new CollaborationGroup(groupName, owners, members);
            group.setExecutionCourse(executionCourse);
        }
        return group;
    }

    @Atomic
    public static CollaborationGroup createGoogleClassroom(ExecutionCourse executionCourse) {
        final CollaborationGroup group = createGroup(executionCourse);
        final String groupDescription = courseDescription(executionCourse);

        final String ownerId = Authenticate.getUser().getCollaborator().getGoogleId();
        final JsonObject result = org.fenixedu.collaboration.domain.google.Client.createCourse(ownerId,
                group.getName(), groupDescription);
        group.setGoogleId(result.get("id").getAsString());
        group.setGoogleUrl(result.get("alternateLink").getAsString());
        group.setGoogleEnrollmentCode(result.get("enrollmentCode").getAsString());
        return group;
    }

    @Atomic
    public static CollaborationGroup createAzureGroup(final String name, final String description) {
        final String groupName = CollaborationIntegrationsConfiguration.getConfiguration().organizationPrefix()
                + "-" + Authenticate.getUser().getUsername().toUpperCase() + "-" + name.toUpperCase();
        final Set<User> owners = Stream.of(Authenticate.getUser()).collect(Collectors.toSet());
        final CollaborationGroup group = new CollaborationGroup(groupName, owners, Collections.emptySet());
        final JsonObject result = Client.createGrouo(groupName, description, getAzureIds(group.getOwnersSet()),
                Collections.emptySet());
        group.setAzureId(result.get("id").getAsString());
        return group;
    }

    @Atomic
    public static CollaborationGroup createAzureGroup(final ExecutionCourse executionCourse) {
        final CollaborationGroup group = createGroup(executionCourse);
        final String groupDescription = courseDescription(executionCourse);

        final JsonObject body = new JsonObject();
        body.addProperty("externalId", "IST" + executionCourse.getExternalId());
        body.addProperty("description", groupDescription);
        body.addProperty("classCode", group.getName());
        body.addProperty("displayName", executionCourse.getName());
        body.addProperty("externalName", executionCourse.getName());
        body.addProperty("externalSource", "FenixEdu@tecnico.ulisboa.pt");
        body.addProperty("mailNickname", group.getName());
        final JsonObject result = Client.createClass(body);
        group.setAzureId(result.get("id").getAsString());
        return group;
    }

    private static String courseSlug(final ExecutionCourse executionCourse) {
        final ExecutionSemester executionPeriod = executionCourse.getExecutionPeriod();
        final StringBuilder builder = new StringBuilder();
        builder.append(normalise(executionPeriod.getQualifiedName()));
        builder.append("-");
        builder.append(normalise(executionCourse.getSigla()));
        return builder.toString();
    }

    private static String courseDescription(final ExecutionCourse executionCourse) {
        final ExecutionSemester executionPeriod = executionCourse.getExecutionPeriod();
        final StringBuilder builder = new StringBuilder();
        builder.append(executionCourse.getName());
        builder.append(" - ");
        builder.append(executionPeriod.getQualifiedName());
        builder.append(" - ");
        builder.append(executionCourse.getDegreePresentationString());
        return builder.toString();
    }

    private static String normalise(String string) {
        return string.replace(" ", "")
                .replace("/", "")
                .replace("-", "")
                .replace("º", "")
                .replace("ª", "");
    }

    @Atomic
    public void createAzureTeam() {
        if (getAzureId() != null && !getAzureId().isEmpty()) {
            final JsonObject jsonObject = Client.createTeam(getAzureId());
            final JsonElement webUrl = jsonObject.get("webUrl");
            if (webUrl != null && !webUrl.isJsonNull()) {
                setAzureUrl(webUrl.getAsString());
            }
        }
    }

    @Atomic
    public void updateMembers() {
        final ExecutionCourse executionCourse = getExecutionCourse();
        if (executionCourse != null) {
            final Set<User> owners = executionCourse.getProfessorshipsSet().stream()
                    .map(p -> p.getPerson().getUser())
                    .collect(Collectors.toSet());
            add(getOwnersSet(), owners);
            getOwnersSet().stream()
                    .filter(c -> !owners.contains(c.getUser()))
                    .forEach(c -> getOwnersSet().remove(c));
            final Set<User> members = executionCourse.getAttendsSet().stream()
                    .map(a -> a.getRegistration().getPerson().getUser())
                    .collect(Collectors.toSet());
            add(getMembersSet(), members);
            getMembersSet().stream()
                    .filter(c -> !members.contains(c.getUser()))
                    .forEach(c -> getMembersSet().remove(c));
        }
    }

    @Atomic
    public void delete() {
        try {
            if (getAzureId() != null && !getAzureId().isEmpty()) {
                if (getExecutionCourse() == null) {
                    Client.deleteGroup(getAzureId());
                } else {
                    Client.deleteClass(getAzureId());
                }
            }
        } catch (final Throwable t) {
            System.out.println("Unable to delete remote group: " + getAzureId() + " for domain object " + getExternalId());
        }
        getOwnersSet().clear();
        getMembersSet().clear();
        setExecutionCourse(null);
        deleteDomainObject();
    }

}
