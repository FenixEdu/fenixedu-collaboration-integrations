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
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.collaboration.domain.CollaborationGroup;
import org.fenixedu.collaboration.domain.Collaborator;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@SpringFunctionality(app = CollaborationController.class, title = "title.collaboration.azure")
@RequestMapping("/azure")
public class AzureCollaborationController {

    @RequestMapping(method = RequestMethod.GET)
    public String home(final Model model) {
        return CollaborationController.home(model, "collaboration/azure/home",
                c -> c.getAzureId() == null || c.getAzureId().isEmpty(), Collaborator::getAzureId);
    }

    @SkipCSRF
    @RequestMapping(value = "/{executionCourse}/createGroup", method = RequestMethod.POST)
    public String createGroupForExecutionCourse(final @PathVariable ExecutionCourse executionCourse) {
        if (CollaborationController.isTeacher(executionCourse)) {
            CollaborationGroup.createAzureGroup(executionCourse);
        }
        return "redirect:/collaboration/azure";
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/activateGroup", method = RequestMethod.POST)
    public String activateGroup(final @PathVariable CollaborationGroup group) {
        group.createAzureTeam();
        return "redirect:/collaboration/azure";
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/updateMembers", method = RequestMethod.POST)
    public String updateMembers(final @PathVariable CollaborationGroup group) {
        return CollaborationController.updateMembers(group, "redirect:/collaboration/azure");
    }

    @SkipCSRF
    @RequestMapping(value = "/{group}/delete", method = RequestMethod.POST)
    public String deleteGroup(final @PathVariable CollaborationGroup group) {
        return CollaborationController.deleteGroup(group, "redirect:/collaboration/azure");
    }

    @RequestMapping(value = "/createNewTeam", method = RequestMethod.GET)
    public String prepareCreateNewTeam() {
        return "collaboration/azure/createTeam";
    }

    @SkipCSRF
    @RequestMapping(value = "/createNewTeam", method = RequestMethod.POST)
    public String createNewTeam(final Model model, @RequestParam(required = false) String name,
                                @RequestParam(required = false) String description) {
        final CollaborationGroup group = CollaborationGroup.createAzureGroup(name, description);
        return "redirect:/collaboration/azure";
    }

}