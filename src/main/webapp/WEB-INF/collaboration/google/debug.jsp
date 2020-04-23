<%@ page import="org.fenixedu.bennu.core.groups.Group" %>
<%@ page import="org.fenixedu.bennu.core.security.Authenticate" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% final String contextPath = request.getContextPath(); %>

<link rel="stylesheet" type="text/css" media="screen" href="<%= request.getContextPath() %>/CSS/accounting.css"/>
<style>
    <!--
    .info-div {
        border: 1px solid lightgray;
        padding: 2rem;
    }

    .info-div dl {
        display: -webkit-box;
        display: -ms-flexbox;
        display: flex;
        -webkit-box-orient: horizontal;
        -webkit-box-direction: normal;
        -ms-flex-flow: row nowrap;
        flex-flow: row nowrap;
        -webkit-box-pack: justify;
        -ms-flex-pack: justify;
        justify-content: space-between;
    }

    .info-div dl dd {
        text-align: right;
    }

    .info-div dl dt {
        text-align: left;
    }

    .part1 {
        margin-right: 20px;
    }

    .img-p-circle {
        border-radius: 50%;
        width: 75px;
    }

    .docType {
        color: gray;
    }

    .json-viewer {
        color: #000;
        padding-left: 20px;
    }

    .json-viewer ul {
        list-style-type: none;
        margin: 0;
        margin: 0 0 0 1px;
        border-left: 1px dotted #ccc;
        padding-left: 2em;
    }

    .json-viewer .hide {
        display: none;
    }

    .json-viewer ul li .type-string,
    .json-viewer ul li .type-date {
        color: #0B7500;
    }

    .json-viewer ul li .type-boolean {
        color: #1A01CC;
        font-weight: bold;
    }

    .json-viewer ul li .type-number {
        color: #1A01CC;
    }

    .json-viewer ul li .type-null {
        color: red;
    }

    .json-viewer a.list-link {
        color: #000;
        text-decoration: none;
        position: relative;
    }

    .json-viewer a.list-link:before {
        color: #aaa;
        content: "\25BC";
        position: absolute;
        display: inline-block;
        width: 1em;
        left: -1em;
    }

    .json-viewetent: "\25B6";
    }

    .json-viewer a.list-link.empty:before {
    contr a.list-link.collapsed:before {
        conent: "";
    }

    .json-viewer .items-ph {
        color: #aaa;
        padding: 0 1em;
    }

    .json-viewer .items-ph:hover {
        text-decoration: underline;
    }
    -->
</style>

<h2>
    <spring:message code="title.collaboration" text="Collaboration Tools"/>
</h2>

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
                   class="form-control ui-autocomplete-input"
                    <c:if test="${not empty user}">
                        value="${user.username}"
                    </c:if>
            />
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

<c:if test="${not empty user}">
    <div class="info-div">
        <div class="row">
            <div class="col-md-2">
                <img class="img-p-circle" alt="" src="/fenix/user/photo/${user.username}">
            </div>
            <div class="col-md-4">
                <dl>
                    <dt>User</dt>
                    <dd>
                        ${user.displayName}
                        <span class="docType">${user.username}</span>
                    </dd>
                </dl>
                <dl>
                    <dt>Google ID</dt>
                    <dd>
                        <c:if test="${not empty user.collaborator}">
                            <c:if test="${not empty user.collaborator.googleId}">
                                ${user.collaborator.googleId}
                            </c:if>
                        </c:if>
                    </dd>
                </dl>
                <dl>
                </dl>
            </div>
            <div class="col-md-1">
            </div>
            <div class="col-md-3">
            </div>
        </div>
    </div>

    <br/>
    <h4>
        Owned Classrooms From Fenix
    </h4>
    <table class="table tdmiddle">
        <thead>
        <tr>
            <th><spring:message code="title.collaboration.group.id" text="Group ID"/></th>
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
                        <c:if test="${not empty group.googleUrl}">
                            ${group.googleId}
                        </c:if>
                    </td>
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
        </tbody>
    </table>

    <h4>
        Member Classrooms From Fenix
    </h4>
    <table class="table tdmiddle">
        <thead>
        <tr>
            <th><spring:message code="title.collaboration.group.id" text="Group ID"/></th>
            <th><spring:message code="title.collaboration.group" text="Group"/></th>
            <th><spring:message code="title.collaboration.group.url" text="Group Link"/></th>
            <th><spring:message code="title.collaboration.group.state" text="Estado"/></th>
            <th></th>
        </tr>
        </thead>
        <tbody>
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
        </tbody>
    </table>
</c:if>

<br/>
<h4>
    Remote Information
</h4>
<c:if test="${not empty courses}">
    <pre class="json-viewer">${courses}</pre>
</c:if>

<% } %>
