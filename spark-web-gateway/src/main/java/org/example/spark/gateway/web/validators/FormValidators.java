/*
 * Spark - The inventory management application
 * Copyright (C) 2026 Yegore Vlussove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.example.spark.gateway.web.validators;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.example.spark.gateway.web.models.*;

public class FormValidators {

	@Nullable
	public static String validateSignInForm(@Nonnull SignInForm signInForm) {
		if (signInForm.getUsername().isEmpty()) {
			return "The username field is empty";
		}
		if (signInForm.getPassword().isEmpty()) {
			return "The password field is empty";
		}
		return null;
	}

	@Nullable
	public static String validateLogInForm(@Nonnull LogInForm logInForm) {
		if (logInForm.getUsername().isEmpty()) {
			return "The username field is empty";
		}
		if (logInForm.getPassword().isEmpty()) {
			return "The password field is empty";
		}
		return null;
	}

	@Nullable
	public static String validateNewOrderForm(@Nonnull NewOrderForm newOrderForm) {
		if (newOrderForm.getTimestamp() == null) {
			return "The order timestamp is not specified";
		}
		if (newOrderForm.getLineItems().length == 0) {
			return "The order has no content";
		}
		for (LineItem lineItem: newOrderForm.getLineItems()) {
			if (lineItem.amount() > 10 || lineItem.amount() < 1) {
				return "The line item amount is outside the accepted range";
			}
		}
		return null;
	}

	@Nullable
	public static String validatePlacedOrderForm(@Nonnull PlacedOrderForm placedOrderForm) {
		if (placedOrderForm.getOrderId() == null) {
			return "The order id is not specified";
		}
		if (placedOrderForm.getVersion() == null) {
			return "The order version is not specified";
		}
		return null;
	}
}
