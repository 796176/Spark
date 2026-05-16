<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional"/>
<jsp:useBean id="accountId" scope="request" type="java.lang.Long"/>
<jsp:useBean id="managedOrder" scope="request" type="org.example.spark.gateway.web.models.DetailedOrder"/>
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>
			<%= "Order No. " + managedOrder.orderId() %>
		</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/navigable.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/order_management_form.css"/>

		<script type="module" src="/static/scripts/form_submission.js"></script>
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
			<ol>
				<li>
					<a href="#Order_Timestamp">Timestamp</a>
				</li>
				<li>
					<a href="#Order_Content">Content</a>
				</li>
				<li>
					<a href="#Order_Status">Status</a>
				</li>
			</ol>
		</nav>

		<main>
			<h1>
				<%= "Order No. " + managedOrder.orderId() %>
			</h1>
			<h2 id="Order_Timestamp">Order Timestamp</h2>
			<p>
				<%= java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(java.util.Date.from(java.time.Instant.ofEpochMilli(managedOrder.timestamp()))) %>
			</p>
			<h2 id="Order_Content">Order Content</h2>
			<ol class="decorated_list">
				<% for (var item: managedOrder.items()) { %>
					<li class="item decorated_li">
						<span class="item_name decorated_sli">
							<%= item.name() %>
						</span>
						<span class="item_amount decorated_sli">
							<%= "x" + item.amount() %>
						</span>
						<span class="item_accumulative_price decorated_sli">
							<%= "$" + item.price().currencyAmount() + "." + item.price().centAmount() %>
						</span>
					</li>
				<% } %>
			</ol>

			<form:form action="/panel/account/${accountId}/order/${managedOrder.orderId()}/save">
				<h2 id="Order_Status">Order Status</h2>
				<label>
					<span>Current Status</span>
					<select name="order_status">
						<% for (var status: org.example.spark.gateway.web.models.Order.Status.values()) { %>
							<% var optionalAttrs = new java.lang.StringBuilder(); %>
							<% if (status.equals(managedOrder.status())) { %>
								<% optionalAttrs.append(" selected"); %>
							<% } %>
							<% if (!org.example.spark.gateway.web.interactors.OrderManagementFormInteractor.canBeAssignedByAdmin(managedOrder, status)) { %>
								<% optionalAttrs.append(" disabled"); %>
							<% } %>
							<%= "<option value=\"" + status.getId() + "\"" + optionalAttrs.toString() + ">" + status.toString() + "</option>" %>
						<% } %>
					</select>
				</label>
				<jsp:element name="input">
                	<jsp:attribute name="type">hidden</jsp:attribute>
                	<jsp:attribute name="name">previous_order_status</jsp:attribute>
                	<jsp:attribute name="value">
                		<%= managedOrder.status().getId() %>
                	</jsp:attribute>
				</jsp:element>
				<jsp:element name="input">
                	<jsp:attribute name="type">hidden</jsp:attribute>
                	<jsp:attribute name="name">order_version</jsp:attribute>
                	<jsp:attribute name="value">
                		<%= managedOrder.version() %>
                	</jsp:attribute>
				</jsp:element>
				<p class="error_field decorated_error"></p>
				<p>
					<button class="submit_form_button decorated_button">Save</button>
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
