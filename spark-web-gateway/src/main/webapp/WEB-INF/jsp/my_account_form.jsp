<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="org.example.spark.gateway.web.models.Account" />
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>My Account</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/my_account.css"/>
	</head>
	<body>
		<header>
			<div class="left_elements">
				<a href="/">Spark</a>
				<a href="/inventory">Inventory</a>
				<a href="/orders">Orders</a>
			</div>
			<div class="right_elements">
				<a href="/myaccount">${account.getName()}</a>
			</div>
		</header>

		<main>
			<div class="personal_info">
				<img class="profile_pic" src="/static/icons/account_circle_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg"/>
				<p class="profile_name">${account.getName()}</p>
			</div>
			<div class="account_action">
				<form:form action="/logout">
					<button class="decorated_button">Log Out</button>
				</form:form>
				<form:form action="/myaccount" method="DELETE">
					<button class="decorated_button">Delete Account</button>
				</form:form>
			</div>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>