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
import org.fenixedu.academic.domain.Person;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.stream.Collectors;

@SpringApplication(group = "logged", path = "collaboration-integrations", title = "title.collaboration", hint = "Collaboration")
@SpringFunctionality(app = CollaborationController.class, title = "title.collaboration")
@RequestMapping("/collaboration")
public class CollaborationController {

    @RequestMapping(method = RequestMethod.GET)
    public String home(final Model model) {
        final User user = Authenticate.getUser();
        model.addAttribute("user", user);

        final Person person = user.getPerson();
        if (person != null) {
            final Set<ExecutionCourse> courses = person.getProfessorships(ExecutionSemester.readActualExecutionSemester()).stream()
                    .map(p -> p.getExecutionCourse())
                    .filter(ec -> ec.getCollaborationGroup() == null)
                    .collect(Collectors.toSet());
            model.addAttribute("ownedCourses", courses);
        }

        if (allowdToCreateGroups(user)) {
            model.addAttribute("allowdToCreateGroups", Boolean.TRUE);
        }

        return "collaboration/home";
    }

    private boolean allowdToCreateGroups(final User user) {
        return Group.parse("activeResearchers | activeGrantOwner | activeEmployees | activeTeachers").isMember(user);
    }

    @SkipCSRF
    @RequestMapping(value = "/{executionCourse}/createGroup", method = RequestMethod.POST)
    public String createGroupForExecutionCourse(final @PathVariable ExecutionCourse executionCourse) {
        if (isTeacher(executionCourse)) {
            CollaborationGroup.create(executionCourse);
        }
        return "redirect:/collaboration";
    }

    private boolean isTeacher(final ExecutionCourse executionCourse) {
        final User user = Authenticate.getUser();
        return executionCourse.getProfessorshipsSet().stream()
                .anyMatch(p -> p.getPerson().getUser() == user);
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/activateGroup", method = RequestMethod.POST)
    public String activateGroup(final @PathVariable CollaborationGroup group) {
        group.createTeam();
        return "redirect:/collaboration";
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/updateMembers", method = RequestMethod.POST)
    public String updateMembers(final @PathVariable CollaborationGroup group) {
        group.updateMembers();
        return "redirect:/collaboration";
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/delete", method = RequestMethod.POST)
    public String deleteGroup(final @PathVariable CollaborationGroup group) {
        final User user = Authenticate.getUser();
        if (group.getOwnersSet().stream().map(c -> c.getUser()).anyMatch(u -> u == user)) {
            group.delete();
        }
        return "redirect:/collaboration";
    }

    @RequestMapping(value = "/createNewTeam", method = RequestMethod.GET)
    public String prepareCreateNewTeam() {
        return "collaboration/createTeam";
    }

    @SkipCSRF
    @RequestMapping(value = "/createNewTeam", method = RequestMethod.POST)
    public String createNewTeam(final Model model, @RequestParam(required = false) String name,
                                @RequestParam(required = false) String description) {
        final CollaborationGroup group = CollaborationGroup.create(name, description);
        return "redirect:/collaboration";
    }

    /*
    @RequestMapping(value = "/{group}/manageMembers", method = RequestMethod.GET)
    public String manageMembers(final Model model) {
        final User user = Authenticate.getUser();
        if (group.getOwnersSet().stream().map(c -> c.getUser()).anyMatch(u -> u == user)) {
            group.delete();
        }
        return "redirect:/collaboration";
    }
     */

}