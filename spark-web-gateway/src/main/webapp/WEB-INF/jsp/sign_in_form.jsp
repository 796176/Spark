<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Sign In</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/sign_in_form.css"/>
	</head>
	<body>
		<header>
			<div class="left_elements">
				<a href="/">Spark</a>
				<a href="/inventory">Inventory</a>
				<a href="/orders">Orders</a>
			</div>
			<div class="right_elements">
				<a href="/login">Log In</a>
			</div>
		</header>

		<main>
			<form:form>
				<p>
					<label>
						<span>User name</span>
						<input name="username" required/>
					</label>
				</p>
				<p>
					<label>
						<span>Password</span>
						<input type="password" name="password" required/>
					</label>
				</p>
				<button class="decorated_button">Sign In</button>
			</form:form>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>