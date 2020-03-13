<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% final String contextPath = request.getContextPath(); %>

<h2>
    <spring:message code="title.collaboration" text="Collaboration Tools"/>
</h2>

<h3>
    <spring:message code="title.collaboration.azure" text="Microsoft Teams"/>
</h3>

<h4>
    <spring:message code="title.collaboration.azure.createTeam" text="Create Team"/>
</h4>

<form class="form-horizontal" method="POST" action="<%= contextPath %>/collaboration/createNewTeam">
    ${csrf.field()}

    <div class="form-group">
        <label class="control-label col-sm-2" for="type">
            <spring:message code="title.collaboration.group.name" text="Group Name" />
        </label>
        <div class="col-sm-10">
            <input name="name" type="text" placeholder="Group Name" required class="form-control">
        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-sm-2" for="type">
            <spring:message code="title.collaboration.group.description" text="Group Description" />
        </label>
        <div class="col-sm-10">
            <input name="description" type="text" placeholder="Group Description" required class="form-control">
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
