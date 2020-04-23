<%@ page import="org.fenixedu.bennu.core.groups.Group" %>
<%@ page import="org.fenixedu.bennu.core.security.Authenticate" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% final String contextPath = request.getContextPath(); %>

<h2>
    <spring:message code="title.collaboration" text="Collaboration Tools"/>
</h2>

<h3>
    <spring:message code="title.collaboration.google" text="Google Classroom"/>
</h3>
<div class="warning alert-warning" style="margin: 10px; padding: 20px;">
    <p class="warning"><spring:message code="label.collaboration.google.timing" text="Timing Message" /></p>
</div>
<spring:message var="confirmDelete" code="label.collaboration.group.confirmDelete" text="Are you sure you want to delete the group" />
<table class="table tdmiddle">
    <thead>
        <tr>
            <th><spring:message code="title.collaboration.group" text="Group"/></th>
            <th><spring:message code="title.collaboration.group.url" text="Group Link"/></th>
            <th><spring:message code="title.collaboration.group.state" text="Estado"/></th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="group" items="${user.collaborator.ownedGroup}">
            <c:if test="${not empty group.googleId}">
            <tr>
                <td>
                    <c:if test="${empty group.executionCourse}">
                        ${group.name}
                    </c:if>
                    <c:if test="${not empty group.executionCourse}">
                        ${group.executionCourse.name}
                        <br>
                        <span style="color: gray;">${group.executionCourse.executionPeriod.qualifiedName}</span>
                        <br>
                        <span style="color: gray;">${group.executionCourse.degreePresentationString}</span>
                    </c:if>
                </td>
                <td>
                    <c:if test="${not empty group.googleUrl}">
                        <a href="${group.googleUrl}">
                            ${group.name}
                        </a>
                    </c:if>
                </td>
                <td>
                    <c:if test="${empty group.googleUrl}">
                        <spring:message code="title.collaboration.group.state.pendingActivation" text="Pending Activation"/>
                    </c:if>
                    <c:if test="${not empty group.googleUrl}">
                        <spring:message code="title.collaboration.group.state.active" text="Activo"/>
                        <br/>
                        <c:if test="${empty group.executionCourse}">
                            <span style="color: gray;"><spring:message code="title.collaboration.group.members.managed.remotely" text="Members Managed Remotely"/></span>
                        </c:if>
                        <c:if test="${not empty group.executionCourse}">
                            <span style="color: gray;"><spring:message code="title.collaboration.group.owners.count" text="Owners"/>: ${group.googleOwnerCount} / ${fn:length(group.owners)}</span>
                            <br/>
                            <span style="color: gray;"><spring:message code="title.collaboration.group.members.count" text="Members"/>: ${group.googleMemberCount} / ${fn:length(group.members)}</span>
                        </c:if>
                    </c:if>
                </td>
                <td>
                    <c:if test="${not empty group.executionCourse}">
                        <c:if test="${not empty group.googleUrl}">
                            <form class="form-horizontal" method="POST" action="<%= contextPath %>/collaboration/google/${group.externalId}/updateMembers">
                                    ${csrf.field()}
                                <button id="submitRequest" class="btn btn-default">
                                    <spring:message code="label.updateMembers" text="Update Members" />
                                </button>
                            </form>
                            <br/>
                        </c:if>
                    </c:if>
                    <c:if test="${not empty group.googleUrl}">
                        <form class="form-horizontal" method="POST" action="<%= contextPath %>/collaboration/google/${group.externalId}/delete">
                            ${csrf.field()}
                            <button id="submitRequest" class="btn btn-danger" onclick="return confirm('${confirmDelete}');">
                                <spring:message code="label.delete" text="Delete" />
                            </button>
                        </form>
                    </c:if>
                </td>
            </tr>
            </c:if>
        </c:forEach>
        <c:forEach var="group" items="${user.collaborator.memberGroup}">
            <tr>
                <td>
                    <c:if test="${empty group.executionCourse}">
                        ${group.name}
                    </c:if>
                    <c:if test="${not empty group.executionCourse}">
                        ${group.executionCourse.name}
                        <br>
                        <span style="color: gray;">${group.executionCourse.executionPeriod.qualifiedName}</span>
                        <br>
                        <span style="color: gray;">${group.executionCourse.degreePresentationString}</span>
                    </c:if>
                </td>
                <td>
                    <c:if test="${not empty group.googleUrl}">
                        <a href="${group.googleUrl}">
                                ${group.name}
                        </a>
                    </c:if>
                </td>
                <td>
                    <c:if test="${empty group.googleUrl}">
                        <spring:message code="title.collaboration.group.state.pendingActivation" text="Pending Activation"/>
                    </c:if>
                    <c:if test="${not empty group.googleUrl}">
                        <spring:message code="title.collaboration.group.state.active" text="Activo"/>
                        <br/>
                        <span style="color: gray;"><spring:message code="title.collaboration.group.owners.count" text="Owners"/>: ${fn:length(group.owners)}</span>
                        <br/>
                        <span style="color: gray;"><spring:message code="title.collaboration.group.members.count" text="Members"/>: ${fn:length(group.members)}</span>
                    </c:if>
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
                    <spring:message code="title.collaboration.group.not.created" text="Not Created"/>
                </td>
                <td>
                    <form class="form-horizontal" method="POST" action="<%= contextPath %>/collaboration/google/${executionCourse.externalId}/createGroup">
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

<% if (Group.managers().isMember(Authenticate.getUser())) { %>
<h3>
    <spring:message code="title.collaboration.google.debug" text="Debug Google Classroom"/>
</h3>
<br/>
<form class="form-horizontal" action="" method="POST">
    ${csrf.field()}
    <div class="form-group">
        <div class="col-sm-1">
        </div>
        <div class="col-sm-8">
            <input type="text" name="username" required="required" placeholder="Username"
                   class="form-control ui-autocomplete-input"/>
        </div>
        <div class="col-sm-3">
            <button id="submitRequest" class="btn btn-primary">
                Selecionar
            </button>
        </div>
    </div>
    <div class="form-group">
    </div>
</form>
<% } %>
