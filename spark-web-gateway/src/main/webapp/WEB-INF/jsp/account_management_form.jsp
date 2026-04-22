<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional"/>
<jsp:useBean id="managedAccount" scope="request" type="org.example.spark.gateway.web.models.Account"/>
<jsp:useBean id="errorMessage" scope="request" type="java.util.Optional"/>
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Welcome</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/navigable.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/account_management_form.css"/>
	</head>
	<body>
		<header>
			<div class="left_elements">
				<a href="/">Spark</a>
				<% if (account.isPresent() && org.example.spark.authorization.BasicAuthorizer.isAdmin(((org.example.spark.gateway.web.models.Account) account.get()).getRoles())) { %>
					<a href="/panel/accounts">Accounts</a>
					<a href="/panel/inventory">Inventory</a>
				<% } else { %>
					<a href="/inventory">Inventory</a>
					<a href="/orders">Orders</a>
				<% } %>
			</div>
			<div class="right_elements">
				<% if (account.isPresent()) { %>
					<a href="/myaccount">${account.get().getName()}</a>
				<% } else { %>
					<a href="/login">Log In</a>
				<% } %>
			</div>
		</header>

		<nav>
			<p>Contents</p>
			<ol>
				<li>
					<a href="#Account_Name">Name</a>
				</li>
				<li>
					<a href="#Account_Roles">Roles</a>
				</li>
				<li>
					<a href="#Account_Status">Status</a>
				</li>
			</ol>
		</nav>

		<main>
			<h1>
				<%= "Account No. " + managedAccount.getId() %>
			</h1>
			<h2 id="Account_Name">Account Name</h2>
			<p>
				<%= managedAccount.getName() %>
			</p>
			<form:form action="/panel/account/${managedAccount.getId()}/save">
				<h2 id="Account_Roles">Account Roles</h2>
				<% for (var role: org.example.spark.authorization.Role.assignableRoles()) { %>
					<label>
						<span>
							<%= role.toString() %>
						</span>
						<% if (org.example.spark.authorization.BasicAuthorizer.hasRole(managedAccount.getRoles(), role)) { %>
							<jsp:element name="input">
								<jsp:attribute name="type">checkbox</jsp:attribute>
								<jsp:attribute name="checked"></jsp:attribute>
								<jsp:attribute name="name">
									<%= "role:" + role.getId() %>
								</jsp:attribute>
							</jsp:element>
						<% } else { %>
							<jsp:element name="input">
								<jsp:attribute name="type">checkbox</jsp:attribute>
								<jsp:attribute name="name">
									<%= "role:" + role.getId() %>
								</jsp:attribute>
							</jsp:element>
						<% } %>
					</label>
				<% } %>
				<jsp:element name="input">
					<jsp:attribute name="type">hidden</jsp:attribute>
					<jsp:attribute name="name">previously_assigned_roles</jsp:attribute>
					<jsp:attribute name="value">
						<%= org.example.spark.gateway.web.converters.RoleEncoder.encode(managedAccount.getRoles()) %>
					</jsp:attribute>
				</jsp:element>
				<h2 id="Account_Status">Account Status</h2>
					<label>
					<span>Select Status</span>
					<select name="account_status">
						<% for (var status: org.example.spark.gateway.web.models.Account.Status.values()) { %>
							<% var optionalAttrs = new java.lang.StringBuilder(); %>
							<% if (status.equals(managedAccount.getStatus())) { %>
								<% optionalAttrs.append(" selected"); %>
							<% } %>
							<% if (!org.example.spark.gateway.web.interactors.AccountManagementFormInteractor.canBeAssignedByAdmin(managedAccount, status)) { %>
								<% optionalAttrs.append(" disabled"); %>
							<% } %>
							<%= "<option value=\"" + status.getId() + "\"" + optionalAttrs.toString() + ">" + status.toString() + "</option>" %>
						<% } %>
					</select>
				</label>
				<jsp:element name="input">
					<jsp:attribute name="type">hidden</jsp:attribute>
					<jsp:attribute name="name">previous_status</jsp:attribute>
					<jsp:attribute name="value">
						<%= managedAccount.getStatus().getId() %>
					</jsp:attribute>
				</jsp:element>
				<p>
					<button class="decorated_button">Save</button>
				</p>
			</form:form>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>
