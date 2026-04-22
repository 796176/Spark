<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional" />
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Welcome</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
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
			<h1>Welcome Page</h1>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>