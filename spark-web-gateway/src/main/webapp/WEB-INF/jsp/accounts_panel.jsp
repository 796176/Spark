<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional" />
<jsp:useBean id="accounts" scope="request" type="org.example.spark.gateway.web.models.Account[]" />
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Inventory Panel</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/accounts_panel.css"/>
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

		<main>
			<h1>Account Panel</h1>
			<a href="/panel/newaccount" class="decorated_link">New Account</a>
			<% if (accounts.length == 0) { %>
				<p>Empty</p>
			<% } else { %>
				<ol class="decorated_list">
					<% for (var acc: accounts) { %>
						<li class="account decorated_li">
						<jsp:element name="a" class="decorated_embedded_link account_info">
							<jsp:attribute name="href">
								<%= "/panel/account/" + acc.getId() %>
							</jsp:attribute>
							<jsp:body>
								<span class="account_id decorated_sli">
									<%= acc.getId() %>
								</span
								<span class="account_name decorated_sli">
									<%= acc.getName() %>
								</span>
								<span class="account_roles decorated_sli">
									<%= String.join(", ", java.util.Arrays.stream(acc.getRoles()).map(java.util.Objects::toString).toList()) %>
								</span>
								<span class="account_status decorated_sli">
									<%= acc.getStatus().toString() %>
								</span>
							</jsp:body>
						</jsp:element>
						<jsp:element name="a" class="decorated_embedded_link account_orders">
							<jsp:attribute name="href">
								<%= "/panel/account/" + acc.getId() + "/orders" %>
							</jsp:attribute>
							<jsp:body>Orders</jsp:body>
						</jsp:element>
						</li>
					<% } %>
				</ol>
			<% } %>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>
