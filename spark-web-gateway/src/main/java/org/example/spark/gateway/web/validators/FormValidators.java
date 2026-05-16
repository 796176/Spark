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
import org.example.spark.authorization.Role;
import org.example.spark.gateway.web.models.*;

import java.util.Arrays;
import java.util.Comparator;

public class FormValidators {

	@Nullable
	public static String validateSignInForm(@Nonnull SignInForm signInForm) {
		if (signInForm.getUsername() == null || signInForm.getUsername().isEmpty()) {
			return "The username field is empty";
		}
		if (signInForm.getPassword() == null || signInForm.getPassword().isEmpty()) {
			return "The password field is empty";
		}
		return null;
	}

	@Nullable
	public static String validateLogInForm(@Nonnull LogInForm logInForm) {
		if (logInForm.getUsername() == null || logInForm.getUsername().isEmpty()) {
			return "The username field is empty";
		}
		if (logInForm.getPassword() == null || logInForm.getPassword().isEmpty()) {
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

	private static boolean anyNull(Object... objects) {
		for (Object o: objects) {
			if (o == null) return true;
		}
		return false;
	}

	private static boolean rolesAreUnique(Role[] roles) {
		Comparator<Role> roleComparator = (r1, r2) -> (int) (r1.getId() - r2.getId());
		Arrays.sort(roles, roleComparator);
		for (int i = 1; i < roles.length; i++) {
			if (roles[i - 1].equals(roles[i])) {
				return false;
			}
		}
		return true;
	}

	@Nullable
	public static String validateAccountManagementForm(@Nonnull AccountManagementForm accountManagementForm) {
		Role[] previouslyAssignedRoles = accountManagementForm.getPreviouslyAssignedRoles();
		if (previouslyAssignedRoles == null) {
			return "The previously assigned roles are not specified";
		}
		if (anyNull((Object[]) previouslyAssignedRoles) || !rolesAreUnique(previouslyAssignedRoles)) {
			return "The elements of previously assigned roles are outside the accepted range";
		}

		Role[] currentlyAssignedRoles = accountManagementForm.getCurrentlyAssignedRoles();
		if (currentlyAssignedRoles == null || currentlyAssignedRoles.length == 0) {
			return "The currently assigned roles are not specified";
		}
		if (anyNull((Object[]) currentlyAssignedRoles) || !rolesAreUnique(currentlyAssignedRoles)) {
			return "The elements of currently assigned roles are outside the accepted range";
		}

		if (accountManagementForm.getPreviousStatus() == null) {
			return "The previous status is not specified";
		}
		if (accountManagementForm.getCurrentStatus() == null) {
			return "The current status is not specified";
		}
		if (
			accountManagementForm.getCurrentStatus() == Account.Status.DELETED &&
			accountManagementForm.getPreviousStatus() != Account.Status.DELETED
		) {
			return "The value of current status is not acceptable";
		}

		return null;
	}

	@Nullable
	public static String validateItemManagementForm(@Nonnull ItemManagementForm itemManagementForm) {
		if (itemManagementForm.getPreviousItemAmount() == null) {
			return "The previous item amount is not specified";
		}
		if (itemManagementForm.getCurrentItemAmount() == null) {
			return "The current item amount is not specified";
		}
		if (itemManagementForm.getVersion() == null) {
			return "The version is not specified";
		}

		return null;
	}

	@Nullable
	public static String validateOrderManagementForm(@Nonnull OrderManagementForm orderManagementForm) {
		Order.Status previousStatus = orderManagementForm.getPreviousStatus();
		if (previousStatus == null) {
			return "The previous status is not specified";
		}
		Order.Status currentStatus = orderManagementForm.getCurrentStatus();
		if (currentStatus == null) {
			return "The current status is not specified";
		}
		boolean triesToSetCurrentStatusToAcceptedOrRejected =
			previousStatus != currentStatus &&
			(currentStatus == Order.Status.ACCEPTED || currentStatus == Order.Status.REJECTED);
		if (triesToSetCurrentStatusToAcceptedOrRejected && previousStatus != Order.Status.PENDING_ACCEPTANCE) {
			return "The value of current status is outside the accepted range";
		}

		if (orderManagementForm.getVersion() == null) {
			return "The version is not specified";
		}

		return null;
	}

	@Nullable
	public static String validateCreatingAccountForm(@Nonnull CreatingAccountForm creatingAccountForm) {
		if (creatingAccountForm.getUsername() == null) {
			return "The username is not specified";
		}
		if (creatingAccountForm.getUsername().isBlank()) {
			return "The username is blank";
		}

		if (creatingAccountForm.getPassword() == null) {
			return "The password is not specified";
		}
		if (creatingAccountForm.getPassword().isBlank()) {
			return "The password is blank";
		}

		return null;
	}

	@Nullable
	public static String validateCreatingItemForm(@Nonnull CreatingItemForm creatingItemForm) {
		if (creatingItemForm.getItemName() == null) {
			return "The item name is not specified";
		}
		if (creatingItemForm.getItemName().isBlank()) {
			return "The item name is blank";
		}

		if (creatingItemForm.getPrice() == null) {
			return "The price is not specified";
		}
		if (creatingItemForm.getPrice().centAmount() < 0 || creatingItemForm.getPrice().currencyAmount() < 0) {
			return "The price can't be negative";
		}

		if (creatingItemForm.getAmount() == null) {
			return "The amount is not specified";
		}

		return null;
	}
}
