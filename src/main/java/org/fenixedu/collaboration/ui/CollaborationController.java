/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST GIAF Invoices.
 *
 * FenixEdu IST GIAF Invoices is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST GIAF Invoices is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST GIAF Invoices.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.collaboration.ui;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.fenixedu.collaboration.domain.Collaborator;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SpringApplication(group = "logged", path = "collaboration-integrations", title = "title.collaboration", hint = "Collaboration")
@RequestMapping("/collaboration")
public class CollaborationController {

    static String home(final Model model, final String returnPath,
                       final Predicate<CollaborationGroup> allowCreatePredicate,
                       final Function<Collaborator, String> toUserId) {
        final User user = Authenticate.getUser();
        model.addAttribute("user", user);

        final Collaborator collaborator = user.getCollaborator();
        final String userId = collaborator == null ? null : toUserId.apply(collaborator);
        if (userId != null && !userId.isEmpty() && allowdToCreateGroups(user)) {
            model.addAttribute("allowdToCreateGroups", Boolean.TRUE);

            final Person person = user.getPerson();
            if (person != null) {
                final Set<ExecutionCourse> courses = person.getProfessorships(ExecutionYear.readCurrentExecutionYear()).stream()
                        .map(p -> p.getExecutionCourse())
                        .filter(ec -> ec.getCollaborationGroup() == null || allowCreatePredicate.test(ec.getCollaborationGroup()))
                        .collect(Collectors.toSet());
                model.addAttribute("ownedCourses", courses);
            }
        }

        return returnPath;
    }

    static boolean allowdToCreateGroups(final User user) {
        return Group.parse("activeResearchers | activeGrantOwner | activeEmployees | activeTeachers").isMember(user);
    }

    static boolean isTeacher(final ExecutionCourse executionCourse) {
        final User user = Authenticate.getUser();
        return executionCourse.getProfessorshipsSet().stream()
                .anyMatch(p -> p.getPerson().getUser() == user);
    }

    static String updateMembers(final CollaborationGroup group, final String returnPath) {
        group.updateMembers();
        return returnPath;
    }

    static String deleteGroup(final CollaborationGroup group, final String returnPath) {
        final User user = Authenticate.getUser();
        if (group.getOwnersSet().stream().map(c -> c.getUser()).anyMatch(u -> u == user)) {
            group.delete();
        }
        return returnPath;
    }

}