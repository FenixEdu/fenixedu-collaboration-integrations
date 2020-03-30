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
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.fenixedu.collaboration.domain.Collaborator;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SpringFunctionality(app = CollaborationController.class, title = "title.collaboration.google")
@RequestMapping("/collaboration/google")
public class GoogleCollaborationController {

    @RequestMapping(method = RequestMethod.GET)
    public String home(final Model model) {
        return CollaborationController.home(model, "collaboration/google/home",
                c -> c.getGoogleId() == null || c.getGoogleId().isEmpty(), Collaborator::getGoogleId);
    }

    @SkipCSRF
    @RequestMapping(value = "/{executionCourse}/createGroup", method = RequestMethod.POST)
    public String createGroupForExecutionCourse(final @PathVariable ExecutionCourse executionCourse) {
        if (CollaborationController.isTeacher(executionCourse)) {
            CollaborationGroup.createGoogleClassroom(executionCourse);
        }
        return "redirect:/collaboration/google";
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/updateMembers", method = RequestMethod.POST)
    public String updateMembers(final @PathVariable CollaborationGroup group) {
        return CollaborationController.updateMembers(group, "redirect:/collaboration/google");
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/delete", method = RequestMethod.POST)
    public String deleteGroup(final @PathVariable CollaborationGroup group) {
        final User user = Authenticate.getUser();
        if (group.getOwnersSet().stream().map(c -> c.getUser()).anyMatch(u -> u == user)) {
            group.deleteGoogleClassroom();
        }
        return "redirect:/collaboration/google";
    }

}