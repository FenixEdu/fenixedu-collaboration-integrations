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
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.collaboration.domain.LimeSurvey;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.stream.Collectors;

@SpringFunctionality(app = CollaborationController.class, title = "title.collaboration.limesurvey")
@RequestMapping("/collaboration/limesurvey")
public class LimeSurveyController {

    @RequestMapping(method = RequestMethod.GET)
    public String home(final Model model) {
        final User user = Authenticate.getUser();
        model.addAttribute("user", user);

        final Person person = user.getPerson();
        if (person != null) {
            final Set<ExecutionCourse> courses = person.getProfessorships(ExecutionSemester.readActualExecutionSemester()).stream()
                .map(p -> p.getExecutionCourse())
                .collect(Collectors.toSet());
            model.addAttribute("ownedCourses", courses);
        }

        return "collaboration/limesurvey/home";
    }

    @SkipCSRF
    @RequestMapping(value = "/{executionCourse}/createSurvey", method = RequestMethod.GET)
    public String prepareCreateSurvey(final Model model, final @PathVariable ExecutionCourse executionCourse) {
        model.addAttribute("executionCourse", executionCourse);
        return "collaboration/limesurvey/createSurvey";
    }

    @SkipCSRF
    @RequestMapping(value = "/{executionCourse}/createSurvey", method = RequestMethod.POST)
    public String createSurvey(final @PathVariable ExecutionCourse executionCourse,
                               final @RequestParam(required = true) String title,
                               final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = true) DateTime start,
                               final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = true) DateTime end) {
        if (CollaborationController.isTeacher(executionCourse)) {
            LimeSurvey.createSurvey(executionCourse, title, start, end);
        }
        return "redirect:/collaboration/limesurvey";
    }

    @SkipCSRF
    @RequestMapping(value = "/{limesurvey}/delete", method = RequestMethod.POST)
    public String deleteSurvey(final @PathVariable LimeSurvey limesurvey) {
        final User user = Authenticate.getUser();
        if (CollaborationController.isTeacher(limesurvey.getExecutionCourse())) {
            limesurvey.delete();
        }
        return "redirect:/collaboration/limesurvey";
    }

}