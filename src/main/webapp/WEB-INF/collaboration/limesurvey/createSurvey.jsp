<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% final String contextPath = request.getContextPath(); %>

${portal.toolkit()}

<h2>
    <spring:message code="title.collaboration" text="Collaboration Tools"/>
</h2>

<h3>
    <spring:message code="title.collaboration.limesurvey" text="Lime Survey"/>
</h3>

<h4>
    <spring:message code="title.collaboration.limesurvey.createSurvey" text="Create Survey"/>
</h4>

<form class="form-horizontal" method="POST">
    ${csrf.field()}

    <div class="form-group">
        <label class="control-label col-sm-2" for="type">
            <spring:message code="title.collaboration.executionCourse" text="Disciplina"/>
        </label>
        <div class="col-sm-10">
            <input name="coursePlaceholder" type="text" class="form-control" disabled value="${executionCourse.name}">
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-sm-2" for="type">
            <spring:message code="title.collaboration.limesurvey.title" text="Test / Inquiry" />
        </label>
        <div class="col-sm-10">
            <input name="title" type="text" required class="form-control">
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-sm-2" for="type">
            <spring:message code="title.collaboration.limesurvey.start" text="Início" />
        </label>
        <div class="col-sm-10">
            <input name="start" type="text" required class="form-control" bennu-datetime>
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-sm-2" for="type">
            <spring:message code="title.collaboration.limesurvey.end" text="End" />
        </label>
        <div class="col-sm-10">
            <input name="end" type="text" required class="form-control" bennu-datetime>
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-10 col-sm-offset-2">
            <button id="submitRequest" class="btn btn-primary">
                <spring:message code="label.create" text="Create" />
            </button>
        </div>
    </div>
</form>
