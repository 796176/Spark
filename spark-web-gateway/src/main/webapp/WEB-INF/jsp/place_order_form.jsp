<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional" />
<jsp:useBean id="items" scope="request" type="org.example.spark.gateway.web.models.Item[]" />
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Placing Order</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/decorated_inventory.css"/>
		<link rel="stylesheet" href="/static/css/place_order_form.css"/>

		<script type="module" src="/static/scripts/form_submission.js"></script>
		<script type="module" src="/static/scripts/cart_control.js"></script>
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
								<span class="item_name decorated_inventory_name">
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
									<button class="add_to_cart_button">
										<img src="/static/icons/add_shopping_cart_24dp_000000_FILL0_wght400_GRAD0_opsz24.svg"/>
										<span>Add</span>
									</button>
									<div class="cart_control hidden">
										<jsp:element name="select" class="item_amount">
											<jsp:attribute name="name">
												<%= item.itemId() %>
											</jsp:attribute>
											<jsp:body>
												<option value="1">1</option>
												<option value="2">2</option>
												<option value="3">3</option>
												<option value="4">4</option>
												<option value="5">5</option>
												<hr>
												<option value="0" selected>Remove</option>
											</jsp:body>
										</jsp:element>
										<span>In cart</span>
									</div>
								<% } else { %>
									<span class="item_availability">Out of Stock</span>
								<% } %>
							</li>
						<% } %>
					</ol>
					<input name="timestamp" value="${System.currentTimeMillis()}" type="hidden"/>
					<p class="error_field decorated_error"></p>
					<button class="submit_form_button decorated_button">Place</button>
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