<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional" />
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Welcome</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
	</head>
	<body>
		<header>
			<p>Spark</p>
			<p><a href="/inventory">Inventory</a></p>
			<p><a href="/orders">Orders</a></p>
			<% if (account.isPresent()) { %>
				<p><a href="/myaccount">${(account.get()).getName()}</a></p>
			<% } else { %>
				<p><a href="/login">Log In</a></p>
			<% } %>
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