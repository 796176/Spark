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
import org.example.spark.gateway.web.models.LogInForm;
import org.example.spark.gateway.web.models.SignInForm;

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
}
