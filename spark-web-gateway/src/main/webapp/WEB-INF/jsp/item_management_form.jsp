<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:useBean id="account" scope="request" type="java.util.Optional"/>
<jsp:useBean id="managedItem" scope="request" type="org.example.spark.gateway.web.models.Item"/>
<jsp:useBean id="errorMessage" scope="request" type="java.util.Optional"/>
<html lang="en-US">
	<head>
		<meta charset="utf-8"/>
		<title>Welcome</title>
		<link rel="icon" href="/static/icons/local_mall_24dp_E3E3E3_FILL0_wght400_GRAD0_opsz24.svg" type="image/x-icon"/>
		<link rel="stylesheet" href="/static/css/general.css"/>
		<link rel="stylesheet" href="/static/css/navigable.css"/>
		<link rel="stylesheet" href="/static/css/decorated_elements.css"/>
		<link rel="stylesheet" href="/static/css/item_management_form.css"/>
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
					<a href="#Item_Name">Name</a>
				</li>
				<li>
					<a href="#Item_Price">Price</a>
				</li>
				<li>
					<a href="#Item_Amount">Amount</a>
				</li>
				<li>
					<a href="#Item_Actions">Actions</a>
				</li>

			</ol>
		</nav>

		<main>
			<h1>
				<%= "Item No. " + managedItem.itemId() %>
			</h1>
			<h2 id="Item_Name">Item Name</h2>
			<p>
				<%= managedItem.name() %>
			</p>
			<h2 id="Item_Price">Item Price</h2>
			<p>
				<%= "$" + managedItem.price().currencyAmount() + "." + managedItem.price().centAmount() %>
			</p>
			<form:form action="/panel/item/${managedItem.itemId()}/save">
				<h2 id="Item_Amount">Item Amount</h2>
				<label>
					<span>Current Amount</span>
					<jsp:element name="input">
					<jsp:attribute name="type">number</jsp:attribute>
						<jsp:attribute name="name">item_amount</jsp:attribute>
						<jsp:attribute name="required"></jsp:attribute>
						<jsp:attribute name="value">
							<%= managedItem.amount() %>
						</jsp:attribute>
					</jsp:element>
				</label>
				<jsp:element name="input">
					<jsp:attribute name="type">hidden</jsp:attribute>
					<jsp:attribute name="name">previous_item_amount</jsp:attribute>
					<jsp:attribute name="value">
						<%= managedItem.amount() %>
					</jsp:attribute>
				</jsp:element>
				<jsp:element name="input">
					<jsp:attribute name="type">hidden</jsp:attribute>
					<jsp:attribute name="name">item_version</jsp:attribute>
					<jsp:attribute name="value">
						<%= managedItem.version() %>
					</jsp:attribute>
				</jsp:element>
				<p>
					<button class="decorated_button">Save</button>
				</p>
			</form:form>
			<h2 id="Item_Actions">Item Actions</h2>
			<form:form action="/panel/item/${managedItem.itemId()}/delete" method="DELETE">
				<p>
					<button class="decorated_button">Delete Item</button>
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
