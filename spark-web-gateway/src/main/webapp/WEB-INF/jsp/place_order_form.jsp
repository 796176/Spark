<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional" />
<jsp:useBean id="items" scope="request" type="org.example.spark.gateway.web.models.Item[]" />
<jsp:useBean id="errorMessage" scope="request" type="java.util.Optional" />
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Placing Order</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/decorated_inventory.css"/>
		<link rel="stylesheet" href="/static/css/place_order_form.css"/>
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
			<% if (items.length > 0) { %>
				<form:form>
					<ol class="inventory_list decorated_inventory">
						<% for (var item: items) { %>
							<li class="item decorated_inventory_item">
								<span class="item_picture_box">
									<% if (item.pictureName() == null) { %>
										<img class="decorated_inventory_picture" src="/static/images/Placeholder_view_vector.png"/>
									<% } else { %>
										<jsp:element name="img" class="decorated_inventory_picture">
											<jsp:attribute name="src">
												<%= "/upload/" + item.pictureName() %>
											</jsp:attribute>
										</jsp:element>
									<% } %>
								</span>
								<span class="item_name">
									<%= item.name() %>
								</span>
								<span class="item_price decorated_inventory_price">
									<strong>
										<%= "$" + item.price().currencyAmount() %>
									</strong>
									<sub>
										<%= "." + item.price().centAmount() %>
									</sub>
								</span>
								<% if (item.amount() > 0) { %>
									<jsp:element name="input">
										<jsp:attribute name="type">number</jsp:attribute>
										<jsp:attribute name="class">item_amount_input</jsp:attribute>
										<jsp:attribute name="name">
											<%= item.itemId() %>
										</jsp:attribute>
										<jsp:attribute name="min">0</jsp:attribute>
										<jsp:attribute name="value">0</jsp:attribute>
										<jsp:attribute name="required"></jsp:attribute>
										<jsp:attribute name="max">10</jsp:attribute>
									</jsp:element>
								<% } else { %>
									<span class="item_availability">Out of Stock</span>
								<% } %>
							</li>
						<% } %>
					</ol>
					<input name="timestamp" value="${System.currentTimeMillis()}" type="hidden"/>
					<% if (errorMessage.isPresent()) { %>
						<p class="decorated_error_message">
							<%= errorMessage.get() %>
						</p>
					<% } %>
					<button class="decorated_button">Place</button>
				</form:form>
			<% } else { %>
				<p>The inventory is empty<\p>
			<% } %>
		</main>

		<footer>
			<p>Copyright © 2026 Yegore Vlussove.</p>
			<p>The source code is available at <a href="https://github.com/796176/Spark">GitHub</a> under GPL 3</p>
			<p>The icons used here are property of Google and available under Apache License 2</p>
		</footer>
	</body>
</html>