<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional" />
<jsp:useBean id="orders" scope="request" type="org.example.spark.gateway.web.models.Order[]" />
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Placed Orders</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/orders.css"/>
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
			<a class="decorated_link" href="/placeorder">Place Order</a>
			<p>Placed Orders</p>
			<ol class="decorated_list">
				<% for (var order: orders) { %>
					<li class="order decorated_li">
						<jsp:element name="a">
							<jsp:attribute name="href">
								<%= "/order/" + order.orderId() %>
							</jsp:attribute>
							<jsp:body>
								<span class="order_number decorated_sli">
									<%= "Order No. " + order.orderId() %>
								</span>
								<span class="order_date decorated_sli">
									<%= java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(java.util.Date.from(java.time.Instant.ofEpochMilli(order.timestamp()))) %>
								</span>
								<span class="order_status decorated_sli">
									<%= order.status() %>
								</span>
							</jsp:body>
						</jsp:element>
					</li>
				<% } %>
			</ol>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>