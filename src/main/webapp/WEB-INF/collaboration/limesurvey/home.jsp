<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% final String contextPath = request.getContextPath(); %>

<h2>
    <spring:message code="title.collaboration" text="Collaboration Tools"/>
</h2>

<h3>
    <spring:message code="title.collaboration.limesurvey" text="Lime Survey"/>
</h3>
<spring:message var="confirmDelete" code="label.collaboration.survey.confirmDelete" text="Are you sure you want to delete the survey" />
<table class="table tdmiddle">
    <thead>
        <tr>
            <th><spring:message code="title.collaboration.executionCourse" text="Disciplina"/></th>
            <th><spring:message code="title.collaboration.limesurvey.title" text="Test / Inquiry"/></th>
            <th><spring:message code="title.collaboration.limesurvey.start" text="Start"/></th>
            <th><spring:message code="title.collaboration.limesurvey.end" text="End"/></th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="executionCourse" items="${ownedCourses}">
            <c:forEach var="limeSurvey" items="${executionCourse.limeSurvey}">
                <tr>
                    <td>
                        ${executionCourse.name}
                        <br>
                        <span style="color: gray;">${executionCourse.executionPeriod.qualifiedName}</span>
                        <br>
                        <span style="color: gray;">${executionCourse.degreePresentationString}</span>
                    </td>
                    <td>
                        <a href="https://surveys.tecnico.ulisboa.pt/index.php/admin/survey/sa/view/surveyid/${limeSurvey.surveyId}">
                            ${limeSurvey.title}
                        </a>
                    </td>
                    <td>
                        ${limeSurvey.startSurvey}
                    </td>
                    <td>
                        ${limeSurvey.endSurvey}
                    </td>
                    <td>
                        <form class="form-horizontal" method="POST" action="<%= contextPath %>/collaboration/limesurvey/${limeSurvey.externalId}/delete">
                            ${csrf.field()}
                            <button id="submitRequest" class="btn btn-danger" onclick="return confirm('${confirmDelete}');">
                                <spring:message code="label.delete" text="Delete" />
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </c:forEach>
        <c:forEach var="limeSurveyParticipant" items="${user.limeSurveyParticipant}">
            <tr>
                <td>
                    ${limeSurveyParticipant.limeSurvey.executionCourse.name}
                    <br>
                    <span style="color: gray;">${limeSurveyParticipant.limeSurvey.executionCourse.executionPeriod.qualifiedName}</span>
                    <br>
                    <span style="color: gray;">${limeSurveyParticipant.limeSurvey.executionCourse.degreePresentationString}</span>
                </td>
                <td>
                    <a href="https://surveys.tecnico.ulisboa.pt/index.php/${limeSurveyParticipant.limeSurvey.surveyId}?token=${limeSurveyParticipant.participationToken}">
                        ${limeSurveyParticipant.limeSurvey.title}
                    </a>
                </td>
                <td>
                        ${limeSurveyParticipant.limeSurvey.startSurvey}
                </td>
                <td>
                        ${limeSurveyParticipant.limeSurvey.endSurvey}
                </td>
                <td>
                </td>
            </tr>
        </c:forEach>
        <c:forEach var="executionCourse" items="${ownedCourses}">
            <tr>
                <td>
                    ${executionCourse.name}
                    <br>
                    <span style="color: gray;">${executionCourse.executionPeriod.qualifiedName}</span>
                    <br>
                    <span style="color: gray;">${executionCourse.degreePresentationString}</span>
                </td>
                <td>
                </td>
                <td>
                </td>
                <td>
                </td>
                <td>
                    <form class="form-horizontal" method="GET" action="<%= contextPath %>/collaboration/limesurvey/${executionCourse.externalId}/createSurvey">
                        ${csrf.field()}
                        <button id="submitRequest" class="btn btn-primary">
                            <spring:message code="label.create" text="Create" />
                        </button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
