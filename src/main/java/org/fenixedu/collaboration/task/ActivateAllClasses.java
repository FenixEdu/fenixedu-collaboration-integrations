package org.fenixedu.collaboration.task;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.fenixedu.collaboration.domain.azure.UpdateTeam;
import org.fenixedu.collaboration.domain.google.UpdateClassroom;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

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
        Bennu.getInstance().getUserSet().stream()
                .map(u -> u.getCollaborator())
                .filter(c -> c != null)
                .flatMap(c -> c.getOwnedGroupSet().stream())
                .filter(group -> group.getAzureUrl() == null || group.getAzureUrl().isEmpty())
                .forEach(group -> processTx(group));
        taskLog("Done");
    }

    private void processTx(final CollaborationGroup group) {
        try {
            FenixFramework.atomic(() -> {
                if (group.getAzureUrl() == null || group.getAzureUrl().isEmpty()) {
                    taskLog("Creating team %s (%s)%n", group.getName(), group.getExternalId());
                    group.createAzureTeam();
                    taskLog("   team url: %s%n", group.getAzureUrl());
                }
            });
        } catch (final Throwable t) {
            taskLog("Skipping %s (%s) due to error: %s%n", group.getName(), group.getExternalId(), t.getMessage());
        }
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
            if ((group.getAzureUrl() == null || group.getAzureUrl().isEmpty())
                    && group.getAzureId() != null && !group.getAzureId().isEmpty()) {
                taskLog("Creating team %s (%s)%n", executionCourse.getNome(), executionCourse.getExternalId());
                group.createAzureTeam();
                taskLog("   team url: %s%n", group.getAzureUrl());
            }
            if (isAzure(group) || isGoogle(group)) {
                taskLog("Updateing members for %s%n", executionCourse.getNome());
                updateMembers(group);
            }
        }
    }

    private boolean isAzure(final CollaborationGroup group) {
        return group.getAzureId() != null && !group.getAzureId().isEmpty();
    }

    private boolean isGoogle(CollaborationGroup group) {
        return group.getGoogleId() != null && !group.getGoogleId().isEmpty();
    }

    public void updateMembers(final CollaborationGroup group) {
        group.updateMembers();
        if (isAzure(group)) {
            UpdateTeam.updateMembers(group);
        }
        if (isGoogle(group)) {
            UpdateClassroom.updateMembers(group);
        }
    }

}
