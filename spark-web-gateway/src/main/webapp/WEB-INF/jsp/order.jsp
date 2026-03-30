<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional" />
<jsp:useBean id="detailedOrder" scope="request" type="org.example.spark.gateway.web.models.DetailedOrder" />
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
			<h1>
				<%= "Order No. " + detailedOrder.orderId() + " Details" %>
			</h1>

			<h2>Order Content</h2>
			<ol>
				<% for (var item: detailedOrder.items()) { %>
					<li>
						<div>
							<span>
								<%= item.name() %>
							</span>
							<span>
								<%= "x" + item.amount() %>
							</span>
							<span>
								<%= "$" + item.price().currencyAmount() + "." + item.price().centAmount() %>
							</span>
						</div>
					</li>
				<% } %>
			</ol>

			<h2>Total Price</h2>
			<% var totalPrice = new org.example.spark.gateway.web.models.Money(0, 0); %>
			<% for (var item: detailedOrder.items()) { %>
				<% totalPrice = totalPrice.plus(item.price()); %>
			<% } %>
			<%= "$" + totalPrice.currencyAmount() + "." + totalPrice.centAmount() %>

			<h2>Order Status</h2>
			${detailedOrder.status()}

			<% if (detailedOrder.status().equals(org.example.spark.gateway.web.models.Order.Status.PENDING_ACCEPTANCE)) { %>
				<form:form action="/cancelorder">
					<input name="order_id" value="${detailedOrder.orderId()}" type="hidden"/>
					<input name="version" value="${detailedOrder.version()}" type="hidden"/>
					<button>Cancel Order</button>
				</form:form>
			<% } else if (detailedOrder.status().equals(org.example.spark.gateway.web.models.Order.Status.CANCELED)) { %>
				<form:form action="/restoreorder">
					<input name="order_id" value="${detailedOrder.orderId()}" type="hidden"/>
					<input name="version" value="${detailedOrder.version()}" type="hidden"/>
					<button>Restore Order</button>
				</form:form>
			<% } %>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>